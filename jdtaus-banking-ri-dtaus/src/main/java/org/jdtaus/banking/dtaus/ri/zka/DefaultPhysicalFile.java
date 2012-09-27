/*
 *  jDTAUS Banking RI DTAUS
 *  Copyright (C) 2005 Christian Schulte
 *  <cs@schulte.it>
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package org.jdtaus.banking.dtaus.ri.zka;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.jdtaus.banking.dtaus.Checksum;
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.LogicalFile;
import org.jdtaus.banking.dtaus.PhysicalFile;
import org.jdtaus.banking.dtaus.PhysicalFileFactory;
import org.jdtaus.banking.dtaus.spi.HeaderValidator;
import org.jdtaus.banking.dtaus.spi.IllegalHeaderException;
import org.jdtaus.banking.messages.AnalysesFileMessage;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.io.FileOperations;
import org.jdtaus.core.monitor.spi.Task;
import org.jdtaus.core.monitor.spi.TaskMonitor;

/**
 * Default {@code PhysicalFile} implementation.
 * <p><b>Note:</b><br/>
 * This implementation is not thread-safe.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public final class DefaultPhysicalFile implements PhysicalFile
{

    /** Index der logischen Dateien. */
    private AbstractLogicalFile[] index;

    /** Anzahl vorhandener logischer Dateien. */
    private int dtausCount = 0;

    /** Mapping of attribute names to theire values. */
    private final java.util.Properties properties;

    /** <code>FileOperations</code> requirement. **/
    private FileOperations fileOperations;

    /** Format of this instance. */
    private final int format;

    /**
     * Creates a new {@code DefaultPhysicalFile} instance.
     *
     * @param format The format of the new instance.
     * @param fileOperations The {@code FileOperations} implementation to operate on.
     * @param properties Configuration properties.
     *
     * @throws NullPointerException if either {@code fileOperations} or {@code properties} is {@code null}.
     * @throws IllegalArgumentException if {@code format} is not equal to {@code FORMAT_DISK} and {@code FORMAT_TAPE}.
     * @throws IOException wenn nicht gelesen werden kann.
     *
     * @see PhysicalFileFactory#FORMAT_DISK
     * @see PhysicalFileFactory#FORMAT_TAPE
     */
    public DefaultPhysicalFile(
        final int format, final FileOperations fileOperations, final java.util.Properties properties )
        throws IOException
    {
        super();

        if ( fileOperations == null )
        {
            throw new NullPointerException( "fileOperations" );
        }
        if ( properties == null )
        {
            throw new NullPointerException( "properties" );
        }
        if ( format != PhysicalFileFactory.FORMAT_DISK && format != PhysicalFileFactory.FORMAT_TAPE )
        {
            throw new IllegalArgumentException( Integer.toString( format ) );
        }

        this.properties = properties;
        this.fileOperations = fileOperations;
        this.format = format;
        this.checksum();
    }

    public int count()
    {
        return this.dtausCount;
    }

    public LogicalFile add( final Header header ) throws IOException
    {
        if ( header == null )
        {
            throw new NullPointerException( "header" );
        }

        IllegalHeaderException result = null;
        final HeaderValidator[] validators = this.getHeaderValidator();

        for ( int i = validators.length - 1; i >= 0; i-- )
        {
            result = validators[i].assertValidHeader( header, result );
        }

        if ( result != null && result.getMessages().length > 0 )
        {
            throw result;
        }

        this.resizeIndex( this.dtausCount );

        final AbstractLogicalFile lFile = this.newLogicalFile(
            ( this.dtausCount == 0 ? 0L : this.index[this.dtausCount - 1].getChecksumPosition() +
                                          this.index[this.dtausCount - 1].getBlockSize() ) );

        lFile.insertBytes( lFile.getHeaderPosition(), this.format * 2 );
        lFile.writeHeader( header );
        lFile.writeChecksum( new Checksum() );
        lFile.checksum();
        this.index[this.dtausCount] = lFile;
        return this.index[this.dtausCount++];
    }

    public LogicalFile get( int dtausId )
    {
        if ( !this.checkLogicalFileExists( dtausId ) )
        {
            throw new IllegalArgumentException( "dtausId" );
        }
        return this.index[dtausId];
    }

    public void remove( int dtausId ) throws IOException
    {
        if ( !this.checkLogicalFileExists( dtausId ) )
        {
            throw new IllegalArgumentException( "dtausId" );
        }

        this.index[dtausId].removeBytes(
            this.index[dtausId].getHeaderPosition(), this.index[dtausId].getChecksumPosition() -
                                                     this.index[dtausId].getHeaderPosition() + this.format );

        System.arraycopy( this.index, dtausId + 1, this.index, dtausId, --this.dtausCount - dtausId );
    }

    public void commit() throws IOException
    {
        this.getFileOperations().close();
    }

    public int getLogicalFileCount() throws IOException
    {
        return this.count();
    }

    public LogicalFile addLogicalFile( final Header header ) throws IOException
    {
        return this.add( header );
    }

    public LogicalFile getLogicalFile( final int index ) throws IOException
    {
        return this.get( index );
    }

    public void removeLogicalFile( final int index ) throws IOException
    {
        this.remove( index );
    }

    /** FileOperations requirement getter method. */
    private FileOperations getFileOperations()
    {
        return this.fileOperations;
    }

    private boolean checkLogicalFileExists( int dtausId )
    {
        return dtausId < this.dtausCount && dtausId >= 0;
    }

    private void checksum() throws IOException
    {
        this.dtausCount = 0;
        int dtausIndex = 0;
        final long length = this.getFileOperations().getLength();
        long maximumProgress = length;
        long progressDivisor = 1L;

        while ( maximumProgress > Integer.MAX_VALUE )
        {
            maximumProgress /= 2L;
            progressDivisor *= 2L;
        }

        final Task task = new Task();
        task.setIndeterminate( false );
        task.setCancelable( false );
        task.setDescription( new AnalysesFileMessage() );
        task.setMinimum( 0 );
        task.setProgress( 0 );
        task.setMaximum( (int) maximumProgress );

        try
        {
            this.getTaskMonitor().monitor( task );

            for ( long position = 0L; position < length;
                  position = this.index[dtausIndex].getChecksumPosition() + this.index[dtausIndex++].getBlockSize() )
            {
                task.setProgress( (int) ( position / progressDivisor ) );
                this.resizeIndex( dtausIndex );
                this.index[dtausIndex] = this.newLogicalFile( position );
                this.index[dtausIndex].checksum();
                this.dtausCount++;
            }
        }
        finally
        {
            this.getTaskMonitor().finish( task );
        }
    }

    private AbstractLogicalFile newLogicalFile( final long headerPosition ) throws IOException
    {
        final AbstractLogicalFile ret;

        switch ( this.format )
        {
            case PhysicalFileFactory.FORMAT_DISK:
                ret = new DTAUSDisk();
                break;
            case PhysicalFileFactory.FORMAT_TAPE:
                ret = new DTAUSTape();
                break;
            default:
                throw new IllegalStateException();

        }

        ret.setFileOperations( this.getFileOperations() );
        ret.setHeaderPosition( headerPosition );
        ret.setChecksumPosition( headerPosition + this.format );

        for ( Iterator it = this.properties.entrySet().iterator(); it.hasNext(); )
        {
            final Map.Entry e = (Map.Entry) it.next();
            final String key = (String) e.getKey();

            if ( key.startsWith( DefaultPhysicalFileFactory.ATTRIBUTE_SPACE_CHARACTERS_ALLOWED ) )
            {
                int field = Integer.parseInt( key.substring( key.lastIndexOf( '.' ) + 1 ), 16 );
                final boolean allowed =
                    e.getValue() != null && Boolean.valueOf( e.getValue().toString() ).booleanValue();

                ret.getConfiguration().setSpaceCharacterAllowed( field, allowed );
            }
        }

        ret.addListener( new AbstractLogicalFile.Listener()
        {

            public void bytesInserted( final long position, final long bytes ) throws IOException
            {
                final int fileIndex = this.getFileIndex( position );
                if ( fileIndex >= 0 )
                {
                    // Increment properties headerPosition and checksumPosition for all remaining files.
                    for ( int i = fileIndex + 1; i < dtausCount; i++ )
                    {
                        index[i].setHeaderPosition( index[i].getHeaderPosition() + bytes );
                        index[i].setChecksumPosition( index[i].getChecksumPosition() + bytes );
                    }
                }
            }

            public void bytesDeleted( final long position, final long bytes ) throws IOException
            {
                final int fileIndex = this.getFileIndex( position );
                if ( fileIndex >= 0 )
                {
                    // Decrement properties headerPosition and checksumPosition for all remaining files.
                    for ( int i = fileIndex + 1; i < dtausCount; i++ )
                    {
                        index[i].setHeaderPosition( index[i].getHeaderPosition() - bytes );
                        index[i].setChecksumPosition( index[i].getChecksumPosition() - bytes );
                    }
                }
            }

            private int getFileIndex( final long position )
            {
                for ( int i = dtausCount - 1; i >= 0; i-- )
                {
                    if ( position >= index[i].getHeaderPosition() && position <= index[i].getChecksumPosition() )
                    {
                        return i;
                    }
                }

                return -1;
            }

        } );

        return ret;
    }

    private void resizeIndex( int index )
    {
        if ( this.index == null )
        {
            this.index = new AbstractLogicalFile[ index + 1 ];
        }
        else if ( this.index.length < index + 1 )
        {
            while ( this.index.length < index + 1 )
            {
                final int newLength = this.index.length * 2;
                final AbstractLogicalFile[] newIndex = new AbstractLogicalFile[ newLength ];
                System.arraycopy( this.index, 0, newIndex, 0, this.index.length );
                this.index = newIndex;
            }
        }
    }

    //--Dependencies------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the configured <code>TaskMonitor</code> implementation.
     *
     * @return The configured <code>TaskMonitor</code> implementation.
     */
    private TaskMonitor getTaskMonitor()
    {
        return (TaskMonitor) ContainerFactory.getContainer().
            getDependency( this, "TaskMonitor" );

    }

    /**
     * Gets the configured <code>HeaderValidator</code> implementation.
     *
     * @return The configured <code>HeaderValidator</code> implementation.
     */
    private HeaderValidator[] getHeaderValidator()
    {
        return (HeaderValidator[]) ContainerFactory.getContainer().
            getDependency( this, "HeaderValidator" );

    }

// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
}

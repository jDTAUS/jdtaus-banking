/*
 *  jDTAUS Banking RI DTAUS
 *  Copyright (c) 2005 Christian Schulte
 *
 *  Christian Schulte, Haldener Strasse 72, 58095 Hagen, Germany
 *  <cs@jdtaus.org> (+49 2331 3543887)
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
import org.jdtaus.banking.dtaus.spi.HeaderValidator;
import org.jdtaus.banking.dtaus.spi.IllegalHeaderException;
import org.jdtaus.banking.messages.AnalysesFileMessage;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.ContextFactory;
import org.jdtaus.core.container.ContextInitializer;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.container.Properties;
import org.jdtaus.core.container.Property;
import org.jdtaus.core.container.Specification;
import org.jdtaus.core.io.StructuredFileListener;
import org.jdtaus.core.io.util.StructuredFileOperations;
import org.jdtaus.core.monitor.spi.Task;
import org.jdtaus.core.monitor.spi.TaskMonitor;

/**
 * Default {@code PhysicalFile}-Implementierung.
 * <p/>
 * <b>Hinweis:</b><br/>
 * Implementierung darf niemals von mehreren Threads gleichzeitig verwendet
 * werden.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class DefaultPhysicalFile implements PhysicalFile
{
    //--Attribute---------------------------------------------------------------

    /** Index der logischen Dateien. */
    private AbstractLogicalFile[] index;

    /** Anzahl vorhandener logischer Dateien. */
    private int dtausCount = 0;

    /** Mapping of attribute names to theire values. */
    private final java.util.Properties properties;

    /** <code>StructuredFile</code> requirement. **/
    private StructuredFileOperations structuredFile;

    //---------------------------------------------------------------Attribute--
    //--Implementation----------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausImplementation
    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(DefaultPhysicalFile.class.getName());
// </editor-fold>//GEN-END:jdtausImplementation

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausConstructors
    // This section is managed by jdtaus-container-mojo.

    /**
     * Initializes the properties of the instance.
     *
     * @param meta the property values to initialize the instance with.
     *
     * @throws NullPointerException if {@code meta} is {@code null}.
     */
    private void initializeProperties(final Properties meta)
    {
        Property p;

        if(meta == null)
        {
            throw new NullPointerException("meta");
        }

    }
// </editor-fold>//GEN-END:jdtausConstructors

    //------------------------------------------------------------Constructors--
    //--Konstruktoren-----------------------------------------------------------

    /**
     * Creates a new {@code DefaultPhysicalFile} instance.
     * <p>Registers a {@code StructuredFileListener} with the given
     * {@code structuredFile} and then checksums the contents of the file.</p>
     *
     * @param structuredFile the {@code StructuredFile} implementation to
     * operate on.
     * @param properties configuration properties.
     *
     * @throws NullPointerException if either {@code structuredFile} or
     * {@code properties} is {@code null}
     * @throws IOException wenn nicht gelesen werden kann.
     */
    public DefaultPhysicalFile(
        final StructuredFileOperations structuredFile,
        final java.util.Properties properties ) throws IOException
    {
        super();
        this.initializeProperties( META.getProperties() );

        if ( structuredFile == null )
        {
            throw new NullPointerException( "structuredFile" );
        }
        if ( properties == null )
        {
            throw new NullPointerException( "properties" );
        }

        this.properties = properties;
        this.structuredFile = structuredFile;
        this.structuredFile.addStructuredFileListener(
            new StructuredFileListener()
            {

                public void blocksInserted( long l, long l0 ) throws IOException
                {
                    final int fileIndex;

                    if ( ( fileIndex = this.getFileIndex( l ) ) >= 0 )
                    {
                        // Increment properties headerBlock and checksumBlock
                        // for all remaining files.
                        for ( int i = fileIndex + 1; i < dtausCount; i++ )
                        {
                            index[i].setHeaderBlock( index[i].getHeaderBlock() +
                                                     l0 );
                            index[i].setChecksumBlock(
                                index[i].getChecksumBlock() + l0 );

                        }
                    }
                }

                public void blocksDeleted( long l, long l0 ) throws IOException
                {
                    final int fileIndex;

                    if ( ( fileIndex = this.getFileIndex( l ) ) >= 0 )
                    {
                        // Decrement properties headerBlock and checksumBlock
                        // for all remaining files.
                        for ( int i = fileIndex + 1; i < dtausCount; i++ )
                        {
                            index[i].setHeaderBlock( index[i].getHeaderBlock() -
                                                     l0 );
                            index[i].setChecksumBlock(
                                index[i].getChecksumBlock() - l0 );

                        }
                    }
                }

                private int getFileIndex( final long block )
                {
                    int i;
                    for ( i = dtausCount - 1; i >= 0; i-- )
                    {
                        if ( block >= index[i].getHeaderBlock() &&
                            block <= index[i].getChecksumBlock() )
                        {
                            break;
                        }
                    }

                    return i;
                }

            } );

        this.checksum();
    }

    //-----------------------------------------------------------Konstruktoren--
    //--Dependencies------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
    // This section is managed by jdtaus-container-mojo.

    /** Configured <code>TaskMonitor</code> implementation. */
    private transient TaskMonitor dTaskMonitor;

    /**
     * Gets the configured <code>TaskMonitor</code> implementation.
     *
     * @return the configured <code>TaskMonitor</code> implementation.
     */
    private TaskMonitor getTaskMonitor()
    {
        TaskMonitor ret = null;
        if(this.dTaskMonitor != null)
        {
            ret = this.dTaskMonitor;
        }
        else
        {
            ret = (TaskMonitor) ContainerFactory.getContainer().
                getDependency(DefaultPhysicalFile.class,
                "TaskMonitor");

            if(ModelFactory.getModel().getModules().
                getImplementation(DefaultPhysicalFile.class.getName()).
                getDependencies().getDependency("TaskMonitor").
                isBound())
            {
                this.dTaskMonitor = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext()))
        {
            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }
// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
    //--PhysicalFile------------------------------------------------------------

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

        HeaderValidator validator = null;
        IllegalHeaderException result = null;
        final Specification validatorSpec = ModelFactory.getModel().
            getModules().getSpecification( HeaderValidator.class.getName() );

        for ( int i = validatorSpec.getImplementations().
            getImplementations().length - 1; i >= 0; i-- )
        {
            validator = ( HeaderValidator ) ContainerFactory.getContainer().
                getImplementation( HeaderValidator.class,
                                   validatorSpec.getImplementations().
                                   getImplementation( i ).
                                   getName() );

            result = validator.assertValidHeader( header, result );
        }

        if ( result != null && result.getMessages().length > 0 )
        {
            throw result;
        }

        final AbstractLogicalFile lFile = this.newLogicalFile(
            this.dtausCount == 0
            ? 0L
            : this.index[this.dtausCount - 1].getChecksumBlock() + 1L );

        this.getStructuredFile().insertBlocks(
            this.dtausCount == 0
            ? 0L
            : this.index[this.dtausCount - 1].getChecksumBlock() + 1L, 2L );

        this.resizeIndex( this.dtausCount );
        this.index[this.dtausCount] = lFile;
        lFile.writeHeader( this.index[this.dtausCount].getHeaderBlock(),
                           header );

        lFile.writeChecksum( this.index[this.dtausCount].getHeaderBlock() + 1L,
                             new Checksum() );

        this.index[this.dtausCount].checksum();
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
        this.getStructuredFile().deleteBlocks(
            this.index[dtausId].getHeaderBlock(),
            this.index[dtausId].getChecksumBlock() -
            this.index[dtausId].getHeaderBlock() + 1 );

        System.arraycopy( this.index, dtausId + 1, this.index, dtausId,
                          --this.dtausCount - dtausId );

    }

    public void commit() throws IOException
    {
        this.getStructuredFile().close();
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

    //------------------------------------------------------------PhysicalFile--
    //--DefaultPhysicalFile-----------------------------------------------------

    /** StructuredFileOperations requirement getter method. */
    private StructuredFileOperations getStructuredFile()
    {
        return this.structuredFile;
    }

    private boolean checkLogicalFileExists( int dtausId )
    {
        return dtausId < this.dtausCount && dtausId >= 0;
    }

    private void checksum() throws IOException
    {
        this.dtausCount = 0;
        int dtausIndex = 0;
        final long blockCount = this.getStructuredFile().getBlockCount();

        long maximumProgress = blockCount;
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
        task.setMaximum( ( int ) maximumProgress );

        try
        {
            this.getTaskMonitor().monitor( task );

            for ( long block = 0L; block < blockCount;)
            {
                task.setProgress( ( int ) ( block / progressDivisor ) );

                this.resizeIndex( dtausIndex );
                this.index[dtausIndex] = this.newLogicalFile( block );
                this.index[dtausIndex].checksum();
                block = this.index[dtausIndex++].getChecksumBlock() + 1L;
                this.dtausCount++;
            }
        }
        finally
        {
            this.getTaskMonitor().finish( task );
        }
    }

    private AbstractLogicalFile newLogicalFile(
        final long headerBlock ) throws IOException
    {
        final AbstractLogicalFile ret;

        switch ( this.getStructuredFile().getBlockSize() )
        {
            case 128:
                ret = new DTAUSDisk( headerBlock, this.getStructuredFile() );
                break;
            case 150:
                ret = new DTAUSTape( headerBlock, this.getStructuredFile() );
                break;
            default:
                throw new IllegalStateException();

        }

        for ( Iterator it = this.properties.entrySet().iterator();
            it.hasNext();)
        {
            final Map.Entry e = ( Map.Entry ) it.next();
            final String key = ( String ) e.getKey();

            if ( key.startsWith(
                DefaultPhysicalFileFactory.ATTRIBUTE_SPACE_CHARACTERS_ALLOWED ) )
            {
                int field =
                    Integer.parseInt( key.substring( key.lastIndexOf( '.' ) + 1 ),
                                      16 );

                final boolean allowed = e.getValue() != null &&
                    Boolean.valueOf( e.getValue().toString() ).booleanValue();

                ret.getConfiguration().setSpaceCharacterAllowed(
                    field, allowed );

            }
        }

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
                final AbstractLogicalFile[] newIndex =
                    new AbstractLogicalFile[ newLength ];

                System.arraycopy( this.index, 0, newIndex, 0,
                                  this.index.length );

                this.index = newIndex;
            }
        }
    }

    //-----------------------------------------------------DefaultPhysicalFile--
}

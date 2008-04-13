/*
 *  jDTAUS - DTAUS fileformat.
 *  Copyright (c) 2005 Christian Schulte <cs@schulte.it>
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

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.jdtaus.banking.dtaus.CorruptedException;
import org.jdtaus.banking.dtaus.PhysicalFile;
import org.jdtaus.banking.dtaus.PhysicalFileException;
import org.jdtaus.banking.dtaus.PhysicalFileFactory;
import org.jdtaus.banking.dtaus.spi.Fields;
import org.jdtaus.banking.messages.IllegalDataMessage;
import org.jdtaus.banking.messages.IllegalFileLengthMessage;
import org.jdtaus.core.container.ContainerInitializer;
import org.jdtaus.core.container.Dependency;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.container.Properties;
import org.jdtaus.core.container.Property;
import org.jdtaus.core.container.PropertyException;
import org.jdtaus.core.io.FileOperations;
import org.jdtaus.core.io.util.CoalescingFileOperations;
import org.jdtaus.core.io.util.RandomAccessFileOperations;
import org.jdtaus.core.io.util.ReadAheadFileOperations;
import org.jdtaus.core.io.util.StructuredFileOperations;
import org.jdtaus.core.nio.util.Charsets;
import org.jdtaus.core.text.Message;

/**
 * Default {@code PhysicalFileFactory}-Implementierung.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 *
 * @see #initialize()
 */
public final class DefaultPhysicalFileFactory
    implements PhysicalFileFactory, ContainerInitializer
{
    //--Implementation----------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausImplementation
    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(DefaultPhysicalFileFactory.class.getName());
// </editor-fold>//GEN-END:jdtausImplementation

    //----------------------------------------------------------Implementation--
    //--Constants---------------------------------------------------------------

    /**
     * Constant for the name of attribute {@code readAheadCaching}.
     * <p>
     * The {@code readAheadCaching} attribute is used to enabled or disable the
     * use of a read-ahead caching algorithm. Its expected value is of type
     * {@code Boolean}.
     * </p>
     */
    public static final String ATTRIBUTE_READAHEAD_CACHING =
        DefaultPhysicalFileFactory.class.getName() + ".readAheadCaching";

    /**
     * Constant for the name of attribute {@code readAheadCacheSize}.
     * <p>
     * The {@code readAheadCacheSize} attribute is used to specify the
     * size of the read-ahead cache. Its expected value is of type
     * {@code Integer}.
     * </p>
     */
    public static final String ATTRIBUTE_READAHEAD_CACHESIZE =
        DefaultPhysicalFileFactory.class.getName() + ".readAheadCacheSize";

    /**
     * Constant for the name of attribute {@code coalescingCaching}.
     * <p>
     * The {@code coalescingCaching} attribute is used to enabled or disable the
     * use of a coalescing caching algorithm. Its expected value is of type
     * {@code Boolean}.
     * </p>
     */
    public static final String ATTRIBUTE_COALESCING_CACHING =
        DefaultPhysicalFileFactory.class.getName() + ".coalescingCaching";

    /**
     * Constant for the name of attribute {@code coalescingBlockSize}.
     * <p>
     * The {@code coalescingBlockSize} attribute is used to specify the
     * value of property {@code blockSize} to use when constructing the
     * coalescing cache implementation. Its expected value is of type
     * {@code Integer}.
     * </p>
     */
    public static final String ATTRIBUTE_COALESCING_BLOCKSIZE =
        DefaultPhysicalFileFactory.class.getName() + ".coalescingBlockSize";

    /**
     * Constant for the name of attribute {@code spaceCharactersAllowed}.
     * <p>
     * The {@code spaceCharactersAllowed} attribute is used to specify numeric
     * fields for which space characters are to be allowed. It is used as
     * a prefix with the hexadecimal field constant appended. Its expected value
     * is of type {@code Boolean}.
     * </p>
     */
    public static final String ATTRIBUTE_SPACE_CHARACTERS_ALLOWED =
        DefaultPhysicalFileFactory.class.getName() + ".spaceCharactersAllowed.";

    //---------------------------------------------------------------Constants--
    //--Constructors------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausConstructors
    // This section is managed by jdtaus-container-mojo.

    /**
     * <code>DefaultPhysicalFileFactory</code> implementation constructor.
     *
     * @param meta Implementation meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    private DefaultPhysicalFileFactory(final Implementation meta)
    {
        super();
        if(meta == null)
        {
            throw new NullPointerException("meta");
        }
        this.initializeProperties(meta.getProperties());
    }
    /**
     * <code>DefaultPhysicalFileFactory</code> dependency constructor.
     *
     * @param meta dependency meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    private DefaultPhysicalFileFactory(final Dependency meta)
    {
        super();
        if(meta == null)
        {
            throw new NullPointerException("meta");
        }
        this.initializeProperties(meta.getProperties());
    }

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

        p = meta.getProperty("defaultFormat");
        this.pDefaultFormat = ((java.lang.Integer) p.getValue()).intValue();

    }
// </editor-fold>//GEN-END:jdtausConstructors

    //------------------------------------------------------------Constructors--
    //--ContainerInitializer----------------------------------------------------

    /**
     * Checks configured properties.
     *
     * @see #assertValidProperties()
     */
    public void initialize()
    {
        this.assertValidProperties();
    }

    //----------------------------------------------------ContainerInitializer--
    //--Dependencies------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
    // This section is managed by jdtaus-container-mojo.

// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
    //--Properties--------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausProperties
    // This section is managed by jdtaus-container-mojo.

    /**
     * Property {@code defaultFormat}.
     * @serial
     */
    private int pDefaultFormat;

    /**
     * Gets the value of property <code>defaultFormat</code>.
     *
     * @return the value of property <code>defaultFormat</code>.
     */
    private int getDefaultFormat()
    {
        return this.pDefaultFormat;
    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
    //--PhysicalFileFactory-----------------------------------------------------

    public int analyse( final File file )
        throws PhysicalFileException, IOException
    {
        if ( file == null )
        {
            throw new NullPointerException( "file" );
        }

        final FileOperations ops = new RandomAccessFileOperations(
            new RandomAccessFile( file, "r" ) );

        final int format = this.analyse( ops );

        ops.close();

        return format;
    }

    public int analyse( final FileOperations fileOperations )
        throws PhysicalFileException, IOException
    {
        int blockSize = 128;
        long remainder = 0;
        int read = 0;
        int ret = FORMAT_DISK;
        int size = 0x000000FF;
        int total = 0;
        Message msg;

        final Message[] messages;
        final byte[] buf = new byte[ 4 ];
        final String str;
        final long length;

        if ( fileOperations == null )
        {
            throw new NullPointerException( "fileOperations" );
        }

        length = fileOperations.getLength();
        try
        {
            ThreadLocalMessages.getMessages().clear();
            AbstractErrorMessage.setErrorsEnabled( false );

            if ( length >= 128 )
            { // mindestens ein Disketten-Satzabschnitt.
                // die ersten 4 Byte lesen.
                fileOperations.setFilePointer( 0L );
                do
                {
                    read = fileOperations.read( buf, total,
                                                buf.length - total );

                    if ( read == FileOperations.EOF )
                    {
                        throw new EOFException();
                    }
                    else
                    {
                        total += read;
                    }
                }
                while ( total < buf.length );

                // Diskettenformat prüfen "0128".
                str = Charsets.decode( buf, "ISO646-DE" );
                if ( "0128".equals( str ) )
                {
                    remainder = length % blockSize;
                }
                else
                {
                    size &= buf[0];
                    size <<= 8;
                    size |= 0xFF;
                    size &= buf[1];

                    if ( size == 150 )
                    {
                        ret = FORMAT_TAPE;
                        blockSize = 150;
                        remainder = length % blockSize;
                    }
                    else
                    {
                        msg =
                            new IllegalDataMessage(
                            Fields.FIELD_A1, IllegalDataMessage.TYPE_CONSTANT,
                            0L, str );

                        if ( AbstractErrorMessage.isErrorsEnabled() )
                        {
                            throw new CorruptedException( META, 0L );
                        }
                        else
                        {
                            ThreadLocalMessages.getMessages().addMessage( msg );
                        }
                    }
                }
            }
            else
            {
                msg = new IllegalFileLengthMessage( length, blockSize );
                if ( AbstractErrorMessage.isErrorsEnabled() )
                {
                    throw new CorruptedException( META, length );
                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }

            if ( remainder > 0 )
            {
                msg = new IllegalFileLengthMessage( length, blockSize );
                if ( AbstractErrorMessage.isErrorsEnabled() )
                {
                    throw new CorruptedException( META, length );
                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }

            messages = ThreadLocalMessages.getMessages().getMessages();
            if ( messages.length > 0 )
            {
                throw new PhysicalFileException( messages );
            }

            return ret;
        }
        finally
        {
            AbstractErrorMessage.setErrorsEnabled( true );
        }
    }

    public PhysicalFile createPhysicalFile( final File file, final int format )
        throws IOException
    {
        return this.createPhysicalFile( file, format,
                                        this.getDefaultProperties() );

    }

    public PhysicalFile createPhysicalFile(
        final File file, final int format,
        final java.util.Properties properties ) throws IOException
    {
        if ( file == null )
        {
            throw new NullPointerException( "file" );
        }

        this.assertValidProperties( properties );

        FileOperations ops = new RandomAccessFileOperations(
            new RandomAccessFile( file, "rw" ) );

        ops = this.configureCoalescingCaching( ops, properties );

        return this.createPhysicalFile( ops, format, properties );
    }

    public PhysicalFile createPhysicalFile(
        final FileOperations ops, final int format ) throws IOException
    {
        return this.createPhysicalFile( ops, format,
                                        this.getDefaultProperties() );

    }

    public PhysicalFile createPhysicalFile(
        FileOperations ops, final int format,
        final java.util.Properties properties ) throws IOException
    {
        if ( ops == null )
        {
            throw new NullPointerException( "ops" );
        }
        if ( format != FORMAT_DISK && format != FORMAT_TAPE )
        {
            throw new IllegalArgumentException( Integer.toString( format ) );
        }

        this.assertValidProperties( properties );

        try
        {
            ops.setLength( 0L );
            return this.getPhysicalFile( ops, format, properties );
        }
        catch ( PhysicalFileException e )
        {
            throw new AssertionError( e );
        }
    }

    public PhysicalFile getPhysicalFile( final File file )
        throws PhysicalFileException, IOException
    {
        return this.getPhysicalFile( file, this.getDefaultProperties() );
    }

    public PhysicalFile getPhysicalFile( final FileOperations ops )
        throws PhysicalFileException, IOException
    {
        return this.getPhysicalFile( ops, this.getDefaultProperties() );
    }

    public PhysicalFile getPhysicalFile(
        final FileOperations ops, final java.util.Properties properties )
        throws PhysicalFileException, IOException
    {
        if ( ops == null )
        {
            throw new NullPointerException( "ops" );
        }

        this.assertValidProperties( properties );

        return this.getPhysicalFile( ops, this.getDefaultFormat(), properties );
    }

    public PhysicalFile getPhysicalFile(
        final File file, final java.util.Properties properties )
        throws PhysicalFileException, IOException
    {
        if ( file == null )
        {
            throw new NullPointerException( "file" );
        }

        this.assertValidProperties( properties );

        FileOperations ops = new RandomAccessFileOperations(
            new RandomAccessFile( file, "rw" ) );

        ops = this.configureReadAheadCaching( ops, properties );

        return this.getPhysicalFile( ops, properties );
    }

    //-----------------------------------------------------PhysicalFileFactory--
    //--DefaultPhysicalFileFactory----------------------------------------------

    /** Creates a new {@code DefaultPhysicalFileFactory} instance. */
    public DefaultPhysicalFileFactory()
    {
        this( META );
        this.initialize();
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException for illegal property values.
     */
    private void assertValidProperties()
    {
        final int defaultFormat = this.getDefaultFormat();
        if ( defaultFormat != FORMAT_DISK &&
            defaultFormat != FORMAT_TAPE )
        {
            throw new PropertyException( "defaultFormat",
                                         new Integer( defaultFormat ) );

        }
    }

    /**
     * Checks that given properties are valid.
     *
     * @param properties the properties to check.
     *
     * @throws NullPointerException if {@code properties} is {@code null}.
     * @throws IllegalArgumentException if {@code properties} holds invalid
     * values.
     */
    private void assertValidProperties( final java.util.Properties properties )
    {
        if ( properties == null )
        {
            throw new NullPointerException( "properties" );
        }

        for ( Iterator it = properties.entrySet().iterator(); it.hasNext();)
        {
            final Map.Entry entry = ( Map.Entry ) it.next();
            final String name = ( String ) entry.getKey();
            final String value = ( String ) entry.getValue();

            if ( name.startsWith( ATTRIBUTE_SPACE_CHARACTERS_ALLOWED ) )
            {
                try
                {
                    Integer.parseInt( name.substring(
                                      name.lastIndexOf( '.' ) + 1 ), 16 );

                }
                catch ( NumberFormatException e )
                {
                    throw new IllegalArgumentException( name + ": " +
                                                        e.getMessage() );

                }
            }

            if ( value != null &&
                ( ATTRIBUTE_READAHEAD_CACHESIZE.equals( name ) ||
                ATTRIBUTE_COALESCING_BLOCKSIZE.equals( name ) ) )
            {
                try
                {
                    Integer.parseInt( value );
                }
                catch ( NumberFormatException e )
                {
                    throw new IllegalArgumentException(
                        DefaultPhysicalFileFactoryBundle.getInstance().
                        getIllegalAttributeTypeMessage( Locale.getDefault() ).
                        format( new Object[] { name, value != null
                                               ? value.getClass().getName()
                                               : null, Integer.class.getName()
                            } ) );
                }
            }
        }
    }

    private java.util.Properties getDefaultProperties()
    {
        final java.util.Properties properties = new java.util.Properties();
        properties.setProperty( ATTRIBUTE_READAHEAD_CACHING,
                                Boolean.toString( true ) );

        properties.setProperty( ATTRIBUTE_COALESCING_CACHING,
                                Boolean.toString( true ) );

        return properties;
    }

    private FileOperations configureReadAheadCaching(
        FileOperations ops, final java.util.Properties properties )
        throws IOException
    {
        final String readAheadCaching =
            properties.getProperty( ATTRIBUTE_READAHEAD_CACHING );

        final String readAheadCacheSize =
            properties.getProperty( ATTRIBUTE_READAHEAD_CACHESIZE );

        final boolean isReadAheadCaching = readAheadCaching != null &&
            Boolean.valueOf( readAheadCaching ).booleanValue();

        if ( isReadAheadCaching )
        {
            if ( readAheadCacheSize != null )
            {
                ops = new ReadAheadFileOperations(
                    ops, Integer.parseInt( readAheadCacheSize ) );

            }
            else
            {
                ops = new ReadAheadFileOperations( ops );
            }
        }

        return ops;
    }

    private FileOperations configureCoalescingCaching(
        FileOperations ops, final java.util.Properties properties )
        throws IOException
    {
        final String coalescingCaching =
            properties.getProperty( ATTRIBUTE_COALESCING_CACHING );

        final String coalescingBlockSize =
            properties.getProperty( ATTRIBUTE_COALESCING_BLOCKSIZE );

        final boolean isCoalescingCaching = coalescingCaching != null &&
            Boolean.valueOf( coalescingCaching ).booleanValue();

        if ( isCoalescingCaching )
        {
            if ( coalescingBlockSize != null )
            {
                ops = new CoalescingFileOperations(
                    ops, Integer.parseInt( coalescingBlockSize ) );

            }
            else
            {
                ops = new CoalescingFileOperations( ops );
            }
        }

        return ops;
    }

    private PhysicalFile getPhysicalFile(
        final FileOperations ops, int format,
        final java.util.Properties properties )
        throws PhysicalFileException, IOException
    {
        if ( ops == null )
        {
            throw new NullPointerException( "ops" );
        }
        if ( format != FORMAT_DISK && format != FORMAT_TAPE )
        {
            throw new IllegalArgumentException( Integer.toString( format ) );
        }

        this.assertValidProperties( properties );

        final DefaultPhysicalFile ret;
        final Message[] messages;
        final StructuredFileOperations sops;
        format = ops.getLength() > 0
            ? this.analyse( ops )
            : format;

        switch ( format )
        {
            case FORMAT_DISK:
                sops = new StructuredFileOperations( FORMAT_DISK, ops );
                break;

            case FORMAT_TAPE:
                sops = new StructuredFileOperations( FORMAT_TAPE, ops );
                break;

            default:
                throw new IllegalStateException();

        }

        try
        {
            ThreadLocalMessages.getMessages().clear();
            AbstractErrorMessage.setErrorsEnabled( false );

            ret = new DefaultPhysicalFile( sops, properties );

            messages = ThreadLocalMessages.getMessages().getMessages();
            if ( messages.length > 0 )
            {
                throw new PhysicalFileException( messages );
            }

            return ret;
        }
        finally
        {
            AbstractErrorMessage.setErrorsEnabled( true );
        }
    }

    //----------------------------------------------DefaultPhysicalFileFactory--
}

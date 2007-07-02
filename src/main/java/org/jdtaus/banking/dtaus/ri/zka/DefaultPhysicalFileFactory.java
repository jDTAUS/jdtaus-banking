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
import java.io.IOException;
import org.jdtaus.banking.dtaus.CorruptedException;
import org.jdtaus.banking.dtaus.PhysicalFile;
import org.jdtaus.banking.dtaus.PhysicalFileException;
import org.jdtaus.banking.dtaus.PhysicalFileFactory;
import org.jdtaus.banking.dtaus.spi.Fields;
import org.jdtaus.banking.dtaus.ri.zka.messages.IllegalDataMessage;
import org.jdtaus.banking.dtaus.ri.zka.messages.IllegalFileLengthMessage;
import org.jdtaus.core.container.ContainerInitializer;
import org.jdtaus.core.container.Dependency;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.container.Properties;
import org.jdtaus.core.container.Property;
import org.jdtaus.core.container.PropertyException;
import org.jdtaus.core.io.FileOperations;
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

    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(DefaultPhysicalFileFactory.class.getName());

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /**
     * Protected <code>DefaultPhysicalFileFactory</code> implementation constructor.
     *
     * @param meta Implementation meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    protected DefaultPhysicalFileFactory(final Implementation meta)
    {
        super();
        if(meta == null)
        {
            throw new NullPointerException("meta");
        }
        this.initializeProperties(meta.getProperties());
    }
    /**
     * Protected <code>DefaultPhysicalFileFactory</code> dependency constructor.
     *
     * @param meta dependency meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    protected DefaultPhysicalFileFactory(final Dependency meta)
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
    protected void initializeProperties(final Properties meta)
    {
        Property p;

        if(meta == null)
        {
            throw new NullPointerException("meta");
        }

        p = meta.getProperty("defaultFormat");
        this._defaultFormat = ((java.lang.Integer) p.getValue()).intValue();

    }

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

    // This section is managed by jdtaus-container-mojo.


    //------------------------------------------------------------Dependencies--
    //--Properties--------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /**
     * Property {@code defaultFormat}.
     * @serial
     */
    private int _defaultFormat;

    /**
     * Gets the value of property <code>defaultFormat</code>.
     *
     * @return the value of property <code>defaultFormat</code>.
     */
    protected int getDefaultFormat()
    {
        return this._defaultFormat;
    }


    //--------------------------------------------------------------Properties--
    //--PhysicalFileFactory-----------------------------------------------------

    public PhysicalFile createPhysicalFile(final FileOperations ops,
        final int format) throws IOException
    {
        if(ops == null)
        {
            throw new NullPointerException("ops");
        }
        if(format != FORMAT_DISK && format != FORMAT_TAPE)
        {
            throw new IllegalArgumentException(Integer.toString(format));
        }

        try
        {
            ops.setLength(0L);
            return this.getPhysicalFile(ops, format);
        }
        catch(PhysicalFileException e)
        {
            throw new AssertionError(e);
        }
    }

    public int analyse(final FileOperations fileOperations) throws
        PhysicalFileException, IOException
    {
        int blockSize = 128;
        long remainder = 0;
        int read = 0;
        int ret = PhysicalFileFactory.FORMAT_DISK;
        int size = 0x000000FF;
        int total = 0;
        Message msg;

        final Message[] messages;
        final byte[] buf = new byte[4];
        final String str;
        final long length;

        if(fileOperations == null)
        {
            throw new NullPointerException("fileOperations");
        }

        length = fileOperations.getLength();
        try
        {
            ThreadLocalMessages.getMessages().clear();
            AbstractErrorMessage.setErrorsEnabled(false);

            if(length >= 128)
            { // mindestens ein Disketten-Satzabschnitt.
                // die ersten 4 Byte lesen.
                fileOperations.setFilePointer(0L);
                do
                {
                    read = fileOperations.read(buf, total, buf.length - total);
                    if(read == -1)
                    {
                        throw new EOFException();
                    }
                    else
                    {
                        total += read;
                    }
                } while(total < buf.length);

                // Diskettenformat prüfen "0128".
                str = Charsets.decode(buf, "ISO646-DE");
                if("0128".equals(str))
                {
                    remainder = length % blockSize;
                }
                else
                {
                    size &= buf[0];
                    size <<= 8;
                    size |= 0xFF;
                    size &= buf[1];

                    if(size == 150)
                    {
                        ret = PhysicalFileFactory.FORMAT_TAPE;
                        blockSize = 150;
                        remainder = length % blockSize;
                    }
                    else
                    {
                        msg = new IllegalDataMessage(Fields.FIELD_A1,
                            IllegalDataMessage.TYPE_CONSTANT, 0L, str);

                        if(AbstractErrorMessage.isErrorsEnabled())
                        {
                            throw new CorruptedException(META, 0L);
                        }
                        else
                        {
                            ThreadLocalMessages.getMessages().addMessage(msg);
                        }
                    }
                }
            }
            else
            {
                msg = new IllegalFileLengthMessage(length, blockSize);
                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(META, length);
                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }
            }

            if(remainder > 0)
            {
                msg = new IllegalFileLengthMessage(length, blockSize);
                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(META, length);
                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }
            }

            messages = ThreadLocalMessages.getMessages().getMessages();
            if(messages.length > 0)
            {
                throw new PhysicalFileException(messages);
            }

            return ret;
        }
        finally
        {
            AbstractErrorMessage.setErrorsEnabled(true);
        }
    }

    public PhysicalFile getPhysicalFile(final FileOperations ops) throws
        PhysicalFileException, IOException
    {
        return this.getPhysicalFile(ops, this.getDefaultFormat());
    }

    //-----------------------------------------------------PhysicalFileFactory--
    //--DefaultPhysicalFileFactory----------------------------------------------

    /** Creates a new {@code DefaultPhysicalFileFactory} instance. */
    public DefaultPhysicalFileFactory()
    {
        this(DefaultPhysicalFileFactory.META);
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
        if(defaultFormat != PhysicalFileFactory.FORMAT_DISK &&
            defaultFormat != PhysicalFileFactory.FORMAT_TAPE)
        {

            throw new PropertyException("defaultFormat",
                new Integer(defaultFormat));

        }
    }

    private PhysicalFile getPhysicalFile(final FileOperations ops,
        int format) throws PhysicalFileException, IOException
    {
        if(ops == null)
        {
            throw new NullPointerException("ops");
        }
        if(format != FORMAT_DISK && format != FORMAT_TAPE)
        {
            throw new IllegalArgumentException(Integer.toString(format));
        }

        final DefaultPhysicalFile ret;
        final Message[] messages;
        final StructuredFileOperations sops;
        format = ops.getLength() > 0 ? this.analyse(ops) : format;

        switch(format)
        {
            case PhysicalFileFactory.FORMAT_DISK:
                sops = new StructuredFileOperations(
                    PhysicalFileFactory.FORMAT_DISK, ops);

                break;
            case PhysicalFileFactory.FORMAT_TAPE:
                sops = new StructuredFileOperations(
                    PhysicalFileFactory.FORMAT_TAPE, ops);

                break;
            default:
                throw new IllegalStateException();

        }

        try
        {
            ThreadLocalMessages.getMessages().clear();
            AbstractErrorMessage.setErrorsEnabled(false);

            ret = new DefaultPhysicalFile(sops);
            ret.checksum();

            messages = ThreadLocalMessages.getMessages().getMessages();
            if(messages.length > 0)
            {
                throw new PhysicalFileException(messages);
            }

            return ret;
        }
        finally
        {
            AbstractErrorMessage.setErrorsEnabled(true);
        }
    }

    //----------------------------------------------DefaultPhysicalFileFactory--
}

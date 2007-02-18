/*
 *  jDTAUS - DTAUS fileformat.
 *  Copyright (C) 2005 - 2007 Christian Schulte <cs@schulte.it>
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
package org.jdtaus.banking.dtaus.spi.runtime;

import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import org.jdtaus.banking.dtaus.Fields;
import org.jdtaus.banking.dtaus.PhysicalFile;
import org.jdtaus.banking.dtaus.PhysicalFileException;
import org.jdtaus.banking.dtaus.PhysicalFileFactory;
import org.jdtaus.banking.dtaus.spi.AbstractErrorMessage;
import org.jdtaus.banking.dtaus.spi.ThreadLocalMessages;
import org.jdtaus.banking.dtaus.spi.runtime.messages.IllegalDataMessage;
import org.jdtaus.core.container.ContainerError;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.ContextFactory;
import org.jdtaus.core.container.ContextInitializer;
import org.jdtaus.core.container.Dependency;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.io.FileOperations;
import org.jdtaus.core.io.IOError;
import org.jdtaus.core.io.spi.StructuredFileOperations;
import org.jdtaus.core.logging.spi.Logger;
import org.jdtaus.core.text.Message;
import org.jdtaus.core.text.spi.ApplicationLogger;

/**
 * Default {@code PhysicalFileFactory}-Implementierung.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class DefaultPhysicalFileFactory implements PhysicalFileFactory {

    //--Constructors------------------------------------------------------------

    // This section is generated by jdtaus-source-plugin.

    /** Protected <code>DefaultPhysicalFileFactory</code> implementation constructor.
    * @param meta Implementation meta-data.
    */
    protected DefaultPhysicalFileFactory(final Implementation meta) {
        super();
        this._defaultFormat = ((java.lang.Integer) meta.getProperties().
            getProperty("defaultFormat").getValue()).intValue();

        this.assertValidProperties();
    }
    /** Protected <code>DefaultPhysicalFileFactory</code> dependency constructor.
    * @param meta Dependency meta-data.
    */
    protected DefaultPhysicalFileFactory(final Dependency meta) {
        super();
        this._defaultFormat = ((java.lang.Integer) meta.getProperties().
            getProperty("defaultFormat").getValue()).intValue();

        this.assertValidProperties();
    }

    //------------------------------------------------------------Constructors--
    //--Dependencies------------------------------------------------------------

    // This section is generated by jdtaus-source-plugin.

    /** Configured <code>ApplicationLogger</code> implementation. */
    private transient ApplicationLogger _dependency3;

    /** <code>ApplicationLogger</code> implementation getter. */
    private ApplicationLogger getApplicationLogger() {
        ApplicationLogger ret = null;
        if(this._dependency3 != null) {
           ret = this._dependency3;
        } else {
            ret = (ApplicationLogger) ContainerFactory.getContainer().
                getDependency(DefaultPhysicalFileFactory.class,
                "ApplicationLogger");

            if(ret == null) {
                throw new ContainerError("ApplicationLogger");
            }

            if(ModelFactory.getModel().getModules().
                getImplementation(DefaultPhysicalFileFactory.class.getName()).
                getDependencies().getDependency("ApplicationLogger").
                isBound()) {

                this._dependency3 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext())) {

            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }
    /** Configured <code>Logger</code> implementation. */
    private transient Logger _dependency2;

    /** <code>Logger</code> implementation getter. */
    private Logger getLogger() {
        Logger ret = null;
        if(this._dependency2 != null) {
           ret = this._dependency2;
        } else {
            ret = (Logger) ContainerFactory.getContainer().
                getDependency(DefaultPhysicalFileFactory.class,
                "Logger");

            if(ret == null) {
                throw new ContainerError("Logger");
            }

            if(ModelFactory.getModel().getModules().
                getImplementation(DefaultPhysicalFileFactory.class.getName()).
                getDependencies().getDependency("Logger").
                isBound()) {

                this._dependency2 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext())) {

            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }
    /** Configured <code>TapeStructuredFileOperations</code> implementation. */
    private transient StructuredFileOperations _dependency1;

    /** <code>TapeStructuredFileOperations</code> implementation getter. */
    private StructuredFileOperations getTapeStructuredFileOperations() {
        StructuredFileOperations ret = null;
        if(this._dependency1 != null) {
           ret = this._dependency1;
        } else {
            ret = (StructuredFileOperations) ContainerFactory.getContainer().
                getDependency(DefaultPhysicalFileFactory.class,
                "TapeStructuredFileOperations");

            if(ret == null) {
                throw new ContainerError("TapeStructuredFileOperations");
            }

            if(ModelFactory.getModel().getModules().
                getImplementation(DefaultPhysicalFileFactory.class.getName()).
                getDependencies().getDependency("TapeStructuredFileOperations").
                isBound()) {

                this._dependency1 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext())) {

            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }
    /** Configured <code>DiskStructuredFileOperations</code> implementation. */
    private transient StructuredFileOperations _dependency0;

    /** <code>DiskStructuredFileOperations</code> implementation getter. */
    private StructuredFileOperations getDiskStructuredFileOperations() {
        StructuredFileOperations ret = null;
        if(this._dependency0 != null) {
           ret = this._dependency0;
        } else {
            ret = (StructuredFileOperations) ContainerFactory.getContainer().
                getDependency(DefaultPhysicalFileFactory.class,
                "DiskStructuredFileOperations");

            if(ret == null) {
                throw new ContainerError("DiskStructuredFileOperations");
            }

            if(ModelFactory.getModel().getModules().
                getImplementation(DefaultPhysicalFileFactory.class.getName()).
                getDependencies().getDependency("DiskStructuredFileOperations").
                isBound()) {

                this._dependency0 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext())) {

            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }

    //------------------------------------------------------------Dependencies--
    //--Properties--------------------------------------------------------------

    // This section is generated by jdtaus-source-plugin.

    /**
     * Property {@code defaultFormat}.
     * @serial
     */
    private int _defaultFormat;

    /** <code>defaultFormat</code> property getter. */
    protected int getDefaultFormat() {
        return this._defaultFormat;
    }


    //--------------------------------------------------------------Properties--
    //--PhysicalFileFactory-----------------------------------------------------

    public int analyse(final FileOperations fileOperations) throws
        PhysicalFileException, IOError {

        int blockSize = -1;
        int read = 0;
        int ret = -1;
        int size = 0x000000FF;
        int total = 0;

        final Message[] messages;
        final byte[] buf = new byte[4];
        final String str;
        final long length;

        if(fileOperations == null) {
            throw new NullPointerException("fileOperations");
        }

        length = fileOperations.getLength();
        try {
            ThreadLocalMessages.getMessages().clear();
            AbstractErrorMessage.setErrorsEnabled(false);

            if(length >= 128) { // mindestens ein Satzabschnitt.
                // die ersten 4 Byte lesen.
                fileOperations.setFilePointer(0L);
                do {
                    read = fileOperations.read(buf, total, buf.length - total);
                    if(read == -1) {
                        throw new IOError(new EOFException());
                    } else {
                        total += read;
                    }
                } while(total < buf.length);

                // Diskettenformat prüfen "0128".
                str = new String(buf, "DIN_66003");
                if("0128".equals(str)) {
                    ret = PhysicalFileFactory.FORMAT_DISK;
                    blockSize = 128;
                } else {
                    size &= buf[0];
                    size <<= 8;
                    size |= 0xFF;
                    size &= buf[1];

                    if(size == 150) {
                        ret = PhysicalFileFactory.FORMAT_TAPE;
                        blockSize = 150;
                    } else {
                        ThreadLocalMessages.getMessages().addMessage(
                            new IllegalDataMessage(Fields.FIELD_A1,
                            IllegalDataMessage.TYPE_CONSTANT, 0L, str));

                    }
                }
            }

            // Datei-Länge prüfen.
            final BigDecimal decLength = BigDecimal.valueOf(length);
            final BigDecimal decBlockSize = BigDecimal.valueOf(blockSize);
            if(!decLength.divideAndRemainder(decBlockSize)[1].
                equals(BigDecimal.ZERO)) {

                throw new PhysicalFileException(null);
            }

            messages = ThreadLocalMessages.getMessages().getMessages();
            if(messages.length > 0) {
                throw new PhysicalFileException(messages);
            }

            return ret;
        } catch(IOException e) {
            this.getLogger().error(e);
            throw new IOError(e);
        } finally {
            AbstractErrorMessage.setErrorsEnabled(true);
        }
    }

    public final PhysicalFile getPhysicalFile(final FileOperations ops) throws
        PhysicalFileException, IOError {

        if(ops == null) {
            throw new NullPointerException("ops");
        }

        final DefaultPhysicalFile ret;
        final Message[] messages;
        final StructuredFileOperations sops;
        final int format = ops.getLength() > 0 ?
            this.analyse(ops) : this.getDefaultFormat();

        switch(format) {
            case PhysicalFileFactory.FORMAT_DISK:
                sops = this.getDiskStructuredFileOperations();
                break;
            case PhysicalFileFactory.FORMAT_TAPE:
                sops = this.getTapeStructuredFileOperations();
                break;
            default:
                throw new IllegalStateException();

        }

        sops.setFileOperations(ops);

        try {
            ThreadLocalMessages.getMessages().clear();
            AbstractErrorMessage.setErrorsEnabled(false);

            ret = new DefaultPhysicalFile(sops);
            ret.checksum();

            messages = ThreadLocalMessages.getMessages().getMessages();
            if(messages.length > 0) {
                throw new PhysicalFileException(messages);
            }

            return ret;
        } finally {
            AbstractErrorMessage.setErrorsEnabled(true);
        }
    }

    //-----------------------------------------------------PhysicalFileFactory--
    //--DefaultPhysicalFileFactory----------------------------------------------

    protected void assertValidProperties() {
        final int defaultFormat = this.getDefaultFormat();
        if(defaultFormat != PhysicalFileFactory.FORMAT_DISK &&
            defaultFormat != PhysicalFileFactory.FORMAT_TAPE) {

            throw new ContainerError("defaultFormat=" + defaultFormat);
        }
    }

    //----------------------------------------------DefaultPhysicalFileFactory--

}

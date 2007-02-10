/*
 *  jDTAUS - DTAUS fileformat.
 *  Copyright (C) 2005 Christian Schulte <cs@schulte.it>
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
package org.jdtaus.common.dtaus.impl;

import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import org.jdtaus.common.container.ContainerError;
import org.jdtaus.common.container.ContainerFactory;
import org.jdtaus.common.container.model.Dependency;
import org.jdtaus.common.container.model.Implementation;
import org.jdtaus.common.container.model.ModelFactory;
import org.jdtaus.common.dtaus.Fields;
import org.jdtaus.common.dtaus.InvalidFileException;
import org.jdtaus.common.dtaus.PhysicalFile;
import org.jdtaus.common.dtaus.PhysicalFileFactory;
import org.jdtaus.common.dtaus.messages.AbstractErrorMessage;
import org.jdtaus.common.dtaus.messages.IllegalDataMessage;
import org.jdtaus.common.dtaus.messages.IllegalFileLengthMessage;
import org.jdtaus.common.io.FileOperations;
import org.jdtaus.common.io.IOError;
import org.jdtaus.common.io.StructuredFileOperations;
import org.jdtaus.common.logging.Logger;
import org.jdtaus.common.monitor.MessageRecorder;

/**
 * Default {@code PhysicalFileFactory}-Implementierung.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class DefaultPhysicalFileFactory implements PhysicalFileFactory {
    
    //--Implementation----------------------------------------------------------

    // Dieser Abschnitt wird von jdtaus-source-plugin generiert.

    /** Metadaten dieser Implementierung. */
    private static final Implementation IMPL =
        ModelFactory.getModel().getModules().
        getImplementation(DefaultPhysicalFileFactory.class.getName());

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

    // Dieser Abschnitt wird von jdtaus-source-plugin generiert.

    /** <code>DefaultPhysicalFileFactory</code> Implementierungs-Konstruktor.
    * @param meta Metadaten der Implementierung.
    */ 
    protected DefaultPhysicalFileFactory(final Implementation meta) {
        super();
        this._defaultFormat = ((java.lang.Integer) meta.getProperties().
            getProperty("defaultFormat").getValue()).intValue();

        this.assertValidProperties();
    }
    /** <code>DefaultPhysicalFileFactory</code> Implementierungs-Konstruktor.
    * @param meta Metadaten der Dependency.
    */ 
    protected DefaultPhysicalFileFactory(final Dependency meta) {
        super();
        this._defaultFormat = ((java.lang.Integer) meta.getProperties().
            getProperty("defaultFormat").getValue()).intValue();

        this.assertValidProperties();
    }

    //------------------------------------------------------------Constructors--
    //--Dependencies------------------------------------------------------------

    // Dieser Abschnitt wird von jdtaus-source-plugin generiert.

    /** Konfigurierte <code>MessageRecorder</code>-Implementierung. */
    private transient MessageRecorder _dependency3;

    /** <code>MessageRecorder</code>-Implementierung. */
    private MessageRecorder getMessageRecorder() {
        MessageRecorder ret = null;
        if(this._dependency3 != null) {
           ret = this._dependency3;
        } else {
            ret = (MessageRecorder) ContainerFactory.getContainer().
                getDependency(DefaultPhysicalFileFactory.IMPL.getIdentifier(),
                "MessageRecorder");

            if(ret == null) {
                throw new ContainerError("MessageRecorder");
            }

            if(DefaultPhysicalFileFactory.IMPL.getDependencies().
                getDependency("MessageRecorder").isBound()) {

                this._dependency3 = ret;
            }
        }

        return ret;
    }
    /** Konfigurierte <code>Logger</code>-Implementierung. */
    private transient Logger _dependency2;

    /** <code>Logger</code>-Implementierung. */
    private Logger getLogger() {
        Logger ret = null;
        if(this._dependency2 != null) {
           ret = this._dependency2;
        } else {
            ret = (Logger) ContainerFactory.getContainer().
                getDependency(DefaultPhysicalFileFactory.IMPL.getIdentifier(),
                "Logger");

            if(ret == null) {
                throw new ContainerError("Logger");
            }

            if(DefaultPhysicalFileFactory.IMPL.getDependencies().
                getDependency("Logger").isBound()) {

                this._dependency2 = ret;
            }
        }

        return ret;
    }
    /** Konfigurierte <code>TapeStructuredFileOperations</code>-Implementierung. */
    private transient StructuredFileOperations _dependency1;

    /** <code>TapeStructuredFileOperations</code>-Implementierung. */
    private StructuredFileOperations getTapeStructuredFileOperations() {
        StructuredFileOperations ret = null;
        if(this._dependency1 != null) {
           ret = this._dependency1;
        } else {
            ret = (StructuredFileOperations) ContainerFactory.getContainer().
                getDependency(DefaultPhysicalFileFactory.IMPL.getIdentifier(),
                "TapeStructuredFileOperations");

            if(ret == null) {
                throw new ContainerError("TapeStructuredFileOperations");
            }

            if(DefaultPhysicalFileFactory.IMPL.getDependencies().
                getDependency("TapeStructuredFileOperations").isBound()) {

                this._dependency1 = ret;
            }
        }

        return ret;
    }
    /** Konfigurierte <code>DiskStructuredFileOperations</code>-Implementierung. */
    private transient StructuredFileOperations _dependency0;

    /** <code>DiskStructuredFileOperations</code>-Implementierung. */
    private StructuredFileOperations getDiskStructuredFileOperations() {
        StructuredFileOperations ret = null;
        if(this._dependency0 != null) {
           ret = this._dependency0;
        } else {
            ret = (StructuredFileOperations) ContainerFactory.getContainer().
                getDependency(DefaultPhysicalFileFactory.IMPL.getIdentifier(),
                "DiskStructuredFileOperations");

            if(ret == null) {
                throw new ContainerError("DiskStructuredFileOperations");
            }

            if(DefaultPhysicalFileFactory.IMPL.getDependencies().
                getDependency("DiskStructuredFileOperations").isBound()) {

                this._dependency0 = ret;
            }
        }

        return ret;
    }

    //------------------------------------------------------------Dependencies--
    //--Properties--------------------------------------------------------------

    // Dieser Abschnitt wird von jdtaus-source-plugin generiert.

    /**
     * Property {@code defaultFormat}.
     * @serial
     */
    private int _defaultFormat;

    /** <code>defaultFormat</code>-Eigenschaft Zugriffsmethode. */
    protected int getDefaultFormat() {
        return this._defaultFormat;
    }


    //--------------------------------------------------------------Properties--
    //--PhysicalFileFactory-----------------------------------------------------
    
    public int detectFormat(final FileOperations fileOperations) throws
        InvalidFileException, IOError {
        
        int blockSize = -1;
        int read = 0;
        int ret = -1;
        int size = 0x000000FF;
        int total = 0;
        
        final byte[] buf = new byte[4];
        final String str;
        final long length;
        final AbstractErrorMessage[] messages;
        
        if(fileOperations == null) {
            throw new NullPointerException("fileOperations");
        }
        
        length = fileOperations.getLength();
        try {
            this.getMessageRecorder().remove(AbstractErrorMessage.class);
            AbstractErrorMessage.setErrorsEnabled(false);
            
            if(length >= 128) { // mindestens ein Satzabschnitt.
                // die ersten 4 Byte lesen.
                fileOperations.seek(0L);
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
                        this.getMessageRecorder().record(new IllegalDataMessage(
                            Fields.FIELD_A1, IllegalDataMessage.CONSTANT,
                            0L, str));
                        
                    }
                }
            } else {
                this.getMessageRecorder().record(new IllegalFileLengthMessage(
                    length, 0));
                
            }
            
            // Datei-Länge prüfen.
            final BigDecimal decLength = BigDecimal.valueOf(length);
            final BigDecimal decBlockSize = BigDecimal.valueOf(blockSize);
            if(!decLength.divideAndRemainder(decBlockSize)[1].
                equals(BigDecimal.ZERO)) {
                
                this.getMessageRecorder().record(new IllegalFileLengthMessage(
                    length, blockSize));
                
            }
            
            messages = AbstractErrorMessage.getErrorMessages(
                this.getMessageRecorder().getMessages());
            
            if(messages.length > 0) {
                throw new InvalidFileException(messages);
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
        InvalidFileException, IOError {
        
        if(ops == null) {
            throw new NullPointerException("ops");
        }
        
        final DefaultPhysicalFile ret;
        final AbstractErrorMessage[] messages;
        final StructuredFileOperations sops;
        final int format = ops.getLength() > 0 ?
            this.detectFormat(ops) : this.getDefaultFormat();
        
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
            this.getMessageRecorder().remove(AbstractErrorMessage.class);
            AbstractErrorMessage.setErrorsEnabled(false);
            
            ret = new DefaultPhysicalFile(sops);
            ret.checksum();
            
            messages = AbstractErrorMessage.getErrorMessages(
                this.getMessageRecorder().getMessages());
            
            if(messages.length > 0) {
                throw new InvalidFileException(messages);
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

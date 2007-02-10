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

import java.util.Date;
import org.jdtaus.common.container.ContainerError;
import org.jdtaus.common.container.ContainerFactory;
import org.jdtaus.common.container.model.Dependency;
import org.jdtaus.common.container.model.Implementation;
import org.jdtaus.common.container.model.ModelFactory;
import org.jdtaus.common.data.BankAccount;
import org.jdtaus.common.dtaus.Checksum;
import org.jdtaus.common.dtaus.Configuration;
import org.jdtaus.common.dtaus.Currency;
import org.jdtaus.common.dtaus.Fields;
import org.jdtaus.common.dtaus.Header;
import org.jdtaus.common.dtaus.HeaderLabel;
import org.jdtaus.common.dtaus.PhysicalFileError;
import org.jdtaus.common.dtaus.Transaction;
import org.jdtaus.common.dtaus.TransactionType;
import org.jdtaus.common.dtaus.Utility;
import org.jdtaus.common.dtaus.messages.ForbiddenTransactionTypeMessage;
import org.jdtaus.common.dtaus.messages.IllegalDataMessage;
import org.jdtaus.common.dtaus.messages.UnsupportedCurrencyMessage;
import org.jdtaus.common.dtaus.messages.UnsupportedHeaderLabelMessage;
import org.jdtaus.common.dtaus.messages.UnsupportedTransactionTypeMessage;
import org.jdtaus.common.io.StructuredFileOperations;
import org.jdtaus.common.logging.Logger;
import org.jdtaus.common.monitor.MessageRecorder;
import org.jdtaus.common.monitor.TaskMonitor;
import org.jdtaus.common.util.MemoryManager;

/**
 * Anlage 3 - 1.1 DTAUS0: Zahlungsverkehrssammelauftrag Diskettenformat.
 * <p/>
 * <b>Hinweis:</b><br/>
 * Implementierung darf niemals von mehreren Threads gleichzeitig verwendet
 * werden.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class DTAUSDisk extends AbstractLogicalFile {
    
    //--Implementation----------------------------------------------------------

    // Dieser Abschnitt wird von jdtaus-source-plugin generiert.

    /** Metadaten dieser Implementierung. */
    private static final Implementation IMPL =
        ModelFactory.getModel().getModules().
        getImplementation(DTAUSDisk.class.getName());

    //----------------------------------------------------------Implementation--
    //--Konstanten--------------------------------------------------------------
    
    /** Zeichenkette für Exception-Meldungen. */
    private static final String MSG_BLOCKSIZE = "blockSize=";
    
    /**
     * Index = A Datensatz-Feld - 1,
     * Wert = Offset relativ zum Anfang des Satzabschnittes.
     */
    protected static final int[] ARECORD_OFFSETS = {
        0, 4, 5, 7, 15, 23, 50, 56, 60, 70, 80, 95, 103, 127
    };
    
    /**
     * Index = A Datensatz-Feld - 1,
     * Wert = Länge des Feldes in Byte.
     */
    protected static final int[] ARECORD_LENGTH = {
        4, 1, 2, 8, 8, 27, 6, 4, 10, 10, 15, 8, 24, 1
    };
    
    /**
     * Index = E Datensatz-Feld - 1,
     * Wert = Offset relativ zum Anfang des Satzabschnittes.
     */
    protected static final int[] ERECORD_OFFSETS = {
        0, 4, 5, 10, 17, 30, 47, 64, 77
    };
    
    /**
     * Index = E Datensatz-Feld -1,
     * Wert = Länge des Feldes in Byte.
     */
    protected static final int[] ERECORD_LENGTH = {
        4, 1, 5, 7, 13, 17, 17, 13, 51
    };
    
    /** Länge des konstanten Teiles eines C Datensatzes in Byte. */
    protected static final int CRECORD_CONST_LENGTH = 187;
    
    /** Länge eines Erweiterungsteiles in Byte. */
    protected static final int CRECORD_EXT_LENGTH = 29;
    
    /**
     * Index = C Datensatz-Feld - 1,
     * Wert = Offset relativ zum ersten Satzabschnitt.
     */
    protected static final int[] CRECORD_OFFSETS1 = {
        0, 4, 5, 13, 21, 32, 44, 49, 50, 61, 69, 79, 90, 93, 120
    };
    
    /**
     * Index = C Datensatz-Feld - 1 (erster Satzabschnitt),
     * Wert = Länge des Feldes in Byte.
     */
    protected static final int[] CRECORD_LENGTH1 = {
        4, 1, 8, 8, 10, 11, 5, 1, 11, 8, 10, 11, 3, 27, 8
    };
    
    /**
     * Index = C Datensatz-Feld des zweiten Satzabschnittes - 1,
     * Wert = Offset relativ zum zweiten Satzabschnitt.
     */
    protected static final int[] CRECORD_OFFSETS2 = {
        0, 27, 54, 55, 57, 59, 61, 88, 90, 117
    };
    
    /**
     * Index = C Datensatz-Feld des zweiten Satzabschnittes - 1,
     * Wert = Länge des Feldes in Byte.
     */
    protected static final int[] CRECORD_LENGTH2 = {
        27, 27, 1, 2, 2, 2, 27, 2, 27, 11
    };
    
    /**
     * Index = C Datensatz-Feld des 3., 4., 5. und 6. Satzabschnittes - 1,
     * Wert = Offset relativ zum Anfang des 3., 4., 5. und 6. Satzabschnittes.
     */
    protected static final int[] CRECORD_OFFSETS_EXT = {
        0, 2, 29, 31, 58, 60, 87, 89, 116
    };
    
    /**
     * Index = C Datensatz-Feld des 3., 4., 5. und 6. Satzabschnittes - 1,
     * Wert = Länge des Feldes in Byte.
     */
    protected static final int[] CRECORD_LENGTH_EXT = {
        2, 27, 2, 27, 2, 27, 2, 27, 12
    };
    
    /**
     * Index = Anzahl Erweiterungsteile,
     * Wert = Anzahl benötigter Satzabschnitte.
     */
    protected static final int[] CRECORD_EXTENSIONCOUNT_TO_BLOCKCOUNT = {
        2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6
    };
    
    /**
     * Index = Index Erweiterungsteil,
     * Wert = Satzabschnitt-Offset zu Transaktionsbeginn.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_BLOCKOFFSET = {
        1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5
    };
    
    /**
     * Index = Index Erweiterungsteil,
     * Wert = Anfangsposition des Erweiterungsteils relativ zum Anfang des
     * Satzabschnittes.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_TYPEOFFSET = {
        DTAUSDisk.CRECORD_OFFSETS2[5], DTAUSDisk.CRECORD_OFFSETS2[7],
        DTAUSDisk.CRECORD_OFFSETS_EXT[0], DTAUSDisk.CRECORD_OFFSETS_EXT[2],
        DTAUSDisk.CRECORD_OFFSETS_EXT[4], DTAUSDisk.CRECORD_OFFSETS_EXT[6],
        DTAUSDisk.CRECORD_OFFSETS_EXT[0], DTAUSDisk.CRECORD_OFFSETS_EXT[2],
        DTAUSDisk.CRECORD_OFFSETS_EXT[4], DTAUSDisk.CRECORD_OFFSETS_EXT[6],
        DTAUSDisk.CRECORD_OFFSETS_EXT[0], DTAUSDisk.CRECORD_OFFSETS_EXT[2],
        DTAUSDisk.CRECORD_OFFSETS_EXT[4], DTAUSDisk.CRECORD_OFFSETS_EXT[6],
        DTAUSDisk.CRECORD_OFFSETS_EXT[0], DTAUSDisk.CRECORD_OFFSETS_EXT[2],
        DTAUSDisk.CRECORD_OFFSETS_EXT[4], DTAUSDisk.CRECORD_OFFSETS_EXT[6]
    };
    
    /**
     * Index = Index Erweiterungsteil,
     * Wert = Anfangsposition des Erweiterungsteils relativ zum Anfang des
     * Satzabschnittes.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_TYPELENGTH = {
        DTAUSDisk.CRECORD_LENGTH2[5], DTAUSDisk.CRECORD_LENGTH2[7],
        DTAUSDisk.CRECORD_LENGTH_EXT[0], DTAUSDisk.CRECORD_LENGTH_EXT[2],
        DTAUSDisk.CRECORD_LENGTH_EXT[4], DTAUSDisk.CRECORD_LENGTH_EXT[6],
        DTAUSDisk.CRECORD_LENGTH_EXT[0], DTAUSDisk.CRECORD_LENGTH_EXT[2],
        DTAUSDisk.CRECORD_LENGTH_EXT[4], DTAUSDisk.CRECORD_LENGTH_EXT[6],
        DTAUSDisk.CRECORD_LENGTH_EXT[0], DTAUSDisk.CRECORD_LENGTH_EXT[2],
        DTAUSDisk.CRECORD_LENGTH_EXT[4], DTAUSDisk.CRECORD_LENGTH_EXT[6],
        DTAUSDisk.CRECORD_LENGTH_EXT[0], DTAUSDisk.CRECORD_LENGTH_EXT[2],
        DTAUSDisk.CRECORD_LENGTH_EXT[4], DTAUSDisk.CRECORD_LENGTH_EXT[6]
    };
    
    /**
     * Index = Index Erweiterungsteil,
     * Wert = Anfangsposition des Erweiterungsteils relativ zum Anfang des
     * Satzabschnittes.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_VALUEOFFSET = {
        DTAUSDisk.CRECORD_OFFSETS2[6], DTAUSDisk.CRECORD_OFFSETS2[8],
        DTAUSDisk.CRECORD_OFFSETS_EXT[1], DTAUSDisk.CRECORD_OFFSETS_EXT[3],
        DTAUSDisk.CRECORD_OFFSETS_EXT[5], DTAUSDisk.CRECORD_OFFSETS_EXT[7],
        DTAUSDisk.CRECORD_OFFSETS_EXT[1], DTAUSDisk.CRECORD_OFFSETS_EXT[3],
        DTAUSDisk.CRECORD_OFFSETS_EXT[5], DTAUSDisk.CRECORD_OFFSETS_EXT[7],
        DTAUSDisk.CRECORD_OFFSETS_EXT[1], DTAUSDisk.CRECORD_OFFSETS_EXT[3],
        DTAUSDisk.CRECORD_OFFSETS_EXT[5], DTAUSDisk.CRECORD_OFFSETS_EXT[7],
        DTAUSDisk.CRECORD_OFFSETS_EXT[1], DTAUSDisk.CRECORD_OFFSETS_EXT[3],
        DTAUSDisk.CRECORD_OFFSETS_EXT[5], DTAUSDisk.CRECORD_OFFSETS_EXT[7]
    };
    
    /**
     * Index = Index Erweiterungsteil,
     * Wert = Anfangsposition des Erweiterungsteils relativ zum Anfang des
     * Satzabschnittes.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_VALUELENGTH = {
        DTAUSDisk.CRECORD_LENGTH2[6], DTAUSDisk.CRECORD_LENGTH2[8],
        DTAUSDisk.CRECORD_LENGTH_EXT[1], DTAUSDisk.CRECORD_LENGTH_EXT[3],
        DTAUSDisk.CRECORD_LENGTH_EXT[5], DTAUSDisk.CRECORD_LENGTH_EXT[7],
        DTAUSDisk.CRECORD_LENGTH_EXT[1], DTAUSDisk.CRECORD_LENGTH_EXT[3],
        DTAUSDisk.CRECORD_LENGTH_EXT[5], DTAUSDisk.CRECORD_LENGTH_EXT[7],
        DTAUSDisk.CRECORD_LENGTH_EXT[1], DTAUSDisk.CRECORD_LENGTH_EXT[3],
        DTAUSDisk.CRECORD_LENGTH_EXT[5], DTAUSDisk.CRECORD_LENGTH_EXT[7],
        DTAUSDisk.CRECORD_LENGTH_EXT[1], DTAUSDisk.CRECORD_LENGTH_EXT[3],
        DTAUSDisk.CRECORD_LENGTH_EXT[5], DTAUSDisk.CRECORD_LENGTH_EXT[7]
    };
    
    /**
     * Index = Index Erweiterungsteil,
     * Wert = Anzahl der folgenden Erweiterungsteile im selben Satzabschnitt.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_FOLLOWINGEXTENSIONS = {
        1, 0, 3, 2, 1, 0, 3, 2, 1, 0, 3, 2, 1, 0, 3, 2, 1, 0
    };
    
    /**
     * Index = Index Erweiterungsteil,
     * Wert = Feld-Konstante für das Typen-Feld des Erweiterungsteils.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_TYPEFIELD = {
        Fields.FIELD_C19, Fields.FIELD_C21,
        Fields.FIELD_C24, Fields.FIELD_C26, Fields.FIELD_C28,
        Fields.FIELD_C30, Fields.FIELD_C33, Fields.FIELD_C35,
        Fields.FIELD_C37, Fields.FIELD_C39, Fields.FIELD_C42,
        Fields.FIELD_C44, Fields.FIELD_C46, Fields.FIELD_C48,
        Fields.FIELD_C51, Fields.FIELD_C53, Fields.FIELD_C55,
        Fields.FIELD_C57, Fields.FIELD_C59
    };
    
    /**
     * Index = Index Erweiterungsteil,
     * Wert = Feld-Konstante für das Werte-Feld des Erweiterungsteils.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_VALUEFIELD = {
        Fields.FIELD_C20, Fields.FIELD_C22,
        Fields.FIELD_C25, Fields.FIELD_C27, Fields.FIELD_C29,
        Fields.FIELD_C31, Fields.FIELD_C34, Fields.FIELD_C36,
        Fields.FIELD_C38, Fields.FIELD_C40, Fields.FIELD_C43,
        Fields.FIELD_C45, Fields.FIELD_C47, Fields.FIELD_C49,
        Fields.FIELD_C52, Fields.FIELD_C54, Fields.FIELD_C56,
        Fields.FIELD_C58
    };
    
    //--------------------------------------------------------------Konstanten--
    //--Constructors------------------------------------------------------------

    // Dieser Abschnitt wird von jdtaus-source-plugin generiert.

    /** <code>DTAUSDisk</code> Implementierungs-Konstruktor.
    * @param meta Metadaten der Implementierung.
    */ 
    protected DTAUSDisk(final Implementation meta) {
        super();
    }
    /** <code>DTAUSDisk</code> Implementierungs-Konstruktor.
    * @param meta Metadaten der Dependency.
    */ 
    protected DTAUSDisk(final Dependency meta) {
        super();
    }

    //------------------------------------------------------------Constructors--
    //--Konstruktoren-----------------------------------------------------------
    
    /**
     * Erzeugt eine neue {@code DTAUSDisk} Instanz.
     *
     * @param headerBlock Satzabschnitt, in dem der A-Datensatz erwartet wird.
     * @param persistence zu verwendende {@code StructuredFile}-Implementierung.
     *
     * @throws NullPointerException {@code if(persistence == null)}
     * @throws IllegalArgumentException bei ungültigen Angaben.
     */
    protected DTAUSDisk(final long headerBlock,
        final StructuredFileOperations persistence) {
        
        this(DTAUSDisk.IMPL);
        if(persistence == null) {
            throw new NullPointerException("persistence");
        }
        
        if(persistence.getBlockSize() != 128) {
            throw new IllegalArgumentException(DTAUSDisk.MSG_BLOCKSIZE +
                persistence.getBlockSize());
            
        }
        
        this.setStructuredFile(persistence);
        this.setHeaderBlock(headerBlock);
    }
    
    //-----------------------------------------------------------Konstruktoren--
    //--Dependencies------------------------------------------------------------

    // Dieser Abschnitt wird von jdtaus-source-plugin generiert.

    /** Konfigurierte <code>TaskMonitor</code>-Implementierung. */
    private transient TaskMonitor _dependency5;

    /** <code>TaskMonitor</code>-Implementierung. */
    private TaskMonitor getTaskMonitor() {
        TaskMonitor ret = null;
        if(this._dependency5 != null) {
           ret = this._dependency5;
        } else {
            ret = (TaskMonitor) ContainerFactory.getContainer().
                getDependency(DTAUSDisk.IMPL.getIdentifier(),
                "TaskMonitor");

            if(ret == null) {
                throw new ContainerError("TaskMonitor");
            }

            if(DTAUSDisk.IMPL.getDependencies().
                getDependency("TaskMonitor").isBound()) {

                this._dependency5 = ret;
            }
        }

        return ret;
    }
    /** Konfigurierte <code>MessageRecorder</code>-Implementierung. */
    private transient MessageRecorder _dependency4;

    /** <code>MessageRecorder</code>-Implementierung. */
    private MessageRecorder getMessageRecorder() {
        MessageRecorder ret = null;
        if(this._dependency4 != null) {
           ret = this._dependency4;
        } else {
            ret = (MessageRecorder) ContainerFactory.getContainer().
                getDependency(DTAUSDisk.IMPL.getIdentifier(),
                "MessageRecorder");

            if(ret == null) {
                throw new ContainerError("MessageRecorder");
            }

            if(DTAUSDisk.IMPL.getDependencies().
                getDependency("MessageRecorder").isBound()) {

                this._dependency4 = ret;
            }
        }

        return ret;
    }
    /** Konfigurierte <code>MemoryManager</code>-Implementierung. */
    private transient MemoryManager _dependency3;

    /** <code>MemoryManager</code>-Implementierung. */
    private MemoryManager getMemoryManager() {
        MemoryManager ret = null;
        if(this._dependency3 != null) {
           ret = this._dependency3;
        } else {
            ret = (MemoryManager) ContainerFactory.getContainer().
                getDependency(DTAUSDisk.IMPL.getIdentifier(),
                "MemoryManager");

            if(ret == null) {
                throw new ContainerError("MemoryManager");
            }

            if(DTAUSDisk.IMPL.getDependencies().
                getDependency("MemoryManager").isBound()) {

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
                getDependency(DTAUSDisk.IMPL.getIdentifier(),
                "Logger");

            if(ret == null) {
                throw new ContainerError("Logger");
            }

            if(DTAUSDisk.IMPL.getDependencies().
                getDependency("Logger").isBound()) {

                this._dependency2 = ret;
            }
        }

        return ret;
    }
    /** Konfigurierte <code>Configuration</code>-Implementierung. */
    private transient Configuration _dependency1;

    /** <code>Configuration</code>-Implementierung. */
    private Configuration getConfiguration() {
        Configuration ret = null;
        if(this._dependency1 != null) {
           ret = this._dependency1;
        } else {
            ret = (Configuration) ContainerFactory.getContainer().
                getDependency(DTAUSDisk.IMPL.getIdentifier(),
                "Configuration");

            if(ret == null) {
                throw new ContainerError("Configuration");
            }

            if(DTAUSDisk.IMPL.getDependencies().
                getDependency("Configuration").isBound()) {

                this._dependency1 = ret;
            }
        }

        return ret;
    }
    /** Konfigurierte <code>Utility</code>-Implementierung. */
    private transient Utility _dependency0;

    /** <code>Utility</code>-Implementierung. */
    private Utility getUtility() {
        Utility ret = null;
        if(this._dependency0 != null) {
           ret = this._dependency0;
        } else {
            ret = (Utility) ContainerFactory.getContainer().
                getDependency(DTAUSDisk.IMPL.getIdentifier(),
                "Utility");

            if(ret == null) {
                throw new ContainerError("Utility");
            }

            if(DTAUSDisk.IMPL.getDependencies().
                getDependency("Utility").isBound()) {

                this._dependency0 = ret;
            }
        }

        return ret;
    }

    //------------------------------------------------------------Dependencies--
    //--AbstractLogicalFile-----------------------------------------------------
    
    protected int checksumTransaction(final long block,
        final Transaction transaction, final Checksum checksum) throws
        PhysicalFileError {
        
        int ret = 2;
        final long extCount = this.readNumber(Fields.FIELD_C18, block + 1,
            DTAUSDisk.CRECORD_OFFSETS2[4], DTAUSDisk.CRECORD_LENGTH2[4],
            AbstractLogicalFile.ENCODING_ASCII);
        
        if(extCount != -1L) {
            final Transaction t = this.readTransaction(block, transaction);
            final BankAccount target = t.getTargetAccount();
            
            if(target != null) {
                checksum.setSumAmount(checksum.getSumAmount() + t.getAmount());
                checksum.setSumTargetAccount(checksum.getSumTargetAccount() +
                    target.getAccount());
                
                checksum.setSumTargetBank(checksum.getSumTargetBank() +
                    target.getBank());
                
            }
            
            ret = CRECORD_EXTENSIONCOUNT_TO_BLOCKCOUNT[(int) extCount];
        }
        
        return ret;
    }
    
    protected char getBlockType(final long block) throws PhysicalFileError {
        // Feld 2
        final String str = this.readString(Fields.FIELD_A2, block,
            DTAUSDisk.ARECORD_OFFSETS[1], DTAUSDisk.ARECORD_LENGTH[1], true,
            AbstractLogicalFile.ENCODING_ASCII);
        
        char ret;
        if(str == null || str.length() != 1) {
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_A2, IllegalDataMessage.CONSTANT,
                block * this.persistence.getBlockSize() +
                DTAUSDisk.ARECORD_OFFSETS[1], str));
            
            ret = '?';
        } else if("A".equals(str)) {
            ret = 'A';
        } else if("C".equals(str)) {
            ret = 'C';
        } else if("E".equals(str)) {
            ret = 'E';
        } else {
            ret = str.toCharArray()[0];
        }
        
        return ret;
    }
    
    protected Header readHeader(final long headerBlock) throws
        PhysicalFileError {
        
        long num;
        String str;
        final Date createDate;
        final Date executionDate;
        final Header.Schedule schedule;
        final Header ret;
        final HeaderLabel label;
        final int blockSize;
        final Currency cur;
        boolean isBank = false;
        
        ret = new Header();
        blockSize = this.persistence.getBlockSize();
        
        // Feld 1
        num = this.readNumber(Fields.FIELD_A1, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[0], DTAUSDisk.ARECORD_LENGTH[0],
            AbstractLogicalFile.ENCODING_ASCII);
        
        if(num != blockSize) {
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_A1, IllegalDataMessage.CONSTANT,
                headerBlock * this.persistence.getBlockSize() +
                DTAUSDisk.ARECORD_OFFSETS[0], Long.valueOf(num)));
            
        }
        
        // Feld 2
        str = this.readString(Fields.FIELD_A2, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[1], DTAUSDisk.ARECORD_LENGTH[1], true,
            AbstractLogicalFile.ENCODING_ASCII);
        
        if(str == null || str.length() != 1 || str.toCharArray()[0] != 'A') {
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_A2, IllegalDataMessage.CONSTANT,
                headerBlock * this.persistence.getBlockSize() +
                DTAUSDisk.ARECORD_OFFSETS[1], str));
            
        }
        
        // Feld 3
        str = this.readString(Fields.FIELD_A3, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[2], DTAUSDisk.ARECORD_LENGTH[2], true,
            AbstractLogicalFile.ENCODING_ASCII);
        
        if(str != null) {
            label = this.getConfiguration().getHeaderLabel(str);
            if(label == null) {
                this.getMessageRecorder().record(
                    new UnsupportedHeaderLabelMessage(
                    headerBlock * this.persistence.getBlockSize() +
                    DTAUSDisk.ARECORD_OFFSETS[2], str,
                    this.getConfiguration().getHeaderLabels()));
                
            } else {
                isBank = label.isSendByBank();
                ret.setLabel(label);
            }
        }
        
        // Feld 4
        num = this.readNumber(Fields.FIELD_A4, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[3], DTAUSDisk.ARECORD_LENGTH[3],
            AbstractLogicalFile.ENCODING_ASCII);
        
        this.checkBankCode(Fields.FIELD_A4, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[3], (int) num, false);
        
        ret.setRecipientBank((int) num);
        
        // Feld 5
        // Nur belegt wenn Absender Kreditinistitut ist, sonst 0.
        num = this.readNumber(Fields.FIELD_A5, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[4], DTAUSDisk.ARECORD_LENGTH[4],
            AbstractLogicalFile.ENCODING_ASCII);
        
        this.checkBankCode(Fields.FIELD_A5, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[4], (int) num, false);
        
        ret.setBankData5(isBank ? (int) num : 0);
        
        // Feld 6
        str = this.readString(Fields.FIELD_A6, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[5], DTAUSDisk.ARECORD_LENGTH[5], true,
            AbstractLogicalFile.ENCODING_ASCII);
        
        ret.setSenderName(str);
        
        // Feld 7
        createDate = this.readShortDate(Fields.FIELD_A7, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[6], AbstractLogicalFile.ENCODING_ASCII);
        
        this.checkCreateDate(Fields.FIELD_A7, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[6], createDate);
        
        // Feld 8
        // Nur belegt wenn Absender Kreditinistitut ist, sonst "".
        str = this.readString(Fields.FIELD_A8, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[7], DTAUSDisk.ARECORD_LENGTH[7], true,
            AbstractLogicalFile.ENCODING_ASCII);
        
        ret.setBankData8(isBank ? str : "");
        
        // Feld 9
        num = this.readNumber(Fields.FIELD_A9, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[8], DTAUSDisk.ARECORD_LENGTH[8],
            AbstractLogicalFile.ENCODING_ASCII);
        
        ret.setAccount(num);
        
        // Feld 10
        num = this.readNumber(Fields.FIELD_A10, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[9], DTAUSDisk.ARECORD_LENGTH[9],
            AbstractLogicalFile.ENCODING_ASCII);
        
        ret.setReference(num);
        
        // Feld 11b
        executionDate = this.readLongDate(Fields.FIELD_A11B, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[11], AbstractLogicalFile.ENCODING_ASCII);
        
        this.checkExecutionDate(Fields.FIELD_A11B, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[11], executionDate);
        
        if(createDate != null) {
            schedule = new Header.Schedule(createDate, executionDate);
            this.checkHeaderSchedule(Fields.FIELD_A11B, headerBlock,
                DTAUSDisk.ARECORD_OFFSETS[10], schedule);
            
            ret.setSchedule(schedule);
        }
        
        // Feld 12
        str = this.readString(Fields.FIELD_A12, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[13], DTAUSDisk.ARECORD_LENGTH[13], true,
            AbstractLogicalFile.ENCODING_ASCII);
        
        if(str == null || str.length() < 1) {
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_A12, IllegalDataMessage.ALPHA_NUMERIC,
                headerBlock * this.persistence.getBlockSize() +
                DTAUSDisk.ARECORD_OFFSETS[13], str));
            
        } else {
            cur = this.getConfiguration().getCurrency(str.toCharArray()[0]);
            if(cur == null) {
                this.getMessageRecorder().record(
                    new UnsupportedCurrencyMessage(
                    headerBlock * this.persistence.getBlockSize() +
                    DTAUSDisk.ARECORD_OFFSETS[13], str.toCharArray()[0],
                    this.getConfiguration().getCurrencies(this)));
                
            } else {
                ret.setCurrency(cur);
            }
        }
        
        return ret;
    }
    
    protected void writeHeader(final long headerBlock, final Header header) {
        int bankCode;
        final Header.Schedule schedule;
        final HeaderLabel label;
        final boolean isBank;
        
        schedule = header.getSchedule();
        label = header.getLabel();
        bankCode = header.getRecipientBank();
        isBank = label.isSendByBank();
        
        // Feld 1
        this.writeNumber(Fields.FIELD_A1, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[0], DTAUSDisk.ARECORD_LENGTH[0],
            this.persistence.getBlockSize(),
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 2
        this.writeString(Fields.FIELD_A2, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[1], DTAUSDisk.ARECORD_LENGTH[1], "A",
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 3
        this.writeString(Fields.FIELD_A3, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[2], DTAUSDisk.ARECORD_LENGTH[2],
            label.getLabel(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 4
        this.writeNumber(Fields.FIELD_A4, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[3], DTAUSDisk.ARECORD_LENGTH[3],
            bankCode, AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 5
        bankCode = isBank ? header.getBankData5() : 0;
        this.writeNumber(Fields.FIELD_A5, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[4], DTAUSDisk.ARECORD_LENGTH[4],
            bankCode, AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 6
        this.writeString(Fields.FIELD_A6, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[5], DTAUSDisk.ARECORD_LENGTH[5],
            header.getSenderName(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 7
        this.writeShortDate(Fields.FIELD_A7, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[6], schedule.getCreateDate(),
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 8
        this.writeString(Fields.FIELD_A8, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[7], DTAUSDisk.ARECORD_LENGTH[7],
            header.getBankData8(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 9
        this.writeNumber(Fields.FIELD_A9, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[8], DTAUSDisk.ARECORD_LENGTH[8],
            header.getAccount(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 10
        this.writeNumber(Fields.FIELD_A10, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[9], DTAUSDisk.ARECORD_LENGTH[9],
            header.getReference(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 11a
        this.writeString(Fields.FIELD_A11A, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[10], DTAUSDisk.ARECORD_LENGTH[10], "",
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 11b
        this.writeLongDate(Fields.FIELD_A11B, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[11], schedule.getExecutionDate(),
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 11c
        this.writeString(Fields.FIELD_A11C, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[12], DTAUSDisk.ARECORD_LENGTH[12], "",
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 12
        this.writeString(Fields.FIELD_A12, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[13], DTAUSDisk.ARECORD_LENGTH[13],
            String.valueOf(header.getCurrency().getLabel()),
            AbstractLogicalFile.ENCODING_ASCII);
        
    }
    
    protected Checksum readChecksum(final long checksumBlock) throws
        PhysicalFileError {
        
        long num;
        final String str;
        final Checksum checksum;
        
        checksum = new Checksum();
        
        // Feld 1
        num = this.readNumber(Fields.FIELD_E1, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[0], DTAUSDisk.ERECORD_LENGTH[0],
            AbstractLogicalFile.ENCODING_ASCII);
        
        if(num != this.persistence.getBlockSize()) {
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_E1, IllegalDataMessage.CONSTANT,
                checksumBlock * this.persistence.getBlockSize() +
                DTAUSDisk.ERECORD_OFFSETS[0], Long.valueOf(num)));
            
        }
        
        // Feld 2
        str = this.readString(Fields.FIELD_E2, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[1], DTAUSDisk.ERECORD_LENGTH[1], true,
            AbstractLogicalFile.ENCODING_ASCII);
        
        if(str == null || str.length() != 1 || str.toCharArray()[0] != 'E') {
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_E2, IllegalDataMessage.CONSTANT,
                checksumBlock * this.persistence.getBlockSize()+
                DTAUSDisk.ERECORD_OFFSETS[1], str));
            
        }
        
        // Feld 4
        num = this.readNumber(Fields.FIELD_E4, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[3], DTAUSDisk.ERECORD_LENGTH[3],
            AbstractLogicalFile.ENCODING_ASCII);
        
        checksum.setTransactionCount((int) num);
        
        // Feld 6
        num = this.readNumber(Fields.FIELD_E6, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[5], DTAUSDisk.ERECORD_LENGTH[5],
            AbstractLogicalFile.ENCODING_ASCII);
        
        checksum.setSumTargetAccount(num);
        
        // Feld 7
        num = this.readNumber(Fields.FIELD_E7, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[6], DTAUSDisk.ERECORD_LENGTH[6],
            AbstractLogicalFile.ENCODING_ASCII);
        
        checksum.setSumTargetBank(num);
        
        // Feld 8
        num = this.readNumber(Fields.FIELD_E8, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[7], DTAUSDisk.ERECORD_LENGTH[7],
            AbstractLogicalFile.ENCODING_ASCII);
        
        checksum.setSumAmount(num);
        return checksum;
    }
    
    protected void writeChecksum(final long checksumBlock,
        final Checksum checksum) {
        
        // Feld 1
        this.writeNumber(Fields.FIELD_E1, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[0], DTAUSDisk.ERECORD_LENGTH[0],
            this.persistence.getBlockSize(),
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 2
        this.writeString(Fields.FIELD_E2, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[1], DTAUSDisk.ERECORD_LENGTH[1], "E",
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 3
        this.writeString(Fields.FIELD_E3, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[2], DTAUSDisk.ERECORD_LENGTH[2], "",
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 4
        this.writeNumber(Fields.FIELD_E4, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[3], DTAUSDisk.ERECORD_LENGTH[3],
            checksum.getTransactionCount(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 5
        this.writeNumber(Fields.FIELD_E5, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[4], DTAUSDisk.ERECORD_LENGTH[4], 0L,
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 6
        this.writeNumber(Fields.FIELD_E6, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[5], DTAUSDisk.ERECORD_LENGTH[5],
            checksum.getSumTargetAccount(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 7
        this.writeNumber(Fields.FIELD_E7, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[6], DTAUSDisk.ERECORD_LENGTH[6],
            checksum.getSumTargetBank(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 8
        this.writeNumber(Fields.FIELD_E8, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[7], DTAUSDisk.ERECORD_LENGTH[7],
            checksum.getSumAmount(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Feld 9
        this.writeString(Fields.FIELD_E9, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[8], DTAUSDisk.ERECORD_LENGTH[8], "",
            AbstractLogicalFile.ENCODING_ASCII);
        
    }
    
    protected Transaction readTransaction(final long block,
        final Transaction transaction) throws PhysicalFileError {
        
        int search;
        long blockOffset;
        long num;
        long keyType;
        String str;
        final long extCount;
        final Currency cur;
        final TransactionType type;
        final Transaction.Description desc = new Transaction.Description();
        final BankAccount targetAccount = new BankAccount();
        final BankAccount executiveAccount = new BankAccount();
        
        transaction.setExecutiveExt(null);
        transaction.setTargetExt(null);
        extCount = this.readNumber(Fields.FIELD_C18, block + 1,
            DTAUSDisk.CRECORD_OFFSETS2[4], DTAUSDisk.CRECORD_LENGTH2[4],
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 1
        num = this.readNumber(Fields.FIELD_C1, block,
            DTAUSDisk.CRECORD_OFFSETS1[0], DTAUSDisk.CRECORD_LENGTH1[0],
            AbstractLogicalFile.ENCODING_ASCII);
        
        if(num != DTAUSDisk.CRECORD_CONST_LENGTH +
            extCount * DTAUSDisk.CRECORD_EXT_LENGTH) {
            
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_C1, IllegalDataMessage.NUMERIC,
                block * this.persistence.getBlockSize() +
                DTAUSDisk.CRECORD_OFFSETS1[0], Long.valueOf(num)));
            
        }
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 2
        str = this.readString(Fields.FIELD_C2, block,
            DTAUSDisk.CRECORD_OFFSETS1[1], DTAUSDisk.CRECORD_LENGTH1[1], true,
            AbstractLogicalFile.ENCODING_ASCII);
        
        if(str == null || str.length() != 1 || str.toCharArray()[0] != 'C') {
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_C2, IllegalDataMessage.CONSTANT,
                block * this.persistence.getBlockSize() +
                DTAUSDisk.CRECORD_OFFSETS1[1], str));
            
        }
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 3
        num = this.readNumber(Fields.FIELD_C3, block,
            DTAUSDisk.CRECORD_OFFSETS1[2], DTAUSDisk.CRECORD_LENGTH1[2],
            AbstractLogicalFile.ENCODING_ASCII);
        
        this.checkBankCode(Fields.FIELD_C3, block,
            DTAUSDisk.CRECORD_OFFSETS1[2], (int) num, false);
        
        transaction.setPrimaryBank((int) num);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 4
        num = this.readNumber(Fields.FIELD_C4, block,
            DTAUSDisk.CRECORD_OFFSETS1[3], DTAUSDisk.CRECORD_LENGTH1[3],
            AbstractLogicalFile.ENCODING_ASCII);
        
        this.checkBankCode(Fields.FIELD_C3, block,
            DTAUSDisk.CRECORD_OFFSETS1[2], (int) num, true);
        
        targetAccount.setBank((int) num);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 5
        num = this.readNumber(Fields.FIELD_C5, block,
            DTAUSDisk.CRECORD_OFFSETS1[4], DTAUSDisk.CRECORD_LENGTH1[4],
            AbstractLogicalFile.ENCODING_ASCII);
        
        this.checkAccountCode(Fields.FIELD_C5, block,
            DTAUSDisk.CRECORD_OFFSETS1[4], num, true);
        
        targetAccount.setAccount(num);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 6
        num = this.readNumber(Fields.FIELD_C6, block,
            DTAUSDisk.CRECORD_OFFSETS1[5], DTAUSDisk.CRECORD_LENGTH1[5],
            AbstractLogicalFile.ENCODING_ASCII);
        
        transaction.setReference(num);
        
        // Konstanter Teil - Satzaschnitt 1 - Felder 7a & 7b
        keyType = this.readNumber(Fields.FIELD_C7A, block,
            DTAUSDisk.CRECORD_OFFSETS1[6], 2,
            AbstractLogicalFile.ENCODING_ASCII);
        
        num = this.readNumber(Fields.FIELD_C7B, block,
            DTAUSDisk.CRECORD_OFFSETS1[6] + 2, DTAUSDisk.CRECORD_LENGTH1[6] - 2,
            AbstractLogicalFile.ENCODING_ASCII);
        
        type = this.getConfiguration().
            getTransactionType((int) keyType, (int) num);
        
        if(type == null) {
            this.getMessageRecorder().record(
                new UnsupportedTransactionTypeMessage(
                block * this.persistence.getBlockSize() +
                DTAUSDisk.CRECORD_OFFSETS1[6], (int) keyType, (int) num,
                this.getConfiguration().getTransactionTypes(this)));
            
        } else {
            if(!ForbiddenTransactionTypeMessage.isTransactionTypeAllowed(
                type, this.getConfiguration().getTransactionTypes(this))) {
                
                this.getMessageRecorder().record(
                    new ForbiddenTransactionTypeMessage(this.getHeader(),
                    type, this.getConfiguration().getTransactionTypes(this)));
                
            }
            
            transaction.setType(type);
        }
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 10
        num = this.readNumber(Fields.FIELD_C10, block,
            DTAUSDisk.CRECORD_OFFSETS1[9], DTAUSDisk.CRECORD_LENGTH1[9],
            AbstractLogicalFile.ENCODING_ASCII);
        
        this.checkBankCode(Fields.FIELD_C10, block,
            DTAUSDisk.CRECORD_OFFSETS1[9], (int) num, true);
        
        executiveAccount.setBank((int) num);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 11
        num = this.readNumber(Fields.FIELD_C11, block,
            DTAUSDisk.CRECORD_OFFSETS1[10], DTAUSDisk.CRECORD_LENGTH1[10],
            AbstractLogicalFile.ENCODING_ASCII);
        
        this.checkAccountCode(Fields.FIELD_C11, block,
            DTAUSDisk.CRECORD_OFFSETS1[10], num, true);
        
        executiveAccount.setAccount(num);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 12
        num = this.readNumber(Fields.FIELD_C12, block,
            DTAUSDisk.CRECORD_OFFSETS1[11], DTAUSDisk.CRECORD_LENGTH1[11],
            AbstractLogicalFile.ENCODING_ASCII);
        
        this.checkAmount(Fields.FIELD_C12, block,
            DTAUSDisk.CRECORD_OFFSETS1[11], num, true);
        
        transaction.setAmount(num);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 14a
        str = this.readString(Fields.FIELD_C14A, block,
            DTAUSDisk.CRECORD_OFFSETS1[13], DTAUSDisk.CRECORD_LENGTH1[13],
            true, AbstractLogicalFile.ENCODING_ASCII);
        
        this.checkHolder(Fields.FIELD_C14A, block,
            DTAUSDisk.CRECORD_OFFSETS1[13], str, true);
        
        targetAccount.setHolder(str);
        transaction.setTargetAccount(targetAccount);
        
        // Konstanter Teil - Satzaschnitt 2 - Feld 15(1)
        str = this.readString(Fields.FIELD_C15, block + 1,
            DTAUSDisk.CRECORD_OFFSETS2[0], DTAUSDisk.CRECORD_LENGTH2[0], true,
            AbstractLogicalFile.ENCODING_ASCII);
        
        this.checkHolder(Fields.FIELD_C15, block + 1,
            DTAUSDisk.CRECORD_OFFSETS2[0], str, true);
        
        executiveAccount.setHolder(str);
        transaction.setExecutiveAccount(executiveAccount);
        
        // Konstanter Teil - Satzaschnitt 2 - Feld 16(2)
        str = this.readString(Fields.FIELD_C16, block + 1,
            DTAUSDisk.CRECORD_OFFSETS2[1], DTAUSDisk.CRECORD_LENGTH2[1], true,
            AbstractLogicalFile.ENCODING_ASCII);
        
        if(str != null) {
            desc.addDescription(str);
        }
        
        // Konstanter Teil - Satzaschnitt 2 - Feld 17a(3)
        str = this.readString(Fields.FIELD_C17A, block + 1,
            DTAUSDisk.CRECORD_OFFSETS2[2], DTAUSDisk.CRECORD_LENGTH2[2], true,
            AbstractLogicalFile.ENCODING_ASCII);
        
        if(str == null || str.length() < 1) {
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_C17A, IllegalDataMessage.ALPHA_NUMERIC,
                (block + 1) * this.persistence.getBlockSize() +
                DTAUSDisk.CRECORD_OFFSETS2[2], str));
            
        } else {
            cur = this.getConfiguration().getCurrency(str.toCharArray()[0]);
            if(cur == null) {
                this.getMessageRecorder().record(
                    new UnsupportedCurrencyMessage(
                    (block + 1L) * this.persistence.getBlockSize() +
                    DTAUSDisk.CRECORD_OFFSETS2[2], str.toCharArray()[0],
                    this.getConfiguration().getCurrencies(this)));
                
            } else {
                transaction.setCurrency(cur);
            }
        }
        
        //if(header.getLabel().isBank()) {
        // Konstanter Teil - Satzaschnitt 1 - Feld 8
        //    num = this.readNumber(block, DTAUSDisk.CRECORD_OFFSETS1[7],
        //        DTAUSDisk.CRECORD_LENGTH1[7]);
        
        //    transaction.set
        //
        //}
        
        // Erweiterungsteile des 2., 3., 4., 5. und 6. Satzabschnittes.
        for(search = 0; search < extCount; search++) {
            blockOffset = block +
                DTAUSDisk.CRECORD_EXTINDEX_TO_BLOCKOFFSET[search];
            
            num = this.readNumber(
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEFIELD[search], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPELENGTH[search],
                AbstractLogicalFile.ENCODING_ASCII);
            
            str = this.readString(
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEFIELD[search], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEOFFSET[search],
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUELENGTH[search], true,
                AbstractLogicalFile.ENCODING_ASCII);
            
            if(num == 1L) {
                if(transaction.getTargetExt() != null) {
                    this.getMessageRecorder().record(new IllegalDataMessage(
                        DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEFIELD[search],
                        IllegalDataMessage.NUMERIC,
                        blockOffset * this.persistence.getBlockSize() +
                        DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                        Long.valueOf(num)));
                    
                } else {
                    transaction.setTargetExt(str);
                }
            } else if(num == 2L) {
                if(str != null) {
                    desc.addDescription(str);
                }
            } else if(num == 3L) {
                if(transaction.getExecutiveExt() != null) {
                    this.getMessageRecorder().record(new IllegalDataMessage(
                        DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEFIELD[search],
                        IllegalDataMessage.NUMERIC,
                        blockOffset * this.persistence.getBlockSize() +
                        DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                        Long.valueOf(num)));
                    
                } else {
                    transaction.setExecutiveExt(str);
                }
            } else {
                this.getMessageRecorder().record(new IllegalDataMessage(
                    DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEFIELD[search],
                    IllegalDataMessage.NUMERIC,
                    blockOffset * this.persistence.getBlockSize() +
                    DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                    Long.valueOf(num)));
                
            }
        }
        
        transaction.setDescription(desc);
        return transaction;
    }
    
    protected void writeTransaction(final long block,
        final Transaction transaction) {
        
        int i;
        int blockIndex = 1;
        long blockOffset;
        long lastBlockOffset;
        int extIndex;
        String str;
        final BankAccount target = transaction.getTargetAccount();
        final BankAccount executive = transaction.getExecutiveAccount();
        final Transaction.Description desc = transaction.getDescription();
        final TransactionType type = transaction.getType();
        final int descCount;
        int extCount = desc.getDescriptionCount() - 1;
        if(transaction.getExecutiveExt() != null) {
            extCount++;
        }
        
        if(transaction.getTargetExt() != null) {
            extCount++;
        }
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 1
        this.writeNumber(Fields.FIELD_C1, block,
            DTAUSDisk.CRECORD_OFFSETS1[0], DTAUSDisk.CRECORD_LENGTH1[0],
            DTAUSDisk.CRECORD_CONST_LENGTH +
            extCount * DTAUSDisk.CRECORD_EXT_LENGTH,
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 2
        this.writeString(Fields.FIELD_C2, block,
            DTAUSDisk.CRECORD_OFFSETS1[1], DTAUSDisk.CRECORD_LENGTH1[1], "C",
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 3
        this.writeNumber(Fields.FIELD_C3, block,
            DTAUSDisk.CRECORD_OFFSETS1[2], DTAUSDisk.CRECORD_LENGTH1[2],
            transaction.getPrimaryBank(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 4
        this.writeNumber(Fields.FIELD_C4, block,
            DTAUSDisk.CRECORD_OFFSETS1[3], DTAUSDisk.CRECORD_LENGTH1[3],
            target.getBank(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 5
        this.writeNumber(Fields.FIELD_C5, block,
            DTAUSDisk.CRECORD_OFFSETS1[4], DTAUSDisk.CRECORD_LENGTH1[4],
            target.getAccount(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 6
        // TODO -1
        this.writeNumber(-1, block, DTAUSDisk.CRECORD_OFFSETS1[5] - 1, 1, 0L,
            AbstractLogicalFile.ENCODING_ASCII);
        
        this.writeNumber(Fields.FIELD_C6, block,
            DTAUSDisk.CRECORD_OFFSETS1[5], DTAUSDisk.CRECORD_LENGTH1[5],
            transaction.getReference(), AbstractLogicalFile.ENCODING_ASCII);
        
        this.writeNumber(-1, block, DTAUSDisk.CRECORD_OFFSETS1[6] - 1, 1, 0L,
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 1. Satzabschnitt - Felder 7a & 7b
        // TODO -3, +/- 2
        this.writeNumber(Fields.FIELD_C7A, block,
            DTAUSDisk.CRECORD_OFFSETS1[6], DTAUSDisk.CRECORD_LENGTH1[6] - 3,
            type.getKeyType(), AbstractLogicalFile.ENCODING_ASCII);
        
        this.writeNumber(Fields.FIELD_C7B, block,
            DTAUSDisk.CRECORD_OFFSETS1[6] + 2, DTAUSDisk.CRECORD_LENGTH1[6] - 2,
            type.getType(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 8
        this.writeString(Fields.FIELD_C8, block,
            DTAUSDisk.CRECORD_OFFSETS1[7], DTAUSDisk.CRECORD_LENGTH1[7], "",
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 9
        this.writeNumber(Fields.FIELD_C9, block,
            DTAUSDisk.CRECORD_OFFSETS1[8], DTAUSDisk.CRECORD_LENGTH1[8], 0L,
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 10
        this.writeNumber(Fields.FIELD_C10, block,
            DTAUSDisk.CRECORD_OFFSETS1[9], DTAUSDisk.CRECORD_LENGTH1[9],
            executive.getBank(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 11
        this.writeNumber(Fields.FIELD_C11, block,
            DTAUSDisk.CRECORD_OFFSETS1[10], DTAUSDisk.CRECORD_LENGTH1[10],
            executive.getAccount(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 12
        this.writeNumber(Fields.FIELD_C12, block,
            DTAUSDisk.CRECORD_OFFSETS1[11], DTAUSDisk.CRECORD_LENGTH1[11],
            transaction.getAmount(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 13
        this.writeString(Fields.FIELD_C13, block,
            DTAUSDisk.CRECORD_OFFSETS1[12], DTAUSDisk.CRECORD_LENGTH1[12], "",
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 14a
        this.writeString(Fields.FIELD_C14A, block,
            DTAUSDisk.CRECORD_OFFSETS1[13], DTAUSDisk.CRECORD_LENGTH1[13],
            target.getHolder(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 14b
        this.writeString(Fields.FIELD_C14B, block,
            DTAUSDisk.CRECORD_OFFSETS1[14], DTAUSDisk.CRECORD_LENGTH1[14], "",
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 2. Satzabschnitt - Feld 15(1)
        this.writeString(Fields.FIELD_C15, block + 1L,
            DTAUSDisk.CRECORD_OFFSETS2[0], DTAUSDisk.CRECORD_LENGTH2[0],
            executive.getHolder(), AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 2. Satzabschnitt - Feld 16(2)
        this.writeString(Fields.FIELD_C16, block + 1L,
            DTAUSDisk.CRECORD_OFFSETS2[1], DTAUSDisk.CRECORD_LENGTH2[1],
            desc.getDescription(0), AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 2. Satzabschnitt - Feld 17a(3)
        this.writeString(Fields.FIELD_C17A, block + 1L,
            DTAUSDisk.CRECORD_OFFSETS2[2], DTAUSDisk.CRECORD_LENGTH2[2],
            String.valueOf(transaction.getCurrency().getLabel()),
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 2. Satzabschnitt - Feld 17b(4)
        this.writeString(Fields.FIELD_C17B, block + 1L,
            DTAUSDisk.CRECORD_OFFSETS2[3], DTAUSDisk.CRECORD_LENGTH2[3], "",
            AbstractLogicalFile.ENCODING_ASCII);
        
        // Konstanter Teil - 2. Satzabschnitt - Feld 18(5)
        this.writeNumber(Fields.FIELD_C18, block + 1L,
            DTAUSDisk.CRECORD_OFFSETS2[4], DTAUSDisk.CRECORD_LENGTH2[4],
            extCount, AbstractLogicalFile.ENCODING_ASCII);
        
        // Erweiterungs-Teile im zweiten Satzabschnitt initialisieren.
        this.initializeExtensionBlock(blockIndex, block);
        
        // Erweiterungs-Teile.
        extIndex = 0;
        
        blockOffset = block +
            DTAUSDisk.CRECORD_EXTINDEX_TO_BLOCKOFFSET[extIndex];
        
        lastBlockOffset = blockOffset;
        
        // Erweiterung des beteiligten Kontos als ersten Erweiterungsteil.
        if((str = transaction.getTargetExt()) != null) {
            this.writeNumber(
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex],
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPELENGTH[extIndex], 1L,
                AbstractLogicalFile.ENCODING_ASCII);
            
            this.writeString(
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex], str,
                AbstractLogicalFile.ENCODING_ASCII);
            
            extIndex++;
        }
        
        // Verwendungszweck-Zeilen des 2., 3., 4., 5. und 6. Satzabschnittes.
        descCount = desc.getDescriptionCount();
        for(i = 1; i < descCount; i++, extIndex++) {
            blockOffset = block +
                DTAUSDisk.CRECORD_EXTINDEX_TO_BLOCKOFFSET[extIndex];
            
            if(blockOffset != lastBlockOffset) {
                // Nächsten Satzabschnitt initialisieren.
                this.initializeExtensionBlock(++blockIndex, block);
            }
            
            this.writeNumber(
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex],
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPELENGTH[extIndex], 2L,
                AbstractLogicalFile.ENCODING_ASCII);
            
            this.writeString(
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex],
                desc.getDescription(i), AbstractLogicalFile.ENCODING_ASCII);
            
            lastBlockOffset = blockOffset;
        }
        
        // Erweiterung des Auftraggeber-Kontos im letzten Erweiterungsteil.
        if((str = transaction.getExecutiveExt()) != null) {
            blockOffset = block +
                DTAUSDisk.CRECORD_EXTINDEX_TO_BLOCKOFFSET[extIndex];
            
            if(blockOffset != lastBlockOffset) {
                // Nächsten Satzabschnitt initialisieren.
                this.initializeExtensionBlock(++blockIndex, block);
            }
            
            this.writeNumber(
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex],
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPELENGTH[extIndex], 3L,
                AbstractLogicalFile.ENCODING_ASCII);
            
            this.writeString(
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex], str,
                AbstractLogicalFile.ENCODING_ASCII);
            
            extIndex++;
            lastBlockOffset = blockOffset;
        }
    }
    
    
    
    protected int blockCount(final Transaction transaction) {
        int extCount = transaction.getDescription().getDescriptionCount() - 1;
        if(transaction.getExecutiveExt() != null) {
            extCount++;
        }
        
        if(transaction.getTargetExt() != null) {
            extCount++;
        }
        
        return DTAUSDisk.CRECORD_EXTENSIONCOUNT_TO_BLOCKCOUNT[extCount];
    }
    
    protected int blockCount(final long block) throws PhysicalFileError {
        final long extCount = this.readNumber(Fields.FIELD_C18,
            block + 1L, DTAUSDisk.CRECORD_OFFSETS2[4],
            DTAUSDisk.CRECORD_LENGTH2[4], AbstractLogicalFile.ENCODING_ASCII);
        
        return DTAUSDisk.CRECORD_EXTENSIONCOUNT_TO_BLOCKCOUNT[(int) extCount];
    }
    
//-----------------------------------------------------AbstractLogicalFile--
//--DTAUSDisk---------------------------------------------------------------
    
    private void initializeExtensionBlock(final int blockIndex,
        final long block) {
        
        int extIndex;
        int startingExt;
        int endingExt;
        int reservedField;
        int reservedOffset;
        int reservedLength;
        
        if(blockIndex == 1) {
            startingExt = 0;
            endingExt = 1;
            reservedField = Fields.FIELD_C23;
            reservedOffset = DTAUSDisk.CRECORD_OFFSETS2[9];
            reservedLength = DTAUSDisk.CRECORD_LENGTH2[9];
        } else if(blockIndex == 2) {
            startingExt = 2;
            endingExt = 5;
            reservedField = Fields.FIELD_C32;
            reservedOffset = DTAUSDisk.CRECORD_OFFSETS_EXT[8];
            reservedLength = DTAUSDisk.CRECORD_LENGTH_EXT[8];
        } else if(blockIndex == 3) {
            startingExt = 6;
            endingExt = 9;
            reservedField = Fields.FIELD_C41;
            reservedOffset = DTAUSDisk.CRECORD_OFFSETS_EXT[8];
            reservedLength = DTAUSDisk.CRECORD_LENGTH_EXT[8];
        } else if(blockIndex == 4) {
            startingExt = 10;
            endingExt = 13;
            reservedField = Fields.FIELD_C50;
            reservedOffset = DTAUSDisk.CRECORD_OFFSETS_EXT[8];
            reservedLength = DTAUSDisk.CRECORD_LENGTH_EXT[8];
        } else if(blockIndex == 5) {
            startingExt = 14;
            endingExt = 17;
            reservedField = Fields.FIELD_C59;
            reservedOffset = DTAUSDisk.CRECORD_OFFSETS_EXT[8];
            reservedLength = DTAUSDisk.CRECORD_LENGTH_EXT[8];
        } else {
            throw new IllegalArgumentException("blockIndex=" + blockIndex);
        }
        
        // Erweiterungsteile leeren.
        for(extIndex = startingExt; extIndex <= endingExt; extIndex++) {
            final long blockOffset = block +
                DTAUSDisk.CRECORD_EXTINDEX_TO_BLOCKOFFSET[extIndex];
            
            this.writeNumber(
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex],
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPELENGTH[extIndex], 0L,
                AbstractLogicalFile.ENCODING_ASCII);
            
            this.writeString(
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex], "",
                AbstractLogicalFile.ENCODING_ASCII);
            
        }
        
        // Reserve-Feld initialisieren.
        this.writeString(reservedField, block + blockIndex, reservedOffset,
            reservedLength, "", AbstractLogicalFile.ENCODING_ASCII);
        
    }
    
    protected MemoryManager getMemoryManagerImpl() {
        return this.getMemoryManager();
    }
    
    protected Logger getLoggerImpl() {
        return this.getLogger();
    }
    
    protected Utility getUtilityImpl() {
        return this.getUtility();
    }
    
    protected MessageRecorder getMessageRecorderImpl() {
        return this.getMessageRecorder();
    }
    
    protected TaskMonitor getTaskMonitorImpl() {
        return this.getTaskMonitor();
    }
    
    protected Configuration getConfigurationImpl() {
        return this.getConfiguration();
    }
    
    //---------------------------------------------------------------DTAUSDisk--
    
}

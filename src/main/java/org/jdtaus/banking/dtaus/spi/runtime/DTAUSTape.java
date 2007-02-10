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

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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
 * Anlage 3.1.2 DTAUS: Zahlungsverkehrssammelauftrag Magnetbandformat.
 * <p/>
 * <b>Hinweis:</b><br/>
 * Implementierung darf niemals von mehreren Threads gleichzeitig verwendet
 * werden.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class DTAUSTape extends AbstractLogicalFile {
    
    //--Implementation----------------------------------------------------------

    // Dieser Abschnitt wird von jdtaus-source-plugin generiert.

    /** Metadaten dieser Implementierung. */
    private static final Implementation IMPL =
        ModelFactory.getModel().getModules().
        getImplementation(DTAUSTape.class.getName());

    //----------------------------------------------------------Implementation--
    //--Konstanten--------------------------------------------------------------
    
    /** Zeichenkette für Exception-Meldungen. */
    private static final String MSG_BLOCKSIZE = "blockSize=";
    
    /**
     * Index = A Datensatz-Feld - 1,
     * Wert = Offset relativ zum Anfang des Satzabschnittes.
     */
    protected static final int[] ARECORD_OFFSETS = {
        0, 2, 4, 5, 7, 12, 17, 44, 48, 52, 58, 68, 83, 91, 149
    };
    
    /**
     * Index = A Datensatz-Feld - 1,
     * Wert = Länge des Feldes in Byte.
     */
    protected static final int[] ARECORD_LENGTH = {
        2, 2, 1, 2, 5, 5, 27, 4, 4, 6, 10, 15, 8, 58, 1
    };
    
    /**
     * Index = E Datensatz-Feld - 1,
     * Wert = Offset relativ zum Anfang des Satzabschnittes.
     */
    protected static final int[] ERECORD_OFFSETS = {
        0, 2, 4, 5, 10, 14, 21, 30, 39, 46
    };
    
    /**
     * Index = E Datensatz-Feld -1,
     * Wert = Länge des Feldes in Byte.
     */
    protected static final int[] ERECORD_LENGTH = {
        2, 2, 1, 5, 4, 7, 9, 9, 7, 104
    };
    
    /** Länge des konstanten Teiles eines C Datensatzes in Byte. */
    protected static final int CRECORD_CONST_LENGTH = 150;
    
    /** Länge eines Erweiterungsteiles in Byte. */
    protected static final int CRECORD_EXT_LENGTH = 29;
    
    /**
     * Index = C Datensatz-Feld - 1,
     * Wert = Offset relativ zum ersten Satzabschnitt.
     */
    protected static final int[] CRECORD_OFFSETS1 = {
        0, 2, 4, 5, 10, 15, 21, 27, 34, 35, 37, 38, 44, 49, 55, 61, 64, 91,
        118, 145, 146, 148
    };
    
    /**
     * Index = C Datensatz-Feld - 1 (erster Satzabschnitt),
     * Wert = Länge des Feldes in Byte.
     */
    protected static final int[] CRECORD_LENGTH1 = {
        2, 2, 1, 5, 5, 6, 6, 7, 1, 2, 1, 6, 5, 6, 6, 3, 27, 27, 27, 1, 2, 2
    };
    
    /**
     * Index = C Datensatz-Feld des 2., 3. und 4. Satzabschnittes - 1,
     * Wert = Offset relativ zum Anfang des 2., 3. und 4. Satzabschnittes.
     */
    protected static final int[] CRECORD_OFFSETS_EXT = {
        0, 2, 29, 31, 58, 60, 87, 89, 116, 118, 145
    };
    
    /**
     * Index = C Datensatz-Feld des 2., 3. und 4. Satzabschnittes - 1,
     * Wert = Länge des Feldes in Byte.
     */
    protected static final int[] CRECORD_LENGTH_EXT = {
        2, 27, 2, 27, 2, 27, 2, 27, 2, 27, 5
    };
    
    /**
     * Index = Anzahl Erweiterungsteile,
     * Wert = Anzahl benötigter Satzabschnitte.
     */
    protected static final int[] CRECORD_EXTENSIONCOUNT_TO_BLOCKCOUNT = {
        1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4
    };
    
    /**
     * Index = Index Erweiterungsteil,
     * Wert = Satzabschnitt-Offset zu Transaktionsbeginn.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_BLOCKOFFSET = {
        1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3
    };
    
    /**
     * Index = Index Erweiterungsteil,
     * Wert = Anfangsposition des Erweiterungsteils relativ zum Anfang des
     * Satzabschnittes.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_TYPEOFFSET = {
        DTAUSTape.CRECORD_OFFSETS_EXT[0], DTAUSTape.CRECORD_OFFSETS_EXT[2],
        DTAUSTape.CRECORD_OFFSETS_EXT[4], DTAUSTape.CRECORD_OFFSETS_EXT[6],
        DTAUSTape.CRECORD_OFFSETS_EXT[8], DTAUSTape.CRECORD_OFFSETS_EXT[0],
        DTAUSTape.CRECORD_OFFSETS_EXT[2], DTAUSTape.CRECORD_OFFSETS_EXT[4],
        DTAUSTape.CRECORD_OFFSETS_EXT[6], DTAUSTape.CRECORD_OFFSETS_EXT[8],
        DTAUSTape.CRECORD_OFFSETS_EXT[0], DTAUSTape.CRECORD_OFFSETS_EXT[2],
        DTAUSTape.CRECORD_OFFSETS_EXT[4], DTAUSTape.CRECORD_OFFSETS_EXT[6],
        DTAUSTape.CRECORD_OFFSETS_EXT[8]
    };
    
    /**
     * Index = Index Erweiterungsteil,
     * Wert = Anfangsposition des Erweiterungsteils relativ zum Anfang des
     * Satzabschnittes.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_TYPELENGTH = {
        DTAUSTape.CRECORD_LENGTH_EXT[0], DTAUSTape.CRECORD_LENGTH_EXT[2],
        DTAUSTape.CRECORD_LENGTH_EXT[4], DTAUSTape.CRECORD_LENGTH_EXT[6],
        DTAUSTape.CRECORD_LENGTH_EXT[8], DTAUSTape.CRECORD_LENGTH_EXT[0],
        DTAUSTape.CRECORD_LENGTH_EXT[2], DTAUSTape.CRECORD_LENGTH_EXT[4],
        DTAUSTape.CRECORD_LENGTH_EXT[6], DTAUSTape.CRECORD_LENGTH_EXT[8],
        DTAUSTape.CRECORD_LENGTH_EXT[0], DTAUSTape.CRECORD_LENGTH_EXT[2],
        DTAUSTape.CRECORD_LENGTH_EXT[4], DTAUSTape.CRECORD_LENGTH_EXT[6],
        DTAUSTape.CRECORD_LENGTH_EXT[8]
            
    };
    
    /**
     * Index = Index Erweiterungsteil,
     * Wert = Anfangsposition des Erweiterungsteils relativ zum Anfang des
     * Satzabschnittes.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_VALUEOFFSET = {
        DTAUSTape.CRECORD_OFFSETS_EXT[1], DTAUSTape.CRECORD_OFFSETS_EXT[3],
        DTAUSTape.CRECORD_OFFSETS_EXT[5], DTAUSTape.CRECORD_OFFSETS_EXT[7],
        DTAUSTape.CRECORD_OFFSETS_EXT[9], DTAUSTape.CRECORD_OFFSETS_EXT[1],
        DTAUSTape.CRECORD_OFFSETS_EXT[3], DTAUSTape.CRECORD_OFFSETS_EXT[5],
        DTAUSTape.CRECORD_OFFSETS_EXT[7], DTAUSTape.CRECORD_OFFSETS_EXT[9],
        DTAUSTape.CRECORD_OFFSETS_EXT[1], DTAUSTape.CRECORD_OFFSETS_EXT[3],
        DTAUSTape.CRECORD_OFFSETS_EXT[5], DTAUSTape.CRECORD_OFFSETS_EXT[7],
        DTAUSTape.CRECORD_OFFSETS_EXT[9]
    };
    
    /**
     * Index = Index Erweiterungsteil,
     * Wert = Anfangsposition des Erweiterungsteils relativ zum Anfang des
     * Satzabschnittes.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_VALUELENGTH = {
        DTAUSTape.CRECORD_LENGTH_EXT[1], DTAUSTape.CRECORD_LENGTH_EXT[3],
        DTAUSTape.CRECORD_LENGTH_EXT[5], DTAUSTape.CRECORD_LENGTH_EXT[7],
        DTAUSTape.CRECORD_LENGTH_EXT[9], DTAUSTape.CRECORD_LENGTH_EXT[1],
        DTAUSTape.CRECORD_LENGTH_EXT[3], DTAUSTape.CRECORD_LENGTH_EXT[5],
        DTAUSTape.CRECORD_LENGTH_EXT[7], DTAUSTape.CRECORD_LENGTH_EXT[9],
        DTAUSTape.CRECORD_LENGTH_EXT[1], DTAUSTape.CRECORD_LENGTH_EXT[3],
        DTAUSTape.CRECORD_LENGTH_EXT[5], DTAUSTape.CRECORD_LENGTH_EXT[7],
        DTAUSTape.CRECORD_LENGTH_EXT[9]
    };
    
    /**
     * Index = Index Erweiterungsteil,
     * Wert = Anzahl der folgenden Erweiterungsteile im selben Satzabschnitt.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_FOLLOWINGEXTENSIONS = {
        4, 3, 2, 1, 0, 4, 3, 2, 1, 0, 4, 3, 2, 1, 0
    };
    
    /**
     * Index = Index Erweiterungsteil,
     * Wert = Feld-Konstante für das Typen-Feld des Erweiterungsteils.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_TYPEFIELD = {
        Fields.FIELD_C19, Fields.FIELD_C21, Fields.FIELD_C23,
        Fields.FIELD_C25, Fields.FIELD_C27, Fields.FIELD_C30,
        Fields.FIELD_C32, Fields.FIELD_C34, Fields.FIELD_C36,
        Fields.FIELD_C38, Fields.FIELD_C41, Fields.FIELD_C43,
        Fields.FIELD_C45, Fields.FIELD_C47, Fields.FIELD_C48,
        Fields.FIELD_C51, Fields.FIELD_C53, Fields.FIELD_C55,
        Fields.FIELD_C57, Fields.FIELD_C59
    };
    
    /**
     * Index = Index Erweiterungsteil,
     * Wert = Feld-Konstante für das Werte-Feld des Erweiterungsteils.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_VALUEFIELD = {
        Fields.FIELD_C20, Fields.FIELD_C22, Fields.FIELD_C24,
        Fields.FIELD_C26, Fields.FIELD_C28, Fields.FIELD_C31,
        Fields.FIELD_C33, Fields.FIELD_C35, Fields.FIELD_C37,
        Fields.FIELD_C39, Fields.FIELD_C42, Fields.FIELD_C44,
        Fields.FIELD_C46, Fields.FIELD_C48, Fields.FIELD_C49,
        Fields.FIELD_C52, Fields.FIELD_C54, Fields.FIELD_C56,
        Fields.FIELD_C58
    };
    
    //--------------------------------------------------------------Konstanten--
    //--Attribute---------------------------------------------------------------
    
    /** Temporäres Kalendar-Objekt. */
    protected transient Calendar calendar;
    
    //---------------------------------------------------------------Attribute--
    //--Constructors------------------------------------------------------------

    // Dieser Abschnitt wird von jdtaus-source-plugin generiert.

    /** <code>DTAUSTape</code> Implementierungs-Konstruktor.
    * @param meta Metadaten der Implementierung.
    */ 
    protected DTAUSTape(final Implementation meta) {
        super();
    }
    /** <code>DTAUSTape</code> Implementierungs-Konstruktor.
    * @param meta Metadaten der Dependency.
    */ 
    protected DTAUSTape(final Dependency meta) {
        super();
    }

    //------------------------------------------------------------Constructors--
    //--Konstruktoren-----------------------------------------------------------
    
    /**
     * Erzeugt eine neue {@code DTAUSTape} Instanz.
     *
     * @param headerBlock Satzabschnitt, in dem der A-Datensatz erwartet wird.
     * @param persistence zu verwendende {@code StructuredFile}-Implementierung.
     *
     * @throws NullPointerException {@code if(persistence == null)}
     * @throws IllegalArgumentException bei ungültigen Angaben.
     */
    protected DTAUSTape(final long headerBlock,
        final StructuredFileOperations persistence) {
        
        this(DTAUSTape.IMPL);
        if(persistence == null) {
            throw new NullPointerException("persistence");
        }
        
        if(persistence.getBlockSize() != 150) {
            throw new IllegalArgumentException(DTAUSTape.MSG_BLOCKSIZE +
                persistence.getBlockSize());
            
        }
        
        this.calendar = Calendar.getInstance(Locale.GERMANY);
        this.calendar.setLenient(false);
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
                getDependency(DTAUSTape.IMPL.getIdentifier(),
                "TaskMonitor");

            if(ret == null) {
                throw new ContainerError("TaskMonitor");
            }

            if(DTAUSTape.IMPL.getDependencies().
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
                getDependency(DTAUSTape.IMPL.getIdentifier(),
                "MessageRecorder");

            if(ret == null) {
                throw new ContainerError("MessageRecorder");
            }

            if(DTAUSTape.IMPL.getDependencies().
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
                getDependency(DTAUSTape.IMPL.getIdentifier(),
                "MemoryManager");

            if(ret == null) {
                throw new ContainerError("MemoryManager");
            }

            if(DTAUSTape.IMPL.getDependencies().
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
                getDependency(DTAUSTape.IMPL.getIdentifier(),
                "Logger");

            if(ret == null) {
                throw new ContainerError("Logger");
            }

            if(DTAUSTape.IMPL.getDependencies().
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
                getDependency(DTAUSTape.IMPL.getIdentifier(),
                "Configuration");

            if(ret == null) {
                throw new ContainerError("Configuration");
            }

            if(DTAUSTape.IMPL.getDependencies().
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
                getDependency(DTAUSTape.IMPL.getIdentifier(),
                "Utility");

            if(ret == null) {
                throw new ContainerError("Utility");
            }

            if(DTAUSTape.IMPL.getDependencies().
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
        
        int ret = 1;
        // Konstanter Teil - 1. Satzabschnitt - Feld 18
        final long extCount = this.readNumberPackedPositive(
            Fields.FIELD_C18, block, DTAUSTape.CRECORD_OFFSETS1[21],
            DTAUSTape.CRECORD_LENGTH1[21], true);
        
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
            DTAUSTape.ARECORD_OFFSETS[2], DTAUSTape.ARECORD_LENGTH[2], true,
            AbstractLogicalFile.ENCODING_EBCDI);
        
        char ret;
        if(str == null || str.length() != 1) {
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_A2, IllegalDataMessage.CONSTANT,
                block * this.persistence.getBlockSize() +
                DTAUSTape.ARECORD_OFFSETS[2], str));
            
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
    
    protected int blockCount(final Transaction transaction) {
        int extCount = transaction.getDescription().getDescriptionCount() - 1;
        if(transaction.getExecutiveExt() != null) {
            extCount++;
        }
        if(transaction.getTargetExt() != null) {
            extCount++;
        }
        return DTAUSTape.CRECORD_EXTENSIONCOUNT_TO_BLOCKCOUNT[extCount];
    }
    
    protected int blockCount(final long block) throws PhysicalFileError {
        int extCount = (int) this.readNumberPackedPositive(
            Fields.FIELD_C18, block, DTAUSTape.CRECORD_OFFSETS1[21],
            DTAUSTape.CRECORD_LENGTH1[21], true);
        
        if(extCount == -1) {
            extCount = 0;
        }
        
        return DTAUSTape.CRECORD_EXTENSIONCOUNT_TO_BLOCKCOUNT[extCount];
    }
    
    public Header readHeader(final long headerBlock) throws PhysicalFileError {
        long num;
        int cal;
        String str;
        final Currency cur;
        final Date createDate;
        final Date executionDate;
        final HeaderLabel label;
        final Header.Schedule schedule;
        final Header ret;
        final int blockSize;
        boolean isBank = false;
        
        ret = new Header();
        blockSize = this.persistence.getBlockSize();
        
        // Feld 1
        num = this.readNumberBinary(Fields.FIELD_A1, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[0], DTAUSTape.ARECORD_LENGTH[0]);
        
        if(num != blockSize) {
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_A1, IllegalDataMessage.CONSTANT,
                headerBlock * this.persistence.getBlockSize() +
                DTAUSTape.ARECORD_OFFSETS[0], Long.valueOf(num)));
            
        }
        
        // Feld 2
        str = this.readString(Fields.FIELD_A2, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[2], DTAUSTape.ARECORD_LENGTH[2], true,
            AbstractLogicalFile.ENCODING_EBCDI);
        
        if(str == null || str.length() != 1 || str.toCharArray()[0] != 'A') {
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_A1, IllegalDataMessage.CONSTANT,
                headerBlock * this.persistence.getBlockSize() +
                DTAUSTape.ARECORD_OFFSETS[0], str));
            
        }
        
        // Feld 3
        str = this.readString(Fields.FIELD_A3, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[3], DTAUSTape.ARECORD_LENGTH[3], true,
            AbstractLogicalFile.ENCODING_EBCDI);
        
        if(str != null) {
            label = this.getConfiguration().getHeaderLabel(str);
            if(label == null) {
                this.getMessageRecorder().record(
                    new UnsupportedHeaderLabelMessage(
                    headerBlock * this.persistence.getBlockSize() +
                    DTAUSTape.ARECORD_OFFSETS[3], str,
                    this.getConfiguration().getHeaderLabels()));
                
            } else {
                isBank = label.isSendByBank();
                ret.setLabel(label);
            }
        }
        
        // Feld 4
        num = this.readNumberPackedPositive(Fields.FIELD_A4, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[4], DTAUSTape.ARECORD_LENGTH[4], true);
        
        this.checkBankCode(Fields.FIELD_A4, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[4], (int) num, false);
        
        ret.setRecipientBank((int) num);
        
        // Feld 5
        // Nur belegt wenn Absender Kreditinistitut ist, sonst 0.
        num = this.readNumberPackedPositive(Fields.FIELD_A5, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[5], DTAUSTape.ARECORD_LENGTH[5],
            true);
        
        this.checkBankCode(Fields.FIELD_A5, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[5], (int) num, false);
        
        ret.setBankData5(isBank ? (int) num : 0);
        
        // Feld 6
        str = this.readString(Fields.FIELD_A6, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[6], DTAUSTape.ARECORD_LENGTH[6], true,
            AbstractLogicalFile.ENCODING_EBCDI);
        
        ret.setSenderName(str);
        
        // Feld 7
        this.calendar.clear();
        num = this.readNumberPackedPositive(Fields.FIELD_A7, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[7], DTAUSTape.ARECORD_LENGTH[7], true);
        
        cal = (int) Math.floor(num / AbstractLogicalFile.EXP10[4]);
        num -= (cal * AbstractLogicalFile.EXP10[4]);
        this.calendar.set(Calendar.DAY_OF_MONTH, cal);
        cal = (int) Math.floor(num / AbstractLogicalFile.EXP10[2]);
        num -= (cal * AbstractLogicalFile.EXP10[2]);
        this.calendar.set(Calendar.MONTH, cal - 1);
        num = num <= 79L ? 2000L + num : 1900L + num;
        this.calendar.set(Calendar.YEAR, (int) num);
        createDate = this.calendar.getTime();
        
        this.checkCreateDate(Fields.FIELD_A7, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[7], createDate);
        
        // Feld 8
        // Nur belegt wenn Absender Kreditinistitut ist, sonst leer.
        str = this.readString(Fields.FIELD_A8, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[8], DTAUSTape.ARECORD_LENGTH[8],
            true, AbstractLogicalFile.ENCODING_EBCDI);
        
        ret.setBankData8(isBank ? str : "");
        
        // Feld 9
        num = this.readNumberPackedPositive(Fields.FIELD_A9, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[9], DTAUSTape.ARECORD_LENGTH[9], true);
        
        ret.setAccount(num);
        
        // Feld 10
        num = this.readNumber(Fields.FIELD_A10, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[10], DTAUSTape.ARECORD_LENGTH[10],
            AbstractLogicalFile.ENCODING_EBCDI);
        
        ret.setReference(num);
        
        // Feld 11b
        executionDate = this.readLongDate(Fields.FIELD_A11B, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[12], AbstractLogicalFile.ENCODING_EBCDI);
        
        this.checkExecutionDate(Fields.FIELD_A11B, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[12], executionDate);
        
        // Feld 12
        str = this.readString(Fields.FIELD_A12, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[14], DTAUSTape.ARECORD_LENGTH[14], true,
            AbstractLogicalFile.ENCODING_EBCDI);
        
        if(str == null || str.length() < 1) {
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_A12, IllegalDataMessage.ALPHA_NUMERIC,
                headerBlock * this.persistence.getBlockSize() +
                DTAUSTape.ARECORD_OFFSETS[14], str));
            
        } else {
            cur = this.getConfiguration().getCurrency(str.toCharArray()[0]);
            if(cur == null) {
                this.getMessageRecorder().record(
                    new UnsupportedCurrencyMessage(
                    headerBlock * this.persistence.getBlockSize() +
                    DTAUSTape.ARECORD_OFFSETS[14], str.toCharArray()[0],
                    this.getConfiguration().getCurrencies(this)));
                
            } else {
                ret.setCurrency(cur);
            }
        }
        
        schedule = new Header.Schedule(this.calendar.getTime(), executionDate);
        this.checkHeaderSchedule(Fields.FIELD_A11B, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[12], schedule);
        
        ret.setSchedule(schedule);
        return ret;
    }
    
    public void writeHeader(final long headerBlock, final Header header) {
        final Header.Schedule schedule;
        final HeaderLabel label;
        long num = 0L;
        int cal = 0;
        int yy;
        
        schedule = header.getSchedule();
        label = header.getLabel();
        
        // Feld 1
        this.writeNumberBinary(Fields.FIELD_A1, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[0], DTAUSTape.ARECORD_LENGTH[0],
            this.persistence.getBlockSize());
        
        // Feld 1b
        // TODO -1
        this.writeNumberBinary(-1, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[1], DTAUSTape.ARECORD_LENGTH[1], 0L);
        
        // Feld 2
        this.writeString(Fields.FIELD_A2, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[2], DTAUSTape.ARECORD_LENGTH[2],
            "A", AbstractLogicalFile.ENCODING_EBCDI);
        
        // Feld 3
        this.writeString(Fields.FIELD_A3, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[3], DTAUSTape.ARECORD_LENGTH[3],
            label.getLabel(), AbstractLogicalFile.ENCODING_EBCDI);
        
        // Feld 4
        this.writeNumberPackedPositive(Fields.FIELD_A4, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[4], DTAUSTape.ARECORD_LENGTH[4],
            header.getRecipientBank(), true);
        
        // Feld 5
        this.writeNumberPackedPositive(Fields.FIELD_A5, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[5], DTAUSTape.ARECORD_LENGTH[5],
            header.getBankData5(), true);
        
        // Feld 6
        this.writeString(Fields.FIELD_A6, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[6], DTAUSTape.ARECORD_LENGTH[6],
            header.getSenderName(), AbstractLogicalFile.ENCODING_EBCDI);
        
        // Feld 7
        this.calendar.clear();
        this.calendar.setTime(schedule.getCreateDate());
        num = this.calendar.get(Calendar.DAY_OF_MONTH) *
            AbstractLogicalFile.EXP10[4];
        
        num += (this.calendar.get(Calendar.MONTH) + 1) *
            AbstractLogicalFile.EXP10[2];
        
        cal = this.calendar.get(Calendar.YEAR);
        yy = (int) Math.floor(cal / 100.00D);
        cal -= yy * 100.00D;
        num += cal;
        
        this.writeNumberPackedPositive(Fields.FIELD_A7, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[7], DTAUSTape.ARECORD_LENGTH[7], num,
            true);
        
        // Feld 8
        this.writeString(Fields.FIELD_A8, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[8], DTAUSTape.ARECORD_LENGTH[8],
            header.getBankData8(), AbstractLogicalFile.ENCODING_EBCDI);
        
        // Feld 9
        this.writeNumberPackedPositive(Fields.FIELD_A9, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[9], DTAUSTape.ARECORD_LENGTH[9],
            header.getAccount(), true);
        
        // Feld 10
        this.writeNumber(Fields.FIELD_A10, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[10], DTAUSTape.ARECORD_LENGTH[10],
            header.getReference(), AbstractLogicalFile.ENCODING_EBCDI);
        
        // Feld 11a
        this.writeString(Fields.FIELD_A11A, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[11], DTAUSTape.ARECORD_LENGTH[11], "",
            AbstractLogicalFile.ENCODING_EBCDI);
        
        // Feld 11b
        this.writeLongDate(Fields.FIELD_A11B, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[12], schedule.getExecutionDate(),
            AbstractLogicalFile.ENCODING_EBCDI);
        
        // Feld 11c
        this.writeString(Fields.FIELD_A11C, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[13], DTAUSTape.ARECORD_LENGTH[13], "",
            AbstractLogicalFile.ENCODING_EBCDI);
        
        // Feld 12
        this.writeString(Fields.FIELD_A12, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[14], DTAUSTape.ARECORD_LENGTH[14],
            String.valueOf(header.getCurrency().getLabel()),
            AbstractLogicalFile.ENCODING_EBCDI);
        
    }
    
    public Checksum readChecksum(final long checksumBlock) throws
        PhysicalFileError {
        
        long num;
        final String str;
        final Checksum checksum;
        
        checksum = new Checksum();
        
        // Feld 1
        num = this.readNumberBinary(Fields.FIELD_E1, checksumBlock,
            DTAUSTape.ERECORD_OFFSETS[0], DTAUSTape.ERECORD_LENGTH[0]);
        
        if(num != this.persistence.getBlockSize()) {
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_E1, IllegalDataMessage.CONSTANT,
                checksumBlock * this.persistence.getBlockSize() +
                DTAUSTape.ERECORD_OFFSETS[0], Long.valueOf(num)));
            
        }
        
        // Feld 2
        str = this.readString(Fields.FIELD_E2, checksumBlock,
            DTAUSTape.ERECORD_OFFSETS[2], DTAUSTape.ERECORD_LENGTH[2], true,
            AbstractLogicalFile.ENCODING_EBCDI);
        
        if(str == null || str.length() != 1 || str.toCharArray()[0] != 'E') {
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_E2, IllegalDataMessage.CONSTANT,
                checksumBlock * this.persistence.getBlockSize() +
                DTAUSTape.ERECORD_OFFSETS[2], str));
            
        }
        
        // Feld 4
        num = this.readNumberPackedPositive(Fields.FIELD_E4,
            checksumBlock, DTAUSTape.ERECORD_OFFSETS[4],
            DTAUSTape.ERECORD_LENGTH[4], true);
        
        checksum.setTransactionCount((int) num);
        
        // Feld 6
        num = this.readNumberPackedPositive(Fields.FIELD_E6,
            checksumBlock, DTAUSTape.ERECORD_OFFSETS[6],
            DTAUSTape.ERECORD_LENGTH[6], true);
        
        checksum.setSumTargetAccount(num);
        
        // Feld 7
        num = this.readNumberPackedPositive(Fields.FIELD_E7,
            checksumBlock, DTAUSTape.ERECORD_OFFSETS[7],
            DTAUSTape.ERECORD_LENGTH[7], true);
        
        checksum.setSumTargetBank(num);
        
        // Feld 8
        num = this.readNumberPackedPositive(Fields.FIELD_E8,
            checksumBlock, DTAUSTape.ERECORD_OFFSETS[8],
            DTAUSTape.ERECORD_LENGTH[8], true);
        
        checksum.setSumAmount(num);
        return checksum;
    }
    
    public void writeChecksum(final long checksumBlock,
        final Checksum checksum) {
        
        // Feld 1
        this.writeNumberBinary(Fields.FIELD_E1, checksumBlock,
            DTAUSTape.ERECORD_OFFSETS[0], DTAUSTape.ERECORD_LENGTH[0],
            this.persistence.getBlockSize());
        
        // Feld 1b
        // TODO -1
        this.writeNumberBinary(-1, checksumBlock, DTAUSTape.ERECORD_OFFSETS[1],
            DTAUSTape.ERECORD_LENGTH[1], 0L);
        
        // Feld 2
        this.writeString(Fields.FIELD_E2, checksumBlock,
            DTAUSTape.ERECORD_OFFSETS[2], DTAUSTape.ERECORD_LENGTH[2], "E",
            AbstractLogicalFile.ENCODING_EBCDI);
        
        // Feld 3
        this.writeString(Fields.FIELD_E3, checksumBlock,
            DTAUSTape.ERECORD_OFFSETS[3], DTAUSTape.ERECORD_LENGTH[3], "",
            AbstractLogicalFile.ENCODING_EBCDI);
        
        // Feld 4
        this.writeNumberPackedPositive(Fields.FIELD_E4, checksumBlock,
            DTAUSTape.ERECORD_OFFSETS[4], DTAUSTape.ERECORD_LENGTH[4],
            checksum.getTransactionCount(), true);
        
        // Feld 5
        this.writeNumberPackedPositive(Fields.FIELD_E5, checksumBlock,
            DTAUSTape.ERECORD_OFFSETS[5], DTAUSTape.ERECORD_LENGTH[5], 0L,
            false);
        
        // Feld 6
        this.writeNumberPackedPositive(Fields.FIELD_E6, checksumBlock,
            DTAUSTape.ERECORD_OFFSETS[6], DTAUSTape.ERECORD_LENGTH[6],
            checksum.getSumTargetAccount(), true);
        
        // Feld 7
        this.writeNumberPackedPositive(Fields.FIELD_E7, checksumBlock,
            DTAUSTape.ERECORD_OFFSETS[7], DTAUSTape.ERECORD_LENGTH[7],
            checksum.getSumTargetBank(), true);
        
        // Feld 8
        this.writeNumberPackedPositive(Fields.FIELD_E8, checksumBlock,
            DTAUSTape.ERECORD_OFFSETS[8], DTAUSTape.ERECORD_LENGTH[8],
            checksum.getSumAmount(), true);
        
        // Feld 9
        this.writeString(Fields.FIELD_E9, checksumBlock,
            DTAUSTape.ERECORD_OFFSETS[9], DTAUSTape.ERECORD_LENGTH[9], "",
            AbstractLogicalFile.ENCODING_EBCDI);
        
    }
    
    public Transaction readTransaction(final long block,
        final Transaction transaction) throws PhysicalFileError {
        
        long num;
        String str;
        int search;
        int keyType;
        long blockOffset;
        final long extCount;
        final Currency cur;
        final TransactionType type;
        final Transaction.Description desc = new Transaction.Description();
        final BankAccount targetAccount = new BankAccount();
        final BankAccount executiveAccount = new BankAccount();
        
        transaction.setExecutiveExt(null);
        transaction.setTargetExt(null);
        // Konstanter Teil - 1. Satzabschnitt - Feld 18
        extCount = this.readNumberPackedPositive(Fields.FIELD_C18, block,
            DTAUSTape.CRECORD_OFFSETS1[21], DTAUSTape.CRECORD_LENGTH1[21],
            true);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 1
        num = this.readNumberBinary(Fields.FIELD_C1, block,
            DTAUSTape.CRECORD_OFFSETS1[0], DTAUSTape.CRECORD_LENGTH1[0]);
        
        if(num != DTAUSTape.CRECORD_CONST_LENGTH +
            extCount * DTAUSTape.CRECORD_EXT_LENGTH) {
            
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_C1, IllegalDataMessage.NUMERIC,
                block * this.persistence.getBlockSize() +
                DTAUSTape.CRECORD_OFFSETS1[0], Long.valueOf(num)));
            
        }
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 2
        str = this.readString(Fields.FIELD_C2, block,
            DTAUSTape.CRECORD_OFFSETS1[2], DTAUSTape.CRECORD_LENGTH1[2], true,
            AbstractLogicalFile.ENCODING_EBCDI);
        
        if(str == null || str.length() != 1 || str.toCharArray()[0] != 'C') {
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_C2, IllegalDataMessage.CONSTANT,
                block * this.persistence.getBlockSize() +
                DTAUSTape.CRECORD_OFFSETS1[2], str));
            
        }
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 3
        num = this.readNumberPackedPositive(Fields.FIELD_C3, block,
            DTAUSTape.CRECORD_OFFSETS1[3], DTAUSTape.CRECORD_LENGTH1[3], true);
        
        this.checkBankCode(Fields.FIELD_C3, block,
            DTAUSTape.CRECORD_OFFSETS1[3], (int) num, false);
        
        transaction.setPrimaryBank((int) num);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 4
        num = this.readNumberPackedPositive(Fields.FIELD_C4, block,
            DTAUSTape.CRECORD_OFFSETS1[4], DTAUSTape.CRECORD_LENGTH1[4], true);
        
        this.checkBankCode(Fields.FIELD_C4, block,
            DTAUSTape.CRECORD_OFFSETS1[4], (int) num, true);
        
        targetAccount.setBank((int) num);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 5
        num = this.readNumberPackedPositive(Fields.FIELD_C5, block,
            DTAUSTape.CRECORD_OFFSETS1[5], DTAUSTape.CRECORD_LENGTH1[5], true);
        
        this.checkAccountCode(Fields.FIELD_C5, block,
            DTAUSTape.CRECORD_OFFSETS1[5], num, true);
        
        targetAccount.setAccount(num);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 6a
        num = this.readNumberPackedPositive(Fields.FIELD_C6A, block,
            DTAUSTape.CRECORD_OFFSETS1[6], DTAUSTape.CRECORD_LENGTH1[6], false);
        
        transaction.setReference(num);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 6b
        //num = this.readNumberPackedPositive(block,
        //    DTAUSTape.CRECORD_OFFSETS1[7], DTAUSTape.CRECORD_LENGTH1[7], true);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 7a
        keyType = (int) this.readNumberPackedPositive(Fields.FIELD_C7A,
            block, DTAUSTape.CRECORD_OFFSETS1[8], DTAUSTape.CRECORD_LENGTH1[8],
            false);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 7b
        num = this.readNumberPackedPositive(Fields.FIELD_C7B, block,
            DTAUSTape.CRECORD_OFFSETS1[9], DTAUSTape.CRECORD_LENGTH1[9], true);
        
        type = this.getConfiguration().getTransactionType(keyType, (int) num);
        if(type == null) {
            this.getMessageRecorder().record(
                new UnsupportedTransactionTypeMessage(
                block * this.persistence.getBlockSize() +
                DTAUSTape.CRECORD_OFFSETS1[8], keyType, (int) num,
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
        num = this.readNumberPackedPositive(Fields.FIELD_C10, block,
            DTAUSTape.CRECORD_OFFSETS1[12], DTAUSTape.CRECORD_LENGTH1[12],
            true);
        
        this.checkBankCode(Fields.FIELD_C10, block,
            DTAUSTape.CRECORD_OFFSETS1[12], (int) num, true);
        
        executiveAccount.setBank((int) num);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 11
        num = this.readNumberPackedPositive(Fields.FIELD_C11, block,
            DTAUSTape.CRECORD_OFFSETS1[13], DTAUSTape.CRECORD_LENGTH1[13],
            true);
        
        this.checkAccountCode(Fields.FIELD_C11, block,
            DTAUSTape.CRECORD_OFFSETS1[13], num, true);
        
        executiveAccount.setAccount(num);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 12
        num = this.readNumberPackedPositive(Fields.FIELD_C12, block,
            DTAUSTape.CRECORD_OFFSETS1[14], DTAUSTape.CRECORD_LENGTH1[14],
            true);
        
        this.checkAmount(Fields.FIELD_C12, block,
            DTAUSTape.CRECORD_OFFSETS1[14], num, true);
        
        transaction.setAmount(num);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 14
        str = this.readString(Fields.FIELD_C14, block,
            DTAUSTape.CRECORD_OFFSETS1[16], DTAUSTape.CRECORD_LENGTH1[16], true,
            AbstractLogicalFile.ENCODING_EBCDI);
        
        this.checkHolder(Fields.FIELD_C14, block,
            DTAUSTape.CRECORD_OFFSETS1[16], str, true);
        
        targetAccount.setHolder(str);
        transaction.setTargetAccount(targetAccount);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 15
        str = this.readString(Fields.FIELD_C15, block,
            DTAUSTape.CRECORD_OFFSETS1[17], DTAUSTape.CRECORD_LENGTH1[17], true,
            AbstractLogicalFile.ENCODING_EBCDI);
        
        executiveAccount.setHolder(str);
        transaction.setExecutiveAccount(executiveAccount);
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 16
        str = this.readString(Fields.FIELD_C16, block,
            DTAUSTape.CRECORD_OFFSETS1[18], DTAUSTape.CRECORD_LENGTH1[18], true,
            AbstractLogicalFile.ENCODING_EBCDI);
        
        if(str != null) {
            desc.addDescription(str);
        }
        
        // Konstanter Teil - Satzaschnitt 1 - Feld 17a
        str = this.readString(Fields.FIELD_C17A, block,
            DTAUSTape.CRECORD_OFFSETS1[19], DTAUSTape.CRECORD_LENGTH1[19], true,
            AbstractLogicalFile.ENCODING_EBCDI);
        
        if(str == null || str.length() < 1) {
            this.getMessageRecorder().record(new IllegalDataMessage(
                Fields.FIELD_C17A, IllegalDataMessage.ALPHA_NUMERIC,
                block * this.persistence.getBlockSize() +
                DTAUSTape.CRECORD_OFFSETS1[19], str));
            
        } else {
            cur = this.getConfiguration().getCurrency(str.toCharArray()[0]);
            if(cur == null) {
                this.getMessageRecorder().record(
                    new UnsupportedCurrencyMessage(
                    block * this.persistence.getBlockSize() +
                    DTAUSTape.CRECORD_OFFSETS1[19], str.toCharArray()[0],
                    this.getConfiguration().getCurrencies(this)));
                
            } else {
                transaction.setCurrency(cur);
            }
        }
        //if(header.getLabel().isBank()) {
        // Konstanter Teil - Satzaschnitt 1 - Feld 8
        //    num = this.readNumber(block, DTAUSTape.CRECORD_OFFSETS1[7],
        //        DTAUSTape.CRECORD_LENGTH1[7]);
        
        //    transaction.set
        //
        //}
        
        // Erweiterungsteile des 2., 3., und 4. Satzabschnitts.
        for(search = 0; search < extCount; search++) {
            blockOffset = block +
                DTAUSTape.CRECORD_EXTINDEX_TO_BLOCKOFFSET[search];
            
            num = this.readNumber(
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[search], blockOffset,
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPELENGTH[search],
                AbstractLogicalFile.ENCODING_EBCDI);
            
            str = this.readString(
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[search], blockOffset,
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUEOFFSET[search],
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUELENGTH[search], true,
                AbstractLogicalFile.ENCODING_EBCDI);
            
            if(num == 1L) {
                if(transaction.getTargetExt() != null) {
                    this.getMessageRecorder().record(new IllegalDataMessage(
                        DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[search],
                        IllegalDataMessage.NUMERIC,
                        blockOffset * this.persistence.getBlockSize() +
                        DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
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
                        DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[search],
                        IllegalDataMessage.NUMERIC,
                        blockOffset * this.persistence.getBlockSize() +
                        DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                        Long.valueOf(num)));
                    
                } else {
                    transaction.setExecutiveExt(str);
                }
            } else {
                this.getMessageRecorder().record(new IllegalDataMessage(
                    DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[search],
                    IllegalDataMessage.NUMERIC,
                    blockOffset * this.persistence.getBlockSize() +
                    DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                    Long.valueOf(num)));
                
            }
        }
        
        transaction.setDescription(desc);
        return transaction;
    }
    
    public void writeTransaction(final long block,
        final Transaction transaction) {
        
        int i;
        long blockOffset;
        int extIndex;
        int followingIndex;
        String str;
        final TransactionType type = transaction.getType();
        final BankAccount target = transaction.getTargetAccount();
        final BankAccount executive = transaction.getExecutiveAccount();
        final Transaction.Description desc = transaction.getDescription();
        final int descCount;
        int extCount = desc.getDescriptionCount() - 1;
        if(transaction.getExecutiveExt() != null) {
            extCount++;
        }
        if(transaction.getTargetExt() != null) {
            extCount++;
        }
        // Konstanter Teil - 1. Satzabschnitt - Feld 1a
        this.writeNumberBinary(Fields.FIELD_C1, block,
            DTAUSTape.CRECORD_OFFSETS1[0], DTAUSTape.CRECORD_LENGTH1[0],
            DTAUSTape.CRECORD_CONST_LENGTH + extCount *
            DTAUSTape.CRECORD_EXT_LENGTH);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 1b
        this.writeNumberBinary(Fields.FIELD_C1, block,
            DTAUSTape.CRECORD_OFFSETS1[1], DTAUSTape.CRECORD_LENGTH1[1], 0L);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 2
        this.writeString(Fields.FIELD_C2, block,
            DTAUSTape.CRECORD_OFFSETS1[2], DTAUSTape.CRECORD_LENGTH1[2], "C",
            AbstractLogicalFile.ENCODING_EBCDI);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 3
        this.writeNumberPackedPositive(Fields.FIELD_C3, block,
            DTAUSTape.CRECORD_OFFSETS1[3], DTAUSTape.CRECORD_LENGTH1[3],
            transaction.getPrimaryBank(), true);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 4
        this.writeNumberPackedPositive(Fields.FIELD_C4, block,
            DTAUSTape.CRECORD_OFFSETS1[4], DTAUSTape.CRECORD_LENGTH1[4],
            target.getBank(), true);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 5
        this.writeNumberPackedPositive(Fields.FIELD_C5, block,
            DTAUSTape.CRECORD_OFFSETS1[5], DTAUSTape.CRECORD_LENGTH1[5],
            target.getAccount(), true);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 6a
        this.writeNumberPackedPositive(Fields.FIELD_C6A, block,
            DTAUSTape.CRECORD_OFFSETS1[6], DTAUSTape.CRECORD_LENGTH1[6],
            transaction.getReference(), false);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 6b
        this.writeNumberPackedPositive(Fields.FIELD_C6B, block,
            DTAUSTape.CRECORD_OFFSETS1[7], DTAUSTape.CRECORD_LENGTH1[7], 0L,
            true);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 7a
        this.writeNumberPackedPositive(Fields.FIELD_C7A, block,
            DTAUSTape.CRECORD_OFFSETS1[8], DTAUSTape.CRECORD_LENGTH1[8],
            type.getKeyType(), false);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 7b
        this.writeNumberPackedPositive(Fields.FIELD_C7B, block,
            DTAUSTape.CRECORD_OFFSETS1[9], DTAUSTape.CRECORD_LENGTH1[9],
            type.getType(), true);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 8
        this.writeString(Fields.FIELD_C8, block,
            DTAUSTape.CRECORD_OFFSETS1[10], DTAUSTape.CRECORD_LENGTH1[10], "",
            AbstractLogicalFile.ENCODING_EBCDI);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 9
        this.writeNumberPackedPositive(Fields.FIELD_C9, block,
            DTAUSTape.CRECORD_OFFSETS1[11], DTAUSTape.CRECORD_LENGTH1[11], 0L,
            true);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 10
        this.writeNumberPackedPositive(Fields.FIELD_C10, block,
            DTAUSTape.CRECORD_OFFSETS1[12], DTAUSTape.CRECORD_LENGTH1[12],
            executive.getBank(), true);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 11
        this.writeNumberPackedPositive(Fields.FIELD_C11, block,
            DTAUSTape.CRECORD_OFFSETS1[13], DTAUSTape.CRECORD_LENGTH1[13],
            executive.getAccount(), true);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 12
        this.writeNumberPackedPositive(Fields.FIELD_C12, block,
            DTAUSTape.CRECORD_OFFSETS1[14], DTAUSTape.CRECORD_LENGTH1[14],
            transaction.getAmount(), true);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 13
        this.writeString(Fields.FIELD_C13, block,
            DTAUSTape.CRECORD_OFFSETS1[15], DTAUSTape.CRECORD_LENGTH1[15], "",
            AbstractLogicalFile.ENCODING_EBCDI);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 14
        this.writeString(Fields.FIELD_C14, block,
            DTAUSTape.CRECORD_OFFSETS1[16], DTAUSTape.CRECORD_LENGTH1[16],
            target.getHolder(), AbstractLogicalFile.ENCODING_EBCDI);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 15
        this.writeString(Fields.FIELD_C15, block,
            DTAUSTape.CRECORD_OFFSETS1[17], DTAUSTape.CRECORD_LENGTH1[17],
            executive.getHolder(), AbstractLogicalFile.ENCODING_EBCDI);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 16
        this.writeString(Fields.FIELD_C16, block,
            DTAUSTape.CRECORD_OFFSETS1[18], DTAUSTape.CRECORD_LENGTH1[18],
            desc.getDescription(0), AbstractLogicalFile.ENCODING_EBCDI);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 17a
        this.writeString(Fields.FIELD_C17A, block,
            DTAUSTape.CRECORD_OFFSETS1[19], DTAUSTape.CRECORD_LENGTH1[19],
            String.valueOf(transaction.getCurrency().getLabel()),
            AbstractLogicalFile.ENCODING_EBCDI);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 17b
        this.writeString(Fields.FIELD_C17B, block,
            DTAUSTape.CRECORD_OFFSETS1[20], DTAUSTape.CRECORD_LENGTH1[20], "",
            AbstractLogicalFile.ENCODING_EBCDI);
        
        // Konstanter Teil - 1. Satzabschnitt - Feld 18
        this.writeNumberPackedPositive(Fields.FIELD_C18, block,
            DTAUSTape.CRECORD_OFFSETS1[21], DTAUSTape.CRECORD_LENGTH1[21],
            extCount, true);
        
        // Erweiterungsteile des 2., 3., und 4. Satzabschnittes.
        descCount = desc.getDescriptionCount();
        extIndex = 0;
        if((str = transaction.getTargetExt()) != null) {
            blockOffset = block +
                DTAUSTape.CRECORD_EXTINDEX_TO_BLOCKOFFSET[extIndex];
            
            this.writeNumber(
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex], blockOffset,
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex],
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPELENGTH[extIndex], 1L,
                AbstractLogicalFile.ENCODING_EBCDI);
            
            this.writeString(
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex], blockOffset,
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex], str,
                AbstractLogicalFile.ENCODING_EBCDI);
            
            // Folgende Erweiterungsteile im selben Satzabschnitt leeren.
            for(followingIndex =
                DTAUSTape.CRECORD_EXTINDEX_TO_FOLLOWINGEXTENSIONS[extIndex]
                ;followingIndex > 0; followingIndex--) {
                
                this.writeNumber(
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[
                    extIndex + followingIndex], blockOffset,
                    DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[
                    extIndex + followingIndex],
                    DTAUSTape.CRECORD_EXTINDEX_TO_TYPELENGTH[
                    extIndex + followingIndex], 0L,
                    AbstractLogicalFile.ENCODING_EBCDI);
                
                this.writeString(
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[
                    extIndex + followingIndex], blockOffset,
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUEOFFSET[
                    extIndex + followingIndex],
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUELENGTH[
                    extIndex + followingIndex], "",
                    AbstractLogicalFile.ENCODING_EBCDI);
                
            }
            
            // Reservefeld des 2. Satzabschnitts leeren.
            this.writeString(
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex] + 1,
                blockOffset,
                DTAUSTape.CRECORD_OFFSETS_EXT[10],
                DTAUSTape.CRECORD_LENGTH_EXT[10], "",
                AbstractLogicalFile.ENCODING_EBCDI);
            
            extIndex++;
        }
        for(i = 1; i < descCount; i++, extIndex++) {
            blockOffset = block +
                DTAUSTape.CRECORD_EXTINDEX_TO_BLOCKOFFSET[extIndex];
            
            this.writeNumber(
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex], blockOffset,
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex],
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPELENGTH[extIndex], 2L,
                AbstractLogicalFile.ENCODING_EBCDI);
            
            this.writeString(
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex], blockOffset,
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex],
                desc.getDescription(i), AbstractLogicalFile.ENCODING_EBCDI);
            
            // Folgende Erweiterungsteile im selben Satzabschnitt leeren.
            for(followingIndex =
                DTAUSTape.CRECORD_EXTINDEX_TO_FOLLOWINGEXTENSIONS[extIndex]
                ;followingIndex > 0; followingIndex--) {
                
                this.writeNumber(
                    DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[
                    extIndex + followingIndex], blockOffset,
                    DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[
                    extIndex + followingIndex],
                    DTAUSTape.CRECORD_EXTINDEX_TO_TYPELENGTH[
                    extIndex + followingIndex], 0L,
                    AbstractLogicalFile.ENCODING_EBCDI);
                
                this.writeString(
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[
                    extIndex + followingIndex], blockOffset,
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUEOFFSET[
                    extIndex + followingIndex],
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUELENGTH[
                    extIndex + followingIndex], "",
                    AbstractLogicalFile.ENCODING_EBCDI);
                
            }
            
            // Reservefeld des 2., 3.  und 4. Satzabschnitts leeren.
            this.writeString(DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[
                DTAUSTape.CRECORD_EXTINDEX_TO_FOLLOWINGEXTENSIONS[
                extIndex] + extIndex] + 7, blockOffset,
                DTAUSTape.CRECORD_OFFSETS_EXT[10],
                DTAUSTape.CRECORD_LENGTH_EXT[10], "",
                AbstractLogicalFile.ENCODING_EBCDI);
            
        }
        if((str = transaction.getExecutiveExt()) != null) {
            blockOffset = block +
                DTAUSTape.CRECORD_EXTINDEX_TO_BLOCKOFFSET[extIndex];
            
            this.writeNumber(
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex], blockOffset,
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex],
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPELENGTH[extIndex], 3L,
                AbstractLogicalFile.ENCODING_EBCDI);
            
            this.writeString(
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex], blockOffset,
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex], str,
                AbstractLogicalFile.ENCODING_EBCDI);
            
            // Folgende Erweiterungsteile im selben Satzabschnitt leeren.
            for(followingIndex =
                DTAUSTape.CRECORD_EXTINDEX_TO_FOLLOWINGEXTENSIONS[extIndex]
                ;followingIndex > 0; followingIndex--) {
                
                this.writeNumber(
                    DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[
                    extIndex + followingIndex], blockOffset,
                    DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[
                    extIndex + followingIndex],
                    DTAUSTape.CRECORD_EXTINDEX_TO_TYPELENGTH[
                    extIndex + followingIndex], 0L,
                    AbstractLogicalFile.ENCODING_EBCDI);
                
                this.writeString(
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[
                    extIndex + followingIndex], blockOffset,
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUEOFFSET[
                    extIndex + followingIndex],
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUELENGTH[
                    extIndex + followingIndex], "",
                    AbstractLogicalFile.ENCODING_EBCDI);
                
            }
            
            // Reservefeld des 2., 3. oder 4. Satzabschnitts leeren.
            this.writeString(DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[
                DTAUSTape.CRECORD_EXTINDEX_TO_FOLLOWINGEXTENSIONS[
                extIndex] + extIndex] + 7, blockOffset,
                DTAUSTape.CRECORD_OFFSETS_EXT[10],
                DTAUSTape.CRECORD_LENGTH_EXT[10], "",
                AbstractLogicalFile.ENCODING_EBCDI);
            
        }
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
    
    //-----------------------------------------------------AbstractLogicalFile--
    
}

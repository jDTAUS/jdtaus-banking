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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import org.jdtaus.banking.AlphaNumericText27;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.Kontonummer;
import org.jdtaus.banking.Referenznummer;
import org.jdtaus.banking.Textschluessel;
import org.jdtaus.banking.TextschluesselVerzeichnis;
import org.jdtaus.banking.dtaus.Checksum;
import org.jdtaus.banking.dtaus.Fields;
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.LogicalFileType;
import org.jdtaus.banking.dtaus.PhysicalFileError;
import org.jdtaus.banking.dtaus.Transaction;
import org.jdtaus.banking.dtaus.spi.CurrencyDirectory;
import org.jdtaus.banking.dtaus.spi.ThreadLocalMessages;
import org.jdtaus.banking.dtaus.spi.runtime.messages.CurrencyViolationMessage;
import org.jdtaus.banking.dtaus.spi.runtime.messages.IllegalDataMessage;
import org.jdtaus.banking.dtaus.spi.runtime.messages.IllegalScheduleMessage;
import org.jdtaus.core.container.ContainerError;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.ContextFactory;
import org.jdtaus.core.container.ContextInitializer;
import org.jdtaus.core.container.Dependency;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.io.spi.StructuredFileOperations;
import org.jdtaus.core.lang.spi.MemoryManager;
import org.jdtaus.core.logging.spi.Logger;
import org.jdtaus.core.monitor.spi.TaskMonitor;
import org.jdtaus.core.text.MessageEvent;
import org.jdtaus.core.text.spi.ApplicationLogger;

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

    //--Konstanten--------------------------------------------------------------

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

    // This section is generated by jdtaus-source-plugin.

    /** Protected <code>DTAUSTape</code> implementation constructor.
    * @param meta Implementation meta-data.
    */
    protected DTAUSTape(final Implementation meta) {
        super();
    }
    /** Protected <code>DTAUSTape</code> dependency constructor.
    * @param meta Dependency meta-data.
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

        this(ModelFactory.getModel().getModules().
            getImplementation(DTAUSTape.class.getName()));

        if(persistence == null) {
            throw new NullPointerException("persistence");
        }

        if(persistence.getBlockSize() != 150) {
            throw new IllegalArgumentException(
                Integer.toString(persistence.getBlockSize()));

        }

        this.calendar = Calendar.getInstance(Locale.GERMANY);
        this.calendar.setLenient(false);
        this.setStructuredFile(persistence);
        this.setHeaderBlock(headerBlock);
    }

    //-----------------------------------------------------------Konstruktoren--
    //--Dependencies------------------------------------------------------------

    // This section is generated by jdtaus-source-plugin.

    /** Configured <code>CurrencyDirectory</code> implementation. */
    private transient CurrencyDirectory _dependency5;

    /** <code>CurrencyDirectory</code> implementation getter. */
    private CurrencyDirectory getCurrencyDirectory() {
        CurrencyDirectory ret = null;
        if(this._dependency5 != null) {
           ret = this._dependency5;
        } else {
            ret = (CurrencyDirectory) ContainerFactory.getContainer().
                getDependency(DTAUSTape.class,
                "CurrencyDirectory");

            if(ret == null) {
                throw new ContainerError("CurrencyDirectory");
            }

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSTape.class.getName()).
                getDependencies().getDependency("CurrencyDirectory").
                isBound()) {

                this._dependency5 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext())) {

            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }
    /** Configured <code>TextschluesselVerzeichnis</code> implementation. */
    private transient TextschluesselVerzeichnis _dependency4;

    /** <code>TextschluesselVerzeichnis</code> implementation getter. */
    private TextschluesselVerzeichnis getTextschluesselVerzeichnis() {
        TextschluesselVerzeichnis ret = null;
        if(this._dependency4 != null) {
           ret = this._dependency4;
        } else {
            ret = (TextschluesselVerzeichnis) ContainerFactory.getContainer().
                getDependency(DTAUSTape.class,
                "TextschluesselVerzeichnis");

            if(ret == null) {
                throw new ContainerError("TextschluesselVerzeichnis");
            }

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSTape.class.getName()).
                getDependencies().getDependency("TextschluesselVerzeichnis").
                isBound()) {

                this._dependency4 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext())) {

            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }
    /** Configured <code>TaskMonitor</code> implementation. */
    private transient TaskMonitor _dependency3;

    /** <code>TaskMonitor</code> implementation getter. */
    private TaskMonitor getTaskMonitor() {
        TaskMonitor ret = null;
        if(this._dependency3 != null) {
           ret = this._dependency3;
        } else {
            ret = (TaskMonitor) ContainerFactory.getContainer().
                getDependency(DTAUSTape.class,
                "TaskMonitor");

            if(ret == null) {
                throw new ContainerError("TaskMonitor");
            }

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSTape.class.getName()).
                getDependencies().getDependency("TaskMonitor").
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
    /** Configured <code>ApplicationLogger</code> implementation. */
    private transient ApplicationLogger _dependency2;

    /** <code>ApplicationLogger</code> implementation getter. */
    private ApplicationLogger getApplicationLogger() {
        ApplicationLogger ret = null;
        if(this._dependency2 != null) {
           ret = this._dependency2;
        } else {
            ret = (ApplicationLogger) ContainerFactory.getContainer().
                getDependency(DTAUSTape.class,
                "ApplicationLogger");

            if(ret == null) {
                throw new ContainerError("ApplicationLogger");
            }

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSTape.class.getName()).
                getDependencies().getDependency("ApplicationLogger").
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
    /** Configured <code>MemoryManager</code> implementation. */
    private transient MemoryManager _dependency1;

    /** <code>MemoryManager</code> implementation getter. */
    private MemoryManager getMemoryManager() {
        MemoryManager ret = null;
        if(this._dependency1 != null) {
           ret = this._dependency1;
        } else {
            ret = (MemoryManager) ContainerFactory.getContainer().
                getDependency(DTAUSTape.class,
                "MemoryManager");

            if(ret == null) {
                throw new ContainerError("MemoryManager");
            }

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSTape.class.getName()).
                getDependencies().getDependency("MemoryManager").
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
    /** Configured <code>Logger</code> implementation. */
    private transient Logger _dependency0;

    /** <code>Logger</code> implementation getter. */
    private Logger getLogger() {
        Logger ret = null;
        if(this._dependency0 != null) {
           ret = this._dependency0;
        } else {
            ret = (Logger) ContainerFactory.getContainer().
                getDependency(DTAUSTape.class,
                "Logger");

            if(ret == null) {
                throw new ContainerError("Logger");
            }

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSTape.class.getName()).
                getDependencies().getDependency("Logger").
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

            if(t.getAmount() != null) {
                checksum.setSumAmount(checksum.getSumAmount() +
                    t.getAmount().longValue()); // TODO longValueExact()
            }

            if(t.getTargetAccount() != null) {
                checksum.setSumTargetAccount(checksum.getSumTargetAccount() +
                    t.getTargetAccount().longValue());

            }

            if(t.getTargetBank() != null) {
                checksum.setSumTargetBank(checksum.getSumTargetBank() +
                    t.getTargetBank().intValue());

            }

            ret = CRECORD_EXTENSIONCOUNT_TO_BLOCKCOUNT[(int) extCount];
        }

        return ret;
    }

    protected char getBlockType(final long block) throws PhysicalFileError {
        // Feld 2
        final String str = this.readAlphaNumeric(Fields.FIELD_A2, block,
            DTAUSTape.ARECORD_OFFSETS[2], DTAUSTape.ARECORD_LENGTH[2],
            AbstractLogicalFile.ENCODING_EBCDI);

        char ret;
        if(str == null || str.length() != 1) {
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                Fields.FIELD_A2, IllegalDataMessage.TYPE_CONSTANT,
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
        Long Num;
        int cal;
        String str;
        final Currency cur;
        final Date createDate;
        final Date executionDate;
        final LogicalFileType label;
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
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                Fields.FIELD_A1, IllegalDataMessage.TYPE_CONSTANT,
                headerBlock * this.persistence.getBlockSize() +
                DTAUSTape.ARECORD_OFFSETS[0], Long.toString(num)));

        }

        // Feld 2
        str = this.readAlphaNumeric(Fields.FIELD_A2, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[2], DTAUSTape.ARECORD_LENGTH[2],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null && (str.length() != 1 || str.toCharArray()[0] != 'A')) {
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                Fields.FIELD_A1, IllegalDataMessage.TYPE_CONSTANT,
                headerBlock * this.persistence.getBlockSize() +
                DTAUSTape.ARECORD_OFFSETS[0], str));

        }

        // Feld 3
        str = this.readAlphaNumeric(Fields.FIELD_A3, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[3], DTAUSTape.ARECORD_LENGTH[3],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null) {
            label = LogicalFileType.valueOf(str);
            if(label == null) {
                ThreadLocalMessages.getMessages().addMessage(
                    new IllegalDataMessage(Fields.FIELD_A3,
                    IllegalDataMessage.TYPE_FILETYPE,
                    headerBlock * this.persistence.getBlockSize() +
                    DTAUSTape.ARECORD_OFFSETS[3], str));

            } else {
                isBank = label.isSendByBank();
                ret.setType(label);
            }
        }

        // Feld 4
        num = this.readNumberPackedPositive(Fields.FIELD_A4, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[4], DTAUSTape.ARECORD_LENGTH[4], true);

        if(!Bankleitzahl.checkBankleitzahl(new Long(num))) {
            ThreadLocalMessages.getMessages().addMessage(
                new IllegalDataMessage(Fields.FIELD_A4,
                IllegalDataMessage.TYPE_BANKLEITZAHL,
                headerBlock * this.persistence.getBlockSize() +
                DTAUSTape.ARECORD_OFFSETS[4], str));

        } else {
            ret.setBank(Bankleitzahl.valueOf(new Long(num)));
        }

        // Feld 5
        // Nur belegt wenn Absender Kreditinistitut ist, sonst 0.
        num = this.readNumberPackedPositive(Fields.FIELD_A5, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[5], DTAUSTape.ARECORD_LENGTH[5],
            true);

        if(isBank) {
            if(!Bankleitzahl.checkBankleitzahl(new Long(num))) {
                ThreadLocalMessages.getMessages().addMessage(
                    new IllegalDataMessage(Fields.FIELD_A5,
                    IllegalDataMessage.TYPE_BANKLEITZAHL,
                    this.getHeaderBlock() * this.persistence.getBlockSize() +
                    DTAUSTape.ARECORD_OFFSETS[5], Long.toString(num)));

            } else {
                ret.setBankData(Bankleitzahl.valueOf(new Long(num)));
            }
        }

        // Feld 6
        str = this.readAlphaNumeric(Fields.FIELD_A6, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[6], DTAUSTape.ARECORD_LENGTH[6],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null) {
            ret.setCustomer(AlphaNumericText27.parse(str));
        }

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

        if(!Header.Schedule.checkDate(createDate)) {
            ThreadLocalMessages.getMessages().addMessage(
                new IllegalDataMessage(Fields.FIELD_A7,
                IllegalDataMessage.TYPE_SHORTDATE,
                this.getHeaderBlock() * this.persistence.getBlockSize() +
                DTAUSTape.ARECORD_OFFSETS[7], Long.toString(num)));

        }

        // Feld 8
        // Nur belegt wenn Absender Kreditinistitut ist, sonst leer.
        str = this.readAlphaNumeric(Fields.FIELD_A8, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[8], DTAUSTape.ARECORD_LENGTH[8],
            AbstractLogicalFile.ENCODING_EBCDI);

        // Feld 9
        num = this.readNumberPackedPositive(Fields.FIELD_A9, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[9], DTAUSTape.ARECORD_LENGTH[9], true);

        if(!Kontonummer.checkKontonummer(new Long(num))) {
            ThreadLocalMessages.getMessages().addMessage(
                new IllegalDataMessage(Fields.FIELD_A9,
                IllegalDataMessage.TYPE_KONTONUMMER,
                this.getHeaderBlock() * this.persistence.getBlockSize() +
                DTAUSTape.ARECORD_OFFSETS[9], Long.toString(num)));

        } else {
            ret.setAccount(Kontonummer.valueOf(new Long(num)));
        }

        // Feld 10
        Num = this.readNumber(Fields.FIELD_A10, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[10], DTAUSTape.ARECORD_LENGTH[10],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(!Referenznummer.checkReferenznummer(new Long(num))) {
            ThreadLocalMessages.getMessages().addMessage(
                new IllegalDataMessage(Fields.FIELD_A10,
                IllegalDataMessage.TYPE_REFERENZNUMMER,
                this.getHeaderBlock() * this.persistence.getBlockSize() +
                DTAUSTape.ARECORD_OFFSETS[10], Long.toString(num)));

        } else {
            ret.setReference(Referenznummer.valueOf(new Long(num)));
        }

        // Feld 11b
        executionDate = this.readLongDate(Fields.FIELD_A11B, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[12], AbstractLogicalFile.ENCODING_EBCDI);

        // Feld 12
        str = this.readAlphaNumeric(Fields.FIELD_A12, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[14], DTAUSTape.ARECORD_LENGTH[14],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null && str.length() != 1) {
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                Fields.FIELD_A12, IllegalDataMessage.TYPE_ALPHA_NUMERIC,
                headerBlock * this.persistence.getBlockSize() +
                DTAUSTape.ARECORD_OFFSETS[14], str));

        } else {
            final char c = str.toCharArray()[0];
            if(c == ' ') {
                cur = Currency.getInstance("EUR");
                this.getApplicationLogger().log(new MessageEvent(this,
                    new CurrencyViolationMessage(Fields.FIELD_A12,
                    this.getHeaderBlock() * this.persistence.getBlockSize() +
                    DTAUSTape.ARECORD_OFFSETS[14], c, cur),
                    MessageEvent.WARNING));

            } else {
                cur = this.getCurrencyDirectory().getCurrency(c);
                if(cur == null) {
                    ThreadLocalMessages.getMessages().addMessage(
                        new IllegalDataMessage(Fields.FIELD_A12,
                        IllegalDataMessage.TYPE_CURRENCY,
                        headerBlock * this.persistence.getBlockSize() +
                        DTAUSTape.ARECORD_OFFSETS[14], Character.toString(c)));

                }
            }

            ret.setCurrency(cur);
        }

        if(!Header.Schedule.checkSchedule(createDate, executionDate)) {
            ThreadLocalMessages.getMessages().addMessage(
                new IllegalScheduleMessage(this.getHeaderBlock() *
                this.persistence.getBlockSize() +
                DTAUSTape.ARECORD_OFFSETS[12],
                this.getHeader()));

        } else {
            schedule = new Header.Schedule(createDate, executionDate);
            ret.setSchedule(schedule);
        }

        return ret;
    }

    public void writeHeader(final long headerBlock, final Header header) {
        final Header.Schedule schedule;
        final LogicalFileType label;
        final boolean isBank;
        long num = 0L;
        int cal = 0;
        int yy;

        schedule = header.getSchedule();
        label = header.getType();
        isBank = label.isSendByBank();

        // Feld 1
        this.writeNumberBinary(Fields.FIELD_A1, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[0], DTAUSTape.ARECORD_LENGTH[0],
            this.persistence.getBlockSize());

        // Feld 1b
        // TODO -1
        this.writeNumberBinary(-1, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[1], DTAUSTape.ARECORD_LENGTH[1], 0L);

        // Feld 2
        this.writeAlphaNumeric(Fields.FIELD_A2, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[2], DTAUSTape.ARECORD_LENGTH[2],
            "A", AbstractLogicalFile.ENCODING_EBCDI);

        // Feld 3
        this.writeAlphaNumeric(Fields.FIELD_A3, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[3], DTAUSTape.ARECORD_LENGTH[3],
            label.getCode(), AbstractLogicalFile.ENCODING_EBCDI);

        // Feld 4
        this.writeNumberPackedPositive(Fields.FIELD_A4, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[4], DTAUSTape.ARECORD_LENGTH[4],
            header.getBank().intValue(), true);

        // Feld 5
        this.writeNumberPackedPositive(Fields.FIELD_A5, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[5], DTAUSTape.ARECORD_LENGTH[5],
            isBank && header.getBankData() != null ?
                header.getBankData().intValue() : 0, true);

        // Feld 6
        this.writeAlphaNumeric(Fields.FIELD_A6, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[6], DTAUSTape.ARECORD_LENGTH[6],
            header.getCustomer().format(), AbstractLogicalFile.ENCODING_EBCDI);

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
        this.writeAlphaNumeric(Fields.FIELD_A8, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[8], DTAUSTape.ARECORD_LENGTH[8],
            "", AbstractLogicalFile.ENCODING_EBCDI);

        // Feld 9
        this.writeNumberPackedPositive(Fields.FIELD_A9, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[9], DTAUSTape.ARECORD_LENGTH[9],
            header.getAccount().longValue(), true);

        // Feld 10
        this.writeNumber(Fields.FIELD_A10, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[10], DTAUSTape.ARECORD_LENGTH[10],
            header.getReference() != null ?
                header.getReference().longValue() : 0L,
            AbstractLogicalFile.ENCODING_EBCDI);

        // Feld 11a
        this.writeAlphaNumeric(Fields.FIELD_A11A, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[11], DTAUSTape.ARECORD_LENGTH[11], "",
            AbstractLogicalFile.ENCODING_EBCDI);

        // Feld 11b
        this.writeLongDate(Fields.FIELD_A11B, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[12], schedule.getExecutionDate(),
            AbstractLogicalFile.ENCODING_EBCDI);

        // Feld 11c
        this.writeAlphaNumeric(Fields.FIELD_A11C, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[13], DTAUSTape.ARECORD_LENGTH[13], "",
            AbstractLogicalFile.ENCODING_EBCDI);

        // Feld 12
        this.writeAlphaNumeric(Fields.FIELD_A12, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[14], DTAUSTape.ARECORD_LENGTH[14],
            Character.toString(header.getCurrency() != null
            ? this.getCurrencyDirectory().getCode(header.getCurrency())
            : ' '),
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
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                Fields.FIELD_E1, IllegalDataMessage.TYPE_CONSTANT,
                checksumBlock * this.persistence.getBlockSize() +
                DTAUSTape.ERECORD_OFFSETS[0], Long.toString(num)));

        }

        // Feld 2
        str = this.readAlphaNumeric(Fields.FIELD_E2, checksumBlock,
            DTAUSTape.ERECORD_OFFSETS[2], DTAUSTape.ERECORD_LENGTH[2],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null && (str.length() != 1 || str.toCharArray()[0] != 'E')) {
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                Fields.FIELD_E2, IllegalDataMessage.TYPE_CONSTANT,
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
        this.writeAlphaNumeric(Fields.FIELD_E2, checksumBlock,
            DTAUSTape.ERECORD_OFFSETS[2], DTAUSTape.ERECORD_LENGTH[2], "E",
            AbstractLogicalFile.ENCODING_EBCDI);

        // Feld 3
        this.writeAlphaNumeric(Fields.FIELD_E3, checksumBlock,
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
        this.writeAlphaNumeric(Fields.FIELD_E9, checksumBlock,
            DTAUSTape.ERECORD_OFFSETS[9], DTAUSTape.ERECORD_LENGTH[9], "",
            AbstractLogicalFile.ENCODING_EBCDI);

    }

    public Transaction readTransaction(final long block,
        final Transaction transaction) throws PhysicalFileError {

        long num;
        Long Num;
        String str;
        int search;
        int keyType;
        long blockOffset;
        final long extCount;
        final Currency cur;
        final Textschluessel type;
        final Transaction.Description desc = new Transaction.Description();

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

            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                Fields.FIELD_C1, IllegalDataMessage.TYPE_NUMERIC,
                block * this.persistence.getBlockSize() +
                DTAUSTape.CRECORD_OFFSETS1[0], Long.toString(num)));

        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 2
        str = this.readAlphaNumeric(Fields.FIELD_C2, block,
            DTAUSTape.CRECORD_OFFSETS1[2], DTAUSTape.CRECORD_LENGTH1[2],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null && (str.length() != 1 || str.toCharArray()[0] != 'C')) {
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                Fields.FIELD_C2, IllegalDataMessage.TYPE_CONSTANT,
                block * this.persistence.getBlockSize() +
                DTAUSTape.CRECORD_OFFSETS1[2], str));

        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 3
        num = this.readNumberPackedPositive(Fields.FIELD_C3, block,
            DTAUSTape.CRECORD_OFFSETS1[3], DTAUSTape.CRECORD_LENGTH1[3], true);

        if(num != 0L) {
            if(!Bankleitzahl.checkBankleitzahl(new Long(num))) {
                ThreadLocalMessages.getMessages().addMessage(
                    new IllegalDataMessage(Fields.FIELD_C3,
                    IllegalDataMessage.TYPE_BANKLEITZAHL,
                    block * this.persistence.getBlockSize() +
                    DTAUSTape.CRECORD_OFFSETS1[3], Long.toString(num)));

            } else {
                transaction.setPrimaryBank(Bankleitzahl.valueOf(new Long(num)));
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 4
        num = this.readNumberPackedPositive(Fields.FIELD_C4, block,
            DTAUSTape.CRECORD_OFFSETS1[4], DTAUSTape.CRECORD_LENGTH1[4], true);

        if(!Bankleitzahl.checkBankleitzahl(new Long(num))) {
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                Fields.FIELD_C4, IllegalDataMessage.TYPE_BANKLEITZAHL,
                block * this.persistence.getBlockSize() +
                DTAUSTape.CRECORD_OFFSETS1[4], Long.toString(num)));

        } else {
            transaction.setTargetBank(Bankleitzahl.valueOf(new Long(num)));
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 5
        num = this.readNumberPackedPositive(Fields.FIELD_C5, block,
            DTAUSTape.CRECORD_OFFSETS1[5], DTAUSTape.CRECORD_LENGTH1[5], true);

        if(!Kontonummer.checkKontonummer(new Long(num))) {
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                Fields.FIELD_C5, IllegalDataMessage.TYPE_KONTONUMMER,
                block * this.persistence.getBlockSize() +
                DTAUSTape.CRECORD_OFFSETS1[5], Long.toString(num)));

        } else {
            transaction.setTargetAccount(Kontonummer.valueOf(new Long(num)));
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 6a
        num = this.readNumberPackedPositive(Fields.FIELD_C6A, block,
            DTAUSTape.CRECORD_OFFSETS1[6], DTAUSTape.CRECORD_LENGTH1[6], false);

        if(!Referenznummer.checkReferenznummer(new Long(num))) {
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                Fields.FIELD_C6A, IllegalDataMessage.TYPE_REFERENZNUMMER,
                block * this.persistence.getBlockSize() +
                DTAUSTape.CRECORD_OFFSETS1[6], Long.toString(num)));

        } else {
            transaction.setReference(Referenznummer.valueOf(new Long(num)));
        }

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

        type = this.getTextschluesselVerzeichnis().
            getTextschluessel(keyType, (int) num);

        if(type == null
            || (type.isDebit() && !this.getHeader().getType().isDebitAllowed())
            || (type.isRemittance() && !this.getHeader().getType().
            isRemittanceAllowed())) {

            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                Fields.FIELD_C7A, IllegalDataMessage.TYPE_TEXTSCHLUESSEL,
                block * this.persistence.getBlockSize() +
                DTAUSTape.CRECORD_OFFSETS1[8], Integer.toString(keyType) +
                Long.toString(num)));

        } else {
            transaction.setType(type);
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 10
        num = this.readNumberPackedPositive(Fields.FIELD_C10, block,
            DTAUSTape.CRECORD_OFFSETS1[12], DTAUSTape.CRECORD_LENGTH1[12],
            true);

        if(!Bankleitzahl.checkBankleitzahl(new Long(num))) {
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                Fields.FIELD_C10, IllegalDataMessage.TYPE_BANKLEITZAHL,
                block * this.persistence.getBlockSize() +
                DTAUSTape.CRECORD_OFFSETS1[12], Long.toString(num)));

        } else {
            transaction.setExecutiveBank(Bankleitzahl.valueOf(new Long(num)));
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 11
        num = this.readNumberPackedPositive(Fields.FIELD_C11, block,
            DTAUSTape.CRECORD_OFFSETS1[13], DTAUSTape.CRECORD_LENGTH1[13],
            true);

        if(!Kontonummer.checkKontonummer(new Long(num))) {
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                Fields.FIELD_C11, IllegalDataMessage.TYPE_KONTONUMMER,
                block * this.persistence.getBlockSize() +
                DTAUSTape.CRECORD_OFFSETS1[13], Long.toString(num)));

        } else {
            transaction.setExecutiveAccount(Kontonummer.valueOf(new Long(num)));
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 12
        num = this.readNumberPackedPositive(Fields.FIELD_C12, block,
            DTAUSTape.CRECORD_OFFSETS1[14], DTAUSTape.CRECORD_LENGTH1[14],
            true);

        transaction.setAmount(new BigDecimal(num));

        // Konstanter Teil - Satzaschnitt 1 - Feld 14
        str = this.readAlphaNumeric(Fields.FIELD_C14, block,
            DTAUSTape.CRECORD_OFFSETS1[16], DTAUSTape.CRECORD_LENGTH1[16],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null) {
            transaction.setTargetName(AlphaNumericText27.parse(str));
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 15
        str = this.readAlphaNumeric(Fields.FIELD_C15, block,
            DTAUSTape.CRECORD_OFFSETS1[17], DTAUSTape.CRECORD_LENGTH1[17],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null) {
            transaction.setExecutiveName(AlphaNumericText27.parse(str));
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 16
        str = this.readAlphaNumeric(Fields.FIELD_C16, block,
            DTAUSTape.CRECORD_OFFSETS1[18], DTAUSTape.CRECORD_LENGTH1[18],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null) {
            desc.addDescription(AlphaNumericText27.parse(str));
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 17a
        str = this.readAlphaNumeric(Fields.FIELD_C17A, block,
            DTAUSTape.CRECORD_OFFSETS1[19], DTAUSTape.CRECORD_LENGTH1[19],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null && str.length() != 1) {
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                Fields.FIELD_C17A, IllegalDataMessage.TYPE_CURRENCY,
                block * this.persistence.getBlockSize() +
                DTAUSTape.CRECORD_OFFSETS1[19], str));

        } else {
            final char c = str.toCharArray()[0];
            if(c == ' ') {
                cur = Currency.getInstance("EUR");
                this.getApplicationLogger().log(new MessageEvent(this,
                    new CurrencyViolationMessage(Fields.FIELD_C17A,
                    block * this.persistence.getBlockSize() +
                    DTAUSTape.CRECORD_OFFSETS1[19], c, cur),
                    MessageEvent.WARNING));

            } else {
                cur = this.getCurrencyDirectory().getCurrency(c);
                if(cur == null) {
                    ThreadLocalMessages.getMessages().addMessage(
                        new IllegalDataMessage(Fields.FIELD_A12,
                        IllegalDataMessage.TYPE_CURRENCY,
                        block * this.persistence.getBlockSize() +
                        DTAUSTape.CRECORD_OFFSETS1[19], Character.toString(c)));

                }
            }

            transaction.setCurrency(cur);
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

            Num = this.readNumber(
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[search], blockOffset,
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPELENGTH[search],
                AbstractLogicalFile.ENCODING_EBCDI);

            str = this.readAlphaNumeric(
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[search], blockOffset,
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUEOFFSET[search],
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUELENGTH[search],
                AbstractLogicalFile.ENCODING_EBCDI);

            if(Num.longValue() == 1L) {
                if(transaction.getTargetExt() != null) {
                    ThreadLocalMessages.getMessages().addMessage(
                        new IllegalDataMessage(
                        DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[search],
                        IllegalDataMessage.TYPE_NUMERIC,
                        blockOffset * this.persistence.getBlockSize() +
                        DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                        Num.toString()));

                } else if (str != null) {
                    transaction.setTargetExt(AlphaNumericText27.parse(str));
                }
            } else if(Num.longValue() == 2L) {
                if(str != null) {
                    desc.addDescription(AlphaNumericText27.parse(str));
                }
            } else if(Num.longValue() == 3L) {
                if(transaction.getExecutiveExt() != null) {
                    ThreadLocalMessages.getMessages().addMessage(
                        new IllegalDataMessage(
                        DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[search],
                        IllegalDataMessage.TYPE_NUMERIC,
                        blockOffset * this.persistence.getBlockSize() +
                        DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                        Num.toString()));

                } else {
                    transaction.setExecutiveExt(AlphaNumericText27.parse(str));
                }
            } else {
                ThreadLocalMessages.getMessages().addMessage(
                    new IllegalDataMessage(
                    DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[search],
                    IllegalDataMessage.TYPE_NUMERIC,
                    blockOffset * this.persistence.getBlockSize() +
                    DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                    Num.toString()));

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
        AlphaNumericText27 txt;
        final Textschluessel type = transaction.getType();
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
        this.writeAlphaNumeric(Fields.FIELD_C2, block,
            DTAUSTape.CRECORD_OFFSETS1[2], DTAUSTape.CRECORD_LENGTH1[2], "C",
            AbstractLogicalFile.ENCODING_EBCDI);

        // Konstanter Teil - 1. Satzabschnitt - Feld 3
        this.writeNumberPackedPositive(Fields.FIELD_C3, block,
            DTAUSTape.CRECORD_OFFSETS1[3], DTAUSTape.CRECORD_LENGTH1[3],
            transaction.getPrimaryBank() != null ?
                transaction.getPrimaryBank().intValue() : 0, true);

        // Konstanter Teil - 1. Satzabschnitt - Feld 4
        this.writeNumberPackedPositive(Fields.FIELD_C4, block,
            DTAUSTape.CRECORD_OFFSETS1[4], DTAUSTape.CRECORD_LENGTH1[4],
            transaction.getTargetBank().intValue(), true);

        // Konstanter Teil - 1. Satzabschnitt - Feld 5
        this.writeNumberPackedPositive(Fields.FIELD_C5, block,
            DTAUSTape.CRECORD_OFFSETS1[5], DTAUSTape.CRECORD_LENGTH1[5],
            transaction.getTargetAccount().longValue(), true);

        // Konstanter Teil - 1. Satzabschnitt - Feld 6a
        this.writeNumberPackedPositive(Fields.FIELD_C6A, block,
            DTAUSTape.CRECORD_OFFSETS1[6], DTAUSTape.CRECORD_LENGTH1[6],
            transaction.getReference() != null ?
                transaction.getReference().longValue() : 0L, false);

        // Konstanter Teil - 1. Satzabschnitt - Feld 6b
        this.writeNumberPackedPositive(Fields.FIELD_C6B, block,
            DTAUSTape.CRECORD_OFFSETS1[7], DTAUSTape.CRECORD_LENGTH1[7], 0L,
            true);

        // Konstanter Teil - 1. Satzabschnitt - Feld 7a
        this.writeNumberPackedPositive(Fields.FIELD_C7A, block,
            DTAUSTape.CRECORD_OFFSETS1[8], DTAUSTape.CRECORD_LENGTH1[8],
            type.getKey(), false);

        // Konstanter Teil - 1. Satzabschnitt - Feld 7b
        this.writeNumberPackedPositive(Fields.FIELD_C7B, block,
            DTAUSTape.CRECORD_OFFSETS1[9], DTAUSTape.CRECORD_LENGTH1[9],
            type.getExtension(), true);

        // Konstanter Teil - 1. Satzabschnitt - Feld 8
        this.writeAlphaNumeric(Fields.FIELD_C8, block,
            DTAUSTape.CRECORD_OFFSETS1[10], DTAUSTape.CRECORD_LENGTH1[10], "",
            AbstractLogicalFile.ENCODING_EBCDI);

        // Konstanter Teil - 1. Satzabschnitt - Feld 9
        this.writeNumberPackedPositive(Fields.FIELD_C9, block,
            DTAUSTape.CRECORD_OFFSETS1[11], DTAUSTape.CRECORD_LENGTH1[11], 0L,
            true);

        // Konstanter Teil - 1. Satzabschnitt - Feld 10
        this.writeNumberPackedPositive(Fields.FIELD_C10, block,
            DTAUSTape.CRECORD_OFFSETS1[12], DTAUSTape.CRECORD_LENGTH1[12],
            transaction.getExecutiveBank().intValue(), true);

        // Konstanter Teil - 1. Satzabschnitt - Feld 11
        this.writeNumberPackedPositive(Fields.FIELD_C11, block,
            DTAUSTape.CRECORD_OFFSETS1[13], DTAUSTape.CRECORD_LENGTH1[13],
            transaction.getExecutiveAccount().longValue(), true);

        // Konstanter Teil - 1. Satzabschnitt - Feld 12
        this.writeNumberPackedPositive(Fields.FIELD_C12, block,
            DTAUSTape.CRECORD_OFFSETS1[14], DTAUSTape.CRECORD_LENGTH1[14],
            transaction.getAmount().longValue(), true); // TODO longValueExact()

        // Konstanter Teil - 1. Satzabschnitt - Feld 13
        this.writeAlphaNumeric(Fields.FIELD_C13, block,
            DTAUSTape.CRECORD_OFFSETS1[15], DTAUSTape.CRECORD_LENGTH1[15], "",
            AbstractLogicalFile.ENCODING_EBCDI);

        // Konstanter Teil - 1. Satzabschnitt - Feld 14
        this.writeAlphaNumeric(Fields.FIELD_C14, block,
            DTAUSTape.CRECORD_OFFSETS1[16], DTAUSTape.CRECORD_LENGTH1[16],
            transaction.getTargetName().format(),
            AbstractLogicalFile.ENCODING_EBCDI);

        // Konstanter Teil - 1. Satzabschnitt - Feld 15
        this.writeAlphaNumeric(Fields.FIELD_C15, block,
            DTAUSTape.CRECORD_OFFSETS1[17], DTAUSTape.CRECORD_LENGTH1[17],
            transaction.getExecutiveName().format(),
            AbstractLogicalFile.ENCODING_EBCDI);

        // Konstanter Teil - 1. Satzabschnitt - Feld 16
        this.writeAlphaNumeric(Fields.FIELD_C16, block,
            DTAUSTape.CRECORD_OFFSETS1[18], DTAUSTape.CRECORD_LENGTH1[18],
            desc.getDescriptionCount() > 0 ?
                desc.getDescription(0).format() : "",
            AbstractLogicalFile.ENCODING_EBCDI);

        // Konstanter Teil - 1. Satzabschnitt - Feld 17a
        this.writeAlphaNumeric(Fields.FIELD_C17A, block,
            DTAUSTape.CRECORD_OFFSETS1[19], DTAUSTape.CRECORD_LENGTH1[19],
            Character.toString(transaction.getCurrency() != null
            ? this.getCurrencyDirectory().getCode(transaction.getCurrency())
            : ' '),
            AbstractLogicalFile.ENCODING_EBCDI);

        // Konstanter Teil - 1. Satzabschnitt - Feld 17b
        this.writeAlphaNumeric(Fields.FIELD_C17B, block,
            DTAUSTape.CRECORD_OFFSETS1[20], DTAUSTape.CRECORD_LENGTH1[20], "",
            AbstractLogicalFile.ENCODING_EBCDI);

        // Konstanter Teil - 1. Satzabschnitt - Feld 18
        this.writeNumberPackedPositive(Fields.FIELD_C18, block,
            DTAUSTape.CRECORD_OFFSETS1[21], DTAUSTape.CRECORD_LENGTH1[21],
            extCount, true);

        // Erweiterungsteile des 2., 3., und 4. Satzabschnittes.
        descCount = desc.getDescriptionCount();
        extIndex = 0;
        if((txt = transaction.getTargetExt()) != null) {
            blockOffset = block +
                DTAUSTape.CRECORD_EXTINDEX_TO_BLOCKOFFSET[extIndex];

            this.writeNumber(
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex], blockOffset,
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex],
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPELENGTH[extIndex], 1L,
                AbstractLogicalFile.ENCODING_EBCDI);

            this.writeAlphaNumeric(
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex], blockOffset,
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex],
                txt.format(), AbstractLogicalFile.ENCODING_EBCDI);

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

                this.writeAlphaNumeric(
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[
                    extIndex + followingIndex], blockOffset,
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUEOFFSET[
                    extIndex + followingIndex],
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUELENGTH[
                    extIndex + followingIndex], "",
                    AbstractLogicalFile.ENCODING_EBCDI);

            }

            // Reservefeld des 2. Satzabschnitts leeren.
            this.writeAlphaNumeric(
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

            this.writeAlphaNumeric(
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex], blockOffset,
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex],
                desc.getDescription(i).format(),
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

                this.writeAlphaNumeric(
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[
                    extIndex + followingIndex], blockOffset,
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUEOFFSET[
                    extIndex + followingIndex],
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUELENGTH[
                    extIndex + followingIndex], "",
                    AbstractLogicalFile.ENCODING_EBCDI);

            }

            // Reservefeld des 2., 3.  und 4. Satzabschnitts leeren.
            this.writeAlphaNumeric(DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[
                DTAUSTape.CRECORD_EXTINDEX_TO_FOLLOWINGEXTENSIONS[
                extIndex] + extIndex] + 7, blockOffset,
                DTAUSTape.CRECORD_OFFSETS_EXT[10],
                DTAUSTape.CRECORD_LENGTH_EXT[10], "",
                AbstractLogicalFile.ENCODING_EBCDI);

        }
        if((txt = transaction.getExecutiveExt()) != null) {
            blockOffset = block +
                DTAUSTape.CRECORD_EXTINDEX_TO_BLOCKOFFSET[extIndex];

            this.writeNumber(
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex], blockOffset,
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex],
                DTAUSTape.CRECORD_EXTINDEX_TO_TYPELENGTH[extIndex], 3L,
                AbstractLogicalFile.ENCODING_EBCDI);

            this.writeAlphaNumeric(
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex], blockOffset,
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                DTAUSTape.CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex],
                txt.format(), AbstractLogicalFile.ENCODING_EBCDI);

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

                this.writeAlphaNumeric(
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[
                    extIndex + followingIndex], blockOffset,
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUEOFFSET[
                    extIndex + followingIndex],
                    DTAUSTape.CRECORD_EXTINDEX_TO_VALUELENGTH[
                    extIndex + followingIndex], "",
                    AbstractLogicalFile.ENCODING_EBCDI);

            }

            // Reservefeld des 2., 3. oder 4. Satzabschnitts leeren.
            this.writeAlphaNumeric(DTAUSTape.CRECORD_EXTINDEX_TO_VALUEFIELD[
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

    protected ApplicationLogger getApplicationLoggerImpl() {
        return this.getApplicationLogger();
    }

    protected TaskMonitor getTaskMonitorImpl() {
        return this.getTaskMonitor();
    }

    protected TextschluesselVerzeichnis getTextschluesselVerzeichnisImpl() {
        return this.getTextschluesselVerzeichnis();
    }

    //-----------------------------------------------------AbstractLogicalFile--

}

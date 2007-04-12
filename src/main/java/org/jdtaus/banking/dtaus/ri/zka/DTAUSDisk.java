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

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import org.jdtaus.banking.AlphaNumericText27;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.Kontonummer;
import org.jdtaus.banking.Referenznummer10;
import org.jdtaus.banking.Referenznummer11;
import org.jdtaus.banking.Textschluessel;
import org.jdtaus.banking.TextschluesselVerzeichnis;
import org.jdtaus.banking.dtaus.Checksum;
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.LogicalFileType;
import org.jdtaus.banking.dtaus.Transaction;
import org.jdtaus.banking.dtaus.spi.CurrencyDirectory;
import org.jdtaus.banking.dtaus.spi.Fields;
import org.jdtaus.banking.dtaus.ri.zka.messages.CurrencyViolationMessage;
import org.jdtaus.banking.dtaus.ri.zka.messages.IllegalDataMessage;
import org.jdtaus.banking.dtaus.ri.zka.messages.IllegalScheduleMessage;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.ContextFactory;
import org.jdtaus.core.container.ContextInitializer;
import org.jdtaus.core.container.Dependency;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ImplementationException;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.io.util.StructuredFileOperations;
import org.jdtaus.core.lang.spi.MemoryManager;
import org.jdtaus.core.logging.spi.Logger;
import org.jdtaus.core.monitor.spi.TaskMonitor;
import org.jdtaus.core.text.Message;
import org.jdtaus.core.text.MessageEvent;
import org.jdtaus.core.text.spi.ApplicationLogger;

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
public class DTAUSDisk extends AbstractLogicalFile
{

    //--Konstanten--------------------------------------------------------------

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
    //--Implementation----------------------------------------------------------

    // This section is generated by jdtaus-source-plugin.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(DTAUSDisk.class.getName());

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

    // This section is generated by jdtaus-source-plugin.

    /** Protected <code>DTAUSDisk</code> implementation constructor.
    * @param meta Implementation meta-data.
    */
    protected DTAUSDisk(final Implementation meta)
    {
        super();
    }
    /** Protected <code>DTAUSDisk</code> dependency constructor.
    * @param meta Dependency meta-data.
    */
    protected DTAUSDisk(final Dependency meta)
    {
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
     * @throws IOException wenn nicht gelesen werden kann.
     */
    protected DTAUSDisk(final long headerBlock,
        final StructuredFileOperations persistence) throws IOException
    {

        this(DTAUSDisk.META);

        if(persistence == null)
        {
            throw new NullPointerException("persistence");
        }

        if(persistence.getBlockSize() != 128)
        {
            throw new IllegalArgumentException(
                Long.toString(persistence.getBlockSize()));

        }

        this.setStructuredFile(persistence);
        this.setHeaderBlock(headerBlock);
    }

    //-----------------------------------------------------------Konstruktoren--
    //--Dependencies------------------------------------------------------------

    // This section is generated by jdtaus-source-plugin.

    /** Configured <code>CurrencyDirectory</code> implementation. */
    private transient CurrencyDirectory _dependency5;

    /** <code>CurrencyDirectory</code> implementation getter. */
    private CurrencyDirectory getCurrencyDirectory()
    {
        CurrencyDirectory ret = null;
        if(this._dependency5 != null)
        {
           ret = this._dependency5;
        }
        else
        {
            ret = (CurrencyDirectory) ContainerFactory.getContainer().
                getDependency(DTAUSDisk.class,
                "CurrencyDirectory");

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSDisk.class.getName()).
                getDependencies().getDependency("CurrencyDirectory").
                isBound())
            {
                this._dependency5 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext()))
        {
            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }
    /** Configured <code>TextschluesselVerzeichnis</code> implementation. */
    private transient TextschluesselVerzeichnis _dependency4;

    /** <code>TextschluesselVerzeichnis</code> implementation getter. */
    private TextschluesselVerzeichnis getTextschluesselVerzeichnis()
    {
        TextschluesselVerzeichnis ret = null;
        if(this._dependency4 != null)
        {
           ret = this._dependency4;
        }
        else
        {
            ret = (TextschluesselVerzeichnis) ContainerFactory.getContainer().
                getDependency(DTAUSDisk.class,
                "TextschluesselVerzeichnis");

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSDisk.class.getName()).
                getDependencies().getDependency("TextschluesselVerzeichnis").
                isBound())
            {
                this._dependency4 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext()))
        {
            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }
    /** Configured <code>TaskMonitor</code> implementation. */
    private transient TaskMonitor _dependency3;

    /** <code>TaskMonitor</code> implementation getter. */
    private TaskMonitor getTaskMonitor()
    {
        TaskMonitor ret = null;
        if(this._dependency3 != null)
        {
           ret = this._dependency3;
        }
        else
        {
            ret = (TaskMonitor) ContainerFactory.getContainer().
                getDependency(DTAUSDisk.class,
                "TaskMonitor");

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSDisk.class.getName()).
                getDependencies().getDependency("TaskMonitor").
                isBound())
            {
                this._dependency3 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext()))
        {
            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }
    /** Configured <code>ApplicationLogger</code> implementation. */
    private transient ApplicationLogger _dependency2;

    /** <code>ApplicationLogger</code> implementation getter. */
    private ApplicationLogger getApplicationLogger()
    {
        ApplicationLogger ret = null;
        if(this._dependency2 != null)
        {
           ret = this._dependency2;
        }
        else
        {
            ret = (ApplicationLogger) ContainerFactory.getContainer().
                getDependency(DTAUSDisk.class,
                "ApplicationLogger");

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSDisk.class.getName()).
                getDependencies().getDependency("ApplicationLogger").
                isBound())
            {
                this._dependency2 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext()))
        {
            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }
    /** Configured <code>MemoryManager</code> implementation. */
    private transient MemoryManager _dependency1;

    /** <code>MemoryManager</code> implementation getter. */
    private MemoryManager getMemoryManager()
    {
        MemoryManager ret = null;
        if(this._dependency1 != null)
        {
           ret = this._dependency1;
        }
        else
        {
            ret = (MemoryManager) ContainerFactory.getContainer().
                getDependency(DTAUSDisk.class,
                "MemoryManager");

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSDisk.class.getName()).
                getDependencies().getDependency("MemoryManager").
                isBound())
            {
                this._dependency1 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext()))
        {
            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }
    /** Configured <code>Logger</code> implementation. */
    private transient Logger _dependency0;

    /** <code>Logger</code> implementation getter. */
    private Logger getLogger()
    {
        Logger ret = null;
        if(this._dependency0 != null)
        {
           ret = this._dependency0;
        }
        else
        {
            ret = (Logger) ContainerFactory.getContainer().
                getDependency(DTAUSDisk.class,
                "Logger");

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSDisk.class.getName()).
                getDependencies().getDependency("Logger").
                isBound())
            {
                this._dependency0 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext()))
        {
            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }

    //------------------------------------------------------------Dependencies--
    //--AbstractLogicalFile-----------------------------------------------------

    protected int checksumTransaction(final long block,
        final Transaction transaction, final Checksum checksum)
        throws IOException
    {
        int ret = 2;
        final long extCount = this.readNumber(Fields.FIELD_C18, block + 1,
            DTAUSDisk.CRECORD_OFFSETS2[4], DTAUSDisk.CRECORD_LENGTH2[4],
            AbstractLogicalFile.ENCODING_ASCII).longValue();

        if(extCount != -1L)
        {
            final Transaction t = this.readTransaction(block, transaction);
            if(t.getAmount() != null && t.getTargetAccount() != null &&
                t.getTargetBank() != null)
            {
                checksum.add(t);
            }

            ret = CRECORD_EXTENSIONCOUNT_TO_BLOCKCOUNT[(int) extCount];
        }

        return ret;
    }

    protected char getBlockType(final long block) throws IOException
    {
        // Feld 2
        final String str = this.readAlphaNumeric(Fields.FIELD_A2, block,
            DTAUSDisk.ARECORD_OFFSETS[1], DTAUSDisk.ARECORD_LENGTH[1],
            AbstractLogicalFile.ENCODING_ASCII);

        char ret;
        final Message msg;
        if(str == null || str.length() != 1)
        {
            msg = new IllegalDataMessage(Fields.FIELD_A2,
                IllegalDataMessage.TYPE_CONSTANT,
                block * this.persistence.getBlockSize() +
                DTAUSDisk.ARECORD_OFFSETS[1], str);

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new ImplementationException(this.getMeta(),
                    msg.getText(Locale.getDefault()));

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }

            ret = '?';
        }
        else if("A".equals(str))
        {
            ret = 'A';
        }
        else if("C".equals(str))
        {
            ret = 'C';
        }
        else if("E".equals(str))
        {
            ret = 'E';
        }
        else
        {
            ret = str.toCharArray()[0];
        }

        return ret;
    }

    protected Header readHeader(final long headerBlock) throws IOException
    {
        Long num;
        String str;
        final Date createDate;
        final Date executionDate;
        final Header.Schedule schedule;
        final Header ret;
        final LogicalFileType label;
        final int blockSize;
        final Currency cur;
        boolean isBank = false;
        Message msg;

        ret = new Header();
        blockSize = this.persistence.getBlockSize();

        // Feld 1
        num = this.readNumber(Fields.FIELD_A1, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[0], DTAUSDisk.ARECORD_LENGTH[0],
            AbstractLogicalFile.ENCODING_ASCII);

        if(num.intValue() != blockSize)
        {
            msg = new IllegalDataMessage(Fields.FIELD_A1,
                IllegalDataMessage.TYPE_CONSTANT,
                headerBlock * this.persistence.getBlockSize() +
                DTAUSDisk.ARECORD_OFFSETS[0], num.toString());

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new ImplementationException(this.getMeta(),
                    msg.getText(Locale.getDefault()));

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }
        }

        // Feld 2
        str = this.readAlphaNumeric(Fields.FIELD_A2, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[1], DTAUSDisk.ARECORD_LENGTH[1],
            AbstractLogicalFile.ENCODING_ASCII);

        if(str != null && (str.length() != 1 || str.toCharArray()[0] != 'A'))
        {
            msg = new IllegalDataMessage(Fields.FIELD_A2,
                IllegalDataMessage.TYPE_CONSTANT,
                headerBlock * this.persistence.getBlockSize() +
                DTAUSDisk.ARECORD_OFFSETS[1], str);

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new ImplementationException(this.getMeta(),
                    msg.getText(Locale.getDefault()));

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }
        }

        // Feld 3
        str = this.readAlphaNumeric(Fields.FIELD_A3, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[2], DTAUSDisk.ARECORD_LENGTH[2],
            AbstractLogicalFile.ENCODING_ASCII);

        ret.setType(null);
        if(str != null)
        {
            label = LogicalFileType.valueOf(str);
            if(label == null)
            {
                msg = new IllegalDataMessage(Fields.FIELD_A3,
                    IllegalDataMessage.TYPE_FILETYPE,
                    headerBlock * this.persistence.getBlockSize() +
                    DTAUSDisk.ARECORD_OFFSETS[2], str);

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new ImplementationException(this.getMeta(),
                        msg.getText(Locale.getDefault()));

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }

            }
            else
            {
                isBank = label.isSendByBank();
                ret.setType(label);
            }
        }

        // Feld 4
        num = this.readNumber(Fields.FIELD_A4, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[3], DTAUSDisk.ARECORD_LENGTH[3],
            AbstractLogicalFile.ENCODING_ASCII);

        if(!Bankleitzahl.checkBankleitzahl(num))
        {
            msg = new IllegalDataMessage(Fields.FIELD_A4,
                IllegalDataMessage.TYPE_BANKLEITZAHL,
                this.getHeaderBlock() * this.persistence.getBlockSize() +
                DTAUSDisk.ARECORD_OFFSETS[3], num.toString());

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new ImplementationException(this.getMeta(),
                    msg.getText(Locale.getDefault()));

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }

        }
        else
        {
            ret.setBank(Bankleitzahl.valueOf(num));
        }

        // Feld 5
        // Nur belegt wenn Absender Kreditinistitut ist, sonst 0.
        num = this.readNumber(Fields.FIELD_A5, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[4], DTAUSDisk.ARECORD_LENGTH[4],
            AbstractLogicalFile.ENCODING_ASCII);

        ret.setBankData(null);

        if(isBank)
        {
            if(!Bankleitzahl.checkBankleitzahl(num))
            {
                msg = new IllegalDataMessage(Fields.FIELD_A5,
                    IllegalDataMessage.TYPE_BANKLEITZAHL,
                    this.getHeaderBlock() * this.persistence.getBlockSize() +
                    DTAUSDisk.ARECORD_OFFSETS[4], num.toString());

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new ImplementationException(this.getMeta(),
                        msg.getText(Locale.getDefault()));

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }

            }
            else
            {
                ret.setBankData(Bankleitzahl.valueOf(num));
            }
        }

        // Feld 6
        str = this.readAlphaNumeric(Fields.FIELD_A6, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[5], DTAUSDisk.ARECORD_LENGTH[5],
            AbstractLogicalFile.ENCODING_ASCII);

        if(str != null)
        {
            try
            {
                ret.setCustomer(AlphaNumericText27.parse(str));
            }
            catch(ParseException e)
            {
                throw new ImplementationException(this.getMeta(), e);
            }
        }

        // Feld 7
        createDate = this.readShortDate(Fields.FIELD_A7, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[6], AbstractLogicalFile.ENCODING_ASCII);

        // Feld 8
        // Nur belegt wenn Absender Kreditinistitut ist, sonst "".
        str = this.readAlphaNumeric(Fields.FIELD_A8, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[7], DTAUSDisk.ARECORD_LENGTH[7],
            AbstractLogicalFile.ENCODING_ASCII);

        // Feld 9
        num = this.readNumber(Fields.FIELD_A9, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[8], DTAUSDisk.ARECORD_LENGTH[8],
            AbstractLogicalFile.ENCODING_ASCII);

        if(!Kontonummer.checkKontonummer(num))
        {
            msg = new IllegalDataMessage(Fields.FIELD_A9,
                IllegalDataMessage.TYPE_KONTONUMMER,
                this.getHeaderBlock() * this.persistence.getBlockSize() +
                DTAUSDisk.ARECORD_OFFSETS[8], num.toString());

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new ImplementationException(this.getMeta(),
                    msg.getText(Locale.getDefault()));

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }

        }
        else
        {
            ret.setAccount(Kontonummer.valueOf(num));
        }

        // Feld 10
        num = this.readNumber(Fields.FIELD_A10, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[9], DTAUSDisk.ARECORD_LENGTH[9],
            AbstractLogicalFile.ENCODING_ASCII);

        if(!Referenznummer10.checkReferenznummer10(num))
        {
            msg = new IllegalDataMessage(Fields.FIELD_A10,
                IllegalDataMessage.TYPE_REFERENZNUMMER,
                this.getHeaderBlock() * this.persistence.getBlockSize() +
                DTAUSDisk.ARECORD_OFFSETS[9], num.toString());

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new ImplementationException(this.getMeta(),
                    msg.getText(Locale.getDefault()));

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }

        }
        else
        {
            ret.setReference(Referenznummer10.valueOf(num));
        }

        // Feld 11b
        executionDate = this.readLongDate(Fields.FIELD_A11B, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[11], AbstractLogicalFile.ENCODING_ASCII);

        if(createDate != null)
        {
            if(!Header.Schedule.checkSchedule(createDate, executionDate))
            {
                msg = new IllegalScheduleMessage(this.getHeaderBlock() *
                    this.persistence.getBlockSize() +
                    DTAUSDisk.ARECORD_OFFSETS[11],
                    this.getHeader());

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new ImplementationException(this.getMeta(),
                        msg.getText(Locale.getDefault()));

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }

            }
            else
            {
                schedule = new Header.Schedule(createDate, executionDate);
                ret.setSchedule(schedule);
            }
        }

        // Feld 12
        str = this.readAlphaNumeric(Fields.FIELD_A12, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[13], DTAUSDisk.ARECORD_LENGTH[13],
            AbstractLogicalFile.ENCODING_ASCII);

        ret.setCurrency(null);
        if(str != null)
        {
            if(str.length() != 1)
            {
                msg = new IllegalDataMessage(Fields.FIELD_A12,
                    IllegalDataMessage.TYPE_CURRENCY,
                    headerBlock * this.persistence.getBlockSize() +
                    DTAUSDisk.ARECORD_OFFSETS[13], str);

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new ImplementationException(this.getMeta(),
                        msg.getText(Locale.getDefault()));

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }
            }
            else
            {
                final char c = str.toCharArray()[0];
                if(c == ' ')
                {
                    cur = Currency.getInstance("EUR");
                    this.getApplicationLogger().log(new MessageEvent(this,
                        new CurrencyViolationMessage(Fields.FIELD_A12,
                        headerBlock * this.persistence.getBlockSize() +
                        DTAUSDisk.ARECORD_OFFSETS[13], c, cur),
                        MessageEvent.WARNING));

                }
                else
                {
                    cur = this.getCurrencyDirectory().getCurrency(c);
                    if(cur == null)
                    {
                        msg = new IllegalDataMessage(Fields.FIELD_A12,
                            IllegalDataMessage.TYPE_CURRENCY,
                            headerBlock * this.persistence.getBlockSize() +
                            DTAUSDisk.ARECORD_OFFSETS[13], str);

                        if(AbstractErrorMessage.isErrorsEnabled())
                        {
                            throw new ImplementationException(this.getMeta(),
                                msg.getText(Locale.getDefault()));

                        }
                        else
                        {
                            ThreadLocalMessages.getMessages().addMessage(msg);
                        }
                    }
                }

                ret.setCurrency(cur);
            }
        }

        return ret;
    }

    protected void writeHeader(final long headerBlock,
        final Header header) throws IOException
    {
        final Header.Schedule schedule;
        final LogicalFileType label;
        final boolean isBank;

        schedule = header.getSchedule();
        label = header.getType();
        isBank = label.isSendByBank();

        // Feld 1
        this.writeNumber(Fields.FIELD_A1, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[0], DTAUSDisk.ARECORD_LENGTH[0],
            this.persistence.getBlockSize(),
            AbstractLogicalFile.ENCODING_ASCII);

        // Feld 2
        this.writeAlphaNumeric(Fields.FIELD_A2, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[1], DTAUSDisk.ARECORD_LENGTH[1], "A",
            AbstractLogicalFile.ENCODING_ASCII);

        // Feld 3
        this.writeAlphaNumeric(Fields.FIELD_A3, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[2], DTAUSDisk.ARECORD_LENGTH[2],
            label.getCode(), AbstractLogicalFile.ENCODING_ASCII);

        // Feld 4
        this.writeNumber(Fields.FIELD_A4, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[3], DTAUSDisk.ARECORD_LENGTH[3],
            header.getBank().intValue(), AbstractLogicalFile.ENCODING_ASCII);

        // Feld 5
        this.writeNumber(Fields.FIELD_A5, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[4], DTAUSDisk.ARECORD_LENGTH[4],
            isBank && header.getBankData() != null ?
                header.getBankData().intValue() : 0,
            AbstractLogicalFile.ENCODING_ASCII);

        // Feld 6
        this.writeAlphaNumeric(Fields.FIELD_A6, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[5], DTAUSDisk.ARECORD_LENGTH[5],
            header.getCustomer().format(), AbstractLogicalFile.ENCODING_ASCII);

        // Feld 7
        this.writeShortDate(Fields.FIELD_A7, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[6], schedule.getCreateDate(),
            AbstractLogicalFile.ENCODING_ASCII);

        // Feld 8
        this.writeAlphaNumeric(Fields.FIELD_A8, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[7], DTAUSDisk.ARECORD_LENGTH[7],
            "", AbstractLogicalFile.ENCODING_ASCII);

        // Feld 9
        this.writeNumber(Fields.FIELD_A9, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[8], DTAUSDisk.ARECORD_LENGTH[8],
            header.getAccount().longValue(),
            AbstractLogicalFile.ENCODING_ASCII);

        // Feld 10
        this.writeNumber(Fields.FIELD_A10, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[9], DTAUSDisk.ARECORD_LENGTH[9],
            header.getReference() != null ?
                header.getReference().longValue() : 0L,
            AbstractLogicalFile.ENCODING_ASCII);

        // Feld 11a
        this.writeAlphaNumeric(Fields.FIELD_A11A, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[10], DTAUSDisk.ARECORD_LENGTH[10], "",
            AbstractLogicalFile.ENCODING_ASCII);

        // Feld 11b
        this.writeLongDate(Fields.FIELD_A11B, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[11], schedule.getExecutionDate(),
            AbstractLogicalFile.ENCODING_ASCII);

        // Feld 11c
        this.writeAlphaNumeric(Fields.FIELD_A11C, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[12], DTAUSDisk.ARECORD_LENGTH[12], "",
            AbstractLogicalFile.ENCODING_ASCII);

        // Feld 12
        this.writeAlphaNumeric(Fields.FIELD_A12, headerBlock,
            DTAUSDisk.ARECORD_OFFSETS[13], DTAUSDisk.ARECORD_LENGTH[13],
            Character.toString(header.getCurrency() != null
            ? this.getCurrencyDirectory().getCode(header.getCurrency())
            : ' '),
            AbstractLogicalFile.ENCODING_ASCII);

    }

    protected Checksum readChecksum(final long checksumBlock) throws IOException
    {
        Long num;
        final String str;
        final Checksum checksum;
        Message msg;
        checksum = new Checksum();

        // Feld 1
        num = this.readNumber(Fields.FIELD_E1, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[0], DTAUSDisk.ERECORD_LENGTH[0],
            AbstractLogicalFile.ENCODING_ASCII);

        if(num.intValue() != this.persistence.getBlockSize())
        {
            msg = new IllegalDataMessage(Fields.FIELD_E1,
                IllegalDataMessage.TYPE_CONSTANT,
                checksumBlock * this.persistence.getBlockSize() +
                DTAUSDisk.ERECORD_OFFSETS[0], num.toString());

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new ImplementationException(this.getMeta(),
                    msg.getText(Locale.getDefault()));

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }

        }

        // Feld 2
        str = this.readAlphaNumeric(Fields.FIELD_E2, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[1], DTAUSDisk.ERECORD_LENGTH[1],
            AbstractLogicalFile.ENCODING_ASCII);

        if(str != null && (str.length() != 1 || str.toCharArray()[0] != 'E'))
        {
            msg = new IllegalDataMessage(Fields.FIELD_E2,
                IllegalDataMessage.TYPE_CONSTANT,
                checksumBlock * this.persistence.getBlockSize()+
                DTAUSDisk.ERECORD_OFFSETS[1], str);

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new ImplementationException(this.getMeta(),
                    msg.getText(Locale.getDefault()));

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }

        }

        // Feld 4
        num = this.readNumber(Fields.FIELD_E4, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[3], DTAUSDisk.ERECORD_LENGTH[3],
            AbstractLogicalFile.ENCODING_ASCII);

        checksum.setTransactionCount(num.intValue());

        // Feld 6
        num = this.readNumber(Fields.FIELD_E6, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[5], DTAUSDisk.ERECORD_LENGTH[5],
            AbstractLogicalFile.ENCODING_ASCII);

        checksum.setSumTargetAccount(num.longValue());

        // Feld 7
        num = this.readNumber(Fields.FIELD_E7, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[6], DTAUSDisk.ERECORD_LENGTH[6],
            AbstractLogicalFile.ENCODING_ASCII);

        checksum.setSumTargetBank(num.longValue());

        // Feld 8
        num = this.readNumber(Fields.FIELD_E8, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[7], DTAUSDisk.ERECORD_LENGTH[7],
            AbstractLogicalFile.ENCODING_ASCII);

        checksum.setSumAmount(num.longValue());
        return checksum;
    }

    protected void writeChecksum(final long checksumBlock,
        final Checksum checksum) throws IOException
    {
        // Feld 1
        this.writeNumber(Fields.FIELD_E1, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[0], DTAUSDisk.ERECORD_LENGTH[0],
            this.persistence.getBlockSize(),
            AbstractLogicalFile.ENCODING_ASCII);

        // Feld 2
        this.writeAlphaNumeric(Fields.FIELD_E2, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[1], DTAUSDisk.ERECORD_LENGTH[1], "E",
            AbstractLogicalFile.ENCODING_ASCII);

        // Feld 3
        this.writeAlphaNumeric(Fields.FIELD_E3, checksumBlock,
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
        this.writeAlphaNumeric(Fields.FIELD_E9, checksumBlock,
            DTAUSDisk.ERECORD_OFFSETS[8], DTAUSDisk.ERECORD_LENGTH[8], "",
            AbstractLogicalFile.ENCODING_ASCII);

    }

    protected Transaction readTransaction(final long block,
        final Transaction transaction) throws IOException
    {
        int search;
        long blockOffset;
        Long num;
        Long keyType;
        String str;
        Message msg;
        final long extCount;
        final Currency cur;
        final Textschluessel type;
        final Transaction.Description desc = new Transaction.Description();
        final Kontonummer targetAccount;
        final Bankleitzahl targetBank;
        final AlphaNumericText27 targetName;

        transaction.setExecutiveExt(null);
        transaction.setTargetExt(null);
        extCount = this.readNumber(Fields.FIELD_C18, block + 1,
            DTAUSDisk.CRECORD_OFFSETS2[4], DTAUSDisk.CRECORD_LENGTH2[4],
            AbstractLogicalFile.ENCODING_ASCII).longValue();

        // Konstanter Teil - Satzaschnitt 1 - Feld 1
        num = this.readNumber(Fields.FIELD_C1, block,
            DTAUSDisk.CRECORD_OFFSETS1[0], DTAUSDisk.CRECORD_LENGTH1[0],
            AbstractLogicalFile.ENCODING_ASCII);

        if(num.intValue() != DTAUSDisk.CRECORD_CONST_LENGTH +
            extCount * DTAUSDisk.CRECORD_EXT_LENGTH)
        {
            msg = new IllegalDataMessage(Fields.FIELD_C1,
                IllegalDataMessage.TYPE_NUMERIC,
                block * this.persistence.getBlockSize() +
                DTAUSDisk.CRECORD_OFFSETS1[0], num.toString());

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new ImplementationException(this.getMeta(),
                    msg.getText(Locale.getDefault()));

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }

        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 2
        str = this.readAlphaNumeric(Fields.FIELD_C2, block,
            DTAUSDisk.CRECORD_OFFSETS1[1], DTAUSDisk.CRECORD_LENGTH1[1],
            AbstractLogicalFile.ENCODING_ASCII);

        if(str != null && (str.length() != 1 || str.toCharArray()[0] != 'C'))
        {
            msg = new IllegalDataMessage(Fields.FIELD_C2,
                IllegalDataMessage.TYPE_CONSTANT,
                block * this.persistence.getBlockSize() +
                DTAUSDisk.CRECORD_OFFSETS1[1], str);

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new ImplementationException(this.getMeta(),
                    msg.getText(Locale.getDefault()));

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }

        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 3
        num = this.readNumber(Fields.FIELD_C3, block,
            DTAUSDisk.CRECORD_OFFSETS1[2], DTAUSDisk.CRECORD_LENGTH1[2],
            AbstractLogicalFile.ENCODING_ASCII);

        transaction.setPrimaryBank(null);

        if(num.longValue() != 0L)
        {
            if(!Bankleitzahl.checkBankleitzahl(num))
            {
                msg = new IllegalDataMessage(Fields.FIELD_C3,
                    IllegalDataMessage.TYPE_BANKLEITZAHL,
                    block * this.persistence.getBlockSize() +
                    DTAUSDisk.CRECORD_OFFSETS1[2], num.toString());

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new ImplementationException(this.getMeta(),
                        msg.getText(Locale.getDefault()));

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }

            }
            else
            {
                transaction.setPrimaryBank(Bankleitzahl.valueOf(num));
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 4
        num = this.readNumber(Fields.FIELD_C4, block,
            DTAUSDisk.CRECORD_OFFSETS1[3], DTAUSDisk.CRECORD_LENGTH1[3],
            AbstractLogicalFile.ENCODING_ASCII);

        if(!Bankleitzahl.checkBankleitzahl(num))
        {
            msg = new IllegalDataMessage(Fields.FIELD_C4,
                IllegalDataMessage.TYPE_BANKLEITZAHL,
                block * this.persistence.getBlockSize() +
                DTAUSDisk.CRECORD_OFFSETS1[3], num.toString());

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new ImplementationException(this.getMeta(),
                    msg.getText(Locale.getDefault()));

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }
        }
        else
        {
            transaction.setTargetBank(Bankleitzahl.valueOf(num));
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 5
        num = this.readNumber(Fields.FIELD_C5, block,
            DTAUSDisk.CRECORD_OFFSETS1[4], DTAUSDisk.CRECORD_LENGTH1[4],
            AbstractLogicalFile.ENCODING_ASCII);

        if(!Kontonummer.checkKontonummer(num))
        {
            msg = new IllegalDataMessage(Fields.FIELD_C5,
                IllegalDataMessage.TYPE_KONTONUMMER,
                block * this.persistence.getBlockSize() +
                DTAUSDisk.CRECORD_OFFSETS1[4], num.toString());

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new ImplementationException(this.getMeta(),
                    msg.getText(Locale.getDefault()));

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }
        }
        else
        {
            transaction.setTargetAccount(Kontonummer.valueOf(num));
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 6
        num = this.readNumber(Fields.FIELD_C6, block,
            DTAUSDisk.CRECORD_OFFSETS1[5], DTAUSDisk.CRECORD_LENGTH1[5],
            AbstractLogicalFile.ENCODING_ASCII);

        if(!Referenznummer11.checkReferenznummer11(num))
        {
            msg = new IllegalDataMessage(Fields.FIELD_C6,
                IllegalDataMessage.TYPE_REFERENZNUMMER,
                block * this.persistence.getBlockSize() +
                DTAUSDisk.CRECORD_OFFSETS1[5], num.toString());

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new ImplementationException(this.getMeta(),
                    msg.getText(Locale.getDefault()));

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }
        }
        else
        {
            transaction.setReference(Referenznummer11.valueOf(num));
        }

        // Konstanter Teil - Satzaschnitt 1 - Felder 7a & 7b
        keyType = this.readNumber(Fields.FIELD_C7A, block,
            DTAUSDisk.CRECORD_OFFSETS1[6], 2,
            AbstractLogicalFile.ENCODING_ASCII);

        num = this.readNumber(Fields.FIELD_C7B, block,
            DTAUSDisk.CRECORD_OFFSETS1[6] + 2, DTAUSDisk.CRECORD_LENGTH1[6] - 2,
            AbstractLogicalFile.ENCODING_ASCII);

        type = this.getTextschluesselVerzeichnis().
            getTextschluessel(keyType.intValue(), num.intValue());

        transaction.setType(null);

        if(type == null
            || (type.isDebit() && !this.getHeader().getType().isDebitAllowed())
            || (type.isRemittance() && !this.getHeader().getType().
            isRemittanceAllowed()))
        {
            msg = new IllegalDataMessage(Fields.FIELD_C7A,
                IllegalDataMessage.TYPE_TEXTSCHLUESSEL,
                block * this.persistence.getBlockSize() +
                DTAUSDisk.CRECORD_OFFSETS1[6], keyType.toString() +
                num.toString());

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new ImplementationException(this.getMeta(),
                    msg.getText(Locale.getDefault()));

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }
        }
        else
        {
            transaction.setType(type);
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 10
        num = this.readNumber(Fields.FIELD_C10, block,
            DTAUSDisk.CRECORD_OFFSETS1[9], DTAUSDisk.CRECORD_LENGTH1[9],
            AbstractLogicalFile.ENCODING_ASCII);

        if(!Bankleitzahl.checkBankleitzahl(num))
        {
            msg = new IllegalDataMessage(Fields.FIELD_C10,
                IllegalDataMessage.TYPE_BANKLEITZAHL,
                block * this.persistence.getBlockSize() +
                DTAUSDisk.CRECORD_OFFSETS1[9], num.toString());

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new ImplementationException(this.getMeta(),
                    msg.getText(Locale.getDefault()));

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }
        }
        else
        {
            transaction.setExecutiveBank(Bankleitzahl.valueOf(num));
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 11
        num = this.readNumber(Fields.FIELD_C11, block,
            DTAUSDisk.CRECORD_OFFSETS1[10], DTAUSDisk.CRECORD_LENGTH1[10],
            AbstractLogicalFile.ENCODING_ASCII);

        if(!Kontonummer.checkKontonummer(num))
        {
            msg = new IllegalDataMessage(Fields.FIELD_C11,
                IllegalDataMessage.TYPE_KONTONUMMER,
                block * this.persistence.getBlockSize() +
                DTAUSDisk.CRECORD_OFFSETS1[10], num.toString());

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new ImplementationException(this.getMeta(),
                    msg.getText(Locale.getDefault()));

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }
        }
        else
        {
            transaction.setExecutiveAccount(Kontonummer.valueOf(num));
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 12
        num = this.readNumber(Fields.FIELD_C12, block,
            DTAUSDisk.CRECORD_OFFSETS1[11], DTAUSDisk.CRECORD_LENGTH1[11],
            AbstractLogicalFile.ENCODING_ASCII);

        transaction.setAmount(new BigDecimal(num.longValue()));

        // Konstanter Teil - Satzaschnitt 1 - Feld 14a
        str = this.readAlphaNumeric(Fields.FIELD_C14A, block,
            DTAUSDisk.CRECORD_OFFSETS1[13], DTAUSDisk.CRECORD_LENGTH1[13],
            AbstractLogicalFile.ENCODING_ASCII);

        if(str != null)
        {
            try
            {
                transaction.setTargetName(AlphaNumericText27.parse(str));
            }
            catch(ParseException e)
            {
                throw new ImplementationException(this.getMeta(), e);
            }
        }

        // Konstanter Teil - Satzaschnitt 2 - Feld 15(1)
        str = this.readAlphaNumeric(Fields.FIELD_C15, block + 1,
            DTAUSDisk.CRECORD_OFFSETS2[0], DTAUSDisk.CRECORD_LENGTH2[0],
            AbstractLogicalFile.ENCODING_ASCII);

        if(str != null)
        {
            try
            {
                transaction.setExecutiveName(AlphaNumericText27.parse(str));
            }
            catch(ParseException e)
            {
                throw new ImplementationException(this.getMeta(), e);
            }
        }

        // Konstanter Teil - Satzaschnitt 2 - Feld 16(2)
        str = this.readAlphaNumeric(Fields.FIELD_C16, block + 1,
            DTAUSDisk.CRECORD_OFFSETS2[1], DTAUSDisk.CRECORD_LENGTH2[1],
            AbstractLogicalFile.ENCODING_ASCII);

        if(str != null)
        {
            try
            {
                desc.addDescription(AlphaNumericText27.parse(str));
            }
            catch(ParseException e)
            {
                throw new ImplementationException(this.getMeta(), e);
            }
        }

        // Konstanter Teil - Satzaschnitt 2 - Feld 17a(3)
        str = this.readAlphaNumeric(Fields.FIELD_C17A, block + 1,
            DTAUSDisk.CRECORD_OFFSETS2[2], DTAUSDisk.CRECORD_LENGTH2[2],
            AbstractLogicalFile.ENCODING_ASCII);

        transaction.setCurrency(null);

        if(str != null)
        {
            if(str.length() != 1)
            {
                msg = new IllegalDataMessage(Fields.FIELD_C17A,
                    IllegalDataMessage.TYPE_CURRENCY,
                    block * this.persistence.getBlockSize() +
                    DTAUSDisk.CRECORD_OFFSETS1[10], str);

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new ImplementationException(this.getMeta(),
                        msg.getText(Locale.getDefault()));

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }
            }
            else
            {
                final char c = str.toCharArray()[0];
                if(c == ' ')
                {
                    cur = Currency.getInstance("EUR");
                    this.getApplicationLogger().log(new MessageEvent(this,
                        new CurrencyViolationMessage(Fields.FIELD_C17A,
                        this.getHeaderBlock() * this.persistence.getBlockSize() +
                        DTAUSDisk.CRECORD_OFFSETS1[10], c, cur),
                        MessageEvent.WARNING));

                }
                else
                {
                    cur = this.getCurrencyDirectory().getCurrency(c);
                    if(cur == null)
                    {
                        msg = new IllegalDataMessage(Fields.FIELD_C17A,
                            IllegalDataMessage.TYPE_CURRENCY,
                            block * this.persistence.getBlockSize() +
                            DTAUSDisk.CRECORD_OFFSETS1[10], str);

                        if(AbstractErrorMessage.isErrorsEnabled())
                        {
                            throw new ImplementationException(this.getMeta(),
                                msg.getText(Locale.getDefault()));

                        }
                        else
                        {
                            ThreadLocalMessages.getMessages().addMessage(msg);
                        }
                    }
                }

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
        for(search = 0; search < extCount; search++)
        {
            blockOffset = block +
                DTAUSDisk.CRECORD_EXTINDEX_TO_BLOCKOFFSET[search];

            num = this.readNumber(
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEFIELD[search], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPELENGTH[search],
                AbstractLogicalFile.ENCODING_ASCII);

            str = this.readAlphaNumeric(
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEFIELD[search], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEOFFSET[search],
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUELENGTH[search],
                AbstractLogicalFile.ENCODING_ASCII);

            if(num.longValue() == 1L)
            {
                if(transaction.getTargetExt() != null)
                {
                    msg = new IllegalDataMessage(
                        DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEFIELD[search],
                        IllegalDataMessage.TYPE_CONSTANT,
                        block * this.persistence.getBlockSize() +
                        DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                        num.toString());

                    if(AbstractErrorMessage.isErrorsEnabled())
                    {
                        throw new ImplementationException(this.getMeta(),
                            msg.getText(Locale.getDefault()));

                    }
                    else
                    {
                        ThreadLocalMessages.getMessages().addMessage(msg);
                    }

                }
                else if(str != null)
                {
                    try
                    {
                        transaction.setTargetExt(
                            AlphaNumericText27.parse(str));

                    }
                    catch(ParseException e)
                    {
                        throw new ImplementationException(this.getMeta(), e);
                    }
                }
            }
            else if(num.longValue() == 2L)
            {
                if(str != null)
                {
                    try
                    {
                        desc.addDescription(AlphaNumericText27.parse(str));
                    }
                    catch(ParseException e)
                    {
                        throw new ImplementationException(this.getMeta(), e);
                    }
                }
            }
            else if(num.longValue() == 3L)
            {
                if(transaction.getExecutiveExt() != null)
                {
                    msg = new IllegalDataMessage(
                        DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEFIELD[search],
                        IllegalDataMessage.TYPE_CONSTANT,
                        block * this.persistence.getBlockSize() +
                        DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                        num.toString());

                    if(AbstractErrorMessage.isErrorsEnabled())
                    {
                        throw new ImplementationException(this.getMeta(),
                            msg.getText(Locale.getDefault()));

                    }
                    else
                    {
                        ThreadLocalMessages.getMessages().addMessage(msg);
                    }

                }
                else if(str != null)
                {
                    try
                    {
                        transaction.setExecutiveExt(
                            AlphaNumericText27.parse(str));

                    }
                    catch(ParseException e)
                    {
                        throw new ImplementationException(this.getMeta(), e);
                    }
                }
            }
            else
            {
                msg = new IllegalDataMessage(
                    DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEFIELD[search],
                    IllegalDataMessage.TYPE_CONSTANT,
                    block * this.persistence.getBlockSize() +
                    DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                    num.toString());

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new ImplementationException(this.getMeta(),
                        msg.getText(Locale.getDefault()));

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }

            }
        }

        transaction.setDescription(desc);
        return transaction;
    }

    protected void writeTransaction(final long block,
        final Transaction transaction) throws IOException
    {
        int i;
        int blockIndex = 1;
        long blockOffset;
        long lastBlockOffset;
        int extIndex;
        String str;
        AlphaNumericText27 txt;
        final Transaction.Description desc = transaction.getDescription();
        final Textschluessel type = transaction.getType();
        final int descCount;
        int extCount = desc.getDescriptionCount() > 0 ?
            desc.getDescriptionCount() - 1 : 0;

        if(transaction.getExecutiveExt() != null)
        {
            extCount++;
        }

        if(transaction.getTargetExt() != null)
        {
            extCount++;
        }

        // Konstanter Teil - 1. Satzabschnitt - Feld 1
        this.writeNumber(Fields.FIELD_C1, block,
            DTAUSDisk.CRECORD_OFFSETS1[0], DTAUSDisk.CRECORD_LENGTH1[0],
            DTAUSDisk.CRECORD_CONST_LENGTH +
            extCount * DTAUSDisk.CRECORD_EXT_LENGTH,
            AbstractLogicalFile.ENCODING_ASCII);

        // Konstanter Teil - 1. Satzabschnitt - Feld 2
        this.writeAlphaNumeric(Fields.FIELD_C2, block,
            DTAUSDisk.CRECORD_OFFSETS1[1], DTAUSDisk.CRECORD_LENGTH1[1], "C",
            AbstractLogicalFile.ENCODING_ASCII);

        // Konstanter Teil - 1. Satzabschnitt - Feld 3
        this.writeNumber(Fields.FIELD_C3, block,
            DTAUSDisk.CRECORD_OFFSETS1[2], DTAUSDisk.CRECORD_LENGTH1[2],
            transaction.getPrimaryBank() != null ?
                transaction.getPrimaryBank().intValue() : 0,
            AbstractLogicalFile.ENCODING_ASCII);

        // Konstanter Teil - 1. Satzabschnitt - Feld 4
        this.writeNumber(Fields.FIELD_C4, block,
            DTAUSDisk.CRECORD_OFFSETS1[3], DTAUSDisk.CRECORD_LENGTH1[3],
            transaction.getTargetBank().intValue(),
            AbstractLogicalFile.ENCODING_ASCII);

        // Konstanter Teil - 1. Satzabschnitt - Feld 5
        this.writeNumber(Fields.FIELD_C5, block,
            DTAUSDisk.CRECORD_OFFSETS1[4], DTAUSDisk.CRECORD_LENGTH1[4],
            transaction.getTargetAccount().longValue(),
            AbstractLogicalFile.ENCODING_ASCII);

        // Konstanter Teil - 1. Satzabschnitt - Feld 6
        // TODO -1
        this.writeNumber(-1, block, DTAUSDisk.CRECORD_OFFSETS1[5] - 1, 1, 0L,
            AbstractLogicalFile.ENCODING_ASCII);

        this.writeNumber(Fields.FIELD_C6, block,
            DTAUSDisk.CRECORD_OFFSETS1[5], DTAUSDisk.CRECORD_LENGTH1[5],
            transaction.getReference() != null ?
                transaction.getReference().longValue() : 0L,
            AbstractLogicalFile.ENCODING_ASCII);

        this.writeNumber(-1, block, DTAUSDisk.CRECORD_OFFSETS1[6] - 1, 1, 0L,
            AbstractLogicalFile.ENCODING_ASCII);

        // Konstanter Teil - 1. Satzabschnitt - Felder 7a & 7b
        // TODO -3, +/- 2
        this.writeNumber(Fields.FIELD_C7A, block,
            DTAUSDisk.CRECORD_OFFSETS1[6], DTAUSDisk.CRECORD_LENGTH1[6] - 3,
            type.getKey(), AbstractLogicalFile.ENCODING_ASCII);

        this.writeNumber(Fields.FIELD_C7B, block,
            DTAUSDisk.CRECORD_OFFSETS1[6] + 2, DTAUSDisk.CRECORD_LENGTH1[6] - 2,
            type.getExtension(), AbstractLogicalFile.ENCODING_ASCII);

        // Konstanter Teil - 1. Satzabschnitt - Feld 8
        this.writeAlphaNumeric(Fields.FIELD_C8, block,
            DTAUSDisk.CRECORD_OFFSETS1[7], DTAUSDisk.CRECORD_LENGTH1[7], "",
            AbstractLogicalFile.ENCODING_ASCII);

        // Konstanter Teil - 1. Satzabschnitt - Feld 9
        this.writeNumber(Fields.FIELD_C9, block,
            DTAUSDisk.CRECORD_OFFSETS1[8], DTAUSDisk.CRECORD_LENGTH1[8], 0L,
            AbstractLogicalFile.ENCODING_ASCII);

        // Konstanter Teil - 1. Satzabschnitt - Feld 10
        this.writeNumber(Fields.FIELD_C10, block,
            DTAUSDisk.CRECORD_OFFSETS1[9], DTAUSDisk.CRECORD_LENGTH1[9],
            transaction.getExecutiveBank().intValue(),
            AbstractLogicalFile.ENCODING_ASCII);

        // Konstanter Teil - 1. Satzabschnitt - Feld 11
        this.writeNumber(Fields.FIELD_C11, block,
            DTAUSDisk.CRECORD_OFFSETS1[10], DTAUSDisk.CRECORD_LENGTH1[10],
            transaction.getExecutiveAccount().longValue(),
            AbstractLogicalFile.ENCODING_ASCII);

        // Konstanter Teil - 1. Satzabschnitt - Feld 12
        this.writeNumber(Fields.FIELD_C12, block,
            DTAUSDisk.CRECORD_OFFSETS1[11], DTAUSDisk.CRECORD_LENGTH1[11],
            transaction.getAmount().longValue(),
            AbstractLogicalFile.ENCODING_ASCII); // TODO longValueExact()

        // Konstanter Teil - 1. Satzabschnitt - Feld 13
        this.writeAlphaNumeric(Fields.FIELD_C13, block,
            DTAUSDisk.CRECORD_OFFSETS1[12], DTAUSDisk.CRECORD_LENGTH1[12], "",
            AbstractLogicalFile.ENCODING_ASCII);

        // Konstanter Teil - 1. Satzabschnitt - Feld 14a
        this.writeAlphaNumeric(Fields.FIELD_C14A, block,
            DTAUSDisk.CRECORD_OFFSETS1[13], DTAUSDisk.CRECORD_LENGTH1[13],
            transaction.getTargetName().format(),
            AbstractLogicalFile.ENCODING_ASCII);

        // Konstanter Teil - 1. Satzabschnitt - Feld 14b
        this.writeAlphaNumeric(Fields.FIELD_C14B, block,
            DTAUSDisk.CRECORD_OFFSETS1[14], DTAUSDisk.CRECORD_LENGTH1[14], "",
            AbstractLogicalFile.ENCODING_ASCII);

        // Konstanter Teil - 2. Satzabschnitt - Feld 15(1)
        this.writeAlphaNumeric(Fields.FIELD_C15, block + 1L,
            DTAUSDisk.CRECORD_OFFSETS2[0], DTAUSDisk.CRECORD_LENGTH2[0],
            transaction.getExecutiveName().format(),
            AbstractLogicalFile.ENCODING_ASCII);

        // Konstanter Teil - 2. Satzabschnitt - Feld 16(2)
        this.writeAlphaNumeric(Fields.FIELD_C16, block + 1L,
            DTAUSDisk.CRECORD_OFFSETS2[1], DTAUSDisk.CRECORD_LENGTH2[1],
            desc.getDescriptionCount() > 0 ?
                desc.getDescription(0).format() : "",
            AbstractLogicalFile.ENCODING_ASCII);

        // Konstanter Teil - 2. Satzabschnitt - Feld 17a(3)
        this.writeAlphaNumeric(Fields.FIELD_C17A, block + 1L,
            DTAUSDisk.CRECORD_OFFSETS2[2], DTAUSDisk.CRECORD_LENGTH2[2],
            Character.toString(transaction.getCurrency() != null
            ? this.getCurrencyDirectory().getCode(transaction.getCurrency())
            : ' '),
            AbstractLogicalFile.ENCODING_ASCII);

        // Konstanter Teil - 2. Satzabschnitt - Feld 17b(4)
        this.writeAlphaNumeric(Fields.FIELD_C17B, block + 1L,
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
        if((txt = transaction.getTargetExt()) != null)
        {
            this.writeNumber(
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex],
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPELENGTH[extIndex], 1L,
                AbstractLogicalFile.ENCODING_ASCII);

            this.writeAlphaNumeric(
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex],
                txt.format(), AbstractLogicalFile.ENCODING_ASCII);

            extIndex++;
        }

        // Verwendungszweck-Zeilen des 2., 3., 4., 5. und 6. Satzabschnittes.
        descCount = desc.getDescriptionCount();
        for(i = 1; i < descCount; i++, extIndex++)
        {
            blockOffset = block +
                DTAUSDisk.CRECORD_EXTINDEX_TO_BLOCKOFFSET[extIndex];

            if(blockOffset != lastBlockOffset)
            {
                // Nächsten Satzabschnitt initialisieren.
                this.initializeExtensionBlock(++blockIndex, block);
            }

            this.writeNumber(
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex],
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPELENGTH[extIndex], 2L,
                AbstractLogicalFile.ENCODING_ASCII);

            this.writeAlphaNumeric(
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex],
                desc.getDescription(i).format(),
                AbstractLogicalFile.ENCODING_ASCII);

            lastBlockOffset = blockOffset;
        }

        // Erweiterung des Auftraggeber-Kontos im letzten Erweiterungsteil.
        if((txt = transaction.getExecutiveExt()) != null)
        {
            blockOffset = block +
                DTAUSDisk.CRECORD_EXTINDEX_TO_BLOCKOFFSET[extIndex];

            if(blockOffset != lastBlockOffset)
            {
                // Nächsten Satzabschnitt initialisieren.
                this.initializeExtensionBlock(++blockIndex, block);
            }

            this.writeNumber(
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex],
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPELENGTH[extIndex], 3L,
                AbstractLogicalFile.ENCODING_ASCII);

            this.writeAlphaNumeric(
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex],
                txt.format(), AbstractLogicalFile.ENCODING_ASCII);

            extIndex++;
            lastBlockOffset = blockOffset;
        }
    }

    protected int blockCount(final Transaction transaction)
    {
        int extCount = transaction.getDescription().getDescriptionCount() > 0
            ? transaction.getDescription().getDescriptionCount() - 1 : 0;

        if(transaction.getExecutiveExt() != null)
        {
            extCount++;
        }

        if(transaction.getTargetExt() != null)
        {
            extCount++;
        }

        return DTAUSDisk.CRECORD_EXTENSIONCOUNT_TO_BLOCKCOUNT[extCount];
    }

    protected int blockCount(final long block) throws IOException
    {
        int extCount = this.readNumber(Fields.FIELD_C18,
            block + 1L, DTAUSDisk.CRECORD_OFFSETS2[4],
            DTAUSDisk.CRECORD_LENGTH2[4], AbstractLogicalFile.ENCODING_ASCII).
            intValue();

        if(extCount == -1)
        {
            extCount = 0;
        }

        return DTAUSDisk.CRECORD_EXTENSIONCOUNT_TO_BLOCKCOUNT[extCount];
    }

    //-----------------------------------------------------AbstractLogicalFile--
    //--DTAUSDisk---------------------------------------------------------------

    private void initializeExtensionBlock(final int blockIndex,
        final long block) throws IOException
    {
        int extIndex;
        int startingExt;
        int endingExt;
        int reservedField;
        int reservedOffset;
        int reservedLength;

        if(blockIndex == 1)
        {
            startingExt = 0;
            endingExt = 1;
            reservedField = Fields.FIELD_C23;
            reservedOffset = DTAUSDisk.CRECORD_OFFSETS2[9];
            reservedLength = DTAUSDisk.CRECORD_LENGTH2[9];
        }
        else if(blockIndex == 2)
        {
            startingExt = 2;
            endingExt = 5;
            reservedField = Fields.FIELD_C32;
            reservedOffset = DTAUSDisk.CRECORD_OFFSETS_EXT[8];
            reservedLength = DTAUSDisk.CRECORD_LENGTH_EXT[8];
        }
        else if(blockIndex == 3)
        {
            startingExt = 6;
            endingExt = 9;
            reservedField = Fields.FIELD_C41;
            reservedOffset = DTAUSDisk.CRECORD_OFFSETS_EXT[8];
            reservedLength = DTAUSDisk.CRECORD_LENGTH_EXT[8];
        }
        else if(blockIndex == 4)
        {
            startingExt = 10;
            endingExt = 13;
            reservedField = Fields.FIELD_C50;
            reservedOffset = DTAUSDisk.CRECORD_OFFSETS_EXT[8];
            reservedLength = DTAUSDisk.CRECORD_LENGTH_EXT[8];
        }
        else if(blockIndex == 5)
        {
            startingExt = 14;
            endingExt = 17;
            reservedField = Fields.FIELD_C59;
            reservedOffset = DTAUSDisk.CRECORD_OFFSETS_EXT[8];
            reservedLength = DTAUSDisk.CRECORD_LENGTH_EXT[8];
        }
        else
        {
            throw new IllegalArgumentException(Integer.toString(blockIndex));
        }

        // Erweiterungsteile leeren.
        for(extIndex = startingExt; extIndex <= endingExt; extIndex++)
        {
            final long blockOffset = block +
                DTAUSDisk.CRECORD_EXTINDEX_TO_BLOCKOFFSET[extIndex];

            this.writeNumber(
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex],
                DTAUSDisk.CRECORD_EXTINDEX_TO_TYPELENGTH[extIndex], 0L,
                AbstractLogicalFile.ENCODING_ASCII);

            this.writeAlphaNumeric(
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex], blockOffset,
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                DTAUSDisk.CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex], "",
                AbstractLogicalFile.ENCODING_ASCII);

        }

        // Reserve-Feld initialisieren.
        this.writeAlphaNumeric(reservedField, block + blockIndex,
            reservedOffset, reservedLength, "",
            AbstractLogicalFile.ENCODING_ASCII);

    }

    protected MemoryManager getMemoryManagerImpl()
    {
        return this.getMemoryManager();
    }

    protected Logger getLoggerImpl()
    {
        return this.getLogger();
    }

    protected TaskMonitor getTaskMonitorImpl()
    {
        return this.getTaskMonitor();
    }

    protected TextschluesselVerzeichnis getTextschluesselVerzeichnisImpl()
    {
        return this.getTextschluesselVerzeichnis();
    }

    protected ApplicationLogger getApplicationLoggerImpl()
    {
        return this.getApplicationLogger();
    }

    protected Implementation getMeta()
    {
        return DTAUSDisk.META;
    }

//---------------------------------------------------------------DTAUSDisk--

}

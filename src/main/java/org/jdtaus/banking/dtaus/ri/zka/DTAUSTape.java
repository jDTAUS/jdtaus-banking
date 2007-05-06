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
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Calendar;
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
import org.jdtaus.banking.dtaus.CorruptedException;
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.LogicalFileType;
import org.jdtaus.banking.dtaus.Transaction;
import org.jdtaus.banking.dtaus.spi.Fields;
import org.jdtaus.banking.dtaus.ri.zka.messages.IllegalDataMessage;
import org.jdtaus.banking.dtaus.ri.zka.messages.IllegalScheduleMessage;
import org.jdtaus.banking.spi.CurrencyMapper;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.ContextFactory;
import org.jdtaus.core.container.ContextInitializer;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.container.Properties;
import org.jdtaus.core.container.Property;
import org.jdtaus.core.io.util.StructuredFileOperations;
import org.jdtaus.core.lang.spi.MemoryManager;
import org.jdtaus.core.logging.spi.Logger;
import org.jdtaus.core.monitor.spi.TaskMonitor;
import org.jdtaus.core.text.Message;
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
public final class DTAUSTape extends AbstractLogicalFile
{

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
    private transient Calendar calendar;

    //---------------------------------------------------------------Attribute--
    //--Implementation----------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(DTAUSTape.class.getName());

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

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
     * @throws IOException wenn nicht gelesen werden kann.
     */
    public DTAUSTape(final long headerBlock,
        final StructuredFileOperations persistence) throws IOException
    {
        super();
        this.initializeProperties(DTAUSTape.META.getProperties());

        if(persistence == null)
        {
            throw new NullPointerException("persistence");
        }

        if(persistence.getBlockSize() != 150)
        {
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

    // This section is managed by jdtaus-container-mojo.

    /** Configured <code>CurrencyMapper</code> implementation. */
    private transient CurrencyMapper _dependency5;

    /**
     * Gets the configured <code>CurrencyMapper</code> implementation.
     *
     * @return the configured <code>CurrencyMapper</code> implementation.
     */
    private CurrencyMapper getCurrencyMapper()
    {
        CurrencyMapper ret = null;
        if(this._dependency5 != null)
        {
            ret = this._dependency5;
        }
        else
        {
            ret = (CurrencyMapper) ContainerFactory.getContainer().
                getDependency(DTAUSTape.class,
                "CurrencyMapper");

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSTape.class.getName()).
                getDependencies().getDependency("CurrencyMapper").
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

    /**
     * Gets the configured <code>TextschluesselVerzeichnis</code> implementation.
     *
     * @return the configured <code>TextschluesselVerzeichnis</code> implementation.
     */
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
                getDependency(DTAUSTape.class,
                "TextschluesselVerzeichnis");

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSTape.class.getName()).
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

    /**
     * Gets the configured <code>TaskMonitor</code> implementation.
     *
     * @return the configured <code>TaskMonitor</code> implementation.
     */
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
                getDependency(DTAUSTape.class,
                "TaskMonitor");

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSTape.class.getName()).
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

    /**
     * Gets the configured <code>ApplicationLogger</code> implementation.
     *
     * @return the configured <code>ApplicationLogger</code> implementation.
     */
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
                getDependency(DTAUSTape.class,
                "ApplicationLogger");

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSTape.class.getName()).
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

    /**
     * Gets the configured <code>MemoryManager</code> implementation.
     *
     * @return the configured <code>MemoryManager</code> implementation.
     */
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
                getDependency(DTAUSTape.class,
                "MemoryManager");

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSTape.class.getName()).
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

    /**
     * Gets the configured <code>Logger</code> implementation.
     *
     * @return the configured <code>Logger</code> implementation.
     */
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
                getDependency(DTAUSTape.class,
                "Logger");

            if(ModelFactory.getModel().getModules().
                getImplementation(DTAUSTape.class.getName()).
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
        int ret = 1;
        // Konstanter Teil - 1. Satzabschnitt - Feld 18
        final long extCount = this.readNumberPackedPositive(
            Fields.FIELD_C18, block, DTAUSTape.CRECORD_OFFSETS1[21],
            DTAUSTape.CRECORD_LENGTH1[21], true);

        if(extCount != AbstractLogicalFile.NO_NUMBER)
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
            DTAUSTape.ARECORD_OFFSETS[2], DTAUSTape.ARECORD_LENGTH[2],
            AbstractLogicalFile.ENCODING_EBCDI);

        char ret;
        final Message msg;

        if(str == null || str.length() != 1)
        {
            msg = new IllegalDataMessage(Fields.FIELD_A2,
                IllegalDataMessage.TYPE_CONSTANT,
                block * this.persistence.getBlockSize() +
                DTAUSTape.ARECORD_OFFSETS[2], str);

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new CorruptedException(this.getMeta(),
                    block * this.persistence.getBlockSize() +
                    DTAUSTape.ARECORD_OFFSETS[2]);

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

    protected int blockCount(final Transaction transaction)
    {
        int extCount = transaction.getDescription().getDescriptionCount() > 0 ?
            transaction.getDescription().getDescriptionCount() - 1 : 0;

        if(transaction.getExecutiveExt() != null)
        {
            extCount++;
        }
        if(transaction.getTargetExt() != null)
        {
            extCount++;
        }
        return DTAUSTape.CRECORD_EXTENSIONCOUNT_TO_BLOCKCOUNT[extCount];
    }

    protected int blockCount(final long block) throws IOException
    {
        long extCount = this.readNumberPackedPositive(
            Fields.FIELD_C18, block, DTAUSTape.CRECORD_OFFSETS1[21],
            DTAUSTape.CRECORD_LENGTH1[21], true);

        if(extCount == AbstractLogicalFile.NO_NUMBER)
        {
            extCount = 0;
        }

        return DTAUSTape.CRECORD_EXTENSIONCOUNT_TO_BLOCKCOUNT[(int) extCount];
    }

    public Header readHeader(final long headerBlock) throws IOException
    {
        long num;
        Long Num;
        int cal;
        String str;
        final Currency cur;
        final Date createDate;
        final Date executionDate;
        final LogicalFileType label;
        final Header ret;
        final int blockSize;
        boolean isBank = false;
        Message msg;
        ret = new Header();
        Header.Schedule schedule = null;
        blockSize = this.persistence.getBlockSize();

        // Feld 1
        num = this.readNumberBinary(Fields.FIELD_A1, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[0], DTAUSTape.ARECORD_LENGTH[0]);

        if(num != blockSize)
        {
            msg = new IllegalDataMessage(Fields.FIELD_A1,
                IllegalDataMessage.TYPE_CONSTANT,
                headerBlock * this.persistence.getBlockSize() +
                DTAUSTape.ARECORD_OFFSETS[0], Long.toString(num));

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new CorruptedException(this.getMeta(),
                    headerBlock * this.persistence.getBlockSize() +
                    DTAUSTape.ARECORD_OFFSETS[0]);

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }
        }

        // Feld 2
        str = this.readAlphaNumeric(Fields.FIELD_A2, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[2], DTAUSTape.ARECORD_LENGTH[2],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null && (str.length() != 1 || str.toCharArray()[0] != 'A'))
        {
            msg = new IllegalDataMessage(Fields.FIELD_A1,
                IllegalDataMessage.TYPE_CONSTANT,
                headerBlock * this.persistence.getBlockSize() +
                DTAUSTape.ARECORD_OFFSETS[0], str);

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new CorruptedException(this.getMeta(),
                    headerBlock * this.persistence.getBlockSize() +
                    DTAUSTape.ARECORD_OFFSETS[0]);

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }
        }

        // Feld 3
        str = this.readAlphaNumeric(Fields.FIELD_A3, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[3], DTAUSTape.ARECORD_LENGTH[3],
            AbstractLogicalFile.ENCODING_EBCDI);

        ret.setType(null);

        if(str != null)
        {
            label = LogicalFileType.valueOf(str);
            if(label == null)
            {
                msg = new IllegalDataMessage(Fields.FIELD_A3,
                    IllegalDataMessage.TYPE_FILETYPE,
                    headerBlock * this.persistence.getBlockSize() +
                    DTAUSTape.ARECORD_OFFSETS[3], str);

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(this.getMeta(),
                        headerBlock * this.persistence.getBlockSize() +
                        DTAUSTape.ARECORD_OFFSETS[3]);

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
        num = this.readNumberPackedPositive(Fields.FIELD_A4, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[4], DTAUSTape.ARECORD_LENGTH[4], true);

        ret.setBank(null);
        if(num != AbstractLogicalFile.NO_NUMBER)
        {
            if(!Bankleitzahl.checkBankleitzahl(new Long(num)))
            {
                msg = new IllegalDataMessage(Fields.FIELD_A4,
                    IllegalDataMessage.TYPE_BANKLEITZAHL,
                    headerBlock * this.persistence.getBlockSize() +
                    DTAUSTape.ARECORD_OFFSETS[4], str);

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(this.getMeta(),
                        headerBlock * this.persistence.getBlockSize() +
                        DTAUSTape.ARECORD_OFFSETS[4]);

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }
            }
            else
            {
                ret.setBank(Bankleitzahl.valueOf(new Long(num)));
            }
        }

        // Feld 5
        // Nur belegt wenn Absender Kreditinistitut ist, sonst 0.
        num = this.readNumberPackedPositive(Fields.FIELD_A5, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[5], DTAUSTape.ARECORD_LENGTH[5],
            true);

        ret.setBankData(null);
        if(num != AbstractLogicalFile.NO_NUMBER)
        {
            if(isBank)
            {
                if(!Bankleitzahl.checkBankleitzahl(new Long(num)))
                {
                    msg = new IllegalDataMessage(Fields.FIELD_A5,
                        IllegalDataMessage.TYPE_BANKLEITZAHL,
                        this.getHeaderBlock() * this.persistence.getBlockSize()
                        + DTAUSTape.ARECORD_OFFSETS[5], Long.toString(num));

                    if(AbstractErrorMessage.isErrorsEnabled())
                    {
                        throw new CorruptedException(this.getMeta(),
                            this.getHeaderBlock() *
                            this.persistence.getBlockSize()
                            + DTAUSTape.ARECORD_OFFSETS[5]);

                    }
                    else
                    {
                        ThreadLocalMessages.getMessages().addMessage(msg);
                    }
                }
                else
                {
                    ret.setBankData(Bankleitzahl.valueOf(new Long(num)));
                }
            }
        }

        // Feld 6
        str = this.readAlphaNumeric(Fields.FIELD_A6, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[6], DTAUSTape.ARECORD_LENGTH[6],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null)
        {
            try
            {
                ret.setCustomer(AlphaNumericText27.parse(str));
            }
            catch(ParseException e)
            {
                throw new AssertionError(e);
            }
        }

        // Feld 7
        num = this.readNumberPackedPositive(Fields.FIELD_A7, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[7], DTAUSTape.ARECORD_LENGTH[7], true);

        if(num != AbstractLogicalFile.NO_NUMBER)
        {
            this.calendar.clear();
            cal = (int) Math.floor(num / AbstractLogicalFile.EXP10[4]);
            num -= (cal * AbstractLogicalFile.EXP10[4]);
            this.calendar.set(Calendar.DAY_OF_MONTH, cal);
            cal = (int) Math.floor(num / AbstractLogicalFile.EXP10[2]);
            num -= (cal * AbstractLogicalFile.EXP10[2]);
            this.calendar.set(Calendar.MONTH, cal - 1);
            num = num <= 79L ? 2000L + num : 1900L + num;
            this.calendar.set(Calendar.YEAR, (int) num);
            createDate = this.calendar.getTime();
            if(!Header.Schedule.checkDate(createDate))
            {
                msg = new IllegalDataMessage(Fields.FIELD_A7,
                    IllegalDataMessage.TYPE_SHORTDATE,
                    this.getHeaderBlock() * this.persistence.getBlockSize() +
                    DTAUSTape.ARECORD_OFFSETS[7], Long.toString(num));

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(this.getMeta(),
                        this.getHeaderBlock() *
                        this.persistence.getBlockSize() +
                        DTAUSTape.ARECORD_OFFSETS[7]);

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }
            }
        }
        else
        {
            createDate = null;
        }

        // Feld 8
        // Nur belegt wenn Absender Kreditinistitut ist, sonst leer.
        str = this.readAlphaNumeric(Fields.FIELD_A8, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[8], DTAUSTape.ARECORD_LENGTH[8],
            AbstractLogicalFile.ENCODING_EBCDI);

        // Feld 9
        num = this.readNumberPackedPositive(Fields.FIELD_A9, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[9], DTAUSTape.ARECORD_LENGTH[9], true);

        ret.setAccount(null);
        if(num != AbstractLogicalFile.NO_NUMBER)
        {
            if(!Kontonummer.checkKontonummer(new Long(num)))
            {
                msg = new IllegalDataMessage(Fields.FIELD_A9,
                    IllegalDataMessage.TYPE_KONTONUMMER,
                    this.getHeaderBlock() * this.persistence.getBlockSize() +
                    DTAUSTape.ARECORD_OFFSETS[9], Long.toString(num));

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(this.getMeta(),
                        this.getHeaderBlock() *
                        this.persistence.getBlockSize() +
                        DTAUSTape.ARECORD_OFFSETS[9]);

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }
            }
            else
            {
                ret.setAccount(Kontonummer.valueOf(new Long(num)));
            }
        }

        // Feld 10
        Num = this.readNumber(Fields.FIELD_A10, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[10], DTAUSTape.ARECORD_LENGTH[10],
            AbstractLogicalFile.ENCODING_EBCDI);

        num = Num.longValue();
        if(num != AbstractLogicalFile.NO_NUMBER)
        {
            if(!Referenznummer10.checkReferenznummer10(Num))
            {
                msg = new IllegalDataMessage(Fields.FIELD_A10,
                    IllegalDataMessage.TYPE_REFERENZNUMMER,
                    this.getHeaderBlock() * this.persistence.getBlockSize() +
                    DTAUSTape.ARECORD_OFFSETS[10], Num.toString());

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(this.getMeta(),
                        this.getHeaderBlock() *
                        this.persistence.getBlockSize() +
                        DTAUSTape.ARECORD_OFFSETS[10]);

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }
            }
            else
            {
                ret.setReference(Referenznummer10.valueOf(Num));
            }
        }

        // Feld 11b
        executionDate = this.readLongDate(Fields.FIELD_A11B, headerBlock,
            DTAUSTape.ARECORD_OFFSETS[12], AbstractLogicalFile.ENCODING_EBCDI);

        ret.setSchedule(null);
        if(createDate != null)
        {
            if(!Header.Schedule.checkSchedule(createDate, executionDate))
            {
                msg = new IllegalScheduleMessage(this.getHeaderBlock() *
                    this.persistence.getBlockSize() +
                    DTAUSTape.ARECORD_OFFSETS[12], createDate, executionDate);

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(this.getMeta(),
                        this.getHeaderBlock() *
                        this.persistence.getBlockSize() +
                        DTAUSTape.ARECORD_OFFSETS[12]);

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
            DTAUSTape.ARECORD_OFFSETS[14], DTAUSTape.ARECORD_LENGTH[14],
            AbstractLogicalFile.ENCODING_EBCDI);

        ret.setCurrency(null);
        if(str != null)
        {
            if(str.length() != 1)
            {
                msg = new IllegalDataMessage(Fields.FIELD_A12,
                    IllegalDataMessage.TYPE_ALPHA_NUMERIC,
                    headerBlock * this.persistence.getBlockSize() +
                    DTAUSTape.ARECORD_OFFSETS[14], str);

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(this.getMeta(),
                        headerBlock * this.persistence.getBlockSize() +
                        DTAUSTape.ARECORD_OFFSETS[14]);

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }
            }
            else
            {
                final char c = str.toCharArray()[0];
                cur = this.getCurrencyMapper().
                    getDtausCurrency(c, createDate);

                if(cur == null)
                {
                    msg = new IllegalDataMessage(Fields.FIELD_A12,
                        IllegalDataMessage.TYPE_CURRENCY,
                        headerBlock * this.persistence.getBlockSize() +
                        DTAUSTape.ARECORD_OFFSETS[14],
                        Character.toString(c));

                    if(AbstractErrorMessage.isErrorsEnabled())
                    {
                        throw new CorruptedException(this.getMeta(),
                            headerBlock * this.persistence.getBlockSize() +
                            DTAUSTape.ARECORD_OFFSETS[14]);

                    }
                    else
                    {
                        ThreadLocalMessages.getMessages().addMessage(msg);
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
            Character.toString(this.getCurrencyMapper().
            getDtausCode(header.getCurrency(), header.getSchedule().
            getCreateDate())), AbstractLogicalFile.ENCODING_EBCDI);

    }

    protected Checksum readChecksum(final long checksumBlock) throws IOException
    {
        long num;
        final String str;
        final Checksum checksum;

        Message msg;
        checksum = new Checksum();

        // Feld 1
        num = this.readNumberBinary(Fields.FIELD_E1, checksumBlock,
            DTAUSTape.ERECORD_OFFSETS[0], DTAUSTape.ERECORD_LENGTH[0]);

        if(num != this.persistence.getBlockSize())
        {
            msg = new IllegalDataMessage(Fields.FIELD_E1,
                IllegalDataMessage.TYPE_CONSTANT,
                checksumBlock * this.persistence.getBlockSize() +
                DTAUSTape.ERECORD_OFFSETS[0], Long.toString(num));

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new CorruptedException(this.getMeta(),
                    checksumBlock * this.persistence.getBlockSize() +
                    DTAUSTape.ERECORD_OFFSETS[0]);

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }
        }

        // Feld 2
        str = this.readAlphaNumeric(Fields.FIELD_E2, checksumBlock,
            DTAUSTape.ERECORD_OFFSETS[2], DTAUSTape.ERECORD_LENGTH[2],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null && (str.length() != 1 || str.toCharArray()[0] != 'E'))
        {
            msg = new IllegalDataMessage(Fields.FIELD_E2,
                IllegalDataMessage.TYPE_CONSTANT,
                checksumBlock * this.persistence.getBlockSize() +
                DTAUSTape.ERECORD_OFFSETS[2], str);

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new CorruptedException(this.getMeta(),
                    checksumBlock * this.persistence.getBlockSize() +
                    DTAUSTape.ERECORD_OFFSETS[2]);

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }
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

    protected void writeChecksum(final long checksumBlock,
        final Checksum checksum) throws IOException
    {
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

    protected Transaction readTransaction(final long block,
        final Transaction transaction) throws IOException
    {
        long num;
        Long Num;
        String str;
        int search;
        long keyType;
        long blockOffset;
        final long extCount;
        final Currency cur;
        final Textschluessel type;
        final Transaction.Description desc = new Transaction.Description();
        Message msg;

        transaction.setExecutiveExt(null);
        transaction.setTargetExt(null);
        // Konstanter Teil - 1. Satzabschnitt - Feld 18
        extCount = this.readNumberPackedPositive(Fields.FIELD_C18, block,
            DTAUSTape.CRECORD_OFFSETS1[21], DTAUSTape.CRECORD_LENGTH1[21],
            true);

        // Konstanter Teil - Satzaschnitt 1 - Feld 1
        num = this.readNumberBinary(Fields.FIELD_C1, block,
            DTAUSTape.CRECORD_OFFSETS1[0], DTAUSTape.CRECORD_LENGTH1[0]);

        if(extCount != AbstractLogicalFile.NO_NUMBER &&
            num != DTAUSTape.CRECORD_CONST_LENGTH +
            extCount * DTAUSTape.CRECORD_EXT_LENGTH)
        {
            msg = new IllegalDataMessage(Fields.FIELD_C1,
                IllegalDataMessage.TYPE_NUMERIC,
                block * this.persistence.getBlockSize() +
                DTAUSTape.CRECORD_OFFSETS1[0], Long.toString(num));

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new CorruptedException(this.getMeta(),
                    block * this.persistence.getBlockSize() +
                    DTAUSTape.CRECORD_OFFSETS1[0]);

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 2
        str = this.readAlphaNumeric(Fields.FIELD_C2, block,
            DTAUSTape.CRECORD_OFFSETS1[2], DTAUSTape.CRECORD_LENGTH1[2],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null && (str.length() != 1 || str.toCharArray()[0] != 'C'))
        {
            msg = new IllegalDataMessage(Fields.FIELD_C2,
                IllegalDataMessage.TYPE_CONSTANT,
                block * this.persistence.getBlockSize() +
                DTAUSTape.CRECORD_OFFSETS1[2], str);

            if(AbstractErrorMessage.isErrorsEnabled())
            {
                throw new CorruptedException(this.getMeta(),
                    block * this.persistence.getBlockSize() +
                    DTAUSTape.CRECORD_OFFSETS1[2]);

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage(msg);
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 3
        num = this.readNumberPackedPositive(Fields.FIELD_C3, block,
            DTAUSTape.CRECORD_OFFSETS1[3], DTAUSTape.CRECORD_LENGTH1[3], true);

        transaction.setPrimaryBank(null);
        if(num != AbstractLogicalFile.NO_NUMBER && num != 0L)
        {
            if(!Bankleitzahl.checkBankleitzahl(new Long(num)))
            {
                msg = new IllegalDataMessage(Fields.FIELD_C3,
                    IllegalDataMessage.TYPE_BANKLEITZAHL,
                    block * this.persistence.getBlockSize() +
                    DTAUSTape.CRECORD_OFFSETS1[3], Long.toString(num));

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(this.getMeta(),
                        block * this.persistence.getBlockSize() +
                        DTAUSTape.CRECORD_OFFSETS1[3]);

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }
            }
            else
            {
                transaction.setPrimaryBank(Bankleitzahl.valueOf(new Long(num)));
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 4
        num = this.readNumberPackedPositive(Fields.FIELD_C4, block,
            DTAUSTape.CRECORD_OFFSETS1[4], DTAUSTape.CRECORD_LENGTH1[4], true);

        transaction.setTargetBank(null);
        if(num != AbstractLogicalFile.NO_NUMBER)
        {
            if(!Bankleitzahl.checkBankleitzahl(new Long(num)))
            {
                msg = new IllegalDataMessage(Fields.FIELD_C4,
                    IllegalDataMessage.TYPE_BANKLEITZAHL,
                    block * this.persistence.getBlockSize() +
                    DTAUSTape.CRECORD_OFFSETS1[4], Long.toString(num));

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(this.getMeta(),
                        block * this.persistence.getBlockSize() +
                        DTAUSTape.CRECORD_OFFSETS1[4]);

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }
            }
            else
            {
                transaction.setTargetBank(Bankleitzahl.valueOf(new Long(num)));
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 5
        num = this.readNumberPackedPositive(Fields.FIELD_C5, block,
            DTAUSTape.CRECORD_OFFSETS1[5], DTAUSTape.CRECORD_LENGTH1[5], true);

        transaction.setTargetAccount(null);
        if(num != AbstractLogicalFile.NO_NUMBER)
        {
            if(!Kontonummer.checkKontonummer(new Long(num)))
            {
                msg = new IllegalDataMessage(Fields.FIELD_C5,
                    IllegalDataMessage.TYPE_KONTONUMMER,
                    block * this.persistence.getBlockSize() +
                    DTAUSTape.CRECORD_OFFSETS1[5], Long.toString(num));

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(this.getMeta(),
                        block * this.persistence.getBlockSize() +
                        DTAUSTape.CRECORD_OFFSETS1[5]);

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }
            }
            else
            {
                transaction.setTargetAccount(Kontonummer.valueOf(new Long(num)));
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 6a
        num = this.readNumberPackedPositive(Fields.FIELD_C6A, block,
            DTAUSTape.CRECORD_OFFSETS1[6], DTAUSTape.CRECORD_LENGTH1[6], false);

        transaction.setReference(null);
        if(num != AbstractLogicalFile.NO_NUMBER)
        {
            if(!Referenznummer11.checkReferenznummer11(new Long(num)))
            {
                msg = new IllegalDataMessage(Fields.FIELD_C6A,
                    IllegalDataMessage.TYPE_REFERENZNUMMER,
                    block * this.persistence.getBlockSize() +
                    DTAUSTape.CRECORD_OFFSETS1[6], Long.toString(num));

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(this.getMeta(),
                        block * this.persistence.getBlockSize() +
                        DTAUSTape.CRECORD_OFFSETS1[6]);

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }
            }
            else
            {
                transaction.setReference(Referenznummer11.valueOf(new Long(num)));
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 6b
        //num = this.readNumberPackedPositive(block,
        //    DTAUSTape.CRECORD_OFFSETS1[7], DTAUSTape.CRECORD_LENGTH1[7], true);

        // Konstanter Teil - Satzaschnitt 1 - Feld 7a
        keyType = this.readNumberPackedPositive(Fields.FIELD_C7A, block,
            DTAUSTape.CRECORD_OFFSETS1[8], DTAUSTape.CRECORD_LENGTH1[8], false);

        // Konstanter Teil - Satzaschnitt 1 - Feld 7b
        num = this.readNumberPackedPositive(Fields.FIELD_C7B, block,
            DTAUSTape.CRECORD_OFFSETS1[9], DTAUSTape.CRECORD_LENGTH1[9], true);

        transaction.setType(null);
        if(num != AbstractLogicalFile.NO_NUMBER &&
            keyType != AbstractLogicalFile.NO_NUMBER)
        {
            type = this.getTextschluesselVerzeichnis().
                getTextschluessel((int) keyType, (int) num);

            if(type == null
                || (type.isDebit() && !this.getHeader().getType().isDebitAllowed())
                || (type.isRemittance() && !this.getHeader().getType().
                isRemittanceAllowed()))
            {
                msg = new IllegalDataMessage(Fields.FIELD_C7A,
                    IllegalDataMessage.TYPE_TEXTSCHLUESSEL,
                    block * this.persistence.getBlockSize() +
                    DTAUSTape.CRECORD_OFFSETS1[8], Long.toString(keyType) +
                    Long.toString(num));

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(this.getMeta(),
                        block * this.persistence.getBlockSize() +
                        DTAUSTape.CRECORD_OFFSETS1[8]);

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
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 10
        num = this.readNumberPackedPositive(Fields.FIELD_C10, block,
            DTAUSTape.CRECORD_OFFSETS1[12], DTAUSTape.CRECORD_LENGTH1[12],
            true);

        transaction.setExecutiveBank(null);
        if(num != AbstractLogicalFile.NO_NUMBER)
        {
            if(!Bankleitzahl.checkBankleitzahl(new Long(num)))
            {
                msg = new IllegalDataMessage(Fields.FIELD_C10,
                    IllegalDataMessage.TYPE_BANKLEITZAHL,
                    block * this.persistence.getBlockSize() +
                    DTAUSTape.CRECORD_OFFSETS1[12], Long.toString(num));

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(this.getMeta(),
                        block * this.persistence.getBlockSize() +
                        DTAUSTape.CRECORD_OFFSETS1[12]);

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }
            }
            else
            {
                transaction.setExecutiveBank(
                    Bankleitzahl.valueOf(new Long(num)));

            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 11
        num = this.readNumberPackedPositive(Fields.FIELD_C11, block,
            DTAUSTape.CRECORD_OFFSETS1[13], DTAUSTape.CRECORD_LENGTH1[13],
            true);

        transaction.setExecutiveAccount(null);
        if(num != AbstractLogicalFile.NO_NUMBER)
        {
            if(!Kontonummer.checkKontonummer(new Long(num)))
            {
                msg = new IllegalDataMessage(Fields.FIELD_C11,
                    IllegalDataMessage.TYPE_KONTONUMMER,
                    block * this.persistence.getBlockSize() +
                    DTAUSTape.CRECORD_OFFSETS1[13], Long.toString(num));

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(this.getMeta(),
                        block * this.persistence.getBlockSize() +
                        DTAUSTape.CRECORD_OFFSETS1[13]);

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }
            }
            else
            {
                transaction.setExecutiveAccount(
                    Kontonummer.valueOf(new Long(num)));

            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 12
        num = this.readNumberPackedPositive(Fields.FIELD_C12, block,
            DTAUSTape.CRECORD_OFFSETS1[14], DTAUSTape.CRECORD_LENGTH1[14],
            true);

        transaction.setAmount(num != AbstractLogicalFile.NO_NUMBER ?
            BigInteger.valueOf(num) : null);

        // Konstanter Teil - Satzaschnitt 1 - Feld 14
        str = this.readAlphaNumeric(Fields.FIELD_C14, block,
            DTAUSTape.CRECORD_OFFSETS1[16], DTAUSTape.CRECORD_LENGTH1[16],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null)
        {
            try
            {
                transaction.setTargetName(AlphaNumericText27.parse(str));
            }
            catch(ParseException e)
            {
                throw new AssertionError(e);
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 15
        str = this.readAlphaNumeric(Fields.FIELD_C15, block,
            DTAUSTape.CRECORD_OFFSETS1[17], DTAUSTape.CRECORD_LENGTH1[17],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null)
        {
            try
            {
                transaction.setExecutiveName(AlphaNumericText27.parse(str));
            }
            catch(ParseException e)
            {
                throw new AssertionError(e);
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 16
        str = this.readAlphaNumeric(Fields.FIELD_C16, block,
            DTAUSTape.CRECORD_OFFSETS1[18], DTAUSTape.CRECORD_LENGTH1[18],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null)
        {
            try
            {
                desc.addDescription(AlphaNumericText27.parse(str));
            }
            catch(ParseException e)
            {
                throw new AssertionError(e);
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 17a
        str = this.readAlphaNumeric(Fields.FIELD_C17A, block,
            DTAUSTape.CRECORD_OFFSETS1[19], DTAUSTape.CRECORD_LENGTH1[19],
            AbstractLogicalFile.ENCODING_EBCDI);

        if(str != null)
        {
            if(str.length() != 1)
            {
                msg = new IllegalDataMessage(Fields.FIELD_C17A,
                    IllegalDataMessage.TYPE_CURRENCY,
                    block * this.persistence.getBlockSize() +
                    DTAUSTape.CRECORD_OFFSETS1[19], str);

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(this.getMeta(),
                        block * this.persistence.getBlockSize() +
                        DTAUSTape.CRECORD_OFFSETS1[19]);

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage(msg);
                }
            }
            else
            {
                final char c = str.toCharArray()[0];
                cur = this.getCurrencyMapper().getDtausCurrency(c,
                    this.getHeader().getSchedule().getCreateDate());

                if(cur == null)
                {
                    msg = new IllegalDataMessage(Fields.FIELD_A12,
                        IllegalDataMessage.TYPE_CURRENCY,
                        block * this.persistence.getBlockSize() +
                        DTAUSTape.CRECORD_OFFSETS1[19],
                        Character.toString(c));

                    if(AbstractErrorMessage.isErrorsEnabled())
                    {
                        throw new CorruptedException(this.getMeta(),
                            block * this.persistence.getBlockSize() +
                            DTAUSTape.CRECORD_OFFSETS1[19]);

                    }
                    else
                    {
                        ThreadLocalMessages.getMessages().addMessage(msg);
                    }
                }

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
        for(search = 0; search < extCount &&
            extCount != AbstractLogicalFile.NO_NUMBER; search++)
        {
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

            num = Num.longValue();
            if(num == 1L)
            {
                if(transaction.getTargetExt() != null)
                {
                    msg = new IllegalDataMessage(
                        DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[search],
                        IllegalDataMessage.TYPE_NUMERIC,
                        blockOffset * this.persistence.getBlockSize() +
                        DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                        Num.toString());

                    if(AbstractErrorMessage.isErrorsEnabled())
                    {
                        throw new CorruptedException(this.getMeta(),
                            blockOffset * this.persistence.getBlockSize() +
                            DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[search]);

                    }
                    else
                    {
                        ThreadLocalMessages.getMessages().addMessage(msg);
                    }
                }
                else if (str != null)
                {
                    try
                    {
                        transaction.setTargetExt(AlphaNumericText27.parse(str));
                    }
                    catch(ParseException e)
                    {
                        throw new AssertionError(e);
                    }
                }
            }
            else if(num == 2L)
            {
                if(str != null)
                {
                    try
                    {
                        desc.addDescription(AlphaNumericText27.parse(str));
                    }
                    catch(ParseException e)
                    {
                        throw new AssertionError(e);
                    }
                }
            }
            else if(num == 3L)
            {
                if(transaction.getExecutiveExt() != null)
                {
                    msg = new IllegalDataMessage(
                        DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[search],
                        IllegalDataMessage.TYPE_NUMERIC,
                        blockOffset * this.persistence.getBlockSize() +
                        DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                        Num.toString());

                    if(AbstractErrorMessage.isErrorsEnabled())
                    {
                        throw new CorruptedException(this.getMeta(),
                            blockOffset * this.persistence.getBlockSize() +
                            DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[search]);

                    }
                    else
                    {
                        ThreadLocalMessages.getMessages().addMessage(msg);
                    }
                }
                else
                {
                    try
                    {
                        transaction.setExecutiveExt(
                            AlphaNumericText27.parse(str));

                    }
                    catch(ParseException e)
                    {
                        throw new AssertionError(e);
                    }
                }
            }
            else if(num != AbstractLogicalFile.NO_NUMBER)
            {
                msg = new IllegalDataMessage(
                    DTAUSTape.CRECORD_EXTINDEX_TO_TYPEFIELD[search],
                    IllegalDataMessage.TYPE_NUMERIC,
                    blockOffset * this.persistence.getBlockSize() +
                    DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[search],
                    Num.toString());

                if(AbstractErrorMessage.isErrorsEnabled())
                {
                    throw new CorruptedException(this.getMeta(),
                        blockOffset * this.persistence.getBlockSize() +
                        DTAUSTape.CRECORD_EXTINDEX_TO_TYPEOFFSET[search]);

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
        long blockOffset;
        int extIndex;
        int followingIndex;
        AlphaNumericText27 txt;
        final Textschluessel type = transaction.getType();
        final Transaction.Description desc = transaction.getDescription();
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
            Character.toString(this.getCurrencyMapper().getDtausCode(
            transaction.getCurrency(), this.getHeader().getSchedule().
            getCreateDate())), AbstractLogicalFile.ENCODING_EBCDI);

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
        if((txt = transaction.getTargetExt()) != null)
        {
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
                ;followingIndex > 0; followingIndex--)
            {

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
        for(i = 1; i < descCount; i++, extIndex++)
        {
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
                ;followingIndex > 0; followingIndex--)
            {

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
        if((txt = transaction.getExecutiveExt()) != null)
        {
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
                ;followingIndex > 0; followingIndex--)
            {

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

    protected MemoryManager getMemoryManagerImpl()
    {
        return this.getMemoryManager();
    }

    protected Logger getLoggerImpl()
    {
        return this.getLogger();
    }

    protected ApplicationLogger getApplicationLoggerImpl()
    {
        return this.getApplicationLogger();
    }

    protected TaskMonitor getTaskMonitorImpl()
    {
        return this.getTaskMonitor();
    }

    protected TextschluesselVerzeichnis getTextschluesselVerzeichnisImpl()
    {
        return this.getTextschluesselVerzeichnis();
    }

    protected Implementation getMeta()
    {
        return DTAUSTape.META;
    }

    protected CurrencyMapper getCurrencyMapperImpl()
    {
        return this.getCurrencyMapper();
    }

    //-----------------------------------------------------AbstractLogicalFile--

}

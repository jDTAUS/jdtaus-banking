/*
 *  jDTAUS Banking RI DTAUS
 *  Copyright (c) 2005 Christian Schulte
 *
 *  Christian Schulte, Haldener Strasse 72, 58095 Hagen, Germany
 *  <schulte2005@users.sourceforge.net> (+49 2331 3543887)
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
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;
import java.util.Locale;
import javax.swing.event.EventListenerList;
import org.jdtaus.banking.AlphaNumericText27;
import org.jdtaus.banking.TextschluesselVerzeichnis;
import org.jdtaus.banking.dtaus.Checksum;
import org.jdtaus.banking.dtaus.CorruptedException;
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.LogicalFile;
import org.jdtaus.banking.dtaus.Transaction;
import org.jdtaus.banking.dtaus.spi.CurrencyCounter;
import org.jdtaus.banking.dtaus.spi.Fields;
import org.jdtaus.banking.dtaus.spi.HeaderValidator;
import org.jdtaus.banking.dtaus.spi.IllegalHeaderException;
import org.jdtaus.banking.dtaus.spi.IllegalTransactionException;
import org.jdtaus.banking.dtaus.spi.TransactionValidator;
import org.jdtaus.banking.messages.ChecksumErrorMessage;
import org.jdtaus.banking.messages.ChecksumsFileMessage;
import org.jdtaus.banking.messages.IllegalDataMessage;
import org.jdtaus.banking.spi.CurrencyMapper;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.io.FileOperations;
import org.jdtaus.core.io.util.FlushableFileOperations;
import org.jdtaus.core.lang.spi.MemoryManager;
import org.jdtaus.core.logging.spi.Logger;
import org.jdtaus.core.messages.DeletesBlocksMessage;
import org.jdtaus.core.messages.InsertsBlocksMessage;
import org.jdtaus.core.monitor.spi.Task;
import org.jdtaus.core.monitor.spi.TaskMonitor;
import org.jdtaus.core.nio.util.Charsets;
import org.jdtaus.core.text.Message;
import org.jdtaus.core.text.spi.ApplicationLogger;

/**
 * Abstrakte Klasse für {@code LogicalFile}-Implementierungen.
 * <p>Stellt diverse Hilfs-Methoden sowie die Überprüfung von Vor- und Nachbedingungen zur Verfügung.</p>
 * <p><b>Hinweis:</b><br/>
 * Implementierung ist nicht vor gleichzeitigen Zugriffen unterschiedlicher Threads geschützt.</p>
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public abstract class AbstractLogicalFile implements LogicalFile
{

    public interface Listener extends EventListener
    {

        /**
         * Gets called whenever bytes were inserted into an instance the listener is registered with. The byte
         * previously at {@code position} will have moved to {@code position + insertedBytes}.
         *
         * @param position The position of the first inserted byte.
         * @param bytes The number of bytes which were inserted at {@code position}.
         *
         * @throws IOException if reading or writing fails.
         */
        void bytesInserted( long position, long bytes ) throws IOException;

        /**
         * Gets called whenever bytes were deleted an instance the listener is registered with. The byte previously at
         * {@code position + bytes} will have moved to {@code position}.
         *
         * @param position The position of the first deleted byte.
         * @param bytes The number of bytes which were deleted starting at {@code position} inclusive.
         *
         * @throws IOException if reading or writing fails.
         */
        void bytesDeleted( long position, long bytes ) throws IOException;

    }

    /** Konstante für ASCII-Zeichensatz. */
    protected static final int ENCODING_ASCII = 1;

    /** Konstante für EBCDI-Zeichensatz. */
    protected static final int ENCODING_EBCDI = 2;

    /** Return-Code. */
    protected static final long NO_NUMBER = Long.MIN_VALUE;

    /** Maximum allowed days between create and execution date. */
    protected static final int MAX_SCHEDULEDAYS = 15;

    /** Maximale Anzahl unterstützter Transaktionen pro logischer Datei. */
    private static final int MAX_TRANSACTIONS = 9999999;

    /** 01/01/1980 00:00:00 CET. */
    private static final long VALID_DATES_START_MILLIS = 315529200000L;

    /** 12/31/2079 23:59:59 CET. */
    private static final long VALID_DATES_END_MILLIS = 3471289199999L;

    /** Anzahl Ziffern der größten, abbildbaren Zahl des Formats. */
    private static final int FORMAT_MAX_DIGITS = 17;

    /** Anzahl Zeichen der größten, abbildbaren Zeichenkette des Formats. */
    private static final int FORMAT_MAX_CHARS = 105;

    /**
     * Index = Exponent,
     * Wert = 10er Potenz.
     */
    protected static final long[] EXP10 = new long[ FORMAT_MAX_DIGITS + 1 ];

    /**
     * Index = Ziffer,
     * Wert = ASCII-Zeichen.
     */
    private static final byte[] DIGITS_TO_ASCII =
    {
        48, 49, 50, 51, 52, 53, 54, 55, 56, 57
    };

    /**
     * Index = ASCII-Code einer Ziffer,
     * Wert = Ziffer.
     */
    private static final byte[] ASCII_TO_DIGITS = new byte[ 60 ];

    /**
     * Index = Ziffer,
     * Wert = EBCDI-Zeichen.
     */
    private static final byte[] DIGITS_TO_EBCDI =
    {
        (byte) 0xF0, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3, (byte) 0xF4, (byte) 0xF5, (byte) 0xF6, (byte) 0xF7,
        (byte) 0xF8, (byte) 0xF9
    };

    /**
     * Index = EBCDI-Code einer Ziffer,
     * Wert = Ziffer.
     */
    private static final byte[] EBCDI_TO_DIGITS = new byte[ 0xFA ];

    /** Charset name for the disk format. */
    private static final String DIN66003 = "ISO646-DE";

    /** Charset name for the tape format. */
    private static final String IBM273 = "IBM273";

    /** ASCII space character. */
    private static final byte ASCII_SPACE = (byte) 32;

    /** EBCDI space character. */
    private static final byte EBCDI_SPACE = (byte) 0x40;

    /** Verwendete {@code FileOperations} Implementierung. */
    private FileOperations fileOperations;

    /** Position des A-Datensatzes. */
    private long headerPosition;

    /** Position des E-Datensatzes. */
    private long checksumPosition;

    /**
     * Index = laufende Transaktionsnummer,
     * Wert = Position an der die Transaktion beginnt relativ zur Position des A-Datensatzes.
     */
    private long[] index;

    /** Zwischengespeicherter A Datensatz. */
    private Header cachedHeader = null;

    /** Zwischengespeicherter E Datensatz. */
    private Checksum cachedChecksum = null;

    /** Calendar der Instanz. */
    private final Calendar calendar = Calendar.getInstance( Locale.GERMANY );

    /** Puffer zum Lesen und Schreiben von Daten. */
    private final byte[] buffer = new byte[ FORMAT_MAX_CHARS + 1 ];

    /** Hilfs-Puffer. */
    private final StringBuffer shortDateBuffer = new StringBuffer( 6 );

    /** Hilfs-Puffer. */
    private final StringBuffer longDateBuffer = new StringBuffer( 8 );

    /** Abbildung von ISO Währungs-Codes zur Anzahl der vorhandenen Zahlungen mit der entsprechenden Währung. */
    private CurrencyCounter counter;

    /** Implementation configuration. */
    private Configuration configuration;

    /** Mininum number of bytes to copy to start any task monitoring. */
    private Integer monitoringThreshold;

    /** {@code Listener}s of the instance. */
    private final EventListenerList listeners = new EventListenerList();

    /** Pre-allocated temporary buffer. */
    private byte[] defaultBuffer;

    /** Statische Initialisierung der konstanten Felder. */
    static
    {
        for ( int i = 0; i <= FORMAT_MAX_DIGITS; i++ )
        {
            EXP10[i] = (long) Math.floor( Math.pow( 10.00D, i ) );
        }

        Arrays.fill( ASCII_TO_DIGITS, (byte) -1 );
        Arrays.fill( EBCDI_TO_DIGITS, (byte) -1 );
        ASCII_TO_DIGITS[48] = 0;
        ASCII_TO_DIGITS[49] = 1;
        ASCII_TO_DIGITS[50] = 2;
        ASCII_TO_DIGITS[51] = 3;
        ASCII_TO_DIGITS[52] = 4;
        ASCII_TO_DIGITS[53] = 5;
        ASCII_TO_DIGITS[54] = 6;
        ASCII_TO_DIGITS[55] = 7;
        ASCII_TO_DIGITS[56] = 8;
        ASCII_TO_DIGITS[57] = 9;
        EBCDI_TO_DIGITS[0xF0] = 0;
        EBCDI_TO_DIGITS[0xF1] = 1;
        EBCDI_TO_DIGITS[0xF2] = 2;
        EBCDI_TO_DIGITS[0xF3] = 3;
        EBCDI_TO_DIGITS[0xF4] = 4;
        EBCDI_TO_DIGITS[0xF5] = 5;
        EBCDI_TO_DIGITS[0xF6] = 6;
        EBCDI_TO_DIGITS[0xF7] = 7;
        EBCDI_TO_DIGITS[0xF8] = 8;
        EBCDI_TO_DIGITS[0xF9] = 9;
    }

    /**
     * Erzeugt eine neue {@code AbstractLogicalFile} Instanz.
     *
     * @see #setHeaderPosition(long)
     * @see #setChecksumPosition(long)
     * @see #setFileOperations(org.jdtaus.core.io.FileOperations)
     * @see #checksum()
     */
    protected AbstractLogicalFile()
    {
        this.calendar.setLenient( false );
        Arrays.fill( this.buffer, (byte) -1 );
    }

    /**
     * Gets the value of property {@code configuration}.
     *
     * @return Implementation configuration.
     */
    protected Configuration getConfiguration()
    {
        if ( this.configuration == null )
        {
            this.configuration = new Configuration();
        }

        return this.configuration;
    }

    /**
     * Sets the value of property {@code configuration}.
     *
     * @param configuration Implementation configuration.
     */
    protected void setConfiguration( final Configuration configuration )
    {
        this.configuration = configuration;
    }

    /**
     * Liest den Wert der Property {@code headerPosition}.
     *
     * @return Position des A Datensatzes.
     */
    protected long getHeaderPosition()
    {
        return this.headerPosition;
    }

    /**
     * Schreibt den Wert der Property {@code headerPosition}.
     *
     * @param headerPosition Position des A Datensatzes.
     *
     * @throws IllegalArgumentException wenn {@code headerPosition} negativ ist.
     * @throws IOException wenn die aktuelle Anzahl Bytes nicht ermittelt werden kann.
     */
    protected void setHeaderPosition( final long headerPosition ) throws IOException
    {
        if ( headerPosition < 0L )
        {
            throw new IllegalArgumentException( Long.toString( headerPosition ) );
        }

        this.headerPosition = headerPosition;
    }

    /**
     * Liest den Wert der Property {@code checksumPosition}.
     *
     * @return Position des E-Datensatzes.
     */
    protected long getChecksumPosition()
    {
        return this.checksumPosition;
    }

    /**
     * Schreibt den Wert der Property {@code checksumPosition}.
     *
     * @param checksumPosition Position des E-Datensatzes.
     *
     * @throws IllegalArgumentException wenn {@code checksumPosition} negativ ist.
     * @throws IOException wenn die aktuelle Anzahl Byte nicht ermittelt werden kann.
     */
    protected void setChecksumPosition( final long checksumPosition ) throws IOException
    {
        if ( checksumPosition <= this.getHeaderPosition() )
        {
            throw new IllegalArgumentException( Long.toString( checksumPosition ) );
        }

        this.checksumPosition = checksumPosition;
    }

    /**
     * Ermittelt die zugrunde liegende {@code FileOperations} Implementierung.
     *
     * @return zugrunde liegende {@code FileOperations} Implementierung.
     */
    protected FileOperations getFileOperations()
    {
        return this.fileOperations;
    }

    /**
     * Ändert die zu Grunde liegende {@code FileOperations} Implementierung.
     *
     * @param fileOperations neue {@code FileOperations} Implementierung.
     *
     * @throws NullPointerException wenn {@code fileOperations} {@code null} ist.
     * @throws IOException wenn zwischengespeicherte Änderungen der vorherigen Instanz nicht geschrieben werden können.
     */
    protected void setFileOperations( final FileOperations fileOperations ) throws IOException
    {
        if ( fileOperations == null )
        {
            throw new NullPointerException( "fileOperations" );
        }

        if ( this.fileOperations != null && this.fileOperations instanceof FlushableFileOperations )
        {
            ( (FlushableFileOperations) this.fileOperations ).flush();
        }

        this.fileOperations = fileOperations;
        this.cachedHeader = null;
        this.cachedChecksum = null;
        this.index = null;
        Arrays.fill( this.buffer, (byte) -1 );
    }

    /**
     * Gets the value of property {@code monitoringThreshold}.
     *
     * @return The mininum number of bytes to copy to start any task monitoring.
     */
    public int getMonitoringThreshold()
    {
        if ( this.monitoringThreshold == null )
        {
            this.monitoringThreshold = this.getDefaultMonitoringThreshold();
        }

        return this.monitoringThreshold.intValue();
    }

    /**
     * Sets the value of property {@code monitoringThreshold}.
     *
     * @param value The mininum number of bytes to copy to start any task monitoring.
     */
    public void setMonitoringThreshold( final int value )
    {
        this.monitoringThreshold = new Integer( value );
    }

    /**
     * Adds a {@code Listener} to the listener list.
     *
     * @param listener The listener to be added to the listener list.
     *
     * @throws NullPointerException if {@code listener} is {@code null}.
     */
    public void addListener( final Listener listener )
    {
        this.listeners.add( Listener.class, listener );
    }

    /**
     * Removes a {@code Listener} from the listener list.
     *
     * @param listener The listener to be removed from the listener list.
     *
     * @throws NullPointerException if {@code listener} is {@code null}.
     */
    public void removeFileOperationsListener( final Listener listener )
    {
        this.listeners.remove( Listener.class, listener );
    }

    /**
     * Gets all currently registered {@code Listener}s.
     *
     * @return all currently registered {@code Listener}s.
     */
    public Listener[] getListeners()
    {
        return (Listener[]) this.listeners.getListeners( Listener.class );
    }

    /**
     * Notifies all registered listeners about inserted bytes.
     *
     * @param position The position of the first inserted byte.
     * @param bytes The number of bytes which were inserted at {@code position}.
     *
     * @throws IOException if reading or writing fails.
     */
    protected void fireBytesInserted( final long position, final long bytes ) throws IOException
    {
        final Object[] list = this.listeners.getListenerList();
        for ( int i = list.length - 2; i >= 0; i -= 2 )
        {
            if ( list[i] == Listener.class )
            {
                ( (Listener) list[i + 1] ).bytesInserted( position, bytes );
            }
        }
    }

    /**
     * Notifies all registered listeners about deleted bytes.
     *
     * @param position The position of the first deleted byte.
     * @param bytes The number of bytes which were deleted starting at {@code position} inclusive.
     *
     * @throws IOException if reading or writing fails.
     */
    protected void fireBytesDeleted( final long position, final long bytes ) throws IOException
    {
        final Object[] list = this.listeners.getListenerList();
        for ( int i = list.length - 2; i >= 0; i -= 2 )
        {
            if ( list[i] == Listener.class )
            {
                ( (Listener) list[i + 1] ).bytesDeleted( position, bytes );
            }
        }
    }

    /**
     * Hilfs-Methode zum Lesen von Zahlen.
     * <p>Sollten ungültige Daten gelesen werden, so wird {@code NO_NUMBER} zurückgeliefert und eine entsprechende
     * {@code IllegalDataMessage} erzeugt.</p>
     *
     * @param field Feld-Konstante des zu lesenden Feldes.
     * @param position Position ab der Ziffern gelesen werden sollen.
     * @param len Anzahl von Ziffern, die gelesen werden sollen.
     * @param encoding zu verwendende Kodierung.
     *
     * @return gelesene Zahl oder {@code NO_NUMBER} wenn gelesene Daten nicht als Zahl interpretiert werden konnten.
     *
     * @throws CorruptedException wenn die Datei Fehler enthält und {@link ThreadLocalMessages#isErrorsEnabled()} gleich
     * {@code true} ist.
     * @throws IOException wenn nicht gelesen werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see #NO_NUMBER
     * @see org.jdtaus.banking.dtaus.spi.Fields
     * @see ThreadLocalMessages#isErrorsEnabled()
     */
    protected Long readNumber( final int field, final long position, final int len, final int encoding )
        throws IOException
    {
        return this.readNumber(
            field, position, len, encoding, this.getConfiguration().isSpaceCharacterAllowed( field ) );

    }

    /**
     * Hilfs-Methode zum Lesen von Zahlen mit gegebenenfalls Konvertierung von Leerzeichen zu Nullen.
     * <p>Die Verwendung dieser Methode mit {@code allowSpaces == true} entspricht einem Verstoß gegen die
     * Spezifikation. Diese Methode existiert ausschließlich um ungültige Dateien lesen zu können und sollte nur in
     * diesen Fällen verwendet werden.</p>
     * <p>Sollten ungültige Daten gelesen werden, so wird {@code NO_NUMBER} zurückgeliefert und eine entsprechende
     * {@code IllegalDataMessage} erzeugt.</p>
     *
     * @param field Feld-Konstante des zu lesenden Feldes.
     * @param position Position aber der die Ziffern gelesen werden sollen.
     * @param len Anzahl von Ziffern, die gelesen werden sollen.
     * @param encoding Zu verwendende Kodierung.
     * @param allowSpaces {@code true} wenn vorhandene Leerzeichen durch Nullen ersetzt werden sollen; {@code false}
     * für eine strikte Einhaltung der Spezifikation.
     *
     * @return gelesene Zahl oder {@code NO_NUMBER} wenn gelesene Daten nicht als Zahl interpretiert werden konnten.
     *
     * @throws CorruptedException wenn die Datei Fehler enthält und {@link ThreadLocalMessages#isErrorsEnabled()} gleich
     * {@code true} ist.
     * @throws IOException wenn nicht gelesen werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see #NO_NUMBER
     * @see org.jdtaus.banking.dtaus.spi.Fields
     * @see org.jdtaus.banking.dtaus.ri.zka.ThreadLocalMessages
     */
    protected Long readNumber( final int field, final long position, final int len, final int encoding,
                               final boolean allowSpaces ) throws IOException
    {
        long ret = 0L;
        final byte space;
        final byte[] table;
        final byte[] revTable;
        final String cset;
        String logViolation = null; // Wenn != null wird der Verstoß geloggt.

        if ( encoding == ENCODING_ASCII )
        {
            table = DIGITS_TO_ASCII;
            revTable = ASCII_TO_DIGITS;
            space = ASCII_SPACE;
            cset = DIN66003;
        }
        else if ( encoding == ENCODING_EBCDI )
        {
            table = DIGITS_TO_EBCDI;
            revTable = EBCDI_TO_DIGITS;
            space = EBCDI_SPACE;
            cset = IBM273;
        }
        else
        {
            throw new IllegalArgumentException( Integer.toString( encoding ) );
        }

        this.fileOperations.setFilePointer( position );
        this.fileOperations.read( this.buffer, 0, len );

        for ( int read = 0; read < len; read++ )
        {
            if ( allowSpaces && this.buffer[read] == space )
            {
                if ( logViolation == null )
                {
                    logViolation = Charsets.decode( this.buffer, 0, len, cset );
                }

                this.buffer[read] = table[0];
            }

            if ( !( this.buffer[read] >= table[0] && this.buffer[read] <= table[9] ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        field, IllegalDataMessage.TYPE_NUMERIC, position,
                        Charsets.decode( this.buffer, 0, len, cset ) );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }

                ret = NO_NUMBER;
                logViolation = null;
                break;
            }
            else
            {
                ret += revTable[this.buffer[read] & 0xFF] * EXP10[len - read - 1];
            }
        }

        if ( logViolation != null )
        {
            if ( this.getLogger().isInfoEnabled() )
            {
                this.getLogger().info( this.getReadNumberIllegalFileInfoMessage(
                    this.getLocale(), logViolation, new Long( ret ) ) );

            }
        }

        return new Long( ret );
    }

    /**
     * Hilfs-Methode zum Schreiben von Zahlen.
     *
     * @param field Feld-Konstante des zu beschreibenden Feldes.
     * @param position Position ab der die Daten geschrieben werden sollen.
     * @param len Anzahl an Ziffern die geschrieben werden sollen. Hierbei wird linksseitig mit Nullen aufgefüllt, so
     * dass exakt {@code len} Ziffern geschrieben werden.
     * @param number Die zu schreibende Zahl.
     * @param encoding Zu verwendende Kodierung.
     *
     * @throws IllegalArgumentException wenn {@code number} nicht mit {@code len} Ziffern darstellbar ist.
     * @throws IOException wenn nicht geschrieben werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.spi.Fields
     */
    protected void writeNumber( final int field, final long position, final int len, long number, final int encoding )
        throws IOException
    {
        int i;
        int pos;
        final long maxValue = EXP10[len] - 1L;
        int digit;
        final byte[] table;

        if ( number < 0L || number > maxValue )
        {
            throw new IllegalArgumentException( Long.toString( number ) );
        }

        if ( encoding == ENCODING_ASCII )
        {
            table = DIGITS_TO_ASCII;
        }
        else if ( encoding == ENCODING_EBCDI )
        {
            table = DIGITS_TO_EBCDI;
        }
        else
        {
            throw new IllegalArgumentException( Integer.toString( encoding ) );
        }

        for ( i = len - 1, pos = 0; i >= 0; i--, pos++ )
        {
            digit = (int) Math.floor( number / EXP10[i] );
            number -= ( digit * EXP10[i] );
            this.buffer[pos] = table[digit];
        }

        this.fileOperations.setFilePointer( position );
        this.fileOperations.write( this.buffer, 0, len );
    }

    /**
     * Hilds-Methode zum Lesen einer alpha-numerischen Zeichenkette.
     * <p>Sollten ungültige Daten gelesen werden, so wird {@code null} zurückgeliefert und eine entsprechende
     * {@code IllegalDataMessage} erzeugt.</p>
     *
     * @param field Feld-Konstante des zu lesenden Feldes.
     * @param position Position ab der die Zeichen gelesen werden sollen.
     * @param len Anzahl von Zeichen, die gelesen werden sollen.
     * @param encoding Zu verwendende Kodierung.
     *
     * @return gelesene Zeichenkette oder {@code null} wenn ungültige Zeichen gelesen werden.
     *
     * @throws CorruptedException wenn die Datei Fehler enthält und {@link ThreadLocalMessages#isErrorsEnabled()} gleich
     * {@code true} ist.
     * @throws IOException wenn nicht gelesen werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.spi.Fields
     * @see org.jdtaus.banking.dtaus.ri.zka.ThreadLocalMessages
     */
    protected AlphaNumericText27 readAlphaNumeric( final int field, final long position, final int len,
                                                   final int encoding ) throws IOException
    {
        final String cset;
        final String str;
        AlphaNumericText27 txt = null;

        if ( encoding == ENCODING_ASCII )
        {
            cset = DIN66003;
        }
        else if ( encoding == ENCODING_EBCDI )
        {
            cset = IBM273;
        }
        else
        {
            throw new IllegalArgumentException( Integer.toString( encoding ) );
        }

        this.fileOperations.setFilePointer( position );
        this.fileOperations.read( this.buffer, 0, len );
        str = Charsets.decode( this.buffer, 0, len, cset );

        try
        {
            txt = AlphaNumericText27.parse( str );
        }
        catch ( ParseException e )
        {
            if ( this.getLogger().isDebugEnabled() )
            {
                this.getLogger().debug( e.toString() );
            }

            txt = null;
            if ( ThreadLocalMessages.isErrorsEnabled() )
            {
                throw new CorruptedException( this.getImplementation(), position );
            }
            else
            {
                final Message msg =
                    new IllegalDataMessage( field, IllegalDataMessage.TYPE_ALPHA_NUMERIC, position, str );

                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        return txt;
    }

    /**
     * Hilfs-Methode zum Schreiben einer Zeichenkette.
     *
     * @param field Feld-Konstante des zu beschreibenden Feldes.
     * @param position Position ab der die Zeichen geschrieben werden sollen.
     * @param len Anzahl von Zeichen die maximal geschrieben werden sollen. Sollte {@code str} kürzer als {@code len}
     * sein, wird linksseitig mit Leerzeichen aufgefüllt.
     * @param str Die zu schreibende Zeichenkette.
     * @param encoding Zu verwendende Kodierung.
     *
     * @throws NullPointerException wenn {@code str null} ist.
     * @throws IllegalArgumentException wenn {@code str} länger als {@code len} Zeichen lang ist oder ungültige Zeichen
     * enthält.
     * @throws IOException wenn nicht geschrieben werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.spi.Fields
     */
    protected void writeAlphaNumeric( final int field, final long position, final int len, final String str,
                                      final int encoding ) throws IOException
    {
        final int length;
        final int delta;
        final char[] c;
        final byte[] buf;
        final byte space;
        final String cset;

        if ( str == null )
        {
            throw new NullPointerException( "str" );
        }
        if ( ( length = str.length() ) > len )
        {
            throw new IllegalArgumentException( str );
        }

        if ( encoding == ENCODING_ASCII )
        {
            space = ASCII_SPACE;
            cset = DIN66003;
        }
        else if ( encoding == ENCODING_EBCDI )
        {
            space = EBCDI_SPACE;
            cset = IBM273;
        }
        else
        {
            throw new IllegalArgumentException( Integer.toString( encoding ) );
        }

        c = str.toCharArray();
        for ( int i = c.length - 1; i >= 0; i-- )
        {
            if ( !AlphaNumericText27.checkAlphaNumeric( c[i] ) )
            {
                throw new IllegalArgumentException( Character.toString( c[i] ) );
            }
        }

        buf = Charsets.encode( str, cset );
        if ( length < len )
        {
            delta = len - length;
            System.arraycopy( buf, 0, this.buffer, 0, buf.length );
            Arrays.fill( this.buffer, buf.length, buf.length + delta, space );
        }
        else
        {
            System.arraycopy( buf, 0, this.buffer, 0, buf.length );
        }

        this.fileOperations.setFilePointer( position );
        this.fileOperations.write( this.buffer, 0, len );
    }

    /**
     * Hilfs-Methode zum Lesen einer Datums-Angabe mit zweistelliger Jahres-Zahl.
     * <p>Zweistellige Jahres-Angaben kleiner oder gleich 79 werden als {@code 2000 + zweistelliges Jahr} interpretiert.
     * Zweistellige Jahres-Angaben größer oder gleich 80 werden als {@code 1900 + zweistelliges Jahr} interpretiert.</p>
     * <p>Sollten ungültige Daten gelesen werden, so wird {@code null} zurückgeliefert und eine entsprechende
     * {@code IllegalDataMessage} erzeugt.</p>
     *
     * @param field Feld-Konstante des zu lesenden Feldes.
     * @param position Position ab der die Zeichen gelesen werden sollen.
     * @param encoding Zu verwendende Kodierung.
     *
     * @return das gelesene Datum oder {@code null} wenn kein Datum gelesen werden kann.
     *
     * @throws CorruptedException wenn die Datei Fehler enthält und {@link ThreadLocalMessages#isErrorsEnabled()} gleich
     * {@code true} ist.
     * @throws IOException wenn nicht gelesen werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.spi.Fields
     * @see org.jdtaus.banking.dtaus.ri.zka.ThreadLocalMessages
     */
    protected Date readShortDate( final int field, final long position, final int encoding ) throws IOException
    {
        final int len;
        final String cset;

        Date ret = null;
        String str = null;
        boolean legal = false;
        Message msg;

        if ( encoding == ENCODING_ASCII )
        {
            cset = DIN66003;
        }
        else if ( encoding == ENCODING_EBCDI )
        {
            cset = IBM273;
        }
        else
        {
            throw new IllegalArgumentException( Integer.toString( encoding ) );
        }

        try
        {
            this.fileOperations.setFilePointer( position );
            this.fileOperations.read( this.buffer, 0, 6 );
            str = Charsets.decode( this.buffer, 0, 6, cset );
            len = str.trim().length();

            if ( len == 6 )
            {
                this.calendar.clear();
                // Tag
                this.calendar.set( Calendar.DAY_OF_MONTH, Integer.valueOf( str.substring( 0, 2 ) ).intValue() );

                // Monat
                this.calendar.set( Calendar.MONTH, Integer.valueOf( str.substring( 2, 4 ) ).intValue() - 1 );

                // Jahr
                int year = Integer.valueOf( str.substring( 4, 6 ) ).intValue();
                year = year <= 79 ? 2000 + year : 1900 + year;

                this.calendar.set( Calendar.YEAR, year );
                ret = this.calendar.getTime();

                if ( !this.checkDate( ret ) )
                {
                    if ( ThreadLocalMessages.isErrorsEnabled() )
                    {
                        throw new CorruptedException( this.getImplementation(), position );
                    }
                    else
                    {
                        msg = new IllegalDataMessage( field, IllegalDataMessage.TYPE_SHORTDATE, position, str );
                        ThreadLocalMessages.getMessages().addMessage( msg );
                    }

                    ret = null;
                }
            }

            if ( len == 0 || len == 6 )
            {
                legal = true;
            }

        }
        catch ( NumberFormatException e )
        {
            if ( this.getLogger().isDebugEnabled() )
            {
                this.getLogger().debug( e.toString() );
            }

            ret = null;
            legal = false;
        }

        if ( !legal )
        {
            if ( ThreadLocalMessages.isErrorsEnabled() )
            {
                throw new CorruptedException( this.getImplementation(), position );
            }
            else
            {
                msg = new IllegalDataMessage( field, IllegalDataMessage.TYPE_SHORTDATE, position, str );
                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        return ret;
    }

    /**
     * Hilfs-Methode zum Schreiben einer Datums-Angabe mit zweistelliger Jahres-Zahl.
     * <p>Es werden nur Daten mit Jahren größer oder gleich 1980 und kleiner oder gleich 2079 akzeptiert.</p>
     *
     * @param field Feld-Konstante des zu beschreibenden Feldes.
     * @param position Position ab der die Zeichen geschrieben werden sollen.
     * @param date Die zu schreibende Datums-Angabe oder {@code null} um eine optionale Datums-Angabe zu entfernen.
     * @param encoding Zu verwendende Kodierung.
     *
     * @throws IllegalArgumentException wenn das Jahr von {@code date} nicht größer oder gleich 1980 und kleiner oder
     * gleich 2079 ist.
     * @throws IOException wenn nicht geschrieben werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.spi.Fields
     */
    protected void writeShortDate( final int field, final long position, final Date date, final int encoding )
        throws IOException
    {
        int i;
        final byte[] buf;
        final String cset;

        if ( encoding == ENCODING_ASCII )
        {
            cset = DIN66003;
        }
        else if ( encoding == ENCODING_EBCDI )
        {
            cset = IBM273;
        }
        else
        {
            throw new IllegalArgumentException( Integer.toString( encoding ) );
        }

        if ( date != null )
        {
            if ( !this.checkDate( date ) )
            {
                throw new IllegalArgumentException( date.toString() );
            }

            this.shortDateBuffer.setLength( 0 );
            this.calendar.clear();
            this.calendar.setTime( date );
            // Tag
            i = this.calendar.get( Calendar.DAY_OF_MONTH );
            if ( i < 10 )
            {
                this.shortDateBuffer.append( '0' );
            }
            this.shortDateBuffer.append( i );
            // Monat
            i = this.calendar.get( Calendar.MONTH ) + 1;
            if ( i < 10 )
            {
                this.shortDateBuffer.append( '0' );
            }
            this.shortDateBuffer.append( i );
            // Jahr
            i = this.calendar.get( Calendar.YEAR );
            this.shortDateBuffer.append( i >= 2000 && i <= 2009 ? "0" : "" );
            this.shortDateBuffer.append( i >= 1980 && i < 2000 ? i - 1900 : i - 2000 );

            buf = Charsets.encode( this.shortDateBuffer.toString(), cset );
        }
        else
        {
            buf = Charsets.encode( "      ", cset );
        }

        this.fileOperations.setFilePointer( position );
        this.fileOperations.write( buf, 0, 6 );
    }

    /**
     * Hilfs-Methode zum Lesen einer Datums-Angabe mit vierstelliger Jahres-Zahl.
     * <p>Sollten ungültige Daten gelesen werden, so wird {@code null} zurückgeliefert und eine entsprechende
     * {@code IllegalDataMessage} erzeugt.</p>
     *
     * @param field Feld-Konstante des zu lesenden Feldes.
     * @param position Position ab der die Zeichen gelesen werden sollen.
     * @param encoding Zu verwendende Kodierung.
     *
     * @return gelesenes Datum oder {@code null} wenn nicht gelesen werden kann.
     *
     * @throws IOException wenn nicht gelesen werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.spi.Fields
     * @see org.jdtaus.banking.dtaus.ri.zka.ThreadLocalMessages
     */
    protected Date readLongDate( final int field, final long position, final int encoding ) throws IOException
    {
        final int len;
        final String cset;

        boolean legal = false;
        Date ret = null;
        String str = null;
        Message msg;

        if ( encoding == ENCODING_ASCII )
        {
            cset = DIN66003;
        }
        else if ( encoding == ENCODING_EBCDI )
        {
            cset = IBM273;
        }
        else
        {
            throw new IllegalArgumentException( Integer.toString( encoding ) );
        }

        try
        {
            this.fileOperations.setFilePointer( position );
            this.fileOperations.read( this.buffer, 0, 8 );
            str = Charsets.decode( this.buffer, 0, 8, cset );
            len = str.trim().length();
            if ( len == 8 )
            {
                this.calendar.clear();
                // Tag
                this.calendar.set( Calendar.DAY_OF_MONTH, Integer.valueOf( str.substring( 0, 2 ) ).intValue() );

                // Monat
                this.calendar.set( Calendar.MONTH, Integer.valueOf( str.substring( 2, 4 ) ).intValue() - 1 );

                // Jahr
                this.calendar.set( Calendar.YEAR, Integer.valueOf( str.substring( 4, 8 ) ).intValue() );

                ret = this.calendar.getTime();
                if ( !this.checkDate( ret ) )
                {
                    if ( ThreadLocalMessages.isErrorsEnabled() )
                    {
                        throw new CorruptedException( this.getImplementation(), position );
                    }
                    else
                    {
                        msg = new IllegalDataMessage( field, IllegalDataMessage.TYPE_LONGDATE, position, str );
                        ThreadLocalMessages.getMessages().addMessage( msg );
                    }

                    ret = null;
                }

            }

            if ( len == 0 || len == 8 )
            {
                legal = true;
            }

        }
        catch ( NumberFormatException e )
        {
            if ( this.getLogger().isDebugEnabled() )
            {
                this.getLogger().debug( e.toString() );
            }

            legal = false;
            ret = null;
        }

        if ( !legal )
        {
            if ( ThreadLocalMessages.isErrorsEnabled() )
            {
                throw new CorruptedException( this.getImplementation(), position );
            }
            else
            {
                msg = new IllegalDataMessage( field, IllegalDataMessage.TYPE_LONGDATE, position, str );
                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        return ret;
    }

    /**
     * Hilfs-Methode zum Schreiben einer Datums-Angabe mit vierstelliger Jahres-Zahl.
     *
     * @param field Feld-Konstante des zu beschreibenden Feldes.
     * @param position Position ab der die Zeichen geschrieben werden sollen.
     * @param date Die zu schreibende Datums-Angabe oder {@code null} um eine optionale Datums-Angabe zu entfernen.
     * @param encoding Zu verwendende Kodierung.
     *
     * @throws IllegalArgumentException wenn das Jahr von {@code date} nicht größer oder gleich 1980 und kleiner oder
     * gleich 2079 ist.
     * @throws IOException wenn nicht geschrieben werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.spi.Fields
     * @see org.jdtaus.banking.dtaus.ri.zka.ThreadLocalMessages
     */
    protected void writeLongDate( final int field, final long position, final Date date, final int encoding )
        throws IOException
    {
        int i;
        final byte[] buf;
        final String cset;

        if ( encoding == ENCODING_ASCII )
        {
            cset = DIN66003;
        }
        else if ( encoding == ENCODING_EBCDI )
        {
            cset = IBM273;
        }
        else
        {
            throw new IllegalArgumentException( Integer.toString( encoding ) );
        }

        if ( date != null )
        {
            if ( !this.checkDate( date ) )
            {
                throw new IllegalArgumentException( date.toString() );
            }

            this.longDateBuffer.setLength( 0 );
            this.calendar.clear();
            this.calendar.setTime( date );
            // Tag
            i = this.calendar.get( Calendar.DAY_OF_MONTH );
            if ( i < 10 )
            {
                this.longDateBuffer.append( '0' );
            }
            this.longDateBuffer.append( i );
            // Monat
            i = this.calendar.get( Calendar.MONTH ) + 1;
            if ( i < 10 )
            {
                this.longDateBuffer.append( '0' );
            }
            this.longDateBuffer.append( i );
            // Jahr
            i = this.calendar.get( Calendar.YEAR );
            this.longDateBuffer.append( i );
            buf = Charsets.encode( this.longDateBuffer.toString(), cset );
        }
        else
        {
            buf = Charsets.encode( "        ", cset );
        }

        this.fileOperations.setFilePointer( position );
        this.fileOperations.write( buf, 0, 8 );
    }

    /**
     * Hilfs-Methode zum Lesen von gepackten EBCDI Zahlen.
     * <p>Sollten ungültige Daten gelesen werden, so wird {@code NO_NUMBER} zurückgeliefert und eine entsprechende
     * {@code IllegalDataMessage} erzeugt.</p>
     *
     * @param field Feld-Konstante des zu lesenden Feldes.
     * @param position Position ab der die Daten gelesen werden sollen.
     * @param len Anzahl von Byte, die gelesen werden sollen.
     * @param sign {@code true} wenn ein Vorzeichen erwartet wird; {@code false} wenn kein Vorzeichen erwartet wird.
     *
     * @return gelesene Zahl oder {@code NO_NUMBER} wenn gelesene Daten nicht als Zahl interpretiert werden konnten.
     *
     * @throws CorruptedException wenn die Datei Fehler enthält und {@link ThreadLocalMessages#isErrorsEnabled()} gleich
     * {@code true} ist.
     * @throws IOException wenn nicht gelesen werden kann.
     *
     * @see org.jdtaus.banking.dtaus.spi.Fields
     * @see ThreadLocalMessages#isErrorsEnabled()
     * @see #NO_NUMBER
     */
    protected long readNumberPackedPositive( final int field, final long position, final int len, final boolean sign )
        throws IOException
    {
        long ret = 0L;
        final int nibbles = 2 * len;
        int exp = nibbles - ( sign ? 2 : 1 );
        boolean highNibble = true;
        int read = 0;
        Message msg;

        this.fileOperations.setFilePointer( position );
        this.fileOperations.read( this.buffer, 0, len );

        for ( int nibble = 0; nibble < nibbles; nibble++, exp-- )
        {
            final int digit = highNibble ? ( ( this.buffer[read] & 0xF0 ) >> 4 ) : ( this.buffer[read++] & 0xF );

            highNibble = !highNibble;

            // Vorzeichen des letzten Nibbles.
            if ( sign && exp < 0 )
            {
                if ( digit != 0xC )
                {
                    if ( ThreadLocalMessages.isErrorsEnabled() )
                    {
                        throw new CorruptedException( this.getImplementation(), position );
                    }
                    else
                    {
                        msg = new IllegalDataMessage(
                            field, IllegalDataMessage.TYPE_PACKET_POSITIVE, position, Integer.toString( digit ) );

                        ThreadLocalMessages.getMessages().addMessage( msg );
                    }

                    ret = NO_NUMBER;
                    break;
                }
            }
            else
            {
                if ( digit < 0 || digit > 9 )
                {
                    if ( !ThreadLocalMessages.isErrorsEnabled() )
                    {
                        throw new CorruptedException( this.getImplementation(), position );
                    }
                    else
                    {
                        msg = new IllegalDataMessage(
                            field, IllegalDataMessage.TYPE_PACKET_POSITIVE, position, Integer.toString( digit ) );

                        ThreadLocalMessages.getMessages().addMessage( msg );
                    }

                    ret = NO_NUMBER;
                    break;
                }

                ret += ( digit * EXP10[exp] );
            }
        }

        return ret;
    }

    /**
     * Hilfs-Methode zum Schreiben von gepackten EBCDI-Zahlen.
     *
     * @param field Feld-Konstante des zu beschreibenden Feldes.
     * @param position Position ab der die Daten geschrieben werden sollen.
     * @param len Anzahl an Byte die geschrieben werden sollen. Hierbei wird linksseitig mit Nullen aufgefüllt, so dass
     * exakt {@code len} Ziffern geschrieben werden.
     * @param number Die zu schreibende Zahl.
     * @param sign {@code true} wenn ein Vorzeichen geschrieben werden soll; {@code false} wenn kein Vorzeichen
     * geschrieben werden soll.
     *
     * @throws IllegalArgumentException wenn {@code number} nicht mit {@code len} Byte darstellbar ist.
     * @throws IOException wenn nicht geschrieben werden kann.
     *
     * @see org.jdtaus.banking.dtaus.spi.Fields
     */
    protected void writeNumberPackedPositive( final int field, final long position, final int len, long number,
                                              final boolean sign ) throws IOException
    {
        int i;
        int pos = 0;
        final int nibbles = len * 2;
        final int digits = nibbles - ( sign ? 1 : 0 );
        int exp = digits - 1;
        final long maxValue = EXP10[digits] - 1L;
        byte b = 0;
        boolean highNibble = true;

        if ( number < 0L || number > maxValue )
        {
            throw new IllegalArgumentException( Long.toString( number ) );
        }

        for ( i = 0; i < nibbles; i++, exp-- )
        {
            final int digit;

            if ( sign && exp < 0 )
            {
                digit = 0xC;
            }
            else
            {
                digit = (int) Math.floor( number / EXP10[exp] );
                number -= ( digit * EXP10[exp] );
            }
            if ( highNibble )
            {
                b = (byte) ( ( digit << 4 ) & 0xF0 );
            }
            else
            {
                this.buffer[pos++] = (byte) ( b | digit );
            }

            highNibble = !highNibble;
        }

        this.fileOperations.setFilePointer( position );
        this.fileOperations.write( this.buffer, 0, len );
    }

    /**
     * Hilfs-Methode zum Lesen von binär gespeicherten Zahlen.
     *
     * @param field Feld-Konstante des zu lesenden Feldes.
     * @param position Position ab der die Daten gelesen werden sollen.
     * @param len Anzahl von Byte, die gelesen werden sollen.
     *
     * @return gelesene Zahl.
     *
     * @throws IllegalArgumentException wenn {@code len} negativ, {@code 0} oder größer als {@code 8} ist.
     * @throws IOException wenn nicht gelesen werden kann.
     *
     * @see org.jdtaus.banking.dtaus.spi.Fields
     */
    protected long readNumberBinary( final int field, final long position, final int len ) throws IOException
    {
        if ( len <= 0 || len > 8 )
        {
            throw new IllegalArgumentException( Integer.toString( len ) );
        }

        long ret = 0L;
        int shift = ( len - 1 ) * 8;

        this.fileOperations.setFilePointer( position );
        this.fileOperations.read( this.buffer, 0, len );

        for ( int i = 0; i < len; i++, shift -= 8 )
        {
            ret |= ( ( this.buffer[i] & 0xFF ) << shift );
        }

        return ret;
    }

    /**
     * Hilfs-Methode zum Schreiben von binär gespeicherten Zahlen.
     *
     * @param field Feld-Konstante des zu beschreibenden Feldes.
     * @param position Position ab der die Daten geschrieben werden sollen.
     * @param len Anzahl an Byte die geschrieben werden sollen.
     * @param number Die zu schreibende Zahl.
     *
     * @throws IllegalArgumentException wenn {@code len} negativ, {@code 0} oder größer als {@code 8} ist.
     * @throws IOException wenn nicht geschrieben werden kann.
     *
     * @see org.jdtaus.banking.dtaus.spi.Fields
     */
    protected void writeNumberBinary( final int field, final long position, final int len, final long number )
        throws IOException
    {
        if ( len <= 0 || len > 8 )
        {
            throw new IllegalArgumentException( Integer.toString( len ) );
        }

        int shift = ( len - 1 ) * 8;
        int i;

        for ( i = 0; i < len; i++, shift -= 8 )
        {
            this.buffer[i] = (byte) ( ( number >> shift ) & 0xFFL );
        }

        this.fileOperations.setFilePointer( position );
        this.fileOperations.write( this.buffer, 0, len );
    }

    /**
     * Prüfung einer laufenden Transaktionsnummer.
     *
     * @param id zu prüfende Transaktionsnummer.
     * @param checksum aktuelle Prüfsumme.
     *
     * @throws NullPointerException {@code if(checksum == null)}
     */
    protected boolean checkTransactionId( final int id, final Checksum checksum )
    {
        if ( checksum == null )
        {
            throw new NullPointerException( "checksum" );
        }

        final int count = checksum.getTransactionCount();
        return count > 0 && id >= 0 && id < count;
    }

    /**
     * Prüfung einer Menge von Transaktionen.
     *
     * @param transactionCount zu prüfende Menge Transaktionen.
     */
    protected boolean checkTransactionCount( final int transactionCount )
    {
        return transactionCount >= 0 && transactionCount <= MAX_TRANSACTIONS;
    }

    /**
     * Prüfung eines Datums.
     *
     * @param date zu prüfendes Datum.
     *
     * @return {@code true} wenn {@code date} im gültigen Bereich liegt; {@code false} wenn nicht,
     */
    protected boolean checkDate( final Date date )
    {
        boolean valid = false;

        if ( date != null )
        {
            final long millis = date.getTime();
            valid = millis >= VALID_DATES_START_MILLIS && millis <= VALID_DATES_END_MILLIS;
        }

        return valid;
    }

    /**
     * Hilfsmethode zum dynamischen Vergrössern des Index.
     *
     * @param index laufende Transaktionsnummer, für die der Index angepasst werden soll.
     * @param checksum aktuelle Prüfsumme zur Initialisierung des Index.
     */
    protected void resizeIndex( final int index, final Checksum checksum )
    {
        if ( this.index == null )
        {
            this.index = this.getMemoryManager().allocateLongs( checksum.getTransactionCount() + 1 );
            Arrays.fill( this.index, -1L );
        }

        while ( this.index.length < index + 1 )
        {
            int newLength = this.index.length * 2;
            if ( newLength <= index )
            {
                newLength = index + 1;
            }
            else if ( newLength > MAX_TRANSACTIONS )
            {
                newLength = MAX_TRANSACTIONS;
            }

            final long[] newIndex = this.getMemoryManager().allocateLongs( newLength );
            System.arraycopy( this.index, 0, newIndex, 0, this.index.length );
            Arrays.fill( newIndex, this.index.length, newIndex.length, -1L );
            this.index = newIndex;
        }
    }

    /**
     * Inserts a given number of bytes at a given position.
     *
     * @param position The position to insert bytes at.
     * @param bytes The number of bytes to insert.
     *
     * @throws IOException if inserting bytes fails.
     */
    protected void insertBytes( final long position, final long bytes ) throws IOException
    {
        final Task task = new Task();
        long toMoveByte = this.getFileOperations().getLength() - position;
        long progress = 0L;
        long progressDivisor = 1L;
        long maxProgress = toMoveByte;

        if ( toMoveByte <= 0L )
        {
            this.getFileOperations().setLength( this.getFileOperations().getLength() + bytes );
            this.fireBytesInserted( position, bytes );
            return;
        }

        final byte[] buf = this.getBuffer( toMoveByte > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) toMoveByte );
        while ( maxProgress > Integer.MAX_VALUE )
        {
            maxProgress /= 2L;
            progressDivisor *= 2L;
        }

        task.setIndeterminate( false );
        task.setCancelable( false );
        task.setMinimum( 0 );
        task.setMaximum( (int) maxProgress );
        task.setProgress( (int) progress );
        task.setDescription( new InsertsBlocksMessage() );

        final boolean monitoring = toMoveByte > this.getMonitoringThreshold();
        if ( monitoring )
        {
            this.getTaskMonitor().monitor( task );
        }

        try
        {
            long readPos = this.getFileOperations().getLength();
            while ( toMoveByte > 0L )
            {
                final int moveLen = buf.length >= toMoveByte ? (int) toMoveByte : buf.length;
                readPos -= moveLen;
                final long writePos = readPos + bytes;

                this.getFileOperations().setFilePointer( readPos );
                int read = 0;
                int total = 0;

                do
                {
                    read = this.getFileOperations().read( buf, total, moveLen - total );
                    assert read != FileOperations.EOF : "Unexpected end of file.";
                    total += read;
                }
                while ( total < moveLen );

                this.getFileOperations().setFilePointer( writePos );
                this.getFileOperations().write( buf, 0, moveLen );

                toMoveByte -= moveLen;
                progress += moveLen;
                task.setProgress( (int) ( progress / progressDivisor ) );
            }
        }
        finally
        {
            if ( monitoring )
            {
                this.getTaskMonitor().finish( task );
            }
        }

        this.fireBytesInserted( position, bytes );
    }

    /**
     * Removes a given number of bytes at a given position.
     *
     * @param position The position to remove bytes at.
     * @param bytes The number of bytes to remove.
     *
     * @throws IOException if removing bytes fails.
     */
    protected void removeBytes( final long position, final long bytes ) throws IOException
    {
        final Task task = new Task();
        long toMoveByte = this.getFileOperations().getLength() - position - bytes;
        long progress = 0L;
        long progressDivisor = 1L;
        long maxProgress = toMoveByte;

        // No blocks are following the ones to remove.
        if ( toMoveByte == 0L )
        {
            this.getFileOperations().setLength( this.getFileOperations().getLength() - bytes );
            this.fireBytesDeleted( position, bytes );
            return;
        }

        final byte[] buf = this.getBuffer( toMoveByte > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) toMoveByte );
        while ( maxProgress > Integer.MAX_VALUE )
        {
            maxProgress /= 2L;
            progressDivisor *= 2L;
        }

        task.setIndeterminate( false );
        task.setCancelable( false );
        task.setMinimum( 0 );
        task.setMaximum( (int) maxProgress );
        task.setProgress( (int) progress );
        task.setDescription( new DeletesBlocksMessage() );

        final boolean monitoring = toMoveByte > this.getMonitoringThreshold();
        if ( monitoring )
        {
            this.getTaskMonitor().monitor( task );
        }

        try
        {
            long readPos = position + bytes;
            while ( toMoveByte > 0L )
            {
                final int len = toMoveByte <= buf.length ? (int) toMoveByte : buf.length;
                final long writePos = readPos - bytes;

                this.getFileOperations().setFilePointer( readPos );

                int read = 0;
                int total = 0;
                do
                {
                    read = this.getFileOperations().read( buf, total, len - total );
                    assert read != FileOperations.EOF : "Unexpected end of file.";
                    total += read;

                }
                while ( total < len );

                // Move the block count blocks to the beginning.
                this.getFileOperations().setFilePointer( writePos );
                this.getFileOperations().write( buf, 0, len );

                toMoveByte -= len;
                readPos += len;
                progress += len;
                task.setProgress( (int) ( progress / progressDivisor ) );
            }

            this.getFileOperations().setLength( this.getFileOperations().getLength() - bytes );
        }
        finally
        {
            if ( monitoring )
            {
                this.getTaskMonitor().finish( task );
            }
        }

        this.fireBytesDeleted( position, bytes );
    }

    private byte[] getBuffer( final int requested ) throws IOException
    {
        final long length = this.getFileOperations().getLength();

        if ( requested <= 0 || requested > length )
        {
            throw new IllegalArgumentException( Integer.toString( requested ) );
        }

        if ( this.defaultBuffer == null )
        {
            this.defaultBuffer = this.getMemoryManager().allocateBytes( this.getDefaultBufferSize() );
        }

        return requested <= this.defaultBuffer.length || this.getMemoryManager().getAvailableBytes() < requested
               ? this.defaultBuffer : this.getMemoryManager().allocateBytes( requested );

    }

    /**
     * Ermittelt die Größe eines Satzabschnitts.
     *
     * @return Größe eines Satzabschnitts in Byte.
     */
    protected abstract int getBlockSize();

    /**
     * Ermittelt den Typ eines Satzabschnitts.
     *
     * @param position Position des zu lesenden Satzabschnitts.
     *
     * @return Datensatztyp des an {@code position} beginnenden Satzabschnitts {@code position}.
     *
     * @throws IOException wenn nicht gelesen werden kann.
     */
    protected abstract char getBlockType( long position ) throws IOException;

    /**
     * Ermittlung der Bytes einer Transaktion.
     *
     * @param transaction Transaktion, für die die Anzahl benötigter Bytes ermittelt werden soll.
     *s
     * @return Anzahl der von {@code transaction} belegten Bytes.
     */
    protected abstract int byteCount( Transaction transaction );

    /**
     * Gets implementation meta-data.
     *
     * @return implementation meta-data.
     */
    protected abstract Implementation getImplementation();

    /**
     * Liest den A Datensatz. Die entsprechenden Vor- und Nachbedingungen werden in
     * {@link AbstractLogicalFile#getHeader()} geprüft.
     *
     * @return A Datensatz.
     *
     * @throws IOException wenn nicht gelesen werden kann.
     *
     * @see #getHeaderPosition()
     */
    protected abstract Header readHeader() throws IOException;

    /**
     * Schreibt den A Datensatz. Die entsprechenden Vor- und Nachbedingungen werden in
     * {@link AbstractLogicalFile#setHeader(Header)} geprüft.
     *
     * @param header A Datensatz.
     *
     * @throws IOException wenn nicht geschrieben werden kann.
     *
     * @see #getHeaderPosition()
     */
    protected abstract void writeHeader( Header header ) throws IOException;

    /**
     * Liest den E Datensatz. Die entsprechenden Vor- und Nachbedingungen werden in
     * {@link AbstractLogicalFile#getChecksum()} geprüft.
     *
     * @return E Datensatz.
     *
     * @throws IOException wenn nicht gelesen werden kann.
     *
     * @see #getChecksumPosition()
     */
    protected abstract Checksum readChecksum() throws IOException;

    /**
     * Schreibt den E Datensatz. Die entsprechenden Vor- und Nachbedingungen werden in
     * {@link AbstractLogicalFile#setChecksum(Checksum)} geprüft.
     *
     * @param checksum E Datensatz.
     *
     * @throws IOException wenn nicht geschrieben werden kann.
     *
     * @see #getChecksumPosition()
     */
    protected abstract void writeChecksum( Checksum checksum ) throws IOException;

    /**
     * Liest einen C Datensatz. Die entsprechenden Vor- und Nachbedingungen werden in
     * {@link AbstractLogicalFile#getTransaction(int)} geprüft.
     *
     * @param position Position des C Datensatzes.
     * @param transaction Instanz, die die gelesenen Daten aufnehmen soll.
     *
     * @return an {@code position} beginnender C Datensatz.
     *
     * @throws IOException wenn nicht gelesen werden kann.
     */
    protected abstract Transaction readTransaction( long position, Transaction transaction ) throws IOException;

    /**
     * Schreibt einen C Datensatz. Die entsprechenden Vor- und Nachbedingungen werden in
     * {@link AbstractLogicalFile#setTransaction(int, Transaction)} geprüft.
     *
     * @param position Position des C Datensatzes.
     * @param transaction Daten des C Datensatzes.
     *
     * @throws IOException wenn nicht geschrieben werden kann.
     */
    protected abstract void writeTransaction( long position, Transaction transaction ) throws IOException;

    public Header getHeader() throws IOException
    {
        if ( this.cachedHeader == null )
        {
            this.cachedHeader = this.readHeader();
        }

        return (Header) this.cachedHeader.clone();
    }

    public Header setHeader( final Header header ) throws IOException
    {
        IllegalHeaderException result = null;
        final Header old = this.getHeader();
        final HeaderValidator[] validators = this.getHeaderValidator();

        for ( int i = validators.length - 1; i >= 0; i-- )
        {
            result = validators[i].assertValidHeader( this, header, this.counter, result );
        }

        if ( result != null && result.getMessages().length > 0 )
        {
            throw result;
        }

        this.writeHeader( header );
        this.cachedHeader = (Header) header.clone();
        return old;
    }

    public Checksum getChecksum() throws IOException
    {
        if ( this.cachedChecksum == null )
        {
            this.cachedChecksum = this.readChecksum();
        }

        return (Checksum) this.cachedChecksum.clone();
    }

    protected void setChecksum( final Checksum checksum ) throws IOException
    {
        this.writeChecksum( checksum );
        this.cachedChecksum = (Checksum) checksum.clone();
    }

    protected void checksum() throws IOException
    {
        final Checksum c = new Checksum();
        Transaction t = new Transaction();
        final Task task = new Task();
        task.setIndeterminate( true );
        task.setCancelable( false );
        task.setDescription( new ChecksumsFileMessage() );

        try
        {
            this.getTaskMonitor().monitor( task );

            final long fileLength = this.fileOperations.getLength();
            long position = this.getHeaderPosition();
            char type = this.getBlockType( position );
            this.setChecksumPosition( position + this.getBlockSize() );
            this.counter = new CurrencyCounter();

            if ( type == 'A' )
            {
                this.getHeader(); // A-Datensatz prüfen.

                position += this.getBlockSize();
                int transactionIndex = 0;

                while ( position < fileLength && ( type = this.getBlockType( position ) ) == 'C' )
                {
                    this.resizeIndex( transactionIndex, c );
                    this.index[transactionIndex] = position - this.getHeaderPosition();
                    t = this.readTransaction( this.getHeaderPosition() + this.index[transactionIndex++], t );
                    final int len = this.byteCount( t );

                    if ( t.getCurrency() != null )
                    {
                        this.counter.add( t.getCurrency() );
                    }
                    if ( t.getAmount() != null && t.getTargetAccount() != null && t.getTargetBank() != null )
                    {
                        c.add( t );
                    }

                    position += len;
                    this.setChecksumPosition( position );
                    c.setTransactionCount( transactionIndex );
                }

                this.setChecksumPosition( position );
                if ( type == 'E' )
                {
                    final Checksum stored = this.getChecksum();
                    if ( !stored.equals( c ) )
                    {
                        if ( ThreadLocalMessages.isErrorsEnabled() )
                        {
                            throw new CorruptedException( this.getImplementation(), position );
                        }
                        else
                        {
                            final Message msg = new ChecksumErrorMessage( stored, c, this.getHeaderPosition() );
                            ThreadLocalMessages.getMessages().addMessage( msg );
                        }
                    }
                }
                else
                {
                    if ( ThreadLocalMessages.isErrorsEnabled() )
                    {
                        throw new CorruptedException(
                            this.getImplementation(), position + DTAUSDisk.ERECORD_OFFSETS[1] );

                    }
                    else
                    {
                        final Message msg = new IllegalDataMessage(
                            Fields.FIELD_E2, IllegalDataMessage.TYPE_CONSTANT, position + DTAUSDisk.ERECORD_OFFSETS[1],
                            Character.toString( type ) );

                        ThreadLocalMessages.getMessages().addMessage( msg );
                    }
                }
            }
            else
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position + DTAUSDisk.ARECORD_OFFSETS[1] );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_A2, IllegalDataMessage.TYPE_CONSTANT, position + DTAUSDisk.ARECORD_OFFSETS[1],
                        Character.toString( type ) );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
        }
        finally
        {
            this.getTaskMonitor().finish( task );
        }
    }

    public final void createTransaction( final Transaction transaction ) throws IOException
    {
        this.addTransaction( transaction );
    }

    public int addTransaction( final Transaction transaction ) throws IOException
    {
        final Checksum checksum = this.getChecksum();
        final int newCount = checksum.getTransactionCount() + 1;

        if ( !this.checkTransactionCount( newCount ) )
        {
            throw new ArrayIndexOutOfBoundsException( newCount );
        }

        IllegalTransactionException result = null;
        final TransactionValidator[] validators = this.getTransactionValidator();

        for ( int i = validators.length - 1; i >= 0; i-- )
        {
            result = validators[i].assertValidTransaction( this, transaction, result );
        }

        if ( result != null && result.getMessages().length > 0 )
        {
            throw result;
        }

        this.counter.add( transaction.getCurrency() );
        checksum.setTransactionCount( newCount );
        checksum.add( transaction );

        final int transactionIndex = checksum.getTransactionCount() - 1;
        final int len = this.byteCount( transaction );
        this.insertBytes( this.getChecksumPosition(), len );
        this.setChecksumPosition( this.getChecksumPosition() + len );
        this.resizeIndex( transactionIndex, checksum );
        this.index[transactionIndex] = this.getChecksumPosition() - len - this.getHeaderPosition();
        this.writeTransaction( this.getHeaderPosition() + this.index[transactionIndex], transaction );
        this.writeChecksum( checksum );
        this.cachedChecksum = checksum;
        return transactionIndex;
    }

    public Transaction getTransaction( final int index ) throws IOException
    {
        final Checksum checksum = this.getChecksum();
        if ( !this.checkTransactionId( index, checksum ) )
        {
            throw new ArrayIndexOutOfBoundsException( index );
        }

        return this.readTransaction( this.index[index] + this.getHeaderPosition(), new Transaction() );
    }

    public Transaction setTransaction( final int index, final Transaction transaction ) throws IOException
    {
        final Checksum checksum = this.getChecksum();
        if ( !this.checkTransactionId( index, checksum ) )
        {
            throw new ArrayIndexOutOfBoundsException( index );
        }

        IllegalTransactionException result = null;
        final TransactionValidator[] validators = this.getTransactionValidator();

        for ( int i = validators.length - 1; i >= 0; i-- )
        {
            result = validators[i].assertValidTransaction( this, transaction, result );
        }

        if ( result != null && result.getMessages().length > 0 )
        {
            throw result;
        }

        final Transaction old = this.getTransaction( index );

        if ( !old.getCurrency().getCurrencyCode().equals( transaction.getCurrency().getCurrencyCode() ) )
        {
            this.counter.substract( old.getCurrency() );
            this.counter.add( transaction.getCurrency() );
        }

        int i;
        checksum.subtract( old );
        checksum.add( transaction );
        final int oldLen = this.byteCount( old );
        final int newLen = this.byteCount( transaction );
        if ( oldLen < newLen )
        {
            final int delta = newLen - oldLen;
            this.insertBytes( this.getHeaderPosition() + this.index[index], delta );

            for ( i = index + 1; i < this.index.length; i++ )
            {
                if ( this.index[i] != -1L )
                {
                    this.index[i] += delta;
                }
            }

            this.setChecksumPosition( this.getChecksumPosition() + delta );
        }
        else if ( oldLen > newLen )
        {
            final int delta = oldLen - newLen;
            this.removeBytes( this.getHeaderPosition() + this.index[index], delta );

            for ( i = index + 1; i < this.index.length; i++ )
            {
                if ( this.index[i] != -1L )
                {
                    this.index[i] -= delta;
                }
            }

            this.setChecksumPosition( this.getChecksumPosition() - delta );
        }

        this.writeTransaction( this.getHeaderPosition() + this.index[index], transaction );
        this.writeChecksum( checksum );
        this.cachedChecksum = checksum;
        return old;
    }

    public Transaction removeTransaction( final int index ) throws IOException
    {
        final Checksum checksum = this.getChecksum();
        if ( !this.checkTransactionId( index, checksum ) )
        {
            throw new ArrayIndexOutOfBoundsException( index );
        }

        final Transaction removed = this.getTransaction( index );
        checksum.setTransactionCount( checksum.getTransactionCount() - 1 );
        checksum.subtract( removed );
        this.counter.substract( removed.getCurrency() );

        final int len = this.byteCount( removed );
        this.removeBytes( this.getHeaderPosition() + this.index[index], len );
        this.setChecksumPosition( this.getChecksumPosition() - len );
        for ( int i = index + 1; i < this.index.length; i++ )
        {
            if ( this.index[i] != -1L )
            {
                this.index[i] -= len;
            }

            this.index[i - 1] = this.index[i];
        }

        this.writeChecksum( checksum );
        this.cachedChecksum = checksum;
        return removed;
    }

    //--Dependencies------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the configured <code>Logger</code> implementation.
     *
     * @return The configured <code>Logger</code> implementation.
     */
    protected Logger getLogger()
    {
        return (Logger) ContainerFactory.getContainer().
            getDependency( this, "Logger" );

    }

    /**
     * Gets the configured <code>MemoryManager</code> implementation.
     *
     * @return The configured <code>MemoryManager</code> implementation.
     */
    protected MemoryManager getMemoryManager()
    {
        return (MemoryManager) ContainerFactory.getContainer().
            getDependency( this, "MemoryManager" );

    }

    /**
     * Gets the configured <code>ApplicationLogger</code> implementation.
     *
     * @return The configured <code>ApplicationLogger</code> implementation.
     */
    protected ApplicationLogger getApplicationLogger()
    {
        return (ApplicationLogger) ContainerFactory.getContainer().
            getDependency( this, "ApplicationLogger" );

    }

    /**
     * Gets the configured <code>TaskMonitor</code> implementation.
     *
     * @return The configured <code>TaskMonitor</code> implementation.
     */
    protected TaskMonitor getTaskMonitor()
    {
        return (TaskMonitor) ContainerFactory.getContainer().
            getDependency( this, "TaskMonitor" );

    }

    /**
     * Gets the configured <code>TextschluesselVerzeichnis</code> implementation.
     *
     * @return The configured <code>TextschluesselVerzeichnis</code> implementation.
     */
    protected TextschluesselVerzeichnis getTextschluesselVerzeichnis()
    {
        return (TextschluesselVerzeichnis) ContainerFactory.getContainer().
            getDependency( this, "TextschluesselVerzeichnis" );

    }

    /**
     * Gets the configured <code>CurrencyMapper</code> implementation.
     *
     * @return The configured <code>CurrencyMapper</code> implementation.
     */
    protected CurrencyMapper getCurrencyMapper()
    {
        return (CurrencyMapper) ContainerFactory.getContainer().
            getDependency( this, "CurrencyMapper" );

    }

    /**
     * Gets the configured <code>HeaderValidator</code> implementation.
     *
     * @return The configured <code>HeaderValidator</code> implementation.
     */
    protected HeaderValidator[] getHeaderValidator()
    {
        return (HeaderValidator[]) ContainerFactory.getContainer().
            getDependency( this, "HeaderValidator" );

    }

    /**
     * Gets the configured <code>TransactionValidator</code> implementation.
     *
     * @return The configured <code>TransactionValidator</code> implementation.
     */
    protected TransactionValidator[] getTransactionValidator()
    {
        return (TransactionValidator[]) ContainerFactory.getContainer().
            getDependency( this, "TransactionValidator" );

    }

    /**
     * Gets the configured <code>Locale</code> implementation.
     *
     * @return The configured <code>Locale</code> implementation.
     */
    protected Locale getLocale()
    {
        return (Locale) ContainerFactory.getContainer().
            getDependency( this, "Locale" );

    }

// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
    //--Properties--------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausProperties
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the value of property <code>defaultMonitoringThreshold</code>.
     *
     * @return Number of bytes which need to minimally be copied to enable any task monitoring during copy operations.
     */
    protected java.lang.Integer getDefaultMonitoringThreshold()
    {
        return (java.lang.Integer) ContainerFactory.getContainer().
            getProperty( this, "defaultMonitoringThreshold" );

    }

    /**
     * Gets the value of property <code>defaultBufferSize</code>.
     *
     * @return Size of the pre-alocated default buffer in byte.
     */
    protected int getDefaultBufferSize()
    {
        return ( (java.lang.Integer) ContainerFactory.getContainer().
            getProperty( this, "defaultBufferSize" ) ).intValue();

    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>readNumberIllegalFileInfo</code>.
     * <blockquote><pre>Ein ungültiges Leerzeichen in einem numerischen Feld wurde zu einer Null konvertiert. Gelesene Zeichenkette "{0}" wurde zur Zahl "{1,number}" konvertiert.</pre></blockquote>
     * <blockquote><pre>An illegal space character in a numeric field has been converted to zero. Converted string "{0}" to number "{1,number}".</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param readString format argument.
     * @param convertedNumber format argument.
     *
     * @return the text of message <code>readNumberIllegalFileInfo</code>.
     */
    protected String getReadNumberIllegalFileInfoMessage( final Locale locale,
            final java.lang.String readString,
            final java.lang.Number convertedNumber )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "readNumberIllegalFileInfo", locale,
                new Object[]
                {
                    readString,
                    convertedNumber
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

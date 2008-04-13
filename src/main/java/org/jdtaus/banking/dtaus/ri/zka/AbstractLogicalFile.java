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
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.container.Specification;
import org.jdtaus.core.io.util.StructuredFileOperations;
import org.jdtaus.core.lang.spi.MemoryManager;
import org.jdtaus.core.logging.spi.Logger;
import org.jdtaus.core.monitor.spi.Task;
import org.jdtaus.core.monitor.spi.TaskMonitor;
import org.jdtaus.core.nio.util.Charsets;
import org.jdtaus.core.text.Message;
import org.jdtaus.core.text.spi.ApplicationLogger;

/**
 * Abstrakte Klasse für {@code LogicalFile}-Implementierungen.
 * <p>Stellt diverse Hilfs-Methoden sowie die Überprüfung von Vor- und
 * Nachbedingungen zur Verfügung.</p>
 * <p><b>Hinweis:</b><br/>
 * Implementierung darf niemals von mehreren Threads gleichzeitig verwendet
 * werden.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public abstract class AbstractLogicalFile implements LogicalFile
{
    //--Konstanten--------------------------------------------------------------

    // Vom Format vorgegebene Längen.
    /** Maximale Anzahl unterstützter Transaktionen pro logischer Datei. */
    protected static final int MAX_TRANSACTIONS = 9999999;

    /** Anzahl Ziffern der größten, abbildbaren Zahl des Formats. */
    protected static final int FORMAT_MAX_DIGITS = 17;

    /** Anzahl Zeichen der größten, abbildbaren Zeichenkette des Formats. */
    protected static final int FORMAT_MAX_CHARS = 105;

    /** Konstante für ASCII-Zeichensatz. */
    protected static final int ENCODING_ASCII = 1;

    /** Konstante für EBCDI-Zeichensatz. */
    protected static final int ENCODING_EBCDI = 2;

    /**
     * Index = Exponent,
     * Wert = 10er Potenz.
     */
    protected static final long[] EXP10 =
        new long[ AbstractLogicalFile.FORMAT_MAX_DIGITS + 1 ];

    /**
     * Index = Ziffer,
     * Wert = ASCII-Zeichen.
     */
    protected static final byte[] DIGITS_TO_ASCII = {
        48, 49, 50, 51, 52, 53, 54, 55, 56, 57
    };

    /**
     * Index = ASCII-Code einer Ziffer,
     * Wert = Ziffer.
     */
    protected static final byte[] ASCII_TO_DIGITS = new byte[ 60 ];

    /**
     * Index = Ziffer,
     * Wert = EBCDI-Zeichen.
     */
    protected static final byte[] DIGITS_TO_EBCDI = {
        ( byte ) 0xF0, ( byte ) 0xF1, ( byte ) 0xF2, ( byte ) 0xF3,
        ( byte ) 0xF4, ( byte ) 0xF5, ( byte ) 0xF6, ( byte ) 0xF7,
        ( byte ) 0xF8, ( byte ) 0xF9
    };

    /**
     * Index = EBCDI-Code einer Ziffer,
     * Wert = Ziffer.
     */
    protected static final byte[] EBCDI_TO_DIGITS = new byte[ 0xFA ];

    /** Konstante 10^7. */
    protected static final double EXP10_7 = Math.pow( 10, 7 );

    /** Maximale Anzahl unterstützter Verwendungszweckzeilen. */
    protected static final int MAX_DESCRIPTIONS = 14;

    /** Charset name for the disk format. */
    protected static final String DIN66003 = "ISO646-DE";

    /** Charset name for the tap format. */
    protected static final String IBM273 = "IBM273";

    /** Return-Code. */
    protected static final long NO_NUMBER = Long.MIN_VALUE;

    //--------------------------------------------------------------Konstanten--
    //--Attribute---------------------------------------------------------------

    /** Verwendete {@code StructuredFile} Implementierung. */
    protected StructuredFileOperations persistence;

    /** Datums-Format mit zweistelliger Jahresangabe. */
    protected final DateFormat shortDateFormat =
        new SimpleDateFormat( "ddMMyy", Locale.GERMANY );

    /** Datums-Format mit vierstelliger Jahresangabe. */
    protected final DateFormat longDateFormat =
        new SimpleDateFormat( "ddMMyyyy", Locale.GERMANY );

    /** Puffer zum Lesen und Schreiben von Daten. */
    protected final byte[] buffer =
        new byte[ AbstractLogicalFile.FORMAT_MAX_CHARS + 1 ];

    /** Satzabschnitt-Offset des A-Datensatzes. */
    private long headerBlock;

    /** Satzabschnitt-Offset des E-Datensatzes. */
    private long checksumBlock;

    /**
     * Index = laufende Transaktionsnummer,
     * Wert = Offset des Satzabschnittes an der die Transaktion beginnt.
     */
    protected long[] index;

    /** Zwischengespeicherter A Datensatz. */
    protected Header cachedHeader = null;

    /** Zwischengespeicherter E Datensatz. */
    protected Checksum cachedChecksum = null;

    /** Calendar der Instanz. */
    protected final Calendar calendar = Calendar.getInstance( Locale.GERMANY );

    /**
     * Abbildung von ISO Währungs-Codes zur Anzahl der vorhandenen Zahlungen
     * mit der entsprechenden Währung.
     */
    protected CurrencyCounter counter = new CurrencyCounter();

    //---------------------------------------------------------------Attribute--
    //--Konstruktoren-----------------------------------------------------------

    /** Statische Initialisierung der konstanten Felder. */
    static
    {
        for ( int i = 0; i <= AbstractLogicalFile.FORMAT_MAX_DIGITS; i++ )
        {
            AbstractLogicalFile.EXP10[i] =
                ( long ) Math.floor( Math.pow( 10.00D, i ) );

        }

        Arrays.fill( AbstractLogicalFile.ASCII_TO_DIGITS, ( byte ) -1 );
        Arrays.fill( AbstractLogicalFile.EBCDI_TO_DIGITS, ( byte ) -1 );
        AbstractLogicalFile.ASCII_TO_DIGITS[48] = 0;
        AbstractLogicalFile.ASCII_TO_DIGITS[49] = 1;
        AbstractLogicalFile.ASCII_TO_DIGITS[50] = 2;
        AbstractLogicalFile.ASCII_TO_DIGITS[51] = 3;
        AbstractLogicalFile.ASCII_TO_DIGITS[52] = 4;
        AbstractLogicalFile.ASCII_TO_DIGITS[53] = 5;
        AbstractLogicalFile.ASCII_TO_DIGITS[54] = 6;
        AbstractLogicalFile.ASCII_TO_DIGITS[55] = 7;
        AbstractLogicalFile.ASCII_TO_DIGITS[56] = 8;
        AbstractLogicalFile.ASCII_TO_DIGITS[57] = 9;
        AbstractLogicalFile.EBCDI_TO_DIGITS[0xF0] = 0;
        AbstractLogicalFile.EBCDI_TO_DIGITS[0xF1] = 1;
        AbstractLogicalFile.EBCDI_TO_DIGITS[0xF2] = 2;
        AbstractLogicalFile.EBCDI_TO_DIGITS[0xF3] = 3;
        AbstractLogicalFile.EBCDI_TO_DIGITS[0xF4] = 4;
        AbstractLogicalFile.EBCDI_TO_DIGITS[0xF5] = 5;
        AbstractLogicalFile.EBCDI_TO_DIGITS[0xF6] = 6;
        AbstractLogicalFile.EBCDI_TO_DIGITS[0xF7] = 7;
        AbstractLogicalFile.EBCDI_TO_DIGITS[0xF8] = 8;
        AbstractLogicalFile.EBCDI_TO_DIGITS[0xF9] = 9;
    }

    /** Erzeugt eine neue {@code AbstractLogicalFile} Instanz. */
    protected AbstractLogicalFile()
    {
        this.calendar.setLenient( false );
        Arrays.fill( this.buffer, ( byte ) -1 );
    }

    /**
     * Ändert die zu Grunde liegende {@code StructuredFile} Implementierung.
     *
     * @param structuredFile neue {@code StructuredFile} Implementierung.
     *
     * @throws NullPointerException wenn {@code structuredFile} {@code null}
     * ist.
     * @throws IOException wenn zwischengespeicherte Änderungen der vorherigen
     * Instanz nicht geschrieben werden können.
     */
    protected void setStructuredFile(
        final StructuredFileOperations structuredFile ) throws IOException
    {
        if ( structuredFile == null )
        {
            throw new NullPointerException( "structuredFile" );
        }

        if ( this.persistence != null )
        {
            this.persistence.flush();
        }

        this.persistence = structuredFile;
        this.cachedHeader = null;
        this.cachedChecksum = null;
        this.index = null;
        Arrays.fill( this.buffer, ( byte ) -1 );
    }

    //-----------------------------------------------------------Konstruktoren--
    //--Dependencies------------------------------------------------------------

    protected abstract MemoryManager getMemoryManagerImpl();

    protected abstract Logger getLoggerImpl();

    protected abstract ApplicationLogger getApplicationLoggerImpl();

    protected abstract TaskMonitor getTaskMonitorImpl();

    protected abstract TextschluesselVerzeichnis getTextschluesselVerzeichnisImpl();

    protected abstract Implementation getMeta();

    protected abstract CurrencyMapper getCurrencyMapperImpl();

    //------------------------------------------------------------Dependencies--
    //--long readNumber(...)----------------------------------------------------

    /**
     * Hilfs-Methode zum Lesen von Zahlen.
     * <p>Sollten ungültige Daten gelesen werden, so wird {@code NO_NUMBER}
     * zurückgeliefert und eine entsprechende {@code IllegalDataMessage}
     * erzeugt.</p>
     *
     * @param field Feld-Konstante des zu lesenden Feldes.
     * @param block Satzabschnitt, aus dem die Daten gelesen werden sollen.
     * @param off Position in {@code block} ab dem die Ziffern gelesen werden
     * sollen.
     * @param len Anzahl von Ziffern, die gelesen werden sollen.
     * @param encoding zu verwendendes Encoding.
     *
     * @return gelesene Zahl oder {@code NO_NUMBER} wenn gelesene Daten nicht
     * als Zahl interpretiert werden konnten.
     *
     * @throws PhysicalFileError wenn die Datei Fehler enthält und
     * {@link org.jdtaus.banking.dtaus.spi.AbstractErrorMessage#isErrorsEnabled()}
     * gleich {@code true} ist.
     * @throws IOException wenn nicht gelesen werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.spi.Fields
     * @see org.jdtaus.banking.dtaus.ri.zka.AbstractErrorMessage
     * @see org.jdtaus.banking.dtaus.ri.zka.ThreadLocalMessages
     * @see #NO_NUMBER
     */
    protected Long readNumber( final int field, final long block,
                                final int off, final int len,
                                final int encoding ) throws IOException
    {
        return this.readNumber( field, block, off, len, encoding,
                                this.getConfiguration().
                                isSpaceCharacterAllowed( field ) );

    }

    /**
     * Hilfs-Methode zum Lesen von Zahlen mit gegebenenfalls Konvertierung von
     * Leerzeichen zu Nullen.
     * <p>Die Verwendung dieser Methode mit {@code allowSpaces == true}
     * entspricht einem Verstoß gegen die Spezifikation. Diese Methode existiert
     * ausschließlich um ungültige Dateien lesen zu können und sollte nur in
     * diesen bestimmten Fällen verwendet werden.</p>
     * <p>Sollten ungültige Daten gelesen werden, so wird {@code NO_NUMBER}
     * zurückgeliefert und eine entsprechende {@code IllegalDataMessage}
     * erzeugt.</p>
     *
     * @param field Feld-Konstante des zu lesenden Feldes.
     * @param block Satzabschnitt, aus dem die Daten gelesen werden sollen.
     * @param off Position in {@code block} ab dem die Ziffern gelesen werden
     * sollen.
     * @param len Anzahl von Ziffern, die gelesen werden sollen.
     * @param encoding Zu verwendendes Encoding.
     * @param allowSpaces {@code true} wenn vorhandene Leerzeichen durch
     * Nullen ersetzt werden sollen; {@code false} für eine strikte Einhaltung
     * der Spezifikation.
     *
     * @return gelesene Zahl oder {@code NO_NUMBER} wenn gelesene Daten nicht
     * als Zahl interpretiert werden konnten.
     *
     * @throws PhysicalFileError wenn die Datei Fehler enthält und
     * {@link org.jdtaus.banking.dtaus.spi.AbstractErrorMessage#isErrorsEnabled()}
     * gleich {@code true} ist.
     * @throws IOException wenn nicht gelesen werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.spi.Fields
     * @see org.jdtaus.banking.dtaus.ri.zka.AbstractErrorMessage
     * @see org.jdtaus.banking.dtaus.ri.zka.ThreadLocalMessages
     * @see #NO_NUMBER
     */
    protected Long readNumber( final int field, final long block,
                                final int off, final int len,
                                final int encoding, final boolean allowSpaces )
        throws IOException
    {
        long ret = 0L;
        int read;
        final byte space;
        final byte[] table;
        final byte[] revTable;
        final MessageFormat fmt;
        final String cset;
        String logViolation = null; // Wenn != null wird der Verstoß geloggt.

        if ( encoding == AbstractLogicalFile.ENCODING_ASCII )
        {
            table = AbstractLogicalFile.DIGITS_TO_ASCII;
            revTable = AbstractLogicalFile.ASCII_TO_DIGITS;
            space = ( byte ) 32;
            cset = AbstractLogicalFile.DIN66003;
        }
        else if ( encoding == AbstractLogicalFile.ENCODING_EBCDI )
        {
            table = AbstractLogicalFile.DIGITS_TO_EBCDI;
            revTable = AbstractLogicalFile.EBCDI_TO_DIGITS;
            space = ( byte ) 0x40;
            cset = AbstractLogicalFile.IBM273;
        }
        else
        {
            throw new IllegalArgumentException( Integer.toString( encoding ) );
        }

        this.persistence.readBlock( block, off, this.buffer, 0, len );
        for ( read = 0; read < len; read++ )
        {
            if ( allowSpaces && this.buffer[read] == space )
            {
                if ( logViolation == null )
                {
                    logViolation = Charsets.decode( this.buffer, 0, len, cset );
                }

                this.buffer[read] = table[0];
            }

            if ( !( this.buffer[read] >= table[0] &&
                this.buffer[read] <= table[9] ) )
            {
                final Message msg = new IllegalDataMessage(
                    field, IllegalDataMessage.TYPE_NUMERIC, block *
                    this.persistence.getBlockSize() + off,
                    Charsets.decode( this.buffer, 0, len, cset ) );

                if ( AbstractErrorMessage.isErrorsEnabled() )
                {
                    throw new CorruptedException(
                        this.getMeta(),
                        block * this.persistence.getBlockSize() + off );

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage( msg );
                }

                ret = AbstractLogicalFile.NO_NUMBER;
                break;
            }
            else
            {
                if ( this.buffer[read] < 0 )
                {
                    ret += revTable[this.buffer[read] + 256] *
                        AbstractLogicalFile.EXP10[len - read - 1];

                }
                else
                {
                    ret += revTable[this.buffer[read]] *
                        AbstractLogicalFile.EXP10[len - read - 1];

                }
            }
        }

        if ( logViolation != null )
        {
            if ( this.getLoggerImpl().isInfoEnabled() )
            {
                fmt = AbstractLogicalFileBundle.getInstance().
                    getReadNumberIllegalFileInfoMessage( Locale.getDefault() );

                this.getLoggerImpl().info(
                    fmt.format( new Object[] {
                                logViolation,
                                new Long( ret )
                            } ) );

            }
        }

        return new Long( ret );
    }

    //----------------------------------------------------long readNumber(...)--
    //--void writeNumber(...)---------------------------------------------------

    /**
     * Hilfs-Methode zum Schreiben von Zahlen.
     *
     * @param field Feld-Konstante des zu beschreibenden Feldes.
     * @param block Block, in den die Daten geschrieben werden sollen.
     * @param off Position in {@code block} ab der die Ziffern geschrieben
     * werden sollen.
     * @param len Anzahl an Ziffern die geschrieben werden sollen. Hierbei wird
     * linksseitig mit Nullen aufgefüllt, so dass exakt {@code len} Ziffern
     * geschrieben werden.
     * @param number Die zu schreibende Zahl.
     * @param encoding Zu verwendendes Encoding.
     *
     * @throws IllegalArgumentException wenn {@code number} nicht mit
     * {@code len} Ziffern darstellbar ist.
     * @throws IOException wenn nicht geschrieben werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.Fields
     */
    protected void writeNumber( final int field, final long block,
                                 final int off, final int len, long number,
                                 final int encoding ) throws IOException
    {
        int i;
        int pos;
        final long maxValue = AbstractLogicalFile.EXP10[len] - 1L;
        byte digit;
        final byte[] table;

        if ( number < 0L || number > maxValue )
        {
            throw new IllegalArgumentException( Long.toString( number ) );
        }

        if ( encoding == AbstractLogicalFile.ENCODING_ASCII )
        {
            table = AbstractLogicalFile.DIGITS_TO_ASCII;
        }
        else if ( encoding == AbstractLogicalFile.ENCODING_EBCDI )
        {
            table = AbstractLogicalFile.DIGITS_TO_EBCDI;
        }
        else
        {
            throw new IllegalArgumentException( Integer.toString( encoding ) );
        }

        for ( i = len - 1, pos = 0; i >= 0; i--, pos++ )
        {
            digit = ( byte ) Math.floor( number / AbstractLogicalFile.EXP10[i] );
            number -= ( digit * AbstractLogicalFile.EXP10[i] );
            this.buffer[pos] = table[digit];
        }

        this.persistence.writeBlock( block, off, this.buffer, 0, len );
    }

    //---------------------------------------------------void writeNumber(...)--
    //--String readAlphaNumeric(...)--------------------------------------------

    /**
     * Hilds-Methode zum Lesen einer alpha-numerischen Zeichenkette.
     * <p>Sollten ungültige Daten gelesen werden, so wird {@code null}
     * zurückgeliefert und eine entsprechende {@code IllegalDataMessage}
     * erzeugt.</p>
     *
     * @param field Feld-Konstante des zu lesenden Feldes.
     * @param block Satzabschnitt, aus dem die Zeichen gelesen werden sollen.
     * @param off Position in {@code block} ab einschließlich der die Zeichen
     * gelesen werden sollen.
     * @param len Anzahl von Zeichen, die gelesen werden sollen.
     * @param encoding Konstante für das zu verwendende Encoding.
     *
     * @return gelesene Zeichenkette oder {@code null} wenn ungültige Zeichen
     * gelesen werden.
     *
     * @throws PhysicalFileError wenn die Datei Fehler enthält und
     * {@link org.jdtaus.banking.dtaus.spi.AbstractErrorMessage#isErrorsEnabled()}
     * gleich {@code true} ist.
     * @throws IOException wenn nicht gelesen werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.Fields
     * @see org.jdtaus.banking.dtaus.spi.AbstractErrorMessage
     * @see org.jdtaus.banking.dtaus.spi.ThreadLocalMessages
     */
    protected AlphaNumericText27 readAlphaNumeric(
        final int field, final long block, final int off, final int len,
        final int encoding ) throws IOException
    {
        final String cset;
        final String str;
        AlphaNumericText27 txt = null;

        if ( encoding == AbstractLogicalFile.ENCODING_ASCII )
        {
            cset = AbstractLogicalFile.DIN66003;
        }
        else if ( encoding == AbstractLogicalFile.ENCODING_EBCDI )
        {
            cset = AbstractLogicalFile.IBM273;
        }
        else
        {
            throw new IllegalArgumentException( Integer.toString( encoding ) );
        }

        this.persistence.readBlock( block, off, this.buffer, 0, len );
        str = Charsets.decode( this.buffer, 0, len, cset );

        try
        {
            txt = AlphaNumericText27.parse( str );
        }
        catch ( ParseException e )
        {
            txt = null;
            final Message msg =
                new IllegalDataMessage(
                field, IllegalDataMessage.TYPE_ALPHA_NUMERIC, block *
                this.persistence.getBlockSize() + off, str );

            if ( AbstractErrorMessage.isErrorsEnabled() )
            {
                throw new CorruptedException(
                    this.getMeta(), block * this.persistence.getBlockSize() +
                    off );

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        return txt;
    }

    //--------------------------------------------String readAlphaNumeric(...)--
    //--void writeAlphaNumeric(...)---------------------------------------------

    /**
     * Hilfs-Methode zum Schreiben einer Zeichenkette.
     *
     * @param field Feld-Konstante des zu beschreibenden Feldes.
     * @param block Satzabschnitt, in den die Zeichen geschrieben werden sollen.
     * @param off Position in {@code block} ab einschließlich der {@code str}
     * geschrieben werden soll.
     * @param len Anzahl von Zeichen die maximal geschrieben werden sollen.
     * Sollte {@code str} kürzer als {@code len} sein, wird linksseitig mit
     * Leerzeichen aufgefüllt.
     * @param str Die zu schreibende Zeichenkette.
     * @param encoding Konstante für das zu verwendende Encoding.
     *
     * @throws NullPointerException wenn {@code str null} ist.
     * @throws IllegalArgumentException wenn {@code str} länger als {@code len}
     * Zeichen lang ist oder ungültige Zeichen enthält.
     * @throws IOException wenn nicht geschrieben werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.Fields
     */
    protected void writeAlphaNumeric( final int field, final long block,
                                       final int off, final int len,
                                       final String str, final int encoding )
        throws IOException
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

        if ( encoding == AbstractLogicalFile.ENCODING_ASCII )
        {
            space = ( byte ) 32;
            cset = AbstractLogicalFile.DIN66003;
        }
        else if ( encoding == AbstractLogicalFile.ENCODING_EBCDI )
        {
            space = ( byte ) 0x40;
            cset = AbstractLogicalFile.IBM273;
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

        this.persistence.writeBlock( block, off, this.buffer, 0, len );
    }

    //---------------------------------------------void writeAlphaNumeric(...)--
    //--Date readShortDate(...)-------------------------------------------------

    /**
     * Hilfs-Methode zum Lesen einer Datums-Angabe mit zweistelliger
     * Jahres-Zahl.
     * <p>Zweistellige Jahres-Angaben kleiner oder gleich 79 werden als
     * {@code 2000 + zweistelliges Jahr} interpretiert. Zweistellige
     * Jahres-Angaben größer oder gleich 80 werden als
     * {@code 1900 + zweistelliges Jahr} interpretiert.</p>
     * <p>Sollten ungültige Daten gelesen werden, so wird {@code null}
     * zurückgeliefert und eine entsprechende {@code IllegalDataMessage}
     * erzeugt.</p>
     *
     * @param field Feld-Konstante des zu lesenden Feldes.
     * @param block Satzabschnitt, aus dem die Zeichen gelesen werden sollen.
     * @param off Position in {@code block} ab einschließlich der die Zeichen
     * gelesen werden sollen.
     * @param encoding Konstante für das zu verwendende Encoding.
     *
     * @return das gelesene Datum oder {@code null} wenn kein Datum gelesen
     * werden kann.
     *
     * @throws PhysicalFileError wenn die Datei Fehler enthält und
     * {@link org.jdtaus.banking.dtaus.spi.AbstractErrorMessage#isErrorsEnabled()}
     * gleich {@code true} ist.
     * @throws IOException wenn nicht gelesen werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.Fields
     * @see org.jdtaus.banking.dtaus.spi.AbstractErrorMessage
     * @see org.jdtaus.banking.dtaus.spi.ThreadLocalMessages
     */
    protected Date readShortDate( final int field, final long block,
                                   final int off, final int encoding )
        throws IOException
    {
        final int len;
        final String cset;

        Date ret = null;
        String str = null;
        boolean legal = false;
        Message msg;

        if ( encoding == AbstractLogicalFile.ENCODING_ASCII )
        {
            cset = AbstractLogicalFile.DIN66003;
        }
        else if ( encoding == AbstractLogicalFile.ENCODING_EBCDI )
        {
            cset = AbstractLogicalFile.IBM273;
        }
        else
        {
            throw new IllegalArgumentException( Integer.toString( encoding ) );
        }

        try
        {
            this.persistence.readBlock( block, off, this.buffer, 0, 6 );
            str = Charsets.decode( this.buffer, 0, 6, cset );
            len = str.trim().length();
            if ( len == 6 )
            {
                this.calendar.clear();
                // Tag
                this.calendar.set( Calendar.DAY_OF_MONTH, Integer.valueOf(
                                   str.substring( 0, 2 ) ).intValue() );

                // Monat
                this.calendar.set( Calendar.MONTH,
                                   Integer.valueOf( str.substring( 2, 4 ) ).
                                   intValue() - 1 );

                // Jahr
                int year = Integer.valueOf( str.substring( 4, 6 ) ).intValue();
                year = year <= 79
                    ? 2000 + year
                    : 1900 + year;

                this.calendar.set( Calendar.YEAR, year );
                ret = this.calendar.getTime();

                if ( !this.checkDate( ret ) )
                {
                    msg = new IllegalDataMessage(
                        field, IllegalDataMessage.TYPE_SHORTDATE, block *
                        this.persistence.getBlockSize() + off, str );

                    if ( AbstractErrorMessage.isErrorsEnabled() )
                    {
                        throw new CorruptedException(
                            this.getMeta(), block *
                            this.persistence.getBlockSize() + off );

                    }
                    else
                    {
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
            ret = null;
            legal = false;
        }

        if ( !legal )
        {
            msg = new IllegalDataMessage(
                field, IllegalDataMessage.TYPE_SHORTDATE, block *
                this.persistence.getBlockSize() + off, str );

            if ( AbstractErrorMessage.isErrorsEnabled() )
            {
                throw new CorruptedException(
                    this.getMeta(), block * this.persistence.getBlockSize() +
                    off );

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        return ret;
    }

    //-------------------------------------------------Date readShortDate(...)--
    //--void writeShortDate(...)------------------------------------------------

    /** Hilfs-Puffer. */
    private final StringBuffer shortDateBuffer = new StringBuffer( 6 );

    /**
     * Hilfs-Methode zum Schreiben einer Datums-Angabe mit zweistelliger
     * Jahres-Zahl.
     * <p>Es werden nur Daten mit Jahren größer oder gleich 1980 und kleiner
     * oder gleich 2079 akzeptiert.</p>
     *
     * @param field Feld-Konstante des zu beschreibenden Feldes.
     * @param block Satzabschnitt, in den die Zeichen geschrieben werden sollen.
     * @param off Position in {@code block} ab einschließlich der {@code str}
     * geschrieben werden soll.
     * @param date Die zu schreibende Datums-Angabe oder {@code null} um
     * eine optionale Datums-Angabe zu entfernen.
     * @param encoding Konstante für das zu verwendende Encoding.
     *
     * @throws IllegalArgumentException wenn das Jahr von {@code date} nicht
     * größer oder gleich 1980 und kleiner oder gleich 2079 ist.
     * @throws IOException wenn nicht geschrieben werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.Fields
     */
    protected void writeShortDate( final int field, final long block,
                                    final int off, final Date date,
                                    final int encoding ) throws IOException
    {
        int i;
        final byte[] buf;
        final String cset;

        if ( encoding == AbstractLogicalFile.ENCODING_ASCII )
        {
            cset = AbstractLogicalFile.DIN66003;
        }
        else if ( encoding == AbstractLogicalFile.ENCODING_EBCDI )
        {
            cset = AbstractLogicalFile.IBM273;
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
            this.shortDateBuffer.append( i >= 2000 && i <= 2009
                                         ? "0"
                                         : "" );

            this.shortDateBuffer.append( i >= 1980 && i < 2000
                                         ? i - 1900
                                         : i - 2000 );

            buf = Charsets.encode( this.shortDateBuffer.toString(), cset );
        }
        else
        {
            buf = Charsets.encode( "      ", cset );
        }

        this.persistence.writeBlock( block, off, buf, 0, 6 );
    }

    //------------------------------------------------void writeShortDate(...)--
    //--Date readLongDate(...)--------------------------------------------------

    /**
     * Hilfs-Methode zum Lesen einer Datums-Angabe mit vierstelliger
     * Jahres-Zahl.
     * <p>Sollten ungültige Daten gelesen werden, so wird {@code null}
     * zurückgeliefert und eine entsprechende {@code IllegalDataMessage}
     * erzeugt.</p>
     *
     * @param field Feld-Konstante des zu lesenden Feldes.
     * @param block Satzabschnitt, aus dem die Zeichen gelesen werden sollen.
     * @param off Position in {@code block} ab einschließlich der die Zeichen
     * gelesen werden sollen.
     * @param encoding Konstante für das zu verwendende Encoding.
     *
     * @return gelesenes Datum oder {@code null} wenn nicht gelesen werden kann.
     *
     * @throws IOException wenn nicht gelesen werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.Fields
     * @see org.jdtaus.banking.dtaus.spi.AbstractErrorMessage
     * @see org.jdtaus.banking.dtaus.spi.ThreadLocalMessages
     */
    protected Date readLongDate( final int field, final long block,
                                  final int off, final int encoding )
        throws IOException
    {
        final int len;
        final String cset;

        boolean legal = false;
        Date ret = null;
        String str = null;
        Message msg;

        if ( encoding == AbstractLogicalFile.ENCODING_ASCII )
        {
            cset = AbstractLogicalFile.DIN66003;
        }
        else if ( encoding == AbstractLogicalFile.ENCODING_EBCDI )
        {
            cset = AbstractLogicalFile.IBM273;
        }
        else
        {
            throw new IllegalArgumentException( Integer.toString( encoding ) );
        }

        try
        {
            this.persistence.readBlock( block, off, this.buffer, 0, 8 );
            str = Charsets.decode( this.buffer, 0, 8, cset );
            len = str.trim().length();
            if ( len == 8 )
            {
                this.calendar.clear();
                // Tag
                this.calendar.set( Calendar.DAY_OF_MONTH, Integer.valueOf(
                                   str.substring( 0, 2 ) ).intValue() );

                // Monat
                this.calendar.set( Calendar.MONTH,
                                   Integer.valueOf( str.substring( 2, 4 ) ).
                                   intValue() - 1 );

                // Jahr
                this.calendar.set( Calendar.YEAR, Integer.valueOf(
                                   str.substring( 4, 8 ) ).intValue() );

                ret = this.calendar.getTime();
                if ( !this.checkDate( ret ) )
                {
                    msg = new IllegalDataMessage(
                        field, IllegalDataMessage.TYPE_LONGDATE, block *
                        this.persistence.getBlockSize() + off, str );

                    if ( AbstractErrorMessage.isErrorsEnabled() )
                    {
                        throw new CorruptedException(
                            this.getMeta(), block *
                            this.persistence.getBlockSize() + off );

                    }
                    else
                    {
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
            legal = false;
            ret = null;
        }

        if ( !legal )
        {
            msg = new IllegalDataMessage(
                field, IllegalDataMessage.TYPE_LONGDATE, block *
                this.persistence.getBlockSize() + off, str );

            if ( AbstractErrorMessage.isErrorsEnabled() )
            {
                throw new CorruptedException(
                    this.getMeta(), block * this.persistence.getBlockSize() +
                    off );

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        return ret;
    }

    //-------------------------------------------------Date readShortDate(...)--
    //--void writeShortDate(...)------------------------------------------------

    /** Hilfs-Puffer. */
    private final StringBuffer longDateBuffer = new StringBuffer( 8 );

    /**
     * Hilfs-Methode zum Schreiben einer Datums-Angabe mit vierstelliger
     * Jahres-Zahl.
     *
     * @param field Feld-Konstante des zu beschreibenden Feldes.
     * @param block Satzabschnitt, in den die Zeichen geschrieben werden sollen.
     * @param off Position in {@code block} ab einschließlich der {@code str}
     * geschrieben werden soll.
     * @param date Die zu schreibende Datums-Angabe oder {@code null} um
     * eine optionale Datums-Angabe zu entfernen.
     * @param encoding Konstante für das zu verwendende Encoding.
     *
     * @throws IllegalArgumentException wenn das Jahr von {@code date} nicht
     * größer oder gleich 1980 und kleiner oder gleich 2079 ist.
     * @throws IOException wenn nicht geschrieben werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.Fields
     */
    protected void writeLongDate( final int field, final long block,
                                   final int off, final Date date,
                                   final int encoding ) throws IOException
    {
        int i;
        final byte[] buf;
        final String cset;

        if ( encoding == AbstractLogicalFile.ENCODING_ASCII )
        {
            cset = AbstractLogicalFile.DIN66003;
        }
        else if ( encoding == AbstractLogicalFile.ENCODING_EBCDI )
        {
            cset = AbstractLogicalFile.IBM273;
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

        this.persistence.writeBlock( block, off, buf, 0, 8 );
    }

    //-------------------------------------------------void writeLongDate(...)--
    //--long readNumberPackedPositive(...)--------------------------------------

    /**
     * Hilfs-Methode zum Lesen von gepackten EBCDI Zahlen.
     * <p>Sollten ungültige Daten gelesen werden, so wird {@code -1}
     * zurückgeliefert und eine entsprechende {@code IllegalDataMessage}
     * erzeugt.</p>
     *
     * @param field Feld-Konstante des zu lesenden Feldes.
     * @param block Satzabschnitt, aus dem die Daten gelesen werden sollen.
     * @param off Position in {@code block} ab dem die Ziffern gelesen werden
     * sollen.
     * @param len Anzahl von Byte, die gelesen werden sollen.
     * @param sign {@code true} wenn ein Vorzeichen erwartet wird;
     * {@code false} wenn kein Vorzeichen erwartet wird.
     *
     * @return gelesene Zahl oder {@code NO_NUMBER} wenn gelesene Daten nicht
     * als Zahl interpretiert werden konnten.
     *
     * @throws PhysicalFileError wenn die Datei Fehler enthält und
     * {@link org.jdtaus.banking.dtaus.spi.AbstractErrorMessage#isErrorsEnabled()}
     * gleich {@code true} ist.
     * @throws IOException wenn nicht gelesen werden kann.
     *
     * @see org.jdtaus.banking.dtaus.spi.Fields
     * @see org.jdtaus.banking.dtaus.ri.zka.AbstractErrorMessage
     * @see org.jdtaus.banking.dtaus.ri.zka.ThreadLocalMessages
     * @see #NO_NUMBER
     */
    protected long readNumberPackedPositive(
        final int field, final long block, final int off, final int len,
        final boolean sign ) throws IOException
    {
        long ret = 0L;
        final int nibbles = 2 * len;
        int exp = nibbles - ( sign
            ? 2
            : 1 );
        boolean highNibble = true;
        int nibble = 0;
        int read = 0;
        int digit;
        Message msg;

        this.persistence.readBlock( block, off, this.buffer, 0, len );
        for (; nibble < nibbles; nibble++, exp-- )
        {
            if ( highNibble )
            {
                if ( this.buffer[read] < 0 )
                {
                    digit = ( this.buffer[read] + 256 ) >> 4;
                }
                else
                {
                    digit = this.buffer[read] >> 4;
                }

                highNibble = false;
            }
            else
            {
                digit = ( this.buffer[read++] & 0xF );
                highNibble = true;
            }

            // Vorzeichen des letzten Nibbles.
            if ( sign && exp < 0 )
            {
                if ( digit != 0xC )
                {
                    msg = new IllegalDataMessage(
                        field, IllegalDataMessage.TYPE_PACKET_POSITIVE, block *
                        this.persistence.getBlockSize() + off,
                        Integer.toString( digit ) );

                    if ( AbstractErrorMessage.isErrorsEnabled() )
                    {
                        throw new CorruptedException(
                            this.getMeta(), block *
                            this.persistence.getBlockSize() + off );

                    }
                    else
                    {
                        ThreadLocalMessages.getMessages().addMessage( msg );
                    }

                    ret = AbstractLogicalFile.NO_NUMBER;
                    break;
                }
            }
            else
            {
                if ( digit < 0 || digit > 9 )
                {
                    msg = new IllegalDataMessage(
                        field, IllegalDataMessage.TYPE_PACKET_POSITIVE, block *
                        this.persistence.getBlockSize() + off,
                        Integer.toString( digit ) );

                    if ( !AbstractErrorMessage.isErrorsEnabled() )
                    {
                        throw new CorruptedException(
                            this.getMeta(), block *
                            this.persistence.getBlockSize() + off );

                    }
                    else
                    {
                        ThreadLocalMessages.getMessages().addMessage( msg );
                    }

                    ret = AbstractLogicalFile.NO_NUMBER;
                    break;
                }
                ret += ( digit & 0xF ) * AbstractLogicalFile.EXP10[exp];
            }
        }

        return ret;
    }

    //--------------------------------------long readNumberPackedPositive(...)--
    //--void writeNumberPackedPositive(...)-------------------------------------

    /**
     * Hilfs-Methode zum Schreiben von gepackten EBCDI-Zahlen.
     *
     * @param field Feld-Konstante des zu beschreibenden Feldes.
     * @param block Block, in den die Daten geschrieben werden sollen.
     * @param off Position in {@code block} ab der die Ziffern geschrieben
     * werden sollen.
     * @param len Anzahl an Byte die geschrieben werden sollen. Hierbei wird
     * linksseitig mit Nullen aufgefüllt, so dass exakt {@code len} Ziffern
     * geschrieben werden.
     * @param number Die zu schreibende Zahl.
     * @param sign {@code true} wenn ein Vorzeichen geschrieben werden soll;
     * {@code false} wenn kein Vorzeichen geschrieben werden soll.
     *
     * @throws IllegalArgumentException wenn {@code number} nicht mit
     * {@code len} Byte darstellbar ist.
     * @throws IOException wenn nicht geschrieben werden kann.
     *
     * @see org.jdtaus.banking.dtaus.Fields
     */
    protected void writeNumberPackedPositive(
        final int field, final long block, final int off, final int len,
        long number, final boolean sign ) throws IOException
    {
        int i;
        int pos = 0;
        final int nibbles = len * 2;
        final int digits = nibbles - ( sign
            ? 1
            : 0 );
        int exp = digits - 1;
        final long maxValue = AbstractLogicalFile.EXP10[digits] - 1L;
        byte b = 0;
        byte digit;
        boolean highNibble = true;

        if ( number < 0L || number > maxValue )
        {
            throw new IllegalArgumentException( Long.toString( number ) );
        }

        for ( i = 0; i < nibbles; i++, exp-- )
        {
            // Vorzeichen des letzten Nibbles.
            if ( sign && exp < 0 )
            {
                digit = 0xC;
            }
            else
            {
                digit = ( byte ) Math.floor(
                    number / AbstractLogicalFile.EXP10[exp] );

                number -= ( digit * AbstractLogicalFile.EXP10[exp] );
            }
            if ( highNibble )
            {
                b = ( byte ) ( ( ( byte ) ( digit << 4 ) ) & 0xF0 );
                highNibble = false;
            }
            else
            {
                b |= digit;
                highNibble = true;
                this.buffer[pos++] = b;
            }
        }

        this.persistence.writeBlock( block, off, this.buffer, 0, len );
    }

    //-------------------------------------void writeNumberPackedPositive(...)--
    //--long readNumberBinary(...)----------------------------------------------

    /**
     * Hilfs-Methode zum Lesen von binär gespeicherten Zahlen.
     *
     * @param field Feld-Konstante des zu lesenden Feldes.
     * @param block Satzabschnitt, aus dem die Daten gelesen werden sollen.
     * @param off Position in {@code block} ab dem die Ziffern gelesen werden
     * sollen.
     * @param len Anzahl von Byte, die gelesen werden sollen.
     *
     * @return gelesene Zahl.
     *
     * @throws IllegalArgumentException wenn {@code len} negativ, {@code 0} oder
     * größer als {@code 8} ist.
     * @throws IOException wenn nicht gelesen werden kann.
     *
     * @see org.jdtaus.banking.dtaus.Fields
     */
    protected long readNumberBinary(
        final int field, final long block, final int off, final int len )
        throws IOException
    {
        if ( len <= 0 || len > 8 )
        {
            throw new IllegalArgumentException( Integer.toString( len ) );
        }

        long ret = 0L;
        int shift = ( len - 1 ) * 8;
        int i;
        long read;

        this.persistence.readBlock( block, off, this.buffer, 0, len );
        for ( i = 0; i < len; i++, shift -= 8 )
        {
            read = this.buffer[i] << shift;
            if ( read < 0 )
            {
                read += 256;
            }

            ret |= read;
        }

        return ret;
    }

    //--------------------------------------long readNumberPackedPositive(...)--
    //--void writeNumberBinary(...)---------------------------------------------

    /**
     * Hilfs-Methode zum Schreiben von binär gespeicherten Zahlen.
     *
     * @param field Feld-Konstante des zu beschreibenden Feldes.
     * @param block Block, in den die Daten geschrieben werden sollen.
     * @param off Position in {@code block} ab der die Ziffern geschrieben
     * werden sollen.
     * @param len Anzahl an Byte die geschrieben werden sollen.
     * @param number Die zu schreibende Zahl.
     *
     * @throws IllegalArgumentException wenn {@code len} negativ, {@code 0} oder
     * größer als {@code 8} ist.
     * @throws IOException wenn nicht geschrieben werden kann.
     *
     * @see org.jdtaus.banking.dtaus.Fields
     */
    protected void writeNumberBinary( final int field, final long block,
                                       final int off, final int len,
                                       final long number ) throws IOException
    {
        if ( len <= 0 || len > 8 )
        {
            throw new IllegalArgumentException( Integer.toString( len ) );
        }

        int shift = ( len - 1 ) * 8;
        int i;

        for ( i = 0; i < len; i++, shift -= 8 )
        {
            this.buffer[i] = ( byte ) ( ( number >> shift ) & 0xFFL );
        }

        this.persistence.writeBlock( block, off, this.buffer, 0, len );
    }

    //---------------------------------------------void writeNumberBinary(...)--
    //--boolean checkTransactionId(...)-----------------------------------------

    /**
     * Prüfung einer laufenden Transaktionsnummer.
     *
     * @param id zu prüfende Transaktionsnummer.
     * @param checksum aktuelle Prüfsumme.
     *
     * @throws NullPointerException {@code if(checksum == null)}
     */
    protected boolean checkTransactionId( final int id,
                                           final Checksum checksum )
    {
        if ( checksum == null )
        {
            throw new NullPointerException( "checksum" );
        }

        final int count = checksum.getTransactionCount();
        return count > 0 && id >= 0 && id < count;
    }

    //-----------------------------------------boolean checkTransactionId(...)--
    //--boolean checkTransactionCount(...)--------------------------------------

    /**
     * Prüfung einer Menge von Transaktionen.
     *
     * @param transactionCount zu prüfende Menge Transaktionen.
     */
    protected boolean checkTransactionCount( final int transactionCount )
    {
        return transactionCount >= 0 &&
            transactionCount <= AbstractLogicalFile.MAX_TRANSACTIONS;

    }

    //-----------------------------------------boolean checkTransactionId(...)--
    //--void checkAmount(...)---------------------------------------------------

    /**
     * Prüfung eines Transaktions-Betrages.
     * <p/>
     * Sollte {@code amount} keinem gültigen Betrag entsprechen, so wird eine
     * entsprechende {@code IllegalDataMessage} aufgezeichnet.
     *
     * @param field Feld-Konstante des Feldes.
     * @param block Satzabschnitt.
     * @param off Position in {@code block} der die Ziffern zugeordnet sind.
     * @param amount Wert für den geprüft werden soll, ob er einem gültigen
     * Transaktions-Betrag entspricht (Euro = Cent).
     * @param isMandatory {@code true} wenn bei der Prüfung vorausgesetzt werden
     * soll, daß ein Betrag eine Pflichtangabe ist; {@code false} wenn nicht.
     */
    protected final void checkAmount( final int field, final long block,
                                        final int off, final long amount,
                                        final boolean isMandatory )
    {
        final Message msg;

        if ( !this.checkAmount( amount, isMandatory ) )
        {
            msg = new IllegalDataMessage(
                field, IllegalDataMessage.TYPE_NUMERIC, block *
                this.persistence.getBlockSize() + off, Long.toString( amount ) );

            if ( AbstractErrorMessage.isErrorsEnabled() )
            {
                throw new CorruptedException(
                    this.getMeta(), block * this.persistence.getBlockSize() +
                    off );

            }
            else
            {
                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }
    }

    /**
     * Prüfung eines Transaktions-Betrages.
     *
     * @param amount Wert für den geprüft werden soll, ob er einem gültigen
     * Transaktions-Betrag entspricht (Euro = Cent).
     * @param isMandatory {@code true} wenn bei der Prüfung vorausgesetzt werden
     * soll, daß ein Betrag eine Pflichtangabe ist; {@code false} wenn nicht.
     *
     * @return {@code true} wenn {@code amount} einem gültigen Transaktions-
     * Betrag entspricht; {@code false} sonst.
     */
    protected boolean checkAmount( final long amount,
                                    final boolean isMandatory )
    {
        return ( isMandatory
            ? amount > 0L
            : amount >= 0L ) &&
            amount < 100000000000L;

    }

    //---------------------------------------------------void checkAmount(...)--
    //--boolean checkDate()-----------------------------------------------------

    /** Maximum allowed days between create and execution date. */
    protected static final int MAX_SCHEDULEDAYS = 15;

    /** Maximum allowed days between create and execution date in millis. */
    private static final long MAX_SCHEDULEDAYS_MILLIS =
        MAX_SCHEDULEDAYS * 86400000L;

    /** 01/01/1980 00:00:00 CET. */
    private static final long VALID_DATES_START_MILLIS = 315529200000L;

    /** 12/31/2079 23:59:59 CET. */
    private static final long VALID_DATES_END_MILLIS = 3471289199999L;

    /**
     * Prüfung eines Datums.
     *
     * @param date zu prüfendes Datum.
     *
     * @return {@code true} wenn {@code date} im gültigen Bereich liegt;
     * {@code false} wenn nicht,
     */
    protected boolean checkDate( final Date date )
    {
        boolean valid = false;

        if ( date != null )
        {
            final long millis = date.getTime();
            valid = millis >= VALID_DATES_START_MILLIS &&
                millis <= VALID_DATES_END_MILLIS;

        }

        return valid;
    }

    /**
     * Prüfung einer Auftrags-Terminierung.
     *
     * @param createDate zu prüfendes Dateierstellungs-Datum.
     * @param executionDate zu prüfendes Ausführungs-Datum.
     *
     * @return {@code true} wenn {@code createDate} in Kombination mit
     * {@code executionDate} einer gültigen Auftrags-Terminierung entspricht;
     * {@code false} wenn nicht;
     */
    protected boolean checkSchedule( final Date createDate,
                                      final Date executionDate )
    {
        boolean valid = createDate != null;

        if ( valid )
        {
            final long createMillis = createDate.getTime();
            if ( executionDate != null )
            {
                final long executionMillis = executionDate.getTime();
                valid = executionMillis >= createMillis &&
                    executionMillis <= createMillis +
                    MAX_SCHEDULEDAYS_MILLIS;

            }
        }

        return valid;
    }

    //-----------------------------------------------------boolean checkDate()--
    //--void resizeIndex(...)---------------------------------------------------

    /**
     * Hilfsmethode zum dynamischen Vergrössern des Index.
     *
     * @param index laufende Transaktionsnummer, für die der Index angepasst
     * werden soll.
     * @param checksum aktuelle Prüfsumme zur Initialisierung des Index.
     */
    protected void resizeIndex( final int index, final Checksum checksum )
    {
        long[] newIndex;
        int newLength;
        final int oldLength = this.index == null
            ? 0
            : this.index.length;

        // Index initialisieren.
        if ( this.index == null )
        {
            this.index = this.getMemoryManagerImpl().
                allocateLongs( checksum.getTransactionCount() + 1 );

            Arrays.fill( this.index, -1L );
            this.index[0] = 1L;
        }

        while ( this.index.length < index + 1 )
        {
            newLength = this.index.length * 2;
            if ( newLength <= index )
            {
                newLength = index + 1;
            }
            else if ( newLength > AbstractLogicalFile.MAX_TRANSACTIONS )
            {
                newLength = AbstractLogicalFile.MAX_TRANSACTIONS;
            }

            newIndex = this.getMemoryManagerImpl().allocateLongs( newLength );
            System.arraycopy( this.index, 0, newIndex, 0, this.index.length );
            Arrays.fill( newIndex, this.index.length, newIndex.length, -1L );
            this.index = newIndex;
        }

        if ( this.getLoggerImpl().isDebugEnabled() &&
            this.index.length != oldLength )
        {
            final MessageFormat fmt = AbstractLogicalFileBundle.getInstance().
                getLogResizeIndexMessage( Locale.getDefault() );

            this.getLoggerImpl().debug(
                fmt.format( new Object[] {
                            new Long( this.index.length )
                        } ) );

        }
    }

    //---------------------------------------------------void resizeIndex(...)--
    //--int getBlockType(...)---------------------------------------------------
    /**
     * Ermittelt den Typ eines Satzabschnitts.
     *
     * @param block Index des zu lesenden Satzabschnitts.
     *
     * @return Datensatztyp des Satzabschnitts {@code block}.
     *
     * @throws IOException wenn nicht gelesen werden kann.
     */
    protected abstract char getBlockType( long block ) throws IOException;

    //---------------------------------------------------int getBlockType(...)--
    //--void checksumTransaction(...)-------------------------------------------

    /**
     * Aktualisierung der Prüfsumme. Addiert die relevanten Daten der
     * Transaktion an Satzabschnitt {@code block} zu {@code checksum}.
     *
     * @param block Satzabschnitt der Transaktion, die zu {@code checksum}
     * addiert werden soll.
     * @param transaction temporäres Transaction-Objekt, das zum
     * Zwischenspeichern der Transaktionen verwendet werden soll.
     * @param checksum Prüfsumme, zu der die Daten der Transaktion an
     * Satzabschnitt {@code block} addiert werden sollen.
     *
     * @return Anzahl Satzabschnitte die von der Transaktion belegt werden.
     *
     * @throws IOException wenn nicht gelesen werden kann.
     */
    protected abstract int checksumTransaction(
        long block, Transaction transaction, Checksum checksum )
        throws IOException;

    //-------------------------------------------void checksumTransaction(...)--
    //--int blockCount(...)-----------------------------------------------------

    /**
     * Ermittlung der belegten Satzabschnitte einer Transaktion.
     *
     * @param transaction Transaktion, für die die Anzahl benötigter
     * Satzabschnitte ermittelt werden soll.
     *
     * @return Anzahl der von {@code transaction} belegten Satzabschnitte.
     */
    protected abstract int blockCount( Transaction transaction );

    /**
     * Ermittlung der belegten Satzabschnitte einer Transaktion.
     *
     * @param block Satzabschnitt, an dem die Transaktion beginnt, für die die
     * Anzahl belegter Satzabschnitte ermittelt werden soll.
     *
     * @return Anzahl der von der Transaktion in Satzabschnitt {@code block}
     * belegten Satzabschnitte.
     *
     * @throws IOException wenn nicht gelesen werden kann.
     */
    protected abstract int blockCount( long block ) throws IOException;

    //-----------------------------------------------------int blockCount(...)--

    /**
     * Liest den A Datensatz. Die entsprechenden Vor- und Nachbedingungen
     * werden in {@link AbstractLogicalFile#getHeader()} geprüft.
     *
     * @param headerBlock Satzabschnitt aus dem der A Datensatz gelesen werden
     * soll.
     *
     * @return A Datensatz.
     *
     * @throws IOException wenn nicht gelesen werden kann.
     */
    protected abstract Header readHeader( long headerBlock ) throws IOException;

    /**
     * Schreibt den A Datensatz. Die entsprechenden Vor- und Nachbedingungen
     * werden in {@link AbstractLogicalFile#setHeader(Header)} geprüft.
     *
     * @param headerBlock Satzabschnitt in den der A Datensatz geschrieben
     * werden soll.
     * @param header A Datensatz.
     *
     * @throws IOException wenn nicht geschrieben werden kann.
     */
    protected abstract void writeHeader( long headerBlock, Header header )
        throws IOException;

    /**
     * Liest den E Datensatz. Die entsprechenden Vor- und Nachbedingungen
     * werden in {@link AbstractLogicalFile#getChecksum()} geprüft.
     *
     * @param checksumBlock Satzabschnitt aus dem der E Datensatz gelesen
     * werden soll.
     *
     * @return E Datensatz.
     *
     * @throws IOException wenn nicht gelesen werden kann.
     */
    protected abstract Checksum readChecksum( long checksumBlock )
        throws IOException;

    /**
     * Schreibt den E Datensatz. Die entsprechenden Vor- und Nachbedingungen
     * werden in {@link AbstractLogicalFile#setChecksum(Checksum)} geprüft.
     *
     * @param checksumBlock Satzabschnitt in den der E Datensatz geschrieben
     * werden soll.
     * @param checksum E Datensatz.
     *
     * @throws IOException wenn nicht geschrieben werden kann.     *
     */
    protected abstract void writeChecksum(
        long checksumBlock, Checksum checksum ) throws IOException;

    /**
     * Liest einen C Datensatz. Die entsprechenden Vor- und Nachbedingungen
     * werden in {@link AbstractLogicalFile#getTransaction(int)} geprüft.
     *
     * @param block Satzabschnitt, an dem der C Datensatz beginnt.
     * @param transaction Instanz, die die gelesenen Daten aufnehmen soll.
     *
     * @return an {@code block} beginnender C Datensatz.
     *
     * @throws IOException wenn nicht gelesen werden kann.
     */
    protected abstract Transaction readTransaction(
        long block, Transaction transaction ) throws IOException;

    /**
     * Schreibt einen C Datensatz. Die entsprechenden Vor- und Nachbedingungen
     * werden in {@link AbstractLogicalFile#setTransaction(int, Transaction)}
     * geprüft.
     *
     * @param block Satzabschnitt, an dem der C Datensatz beginnen soll.
     * @param transaction Daten des C Datensatzes.
     *
     * @throws IOException wenn nicht geschrieben werden kann.
     */
    protected abstract void writeTransaction(
        long block, Transaction transaction ) throws IOException;

    //--Property "headerBlock"--------------------------------------------------
    /**
     * Liest den Wert der Property {@code headerBlock}.
     *
     * @return Satzabschnitt in dem die logische Datei beginnt.
     */
    protected long getHeaderBlock()
    {
        return this.headerBlock;
    }

    /**
     * Schreibt den Wert der Property {@code headerBlock}.
     *
     * @param headerBlock Satzabschnitt in dem die logische Datei beginnt.
     *
     * @throws IllegalArgumentException wenn {@code headerBlock} negativ oder
     * größer als die Anzahl vorhandener Satzabschnitte ist.
     * @throws IOException wenn die aktuelle Anzahl Satzabschnitte nicht
     * ermittelt werden kann.
     */
    protected void setHeaderBlock( final long headerBlock ) throws IOException
    {
        if ( headerBlock < 0L ||
            headerBlock > this.persistence.getBlockCount() )
        {

            throw new IllegalArgumentException( "headerBlock=" + headerBlock );
        }

        this.headerBlock = headerBlock;
    }

    //--------------------------------------------------Property "headerBlock"--
    //--Property "checksumBlock"------------------------------------------------

    /**
     * Liest den Wert der Property {@code checksumBlock}.
     *
     * @return Satzabschnitt des E-Datensatzes.
     */
    protected long getChecksumBlock()
    {
        return this.checksumBlock;
    }

    /**
     * Schreibt den Wert der Property {@code checksumBlock}.
     *
     * @param checksumBlock Satzabschnitt des E-Datensatzes.
     *
     * @throws IllegalArgumentException wenn {@code checksumBlock} negativ oder
     * größer als die vorhandene Anzahl Satzabschnitte ist.
     * @throws IOException wenn die aktuelle Anzahl Satzabschnitte nicht
     * ermittelt werden kann.
     */
    protected void setChecksumBlock(
        final long checksumBlock ) throws IOException
    {

        if ( checksumBlock <= this.getHeaderBlock() ||
            checksumBlock > this.persistence.getBlockCount() )
        {
            throw new IllegalArgumentException( "checksumBlock=" +
                                                checksumBlock );

        }

        this.checksumBlock = checksumBlock;
    }

    //------------------------------------------------Property "checksumBlock"--
    //--Property "configuration"------------------------------------------------

    /**
     * Implementation configuration.
     */
    private Configuration configuration;

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

    //------------------------------------------------Property "configuration"--
    //--LogicalFile-------------------------------------------------------------

    public Header getHeader() throws IOException
    {
        if ( this.cachedHeader == null )
        {
            this.cachedHeader = this.readHeader( this.getHeaderBlock() );
        }
        return ( Header ) this.cachedHeader.clone();
    }

    public Header setHeader( final Header header ) throws IOException
    {
        HeaderValidator validator = null;
        IllegalHeaderException result = null;
        final Header old = this.getHeader();
        final Specification validatorSpec = ModelFactory.getModel().
            getModules().getSpecification( HeaderValidator.class.getName() );

        for ( int i = validatorSpec.getImplementations().
            getImplementations().length - 1; i >= 0; i-- )
        {
            validator = ( HeaderValidator ) ContainerFactory.getContainer().
                getImplementation( HeaderValidator.class,
                                   validatorSpec.getImplementations().
                                   getImplementation( i ).
                                   getName() );

            result = validator.assertValidHeader( this, header, this.counter,
                                                  result );

        }

        if ( result != null && result.getMessages().length > 0 )
        {
            throw result;
        }

        this.writeHeader( this.getHeaderBlock(), header );
        this.cachedHeader = ( Header ) header.clone();
        return old;
    }

    public Checksum getChecksum() throws IOException
    {
        if ( this.cachedChecksum == null )
        {
            this.cachedChecksum = this.readChecksum( this.getChecksumBlock() );
        }
        return ( Checksum ) this.cachedChecksum.clone();
    }

    protected void setChecksum( final Checksum checksum ) throws IOException
    {
        this.writeChecksum( this.getChecksumBlock(), checksum );
        this.cachedChecksum = ( Checksum ) checksum.clone();
    }

    protected void checksum() throws IOException
    {
        char type;
        int count = 1;
        long block;
        long blockOffset = 1L;
        final Checksum stored;
        final Checksum c = new Checksum();
        final Transaction t = new Transaction();
        final long startBlock = this.getHeaderBlock();
        final Task task = new Task();
        Message msg;

        try
        {
            final long blockCount = this.persistence.getBlockCount();
            task.setIndeterminate( true );
            task.setCancelable( false );
            task.setDescription( new ChecksumsFileMessage() );
            this.getTaskMonitorImpl().monitor( task );

            block = startBlock;
            type = this.getBlockType( block++ );
            this.setChecksumBlock( block );
            if ( type != 'A' )
            {
                msg = new IllegalDataMessage(
                    Fields.FIELD_A2, IllegalDataMessage.TYPE_CONSTANT, block -
                    1L * this.persistence.getBlockSize() +
                    DTAUSDisk.ARECORD_OFFSETS[1], Character.toString( type ) );

                if ( AbstractErrorMessage.isErrorsEnabled() )
                {
                    throw new CorruptedException(
                        this.getMeta(),
                        block * this.persistence.getBlockSize() +
                        DTAUSDisk.ARECORD_OFFSETS[1] );

                }
                else
                {
                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                this.getHeader(); // A-Datensatz prüfen.
                this.counter = new CurrencyCounter();

                while ( block < blockCount &&
                    ( type = this.getBlockType( block ) ) == 'C' )
                {
                    final int id = count - 1;
                    final long blocks;

                    this.resizeIndex( id, c );
                    this.index[id] = blockOffset;
                    blocks = this.checksumTransaction( block, t, c );

                    if ( t.getCurrency() != null )
                    {
                        this.counter.add( t.getCurrency() );
                    }

                    block += blocks;
                    blockOffset += blocks;
                    c.setTransactionCount( count++ );
                    this.setChecksumBlock( block );
                }

                this.setChecksumBlock( block );
                if ( type == 'E' )
                {
                    stored = this.getChecksum();
                    if ( !stored.equals( c ) )
                    {
                        msg =
                            new ChecksumErrorMessage(
                            stored, c, this.getHeaderBlock() *
                            this.persistence.getBlockSize() );

                        if ( AbstractErrorMessage.isErrorsEnabled() )
                        {
                            throw new CorruptedException(
                                this.getMeta(), block *
                                this.persistence.getBlockSize() );

                        }
                        else
                        {
                            ThreadLocalMessages.getMessages().addMessage( msg );
                        }
                    }
                }
                else
                {
                    msg =
                        new IllegalDataMessage(
                        Fields.FIELD_E2, IllegalDataMessage.TYPE_CONSTANT,
                        block * this.persistence.getBlockSize() +
                        DTAUSDisk.ERECORD_OFFSETS[1],
                        Character.toString( type ) );

                    if ( AbstractErrorMessage.isErrorsEnabled() )
                    {
                        throw new CorruptedException(
                            this.getMeta(), block *
                            this.persistence.getBlockSize() +
                            DTAUSDisk.ERECORD_OFFSETS[1] );

                    }
                    else
                    {
                        ThreadLocalMessages.getMessages().addMessage( msg );
                    }

                }
            }
        }
        finally
        {
            this.getTaskMonitorImpl().finish( task );
        }
    }

    public final void createTransaction( final Transaction transaction )
        throws IOException
    {
        this.addTransaction( transaction );
    }

    public int addTransaction(
        final Transaction transaction ) throws IOException
    {
        final int transactionId;
        final int blockCount;
        final Checksum checksum = this.getChecksum();
        final int newCount = checksum.getTransactionCount() + 1;

        if ( !this.checkTransactionCount( newCount ) )
        {
            throw new ArrayIndexOutOfBoundsException( newCount );
        }

        TransactionValidator validator = null;
        IllegalTransactionException result = null;
        final Specification validatorSpec =
            ModelFactory.getModel().
            getModules().getSpecification( TransactionValidator.class.getName() );

        for ( int i = validatorSpec.getImplementations().
            getImplementations().length - 1; i >= 0; i-- )
        {
            validator = ( TransactionValidator ) ContainerFactory.getContainer().
                getImplementation( TransactionValidator.class,
                                   validatorSpec.getImplementations().
                                   getImplementation( i ).
                                   getName() );

            result = validator.assertValidTransaction( this, transaction,
                                                       result );

        }

        if ( result != null && result.getMessages().length > 0 )
        {
            throw result;
        }

        this.counter.add( transaction.getCurrency() );

        checksum.setTransactionCount( newCount );
        checksum.add( transaction );
        transactionId = checksum.getTransactionCount() - 1;
        blockCount = this.blockCount( transaction );
        this.persistence.insertBlocks( this.getChecksumBlock(), blockCount );
        this.setChecksumBlock( this.getChecksumBlock() + blockCount );
        this.resizeIndex( transactionId, checksum );
        this.index[transactionId] = this.getChecksumBlock() - blockCount -
            this.getHeaderBlock();

        this.writeTransaction( this.getHeaderBlock() +
                               this.index[transactionId], transaction );

        this.writeChecksum( this.getChecksumBlock(), checksum );
        this.cachedChecksum = checksum;

        return transactionId;
    }

    public Transaction getTransaction( final int index ) throws IOException
    {
        final Checksum checksum = this.getChecksum();
        if ( !this.checkTransactionId( index, checksum ) )
        {
            throw new ArrayIndexOutOfBoundsException( index );
        }

        return this.readTransaction( this.getHeaderBlock() +
                                     this.index[index], new Transaction() );

    }

    public Transaction setTransaction(
        final int index, final Transaction transaction ) throws IOException
    {
        final Checksum checksum = this.getChecksum();
        if ( !this.checkTransactionId( index, checksum ) )
        {
            throw new ArrayIndexOutOfBoundsException( index );
        }

        TransactionValidator validator = null;
        IllegalTransactionException result = null;
        final Specification validatorSpec =
            ModelFactory.getModel().
            getModules().getSpecification( TransactionValidator.class.getName() );

        for ( int i = validatorSpec.getImplementations().
            getImplementations().length - 1; i >= 0; i-- )
        {
            validator = ( TransactionValidator ) ContainerFactory.getContainer().
                getImplementation( TransactionValidator.class,
                                   validatorSpec.getImplementations().
                                   getImplementation( i ).
                                   getName() );

            result = validator.assertValidTransaction( this, transaction,
                                                       result );

        }

        if ( result != null && result.getMessages().length > 0 )
        {
            throw result;
        }

        final Transaction old = this.getTransaction( index );

        if ( !old.getCurrency().getCurrencyCode().
            equals( transaction.getCurrency().getCurrencyCode() ) )
        {
            this.counter.substract( old.getCurrency() );
            this.counter.add( transaction.getCurrency() );
        }

        final int oldBlocks;
        final int newBlocks;
        final int delta;
        int i;

        checksum.substract( old );
        checksum.add( transaction );
        oldBlocks = this.blockCount( old );
        newBlocks = this.blockCount( transaction );
        if ( oldBlocks < newBlocks )
        {
            delta = newBlocks - oldBlocks;
            this.persistence.insertBlocks( this.getHeaderBlock() +
                                           this.index[index], delta );

            for ( i = index + 1; i < this.index.length; i++ )
            {
                if ( this.index[i] != -1L )
                {
                    this.index[i] += delta;
                }
            }
            this.setChecksumBlock( this.getChecksumBlock() + delta );
        }
        else if ( oldBlocks > newBlocks )
        {
            delta = oldBlocks - newBlocks;
            this.persistence.deleteBlocks( this.getHeaderBlock() +
                                           this.index[index], delta );

            for ( i = index + 1; i < this.index.length; i++ )
            {
                if ( this.index[i] != -1L )
                {
                    this.index[i] -= delta;
                }
            }
            this.setChecksumBlock( this.getChecksumBlock() - delta );
        }
        this.writeTransaction( this.getHeaderBlock() + this.index[index],
                               transaction );

        this.writeChecksum( this.getChecksumBlock(), checksum );
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

        this.counter.substract( removed.getCurrency() );

        checksum.setTransactionCount( checksum.getTransactionCount() - 1 );
        checksum.substract( removed );

        final int blockCount = this.blockCount(
            this.getHeaderBlock() + this.index[index] );

        this.persistence.deleteBlocks( this.getHeaderBlock() +
                                       this.index[index], blockCount );

        this.setChecksumBlock( this.getChecksumBlock() - blockCount );
        for ( int i = index + 1; i < this.index.length; i++ )
        {
            if ( this.index[i] != -1L )
            {
                this.index[i] -= blockCount;
            }
            this.index[i - 1] = this.index[i];
        }

        this.writeChecksum( this.getChecksumBlock(), checksum );
        this.cachedChecksum = checksum;
        return removed;
    }

    //-------------------------------------------------------------LogicalFile--
}

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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.jdtaus.banking.AlphaNumericText27;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.Kontonummer;
import org.jdtaus.banking.Textschluessel;
import org.jdtaus.banking.TextschluesselVerzeichnis;
import org.jdtaus.banking.dtaus.Checksum;
import org.jdtaus.banking.dtaus.Fields;
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.LogicalFile;
import org.jdtaus.banking.dtaus.LogicalFileType;
import org.jdtaus.banking.dtaus.PhysicalFileError;
import org.jdtaus.banking.dtaus.Transaction;
import org.jdtaus.banking.dtaus.spi.ThreadLocalMessages;
import org.jdtaus.banking.dtaus.spi.runtime.messages.ChecksumErrorMessage;
import org.jdtaus.banking.dtaus.spi.runtime.messages.IllegalDataMessage;
import org.jdtaus.banking.dtaus.spi.runtime.messages.IllegalScheduleMessage;
import org.jdtaus.core.text.Message;
import org.jdtaus.core.text.spi.ApplicationLogger;
import org.jdtaus.core.io.IOError;
import org.jdtaus.core.io.spi.StructuredFileOperations;
import org.jdtaus.core.lang.spi.MemoryManager;
import org.jdtaus.core.logging.spi.Logger;
import org.jdtaus.core.monitor.Task;
import org.jdtaus.core.monitor.spi.TaskMonitor;

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
public abstract class AbstractLogicalFile implements LogicalFile {

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
    protected static final long[] EXP10 = new long[
        AbstractLogicalFile.FORMAT_MAX_DIGITS + 1];

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
    protected static final byte[] ASCII_TO_DIGITS = new byte[60];

    /**
     * Index = Ziffer,
     * Wert = EBCDI-Zeichen.
     */
    protected static final byte[] DIGITS_TO_EBCDI = {
        (byte) 0xF0, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3, (byte) 0xF4,
        (byte) 0xF5, (byte) 0xF6, (byte) 0xF7, (byte) 0xF8, (byte) 0xF9

    };

    /**
     * Index = EBCDI-Code einer Ziffer,
     * Wert = Ziffer.
     */
    protected static final byte[] EBCDI_TO_DIGITS = new byte[0xFA];

    /** Zeichensatz-Namen. */
    protected static final String[] ENCODING_NAMES = {
        null, "DIN_66003", "IBM273"
    };

    /** Konstante 10^7. */
    protected static final double EXP10_7 = Math.pow(10, 7);

    /** Maximale Anzahl unterstützter Verwendungszweckzeilen. */
    protected static final int MAX_DESCRIPTIONS = 14;

    //--------------------------------------------------------------Konstanten--
    //--Attribute---------------------------------------------------------------

    /** Verwendete {@code StructuredFile} Implementierung. */
    protected transient StructuredFileOperations persistence;

    /** Datums-Format mit zweistelliger Jahresangabe. */
    protected final transient DateFormat shortDateFormat =
        new SimpleDateFormat("ddMMyy", Locale.GERMANY);

    /** Datums-Format mit vierstelliger Jahresangabe. */
    protected final transient DateFormat longDateFormat =
        new SimpleDateFormat("ddMMyyyy", Locale.GERMANY);

    /** Puffer zum Lesen und Schreiben von Daten. */
    protected final transient byte[] buffer =
        new byte[AbstractLogicalFile.FORMAT_MAX_CHARS + 1];

    /** Satzabschnitt-Offset des A-Datensatzes. */
    private long headerBlock;

    /** Satzabschnitt-Offset des E-Datensatzes. */
    private long checksumBlock;

    /**
     * Index = laufende Transaktionsnummer,
     * Wert = Offset des Satzabschnittes an der die Transaktion beginnt.
     */
    protected transient long[] index;

    /** Zwischengespeicherter A Datensatz. */
    protected transient Header cachedHeader = null;

    /** Zwischengespeicherter E Datensatz. */
    protected transient Checksum cachedChecksum = null;

    /** Calendar der Instanz. */
    protected final Calendar calendar = Calendar.getInstance(Locale.GERMANY);

    //---------------------------------------------------------------Attribute--
    //--Konstruktoren-----------------------------------------------------------

    /** Statische Initialisierung der konstanten Felder. */
    static {
        for(int i = 0; i <= AbstractLogicalFile.FORMAT_MAX_DIGITS; i++) {
            AbstractLogicalFile.EXP10[i] =
                (long) Math.floor(Math.pow(10.00D, i));

        }

        Arrays.fill(AbstractLogicalFile.ASCII_TO_DIGITS, (byte) -1);
        Arrays.fill(AbstractLogicalFile.EBCDI_TO_DIGITS, (byte) -1);
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
    protected AbstractLogicalFile() {
        this.calendar.setLenient(false);
        Arrays.fill(this.buffer, (byte) -1);
    }

    /**
     * Ändert die zu Grunde liegende {@code StructuredFile} Implementierung.
     *
     * @param structuredFile neue {@code StructuredFile} Implementierung.
     *
     * @throws NullPointerException wenn {@code structuredFile} {@code null}
     * ist.
     * @throws IOError wenn zwischengespeicherte Änderungen der vorherigen
     * Instanz nicht geschrieben werden können.
     */
    public void setStructuredFile(
        final StructuredFileOperations structuredFile) throws IOError {

        if(structuredFile == null) {
            throw new NullPointerException("structuredFile");
        }

        if(this.persistence != null) {
            this.persistence.flush();
        }

        this.persistence = structuredFile;
        this.cachedHeader = null;
        this.cachedChecksum = null;
        this.index = null;
        Arrays.fill(this.buffer, (byte) -1);
    }

    //-----------------------------------------------------------Konstruktoren--
    //--Dependencies------------------------------------------------------------

    protected abstract MemoryManager getMemoryManagerImpl();

    protected abstract Logger getLoggerImpl();

    protected abstract ApplicationLogger getApplicationLoggerImpl();

    protected abstract TaskMonitor getTaskMonitorImpl();

    protected abstract TextschluesselVerzeichnis
        getTextschluesselVerzeichnisImpl();

    //------------------------------------------------------------Dependencies--
    //--long readNumber(...)----------------------------------------------------

    /**
     * Hilfs-Methode zum Lesen von Zahlen.
     * <p>Sollten ungültige Daten gelesen werden, so wird die Zahl {@code -1}
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
     * @return gelesene Zahl oder {@code -1} wenn gelesene Daten nicht als Zahl
     * interpretiert werden konnten.
     *
     * @throws PhysicalFileError wenn die Datei Fehler enthält und
     * {@link org.jdtaus.banking.dtaus.spi.AbstractErrorMessage#isErrorsEnabled()}
     * gleich {@code true} ist.
     * @throws IOError wenn nicht gelesen werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.Fields
     * @see org.jdtaus.banking.dtaus.spi.AbstractErrorMessage
     * @see org.jdtaus.banking.dtaus.spi.ThreadLocalMessages
     */
    protected Long readNumber(final int field, final long block, final int off,
        final int len, final int encoding) throws PhysicalFileError, IOError {

        return this.readNumber(field, block, off, len, encoding, false);
    }

    /**
     * Hilfs-Methode zum Lesen von Zahlen mit gegebenenfalls Konvertierung von
     * Leerzeichen zu Nullen.
     * <p>Die Verwendung dieser Methode mit {@code allowSpaces == true}
     * entspricht einem Verstoß gegen die Spezifikation. Diese Methode existiert
     * ausschließlich um ungültige Dateien lesen zu können und sollte nur in
     * diesen bestimmten Fällen verwendet werden.</p>
     * <p>Sollten ungültige Daten gelesen werden, so wird die Zahl {@code -1}
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
     * @return gelesene Zahl oder {@code -1} wenn gelesene Daten nicht als Zahl
     * interpretiert werden konnten.
     *
     * @throws PhysicalFileError wenn die Datei Fehler enthält und
     * {@link org.jdtaus.banking.dtaus.spi.AbstractErrorMessage#isErrorsEnabled()}
     * gleich {@code true} ist.
     * @throws IOError wenn nicht gelesen werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.Fields
     * @see org.jdtaus.banking.dtaus.spi.AbstractErrorMessage
     * @see org.jdtaus.banking.dtaus.spi.ThreadLocalMessages
     */
    protected Long readNumber(final int field, final long block, final int off,
        final int len, final int encoding, final boolean allowSpaces) throws
        PhysicalFileError, IOError {

        long ret = 0L;
        int read;
        final byte space;
        final byte[] table;
        final byte[] revTable;
        final MessageFormat fmt;
        String logViolation = null; // Wenn != null wird der Verstoß geloggt.

        if(encoding == AbstractLogicalFile.ENCODING_ASCII) {
            table = AbstractLogicalFile.DIGITS_TO_ASCII;
            revTable = AbstractLogicalFile.ASCII_TO_DIGITS;
            space = (byte) 32;
        } else if(encoding == AbstractLogicalFile.ENCODING_EBCDI) {
            table = AbstractLogicalFile.DIGITS_TO_EBCDI;
            revTable = AbstractLogicalFile.EBCDI_TO_DIGITS;
            space = (byte) 0x40;
        } else {
            throw new IllegalArgumentException(Integer.toString(encoding));
        }

        this.persistence.readBlock(block, off, this.buffer, 0, len);
        try {
            for(read = 0; read < len; read++) {
                if(allowSpaces && this.buffer[read] == space) {
                    if(logViolation == null) {
                        logViolation = new String(this.buffer, 0, len,
                            AbstractLogicalFile.ENCODING_NAMES[encoding]);

                    }
                    this.buffer[read] = table[0];
                }

                if(!(this.buffer[read] >= table[0] &&
                    this.buffer[read] <= table[9])) {

                    ThreadLocalMessages.getMessages().
                        addMessage(new IllegalDataMessage(field,
                        IllegalDataMessage.TYPE_NUMERIC,
                        block * this.persistence.getBlockSize() + off,
                        new String(this.buffer, 0, len,
                        AbstractLogicalFile.ENCODING_NAMES[encoding])));

                    ret = -1;
                    break;
                } else {
                    if(this.buffer[read] < 0) {
                        ret += revTable[this.buffer[read] + 256] *
                            AbstractLogicalFile.EXP10[len - read - 1];

                    } else {
                        ret += revTable[this.buffer[read]] *
                            AbstractLogicalFile.EXP10[len - read - 1];

                    }
                }
            }
        } catch(UnsupportedEncodingException e) {
            this.getLoggerImpl().error(e);
            throw new IOError(e);
        }

        if(logViolation != null) {
            if(this.getLoggerImpl().isInfoEnabled()) {
                fmt = AbstractLogicalFileBundle.
                    getReadNumberIllegalFileInfoMessage(Locale.getDefault());

                this.getLoggerImpl().info(fmt.format(new Object[] {
                    logViolation, new Long(ret)
                }));
            }
        }

        return new Long(ret);
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
     * @throws IOError wenn nicht geschrieben werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.Fields
     */
    protected void writeNumber(final int field, final long block,
        final int off, final int len, long number, final int encoding) throws
        IOError {

        int i;
        int pos;
        final long maxValue =  AbstractLogicalFile.EXP10[len] - 1L;
        byte digit;
        final byte[] table;

        if(number < 0L || number > maxValue) {
            throw new IllegalArgumentException(Long.toString(number));
        }

        if(encoding == AbstractLogicalFile.ENCODING_ASCII) {
            table = AbstractLogicalFile.DIGITS_TO_ASCII;
        } else if(encoding == AbstractLogicalFile.ENCODING_EBCDI) {
            table = AbstractLogicalFile.DIGITS_TO_EBCDI;
        } else {
            throw new IllegalArgumentException(Integer.toString(encoding));
        }

        for(i = len - 1, pos = 0; i >= 0; i--, pos++) {
            digit = (byte) Math.floor(number / AbstractLogicalFile.EXP10[i]);
            number -= (digit * AbstractLogicalFile.EXP10[i]);
            this.buffer[pos] = table[digit];
        }

        this.persistence.writeBlock(block, off, this.buffer, 0, len);
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
     * @throws IOError wenn nicht gelesen werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.Fields
     * @see org.jdtaus.banking.dtaus.spi.AbstractErrorMessage
     * @see org.jdtaus.banking.dtaus.spi.ThreadLocalMessages
     */
    protected String readAlphaNumeric(final int field, final long block,
        final int off, final int len, final int encoding) throws
        PhysicalFileError, IOError {

        String str;
        final char[] c;

        if(encoding != AbstractLogicalFile.ENCODING_ASCII &&
            encoding != AbstractLogicalFile.ENCODING_EBCDI) {

            throw new IllegalArgumentException(Integer.toString(encoding));
        }


        this.persistence.readBlock(block, off, this.buffer, 0, len);
        try {
            str = new String(this.buffer, 0, len,
                AbstractLogicalFile.ENCODING_NAMES[encoding]);

            c = str.toCharArray();
            for(int i = c.length - 1; i >= 0; i--) {
                if(!AlphaNumericText27.checkAlphaNumeric(c[i])) {
                    ThreadLocalMessages.getMessages().
                        addMessage(new IllegalDataMessage(field,
                        IllegalDataMessage.TYPE_ALPHA_NUMERIC,
                        block * this.persistence.getBlockSize() + off, str));

                    str = null;
                    break;
                }
            }

            return str;
        } catch(UnsupportedEncodingException e) {
            this.getLoggerImpl().error(e);
            throw new IOError(e);
        }
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
     * @throws IOError wenn nicht geschrieben werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.Fields
     */
    protected void writeAlphaNumeric(final int field, final long block,
        final int off, final int len, final String str,
        final int encoding) throws IOError {

        final int length;
        final int delta;
        final char[] c;
        final byte[] buf;
        final byte space;

        if(str == null) {
            throw new NullPointerException("str");
        }
        if((length = str.length()) > len) {
            throw new IllegalArgumentException(str);
        }

        if(encoding == AbstractLogicalFile.ENCODING_ASCII) {
            space = (byte) 32;
        } else if(encoding == AbstractLogicalFile.ENCODING_EBCDI) {
            space = (byte) 0x40;
        } else {
            throw new IllegalArgumentException(Integer.toString(encoding));
        }

        c = str.toCharArray();
        for(int i = c.length - 1; i >= 0; i--) {
            if(!AlphaNumericText27.checkAlphaNumeric(c[i])) {
                throw new IllegalArgumentException(Character.toString(c[i]));
            }
        }

        try {
            buf = str.getBytes(AbstractLogicalFile.ENCODING_NAMES[encoding]);
            if(length < len) {
                delta = len - length;
                System.arraycopy(buf, 0, this.buffer, 0, buf.length);
                Arrays.fill(this.buffer, buf.length, buf.length + delta, space);
            } else {
                System.arraycopy(buf, 0, this.buffer, 0, buf.length);
            }
        } catch(UnsupportedEncodingException e) {
            this.getLoggerImpl().error(e);
            throw new IOError(e);
        }

        this.persistence.writeBlock(block, off, this.buffer, 0, len);
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
     * @throws IOError wenn nicht gelesen werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.Fields
     * @see org.jdtaus.banking.dtaus.spi.AbstractErrorMessage
     * @see org.jdtaus.banking.dtaus.spi.ThreadLocalMessages
     */
    protected Date readShortDate(final int field, final long block,
        final int off, final int encoding) throws PhysicalFileError, IOError {

        final int len;

        Date ret = null;
        String str = null;
        boolean legal = false;

        if(encoding != AbstractLogicalFile.ENCODING_ASCII &&
            encoding != AbstractLogicalFile.ENCODING_EBCDI) {

            throw new IllegalArgumentException(Integer.toString(encoding));
        }

        try {
            this.persistence.readBlock(block, off, this.buffer, 0, 6);
            str = new String(this.buffer, 0, 6,
                AbstractLogicalFile.ENCODING_NAMES[encoding]);

            len = str.trim().length();
            if(len == 6) {
                this.calendar.clear();
                // Tag
                this.calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(
                    str.substring(0, 2)).intValue());

                // Monat
                this.calendar.set(Calendar.MONTH,
                    Integer.valueOf(str.substring(2, 4)).intValue() - 1);

                // Jahr
                int year = Integer.valueOf(str.substring(4, 6)).intValue();
                year = year <= 79 ? 2000 + year : 1900 + year;
                this.calendar.set(Calendar.YEAR, year);
                ret = this.calendar.getTime();

                if(!Header.Schedule.checkDate(ret)) {
                    ThreadLocalMessages.getMessages().addMessage(
                        new IllegalDataMessage(field,
                        IllegalDataMessage.TYPE_SHORTDATE,
                        block * this.persistence.getBlockSize() + off, str));

                    ret = null;
                }
            }

            if(len == 0 || len == 6) {
                legal = true;
            }

        } catch(NumberFormatException e) {
            ret = null;
            legal = false;
        } catch(IOException e) {
            this.getLoggerImpl().error(e);
            throw new IOError(e);
        }

        if(!legal) {
            ThreadLocalMessages.getMessages().addMessage(
                new IllegalDataMessage(field, IllegalDataMessage.TYPE_SHORTDATE,
                block * this.persistence.getBlockSize() + off, str));

        }

        return ret;
    }

    //-------------------------------------------------Date readShortDate(...)--
    //--void writeShortDate(...)------------------------------------------------

    /** Hilfs-Puffer. */
    private final StringBuffer shortDateBuffer = new StringBuffer(6);

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
     * @throws IOError wenn nicht geschrieben werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.Fields
     */
    protected void writeShortDate(final int field, final long block,
        final int off, final Date date, final int encoding) throws IOError {

        int i;
        final byte[] buf;

        if(encoding != AbstractLogicalFile.ENCODING_ASCII &&
            encoding != AbstractLogicalFile.ENCODING_EBCDI) {

            throw new IllegalArgumentException(Integer.toString(encoding));
        }
        try {
            if(date != null) {
                if(!Header.Schedule.checkDate(date)) {
                    throw new IllegalArgumentException(
                        Long.toString(date.getTime()));

                }

                this.shortDateBuffer.setLength(0);
                this.calendar.clear();
                this.calendar.setTime(date);
                // Tag
                i = this.calendar.get(Calendar.DAY_OF_MONTH);
                if(i < 10) {
                    this.shortDateBuffer.append('0');
                }
                this.shortDateBuffer.append(i);
                // Monat
                i = this.calendar.get(Calendar.MONTH) + 1;
                if(i < 10) {
                    this.shortDateBuffer.append('0');
                }
                this.shortDateBuffer.append(i);
                // Jahr
                i = this.calendar.get(Calendar.YEAR);
                this.shortDateBuffer.append(i >= 2000 && i <= 2009 ? "0" : "");
                this.shortDateBuffer.append(i >= 1980 && i < 2000 ?
                    i - 1900 : i - 2000);

                buf = this.shortDateBuffer.toString().getBytes(
                    AbstractLogicalFile.ENCODING_NAMES[encoding]);

            } else {
                buf = "      ".getBytes(
                    AbstractLogicalFile.ENCODING_NAMES[encoding]);

            }

            this.persistence.writeBlock(block, off, buf, 0, 6);
        } catch(IOException e) {
            this.getLoggerImpl().error(e);
            throw new IOError(e);
        }
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
     * @throws PhysicalFileError wenn die Datei Fehler enthält und
     * {@link org.jdtaus.banking.dtaus.spi.AbstractErrorMessage#isErrorsEnabled()}
     * gleich {@code true} ist.
     * @throws IOError wenn nicht gelesen werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.Fields
     * @see org.jdtaus.banking.dtaus.spi.AbstractErrorMessage
     * @see org.jdtaus.banking.dtaus.spi.ThreadLocalMessages
     */
    protected Date readLongDate(final int field, final long block,
        final int off, final int encoding) throws PhysicalFileError, IOError {

        final int len;

        boolean legal = false;
        Date ret = null;
        String str = null;

        if(encoding != AbstractLogicalFile.ENCODING_ASCII &&
            encoding != AbstractLogicalFile.ENCODING_EBCDI) {

            throw new IllegalArgumentException(Integer.toString(encoding));
        }

        try {
            this.persistence.readBlock(block, off, this.buffer, 0, 8);
            str = new String(this.buffer, 0, 8,
                AbstractLogicalFile.ENCODING_NAMES[encoding]);

            len = str.trim().length();
            if(len == 8) {
                this.calendar.clear();
                // Tag
                this.calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(
                    str.substring(0, 2)).intValue());

                // Monat
                this.calendar.set(Calendar.MONTH,
                    Integer.valueOf(str.substring(2, 4)).intValue() - 1);

                // Jahr
                this.calendar.set(Calendar.YEAR, Integer.valueOf(
                    str.substring(4, 8)).intValue());

                ret = this.calendar.getTime();
                if(!Header.Schedule.checkDate(ret)) {
                    ThreadLocalMessages.getMessages().addMessage(
                        new IllegalDataMessage(field,
                        IllegalDataMessage.TYPE_LONGDATE,
                        block * this.persistence.getBlockSize() + off, str));

                    ret = null;
                }

            }

            if(len == 0 || len == 8) {
                legal = true;
            }

        } catch(NumberFormatException e) {
            legal = false;
            ret = null;
        } catch(IOException e) {
            this.getLoggerImpl().error(e);
            throw new IOError(e);
        }

        if(!legal) {
            ThreadLocalMessages.getMessages().addMessage(
                new IllegalDataMessage(field, IllegalDataMessage.TYPE_LONGDATE,
                block * this.persistence.getBlockSize() + off, str));

        }

        return ret;
    }

    //-------------------------------------------------Date readShortDate(...)--
    //--void writeShortDate(...)------------------------------------------------

    /** Hilfs-Puffer. */
    private final StringBuffer longDateBuffer = new StringBuffer(8);

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
     * @throws IOError wenn nicht geschrieben werden kann.
     *
     * @see #ENCODING_ASCII
     * @see #ENCODING_EBCDI
     * @see org.jdtaus.banking.dtaus.Fields
     */
    protected void writeLongDate(final int field, final long block,
        final int off, final Date date,
        final int encoding) throws IOError {

        int i;
        final byte[] buf;

        if(encoding != AbstractLogicalFile.ENCODING_ASCII
            && encoding != AbstractLogicalFile.ENCODING_EBCDI) {

            throw new IllegalArgumentException(Integer.toString(encoding));
        }

        try {
            if(date != null) {
                if(!Header.Schedule.checkDate(date)) {
                    throw new IllegalArgumentException(
                        Long.toString(date.getTime()));

                }

                this.longDateBuffer.setLength(0);
                this.calendar.clear();
                this.calendar.setTime(date);
                // Tag
                i = this.calendar.get(Calendar.DAY_OF_MONTH);
                if(i < 10) {
                    this.shortDateBuffer.append('0');
                }
                this.shortDateBuffer.append(i);
                // Monat
                i = this.calendar.get(Calendar.MONTH) + 1;
                if(i < 10) {
                    this.shortDateBuffer.append('0');
                }
                this.shortDateBuffer.append(i);
                // Jahr
                i = this.calendar.get(Calendar.YEAR);
                this.shortDateBuffer.append(i);
                buf = this.longDateBuffer.toString().getBytes(
                    AbstractLogicalFile.ENCODING_NAMES[encoding]);

            } else {
                buf = "        ".getBytes(
                    AbstractLogicalFile.ENCODING_NAMES[encoding]);

            }


            this.persistence.writeBlock(block, off, this.buffer, 0, 8);
        } catch(IOException e) {
            this.getLoggerImpl().error(e);
            throw new IOError(e);
        }
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
     * @return gelesene Zahl oder {@code -1} wenn gelesene Daten nicht als
     * Zahl interpretiert werden konnten.
     *
     * @throws PhysicalFileError wenn die Datei Fehler enthält und
     * {@link org.jdtaus.banking.dtaus.spi.AbstractErrorMessage#isErrorsEnabled()}
     * gleich {@code true} ist.
     * @throws IOError wenn nicht gelesen werden kann.
     *
     * @see org.jdtaus.banking.dtaus.Fields
     * @see org.jdtaus.banking.dtaus.spi.AbstractErrorMessage
     * @see org.jdtaus.banking.dtaus.spi.ThreadLocalMessages
     */
    protected long readNumberPackedPositive(final int field, final long block,
        final int off, final int len, final boolean sign) throws
        PhysicalFileError, IOError {

        long ret = 0L;
        final int nibbles = 2 * len;
        int exp = nibbles - (sign ? 2 : 1);
        boolean highNibble = true;
        int nibble = 0;
        int read = 0;
        int digit;

        this.persistence.readBlock(block, off, this.buffer, 0, len);
        for(; nibble < nibbles; nibble++, exp--) {
            if(highNibble) {
                if(this.buffer[read] < 0) {
                    digit = (this.buffer[read] + 256) >> 4;
                } else {
                    digit = this.buffer[read] >> 4;
                }

                highNibble = false;
            } else {
                digit = (this.buffer[read++] & 0xF);
                highNibble = true;
            }

            // Vorzeichen des letzten Nibbles.
            if(sign && exp < 0) {
                if(digit != 0xC) {
                    ThreadLocalMessages.getMessages().addMessage(
                        new IllegalDataMessage(field,
                        IllegalDataMessage.TYPE_PACKET_POSITIVE,
                        block * this.persistence.getBlockSize() + off,
                        Integer.toString(digit)));

                    ret = -1L;
                    break;
                }
            } else {
                if(digit < 0 || digit > 9) {
                    ThreadLocalMessages.getMessages().addMessage(
                        new IllegalDataMessage(field,
                        IllegalDataMessage.TYPE_PACKET_POSITIVE,
                        block * this.persistence.getBlockSize() + off,
                        Integer.toString(digit)));

                    ret = -1L;
                    break;
                }
                ret += (digit & 0xF) * AbstractLogicalFile.EXP10[exp];
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
     * @throws IOError wenn nicht geschrieben werden kann.
     *
     * @see org.jdtaus.banking.dtaus.Fields
     */
    protected void writeNumberPackedPositive(final int field, final long block,
        final int off, final int len, long number, final boolean sign) throws
        IOError {

        int i;
        int pos = 0;
        final int nibbles = len * 2;
        final int digits = nibbles - (sign ? 1 : 0);
        int exp = digits - 1;
        final long maxValue =  AbstractLogicalFile.EXP10[digits] - 1L;
        byte b = 0;
        byte digit;
        boolean highNibble = true;

        if(number < 0L || number > maxValue) {
            throw new IllegalArgumentException(Long.toString(number));
        }

        for(i = 0; i < nibbles; i++, exp--) {
            // Vorzeichen des letzten Nibbles.
            if(sign && exp < 0) {
                digit = 0xC;
            } else {
                digit = (byte) Math.floor(
                    number / AbstractLogicalFile.EXP10[exp]);

                number -= (digit * AbstractLogicalFile.EXP10[exp]);
            }
            if(highNibble) {
                b = (byte) (((byte) (digit << 4)) & 0xF0);
                highNibble = false;
            } else {
                b |= digit;
                highNibble = true;
                this.buffer[pos++] = b;
            }
        }

        this.persistence.writeBlock(block, off, this.buffer, 0, len);
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
     * @throws IOError wenn nicht gelesen werden kann.
     *
     * @see org.jdtaus.banking.dtaus.Fields
     */
    protected long readNumberBinary(final int field, final long block,
        final int off, final int len) throws IOError {

        if(len <= 0 || len > 8) {
            throw new IllegalArgumentException(Integer.toString(len));
        }

        long ret = 0L;
        int shift = (len - 1) * 8;
        int i;
        long read;

        this.persistence.readBlock(block, off, this.buffer, 0, len);
        for(i = 0; i < len; i++, shift -= 8) {
            read = this.buffer[i] << shift;
            if(read < 0) {
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
     * @throws IOError wenn nicht geschrieben werden kann.
     *
     * @see org.jdtaus.banking.dtaus.Fields
     */
    protected void writeNumberBinary(final int field, final long block,
        final int off, final int len, final long number) throws IOError {

        if(len <= 0 || len > 8) {
            throw new IllegalArgumentException(Integer.toString(len));
        }

        int shift = (len - 1) * 8;
        int i;

        for(i = 0; i < len; i++, shift -= 8) {
            this.buffer[i] = (byte) ((number >> shift) & 0xFFL);
        }

        this.persistence.writeBlock(block, off, this.buffer, 0, len);
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
    protected boolean checkTransactionId(final int id,
        final Checksum checksum) {

        if(checksum == null) {
            throw new NullPointerException("checksum");
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
    protected boolean checkTransactionCount(final int transactionCount) {
        return transactionCount >= 0 &&
            transactionCount <= AbstractLogicalFile.MAX_TRANSACTIONS;

    }

    //-----------------------------------------boolean checkTransactionId(...)--
    //--void checkAccountCode(...)----------------------------------------------

    /**
     * Prüfung einer Kontonummer.
     * <p>Sollte {@code accountCode} keiner gültigen Kontonummer entsprechen,
     * so wird eine entsprechende {@code IllegalDataMessage} aufgezeichnet.</p>
     *
     * @param field Feld-Konstante des Feldes.
     * @param block Satzabschnitt.
     * @param off Position in {@code block} der die Ziffern zugeordnet sind.
     * @param accountCode Wert für den geprüft werden soll, ob er einer gültigen
     * Kontonummer entspricht.
     * @param isMandatory {@code true} wenn bei der Prüfung vorausgesetzt werden
     * soll, daß die Kontonummer eine Pflichtangabe ist; {@code false} wenn
     * nicht.
     */
    protected final void checkAccountCode(final int field, final long block,
        final int off, final long accountCode, final boolean isMandatory) {

        if(!this.checkAccountCode(accountCode, isMandatory)) {
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                field, IllegalDataMessage.TYPE_NUMERIC,
                block * this.persistence.getBlockSize() + off,
                Long.toString(accountCode)));

        }
    }

    /**
     * Prüfung einer Kontonummer.
     *
     * @param accountCode Wert für den geprüft werden soll, ob er einer gültigen
     * Kontonummer entspricht.
     * @param isMandatory {@code true} wenn bei der Prüfung vorausgesetzt werden
     * soll, daß die Kontonummer einer Pflichtangabe ist; {@code false} wenn
     * nicht.
     *
     * @return {@code true} wenn {@code account} einer gültigen Kontonummer
     * entspricht; {@code false} sonst.
     */
    protected boolean checkAccountCode(final long accountCode,
        final boolean isMandatory) {

        return (isMandatory && Kontonummer.
            checkKontonummer(new Long(accountCode))) ||
            (!isMandatory && (accountCode == 0L || Kontonummer.
            checkKontonummer(new Long(accountCode))));

    }

    //----------------------------------------------void checkAccountCode(...)--
    //--void checkBankCode(...)-------------------------------------------------

    /**
     * Prüfung einer Bankleitzahl.
     * <p/>
     * Sollte {@code bankCode} keiner gültigen Bankleitzahl entsprechen,
     * so wird eine entsprechende {@code IllegalDataMessage} aufgezeichnet.
     *
     * @param field Feld-Konstante des Feldes.
     * @param block Satzabschnitt.
     * @param off Position in {@code block} der die Ziffern zugeordnet sind.
     * @param bankCode Wert für den geprüft werden soll, ob er einer gültigen
     * Bankleitzahl entspricht.
     * @param isMandatory {@code true}, wenn bei der Prüfung davon ausgegangen
     * werden soll, daß eine Bankangabe Pflicht ist; {@code false}, wenn bei
     * der Prüfung davon ausgegangen werden soll, daß eine Bankangabe optional
     * ist.
     */
    protected final void checkBankCode(final int field, final long block,
        final int off, final int bankCode, final boolean isMandatory) {

        if(!this.checkBankCode(bankCode, isMandatory)) {
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                field, IllegalDataMessage.TYPE_NUMERIC,
                block * this.persistence.getBlockSize() + off,
                Integer.toString(bankCode)));

        }
    }

    /**
     * Prüfung einer Bankleitzahl.
     *
     * @param bankCode Wert für den geprüft werden soll, ob er einer gültigen
     * Bankleitzahl entspricht.
     * @param isMandatory {@code true}, wenn bei der Prüfung davon ausgegangen
     * werden soll, daß eine Bankangabe Pflicht ist; {@code false}, wenn bei
     * der Prüfung davon ausgegangen werden soll, daß eine Bankangabe optional
     * ist.
     *
     * @return {@code true} wenn {@code bankCode} einer gültigen Bankleitzahl
     * entspricht; {@code false} wenn nicht.
     */
    protected boolean checkBankCode(final int bankCode,
        final boolean isMandatory) {

        boolean valid = bankCode >= 0;

        if(valid) {
            valid = (isMandatory && Bankleitzahl.
                checkBankleitzahl(new Integer(bankCode))) ||
                (!isMandatory && (bankCode == 0 || Bankleitzahl.
                checkBankleitzahl(new Integer(bankCode))));

        }

        return valid;
    }

    //-------------------------------------------------void checkBankCode(...)--
    //--void checkCreateDate(...)-----------------------------------------------

    /**
     * Prüfung eines Erstellungsdatums.
     * <p/>
     * Sollte {@code createDate} keinem gültigen Erstellungsdatum entsprechen,
     * so wird eine entsprechende {@code IllegalDateMessage} erzeugt.
     *
     * @param field Feld-Konstante des Feldes des Erstellungsdatums.
     * @param block Satzabschnitt.
     * @param off Position in {@code block} der das Erstellungsdatum zugeordnet
     * ist.
     * @param createDate Erstellungsdatum.
     *
     * @throws PhysicalFileError bei technischen Fehlern.
     */
    protected final void checkCreateDate(final int field, final long block,
        final int off, final Date createDate) throws PhysicalFileError {

        if(createDate == null || !Header.Schedule.checkDate(createDate)) {
            ThreadLocalMessages.getMessages().addMessage(
                new IllegalDataMessage(field, IllegalDataMessage.TYPE_SHORTDATE,
                block * this.persistence.getBlockSize() + off,
                createDate != null ?
                    Long.toString(createDate.getTime()) : null));


        }
    }

    //-----------------------------------------------void checkCreateDate(...)--
    //--void checkExecutionDate(...)--------------------------------------------

    /**
     * Prüfung eines Ausführungsdatums.
     * <p/>
     * Sollte {@code executionDate} keinem gültigen Ausführungssdatum
     * entsprechen, so wird eine entsprechende {@code IllegalDataMessage}
     * erzeugt.
     *
     * @param field Feld-Konstante des Feldes des Erstellungsdatums.
     * @param block Satzabschnitt.
     * @param off Position in {@code block} der das Erstellungsdatum zugeordnet
     * ist.
     * @param executionDate Ausführungsdatum.
     *
     * @throws PhysicalFileError bei technischen Fehlern.
     */
    protected final void checkExecutionDate(final int field, final long block,
        final int off, final Date executionDate) throws PhysicalFileError {

        if(executionDate != null && !Header.Schedule.checkDate(executionDate)) {
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                field, IllegalDataMessage.TYPE_LONGDATE,
                block * this.persistence.getBlockSize() + off,
                Long.toString(executionDate.getTime())));

        }
    }

    //--------------------------------------------void checkExecutionDate(...)--
    //--void checkHeaderSchedule(...)-------------------------------------------

    /**
     * Prüfung eines Erstellungs- und Ausführungsdatums.
     * <p/>
     * Sollte {@code schedule} keiner gültigen Auftragsterminierung entsprechen,
     * so wird eine entsprechende {@code IllegalDataMessage} aufgezeichnet.
     *
     * @param field Feld-Konstante des Feldes des Ausführungsdatums.
     * @param block Satzabschnitt.
     * @param off Position in {@code block} der das Ausführungsdatum zugeordnet
     * ist.
     * @param schedule Terminierung einer logischen DTAUS-Datei.
     *
     * @throws NullPointerException {@code if(schedule == null)}
     */
    protected final void checkHeaderSchedule(final int field, final long block,
        final int off, final Header.Schedule schedule) {

        if(!this.checkHeaderSchedule(schedule)) {
            ThreadLocalMessages.getMessages().addMessage(
                new IllegalScheduleMessage(
                block * this.persistence.getBlockSize(), this.getHeader()));

        }
    }

    /**
     * Prüfung eines Erstellungs- und Ausführungsdatums.
     *
     * @param schedule Terminierung einer logischen DTAUS-Datei.
     *
     * @return {@code true} wenn {@code schedule} einer gültigen Terminierung
     * entspricht; {@code false} wenn nicht.
     *
     * @throws NullPointerException {@code if(schedule == null)}
     */
    protected boolean checkHeaderSchedule(final Header.Schedule schedule) {
        if(schedule == null) {
            throw new NullPointerException("schedule");
        }

        return Header.Schedule.checkSchedule(schedule.getCreateDate(),
            schedule.getExecutionDate());

    }

    //-------------------------------------------void checkHeaderSchedule(...)--
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
    protected final void checkAmount(final int field, final long block,
        final int off, final long amount, final boolean isMandatory) {

        if(!this.checkAmount(amount, isMandatory)) {
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                field, IllegalDataMessage.TYPE_NUMERIC,
                block * this.persistence.getBlockSize() + off,
                Long.toString(amount)));

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
    protected boolean checkAmount(final long amount,
        final boolean isMandatory) {

        return (isMandatory ? amount > 0L : amount >= 0L) &&
            amount < 100000000000L;

    }

    //---------------------------------------------------void checkAmount(...)--
    //--void checkHolder(...)---------------------------------------------------

    /**
     * Prüfung von Kontoinhaber-Daten.
     * <p/>
     * Sollte {@code holder} keiner gültigen Konto-Inhaber-Angabe entsprechen,
     * so wird eine entsprechende {@code IllegalDataMessage} aufgezeichnet.
     *
     * @param field Feld-Konstante des Feldes.
     * @param block Block.
     * @param off Position in {@code block} der die Ziffern zugeordnet sind.
     * @param name Wert für den geprüft werden soll, ob er einer
     * gültigen Kontoinhaber-Angabe entspricht.
     * @param isMandatory {@code true} wenn bei der Prüfung davon ausgegangen
     * werden soll, daß die Inhaberangabe Pflicht ist; {@code false} wenn
     * nicht.
     */
    protected final void checkName(final int field, final long block,
        final int off, final String holder, final boolean isMandatory) {

        if(!this.checkName(holder, isMandatory)) {
            ThreadLocalMessages.getMessages().addMessage(new IllegalDataMessage(
                field, IllegalDataMessage.TYPE_ALPHA_NUMERIC,
                block * this.persistence.getBlockSize() + off, holder));

        }
    }

    /**
     * Prüfung von Kontoinhaber-Daten.
     *
     * @param name Wert für den geprüft werden soll, ob er einer
     * gültigen Kontoinhaber-Angabe entspricht.
     * @param isMandatory {@code true} wenn bei der Prüfung davon ausgegangen
     * werden soll, daß die Inhaberangabe Pflicht ist; {@code false} wenn
     * nicht.
     *
     * @return {@code true} wenn {@code name} einer gültigen
     * Kontoinhaber-Angabe entspricht; {@code false} sonst.
     */
    protected boolean checkName(final String name,
        final boolean isMandatory) {

        // Feld ist in beiden Formaten gleich lang.
        boolean ret = true;

        if(name != null) {
            final char[] chars = name.toCharArray();
            ret =  (!isMandatory && chars.length >= 0) ||
                (isMandatory && chars.length > 0) &&
                chars.length <= DTAUSDisk.CRECORD_LENGTH1[13];

            if(ret) {
                for(int i = chars.length - 1; i >= 0; i--) {
                    if(!AlphaNumericText27.checkAlphaNumeric(chars[i])) {
                        ret = false;
                        break;
                    }
                }
            }

        } else if(isMandatory) {
            ret = false;
        }

        return ret;
    }

    //---------------------------------------------------void checkHolder(...)--
    //--boolean checkDescriptionCount(...)--------------------------------------

    /**
     * Prüfung einer Anzahl Verwendungszweckzeilen.
     *
     * @param descriptionCount Wert für den geprüft werden soll, ob er einer
     * gültigen Anzahl Verwendungszweckzeilen pro Transaktion entspricht.
     *
     * @return {@code true}, wenn {@code descriptionCount} einer gültigen
     * Anzahl Verwendungszweckzeilen pro Transaktion entspricht;
     * {@code false} sonst.
     */
    private boolean checkDescriptionCount(final int descriptionCount) {
        return descriptionCount >= 0 &&
            descriptionCount <= AbstractLogicalFile.MAX_DESCRIPTIONS;

    }

    //--------------------------------------boolean checkDescriptionCount(...)--
    //--void checkHeader(...)---------------------------------------------------

    /**
     * Prüfung eines A-Datensatzes.
     *
     * @param header zu prüfender A-Datensatz.
     *
     * @throws NullPointerException wenn Angaben fehlen.
     * @throws IllegalArgumentException wenn Angaben ungültig sind.
     */
    protected void checkHeader(final Header header) {
        if(header == null) {
            throw new NullPointerException("header");
        }

        final LogicalFileType type;
        final Header.Schedule schedule;

        if(header == null) {
            throw new NullPointerException("header");
        }
        if((schedule = header.getSchedule()) == null) {
            throw new NullPointerException("schedule");
        }
        if(schedule.getCreateDate() == null) {
            throw new NullPointerException("createDate");
        }
        if((type = header.getType()) == null) {
            throw new NullPointerException("type");
        }
        if(header.getCustomer() == null) {
            throw new NullPointerException("customer");
        }
        if(header.getBank() == null) {
            throw new NullPointerException("bank");
        }
        if(header.getAccount() == null) {
            throw new NullPointerException("account");
        }
        /*
        if(type.isSendByBank() &&
            !this.checkBankCode(header.getBankData5(), false)) {

            throw new IllegalArgumentException("bankData5=" +
                header.getBankData5());

        }*/
    }

    //---------------------------------------------------void checkHeader(...)--
    //--void checkTransaction(...)----------------------------------------------

    /**
     * Prüfung einer Transaktion.
     *
     * @param transaction zu prüfende Transaktion.
     *
     * @throws NullPointerException wenn Angaben fehlen.
     * @throws IllegalArgumentException wenn Angaben ungültig sind.
     */
    protected void checkTransaction(final Transaction transaction) {
        int i;
        final Transaction.Description desc;
        final Kontonummer executiveAccount;
        final Bankleitzahl executiveBank;
        final AlphaNumericText27 executiveName;
        final Kontonummer targetAccount;
        final Bankleitzahl targetBank;
        final AlphaNumericText27 targetName;
        final Textschluessel type;
        final LogicalFileType lFileType = this.getHeader().getType();
        final Textschluessel[] allowedTypes =
            this.getTextschluesselVerzeichnisImpl().
            search(lFileType.isDebitAllowed(), lFileType.isRemittanceAllowed());


        if(transaction == null) {
            throw new NullPointerException("transaction");
        }
        if((desc = transaction.getDescription()) == null) {
            throw new NullPointerException("description");
        }
        if((executiveAccount = transaction.getExecutiveAccount()) == null) {
            throw new NullPointerException("executiveAccount");
        }
        if((executiveBank = transaction.getExecutiveBank()) == null) {
            throw new NullPointerException("executiveBank");
        }
        if((executiveName = transaction.getExecutiveName()) == null) {
            throw new NullPointerException("executiveName");
        }
        if((targetAccount = transaction.getTargetAccount()) == null) {
            throw new NullPointerException("targetAccount");
        }
        if((targetBank = transaction.getTargetBank()) == null) {
            throw new NullPointerException("targetBank");
        }
        if((targetName = transaction.getTargetName()) == null) {
            throw new NullPointerException("targetName");
        }
        if((type = transaction.getType()) == null) {
            throw new NullPointerException("type");
        }
        if(allowedTypes != null) {
            for(i = allowedTypes.length - 1; i >= 0; i--) {
                if(type.equals(allowedTypes[i])) {
                    break;
                }
            }
            if(i < 0) {
                throw new IllegalArgumentException(type.toString());
            }
        } else {
            throw new IllegalArgumentException(type.toString());
        }
        if(!this.checkAmount(transaction.getAmount().longValue(), true)) {
            throw new IllegalArgumentException("amount=" +
                transaction.getAmount());

        }
        if(!this.checkDescriptionCount(desc.getDescriptionCount())) {
            throw new IllegalArgumentException("descriptionCount=" +
                desc.getDescriptionCount());

        }
    }

    //----------------------------------------------void checkTransaction(...)--
    //--void resizeIndex(...)---------------------------------------------------

    /**
     * Hilfsmethode zum dynamischen Vergrössern des Index.
     *
     * @param index laufende Transaktionsnummer, für die der Index angepasst
     * werden soll.
     * @param checksum aktuelle Prüfsumme zur Initialisierung des Index.
     */
    protected void resizeIndex(final int index, final Checksum checksum) {
        long[] newIndex;
        int newLength;
        final int oldLength = this.index == null ? 0 : this.index.length;

        // Index initialisieren.
        if(this.index == null) {
            this.index = this.getMemoryManagerImpl().
                allocateLongs(checksum.getTransactionCount() + 1);

            Arrays.fill(this.index, -1L);
            this.index[0] = 1L;
        }

        while(this.index.length < index + 1) {
            newLength = this.index.length * 2;
            if(newLength <= index) {
                newLength = index + 1;
            } else if(newLength > AbstractLogicalFile.MAX_TRANSACTIONS) {
                newLength = AbstractLogicalFile.MAX_TRANSACTIONS;
            }

            newIndex = this.getMemoryManagerImpl().allocateLongs(newLength);
            System.arraycopy(this.index, 0, newIndex, 0, this.index.length);
            Arrays.fill(newIndex, this.index.length, newIndex.length, -1L);
            this.index = newIndex;
        }

        if(this.getLoggerImpl().isDebugEnabled() &&
            this.index.length != oldLength) {

            final MessageFormat fmt =  AbstractLogicalFileBundle.
                getLogResizeIndexMessage(Locale.getDefault());

            this.getLoggerImpl().debug(fmt.format(new Object[] {
                new Long(this.index.length)
            }));
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
     * @throws PhysicalFileError bei technischen Fehlern.
     */
    protected abstract char getBlockType(long block) throws PhysicalFileError;

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
     * @throws PhysicalFileError bei technischen Fehlern.
     */
    protected abstract int checksumTransaction(long block,
        Transaction transaction, Checksum checksum) throws PhysicalFileError;

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
    protected abstract int blockCount(Transaction transaction);

    /**
     * Ermittlung der belegten Satzabschnitte einer Transaktion.
     *
     * @param block Satzabschnitt, an dem die Transaktion beginnt, für die die
     * Anzahl belegter Satzabschnitte ermittelt werden soll.
     *
     * @return Anzahl der von der Transaktion in Satzabschnitt {@code block}
     * belegten Satzabschnitte.
     */
    protected abstract int blockCount(long block);

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
     * @throws PhysicalFileError bei technischen Fehlern.
     */
    protected abstract Header readHeader(long headerBlock) throws
        PhysicalFileError;

    /**
     * Schreibt den A Datensatz. Die entsprechenden Vor- und Nachbedingungen
     * werden in {@link AbstractLogicalFile#setHeader(Header)} geprüft.
     *
     * @param headerBlock Satzabschnitt in den der A Datensatz geschrieben
     * werden soll.
     * @param header A Datensatz.
     *
     * @throws PhysicalFileError bei technischen Fehlern.
     */
    protected abstract void writeHeader(long headerBlock, Header header) throws
        PhysicalFileError;

    /**
     * Liest den E Datensatz. Die entsprechenden Vor- und Nachbedingungen
     * werden in {@link AbstractLogicalFile#getChecksum()} geprüft.
     *
     * @param checksumBlock Satzabschnitt aus dem der E Datensatz gelesen
     * werden soll.
     *
     * @return E Datensatz.
     *
     * @throws PhysicalFileError bei technischen Fehlern.
     */
    protected abstract Checksum readChecksum(long checksumBlock) throws
        PhysicalFileError;

    /**
     * Schreibt den E Datensatz. Die entsprechenden Vor- und Nachbedingungen
     * werden in {@link AbstractLogicalFile#setChecksum(Checksum)} geprüft.
     *
     * @param checksumBlock Satzabschnitt in den der E Datensatz geschrieben
     * werden soll.
     * @param checksum E Datensatz.
     *
     * @throws PhysicalFileError bei technischen Fehlern.
     */
    protected abstract void writeChecksum(long checksumBlock,
        Checksum checksum) throws PhysicalFileError;

    /**
     * Liest einen C Datensatz. Die entsprechenden Vor- und Nachbedingungen
     * werden in {@link AbstractLogicalFile#getTransaction(int)} geprüft.
     *
     * @param block Satzabschnitt, an dem der C Datensatz beginnt.
     * @param transaction Instanz, die die gelesenen Daten aufnehmen soll.
     *
     * @return an {@code block} beginnender C Datensatz.
     *
     * @throws PhysicalFileError bei technischen Fehlern.
     */
    protected abstract Transaction readTransaction(long block,
        Transaction transaction) throws PhysicalFileError;

    /**
     * Schreibt einen C Datensatz. Die entsprechenden Vor- und Nachbedingungen
     * werden in {@link AbstractLogicalFile#setTransaction(int, Transaction)}
     * geprüft.
     *
     * @param block Satzabschnitt, an dem der C Datensatz beginnen soll.
     * @param transaction Daten des C Datensatzes.
     *
     * @throws PhysicalFileError bei technischen Fehlern.
     */
    protected abstract void writeTransaction(long block,
        Transaction transaction) throws PhysicalFileError;

    //--Property "headerBlock"--------------------------------------------------

    /**
     * Liest den Wert der Property {@code headerBlock}.
     *
     * @return Satzabschnitt in dem die logische Datei beginnt.
     */
    protected long getHeaderBlock() {
        return this.headerBlock;
    }

    /**
     * Schreibt den Wert der Property {@code headerBlock}.
     *
     * @param headerBlock Satzabschnitt in dem die logische Datei beginnt.
     *
     * @throws IllegalArgumentException wenn {@code headerBlock} negativ oder
     * größer als die Anzahl vorhandener Satzabschnitte ist.
     */
    protected void setHeaderBlock(final long headerBlock) {
        if(headerBlock < 0L ||
            headerBlock > this.persistence.getBlockCount()) {

            throw new IllegalArgumentException("headerBlock=" + headerBlock);
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
    protected long getChecksumBlock() {
        return this.checksumBlock;
    }

    /**
     * Schreibt den Wert der Property {@code checksumBlock}.
     *
     * @param checksumBlock Satzabschnitt des E-Datensatzes.
     *
     * @throws IllegalArgumentException wenn {@code checksumBlock} negativ oder
     * größer als die vorhandene Anzahl Satzabschnitte ist.
     */
    protected void setChecksumBlock(final long checksumBlock) {
        if(checksumBlock <= this.getHeaderBlock() ||
            checksumBlock > this.persistence.getBlockCount()) {

            throw new IllegalArgumentException("checksumBlock=" +
                checksumBlock);

        }

        this.checksumBlock = checksumBlock;
    }

    //------------------------------------------------Property "checksumBlock"--
    //--LogicalFile-------------------------------------------------------------

    static class ChecksumMessage extends Message {

        private static final Object[] NO_ARGS = {};

        public Object[] getFormatArguments(final Locale locale) {
            return NO_ARGS;
        }

        public String getText(final Locale locale) {
            return AbstractLogicalFileBundle.getChecksumTaskText(locale);
        }
    }

    static class ChecksumTask extends Task {

        private final Message description = new ChecksumMessage();

        public Message getDescription() {
            return this.description;
        }
    }

    public Header getHeader() throws PhysicalFileError, IOError {
        if(this.cachedHeader == null) {
            this.cachedHeader = this.readHeader(this.getHeaderBlock());
        }
        return (Header) this.cachedHeader.clone();
    }

    public Header setHeader(final Header header) throws
        PhysicalFileError, IOError {

        this.checkHeader(header);
        final Checksum checksum = this.getChecksum();
        final Header old = this.getHeader();
        final LogicalFileType oldLabel = old.getType();
        final LogicalFileType newLabel = header.getType();
        if(oldLabel != null && checksum.getTransactionCount() > 0 &&
            (oldLabel.isDebitAllowed() && !newLabel.isDebitAllowed()) ||
            (oldLabel.isRemittanceAllowed() &&
            !newLabel.isRemittanceAllowed()))  {

            throw new IllegalArgumentException(newLabel.toString());
        }
        this.writeHeader(this.getHeaderBlock(), header);
        this.cachedHeader = (Header) header.clone();
        this.persistence.flush();
        return old;
    }

    public Checksum getChecksum() throws PhysicalFileError, IOError {
        if(this.cachedChecksum == null) {
            this.cachedChecksum = this.readChecksum(this.getChecksumBlock());
        }
        return (Checksum) this.cachedChecksum.clone();
    }

    protected void setChecksum(final Checksum checksum) throws
        PhysicalFileError, IOError {

        this.writeChecksum(this.getChecksumBlock(), checksum);
        this.cachedChecksum = (Checksum) checksum.clone();
        this.persistence.flush();
    }

    public void checksum() throws PhysicalFileError, IOError {
        char type;
        int count = 1;
        long block;
        long blockOffset = 1L;
        final Checksum stored;
        final Checksum c = new Checksum();
        final Transaction t = new Transaction();
        final long startBlock = this.getHeaderBlock();
        final ChecksumTask task = new ChecksumTask();

        try {
            task.setIndeterminate(true);
            this.getTaskMonitorImpl().monitor(task);

            block = startBlock;
            type = this.getBlockType(block++);
            this.setChecksumBlock(block);
            if(type != 'A') {
                ThreadLocalMessages.getMessages().addMessage(
                    new IllegalDataMessage(Fields.FIELD_A2,
                    IllegalDataMessage.TYPE_CONSTANT,
                    block - 1L * this.persistence.getBlockSize() +
                    DTAUSDisk.ARECORD_OFFSETS[1], Character.toString(type)));

            } else {
                this.getHeader(); // A-Datensatz prüfen.
                while((type = this.getBlockType(block)) == 'C') {
                    final int id = count - 1;
                    final long blocks;

                    this.resizeIndex(id, c);
                    this.index[id] = blockOffset;
                    blocks = this.checksumTransaction(block, t, c);
                    block += blocks;
                    blockOffset += blocks;
                    c.setTransactionCount(count++);
                    this.setChecksumBlock(block);
                }

                this.setChecksumBlock(block);
                if(type == 'E') {
                    stored = this.getChecksum();
                    if(!stored.equals(c)) {
                        ThreadLocalMessages.getMessages().addMessage(
                            new ChecksumErrorMessage(stored, c,
                            this.getHeaderBlock() *
                            this.persistence.getBlockSize()));

                    }
                } else {
                    ThreadLocalMessages.getMessages().addMessage(
                        new IllegalDataMessage(Fields.FIELD_E2,
                        IllegalDataMessage.TYPE_CONSTANT,
                        block * this.persistence.getBlockSize() +
                        DTAUSDisk.ERECORD_OFFSETS[1],
                        Character.toString(type)));

                }
            }
        } finally {
            this.getTaskMonitorImpl().finish(task);
        }
    }

    public void createTransaction(final Transaction transaction) throws
        PhysicalFileError, IOError {

        final int transactionId;
        final int blockCount;
        final Checksum checksum = this.getChecksum();
        final int newCount = checksum.getTransactionCount() + 1;

        if(!this.checkTransactionCount(newCount)) {
            throw new ArrayIndexOutOfBoundsException(newCount);
        }
        this.checkTransaction(transaction);

        checksum.setTransactionCount(newCount);
        checksum.setSumAmount(checksum.getSumAmount() +
            transaction.getAmount().longValue());

        checksum.setSumTargetAccount(checksum.getSumTargetAccount() +
            transaction.getTargetAccount().longValue());

        checksum.setSumTargetBank(checksum.getSumTargetBank() +
            transaction.getTargetBank().intValue());

        transactionId = checksum.getTransactionCount() - 1;
        blockCount = this.blockCount(transaction);
        this.persistence.insertBlocks(this.getChecksumBlock(), blockCount);
        this.setChecksumBlock(this.getChecksumBlock() + blockCount);
        this.resizeIndex(transactionId, checksum);
        this.index[transactionId] = this.getChecksumBlock() - blockCount -
            this.getHeaderBlock();

        this.writeTransaction(this.getHeaderBlock() +
            this.index[transactionId], transaction);

        this.writeChecksum(this.getChecksumBlock(), checksum);
        this.cachedChecksum = checksum;
        this.persistence.flush();
    }

    public Transaction getTransaction(final int index) throws
        PhysicalFileError, IOError {

        final Checksum checksum = this.getChecksum();
        if(!this.checkTransactionId(index, checksum)) {
            throw new ArrayIndexOutOfBoundsException(index);
        }

        return this.readTransaction(this.getHeaderBlock() +
            this.index[index], new Transaction());

    }

    public Transaction setTransaction(final int index,
        final Transaction transaction) throws PhysicalFileError, IOError {

        final Checksum checksum = this.getChecksum();
        if(!this.checkTransactionId(index, checksum)) {
            throw new ArrayIndexOutOfBoundsException(index);
        }

        this.checkTransaction(transaction);
        final Transaction old = this.getTransaction(index);
        final int oldBlocks;
        final int newBlocks;
        final int delta;
        int i;

        checksum.setSumAmount(checksum.getSumAmount() -
            old.getAmount().longValue());

        checksum.setSumTargetAccount(checksum.getSumTargetAccount() -
            old.getTargetAccount().longValue());

        checksum.setSumTargetBank(checksum.getSumTargetBank() -
            old.getTargetBank().intValue());

        oldBlocks = this.blockCount(old);
        newBlocks = this.blockCount(transaction);
        if(oldBlocks < newBlocks) {
            delta = newBlocks - oldBlocks;
            this.persistence.insertBlocks(this.getHeaderBlock() +
                this.index[index], delta);

            for(i = index + 1; i < this.index.length; i++) {
                if(this.index[i] != -1L) {
                    this.index[i] += delta;
                }
            }
            this.setChecksumBlock(this.getChecksumBlock() + delta);
        } else if(oldBlocks > newBlocks) {
            delta = oldBlocks - newBlocks;
            this.persistence.deleteBlocks(this.getHeaderBlock() +
                this.index[index], delta);

            for(i = index + 1; i < this.index.length; i++) {
                if(this.index[i] != -1L) {
                    this.index[i] -= delta;
                }
            }
            this.setChecksumBlock(this.getChecksumBlock() - delta);
        }
        this.writeTransaction(this.getHeaderBlock() + this.index[index],
            transaction);

        checksum.setSumTargetAccount(checksum.getSumTargetAccount() +
            transaction.getTargetAccount().longValue());

        checksum.setSumAmount(checksum.getSumAmount() +
            transaction.getAmount().longValue());

        checksum.setSumTargetBank(checksum.getSumTargetBank() +
            transaction.getTargetBank().intValue());

        this.writeChecksum(this.getChecksumBlock(), checksum);
        this.cachedChecksum = checksum;
        this.persistence.flush();
        return old;
    }

    public Transaction removeTransaction(final int index) throws
        PhysicalFileError, IOError {

        final Checksum checksum = this.getChecksum();
        if(!this.checkTransactionId(index, checksum)) {
            throw new ArrayIndexOutOfBoundsException(index);
        }

        final Transaction removed = this.getTransaction(index);
        checksum.setTransactionCount(checksum.getTransactionCount() - 1);
        checksum.setSumAmount(checksum.getSumAmount() -
            removed.getAmount().longValue());

        checksum.setSumTargetAccount(checksum.getSumTargetAccount() -
            removed.getTargetAccount().longValue());

        checksum.setSumTargetBank(checksum.getSumTargetBank() -
            removed.getTargetBank().intValue());

        final int blockCount = this.blockCount(
            this.getHeaderBlock() + this.index[index]);

        this.persistence.deleteBlocks(this.getHeaderBlock() +
            this.index[index], blockCount);

        this.setChecksumBlock(this.getChecksumBlock() - blockCount);
        for(int i = index + 1; i < this.index.length; i++) {
            if(this.index[i] != -1L) {
                this.index[i] -= blockCount;
            }
            this.index[i - 1] = this.index[i];
        }

        this.writeChecksum(this.getChecksumBlock(), checksum);
        this.cachedChecksum = checksum;
        this.persistence.flush();
        return removed;
    }

    //-------------------------------------------------------------LogicalFile--

}

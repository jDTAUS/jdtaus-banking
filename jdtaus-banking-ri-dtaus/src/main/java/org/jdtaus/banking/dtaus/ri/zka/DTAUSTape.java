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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.jdtaus.banking.AlphaNumericText27;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.Kontonummer;
import org.jdtaus.banking.Referenznummer10;
import org.jdtaus.banking.Referenznummer11;
import org.jdtaus.banking.Textschluessel;
import org.jdtaus.banking.dtaus.Checksum;
import org.jdtaus.banking.dtaus.CorruptedException;
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.LogicalFileType;
import org.jdtaus.banking.dtaus.PhysicalFileFactory;
import org.jdtaus.banking.dtaus.Transaction;
import org.jdtaus.banking.dtaus.spi.Fields;
import org.jdtaus.banking.messages.IllegalDataMessage;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.logging.spi.Logger;
import org.jdtaus.core.text.Message;

/**
 * Anlage 3.1.2 DTAUS: Zahlungsverkehrssammelauftrag Magnetbandformat.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public final class DTAUSTape extends AbstractLogicalFile
{

    /**
     * Index = A Datensatz-Feld - 1,
     * Wert = Offset relativ zum Anfang des Satzabschnittes.
     */
    protected static final int[] ARECORD_OFFSETS =
    {
        0, 2, 4, 5, 7, 12, 17, 44, 48, 52, 58, 68, 83, 91, 149
    };

    /**
     * Index = A Datensatz-Feld - 1,
     * Wert = Länge des Feldes in Byte.
     */
    protected static final int[] ARECORD_LENGTH =
    {
        2, 2, 1, 2, 5, 5, 27, 4, 4, 6, 10, 15, 8, 58, 1
    };

    /**
     * Index = E Datensatz-Feld - 1,
     * Wert = Offset relativ zum Anfang des Satzabschnittes.
     */
    protected static final int[] ERECORD_OFFSETS =
    {
        0, 2, 4, 5, 10, 14, 21, 30, 39, 46
    };

    /**
     * Index = E Datensatz-Feld -1,
     * Wert = Länge des Feldes in Byte.
     */
    protected static final int[] ERECORD_LENGTH =
    {
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
    protected static final int[] CRECORD_OFFSETS1 =
    {
        0, 2, 4, 5, 10, 15, 21, 27, 34, 35, 37, 38, 44, 49, 55, 61, 64, 91, 118, 145, 146, 148
    };

    /**
     * Index = C Datensatz-Feld - 1 (erster Satzabschnitt),
     * Wert = Länge des Feldes in Byte.
     */
    protected static final int[] CRECORD_LENGTH1 =
    {
        2, 2, 1, 5, 5, 6, 6, 7, 1, 2, 1, 6, 5, 6, 6, 3, 27, 27, 27, 1, 2, 2
    };

    /**
     * Index = Index Erweiterungsteil,
     * Wert = Offset des Typefelds des Erweiterungsteils relativ zum Anfang der Erweiterungsteile der Transaktion.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_TYPEOFFSET =
    {
        0, 29, 58, 87, 116, 145, 174, 203, 232, 261, 290, 319, 348, 377, 406
    };

    /**
     * Index = Index Erweiterungsteil,
     * Wert = Offset des Wertfelds des Erweiterungsteils relativ zum Anfang der Erweiterungsteile der Transaktion.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_VALUEOFFSET =
    {
        2, 31, 60, 89, 118, 147, 176, 205, 234, 263, 292, 321, 350, 379, 408
    };

    /**
     * Index = Index Erweiterungsteil,
     * Wert = Feld-Konstante für das Typen-Feld des Erweiterungsteils.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_TYPEFIELD =
    {
        Fields.FIELD_C19, Fields.FIELD_C21, Fields.FIELD_C23, Fields.FIELD_C25, Fields.FIELD_C27, Fields.FIELD_C30,
        Fields.FIELD_C32, Fields.FIELD_C34, Fields.FIELD_C36, Fields.FIELD_C38, Fields.FIELD_C41, Fields.FIELD_C43,
        Fields.FIELD_C45, Fields.FIELD_C47, Fields.FIELD_C48, Fields.FIELD_C51, Fields.FIELD_C53, Fields.FIELD_C55,
        Fields.FIELD_C57, Fields.FIELD_C59
    };

    /**
     * Index = Index Erweiterungsteil,
     * Wert = Feld-Konstante für das Werte-Feld des Erweiterungsteils.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_VALUEFIELD =
    {
        Fields.FIELD_C20, Fields.FIELD_C22, Fields.FIELD_C24, Fields.FIELD_C26, Fields.FIELD_C28, Fields.FIELD_C31,
        Fields.FIELD_C33, Fields.FIELD_C35, Fields.FIELD_C37, Fields.FIELD_C39, Fields.FIELD_C42, Fields.FIELD_C44,
        Fields.FIELD_C46, Fields.FIELD_C48, Fields.FIELD_C49, Fields.FIELD_C52, Fields.FIELD_C54, Fields.FIELD_C56,
        Fields.FIELD_C58
    };

    private final Calendar myCalendar;

    /** Erzeugt eine neue {@code DTAUSTape} Instanz. */
    public DTAUSTape()
    {
        super();
        this.myCalendar = Calendar.getInstance( Locale.GERMANY );
        this.myCalendar.setLenient( false );
    }

    protected char getBlockType( final long position ) throws IOException
    {
        // Feld 2
        final AlphaNumericText27 txt =
            this.readAlphaNumeric( Fields.FIELD_A2, position + ARECORD_OFFSETS[2], ARECORD_LENGTH[2], ENCODING_EBCDI );

        char ret = '?';

        if ( txt != null )
        {
            if ( txt.length() != 1 )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position + ARECORD_OFFSETS[2] );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_A2, IllegalDataMessage.TYPE_CONSTANT, position + ARECORD_OFFSETS[2],
                        txt.format() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                ret = txt.charAt( 0 );
            }
        }

        return ret;
    }

    protected int byteCount( final Transaction transaction )
    {
        int extCount = transaction.getDescriptions().length > 0 ? transaction.getDescriptions().length - 1 : 0;
        if ( transaction.getExecutiveExt() != null )
        {
            extCount++;
        }
        if ( transaction.getTargetExt() != null )
        {
            extCount++;
        }

        return this.getBlockSize() + extCount * CRECORD_EXT_LENGTH;
    }

    public Header readHeader() throws IOException
    {
        long num;
        Long Num;
        AlphaNumericText27 txt;
        LogicalFileType label = null;
        final Header ret = new Header();

        // Feld 1
        num = this.readNumberBinary(
            Fields.FIELD_A1, this.getHeaderPosition() + ARECORD_OFFSETS[0], ARECORD_LENGTH[0] );

        if ( num != this.getBlockSize() )
        {
            if ( ThreadLocalMessages.isErrorsEnabled() )
            {
                throw new CorruptedException( this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[0] );
            }
            else
            {
                final Message msg = new IllegalDataMessage(
                    Fields.FIELD_A1, IllegalDataMessage.TYPE_CONSTANT, this.getHeaderPosition() + ARECORD_OFFSETS[0],
                    Long.toString( num ) );

                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        // Feld 2
        txt = this.readAlphaNumeric(
            Fields.FIELD_A2, this.getHeaderPosition() + ARECORD_OFFSETS[2], ARECORD_LENGTH[2], ENCODING_EBCDI );

        if ( txt != null && ( txt.length() != 1 || txt.charAt( 0 ) != 'A' ) )
        {
            if ( ThreadLocalMessages.isErrorsEnabled() )
            {
                throw new CorruptedException( this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[0] );
            }
            else
            {
                final Message msg = new IllegalDataMessage(
                    Fields.FIELD_A1, IllegalDataMessage.TYPE_CONSTANT, this.getHeaderPosition() + ARECORD_OFFSETS[0],
                    txt.format() );

                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        // Feld 3
        txt = this.readAlphaNumeric(
            Fields.FIELD_A3, this.getHeaderPosition() + ARECORD_OFFSETS[3], ARECORD_LENGTH[3], ENCODING_EBCDI );

        ret.setType( null );

        if ( txt != null )
        {
            label = LogicalFileType.valueOf( txt.format() );
            if ( label == null )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException(
                        this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[3] );

                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_A3, IllegalDataMessage.TYPE_FILETYPE,
                        this.getHeaderPosition() + ARECORD_OFFSETS[3], txt.format() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                ret.setType( label );
            }
        }

        // Feld 4
        num = this.readNumberPackedPositive(
            Fields.FIELD_A4, this.getHeaderPosition() + ARECORD_OFFSETS[4], ARECORD_LENGTH[4], true );

        ret.setBank( null );
        if ( num != NO_NUMBER )
        {
            if ( !Bankleitzahl.checkBankleitzahl( new Long( num ) ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException(
                        this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[4] );

                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_A4, IllegalDataMessage.TYPE_BANKLEITZAHL,
                        this.getHeaderPosition() + ARECORD_OFFSETS[4], Long.toString( num ) );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                ret.setBank( Bankleitzahl.valueOf( new Long( num ) ) );
            }
        }

        // Feld 5
        // Nur belegt wenn Absender Kreditinistitut ist, sonst 0.
        num = this.readNumberPackedPositive(
            Fields.FIELD_A5, this.getHeaderPosition() + ARECORD_OFFSETS[5], ARECORD_LENGTH[5], true );

        ret.setBankData( null );
        if ( num != NO_NUMBER )
        {
            if ( label != null && label.isSendByBank() )
            {
                if ( !Bankleitzahl.checkBankleitzahl( new Long( num ) ) )
                {
                    if ( ThreadLocalMessages.isErrorsEnabled() )
                    {
                        throw new CorruptedException(
                            this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[5] );

                    }
                    else
                    {
                        final Message msg = new IllegalDataMessage(
                            Fields.FIELD_A5, IllegalDataMessage.TYPE_BANKLEITZAHL,
                            this.getHeaderPosition() + ARECORD_OFFSETS[5], Long.toString( num ) );

                        ThreadLocalMessages.getMessages().addMessage( msg );
                    }
                }
                else
                {
                    ret.setBankData( Bankleitzahl.valueOf( new Long( num ) ) );
                }
            }
        }

        // Feld 6
        txt = this.readAlphaNumeric(
            Fields.FIELD_A6, this.getHeaderPosition() + ARECORD_OFFSETS[6], ARECORD_LENGTH[6], ENCODING_EBCDI );

        ret.setCustomer( txt );

        // Feld 7
        num = this.readNumberPackedPositive(
            Fields.FIELD_A7, this.getHeaderPosition() + ARECORD_OFFSETS[7], ARECORD_LENGTH[7], true );

        final Date createDate;
        if ( num != NO_NUMBER )
        {
            this.myCalendar.clear();
            int cal = (int) Math.floor( num / EXP10[4] );
            num -= ( cal * EXP10[4] );
            this.myCalendar.set( Calendar.DAY_OF_MONTH, cal );
            cal = (int) Math.floor( num / EXP10[2] );
            num -= ( cal * EXP10[2] );
            this.myCalendar.set( Calendar.MONTH, cal - 1 );
            num = num <= 79L ? 2000L + num : 1900L + num;
            this.myCalendar.set( Calendar.YEAR, (int) num );
            createDate = this.myCalendar.getTime();
            if ( !this.checkDate( createDate ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException(
                        this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[7] );

                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_A7, IllegalDataMessage.TYPE_SHORTDATE,
                        this.getHeaderPosition() + ARECORD_OFFSETS[7], Long.toString( num ) );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
        }
        else
        {
            createDate = null;
        }

        // Feld 8
        // Nur belegt wenn Absender Kreditinistitut ist, sonst leer.
        txt = this.readAlphaNumeric(
            Fields.FIELD_A8, this.getHeaderPosition() + ARECORD_OFFSETS[8], ARECORD_LENGTH[8], ENCODING_EBCDI );

        // Feld 9
        num = this.readNumberPackedPositive(
            Fields.FIELD_A9, this.getHeaderPosition() + ARECORD_OFFSETS[9], ARECORD_LENGTH[9], true );

        ret.setAccount( null );
        if ( num != NO_NUMBER )
        {
            if ( !Kontonummer.checkKontonummer( new Long( num ) ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException(
                        this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[9] );

                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_A9, IllegalDataMessage.TYPE_KONTONUMMER,
                        this.getHeaderPosition() + ARECORD_OFFSETS[9], Long.toString( num ) );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                ret.setAccount( Kontonummer.valueOf( new Long( num ) ) );
            }
        }

        // Feld 10
        Num = this.readNumber(
            Fields.FIELD_A10, this.getHeaderPosition() + ARECORD_OFFSETS[10], ARECORD_LENGTH[10], ENCODING_EBCDI );

        num = Num.longValue();
        if ( num != NO_NUMBER )
        {
            if ( !Referenznummer10.checkReferenznummer10( Num ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException(
                        this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[10] );

                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_A10, IllegalDataMessage.TYPE_REFERENZNUMMER,
                        this.getHeaderPosition() + ARECORD_OFFSETS[10], Num.toString() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                ret.setReference( Referenznummer10.valueOf( Num ) );
            }
        }

        // Feld 11b
        final Date executionDate = this.readLongDate(
            Fields.FIELD_A11B, this.getHeaderPosition() + ARECORD_OFFSETS[12], ENCODING_EBCDI );

        ret.setCreateDate( createDate );
        ret.setExecutionDate( executionDate );

        // Feld 12
        txt = this.readAlphaNumeric(
            Fields.FIELD_A12, this.getHeaderPosition() + ARECORD_OFFSETS[14], ARECORD_LENGTH[14], ENCODING_EBCDI );

        ret.setCurrency( null );
        if ( txt != null )
        {
            if ( txt.length() != 1 )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException(
                        this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[14] );

                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_A12, IllegalDataMessage.TYPE_ALPHA_NUMERIC,
                        this.getHeaderPosition() + ARECORD_OFFSETS[14], txt.format() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                final char c = txt.charAt( 0 );
                final Currency cur = this.getCurrencyMapper().getDtausCurrency( c, createDate );
                if ( cur == null )
                {
                    if ( ThreadLocalMessages.isErrorsEnabled() )
                    {
                        throw new CorruptedException(
                            this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[14] );

                    }
                    else
                    {
                        final Message msg = new IllegalDataMessage(
                            Fields.FIELD_A12, IllegalDataMessage.TYPE_CURRENCY,
                            this.getHeaderPosition() + ARECORD_OFFSETS[14], Character.toString( c ) );

                        ThreadLocalMessages.getMessages().addMessage( msg );
                    }
                }

                ret.setCurrency( cur );
            }
        }

        return ret;
    }

    protected void writeHeader( final Header header ) throws IOException
    {
        final LogicalFileType label;
        final boolean isBank;
        long num = 0L;
        int cal = 0;
        int yy;

        label = header.getType();
        isBank = label.isSendByBank();

        // Feld 1
        this.writeNumberBinary(
            Fields.FIELD_A1, this.getHeaderPosition() + ARECORD_OFFSETS[0], ARECORD_LENGTH[0], this.getBlockSize() );

        // Feld 1b
        // TODO -1
        this.writeNumberBinary( -1, this.getHeaderPosition() + ARECORD_OFFSETS[1], ARECORD_LENGTH[1], 0L );

        // Feld 2
        this.writeAlphaNumeric(
            Fields.FIELD_A2, this.getHeaderPosition() + ARECORD_OFFSETS[2], ARECORD_LENGTH[2], "A", ENCODING_EBCDI );

        // Feld 3
        this.writeAlphaNumeric(
            Fields.FIELD_A3, this.getHeaderPosition() + ARECORD_OFFSETS[3], ARECORD_LENGTH[3], label.getCode(),
            ENCODING_EBCDI );

        // Feld 4
        this.writeNumberPackedPositive(
            Fields.FIELD_A4, this.getHeaderPosition() + ARECORD_OFFSETS[4], ARECORD_LENGTH[4],
            header.getBank().intValue(), true );

        // Feld 5
        this.writeNumberPackedPositive(
            Fields.FIELD_A5, this.getHeaderPosition() + ARECORD_OFFSETS[5], ARECORD_LENGTH[5],
            ( isBank && header.getBankData() != null ? header.getBankData().intValue() : 0 ), true );

        // Feld 6
        this.writeAlphaNumeric(
            Fields.FIELD_A6, this.getHeaderPosition() + ARECORD_OFFSETS[6], ARECORD_LENGTH[6],
            header.getCustomer().format(), ENCODING_EBCDI );

        // Feld 7
        this.myCalendar.clear();
        this.myCalendar.setTime( header.getCreateDate() );
        num = this.myCalendar.get( Calendar.DAY_OF_MONTH ) * EXP10[4];
        num += ( this.myCalendar.get( Calendar.MONTH ) + 1 ) * EXP10[2];
        cal = this.myCalendar.get( Calendar.YEAR );
        yy = (int) Math.floor( cal / 100.00D );
        cal -= yy * 100.00D;
        num += cal;

        this.writeNumberPackedPositive(
            Fields.FIELD_A7, this.getHeaderPosition() + ARECORD_OFFSETS[7], ARECORD_LENGTH[7], num, true );

        // Feld 8
        this.writeAlphaNumeric(
            Fields.FIELD_A8, this.getHeaderPosition() + ARECORD_OFFSETS[8], ARECORD_LENGTH[8], "", ENCODING_EBCDI );

        // Feld 9
        this.writeNumberPackedPositive(
            Fields.FIELD_A9, this.getHeaderPosition() + ARECORD_OFFSETS[9], ARECORD_LENGTH[9],
            header.getAccount().longValue(), true );

        // Feld 10
        this.writeNumber(
            Fields.FIELD_A10, this.getHeaderPosition() + ARECORD_OFFSETS[10], ARECORD_LENGTH[10],
            ( header.getReference() != null ? header.getReference().longValue() : 0L ), ENCODING_EBCDI );

        // Feld 11a
        this.writeAlphaNumeric(
            Fields.FIELD_A11A, this.getHeaderPosition() + ARECORD_OFFSETS[11], ARECORD_LENGTH[11], "", ENCODING_EBCDI );

        // Feld 11b
        this.writeLongDate(
            Fields.FIELD_A11B, this.getHeaderPosition() + ARECORD_OFFSETS[12], header.getExecutionDate(),
            ENCODING_EBCDI );

        // Feld 11c
        this.writeAlphaNumeric(
            Fields.FIELD_A11C, this.getHeaderPosition() + ARECORD_OFFSETS[13], ARECORD_LENGTH[13], "", ENCODING_EBCDI );

        // Feld 12
        this.writeAlphaNumeric(
            Fields.FIELD_A12, this.getHeaderPosition() + ARECORD_OFFSETS[14], ARECORD_LENGTH[14],
            Character.toString( this.getCurrencyMapper().getDtausCode( header.getCurrency(), header.getCreateDate() ) ),
            ENCODING_EBCDI );

    }

    protected Checksum readChecksum() throws IOException
    {
        long num;
        final AlphaNumericText27 txt;
        final Checksum checksum = new Checksum();

        // Feld 1
        num = this.readNumberBinary(
            Fields.FIELD_E1, this.getChecksumPosition() + ERECORD_OFFSETS[0], ERECORD_LENGTH[0] );

        if ( num != this.getBlockSize() )
        {
            if ( ThreadLocalMessages.isErrorsEnabled() )
            {
                throw new CorruptedException(
                    this.getImplementation(), this.getChecksumPosition() + ERECORD_OFFSETS[0] );

            }
            else
            {
                final Message msg = new IllegalDataMessage(
                    Fields.FIELD_E1, IllegalDataMessage.TYPE_CONSTANT,
                    this.getChecksumPosition() + ERECORD_OFFSETS[0], Long.toString( num ) );

                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        // Feld 2
        txt = this.readAlphaNumeric(
            Fields.FIELD_E2, this.getChecksumPosition() + ERECORD_OFFSETS[2], ERECORD_LENGTH[2], ENCODING_EBCDI );

        if ( txt != null && ( txt.length() != 1 || txt.charAt( 0 ) != 'E' ) )
        {
            if ( ThreadLocalMessages.isErrorsEnabled() )
            {
                throw new CorruptedException(
                    this.getImplementation(), this.getChecksumPosition() + ERECORD_OFFSETS[2] );

            }
            else
            {
                final Message msg = new IllegalDataMessage(
                    Fields.FIELD_E2, IllegalDataMessage.TYPE_CONSTANT, this.getChecksumPosition() + ERECORD_OFFSETS[2],
                    txt.format() );

                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        // Feld 4
        num = this.readNumberPackedPositive(
            Fields.FIELD_E4, this.getChecksumPosition() + ERECORD_OFFSETS[4], ERECORD_LENGTH[4], true );

        if ( num != NO_NUMBER )
        {
            checksum.setTransactionCount( (int) num );
        }

        // Feld 6
        num = this.readNumberPackedPositive(
            Fields.FIELD_E6, this.getChecksumPosition() + ERECORD_OFFSETS[6], ERECORD_LENGTH[6], true );

        if ( num != NO_NUMBER )
        {
            checksum.setSumTargetAccount( num );
        }

        // Feld 7
        num = this.readNumberPackedPositive(
            Fields.FIELD_E7, this.getChecksumPosition() + ERECORD_OFFSETS[7], ERECORD_LENGTH[7], true );

        if ( num != NO_NUMBER )
        {
            checksum.setSumTargetBank( num );
        }

        // Feld 8
        num = this.readNumberPackedPositive(
            Fields.FIELD_E8, this.getChecksumPosition() + ERECORD_OFFSETS[8], ERECORD_LENGTH[8], true );

        if ( num != NO_NUMBER )
        {
            checksum.setSumAmount( num );
        }

        return checksum;
    }

    protected void writeChecksum( final Checksum checksum ) throws IOException
    {
        // Feld 1
        this.writeNumberBinary(
            Fields.FIELD_E1, this.getChecksumPosition() + ERECORD_OFFSETS[0], ERECORD_LENGTH[0], this.getBlockSize() );

        // Feld 1b
        // TODO -1
        this.writeNumberBinary( -1, this.getChecksumPosition() + ERECORD_OFFSETS[1], ERECORD_LENGTH[1], 0L );

        // Feld 2
        this.writeAlphaNumeric(
            Fields.FIELD_E2, this.getChecksumPosition() + ERECORD_OFFSETS[2], ERECORD_LENGTH[2], "E", ENCODING_EBCDI );

        // Feld 3
        this.writeAlphaNumeric(
            Fields.FIELD_E3, this.getChecksumPosition() + ERECORD_OFFSETS[3], ERECORD_LENGTH[3], "", ENCODING_EBCDI );

        // Feld 4
        this.writeNumberPackedPositive(
            Fields.FIELD_E4, this.getChecksumPosition() + ERECORD_OFFSETS[4], ERECORD_LENGTH[4],
            checksum.getTransactionCount(), true );

        // Feld 5
        this.writeNumberPackedPositive(
            Fields.FIELD_E5, this.getChecksumPosition() + ERECORD_OFFSETS[5], ERECORD_LENGTH[5], 0L, false );

        // Feld 6
        this.writeNumberPackedPositive(
            Fields.FIELD_E6, this.getChecksumPosition() + ERECORD_OFFSETS[6], ERECORD_LENGTH[6],
            checksum.getSumTargetAccount(), true );

        // Feld 7
        this.writeNumberPackedPositive(
            Fields.FIELD_E7, this.getChecksumPosition() + ERECORD_OFFSETS[7], ERECORD_LENGTH[7],
            checksum.getSumTargetBank(), true );

        // Feld 8
        this.writeNumberPackedPositive(
            Fields.FIELD_E8, this.getChecksumPosition() + ERECORD_OFFSETS[8], ERECORD_LENGTH[8],
            checksum.getSumAmount(), true );

        // Feld 9
        this.writeAlphaNumeric(
            Fields.FIELD_E9, this.getChecksumPosition() + ERECORD_OFFSETS[9], ERECORD_LENGTH[9], "", ENCODING_EBCDI );

    }

    protected Transaction readTransaction( final long position, final Transaction transaction ) throws IOException
    {
        long num;
        Long Num;
        AlphaNumericText27 txt;
        long keyType;
        final long extCount;
        final Currency cur;
        final Textschluessel type;
        final List desc = new ArrayList( 14 );

        transaction.setExecutiveExt( null );
        transaction.setTargetExt( null );
        // Konstanter Teil - 1. Satzabschnitt - Feld 18
        extCount = this.readNumberPackedPositive(
            Fields.FIELD_C18, position + CRECORD_OFFSETS1[21], CRECORD_LENGTH1[21], true );

        if ( extCount != NO_NUMBER && extCount > this.getMaximumExtensionCount() )
        {
            if ( ThreadLocalMessages.isErrorsEnabled() )
            {
                throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[21] );
            }
            else
            {
                final Message msg = new IllegalDataMessage(
                    Fields.FIELD_C18, IllegalDataMessage.TYPE_CONSTANT, position + CRECORD_OFFSETS1[21],
                    Long.toString( extCount ) );

                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 1
        num = this.readNumberBinary( Fields.FIELD_C1, position + CRECORD_OFFSETS1[0], CRECORD_LENGTH1[0] );

        if ( extCount != NO_NUMBER && num != CRECORD_CONST_LENGTH + extCount * CRECORD_EXT_LENGTH )
        {
            if ( ThreadLocalMessages.isErrorsEnabled() )
            {
                throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[0] );
            }
            else
            {
                final Message msg = new IllegalDataMessage(
                    Fields.FIELD_C1, IllegalDataMessage.TYPE_NUMERIC, position + CRECORD_OFFSETS1[0],
                    Long.toString( num ) );

                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 2
        txt = this.readAlphaNumeric(
            Fields.FIELD_C2, position + CRECORD_OFFSETS1[2], CRECORD_LENGTH1[2], ENCODING_EBCDI );

        if ( txt != null && ( txt.length() != 1 || txt.charAt( 0 ) != 'C' ) )
        {
            if ( ThreadLocalMessages.isErrorsEnabled() )
            {
                throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[2] );
            }
            else
            {
                final Message msg = new IllegalDataMessage(
                    Fields.FIELD_C2, IllegalDataMessage.TYPE_CONSTANT, position + CRECORD_OFFSETS1[2], txt.format() );

                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 3
        num = this.readNumberPackedPositive(
            Fields.FIELD_C3, position + CRECORD_OFFSETS1[3], CRECORD_LENGTH1[3], true );

        transaction.setPrimaryBank( null );
        if ( num != NO_NUMBER && num != 0L )
        {
            if ( !Bankleitzahl.checkBankleitzahl( new Long( num ) ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[3] );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_C3, IllegalDataMessage.TYPE_BANKLEITZAHL, position + CRECORD_OFFSETS1[3],
                        Long.toString( num ) );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                transaction.setPrimaryBank( Bankleitzahl.valueOf( new Long( num ) ) );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 4
        num = this.readNumberPackedPositive(
            Fields.FIELD_C4, position + CRECORD_OFFSETS1[4], CRECORD_LENGTH1[4], true );

        transaction.setTargetBank( null );
        if ( num != NO_NUMBER )
        {
            if ( !Bankleitzahl.checkBankleitzahl( new Long( num ) ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[4] );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_C4, IllegalDataMessage.TYPE_BANKLEITZAHL, position + CRECORD_OFFSETS1[4],
                        Long.toString( num ) );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                transaction.setTargetBank( Bankleitzahl.valueOf( new Long( num ) ) );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 5
        num = this.readNumberPackedPositive(
            Fields.FIELD_C5, position + CRECORD_OFFSETS1[5], CRECORD_LENGTH1[5], true );

        transaction.setTargetAccount( null );
        if ( num != NO_NUMBER )
        {
            if ( !Kontonummer.checkKontonummer( new Long( num ) ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[5] );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_C5, IllegalDataMessage.TYPE_KONTONUMMER, position + CRECORD_OFFSETS1[5],
                        Long.toString( num ) );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                transaction.setTargetAccount( Kontonummer.valueOf( new Long( num ) ) );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 6a
        num = this.readNumberPackedPositive(
            Fields.FIELD_C6A, position + CRECORD_OFFSETS1[6], CRECORD_LENGTH1[6], false );

        transaction.setReference( null );
        if ( num != NO_NUMBER )
        {
            if ( !Referenznummer11.checkReferenznummer11( new Long( num ) ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[6] );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_C6A, IllegalDataMessage.TYPE_REFERENZNUMMER, position + CRECORD_OFFSETS1[6],
                        Long.toString( num ) );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                transaction.setReference( Referenznummer11.valueOf( new Long( num ) ) );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 6b
        //num = this.readNumberPackedPositive(block,
        //    DTAUSTape.CRECORD_OFFSETS1[7], DTAUSTape.CRECORD_LENGTH1[7], true);

        // Konstanter Teil - Satzaschnitt 1 - Feld 7a
        keyType = this.readNumberPackedPositive(
            Fields.FIELD_C7A, position + CRECORD_OFFSETS1[8], CRECORD_LENGTH1[8], false );

        // Konstanter Teil - Satzaschnitt 1 - Feld 7b
        num = this.readNumberPackedPositive(
            Fields.FIELD_C7B, position + CRECORD_OFFSETS1[9], CRECORD_LENGTH1[9], true );

        transaction.setType( null );
        if ( num != NO_NUMBER && keyType != NO_NUMBER )
        {
            type = this.getTextschluesselVerzeichnis().getTextschluessel(
                (int) keyType, (int) num, this.getHeader().getCreateDate() );

            if ( type == null )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[8] );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_C7A, IllegalDataMessage.TYPE_TEXTSCHLUESSEL,
                        position + CRECORD_OFFSETS1[8], Long.toString( keyType ) + Long.toString( num ) );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                transaction.setType( type );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 10
        num = this.readNumberPackedPositive(
            Fields.FIELD_C10, position + CRECORD_OFFSETS1[12], CRECORD_LENGTH1[12], true );

        transaction.setExecutiveBank( null );
        if ( num != NO_NUMBER )
        {
            if ( !Bankleitzahl.checkBankleitzahl( new Long( num ) ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[12] );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_C10, IllegalDataMessage.TYPE_BANKLEITZAHL, position + CRECORD_OFFSETS1[12],
                        Long.toString( num ) );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                transaction.setExecutiveBank( Bankleitzahl.valueOf( new Long( num ) ) );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 11
        num = this.readNumberPackedPositive(
            Fields.FIELD_C11, position + CRECORD_OFFSETS1[13], CRECORD_LENGTH1[13], true );

        transaction.setExecutiveAccount( null );
        if ( num != NO_NUMBER )
        {
            if ( !Kontonummer.checkKontonummer( new Long( num ) ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[13] );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_C11, IllegalDataMessage.TYPE_KONTONUMMER, position + CRECORD_OFFSETS1[13],
                        Long.toString( num ) );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                transaction.setExecutiveAccount(
                    Kontonummer.valueOf( new Long( num ) ) );

            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 12
        num = this.readNumberPackedPositive(
            Fields.FIELD_C12, position + CRECORD_OFFSETS1[14], CRECORD_LENGTH1[14], true );

        transaction.setAmount( num != NO_NUMBER ? BigInteger.valueOf( num ) : null );

        // Konstanter Teil - Satzaschnitt 1 - Feld 14
        txt = this.readAlphaNumeric(
            Fields.FIELD_C14, position + CRECORD_OFFSETS1[16], CRECORD_LENGTH1[16], ENCODING_EBCDI );

        transaction.setTargetName( txt );

        // Konstanter Teil - Satzaschnitt 1 - Feld 15
        txt = this.readAlphaNumeric(
            Fields.FIELD_C15, position + CRECORD_OFFSETS1[17], CRECORD_LENGTH1[17], ENCODING_EBCDI );

        transaction.setExecutiveName( txt );

        // Konstanter Teil - Satzaschnitt 1 - Feld 16
        txt = this.readAlphaNumeric(
            Fields.FIELD_C16, position + CRECORD_OFFSETS1[18], CRECORD_LENGTH1[18], ENCODING_EBCDI );

        if ( txt != null )
        {
            desc.add( txt );
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 17a
        txt = this.readAlphaNumeric(
            Fields.FIELD_C17A, position + CRECORD_OFFSETS1[19], CRECORD_LENGTH1[19], ENCODING_EBCDI );

        if ( txt != null )
        {
            if ( txt.length() != 1 )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[19] );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_C17A, IllegalDataMessage.TYPE_CURRENCY,
                        position + CRECORD_OFFSETS1[19], txt.format() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                final char c = txt.charAt( 0 );
                cur = this.getCurrencyMapper().getDtausCurrency( c, this.getHeader().getCreateDate() );

                if ( cur == null )
                {
                    if ( ThreadLocalMessages.isErrorsEnabled() )
                    {
                        throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[19] );
                    }
                    else
                    {
                        final Message msg = new IllegalDataMessage(
                            Fields.FIELD_A12, IllegalDataMessage.TYPE_CURRENCY, position + CRECORD_OFFSETS1[19],
                            Character.toString( c ) );

                        ThreadLocalMessages.getMessages().addMessage( msg );
                    }
                }

                transaction.setCurrency( cur );
            }
        }

        //if(header.getLabel().isBank()) {
        // Konstanter Teil - Satzaschnitt 1 - Feld 8
        //    num = this.readNumber(block, DTAUSTape.CRECORD_OFFSETS1[7],
        //        DTAUSTape.CRECORD_LENGTH1[7]);

        //    transaction.set
        //
        //}

        // Erweiterungsteile.
        for ( int search = 0; search < extCount && extCount != NO_NUMBER; search++ )
        {
            Num = this.readNumber(
                CRECORD_EXTINDEX_TO_TYPEFIELD[search],
                position + this.getBlockSize() + CRECORD_EXTINDEX_TO_TYPEOFFSET[search], 2, ENCODING_EBCDI );

            txt = this.readAlphaNumeric(
                CRECORD_EXTINDEX_TO_VALUEFIELD[search],
                position + this.getBlockSize() + CRECORD_EXTINDEX_TO_VALUEOFFSET[search], 27, ENCODING_EBCDI );

            num = Num.longValue();
            if ( num == 1L )
            {
                if ( transaction.getTargetExt() != null )
                {
                    if ( ThreadLocalMessages.isErrorsEnabled() )
                    {
                        throw new CorruptedException(
                            this.getImplementation(),
                            position + this.getBlockSize() + CRECORD_EXTINDEX_TO_TYPEOFFSET[search] );

                    }
                    else
                    {
                        final Message msg = new IllegalDataMessage(
                            CRECORD_EXTINDEX_TO_TYPEFIELD[search], IllegalDataMessage.TYPE_NUMERIC,
                            position + this.getBlockSize() + CRECORD_EXTINDEX_TO_TYPEOFFSET[search], Num.toString() );

                        ThreadLocalMessages.getMessages().addMessage( msg );
                    }
                }
                else
                {
                    transaction.setTargetExt( txt );
                }
            }
            else if ( num == 2L )
            {
                if ( txt != null )
                {
                    desc.add( txt );
                }
            }
            else if ( num == 3L )
            {
                if ( transaction.getExecutiveExt() != null )
                {
                    if ( ThreadLocalMessages.isErrorsEnabled() )
                    {
                        throw new CorruptedException(
                            this.getImplementation(),
                            position + this.getBlockSize() + CRECORD_EXTINDEX_TO_TYPEOFFSET[search] );

                    }
                    else
                    {
                        final Message msg = new IllegalDataMessage(
                            CRECORD_EXTINDEX_TO_TYPEFIELD[search], IllegalDataMessage.TYPE_NUMERIC,
                            position + this.getBlockSize() + CRECORD_EXTINDEX_TO_TYPEOFFSET[search], Num.toString() );

                        ThreadLocalMessages.getMessages().addMessage( msg );
                    }
                }
                else
                {
                    transaction.setExecutiveExt( txt );
                }
            }
            else if ( num != NO_NUMBER )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException(
                        this.getImplementation(),
                        position + this.getBlockSize() + CRECORD_EXTINDEX_TO_TYPEOFFSET[search] );

                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        CRECORD_EXTINDEX_TO_TYPEFIELD[search], IllegalDataMessage.TYPE_NUMERIC,
                        position + this.getBlockSize() + CRECORD_EXTINDEX_TO_TYPEOFFSET[search], Num.toString() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
        }

        transaction.setDescriptions( (AlphaNumericText27[]) desc.toArray( new AlphaNumericText27[ desc.size() ] ) );
        return transaction;
    }

    protected void writeTransaction( final long position, final Transaction transaction ) throws IOException
    {
        int i;
        int extIndex;
        AlphaNumericText27 txt;
        final Textschluessel type = transaction.getType();
        final AlphaNumericText27[] desc = transaction.getDescriptions();
        final int descCount;
        int extCount = desc.length > 0 ? desc.length - 1 : 0;

        if ( transaction.getExecutiveExt() != null )
        {
            extCount++;
        }
        if ( transaction.getTargetExt() != null )
        {
            extCount++;
        }
        // Konstanter Teil - 1. Satzabschnitt - Feld 1a
        this.writeNumberBinary(
            Fields.FIELD_C1, position + CRECORD_OFFSETS1[0], CRECORD_LENGTH1[0],
            CRECORD_CONST_LENGTH + extCount * CRECORD_EXT_LENGTH );

        // Konstanter Teil - 1. Satzabschnitt - Feld 1b
        this.writeNumberBinary( Fields.FIELD_C1, position + CRECORD_OFFSETS1[1], CRECORD_LENGTH1[1], 0L );

        // Konstanter Teil - 1. Satzabschnitt - Feld 2
        this.writeAlphaNumeric(
            Fields.FIELD_C2, position + CRECORD_OFFSETS1[2], CRECORD_LENGTH1[2], "C", ENCODING_EBCDI );

        // Konstanter Teil - 1. Satzabschnitt - Feld 3
        this.writeNumberPackedPositive( Fields.FIELD_C3, position + CRECORD_OFFSETS1[3], CRECORD_LENGTH1[3],
                                        transaction.getPrimaryBank() != null
                                        ? transaction.getPrimaryBank().intValue() : 0, true );

        // Konstanter Teil - 1. Satzabschnitt - Feld 4
        this.writeNumberPackedPositive( Fields.FIELD_C4, position + CRECORD_OFFSETS1[4], CRECORD_LENGTH1[4],
                                        transaction.getTargetBank().intValue(), true );

        // Konstanter Teil - 1. Satzabschnitt - Feld 5
        this.writeNumberPackedPositive( Fields.FIELD_C5, position + CRECORD_OFFSETS1[5], CRECORD_LENGTH1[5],
                                        transaction.getTargetAccount().longValue(), true );

        // Konstanter Teil - 1. Satzabschnitt - Feld 6a
        this.writeNumberPackedPositive( Fields.FIELD_C6A, position + CRECORD_OFFSETS1[6], CRECORD_LENGTH1[6],
                                        transaction.getReference() != null
                                        ? transaction.getReference().longValue() : 0L, false );

        // Konstanter Teil - 1. Satzabschnitt - Feld 6b
        this.writeNumberPackedPositive( Fields.FIELD_C6B, position + CRECORD_OFFSETS1[7], CRECORD_LENGTH1[7], 0L,
                                        true );

        // Konstanter Teil - 1. Satzabschnitt - Feld 7a
        this.writeNumberPackedPositive( Fields.FIELD_C7A, position + CRECORD_OFFSETS1[8], CRECORD_LENGTH1[8],
                                        type.getKey(), false );

        // Konstanter Teil - 1. Satzabschnitt - Feld 7b
        this.writeNumberPackedPositive( Fields.FIELD_C7B, position + CRECORD_OFFSETS1[9], CRECORD_LENGTH1[9],
                                        type.getExtension(), true );

        // Konstanter Teil - 1. Satzabschnitt - Feld 8
        this.writeAlphaNumeric( Fields.FIELD_C8, position + CRECORD_OFFSETS1[10], CRECORD_LENGTH1[10], "",
                                ENCODING_EBCDI );

        // Konstanter Teil - 1. Satzabschnitt - Feld 9
        this.writeNumberPackedPositive( Fields.FIELD_C9, position + CRECORD_OFFSETS1[11], CRECORD_LENGTH1[11], 0L,
                                        true );

        // Konstanter Teil - 1. Satzabschnitt - Feld 10
        this.writeNumberPackedPositive( Fields.FIELD_C10, position + CRECORD_OFFSETS1[12], CRECORD_LENGTH1[12],
                                        transaction.getExecutiveBank().intValue(), true );

        // Konstanter Teil - 1. Satzabschnitt - Feld 11
        this.writeNumberPackedPositive( Fields.FIELD_C11, position + CRECORD_OFFSETS1[13], CRECORD_LENGTH1[13],
                                        transaction.getExecutiveAccount().longValue(), true );

        // Konstanter Teil - 1. Satzabschnitt - Feld 12
        this.writeNumberPackedPositive( Fields.FIELD_C12, position + CRECORD_OFFSETS1[14], CRECORD_LENGTH1[14],
                                        transaction.getAmount().longValue(), true ); // TODO longValueExact()

        // Konstanter Teil - 1. Satzabschnitt - Feld 13
        this.writeAlphaNumeric( Fields.FIELD_C13, position + CRECORD_OFFSETS1[15], CRECORD_LENGTH1[15], "",
                                ENCODING_EBCDI );

        // Konstanter Teil - 1. Satzabschnitt - Feld 14
        this.writeAlphaNumeric( Fields.FIELD_C14, position + CRECORD_OFFSETS1[16], CRECORD_LENGTH1[16],
                                transaction.getTargetName().format(), ENCODING_EBCDI );

        // Konstanter Teil - 1. Satzabschnitt - Feld 15
        this.writeAlphaNumeric( Fields.FIELD_C15, position + CRECORD_OFFSETS1[17], CRECORD_LENGTH1[17],
                                transaction.getExecutiveName().format(), ENCODING_EBCDI );

        // Konstanter Teil - 1. Satzabschnitt - Feld 16
        this.writeAlphaNumeric( Fields.FIELD_C16, position + CRECORD_OFFSETS1[18], CRECORD_LENGTH1[18],
                                ( desc.length > 0 ? desc[0].format() : "" ), ENCODING_EBCDI );

        // Konstanter Teil - 1. Satzabschnitt - Feld 17a
        this.writeAlphaNumeric(
            Fields.FIELD_C17A, position + CRECORD_OFFSETS1[19], CRECORD_LENGTH1[19],
            Character.toString( this.getCurrencyMapper().getDtausCode(
            transaction.getCurrency(), this.getHeader().getCreateDate() ) ), ENCODING_EBCDI );

        // Konstanter Teil - 1. Satzabschnitt - Feld 17b
        this.writeAlphaNumeric( Fields.FIELD_C17B, position + CRECORD_OFFSETS1[20], CRECORD_LENGTH1[20], "",
                                ENCODING_EBCDI );

        // Konstanter Teil - 1. Satzabschnitt - Feld 18
        this.writeNumberPackedPositive( Fields.FIELD_C18, position + CRECORD_OFFSETS1[21], CRECORD_LENGTH1[21],
                                        extCount, true );

        // Erweiterungsteile.
        descCount = desc.length;
        extIndex = 0;

        if ( ( txt = transaction.getTargetExt() ) != null )
        {
            this.writeNumber(
                CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex],
                position + this.getBlockSize() + CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex], 2, 1L, ENCODING_EBCDI );

            this.writeAlphaNumeric(
                CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex],
                position + this.getBlockSize() + CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex], 27, txt.format(),
                ENCODING_EBCDI );

            extIndex++;
        }
        for ( i = 1; i < descCount; i++, extIndex++ )
        {
            this.writeNumber(
                CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex],
                position + this.getBlockSize() + CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex], 2, 2L, ENCODING_EBCDI );

            this.writeAlphaNumeric(
                CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex],
                position + this.getBlockSize() + CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex], 27, desc[i].format(),
                ENCODING_EBCDI );

        }
        if ( ( txt = transaction.getExecutiveExt() ) != null )
        {
            this.writeNumber(
                CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex],
                position + this.getBlockSize() + CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex], 2, 3L, ENCODING_EBCDI );

            this.writeAlphaNumeric(
                CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex],
                position + this.getBlockSize() + CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex], 27, txt.format(),
                ENCODING_EBCDI );

        }
    }

    protected int getBlockSize()
    {
        return PhysicalFileFactory.FORMAT_TAPE;
    }

    protected Implementation getImplementation()
    {
        return ModelFactory.getModel().getModules().getImplementation( DTAUSTape.class.getName() );
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

// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
}

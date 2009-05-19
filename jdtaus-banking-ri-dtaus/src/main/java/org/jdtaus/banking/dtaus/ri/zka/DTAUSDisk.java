/*
 *  jDTAUS Banking RI DTAUS
 *  Copyright (c) 2005 Christian Schulte
 *
 *  Christian Schulte, Haldener Strasse 72, 58095 Hagen, Germany
 *  <cs@jdtaus.org> (+49 2331 3543887)
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
import java.util.Currency;
import java.util.Date;
import java.util.List;
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
 * Anlage 3 - 1.1 DTAUS0: Zahlungsverkehrssammelauftrag Diskettenformat.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class DTAUSDisk extends AbstractLogicalFile
{

    /** Länge des konstanten Teiles eines C Datensatzes in Byte. */
    protected static final int CRECORD_CONST_LENGTH = 187;

    /** Länge eines Erweiterungsteiles in Byte. */
    protected static final int CRECORD_EXT_LENGTH = 29;

    /**
     * Index = A Datensatz-Feld - 1,
     * Wert = Offset relativ zum Anfang des Satzabschnittes.
     */
    protected static final int[] ARECORD_OFFSETS =
    {
        0, 4, 5, 7, 15, 23, 50, 56, 60, 70, 80, 95, 103, 127
    };

    /**
     * Index = A Datensatz-Feld - 1,
     * Wert = Länge des Feldes in Byte.
     */
    protected static final int[] ARECORD_LENGTH =
    {
        4, 1, 2, 8, 8, 27, 6, 4, 10, 10, 15, 8, 24, 1
    };

    /**
     * Index = E Datensatz-Feld - 1,
     * Wert = Offset relativ zum Anfang des Satzabschnittes.
     */
    protected static final int[] ERECORD_OFFSETS =
    {
        0, 4, 5, 10, 17, 30, 47, 64, 77
    };

    /**
     * Index = E Datensatz-Feld -1,
     * Wert = Länge des Feldes in Byte.
     */
    protected static final int[] ERECORD_LENGTH =
    {
        4, 1, 5, 7, 13, 17, 17, 13, 51
    };

    /**
     * Index = C Datensatz-Feld - 1,
     * Wert = Offset relativ zum ersten Satzabschnitt.
     */
    protected static final int[] CRECORD_OFFSETS1 =
    {
        0, 4, 5, 13, 21, 32, 44, 49, 50, 61, 69, 79, 90, 93, 120
    };

    /**
     * Index = C Datensatz-Feld - 1 (erster Satzabschnitt),
     * Wert = Länge des Feldes in Byte.
     */
    protected static final int[] CRECORD_LENGTH1 =
    {
        4, 1, 8, 8, 10, 11, 5, 1, 11, 8, 10, 11, 3, 27, 8
    };

    /**
     * Index = C Datensatz-Feld des zweiten Satzabschnittes - 1,
     * Wert = Offset relativ zum zweiten Satzabschnitt.
     */
    protected static final int[] CRECORD_OFFSETS2 =
    {
        0, 27, 54, 55, 57, 59, 61, 88, 90, 117
    };

    /**
     * Index = C Datensatz-Feld des zweiten Satzabschnittes - 1,
     * Wert = Länge des Feldes in Byte.
     */
    protected static final int[] CRECORD_LENGTH2 =
    {
        27, 27, 1, 2, 2, 2, 27, 2, 27, 11
    };

    /**
     * Index = C Datensatz-Feld des 3., 4., 5. und 6. Satzabschnittes - 1,
     * Wert = Offset relativ zum Anfang des 3., 4., 5. und 6. Satzabschnittes.
     */
    protected static final int[] CRECORD_OFFSETS_EXT =
    {
        0, 2, 29, 31, 58, 60, 87, 89, 116
    };

    /**
     * Index = C Datensatz-Feld des 3., 4., 5. und 6. Satzabschnittes - 1,
     * Wert = Länge des Feldes in Byte.
     */
    protected static final int[] CRECORD_LENGTH_EXT =
    {
        2, 27, 2, 27, 2, 27, 2, 27, 12
    };

    /**
     * Index = Anzahl Erweiterungsteile,
     * Wert = Anzahl benötigter Satzabschnitte.
     */
    protected static final int[] CRECORD_EXTENSIONCOUNT_TO_BLOCKCOUNT =
    {
        2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6
    };

    /**
     * Index = Index Erweiterungsteil,
     * Wert = Satzabschnitt-Offset zu Transaktionsbeginn.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_BLOCKOFFSET =
    {
        1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5
    };

    /**
     * Index = Index Erweiterungsteil,
     * Wert = Anfangsposition des Erweiterungsteils relativ zum Anfang des Satzabschnittes.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_TYPEOFFSET =
    {
        CRECORD_OFFSETS2[5], CRECORD_OFFSETS2[7], CRECORD_OFFSETS_EXT[0], CRECORD_OFFSETS_EXT[2],
        CRECORD_OFFSETS_EXT[4], CRECORD_OFFSETS_EXT[6], CRECORD_OFFSETS_EXT[0], CRECORD_OFFSETS_EXT[2],
        CRECORD_OFFSETS_EXT[4], CRECORD_OFFSETS_EXT[6], CRECORD_OFFSETS_EXT[0], CRECORD_OFFSETS_EXT[2],
        CRECORD_OFFSETS_EXT[4], CRECORD_OFFSETS_EXT[6], CRECORD_OFFSETS_EXT[0], CRECORD_OFFSETS_EXT[2],
        CRECORD_OFFSETS_EXT[4], CRECORD_OFFSETS_EXT[6]
    };

    /**
     * Index = Index Erweiterungsteil,
     * Wert = Anfangsposition des Erweiterungsteils relativ zum Anfang des Satzabschnittes.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_TYPELENGTH =
    {
        CRECORD_LENGTH2[5], CRECORD_LENGTH2[7], CRECORD_LENGTH_EXT[0], CRECORD_LENGTH_EXT[2],
        CRECORD_LENGTH_EXT[4], CRECORD_LENGTH_EXT[6], CRECORD_LENGTH_EXT[0], CRECORD_LENGTH_EXT[2],
        CRECORD_LENGTH_EXT[4], CRECORD_LENGTH_EXT[6], CRECORD_LENGTH_EXT[0], CRECORD_LENGTH_EXT[2],
        CRECORD_LENGTH_EXT[4], CRECORD_LENGTH_EXT[6], CRECORD_LENGTH_EXT[0], CRECORD_LENGTH_EXT[2],
        CRECORD_LENGTH_EXT[4], CRECORD_LENGTH_EXT[6]
    };

    /**
     * Index = Index Erweiterungsteil,
     * Wert = Anfangsposition des Erweiterungsteils relativ zum Anfang des Satzabschnittes.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_VALUEOFFSET =
    {
        CRECORD_OFFSETS2[6], CRECORD_OFFSETS2[8], CRECORD_OFFSETS_EXT[1], CRECORD_OFFSETS_EXT[3],
        CRECORD_OFFSETS_EXT[5], CRECORD_OFFSETS_EXT[7], CRECORD_OFFSETS_EXT[1], CRECORD_OFFSETS_EXT[3],
        CRECORD_OFFSETS_EXT[5], CRECORD_OFFSETS_EXT[7], CRECORD_OFFSETS_EXT[1], CRECORD_OFFSETS_EXT[3],
        CRECORD_OFFSETS_EXT[5], CRECORD_OFFSETS_EXT[7], CRECORD_OFFSETS_EXT[1], CRECORD_OFFSETS_EXT[3],
        CRECORD_OFFSETS_EXT[5], CRECORD_OFFSETS_EXT[7]
    };

    /**
     * Index = Index Erweiterungsteil,
     * Wert = Anfangsposition des Erweiterungsteils relativ zum Anfang des Satzabschnittes.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_VALUELENGTH =
    {
        CRECORD_LENGTH2[6], CRECORD_LENGTH2[8], CRECORD_LENGTH_EXT[1], CRECORD_LENGTH_EXT[3],
        CRECORD_LENGTH_EXT[5], CRECORD_LENGTH_EXT[7], CRECORD_LENGTH_EXT[1], CRECORD_LENGTH_EXT[3],
        CRECORD_LENGTH_EXT[5], CRECORD_LENGTH_EXT[7], CRECORD_LENGTH_EXT[1], CRECORD_LENGTH_EXT[3],
        CRECORD_LENGTH_EXT[5], CRECORD_LENGTH_EXT[7], CRECORD_LENGTH_EXT[1], CRECORD_LENGTH_EXT[3],
        CRECORD_LENGTH_EXT[5], CRECORD_LENGTH_EXT[7]
    };

    /**
     * Index = Index Erweiterungsteil,
     * Wert = Anzahl der folgenden Erweiterungsteile im selben Satzabschnitt.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_FOLLOWINGEXTENSIONS =
    {
        1, 0, 3, 2, 1, 0, 3, 2, 1, 0, 3, 2, 1, 0, 3, 2, 1, 0
    };

    /**
     * Index = Index Erweiterungsteil,
     * Wert = Feld-Konstante für das Typen-Feld des Erweiterungsteils.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_TYPEFIELD =
    {
        Fields.FIELD_C19, Fields.FIELD_C21, Fields.FIELD_C24, Fields.FIELD_C26, Fields.FIELD_C28, Fields.FIELD_C30,
        Fields.FIELD_C33, Fields.FIELD_C35, Fields.FIELD_C37, Fields.FIELD_C39, Fields.FIELD_C42, Fields.FIELD_C44,
        Fields.FIELD_C46, Fields.FIELD_C48, Fields.FIELD_C51, Fields.FIELD_C53, Fields.FIELD_C55, Fields.FIELD_C57,
        Fields.FIELD_C59
    };

    /**
     * Index = Index Erweiterungsteil,
     * Wert = Feld-Konstante für das Werte-Feld des Erweiterungsteils.
     */
    protected static final int[] CRECORD_EXTINDEX_TO_VALUEFIELD =
    {
        Fields.FIELD_C20, Fields.FIELD_C22, Fields.FIELD_C25, Fields.FIELD_C27, Fields.FIELD_C29, Fields.FIELD_C31,
        Fields.FIELD_C34, Fields.FIELD_C36, Fields.FIELD_C38, Fields.FIELD_C40, Fields.FIELD_C43, Fields.FIELD_C45,
        Fields.FIELD_C47, Fields.FIELD_C49, Fields.FIELD_C52, Fields.FIELD_C54, Fields.FIELD_C56, Fields.FIELD_C58
    };

    /** Erzeugt eine neue {@code DTAUSDisk} Instanz. */
    public DTAUSDisk()
    {
        super();
    }

    protected char getBlockType( final long position ) throws IOException
    {
        // Feld 2
        final AlphaNumericText27 txt = this.readAlphaNumeric(
            Fields.FIELD_A2, position + ARECORD_OFFSETS[1], ARECORD_LENGTH[1], ENCODING_ASCII );

        char ret = '?';

        if ( txt != null )
        {
            if ( txt.length() != 1 )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position + ARECORD_OFFSETS[1] );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_A2, IllegalDataMessage.TYPE_CONSTANT, position + ARECORD_OFFSETS[1],
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

    protected Header readHeader() throws IOException
    {
        Long num;
        AlphaNumericText27 txt;
        final Header ret = new Header();

        // Feld 1
        num = this.readNumber(
            Fields.FIELD_A1, this.getHeaderPosition() + ARECORD_OFFSETS[0], ARECORD_LENGTH[0], ENCODING_ASCII );

        if ( num.longValue() != NO_NUMBER && num.intValue() != this.getBlockSize() )
        {
            if ( ThreadLocalMessages.isErrorsEnabled() )
            {
                throw new CorruptedException( this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[0] );
            }
            else
            {
                final Message msg = new IllegalDataMessage(
                    Fields.FIELD_A1, IllegalDataMessage.TYPE_CONSTANT, this.getHeaderPosition() + ARECORD_OFFSETS[0],
                    num.toString() );

                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        // Feld 2
        txt = this.readAlphaNumeric(
            Fields.FIELD_A2, this.getHeaderPosition() + ARECORD_OFFSETS[1], ARECORD_LENGTH[1], ENCODING_ASCII );

        if ( txt != null && ( txt.length() != 1 || txt.charAt( 0 ) != 'A' ) )
        {
            if ( ThreadLocalMessages.isErrorsEnabled() )
            {
                throw new CorruptedException( this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[1] );
            }
            else
            {
                final Message msg = new IllegalDataMessage(
                    Fields.FIELD_A2, IllegalDataMessage.TYPE_CONSTANT, this.getHeaderPosition() + ARECORD_OFFSETS[1],
                    txt.format() );

                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        // Feld 3
        txt = this.readAlphaNumeric(
            Fields.FIELD_A3, this.getHeaderPosition() + ARECORD_OFFSETS[2], ARECORD_LENGTH[2], ENCODING_ASCII );

        ret.setType( null );
        if ( txt != null )
        {
            final LogicalFileType label = LogicalFileType.valueOf( txt.format() );

            if ( label == null )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException(
                        this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[2] );

                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_A3, IllegalDataMessage.TYPE_FILETYPE, this.getHeaderPosition() + ARECORD_OFFSETS[2],
                        txt.format() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                ret.setType( label );
            }
        }

        // Feld 4
        num = this.readNumber(
            Fields.FIELD_A4, this.getHeaderPosition() + ARECORD_OFFSETS[3], ARECORD_LENGTH[3], ENCODING_ASCII );

        ret.setBank( null );
        if ( num.longValue() != NO_NUMBER )
        {
            if ( !Bankleitzahl.checkBankleitzahl( num ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException(
                        this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[3] );

                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_A4, IllegalDataMessage.TYPE_BANKLEITZAHL,
                        this.getHeaderPosition() + ARECORD_OFFSETS[3], num.toString() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                ret.setBank( Bankleitzahl.valueOf( num ) );
            }
        }

        // Feld 5
        // Nur belegt wenn Absender Kreditinistitut ist, sonst 0.
        num = this.readNumber(
            Fields.FIELD_A5, this.getHeaderPosition() + ARECORD_OFFSETS[4], ARECORD_LENGTH[4], ENCODING_ASCII );

        ret.setBankData( null );
        if ( num.longValue() != NO_NUMBER && ret.getType() != null && ret.getType().isSendByBank() )
        {
            if ( !Bankleitzahl.checkBankleitzahl( num ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException(
                        this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[4] );

                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_A5, IllegalDataMessage.TYPE_BANKLEITZAHL,
                        this.getHeaderPosition() + ARECORD_OFFSETS[4], num.toString() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                ret.setBankData( Bankleitzahl.valueOf( num ) );
            }
        }

        // Feld 6
        txt = this.readAlphaNumeric(
            Fields.FIELD_A6, this.getHeaderPosition() + ARECORD_OFFSETS[5], ARECORD_LENGTH[5], ENCODING_ASCII );

        ret.setCustomer( txt );

        // Feld 7
        final Date createDate = this.readShortDate(
            Fields.FIELD_A7, this.getHeaderPosition() + ARECORD_OFFSETS[6], ENCODING_ASCII );

        // Feld 8
        // Nur belegt wenn Absender Kreditinistitut ist, sonst "".
        txt = this.readAlphaNumeric(
            Fields.FIELD_A8, this.getHeaderPosition() + ARECORD_OFFSETS[7], ARECORD_LENGTH[7], ENCODING_ASCII );

        // Feld 9
        num = this.readNumber(
            Fields.FIELD_A9, this.getHeaderPosition() + ARECORD_OFFSETS[8], ARECORD_LENGTH[8], ENCODING_ASCII );

        ret.setAccount( null );
        if ( num.longValue() != NO_NUMBER )
        {
            if ( !Kontonummer.checkKontonummer( num ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException(
                        this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[8] );

                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_A9, IllegalDataMessage.TYPE_KONTONUMMER,
                        this.getHeaderPosition() + ARECORD_OFFSETS[8], num.toString() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                ret.setAccount( Kontonummer.valueOf( num ) );
            }
        }

        // Feld 10
        num = this.readNumber(
            Fields.FIELD_A10, this.getHeaderPosition() + ARECORD_OFFSETS[9], ARECORD_LENGTH[9], ENCODING_ASCII );

        ret.setReference( null );
        if ( num.longValue() != NO_NUMBER )
        {
            if ( !Referenznummer10.checkReferenznummer10( num ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException(
                        this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[9] );

                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_A10, IllegalDataMessage.TYPE_REFERENZNUMMER,
                        this.getHeaderPosition() + ARECORD_OFFSETS[9], num.toString() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                ret.setReference( Referenznummer10.valueOf( num ) );
            }
        }

        // Feld 11b
        final Date executionDate = this.readLongDate(
            Fields.FIELD_A11B, this.getHeaderPosition() + ARECORD_OFFSETS[11], ENCODING_ASCII );

        ret.setCreateDate( createDate );
        ret.setExecutionDate( executionDate );

        ret.setCurrency( null );
        if ( createDate != null )
        {
            // Feld 12
            txt = this.readAlphaNumeric(
                Fields.FIELD_A12, this.getHeaderPosition() + ARECORD_OFFSETS[13], ARECORD_LENGTH[13], ENCODING_ASCII );

            if ( txt != null )
            {
                if ( txt.length() != 1 )
                {
                    if ( ThreadLocalMessages.isErrorsEnabled() )
                    {
                        throw new CorruptedException(
                            this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[13] );

                    }
                    else
                    {
                        final Message msg = new IllegalDataMessage(
                            Fields.FIELD_A12, IllegalDataMessage.TYPE_CURRENCY,
                            this.getHeaderPosition() + ARECORD_OFFSETS[13], txt.format() );

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
                                this.getImplementation(), this.getHeaderPosition() + ARECORD_OFFSETS[13] );

                        }
                        else
                        {
                            final Message msg = new IllegalDataMessage(
                                Fields.FIELD_A12, IllegalDataMessage.TYPE_CURRENCY,
                                this.getHeaderPosition() + ARECORD_OFFSETS[13], txt.format() );

                            ThreadLocalMessages.getMessages().addMessage( msg );
                        }
                    }

                    ret.setCurrency( cur );
                }
            }
        }

        return ret;
    }

    protected void writeHeader( final Header header ) throws IOException
    {
        final LogicalFileType label = header.getType();

        // Feld 1
        this.writeNumber(
            Fields.FIELD_A1, this.getHeaderPosition() + ARECORD_OFFSETS[0], ARECORD_LENGTH[0], this.getBlockSize(),
            ENCODING_ASCII );

        // Feld 2
        this.writeAlphaNumeric(
            Fields.FIELD_A2, this.getHeaderPosition() + ARECORD_OFFSETS[1], ARECORD_LENGTH[1], "A", ENCODING_ASCII );

        // Feld 3
        this.writeAlphaNumeric(
            Fields.FIELD_A3, this.getHeaderPosition() + ARECORD_OFFSETS[2], ARECORD_LENGTH[2], label.getCode(),
            ENCODING_ASCII );

        // Feld 4
        this.writeNumber(
            Fields.FIELD_A4, this.getHeaderPosition() + ARECORD_OFFSETS[3], ARECORD_LENGTH[3],
            header.getBank().intValue(), ENCODING_ASCII );

        // Feld 5
        this.writeNumber(
            Fields.FIELD_A5, this.getHeaderPosition() + ARECORD_OFFSETS[4],
            ARECORD_LENGTH[4], ( label.isSendByBank() && header.getBankData() != null
                                 ? header.getBankData().intValue() : 0 ), ENCODING_ASCII );

        // Feld 6
        this.writeAlphaNumeric(
            Fields.FIELD_A6, this.getHeaderPosition() + ARECORD_OFFSETS[5], ARECORD_LENGTH[5],
            header.getCustomer().format(), ENCODING_ASCII );

        // Feld 7
        this.writeShortDate(
            Fields.FIELD_A7, this.getHeaderPosition() + ARECORD_OFFSETS[6], header.getCreateDate(), ENCODING_ASCII );

        // Feld 8
        this.writeAlphaNumeric(
            Fields.FIELD_A8, this.getHeaderPosition() + ARECORD_OFFSETS[7], ARECORD_LENGTH[7], "", ENCODING_ASCII );

        // Feld 9
        this.writeNumber(
            Fields.FIELD_A9, this.getHeaderPosition() + ARECORD_OFFSETS[8], ARECORD_LENGTH[8],
            header.getAccount().longValue(), ENCODING_ASCII );

        // Feld 10
        this.writeNumber(
            Fields.FIELD_A10, this.getHeaderPosition() + ARECORD_OFFSETS[9], ARECORD_LENGTH[9],
            ( header.getReference() != null ? header.getReference().longValue() : 0L ), ENCODING_ASCII );

        // Feld 11a
        this.writeAlphaNumeric(
            Fields.FIELD_A11A, this.getHeaderPosition() + ARECORD_OFFSETS[10], ARECORD_LENGTH[10], "", ENCODING_ASCII );

        // Feld 11b
        this.writeLongDate(
            Fields.FIELD_A11B, this.getHeaderPosition() + ARECORD_OFFSETS[11], header.getExecutionDate(),
            ENCODING_ASCII );

        // Feld 11c
        this.writeAlphaNumeric(
            Fields.FIELD_A11C, this.getHeaderPosition() + ARECORD_OFFSETS[12], ARECORD_LENGTH[12], "", ENCODING_ASCII );

        // Feld 12
        this.writeAlphaNumeric(
            Fields.FIELD_A12, this.getHeaderPosition() + ARECORD_OFFSETS[13], ARECORD_LENGTH[13],
            Character.toString( this.getCurrencyMapper().getDtausCode( header.getCurrency(), header.getCreateDate() ) ),
            ENCODING_ASCII );

    }

    protected Checksum readChecksum() throws IOException
    {
        Long num;
        final AlphaNumericText27 txt;
        final Checksum checksum;
        checksum = new Checksum();

        // Feld 1
        num = this.readNumber(
            Fields.FIELD_E1, this.getChecksumPosition() + ERECORD_OFFSETS[0], ERECORD_LENGTH[0], ENCODING_ASCII );

        if ( num.longValue() != NO_NUMBER && num.intValue() != this.getBlockSize() )
        {
            if ( ThreadLocalMessages.isErrorsEnabled() )
            {
                throw new CorruptedException(
                    this.getImplementation(), this.getChecksumPosition() + ERECORD_OFFSETS[0] );

            }
            else
            {
                final Message msg = new IllegalDataMessage(
                    Fields.FIELD_E1, IllegalDataMessage.TYPE_CONSTANT, this.getChecksumPosition() + ERECORD_OFFSETS[0],
                    num.toString() );

                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        // Feld 2
        txt = this.readAlphaNumeric(
            Fields.FIELD_E2, this.getChecksumPosition() + ERECORD_OFFSETS[1], ERECORD_LENGTH[1], ENCODING_ASCII );

        if ( txt != null && ( txt.length() != 1 || txt.charAt( 0 ) != 'E' ) )
        {
            if ( ThreadLocalMessages.isErrorsEnabled() )
            {
                throw new CorruptedException(
                    this.getImplementation(), this.getChecksumPosition() + ERECORD_OFFSETS[1] );

            }
            else
            {
                final Message msg = new IllegalDataMessage(
                    Fields.FIELD_E2, IllegalDataMessage.TYPE_CONSTANT, this.getChecksumPosition() + ERECORD_OFFSETS[1],
                    txt.format() );

                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        // Feld 4
        num = this.readNumber(
            Fields.FIELD_E4, this.getChecksumPosition() + ERECORD_OFFSETS[3], ERECORD_LENGTH[3], ENCODING_ASCII );

        if ( num.longValue() != NO_NUMBER )
        {
            checksum.setTransactionCount( num.intValue() );
        }

        // Feld 6
        num = this.readNumber(
            Fields.FIELD_E6, this.getChecksumPosition() + ERECORD_OFFSETS[5], ERECORD_LENGTH[5], ENCODING_ASCII );

        if ( num.longValue() != NO_NUMBER )
        {
            checksum.setSumTargetAccount( num.longValue() );
        }

        // Feld 7
        num = this.readNumber(
            Fields.FIELD_E7, this.getChecksumPosition() + ERECORD_OFFSETS[6], ERECORD_LENGTH[6], ENCODING_ASCII );

        if ( num.longValue() != NO_NUMBER )
        {
            checksum.setSumTargetBank( num.longValue() );
        }

        // Feld 8
        num = this.readNumber(
            Fields.FIELD_E8, this.getChecksumPosition() + ERECORD_OFFSETS[7], ERECORD_LENGTH[7], ENCODING_ASCII );

        if ( num.longValue() != NO_NUMBER )
        {
            checksum.setSumAmount( num.longValue() );
        }

        return checksum;
    }

    protected void writeChecksum( final Checksum checksum ) throws IOException
    {
        // Feld 1
        this.writeNumber(
            Fields.FIELD_E1, this.getChecksumPosition() + ERECORD_OFFSETS[0], ERECORD_LENGTH[0], this.getBlockSize(),
            ENCODING_ASCII );

        // Feld 2
        this.writeAlphaNumeric(
            Fields.FIELD_E2, this.getChecksumPosition() + ERECORD_OFFSETS[1], ERECORD_LENGTH[1], "E", ENCODING_ASCII );

        // Feld 3
        this.writeAlphaNumeric(
            Fields.FIELD_E3, this.getChecksumPosition() + ERECORD_OFFSETS[2], ERECORD_LENGTH[2], "", ENCODING_ASCII );

        // Feld 4
        this.writeNumber(
            Fields.FIELD_E4, this.getChecksumPosition() + ERECORD_OFFSETS[3], ERECORD_LENGTH[3],
            checksum.getTransactionCount(), ENCODING_ASCII );

        // Feld 5
        this.writeNumber(
            Fields.FIELD_E5, this.getChecksumPosition() + ERECORD_OFFSETS[4], ERECORD_LENGTH[4], 0L, ENCODING_ASCII );

        // Feld 6
        this.writeNumber(
            Fields.FIELD_E6, this.getChecksumPosition() + ERECORD_OFFSETS[5], ERECORD_LENGTH[5],
            checksum.getSumTargetAccount(), ENCODING_ASCII );

        // Feld 7
        this.writeNumber(
            Fields.FIELD_E7, this.getChecksumPosition() + ERECORD_OFFSETS[6], ERECORD_LENGTH[6],
            checksum.getSumTargetBank(), ENCODING_ASCII );

        // Feld 8
        this.writeNumber(
            Fields.FIELD_E8, this.getChecksumPosition() + ERECORD_OFFSETS[7], ERECORD_LENGTH[7],
            checksum.getSumAmount(), ENCODING_ASCII );

        // Feld 9
        this.writeAlphaNumeric(
            Fields.FIELD_E9, this.getChecksumPosition() + ERECORD_OFFSETS[8], ERECORD_LENGTH[8], "", ENCODING_ASCII );

    }

    protected Transaction readTransaction( final long position,
                                           final Transaction transaction )
        throws IOException
    {
        Long num;
        AlphaNumericText27 txt;
        final List desc = new ArrayList( 14 );

        transaction.setExecutiveExt( null );
        transaction.setTargetExt( null );

        final long extCount = this.readNumber(
            Fields.FIELD_C18, position + this.getBlockSize() + CRECORD_OFFSETS2[4], CRECORD_LENGTH2[4],
            ENCODING_ASCII ).longValue();

        // Konstanter Teil - Satzaschnitt 1 - Feld 1
        num = this.readNumber(
            Fields.FIELD_C1, position + CRECORD_OFFSETS1[0], CRECORD_LENGTH1[0], ENCODING_ASCII );

        if ( num.longValue() != NO_NUMBER && extCount != NO_NUMBER &&
             num.intValue() != CRECORD_CONST_LENGTH + extCount * CRECORD_EXT_LENGTH )
        {
            if ( ThreadLocalMessages.isErrorsEnabled() )
            {
                throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[0] );
            }
            else
            {
                final Message msg = new IllegalDataMessage(
                    Fields.FIELD_C1, IllegalDataMessage.TYPE_NUMERIC, position + CRECORD_OFFSETS1[0], num.toString() );

                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 2
        txt = this.readAlphaNumeric(
            Fields.FIELD_C2, position + CRECORD_OFFSETS1[1], CRECORD_LENGTH1[1], ENCODING_ASCII );

        if ( txt != null && ( txt.length() != 1 || txt.charAt( 0 ) != 'C' ) )
        {
            if ( ThreadLocalMessages.isErrorsEnabled() )
            {
                throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[1] );
            }
            else
            {
                final Message msg = new IllegalDataMessage(
                    Fields.FIELD_C2, IllegalDataMessage.TYPE_CONSTANT, position + CRECORD_OFFSETS1[1], txt.format() );

                ThreadLocalMessages.getMessages().addMessage( msg );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 3
        num = this.readNumber( Fields.FIELD_C3, position + CRECORD_OFFSETS1[2], CRECORD_LENGTH1[2], ENCODING_ASCII );

        transaction.setPrimaryBank( null );
        if ( num.longValue() != NO_NUMBER && num.longValue() != 0L )
        {
            if ( !Bankleitzahl.checkBankleitzahl( num ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException(
                        this.getImplementation(), position + CRECORD_OFFSETS1[2] );

                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_C3, IllegalDataMessage.TYPE_BANKLEITZAHL, position + CRECORD_OFFSETS1[2],
                        num.toString() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                transaction.setPrimaryBank( Bankleitzahl.valueOf( num ) );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 4
        num = this.readNumber(
            Fields.FIELD_C4, position + CRECORD_OFFSETS1[3], CRECORD_LENGTH1[3], ENCODING_ASCII );

        transaction.setTargetBank( null );
        if ( num.longValue() != NO_NUMBER )
        {
            if ( !Bankleitzahl.checkBankleitzahl( num ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[3] );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_C4, IllegalDataMessage.TYPE_BANKLEITZAHL, position + CRECORD_OFFSETS1[3],
                        num.toString() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                transaction.setTargetBank( Bankleitzahl.valueOf( num ) );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 5
        num = this.readNumber(
            Fields.FIELD_C5, position + CRECORD_OFFSETS1[4], CRECORD_LENGTH1[4], ENCODING_ASCII );

        transaction.setTargetAccount( null );
        if ( num.longValue() != NO_NUMBER )
        {
            if ( !Kontonummer.checkKontonummer( num ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[4] );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_C5, IllegalDataMessage.TYPE_KONTONUMMER, position + CRECORD_OFFSETS1[4],
                        num.toString() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                transaction.setTargetAccount( Kontonummer.valueOf( num ) );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 6
        num = this.readNumber(
            Fields.FIELD_C6, position + CRECORD_OFFSETS1[5], CRECORD_LENGTH1[5], ENCODING_ASCII );

        transaction.setReference( null );
        if ( num.longValue() != NO_NUMBER )
        {
            if ( !Referenznummer11.checkReferenznummer11( num ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[5] );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_C6, IllegalDataMessage.TYPE_REFERENZNUMMER, position + CRECORD_OFFSETS1[5],
                        num.toString() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                transaction.setReference( Referenznummer11.valueOf( num ) );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Felder 7a & 7b
        final Long keyType = this.readNumber(
            Fields.FIELD_C7A, position + CRECORD_OFFSETS1[6], 2, ENCODING_ASCII );

        num = this.readNumber(
            Fields.FIELD_C7B, position + CRECORD_OFFSETS1[6] + 2, CRECORD_LENGTH1[6] - 2, ENCODING_ASCII );

        transaction.setType( null );

        if ( keyType.longValue() != NO_NUMBER && num.longValue() != NO_NUMBER )
        {
            final Textschluessel type = this.getTextschluesselVerzeichnis().getTextschluessel(
                keyType.intValue(), num.intValue(), this.getHeader().getCreateDate() );

            if ( type == null )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[6] );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_C7A, IllegalDataMessage.TYPE_TEXTSCHLUESSEL, position + CRECORD_OFFSETS1[6],
                        keyType.toString() + num.toString() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                transaction.setType( type );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 10
        num = this.readNumber( Fields.FIELD_C10, position + CRECORD_OFFSETS1[9], CRECORD_LENGTH1[9], ENCODING_ASCII );

        transaction.setExecutiveBank( null );
        if ( num.longValue() != NO_NUMBER )
        {
            if ( !Bankleitzahl.checkBankleitzahl( num ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[9] );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_C10, IllegalDataMessage.TYPE_BANKLEITZAHL, position + CRECORD_OFFSETS1[9],
                        num.toString() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                transaction.setExecutiveBank( Bankleitzahl.valueOf( num ) );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 11
        num = this.readNumber(
            Fields.FIELD_C11, position + CRECORD_OFFSETS1[10], CRECORD_LENGTH1[10], ENCODING_ASCII );

        transaction.setExecutiveAccount( null );
        if ( num.longValue() != NO_NUMBER )
        {
            if ( !Kontonummer.checkKontonummer( num ) )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException( this.getImplementation(), position + CRECORD_OFFSETS1[10] );
                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_C11, IllegalDataMessage.TYPE_KONTONUMMER, position + CRECORD_OFFSETS1[10],
                        num.toString() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                transaction.setExecutiveAccount( Kontonummer.valueOf( num ) );
            }
        }

        // Konstanter Teil - Satzaschnitt 1 - Feld 12
        num = this.readNumber(
            Fields.FIELD_C12, position + CRECORD_OFFSETS1[11], CRECORD_LENGTH1[11], ENCODING_ASCII );

        transaction.setAmount(
            num.longValue() == NO_NUMBER
            ? null : BigInteger.valueOf( num.longValue() ) );

        // Konstanter Teil - Satzaschnitt 1 - Feld 14a
        txt = this.readAlphaNumeric(
            Fields.FIELD_C14A, position + CRECORD_OFFSETS1[13], CRECORD_LENGTH1[13], ENCODING_ASCII );

        transaction.setTargetName( txt );

        // Konstanter Teil - Satzaschnitt 2 - Feld 15(1)
        txt = this.readAlphaNumeric(
            Fields.FIELD_C15, position + this.getBlockSize() + CRECORD_OFFSETS2[0], CRECORD_LENGTH2[0],
            ENCODING_ASCII );

        transaction.setExecutiveName( txt );

        // Konstanter Teil - Satzaschnitt 2 - Feld 16(2)
        txt = this.readAlphaNumeric(
            Fields.FIELD_C16, position + this.getBlockSize() + CRECORD_OFFSETS2[1], CRECORD_LENGTH2[1],
            ENCODING_ASCII );

        if ( txt != null )
        {
            desc.add( txt );
        }

        // Konstanter Teil - Satzaschnitt 2 - Feld 17a(3)
        txt = this.readAlphaNumeric(
            Fields.FIELD_C17A, position + this.getBlockSize() + CRECORD_OFFSETS2[2], CRECORD_LENGTH2[2],
            ENCODING_ASCII );

        transaction.setCurrency( null );

        if ( txt != null )
        {
            if ( txt.length() != 1 )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException(
                        this.getImplementation(), position + this.getBlockSize() + CRECORD_OFFSETS1[10] );

                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        Fields.FIELD_C17A, IllegalDataMessage.TYPE_CURRENCY,
                        position + this.getBlockSize() + CRECORD_OFFSETS1[10], txt.format() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
            else
            {
                final char c = txt.charAt( 0 );
                final Currency cur = this.getCurrencyMapper().getDtausCurrency( c, this.getHeader().getCreateDate() );

                if ( cur == null )
                {
                    if ( ThreadLocalMessages.isErrorsEnabled() )
                    {
                        throw new CorruptedException(
                            this.getImplementation(), position + this.getBlockSize() + CRECORD_OFFSETS1[10] );

                    }
                    else
                    {
                        final Message msg = new IllegalDataMessage(
                            Fields.FIELD_C17A, IllegalDataMessage.TYPE_CURRENCY,
                            position + this.getBlockSize() + CRECORD_OFFSETS1[10], txt.format() );

                        ThreadLocalMessages.getMessages().addMessage( msg );
                    }
                }

                transaction.setCurrency( cur );
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
        for ( int i = 0; i < extCount && extCount != NO_NUMBER; i++ )
        {
            final long extPos = position + CRECORD_EXTINDEX_TO_BLOCKOFFSET[i] * this.getBlockSize();

            num = this.readNumber(
                CRECORD_EXTINDEX_TO_TYPEFIELD[i], extPos + CRECORD_EXTINDEX_TO_TYPEOFFSET[i],
                CRECORD_EXTINDEX_TO_TYPELENGTH[i], ENCODING_ASCII );

            txt = this.readAlphaNumeric(
                CRECORD_EXTINDEX_TO_VALUEFIELD[i], extPos + CRECORD_EXTINDEX_TO_VALUEOFFSET[i],
                CRECORD_EXTINDEX_TO_VALUELENGTH[i], ENCODING_ASCII );

            if ( num.longValue() == 1L )
            {
                if ( transaction.getTargetExt() != null )
                {
                    if ( ThreadLocalMessages.isErrorsEnabled() )
                    {
                        throw new CorruptedException(
                            this.getImplementation(), extPos + CRECORD_EXTINDEX_TO_TYPEOFFSET[i] );

                    }
                    else
                    {
                        final Message msg = new IllegalDataMessage(
                            CRECORD_EXTINDEX_TO_TYPEFIELD[i], IllegalDataMessage.TYPE_CONSTANT,
                            extPos + CRECORD_EXTINDEX_TO_TYPEOFFSET[i], num.toString() );

                        ThreadLocalMessages.getMessages().addMessage( msg );
                    }
                }
                else
                {
                    transaction.setTargetExt( txt );
                }
            }
            else if ( num.longValue() == 2L )
            {
                if ( txt != null )
                {
                    desc.add( txt );
                }
            }
            else if ( num.longValue() == 3L )
            {
                if ( transaction.getExecutiveExt() != null )
                {
                    if ( ThreadLocalMessages.isErrorsEnabled() )
                    {
                        throw new CorruptedException(
                            this.getImplementation(), extPos + CRECORD_EXTINDEX_TO_TYPEOFFSET[i] );

                    }
                    else
                    {
                        final Message msg = new IllegalDataMessage(
                            CRECORD_EXTINDEX_TO_TYPEFIELD[i], IllegalDataMessage.TYPE_CONSTANT,
                            extPos + CRECORD_EXTINDEX_TO_TYPEOFFSET[i], num.toString() );

                        ThreadLocalMessages.getMessages().addMessage( msg );
                    }
                }
                else
                {
                    transaction.setExecutiveExt( txt );
                }
            }
            else if ( num.longValue() != NO_NUMBER )
            {
                if ( ThreadLocalMessages.isErrorsEnabled() )
                {
                    throw new CorruptedException(
                        this.getImplementation(), extPos + CRECORD_EXTINDEX_TO_TYPEOFFSET[i] );

                }
                else
                {
                    final Message msg = new IllegalDataMessage(
                        CRECORD_EXTINDEX_TO_TYPEFIELD[i], IllegalDataMessage.TYPE_CONSTANT,
                        extPos + CRECORD_EXTINDEX_TO_TYPEOFFSET[i], num.toString() );

                    ThreadLocalMessages.getMessages().addMessage( msg );
                }
            }
        }

        transaction.setDescriptions( (AlphaNumericText27[]) desc.toArray( new AlphaNumericText27[ desc.size() ] ) );
        return transaction;
    }

    protected void writeTransaction( final long position, final Transaction transaction ) throws IOException
    {
        AlphaNumericText27 txt;
        final AlphaNumericText27[] desc = transaction.getDescriptions();
        final Textschluessel type = transaction.getType();
        int extCount = desc.length > 0 ? desc.length - 1 : 0;

        if ( transaction.getExecutiveExt() != null )
        {
            extCount++;
        }

        if ( transaction.getTargetExt() != null )
        {
            extCount++;
        }

        // Konstanter Teil - 1. Satzabschnitt - Feld 1
        this.writeNumber( Fields.FIELD_C1, position + CRECORD_OFFSETS1[0], CRECORD_LENGTH1[0],
                          CRECORD_CONST_LENGTH + extCount * CRECORD_EXT_LENGTH, ENCODING_ASCII );

        // Konstanter Teil - 1. Satzabschnitt - Feld 2
        this.writeAlphaNumeric( Fields.FIELD_C2, position + CRECORD_OFFSETS1[1], CRECORD_LENGTH1[1], "C",
                                ENCODING_ASCII );

        // Konstanter Teil - 1. Satzabschnitt - Feld 3
        this.writeNumber( Fields.FIELD_C3, position + CRECORD_OFFSETS1[2], CRECORD_LENGTH1[2],
                          transaction.getPrimaryBank() != null ? transaction.getPrimaryBank().intValue() : 0,
                          ENCODING_ASCII );

        // Konstanter Teil - 1. Satzabschnitt - Feld 4
        this.writeNumber( Fields.FIELD_C4, position + CRECORD_OFFSETS1[3], CRECORD_LENGTH1[3],
                          transaction.getTargetBank().intValue(), ENCODING_ASCII );

        // Konstanter Teil - 1. Satzabschnitt - Feld 5
        this.writeNumber( Fields.FIELD_C5, position + CRECORD_OFFSETS1[4], CRECORD_LENGTH1[4],
                          transaction.getTargetAccount().longValue(), ENCODING_ASCII );

        // Konstanter Teil - 1. Satzabschnitt - Feld 6
        // TODO -1
        this.writeNumber( -1, position + CRECORD_OFFSETS1[5] - 1, 1, 0L, ENCODING_ASCII );

        this.writeNumber( Fields.FIELD_C6, position + CRECORD_OFFSETS1[5], CRECORD_LENGTH1[5],
                          transaction.getReference() != null ? transaction.getReference().longValue() : 0L,
                          ENCODING_ASCII );

        this.writeNumber( -1, position + CRECORD_OFFSETS1[6] - 1, 1, 0L, ENCODING_ASCII );

        // Konstanter Teil - 1. Satzabschnitt - Felder 7a & 7b
        // TODO -3, +/- 2
        this.writeNumber( Fields.FIELD_C7A, position + CRECORD_OFFSETS1[6], CRECORD_LENGTH1[6] - 3, type.getKey(),
                          ENCODING_ASCII );

        this.writeNumber( Fields.FIELD_C7B, position + CRECORD_OFFSETS1[6] + 2, CRECORD_LENGTH1[6] - 2,
                          type.getExtension(), ENCODING_ASCII );

        // Konstanter Teil - 1. Satzabschnitt - Feld 8
        this.writeAlphaNumeric( Fields.FIELD_C8, position + CRECORD_OFFSETS1[7], CRECORD_LENGTH1[7], "",
                                ENCODING_ASCII );

        // Konstanter Teil - 1. Satzabschnitt - Feld 9
        this.writeNumber( Fields.FIELD_C9, position + CRECORD_OFFSETS1[8], CRECORD_LENGTH1[8], 0L, ENCODING_ASCII );

        // Konstanter Teil - 1. Satzabschnitt - Feld 10
        this.writeNumber( Fields.FIELD_C10, position + CRECORD_OFFSETS1[9], CRECORD_LENGTH1[9],
                          transaction.getExecutiveBank().intValue(), ENCODING_ASCII );

        // Konstanter Teil - 1. Satzabschnitt - Feld 11
        this.writeNumber( Fields.FIELD_C11, position + CRECORD_OFFSETS1[10], CRECORD_LENGTH1[10],
                          transaction.getExecutiveAccount().longValue(), ENCODING_ASCII );

        // Konstanter Teil - 1. Satzabschnitt - Feld 12
        this.writeNumber( Fields.FIELD_C12, position + CRECORD_OFFSETS1[11], CRECORD_LENGTH1[11],
                          transaction.getAmount().longValue(), ENCODING_ASCII ); // TODO longValueExact()

        // Konstanter Teil - 1. Satzabschnitt - Feld 13
        this.writeAlphaNumeric( Fields.FIELD_C13, position + CRECORD_OFFSETS1[12], CRECORD_LENGTH1[12], "",
                                ENCODING_ASCII );

        // Konstanter Teil - 1. Satzabschnitt - Feld 14a
        this.writeAlphaNumeric( Fields.FIELD_C14A, position + CRECORD_OFFSETS1[13], CRECORD_LENGTH1[13],
                                transaction.getTargetName().format(), ENCODING_ASCII );

        // Konstanter Teil - 1. Satzabschnitt - Feld 14b
        this.writeAlphaNumeric( Fields.FIELD_C14B, position + CRECORD_OFFSETS1[14], CRECORD_LENGTH1[14], "",
                                ENCODING_ASCII );

        // Konstanter Teil - 2. Satzabschnitt - Feld 15(1)
        this.writeAlphaNumeric( Fields.FIELD_C15, position + this.getBlockSize() + CRECORD_OFFSETS2[0],
                                CRECORD_LENGTH2[0], transaction.getExecutiveName().format(), ENCODING_ASCII );

        // Konstanter Teil - 2. Satzabschnitt - Feld 16(2)
        this.writeAlphaNumeric( Fields.FIELD_C16, position + this.getBlockSize() + CRECORD_OFFSETS2[1],
                                CRECORD_LENGTH2[1], desc.length > 0 ? desc[0].format() : "", ENCODING_ASCII );

        // Konstanter Teil - 2. Satzabschnitt - Feld 17a(3)
        this.writeAlphaNumeric(
            Fields.FIELD_C17A, position + this.getBlockSize() + CRECORD_OFFSETS2[2],
            CRECORD_LENGTH2[2], Character.toString( this.getCurrencyMapper().getDtausCode(
            transaction.getCurrency(), this.getHeader().getCreateDate() ) ), ENCODING_ASCII );

        // Konstanter Teil - 2. Satzabschnitt - Feld 17b(4)
        this.writeAlphaNumeric( Fields.FIELD_C17B, position + this.getBlockSize() + CRECORD_OFFSETS2[3],
                                CRECORD_LENGTH2[3], "", ENCODING_ASCII );

        // Konstanter Teil - 2. Satzabschnitt - Feld 18(5)
        this.writeNumber( Fields.FIELD_C18, position + this.getBlockSize() + CRECORD_OFFSETS2[4],
                          CRECORD_LENGTH2[4], extCount, ENCODING_ASCII );

        // Erweiterungs-Teile im zweiten Satzabschnitt initialisieren.
        this.initializeExtensionBlock( position + this.getBlockSize(), 1 );

        // Erweiterungs-Teile.
        int extIndex = 0;
        int blockOffset = CRECORD_EXTINDEX_TO_BLOCKOFFSET[extIndex];
        int lastBlockOffset = blockOffset;
        long blockPos = position + blockOffset * this.getBlockSize();

        // Erweiterung des beteiligten Kontos als ersten Erweiterungsteil.
        if ( ( txt = transaction.getTargetExt() ) != null )
        {
            this.writeNumber(
                CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex], blockPos + CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex],
                CRECORD_EXTINDEX_TO_TYPELENGTH[extIndex], 1L, ENCODING_ASCII );

            this.writeAlphaNumeric(
                CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex], blockPos + CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex], txt.format(), ENCODING_ASCII );

            extIndex++;
        }

        // Verwendungszweck-Zeilen des 2., 3., 4., 5. und 6. Satzabschnittes.
        for ( int i = 1; i < desc.length; i++, extIndex++ )
        {
            blockOffset = CRECORD_EXTINDEX_TO_BLOCKOFFSET[extIndex];
            blockPos = position + blockOffset * this.getBlockSize();

            if ( blockOffset != lastBlockOffset )
            {
                // Nächsten Satzabschnitt initialisieren.
                this.initializeExtensionBlock( blockPos, blockOffset );
            }

            this.writeNumber( CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex],
                              blockPos + CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex],
                              CRECORD_EXTINDEX_TO_TYPELENGTH[extIndex], 2L, ENCODING_ASCII );

            this.writeAlphaNumeric( CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex],
                                    blockPos + CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                                    CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex], desc[i].format(), ENCODING_ASCII );

            lastBlockOffset = blockOffset;
        }

        // Erweiterung des Auftraggeber-Kontos im letzten Erweiterungsteil.
        if ( ( txt = transaction.getExecutiveExt() ) != null )
        {
            blockOffset = CRECORD_EXTINDEX_TO_BLOCKOFFSET[extIndex];
            blockPos = position + blockOffset * this.getBlockSize();

            if ( blockOffset != lastBlockOffset )
            {
                // Nächsten Satzabschnitt initialisieren.
                this.initializeExtensionBlock( blockPos, blockOffset );
            }

            this.writeNumber(
                CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex], blockPos + CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex],
                CRECORD_EXTINDEX_TO_TYPELENGTH[extIndex], 3L, ENCODING_ASCII );

            this.writeAlphaNumeric(
                CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex], blockPos + CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex], txt.format(), ENCODING_ASCII );

            extIndex++;
            lastBlockOffset = blockOffset;
        }
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

        return CRECORD_EXTENSIONCOUNT_TO_BLOCKCOUNT[extCount] * this.getBlockSize();
    }

    protected int getBlockSize()
    {
        return PhysicalFileFactory.FORMAT_DISK;
    }

    private void initializeExtensionBlock( final long blockPos, final int blockOffset ) throws IOException
    {
        int extIndex;
        int startingExt;
        int endingExt;
        int reservedField;
        int reservedOffset;
        int reservedLength;

        if ( blockOffset == 1 )
        {
            startingExt = 0;
            endingExt = 1;
            reservedField = Fields.FIELD_C23;
            reservedOffset = CRECORD_OFFSETS2[9];
            reservedLength = CRECORD_LENGTH2[9];
        }
        else if ( blockOffset == 2 )
        {
            startingExt = 2;
            endingExt = 5;
            reservedField = Fields.FIELD_C32;
            reservedOffset = CRECORD_OFFSETS_EXT[8];
            reservedLength = CRECORD_LENGTH_EXT[8];
        }
        else if ( blockOffset == 3 )
        {
            startingExt = 6;
            endingExt = 9;
            reservedField = Fields.FIELD_C41;
            reservedOffset = CRECORD_OFFSETS_EXT[8];
            reservedLength = CRECORD_LENGTH_EXT[8];
        }
        else if ( blockOffset == 4 )
        {
            startingExt = 10;
            endingExt = 13;
            reservedField = Fields.FIELD_C50;
            reservedOffset = CRECORD_OFFSETS_EXT[8];
            reservedLength = CRECORD_LENGTH_EXT[8];
        }
        else if ( blockOffset == 5 )
        {
            startingExt = 14;
            endingExt = 17;
            reservedField = Fields.FIELD_C59;
            reservedOffset = CRECORD_OFFSETS_EXT[8];
            reservedLength = CRECORD_LENGTH_EXT[8];
        }
        else
        {
            throw new IllegalArgumentException( Integer.toString( blockOffset ) );
        }

        // Erweiterungsteile leeren.
        for ( extIndex = startingExt; extIndex <= endingExt; extIndex++ )
        {
            this.writeNumber(
                CRECORD_EXTINDEX_TO_TYPEFIELD[extIndex], blockPos + CRECORD_EXTINDEX_TO_TYPEOFFSET[extIndex],
                CRECORD_EXTINDEX_TO_TYPELENGTH[extIndex], 0L, ENCODING_ASCII );

            this.writeAlphaNumeric(
                CRECORD_EXTINDEX_TO_VALUEFIELD[extIndex], blockPos + CRECORD_EXTINDEX_TO_VALUEOFFSET[extIndex],
                CRECORD_EXTINDEX_TO_VALUELENGTH[extIndex], "", ENCODING_ASCII );

        }

        // Reserve-Feld initialisieren.
        this.writeAlphaNumeric( reservedField, blockPos + reservedOffset, reservedLength, "", ENCODING_ASCII );
    }

    protected Implementation getImplementation()
    {
        return ModelFactory.getModel().getModules().
            getImplementation( DTAUSDisk.class.getName() );

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

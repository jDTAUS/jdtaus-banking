/*
 *  jDTAUS Banking API
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
package org.jdtaus.banking;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Record of the {@code BankleitzahlenVerzeichnis}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 *
 * @see BankleitzahlenVerzeichnis
 */
public class BankleitzahlInfo implements Cloneable, Serializable
{
    //--Constants---------------------------------------------------------------

    /** Serial version UID for backwards compatibility with 1.1.x classes. */
    private static final long serialVersionUID = -7251689236798391940L;

    //---------------------------------------------------------------Constants--
    //--Constants---------------------------------------------------------------

    /**
     * index = index of the field in the record line; value = offset
     * the field's value starts in the record line.
     */
    private static final int[] FIELD_TO_OFFSET =
    {
        0, 8, 9, 67, 72, 107, 134, 139, 150, 152, 158, 159, 160
    };

    /**
     * index = index of the field in the record line; value = length of
     * the field's value in the record line;
     */
    private static final int[] FIELD_TO_LENGTH =
    {
        8, 1, 58, 5, 35, 27, 5, 11, 2, 6, 1, 1, 8
    };

    /**
     * index = index of the field in the record line; value = end offset
     * in the record line exclusive.
     */
    private static final int[] FIELD_TO_ENDOFFSET =
    {
        BankleitzahlInfo.FIELD_TO_OFFSET[0] +
        BankleitzahlInfo.FIELD_TO_LENGTH[0],
        BankleitzahlInfo.FIELD_TO_OFFSET[1] +
        BankleitzahlInfo.FIELD_TO_LENGTH[1],
        BankleitzahlInfo.FIELD_TO_OFFSET[2] +
        BankleitzahlInfo.FIELD_TO_LENGTH[2],
        BankleitzahlInfo.FIELD_TO_OFFSET[3] +
        BankleitzahlInfo.FIELD_TO_LENGTH[3],
        BankleitzahlInfo.FIELD_TO_OFFSET[4] +
        BankleitzahlInfo.FIELD_TO_LENGTH[4],
        BankleitzahlInfo.FIELD_TO_OFFSET[5] +
        BankleitzahlInfo.FIELD_TO_LENGTH[5],
        BankleitzahlInfo.FIELD_TO_OFFSET[6] +
        BankleitzahlInfo.FIELD_TO_LENGTH[6],
        BankleitzahlInfo.FIELD_TO_OFFSET[7] +
        BankleitzahlInfo.FIELD_TO_LENGTH[7],
        BankleitzahlInfo.FIELD_TO_OFFSET[8] +
        BankleitzahlInfo.FIELD_TO_LENGTH[8],
        BankleitzahlInfo.FIELD_TO_OFFSET[9] +
        BankleitzahlInfo.FIELD_TO_LENGTH[9],
        BankleitzahlInfo.FIELD_TO_OFFSET[10] +
        BankleitzahlInfo.FIELD_TO_LENGTH[10],
        BankleitzahlInfo.FIELD_TO_OFFSET[11] +
        BankleitzahlInfo.FIELD_TO_LENGTH[11],
        BankleitzahlInfo.FIELD_TO_OFFSET[12] +
        BankleitzahlInfo.FIELD_TO_LENGTH[12]
    };

    //---------------------------------------------------------------Constants--
    //--BankleitzahlInfo--------------------------------------------------------

    /**
     * Bankleitzahl (german bank code).
     * @serial
     */
    private Bankleitzahl bankCode;

    /**
     * Specifies if this record identifies a bank which is to be used
     * for transactions.
     * @serial
     */
    private boolean headOffice;

    /**
     * The name of the bank.
     * @serial
     */
    private String name;

    /**
     * Postal code of the city the bank is resident.
     * @serial
     */
    private int postalCode = -1;

    /**
     * City the bank resides at.
     * @serial
     */
    private String city;

    /**
     * Description of the bank including information regarding the city the
     * bank resides.
     * @serial
     */
    private String description;

    /**
     * Institute number for PAN.
     * @serial
     */
    private int panInstituteNumber = -1;

    /**
     * SWIFT Bank Identifier Code.
     * @serial
     */
    private String bic;

    /**
     * Label for the algorithm used for validating account numbers.
     * @serial
     */
    private String validationLabel;

    /**
     * The serial number of the record.
     * @serial
     */
    private Integer serialNumber;

    /**
     * Label indicating changes of the record to previous files.
     * @serial
     */
    private char changeLabel;

    /**
     * Indicates if this record will be deleted in upcoming files.
     * @serial
     */
    private boolean markedForDeletion;

    /**
     * Bankleitzahl of the bank the record is replaced with.
     * @serial
     */
    private Bankleitzahl replacingBankCode;

    /**
     * Getter for property {@code bankCode}.
     *
     * @return the german bank code identifying the bank (field 1).
     */
    public Bankleitzahl getBankCode()
    {
        return this.bankCode;
    }

    /**
     * Setter for property {@code bankCode}.
     *
     * @param bankCode the german bank code identifying the bank (field 1).
     */
    public void setBankCode( final Bankleitzahl bankCode )
    {
        this.bankCode = bankCode;
    }

    /**
     * Getter for property {@code headOffice}.
     *
     * @return {@code true} if this record specifies a bank which is to be
     * used for transactions; {@code false} if this record specifies a
     * branch office of a bank not to be used for transactions but sharing
     * a bank code (field 2).
     */
    public boolean isHeadOffice()
    {
        return this.headOffice;
    }

    /**
     * Setter for property {@code headOffice}.
     *
     * @param headOffice {@code true} if this record specifies a bank which is
     * to be used for transactions; {@code false} if this record specifies a
     * branch office of a bank not to be used for transactions but sharing
     * a bank code (field 2).
     */
    public void setHeadOffice( final boolean headOffice )
    {
        this.headOffice = headOffice;
    }

    /**
     * Getter for property {@code name}.
     *
     * @return the name of the bank (field 3).
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Setter for property {@code name}.
     *
     * @param name the name of the bank (field 3).
     */
    public void setName( final String name )
    {
        this.name = name;
    }

    /**
     * Getter for property {@code postalCode}.
     *
     * @return the postal code of the city the bank is resident (field 4).
     */
    public int getPostalCode()
    {
        return this.postalCode;
    }

    /**
     * Setter for property {@code postalCode}.
     *
     * @param postalCode the postal code of the city the bank is resident
     * (field 4).
     */
    public void setPostalCode( final int postalCode )
    {
        this.postalCode = postalCode;
    }

    /**
     * Getter for property {@code city}.
     *
     * @return the city the bank resides at (field 5).
     */
    public String getCity()
    {
        return this.city;
    }

    /**
     * Setter for property {@code city}.
     *
     * @param city the city the bank resides at (field 5).
     */
    public void setCity( final String city )
    {
        this.city = city;
    }

    /**
     * Getter for property {@code description}.
     *
     * @return a description of the bank including information regarding
     * the city the bank resides to be used for displaying on e.g. invoices
     * (field 6).
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Setter for property {@code description}.
     *
     * @param description a description of the bank including information
     * regarding the city the bank resides to be used for displaying on e.g.
     * invoices (field 6).
     */
    public void setDescription( final String description )
    {
        this.description = description;
    }

    /**
     * Getter for property {@code panInstituteNumber}.
     *
     * @return the institute number for PAN or {@code 0} if no number
     * is specified (field 7).
     */
    public int getPanInstituteNumber()
    {
        return this.panInstituteNumber;
    }

    /**
     * Setter for property {@code panInstituteNumber}.
     *
     * @param panInstituteNumber the institute number for PAN or {@code 0} if no
     * number is specified (field 7).
     */
    public void setPanInstituteNumber( final int panInstituteNumber )
    {
        this.panInstituteNumber = panInstituteNumber;
    }

    /**
     * Getter for property {@code bic}.
     *
     * @return the SWIFT Bank Identifier Code (field 8).
     */
    public String getBic()
    {
        return this.bic;
    }

    /**
     * Setter for property {@code bic}.
     *
     * @param bic the SWIFT Bank Identifier Code (field 8).
     */
    public void setBic( final String bic )
    {
        this.bic = bic;
    }

    /**
     * Getter for property {@code validationLabel}.
     *
     * @return the label for the algorithm to be used for validating account
     * numbers (field 9).
     */
    public String getValidationLabel()
    {
        return this.validationLabel;
    }

    /**
     * Setter for property {@code validationLabel}.
     *
     * @param validationLabel the label for the algorithm to be used for
     * validating account numbers (field 9).
     */
    public void setValidationLabel( final String validationLabel )
    {
        this.validationLabel = validationLabel;
    }

    /**
     * Getter for property {@code serialNumber}.
     *
     * @return the serial number of the record (field 10).
     */
    public Integer getSerialNumber()
    {
        return this.serialNumber;
    }

    /**
     * Setter for property {@code serialNumber}.
     *
     * @param serialNumber the serial number of the record (field 10).
     */
    public void setSerialNumber( final Integer serialNumber )
    {
        this.serialNumber = serialNumber;
    }

    /**
     * Getter for property {@code changeLabel}.
     *
     * @return the label used to indicate changes of the record since
     * previous files (field 11).
     */
    public char getChangeLabel()
    {
        return this.changeLabel;
    }

    /**
     * Setter for property {@code changeLabel}.
     *
     * @param changeLabel the label used to indicate changes of the record since
     * previous files (field 11).
     */
    public void setChangeLabel( final char changeLabel )
    {
        this.changeLabel = changeLabel;
    }

    /**
     * Getter for property {@code markedForDeletion}.
     *
     * @return {@code true} if this record will be deleted from upcoming
     * files; {@code false} if not (field 12).
     */
    public boolean isMarkedForDeletion()
    {
        return this.markedForDeletion;
    }

    /**
     * Setter for property {@code markedForDeletion}.
     *
     * @param markedForDeletion {@code true} if this record will be deleted from
     * upcoming files; {@code false} if not (field 12).
     */
    public void setMarkedForDeletion( final boolean markedForDeletion )
    {
        this.markedForDeletion = markedForDeletion;
    }

    /**
     * Getter for property {@code replacingBankCode}.
     *
     * @return the bank code of the bank replacing this bank if this record
     * is marked for deletion or {@code null} if no replacing bank code is
     * specified or the record is not to be deleted (field 13).
     */
    public Bankleitzahl getReplacingBankCode()
    {
        return this.replacingBankCode;
    }

    /**
     * Setter for property {@code replacingBankCode}.
     *
     * @param replacingBankCode the bank code of the bank replacing this bank if
     * this record is marked for deletion or {@code null} if no replacing bank
     * code is specified or the record is not to be deleted (field 13).
     */
    public void setReplacingBankCode( final Bankleitzahl replacingBankCode )
    {
        this.replacingBankCode = replacingBankCode;
    }

    /**
     * Parses text from a Bankleitzahlendatei to initialize the instance.
     * <p>This method may be used for reading records from the german
     * Bankleitzahlendatei as published by
     * <a href="http://www.bundesbank.de/index.en.php">Deutsche Bundesbank</a>.
     * It supports reading the new format as of june 2006.</p>
     *
     * @param text a line from a Bankleitzahlendatei to parse into the instance.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws IllegalArgumentException if the parse fails.
     */
    public void parse( final String text )
    {
        if ( text == null )
        {
            throw new NullPointerException( "text=" + text );
        }

        Number blz;
        String field;
        final NumberFormat plzFmt = new DecimalFormat( "00000" );
        final NumberFormat serFmt = new DecimalFormat( "000000" );
        final NumberFormat blzFmt = new DecimalFormat( "00000000" );

        try
        {
            // Field 1
            this.bankCode =
                Bankleitzahl.parse( text.substring(
                BankleitzahlInfo.FIELD_TO_OFFSET[0],
                BankleitzahlInfo.FIELD_TO_ENDOFFSET[0] ).
                trim() );

            // Field 2
            this.headOffice =
                "1".equals( text.substring(
                BankleitzahlInfo.FIELD_TO_OFFSET[1],
                BankleitzahlInfo.FIELD_TO_ENDOFFSET[1] ) );

            // Field 3
            this.name = text.substring(
                BankleitzahlInfo.FIELD_TO_OFFSET[2],
                BankleitzahlInfo.FIELD_TO_ENDOFFSET[2] ).
                trim();

            // Field 4
            this.postalCode =
                plzFmt.parse( text.substring(
                BankleitzahlInfo.FIELD_TO_OFFSET[3],
                BankleitzahlInfo.FIELD_TO_ENDOFFSET[3] ).
                trim() ).intValue();

            // Field 5
            this.city = text.substring(
                BankleitzahlInfo.FIELD_TO_OFFSET[4],
                BankleitzahlInfo.FIELD_TO_ENDOFFSET[4] ).
                trim();

            // Field 6
            this.description = text.substring(
                BankleitzahlInfo.FIELD_TO_OFFSET[5],
                BankleitzahlInfo.FIELD_TO_ENDOFFSET[5] ).
                trim();

            // Field 7
            field = text.substring(
                BankleitzahlInfo.FIELD_TO_OFFSET[6],
                BankleitzahlInfo.FIELD_TO_ENDOFFSET[6] ).
                trim();

            this.panInstituteNumber = field.length() > 0
                ? plzFmt.parse( field ).intValue()
                : 0;

            // Field 8
            this.bic = text.substring(
                BankleitzahlInfo.FIELD_TO_OFFSET[7],
                BankleitzahlInfo.FIELD_TO_ENDOFFSET[7] ).
                trim();

            // Field 9
            this.validationLabel = text.substring(
                BankleitzahlInfo.FIELD_TO_OFFSET[8],
                BankleitzahlInfo.FIELD_TO_ENDOFFSET[8] ).
                trim();

            // Field 10
            this.serialNumber = new Integer(
                serFmt.parse( text.substring(
                BankleitzahlInfo.FIELD_TO_OFFSET[9],
                BankleitzahlInfo.FIELD_TO_ENDOFFSET[9] ).
                trim() ).intValue() );

            // Field 11
            this.changeLabel = text.substring(
                BankleitzahlInfo.FIELD_TO_OFFSET[10],
                BankleitzahlInfo.FIELD_TO_ENDOFFSET[10] ).
                toCharArray()[0];

            // Field 12
            this.markedForDeletion =
                "1".equals( text.substring(
                BankleitzahlInfo.FIELD_TO_OFFSET[11],
                BankleitzahlInfo.FIELD_TO_ENDOFFSET[11] ) );

            // Field 13
            blz = blzFmt.parse( text.substring(
                BankleitzahlInfo.FIELD_TO_OFFSET[12],
                BankleitzahlInfo.FIELD_TO_ENDOFFSET[12] ).
                trim() );

            if ( blz.intValue() != 0 )
            {
                this.replacingBankCode = Bankleitzahl.valueOf( blz );
            }
            else
            {
                this.replacingBankCode = null;
            }

        }
        catch ( ParseException e )
        {
            // TODO JDK 1.5: throw new IllegalArgumentException(text, e);
            throw new IllegalArgumentException( text );
        }
        catch ( IndexOutOfBoundsException e )
        {
            // TODO JDK 1.5: throw new IllegalArgumentException(text, e);
            throw new IllegalArgumentException( text );
        }
    }

    /**
     * Creates a string representing the properties of the instance.
     *
     * @return a string representing the properties of the instance.
     */
    private String internalString()
    {
        return new StringBuffer( 500 ).append( '{' ).
            append( "headOffice=" ).append( this.headOffice ).
            append( ", bankCode=" ).append( this.bankCode ).
            append( ", name=" ).append( this.name ).
            append( ", bic=" ).append( this.bic ).
            append( ", changeLabel=" ).append( this.changeLabel ).
            append( ", city=" ).append( this.city ).
            append( ", markedForDeletion=" ).append( this.markedForDeletion ).
            append( ", panInstituteNumber=" ).append( this.panInstituteNumber ).
            append( ", postalCode=" ).append( this.postalCode ).
            append( ", replacingBankCode=" ).append( this.replacingBankCode ).
            append( ", serialNumber=" ).append( this.serialNumber ).
            append( ", description=" ).append( this.description ).
            append( ", validationLabel=" ).append( this.validationLabel ).
            append( '}' ).toString();

    }

    //--------------------------------------------------------BankleitzahlInfo--
    //--Object------------------------------------------------------------------

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this instance.
     */
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            throw new AssertionError( e );
        }
    }

    /**
     * Indicates whether some other object is equal to this one.
     *
     * @param o the reference object with which to compare.
     *
     * @return {@code true} if this object is the same as {@code o};
     * {@code false} otherwise.
     */
    public boolean equals( final Object o )
    {
        boolean ret = o == this;

        if ( !ret && o instanceof BankleitzahlInfo )
        {
            final BankleitzahlInfo that = (BankleitzahlInfo) o;
            ret = ( this.serialNumber == null
                ? that.serialNumber == null
                : this.serialNumber.equals( that.serialNumber ) );

        }

        return ret;
    }

    /**
     * Returns a hash code value for this object.
     *
     * @return a hash code value for this object.
     */
    public int hashCode()
    {
        return this.serialNumber == null
            ? 0
            : this.serialNumber.intValue();
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    public String toString()
    {
        return super.toString() + this.internalString();
    }

    //------------------------------------------------------------------Object--
}

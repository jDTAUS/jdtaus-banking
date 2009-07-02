/*
 *  jDTAUS Banking RI CurrencyDirectory
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
package org.jdtaus.banking.ri.currencydir;

import java.io.Serializable;
import java.util.Date;

/**
 * Currency.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class XMLCurrency implements Serializable, Cloneable
{
    //--Constants---------------------------------------------------------------

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = 3499875740280116856L;

    //---------------------------------------------------------------Constants--
    //--XMLCurrency-------------------------------------------------------------

    /**
     * ISO currency code.
     * @serial
     */
    private String isoCode;

    /**
     * DTAUS currency code.
     * @serial
     */
    private Character dtausCode;

    /**
     * Start date.
     * @serial
     */
    private Date startDate;

    /**
     * End date.
     * @serial
     */
    private Date endDate;

    /** Cached hash-code. */
    private transient int hashCode = NO_HASHCODE;

    /** Constant for field {@code hashCode} forcing hash code computation. */
    private static final int NO_HASHCODE = Integer.MIN_VALUE;

    /**
     * Gets the ISO currency code.
     *
     * @return the ISO currency code.
     */
    public String getIsoCode()
    {
        return this.isoCode;
    }

    /**
     * Sets the ISO currency code.
     *
     * @param value the ISO currency code.
     */
    public void setIsoCode( final String value )
    {
        this.isoCode = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Gets the DTAUS currency code.
     *
     * @return the DTAUS currency code or {@code null}.
     */
    public Character getDtausCode()
    {
        return this.dtausCode;
    }

    /**
     * Sets the DTAUS currency code.
     *
     * @param value the DTAUS currency code or {@code null}.
     */
    public void setDtausCode( final Character value )
    {
        this.dtausCode = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Gets the start date.
     *
     * @return the start date..
     */
    public Date getStartDate()
    {
        return this.startDate;
    }

    /**
     * Sets the start date.
     *
     * @param value the start date.
     */
    public void setStartDate( final Date value )
    {
        this.startDate = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Gets the end date.
     *
     * @return the end date..
     */
    public Date getEndDate()
    {
        return this.endDate;
    }

    /**
     * Sets the end date.
     *
     * @param value the end date.
     */
    public void setEndDate( final Date value )
    {
        this.endDate = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Checks that the currency is valid at a given date.
     *
     * @param date the date with which to check.
     *
     * @return {@code true}, if the currency is valid at {@code date};
     * {@code false} if not.
     *
     * @throws NullPointerException if {@code date} is {@code null}.
     */
    public boolean isValidAt( final Date date )
    {
        if ( date == null )
        {
            throw new NullPointerException( "date" );
        }

        return ( date.equals( this.getStartDate() ) ||
            date.after( this.getStartDate() ) ) &&
            ( this.getEndDate() == null ||
            date.equals( this.getEndDate() ) ||
            date.before( this.getEndDate() ) );

    }

    /**
     * Creates a string representing the properties of the instance.
     *
     * @return a string representing the properties of the instance.
     */
    private String internalString()
    {
        return new StringBuffer( 200 ).append( '{' ).
            append( "isoCode=" ).append( this.isoCode ).
            append( ", dtausCode=" ).append( this.dtausCode ).
            append( ", startDate=" ).append( this.startDate ).
            append( ", endDate=" ).append( this.endDate ).
            append( '}' ).toString();

    }

    //-------------------------------------------------------------XMLCurrency--
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
            final XMLCurrency ret = (XMLCurrency) super.clone();
            if ( this.startDate != null )
            {
                ret.startDate = (Date) this.startDate.clone();
            }
            if ( this.endDate != null )
            {
                ret.endDate = (Date) this.endDate.clone();
            }

            return ret;
        }
        catch ( CloneNotSupportedException e )
        {
            throw new AssertionError( e );
        }
    }

    /**
     * Indicates whether some other object is equal to this one by comparing
     * the values of all properties.
     *
     * @param o the reference object with which to compare.
     *
     * @return {@code true} if this object is the same as {@code o};
     * {@code false} otherwise.
     */
    public boolean equals( final Object o )
    {
        boolean ret = o == this;

        if ( !ret && o instanceof XMLCurrency )
        {
            final XMLCurrency that = (XMLCurrency) o;
            ret =
                ( this.getIsoCode() == null
                ? that.getIsoCode() == null
                : this.getIsoCode().equals( that.getIsoCode() ) ) &&
                ( this.getDtausCode() == that.getDtausCode() ) &&
                ( this.getStartDate() == null
                ? that.getStartDate() == null
                : this.getStartDate().equals( that.getStartDate() ) ) &&
                ( this.getEndDate() == null
                ? that.getEndDate() == null
                : this.getEndDate().equals( that.getEndDate() ) );

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
        if ( this.hashCode == NO_HASHCODE )
        {
            int hc = 23;

            hc = 37 * hc + ( this.dtausCode == null
                ? 0
                : (int) this.dtausCode.charValue() );

            hc = 37 * hc + ( this.isoCode == null
                ? 0
                : this.isoCode.hashCode() );

            hc = 37 * hc + ( this.startDate == null
                ? 0
                : this.startDate.hashCode() );

            hc = 37 * hc + ( this.endDate == null
                ? 0
                : this.endDate.hashCode() );

            this.hashCode = hc;
        }

        return this.hashCode;
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

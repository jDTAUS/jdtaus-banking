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
package org.jdtaus.banking.ri.currencydir;

import java.io.Serializable;
import java.util.Date;

/**
 * XML representation of a currency.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class XMLCurrency implements Serializable, Cloneable
{
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
    public void setIsoCode(final String value)
    {
        this.isoCode = value;
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
    public void setDtausCode(final Character value)
    {
        this.dtausCode = value;
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
    public void setStartDate(final Date value)
    {
        this.startDate = value;
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
    public void setEndDate(final Date value)
    {
        this.endDate = value;
    }

    /**
     * Creates a string representing the properties of the instance.
     *
     * @return a string representing the properties of the instance.
     */
    private String internalString()
    {
        return new StringBuffer(200).
            append("\n\tisoCode=").append(this.isoCode).
            append("\n\tdtausCode=").append(this.dtausCode).
            append("\n\tstartDate=").append(this.startDate).
            append("\n\tendDate=").append(this.endDate).
            toString();

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
            if(this.startDate != null)
            {
                ret.startDate = (Date) this.startDate.clone();
            }
            if(this.endDate != null)
            {
                ret.endDate = (Date) this.endDate.clone();
            }

            return ret;
        }
        catch(CloneNotSupportedException e)
        {
            throw new AssertionError(e);
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
    public boolean equals(final Object o)
    {
        boolean ret = o == this;

        if(!ret && o instanceof XMLCurrency)
        {
            final XMLCurrency that = (XMLCurrency) o;
            ret =
                (this.isoCode == null ? that.isoCode == null :
                    this.isoCode.equals(that.isoCode)) &&
                (this.dtausCode == that.dtausCode) &&
                (this.startDate == null ? that.startDate == null :
                    this.startDate.equals(that.startDate)) &&
                (this.endDate == null ? that.endDate == null :
                    this.endDate.equals(that.endDate));

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
        return this.internalString().hashCode();
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

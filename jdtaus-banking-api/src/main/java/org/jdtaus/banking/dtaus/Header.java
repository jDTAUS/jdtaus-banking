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
package org.jdtaus.banking.dtaus;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import org.jdtaus.banking.AlphaNumericText27;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.Kontonummer;
import org.jdtaus.banking.Referenznummer10;

/**
 * "A" record.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class Header implements Cloneable, Serializable
{

    /** Constant for the name of property {@code customer}. */
    public static final String PROP_CUSTOMER = "org.jdtaus.banking.dtaus.Header.PROP_CUSTOMER";

    /** Constant for the name of property {@code account}. */
    public static final String PROP_ACCOUNT = "org.jdtaus.banking.dtaus.Header.PROP_ACCOUNT";

    /** Constant for the name of property {@code bank}. */
    public static final String PROP_BANK = "org.jdtaus.banking.dtaus.Header.PROP_BANK";

    /** Constant for the name of property {@code type}. */
    public static final String PROP_TYPE = "org.jdtaus.banking.dtaus.Header.PROP_TYPE";

    /** Constant for the name of property {@code currency}. */
    public static final String PROP_CURRENCY = "org.jdtaus.banking.dtaus.Header.PROP_CURRENCY";

    /** Constant for the name of property {@code reference}. */
    public static final String PROP_REFERENCE = "org.jdtaus.banking.dtaus.Header.PROP_REFERENCE";

    /** Constant for the name of property {@code bankData}. */
    public static final String PROP_BANKDATA = "org.jdtaus.banking.dtaus.Header.PROP_BANKDATA";

    /** Constant for the name of property {@code createDate}. */
    public static final String PROP_CREATEDATE = "org.jdtaus.banking.dtaus.Header.PROP_CREATEDATE";

    /** Constant for the name of property {@code executionDate}. */
    public static final String PROP_EXECUTIONDATE = "org.jdtaus.banking.dtaus.Header.PROP_EXECUTIONDATE";

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = -5704199858711059732L;

    /**
     * Name of the customer a logical file belongs to.
     * @serial
     */
    private AlphaNumericText27 customer;

    /**
     * Code of the account a logical file belongs to.
     * @serial
     */
    private Kontonummer account;

    /**
     * Code of the bank a logical file belongs to.
     * @serial
     */
    private Bankleitzahl bank;

    /**
     * Type of a logical file.
     * @serial
     */
    private LogicalFileType type;

    /**
     * Currency of a logical file.
     * @serial
     */
    private Currency currency;

    /**
     * Reference code of a logical file.
     * @serial
     */
    private Referenznummer10 reference;

    /**
     * Create date of a logical file.
     * @serial
     */
    private Date createDate;

    /**
     * Execution date of a logical file.
     * @serial
     */
    private Date executionDate;

    /**
     * Bank internal data.
     * @serial
     */
    private Bankleitzahl bankData;

    /** Cached hash code. */
    private transient int hashCode = NO_HASHCODE;
    private static final int NO_HASHCODE = Integer.MIN_VALUE;

    /** Creates a new {@code Header} instance. */
    public Header()
    {
        super();
    }

    /**
     * Getter for property {@code customer}.
     *
     * @return Name of the customer a logical file belongs to.
     */
    public AlphaNumericText27 getCustomer()
    {
        return this.customer;
    }

    /**
     * Setter for property {@code customer}.
     *
     * @param value Name of the customer a logical file belongs to.
     */
    public void setCustomer( final AlphaNumericText27 value )
    {
        this.customer = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code account}.
     *
     * @return Code of the account a logical file belongs to.
     */
    public Kontonummer getAccount()
    {
        return this.account;
    }

    /**
     * Setter for property {@code account}.
     *
     * @param value Code of the account a logical file belongs to.
     */
    public void setAccount( final Kontonummer value )
    {
        this.account = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code bank}.
     *
     * @return Code of the bank a logical file belongs to.
     */
    public Bankleitzahl getBank()
    {
        return this.bank;
    }

    /**
     * Setter for property {@code bank}.
     *
     * @param value Code of the bank a logical file belongs to.
     */
    public void setBank( final Bankleitzahl value )
    {
        this.bank = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code type}.
     *
     * @return The type of a logical file.
     */
    public LogicalFileType getType()
    {
        return this.type;
    }

    /**
     * Setter for property {@code type}.
     *
     * @param value The type of a logical file.
     */
    public void setType( final LogicalFileType value )
    {
        this.type = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code currency}.
     *
     * @return Currency of a logical file.
     */
    public Currency getCurrency()
    {
        return this.currency;
    }

    /**
     * Setter for property {@code currency}.
     *
     * @param value Currency for a logical file.
     */
    public void setCurrency( final Currency value )
    {
        this.currency = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code reference}.
     *
     * @return Reference code of a logical file or {@code null}.
     */
    public Referenznummer10 getReference()
    {
        return this.reference;
    }

    /**
     * Setter for property {@code reference}.
     *
     * @param value Reference code of a logical file or {@code null}.
     */
    public void setReference( final Referenznummer10 value )
    {
        this.reference = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code createDate}.
     *
     * @return Create date of a logical file.
     */
    public Date getCreateDate()
    {
        return this.createDate != null ? (Date) this.createDate.clone() : null;
    }

    /**
     * Setter for property {@code createDate}.
     *
     * @param value Create date of a logical file.
     */
    public void setCreateDate( final Date value )
    {
        this.createDate = null;
        this.hashCode = NO_HASHCODE;

        if ( value != null )
        {
            final Calendar cal = Calendar.getInstance();
            cal.setTime( value );
            cal.clear( Calendar.HOUR_OF_DAY );
            cal.clear( Calendar.MINUTE );
            cal.clear( Calendar.SECOND );
            cal.clear( Calendar.MILLISECOND );
            this.createDate = cal.getTime();
        }
    }

    /**
     * Getter for property {@code executionDate}.
     *
     * @return Execution date of a logical file or {@code null}.
     */
    public Date getExecutionDate()
    {
        return this.executionDate != null ? (Date) this.executionDate.clone() : null;
    }

    /**
     * Setter for property {@code executionDate}.
     *
     * @param value Execution date of a logical file or {@code null}.
     */
    public void setExecutionDate( final Date value )
    {
        this.executionDate = null;
        this.hashCode = NO_HASHCODE;

        if ( value != null )
        {
            final Calendar cal = Calendar.getInstance();
            cal.setTime( value );
            cal.clear( Calendar.HOUR_OF_DAY );
            cal.clear( Calendar.MINUTE );
            cal.clear( Calendar.SECOND );
            cal.clear( Calendar.MILLISECOND );
            this.executionDate = cal.getTime();
        }
    }

    /**
     * Getter for property {@code bankData}.
     *
     * @return Bank internal data.
     */
    public Bankleitzahl getBankData()
    {
        return this.bankData;
    }

    /**
     * Setter for property {@code bankData}.
     *
     * @param value Bank internal data.
     */
    public void setBankData( final Bankleitzahl value )
    {
        this.bankData = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Indicates whether some other object is equal to this one by comparing the values of all properties.
     *
     * @param o The reference object with which to compare.
     *
     * @return {@code true} if this object is the same as {@code o}; {@code false} otherwise.
     */
    public boolean equals( final Object o )
    {
        boolean ret = o == this;

        if ( !ret && o instanceof Header )
        {
            final Header that = (Header) o;
            ret = ( this.getAccount() == null
                    ? that.getAccount() == null : this.getAccount().equals( that.getAccount() ) ) &&
                  ( this.getBank() == null
                    ? that.getBank() == null : this.getBank().equals( that.getBank() ) ) &&
                  ( this.getBankData() == null
                    ? that.getBankData() == null : this.getBankData().equals( that.getBankData() ) ) &&
                  ( this.getCurrency() == null
                    ? that.getCurrency() == null : this.getCurrency().equals( that.getCurrency() ) ) &&
                  ( this.getCustomer() == null
                    ? that.getCustomer() == null : this.getCustomer().equals( that.getCustomer() ) ) &&
                  ( this.getType() == null
                    ? that.getType() == null : this.getType().equals( that.getType() ) ) &&
                  ( this.getReference() == null
                    ? that.getReference() == null : this.getReference().equals( that.getReference() ) ) &&
                  ( this.getCreateDate() == null
                    ? that.getCreateDate() == null : this.getCreateDate().equals( that.getCreateDate() ) ) &&
                  ( this.getExecutionDate() == null
                    ? that.getExecutionDate() == null : this.getExecutionDate().equals( that.getExecutionDate() ) );

        }

        return ret;
    }

    /**
     * Returns a hash code value for this object.
     *
     * @return A hash code value for this object.
     */
    public int hashCode()
    {
        if ( this.hashCode == NO_HASHCODE )
        {
            int hc = 23;
            hc = 37 * hc + ( this.account == null ? 0 : this.account.hashCode() );
            hc = 37 * hc + ( this.bank == null ? 0 : this.bank.hashCode() );
            hc = 37 * hc + ( this.bankData == null ? 0 : this.bankData.hashCode() );
            hc = 37 * hc + ( this.currency == null ? 0 : this.currency.hashCode() );
            hc = 37 * hc + ( this.customer == null ? 0 : this.customer.hashCode() );
            hc = 37 * hc + ( this.type == null ? 0 : this.type.hashCode() );
            hc = 37 * hc + ( this.reference == null ? 0 : this.reference.hashCode() );
            hc = 37 * hc + ( this.createDate == null ? 0 : this.createDate.hashCode() );
            hc = 37 * hc + ( this.executionDate == null ? 0 : this.executionDate.hashCode() );
            this.hashCode = hc;
        }

        return this.hashCode;
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return A clone of this instance.
     */
    public Object clone()
    {
        try
        {
            final Object o = super.clone();
            if ( this.createDate != null )
            {
                ( (Header) o ).createDate = (Date) this.createDate.clone();
            }
            if ( this.executionDate != null )
            {
                ( (Header) o ).executionDate = (Date) this.executionDate.clone();
            }

            return o;
        }
        catch ( final CloneNotSupportedException e )
        {
            throw new AssertionError( e );
        }
    }

    /**
     * Returns a string representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString()
    {
        return super.toString() + this.internalString();
    }

    /**
     * Creates a string representing the properties of the instance.
     *
     * @return A string representing the properties of the instance.
     */
    private String internalString()
    {
        return new StringBuffer( 150 ).append( '{' ).
            append( "account=" ).append( this.account ).
            append( ", bank=" ).append( this.bank ).
            append( ", bankData=" ).append( this.bankData ).
            append( ", currency=" ).append( this.currency ).
            append( ", customer=" ).append( this.customer ).
            append( ", createDate=" ).append( this.createDate ).
            append( ", executionDate=" ).append( this.executionDate ).
            append( ", type=" ).append( this.type ).
            append( ", reference=" ).append( this.reference ).
            append( '}' ).toString();

    }

}

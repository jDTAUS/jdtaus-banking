/*
 *  jDTAUS Banking API
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
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class Header implements Cloneable, Serializable
{
    //--Constants---------------------------------------------------------------

    /** Constant for the name of property {@code customer}. */
    public static final String PROP_CUSTOMER =
        Header.class.getName() + ".PROP_CUSTOMER";

    /** Constant for the name of property {@code account}. */
    public static final String PROP_ACCOUNT =
        Header.class.getName() + ".PROP_ACCOUNT";

    /** Constant for the name of property {@code bank}. */
    public static final String PROP_BANK =
        Header.class.getName() + ".PROP_BANK";

    /** Constant for the name of property {@code type}. */
    public static final String PROP_TYPE =
        Header.class.getName() + ".PROP_TYPE";

    /** Constant for the name of property {@code currency}. */
    public static final String PROP_CURRENCY =
        Header.class.getName() + ".PROP_CURRENCY";

    /** Constant for the name of property {@code reference}. */
    public static final String PROP_REFERENCE =
        Header.class.getName() + ".PROP_REFERENCE";

    /** Constant for the name of property {@code bankData}. */
    public static final String PROP_BANKDATA =
        Header.class.getName() + ".PROP_BANKDATA";

    /** Constant for the name of property {@code createDate}. */
    public static final String PROP_CREATEDATE =
        Header.class.getName() + ".PROP_CREATEDATE";

    /** Constant for the name of property {@code executionDate}. */
    public static final String PROP_EXECUTIONDATE =
        Header.class.getName() + ".PROP_EXECUTIONDATE";

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = -5704199858711059732L;

    //---------------------------------------------------------------Constants--
    //--Header------------------------------------------------------------------

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

    /**
     * Getter for property {@code customer}.
     *
     * @return name of the customer a logical file belongs to.
     */
    public AlphaNumericText27 getCustomer()
    {
        return this.customer;
    }

    /**
     * Setter for property {@code customer}.
     *
     * @param customer name of the customer a logical file belongs to.
     */
    public void setCustomer( final AlphaNumericText27 customer )
    {
        this.customer = customer;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code account}.
     *
     * @return code of the account a logical file belongs to.
     */
    public Kontonummer getAccount()
    {
        return this.account;
    }

    /**
     * Setter for property {@code account}.
     *
     * @param account code of the account a logical file belongs to.
     */
    public void setAccount( final Kontonummer account )
    {
        this.account = account;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code bank}.
     *
     * @return code of the bank a logical file belongs to.
     */
    public Bankleitzahl getBank()
    {
        return this.bank;
    }

    /**
     * Setter for property {@code bank}.
     *
     * @param bank code of the bank a logical file belongs to.
     */
    public void setBank( final Bankleitzahl bank )
    {
        this.bank = bank;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code type}.
     *
     * @return the type of a logical file.
     */
    public LogicalFileType getType()
    {
        return this.type;
    }

    /**
     * Setter for property {@code type}.
     *
     * @param type the type of a logical file.
     */
    public void setType( final LogicalFileType type )
    {
        this.type = type;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code currency}.
     *
     * @return currency of a logical file.
     */
    public Currency getCurrency()
    {
        return this.currency;
    }

    /**
     * Setter for property {@code currency}.
     *
     * @param currency currency for a logical file.
     */
    public void setCurrency( final Currency currency )
    {
        this.currency = currency;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code reference}.
     *
     * @return reference code of a logical file or {@code null}.
     */
    public Referenznummer10 getReference()
    {
        return this.reference;
    }

    /**
     * Setter for property {@code reference}.
     *
     * @param reference reference code of a logical file or {@code null}.
     */
    public void setReference( final Referenznummer10 reference )
    {
        this.reference = reference;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code createDate}.
     *
     * @return create date of a logical file.
     */
    public Date getCreateDate()
    {
        return this.createDate != null
            ? (Date) this.createDate.clone()
            : null;
    }

    /**
     * Setter for property {@code createDate}.
     *
     * @param value create date of a logical file.
     */
    public void setCreateDate( Date value )
    {
        if ( value != null )
        {
            final Calendar cal = Calendar.getInstance();
            cal.setTime( value );
            cal.clear( Calendar.HOUR_OF_DAY );
            cal.clear( Calendar.MINUTE );
            cal.clear( Calendar.SECOND );
            cal.clear( Calendar.MILLISECOND );
            value = cal.getTime();
        }

        this.createDate = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code executionDate}.
     *
     * @return execution date of a logical file or {@code null}.
     */
    public Date getExecutionDate()
    {
        return this.executionDate != null
            ? (Date) this.executionDate.clone()
            : null;

    }

    /**
     * Setter for property {@code executionDate}.
     *
     * @param value execution date of a logical file or {@code null}.
     */
    public void setExecutionDate( Date value )
    {
        if ( value != null )
        {
            final Calendar cal = Calendar.getInstance();
            cal.setTime( value );
            cal.clear( Calendar.HOUR_OF_DAY );
            cal.clear( Calendar.MINUTE );
            cal.clear( Calendar.SECOND );
            cal.clear( Calendar.MILLISECOND );
            value = cal.getTime();
        }

        this.executionDate = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code bankData}.
     *
     * @return bank internal data.
     */
    public Bankleitzahl getBankData()
    {
        return this.bankData;
    }

    /**
     * Setter for property {@code bankData}.
     *
     * @param bankData bank internal data.
     */
    public void setBankData( final Bankleitzahl bankData )
    {
        this.bankData = bankData;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Creates a string representing the properties of the instance.
     *
     * @return a string representing the properties of the instance.
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

    //------------------------------------------------------------------Header--
    //--Object------------------------------------------------------------------

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

        if ( !ret && o instanceof Header )
        {
            final Header h = (Header) o;
            ret =
                ( this.getAccount() == null
                ? h.getAccount() == null
                : this.getAccount().equals( h.getAccount() ) ) &&
                ( this.getBank() == null
                ? h.getBank() == null
                : this.getBank().equals( h.getBank() ) ) &&
                ( this.getBankData() == null
                ? h.getBankData() == null
                : this.getBankData().equals( h.getBankData() ) ) &&
                ( this.getCurrency() == null
                ? h.getCurrency() == null
                : this.getCurrency().equals( h.getCurrency() ) ) &&
                ( this.getCustomer() == null
                ? h.getCustomer() == null
                : this.getCustomer().equals( h.getCustomer() ) ) &&
                ( this.getType() == null
                ? h.getType() == null
                : this.getType().equals( h.getType() ) ) &&
                ( this.getReference() == null
                ? h.getReference() == null
                : this.getReference().equals( h.getReference() ) ) &&
                ( this.getCreateDate() == null
                ? h.getCreateDate() == null
                : this.getCreateDate().equals( h.getCreateDate() ) ) &&
                ( this.getExecutionDate() == null
                ? h.getExecutionDate() == null
                : this.getExecutionDate().equals( h.getExecutionDate() ) );

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

            hc = 37 * hc + ( this.account == null
                ? 0
                : this.account.hashCode() );

            hc = 37 * hc + ( this.bank == null
                ? 0
                : this.bank.hashCode() );

            hc = 37 * hc + ( this.bankData == null
                ? 0
                : this.bankData.hashCode() );

            hc = 37 * hc + ( this.currency == null
                ? 0
                : this.currency.hashCode() );

            hc = 37 * hc + ( this.customer == null
                ? 0
                : this.customer.hashCode() );

            hc = 37 * hc + ( this.type == null
                ? 0
                : this.type.hashCode() );
            hc = 37 * hc + ( this.reference == null
                ? 0
                : this.reference.hashCode() );

            hc = 37 * hc + ( this.createDate == null
                ? 0
                : this.createDate.hashCode() );

            hc = 37 * hc + ( this.executionDate == null
                ? 0
                : this.executionDate.hashCode() );

            this.hashCode = hc;
        }

        return this.hashCode;
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this instance.
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
                ( (Header) o ).executionDate =
                    (Date) this.executionDate.clone();
            }

            return o;
        }
        catch ( CloneNotSupportedException e )
        {
            throw new AssertionError( e );
        }
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

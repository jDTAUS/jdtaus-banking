/*
 *  jDTAUS Banking API
 *  Copyright (C) 2005 Christian Schulte
 *  <schulte2005@users.sourceforge.net>
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
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Currency;
import org.jdtaus.banking.AlphaNumericText27;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.Kontonummer;
import org.jdtaus.banking.Referenznummer11;
import org.jdtaus.banking.Textschluessel;

/**
 * "C" record.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $JDTAUS$
 */
public class Transaction implements Cloneable, Serializable
{

    /** Constant for the name of property {@code type}. */
    public static final String PROP_TYPE = "org.jdtaus.banking.dtaus.Transaction.PROP_TYPE";

    /** Constant for the name of property {@code amount}. */
    public static final String PROP_AMOUNT = "org.jdtaus.banking.dtaus.Transaction.PROP_AMOUNT";

    /** Constant for the name of property {@code reference}. */
    public static final String PROP_REFERENCE = "org.jdtaus.banking.dtaus.Transaction.PROP_REFERENCE";

    /** Constant for the name of property {@code descriptions}. */
    public static final String PROP_DESCRIPTIONS = "org.jdtaus.banking.dtaus.Transaction.PROP_DESCRIPTIONS";

    /** Constant for the name of property {@code primaryBank}. */
    public static final String PROP_PRIMARYBANK = "org.jdtaus.banking.dtaus.Transaction.PROP_PRIMARYBANK";

    /** Constant for the name of property {@code executiveAccount}. */
    public static final String PROP_EXECUTIVEACCOUNT = "org.jdtaus.banking.dtaus.Transaction.PROP_EXECUTIVEACCOUNT";

    /** Constant for the name of property {@code executiveBank}. */
    public static final String PROP_EXECUTIVEBANK = "org.jdtaus.banking.dtaus.Transaction.PROP_EXECUTIVEBANK";

    /** Constant for the name of property {@code executiveName}. */
    public static final String PROP_EXECUTIVENAME = "org.jdtaus.banking.dtaus.Transaction.PROP_EXECUTIVENAME";

    /** Constant for the name of property {@code executiveExt}. */
    public static final String PROP_EXECUTIVEEXT = "org.jdtaus.banking.dtaus.Transaction.PROP_EXECUTIVEEXT";

    /** Constant for the name of property {@code targetAccount}. */
    public static final String PROP_TARGETACCOUNT = "org.jdtaus.banking.dtaus.Transaction.PROP_TARGETACCOUNT";

    /** Constant for the name of property {@code targetBank}. */
    public static final String PROP_TARGETBANK = "org.jdtaus.banking.dtaus.Transaction.PROP_TARGETBANK";

    /** Constant for the name of property {@code targetName}. */
    public static final String PROP_TARGETNAME = "org.jdtaus.banking.dtaus.Transaction.PROP_TARGETNAME";

    /** Constant for the name of property {@code targetExt}. */
    public static final String PROP_TARGETEXT = "org.jdtaus.banking.dtaus.Transaction.PROP_TARGETEXT";

    /** Constant for the name of property {@code currency}. */
    public static final String PROP_CURRENCY = "org.jdtaus.banking.dtaus.Transaction.PROP_CURRENCY";

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = 1450133285078489771L;

    /**
     * Payment type.
     * @serial
     */
    private Textschluessel type;

    /**
     * Amount of money to transfer.
     * @serial
     */
    private BigInteger amount;

    /**
     * The currency of the transaction.
     * @serial
     */
    private Currency currency;

    /**
     * Reference number of the transaction.
     * @serial
     */
    private Referenznummer11 reference;

    /**
     * Descriptions of the transaction.
     * @serial
     */
    private AlphaNumericText27[] descriptions;

    /**
     * Code of the primary participating bank.
     * @serial
     */
    private Bankleitzahl primaryBank;

    /**
     * Ordering bank account.
     * @serial
     */
    private Kontonummer executiveAccount;

    /**
     * Ordering bank.
     * @serial
     */
    private Bankleitzahl executiveBank;

    /**
     * Ordering customer.
     * @serial
     */
    private AlphaNumericText27 executiveName;

    /**
     * Extension to property {@code executiveName}.
     * @serial
     */
    private AlphaNumericText27 executiveExt;

    /**
     * Bank account of the debitor/creditor depending on the transaction's type.
     * @serial
     */
    private Kontonummer targetAccount;

    /**
     * Bank of the debitor/creditor depending on the transaction's type.
     * @serial
     */
    private Bankleitzahl targetBank;

    /**
     * Debitor/creditor name depending on the transaction's type.
     * @serial
     */
    private AlphaNumericText27 targetName;

    /**
     * Extension to property {@code targetName}.
     * @serial
     */
    private AlphaNumericText27 targetExt;

    /** Cached hash code. */
    private transient int hashCode = NO_HASHCODE;
    private static final int NO_HASHCODE = Integer.MIN_VALUE;

    /** Creates a new {@code Transaction} instance. */
    public Transaction()
    {
        super();
    }

    /**
     * Getter for property {@code type}.
     *
     * @return Payment type of the transaction.
     */
    public Textschluessel getType()
    {
        return this.type;
    }

    /**
     * Setter for property {@code type}.
     *
     * @param value Payment type of the transaction.
     */
    public void setType( final Textschluessel value )
    {
        this.type = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code amount}.
     *
     * @return Amount of money to transfer in the smallest unit of the currency (&euro; = Cent).
     */
    public BigInteger getAmount()
    {
        return this.amount;
    }

    /**
     * Setter for property {@code amount}.
     *
     * @param value The amount of money to transfer in the smallest unit of the currency (&euro; = Cent).
     */
    public void setAmount( final BigInteger value )
    {
        this.amount = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code currency}.
     *
     * @return Currency of the transaction.
     */
    public Currency getCurrency()
    {
        return this.currency;
    }

    /**
     * Setter for property {@code currency}.
     *
     * @param value Currency of the transaction.
     */
    public void setCurrency( final Currency value )
    {
        this.currency = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code reference}.
     *
     * @return Reference number of the transaction or {@code null}.
     */
    public Referenznummer11 getReference()
    {
        return this.reference;
    }

    /**
     * Setter for property {@code reference}.
     *
     * @param value Reference number of the transaction or {@code null}.
     */
    public void setReference( final Referenznummer11 value )
    {
        this.reference = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Gets all descriptions of the transaction.
     *
     * @return All descriptions of the transaction or an empty array if the transaction does not hold any description.
     */
    public AlphaNumericText27[] getDescriptions()
    {
        if ( this.descriptions == null )
        {
            this.descriptions = new AlphaNumericText27[ 0 ];
            this.hashCode = NO_HASHCODE;
        }

        return (AlphaNumericText27[]) this.descriptions.clone();
    }

    /**
     * Sets all descriptions of the transaction.
     *
     * @param value All descriptions of the transaction or {@code null}.
     */
    public void setDescriptions( final AlphaNumericText27[] value )
    {
        this.descriptions = value != null ? (AlphaNumericText27[]) value.clone() : null;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code primaryBank}.
     *
     * @return Code of the primary participating bank or {@code null}.
     */
    public Bankleitzahl getPrimaryBank()
    {
        return this.primaryBank;
    }

    /**
     * Setter for property {@code primaryBank}.
     *
     * @param value Code of the primary participating bank or {@code null}.
     */
    public void setPrimaryBank( final Bankleitzahl value )
    {
        this.primaryBank = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code executiveAccount}.
     *
     * @return Bank account of the orderer requesting the transaction.
     */
    public Kontonummer getExecutiveAccount()
    {
        return this.executiveAccount;
    }

    /**
     * Setter for property {@code executiveAccount}.
     *
     * @param value Bank account of the orderer requesting the transaction.
     */
    public void setExecutiveAccount( final Kontonummer value )
    {
        this.executiveAccount = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code executiveBank}.
     *
     * @return Ordering bank requesting the transaction.
     */
    public Bankleitzahl getExecutiveBank()
    {
        return this.executiveBank;
    }

    /**
     * Setter for property {@code executiveBank}.
     *
     * @param value Ordering bank requesting the transaction.
     */
    public void setExecutiveBank( final Bankleitzahl value )
    {
        this.executiveBank = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code executiveName}.
     *
     * @return Ordering customer requesting the transaction.
     */
    public AlphaNumericText27 getExecutiveName()
    {
        return this.executiveName;
    }

    /**
     * Setter for property {@code executiveName}.
     *
     * @param value Ordering customer requesting the transaction.
     */
    public void setExecutiveName( final AlphaNumericText27 value )
    {
        this.executiveName = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code executiveExt}.
     *
     * @return Additional data extending the executive name or {@code null}.
     */
    public AlphaNumericText27 getExecutiveExt()
    {
        return this.executiveExt;
    }

    /**
     * Setter for property {@code executiveExt}.
     *
     * @param value Additional data extending the executive name or {@code null}.
     */
    public void setExecutiveExt( final AlphaNumericText27 value )
    {
        this.executiveExt = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code targetAccount}.
     *
     * @return Bank account of the debitor/creditor depending on the transaction's type.
     */
    public Kontonummer getTargetAccount()
    {
        return this.targetAccount;
    }

    /**
     * Setter for property {@code targetAccount}.
     *
     * @param value Bank account of the debitor/creditor depending on the transaction's type.
     */
    public void setTargetAccount( final Kontonummer value )
    {
        this.targetAccount = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code targetBank}.
     *
     * @return Bank of the debitor/creditor depending on the transaction's type.
     */
    public Bankleitzahl getTargetBank()
    {
        return this.targetBank;
    }

    /**
     * Setter for property {@code targetBank}.
     *
     * @param value Bank of the debitor/creditor depending on the transaction's type.
     */
    public void setTargetBank( final Bankleitzahl value )
    {
        this.targetBank = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code targetName}.
     *
     * @return Debitor/creditor depending on the transaction's type.
     */
    public AlphaNumericText27 getTargetName()
    {
        return this.targetName;
    }

    /**
     * Setter for property {@code targetName}.
     *
     * @param value Debitor/creditor depending on the transaction's type.
     */
    public void setTargetName( final AlphaNumericText27 value )
    {
        this.targetName = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code targetExt}.
     *
     * @return Additional data extending the target name or {@code null}.
     */
    public AlphaNumericText27 getTargetExt()
    {
        return this.targetExt;
    }

    /**
     * Setter for property {@code targetExt}.
     *
     * @param value Additional data extending the target name or {@code null}.
     */
    public void setTargetExt( final AlphaNumericText27 value )
    {
        this.targetExt = value;
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
        boolean equal = this == o;

        if ( !equal && o instanceof Transaction )
        {
            final Transaction that = (Transaction) o;
            equal = ( this.getAmount() == null
                      ? that.getAmount() == null : this.getAmount().equals( that.getAmount() ) ) &&
                    ( this.getCurrency() == null
                      ? that.getCurrency() == null : this.getCurrency().equals( that.getCurrency() ) ) &&
                    ( this.getExecutiveAccount() == null
                      ? that.getExecutiveAccount() == null
                      : this.getExecutiveAccount().equals( that.getExecutiveAccount() ) ) &&
                    ( this.getExecutiveBank() == null
                      ? that.getExecutiveBank() == null : this.getExecutiveBank().equals( that.getExecutiveBank() ) ) &&
                    ( this.getExecutiveName() == null
                      ? that.getExecutiveName() == null : this.getExecutiveName().equals( that.getExecutiveName() ) ) &&
                    ( this.getExecutiveExt() == null
                      ? that.getExecutiveExt() == null : this.getExecutiveExt().equals( that.getExecutiveExt() ) ) &&
                    ( this.getPrimaryBank() == null
                      ? that.getPrimaryBank() == null : this.getPrimaryBank().equals( that.getPrimaryBank() ) ) &&
                    ( this.getReference() == null
                      ? that.getReference() == null : this.getReference().equals( that.getReference() ) ) &&
                    ( this.getTargetAccount() == null
                      ? that.getTargetAccount() == null : this.getTargetAccount().equals( that.getTargetAccount() ) ) &&
                    ( this.getTargetBank() == null
                      ? that.getTargetBank() == null : this.getTargetBank().equals( that.getTargetBank() ) ) &&
                    ( this.getTargetName() == null
                      ? that.getTargetName() == null : this.getTargetName().equals( that.getTargetName() ) ) &&
                    ( this.getTargetExt() == null
                      ? that.getTargetExt() == null : this.getTargetExt().equals( that.getTargetExt() ) ) &&
                    ( this.getType() == null
                      ? that.getType() == null : this.getType().equals( that.getType() ) );

            if ( equal )
            {
                if ( this.getDescriptions() == null || this.getDescriptions().length == 0 )
                {
                    equal = that.getDescriptions() == null || that.getDescriptions().length == 0;
                }
                else
                {
                    equal = that.getDescriptions() != null && that.getDescriptions().length != 0
                            ? Arrays.equals( this.getDescriptions(), that.getDescriptions() ) : false;

                }
            }
        }

        return equal;
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
            hc = 37 * hc + ( this.amount == null ? 0 : this.amount.hashCode() );
            hc = 37 * hc + ( this.currency == null ? 0 : this.currency.hashCode() );
            hc = 37 * hc + ( this.executiveAccount == null ? 0 : this.executiveAccount.hashCode() );
            hc = 37 * hc + ( this.executiveBank == null ? 0 : this.executiveBank.hashCode() );
            hc = 37 * hc + ( this.executiveExt == null ? 0 : this.executiveExt.hashCode() );
            hc = 37 * hc + ( this.executiveName == null ? 0 : this.executiveName.hashCode() );
            hc = 37 * hc + ( this.primaryBank == null ? 0 : this.primaryBank.hashCode() );
            hc = 37 * hc + ( this.reference == null ? 0 : this.reference.hashCode() );
            hc = 37 * hc + ( this.targetAccount == null ? 0 : this.targetAccount.hashCode() );
            hc = 37 * hc + ( this.targetBank == null ? 0 : this.targetBank.hashCode() );
            hc = 37 * hc + ( this.targetExt == null ? 0 : this.targetExt.hashCode() );
            hc = 37 * hc + ( this.targetName == null ? 0 : this.targetName.hashCode() );
            hc = 37 * hc + ( this.type == null ? 0 : this.type.hashCode() );

            if ( this.descriptions == null || this.descriptions.length == 0 )
            {
                hc = 37 * hc;
            }
            else
            {
                for ( int i = this.descriptions.length - 1; i >= 0; i-- )
                {
                    hc = 37 * hc + this.descriptions[i].hashCode();
                }
            }

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
            return super.clone();
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
        return new StringBuffer( 300 ).append( '{' ).
            append( "amount=" ).append( this.amount ).
            append( ", currency=" ).append( this.currency ).
            append( ", descriptions=" ).append( this.descriptions ).
            append( ", executiveAccount=" ).append( this.executiveAccount ).
            append( ", executiveBank=" ).append( this.executiveBank ).
            append( ", executiveName=" ).append( (Object) this.executiveName ).
            append( ", executiveExt=" ).append( (Object) this.executiveExt ).
            append( ", primaryBank=" ).append( this.primaryBank ).
            append( ", reference=" ).append( this.reference ).
            append( ", targetAccount=" ).append( this.targetAccount ).
            append( ", targetBank=" ).append( this.targetBank ).
            append( ", targetName=" ).append( (Object) this.targetName ).
            append( ", targetExt=" ).append( (Object) this.targetExt ).
            append( ", type=" ).append( this.type ).
            append( '}' ).toString();

    }

}

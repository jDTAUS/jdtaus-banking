/*
 *  jDTAUS Banking API
 *  Copyright (C) 2005 Christian Schulte
 *  <cs@schulte.it>
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

/**
 * "E" record.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public class Checksum implements Cloneable, Serializable
{

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = -7639085407620663083L;

    /**
     * Number of transactions stored in a logical file.
     * @serial
     */
    private int transactionCount;

    /**
     * Sum of all target account codes of all transactions stored in a logical file.
     * @serial
     */
    private long sumTargetAccount;

    /**
     * Sum of all target bank codes of all transactions stored in a logical file.
     * @serial
     */
    private long sumTargetBank;

    /**
     * Sum of all amounts of all transactions stored in a logical file.
     * @serial
     */
    private long sumAmount;

    /** Cached hash code. */
    private transient int hashCode = NO_HASHCODE;
    private static final int NO_HASHCODE = Integer.MIN_VALUE;

    /** Creates a new {@code Checksum} instance. */
    public Checksum()
    {
        super();
    }

    /**
     * Getter for property {@code transactionCount}.
     *
     * @return Number of transactions stored in a logical file.
     */
    public int getTransactionCount()
    {
        return this.transactionCount;
    }

    /**
     * Setter for property {@code transactionCount}.
     *
     * @param value Number of transactions stored in a logical file.
     */
    public void setTransactionCount( final int value )
    {
        this.transactionCount = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code sumTargetAccount}.
     *
     * @return Sum of all target account codes of all transactions stored in a logical file.
     */
    public long getSumTargetAccount()
    {
        return this.sumTargetAccount;
    }

    /**
     * Setter for property {@code sumTargetAccount}.
     *
     * @param value Sum of all target account codes of all transactions stored in a logical file.
     */
    public void setSumTargetAccount( final long value )
    {
        this.sumTargetAccount = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code sumTargetBank}.
     *
     * @return Sum of all target bank codes of all transactions stored in a logical file.
     */
    public long getSumTargetBank()
    {
        return this.sumTargetBank;
    }

    /**
     * Setter for property {@code sumTargetBank}.
     *
     * @param value Sum of all target bank codes of all transactions stored in a logical file.
     */
    public void setSumTargetBank( final long value )
    {
        this.sumTargetBank = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code sumAmount}.
     *
     * @return Sum of all amounts of all transactions stored in a logical file.
     */
    public long getSumAmount()
    {
        return this.sumAmount;
    }

    /**
     * Setter for property {@code sumAmount}.
     *
     * @param value Sum of all amounts of all transactions stored in a logical file.
     */
    public void setSumAmount( final long value )
    {
        this.sumAmount = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Adds a {@code Transaction} to the checksum.
     *
     * @param transaction The transaction to add to the checksum.
     *
     * @throws NullPointerException if {@code transaction} is {@code null}.
     */
    public void add( final Transaction transaction )
    {
        if ( transaction == null )
        {
            throw new NullPointerException( "transaction" );
        }

        this.sumAmount += transaction.getAmount().longValue(); // TODO longValueExact()
        this.sumTargetAccount += transaction.getTargetAccount().longValue();
        this.sumTargetBank += transaction.getTargetBank().longValue();
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Subtracts a {@code Transaction} from the checksum.
     *
     * @param transaction The transaction to subtract from the checksum.
     *
     * @throws NullPointerException if {@code transaction} is {@code null}.
     * @deprecated Deprecated by {@link #subtract(Transaction) subtract}.
     */
    public final void substract( final Transaction transaction )
    {
        this.subtract( transaction );
    }

    /**
     * Subtracts a {@code Transaction} from the checksum.
     *
     * @param transaction The transaction to subtract from the checksum.
     *
     * @throws NullPointerException if {@code transaction} is {@code null}.
     */
    public void subtract( final Transaction transaction )
    {
        if ( transaction == null )
        {
            throw new NullPointerException( "transaction" );
        }

        this.sumAmount -= transaction.getAmount().longValue(); // TODO longValueExact()
        this.sumTargetAccount -= transaction.getTargetAccount().longValue();
        this.sumTargetBank -= transaction.getTargetBank().longValue();
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
        boolean equal = o == this;

        if ( !equal && o instanceof Checksum )
        {
            final Checksum that = (Checksum) o;
            equal = this.getSumAmount() == that.getSumAmount() &&
                    this.getSumTargetAccount() == that.getSumTargetAccount() &&
                    this.getSumTargetBank() == that.getSumTargetBank() &&
                    this.getTransactionCount() == that.getTransactionCount();

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
            hc = 37 * hc + (int) ( this.sumAmount ^ ( this.sumAmount >>> 32 ) );
            hc = 37 * hc + (int) ( this.sumTargetAccount ^ ( this.sumTargetAccount >>> 32 ) );
            hc = 37 * hc + (int) ( this.sumTargetBank ^ ( this.sumTargetBank >>> 32 ) );
            hc = 37 * hc + this.transactionCount;
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
        return new StringBuffer( 100 ).append( '{' ).
            append( "sumAmount=" ).append( this.sumAmount ).
            append( ", sumTargetAccount=" ).append( this.sumTargetAccount ).
            append( ", sumTargetBank=" ).append( this.sumTargetBank ).
            append( ", transactionCount=" ).append( this.transactionCount ).
            append( '}' ).toString();

    }

}

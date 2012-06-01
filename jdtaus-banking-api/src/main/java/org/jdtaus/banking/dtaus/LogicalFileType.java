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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import org.jdtaus.core.container.ContainerFactory;

/**
 * Type of a logical file.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $JDTAUS$
 */
public final class LogicalFileType implements Serializable
{

    /** Constant for a logical file holding debit orders sent by a customer. Kundendatei Lastschriften. */
    public static final LogicalFileType LK = new LogicalFileType( "LK", true, false, true, false );

    /** Constant for a logical file holding remittance orders sent by a customer. Kundendatei Gutschriften. */
    public static final LogicalFileType GK = new LogicalFileType( "GK", false, true, true, false );

    /** Constant for a logical file holding debit orders sent by a bank. Bankdatei Lastschriften. */
    public static final LogicalFileType LB = new LogicalFileType( "LB", true, false, false, true );

    /** Constant for a logical file holding remittance orders sent by a bank. Bankdatei Gutschriften. */
    public static final LogicalFileType GB = new LogicalFileType( "GB", false, true, false, true );

    /** Supported DTAUS codes. */
    private static final LogicalFileType[] SUPPORTED =
    {
        GB, GK, LB, LK
    };

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = 5932863701674153762L;

    /**
     * DTAUS code.
     * @serial
     */
    private String code;

    /**
     * Flag indicating if a logical file may hold debit orders.
     * @serial
     */
    private boolean debitAllowed;

    /**
     * Flag indicating if a logical file may hold remittance orders.
     * @serial
     */
    private boolean remittanceAllowed;

    /**
     * Flag indicating if a logical file is allowed to be created by a customer.
     * @serial
     */
    private boolean sendByCustomer;

    /**
     * Flag indicating if a logical file is allowed to be created by a bank.
     * @serial
     */
    private boolean sendByBank;

    /**
     * Creates a new {@code LogicalFileType} instance initializing all properties.
     *
     * @param code DTAUS code of the type.
     * @param debitAllowed {@code true} if a logical file may hold debit orders; {@code false} if not.
     * @param remittanceAllowed {@code true} if a logical file may hold remittance orders; {@code false} if not.
     * @param sendByBank {@code true} if a logical file is allowed to be created by a bank; {@code false} if not.
     * @param sendByCustomer {@code true} if a logical file is allowed to be created by a customer; {@code false} if
     * not.
     *
     * @throws NullPointerException if {@code code} is {@code null}.
     */
    private LogicalFileType( final String code, final boolean debitAllowed, final boolean remittanceAllowed,
                             final boolean sendByCustomer, final boolean sendByBank )
    {
        if ( code == null )
        {
            throw new NullPointerException( "code" );
        }

        this.code = code;
        this.debitAllowed = debitAllowed;
        this.remittanceAllowed = remittanceAllowed;
        this.sendByBank = sendByBank;
        this.sendByCustomer = sendByCustomer;
    }

    /**
     * Returns an instance for the type identified by a DTAUS code.
     *
     * @param code A DTAUS code identifying a type of a logical file.
     *
     * @return An instance for {@code code} or {@code null} if {@code code} is no known logical file type.
     *
     * @throws NullPointerException if {@code code} is {@code null}.
     */
    public static LogicalFileType valueOf( final String code )
    {
        if ( code == null )
        {
            throw new NullPointerException( "code" );
        }

        for ( int i = SUPPORTED.length - 1; i >= 0; i-- )
        {
            if ( SUPPORTED[i].getCode().equals( code ) )
            {
                return SUPPORTED[i];
            }
        }

        return null;
    }

    /**
     * Getter for property {@code code}.
     *
     * @return DTAUS code of the logical file type.
     */
    public String getCode()
    {
        return this.code;
    }

    /**
     * Flag indicating if a logical file may hold debit orders.
     *
     * @return {@code true} if a logical file may hold debit orders; {@code false} if not.
     */
    public boolean isDebitAllowed()
    {
        return this.debitAllowed;
    }

    /**
     * Flag indicating if a logical file may hold remittance orders.
     *
     * @return {@code true} if a logical file may hold remittance orders; {@code false} if not.
     */
    public boolean isRemittanceAllowed()
    {
        return this.remittanceAllowed;
    }

    /**
     * Flag indicating if a logical file is allowed to be created by a customer.
     *
     * @return {@code true} if a logical file is allowed to be created by a customer; {@code false} if not.
     */
    public boolean isSendByCustomer()
    {
        return this.sendByCustomer;
    }

    /**
     * Flag indicating if a logical file is allowed to be created by a bank.
     *
     * @return {@code true} if a logical file is allowed to be created by a bank; {@code false} if not.
     */
    public boolean isSendByBank()
    {
        return this.sendByBank;
    }

    /**
     * Gets the short description of the file type for a given locale.
     *
     * @param locale The locale of the short description to return or {@code null} for {@code Locale.getDefault()}.
     *
     * @return The short description of the instance.
     */
    public String getShortDescription( final Locale locale )
    {
        String shortDescription = "";

        final Locale l = locale == null ? Locale.getDefault() : locale;

        if ( "LK".equals( this.getCode() ) )
        {
            shortDescription = this.getLogicalFileType_LKMessage( l );
        }
        else if ( "GK".equals( this.getCode() ) )
        {
            shortDescription = this.getLogicalFileType_GKMessage( l );
        }
        else if ( "LB".equals( this.getCode() ) )
        {
            shortDescription = this.getLogicalFileType_LBMessage( l );
        }
        else if ( "GB".equals( this.getCode() ) )
        {
            shortDescription = this.getLogicalFileType_GBMessage( l );
        }

        return shortDescription;
    }

    /**
     * Searches the implementation for {@code LogicalFileType} instances according to the given arguments.
     *
     * @param debitAllowed Value to compare property {@code debitAllowed} with.
     * @param remittanceAllowed Value to compare property {@code remittanceAllowed} with.
     * @param sendByBank Value to compare property {@code sendByBank} with.
     * @param sendByCustomer Value to compare property {@code sendByCustomer} with.
     *
     * @return All {@code LogicalFileType} instances with property {@code debitAllowed} equal to the
     * {@code debitAllowed} argument, property {@code remittanceAllowed} equal to the {@code remittanceAllowed}
     * argument, property {@code sendByBank} equal to the {@code sendByBank} argument and property
     * {@code sendByCustomer} equal to the {@code sendByCustomer} argument.
     *
     * @deprecated Replaced by {@link #searchLogicalFileTypes(java.lang.Boolean, java.lang.Boolean, java.lang.Boolean, java.lang.Boolean) }.
     */
    public static LogicalFileType[] search( final boolean debitAllowed, final boolean remittanceAllowed,
                                            final boolean sendByBank, final boolean sendByCustomer )
    {
        return searchLogicalFileTypes( Boolean.valueOf( debitAllowed ), Boolean.valueOf( remittanceAllowed ),
                                       Boolean.valueOf( sendByBank ), Boolean.valueOf( sendByCustomer ) );

    }

    /**
     * Searches the implementation for {@code LogicalFileType} instances
     * according to the given arguments.
     *
     * @param debitAllowed Value to compare property {@code debitAllowed} with
     * or {@code null} to ignore that property during searching.
     * @param remittanceAllowed Value to compare property
     * {@code remittanceAllowed} with or {@code null} to ignore that property
     * during searching.
     * @param sendByBank Value to compare property {@code sendByBank} with or
     * {@code null} to ignore that property during searching.
     * @param sendByCustomer Value to compare property {@code sendByCustomer}
     * with or {@code null} to ignore that property during searching.
     *
     * @return All {@code LogicalFileType} instances with property
     * {@code debitAllowed} equal to the {@code debitAllowed} argument when
     * given, property {@code remittanceAllowed} equal to the
     * {@code remittanceAllowed} argument when given, property
     * {@code sendByBank} equal to the {@code sendByBank} argument when given,
     * and property {@code sendByCustomer} equal to the {@code sendByCustomer}
     * argument when given.
     */
    public static LogicalFileType[] searchLogicalFileTypes( final Boolean debitAllowed, final Boolean remittanceAllowed,
                                                            final Boolean sendByBank, final Boolean sendByCustomer )
    {
        final Collection c = new ArrayList( SUPPORTED.length );

        for ( int i = SUPPORTED.length - 1; i >= 0; i-- )
        {
            if ( ( debitAllowed == null ? true : SUPPORTED[i].isDebitAllowed() == debitAllowed.booleanValue() ) &&
                 ( remittanceAllowed == null
                   ? true : SUPPORTED[i].isRemittanceAllowed() == remittanceAllowed.booleanValue() ) &&
                 ( sendByBank == null ? true : SUPPORTED[i].isSendByBank() == sendByBank.booleanValue() ) &&
                 ( sendByCustomer == null ? true : SUPPORTED[i].isSendByCustomer() == sendByCustomer.booleanValue() ) )
            {
                c.add( SUPPORTED[i] );
            }
        }

        return (LogicalFileType[]) c.toArray( new LogicalFileType[ c.size() ] );
    }

    /**
     * Creates a string representing the properties of the instance.
     *
     * @return A string representing the properties of the instance.
     */
    private String internalString()
    {
        return new StringBuffer( 200 ).append( '{' ).
            append( "code=" ).append( this.code ).
            append( ", debitAllowed=" ).append( this.debitAllowed ).
            append( ", remittanceAllowed=" ).append( this.remittanceAllowed ).
            append( ", sendByCustomer=" ).append( this.sendByCustomer ).
            append( ", sendByBank=" ).append( this.sendByBank ).
            append( '}' ).toString();

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

        if ( !ret && o instanceof LogicalFileType )
        {
            final LogicalFileType that = (LogicalFileType) o;

            ret = this.isDebitAllowed() == that.isDebitAllowed() &&
                  this.isRemittanceAllowed() == that.isRemittanceAllowed() &&
                  this.isSendByBank() == that.isSendByBank() &&
                  this.isSendByCustomer() == that.isSendByCustomer() &&
                  ( this.getCode() == null ? that.getCode() == null : this.getCode().equals( that.getCode() ) );

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
        return this.internalString().hashCode();
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

    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>logicalFileType_GB</code>.
     * <blockquote><pre>Bankdatei - Gutschriften</pre></blockquote>
     * <blockquote><pre>Bank file - remittances</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     *
     * @return the text of message <code>logicalFileType_GB</code>.
     */
    private String getLogicalFileType_GBMessage( final Locale locale )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "logicalFileType_GB", locale, null );

    }

    /**
     * Gets the text of message <code>logicalFileType_LB</code>.
     * <blockquote><pre>Bankdatei - Lastschriften</pre></blockquote>
     * <blockquote><pre>Bank file - debits</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     *
     * @return the text of message <code>logicalFileType_LB</code>.
     */
    private String getLogicalFileType_LBMessage( final Locale locale )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "logicalFileType_LB", locale, null );

    }

    /**
     * Gets the text of message <code>logicalFileType_GK</code>.
     * <blockquote><pre>Kundendatei - Gutschriften</pre></blockquote>
     * <blockquote><pre>Customer file - remittances</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     *
     * @return the text of message <code>logicalFileType_GK</code>.
     */
    private String getLogicalFileType_GKMessage( final Locale locale )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "logicalFileType_GK", locale, null );

    }

    /**
     * Gets the text of message <code>logicalFileType_LK</code>.
     * <blockquote><pre>Kundendatei - Lastschriften</pre></blockquote>
     * <blockquote><pre>Customer file - debits</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     *
     * @return the text of message <code>logicalFileType_LK</code>.
     */
    private String getLogicalFileType_LKMessage( final Locale locale )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "logicalFileType_LK", locale, null );

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

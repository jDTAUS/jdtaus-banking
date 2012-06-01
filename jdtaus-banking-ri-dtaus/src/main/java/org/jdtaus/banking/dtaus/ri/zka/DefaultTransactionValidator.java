/*
 *  jDTAUS Banking RI DTAUS
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
package org.jdtaus.banking.dtaus.ri.zka;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jdtaus.banking.Textschluessel;
import org.jdtaus.banking.TextschluesselVerzeichnis;
import org.jdtaus.banking.dtaus.LogicalFile;
import org.jdtaus.banking.dtaus.LogicalFileType;
import org.jdtaus.banking.dtaus.Transaction;
import org.jdtaus.banking.dtaus.spi.IllegalTransactionException;
import org.jdtaus.banking.dtaus.spi.TransactionValidator;
import org.jdtaus.banking.messages.IllegalAmountMessage;
import org.jdtaus.banking.messages.IllegalCurrencyMessage;
import org.jdtaus.banking.messages.IllegalDescriptionCountMessage;
import org.jdtaus.banking.messages.TextschluesselConstraintMessage;
import org.jdtaus.banking.spi.CurrencyMapper;
import org.jdtaus.banking.spi.UnsupportedCurrencyException;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.PropertyException;
import org.jdtaus.core.logging.spi.Logger;
import org.jdtaus.core.messages.MandatoryPropertyMessage;
import org.jdtaus.core.text.Message;

/**
 * jDTAUS Banking SPI {@code TransactionValidator} implementation.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $JDTAUS$
 */
public final class DefaultTransactionValidator implements TransactionValidator
{

    public IllegalTransactionException assertValidTransaction(
        final LogicalFile lFile, final Transaction transaction, IllegalTransactionException result )
        throws IOException
    {
        if ( lFile == null )
        {
            throw new NullPointerException( "lFile" );
        }
        if ( transaction == null )
        {
            throw new NullPointerException( "transaction" );
        }

        this.assertValidProperties();
        final Map properties = new HashMap( 20 );
        final LogicalFileType lFileType = lFile.getHeader().getType();
        final Textschluessel[] allowedTypes = this.getTextschluesselVerzeichnis().searchTextschluessel(
            Boolean.valueOf( lFileType.isDebitAllowed() ), Boolean.valueOf( lFileType.isRemittanceAllowed() ),
            lFile.getHeader().getCreateDate() );

        if ( transaction.getExecutiveAccount() == null )
        {
            properties.put( Transaction.PROP_EXECUTIVEACCOUNT, new MandatoryPropertyMessage() );
        }
        if ( transaction.getExecutiveBank() == null )
        {
            properties.put( Transaction.PROP_EXECUTIVEBANK, new MandatoryPropertyMessage() );
        }
        if ( transaction.getExecutiveName() == null || transaction.getExecutiveName().isEmpty() )
        {
            properties.put( Transaction.PROP_EXECUTIVENAME, new MandatoryPropertyMessage() );
        }
        if ( transaction.getTargetAccount() == null )
        {
            properties.put( Transaction.PROP_TARGETACCOUNT, new MandatoryPropertyMessage() );
        }
        if ( transaction.getTargetBank() == null )
        {
            properties.put( Transaction.PROP_TARGETBANK, new MandatoryPropertyMessage() );
        }
        if ( transaction.getTargetName() == null || transaction.getTargetName().isEmpty() )
        {
            properties.put( Transaction.PROP_TARGETNAME, new MandatoryPropertyMessage() );
        }
        if ( transaction.getType() == null )
        {
            properties.put( Transaction.PROP_TYPE, new MandatoryPropertyMessage() );
        }
        if ( transaction.getCurrency() == null )
        {
            properties.put( Transaction.PROP_CURRENCY, new MandatoryPropertyMessage() );
        }
        if ( transaction.getAmount() == null )
        {
            properties.put( Transaction.PROP_AMOUNT, new MandatoryPropertyMessage() );
        }
        if ( allowedTypes != null && transaction.getType() != null )
        {
            int i;
            for ( i = allowedTypes.length - 1; i >= 0; i-- )
            {
                if ( allowedTypes[i].equals( transaction.getType() ) )
                {
                    break;
                }
            }
            if ( i < 0 )
            {
                properties.put( Transaction.PROP_TYPE, new TextschluesselConstraintMessage(
                    lFileType, transaction.getType() ) );

            }
        }
        else if ( transaction.getType() != null )
        {
            properties.put( Transaction.PROP_TYPE, new TextschluesselConstraintMessage(
                lFileType, transaction.getType() ) );

        }

        if ( transaction.getAmount() != null &&
             !( transaction.getAmount().longValue() >= this.getMinAmount() &&
                transaction.getAmount().longValue() <= this.getMaxAmount() ) )
        {
            properties.put( Transaction.PROP_AMOUNT, new IllegalAmountMessage( transaction.getAmount() ) );
        }
        if ( !( transaction.getDescriptions().length >= this.getMinDescriptions() &&
                transaction.getDescriptions().length <= this.getMaxDescriptions() ) )
        {
            properties.put( Transaction.PROP_DESCRIPTIONS, new IllegalDescriptionCountMessage(
                this.getMaxDescriptions(), transaction.getDescriptions().length ) );

        }

        if ( transaction.getCurrency() != null )
        {
            try
            {
                this.getCurrencyMapper().getDtausCode( transaction.getCurrency(), lFile.getHeader().getCreateDate() );
            }
            catch ( UnsupportedCurrencyException ex )
            {
                if ( this.getLogger().isDebugEnabled() )
                {
                    this.getLogger().debug( ex.toString() );
                }

                properties.put( Transaction.PROP_CURRENCY, new IllegalCurrencyMessage(
                    transaction.getCurrency().getCurrencyCode(), lFile.getHeader().getCreateDate() ) );

            }
        }

        if ( properties.size() > 0 )
        {
            if ( result == null )
            {
                result = new IllegalTransactionException();
            }

            for ( Iterator it = properties.entrySet().iterator(); it.hasNext(); )
            {
                final Map.Entry entry = (Map.Entry) it.next();
                result.addMessage( (String) entry.getKey(), (Message) entry.getValue() );
            }
        }

        return result;
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException for illegal property values.
     */
    private void assertValidProperties()
    {
        if ( this.getMinAmount() < 0L )
        {
            throw new PropertyException( "minAmount", Long.toString( this.getMinAmount() ) );
        }
        if ( this.getMaxAmount() < 0L || this.getMinAmount() > this.getMaxAmount() )
        {
            throw new PropertyException( "maxAmount", Long.toString( this.getMaxAmount() ) );
        }
        if ( this.getMinDescriptions() < 0 )
        {
            throw new PropertyException( "minDescriptions", Integer.toString( this.getMinDescriptions() ) );
        }
        if ( this.getMaxDescriptions() < 0 || this.getMinDescriptions() > this.getMaxDescriptions() )
        {
            throw new PropertyException( "maxDescriptions", Integer.toString( this.getMaxDescriptions() ) );
        }
    }

    //--Constructors------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausConstructors
    // This section is managed by jdtaus-container-mojo.

    /** Standard implementation constructor <code>org.jdtaus.banking.dtaus.ri.zka.DefaultTransactionValidator</code>. */
    public DefaultTransactionValidator()
    {
        super();
    }

// </editor-fold>//GEN-END:jdtausConstructors

    //------------------------------------------------------------Constructors--
    //--Dependencies------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the configured <code>TextschluesselVerzeichnis</code> implementation.
     *
     * @return The configured <code>TextschluesselVerzeichnis</code> implementation.
     */
    private TextschluesselVerzeichnis getTextschluesselVerzeichnis()
    {
        return (TextschluesselVerzeichnis) ContainerFactory.getContainer().
            getDependency( this, "TextschluesselVerzeichnis" );

    }

    /**
     * Gets the configured <code>CurrencyMapper</code> implementation.
     *
     * @return The configured <code>CurrencyMapper</code> implementation.
     */
    private CurrencyMapper getCurrencyMapper()
    {
        return (CurrencyMapper) ContainerFactory.getContainer().
            getDependency( this, "CurrencyMapper" );

    }

    /**
     * Gets the configured <code>Logger</code> implementation.
     *
     * @return The configured <code>Logger</code> implementation.
     */
    private Logger getLogger()
    {
        return (Logger) ContainerFactory.getContainer().
            getDependency( this, "Logger" );

    }

// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
    //--Properties--------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausProperties
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the value of property <code>minDescriptions</code>.
     *
     * @return Minimum number of descriptions any transaction has to specify.
     */
    private int getMinDescriptions()
    {
        return ( (java.lang.Integer) ContainerFactory.getContainer().
            getProperty( this, "minDescriptions" ) ).intValue();

    }

    /**
     * Gets the value of property <code>minAmount</code>.
     *
     * @return Minimum amount any transaction has to specify.
     */
    private long getMinAmount()
    {
        return ( (java.lang.Long) ContainerFactory.getContainer().
            getProperty( this, "minAmount" ) ).longValue();

    }

    /**
     * Gets the value of property <code>maxDescriptions</code>.
     *
     * @return Maximum number of descriptions any transaction is allowed to specify.
     */
    private int getMaxDescriptions()
    {
        return ( (java.lang.Integer) ContainerFactory.getContainer().
            getProperty( this, "maxDescriptions" ) ).intValue();

    }

    /**
     * Gets the value of property <code>maxAmount</code>.
     *
     * @return Maximum amount any transaction is allowed to specify.
     */
    private long getMaxAmount()
    {
        return ( (java.lang.Long) ContainerFactory.getContainer().
            getProperty( this, "maxAmount" ) ).longValue();

    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
}

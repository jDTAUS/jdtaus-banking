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
import org.jdtaus.banking.dtaus.ri.zka.messages.IllegalAmountMessage;
import org.jdtaus.banking.dtaus.ri.zka.messages.IllegalCurrencyMessage;
import org.jdtaus.banking.dtaus.ri.zka.messages.IllegalDescriptionCountMessage;
import org.jdtaus.banking.dtaus.ri.zka.messages.MandatoryPropertyMessage;
import org.jdtaus.banking.dtaus.ri.zka.messages.TextschluesselConstraintMessage;
import org.jdtaus.banking.dtaus.spi.IllegalTransactionException;
import org.jdtaus.banking.dtaus.spi.TransactionValidator;
import org.jdtaus.banking.spi.CurrencyMapper;
import org.jdtaus.banking.spi.UnsupportedCurrencyException;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.ContainerInitializer;
import org.jdtaus.core.container.ContextFactory;
import org.jdtaus.core.container.ContextInitializer;
import org.jdtaus.core.container.Dependency;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.container.Properties;
import org.jdtaus.core.container.Property;
import org.jdtaus.core.container.PropertyException;
import org.jdtaus.core.text.Message;

/**
 * jDTAUS Banking SPI {@code TransactionValidator} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class DefaultTransactionValidator
    implements ContainerInitializer, TransactionValidator
{
    //--Implementation----------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(DefaultTransactionValidator.class.getName());

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /**
     * Protected <code>DefaultTransactionValidator</code> implementation constructor.
     *
     * @param meta Implementation meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    protected DefaultTransactionValidator(final Implementation meta)
    {
        super();
        if(meta == null)
        {
            throw new NullPointerException("meta");
        }
        this.initializeProperties(meta.getProperties());
    }
    /**
     * Protected <code>DefaultTransactionValidator</code> dependency constructor.
     *
     * @param meta dependency meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    protected DefaultTransactionValidator(final Dependency meta)
    {
        super();
        if(meta == null)
        {
            throw new NullPointerException("meta");
        }
        this.initializeProperties(meta.getProperties());
    }

    /**
     * Initializes the properties of the instance.
     *
     * @param meta the property values to initialize the instance with.
     *
     * @throws NullPointerException if {@code meta} is {@code null}.
     */
    protected void initializeProperties(final Properties meta)
    {
        Property p;

        if(meta == null)
        {
            throw new NullPointerException("meta");
        }

        p = meta.getProperty("maxDescriptions");
        this._maxDescriptions = ((java.lang.Integer) p.getValue()).intValue();


        p = meta.getProperty("minDescriptions");
        this._minDescriptions = ((java.lang.Integer) p.getValue()).intValue();


        p = meta.getProperty("maxAmount");
        this._maxAmount = ((java.lang.Long) p.getValue()).longValue();


        p = meta.getProperty("minAmount");
        this._minAmount = ((java.lang.Long) p.getValue()).longValue();

    }

    //------------------------------------------------------------Constructors--
    //--ContainerInitializer----------------------------------------------------

    /**
     * Initializes the instance.
     *
     * @see #assertValidProperties()
     */
    public void initialize()
    {
        this.assertValidProperties();
    }

    //----------------------------------------------------ContainerInitializer--
    //--Dependencies------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /** Configured <code>CurrencyMapper</code> implementation. */
    private transient CurrencyMapper _dependency1;

    /**
     * Gets the configured <code>CurrencyMapper</code> implementation.
     *
     * @return the configured <code>CurrencyMapper</code> implementation.
     */
    private CurrencyMapper getCurrencyMapper()
    {
        CurrencyMapper ret = null;
        if(this._dependency1 != null)
        {
            ret = this._dependency1;
        }
        else
        {
            ret = (CurrencyMapper) ContainerFactory.getContainer().
                getDependency(DefaultTransactionValidator.class,
                "CurrencyMapper");

            if(ModelFactory.getModel().getModules().
                getImplementation(DefaultTransactionValidator.class.getName()).
                getDependencies().getDependency("CurrencyMapper").
                isBound())
            {
                this._dependency1 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext()))
        {
            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }
    /** Configured <code>TextschluesselVerzeichnis</code> implementation. */
    private transient TextschluesselVerzeichnis _dependency0;

    /**
     * Gets the configured <code>TextschluesselVerzeichnis</code> implementation.
     *
     * @return the configured <code>TextschluesselVerzeichnis</code> implementation.
     */
    private TextschluesselVerzeichnis getTextschluesselVerzeichnis()
    {
        TextschluesselVerzeichnis ret = null;
        if(this._dependency0 != null)
        {
            ret = this._dependency0;
        }
        else
        {
            ret = (TextschluesselVerzeichnis) ContainerFactory.getContainer().
                getDependency(DefaultTransactionValidator.class,
                "TextschluesselVerzeichnis");

            if(ModelFactory.getModel().getModules().
                getImplementation(DefaultTransactionValidator.class.getName()).
                getDependencies().getDependency("TextschluesselVerzeichnis").
                isBound())
            {
                this._dependency0 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext()))
        {
            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }

    //------------------------------------------------------------Dependencies--
    //--Properties--------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /**
     * Property {@code maxDescriptions}.
     * @serial
     */
    private int _maxDescriptions;

    /**
     * Gets the value of property <code>maxDescriptions</code>.
     *
     * @return the value of property <code>maxDescriptions</code>.
     */
    protected int getMaxDescriptions()
    {
        return this._maxDescriptions;
    }

    /**
     * Property {@code minDescriptions}.
     * @serial
     */
    private int _minDescriptions;

    /**
     * Gets the value of property <code>minDescriptions</code>.
     *
     * @return the value of property <code>minDescriptions</code>.
     */
    protected int getMinDescriptions()
    {
        return this._minDescriptions;
    }

    /**
     * Property {@code maxAmount}.
     * @serial
     */
    private long _maxAmount;

    /**
     * Gets the value of property <code>maxAmount</code>.
     *
     * @return the value of property <code>maxAmount</code>.
     */
    protected long getMaxAmount()
    {
        return this._maxAmount;
    }

    /**
     * Property {@code minAmount}.
     * @serial
     */
    private long _minAmount;

    /**
     * Gets the value of property <code>minAmount</code>.
     *
     * @return the value of property <code>minAmount</code>.
     */
    protected long getMinAmount()
    {
        return this._minAmount;
    }


    //--------------------------------------------------------------Properties--
    //--TransactionValidator----------------------------------------------------

    public IllegalTransactionException assertValidTransaction(
        final LogicalFile lFile, final Transaction transaction,
        IllegalTransactionException result) throws IOException
    {
        if(lFile == null)
        {
            throw new NullPointerException("lFile");
        }
        if(transaction == null)
        {
            throw new NullPointerException("transaction");
        }

        final Map properties = new HashMap(20);
        final LogicalFileType lFileType = lFile.getHeader().getType();
        final Textschluessel[] allowedTypes =
            this.getTextschluesselVerzeichnis().search(
            lFileType.isDebitAllowed(),
            lFileType.isRemittanceAllowed());

        if(transaction.getExecutiveAccount() == null)
        {
            properties.put(Transaction.PROP_EXECUTIVEACCOUNT,
                new MandatoryPropertyMessage());

        }
        if(transaction.getExecutiveBank() == null)
        {
            properties.put(Transaction.PROP_EXECUTIVEBANK,
                new MandatoryPropertyMessage());

        }
        if(transaction.getExecutiveName() == null)
        {
            properties.put(Transaction.PROP_EXECUTIVENAME,
                new MandatoryPropertyMessage());

        }
        if(transaction.getTargetAccount() == null)
        {
            properties.put(Transaction.PROP_TARGETACCOUNT,
                new MandatoryPropertyMessage());

        }
        if(transaction.getTargetBank() == null)
        {
            properties.put(Transaction.PROP_TARGETBANK,
                new MandatoryPropertyMessage());

        }
        if(transaction.getTargetName() == null)
        {
            properties.put(Transaction.PROP_TARGETNAME,
                new MandatoryPropertyMessage());

        }
        if(transaction.getType() == null)
        {
            properties.put(Transaction.PROP_TYPE,
                new MandatoryPropertyMessage());

        }
        if(transaction.getCurrency() == null)
        {
            properties.put(Transaction.PROP_CURRENCY,
                new MandatoryPropertyMessage());

        }
        if(transaction.getAmount() == null)
        {
            properties.put(Transaction.PROP_AMOUNT,
                new MandatoryPropertyMessage());

        }
        if(allowedTypes != null && transaction.getType() != null)
        {
            int i;
            for(i = allowedTypes.length - 1; i >= 0; i--)
            {
                if(allowedTypes[i].equals(transaction.getType()))
                {
                    break;
                }
            }
            if(i < 0)
            {
                properties.put(Transaction.PROP_TYPE,
                    new TextschluesselConstraintMessage(lFileType,
                    transaction.getType()));

            }
        }
        else if(transaction.getType() != null)
        {
            properties.put(Transaction.PROP_TYPE,
                new TextschluesselConstraintMessage(lFileType,
                transaction.getType()));

        }

        if(transaction.getAmount() != null &&
            !(transaction.getAmount().longValue() >= this.getMinAmount() &&
            transaction.getAmount().longValue() <= this.getMaxAmount()))
        {
            properties.put(Transaction.PROP_AMOUNT,
                new IllegalAmountMessage(transaction.getAmount()));

        }
        if(!(transaction.getDescriptions().length >= this.getMinDescriptions() &&
            transaction.getDescriptions().length <= this.getMaxDescriptions()))
        {
            properties.put(Transaction.PROP_DESCRIPTIONS,
                new IllegalDescriptionCountMessage(
                AbstractLogicalFile.MAX_DESCRIPTIONS));

        }

        if(transaction.getCurrency() != null)
        {
            try
            {
                this.getCurrencyMapper().getDtausCode(transaction.getCurrency(),
                    lFile.getHeader().getCreateDate());

            }
            catch(UnsupportedCurrencyException ex)
            {
                properties.put(Transaction.PROP_CURRENCY,
                    new IllegalCurrencyMessage(transaction.getCurrency().
                    getCurrencyCode(), lFile.getHeader().getCreateDate()));

            }
        }

        if(properties.size() > 0)
        {
            if(result == null)
            {
                result = new IllegalTransactionException();
            }

            for(Iterator it = properties.entrySet().iterator(); it.hasNext();)
            {
                final Map.Entry entry = (Map.Entry) it.next();
                result.addMessage((String) entry.getKey(),
                    (Message) entry.getValue());

            }
        }

        return result;
    }

    //----------------------------------------------------TransactionValidator--
    //--DefaultTransactionValidator---------------------------------------------

    /**
     * Checks configured properties.
     *
     * @throws PropertyException for illegal property values.
     */
    private void assertValidProperties()
    {
        if(this.getMinAmount() < 0L)
        {
            throw new PropertyException("minAmount",
                Long.toString(this.getMinAmount()));

        }
        if(this.getMaxAmount() < 0L ||
            this.getMinAmount() > this.getMaxAmount())
        {
            throw new PropertyException("maxAmount",
                Long.toString(this.getMaxAmount()));

        }
        if(this.getMinDescriptions() < 0)
        {
            throw new PropertyException("minDescriptions",
                Integer.toString(this.getMinDescriptions()));

        }
        if(this.getMaxDescriptions() < 0 ||
            this.getMinDescriptions() > this.getMaxDescriptions())
        {
            throw new PropertyException("maxDescriptions",
                Integer.toString(this.getMaxDescriptions()));

        }
    }

    //---------------------------------------------DefaultTransactionValidator--
}

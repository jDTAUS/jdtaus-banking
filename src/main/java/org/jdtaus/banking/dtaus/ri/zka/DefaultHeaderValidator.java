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
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.LogicalFile;
import org.jdtaus.banking.dtaus.LogicalFileType;
import org.jdtaus.banking.dtaus.ri.zka.messages.CurrencyConstraintMessage;
import org.jdtaus.banking.dtaus.ri.zka.messages.IllegalCurrencyMessage;
import org.jdtaus.banking.dtaus.ri.zka.messages.IllegalDateMessage;
import org.jdtaus.banking.dtaus.ri.zka.messages.IllegalScheduleMessage;
import org.jdtaus.banking.dtaus.ri.zka.messages.MandatoryPropertyMessage;
import org.jdtaus.banking.dtaus.ri.zka.messages.TextschluesselConstraintMessage;
import org.jdtaus.banking.dtaus.spi.CurrencyCounter;
import org.jdtaus.banking.dtaus.spi.HeaderValidator;
import org.jdtaus.banking.dtaus.spi.IllegalHeaderException;
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
 * jDTAUS Banking SPI {@code HeaderValidator} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class DefaultHeaderValidator
    implements ContainerInitializer, HeaderValidator
{
    //--Implementation----------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(DefaultHeaderValidator.class.getName());

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /**
     * Protected <code>DefaultHeaderValidator</code> implementation constructor.
     *
     * @param meta Implementation meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    protected DefaultHeaderValidator(final Implementation meta)
    {
        super();
        if(meta == null)
        {
            throw new NullPointerException("meta");
        }
        this.initializeProperties(meta.getProperties());
    }
    /**
     * Protected <code>DefaultHeaderValidator</code> dependency constructor.
     *
     * @param meta dependency meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    protected DefaultHeaderValidator(final Dependency meta)
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

        p = meta.getProperty("maxDateMillis");
        this._maxDateMillis = ((java.lang.Long) p.getValue()).longValue();


        p = meta.getProperty("minDateMillis");
        this._minDateMillis = ((java.lang.Long) p.getValue()).longValue();


        p = meta.getProperty("maxScheduleDaysMillis");
        this._maxScheduleDaysMillis = ((java.lang.Long) p.getValue()).longValue();

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
    private transient CurrencyMapper _dependency0;

    /**
     * Gets the configured <code>CurrencyMapper</code> implementation.
     *
     * @return the configured <code>CurrencyMapper</code> implementation.
     */
    private CurrencyMapper getCurrencyMapper()
    {
        CurrencyMapper ret = null;
        if(this._dependency0 != null)
        {
            ret = this._dependency0;
        }
        else
        {
            ret = (CurrencyMapper) ContainerFactory.getContainer().
                getDependency(DefaultHeaderValidator.class,
                "CurrencyMapper");

            if(ModelFactory.getModel().getModules().
                getImplementation(DefaultHeaderValidator.class.getName()).
                getDependencies().getDependency("CurrencyMapper").
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
     * Property {@code maxDateMillis}.
     * @serial
     */
    private long _maxDateMillis;

    /**
     * Gets the value of property <code>maxDateMillis</code>.
     *
     * @return the value of property <code>maxDateMillis</code>.
     */
    protected long getMaxDateMillis()
    {
        return this._maxDateMillis;
    }

    /**
     * Property {@code minDateMillis}.
     * @serial
     */
    private long _minDateMillis;

    /**
     * Gets the value of property <code>minDateMillis</code>.
     *
     * @return the value of property <code>minDateMillis</code>.
     */
    protected long getMinDateMillis()
    {
        return this._minDateMillis;
    }

    /**
     * Property {@code maxScheduleDaysMillis}.
     * @serial
     */
    private long _maxScheduleDaysMillis;

    /**
     * Gets the value of property <code>maxScheduleDaysMillis</code>.
     *
     * @return the value of property <code>maxScheduleDaysMillis</code>.
     */
    protected long getMaxScheduleDaysMillis()
    {
        return this._maxScheduleDaysMillis;
    }


    //--------------------------------------------------------------Properties--
    //--HeaderValidator---------------------------------------------------------

    public IllegalHeaderException assertValidHeader(final Header header,
        IllegalHeaderException result)
    {
        if(header == null)
        {
            throw new NullPointerException("header");
        }

        final List messages = new LinkedList();
        final Map properties = new HashMap(20);

        if(header.getCreateDate() == null)
        {
            properties.put(Header.PROP_CREATEDATE,
                new MandatoryPropertyMessage());

        }
        else if(!this.checkDate(header.getCreateDate()))
        {
            properties.put(Header.PROP_CREATEDATE,
                new IllegalDateMessage(header.getCreateDate()));

        }

        if(header.getExecutionDate() != null &&
            !this.checkDate(header.getExecutionDate()))
        {
            properties.put(Header.PROP_EXECUTIONDATE,
                new IllegalDateMessage(header.getExecutionDate()));

        }
        if(header.getType() == null)
        {
            properties.put(Header.PROP_TYPE, new MandatoryPropertyMessage());
        }
        else if(header.getType().isSendByBank() &&
            header.getBankData() == null)
        {
            properties.put(Header.PROP_BANKDATA,
                new MandatoryPropertyMessage());

        }

        if(header.getCustomer() == null)
        {
            properties.put(Header.PROP_CUSTOMER,
                new MandatoryPropertyMessage());

        }
        if(header.getBank() == null)
        {
            properties.put(Header.PROP_BANK,
                new MandatoryPropertyMessage());

        }
        if(header.getAccount() == null)
        {
            properties.put(Header.PROP_ACCOUNT,
                new MandatoryPropertyMessage());

        }
        if(header.getCurrency() == null)
        {
            properties.put(Header.PROP_CURRENCY,
                new MandatoryPropertyMessage());

        }

        if(header.getCreateDate() != null)
        {
            if(header.getCurrency() != null)
            {
                try
                {
                    this.getCurrencyMapper().getDtausCode(header.getCurrency(),
                        header.getCreateDate());

                }
                catch(UnsupportedCurrencyException ex)
                {
                    properties.put(Header.PROP_CURRENCY,
                        new IllegalCurrencyMessage(header.getCurrency().
                        getCurrencyCode(), header.getCreateDate()));

                }
            }

            if(!this.checkSchedule(header.getCreateDate(),
                header.getExecutionDate()))
            {
                messages.add(new IllegalScheduleMessage(header.getCreateDate(),
                    header.getExecutionDate()));

            }
        }

        if(properties.size() > 0 || messages.size() > 0)
        {
            if(result == null)
            {
                result = new IllegalHeaderException();
            }

            for(Iterator it = properties.entrySet().iterator(); it.hasNext();)
            {
                final Map.Entry entry = (Map.Entry) it.next();
                result.addMessage((String) entry.getKey(),
                    (Message) entry.getValue());

            }
            for(Iterator it = messages.iterator(); it.hasNext();)
            {
                result.addMessage((Message) it.next());
            }
        }

        return result;
    }

    public IllegalHeaderException assertValidHeader(final LogicalFile lFile,
        final Header header, final CurrencyCounter counter,
        IllegalHeaderException result) throws IOException
    {
        if(lFile == null)
        {
            throw new NullPointerException("lFile");
        }
        if(header == null)
        {
            throw new NullPointerException("header");
        }
        if(counter == null)
        {
            throw new NullPointerException("counter");
        }

        IllegalHeaderException e = this.assertValidHeader(header, result);
        final LogicalFileType oldLabel = lFile.getHeader().getType();
        final LogicalFileType newLabel = header.getType();

        if(oldLabel != null && lFile.getChecksum().getTransactionCount() > 0 &&
            (oldLabel.isDebitAllowed() && !newLabel.isDebitAllowed()) ||
            (oldLabel.isRemittanceAllowed() && !newLabel.isRemittanceAllowed()))
        {
            if(e == null)
            {
                e = new IllegalHeaderException();
            }

            e.addMessage(Header.PROP_TYPE,
                new TextschluesselConstraintMessage(newLabel,
                lFile.getTransaction(0).getType()));

        }

        final Currency[] oldCurrencies = this.getCurrencyMapper().
            getDtausCurrencies(lFile.getHeader().getCreateDate());

        final Currency[] newCurrencies = this.getCurrencyMapper().
            getDtausCurrencies(header.getCreateDate());

        if(!Arrays.equals(oldCurrencies, newCurrencies))
        {
            final Currency[] current = counter.getCurrencies();
            for(int i = current.length - 1; i >= 0; i--)
            {
                boolean currencyKept = false;

                for(int j = newCurrencies.length - 1; j >= 0; j--)
                {
                    if(newCurrencies[j].getCurrencyCode().
                        equals(current[i].getCurrencyCode()))
                    {
                        currencyKept = true;
                        break;
                    }
                }

                if(!currencyKept)
                {
                    if(e == null)
                    {
                        e = new IllegalHeaderException();
                    }

                    e.addMessage(new CurrencyConstraintMessage(
                        current[i].getCurrencyCode(), header.getCreateDate()));

                }
            }
        }

        return e;
    }

    //---------------------------------------------------------HeaderValidator--
    //--DefaultHeaderValidator--------------------------------------------------

    /**
     * Checks configured properties.
     *
     * @throws PropertyException for illegal property values.
     */
    private void assertValidProperties()
    {
        if(this.getMinDateMillis() < 0L)
        {
            throw new PropertyException("minDateMillis",
                Long.toString(this.getMinDateMillis()));

        }
        if(this.getMaxDateMillis() < 0L ||
            this.getMinDateMillis() > this.getMaxDateMillis())
        {
            throw new PropertyException("maxDateMillis",
                Long.toString(this.getMaxDateMillis()));

        }
        if(this.getMaxScheduleDaysMillis() < 0L)
        {
            throw new PropertyException("maxScheduleDaysMillis",
                Long.toString(this.getMaxScheduleDaysMillis()));

        }
    }

    /**
     * Checks a given date.
     *
     * @param date instance to check.
     *
     * @return {@code true} if {@code date} is legal; {@code false} if not.
     */
    private boolean checkDate(final Date date)
    {
        boolean valid = false;

        if(date != null)
        {
            final long millis = date.getTime();
            valid = millis >= this.getMinDateMillis() &&
                millis <= this.getMaxDateMillis();

        }

        return valid;
    }

    /**
     * Checks a given schedule.
     *
     * @param createDate file creation date to check.
     * @param executionDate file execution date to check.
     *
     * @return {@code true} if {@code createDate} and {@code executionDate} is
     * a legal combination; {@code false} if not.
     */
    private boolean checkSchedule(final Date createDate,
        final Date executionDate)
    {
        boolean valid = createDate != null;

        if(valid)
        {
            final long createMillis = createDate.getTime();
            if(executionDate != null)
            {
                final long executionMillis = executionDate.getTime();
                valid = executionMillis >= createMillis &&
                    executionMillis <= createMillis +
                    this.getMaxScheduleDaysMillis();

            }
        }

        return valid;
    }

    //--------------------------------------------------DefaultHeaderValidator--
}

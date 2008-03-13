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
import org.jdtaus.banking.dtaus.spi.CurrencyCounter;
import org.jdtaus.banking.dtaus.spi.HeaderValidator;
import org.jdtaus.banking.dtaus.spi.IllegalHeaderException;
import org.jdtaus.banking.messages.CurrencyConstraintMessage;
import org.jdtaus.banking.messages.IllegalCurrencyMessage;
import org.jdtaus.banking.messages.IllegalDateMessage;
import org.jdtaus.banking.messages.IllegalScheduleMessage;
import org.jdtaus.banking.messages.TextschluesselConstraintMessage;
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
import org.jdtaus.core.messages.MandatoryPropertyMessage;
import org.jdtaus.core.text.Message;

/**
 * jDTAUS Banking SPI {@code HeaderValidator} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class DefaultHeaderValidator
    implements ContainerInitializer, HeaderValidator
{
    //--Implementation----------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausImplementation
    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(DefaultHeaderValidator.class.getName());
// </editor-fold>//GEN-END:jdtausImplementation

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausConstructors
    // This section is managed by jdtaus-container-mojo.

    /**
     * <code>DefaultHeaderValidator</code> implementation constructor.
     *
     * @param meta Implementation meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    private DefaultHeaderValidator(final Implementation meta)
    {
        super();
        if(meta == null)
        {
            throw new NullPointerException("meta");
        }
        this.initializeProperties(meta.getProperties());
    }
    /**
     * <code>DefaultHeaderValidator</code> dependency constructor.
     *
     * @param meta dependency meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    private DefaultHeaderValidator(final Dependency meta)
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
    private void initializeProperties(final Properties meta)
    {
        Property p;

        if(meta == null)
        {
            throw new NullPointerException("meta");
        }

        p = meta.getProperty("maxDateMillis");
        this.pMaxDateMillis = ((java.lang.Long) p.getValue()).longValue();


        p = meta.getProperty("minDateMillis");
        this.pMinDateMillis = ((java.lang.Long) p.getValue()).longValue();


        p = meta.getProperty("maxScheduleDays");
        this.pMaxScheduleDays = ((java.lang.Integer) p.getValue()).intValue();

    }
// </editor-fold>//GEN-END:jdtausConstructors

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

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
    // This section is managed by jdtaus-container-mojo.

    /** Configured <code>CurrencyMapper</code> implementation. */
    private transient CurrencyMapper dCurrencyMapper;

    /**
     * Gets the configured <code>CurrencyMapper</code> implementation.
     *
     * @return the configured <code>CurrencyMapper</code> implementation.
     */
    private CurrencyMapper getCurrencyMapper()
    {
        CurrencyMapper ret = null;
        if(this.dCurrencyMapper != null)
        {
            ret = this.dCurrencyMapper;
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
                this.dCurrencyMapper = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext()))
        {
            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }
// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
    //--Properties--------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausProperties
    // This section is managed by jdtaus-container-mojo.

    /**
     * Property {@code maxDateMillis}.
     * @serial
     */
    private long pMaxDateMillis;

    /**
     * Gets the value of property <code>maxDateMillis</code>.
     *
     * @return the value of property <code>maxDateMillis</code>.
     */
    private long getMaxDateMillis()
    {
        return this.pMaxDateMillis;
    }

    /**
     * Property {@code minDateMillis}.
     * @serial
     */
    private long pMinDateMillis;

    /**
     * Gets the value of property <code>minDateMillis</code>.
     *
     * @return the value of property <code>minDateMillis</code>.
     */
    private long getMinDateMillis()
    {
        return this.pMinDateMillis;
    }

    /**
     * Property {@code maxScheduleDays}.
     * @serial
     */
    private int pMaxScheduleDays;

    /**
     * Gets the value of property <code>maxScheduleDays</code>.
     *
     * @return the value of property <code>maxScheduleDays</code>.
     */
    private int getMaxScheduleDays()
    {
        return this.pMaxScheduleDays;
    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
    //--HeaderValidator---------------------------------------------------------

    public IllegalHeaderException assertValidHeader(
        final Header header, IllegalHeaderException result )
    {
        if ( header == null )
        {
            throw new NullPointerException( "header" );
        }

        final List messages = new LinkedList();
        final Map properties = new HashMap( 20 );

        if ( header.getCreateDate() == null )
        {
            properties.put( Header.PROP_CREATEDATE,
                            new MandatoryPropertyMessage() );

        }
        else if ( !this.checkDate( header.getCreateDate() ) )
        {
            properties.put( Header.PROP_CREATEDATE, new IllegalDateMessage(
                            header.getCreateDate(),
                            new Date( this.getMinDateMillis() ),
                            new Date( this.getMaxDateMillis() ) ) );

        }

        if ( header.getExecutionDate() != null &&
            !this.checkDate( header.getExecutionDate() ) )
        {
            properties.put( Header.PROP_EXECUTIONDATE, new IllegalDateMessage(
                            header.getExecutionDate(),
                            new Date( this.getMinDateMillis() ),
                            new Date( this.getMaxDateMillis() ) ) );

        }
        if ( header.getType() == null )
        {
            properties.put( Header.PROP_TYPE, new MandatoryPropertyMessage() );
        }
        else if ( header.getType().isSendByBank() &&
            header.getBankData() == null )
        {
            properties.put( Header.PROP_BANKDATA,
                            new MandatoryPropertyMessage() );

        }

        if ( header.getCustomer() == null )
        {
            properties.put( Header.PROP_CUSTOMER,
                            new MandatoryPropertyMessage() );

        }
        if ( header.getBank() == null )
        {
            properties.put( Header.PROP_BANK,
                            new MandatoryPropertyMessage() );

        }
        if ( header.getAccount() == null )
        {
            properties.put( Header.PROP_ACCOUNT,
                            new MandatoryPropertyMessage() );

        }
        if ( header.getCurrency() == null )
        {
            properties.put( Header.PROP_CURRENCY,
                            new MandatoryPropertyMessage() );

        }

        if ( header.getCreateDate() != null )
        {
            if ( header.getCurrency() != null )
            {
                try
                {
                    this.getCurrencyMapper().getDtausCode(
                        header.getCurrency(), header.getCreateDate() );

                }
                catch ( UnsupportedCurrencyException ex )
                {
                    properties.put( Header.PROP_CURRENCY,
                                    new IllegalCurrencyMessage(
                                    header.getCurrency().getCurrencyCode(),
                                    header.getCreateDate() ) );

                }
            }

            if ( !this.checkSchedule( header.getCreateDate(),
                                      header.getExecutionDate() ) )
            {
                messages.add( new IllegalScheduleMessage(
                              header.getCreateDate(), header.getExecutionDate(),
                              this.getMaxScheduleDays() ) );

            }
        }

        if ( properties.size() > 0 || messages.size() > 0 )
        {
            if ( result == null )
            {
                result = new IllegalHeaderException();
            }

            for ( Iterator it = properties.entrySet().iterator(); it.hasNext();)
            {
                final Map.Entry entry = ( Map.Entry ) it.next();
                result.addMessage( ( String ) entry.getKey(),
                                   ( Message ) entry.getValue() );

            }
            for ( Iterator it = messages.iterator(); it.hasNext();)
            {
                result.addMessage( ( Message ) it.next() );
            }
        }

        return result;
    }

    public IllegalHeaderException assertValidHeader(
        final LogicalFile lFile, final Header header,
        final CurrencyCounter counter, IllegalHeaderException result )
        throws IOException
    {
        if ( lFile == null )
        {
            throw new NullPointerException( "lFile" );
        }
        if ( header == null )
        {
            throw new NullPointerException( "header" );
        }
        if ( counter == null )
        {
            throw new NullPointerException( "counter" );
        }

        IllegalHeaderException e = this.assertValidHeader( header, result );
        final LogicalFileType oldLabel = lFile.getHeader().getType();
        final LogicalFileType newLabel = header.getType();

        if ( oldLabel != null && lFile.getChecksum().getTransactionCount() > 0 &&
            ( oldLabel.isDebitAllowed() && !newLabel.isDebitAllowed() ) ||
            ( oldLabel.isRemittanceAllowed() && !newLabel.isRemittanceAllowed() ) )
        {
            if ( e == null )
            {
                e = new IllegalHeaderException();
            }

            e.addMessage( Header.PROP_TYPE, new TextschluesselConstraintMessage(
                          newLabel, lFile.getTransaction( 0 ).getType() ) );

        }

        final Currency[] oldCurrencies = this.getCurrencyMapper().
            getDtausCurrencies( lFile.getHeader().getCreateDate() );

        final Currency[] newCurrencies = this.getCurrencyMapper().
            getDtausCurrencies( header.getCreateDate() );

        if ( !Arrays.equals( oldCurrencies, newCurrencies ) )
        {
            final Currency[] current = counter.getCurrencies();
            for ( int i = current.length - 1; i >= 0; i-- )
            {
                boolean currencyKept = false;

                for ( int j = newCurrencies.length - 1; j >= 0; j-- )
                {
                    if ( newCurrencies[j].getCurrencyCode().
                        equals( current[i].getCurrencyCode() ) )
                    {
                        currencyKept = true;
                        break;
                    }
                }

                if ( !currencyKept )
                {
                    if ( e == null )
                    {
                        e = new IllegalHeaderException();
                    }

                    e.addMessage( new CurrencyConstraintMessage(
                                  current[i].getCurrencyCode(),
                                  header.getCreateDate() ) );

                }
            }
        }

        return e;
    }

    //---------------------------------------------------------HeaderValidator--
    //--DefaultHeaderValidator--------------------------------------------------

    /** Value of property {@code maxScheduleDays} in milliseconds. */
    private long maxScheduleDaysMillis = Long.MIN_VALUE;

    /** Creates a new {@code DefaultHeaderValidator} instance. */
    public DefaultHeaderValidator()
    {
        this( META );
        this.initialize();
    }

    /**
     * Gets the value of property {@code maxScheduleDays} in milliseconds.
     *
     * @return the value of property {@code maxScheduleDays} in milliseconds.
     */
    private long getMaxScheduleDaysMillis()
    {
        if ( this.maxScheduleDaysMillis < 0L )
        {
            this.maxScheduleDaysMillis = this.getMaxScheduleDays() * 86400000L;
        }

        return this.maxScheduleDaysMillis;
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException for illegal property values.
     */
    private void assertValidProperties()
    {
        if ( this.getMinDateMillis() < 0L )
        {
            throw new PropertyException(
                "minDateMillis",
                Long.toString( this.getMinDateMillis() ) );

        }
        if ( this.getMaxDateMillis() < 0L ||
            this.getMinDateMillis() > this.getMaxDateMillis() )
        {
            throw new PropertyException(
                "maxDateMillis",
                Long.toString( this.getMaxDateMillis() ) );

        }
        if ( this.getMaxScheduleDays() < 0 )
        {
            throw new PropertyException(
                "maxScheduleDays",
                Integer.toString( this.getMaxScheduleDays() ) );

        }
    }

    /**
     * Checks a given date.
     *
     * @param date instance to check.
     *
     * @return {@code true} if {@code date} is legal; {@code false} if not.
     */
    private boolean checkDate( final Date date )
    {
        boolean valid = false;

        if ( date != null )
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
    private boolean checkSchedule( final Date createDate,
                                    final Date executionDate )
    {
        boolean valid = createDate != null;

        if ( valid )
        {
            final long createMillis = createDate.getTime();
            if ( executionDate != null )
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

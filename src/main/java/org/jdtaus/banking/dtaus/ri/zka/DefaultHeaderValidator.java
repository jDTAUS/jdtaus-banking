/*
 *  jDTAUS Banking RI DTAUS
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
import org.jdtaus.core.container.PropertyException;
import org.jdtaus.core.messages.MandatoryPropertyMessage;
import org.jdtaus.core.text.Message;

/**
 * jDTAUS Banking SPI {@code HeaderValidator} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class DefaultHeaderValidator implements HeaderValidator
{
    //--Constructors------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausConstructors
    // This section is managed by jdtaus-container-mojo.

    /** Standard implementation constructor <code>org.jdtaus.banking.dtaus.ri.zka.DefaultHeaderValidator</code>. */
    public DefaultHeaderValidator()
    {
        super();
    }

// </editor-fold>//GEN-END:jdtausConstructors

    //------------------------------------------------------------Constructors--
    //--Dependencies------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the configured <code>CurrencyMapper</code> implementation.
     *
     * @return the configured <code>CurrencyMapper</code> implementation.
     */
    private CurrencyMapper getCurrencyMapper()
    {
        return (CurrencyMapper) ContainerFactory.getContainer().
            getDependency( this, "CurrencyMapper" );

    }

// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
    //--Properties--------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausProperties
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the value of property <code>minDateMillis</code>.
     *
     * @return Timestamp any date is not allowed to precede.
     */
    private long getMinDateMillis()
    {
        return ( (java.lang.Long) ContainerFactory.getContainer().
            getProperty( this, "minDateMillis" ) ).longValue();

    }

    /**
     * Gets the value of property <code>maxScheduleDays</code>.
     *
     * @return Maximum number of days allowed for a schedule.
     */
    private int getMaxScheduleDays()
    {
        return ( (java.lang.Integer) ContainerFactory.getContainer().
            getProperty( this, "maxScheduleDays" ) ).intValue();

    }

    /**
     * Gets the value of property <code>maxDateMillis</code>.
     *
     * @return Timestamp any date is not allowed to follow.
     */
    private long getMaxDateMillis()
    {
        return ( (java.lang.Long) ContainerFactory.getContainer().
            getProperty( this, "maxDateMillis" ) ).longValue();

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

        this.assertValidProperties();
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

            for ( Iterator it = properties.entrySet().iterator(); it.hasNext(); )
            {
                final Map.Entry entry = (Map.Entry) it.next();
                result.addMessage( (String) entry.getKey(),
                    (Message) entry.getValue() );

            }
            for ( Iterator it = messages.iterator(); it.hasNext(); )
            {
                result.addMessage( (Message) it.next() );
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

        this.assertValidProperties();

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

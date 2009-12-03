/*
 *  jDTAUS Banking SPI
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
package org.jdtaus.banking.dtaus.spi;

import java.io.Serializable;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for maintaining a mapping of currencies to corresponding counters.
 *
 * <p><b>Note:</b><br/>This class is not synchronized and must not be used concurrently without external
 * synchronization.</p>
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class CurrencyCounter implements Serializable
{

    /** Serial version UID for backwards compatibility with 1.1.x classes. */
    private static final long serialVersionUID = -2765988202184349786L;

    /**
     * Maps ISO currency codes to counters.
     * @serial
     */
    private Map currencyMap;

    /**
     * Gets the currencies for which the instance holds counters.
     *
     * @return The currencies for which the instance holds counters.
     */
    public Currency[] getCurrencies()
    {
        final Set currencies = new HashSet( this.getCurrencyMap().size() );
        for ( Iterator it = this.getCurrencyMap().entrySet().iterator(); it.hasNext(); )
        {
            final Map.Entry entry = (Map.Entry) it.next();
            currencies.add( Currency.getInstance( (String) entry.getKey() ) );
        }

        return (Currency[]) currencies.toArray( new Currency[ currencies.size() ] );
    }

    /**
     * Gets the value of the counter for a given {@code Currency}.
     *
     * @param currency The currency to return the value of the counter for.
     *
     * @return The value of the counter of {@code currency}.
     *
     * @throws NullPointerException if {@code currency} is {@code null}.
     */
    public long getValue( final Currency currency )
    {
        if ( currency == null )
        {
            throw new NullPointerException( "currency" );
        }

        final Long value = (Long) this.getCurrencyMap().get( currency.getCurrencyCode() );
        return value != null ? value.longValue() : 0L;
    }

    /**
     * Adds to the counter of a currency.
     *
     * @param currency The currency to add to.
     *
     * @throws NullPointerException if {@code currency} is {@code null}.
     * @throws IndexOutOfBoundsException if the value of the counter of {@code currency} is equal to
     * {@link Long#MAX_VALUE}.
     */
    public void add( final Currency currency )
    {
        if ( currency == null )
        {
            throw new NullPointerException( "currency" );
        }

        final long value = this.getValue( currency );

        if ( value == Long.MAX_VALUE )
        {
            throw new IndexOutOfBoundsException();
        }

        this.getCurrencyMap().put( currency.getCurrencyCode(), new Long( value + 1L ) );
    }

    /**
     * Substracts from the counter of a currency.
     *
     * @param currency The currency to substract from.
     *
     * @throws NullPointerException if {@code currency} is {@code null}.
     * @throws IndexOutOfBoundsException if the value of the counter of {@code currency} is equal to zero.
     */
    public void substract( final Currency currency )
    {
        if ( currency == null )
        {
            throw new NullPointerException( "currency" );
        }

        final long value = this.getValue( currency );

        if ( value == 0L )
        {
            throw new IndexOutOfBoundsException();
        }

        this.getCurrencyMap().put( currency.getCurrencyCode(), new Long( value - 1L ) );
    }

    /**
     * Gets the {@code Map} backing the instance.
     *
     * @return The {@code Map} backing the instance.
     */
    private Map getCurrencyMap()
    {
        if ( this.currencyMap == null )
        {
            this.currencyMap = new HashMap( 10 );
        }

        return this.currencyMap;
    }

}

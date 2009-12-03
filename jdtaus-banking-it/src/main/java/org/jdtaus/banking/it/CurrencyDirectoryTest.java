/*
 *  jDTAUS Banking Test Suite
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
package org.jdtaus.banking.it;

import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import junit.framework.Assert;
import org.jdtaus.banking.CurrencyDirectory;

/**
 * Testcase for {@code CurrencyDirectory} implementations.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class CurrencyDirectoryTest
{

    /** Implementation to test. */
    private CurrencyDirectory directory;

    /**
     * Gets the {@code CurrencyDirectory} implementation tests are performed with.
     *
     * @return the {@code CurrencyDirectory} implementation tests are performed with.
     */
    public CurrencyDirectory getCurrencyDirectory()
    {
        return this.directory;
    }

    /**
     * Sets the {@code CurrencyDirectory} implementation tests are performed with.
     *
     * @param value the {@code CurrencyDirectory} implementation to perform tests with.
     */
    public final void setCurrencyDirectory( final CurrencyDirectory value )
    {
        this.directory = value;
    }

    /**
     * Checks a given array of currencies to contain an instance corresponding to a given ISO code.
     *
     * @param currencies The currencies to check.
     * @param isoCode ISO code to check existence in {@code currencies} with.
     *
     * @throws NullPointerException if either {@code currencies} or {@code isoCode} is {@code null}.
     */
    protected void assertContainsCurrency( final Currency[] currencies, final String isoCode )
    {
        if ( currencies == null )
        {
            throw new NullPointerException( "currencies" );
        }
        if ( isoCode == null )
        {
            throw new NullPointerException( "isoCode" );
        }

        boolean contains = false;
        for ( int i = currencies.length - 1; i >= 0; i-- )
        {
            if ( currencies[i].getCurrencyCode().equals( isoCode ) )
            {
                contains = true;
                break;
            }
        }

        if ( !contains )
        {
            throw new AssertionError( isoCode );
        }
    }

    /**
     * Checks a given array of currencies to not contain an instance corresponding to a given ISO code.
     *
     * @param currencies The currencies to check.
     * @param isoCode ISO code to check existence in {@code currencies} with.
     *
     * @throws NullPointerException if either {@code currencies} or {@code isoCode} is {@code null}.
     */
    protected void assertNotContainsCurrency( final Currency[] currencies, final String isoCode )
    {
        if ( currencies == null )
        {
            throw new NullPointerException( "currencies" );
        }
        if ( isoCode == null )
        {
            throw new NullPointerException( "isoCode" );
        }

        boolean contains = false;
        for ( int i = currencies.length - 1; i >= 0; i-- )
        {
            if ( currencies[i].getCurrencyCode().equals( isoCode ) )
            {
                contains = true;
                break;
            }
        }

        if ( contains )
        {
            throw new AssertionError( isoCode );
        }
    }

    /**
     * Test the {@link CurrencyDirectory#getDtausCurrencies(Date)} method to handle illegal arguments correctly by
     * throwing a {@code NullPointerException}.
     */
    public void testGetDtausCurrenciesNull()
    {
        assert this.getCurrencyDirectory() != null;

        try
        {
            this.getCurrencyDirectory().getDtausCurrencies( null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    /** Tests the {@link CurrencyDirectory#getDtausCurrencies(Date)} method to return sane values. */
    public void testGetDtausCurrencies() throws Exception
    {
        assert this.getCurrencyDirectory() != null;
        Assert.assertTrue( this.getCurrencyDirectory().getDtausCurrencies( new Date() ).length >= 0 );
    }

    /**
     * Tests the {@link CurrencyDirectory#getDtausCurrencies(Date)} method to return correct values for the EUR and DEM
     * currency for various dates.
     */
    public void testCurrencyConstraints() throws Exception
    {
        assert this.getCurrencyDirectory() != null;

        final Calendar cal = Calendar.getInstance();
        final Date now = cal.getTime();

        cal.set( Calendar.YEAR, 2001 );
        cal.set( Calendar.MONTH, 11 );
        cal.set( Calendar.DAY_OF_MONTH, 31 );

        final Date lastDayDEM = cal.getTime();

        cal.set( Calendar.YEAR, 2002 );
        cal.set( Calendar.MONTH, 0 );
        cal.set( Calendar.DAY_OF_MONTH, 1 );

        final Date firstDayEUR = cal.getTime();

        this.assertContainsCurrency( this.getCurrencyDirectory().getDtausCurrencies( now ), "EUR" );
        this.assertNotContainsCurrency( this.getCurrencyDirectory().getDtausCurrencies( now ), "DEM" );
        this.assertNotContainsCurrency( this.getCurrencyDirectory().getDtausCurrencies( lastDayDEM ), "EUR" );
        this.assertContainsCurrency( this.getCurrencyDirectory().getDtausCurrencies( lastDayDEM ), "DEM" );
        this.assertContainsCurrency( this.getCurrencyDirectory().getDtausCurrencies( firstDayEUR ), "EUR" );
        this.assertNotContainsCurrency( this.getCurrencyDirectory().getDtausCurrencies( firstDayEUR ), "DEM" );
    }

}

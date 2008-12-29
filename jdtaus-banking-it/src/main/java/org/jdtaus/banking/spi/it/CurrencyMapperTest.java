/*
 *  jDTAUS Banking Test Suite
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
package org.jdtaus.banking.spi.it;

import java.util.Currency;
import java.util.Date;
import junit.framework.Assert;
import org.jdtaus.banking.it.CurrencyDirectoryTest;
import org.jdtaus.banking.spi.CurrencyMapper;
import org.jdtaus.banking.spi.UnsupportedCurrencyException;

/**
 * Testcase for {@code CurrencyMapper} implementations.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class CurrencyMapperTest extends CurrencyDirectoryTest
{
    //--CurrencyMapperTest------------------------------------------------------

    /** Implementation to test. */
    private CurrencyMapper mapper;

    /**
     * Gets the {@code CurrencyMapper} implementation tests are performed with.
     *
     * @return the {@code CurrencyMapper} implementation tests are performed
     * with.
     */
    public CurrencyMapper getCurrencyMapper()
    {
        return this.mapper;
    }

    /**
     * Sets the {@code CurrencyMapper} implementation tests are performed
     * with.
     *
     * @param value the {@code CurrencyMapper} implementation to perform
     * tests with.
     */
    public final void setCurrencyMapper( final CurrencyMapper value )
    {
        this.setCurrencyDirectory( value );
        this.mapper = value;
    }

    //------------------------------------------------------CurrencyMapperTest--
    //--Tests-------------------------------------------------------------------

    /**
     * Tests the {@link CurrencyMapper#getDtausCode(Currency,Date)} method to
     * handle illegal arguments correctly and to return the {@code 1} DTAUS code
     * for the {@code EUR} currency.
     */
    public void testGetDtausCode() throws Exception
    {
        assert this.getCurrencyMapper() != null;

        try
        {
            this.getCurrencyMapper().getDtausCode( null, new Date() );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
        }

        try
        {
            this.getCurrencyMapper().getDtausCode(
                Currency.getInstance( "EUR" ), null );

            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
        }

        try
        {
            this.getCurrencyMapper().getDtausCode(
                Currency.getInstance( "DEM" ), new Date() );

            throw new AssertionError();
        }
        catch ( UnsupportedCurrencyException e )
        {
        }

        Assert.assertTrue( this.getCurrencyMapper().
                           getDtausCode( Currency.getInstance( "EUR" ),
                                         new Date() ) == '1' );

    }

    /**
     * Tests the {@link CurrencyMapper#getDtausCurrency(char,Date)} method to
     * handle illegal arguments correctly and to return the {@code EUR}
     * currency for the {@code 1} DTAUS code.
     */
    public void testGetDtausCurrency() throws Exception
    {
        assert this.getCurrencyMapper() != null;

        try
        {
            this.getCurrencyMapper().getDtausCurrency( '1', null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
        }


        Assert.assertEquals( this.getCurrencyMapper().
                             getDtausCurrency( '1', new Date() ),
                             Currency.getInstance( "EUR" ) );

    }

    //-------------------------------------------------------------------Tests--
}

/*
 *  jDTAUS Banking RI CurrencyDirectory
 *  Copyright (c) 2011 Christian Schulte
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
package org.jdtaus.banking.ri.currencydir.test;

import java.util.Currency;
import java.util.Date;
import junit.framework.Assert;
import org.jdtaus.banking.ri.currencydir.JaxpCurrencyDirectory;
import org.jdtaus.banking.spi.UnsupportedCurrencyException;

/**
 * Tests the {@link JaxpCurrencyDirectory} implementation to operate without finding resources.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $JDTAUS$
 */
public class JaxpCurrencyDirectory_NoCurrencies_Test extends AbstractJaxpCurrencyDirectoryTest
{

    protected ClassLoader getClassLoader()
    {
        return this.getClass().getClassLoader();
    }

    public void testCurrencyConstraints() throws Exception
    {
        System.out.println( "Skipped due to empty directory." );
    }

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
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getCurrencyMapper().getDtausCode( Currency.getInstance( "EUR" ), null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getCurrencyMapper().getDtausCode( Currency.getInstance( "DEM" ), new Date() );
            throw new AssertionError();
        }
        catch ( UnsupportedCurrencyException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

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
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

}

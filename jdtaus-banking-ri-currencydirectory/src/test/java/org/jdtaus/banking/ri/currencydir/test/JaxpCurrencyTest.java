/*
 *  jDTAUS Banking RI CurrencyDirectory
 *  Copyright (C) 2005 Christian Schulte
 *  <cs@schulte.it>
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

import java.util.Date;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jdtaus.banking.ri.currencydir.JaxpCurrency;

/**
 * Tests the {@link JaxpCurrency} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public class JaxpCurrencyTest extends TestCase
{

    public void testObject() throws Exception
    {
        JaxpCurrency c1 = new JaxpCurrency();
        JaxpCurrency c2 = new JaxpCurrency();

        Assert.assertEquals( c1, c2 );
        Assert.assertEquals( c1.hashCode(), c2.hashCode() );

        c2.setDtausCode( new Character( 'X' ) );

        Assert.assertFalse( c1.equals( c2 ) );
        Assert.assertFalse( c1.hashCode() == c2.hashCode() );

        c2.setDtausCode( null );
        c2.setEndDate( new Date() );

        Assert.assertFalse( c1.equals( c2 ) );
        Assert.assertFalse( c1.hashCode() == c2.hashCode() );

        c2.setEndDate( null );
        c2.setIsoCode( "DEM" );

        Assert.assertFalse( c1.equals( c2 ) );
        Assert.assertFalse( c1.hashCode() == c2.hashCode() );

        c2.setIsoCode( null );
        c2.setStartDate( new Date() );

        Assert.assertFalse( c1.equals( c2 ) );
        Assert.assertFalse( c1.hashCode() == c2.hashCode() );

        c2.setStartDate( null );

        Assert.assertEquals( c1, c2 );
        Assert.assertEquals( c1.hashCode(), c2.hashCode() );
    }

}

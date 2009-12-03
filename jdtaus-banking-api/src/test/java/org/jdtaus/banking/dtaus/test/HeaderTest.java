/*
 *  jDTAUS Banking API
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
package org.jdtaus.banking.dtaus.test;

import java.io.ObjectInputStream;
import java.text.ParseException;
import java.util.Currency;
import java.util.Date;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jdtaus.banking.AlphaNumericText27;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.Kontonummer;
import org.jdtaus.banking.Referenznummer10;
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.LogicalFileType;

/**
 * jUnit tests for {@code Header} implementations.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class HeaderTest extends TestCase
{

    public static Header getIllegalHeader()
    {
        return new Header();
    }

    public static Header getLegalHeader() throws ParseException
    {
        final Header legal = new Header();
        legal.setAccount( Kontonummer.valueOf( new Long( 1111111111L ) ) );
        legal.setBank( Bankleitzahl.valueOf( new Integer( 11111111 ) ) );
        legal.setBankData( Bankleitzahl.valueOf( new Integer( 11111111 ) ) );
        legal.setCurrency( Currency.getInstance( "EUR" ) );
        legal.setCustomer( AlphaNumericText27.parse( "TEST                       " ) );
        legal.setReference( Referenznummer10.valueOf( new Long( 2222222222L ) ) );
        legal.setCreateDate( new Date() );
        legal.setType( LogicalFileType.LB );
        return legal;
    }

    public void testObject() throws Exception
    {
        Header h1 = new Header();
        Header h2 = new Header();

        final Date createDate = new Date();
        final Date executionDate = new Date();

        System.out.println( h1.toString() );
        System.out.println( h2.toString() );

        Assert.assertEquals( h1, h2 );
        Assert.assertEquals( h2, h1 );
        Assert.assertEquals( h1.hashCode(), h2.hashCode() );
        Assert.assertEquals( h1, h1.clone() );
        Assert.assertEquals( h2, h2.clone() );

        h1.setAccount( Kontonummer.valueOf( new Integer( 123 ) ) );
        h1.setBank( Bankleitzahl.valueOf( new Integer( 45050001 ) ) );
        h1.setBankData( Bankleitzahl.valueOf( new Integer( 45050001 ) ) );
        h1.setCurrency( Currency.getInstance( "EUR" ) );
        h1.setCustomer( AlphaNumericText27.parse( "TEST 1                     " ) );
        h1.setReference( Referenznummer10.valueOf( new Integer( 123 ) ) );
        h1.setCreateDate( createDate );
        h1.setExecutionDate( executionDate );

        h2.setAccount( Kontonummer.valueOf( new Integer( 123 ) ) );
        h2.setBank( Bankleitzahl.valueOf( new Integer( 45050001 ) ) );
        h2.setBankData( Bankleitzahl.valueOf( new Integer( 45050001 ) ) );
        h2.setCurrency( Currency.getInstance( "EUR" ) );
        h2.setCustomer( AlphaNumericText27.parse( "TEST 1                     " ) );
        h2.setReference( Referenznummer10.valueOf( new Integer( 123 ) ) );
        h2.setCreateDate( createDate );
        h2.setExecutionDate( executionDate );

        Assert.assertEquals( h1, h2 );
        Assert.assertEquals( h2, h1 );
        Assert.assertEquals( h1.hashCode(), h2.hashCode() );
        Assert.assertEquals( h1, h1.clone() );
        Assert.assertEquals( h2, h2.clone() );

        System.out.println( h1.toString() );
        System.out.println( h2.toString() );

        Assert.assertFalse( h1.equals( HeaderTest.getIllegalHeader() ) );
        Assert.assertFalse( h1.equals( HeaderTest.getLegalHeader() ) );
        Assert.assertFalse( h2.equals( HeaderTest.getIllegalHeader() ) );
        Assert.assertFalse( h2.equals( HeaderTest.getLegalHeader() ) );
        Assert.assertFalse( h1.hashCode() == HeaderTest.getIllegalHeader().hashCode() );
        Assert.assertFalse( h1.hashCode() == HeaderTest.getLegalHeader().hashCode() );
        Assert.assertFalse( h2.hashCode() == HeaderTest.getIllegalHeader().hashCode() );
        Assert.assertFalse( h2.hashCode() == HeaderTest.getLegalHeader().hashCode() );
        Assert.assertFalse( h1 == h1.clone() );
        Assert.assertFalse( h2 == h2.clone() );

        h1 = HeaderTest.getLegalHeader();
        h2 = HeaderTest.getLegalHeader();

        Assert.assertEquals( h1, h2 );
        Assert.assertEquals( h1.hashCode(), h2.hashCode() );

        h2.setAccount( null );

        Assert.assertFalse( h1.equals( h2 ) );
        Assert.assertFalse( h1.hashCode() == h2.hashCode() );

        h2 = HeaderTest.getLegalHeader();
        h2.setBank( null );

        Assert.assertFalse( h1.equals( h2 ) );
        Assert.assertFalse( h1.hashCode() == h2.hashCode() );

        h2 = HeaderTest.getLegalHeader();
        h2.setBankData( null );

        Assert.assertFalse( h1.equals( h2 ) );
        Assert.assertFalse( h1.hashCode() == h2.hashCode() );

        h2 = HeaderTest.getLegalHeader();
        h2.setCurrency( null );

        Assert.assertFalse( h1.equals( h2 ) );
        Assert.assertFalse( h1.hashCode() == h2.hashCode() );

        h2 = HeaderTest.getLegalHeader();
        h2.setCustomer( null );

        Assert.assertFalse( h1.equals( h2 ) );
        Assert.assertFalse( h1.hashCode() == h2.hashCode() );

        h2 = HeaderTest.getLegalHeader();
        h2.setReference( null );

        Assert.assertFalse( h1.equals( h2 ) );
        Assert.assertFalse( h1.hashCode() == h2.hashCode() );

        h2 = HeaderTest.getLegalHeader();
        h2.setCreateDate( null );

        Assert.assertFalse( h1.equals( h2 ) );
        Assert.assertFalse( h1.hashCode() == h2.hashCode() );

        h2 = HeaderTest.getLegalHeader();
        h2.setExecutionDate( new Date() );

        Assert.assertFalse( h1.equals( h2 ) );
        Assert.assertFalse( h1.hashCode() == h2.hashCode() );

        h2 = HeaderTest.getLegalHeader();
        h2.setType( null );

        Assert.assertFalse( h1.equals( h2 ) );
        Assert.assertFalse( h1.hashCode() == h2.hashCode() );
    }

    public void testSerializable() throws Exception
    {
        final ObjectInputStream in = new ObjectInputStream( this.getClass().getResourceAsStream( "Header.ser" ) );
        final Header h = (Header) in.readObject();
        in.close();
        System.out.println( h );
    }

}

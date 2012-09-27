/*
 *  jDTAUS Banking API
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
package org.jdtaus.banking.test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jdtaus.banking.Bankleitzahl;

/**
 * Tests the {@code Bankleitzahl} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public class BankleitzahlTest extends TestCase
{

    /** Prefix for property names holding valid values. */
    private static final String VALID_PREFIX = "valid.";

    /** Prefix for property names holding invalid values. */
    private static final String INVALID_PREFIX = "invalid.";

    /**
     * Gets an array of valid {@code Bankleitzahl} values.
     *
     * @return An array of valid {@code Bankleitzahl} values.
     *
     * @throws IOException if reading property resources fails.
     */
    protected String[] getValid() throws IOException
    {
        String key;
        final Iterator it;
        final Map properties = this.getProperties();
        final Collection col = new LinkedList();

        for ( it = properties.keySet().iterator(); it.hasNext(); )
        {
            key = (String) it.next();
            if ( key.startsWith( VALID_PREFIX ) )
            {
                col.add( properties.get( key ) );
            }
        }

        return (String[]) col.toArray( new String[ col.size() ] );
    }

    /**
     * Gets an array of invalid {@code Bankleitzahl} values.
     *
     * @return An array of invalid {@code Bankleitzahl} values.
     *
     * @throws IOException if reading property resources fails.
     */
    protected String[] getInvalid() throws IOException
    {
        String key;
        final Iterator it;
        final Map properties = this.getProperties();
        final Collection col = new LinkedList();

        for ( it = properties.keySet().iterator(); it.hasNext(); )
        {
            key = (String) it.next();
            if ( key.startsWith( INVALID_PREFIX ) )
            {
                col.add( properties.get( key ) );
            }
        }

        return (String[]) col.toArray( new String[ col.size() ] );
    }

    private Map getProperties() throws IOException
    {
        final Properties ret = new Properties();
        ret.load( this.getClass().getResourceAsStream( "BankleitzahlTest.properties" ) );
        return ret;
    }

    public void testObject() throws Exception
    {
        final Bankleitzahl blz1 = Bankleitzahl.valueOf( new Integer( 45050001 ) );
        final Bankleitzahl blz2 = Bankleitzahl.valueOf( new Integer( 45050001 ) );
        final Bankleitzahl blz3 = Bankleitzahl.valueOf( new Integer( 45050001 ) );
        final Bankleitzahl blz4 = Bankleitzahl.valueOf( new Integer( 10000000 ) );

        System.out.println( blz1.toString() );
        System.out.println( blz2.toString() );
        System.out.println( blz3.toString() );
        System.out.println( blz4.toString() );

        Assert.assertEquals( blz1, blz2 );
        Assert.assertEquals( blz2, blz3 );
        Assert.assertEquals( blz3, blz1 );

        Assert.assertEquals( blz1.hashCode(), blz2.hashCode() );
        Assert.assertEquals( blz2.hashCode(), blz3.hashCode() );
        Assert.assertEquals( blz3.hashCode(), blz1.hashCode() );

        Assert.assertFalse( blz1.equals( blz4 ) );
        Assert.assertFalse( blz1.hashCode() == blz4.hashCode() );
    }

    public void testComparable() throws Exception
    {
        final Bankleitzahl blz1 = Bankleitzahl.valueOf( new Integer( 10000000 ) );
        final Bankleitzahl blz2 = Bankleitzahl.valueOf( new Integer( 10000001 ) );
        final Bankleitzahl blz3 = Bankleitzahl.valueOf( new Integer( 10000002 ) );

        Assert.assertEquals( blz1.compareTo( blz1 ), 0 );
        Assert.assertEquals( blz2.compareTo( blz2 ), 0 );
        Assert.assertEquals( blz3.compareTo( blz3 ), 0 );

        Assert.assertEquals( blz1.compareTo( blz2 ), -1 );
        Assert.assertEquals( blz1.compareTo( blz3 ), -1 );
        Assert.assertEquals( blz2.compareTo( blz1 ), 1 );
        Assert.assertEquals( blz2.compareTo( blz3 ), -1 );
        Assert.assertEquals( blz3.compareTo( blz1 ), 1 );
        Assert.assertEquals( blz3.compareTo( blz2 ), 1 );

        final List sorted = new ArrayList( 4 );
        sorted.add( blz1 );
        sorted.add( blz2 );
        sorted.add( blz3 );

        Collections.sort( sorted );

        for ( Iterator it = sorted.iterator(); it.hasNext(); )
        {
            final Bankleitzahl blz = (Bankleitzahl) it.next();
            System.out.println( blz.format( Bankleitzahl.ELECTRONIC_FORMAT ) + '\t' +
                                blz.format( Bankleitzahl.LETTER_FORMAT ) );

        }

        try
        {
            blz1.compareTo( null );
            Assert.fail();
        }
        catch ( NullPointerException e )
        {
            System.out.println( e.toString() );
            Assert.assertNotNull( e.getMessage() );
        }

        try
        {
            blz1.compareTo( new Object() );
            Assert.fail();
        }
        catch ( ClassCastException e )
        {
            System.out.println( e.toString() );
            Assert.assertNotNull( e.getMessage() );
        }
    }

    public void testParse() throws Exception
    {
        final String[] valid = this.getValid();
        final String[] invalid = this.getInvalid();

        for ( int i = valid.length - 1; i >= 0; i-- )
        {
            final Bankleitzahl blz = Bankleitzahl.parse( valid[i] );
            Assert.assertEquals( blz, Bankleitzahl.parse( blz.format( Bankleitzahl.ELECTRONIC_FORMAT ) ) );
            Assert.assertEquals( blz, Bankleitzahl.parse( blz.format( Bankleitzahl.LETTER_FORMAT ) ) );
        }
        for ( int i = invalid.length - 1; i >= 0; i-- )
        {
            try
            {
                Bankleitzahl.parse( invalid[i] );
                throw new AssertionError( invalid[i] );
            }
            catch ( final ParseException e )
            {
                System.out.println( e.toString() );
                Assert.assertNotNull( e.getMessage() );
            }
        }
    }

    public void testIsClearingAreaCodeSupported() throws Exception
    {
        final Bankleitzahl supported = Bankleitzahl.valueOf( new Integer( 10000000 ) );
        final Bankleitzahl notSupported = Bankleitzahl.valueOf( new Integer( 9999999 ) );

        Assert.assertTrue( supported.isClearingAreaCodeSupported() );
        Assert.assertFalse( notSupported.isClearingAreaCodeSupported() );
        Assert.assertEquals( 1, supported.getClearingAreaCode() );

        try
        {
            notSupported.getClearingAreaCode();
            throw new AssertionError();
        }
        catch ( final UnsupportedOperationException e )
        {
            System.out.println( e.toString() );
        }
    }

    public void testIsLocalityCodeSupported() throws Exception
    {
        final Bankleitzahl supported = Bankleitzahl.valueOf( new Integer( 100000 ) );
        final Bankleitzahl notSupported = Bankleitzahl.valueOf( new Integer( 99999 ) );

        Assert.assertTrue( supported.isLocalityCodeSupported() );
        Assert.assertFalse( notSupported.isLocalityCodeSupported() );
        Assert.assertEquals( 1, supported.getLocalityCode() );

        try
        {
            notSupported.getLocalityCode();
            throw new AssertionError();
        }
        catch ( final UnsupportedOperationException e )
        {
            System.out.println( e.toString() );
        }
    }

    public void testIsNetworkCodeSupported() throws Exception
    {
        final Bankleitzahl supported = Bankleitzahl.valueOf( new Integer( 10000 ) );
        final Bankleitzahl notSupported = Bankleitzahl.valueOf( new Integer( 9999 ) );

        Assert.assertTrue( supported.isNetworkCodeSupported() );
        Assert.assertFalse( notSupported.isNetworkCodeSupported() );
        Assert.assertEquals( 1, supported.getNetworkCode() );

        try
        {
            notSupported.getNetworkCode();
            throw new AssertionError();
        }
        catch ( final UnsupportedOperationException e )
        {
            System.out.println( e.toString() );
        }
    }

    public void testGetInstituteCode() throws Exception
    {
        final Bankleitzahl blz = Bankleitzahl.valueOf( new Integer( 1 ) );
        Assert.assertEquals( 1, blz.getInstituteCode() );
    }

    public void testCheckBankleitzahl() throws Exception
    {
        Assert.assertTrue( Bankleitzahl.checkBankleitzahl( new Integer( 1 ) ) );
        Assert.assertTrue( Bankleitzahl.checkBankleitzahl( new Integer( 10000000 ) ) );
        Assert.assertFalse( Bankleitzahl.checkBankleitzahl( new Integer( 0 ) ) );
        Assert.assertFalse( Bankleitzahl.checkBankleitzahl( new Integer( 90000000 ) ) );
    }

    public void testSerializable() throws Exception
    {
        final ObjectInputStream in = new ObjectInputStream( this.getClass().getResourceAsStream( "Bankleitzahl.ser" ) );
        final Bankleitzahl b = (Bankleitzahl) in.readObject();
        in.close();
        System.out.println( b.toString() );
        Assert.assertEquals( new Integer( 11111111 ), new Integer( b.intValue() ) );
    }

}

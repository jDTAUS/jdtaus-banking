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
import org.jdtaus.banking.Kontonummer;

/**
 * Tests the {@code Kontonummer} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public class KontonummerTest extends TestCase
{

    /** Prefix for property names holding valid values. */
    private static final String VALID_PREFIX = "valid.";

    /** Prefix for property names holding invalid values. */
    private static final String INVALID_PREFIX = "invalid.";

    /**
     * Gets an array of valid {@code Kontonummer} values.
     *
     * @return An array of valid {@code Kontonummer} values.
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
     * Gets an array of invalid {@code Kontonummer} values.
     *
     * @return An array of invalid {@code Kontonummer} values.
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
        ret.load( this.getClass().getResourceAsStream( "KontonummerTest.properties" ) );
        return ret;
    }

    public void testObject() throws Exception
    {
        final Kontonummer kto1 = Kontonummer.valueOf( new Long( 9999999999L ) );
        final Kontonummer kto2 = Kontonummer.valueOf( new Long( 9999999999L ) );
        final Kontonummer kto3 = Kontonummer.valueOf( new Long( 9999999999L ) );
        final Kontonummer kto4 = Kontonummer.valueOf( new Long( 1111111111L ) );

        System.out.println( kto1.toString() );
        System.out.println( kto2.toString() );
        System.out.println( kto3.toString() );
        System.out.println( kto4.toString() );

        Assert.assertEquals( kto1, kto2 );
        Assert.assertEquals( kto2, kto3 );
        Assert.assertEquals( kto3, kto1 );

        Assert.assertEquals( kto1.hashCode(), kto2.hashCode() );
        Assert.assertEquals( kto2.hashCode(), kto3.hashCode() );
        Assert.assertEquals( kto3.hashCode(), kto1.hashCode() );

        Assert.assertFalse( kto1.equals( kto4 ) );
        Assert.assertFalse( kto1.hashCode() == kto4.hashCode() );
    }

    public void testComparable() throws Exception
    {
        final Kontonummer kto1 = Kontonummer.valueOf( new Long( 1000000000L ) );
        final Kontonummer kto2 = Kontonummer.valueOf( new Long( 1000000001L ) );
        final Kontonummer kto3 = Kontonummer.valueOf( new Long( 1000000002L ) );

        Assert.assertEquals( kto1.compareTo( kto1 ), 0 );
        Assert.assertEquals( kto2.compareTo( kto2 ), 0 );
        Assert.assertEquals( kto3.compareTo( kto3 ), 0 );

        Assert.assertEquals( kto1.compareTo( kto2 ), -1 );
        Assert.assertEquals( kto1.compareTo( kto3 ), -1 );
        Assert.assertEquals( kto2.compareTo( kto1 ), 1 );
        Assert.assertEquals( kto2.compareTo( kto3 ), -1 );
        Assert.assertEquals( kto3.compareTo( kto1 ), 1 );
        Assert.assertEquals( kto3.compareTo( kto2 ), 1 );

        final List sorted = new ArrayList( 4 );
        sorted.add( kto1 );
        sorted.add( kto2 );
        sorted.add( kto3 );

        Collections.sort( sorted );

        for ( Iterator it = sorted.iterator(); it.hasNext(); )
        {
            final Kontonummer kto = (Kontonummer) it.next();
            System.out.println( kto.format( Kontonummer.ELECTRONIC_FORMAT ) + '\t' +
                                kto.format( Kontonummer.LETTER_FORMAT ) );

        }

        try
        {
            kto1.compareTo( null );
            Assert.fail();
        }
        catch ( NullPointerException e )
        {
            System.out.println( e.toString() );
            Assert.assertNotNull( e.getMessage() );
        }

        try
        {
            kto1.compareTo( new Object() );
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
            final Kontonummer kto = Kontonummer.parse( valid[i] );
            Assert.assertEquals( kto, Kontonummer.parse( kto.format( Kontonummer.ELECTRONIC_FORMAT ) ) );
            Assert.assertEquals( kto, Kontonummer.parse( kto.format( Kontonummer.LETTER_FORMAT ) ) );
        }
        for ( int i = invalid.length - 1; i >= 0; i-- )
        {
            try
            {
                Kontonummer.parse( invalid[i] );
                throw new AssertionError( invalid[i] );
            }
            catch ( final ParseException e )
            {
                System.out.println( e.toString() );
                Assert.assertNotNull( e.getMessage() );
            }
        }
    }

    public void testSerializable() throws Exception
    {
        final ObjectInputStream in = new ObjectInputStream( this.getClass().getResourceAsStream( "Kontonummer.ser" ) );
        final Kontonummer k = (Kontonummer) in.readObject();
        in.close();
        System.out.println( k.toString() );
        Assert.assertEquals( new Long( 1111111111 ), new Long( k.longValue() ) );
    }

    //-------------------------------------------------------------------Tests--
}

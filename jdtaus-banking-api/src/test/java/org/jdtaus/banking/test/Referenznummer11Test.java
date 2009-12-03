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
import org.jdtaus.banking.Referenznummer11;

/**
 * Tests the {@code Referenznummer11} implementation.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class Referenznummer11Test extends TestCase
{

    /** Prefix for property names holding valid values. */
    private static final String VALID_PREFIX = "valid.";

    /** Prefix for property names holding invalid values. */
    private static final String INVALID_PREFIX = "invalid.";

    /**
     * Gets an array of valid {@code Referenznummer11} values.
     *
     * @return An array of valid {@code Referenznummer11} values.
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
     * Gets an array of invalid {@code Referenznummer11} values.
     *
     * @return An array of invalid {@code Referenznummer11} values.
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
        ret.load( this.getClass().getResourceAsStream( "Referenznummer11Test.properties" ) );
        return ret;
    }

    public void testObject() throws Exception
    {
        final Referenznummer11 ref1 = Referenznummer11.valueOf( new Long( 99999999999L ) );
        final Referenznummer11 ref2 = Referenznummer11.valueOf( new Long( 99999999999L ) );
        final Referenznummer11 ref3 = Referenznummer11.valueOf( new Long( 99999999999L ) );
        final Referenznummer11 ref4 = Referenznummer11.valueOf( new Long( 11111111111L ) );

        System.out.println( ref1.toString() );
        System.out.println( ref2.toString() );
        System.out.println( ref3.toString() );
        System.out.println( ref4.toString() );

        Assert.assertEquals( ref1, ref2 );
        Assert.assertEquals( ref2, ref3 );
        Assert.assertEquals( ref3, ref1 );

        Assert.assertEquals( ref1.hashCode(), ref2.hashCode() );
        Assert.assertEquals( ref2.hashCode(), ref3.hashCode() );
        Assert.assertEquals( ref3.hashCode(), ref1.hashCode() );

        Assert.assertFalse( ref1.equals( ref4 ) );
        Assert.assertFalse( ref1.hashCode() == ref4.hashCode() );
    }

    public void testComparable() throws Exception
    {
        final Referenznummer11 ref1 = Referenznummer11.valueOf( new Long( 10000000000L ) );
        final Referenznummer11 ref2 = Referenznummer11.valueOf( new Long( 10000000001L ) );
        final Referenznummer11 ref3 = Referenznummer11.valueOf( new Long( 10000000002L ) );
        final Referenznummer11 leadingZeroes1 = Referenznummer11.valueOf( new Long( 1L ) );
        final Referenznummer11 leadingZeroes2 = Referenznummer11.valueOf( new Long( 11L ) );
        final Referenznummer11 leadingZeroes3 = Referenznummer11.valueOf( new Long( 111L ) );
        final Referenznummer11 leadingZeroes4 = Referenznummer11.valueOf( new Long( 1111L ) );
        final Referenznummer11 leadingZeroes5 = Referenznummer11.valueOf( new Long( 11111L ) );
        final Referenznummer11 leadingZeroes6 = Referenznummer11.valueOf( new Long( 111111L ) );
        final Referenznummer11 leadingZeroes7 = Referenznummer11.valueOf( new Long( 1111111L ) );
        final Referenznummer11 leadingZeroes8 = Referenznummer11.valueOf( new Long( 11111111L ) );
        final Referenznummer11 leadingZeroes9 = Referenznummer11.valueOf( new Long( 111111111L ) );
        final Referenznummer11 leadingZeroes10 = Referenznummer11.valueOf( new Long( 1111111111L ) );
        final Referenznummer11 leadingZeroes11 = Referenznummer11.valueOf( new Long( 11111111111L ) );

        Assert.assertEquals( ref1.compareTo( ref1 ), 0 );
        Assert.assertEquals( ref2.compareTo( ref2 ), 0 );
        Assert.assertEquals( ref3.compareTo( ref3 ), 0 );

        Assert.assertEquals( ref1.compareTo( ref2 ), -1 );
        Assert.assertEquals( ref1.compareTo( ref3 ), -1 );
        Assert.assertEquals( ref2.compareTo( ref1 ), 1 );
        Assert.assertEquals( ref2.compareTo( ref3 ), -1 );
        Assert.assertEquals( ref3.compareTo( ref1 ), 1 );
        Assert.assertEquals( ref3.compareTo( ref2 ), 1 );

        final List sorted = new ArrayList( 4 );
        sorted.add( ref1 );
        sorted.add( ref2 );
        sorted.add( ref3 );
        sorted.add( leadingZeroes1 );
        sorted.add( leadingZeroes2 );
        sorted.add( leadingZeroes3 );
        sorted.add( leadingZeroes4 );
        sorted.add( leadingZeroes5 );
        sorted.add( leadingZeroes6 );
        sorted.add( leadingZeroes7 );
        sorted.add( leadingZeroes8 );
        sorted.add( leadingZeroes9 );
        sorted.add( leadingZeroes10 );
        sorted.add( leadingZeroes11 );

        Collections.sort( sorted );

        for ( Iterator it = sorted.iterator(); it.hasNext(); )
        {
            final Referenznummer11 ref = (Referenznummer11) it.next();
            System.out.println( ref.format( Referenznummer11.ELECTRONIC_FORMAT ) + '\t' +
                                ref.format( Referenznummer11.LETTER_FORMAT ) );

        }

        try
        {
            ref1.compareTo( null );
            Assert.fail();
        }
        catch ( NullPointerException e )
        {
            System.out.println( e.toString() );
            Assert.assertNotNull( e.getMessage() );
        }

        try
        {
            ref1.compareTo( new Object() );
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
            final Referenznummer11 ref = Referenznummer11.parse( valid[i] );
            Assert.assertEquals( ref, Referenznummer11.parse( ref.format( Referenznummer11.ELECTRONIC_FORMAT ) ) );
            Assert.assertEquals( ref, Referenznummer11.parse( ref.format( Referenznummer11.LETTER_FORMAT ) ) );
        }
        for ( int i = invalid.length - 1; i >= 0; i-- )
        {
            try
            {
                Referenznummer11.parse( invalid[i] );
                throw new AssertionError( invalid[i] );
            }
            catch ( ParseException e )
            {
                System.out.println( e.toString() );
                Assert.assertNotNull( e.getMessage() );
            }
        }
    }

    public void testSerializable() throws Exception
    {
        final ObjectInputStream in =
            new ObjectInputStream( this.getClass().getResourceAsStream( "Referenznummer11.ser" ) );

        final Referenznummer11 r = (Referenznummer11) in.readObject();
        in.close();
        System.out.println( r.toString() );
        Assert.assertEquals( new Long( 11111111111L ), new Long( r.longValue() ) );
    }

}

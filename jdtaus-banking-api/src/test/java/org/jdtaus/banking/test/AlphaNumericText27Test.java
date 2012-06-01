/*
 *  jDTAUS Banking API
 *  Copyright (C) 2005 Christian Schulte
 *  <schulte2005@users.sourceforge.net>
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
import org.jdtaus.banking.AlphaNumericText27;

/**
 * Tests the {@code AlphaNumericText27} implementation.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $JDTAUS$
 */
public class AlphaNumericText27Test extends TestCase
{

    /** Prefix for property names holding valid values. */
    private static final String VALID_PREFIX = "valid.";

    /** Prefix for property names holding invalid values. */
    private static final String INVALID_PREFIX = "invalid.";

    /**
     * Gets an array of valid {@code AlphaNumericText27} values.
     *
     * @return An array of valid {@code AlphaNumericText27} values.
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
     * Gets an array of invalid {@code AlphaNumericText27} values.
     *
     * @return An array of invalid {@code AlphaNumericText27} values.
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
        ret.load( this.getClass().getResourceAsStream( "AlphaNumericText27Test.properties" ) );
        return ret;
    }

    public void testCharSequence() throws Exception
    {
        final AlphaNumericText27 txt1 = AlphaNumericText27.parse( "TEST" );
        final AlphaNumericText27 txt2 = AlphaNumericText27.parse( "" );

        System.out.println( txt1.toString() );
        System.out.println( txt2.toString() );

        Assert.assertTrue( txt1.length() == 4 );
        Assert.assertTrue( txt2.length() == 0 );
        Assert.assertTrue( txt1.charAt( 0 ) == 'T' );
        Assert.assertTrue( txt1.charAt( 3 ) == 'T' );

        try
        {
            txt2.charAt( 0 );
            Assert.fail();
        }
        catch ( IndexOutOfBoundsException e )
        {
            System.out.println( e.toString() );
            Assert.assertNotNull( e.getMessage() );
        }
    }

    public void testObject() throws Exception
    {
        final AlphaNumericText27 txt1 = AlphaNumericText27.parse( "TEST" );
        final AlphaNumericText27 txt2 = AlphaNumericText27.parse( "TEST" );
        final AlphaNumericText27 txt3 = AlphaNumericText27.parse( "TEST" );
        final AlphaNumericText27 txt4 = AlphaNumericText27.parse( "" );

        Assert.assertEquals( txt1, txt2 );
        Assert.assertEquals( txt2, txt3 );
        Assert.assertEquals( txt3, txt1 );

        Assert.assertEquals( txt1.hashCode(), txt2.hashCode() );
        Assert.assertEquals( txt2.hashCode(), txt3.hashCode() );
        Assert.assertEquals( txt3.hashCode(), txt1.hashCode() );

        Assert.assertFalse( txt1.equals( txt4 ) );
        Assert.assertFalse( txt1.hashCode() == txt4.hashCode() );
    }

    public void testComparable() throws Exception
    {
        final AlphaNumericText27 txt1 = AlphaNumericText27.parse( "A" );
        final AlphaNumericText27 txt2 = AlphaNumericText27.parse( "B" );
        final AlphaNumericText27 txt3 = AlphaNumericText27.parse( "C" );

        Assert.assertEquals( txt1.compareTo( txt1 ), 0 );
        Assert.assertEquals( txt2.compareTo( txt2 ), 0 );
        Assert.assertEquals( txt3.compareTo( txt3 ), 0 );

        Assert.assertTrue( txt1.compareTo( txt2 ) < 0 );
        Assert.assertTrue( txt1.compareTo( txt3 ) < 0 );
        Assert.assertTrue( txt2.compareTo( txt1 ) > 0 );
        Assert.assertTrue( txt2.compareTo( txt3 ) < 0 );
        Assert.assertTrue( txt3.compareTo( txt1 ) > 0 );
        Assert.assertTrue( txt3.compareTo( txt2 ) > 0 );

        final List sorted = new ArrayList( 4 );
        sorted.add( txt1 );
        sorted.add( txt2 );
        sorted.add( txt3 );

        Collections.sort( sorted );

        for ( Iterator it = sorted.iterator(); it.hasNext(); )
        {
            final AlphaNumericText27 txt = (AlphaNumericText27) it.next();
            System.out.println( txt.format() );
        }

        try
        {
            txt1.compareTo( null );
            Assert.fail();
        }
        catch ( NullPointerException e )
        {
            System.out.println( e.toString() );
            Assert.assertNotNull( e.getMessage() );
        }

        try
        {
            txt1.compareTo( new Object() );
            Assert.fail();
        }
        catch ( ClassCastException e )
        {
            System.out.println( e.toString() );
            Assert.assertNotNull( e.getMessage() );
        }

    }

    public void testIsEmpty() throws Exception
    {
        final AlphaNumericText27 txt1 = AlphaNumericText27.parse( "" );
        final AlphaNumericText27 txt2 = AlphaNumericText27.parse( "           " );
        final AlphaNumericText27 txt3 = AlphaNumericText27.parse( "  A    " );

        Assert.assertTrue( txt1.isEmpty() );
        Assert.assertTrue( txt2.isEmpty() );
        Assert.assertFalse( txt3.isEmpty() );
    }

    public void testParse() throws Exception
    {
        final String[] valid = this.getValid();
        final String[] invalid = this.getInvalid();

        for ( int i = valid.length - 1; i >= 0; i-- )
        {
            AlphaNumericText27.parse( valid[i] );
        }
        for ( int i = invalid.length - 1; i >= 0; i-- )
        {
            try
            {
                AlphaNumericText27.parse( invalid[i] );
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
        final ObjectInputStream in = new ObjectInputStream(
            this.getClass().getResourceAsStream( "AlphaNumericText27.ser" ) );

        final AlphaNumericText27 t = (AlphaNumericText27) in.readObject();
        in.close();
        System.out.println( t.toString() );
        Assert.assertEquals( "ABCDEFGHIJKLMNOPQRSTUVWXYZ", t.format() );
    }

}

/*
 *  jDTAUS Banking API
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
package org.jdtaus.banking.test;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.jdtaus.banking.Textschluessel;

/**
 * jUnit tests for {@code Textschluessel} implementations.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class TextschluesselTest extends TestCase
{
    //--Tests-------------------------------------------------------------------

    public void testObject() throws Exception
    {
        final Textschluessel t1 = new Textschluessel();
        final Textschluessel t2 = new Textschluessel();

        t1.setDebit( false );
        t1.setRemittance( false );
        t1.setKey( 0 );
        t1.setExtension( 0 );

        t2.setDebit( false );
        t2.setRemittance( false );
        t2.setKey( 0 );
        t2.setExtension( 0 );

        Assert.assertEquals( t1, t2 );
        Assert.assertEquals( t1.hashCode(), t2.hashCode() );

        t2.setExtension( 1 );

        Assert.assertFalse( t1.equals( t2 ) );
        Assert.assertFalse( t1.hashCode() == t2.hashCode() );

        t2.setExtension( 0 );
        t2.setKey( 1 );

        Assert.assertFalse( t1.equals( t2 ) );
        Assert.assertFalse( t1.hashCode() == t2.hashCode() );

        t2.setKey( 0 );
        t2.setVariable( true );

        Assert.assertFalse( t1.equals( t2 ) );
        Assert.assertFalse( t1.hashCode() == t2.hashCode() );

        t2.setVariable( false );

        Assert.assertEquals( t1, t2 );
        Assert.assertEquals( t1.hashCode(), t2.hashCode() );
    }

    public void testComparable() throws Exception
    {
        final Textschluessel t1 = new Textschluessel();
        t1.setKey( 1 );
        t1.setExtension( 1 );

        final Textschluessel equal = new Textschluessel();
        equal.setKey( 1 );
        equal.setExtension( 1 );

        final Textschluessel greater1 = new Textschluessel();
        greater1.setKey( 2 );
        greater1.setExtension( 1 );

        final Textschluessel greater2 = new Textschluessel();
        greater2.setKey( 1 );
        greater2.setExtension( 2 );

        final Textschluessel lesser1 = new Textschluessel();
        lesser1.setKey( 0 );
        lesser1.setExtension( 1 );

        final Textschluessel lesser2 = new Textschluessel();
        lesser2.setKey( 1 );
        lesser2.setExtension( 0 );

        Assert.assertTrue( t1.compareTo( equal ) == 0 );
        Assert.assertTrue( t1.compareTo( greater1 ) < 0 );
        Assert.assertTrue( t1.compareTo( greater2 ) < 0 );
        Assert.assertTrue( t1.compareTo( lesser1 ) > 0 );
        Assert.assertTrue( t1.compareTo( lesser2 ) > 0 );

        try
        {
            t1.compareTo( null );
            this.fail();
        }
        catch ( NullPointerException e )
        {
        }

        try
        {
            t1.compareTo( new Object() );
            this.fail();
        }
        catch ( ClassCastException e )
        {
        }

    }

    //-------------------------------------------------------------------Tests--
}

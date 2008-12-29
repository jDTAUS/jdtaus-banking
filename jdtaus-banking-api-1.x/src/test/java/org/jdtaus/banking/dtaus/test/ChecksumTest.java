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
package org.jdtaus.banking.dtaus.test;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.jdtaus.banking.dtaus.Checksum;

/**
 * jUnit tests for {@code Checksum} implementations.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class ChecksumTest extends TestCase
{
    //--ChecksumTest------------------------------------------------------------

    public void testObject() throws Exception
    {
        final Checksum c1 = new Checksum();
        final Checksum c2 = new Checksum();
        final Checksum c3 = new Checksum();

        c3.setSumAmount( 1L );
        c3.setSumTargetAccount( 2L );
        c3.setSumTargetBank( 4L );
        c3.setTransactionCount( 5 );

        System.out.println( c1.toString() );
        System.out.println( c2.toString() );
        System.out.println( c3.toString() );

        Assert.assertEquals( c1, c2 );
        Assert.assertEquals( c2, c1 );
        Assert.assertEquals( c1.hashCode(), c2.hashCode() );
        Assert.assertEquals( c1, c1.clone() );
        Assert.assertEquals( c2, c2.clone() );
        Assert.assertEquals( c3, c3.clone() );

        Assert.assertFalse( c1.equals( c3 ) );
        Assert.assertFalse( c3.equals( c1 ) );
        Assert.assertFalse( c1.hashCode() == c3.hashCode() );
        Assert.assertFalse( c1 == c1.clone() );
        Assert.assertFalse( c2 == c2.clone() );
        Assert.assertFalse( c3 == c3.clone() );

        c2.setSumAmount( 100L );

        Assert.assertFalse( c1.equals( c2 ) );
        Assert.assertFalse( c1.hashCode() == c2.hashCode() );

        c2.setSumAmount( 0L );
        c2.setSumTargetAccount( 100L );

        Assert.assertFalse( c1.equals( c2 ) );
        Assert.assertFalse( c1.hashCode() == c2.hashCode() );

        c2.setSumTargetAccount( 0L );
        c2.setSumTargetBank( 100L );

        Assert.assertFalse( c1.equals( c2 ) );
        Assert.assertFalse( c1.hashCode() == c2.hashCode() );

        c2.setSumTargetBank( 0L );
        c2.setTransactionCount( 100 );

        Assert.assertFalse( c1.equals( c2 ) );
        Assert.assertFalse( c1.hashCode() == c2.hashCode() );

        c2.setTransactionCount( 0 );

        Assert.assertEquals( c1, c2 );
        Assert.assertEquals( c1.hashCode(), c2.hashCode() );

        c2.add( TransactionTest.getLegalTransaction() );

        Assert.assertFalse( c1.equals( c2 ) );
        Assert.assertFalse( c1.hashCode() == c2.hashCode() );

        c2.subtract( TransactionTest.getLegalTransaction() );

        Assert.assertEquals( c1, c2 );
        Assert.assertEquals( c1.hashCode(), c2.hashCode() );
    }

    public void testAddSubtract() throws Exception
    {
        final Checksum c = new Checksum();

        c.add( TransactionTest.getLegalTransaction() );
        Assert.assertFalse( c.equals( new Checksum() ) );

        c.subtract( TransactionTest.getLegalTransaction() );
        Assert.assertEquals( c, new Checksum() );
    }

    //------------------------------------------------------------ChecksumTest--
}

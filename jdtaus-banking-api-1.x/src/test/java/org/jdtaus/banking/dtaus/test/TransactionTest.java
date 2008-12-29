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

import java.math.BigInteger;
import java.text.ParseException;
import java.util.Currency;
import java.util.Locale;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jdtaus.banking.AlphaNumericText27;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.Kontonummer;
import org.jdtaus.banking.Referenznummer11;
import org.jdtaus.banking.Textschluessel;
import org.jdtaus.banking.dtaus.Transaction;

/**
 * jUnit tests for {@code Transaction} implementations.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class TransactionTest extends TestCase
{
    //--TransactionTest---------------------------------------------------------

    public static Transaction getIllegalTransaction()
    {
        return new Transaction();
    }

    public static Transaction getLegalTransaction() throws ParseException
    {
        final Transaction legal = new Transaction();
        final Textschluessel type = new Textschluessel();
        type.setDebit( true );
        type.setKey( 4 );
        type.setExtension( 0 );
        type.setRemittance( false );
        type.setVariable( false );
        type.setShortDescription( Locale.getDefault(), "TEST" );

        final AlphaNumericText27[] d = new AlphaNumericText27[ 14 ];
        d[0] = AlphaNumericText27.parse( "DESCRIPTION 1              " );
        d[1] = AlphaNumericText27.parse( "DESCRIPTION 2              " );
        d[2] = AlphaNumericText27.parse( "DESCRIPTION 3              " );
        d[3] = AlphaNumericText27.parse( "DESCRIPTION 4              " );
        d[4] = AlphaNumericText27.parse( "DESCRIPTION 5              " );
        d[5] = AlphaNumericText27.parse( "DESCRIPTION 6              " );
        d[6] = AlphaNumericText27.parse( "DESCRIPTION 7              " );
        d[7] = AlphaNumericText27.parse( "DESCRIPTION 8              " );
        d[8] = AlphaNumericText27.parse( "DESCRIPTION 9              " );
        d[9] = AlphaNumericText27.parse( "DESCRIPTION 10             " );
        d[10] = AlphaNumericText27.parse( "DESCRIPTION 11             " );
        d[11] = AlphaNumericText27.parse( "DESCRIPTION 12             " );
        d[12] = AlphaNumericText27.parse( "DESCRIPTION 13             " );
        d[13] = AlphaNumericText27.parse( "DESCRIPTION 14             " );

        legal.setAmount( new BigInteger( "100" ) );
        legal.setCurrency( Currency.getInstance( "EUR" ) );
        legal.setDescriptions( d );
        legal.setExecutiveAccount(
            Kontonummer.valueOf( new Long( 1111111111L ) ) );

        legal.setExecutiveBank( Bankleitzahl.valueOf( new Integer( 45050001 ) ) );
        legal.setExecutiveExt( AlphaNumericText27.parse(
                               "TEST                       " ) );

        legal.setExecutiveName( AlphaNumericText27.parse(
                                "TEST                       " ) );

        legal.setPrimaryBank( Bankleitzahl.valueOf( new Integer( 45050001 ) ) );
        legal.setReference( Referenznummer11.valueOf( new Long( 11111111111L ) ) );
        legal.setTargetAccount( Kontonummer.valueOf( new Long( 1111111111L ) ) );
        legal.setTargetBank( Bankleitzahl.valueOf( new Integer( 45050001 ) ) );
        legal.setTargetExt( AlphaNumericText27.parse(
                            "TEST                       " ) );

        legal.setTargetName( AlphaNumericText27.parse(
                             "TEST                       " ) );

        legal.setType( type );

        return legal;
    }

    public void testObject() throws Exception
    {
        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();

        Assert.assertEquals( t1, t2 );
        Assert.assertEquals( t2, t1 );
        Assert.assertEquals( t1.hashCode(), t2.hashCode() );
        Assert.assertEquals( t1, t1.clone() );
        Assert.assertEquals( t2, t2.clone() );

        System.out.println( t1.toString() );
        System.out.println( t2.toString() );

        t1.setAmount( new BigInteger( "0" ) );
        t1.setCurrency( Currency.getInstance( "EUR" ) );
        t1.setDescriptions( null );
        t1.setExecutiveAccount( Kontonummer.valueOf( new Integer( 1 ) ) );
        t1.setExecutiveBank( Bankleitzahl.valueOf( new Integer( 45050001 ) ) );
        t1.setExecutiveExt( AlphaNumericText27.parse( "TEST" ) );
        t1.setExecutiveName( AlphaNumericText27.parse( "TEST 2" ) );
        t1.setPrimaryBank( Bankleitzahl.valueOf( new Integer( 45050001 ) ) );
        t1.setReference( Referenznummer11.valueOf( new Integer( 123 ) ) );
        t1.setTargetAccount( Kontonummer.valueOf( new Integer( 123 ) ) );
        t1.setTargetBank( Bankleitzahl.valueOf( new Integer( 45050001 ) ) );
        t1.setTargetExt( AlphaNumericText27.parse( "TEST 3" ) );
        t1.setTargetName( AlphaNumericText27.parse( "TEST 4" ) );

        t2.setAmount( new BigInteger( "0" ) );
        t2.setCurrency( Currency.getInstance( "EUR" ) );
        t2.setDescriptions( null );
        t2.setExecutiveAccount( Kontonummer.valueOf( new Integer( 1 ) ) );
        t2.setExecutiveBank( Bankleitzahl.valueOf( new Integer( 45050001 ) ) );
        t2.setExecutiveExt( AlphaNumericText27.parse( "TEST" ) );
        t2.setExecutiveName( AlphaNumericText27.parse( "TEST 2" ) );
        t2.setPrimaryBank( Bankleitzahl.valueOf( new Integer( 45050001 ) ) );
        t2.setReference( Referenznummer11.valueOf( new Integer( 123 ) ) );
        t2.setTargetAccount( Kontonummer.valueOf( new Integer( 123 ) ) );
        t2.setTargetBank( Bankleitzahl.valueOf( new Integer( 45050001 ) ) );
        t2.setTargetExt( AlphaNumericText27.parse( "TEST 3" ) );
        t2.setTargetName( AlphaNumericText27.parse( "TEST 4" ) );

        Assert.assertEquals( t1, t2 );
        Assert.assertEquals( t2, t1 );
        Assert.assertEquals( t1.hashCode(), t2.hashCode() );
        Assert.assertEquals( t1, t1.clone() );
        Assert.assertEquals( t2, t2.clone() );

        System.out.println( t1.toString() );
        System.out.println( t2.toString() );

        Assert.assertFalse( t1.equals( TransactionTest.getIllegalTransaction() ) );
        Assert.assertFalse( t1.equals( TransactionTest.getLegalTransaction() ) );
        Assert.assertFalse( t2.equals( TransactionTest.getIllegalTransaction() ) );
        Assert.assertFalse( t2.equals( TransactionTest.getLegalTransaction() ) );
        Assert.assertFalse( t1.hashCode() == TransactionTest.
                            getIllegalTransaction().
                            hashCode() );

        Assert.assertFalse( t1.hashCode() == TransactionTest.getLegalTransaction().
                            hashCode() );

        Assert.assertFalse( t2.hashCode() == TransactionTest.
                            getIllegalTransaction().
                            hashCode() );

        Assert.assertFalse( t2.hashCode() == TransactionTest.getLegalTransaction().
                            hashCode() );

        Assert.assertFalse( t1 == t1.clone() );
        Assert.assertFalse( t2 == t2.clone() );

        t1 = TransactionTest.getLegalTransaction();
        t2 = TransactionTest.getLegalTransaction();

        Assert.assertEquals( t1, t2 );
        Assert.assertEquals( t1.hashCode(), t2.hashCode() );

        t2.setAmount( null );

        Assert.assertFalse( t1.equals( t2 ) );
        Assert.assertFalse( t1.hashCode() == t2.hashCode() );

        t2 = TransactionTest.getLegalTransaction();
        t2.setCurrency( null );

        Assert.assertFalse( t1.equals( t2 ) );
        Assert.assertFalse( t1.hashCode() == t2.hashCode() );

        t2 = TransactionTest.getLegalTransaction();
        t2.setDescriptions( null );

        Assert.assertFalse( t1.equals( t2 ) );
        Assert.assertFalse( t1.hashCode() == t2.hashCode() );

        t2 = TransactionTest.getLegalTransaction();
        t2.setExecutiveAccount( null );

        Assert.assertFalse( t1.equals( t2 ) );
        Assert.assertFalse( t1.hashCode() == t2.hashCode() );

        t2 = TransactionTest.getLegalTransaction();
        t2.setExecutiveBank( null );

        Assert.assertFalse( t1.equals( t2 ) );
        Assert.assertFalse( t1.hashCode() == t2.hashCode() );

        t2 = TransactionTest.getLegalTransaction();
        t2.setExecutiveExt( null );

        Assert.assertFalse( t1.equals( t2 ) );
        Assert.assertFalse( t1.hashCode() == t2.hashCode() );

        t2 = TransactionTest.getLegalTransaction();
        t2.setExecutiveName( null );

        Assert.assertFalse( t1.equals( t2 ) );
        Assert.assertFalse( t1.hashCode() == t2.hashCode() );

        t2 = TransactionTest.getLegalTransaction();
        t2.setPrimaryBank( null );

        Assert.assertFalse( t1.equals( t2 ) );
        Assert.assertFalse( t1.hashCode() == t2.hashCode() );

        t2 = TransactionTest.getLegalTransaction();
        t2.setReference( null );

        Assert.assertFalse( t1.equals( t2 ) );
        Assert.assertFalse( t1.hashCode() == t2.hashCode() );

        t2 = TransactionTest.getLegalTransaction();
        t2.setTargetAccount( null );

        Assert.assertFalse( t1.equals( t2 ) );
        Assert.assertFalse( t1.hashCode() == t2.hashCode() );

        t2 = TransactionTest.getLegalTransaction();
        t2.setTargetBank( null );

        Assert.assertFalse( t1.equals( t2 ) );
        Assert.assertFalse( t1.hashCode() == t2.hashCode() );

        t2 = TransactionTest.getLegalTransaction();
        t2.setTargetExt( null );

        Assert.assertFalse( t1.equals( t2 ) );
        Assert.assertFalse( t1.hashCode() == t2.hashCode() );

        t2 = TransactionTest.getLegalTransaction();
        t2.setTargetName( null );

        Assert.assertFalse( t1.equals( t2 ) );
        Assert.assertFalse( t1.hashCode() == t2.hashCode() );

        t2 = TransactionTest.getLegalTransaction();
        t2.setType( null );

        Assert.assertFalse( t1.equals( t2 ) );
        Assert.assertFalse( t1.hashCode() == t2.hashCode() );
    }

    //---------------------------------------------------------TransactionTest--
}

/*
 *  jDTAUS Banking SPI
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
package org.jdtaus.banking.dtaus.spi.test;

import java.util.Locale;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.Transaction;
import org.jdtaus.banking.dtaus.spi.IllegalHeaderException;
import org.jdtaus.banking.dtaus.spi.IllegalTransactionException;
import org.jdtaus.core.text.Message;

/**
 * Testcase for testing instantiation of various exception classes.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public class ExceptionsTest extends TestCase
{
    //--Tests-------------------------------------------------------------------

    public void testExceptionInstantiation() throws Exception
    {
        final IllegalHeaderException h = new IllegalHeaderException();
        final IllegalTransactionException t = new IllegalTransactionException();

        final Message msg = new Message()
        {

            public Object[] getFormatArguments( final Locale locale )
            {
                return new Object[ 0 ];
            }

            public String getText( final Locale locale )
            {
                return this.getClass().getName();
            }

        };

        h.addMessage( msg );
        h.addMessage( Header.PROP_ACCOUNT, msg );
        h.addMessage( Header.PROP_BANK, msg );
        h.addMessage( Header.PROP_BANKDATA, msg );
        h.addMessage( Header.PROP_CREATEDATE, msg );
        h.addMessage( Header.PROP_CURRENCY, msg );
        h.addMessage( Header.PROP_CUSTOMER, msg );
        h.addMessage( Header.PROP_EXECUTIONDATE, msg );
        h.addMessage( Header.PROP_REFERENCE, msg );
        h.addMessage( Header.PROP_TYPE, msg );

        t.addMessage( msg );
        t.addMessage( Transaction.PROP_AMOUNT, msg );
        t.addMessage( Transaction.PROP_CURRENCY, msg );
        t.addMessage( Transaction.PROP_DESCRIPTIONS, msg );
        t.addMessage( Transaction.PROP_EXECUTIVEACCOUNT, msg );
        t.addMessage( Transaction.PROP_EXECUTIVEBANK, msg );
        t.addMessage( Transaction.PROP_EXECUTIVEEXT, msg );
        t.addMessage( Transaction.PROP_EXECUTIVENAME, msg );
        t.addMessage( Transaction.PROP_PRIMARYBANK, msg );
        t.addMessage( Transaction.PROP_REFERENCE, msg );
        t.addMessage( Transaction.PROP_TARGETACCOUNT, msg );
        t.addMessage( Transaction.PROP_TARGETBANK, msg );
        t.addMessage( Transaction.PROP_TARGETEXT, msg );
        t.addMessage( Transaction.PROP_TARGETNAME, msg );
        t.addMessage( Transaction.PROP_TYPE, msg );

        System.out.println( new IllegalHeaderException() );
        System.out.println( new IllegalTransactionException() );
        System.out.println( h );
        System.out.println( t );
    }

    public void testIllegalHeaderException() throws Exception
    {
        final IllegalHeaderException e = new IllegalHeaderException();
        Assert.assertTrue( e.getMessages( "TEST" ).length == 0 );
    }

    public void testIllegalTransactionException() throws Exception
    {
        final IllegalTransactionException e = new IllegalTransactionException();
        Assert.assertTrue( e.getMessages( "TEST" ).length == 0 );
    }

    //-------------------------------------------------------------------Tests--
}

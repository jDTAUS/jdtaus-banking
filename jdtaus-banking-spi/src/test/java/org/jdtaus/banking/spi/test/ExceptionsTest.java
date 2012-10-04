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
package org.jdtaus.banking.spi.test;

import java.util.Date;
import java.util.Locale;
import junit.framework.TestCase;
import org.jdtaus.banking.Textschluessel;
import org.jdtaus.banking.spi.IllegalTextschluesselException;
import org.jdtaus.banking.spi.UnsupportedCurrencyException;
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

        System.out.println(
            new UnsupportedCurrencyException( "EUR", new Date() ).getMessage() );

        final IllegalTextschluesselException e =
            new IllegalTextschluesselException();

        e.addMessage( msg );
        e.addMessage( Textschluessel.PROP_DEBIT, msg );
        e.addMessage( Textschluessel.PROP_EXTENSION, msg );
        e.addMessage( Textschluessel.PROP_KEY, msg );
        e.addMessage( Textschluessel.PROP_REMITTANCE, msg );
        e.addMessage( Textschluessel.PROP_SHORTDESCRIPTION, msg );
        e.addMessage( Textschluessel.PROP_VARIABLE, msg );

        System.out.println( new IllegalTextschluesselException() );
        System.out.println( e );
    }

    //-------------------------------------------------------------------Tests--
}

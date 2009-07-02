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

import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.BankleitzahlExpirationException;
import org.jdtaus.banking.BankleitzahlInfo;
import org.jdtaus.banking.IllegalTextschluesselException;
import org.jdtaus.core.text.Message;

/**
 * Testcase for testing instantiation of various exception classes.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class ExceptionsTest
{
    //--Tests-------------------------------------------------------------------

    public void testExceptionInstantiation() throws Exception
    {
        final IllegalTextschluesselException e =
            new IllegalTextschluesselException()
            {

                public Message[] getMessages()
                {
                    return new Message[ 0 ];
                }

                public Message[] getMessages( final String propertyName )
                {
                    return new Message[ 0 ];
                }

                public String[] getPropertyNames()
                {
                    return new String[ 0 ];
                }

            };

        System.out.println( e.toString() );

        final BankleitzahlInfo info1 = new BankleitzahlInfo();
        final BankleitzahlInfo info2 = new BankleitzahlInfo();
        info1.setBankCode( Bankleitzahl.valueOf( "88888888" ) );
        info2.setBankCode( Bankleitzahl.valueOf( "88888888" ) );
        System.out.println( new BankleitzahlExpirationException(
                            info1, info2 ).toString() );

    }

    //-------------------------------------------------------------------Tests--
}

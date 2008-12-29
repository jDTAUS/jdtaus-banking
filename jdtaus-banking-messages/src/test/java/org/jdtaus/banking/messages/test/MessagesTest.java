/*
 *  jDTAUS Banking Messages
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
package org.jdtaus.banking.messages.test;

import java.math.BigInteger;
import java.util.Date;
import java.util.Locale;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.BankleitzahlInfo;
import org.jdtaus.banking.Textschluessel;
import org.jdtaus.banking.dtaus.Checksum;
import org.jdtaus.banking.dtaus.LogicalFileType;
import org.jdtaus.banking.messages.AnalysesFileMessage;
import org.jdtaus.banking.messages.BankleitzahlExpirationMessage;
import org.jdtaus.banking.messages.BankleitzahlReplacementMessage;
import org.jdtaus.banking.messages.ChecksumErrorMessage;
import org.jdtaus.banking.messages.ChecksumsFileMessage;
import org.jdtaus.banking.messages.CurrencyConstraintMessage;
import org.jdtaus.banking.messages.IllegalAmountMessage;
import org.jdtaus.banking.messages.IllegalCurrencyMessage;
import org.jdtaus.banking.messages.IllegalDataMessage;
import org.jdtaus.banking.messages.IllegalDateMessage;
import org.jdtaus.banking.messages.IllegalDescriptionCountMessage;
import org.jdtaus.banking.messages.IllegalFileLengthMessage;
import org.jdtaus.banking.messages.IllegalScheduleMessage;
import org.jdtaus.banking.messages.OutdatedBankleitzahlenVerzeichnisMessage;
import org.jdtaus.banking.messages.ReadsBankleitzahlenDateiMessage;
import org.jdtaus.banking.messages.TextschluesselConstraintMessage;
import org.jdtaus.banking.messages.UnknownBankleitzahlMessage;
import org.jdtaus.banking.messages.UpdatesBankleitzahlenDateiMessage;
import org.jdtaus.core.text.Message;

/**
 * Unit tests for banking application messages.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class MessagesTest extends TestCase
{
    //--Tests-------------------------------------------------------------------

    /**
     * Tests instantiation of each core application message and for non-null
     * texts for the current default locale.
     */
    public void testMessages() throws Exception
    {
        final Checksum c1 = new Checksum();
        final Checksum c2 = new Checksum();
        c2.setTransactionCount( 1 );

        this.assertNotNull( new AnalysesFileMessage() );
        this.assertNotNull( new ChecksumErrorMessage( c1, c2, 0L ) );
        this.assertNotNull( new ChecksumsFileMessage() );
        this.assertNotNull( new CurrencyConstraintMessage( "DEM", new Date() ) );
        this.assertNotNull( new IllegalAmountMessage( new BigInteger( "10" ) ) );
        this.assertNotNull( new IllegalCurrencyMessage( "DEM", new Date() ) );
        this.assertNotNull( new IllegalDataMessage(
                            0xA1, IllegalDataMessage.TYPE_CONSTANT, 0L, "TEST" ) );

        this.assertNotNull( new IllegalDateMessage(
                            new Date(), new Date(), new Date() ) );

        this.assertNotNull( new IllegalDescriptionCountMessage( 0, 1 ) );
        this.assertNotNull( new IllegalFileLengthMessage( 1, 3 ) );
        this.assertNotNull( new IllegalScheduleMessage(
                            new Date(), new Date(), 0 ) );

        this.assertNotNull( new OutdatedBankleitzahlenVerzeichnisMessage(
                            new Date() ) );

        this.assertNotNull( new TextschluesselConstraintMessage(
                            LogicalFileType.GB, new Textschluessel() ) );

        this.assertNotNull( new UpdatesBankleitzahlenDateiMessage() );
        this.assertNotNull( new ReadsBankleitzahlenDateiMessage() );
        this.assertNotNull( new UnknownBankleitzahlMessage(
                            Bankleitzahl.valueOf( "88888888" ) ) );

        final BankleitzahlInfo testInfo = new BankleitzahlInfo();
        testInfo.setBankCode( Bankleitzahl.valueOf( "88888888" ) );

        this.assertNotNull( new BankleitzahlExpirationMessage( testInfo ) );
        this.assertNotNull( new BankleitzahlReplacementMessage( testInfo ) );
    }

    private void assertNotNull( final Message message )
    {
        Assert.assertNotNull( message.getText( Locale.getDefault() ) );
        System.out.println( message.getText( Locale.getDefault() ) );
    }

    //-------------------------------------------------------------------Tests--
}

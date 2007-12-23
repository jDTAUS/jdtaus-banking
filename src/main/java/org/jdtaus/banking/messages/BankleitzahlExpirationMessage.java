/*
 *  jDTAUS - DTAUS fileformat.
 *  Copyright (c) 2005 Christian Schulte <cs@schulte.it>
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
package org.jdtaus.banking.messages;

import java.util.Locale;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.BankleitzahlInfo;
import org.jdtaus.core.text.Message;

/**
 * Message stating that a Bankleitzahl has expired.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class BankleitzahlExpirationMessage extends Message
{
    //--Constructors------------------------------------------------------------

    /**
     * Information regarding the expired Bankleitzahl.
     * @serial
     */
    private final BankleitzahlInfo info;

    /**
     * Creates a new {@code BankleitzahlExpirationMessage} taking information
     * about the expired Bankleitzahl.
     *
     * @param info information regarding the expired Bankleitzahl.
     *
     * @throws NullPointerException if {@code info} is {@code null}.
     */
    public BankleitzahlExpirationMessage(final BankleitzahlInfo info)
    {
        if(info == null)
        {
            throw new NullPointerException("info");
        }

        this.info = info;
    }

    //------------------------------------------------------------Constructors--
    //--Message-----------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * @return Information regarding the expired Bankleitzahl.
     * <ul>
     * <li>[0]: the expired Bankleitzahl.</li>
     * </ul>
     */
    public Object[] getFormatArguments(final Locale locale)
    {
        return new Object[] {
            this.info.getBankCode().format(Bankleitzahl.LETTER_FORMAT)
        };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * (defaults to "The Bankleitzahl {0} has expired.").
     */
    public String getText(final Locale locale)
    {
        return BankleitzahlExpirationMessageBundle.
            getBankleitzahlExpirationMessage(locale).
            format(this.getFormatArguments(locale));

    }

    //-----------------------------------------------------------------Message--
}

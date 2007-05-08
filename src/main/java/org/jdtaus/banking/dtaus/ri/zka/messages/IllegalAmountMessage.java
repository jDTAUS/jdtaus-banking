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
package org.jdtaus.banking.dtaus.ri.zka.messages;

import java.math.BigInteger;
import java.util.Locale;
import org.jdtaus.core.text.Message;

/**
 * Fehler-Meldung: "Der Betrag {0,number} kann nicht in einer DTAUS Datei
 * gespeichert werden."
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class IllegalAmountMessage extends Message
{
    //--Message-----------------------------------------------------------------

    public Object[] getFormatArguments(final Locale locale)
    {
        return new Object[] { this.getAmount() };
    }

    public String getText(final Locale locale)
    {
        return IllegalAmountMessageBundle.getIllegalAmountMessage(locale).
            format(this.getFormatArguments(locale));

    }

    //-----------------------------------------------------------------Message--
    //--IllegalAmountMessage----------------------------------------------------

    /**
     * The illegal amount.
     * @serial
     */
    private BigInteger amount;

    /**
     * Creates a new {@code IllegalAmountMessage} taking an illegal amount.
     *
     * @param amount the illegal amount.
     *
     * @throws NullPointerException if {@code amount} is {@code null}.
     */
    public IllegalAmountMessage(final BigInteger amount)
    {
        super();

        if(amount == null)
        {
            throw new NullPointerException("amount");
        }

        this.amount = amount;
    }

    /**
     * Gets the illegal amount.
     *
     * @return the illegal amount.
     */
    public BigInteger getAmount()
    {
        return this.amount;
    }

    //----------------------------------------------------IllegalAmountMessage--
}

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

import java.util.Date;
import java.util.Locale;
import org.jdtaus.core.text.Message;

/**
 * Message stating that a date is invalid.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class IllegalDateMessage extends Message
{
    //--Constructors------------------------------------------------------------

    /**
     * The illegal date.
     * @serial
     */
    private Date date;

    /**
     * Creates a new {@code IllegalDateMessage} instance taking the illegal
     * date.
     *
     * @param date the illegal date.
     *
     * @throws NullPointerException if {@code date} is {@code null}.
     */
    public IllegalDateMessage(final Date date)
    {
        super();

        if(date == null)
        {
            throw new NullPointerException("date");
        }

        this.date = date;
    }

    //------------------------------------------------------------Constructors--
    //--Message-----------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * @return the illegal date.
     * <ul>
     * <li>[0]: the illegal date.</li>
     * </ul>
     */
    public Object[] getFormatArguments(final Locale locale)
    {
        return new Object[] {
            this.date
        };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * (defaults to "The date {0,date,long} is either before 1980 or after 2079.").
     */
    public String getText(final Locale locale)
    {
        return IllegalDateMessageBundle.getIllegalDateMessage(locale).
            format(this.getFormatArguments(locale));

    }

    //-----------------------------------------------------------------Message--
}

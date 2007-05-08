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

import java.util.Date;
import java.util.Locale;
import org.jdtaus.core.text.Message;

/**
 * Fehler-Meldung: "Das Datum {0,date,long} liegt entweder vor dem Jahr 1980
 * oder hinter dem Jahr 2079."
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class IllegalDateMessage extends Message
{
    //--Message-----------------------------------------------------------------

    public Object[] getFormatArguments(final Locale locale)
    {
        return new Object[] { this.getDate() };
    }

    public String getText(final Locale locale)
    {
        return IllegalDateMessageBundle.getIllegalDateMessage(locale).
            format(this.getFormatArguments(locale));

    }

    //-----------------------------------------------------------------Message--
    //--IllegalDateMessage------------------------------------------------------

    /**
     * The illegal date.
     * @serial
     */
    private Date date;

    /**
     * Creates a new {@code IllegalDateMessage} instance taking the illegal
     * date.
     *
     * @param date the illegal date for the message.
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

    /**
     * Gets the illegal date of the message.
     *
     * @return the illegal date of the message.
     */
    public Date getDate()
    {
        return this.date;
    }

    //------------------------------------------------------IllegalDateMessage--
}

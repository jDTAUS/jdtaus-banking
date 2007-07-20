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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import org.jdtaus.core.text.Message;

/**
 * Message stating that a given schedule is invalid.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class IllegalScheduleMessage extends Message
{
    //--Constructors------------------------------------------------------------

    /**
     * Create date of the illegal schedule.
     * @serial
     */
    private Date createDate;

    /**
     * Execution date of the illegal schedule.
     * @serial
     */
    private Date executionDate;

    /**
     * Creates a new {@code IllegalScheduleMessage} instance taking the
     * absolute position of the first illegal date, the create date and the
     * date of execution.
     *
     * @param createDate the create date of the schedule.
     * @param executionDate the execution date of the schedule.
     *
     * @throws NullPointerException if {@code createDate} is {@code null}.
     */
    public IllegalScheduleMessage(final Date createDate,
        final Date executionDate)
    {
        super();

        if(createDate == null)
        {
            throw new NullPointerException("createDate");
        }

        this.createDate = createDate;
        this.executionDate = executionDate;
    }

    //------------------------------------------------------------Constructors--
    //--Message-----------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * @return the create date and the date of execution.
     * <ul>
     * <li>[0]: the create date of the schedule.</li>
     * <li>[1]: the date of execution of the invalid schedule.</li>
     * </ul>
     */
    public Object[] getFormatArguments(final Locale locale)
    {
        return new Object[] {
            this.createDate, this.executionDate
        };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * (defaults to "The executiondate {2, date, long} is before create date {1, date, long} or more than 15 days thereafter.").
     */
    public String getText(final Locale locale)
    {
        return IllegalScheduleMessageBundle.
            getIllegalScheduleMessage(locale).
            format(this.getFormatArguments(locale));

    }

    //-----------------------------------------------------------------Message--
}

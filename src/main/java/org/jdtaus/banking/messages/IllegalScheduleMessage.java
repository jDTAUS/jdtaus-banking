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
package org.jdtaus.banking.messages;

import java.util.Date;
import java.util.Locale;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.text.Message;

/**
 * Message stating that a given schedule is invalid.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class IllegalScheduleMessage extends Message
{
    //--Contstants--------------------------------------------------------------

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = -8689097743116031670L;

    //---------------------------------------------------------------Constants--
    //--Message-----------------------------------------------------------------

    /**
     * Create date of the illegal schedule.
     * @serial
     */
    private final Date createDate;

    /**
     * Execution date of the illegal schedule.
     * @serial
     */
    private final Date executionDate;

    /**
     * Maximum number of days allowed between {@code createDate} and
     * {@code executionDate}.
     * @serial
     */
    private final int maxDays;

    /**
     * Creates a new {@code IllegalScheduleMessage} instance taking the create
     * date and the date of execution not forming a valid schedule for a
     * maximum number of days between them.
     *
     * @param createDate the create date of the schedule.
     * @param executionDate the execution date of the schedule.
     * @param maxDays the maximum number of days allowed between
     * {@code createDate} and {@code executionDate}.
     *
     * @throws NullPointerException if {@code createDate} is {@code null}.
     * @throws IllegalArgumentException if {@code maxDays} is negative.
     */
    public IllegalScheduleMessage( final Date createDate,
                                    final Date executionDate,
                                    final int maxDays )
    {
        super();

        if ( createDate == null )
        {
            throw new NullPointerException( "createDate" );
        }
        if ( maxDays < 0 )
        {
            throw new IllegalArgumentException( Integer.toString( maxDays ) );
        }

        this.createDate = createDate;
        this.executionDate = executionDate;
        this.maxDays = maxDays;
    }

    /**
     * {@inheritDoc}
     *
     * @return the create date and the date of execution.
     * <ul>
     * <li>[0]: the create date of the schedule.</li>
     * <li>[1]: the date of execution of the invalid schedule.</li>
     * <li>[2]: the maximum number of days allowed between create date and
     * execution date.</li>
     * </ul>
     */
    public Object[] getFormatArguments( final Locale locale )
    {
        return new Object[] {
            this.createDate, this.executionDate, new Integer( this.maxDays )
        };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * <blockquote><pre>
     * The executiondate {1,date,long} is before create date {0,date,long} or more than {2,number} days thereafter.
     * </pre></blockquote>
     */
    public String getText( final Locale locale )
    {
        return this.getIllegalScheduleMessage( locale, this.createDate,
                                               this.executionDate,
                                               new Integer( this.maxDays ) );

    }

    //-----------------------------------------------------------------Message--
    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>illegalSchedule</code>.
     * <blockquote><pre>Das Ausführungsdatum {1,date,long} liegt vor dem Dateierstellungsdatum {0,date,long} oder mehr als {2,number} Kalendertage dahinter.</pre></blockquote>
     * <blockquote><pre>The executiondate {1,date,long} is before create date {0,date,long} or more than {2,number} days thereafter.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param createDate format argument.
     * @param executionDate format argument.
     * @param maxScheduleDays format argument.
     *
     * @return the text of message <code>illegalSchedule</code>.
     */
    private String getIllegalScheduleMessage( final Locale locale,
            final java.util.Date createDate,
            final java.util.Date executionDate,
            final java.lang.Number maxScheduleDays )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "illegalSchedule", locale,
                new Object[]
                {
                    createDate,
                    executionDate,
                    maxScheduleDays
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

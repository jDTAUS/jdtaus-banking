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
    //--Contstants--------------------------------------------------------------

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = 4086935652662010927L;

    //---------------------------------------------------------------Constants--
    //--Constructors------------------------------------------------------------

    /**
     * The illegal date.
     * @serial
     */
    private final Date date;

    /**
     * The starting date of the range for valid dates.
     * @serial
     */
    private final Date dateRangeStart;

    /**
     * The ending date of the range for valid dates.
     * @serial
     */
    private final Date dateRangeEnd;

    /**
     * Creates a new {@code IllegalDateMessage} instance taking the illegal
     * date and the range of dates for which a date is considered legal.
     *
     * @param date the illegal date.
     * @param dateRangeStart the starting date of the range for valid dates.
     * @param dateRangeEnd the ending date of the range for valid dates.
     *
     * @throws NullPointerException if either {@code date},
     * {@code dateRangeStart} or {@code dateRangeEnd} is {@code null}.
     */
    public IllegalDateMessage( final Date date, final Date dateRangeStart,
                                final Date dateRangeEnd )
    {
        super();

        if ( date == null )
        {
            throw new NullPointerException( "date" );
        }
        if ( dateRangeStart == null )
        {
            throw new NullPointerException( "dateRangeStart" );
        }
        if ( dateRangeEnd == null )
        {
            throw new NullPointerException( "dateRangeEnd" );
        }

        this.date = date;
        this.dateRangeStart = dateRangeStart;
        this.dateRangeEnd = dateRangeEnd;
    }

    //------------------------------------------------------------Constructors--
    //--Message-----------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * @return the illegal date.
     * <ul>
     * <li>[0]: the illegal date.</li>
     * <li>[1]: the starting date of the range for valid dates.</li>
     * <li>[2]: the ending date of the range for valid dates.</li>
     * </ul>
     */
    public Object[] getFormatArguments( final Locale locale )
    {
        return new Object[] {
            this.date, this.dateRangeStart, this.dateRangeEnd
        };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * <blockquote><pre>
     * The date {0,date,long} is either before {1,date,long} or after {2,date,long}.
     * </pre></blockquote>
     */
    public String getText( final Locale locale )
    {
        return IllegalDateMessageBundle.getInstance().
            getIllegalDateMessage( locale ).
            format( this.getFormatArguments( locale ) );

    }

    //-----------------------------------------------------------------Message--
}
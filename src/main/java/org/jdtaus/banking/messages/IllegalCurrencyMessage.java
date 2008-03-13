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
 * Message stating that a currency is invalid at a given date.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class IllegalCurrencyMessage extends Message
{
    //--Contstants--------------------------------------------------------------

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = -2259517733162759316L;

    //---------------------------------------------------------------Constants--
    //--Constructors------------------------------------------------------------

    /**
     * The ISO currency code of the illegal currency.
     * @serial
     */
    private final String currencyCode;

    /**
     * The date at which {@code currenycCode} is illegal.
     * @serial
     */
    private final Date date;

    /**
     * Creates a new {@code IllegalCurrencyMessage} instance taking the ISO
     * currency code of the illegal currency at a given date.
     *
     * @param currencyCode the ISO currency code of the illegal currency.
     * @param date the date at which {@code currencyCode} is illegal.
     *
     * @throws NullPointerException if either {@code currencyCode} or
     * {@code date} is {@code null}.
     */
    public IllegalCurrencyMessage( final String currencyCode, final Date date )
    {
        super();

        if ( currencyCode == null )
        {
            throw new NullPointerException( "currencyCode" );
        }
        if ( date == null )
        {
            throw new NullPointerException( "date" );
        }

        this.currencyCode = currencyCode;
        this.date = date;
    }

    //------------------------------------------------------------Constructors--
    //--Message-----------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * @return The ISO currency code of the illegal currency with corresponding
     * date.
     * <ul>
     * <li>[0]: ISO currency code.</li>
     * <li>[1]: date.</li>
     * </ul>
     */
    public Object[] getFormatArguments( final Locale locale )
    {
        return new Object[] { this.currencyCode, this.date };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * <blockquote><pre>
     * The currency {0} is illegal at {1,date,long}.
     * </pre></blockquote>
     */
    public String getText( final Locale locale )
    {
        return IllegalCurrencyMessageBundle.getInstance().
            getIllegalCurrencyMessage( locale ).
            format( this.getFormatArguments( locale ) );

    }

    //-----------------------------------------------------------------Message--
}

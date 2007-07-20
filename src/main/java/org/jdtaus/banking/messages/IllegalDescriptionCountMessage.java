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
import org.jdtaus.core.text.Message;

/**
 * Message stating that in illegal amount of descriptions is used.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class IllegalDescriptionCountMessage extends Message
{
    //--Constructors------------------------------------------------------------

    /**
     * Maximum number of supported descriptions.
     * @serial
     */
    private int maxDescriptions;

    /**
     * Requested number of descriptions.
     * @serial
     */
    private int requestedDescriptions;

    /**
     * Creates a new {@code IllegalDescriptionCountMessage} instance taking
     * the maximum number of supported descriptions and the number of
     * requested descriptions.
     *
     * @param maxDescriptions the maximum number of supported descriptions.
     * @param requestedDescriptions the requested number of descriptions.
     */
    public IllegalDescriptionCountMessage(final int maxDescriptions,
        final int requestedDescriptions)
    {
        super();

        this.maxDescriptions = maxDescriptions;
        this.requestedDescriptions = requestedDescriptions;
    }

    //------------------------------------------------------------Constructors--
    //--Message-----------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * @return the maximum number of supported descriptions and the requested
     * number of descriptions.
     * <ul>
     * <li>[0]: the maximum number of supported descriptions.</li>
     * <li>[1]: the requested number of descriptions.</li>
     * </ul>
     */
    public Object[] getFormatArguments(final Locale locale)
    {
        return new Object[] {
            new Integer(this.maxDescriptions),
            new Integer(this.requestedDescriptions)
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
        return IllegalDescriptionCountMessageBundle.
            getIllegalDescriptionCountMessage(locale).format(
            this.getFormatArguments(locale));

    }

    //-----------------------------------------------------------------Message--
}

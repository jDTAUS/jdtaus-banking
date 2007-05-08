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

import java.util.Locale;
import org.jdtaus.core.text.Message;

/**
 * Fehler-Meldung: "Es können nicht mehr als {0,number,##}
 * Verwendungszweckzeilen in einer DTAUS Datei gespeichert werden."
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class IllegalDescriptionCountMessage extends Message
{
    //--Message-----------------------------------------------------------------

    public Object[] getFormatArguments(final Locale locale)
    {
        return new Object[] { this.getMaxDescriptions() };
    }

    public String getText(final Locale locale)
    {
        return IllegalDescriptionCountMessageBundle.
            getIllegalDescriptionCountMessage(locale).format(
            this.getFormatArguments(locale));

    }

    //-----------------------------------------------------------------Message--
    //--IllegalDescriptionCountMessage------------------------------------------

    /**
     * Maximum number of supported descriptions.
     * @serial
     */
    private Integer maxDescriptions;

    /**
     * Creates a new {@code IllegalDescriptionCountMessage} instance taking
     * the maximum number of supported descriptions.
     *
     * @param maxDescriptions the maximum number of supported descriptions.
     */
    public IllegalDescriptionCountMessage(final int maxDescriptions)
    {
        super();

        this.maxDescriptions = new Integer(maxDescriptions);
    }

    /**
     * Gets the maximum number of supported descriptions.
     *
     * @return the maximum number of supported descriptions.
     */
    public Integer getMaxDescriptions()
    {
        return this.maxDescriptions;
    }

    //------------------------------------------IllegalDescriptionCountMessage--
}

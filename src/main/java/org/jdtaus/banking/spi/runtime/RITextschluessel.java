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
package org.jdtaus.banking.spi.runtime;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.jdtaus.banking.Textschluessel;

/**
 * Reference {@code Textschluessel} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class RITextschluessel extends Textschluessel
{
    //--Textschluessel----------------------------------------------------------

    public String getShortDescription(Locale locale)
    {
        if(locale == null)
        {
            locale = Locale.getDefault();
        }

        final MessageFormat fmt =
            new MessageFormat((String) this.shortDescriptions.
            get(locale.getLanguage()));

        return fmt.format(new Object[] {
            new Integer(this.getKey()),
            new Integer(this.getExtension())
        });
    }

    //----------------------------------------------------------Textschluessel--
    //--RITextschluessel--------------------------------------------------------

    /**
     * Maps language codes to short descriptions.
     * @serial
     */
    private Map shortDescriptions = new HashMap(10);

    /**
     * Updates property {@code shortDescription} for a given locale.
     *
     * @param locale the locale to update the short description for or
     * {@code null} for {@code Locale.getDefault()}.
     * @param shortDescription the new value for property
     * {@code shortDescription} for {@code locale}.
     *
     * @return the value previously held by the instance for {@code locale} or
     * {@code null} if the instance previously held either no value or a
     * {@code null} value for {@code locale}.
     *
     * @throws NullPointerException if {@code shortDescription} is {@code null}.
     */
    public String updateShortDescription(Locale locale,
        final String shortDescription)
    {
        if(shortDescription == null)
        {
            throw new NullPointerException("shortDescription");
        }

        if(locale == null)
        {
            locale = Locale.getDefault();
        }

        return (String) this.shortDescriptions.
            put(locale.getLanguage(), shortDescription);

    }

    //--------------------------------------------------------RITextschluessel--

}

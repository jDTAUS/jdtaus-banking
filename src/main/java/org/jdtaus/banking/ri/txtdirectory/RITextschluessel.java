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
package org.jdtaus.banking.ri.txtdirectory;

import java.util.Locale;
import org.jdtaus.banking.Textschluessel;

/**
 * Reference {@code Textschluessel} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 *
 * @deprecated Replaced by {@link org.jdtaus.banking.Textschluessel}
 */
public final class RITextschluessel extends Textschluessel
{
    //--RITextschluessel--------------------------------------------------------

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
     *
     * @deprecated Replaced by
     * {@link Textschluessel#setShortDescription(Locale,String)}
     */
    public String updateShortDescription( Locale locale,
                                           final String shortDescription )
    {
        return super.setShortDescription( locale, shortDescription );
    }

    //--------------------------------------------------------RITextschluessel--
}

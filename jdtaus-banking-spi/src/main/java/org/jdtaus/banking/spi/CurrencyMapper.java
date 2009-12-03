/*
 *  jDTAUS Banking SPI
 *  Copyright (c) 2005 Christian Schulte
 *
 *  Christian Schulte, Haldener Strasse 72, 58095 Hagen, Germany
 *  <schulte2005@users.sourceforge.net> (+49 2331 3543887)
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
package org.jdtaus.banking.spi;

import java.util.Currency;
import java.util.Date;
import org.jdtaus.banking.CurrencyDirectory;

/**
 * Maps {@code Currency} instances to various codes.
 * <p>jDTAUS Banking SPI {@code CurrencyMapper} specification to be used by implementations to map {@code Currency}
 * instances to codes and vice versa.</p>
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 *
 * @see org.jdtaus.core.container.Container
 */
public interface CurrencyMapper extends CurrencyDirectory
{

    /**
     * Gets the DTAUS code for a currency at a given date.
     *
     * @param currency The currency to return the corresponding DTAUS code for.
     * @param date The date to return the DTAUS code for.
     *
     * @return The DTAUS code for {@code currency} at {@code date}.
     *
     * @throws NullPointerException if either {@code currency} or {@code date} is {@code null}.
     * @throws UnsupportedCurrencyException if {@code currency} is not known to the directory at {@code date}.
     */
    char getDtausCode( Currency currency, Date date );

    /**
     * Gets the currency for a DTAUS code at a given date.
     *
     * @param code The DTAUS code to return the corresponding currency for.
     * @param date The date to return the currency for.
     *
     * @return The currency corresponding to {@code code} at {@code date} or {@code null} if no currency matching
     * {@code code} is known to the directory at {@code date}.
     *
     * @throws NullPointerException if {@code date} is {@code null}.
     */
    Currency getDtausCurrency( char code, Date date );

}

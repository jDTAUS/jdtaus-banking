/*
 *  jDTAUS Banking API
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
package org.jdtaus.banking;

import java.util.Currency;
import java.util.Date;

/**
 * Directory holding currency information.
 * <p>Example: Getting the jDTAUS Banking SPI implementation.<br/><pre>
 * CurrencyDirectory directory =
 *     (CurrencyDirectory) ContainerFactory.getContainer().
 *     getObject( CurrencyDirectory.class );
 * </pre></p>
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 *
 * @see org.jdtaus.core.container.Container
 */
public interface CurrencyDirectory
{

    /**
     * Gets all DTAUS currencies known to the directory for a given date.
     *
     * @param date The date to return known currencies for.
     *
     * @return All DTAUS currencies known to the directory at {@code date} or an empty array if the directory does not
     * hold currencies at {@code date}.
     *
     * @throws NullPointerException if {@code date} is {@code null}.
     */
    Currency[] getDtausCurrencies( Date date );

}

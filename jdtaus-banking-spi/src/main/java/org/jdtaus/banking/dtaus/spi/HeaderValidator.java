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
package org.jdtaus.banking.dtaus.spi;

import java.io.IOException;
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.LogicalFile;

/**
 * Validates {@code Header} instances.
 * <p>jDTAUS Banking SPI {@code HeaderValidator} specification to be used by
 * implementations to validate {@code Header} instances to hold valid
 * values.</p>
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public interface HeaderValidator
{
    //--HeaderValidator---------------------------------------------------------

    /**
     * Checks a given {@code Header} instance to hold valid values for
     * creating a new {@code LogicalFile}.
     *
     * @param header the instance to check.
     * @param result the validation result to be used or {@code null}.
     *
     * @return the validation result passed in as {@code result}
     * (maybe {@code null} if the implementation did not detect illegal values).
     *
     * @throws NullPointerException if {@code header} is {@code null}.
     */
    IllegalHeaderException assertValidHeader(
        Header header, IllegalHeaderException result );

    /**
     * Checks a given {@code Header} instance to hold valid values for
     * updating a given {@code LogicalFile} with.
     *
     * @param lFile the logical file to update with {@code header}.
     * @param header the instance to check.
     * @param counter a currency counter reflecting the state of {@code lFile}.
     * @param result the validation result to be used or {@code null}.
     *
     * @return the validation result passed in as {@code result}
     * (maybe {@code null} if the implementation did not detect illegal values).
     *
     * @throws NullPointerException if either {@code lFile}, {@code header} or
     * {@code counter} is {@code null}.
     * @throws IOException if reading fails.
     */
    IllegalHeaderException assertValidHeader(
        LogicalFile lFile, Header header, CurrencyCounter counter,
        IllegalHeaderException result ) throws IOException;

    //---------------------------------------------------------HeaderValidator--
}

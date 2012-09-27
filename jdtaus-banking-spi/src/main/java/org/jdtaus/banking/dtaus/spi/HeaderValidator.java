/*
 *  jDTAUS Banking SPI
 *  Copyright (C) 2005 Christian Schulte
 *  <cs@schulte.it>
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
 * <p>jDTAUS Banking SPI {@code HeaderValidator} specification to be used by implementations to validate {@code Header}
 * instances to hold valid values.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public interface HeaderValidator
{

    /**
     * Checks a given {@code Header} instance to hold valid values for creating a new {@code LogicalFile}.
     *
     * @param header The instance to check.
     * @param result The validation result to be used or {@code null}.
     *
     * @return The validation result passed in as {@code result} (maybe {@code null} if the implementation did not
     * detect illegal values).
     *
     * @throws NullPointerException if {@code header} is {@code null}.
     */
    IllegalHeaderException assertValidHeader( Header header, IllegalHeaderException result );

    /**
     * Checks a given {@code Header} instance to hold valid values for updating a given {@code LogicalFile} with.
     *
     * @param lFile The logical file to update with {@code header}.
     * @param header The instance to check.
     * @param counter A currency counter reflecting the state of {@code lFile}.
     * @param result The validation result to be used or {@code null}.
     *
     * @return The validation result passed in as {@code result} (maybe {@code null} if the implementation did not
     * detect illegal values).
     *
     * @throws NullPointerException if either {@code lFile}, {@code header} or {@code counter} is {@code null}.
     * @throws IOException if reading fails.
     */
    IllegalHeaderException assertValidHeader( LogicalFile lFile, Header header, CurrencyCounter counter,
                                              IllegalHeaderException result ) throws IOException;

}

/*
 *  jDTAUS Banking SPI
 *  Copyright (C) 2005 Christian Schulte
 *  <schulte2005@users.sourceforge.net>
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
import org.jdtaus.banking.dtaus.LogicalFile;
import org.jdtaus.banking.dtaus.Transaction;

/**
 * Validates {@code Transaction} instances.
 * <p>jDTAUS Banking SPI {@code TransactionValidator} specification to be used by implementations to validate
 * {@code Transaction} instances to hold valid values.</p>
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $JDTAUS$
 */
public interface TransactionValidator
{

    /**
     * Checks a given {@code Transaction} instance to hold valid values for use with a given {@code LogicalFile}.
     *
     * @param lFile The logical file holding {@code transaction}.
     * @param transaction The instance to check.
     * @param result The validation result to be used or {@code null}.
     *
     * @return The validation result passed in as {@code result} (maybe {@code null} if the implementation did not
     * detect illegal values).
     *
     * @throws NullPointerException if either {@code lFile} or {@code transaction} is {@code null}.
     * @throws IOException if reading fails.
     */
    IllegalTransactionException assertValidTransaction( LogicalFile lFile, Transaction transaction,
                                                        IllegalTransactionException result ) throws IOException;

}

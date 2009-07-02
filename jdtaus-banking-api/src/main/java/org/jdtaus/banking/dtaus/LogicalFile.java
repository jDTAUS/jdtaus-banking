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
package org.jdtaus.banking.dtaus;

import java.io.IOException;

/**
 * Logical DTAUS file (Inlandszahlungsverkehr).
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public interface LogicalFile
{
    //--LogicalFile-------------------------------------------------------------

    /**
     * Gets the A record of the logical file.
     *
     * @return the A record of the logical file.
     *
     * @throws IOException if reading fails.
     */
    Header getHeader() throws IOException;

    /**
     * Updates the A record of the logical file.
     *
     * @param header new A record for the logical file.
     *
     * @return the previous A record of the logical file.
     *
     * @throws NullPointerException if {@code header} is {@code null}.
     * @throws IllegalHeaderException if {@code header} holds illegal values.
     * @throws IOException if reading or writing fails.
     */
    Header setHeader( Header header ) throws IOException;

    /**
     * Gets the E record of the logical file.
     *
     * @return the E record of the logical file.
     *
     * @throws IOException if reading fails.
     */
    Checksum getChecksum() throws IOException;

    /**
     * Adds a C record to the logical file.
     *
     * @param transaction the transaction to add to the logical file.
     *
     * @throws IndexOutOfBoundsException if no more transactions can be added
     * to the logical file.
     * @throws NullPointerException if {@code transaction} is {@code null}.
     * @throws IllegalTransactionException if {@code transaction} holds
     * illegal values.
     * @throws IOException if writing fails.
     *
     * @deprecated This method got replaced by
     * {@link #addTransaction(Transaction)} which is capable of returning the
     * index of the transaction in the file.
     */
     void createTransaction( Transaction transaction ) throws IOException;

    /**
     * Adds a C record to the logical file.
     *
     * @param transaction the transaction to add to the logical file.
     *
     * @return the index of the transaction in the logical file.
     *
     * @throws IndexOutOfBoundsException if no more transactions can be added
     * to the logical file.
     * @throws NullPointerException if {@code transaction} is {@code null}.
     * @throws IllegalTransactionException if {@code transaction} holds
     * illegal values.
     * @throws IOException if writing fails.
     */
    int addTransaction( Transaction transaction ) throws IOException;

    /**
     * Gets a C record for an index.
     *
     * @param index the index of the transaction to return.
     *
     * @return the C record at {@code index}.
     *
     * @throws IndexOutOfBoundsException if the logical file holds transactions
     * and {@code index} is either negative or greater or equal to the number
     * of transactions stored in the file.
     * @throws IOException if reading fails.
     */
    Transaction getTransaction( int index ) throws IOException;

    /**
     * Updates a C record at a given index.
     *
     * @param index the index of the transaction to update.
     * @param transaction the transaction to overwrite the transaction at
     * {@code index} with.
     *
     * @return the transaction previously stored at {@code index}.
     *
     * @throws IndexOutOfBoundsException if the logical file holds transactions
     * and {@code index} is either negative or greater or equal to the number
     * of transactions stored in the file.
     * @throws NullPointerException if {@code transaction} is {@code null}.
     * @throws IllegalTransactionException if {@code transaction} holds illegal
     * values.
     * @throws IOException if reading or writing fails.
     */
    Transaction setTransaction( int index, Transaction transaction )
        throws IOException;

    /**
     * Removes a C record at a given index.
     *
     * @param index the index of the transaction to remove.
     *
     * @return the removed transaction.
     *
     * @throws IndexOutOfBoundsException if the logical file holds transactions
     * and {@code index} is either negative or greater or equal to the number
     * of transactions stored in the file.
     * @throws IOException if reading or writing fails.
     */
    Transaction removeTransaction( int index ) throws IOException;

    //-------------------------------------------------------------LogicalFile--
}

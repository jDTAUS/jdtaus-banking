/*
 *  jDTAUS Banking API
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
package org.jdtaus.banking.dtaus;

import java.io.IOException;

/**
 * Physical DTAUS file (Inlandszahlungsverkehr).
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public interface PhysicalFile
{

    /**
     * Gets the number of logical files stored in the physical file.
     *
     * @return The number of logical files stored in the physical file.
     *
     * @throws IOException if reading fails.
     *
     * @deprecated This method got renamed to {@link #getLogicalFileCount()}.
     */
     int count() throws IOException;

    /**
     * Gets the number of logical files stored in the physical file.
     *
     * @return Number of logical files stored in the physical file.
     *
     * @throws IOException if reading fails.
     */
    int getLogicalFileCount() throws IOException;

    /**
     * Adds a logical file to the physical file.
     *
     * @param header The A record of the new logical file.
     *
     * @return The added logical file.
     *
     * @throws NullPointerException if {@code header} is {@code null}.
     * @throws IllegalHeaderException if {@code header} holds illegal values.
     * @throws IOException if reading or writing fails.
     *
     * @deprecated This method got renamed to {@link #addLogicalFile(Header)}.
     */
     LogicalFile add( Header header ) throws IOException;

    /**
     * Adds a logical file to the physical file.
     *
     * @param header The A record of the new logical file.
     *
     * @return The added logical file.
     *
     * @throws NullPointerException if {@code header} is {@code null}.
     * @throws IllegalHeaderException if {@code header} holds illegal values.
     * @throws IOException if reading or writing fails.
     */
    LogicalFile addLogicalFile( Header header ) throws IOException;

    /**
     * Gets a logical file for an index.
     *
     * @param index The index of the logical file to return.
     *
     * @return The logical file at {@code index}.
     *
     * @throws IndexOutOfBoundsException if {@code index} is either negative, or greater or equal to {@code count()}.
     * @throws IOException if reading fails.
     *
     * @deprecated This method got renamed to {@link #getLogicalFile(int)}.
     */
     LogicalFile get( int index ) throws IOException;

    /**
     * Gets a logical file for an index.
     *
     * @param index The index of the logical file to return.
     *
     * @return The logical file at {@code index}.
     *
     * @throws IndexOutOfBoundsException if {@code index} is either negative, or greater or equal to {@code count()}.
     * @throws IOException if reading fails.
     */
    LogicalFile getLogicalFile( int index ) throws IOException;

    /**
     * Removes a logical file from the physical file.
     *
     * @param index The index of the logical file to remove.
     *
     * @throws IndexOutOfBoundsException if {@code index} is either negative, or greater or equal to {@code count()}.
     * @throws IOException if reading or writing fails.
     *
     * @deprecated This method got renamed to {@link #removeLogicalFile(int)}.
     */
     void remove( int index ) throws IOException;

    /**
     * Removes a logical file from the physical file.
     *
     * @param index The index of the logical file to remove.
     *
     * @throws IndexOutOfBoundsException if {@code index} is either negative, or greater or equal to {@code count()}.
     * @throws IOException if reading or writing fails.
     */
    void removeLogicalFile( int index ) throws IOException;

    /**
     * Commits any pending changes.
     * <p><b>Note:</b><br/>
     * This method should be called once after finishing work with an instance. Implementations may close any open files
     * when calling this method so that no more operations will be possible after the method returns. Therefore state of
     * an instance is undefined after calling this method. Also note that not calling this method may lead to leaking
     * open file descriptors in the system.</p>
     *
     * @throws IOException if reading or writing fails.
     */
    void commit() throws IOException;

}

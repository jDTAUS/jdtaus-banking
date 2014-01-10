/*
 *  jDTAUS Banking RI Bankleitzahlenverzeichnis
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
package org.jdtaus.banking.ri.blzdirectory;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

/**
 * Bankfile provider interface.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 *
 * @see BankfileBankleitzahlenVerzeichnis
 */
public interface BankfileProvider
{

    /**
     * Gets the timestamp this provider was last modified.
     *
     * @return The timestamp this provider was last modified.
     *
     * @throws IOException if getting the last modification timestamp fails.
     */
    long getLastModifiedMillis() throws IOException;

    /**
     * Gets the number of provided bankfile resources.
     *
     * @return The number of provided bankfile resources.
     *
     * @throws IOException if getting the number of provided bankfile resources fails.
     */
    int getBankfileCount() throws IOException;

    /**
     * Gets a bankfile resource.
     *
     * @param index The index of the bankfile resource to get.
     *
     * @return The bankfile resource at {@code index}.
     *
     * @throws IndexOutOfBoundsException if {@code index} is negative or greater or equal to the value returned by
     * method {@code getBankfileCount()}.
     * @throws IOException if getting the bankfile resource fails.
     */
    URL getBankfile( int index ) throws IOException;

    /**
     * Gets the date of validity of a bankfile resource.
     *
     * @param index The index of the bankfile resource to get the date of validity of.
     *
     * @return The date of validity of the bankfile resource {@code index}.
     *
     * @throws IndexOutOfBoundsException if {@code index} is negative or greater or equal to the value returned by
     * method {@code getBankfileCount()}.
     * @throws IOException if getting the date of validity fails.
     */
    Date getDateOfValidity( int index ) throws IOException;

    /**
     * Gets the date of expiration of a bankfile resource.
     *
     * @param index The index of the bankfile resource to get the date of expiration of.
     *
     * @return The date of expiration of the bankfile resource {@code index}.
     *
     * @throws IndexOutOfBoundsException if {@code index} is negative or greater or equal to the value returned by
     * method {@code getBankfileCount()}.
     * @throws IOException if getting the date of expiration fails.
     */
    Date getDateOfExpiration( int index ) throws IOException;

    /**
     * Gets the format of the bankfile resource.
     *
     * @param index The index of the bankfile resource to get the format of.
     *
     * @return The format of the bankfile resource.
     *
     * @throws IOException if getting the format of the bankfile resource fails.
     *
     * @since 1.15
     * @see org.jdtaus.banking.util.BankleitzahlenDatei#JUNE_2006_FORMAT
     * @see org.jdtaus.banking.util.BankleitzahlenDatei#JUNE_2013_FORMAT
     */
    int getFormat( int index ) throws IOException;

}

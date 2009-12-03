/*
 *  jDTAUS Banking RI Bankleitzahlenverzeichnis
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
package org.jdtaus.banking.ri.blzdirectory;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

/**
 * Bankfile provider interface.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
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

}

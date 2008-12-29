/*
 *  jDTAUS Banking API
 *  Copyright (c) 2005 Christian Schulte
 *
 *  Christian Schulte, Haldener Strasse 72, 58095 Hagen, Germany
 *  <cs@jdtaus.org> (+49 2331 3543887)
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

import java.util.Date;

/**
 * Public directory of german bank codes.
 * <p>For further information see the
 * <a href="../../../doc-files/Bankleitzahlen%20Richtlinie%20-%20Stand%208.%20September%202008.pdf">
 * Bankleitzahlen Richtlinie</a>. An updated version of the document may be
 * found at <a href="http://www.bundesbank.de/index.en.php">Deutsche Bundesbank</a>.</p>
 * <p>Example: Getting the jDTAUS Banking SPI implementation.<br/><pre>
 * BankleitzahlenVerzeichnis directory =
 *     (BankleitzahlenVerzeichnis) ContainerFactory.getContainer().
 *     getObject( BankleitzahlenVerzeichnis.class.getName() );
 * </pre></p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 *
 * @see org.jdtaus.core.container.Container
 */
public interface BankleitzahlenVerzeichnis
{
    //--BankleitzahlenVerzeichnis-----------------------------------------------

    /**
     * Gets the date of expiration of the data.
     *
     * @return the date of expiration of the data.
     */
    Date getDateOfExpiration();

    /**
     * Gets the record of the head office for a given Bankleitzahl.
     *
     * @param bankCode a Bankleitzahl to return the record of the corresponding
     * head office for.
     *
     * @return the head office of the bank identified by {@code bankCode} or
     * {@code null} if no head office exists for {@code bankCode}.
     *
     * @throws NullPointerException if {@code bankCode} is {@code null}.
     * @throws BankleitzahlExpirationException if {@code bankCode} has expired.
     */
    BankleitzahlInfo getHeadOffice( Bankleitzahl bankCode )
        throws BankleitzahlExpirationException;

    /**
     * Gets the records of the branch offices for a given Bankleitzahl.
     *
     * @param bankCode the Bankleitzahl to return the records of corresponding
     * branch offices for.
     *
     * @return the branch offices of the bank identified by {@code bankCode} or
     * an empty array if the directory does not hold corresponding records for
     * {@code bankCode}.
     *
     * @throws NullPointerException if {@code bankCode} is {@code null}.
     * @throws BankleitzahlExpirationException if {@code bankCode} has expired.
     */
    BankleitzahlInfo[] getBranchOffices( Bankleitzahl bankCode )
        throws BankleitzahlExpirationException;

    /**
     * Searches the directory for records matching the given criteria.
     *
     * @param name text to select records whose property {@code name} matches
     * the given text; {@code null} to ignore property {@code name} in the
     * search.
     * @param postalCode text to select records whose property
     * {@code postalCode} matches the given text; {@code null} to ignore
     * property {@code postalCode} in the search.
     * @param city text to select records whose property {@code city} matches
     * the given text; {@code null} to ignore property {@code city} in the
     * search.
     * @param branchOffices {@code true} to return records for branch offices;
     * {@code false} to return records for head offices.
     *
     * @return all records matching the given criteria.
     *
     * @throws IllegalArgumentException if {@code name}, {@code postalCode} or
     * {@code city} contains data which cannot be used for searching the
     * directory.
     */
    BankleitzahlInfo[] search( String name, String postalCode, String city,
        boolean branchOffices );

    //-----------------------------------------------BankleitzahlenVerzeichnis--
}

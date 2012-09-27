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
package org.jdtaus.banking;

import java.util.Date;

/**
 * Directory of german transaction types.
 * <p>Example: Getting the jDTAUS Banking SPI implementation.<br/><pre>
 * TextschluesselVerzeichnis directory =
 *     (TextschluesselVerzeichnis) ContainerFactory.getContainer().
 *     getObject( TextschluesselVerzeichnis.class );
 * </pre></p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 *
 * @see org.jdtaus.core.container.Container
 */
public interface TextschluesselVerzeichnis
{

    /**
     * Gets all Textschlüssel instances of the directory.
     *
     * @return All Textschlüssel instances of the directory.
     */
    Textschluessel[] getTextschluessel();

    /**
     * Gets a Textschlüssel instance of the directory.
     *
     * @param key The key of the Textschlüssel to return.
     * @param extension The extension of the Textschlüssel to return.
     *
     * @return The Textschlüssel identified by {@code key} and {@code extension} or {@code null} if nothing is known
     * about {@code key} and {@code extension}.
     *
     * @throws IllegalArgumentException if {@code key} or {@code extension} is negative, or {@code key} is greater than
     * {@code 99} or {@code extension} is greater than {@code 999}.
     */
    Textschluessel getTextschluessel( int key, int extension );

    /**
     * Gets a Textschlüssel instance of the directory for a given date.
     *
     * @param key The key of the Textschlüssel to return.
     * @param extension The extension of the Textschlüssel to return.
     * @param date The date of validity of the Textschlüssel to return.
     *
     * @return The Textschlüssel identified by {@code key} and {@code extension} valid at {@code date} or {@code null}
     * if nothing is known about {@code key} and {@code extension} at {@code date}.
     *
     * @throws IllegalArgumentException if {@code key} or {@code extension} is negative, or {@code key} is greater than
     * {@code 99} or {@code extension} is greater than {@code 999}.
     * @throws NullPointerException if {@code date} is {@code null}.
     *
     * @see Textschluessel#isValidAt(java.util.Date)
     */
    Textschluessel getTextschluessel( int key, int extension, Date date );

    /**
     * Searches the directory for Textschlüssel instances.
     *
     * @param debit Value of property {@code debit} of the Textschlüssel instances to return.
     * @param remittance Value of property {@code remittance} of the Textschlüssel instances to return.
     *
     * @return All Textschlüssel instances from the directory with property {@code debit} equal to the {@code debit}
     * argument and property {@code remittance} equal to the {@code remittance} argument.
     *
     * @deprecated Replaced by {@link #searchTextschluessel(java.lang.Boolean, java.lang.Boolean, java.util.Date) }.
     */
     Textschluessel[] search( boolean debit, boolean remittance );

    /**
     * Searches the directory for Textschlüssel instances for a given date.
     *
     * @param debit Value of property {@code debit} of the Textschlüssel instances to return.
     * @param remittance Value of property {@code remittance} of the Textschlüssel instances to return.
     * @param date The date of validity of the Textschlüssel to return.
     *
     * @return All Textschlüssel instances from the directory with property {@code debit} equal to the {@code debit}
     * argument and property {@code remittance} equal to the {@code remittance} argument valid at {@code date}.
     *
     * @throws NullPointerException if {@code date} is {@code null}.
     *
     * @deprecated Replaced by {@link #searchTextschluessel(java.lang.Boolean, java.lang.Boolean, java.util.Date)}.
     */
     Textschluessel[] search( boolean debit, boolean remittance, Date date );

    /**
     * Searches the directory for Textschlüssel instances.
     *
     * @param debit Value of property {@code debit} of the Textschlüssel instances to return; {@code null} to ignore
     * property {@code debit} during searching.
     * @param remittance Value of property {@code remittance} of the Textschlüssel instances to return; {@code null}
     * to ignore property {@code remittance} during searching.
     * @param date The date of validity of the Textschlüssel to return; {@code null} to ignore properties
     * {@code validFrom} and {@code validTo} during searching.
     *
     * @return All Textschlüssel instances matching the given criteria.
     */
    Textschluessel[] searchTextschluessel( Boolean debit, Boolean remittance, Date date );

}

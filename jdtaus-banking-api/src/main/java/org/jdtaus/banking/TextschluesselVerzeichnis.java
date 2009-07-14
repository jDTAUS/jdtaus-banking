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

import java.util.Date;

/**
 * Directory of german transaction types.
 * <p>Example: Getting the jDTAUS Banking SPI implementation.<br/><pre>
 * TextschluesselVerzeichnis directory =
 *     (TextschluesselVerzeichnis) ContainerFactory.getContainer().
 *     getObject( TextschluesselVerzeichnis.class );
 * </pre></p>
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 *
 * @see org.jdtaus.core.container.Container
 */
public interface TextschluesselVerzeichnis
{
    //--TextschluesselVerzeichnis-----------------------------------------------

    /**
     * Gets all Textschlüssel instances from the directory.
     *
     * @return all Textschlüssel instances from the directory.
     */
    Textschluessel[] getTextschluessel();

    /**
     * Gets a Textschlüssel from the directory.
     * <p>Calling this method is the same as calling<blockquote><pre>
     * getTextschluessel( key, extension, new Date() )</pre></blockquote></p>
     *
     * @param key the key of the Textschlüssel to return.
     * @param extension the extension of the Textschlüssel to return.
     *
     * @return the Textschlüssel identified by {@code key} and {@code extension}
     * or {@code null} if nothing is known about {@code key} and
     * {@code extension}.
     *
     * @throws IllegalArgumentException if {@code key} or {@code extension} is
     * negative, or {@code key} is greater than {@code 99} or {@code extension}
     * is greater than {@code 999}.
     *
     * @deprecated Replaced by {@link #getTextschluessel(int, int, java.util.Date) }
     */
     Textschluessel getTextschluessel( int key, int extension );

    /**
     * Gets a Textschlüssel from the directory for a given date.
     *
     * @param key the key of the Textschlüssel to return.
     * @param extension the extension of the Textschlüssel to return.
     * @param date the date of validity of the Textschlüssel to return.
     *
     * @return the Textschlüssel identified by {@code key} and {@code extension}
     * valid at {@code date} or {@code null} if nothing is known about
     * {@code key} and {@code extension} at {@code date}.
     *
     * @throws IllegalArgumentException if {@code key} or {@code extension} is
     * negative, or {@code key} is greater than {@code 99} or {@code extension}
     * is greater than {@code 999}.
     * @throws NullPointerException if {@code date} is {@code null}.
     *
     * @see Textschluessel#isValidAt(java.util.Date)
     */
    Textschluessel getTextschluessel( int key, int extension, Date date );

    /**
     * Searches the directory for Textschlüssel instances.
     * <p>Calling this method is the same as calling<blockquote><pre>
     * search( key, extension, new Date() )</pre></blockquote></p>
     *
     * @param debit desired value of property {@code debit} of the Textschlüssel
     * instances to return.
     * @param remittance desired value of property {@code remittance} of the
     * Textschlüssel instances to return.
     *
     * @return all Textschlüssel instances from the directory with property
     * {@code debit} equal to the {@code debit} argument and property
     * {@code remittance} equal to the {@code remittance} argument.
     *
     * @deprecated Replaced by {@link #search(boolean, boolean, java.util.Date) }
     */
     Textschluessel[] search( boolean debit, boolean remittance );

    /**
     * Searches the directory for Textschlüssel instances for a given date.
     *
     * @param debit desired value of property {@code debit} of the Textschlüssel
     * instances to return.
     * @param remittance desired value of property {@code remittance} of the
     * Textschlüssel instances to return.
     * @param date the date of validity of the Textschlüssel to return.
     *
     * @return all Textschlüssel instances from the directory with property
     * {@code debit} equal to the {@code debit} argument and property
     * {@code remittance} equal to the {@code remittance} argument valid at
     * {@code date}.
     *
     * @throws NullPointerException if {@code date} is {@code null}.
     */
    Textschluessel[] search( boolean debit, boolean remittance, Date date );

    //------------------------------------------------TextschlüsselVerzeichnis--
}

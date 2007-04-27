/*
 *  jDTAUS - DTAUS fileformat.
 *  Copyright (c) 2005 Christian Schulte <cs@schulte.it>
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
package org.jdtaus.banking.ri.txtdirectory;

import java.util.Locale;
import org.jdtaus.banking.Textschluessel;

/**
 * Gets thrown for duplicate {@code Textschluessel}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class DuplicateTextschluesselException extends RuntimeException
{

    //--Constructors------------------------------------------------------------

    /**
     * Creates a new instance of {@code DuplicateTextschluesselException} taking
     * the duplicate {@code Textschluessel} instance.
     *
     * @param textschluessel the duplicate {@code Textschluessel} instance.
     */
    public DuplicateTextschluesselException(final Textschluessel textschluessel)
    {
        super(DuplicateTextschluesselExceptionBundle.
            getDuplicateTextschluesselMessage(Locale.getDefault()).
            format(textschluessel == null ? null
            : new Object[] { new Integer(textschluessel.getKey()),
            new Integer(textschluessel.getExtension()) }));

        this.textschluessel = textschluessel;
    }

    //------------------------------------------------------------Constructors--
    //--DuplicateTextschluesselException----------------------------------------

    /***
     * The duplicate {@code Textschluessel}.
     * @serial
     */
    private Textschluessel textschluessel;

    /**
     * Gets the the duplicate {@code Textschluessel} instance.
     *
     * @return the duplicate {@code Textschluessel} or {@code null}.
     */
    public Textschluessel getTextschluessel()
    {
        return this.textschluessel;
    }

    //----------------------------------------DuplicateTextschluesselException--
}

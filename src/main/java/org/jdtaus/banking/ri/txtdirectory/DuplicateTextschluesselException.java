/*
 *  jDTAUS Banking RI Textschluesselverzeichnis
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
package org.jdtaus.banking.ri.txtdirectory;

import org.jdtaus.banking.Textschluessel;
import org.jdtaus.core.container.ContainerFactory;

/**
 * Gets thrown for duplicate {@code Textschluessel}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class DuplicateTextschluesselException extends RuntimeException
{
    //--Constants---------------------------------------------------------------

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = 5015126446786857205L;

    //---------------------------------------------------------------Constants--
    //--DuplicateTextschluesselException----------------------------------------

    /***
     * The duplicate {@code Textschluessel}.
     * @serial
     */
    private Textschluessel textschluessel;

    /**
     * Creates a new instance of {@code DuplicateTextschluesselException} taking
     * the duplicate {@code Textschluessel} instance.
     *
     * @param textschluessel the duplicate {@code Textschluessel} instance.
     */
    public DuplicateTextschluesselException(
        final Textschluessel textschluessel )
    {
        super();
        this.textschluessel = textschluessel;
    }

    /**
     * Gets the the duplicate {@code Textschluessel} instance.
     *
     * @return the duplicate {@code Textschluessel} or {@code null}.
     */
    public Textschluessel getTextschluessel()
    {
        return this.textschluessel;
    }

    /**
     * Returns the message of the exception.
     *
     * @return the message of the exception.
     */
    public String getMessage()
    {
        String message = null;

        if ( this.getTextschluessel() != null )
        {
            message = this.getDuplicateTextschluesselMessage(
                new Integer( this.getTextschluessel().getKey() ),
                new Integer( this.getTextschluessel().getExtension() ) );

        }

        return message;
    }

    //----------------------------------------DuplicateTextschluesselException--
    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>duplicateTextschluessel</code>.
     * <blockquote><pre>Textschlüssel {0,number,00}{1,number,000}  ist mehrfach vorhanden.</pre></blockquote>
     * <blockquote><pre>Non-unique Textschluessel {0,number,00}{1,number,000}.</pre></blockquote>
     *
     * @param key format argument.
     * @param extension format argument.
     *
     * @return the text of message <code>duplicateTextschluessel</code>.
     */
    private String getDuplicateTextschluesselMessage(
            java.lang.Number key,
            java.lang.Number extension )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "duplicateTextschluessel",
                new Object[]
                {
                    key,
                    extension
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

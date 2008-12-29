/*
 *  jDTAUS Banking Messages
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
package org.jdtaus.banking.messages;

import java.util.Locale;
import org.jdtaus.banking.Textschluessel;
import org.jdtaus.banking.dtaus.LogicalFileType;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.text.Message;

/**
 * Message stating that a {@code Textschluessel} cannot be used in combination
 * with a logical file type.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class TextschluesselConstraintMessage extends Message
{
    //--Contstants--------------------------------------------------------------

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = 998685158483386658L;

    //---------------------------------------------------------------Constants--
    //--Message-----------------------------------------------------------------

    /**
     * The type of the logical file incompatible with {@code textschluessel}.
     * @serial
     */
    private final LogicalFileType fileType;

    /**
     * The {@code Textschluessel} incompatible with {@code fileType}.
     * @serial
     */
    private final Textschluessel textschluessel;

    /**
     * Creates a new {@code TextschluesselConstraintMessage} taking the
     * logical file's type and the incompatible {@code Textschluessel}.l
     *
     * @param fileType the type of the logical file causing this exception.
     * @param textschluessel the {@code Textschluessel} incompatible with
     * {@code fileType}.
     *
     * @throws NullPointerException if either {@code fileType} or
     * {@code textschluessel} is {@code null}.
     */
    public TextschluesselConstraintMessage( final LogicalFileType fileType,
                                             final Textschluessel textschluessel )
    {
        super();

        if ( fileType == null )
        {
            throw new NullPointerException( "fileType" );
        }
        if ( textschluessel == null )
        {
            throw new NullPointerException( "textschluessel" );
        }

        this.fileType = fileType;
        this.textschluessel = textschluessel;
    }

    /**
     * {@inheritDoc}
     *
     * @return the DTAUS code of the file's type and the key and extension
     * of the incompatible {@code Textschluessel}.
     * <ul>
     * <li>[0]: the DTAUS code of the file's type.</li>
     * <li>[!]: the key of the incompatible {@code Textschluessel}.</li>
     * <li>[2]: the extension of the incompatible {@code Textschluessel}.</li>
     * </ul>
     */
    public Object[] getFormatArguments( final Locale locale )
    {
        return new Object[] {
            this.fileType.getCode(),
            new Integer( this.textschluessel.getKey() ),
            new Integer( this.textschluessel.getExtension() )
        };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * <blockquote><pre>
     * A logical file with label {0} cannot hold transactions with Textschlüssel {1,number,00}{2,number,000}.
     * </pre></blockquote>
     */
    public String getText( final Locale locale )
    {
        return this.getTextschluesselConstraintMessage(
            locale, this.fileType.getShortDescription( locale ),
            new Integer( this.textschluessel.getKey() ),
            new Integer( this.textschluessel.getExtension() ) );

    }

    //-----------------------------------------------------------------Message--
    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>textschluesselConstraint</code>.
     * <blockquote><pre>Eine logische Datei vom Typ {0} kann keine {1,number,00}{2,number,000} Textschlüssel speichern.</pre></blockquote>
     * <blockquote><pre>A logical file with label {0} cannot hold transactions with Textschlüssel {1,number,00}{2,number,000}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param label format argument.
     * @param key format argument.
     * @param extension format argument.
     *
     * @return the text of message <code>textschluesselConstraint</code>.
     */
    private String getTextschluesselConstraintMessage( final Locale locale,
            final java.lang.String label,
            final java.lang.Number key,
            final java.lang.Number extension )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "textschluesselConstraint", locale,
                new Object[]
                {
                    label,
                    key,
                    extension
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

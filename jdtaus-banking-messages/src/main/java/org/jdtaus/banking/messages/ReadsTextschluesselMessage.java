/*
 *  jDTAUS Banking Messages
 *  Copyright (c) 2005 Christian Schulte
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
 *  $JDTAUS$
 */
package org.jdtaus.banking.messages;

import java.util.Locale;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.text.Message;

/**
 * Message stating that Textschlüssel are being read.
 * @author Christian Schulte
 * @version $JDTAUS$
 */
public class ReadsTextschluesselMessage extends Message
{

    /** Serial version UID for backwards compatibility with 1.11.x classes. */
    private static final long serialVersionUID = 4267470749107250621L;

    /** Empty array. */
    private static final Object[] ARGUMENTS =
    {
    };

    /** Creates a new {@code ReadsTextschluesselMessage} instance. */
    public ReadsTextschluesselMessage()
    {
        super();
    }

    /**
     * {@inheritDoc}
     *
     * @return An empty array, since the message has no arguments.
     */
    public Object[] getFormatArguments( final Locale locale )
    {
        return ARGUMENTS;
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * <blockquote><pre>
     * Reads Textschlüssel.
     * </pre></blockquote>
     */
    public String getText( final Locale locale )
    {
        return this.getReadingTextschluesselMessage( locale );
    }

    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>readingTextschluessel</code>.
     * <blockquote><pre>Liest Textschlüssel.</pre></blockquote>
     * <blockquote><pre>Reading Textschlüssel.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     *
     * @return the text of message <code>readingTextschluessel</code>.
     */
    private String getReadingTextschluesselMessage( final Locale locale )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "readingTextschluessel", locale, null );

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

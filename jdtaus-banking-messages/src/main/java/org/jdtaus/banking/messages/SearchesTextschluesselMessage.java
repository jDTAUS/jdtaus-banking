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
 *  $Id$
 */
package org.jdtaus.banking.messages;

import java.util.Locale;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.text.Message;

/**
 * Message stating that Textschlüssel information is being searched.
 *
 * @author Christian Schulte
 * @version $Id$
 */
public final class SearchesTextschluesselMessage extends Message
{

    /** Serial version UID for backwards compatibility with 1.11.x classes. */
    private static final long serialVersionUID = 4800640627259962318L;

    /** Empty array. */
    private static final Object[] ARGUMENTS =
    {
    };

    /** Creates a new {@code SearchesTextschluesselMessage} instance. */
    public SearchesTextschluesselMessage()
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
     * Searches Textschlüssel directory.
     * </pre></blockquote>
     */
    public String getText( final Locale locale )
    {
        return this.getSearchingTextschluesselMessage( locale );
    }

    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>searchingTextschluessel</code>.
     * <blockquote><pre>Durchsucht Textschlüsselverzeichnis.</pre></blockquote>
     * <blockquote><pre>Searching Textschlüssel directory.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     *
     * @return the text of message <code>searchingTextschluessel</code>.
     */
    private String getSearchingTextschluesselMessage( final Locale locale )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "searchingTextschluessel", locale, null );

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

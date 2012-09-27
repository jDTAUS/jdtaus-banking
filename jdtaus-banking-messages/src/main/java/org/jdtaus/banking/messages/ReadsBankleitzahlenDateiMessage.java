/*
 *  jDTAUS Banking Messages
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
package org.jdtaus.banking.messages;

import java.util.Locale;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.text.Message;

/**
 * Message stating that bankfiles are being read.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public final class ReadsBankleitzahlenDateiMessage extends Message
{

    /** Serial version UID for backwards compatibility with 1.1.x classes. */
    private static final long serialVersionUID = -7923064314185101629L;

    /** Empty array. */
    private static final Object[] ARGUMENTS =
    {
    };

    /** Creates a new {@code ReadsBankleitzahlenDateiMessage} instance. */
    public ReadsBankleitzahlenDateiMessage()
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
     * Reads bankfiles.
     * </pre></blockquote>
     */
    public String getText( final Locale locale )
    {
        return this.getReadingBankfilesMessage( locale );
    }

    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>readingBankfiles</code>.
     * <blockquote><pre>Liest Bankleitzahlendateien.</pre></blockquote>
     * <blockquote><pre>Reading bankfiles.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     *
     * @return the text of message <code>readingBankfiles</code>.
     */
    private String getReadingBankfilesMessage( final Locale locale )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "readingBankfiles", locale, null );

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

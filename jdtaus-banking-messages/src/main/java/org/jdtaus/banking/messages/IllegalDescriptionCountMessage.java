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
 * Message stating that in illegal amount of descriptions is used.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public final class IllegalDescriptionCountMessage extends Message
{

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = 1131942219877499068L;

    /**
     * Maximum number of supported descriptions.
     * @serial
     */
    private final int maxDescriptions;

    /**
     * Requested number of descriptions.
     * @serial
     */
    private final int requestedDescriptions;

    /**
     * Creates a new {@code IllegalDescriptionCountMessage} instance taking the maximum number of supported descriptions
     * and the number of requested descriptions.
     *
     * @param maxDescriptions The maximum number of supported descriptions.
     * @param requestedDescriptions The requested number of descriptions.
     */
    public IllegalDescriptionCountMessage( final int maxDescriptions, final int requestedDescriptions )
    {
        super();
        this.maxDescriptions = maxDescriptions;
        this.requestedDescriptions = requestedDescriptions;
    }

    /**
     * {@inheritDoc}
     *
     * @return The maximum number of supported descriptions and the requested number of descriptions.
     * <ul>
     * <li>[0]: the maximum number of supported descriptions.</li>
     * <li>[1]: the requested number of descriptions.</li>
     * </ul>
     */
    public Object[] getFormatArguments( final Locale locale )
    {
        return new Object[]
            {
                new Integer( this.maxDescriptions ),
                new Integer( this.requestedDescriptions )
            };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * <blockquote><pre>
     * The number of descriptions ({1,number}) exceeds the possible number {0,number}.
     * </pre></blockquote>
     */
    public String getText( final Locale locale )
    {
        return this.getIllegalDescriptionCountMessage(
            locale, new Integer( this.requestedDescriptions ), new Integer( this.maxDescriptions ) );

    }

    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>illegalDescriptionCount</code>.
     * <blockquote><pre>Die Anzahl der Verwendungszweckzeilen ({1,number}) übersteigt die Anzahl der zulässigen Verwendungszweckzeilen {0,number}.</pre></blockquote>
     * <blockquote><pre>The number of descriptions ({1,number}) exceeds the possible number {0,number}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param descriptionCount format parameter.
     * @param allowedDescriptions format parameter.
     *
     * @return the text of message <code>illegalDescriptionCount</code>.
     */
    private String getIllegalDescriptionCountMessage( final Locale locale,
            final java.lang.Number descriptionCount,
            final java.lang.Number allowedDescriptions )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "illegalDescriptionCount", locale,
                new Object[]
                {
                    descriptionCount,
                    allowedDescriptions
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

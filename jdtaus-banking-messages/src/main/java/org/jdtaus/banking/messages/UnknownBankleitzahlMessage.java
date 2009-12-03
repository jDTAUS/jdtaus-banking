/*
 *  jDTAUS Banking Messages
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
package org.jdtaus.banking.messages;

import java.util.Locale;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.text.Message;

/**
 * Message stating that Bankleitzahl is unknown.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public final class UnknownBankleitzahlMessage extends Message
{

    /** Serial version UID for backwards compatibility with 1.1.x classes. */
    private static final long serialVersionUID = -7923064314185101629L;

    /**
     * Bankleitzahl of the message.
     * @serial
     */
    private final Bankleitzahl bankCode;

    /**
     * Creates a new {@code UnknownBankleitzahlMessage} taking the unknown Bankleitzahl.
     *
     * @param bankCode The unknown Bankleitzahl.
     *
     * @throws NullPointerException if {@code bankCode} is {@code null}.
     */
    public UnknownBankleitzahlMessage( final Bankleitzahl bankCode )
    {
        if ( bankCode == null )
        {
            throw new NullPointerException( "bankCode" );
        }

        this.bankCode = bankCode;
    }

    /**
     * {@inheritDoc}
     *
     * @return Information regarding the unknown Bankleitzahl.
     * <ul>
     * <li>[0]: the unknown Bankleitzahl.</li>
     * </ul>
     */
    public Object[] getFormatArguments( final Locale locale )
    {
        return new Object[]
            {
                this.bankCode.format( Bankleitzahl.LETTER_FORMAT )
            };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * <blockquote><pre>
     * Unknown Bankleitzahl {0}.
     * </pre></blockquote>
     */
    public String getText( final Locale locale )
    {
        return this.getUnknownBankleitzahlMessage(
            locale, this.bankCode.format( Bankleitzahl.LETTER_FORMAT ) );

    }

    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>unknownBankleitzahl</code>.
     * <blockquote><pre>Unbekannte Bankleitzahl {0}.</pre></blockquote>
     * <blockquote><pre>Unknown Bankleitzahl {0}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param unknownBankleitzahl format argument.
     *
     * @return the text of message <code>unknownBankleitzahl</code>.
     */
    private String getUnknownBankleitzahlMessage( final Locale locale,
            final java.lang.String unknownBankleitzahl )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "unknownBankleitzahl", locale,
                new Object[]
                {
                    unknownBankleitzahl
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

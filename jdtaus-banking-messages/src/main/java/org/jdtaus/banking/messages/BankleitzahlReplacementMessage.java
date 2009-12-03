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
import org.jdtaus.banking.BankleitzahlInfo;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.text.Message;

/**
 * Message stating that a Bankleitzahl has been published as a replacement for an expired Bankleitzahl.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public final class BankleitzahlReplacementMessage extends Message
{

    /** Serial version UID for backwards compatibility with 1.2.x classes. */
    private static final long serialVersionUID = -4309551284229560988L;

    /**
     * Information regarding the replacement Bankleitzahl.
     * @serial
     */
    private final BankleitzahlInfo info;

    /**
     * Creates a new {@code BankleitzahlReplacementMessage} taking information about a replacement Bankleitzahl.
     *
     * @param info Information regarding the replacement Bankleitzahl.
     *
     * @throws NullPointerException if {@code info} is {@code null}.
     */
    public BankleitzahlReplacementMessage( final BankleitzahlInfo info )
    {
        if ( info == null )
        {
            throw new NullPointerException( "info" );
        }

        this.info = info;
    }

    /**
     * {@inheritDoc}
     *
     * @return Information regarding the replacement Bankleitzahl.
     * <ul>
     * <li>[0]: the replacement Bankleitzahl.</li>
     * </ul>
     */
    public Object[] getFormatArguments( final Locale locale )
    {
        return new Object[]
            {
                this.info.getBankCode().format( Bankleitzahl.LETTER_FORMAT )
            };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * <blockquote><pre>
     * The bank has published the replacement Bankleitzahl {0}.
     * </pre></blockquote>
     */
    public String getText( final Locale locale )
    {
        return this.getBankleitzahlReplacementMessage(
            locale, this.info.getBankCode().format( Bankleitzahl.LETTER_FORMAT ) );

    }

    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>bankleitzahlReplacement</code>.
     * <blockquote><pre>Die Bank hat die Bankleitzahl {0} als Nachfolge veröffentlicht.</pre></blockquote>
     * <blockquote><pre>The bank has published the replacement Bankleitzahl {0}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param bankleitzahl format argument.
     *
     * @return the text of message <code>bankleitzahlReplacement</code>.
     */
    private String getBankleitzahlReplacementMessage( final Locale locale,
            final java.lang.String bankleitzahl )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "bankleitzahlReplacement", locale,
                new Object[]
                {
                    bankleitzahl
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

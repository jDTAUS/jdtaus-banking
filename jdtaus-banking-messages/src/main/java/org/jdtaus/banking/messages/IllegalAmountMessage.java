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

import java.math.BigInteger;
import java.util.Locale;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.text.Message;

/**
 * Message stating that an amount is invalid.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public final class IllegalAmountMessage extends Message
{

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = 1922186182644325080L;

    /**
     * The illegal amount.
     * @serial
     */
    private final BigInteger amount;

    /**
     * Creates a new {@code IllegalAmountMessage} taking an amount.
     *
     * @param amount The illegal amount.
     *
     * @throws NullPointerException if {@code amount} is {@code null}.
     */
    public IllegalAmountMessage( final BigInteger amount )
    {
        super();

        if ( amount == null )
        {
            throw new NullPointerException( "amount" );
        }

        this.amount = amount;
    }

    /**
     * {@inheritDoc}
     *
     * @return The illegal amount.
     * <ul>
     * <li>[0]: illegal amount.</li>
     * </ul>
     */
    public Object[] getFormatArguments( final Locale locale )
    {
        return new Object[]
            {
                this.amount
            };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * <blockquote><pre>
     * {0,number} is no legal amount.
     * </pre></blockquote>
     */
    public String getText( final Locale locale )
    {
        return this.getIllegalAmountMessage( locale, this.amount );
    }

    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>illegalAmount</code>.
     * <blockquote><pre>Ungültiger Betrag {0,number}.</pre></blockquote>
     * <blockquote><pre>{0, number} is no legal amount.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param amt format argument.
     *
     * @return the text of message <code>illegalAmount</code>.
     */
    private String getIllegalAmountMessage( final Locale locale,
            final java.lang.Number amt )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "illegalAmount", locale,
                new Object[]
                {
                    amt
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

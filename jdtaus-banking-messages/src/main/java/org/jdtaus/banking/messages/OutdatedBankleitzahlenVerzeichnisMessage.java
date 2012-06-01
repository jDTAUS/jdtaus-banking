/*
 *  jDTAUS Banking Messages
 *  Copyright (C) 2005 Christian Schulte
 *  <schulte2005@users.sourceforge.net>
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

import java.util.Date;
import java.util.Locale;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.text.Message;

/**
 * Message stating that the {@code BundesbankBankleitzahlenVerzeichnis} is
 * outdated.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $JDTAUS$
 */
public final class OutdatedBankleitzahlenVerzeichnisMessage extends Message
{

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = -196759846361567335L;

    /**
     * Date of expiration of the directory.
     * @serial
     */
    private final Date dateOfExpiration;

    /**
     * Creates a new {@code OutdatedDirectoryMessage} instance taking the date of expiration.
     *
     * @param dateOfExpiration the date of expiration of the directory.
     *
     * @throws NullPointerException if {@code dateOfExpiration} is {@code null}.
     */
    public OutdatedBankleitzahlenVerzeichnisMessage( final Date dateOfExpiration )
    {
        if ( dateOfExpiration == null )
        {
            throw new NullPointerException( "dateOfExpiration" );
        }

        this.dateOfExpiration = dateOfExpiration;
    }

    /**
     * {@inheritDoc}
     *
     * @return The date of expiration of the {@code BankleitzahlenVerzeichnis}.
     * <ul>
     * <li>[0]: the date of expiration of the directory.</li>
     * </ul>
     */
    public Object[] getFormatArguments( final Locale locale )
    {
        return new Object[]
            {
                this.dateOfExpiration
            };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * <blockquote><pre>
     * The directory of bankcodes expired at {0,date,long}.
     * </pre></blockquote>
     */
    public String getText( final Locale locale )
    {
        return this.getOutdatedDirectoryMessage( locale, this.dateOfExpiration );
    }

    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>outdatedDirectory</code>.
     * <blockquote><pre>Das Bankleitzahlenverzeichnis ist am {0,date,long} abgelaufen.</pre></blockquote>
     * <blockquote><pre>The directory of bankcodes expired at {0,date,long}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param expirationDate format argument.
     *
     * @return the text of message <code>outdatedDirectory</code>.
     */
    private String getOutdatedDirectoryMessage( final Locale locale,
            final java.util.Date expirationDate )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "outdatedDirectory", locale,
                new Object[]
                {
                    expirationDate
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

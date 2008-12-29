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

import java.util.Date;
import java.util.Locale;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.text.Message;

/**
 * Message stating that the {@code BundesbankBankleitzahlenVerzeichnis} is
 * outdated.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class OutdatedBankleitzahlenVerzeichnisMessage extends Message
{
    //--Contstants--------------------------------------------------------------

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = -196759846361567335L;

    //---------------------------------------------------------------Constants--
    //--Message-----------------------------------------------------------------

    /**
     * Date of expiration of the directory.
     * @serial
     */
    private final Date dateOfExpiration;

    /**
     * Creates a new {@code OutdatedDirectoryMessage} instance taking the
     * date of expiration.
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
     * @return the date of expiration of the {@code BankleitzahlenVerzeichnis}.
     * <ul>
     * <li>[0]: the date of expiration of the directory.</li>
     * </ul>
     */
    public Object[] getFormatArguments( final Locale locale )
    {
        return new Object[] { this.dateOfExpiration };
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
        return this.getOutdatedDirectoryMessage( locale,
                                                 this.dateOfExpiration );

    }

    //-----------------------------------------------------------------Message--
    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>outdatedDirectory</code>.
     * <blockquote><pre>Das Bankleitzahlenverzeichnis is am {0,date,long} abgelaufen.</pre></blockquote>
     * <blockquote><pre>The directory of bankcodes expired at {0,date,long}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param dateOfExpiration format argument.
     *
     * @return the text of message <code>outdatedDirectory</code>.
     */
    private String getOutdatedDirectoryMessage( final Locale locale,
            final java.util.Date dateOfExpiration )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "outdatedDirectory", locale,
                new Object[]
                {
                    dateOfExpiration
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

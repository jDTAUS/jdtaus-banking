/*
 *  jDTAUS Banking API
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
package org.jdtaus.banking;

import java.util.Locale;
import org.jdtaus.core.container.ContainerFactory;

/**
 * Gets thrown whenever the {@code BankleitzahlenVerzeichnis} is queried for
 * a Bankleitzahl which got deleted in the past.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class BankleitzahlExpirationException extends Exception
{
    //--Constants---------------------------------------------------------------

    /** Serial version UID for backwards compatibility with 1.1.x classes. */
    private static final long serialVersionUID = -1834668094534345716L;

    //---------------------------------------------------------------Constants--
    //--BankleitzahlExpirationException-----------------------------------------

    /**
     * Information about the expired Bankleitzahl.
     * @serial
     */
    private BankleitzahlInfo info;

    /**
     * Information about the Bankleitzahl replacing the expired Bankleitzahl.
     * @serial
     */
    private BankleitzahlInfo replacement;

    /**
     * Creates a new {@code BankleitzahlExpirationException} taking the
     * bankcode information of the expired Bankleitzahl.
     *
     * @param info the bankcode information of the expired Bankleitzahl.
     * @param replacement the bankcode information of the Bankleitzahl
     * replacing {@code info}.
     *
     * @throws NullPointerException if either {@code info} or
     * {@code replacement} is {@code null}.
     */
    public BankleitzahlExpirationException( final BankleitzahlInfo info,
        final BankleitzahlInfo replacement )
    {
        super();

        this.info = info;
        this.replacement = replacement;
    }

    /**
     * Gets information about the expired Bankleitzahl.
     *
     * @return information about the expired Bankleitzahl.
     */
    public BankleitzahlInfo getExpiredBankleitzahlInfo()
    {
        return this.info;
    }

    /**
     * Gets information about the Bankleitzahl replacing the expired
     * Bankleitzahl.
     *
     * @return information about the Bankleitzahl replacing the expired
     * Bankleitzahl.
     */
    public BankleitzahlInfo getReplacingBankleitzahlInfo()
    {
        return this.replacement;
    }

    /**
     * Returns the message of the exception.
     *
     * @return the message of the exception.
     */
    public String getMessage()
    {
        return this.getBankleitzahlExpirationMessage(
            this.getLocale(),
            this.info.getBankCode().format( Bankleitzahl.LETTER_FORMAT ),
            this.replacement.getBankCode().format(
            Bankleitzahl.LETTER_FORMAT ) );

    }

    //-----------------------------------------BankleitzahlExpirationException--
    //--Dependencies------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the configured <code>Locale</code> implementation.
     *
     * @return The configured <code>Locale</code> implementation.
     */
    private Locale getLocale()
    {
        return (Locale) ContainerFactory.getContainer().
            getDependency( this, "Locale" );

    }

// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>bankleitzahlExpiration</code>.
     * <blockquote><pre>Die Bankleitzahl {0} ist nicht mehr gültig. Die Bank hat die Bankleitzahl {1} als Ersatz veröffentlicht.</pre></blockquote>
     * <blockquote><pre>The Bankleitzahl {0} has expired. The bank published the replacement Bankleitzahl {1}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param bankleitzahl format argument.
     * @param replacement format argument.
     *
     * @return the text of message <code>bankleitzahlExpiration</code>.
     */
    private String getBankleitzahlExpirationMessage( final Locale locale,
            final java.lang.String bankleitzahl,
            final java.lang.String replacement )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "bankleitzahlExpiration", locale,
                new Object[]
                {
                    bankleitzahl,
                    replacement
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}
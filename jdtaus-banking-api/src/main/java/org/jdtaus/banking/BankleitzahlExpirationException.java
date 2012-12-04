/*
 *  jDTAUS Banking API
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
package org.jdtaus.banking;

import java.util.Locale;
import org.jdtaus.core.container.ContainerFactory;

/**
 * Gets thrown whenever the {@code BankleitzahlenVerzeichnis} is queried for
 * a Bankleitzahl which got deleted in the past.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public class BankleitzahlExpirationException extends Exception
{

    /** Serial version UID for backwards compatibility with 1.1.x classes. */
    private static final long serialVersionUID = -1834668094534345716L;

    /**
     * Information about the expired Bankleitzahl.
     * @serial
     */
    private final BankleitzahlInfo info;

    /**
     * Information about the Bankleitzahl replacing the expired Bankleitzahl.
     * @serial
     */
    private final BankleitzahlInfo replacement;

    /**
     * Creates a new {@code BankleitzahlExpirationException} taking the bankcode information of the expired
     * Bankleitzahl.
     *
     * @param info The bankcode information of the expired Bankleitzahl.
     * @param replacement The bankcode information of the Bankleitzahl replacing {@code info}.
     *
     * @throws NullPointerException if either {@code info} or {@code replacement} is {@code null}.
     */
    public BankleitzahlExpirationException( final BankleitzahlInfo info, final BankleitzahlInfo replacement )
    {
        super();
        this.info = info;
        this.replacement = replacement;
    }

    /**
     * Gets information about the expired Bankleitzahl.
     *
     * @return Information about the expired Bankleitzahl.
     */
    public BankleitzahlInfo getExpiredBankleitzahlInfo()
    {
        return this.info;
    }

    /**
     * Gets information about the Bankleitzahl replacing the expired Bankleitzahl.
     *
     * @return information about the Bankleitzahl replacing the expired Bankleitzahl.
     */
    public BankleitzahlInfo getReplacingBankleitzahlInfo()
    {
        return this.replacement;
    }

    /**
     * Returns the message of the exception.
     *
     * @return The message of the exception.
     */
    public String getMessage()
    {
        return this.getBankleitzahlExpirationMessage(
            this.getLocale(), this.info.getBankCode().format( Bankleitzahl.LETTER_FORMAT ),
            this.replacement.getBankCode().format( Bankleitzahl.LETTER_FORMAT ) );

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
     * @param expired format parameter.
     * @param rplc format parameter.
     *
     * @return the text of message <code>bankleitzahlExpiration</code>.
     */
    private String getBankleitzahlExpirationMessage( final Locale locale,
            final java.lang.String expired,
            final java.lang.String rplc )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "bankleitzahlExpiration", locale,
                new Object[]
                {
                    expired,
                    rplc
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

/*
 *  jDTAUS Banking RI CurrencyDirectory
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
package org.jdtaus.banking.ri.currencydir;

import java.util.Currency;
import org.jdtaus.core.container.ContainerFactory;

/**
 * Gets thrown for duplicate {@code Currency} instances.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class DuplicateCurrencyException extends RuntimeException
{
    //--Constants---------------------------------------------------------------

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = 8084715524328704554L;

    //---------------------------------------------------------------Constants--
    //--DuplicateCurrencyException----------------------------------------------

    /***
     * The duplicate {@code Currency}.
     * @serial
     */
    private Currency currency;

    /**
     * Creates a new instance of {@code DuplicateCurrencyException} taking
     * the duplicate {@code Currency} instance.
     *
     * @param currency the duplicate {@code Currency} instance.
     */
    public DuplicateCurrencyException( final Currency currency )
    {
        super();
        this.currency = currency;
    }

    /**
     * Gets the the duplicate {@code Currency} instance.
     *
     * @return the duplicate {@code Currency} or {@code null}.
     */
    public Currency getCurrency()
    {
        return this.currency;
    }

    /**
     * Returns the message of the exception.
     *
     * @return the message of the exception.
     */
    public String getMessage()
    {
        return this.getDuplicateCurrencyMessage(
            this.currency.getCurrencyCode(), this.currency.getSymbol() );

    }

    //----------------------------------------------DuplicateCurrencyException--
    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>duplicateCurrency</code>.
     * <blockquote><pre>Währung {1} ({0}) ist mehrfach vorhanden.</pre></blockquote>
     * <blockquote><pre>Non-unique currency {1} ({0}).</pre></blockquote>
     *
     * @param currencyCode format argument.
     * @param currencySymbol format argument.
     *
     * @return the text of message <code>duplicateCurrency</code>.
     */
    private String getDuplicateCurrencyMessage(
            java.lang.String currencyCode,
            java.lang.String currencySymbol )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "duplicateCurrency",
                new Object[]
                {
                    currencyCode,
                    currencySymbol
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

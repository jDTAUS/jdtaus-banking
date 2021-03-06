/*
 *  jDTAUS Banking SPI
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
package org.jdtaus.banking.spi;

import java.util.Date;
import java.util.Locale;
import org.jdtaus.core.container.ContainerFactory;

/**
 * Gets thrown for illegal currencies.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public class UnsupportedCurrencyException extends IllegalArgumentException
{

    /** Serial version UID for backwards compatibility with 1.1.x classes. */
    private static final long serialVersionUID = -4268651061144430651L;

    /**
     * Currency code causing an {@code UnsupportedCurrencyException}.
     * @serial
     */
    private String currencyCode;

    /**
     * Date for which the currency is not supported.
     * @serial
     */
    private Date date;

    /**
     * Creates a new {@code UnsupportedCurrencyException} taking the unsupported currency code together with the date
     * for which it was requested.
     *
     * @param currencyCode The ISO currency code which is not supported at {@code date}.
     * @param date the date for which {@code currencyCode} is illegal.
     */
    public UnsupportedCurrencyException( final String currencyCode, final Date date )
    {
        super();
        this.currencyCode = currencyCode;
        this.date = (Date) ( date == null ? null : date.clone() );
    }

    /**
     * Gets the currency code causing this exception to be thrown.
     *
     * @return The currency code causing this exception to be thrown or {@code null}.
     */
    public String getCurrencyCode()
    {
        return this.currencyCode;
    }

    /**
     * Gets the date for which {@code getCurrencyCode()} is not supported.
     *
     * @return The date for which {@code getCurrencyCode()} is not supported or {@code null}.
     */
    public Date getDate()
    {
        return (Date) ( this.date == null ? null : this.date.clone() );
    }

    /**
     * Returns the message of the exception.
     *
     * @return The message of the exception.
     */
    public String getMessage()
    {
        return this.getUnsupportedCurrencyMessage( this.getLocale(), this.currencyCode, this.date );
    }

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
     * Gets the text of message <code>unsupportedCurrency</code>.
     * <blockquote><pre>Die {0} Währung steht am {1,date,long} nicht zur Verfügung.</pre></blockquote>
     * <blockquote><pre>The currency {0} is not available at {1,date,long}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param currency format parameter.
     * @param date format parameter.
     *
     * @return the text of message <code>unsupportedCurrency</code>.
     */
    private String getUnsupportedCurrencyMessage( final Locale locale,
            final java.lang.String currency,
            final java.util.Date date )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "unsupportedCurrency", locale,
                new Object[]
                {
                    currency,
                    date
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

/*
 *  jDTAUS - DTAUS fileformat.
 *  Copyright (c) 2005 Christian Schulte <cs@schulte.it>
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
import java.util.Locale;

/**
 * Gets thrown for duplicate {@code Currency} instances.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class DuplicateCurrencyException extends RuntimeException
{
    //--Constructors------------------------------------------------------------

    /**
     * Creates a new instance of {@code DuplicateCurrencyException} taking
     * the duplicate {@code Currency} instance.
     *
     * @param currency the duplicate {@code Currency} instance.
     */
    public DuplicateCurrencyException(final Currency currency)
    {
        super(DuplicateCurrencyExceptionBundle.
            getDuplicateCurrencyMessage(Locale.getDefault()).
            format(currency == null ? null
            : new Object[] { currency.getCurrencyCode(),
            currency.getSymbol() }));

        this.currency = currency;
    }

    //------------------------------------------------------------Constructors--
    //--DuplicateCurrencyException----------------------------------------------

    /***
     * The duplicate {@code Currency}.
     * @serial
     */
    private Currency currency;

    /**
     * Gets the the duplicate {@code Currency} instance.
     *
     * @return the duplicate {@code Currency} or {@code null}.
     */
    public Currency getCurrency()
    {
        return this.currency;
    }

    //----------------------------------------------DuplicateCurrencyException--
}

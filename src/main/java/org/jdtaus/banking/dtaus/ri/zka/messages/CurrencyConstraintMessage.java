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
package org.jdtaus.banking.dtaus.ri.zka.messages;

import java.util.Date;
import java.util.Locale;
import org.jdtaus.core.text.Message;

/**
 * Fehler-Meldung: "Eine logische Datei vom {0,date,long} kann keine
 * {1} Währung speichern."
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class CurrencyConstraintMessage extends Message
{
    //--Message-----------------------------------------------------------------

    public Object[] getFormatArguments(final Locale locale)
    {
        return new Object[]{ this.getDate(), this.getCurrencyCode() };
    }

    public String getText(final Locale locale)
    {
        return CurrencyConstraintMessageBundle.
            getCurrencyConstraintMessage(locale).
            format(this.getFormatArguments(locale));

    }

    //-----------------------------------------------------------------Message--
    //--CurrencyConstraintMessage-----------------------------------------------

    /**
     * The ISO currency code of the currency violating the constraint.
     * @serial
     */
    private String currencyCode;

    /**
     * The date at which {@code currencyCode} violates the constraint.
     * @serial
     */
    private Date date;

    /**
     * Creates a new {@code CurrencyConstraintMessage} taking a currency code
     * of the currency violating the constraint at a given date.
     *
     * @param currencyCode the ISO currency code of the currency violating the
     * constraint.
     * @param date the date at which {@code currencyCode} violates the
     * constraint.
     *
     * @throws NullPointerException if either {@code currencyCode} or
     * {@code date} is {@code null}.
     */
    public CurrencyConstraintMessage(final String currencyCode,
        final Date date)
    {
        if(currencyCode == null)
        {
            throw new NullPointerException("currencyCode");
        }
        if(date == null)
        {
            throw new NullPointerException("date");
        }

        this.currencyCode = currencyCode;
        this.date = date;
    }

    /**
     * Gets the ISO currency code of the currency violating the constraint.
     *
     * @return the ISO currency code of the currency violating the constraint.
     */
    public String getCurrencyCode()
    {
        return this.currencyCode;
    }

    /**
     * Gets the date at which {@code currencyCode} violates the constraint.
     *
     * @return the date at which {@code currencyCode} violates the constraint.
     */
    public Date getDate()
    {
        return this.date;
    }

    //-----------------------------------------------CurrencyConstraintMessage--
}

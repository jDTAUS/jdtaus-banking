/*
 *  jDTAUS - DTAUS fileformat.
 *  Copyright (C) 2005 - 2007 Christian Schulte <cs@schulte.it>
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
package org.jdtaus.banking.dtaus.spi.runtime.messages;

import java.util.Currency;
import java.util.Locale;
import org.jdtaus.core.text.Message;

/**
 * Meldung falls Annahmen bei fehlende Währungs-Angaben gemacht werden.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class CurrencyViolationMessage extends Message {
    
    //--CurrencyViolationMessage------------------------------------------------
    
    /**
     * Feld-Konstante.
     * @serial
     */
    private Integer field;
    
    /**
     * Absolute Position von {@code field}.
     * @serial
     */
    private Long position;
    
    /**
     * Vorhandener DTAUS code.
     * @serial
     */
    private Character code;
    
    /**
     * Property {@code code} entsprechende Währung.
     * @serial
     */
    private Currency currency;
    
    /**
     * Erzeugt eine neue {@code CurrencyViolationMessage} Instanz.
     *
     * @param field Konstante des Währungs-Feldes.
     * @param position absolute Position des Währungs-Feldes.
     * @param code in {@code field} gespeicherter Wert.
     * @param currency {@code code} entsprechende Währung.
     */
    public CurrencyViolationMessage(final int field, final long position,
        final char code, final Currency currency) {
        
        this.field = new Integer(field);
        this.position = new Long(position);
        this.code = new Character(code);
        this.currency = currency;
    }
    
    //------------------------------------------------CurrencyViolationMessage--
    //--Message-----------------------------------------------------------------
    
    /**
     * Argumente zur Formatierung des Meldungs-Textes.
     *
     * @param locale zu verwendene Lokalisierung.
     *
     * @return Argumente zur Formatierung des Meldungs-Textes. <p>Index 0:
     * Feld-Name<br/>Index 1: absolute Position des Feldes<br/>Index 2:
     * DTAUS Daten<br/>Index 3: angenommener Währungs-Code<br/>
     * Index 4: Währungs-Symbol</p>
     */
    public Object[] getFormatArguments(final Locale locale) {
        return new Object[] {
            Integer.toHexString(this.field.intValue()).toUpperCase(locale),
            this.position,
            this.code,
            this.currency == null ? "" : this.currency.getCurrencyCode(),
            this.currency == null ? "" : this.currency.getSymbol(locale)
        };
    }
    
    public String getText(final Locale locale) {
        return CurrencyViolationMessageBundle.
            getCurrencyViolationMessage(locale).
            format( this.getFormatArguments(locale));
        
    }
    
    //-----------------------------------------------------------------Message--
    
}

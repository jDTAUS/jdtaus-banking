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
package org.jdtaus.banking.dtaus.spi.runtime.messages;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;
import org.jdtaus.banking.dtaus.PhysicalFileError;
import org.jdtaus.banking.dtaus.spi.AbstractErrorMessage;
import org.jdtaus.core.text.Message;

/**
 * Fehler-Meldung für ungültige Feld-Werte.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class IllegalDataMessage extends AbstractErrorMessage {

    //--Konstanten--------------------------------------------------------------

    /**
     * Konstante für Zeichen des DTAUS-Alphabets.
     */
    public static final int TYPE_ALPHA = 1;

    /**
     * Konstante für Ziffern des DTAUS-Alphabets.
     */
    public static final int TYPE_NUMERIC = 2;

    /**
     * Konstante für Zeichen des alpha-numerischen DTAUS-Alphabets.
     */
    public static final int TYPE_ALPHA_NUMERIC = 3;

    /**
     * Konstante für EBCDIC gepackte Zahl.
     */
    public static final int TYPE_PACKET_POSITIVE = 4;

    /**
     * Konstante für konstante Werte.
     */
    public static final int TYPE_CONSTANT = 5;

    /**
     * Konstante für Reserve-Felder.
     */
    public static final int TYPE_RESERVED = 6;

    /**
     * Konstante für Datums-Felder mit zweistelliger Jahresangabe.
     */
    public static final int TYPE_SHORTDATE = 7;

    /**
     * Konstante für Datums-Felder mit vierstelliger Jahresangabe.
     */
    public static final int TYPE_LONGDATE = 8;

    /**
     * Konstante für Feld 3 des A-Datensatzes (Datei-Typ).
     */
    public static final int TYPE_FILETYPE = 9;

    /**
     * Konstante für Bankleitzahlen-Felder.
     */
    public static final int TYPE_BANKLEITZAHL = 10;

    /**
     * Konstante für Kontonummern-Felder.
     */
    public static final int TYPE_KONTONUMMER = 11;

    /**
     * Konstante für Referenznummern-Felder.
     */
    public static final int TYPE_REFERENZNUMMER = 12;

    /**
     * Konstante für Textschlüssel-Felder.
     */
    public static final int TYPE_TEXTSCHLUESSEL = 13;

    /**
     * Konstante für Währungs-Felder.
     */
    public static final int TYPE_CURRENCY = 14;

    /** Alle Typ-Konstanten. */
    private static final int[] TYPES = {
        TYPE_ALPHA, TYPE_NUMERIC, TYPE_ALPHA_NUMERIC, TYPE_PACKET_POSITIVE,
        TYPE_CONSTANT, TYPE_RESERVED, TYPE_SHORTDATE, TYPE_LONGDATE,
        TYPE_FILETYPE, TYPE_BANKLEITZAHL, TYPE_KONTONUMMER, TYPE_REFERENZNUMMER,
        TYPE_TEXTSCHLUESSEL, TYPE_CURRENCY
    };

    //--------------------------------------------------------------Konstanten--
    //--Konstruktoren-----------------------------------------------------------

    /**
     * Erzeugt eine neue {@code IllegalDataMessage} mit Angaben zu den
     * ungültigen Daten.
     *
     * @param field Konstante des defekten Feldes.
     * @param type Konstante aus dieser Klasse zur Beschreibung des Feldes.
     * @param position absolute Datei-Position von {@code field}.
     * @param invalidData ungültige Daten.
     *
     * @throws IllegalArgumentException wenn {@code type} keiner der
     * Konstanten {@link #TYPE_ALPHA TYPE_<i>XYZ</i>} entspricht.
     *
     * @see org.jdtaus.banking.dtaus.Fields
     */
    public IllegalDataMessage(final int field, final int type,
        final long position, final String invalidData) {

        super();
        this.field = field;
        this.type = type;
        this.position = position;
        this.invalidData = invalidData;
        this.assertValidType();
    }

    /**
     * Zugriff auf {@code IllegalDataMessage} Instanzen.
     *
     * @param messages Meldungen aus denen alle Meldungen des Typs
     * {@code IllegalDataMessage} ermittelt werden sollen.
     *
     * @return alle in {@code messages} enthaltenen Meldungen dieses Typs oder
     * ein leeres Array, wenn keine {@code IllegalDataMessage} Meldungen in
     * {@code messages} vorhanden sind.
     *
     * @throws NullPointerException {@code if(messages == null)}
     */
    public static IllegalDataMessage[] getMessages(
        final Message[] messages) {

        if(messages == null) {
            throw new NullPointerException("messages");
        }

        final int numMessages = messages.length;
        final Collection ret = numMessages == 0 ?
            Collections.EMPTY_LIST : new LinkedList();

        for(int i = numMessages - 1; i >= 0; i--) {
            if(messages[i].getClass() == IllegalDataMessage.class) {
                ret.add(messages[i]);
            }
        }

        return (IllegalDataMessage[]) ret.toArray(
            new IllegalDataMessage[ret.size()]);

    }

    private void assertValidType() {
        boolean valid = false;
        for(int i = TYPES.length - 1; i >= 0 && !valid; i--) {
            if(TYPES[i] == this.type) {
                valid = true;
                break;
            }
        }

        if(!valid) {
            throw new IllegalArgumentException(
                Integer.toString(this.getType()));

        }
    }

    //-----------------------------------------------------------Konstruktoren--
    //--IllegalDataMessage------------------------------------------------------

    /**
     * Wert der Property {@code <field>}.
     * @serial
     */
    private final int field;

    /**
     * Wert der Property {@code <position>}.
     * @serial
     */
    private final long position;

    /**
     * Wert der Property {@code <type>}.
     * @serial
     */
    private final int type;

    /**
     * Wert der Property {@code <invalidData>}.
     * @serial
     */
    private final String invalidData;

    /**
     * Liest den Wert der Property {@code <field>}.
     *
     * @return Feld-Konstanten des Feldes, in dem die ungültigen Daten
     * vorgefunden wurden.
     */
    public int getField() {
        return this.field;
    }

    /**
     * Liest den Wert der Property {@code <position>}.
     *
     * @return absolute Position an der die ungültigen Daten vorgefunden wurden.
     */
    public long getPosition() {
        return this.position;
    }

    /**
     * Liest den Wert der Property {@code <type>}.
     *
     * @return Beschreibung der erwarteten Daten.
     *
     * @see #TYPE_ALPHA {@code TYPE_<i>XYZ</i>} Konstanten aus dieser Klasse
     */
    public int getType() {
        return this.type;
    }

    /**
     * Liest den Wert der Property {@code <invalidData>}.
     *
     * @return vorgefundene ungültige Daten.
     */
    public String getInvalidData() {
        return this.invalidData;
    }

    //------------------------------------------------------IllegalDataMessage--
    //--Message-----------------------------------------------------------------

    /**
     * Argumente zur Formatierung des Meldungs-Textes.
     *
     * @return Argumente zur Formatierung des Meldungs-Textes. <p>Index 0:
     * Feld-Name<br/>Index 1: absolute Position des Feldes<br/>Index 2:
     * ungültige Daten</p>
     */
    public Object[] getFormatArguments(final Locale locale) {
        return new Object[] {
            Integer.toHexString(this.getField()).toUpperCase(locale),
            new Long(this.getPosition()),
            this.getInvalidData()
        };
    }

    /**
     * Formatierter Standard-Text der Meldung.
     *
     * @param locale zu verwendende Lokalisierung.
     *
     * @return {@code "{2}" ist kein gültiger Wert für Feld {0} (Position {1, number}).}
     */
    public String getText(final Locale locale) {
        return IllegalDataMessageBundle.getIllegalDataMessage(locale).
            format(this.getFormatArguments(locale));

    }

    //-----------------------------------------------------------------Message--

}

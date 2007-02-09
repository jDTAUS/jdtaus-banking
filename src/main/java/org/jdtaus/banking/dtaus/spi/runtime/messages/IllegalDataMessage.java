/*
 *  jDTAUS - DTAUS fileformat.
 *  Copyright (C) 2005 Christian Schulte <cs@schulte.it>
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
package org.jdtaus.banking.dtaus.messages;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;
import org.jdtaus.banking.dtaus.PhysicalFileError;
import org.jdtaus.common.i18n.Message;

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
    public static final int ALPHA = 1;

    /**
     * Konstante für Ziffern des DTAUS-Alphabets.
     */
    public static final int NUMERIC = 2;

    /**
     * Konstante für Zeichen des alpha-numerischen DTAUS-Alphabets.
     */
    public static final int ALPHA_NUMERIC = 3;

    /**
     * Konstante für EBCDIC gepackte Zahl.
     */
    public static final int PACKET_POSITIVE = 4;

    /**
     * Konstante für konstante Werte.
     */
    public static final int CONSTANT = 5;

    /**
     * Konstante für Reserve-Felder.
     */
    public static final int RESERVED = 6;

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
     * @throws IllegalArgumentException bei ungültigen Angaben.
     * @throws PhysicalFileError {@code if(isErrorsEnabled())}
     *
     * @see org.jdtaus.common.dtaus.Fields
     */
    public IllegalDataMessage(final int field, final int type,
        final long position, final Object invalidData) throws
        PhysicalFileError {

        super();
        this.field = field;
        this.type = type;
        this.position = position;
        this.invalidData = invalidData;
        this.assertValidType();
        if(AbstractErrorMessage.isErrorsEnabled()) {
            throw new PhysicalFileError(this);
        }
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

    private static final int[] TYPES = {
        ALPHA, NUMERIC, ALPHA_NUMERIC, PACKET_POSITIVE, CONSTANT, RESERVED
    };

    private void assertValidType() {
        boolean valid = false;
        for(int i = TYPES.length - 1; i >= 0 && !valid; i--) {
            if(TYPES[i] == this.type) {
                valid = true;
            }
        }

        if(!valid) {
            throw new IllegalArgumentException("type");
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
    private final Object invalidData;

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
     * @see #ALPHA Konstanten aus dieser Klasse
     */
    public int getType() {
        return this.type;
    }

    /**
     * Liest den Wert der Property {@code <invalidData>}.
     *
     * @return vorgefundene ungültige Daten.
     */
    public Object getInvalidData() {
        return this.invalidData;
    }

    //------------------------------------------------------IllegalDataMessage--
    //--Message-----------------------------------------------------------------

    public Object[] getFormatArguments() {
        return new Object[] {
            Integer.toHexString(this.getField()).toUpperCase(),
            new Long(this.getPosition()),
            this.getInvalidData()
        };
    }

    /** {@inheritDoc} */
    public String getText(final Locale locale) {
        return IllegalDataMessageBundle.getIllegalDataMessage(locale).format(
            new Object[] { Integer.toHexString(this.getField()).toUpperCase(),
            Long.valueOf(this.getPosition()), this.getInvalidData() });

    }

    //-----------------------------------------------------------------Message--

}

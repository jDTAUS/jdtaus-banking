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
import org.jdtaus.banking.Textschluessel;
import org.jdtaus.common.i18n.Message;

/**
 * Fehler-Meldung für unbekannte Textschlüssel.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class UnsupportedTransactionTypeMessage
    extends AbstractErrorMessage {

    //--Konstruktoren-----------------------------------------------------------

    /**
     * Erzeugt eine neue {@code UnsupportedTransactionTypeMessage} Instanz mit
     * Detailangaben zu den ungültigen Daten.
     *
     * @param position absolute Datei-Position, an der ein unbekannter
     * Textschlüssel vorgefunden wurde.
     * @param unsupportedKey gelesener Textschlüssel für den keine
     * {@code TransactionType} Instanz bekannt ist.
     * @param unsupportedExt gelesene Ergänzung für die keine
     * {@code TransactionType} Instanz bekannt ist.
     * @param supportedTypes unterstützte {@code TransactionType}
     * Instanzen.
     *
     * @throws PhysicalFileError {@code if(isErrorsEnabled())}
     */
    public UnsupportedTransactionTypeMessage(final long position,
        final int unsupportedKey, final int unsupportedExt,
        final Textschluessel[] supportedTypes) throws PhysicalFileError {

        super();
        this.position = position;
        this.unsupportedKey = unsupportedKey;
        this.unsupportedExt = unsupportedExt;
        this.supportedTypes = supportedTypes;
        if(AbstractErrorMessage.isErrorsEnabled()) {
            throw new PhysicalFileError(this);
        }
    }

    /**
     * Zugriff auf {@code UnsupportedTransactionTypeMessage} Instanzen.
     *
     * @param messages Meldungen aus denen alle Meldungen des Typs
     * {@code UnsupportedTransactionTypeMessage} ermittelt werden sollen.
     *
     * @return alle in {@code messages} enthaltenen Meldungen dieses Typs oder
     * ein leeres Array, wenn keine {@code UnsupportedTransactionTypeMessage}
     * Meldungen in {@code messages} vorhanden sind.
     *
     * @throws NullPointerException {@code if(messages == null)}
     */
    public static UnsupportedTransactionTypeMessage[] getMessages(
        final Message[] messages) {

        if(messages == null) {
            throw new NullPointerException("messages");
        }

        final int numMessages = messages.length;
        final Collection ret = numMessages == 0 ?
            Collections.EMPTY_LIST : new LinkedList();

        for(int i = numMessages - 1; i >= 0; i--) {
            if(messages[i].getClass() ==
                UnsupportedTransactionTypeMessage.class) {

                ret.add(messages[i]);
            }
        }

        return (UnsupportedTransactionTypeMessage[]) ret.toArray(
            new UnsupportedTransactionTypeMessage[ret.size()]);

    }

    //-----------------------------------------------------------Konstruktoren--
    //--UnsupportedTransactionTypeMessage---------------------------------------

    /**
     * Absolute Datei-Position.
     * @serial
     */
    private final long position;

    /**
     * Der nicht unterstütze Textschlüssel.
     * @serial
     */
    private final int unsupportedKey;

    /**
     * Die nicht unterstütze Ergänzung.
     * @serial
     */
    private final int unsupportedExt;

    /**
     * Die unterstützen Kennzeichnungen.
     * @serial
     */
    private final Textschluessel[] supportedTypes;

    /**
     * Liest den Wert der Property {@code <position>}.
     *
     * @return absolute Datei-Position.
     */
    public long getPosition() {
        return this.position;
    }

    /**
     * Liest den Wert der Property {@code <unsupportedKey>}.
     *
     * @return nicht unterstützter Textschlüssel.
     */
    public int getUnsupportedKey() {
        return this.unsupportedKey;
    }

    /**
     * Liest den Wert der Property {@code <unsupportedExt>}.
     *
     * @return nicht unterstützte Ergänzung.
     */
    public int getUnsupportedExt() {
        return this.unsupportedExt;
    }

    /**
     * Liest den Wert der Property {@code <supportedTypes>}.
     *
     * @return unterstützte Kennzeichnungen.
     */
    public Textschluessel[] getSupportedTypes() {
        return this.supportedTypes;
    }

    //---------------------------------------UnsupportedTransactionTypeMessage--
    //--Message-----------------------------------------------------------------

    public Object[] getFormatArguments() {
        return new Object[] {
            new Long(this.getPosition()),
            new Integer(this.getUnsupportedKey()),
            new Integer(this.getUnsupportedExt())
        };
    }

    /** {@inheritDoc} */
    public String getText(final Locale locale) {
        return UnsupportedTransactionTypeMessageBundle.
            getUnsupportedTransactionTypeMessage(locale).format(new Object[] {
            Integer.valueOf(this.getUnsupportedKey()),
            Integer.valueOf(this.getUnsupportedExt())
        });
    }

    //-----------------------------------------------------------------Message--

}

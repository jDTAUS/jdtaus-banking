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
import org.jdtaus.banking.dtaus.LogicalFileType;
import org.jdtaus.banking.dtaus.PhysicalFileError;
import org.jdtaus.common.i18n.Message;

/**
 * Fehler-Meldung für unbekannte Datei-Kennzeichen.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class UnsupportedHeaderLabelMessage extends AbstractErrorMessage {

    //--Konstruktoren-----------------------------------------------------------

    /**
     * Erzeugt eine neue {@code UnsupportedHeaderLabelMessage} Instanz mit
     * Detailangaben zu den ungültigen Daten.
     *
     * @param position absolute Datei-Position, an der ein unbekanntes
     * Datei-Kennzeichen vorgefunden wurde.
     * @param unsupportedLabel gelesene Kennzeichnung für die keine
     * {@code HeaderLabel} Instanz bekannt ist.
     * @param supportedLabels unterstützte {@code HeaderLabel} Instanzen.
     *
     * @throws PhysicalFileError {@code if(isErrorsEnabled())}
     */
    public UnsupportedHeaderLabelMessage(final long position,
        final String unsupportedLabel,
        final LogicalFileType[] supportedLabels) throws PhysicalFileError {

        super();
        this.position = position;
        this.unsupportedLabel = unsupportedLabel;
        this.supportedLabels = supportedLabels;
        if(AbstractErrorMessage.isErrorsEnabled()) {
            throw new PhysicalFileError(this);
        }
    }

    /**
     * Zugriff auf {@code UnsupportedHeaderLabelMessage} Instanzen.
     *
     * @param messages Meldungen aus denen alle Meldungen des Typs
     * {@code UnsupportedHeaderLabelMessage} ermittelt werden sollen.
     *
     * @return alle in {@code messages} enthaltenen Meldungen dieses Typs oder
     * ein leeres Array, wenn keine {@code UnsupportedHeaderLabelMessage}
     * Meldungen in {@code messages} vorhanden sind.
     *
     * @throws NullPointerException {@code if(messages == null)}
     */
    public static UnsupportedHeaderLabelMessage[] getMessages(
        final Message[] messages) {

        if(messages == null) {
            throw new NullPointerException("messages");
        }

        final int numMessages = messages.length;
        final Collection ret = numMessages == 0 ?
            Collections.EMPTY_LIST : new LinkedList();

        for(int i = numMessages - 1; i >= 0; i--) {
            if(messages[i].getClass() == UnsupportedHeaderLabelMessage.class) {
                ret.add(messages[i]);
            }
        }

        return (UnsupportedHeaderLabelMessage[]) ret.toArray(
            new UnsupportedHeaderLabelMessage[ret.size()]);

    }

    //-----------------------------------------------------------Konstruktoren--
    //--UnsupportedHeaderLabelMessage-------------------------------------------

    /**
     * Absolute Datei-Position.
     * @serial
     */
    private final long position;

    /**
     * Die nicht unterstütze Kennzeichnung.
     * @serial
     */
    private final String unsupportedLabel;

    /**
     * Die unterstützen Kennzeichnungen.
     * @serial
     */
    private final LogicalFileType[] supportedLabels;

    /**
     * Liest den Wert der Property {@code <position>}.
     *
     * @return absolute Datei-Position.
     */
    public long getPosition() {
        return this.position;
    }

    /**
     * Liest den Wert der Property {@code <unsupportedLabel>}.
     *
     * @return nicht unterstützte Kennzeichnung.
     */
    public String getUnsupportedLabel() {
        return this.unsupportedLabel;
    }

    /**
     * Liest den Wert der Property {@code <supportedLabels>}.
     *
     * @return unterstützte Kennzeichnungen.
     */
    public LogicalFileType[] getSupportedLabels() {
        return this.supportedLabels;
    }

    //-------------------------------------------UnsupportedHeaderLabelMessage--
    //--Message-----------------------------------------------------------------

    public Object[] getFormatArguments() {
        return new Object[] { new Long(position), this.getUnsupportedLabel() };
    }

    /** {@inheritDoc} */
    public String getText(final Locale locale) {
        return UnsupportedHeaderLabelMessageBundle.
            getUnsupportedHeaderLabelMessage(locale).format(new Object[] {
            this.getUnsupportedLabel()
        });
    }

    //-----------------------------------------------------------------Message--

}

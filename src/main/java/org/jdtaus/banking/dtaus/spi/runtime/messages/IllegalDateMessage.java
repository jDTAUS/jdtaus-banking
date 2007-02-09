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
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import org.jdtaus.banking.dtaus.PhysicalFileError;
import org.jdtaus.common.i18n.Message;

/**
 * Fehler-Meldung für ungültige Datums-Angaben.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class IllegalDateMessage extends AbstractErrorMessage {

    //--Konstanten--------------------------------------------------------------

    /** 1. Januar 1980 00:00:00 CET. */
    private static final long VALID_DATES_START_MILLIS = 315529200000L;

    /** 31. Dezember 2079 23:59:59 CET. */
    private static final long VALID_DATES_END_MILLIS = 3471289199999L;

    /** 1. Januar 1980 00:00:00 CET. */
    private static final Date VALID_DATES_START =
        new Date(VALID_DATES_START_MILLIS);

    /** 31. Dezember 2079 23:59:59 CET. */
    private static final Date VALID_DATES_END =
        new Date(VALID_DATES_END_MILLIS);

    /** Ungültiges Datum. */
    private static final Date INVALID_DATE = new Date(0L);

    //--------------------------------------------------------------Konstanten--
    //--Konstruktoren-----------------------------------------------------------

    /**
     * Erzeugt eine neue {@code IllegalDateMessage} mit Angaben zu dem
     * ungültigen Datum.
     *
     * @param field Konstante des Datums-Feldes.
     * @param position absolute Datei-Position von {@code field}.
     * @param illegalDate ungültiges Datum.
     *
     * @throws PhysicalFileError {@code if(isErrorsEnabled())}
     *
     * @see org.jdtaus.common.dtaus.Fields
     */
    public IllegalDateMessage(final int field, final long position,
        final Date illegalDate) throws PhysicalFileError {

        super();
        this.field = field;
        this.position = position;
        this.illegalDate = illegalDate;
        if(AbstractErrorMessage.isErrorsEnabled()) {
            throw new PhysicalFileError(this);
        }
    }

    /**
     * Zugriff auf {@code IllegalDateMessage} Instanzen.
     *
     * @param messages Meldungen aus denen alle Meldungen des Typs
     * {@code IllegalDateMessage} ermittelt werden sollen.
     *
     * @return alle in {@code messages} enthaltenen Meldungen dieses Typs oder
     * ein leeres Array, wenn keine {@code IllegalDateMessage} Meldungen in
     * {@code messages} vorhanden sind.
     *
     * @throws NullPointerException {@code if(messages == null)}
     */
    public static IllegalDateMessage[] getMessages(
        final Message[] messages) {

        if(messages == null) {
            throw new NullPointerException("messages");
        }

        final int numMessages = messages.length;
        final Collection ret = numMessages == 0 ?
            Collections.EMPTY_LIST : new LinkedList();

        for(int i = numMessages - 1; i >= 0; i--) {
            if(messages[i].getClass() == IllegalDateMessage.class) {
                ret.add(messages[i]);
            }
        }

        return (IllegalDateMessage[]) ret.toArray(
            new IllegalDateMessage[ret.size()]);

    }

    //-----------------------------------------------------------Konstruktoren--
    //--IllegalDateMessage------------------------------------------------------

    /**
     * Wert von Property {@code <field>}.
     * @serial
     */
    private final int field;

    /**
     * Wert von Property {@code <position>}.
     * @serial
     */
    private final long position;

    /**
     * Wert von Property {@code <illegalDate>}.
     * @serial
     */
    private final Date illegalDate;

    /**
     * Liest den Wert der Property {@code <field>}.
     *
     * @return Konstante des Datums-Feldes.
     *
     * @see org.jdtaus.common.dtaus.Fields
     */
    public int getField() {
        return this.field;
    }

    /**
     * Liest den Wert der Property {@code <position>}.
     *
     * @return absolute Datei-Position des Datums-Feldes.
     */
    public long getPosition() {
        return this.position;
    }

    /**
     * Liest den Wert der Property {@code <illegalDate>}.
     *
     * @return ungültiges Datum.
     */
    public Date getIllegalDate() {
        return this.illegalDate;
    }

    /**
     * Prüfung eines Datums.
     *
     * @param date zu prüfendes Datum.
     *
     * @return {@code true} wenn {@code date} im gültigen Bereich zwischen
     * 1980 und 2079 inklusive liegt; {@code false} wenn nicht.
     */
    public static boolean isDateValid(final Date date) {
        boolean ret = date != null;

        if(ret) {
            final long millis = date.getTime();
            ret = millis >= IllegalDateMessage.VALID_DATES_START_MILLIS &&
                millis <= IllegalDateMessage.VALID_DATES_END_MILLIS;

        }

        return ret;
    }

    //------------------------------------------------------IllegalDateMessage--
    //--Message-----------------------------------------------------------------

    public Object[] getFormatArguments() {
        return new Object[] {
            Integer.toHexString(this.getField()),
            new Long(this.getPosition()),
            this.getIllegalDate()
        };
    }

    /** {@inheritDoc} */
    public String getText(final Locale locale) {
        return IllegalDateMessageBundle.getIllegalDateMessage(locale).format(
            new Object[] {
            this.getIllegalDate() == null ?
                IllegalDateMessage.INVALID_DATE : this.getIllegalDate(),
            Integer.toHexString(this.field).toUpperCase(),
            IllegalDateMessage.VALID_DATES_START,
            IllegalDateMessage.VALID_DATES_END,
            Long.valueOf(this.getPosition())
        });
    }

    //-----------------------------------------------------------------Message--

}

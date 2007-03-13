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
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.spi.AbstractErrorMessage;
import org.jdtaus.core.text.Message;

/**
 * Fehler-Meldung für eine ungültige Kombination von Dateierstellungs- und
 * Ausführungs-Datum.
 * <p/>
 * Ausführungsdatum nicht jünger als Dateierstellungsdatum (Feld A7), jedoch
 * höchstens 15 Kalendertage über Erstellungsdatum aus Feld A7.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class IllegalScheduleMessage extends AbstractErrorMessage
{

    //--Konstruktoren-----------------------------------------------------------

    /**
     * Erzeugt eine neue {@code IllegalScheduleMessage}.
     *
     * @param position absolute Position der logischen Datei.
     * @param header A Datensatz mit ungültiger Terminierung.
     *
     * @throws NullPointerException wenn {@code header} {@code null} ist.
     * @throws IllegalArgumentException wenn {@code position} negativ ist
     * oder {@code header.getSchedule()} keiner ungültigen Auftragsterminierung
     * entspricht.
     */
    public IllegalScheduleMessage(final long position, final Header header)
    {
        super();

        if(header == null)
        {
            throw new NullPointerException("header");
        }
        if(position < 0L)
        {
            throw new IllegalArgumentException(Long.toString(position));
        }
        if(Header.Schedule.checkSchedule(header.getSchedule().getCreateDate(),
            header.getSchedule().getExecutionDate()))
        {

            throw new IllegalArgumentException(
                Long.toString(header.getSchedule().getCreateDate().getTime()));

        }

        this.position = position;
        this.header = header;
    }

    /**
     * Zugriff auf {@code IllegalScheduleMessage} Instanzen.
     *
     * @param messages Meldungen aus denen alle Meldungen des Typs
     * {@code IllegalScheduleMessage} ermittelt werden sollen.
     *
     * @return alle in {@code messages} enthaltenen Meldungen dieses Typs oder
     * ein leeres Array, wenn keine {@code IllegalScheduleMessage} Meldungen
     * in {@code messages} vorhanden sind.
     *
     * @throws NullPointerException {@code if(messages == null)}
     */
    public static IllegalScheduleMessage[] getMessages(
        final Message[] messages)
    {

        if(messages == null)
        {
            throw new NullPointerException("messages");
        }

        final int numMessages = messages.length;
        final Collection ret = numMessages == 0 ?
            Collections.EMPTY_LIST : new LinkedList();

        for(int i = numMessages - 1; i >= 0; i--)
        {
            if(messages[i].getClass() == IllegalScheduleMessage.class)
            {
                ret.add(messages[i]);
            }
        }

        return (IllegalScheduleMessage[]) ret.toArray(
            new IllegalScheduleMessage[ret.size()]);

    }

    //-----------------------------------------------------------Konstruktoren--
    //--IllegalScheduleMessage--------------------------------------------------

    /**
     * Wert der Property {@code <position>}.
     * @serial
     */
    private long position;

    /**
     * Wert der Property {@code <header>}.
     * @serial
     */
    private Header header;

    /**
     * Liest den Wert der Property {@code <position>}.
     *
     * @return absolute Position der logischen Datei.
     */
    public long getPosition()
    {
        return this.position;
    }

    /**
     * Liest den Wert der Property {@code <header>}.
     *
     * @return A-Datensatz der logischen Datei mit ungültiger
     * Auftrags-Terminierung.
     */
    public Header getHeader()
    {
        return this.header;
    }

    //--------------------------------------------------IllegalScheduleMessage--
    //--Message-----------------------------------------------------------------

    /**
     * Argumente zur Formatierung des Meldungs-Textes.
     *
     * @return Argumente zur Formatierung des Meldungs-Textes.
     * <p>Index 0: absolute Position der logischen Datei<br/>
     * Index 1: Datei-Erstellungsdatum<br/>
     * Index 2: Auftrags-Ausführungsdatum</p>
     */
    public Object[] getFormatArguments(final Locale locale)
    {
        return new Object[] {
            new Long(this.getPosition()),
            this.getHeader().getSchedule().getCreateDate(),
            this.getHeader().getSchedule().getExecutionDate()
        };
    }

    /**
     * Formatierter Standard-Text der Meldung.
     *
     * @param locale zu verwendende Lokalisierung.
     *
     * @return {@code "Das Ausführungsdatum "{2, date, medium}" liegt vor dem Dateierstellungsdatum "{1, date, medium}" oder mehr als 15 Kalendertage dahinter."}
     */
    public String getText(final Locale locale)
    {
        return IllegalScheduleMessageBundle.getIllegalScheduleMessage(locale).
            format(this.getFormatArguments(locale));

    }

    //-----------------------------------------------------------------Message--

}

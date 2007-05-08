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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.ri.zka.AbstractErrorMessage;
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
     * @param createDate Dateierstellungsdatum.
     * @param executionDate Ausführungsdatum.
     *
     * @throws NullPointerException wenn {@code createDate} {@code null} ist.
     * @throws IllegalArgumentException wenn {@code position} negativ ist.
     */
    public IllegalScheduleMessage(final long position, final Date createDate,
        final Date executionDate)
    {
        super();

        if(createDate == null)
        {
            throw new NullPointerException("createDate");
        }
        if(position < 0L)
        {
            throw new IllegalArgumentException(Long.toString(position));
        }

        this.position = position;
        this.createDate = createDate;
        this.executionDate = executionDate;
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
     * Wert der Property {@code position}.
     * @serial
     */
    private long position;

    /**
     * Wert der Property {@code createDate}.
     * @serial
     */
    private Date createDate;

    /**
     * Wert der Property {@code executionDate}.
     * @serial
     */
    private Date executionDate;

    /**
     * Liest den Wert der Property {@code position}.
     *
     * @return absolute Position der logischen Datei.
     */
    public long getPosition()
    {
        return this.position;
    }

    /**
     * Liest den Wert der Property {@code createDate}.
     *
     * @return Dateierstellungsdatum.
     */
    public Date getCreateDate()
    {
        return this.createDate;
    }

    /**
     * Liest den Wert der Property {@code executionDate}.
     *
     * @return Auftrags-Ausführungsdatum oder {@code null}.
     */
    public Date getExecutionDate()
    {
        return this.executionDate;
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
            this.getCreateDate(),
            this.getExecutionDate()
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

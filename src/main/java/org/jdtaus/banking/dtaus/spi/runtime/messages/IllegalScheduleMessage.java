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
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.PhysicalFileError;
import org.jdtaus.common.i18n.Message;

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
public final class IllegalScheduleMessage extends AbstractErrorMessage {
    
    //--Konstanten--------------------------------------------------------------
    
    /**
     * Maximal erlaubte Anzahl Tage zwischen Erstellungs- und Ausführungsdatum
     * in Millisekunden.
     */
    private static final long MAX_SCHEDULEDAYS_MILLIS =
        15L * 24L * 60L * 60L * 1000L;
    
    //--------------------------------------------------------------Konstanten--
    //--Konstruktoren-----------------------------------------------------------
    
    /**
     * Erzeugt eine neue {@code IllegalScheduleMessage}.
     *
     * @param position Datei-Position des A-Datensatzes.
     * @param schedule ungültige A-Datensatz Terminierung.
     *
     * @throws PhysicalFileError {@code if(isErrorsEnabled())}
     */
    public IllegalScheduleMessage(final long position,
        final Header.Schedule schedule) throws PhysicalFileError {
        
        super();
        this.position = position;
        this.schedule = schedule;
        if(AbstractErrorMessage.isErrorsEnabled()) {
            throw new PhysicalFileError(this);
        }
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
        final Message[] messages) {
        
        if(messages == null) {
            throw new NullPointerException("messages");
        }
        
        final int numMessages = messages.length;
        final Collection ret = numMessages == 0 ?
            Collections.EMPTY_LIST : new LinkedList();
        
        for(int i = numMessages - 1; i >= 0; i--) {
            if(messages[i].getClass() == IllegalScheduleMessage.class) {
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
    private final long position;
    
    /**
     * Wert der Property {@code <schedule>}.
     * @serial
     */
    private final Header.Schedule schedule;
    
    /**
     * Liest den Wert der Property {@code <position>}.
     *
     * @return absolute Datei-Position des A-Datensatzes.
     */
    public long getPosition() {
        return this.position;
    }
    
    /**
     * Liest den Wert der Property {@code <createDate>}.
     *
     * @return ungültiges Dateierstellungsdatum.
     */
    public Header.Schedule getSchedule() {
        return this.schedule;
    }
    
    /**
     * Prüfung einer Auftrags-Terminierung.
     *
     * @param schedule zu prüfende Auftragsterminierung.
     *
     * @return {@code true} wenn {@code schedule} einer gültigen
     * Auftragsterminierung enstpricht; {@code false} wenn nicht.
     */
    public static boolean isScheduleValid(final Header.Schedule schedule) {
        boolean ret = schedule != null && schedule.getCreateDate() != null;
        
        if(ret) {
            final long createMillis = schedule.getCreateDate().getTime();
            if(schedule.getExecutionDate() != null) {
                final long executionMillis =
                    schedule.getExecutionDate().getTime();
                
                ret = executionMillis <= createMillis + MAX_SCHEDULEDAYS_MILLIS;
            }
        }
        
        return ret;
    }
    
    //--------------------------------------------------IllegalScheduleMessage--
    //--Message-----------------------------------------------------------------
    
    public Object[] getFormatArguments() {
        return new Object[] {
            new Long(this.getPosition()),
            this.getSchedule().getCreateDate(),
            this.getSchedule().getExecutionDate()
        };
    }
    
    /** {@inheritDoc} */
    public String getText(final Locale locale) {
        return IllegalScheduleMessageBundle.
            getIllegalScheduleMessage(locale).format(new Object[] {
            this.getSchedule().getCreateDate(),
            this.getSchedule().getExecutionDate()
        });
    }
    
    //-----------------------------------------------------------------Message--
    
}

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
import java.util.LinkedList;
import java.util.Locale;
import org.jdtaus.banking.dtaus.Checksum;
import org.jdtaus.core.text.Message;

/**
 * Fehler-Meldung für Prüfsummenfehler.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class ChecksumErrorMessage extends Message
{
    //--Konstruktoren-----------------------------------------------------------

    /**
     * Erzeugt eine neue {@code ChecksumErrorMessage} Instanz.
     *
     * @param storedChecksum in einer logischen Datei gespeicherte Prüfsumme.
     * @param computedChecksum aus derselben logischen Datei berrechnete
     * Prüfsumme.
     * @param position absolute Position der logischen Datei.
     *
     * @throws NullPointerException wenn entweder {@code storedChecksum} oder
     * {@code computedChecksum} {@code null} ist.
     * @throws IllegalArgumentException wenn {@code storedChecksum} und
     * {@code computedChecksum} gleich sind oder {@code position} negativ ist.
     */
    public ChecksumErrorMessage(final Checksum storedChecksum,
        final Checksum computedChecksum, final long position)
    {

        if(storedChecksum == null)
        {
            throw new NullPointerException("storedChecksum");
        }
        if(computedChecksum == null)
        {
            throw new NullPointerException("computedChecksum");
        }
        if(storedChecksum.equals(computedChecksum))
        {
            throw new IllegalArgumentException(computedChecksum.toString());
        }
        if(position < 0L)
        {
            throw new IllegalArgumentException(Long.toString(position));
        }

        this.storedChecksum = storedChecksum;
        this.computedChecksum = computedChecksum;
        this.position = position;
    }

    /**
     * Zugriff auf {@code ChecksumErrorMessage} Instanzen.
     *
     * @param messages Meldungen aus denen alle Meldungen des Typs
     * {@code ChecksumErrorMessage} ermittelt werden sollen.
     *
     * @return alle in {@code messages} enthaltenen Meldungen dieses Typs oder
     * ein leeres Array, wenn keine {@code ChecksumErrorMessage} Meldungen in
     * {@code messages} vorhanden sind.
     *
     * @throws NullPointerException wenn {@code messages null} ist.
     */
    public static ChecksumErrorMessage[] getMessages(
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
            if(messages[i].getClass() == ChecksumErrorMessage.class)
            {
                ret.add(messages[i]);
            }
        }

        return (ChecksumErrorMessage[]) ret.toArray(
            new ChecksumErrorMessage[ret.size()]);

    }

    //-----------------------------------------------------------Konstruktoren--
    //--ChecksumErrorMessage----------------------------------------------------

    /**
     * Absolute Position der logischen Datei.
     * @serial
     */
    private long position;

    /**
     * Gespeicherte Prüfsumme.
     * @serial
     */
    private Checksum storedChecksum;

    /**
     * Berechnete Prüfsumme.
     * @serial
     */
    private Checksum computedChecksum;

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
     * Liest den Wert der Property {@code storedChecksum}.
     *
     * @return gespeicherte Prüfsumme.
     */
    public Checksum getStoredChecksum()
    {
        return this.storedChecksum;
    }

    /**
     * Liest den Wert der Property {@code computedChecksum}.
     *
     * @return berrechnete Prüfsumme.
     */
    public Checksum getComputedChecksum()
    {
        return this.computedChecksum;
    }

    //----------------------------------------------------ChecksumErrorMessage--
    //--Message-----------------------------------------------------------------

    /**
     * Argumente zur Formatierung des Meldungs-Textes.
     *
     * @return Argumente zur Formatierung des Meldungs-Textes. <p>Index 0:
     * absolute Position der logischen Datei</p>
     */
    public Object[] getFormatArguments(final Locale locale)
    {
        return new Object[] { new Long(this.getPosition()) };
    }

    /**
     * Formatierter Standard-Text der Meldung.
     *
     * @param locale zu verwendende Lokalisierung.
     *
     * @return {@code "Die Prüfsumme der an Position {0, number} beginnenden Datei ist ungültig."}
     */
    public String getText(final Locale locale)
    {
        return ChecksumErrorMessageBundle.getChecksumErrorMessage(locale).
            format(this.getFormatArguments(locale));

    }

    //-----------------------------------------------------------------Message--
}

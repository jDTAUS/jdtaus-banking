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
import org.jdtaus.core.text.Message;

/**
 * Fehler-Meldung für ungültige Datei-Längen.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class IllegalFileLengthMessage extends Message
{
    //--Konstruktoren-----------------------------------------------------------

    /**
     * Erzeugt eine neue {@code IllegalFileLengthMessage} Instanz.
     *
     * @param fileLength physikalische Datei-Länge.
     * @param blockSize Länge eines Satzabschnitts.
     *
     * @throws IllegalArgumentException if either {@code fileLength} or
     * {@code blockSize} is negative, or if {@code fileLength % blockSize}
     * equals {@code 0}.
     */
    public IllegalFileLengthMessage(final long fileLength, final int blockSize)
    {
        if(fileLength < 0)
        {
            throw new IllegalArgumentException(Long.toString(fileLength));
        }
        if(blockSize <= 0)
        {
            throw new IllegalArgumentException(Integer.toString(blockSize));
        }
        if(fileLength % blockSize == 0)
        {
            throw new IllegalArgumentException(
                Long.toString(fileLength % blockSize));

        }

        this.fileLength = fileLength;
        this.blockSize = blockSize;
    }

    /**
     * Zugriff auf {@code IllegalFileLengthMessage} Instanzen.
     *
     * @param messages Meldungen aus denen alle Meldungen des Typs
     * {@code IllegalFileLengthMessage} ermittelt werden sollen.
     *
     * @return alle in {@code messages} enthaltenen Meldungen dieses Typs oder
     * ein leeres Array, wenn keine {@code IllegalFileLengthMessage} Meldungen
     * in {@code messages} vorhanden sind.
     *
     * @throws NullPointerException wenn {@code messages null} ist.
     */
    public static IllegalFileLengthMessage[] getMessages(
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
            if(messages[i].getClass() == IllegalFileLengthMessage.class)
            {
                ret.add(messages[i]);
            }
        }

        return (IllegalFileLengthMessage[]) ret.toArray(
            new IllegalFileLengthMessage[ret.size()]);

    }

    //-----------------------------------------------------------Konstruktoren--
    //--IllegalFileLengthMessage------------------------------------------------

    /**
     * Physikalische Datei-Länge.
     * @serial
     */
    private long fileLength;

    /**
     * Länge eines Satzabschnitts.
     * @serial
     */
    private int blockSize;

    /**
     * Liest den Wert der Property {@code fileLength}.
     *
     * @return physikalische Datei-Länge.
     */
    public long getFilelength()
    {
        return this.fileLength;
    }

    /**
     * Liest den Wert der Property {@code blockSize}.
     *
     * @return Länge eines Satzabschnitts.
     */
    public int getBlockSize()
    {
        return this.blockSize;
    }

    //------------------------------------------------IllegalFileLengthMessage--
    //--Message-----------------------------------------------------------------

    /**
     * Argumente zur Formatierung des Meldungs-Textes.
     *
     * @return Argumente zur Formatierung des Meldungs-Textes. <p>Index 0:
     * physikalische Datei-Länge<br>Index 1: Länge eines Satzabschnitts</p>
     */
    public Object[] getFormatArguments(final Locale locale)
    {
        return new Object[] {
            new Long(this.getFilelength()),
            new Integer(this.getBlockSize())
        };
    }

    /**
     * Formatierter Standard-Text der Meldung.
     *
     * @param locale zu verwendende Lokalisierung.
     *
     * @return {@code "Die Datei-Länge {0, number} ist inkompatible zu einer Block-Größe von {1, number}."}
     */
    public String getText(final Locale locale)
    {
        return IllegalFileLengthMessageBundle.
            getIllegalFileLengthMessage(locale).
            format(this.getFormatArguments(locale));

    }

    //-----------------------------------------------------------------Message--
}

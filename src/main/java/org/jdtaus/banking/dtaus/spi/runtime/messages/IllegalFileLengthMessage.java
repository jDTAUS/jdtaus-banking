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
 * Fehler-Meldung für ungültige Datei-Längen.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class IllegalFileLengthMessage extends AbstractErrorMessage {
    
    //--Konstruktoren-----------------------------------------------------------
    
    /**
     * Erzeugt eine neue {@code IllegalFileLengthMessage} mit Angaben zur
     * ungültigen Dateilänge.
     *
     * @param fileLength Datei-Länge der physikalischen Datei.
     * @param blockSize Grösse eines Satzabschnitts.
     *
     * @throws PhysicalFileError {@code if(isErrorsEnabled())}
     */
    public IllegalFileLengthMessage(final long fileLength,
        final int blockSize) throws PhysicalFileError {
        
        super();
        this.fileLength = fileLength;
        this.blockSize = blockSize;
        if(AbstractErrorMessage.isErrorsEnabled()) {
            throw new PhysicalFileError(this);
        }
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
     * @throws NullPointerException {@code if(messages == null)}
     */
    public static IllegalFileLengthMessage[] getMessages(
        final Message[] messages) {
        
        if(messages == null) {
            throw new NullPointerException("messages");
        }
        
        final int numMessages = messages.length;
        final Collection ret = numMessages == 0 ?
            Collections.EMPTY_LIST : new LinkedList();
        
        for(int i = numMessages - 1; i >= 0; i--) {
            if(messages[i].getClass() == IllegalFileLengthMessage.class) {
                ret.add(messages[i]);
            }
        }
        
        return (IllegalFileLengthMessage[]) ret.toArray(
            new IllegalFileLengthMessage[ret.size()]);
        
    }
    
    //-----------------------------------------------------------Konstruktoren--
    //--IllegalFileLengthMessage------------------------------------------------
    
    /**
     * Wert der Property {@code <fileLength>}.
     * @serial
     */
    private final long fileLength;
    
    /**
     * Wert der Property {@code <blockSize>}.
     * @serial
     */
    private final int blockSize;
    
    /**
     * Liest den Wert der Property {@code <fileLength>}.
     *
     * @return ungültige Datei-Länge der physikalischen Datei.
     */
    public long getFileLength() {
        return this.fileLength;
    }
    
    /**
     * Liest den Wert der Property {@code <blockSize>}.
     *
     * @return Größe eines Satzabschnitts.
     */
    public int getBlockSize() {
        return this.blockSize;
    }
    
    //------------------------------------------------IllegalFileLengthMessage--
    //--Message-----------------------------------------------------------------
    
    public Object[] getFormatArguments() {
        return new Object[] {
            new Long(this.getFileLength()), new Integer(this.getBlockSize())
        };
    }
    
    /** {@inheritDoc} */
    public String getText(final Locale locale) {
        return IllegalFileLengthMessageBundle.
            getIllegalFileLengthMessage(locale).format(new Object[] {
            Long.valueOf(this.getFileLength()),
            Integer.valueOf(this.getBlockSize())
        });
    }
    
    //-----------------------------------------------------------------Message--
    
}

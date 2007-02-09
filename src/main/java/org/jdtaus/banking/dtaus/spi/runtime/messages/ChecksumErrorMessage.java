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
import org.jdtaus.banking.dtaus.Checksum;
import org.jdtaus.banking.dtaus.PhysicalFileError;
import org.jdtaus.common.i18n.Message;

/**
 * Fehler-Meldung für Prüfsummenfehler.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class ChecksumErrorMessage extends AbstractErrorMessage {
    
    //--Konstruktoren-----------------------------------------------------------
    
    /**
     * Erzeugt eine neue {@code ChecksumErrorMessage} Instanz.
     *
     * @param storedChecksum in einer logischen Datei gespeicherte Prüfsumme.
     * @param computedChecksum aus derselben logischen Datei berrechnete
     * Prüfsumme.
     *
     * @throws NullPointerException
     * {@code if(storedChecksum == null || computedChecksum == null)}
     * @throws IllegalArgumentException
     * {@code if(storedChecksum.equals(computedChecksum))}
     * @throws PhysicalFileError {@code if(isErrorsEnabled())}
     */
    public ChecksumErrorMessage(final Checksum storedChecksum,
        final Checksum computedChecksum) throws PhysicalFileError {
        
        if(storedChecksum == null) {
            throw new NullPointerException("storedChecksum");
        }
        if(computedChecksum == null) {
            throw new NullPointerException("computedChecksum");
        }
        if(storedChecksum.equals(computedChecksum)) {
            throw new IllegalArgumentException("storedChecksum=" +
                storedChecksum + ", computedChecksum=" + computedChecksum);
        }
        
        this.storedChecksum = storedChecksum;
        this.computedChecksum = computedChecksum;
        if(AbstractErrorMessage.isErrorsEnabled()) {
            throw new PhysicalFileError(this);
        }
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
     * @throws NullPointerException {@code if(messages == null)}
     */
    public static ChecksumErrorMessage[] getMessages(
        final Message[] messages) {
        
        if(messages == null) {
            throw new NullPointerException("messages");
        }
        
        final int numMessages = messages.length;
        final Collection ret = numMessages == 0 ?
            Collections.EMPTY_LIST : new LinkedList();
        
        for(int i = numMessages - 1; i >= 0; i--) {
            if(messages[i].getClass() == ChecksumErrorMessage.class) {
                ret.add(messages[i]);
            }
        }
        
        return (ChecksumErrorMessage[]) ret.toArray(
            new ChecksumErrorMessage[ret.size()]);
        
    }
    
    //-----------------------------------------------------------Konstruktoren--
    //--ChecksumErrorMessage----------------------------------------------------
    
    /**
     * Gespeicherte Prüfsumme.
     * @serial
     */
    private final Checksum storedChecksum;
    
    /**
     * Berechnete Prüfsumme.
     * @serial
     */
    private final Checksum computedChecksum;
    
    /**
     * Liest den Wert der Property {@code <storedChecksum>}.
     *
     * @return gespeicherte Prüfsumme.
     *
     * @post {@code getStoredChecksum() != null)}
     */
    public Checksum getStoredChecksum() {
        return this.storedChecksum;
    }
    
    /**
     * Liest den Wert der Property {@code <computedChecksum>}.
     *
     * @return berrechnete Prüfsumme.
     *
     * @post {@code getComputedChecksum() != null)}
     */
    public Checksum getComputedChecksum() {
        return this.computedChecksum;
    }
    
    //----------------------------------------------------ChecksumErrorMessage--
    //--Message-----------------------------------------------------------------
    
    public Object[] getFormatArguments() {
        return new Object[] {};
    }

    /** {@inheritDoc} */
    public String getText(final Locale locale) {
        return ChecksumErrorMessageBundle.getChecksumErrorText(locale);
    }
    
    //-----------------------------------------------------------------Message--
    
}

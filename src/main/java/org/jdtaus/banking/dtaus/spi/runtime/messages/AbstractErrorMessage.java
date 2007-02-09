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
import org.jdtaus.core.container.ContextError;
import org.jdtaus.core.container.ContextFactory;
import org.jdtaus.core.i18n.Message;

/**
 * Basis-Klasse für DTAUS Fehler-Meldungen.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public abstract class AbstractErrorMessage extends Message {
    
    //--Konstanten--------------------------------------------------------------
    
    /** Context-Schlüssel für Property {@code <fatal>}. */
    private static final String CTX_ERRORS_ENABLED =
        AbstractErrorMessage.class.getName() + ".errorsEnabled";
    
    //--------------------------------------------------------------Konstanten--
    //--Konstruktoren-----------------------------------------------------------
    
    /** Erzeugt eine neue {@code AbstractErrorMessage}. */
    protected AbstractErrorMessage() {
        super();
    }
    
    /**
     * Zugriff auf {@code AbstractErrorMessage} Instanzen.
     *
     * @param messages Meldungen aus denen alle Meldungen des Typs
     * {@code AbstractErrorMessage} ermittelt werden sollen.
     *
     * @return alle in {@code messages} enthaltenen Meldungen dieses Typs oder
     * ein leeres Array, wenn keine {@code AbstractErrorMessage} Meldungen in
     * {@code messages} vorhanden sind.
     *
     * @throws NullPointerException {@code if(messages == null)}
     */
    public static AbstractErrorMessage[] getErrorMessages(
        final Message[] messages) {
        
        if(messages == null) {
            throw new NullPointerException("messages");
        }
        
        final int numMessages = messages.length;
        final Collection ret = numMessages == 0 ?
            Collections.EMPTY_LIST : new LinkedList();
        
        for(int i = numMessages - 1; i >= 0; i--) {
            if(messages[i] instanceof AbstractErrorMessage) {
                ret.add(messages[i]);
            }
        }
        
        return (AbstractErrorMessage[]) ret.toArray(
            new AbstractErrorMessage[ret.size()]);
        
    }
    
    //-----------------------------------------------------------Konstruktoren--
    //--AbstractErrorMessage----------------------------------------------------
    
    /**
     * Auskunft ob Laufzeitfehler aktiviert sind.
     *
     * @return {@code true} wenn die Erzeugung einer
     * {@code AbstractErrorMessage} im aktuellen Kontext zu einem Laufzeitfehler
     * führt (standard); {@code false} wenn nicht.
     *
     * @throws ContextError wenn der aktuelle Kontext nicht verfügbar ist.
     */
    public static boolean isErrorsEnabled() throws ContextError {
        Boolean fatal = (Boolean) ContextFactory.getContext().
            getAttribute(AbstractErrorMessage.CTX_ERRORS_ENABLED);
        
        if(fatal == null) {
            // Standard-Wert initialisieren.
            AbstractErrorMessage.setErrorsEnabled(true);
            fatal = Boolean.TRUE;
        }
        
        return fatal.booleanValue();
    }
    
    /**
     * Aktiviert oder unterdrückt Laufzeitfehler.
     *
     * @param enabled {@code true} wenn die Erzeugung einer
     * {@code AbstractErrorMessage} im aktuellen Kontext zu einem Laufzeitfehler
     * führen soll (standard); {@code false} wenn nicht.
     *
     * @throws ContextError wenn der aktuelle Kontext nicht verfügbar ist.
     */
    public static void setErrorsEnabled(
        final boolean enabled) throws ContextError {
        
        ContextFactory.getContext().setAttribute(
            AbstractErrorMessage.CTX_ERRORS_ENABLED, enabled ?
                Boolean.TRUE : Boolean.FALSE);
        
    }
    
    //----------------------------------------------------AbstractErrorMessage--
    
}

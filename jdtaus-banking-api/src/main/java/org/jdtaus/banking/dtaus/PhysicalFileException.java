/*
 *  jDTAUS Banking API
 *  Copyright (c) 2005 Christian Schulte
 *
 *  Christian Schulte, Haldener Strasse 72, 58095 Hagen, Germany
 *  <schulte2005@users.sourceforge.net> (+49 2331 3543887)
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
package org.jdtaus.banking.dtaus;

import java.util.Locale;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.text.Message;

/**
 * Gets thrown by methods prepared to handle invalid files.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class PhysicalFileException extends Exception
{

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = -8624765386920924529L;

    /** Empty {@code Message} array. */
    private static final Message[] NO_MESSAGES =
    {
    };

    /**
     * Messages describing the exception.
     * @serial
     */
    private final Message[] messages;

    /**
     * Creates a new {@code PhysicalFileException} instance taking an array of messages describing the exception.
     *
     * @param messages array of messages describing the exception or {@code null} if no information is available.
     */
    public PhysicalFileException( final Message[] messages )
    {
        super();
        this.messages = messages == null ? NO_MESSAGES : messages;
    }

    /**
     * Getter for property {@code messages}.
     *
     * @return Messages describing the exception or an empty array if no information is available.
     */
    public final Message[] getMessages()
    {
        return this.messages;
    }

    /**
     * Returns the message of the exception.
     *
     * @return The message of the exception.
     */
    public String getMessage()
    {
        return this.getPhysicalFileExceptionMessage( this.getLocale() );
    }

    /**
     * Returns a string representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString()
    {
        return super.toString() + this.internalString();
    }

    /**
     * Creates a string representing the properties of the instance.
     *
     * @return A string representing the properties of the instance.
     */
    private String internalString()
    {
        final StringBuffer buf = new StringBuffer( 200 ).append( '{' );
        final Message[] msgs = this.getMessages();
        for ( int i = 0; i < msgs.length; i++ )
        {
            buf.append( "[" ).append( i ).append( "]=" ).append( msgs[i].getText( Locale.getDefault() ) );
            if ( i + 1 < msgs.length )
            {
                buf.append( ", " );
            }
        }

        return buf.append( '}' ).toString();
    }

    //--Dependencies------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the configured <code>Locale</code> implementation.
     *
     * @return The configured <code>Locale</code> implementation.
     */
    private Locale getLocale()
    {
        return (Locale) ContainerFactory.getContainer().
            getDependency( this, "Locale" );

    }

// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>physicalFileException</code>.
     * <blockquote><pre>Datei-Fehler.</pre></blockquote>
     * <blockquote><pre>File error.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     *
     * @return the text of message <code>physicalFileException</code>.
     */
    private String getPhysicalFileExceptionMessage( final Locale locale )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "physicalFileException", locale, null );

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

/*
 *  jDTAUS Banking Messages
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
package org.jdtaus.banking.messages;

import java.util.Locale;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.text.Message;

/**
 * Message stating that a file is being checksummed.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public final class ChecksumsFileMessage extends Message
{
    //--Contstants--------------------------------------------------------------

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = 1362055156122954497L;

    //---------------------------------------------------------------Constants--
    //--Message-----------------------------------------------------------------

    /** Empty array. */
    private static final Object[] ARGUMENTS = {};

    /**
     * {@inheritDoc}
     *
     * @return an empty array, since the message has no arguments.
     */
    public Object[] getFormatArguments( final Locale locale )
    {
        return ARGUMENTS;
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * <blockquote><pre>
     * Computes checksum.
     * </pre></blockquote>
     */
    public String getText( final Locale locale )
    {
        return this.getChecksummingFileMessage( locale );
    }

    //-----------------------------------------------------------------Message--
    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>checksummingFile</code>.
     * <blockquote><pre>Berechnet Prüfsumme.</pre></blockquote>
     * <blockquote><pre>Computes checksum.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     *
     * @return the text of message <code>checksummingFile</code>.
     */
    private String getChecksummingFileMessage( final Locale locale )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "checksummingFile", locale, null );

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

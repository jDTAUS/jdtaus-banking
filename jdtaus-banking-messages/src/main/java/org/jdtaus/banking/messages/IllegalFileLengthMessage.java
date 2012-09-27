/*
 *  jDTAUS Banking Messages
 *  Copyright (C) 2005 Christian Schulte
 *  <cs@schulte.it>
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
 * Message stating that a file has an invalid length.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public final class IllegalFileLengthMessage extends Message
{

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = 961185282869701368L;

    /**
     * The length of a file incompatible to {@code blockSize}.
     * @serial
     */
    private final long fileLength;

    /**
     * The length of one block in byte.
     * @serial
     */
    private final int blockSize;

    /**
     * Creates a new {@code IllegalFileLengthMessage} instance taking the length of a file incompatible to a given block
     * size.
     *
     * @param fileLength Length of a file incompatible to {@code blockSize}.
     * @param blockSize Length of one block in byte.
     *
     * @throws IllegalArgumentException if either {@code fileLength} or {@code blockSize} is negative, or if
     * {@code fileLength % blockSize} equals {@code 0}.
     */
    public IllegalFileLengthMessage( final long fileLength, final int blockSize )
    {
        if ( fileLength < 0 )
        {
            throw new IllegalArgumentException( Long.toString( fileLength ) );
        }
        if ( blockSize <= 0 )
        {
            throw new IllegalArgumentException( Integer.toString( blockSize ) );
        }
        if ( fileLength != 0 && fileLength % blockSize == 0 )
        {
            throw new IllegalArgumentException( Long.toString( fileLength % blockSize ) );
        }

        this.fileLength = fileLength;
        this.blockSize = blockSize;
    }

    /**
     * {@inheritDoc}
     *
     * @return the length of the file and the incompatible block size.
     * <ul>
     * <li>[0]: the length of the file incompatible to {@code blockSize}.</li>
     * <li>[1]: the length of one block in byte.</li>
     * </ul>
     */
    public Object[] getFormatArguments( final Locale locale )
    {
        return new Object[]
            {
                new Long( this.fileLength ), new Integer( this.blockSize )
            };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * <blockquote><pre>
     * The length of the file ({0, number}) is incompatible to the blocksize {1,number}.
     * </pre></blockquote>
     */
    public String getText( final Locale locale )
    {
        return this.getIllegalFileLengthMessage( locale, new Long( this.fileLength ), new Long( this.blockSize ) );
    }

    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>illegalFileLength</code>.
     * <blockquote><pre>Die Datei-Länge {0,number} ist inkompatible zu einer Block-Größe von {1,number}.</pre></blockquote>
     * <blockquote><pre>The length of the file ({0, number}) is incompatible to the blocksize {1,number}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param len format argument.
     * @param blk format argument.
     *
     * @return the text of message <code>illegalFileLength</code>.
     */
    private String getIllegalFileLengthMessage( final Locale locale,
            final java.lang.Number len,
            final java.lang.Number blk )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "illegalFileLength", locale,
                new Object[]
                {
                    len,
                    blk
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

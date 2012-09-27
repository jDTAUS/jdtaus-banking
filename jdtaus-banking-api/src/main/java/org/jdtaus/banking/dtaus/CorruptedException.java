/*
 *  jDTAUS Banking API
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
package org.jdtaus.banking.dtaus;

import java.util.Locale;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ImplementationException;

/**
 * Gets thrown for any unexpected defects detected at runtime.
 * <p>Applications should not depend on this exception for theire correctness. It is thrown whenever an unexpected
 * situation is encountered. <i>{@code CorruptedException} should be used only to detect bugs.</i></p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 *
 * @see PhysicalFileFactory
 * @see PhysicalFile
 * @see LogicalFile
 */
public class CorruptedException extends ImplementationException
{

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = 4974992290184417024L;

    /**
     * Absolute file position at which a {@code CorruptedException} is caused.
     * @serial
     */
    private final long position;

    /**
     * Creates a new {@code CorruptedException} taking the absolute position to the defect.
     *
     * @param implementation meta-data describing the implementation causing the exception to be thrown.
     * @param position absolute position at which the file is defect.
     */
    public CorruptedException( final Implementation implementation, final long position )
    {
        super( implementation );
        this.position = position;
    }

    /**
     * Gets the absolute file position causing this exception to be thrown.
     *
     * @return The absolute file position causing this exception to be thrown or {@code null}.
     */
    public long getPosition()
    {
        return this.position;
    }

    /**
     * Returns the message of the exception.
     *
     * @return The message of the exception.
     */
    public String getMessage()
    {
        return this.getCorruptedExceptionMessage( this.getLocale(), new Long( this.position ) );
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
     * Gets the text of message <code>corruptedException</code>.
     * <blockquote><pre>Die physikalische Datei ist ab Position {0,number} defekt.</pre></blockquote>
     * <blockquote><pre>The physical file got corrupted at position {0,number}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param pos format argument.
     *
     * @return the text of message <code>corruptedException</code>.
     */
    private String getCorruptedExceptionMessage( final Locale locale,
            final java.lang.Number pos )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "corruptedException", locale,
                new Object[]
                {
                    pos
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

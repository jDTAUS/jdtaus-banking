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

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.jdtaus.core.io.FileOperations;

/**
 * Factory for {@code PhysicalFile} instances.
 * <p>Example: Getting the jDTAUS Banking SPI implementation.<br/><pre>
 * PhysicalFileFactory factory =
 *     (PhysicalFileFactory) ContainerFactory.getContainer().
 *     getObject( PhysicalFileFactory.class );
 * </pre></p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 *
 * @see org.jdtaus.core.container.Container
 */
public interface PhysicalFileFactory
{

    /** Constant for the DTAUS disk format. */
    int FORMAT_DISK = 128;

    /** Constant for the DTAUS tape format. */
    int FORMAT_TAPE = 150;

    /**
     * Creates a new {@code PhysicalFile} on a given {@code File}.
     *
     * @param file The file to create a new DTAUS file with.
     * @param format Constant for the format of the new DTAUS file.
     *
     * @return An empty DTAUS file.
     *
     * @throws NullPointerException if {@code file} is {@code null}.
     * @throws IllegalArgumentException if {@code format} is neither {@code FORMAT_DISK} nor {@code FORMAT_TAPE}.
     * @throws IOException if creating a new DTAUS file fails.
     *
     * @see #FORMAT_DISK
     * @see #FORMAT_TAPE
     * @see PhysicalFile#commit()
     */
    PhysicalFile createPhysicalFile( File file, int format ) throws IOException;

    /**
     * Creates a new {@code PhysicalFile} on a given {@code File} taking properties to configure the implementation.
     *
     * @param file The file to create a new DTAUS file with.
     * @param format Constant for the format of the new DTAUS file.
     * @param properties Properties to be passed to implementations.
     *
     * @return An empty DTAUS file.
     *
     * @throws NullPointerException if either {@code file} or {@code properties} is {@code null}.
     * @throws IllegalArgumentException if {@code format} is neither {@code FORMAT_DISK} nor {@code FORMAT_TAPE}.
     * @throws IOException if creating a new DTAUS file fails.
     *
     * @see #FORMAT_DISK
     * @see #FORMAT_TAPE
     * @see PhysicalFile#commit()
     */
    PhysicalFile createPhysicalFile( File file, int format, Properties properties ) throws IOException;

    /**
     * Creates a new {@code PhysicalFile} on given {@code FileOperations}.
     *
     * @param ops The {@code FileOperations} to create a new DTAUS file with.
     * @param format Constant for the format of the new DTAUS file.
     *
     * @return An empty DTAUS file.
     *
     * @throws NullPointerException if {@code ops} is {@code null}.
     * @throws IllegalArgumentException if {@code format} is neither {@code FORMAT_DISK} nor {@code FORMAT_TAPE}.
     * @throws IOException if creating a new DTAUS file fails.
     *
     * @see #FORMAT_DISK
     * @see #FORMAT_TAPE
     * @see PhysicalFile#commit()
     */
    PhysicalFile createPhysicalFile( FileOperations ops, int format ) throws IOException;

    /**
     * Creates a new {@code PhysicalFile} on given {@code FileOperations} taking properties to configure the implementation.
     *
     * @param ops The {@code FileOperations} to create a new DTAUS file with.
     * @param format Constant for the format of the new DTAUS file.
     * @param properties Properties to be passed to implementations.
     *
     * @return An empty DTAUS file.
     *
     * @throws NullPointerException if either {@code ops} or {@code properties} is {@code null}.
     * @throws IllegalArgumentException if {@code format} is neither {@code FORMAT_DISK} nor {@code FORMAT_TAPE}.
     * @throws IOException if creating a new DTAUS file fails.
     *
     * @see #FORMAT_DISK
     * @see #FORMAT_TAPE
     * @see PhysicalFile#commit()
     */
    PhysicalFile createPhysicalFile( FileOperations ops, int format, Properties properties ) throws IOException;

    /**
     * Detects the format by analysing the given {@code File}.
     *
     * @param file The file to analyse.
     *
     * @return Constant for the detected format.
     *
     * @throws NullPointerException if {@code file} is {@code null}.
     * @throws PhysicalFileException if {@code file} provides a file containing errors.
     * @throws IOException if reading fails.
     *
     * @see #FORMAT_DISK
     * @see #FORMAT_TAPE
     * @see PhysicalFile#commit()
     */
    int analyse( File file ) throws PhysicalFileException, IOException;

    /**
     * Detects the format by analysing the given {@code FileOperations}.
     *
     * @param ops {@code FileOperations} to analyse.
     *
     * @return Constant for the detected format.
     *
     * @throws NullPointerException if {@code ops} is {@code null}.
     * @throws PhysicalFileException if {@code ops} provides a file containing errors.
     * @throws IOException if reading fails.
     *
     * @see #FORMAT_DISK
     * @see #FORMAT_TAPE
     * @see PhysicalFile#commit()
     */
    int analyse( FileOperations ops ) throws PhysicalFileException, IOException;

    /**
     * Reads a {@code PhysicalFile} from a given {@code File}.
     *
     * @param file The file to create a {@code PhysicalFile} instance from.
     *
     * @return New {@code PhysicalFile} instance for {@code file}.
     *
     * @throws NullPointerException if {@code file} is {@code null}.
     * @throws PhysicalFileException if {@code file} provides a file containing errors.
     * @throws IOException if reading fails.
     *
     * @see PhysicalFile#commit()
     */
    PhysicalFile getPhysicalFile( File file ) throws PhysicalFileException, IOException;

    /**
     * Reads a {@code PhysicalFile} from a given {@code File} taking properties to configure the implementation.
     *
     * @param file The file to create a {@code PhysicalFile} instance from.
     * @param properties Properties to be passed to implementations.
     *
     * @return New {@code PhysicalFile} instance for {@code file}.
     *
     * @throws NullPointerException if either {@code file} or {@code properties} is {@code null}.
     * @throws PhysicalFileException if {@code file} provides a file containing errors.
     * @throws IOException if reading fails.
     *
     * @see PhysicalFile#commit()
     */
    PhysicalFile getPhysicalFile( File file, Properties properties ) throws PhysicalFileException, IOException;

    /**
     * Reads a {@code PhysicalFile} from given {@code FileOperations}.
     *
     * @param ops {@code FileOperations} to create a {@code PhysicalFile} instance from.
     *
     * @return New {@code PhysicalFile} instance for {@code ops}.
     *
     * @throws NullPointerException if {@code ops} is {@code null}.
     * @throws PhysicalFileException if {@code ops} provides a file containing errors.
     * @throws IOException if reading fails.
     *
     * @see PhysicalFile#commit()
     */
    PhysicalFile getPhysicalFile( FileOperations ops ) throws PhysicalFileException, IOException;

    /**
     * Reads a {@code PhysicalFile} from given {@code FileOperations} taking properties to configure the implementation.
     *
     * @param ops {@code FileOperations} to create a {@code PhysicalFile} instance from.
     * @param properties properties to be passed to implementations.
     *
     * @return new {@code PhysicalFile} instance for {@code ops}.
     *
     * @throws NullPointerException if either {@code ops} or {@code properties} is {@code null}.
     * @throws PhysicalFileException if {@code ops} provides a file containing errors.
     * @throws IOException if reading fails.
     *
     * @see PhysicalFile#commit()
     */
    PhysicalFile getPhysicalFile( FileOperations ops, Properties properties ) throws PhysicalFileException, IOException;

}

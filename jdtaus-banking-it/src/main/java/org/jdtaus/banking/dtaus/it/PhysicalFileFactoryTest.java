/*
 *  jDTAUS Banking Test Suite
 *  Copyright (c) 2005 Christian Schulte
 *
 *  Christian Schulte, Haldener Strasse 72, 58095 Hagen, Germany
 *  <cs@jdtaus.org> (+49 2331 3543887)
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
package org.jdtaus.banking.dtaus.it;

import java.io.File;
import junit.framework.Assert;
import org.jdtaus.banking.dtaus.PhysicalFile;
import org.jdtaus.banking.dtaus.PhysicalFileException;
import org.jdtaus.banking.dtaus.PhysicalFileFactory;
import org.jdtaus.core.io.FileOperations;
import org.jdtaus.core.io.util.MemoryFileOperations;

/**
 * Testcase for {@code BankleitzahlenVerzeichnis} implementations.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class PhysicalFileFactoryTest
{
    //--PhysicalFileFactoryTest-------------------------------------------------

    /** Implementation to test. */
    private PhysicalFileFactory factory;

    /**
     * Gets the {@code PhysicalFileFactory} implementation tests are performed
     * with.
     *
     * @return the {@code PhysicalFileFactory} implementation tests are
     * performed with.
     */
    public PhysicalFileFactory getPhysicalFileFactory()
    {
        return this.factory;
    }

    /**
     * Sets the {@code PhysicalFileFactory} implementation tests are performed
     * with.
     *
     * @param value the {@code PhysicalFileFactory} implementation to perform
     * tests with.
     */
    public final void setPhysicalFileFactory( final PhysicalFileFactory value )
    {
        this.factory = value;
    }

    //-------------------------------------------------PhysicalFileFactoryTest--
    //--Tests-------------------------------------------------------------------

    /**
     * Tests the {@link PhysicalFileFactory#createPhysicalFile(FileOperations,int)}
     * method to handle illegal arguments correctly by throwing a corresponding
     * {@code NullPointerException} or {@code IllegalArgumentException}.
     */
    public void testCreateIllegalArguments() throws Exception
    {
        assert this.getPhysicalFileFactory() != null;

        try
        {
            this.getPhysicalFileFactory().createPhysicalFile(
                (FileOperations) null, PhysicalFileFactory.FORMAT_DISK );

        }
        catch ( NullPointerException e )
        {
        }

        try
        {
            this.getPhysicalFileFactory().createPhysicalFile(
                (File) null, PhysicalFileFactory.FORMAT_DISK );

        }
        catch ( NullPointerException e )
        {
        }

        try
        {
            this.getPhysicalFileFactory().createPhysicalFile(
                new MemoryFileOperations(), Integer.MIN_VALUE );

        }
        catch ( IllegalArgumentException e )
        {
        }


    }

    /**
     * Tests the {@link PhysicalFileFactory#createPhysicalFile(FileOperations,int)}
     * method to return a working {@code PhysicalFile} instance for the tape and
     * disk format.
     */
    public void testCreate() throws Exception
    {
        assert this.getPhysicalFileFactory() != null;

        final FileOperations diskOps = new MemoryFileOperations();
        final FileOperations tapeOps = new MemoryFileOperations();

        final PhysicalFile diskFile = this.getPhysicalFileFactory().
            createPhysicalFile( diskOps, PhysicalFileFactory.FORMAT_DISK );

        final PhysicalFile tapeFile = this.getPhysicalFileFactory().
            createPhysicalFile( tapeOps, PhysicalFileFactory.FORMAT_TAPE );

        Assert.assertNotNull( diskFile );
        Assert.assertNotNull( tapeFile );

        Assert.assertTrue( diskFile.getLogicalFileCount() == 0 );
        Assert.assertTrue( tapeFile.getLogicalFileCount() == 0 );
    }

    /**
     * Test the {@link PhysicalFileFactory#getPhysicalFile(FileOperations)}
     * method to handle illegal arguments correctly.
     */
    public void testGetPhysicalFileNull() throws Exception
    {
        assert this.getPhysicalFileFactory() != null;

        try
        {
            this.getPhysicalFileFactory().
                getPhysicalFile( (FileOperations) null );

        }
        catch ( NullPointerException e )
        {
        }

        try
        {
            this.getPhysicalFileFactory().getPhysicalFile( (File) null );
        }
        catch ( NullPointerException e )
        {
        }
    }

    /**
     * Test the {@link PhysicalFileFactory#getPhysicalFile(FileOperations)}
     * method to return en empty {@code PhysicalFile} for a given
     * {@code FileOperations} instance of no length.
     */
    public void testGetPhysicalFileNoLength() throws Exception
    {
        assert this.getPhysicalFileFactory() != null;

        final PhysicalFile pFile = this.getPhysicalFileFactory().
            getPhysicalFile( new MemoryFileOperations() );

        Assert.assertNotNull( pFile );
        Assert.assertTrue( pFile.getLogicalFileCount() == 0 );
    }

    /**
     * Test the {@link PhysicalFileFactory#analyse(FileOperations)}
     * method to handle illegal arguments correctly.
     */
    public void testAnalyseNull() throws Exception
    {
        assert this.getPhysicalFileFactory() != null;

        try
        {
            this.getPhysicalFileFactory().analyse( (FileOperations) null );
        }
        catch ( NullPointerException e )
        {
        }

        try
        {
            this.getPhysicalFileFactory().analyse( (File) null );
        }
        catch ( NullPointerException e )
        {
        }
    }

    /**
     * Test the {@link PhysicalFileFactory#analyse(FileOperations)}
     * method to correctly analyze an empty file by throwing a
     * {@code PhysicalFileExceptin}.
     */
    public void testAnalyseNoLength() throws Exception
    {
        assert this.getPhysicalFileFactory() != null;

        try
        {
            this.getPhysicalFileFactory().analyse( new MemoryFileOperations() );
            throw new AssertionError();
        }
        catch ( PhysicalFileException e )
        {
        }

    }

    //-------------------------------------------------------------------Tests--
}

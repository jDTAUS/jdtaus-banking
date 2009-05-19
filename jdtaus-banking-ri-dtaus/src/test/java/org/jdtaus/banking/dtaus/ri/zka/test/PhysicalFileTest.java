/*
 *  jDTAUS Banking RI DTAUS
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
package org.jdtaus.banking.dtaus.ri.zka.test;

import java.io.File;
import java.io.RandomAccessFile;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jdtaus.banking.Referenznummer10;
import org.jdtaus.banking.Referenznummer11;
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.LogicalFile;
import org.jdtaus.banking.dtaus.PhysicalFile;
import org.jdtaus.banking.dtaus.PhysicalFileFactory;
import org.jdtaus.banking.dtaus.Transaction;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.io.FileOperations;
import org.jdtaus.core.io.util.RandomAccessFileOperations;

/**
 * Tests für {@link org.jdtaus.common.dtaus.PhysicalFile}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class PhysicalFileTest extends TestCase
{

    public void testGetLogicalFileCount() throws Exception
    {
        final PhysicalFile file = LogicalFileTest.getDTAUSValidHeaderChecksumAndTransaction( true );
        Assert.assertTrue( file.getLogicalFileCount() == 1 );
    }

    public void testCreateReadDisk() throws Exception
    {
        final File tmp = File.createTempFile( "jdtaus", ".dta" );
        final FileOperations ops = new RandomAccessFileOperations( new RandomAccessFile(
            tmp.getAbsolutePath(), "rw" ) );

        final PhysicalFileFactory factory = (PhysicalFileFactory) ContainerFactory.getContainer().
            getObject( PhysicalFileFactory.class, "jDTAUS Banking SPI" );

        PhysicalFile pFile = factory.createPhysicalFile( ops, PhysicalFileFactory.FORMAT_DISK );
        this.createLegalFile( pFile );
        pFile = factory.getPhysicalFile( tmp );
        pFile.commit();
        if ( !tmp.delete() )
        {
            System.err.println( "Could not delete " + tmp.getAbsolutePath() );
        }
    }

    public void testCreateReadTape() throws Exception
    {
        final File tmp = File.createTempFile( "jdtaus", ".dta" );
        final FileOperations ops = new RandomAccessFileOperations( new RandomAccessFile(
            tmp.getAbsolutePath(), "rw" ) );

        final PhysicalFileFactory factory = (PhysicalFileFactory) ContainerFactory.getContainer().
            getObject( PhysicalFileFactory.class, "jDTAUS Banking SPI" );

        PhysicalFile pFile = factory.createPhysicalFile( ops, PhysicalFileFactory.FORMAT_TAPE );
        this.createLegalFile( pFile );
        pFile = factory.getPhysicalFile( tmp );
        pFile.commit();
        if ( !tmp.delete() )
        {
            System.err.println( "Could not delete " + tmp.getAbsolutePath() );
        }
    }

    public void testAddDeleteSimpleDisk() throws Exception
    {
        final File tmp = File.createTempFile( "jdtaus", ".dta" );
        final FileOperations ops = new RandomAccessFileOperations( new RandomAccessFile(
            tmp.getAbsolutePath(), "rw" ) );

        final PhysicalFileFactory factory = (PhysicalFileFactory) ContainerFactory.getContainer().
            getObject( PhysicalFileFactory.class, "jDTAUS Banking SPI" );

        final PhysicalFile pFile = factory.createPhysicalFile( ops, PhysicalFileFactory.FORMAT_DISK );
        this.testAddDeleteSimple( pFile );
        Assert.assertTrue( ops.getLength() == 0L );
        pFile.commit();
        if ( !tmp.delete() )
        {
            System.err.println( "Could not delete " + tmp.getAbsolutePath() );
        }
    }

    public void testAddDeleteSimpleTape() throws Exception
    {
        final File tmp = File.createTempFile( "jdtaus", ".dta" );
        final FileOperations ops = new RandomAccessFileOperations( new RandomAccessFile(
            tmp.getAbsolutePath(), "rw" ) );

        final PhysicalFileFactory factory = (PhysicalFileFactory) ContainerFactory.getContainer().
            getObject( PhysicalFileFactory.class, "jDTAUS Banking SPI" );

        final PhysicalFile pFile = factory.createPhysicalFile( ops, PhysicalFileFactory.FORMAT_TAPE );
        this.testAddDeleteSimple( pFile );
        Assert.assertTrue( ops.getLength() == 0L );
        pFile.commit();
        if ( !tmp.delete() )
        {
            System.err.println( "Could not delete " + tmp.getAbsolutePath() );
        }
    }

    public void testAddDeleteDisk() throws Exception
    {
        final File tmp = File.createTempFile( "jdtaus", ".dta" );
        FileOperations ops = new RandomAccessFileOperations( new RandomAccessFile( tmp.getAbsolutePath(), "rw" ) );
        final PhysicalFileFactory factory = (PhysicalFileFactory) ContainerFactory.getContainer().
            getObject( PhysicalFileFactory.class, "jDTAUS Banking SPI" );

        final PhysicalFile pFile = factory.createPhysicalFile( ops, PhysicalFileFactory.FORMAT_DISK );
        this.testAddDelete( pFile );
        Assert.assertTrue( ops.getLength() == 0L );
        pFile.commit();
        if ( !tmp.delete() )
        {
            System.err.println( "Could not delete " + tmp.getAbsolutePath() );
        }
    }

    public void testAddDeleteTape() throws Exception
    {
        final File tmp = File.createTempFile( "jdtaus", ".dta" );
        FileOperations ops = new RandomAccessFileOperations( new RandomAccessFile( tmp.getAbsolutePath(), "rw" ) );
        final PhysicalFileFactory factory = (PhysicalFileFactory) ContainerFactory.getContainer().
            getObject( PhysicalFileFactory.class, "jDTAUS Banking SPI" );

        final PhysicalFile pFile = factory.createPhysicalFile( ops, PhysicalFileFactory.FORMAT_TAPE );
        this.testAddDelete( pFile );
        Assert.assertTrue( ops.getLength() == 0L );
        pFile.commit();
        if ( !tmp.delete() )
        {
            System.err.println( "Could not delete " + tmp.getAbsolutePath() );
        }
    }

    private void createLegalFile( final PhysicalFile pFile ) throws Exception
    {
        Assert.assertTrue( pFile.getLogicalFileCount() == 0 );

        for ( int i = 10; i >= 0; i-- )
        {
            final LogicalFile lFile = pFile.addLogicalFile( LogicalFileTest.getLegalHeader() );
            final Header header = lFile.getHeader();
            Assert.assertEquals( LogicalFileTest.getLegalHeader(), header );

            for ( int j = 10; j >= 0; j-- )
            {
                final int index = lFile.addTransaction( LogicalFileTest.getLegalTransaction() );
                final Transaction transaction = lFile.getTransaction( index );
                Assert.assertEquals( LogicalFileTest.getLegalTransaction(), transaction );
            }
        }

        pFile.commit();
    }

    private void testAddDeleteSimple( final PhysicalFile pFile )
        throws Exception
    {
        Assert.assertTrue( pFile.getLogicalFileCount() == 0 );
        final Header header = LogicalFileTest.getLegalHeader();
        final LogicalFile lFile = pFile.addLogicalFile( header );
        final Transaction transaction = LogicalFileTest.getLegalTransaction();
        final int index = lFile.addTransaction( transaction );

        Assert.assertEquals( LogicalFileTest.getLegalHeader(), lFile.getHeader() );
        Assert.assertEquals( LogicalFileTest.getLegalTransaction(), lFile.getTransaction( index ) );
        Assert.assertEquals( LogicalFileTest.getLegalTransaction(), lFile.removeTransaction( 0 ) );

        pFile.removeLogicalFile( 0 );
    }

    private void testAddDelete( final PhysicalFile pFile ) throws Exception
    {
        // Test-Datei mit 10 logischen Dateien a 10 Transaktionen erstellen.
        Header h;
        Transaction tr;
        int files;
        int transactions;
        int removed;
        final int fileCount = 10;
        final int transactionCount = 10;

        try
        {
            pFile.addLogicalFile( null );
            fail( "NullPointerException not thrown" );
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            pFile.addLogicalFile( new Header() );
            fail( "IllegalArgumentException not thrown" );
        }
        catch ( IllegalArgumentException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        for ( files = 0; files < fileCount; files++ )
        {
            h = LogicalFileTest.getLegalHeader();
            h.setReference( Referenznummer10.valueOf( new Long( files ) ) );
            pFile.addLogicalFile( h );
        }

        Assert.assertTrue( pFile.getLogicalFileCount() == fileCount );
        for ( files = pFile.getLogicalFileCount() - 1; files >= 0; files-- )
        {
            final LogicalFile dtaus = pFile.getLogicalFile( files );
            final Header test = LogicalFileTest.getLegalHeader();
            test.setReference( Referenznummer10.valueOf( new Long( files ) ) );
            Assert.assertEquals( test, dtaus.getHeader() );

            for ( transactions = 0; transactions < transactionCount; transactions++ )
            {
                tr = LogicalFileTest.getLegalTransaction();
                tr.setReference( Referenznummer11.valueOf( new Long( transactions ) ) );
                dtaus.addTransaction( tr );
            }
        }

        for ( files = pFile.getLogicalFileCount() - 1; files >= 0; files-- )
        {
            final LogicalFile dtaus = pFile.getLogicalFile( files );
            final Header test = LogicalFileTest.getLegalHeader();
            test.setReference( Referenznummer10.valueOf( new Long( files ) ) );
            Assert.assertEquals( test, dtaus.getHeader() );

            for ( transactions = dtaus.getChecksum().getTransactionCount() - 1; transactions >= 0; transactions-- )
            {
                tr = dtaus.getTransaction( transactions );
                final Transaction testTransaction = LogicalFileTest.getLegalTransaction();
                testTransaction.setReference( Referenznummer11.valueOf( new Long( transactions ) ) );
                Assert.assertEquals( testTransaction, tr );
            }
        }

        // In jeder logischen Datei die erste Transaktion entfernen.
        for ( files = pFile.getLogicalFileCount() - 1; files >= 0; files-- )
        {
            final LogicalFile dtaus = pFile.getLogicalFile( files );
            final Header test = LogicalFileTest.getLegalHeader();
            test.setReference( Referenznummer10.valueOf( new Long( files ) ) );
            Assert.assertEquals( test, dtaus.getHeader() );
            dtaus.removeTransaction( 0 );
        }

        // Erste logische Datei entfernen und Inhalte der anderen Dateien prüfen.
        removed = 0;
        while ( ( files = pFile.getLogicalFileCount() ) > 0 )
        {
            pFile.removeLogicalFile( 0 );
            removed++;
            Assert.assertTrue( files - 1 == pFile.getLogicalFileCount() );
            for ( files = pFile.getLogicalFileCount() - 1; files >= 0; files-- )
            {
                final LogicalFile dtaus = pFile.getLogicalFile( files );
                final Header test = LogicalFileTest.getLegalHeader();
                test.setReference( Referenznummer10.valueOf( new Long( files + removed ) ) );
                Assert.assertEquals( test, dtaus.getHeader() );

                for ( transactions = dtaus.getChecksum().getTransactionCount() - 1; transactions >= 0; transactions-- )
                {
                    tr = dtaus.getTransaction( transactions );
                    final Transaction testTransaction = LogicalFileTest.getLegalTransaction();
                    testTransaction.setReference( Referenznummer11.valueOf( new Long( transactions + 1 ) ) );
                    Assert.assertEquals( testTransaction, tr );
                }
            }
        }
    }

}

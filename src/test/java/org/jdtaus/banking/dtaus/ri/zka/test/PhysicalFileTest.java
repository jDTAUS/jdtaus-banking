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
import org.jdtaus.banking.dtaus.test.HeaderTest;
import org.jdtaus.banking.dtaus.test.TransactionTest;
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

    //--Tests-------------------------------------------------------------------

    public void testGetLogicalFileCount() throws Exception
    {
        final PhysicalFile file = LogicalFileTest.
            getDTAUSValidHeaderChecksumAndTransaction(true);

        Assert.assertTrue(file.count() == 1);
    }

    public void testAddDeleteSimple() throws Exception
    {
        final File tmp = File.createTempFile("jdtaus", "tmp");
        final FileOperations ops = new RandomAccessFileOperations(
            new RandomAccessFile(tmp.getAbsolutePath(), "rw"));

        final PhysicalFileFactory factory =
            (PhysicalFileFactory) ContainerFactory.getContainer().
            getImplementation(PhysicalFileFactory.class,
            "jDTAUS Banking SPI");

        final PhysicalFile pFile = factory.getPhysicalFile(ops);
        Assert.assertTrue(pFile.count() == 0);

        final Header header = HeaderTest.getLegalHeader();
        final LogicalFile lFile = pFile.add(header);
        final Transaction transaction = TransactionTest.getLegalTransaction();
        lFile.createTransaction(transaction);
        System.out.println(lFile.getTransaction(0));
        lFile.removeTransaction(0);
        pFile.remove(0);
        Assert.assertTrue(ops.getLength() == 0L);

        if(!tmp.delete())
        {
            System.err.println("Could not delete " + tmp.getAbsolutePath());
        }
    }

    public void testAddDelete() throws Exception
    {
        // Test-Datei mit 10 logischen Dateien a 10 Transaktionen erstellen.
        PhysicalFile file;
        Header h;
        Transaction tr;
        int files;
        int transactions;
        int removed;
        final int fileCount = 10;
        final int transactionCount = 10;

        final File tmp = File.createTempFile("jdtaus", "tmp");
        FileOperations ops = new RandomAccessFileOperations(
            new RandomAccessFile(tmp.getAbsolutePath(), "rw"));

        PhysicalFileFactory factory = (PhysicalFileFactory) ContainerFactory.
            getContainer().getImplementation(PhysicalFileFactory.class,
            "jDTAUS Banking SPI");

        file = factory.getPhysicalFile(ops);

        try
        {
            file.add(null);
            super.fail("NullPointerException not thrown");
        }
        catch (NullPointerException e)
        {
        }

        try
        {
            file.add(HeaderTest.getIllegalHeader());
            super.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException e)
        {
        }
        catch(NullPointerException e)
        {
        }

        for(files = 0; files < fileCount; files++)
        {
            h = HeaderTest.getLegalHeader();
            h.setReference(Referenznummer10.valueOf(new Long(files)));
            file.add(h);
        }

        Assert.assertTrue(file.count() == fileCount);
        for(files = file.count() - 1; files >= 0; files--)
        {
            final LogicalFile dtaus = file.get(files);
            Assert.assertTrue(dtaus.getHeader().getReference().
                longValue() == files);

            for(transactions = 0
                ; transactions < transactionCount; transactions++)
            {

                tr = TransactionTest.getLegalTransaction();
                tr.setReference(Referenznummer11.valueOf(
                    new Long(transactions)));

                dtaus.createTransaction(tr);
            }
        }

        for(files = file.count() - 1; files >= 0; files--)
        {
            final LogicalFile dtaus = file.get(files);
            Assert.assertTrue(dtaus.getHeader().getReference().
                longValue() == files);

            for(transactions = dtaus.getChecksum().getTransactionCount() - 1
                ; transactions >= 0; transactions--)
            {

                tr = dtaus.getTransaction(transactions);
                Assert.assertTrue(tr.getReference().
                    longValue() == transactions);

            }
        }

        // In jeder logischen Datei die erste Transaktion entfernen.
        for(files = file.count() - 1; files >= 0; files--)
        {
            final LogicalFile dtaus = file.get(files);
            Assert.assertTrue(dtaus.getHeader().getReference().
                longValue() == files);

            dtaus.removeTransaction(0);
        }

        // Erste logische Datei entfernen und Inhalte der anderen Dateien
        // prüfen.
        removed = 0;
        while((files = file.count()) > 0)
        {
            file.remove(0);
            removed++;
            Assert.assertTrue(files - 1 == file.count());
            for(files = file.count() - 1
                ; files >= 0; files--)
            {

                final LogicalFile dtaus = file.get(files);
                Assert.assertTrue(dtaus.getHeader().getReference().
                    longValue() == files + removed);

                for(transactions =
                    dtaus.getChecksum().getTransactionCount() - 1
                    ; transactions >= 0; transactions--)
                {

                    tr = dtaus.getTransaction(transactions);
                    Assert.assertTrue(tr.getReference().
                        longValue() == transactions + 1);

                }
            }
        }

        Assert.assertTrue(ops.getLength() == 0L);

        if(!tmp.delete())
        {
            System.err.println("Could not delete " + tmp.getAbsolutePath());
        }
    }

    //-------------------------------------------------------------------Tests--

}

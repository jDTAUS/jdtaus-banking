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
package org.jdtaus.banking.dtaus.spi.runtime.test;

import java.util.Calendar;
import java.util.Currency;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jdtaus.banking.dtaus.Checksum;
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.LogicalFile;
import org.jdtaus.banking.dtaus.LogicalFileType;
import org.jdtaus.banking.dtaus.PhysicalFile;
import org.jdtaus.banking.dtaus.PhysicalFileException;
import org.jdtaus.banking.dtaus.PhysicalFileFactory;
import org.jdtaus.banking.dtaus.Transaction;
import org.jdtaus.banking.dtaus.test.HeaderTest;
import org.jdtaus.banking.dtaus.test.TransactionTest;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.io.MemoryFileOperations;

/**
 * jUnit tests for {@link LogicalFile} implementations.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class LogicalFileTest extends TestCase
{

    //--DTAUS getDTAUSSingleHeader()--------------------------------------------

    protected static MemoryFileOperations getMemoryFileOperations()
    {
        return new MemoryFileOperations();
    }

    protected static PhysicalFileFactory getPhysicalFileFactory()
    {
        return (PhysicalFileFactory) ContainerFactory.getContainer().
            getImplementation(PhysicalFileFactory.class,
            "jDTAUS Banking RI DTAUS");

    }

    protected static PhysicalFile getDTAUSSingleHeader() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write(LogicalFileTest.class.getResourceAsStream(
            "dtaus0_valid_header_and_checksum"));

        return factory.getPhysicalFile(ops);
    }

    protected static PhysicalFile getDTAUSUnsupportedDataHeader() throws
        Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write(LogicalFileTest.class.getResourceAsStream(
            "dtaus0_unsupported_data_header"));

        return factory.getPhysicalFile(ops);
    }

    protected static PhysicalFile getDTAUSIllegalFormatHeader()
    throws Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write(LogicalFileTest.class.getResourceAsStream(
            "dtaus0_illegal_format_header"));

        return factory.getPhysicalFile(ops);
    }

    protected static PhysicalFile getDTAUSValidHeaderAndChecksum() throws
        Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write(LogicalFileTest.class.getResourceAsStream(
            "dtaus0_valid_header_and_checksum"));

        return factory.getPhysicalFile(ops);
    }

    protected static PhysicalFile getDTAUSValidHeaderIllegalFormatChecksum()
    throws Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write(LogicalFileTest.class.getResourceAsStream(
            "dtaus0_valid_header_illegal_format_checksum"));

        return factory.getPhysicalFile(ops);
    }

    protected static PhysicalFile getDTAUSValidHeaderChecksumAndTransaction(
        boolean extensions) throws Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write(LogicalFileTest.class.getResourceAsStream(
            (extensions ? "dtaus0_valid_header_checksum_and_transaction_ext"
            : "dtaus0_valid_header_checksum_and_transaction")));

        return factory.getPhysicalFile(ops);
    }

    protected static PhysicalFile
        getDTAUSValidHeaderChecksumIllegalFormatTransaction() throws Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write(LogicalFileTest.class.getResourceAsStream(
            "dtaus0_valid_header_checksum_illegal_format_transaction"));

        return factory.getPhysicalFile(ops);
    }

    protected static PhysicalFile getEmpty() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write(LogicalFileTest.class.getResourceAsStream(
            "empty_256"));

        return factory.getPhysicalFile(ops);
    }

    protected static PhysicalFile getEmpty2() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write(LogicalFileTest.class.getResourceAsStream(
            "empty_256_2"));

        return factory.getPhysicalFile(ops);
    }

    protected static PhysicalFile getEmpty3() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write(LogicalFileTest.class.getResourceAsStream(
            "empty_256_3"));

        return factory.getPhysicalFile(ops);
    }

    protected static PhysicalFile getIllegalFileLength() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write(LogicalFileTest.class.getResourceAsStream(
            "illegal_file_length"));

        return factory.getPhysicalFile(ops);
    }

    //--------------------------------------------DTAUS getDTAUSSingleHeader()--
    //--Tests-------------------------------------------------------------------

    /**
     * Testet die {@link org.jdtaus.common.dtaus.LogicalFile#getHeader()}
     * Methode.
     */
    public void testGetHeader() throws Exception
    {
        final Calendar createCal = Calendar.getInstance();
        final Calendar executionCal = Calendar.getInstance();

        createCal.set(1980, 0, 1, 0, 0, 0);
        executionCal.set(1980, 0, 15, 0, 0, 0);
        createCal.set(Calendar.MILLISECOND, 0);
        executionCal.set(Calendar.MILLISECOND, 0);

        try
        {
            getDTAUSUnsupportedDataHeader();
            this.fail();
        }
        catch (PhysicalFileException e)
        {
            System.err.println(e.toString());
        }

        try
        {
            getDTAUSIllegalFormatHeader();
            this.fail();
        }
        catch (PhysicalFileException e)
        {
            System.err.println(e.toString());
        }

        final PhysicalFile file = getDTAUSValidHeaderAndChecksum();
        final Header h = file.get(0).getHeader();
        Assert.assertTrue(h.getCustomer().format().equals(
            "ABCDEFGHUJKI.UHZGTÄÖÜß+ ,$&"));

        Assert.assertTrue(h.getAccount().longValue() == 1111111111L);
        Assert.assertTrue(h.getBank().intValue() == 11111111);
        Assert.assertTrue(h.getBankData() == null);
        Assert.assertTrue(h.getCurrency().equals(Currency.getInstance("EUR")));

        Assert.assertTrue(h.getType().equals(LogicalFileType.LK));

        Assert.assertTrue(h.getReference().longValue() == 2222222222L);
        Assert.assertTrue(h.getSchedule().getCreateDate().equals(
            createCal.getTime()));

        Assert.assertTrue(h.getSchedule().getExecutionDate().equals(
            executionCal.getTime()));

    }

    /**
     * Testet die {@link org.jdtaus.common.dtaus.LogicalFile#setHeader(Header)}
     * Methode.
     */
    public void testSetHeader() throws Exception
    {
        try
        {
            final PhysicalFile file = getDTAUSValidHeaderAndChecksum();
            file.get(0).setHeader(HeaderTest.getIllegalHeader());
        }
        catch(IllegalArgumentException e)
        {
        }
        catch(NullPointerException e)
        {
        }

        final PhysicalFile file = getDTAUSValidHeaderAndChecksum();
        final Header legal = HeaderTest.getLegalHeader();
        file.get(0).setHeader(legal);
        Assert.assertTrue(file.get(0).getHeader().equals(legal));
    }

    /**
     * Testet die {@link org.jdtaus.common.dtaus.LogicalFile#getChecksum()}
     * Methode.
     */
    public void testGetChecksum() throws Exception
    {
        try
        {
            getDTAUSValidHeaderIllegalFormatChecksum();
            this.fail();
        }
        catch (PhysicalFileException e)
        {
            System.err.println(e.toString());
        }

        final PhysicalFile file = getDTAUSValidHeaderAndChecksum();
        LogicalFile dt = file.get(0);
        Checksum c = dt.getChecksum();
        Assert.assertTrue(c.getSumTargetAccount() == 0L);
        Assert.assertTrue(c.getSumTargetBank() == 0L);
        Assert.assertTrue(c.getSumAmount() == 0L);
        Assert.assertTrue(c.getTransactionCount() == 0);
    }

    /**
     * Testet die
     * {@link org.jdtaus.common.dtaus.LogicalFile#createTransaction(Transaction)}
     * Methode.
     */
    public void testCreateTransaction() throws Exception
    {
        final Transaction legal = TransactionTest.getLegalTransaction();

        PhysicalFile pFile = getDTAUSValidHeaderAndChecksum();
        LogicalFile lFile = pFile.get(0);

        try
        {
            lFile.createTransaction(TransactionTest.getIllegalTransaction());
            this.fail();
        }
        catch(IllegalArgumentException e)
        {
        }
        catch(NullPointerException e)
        {
        }

        lFile.createTransaction(TransactionTest.getLegalTransaction());
        Assert.assertTrue(TransactionTest.getLegalTransaction().equals(
            lFile.getTransaction(lFile.getChecksum().getTransactionCount() - 1)));

    }

    /**
     * Testet die
     * {@link org.jdtaus.common.dtaus.LogicalFile#getTransaction(int)}
     * Methode.
     */
    public void testGetTransaction() throws Exception
    {

        try
        {
            PhysicalFile file = getDTAUSValidHeaderAndChecksum();
            LogicalFile dt = file.get(0);
            dt.getTransaction(-1);
            dt.getTransaction(10000000);
            this.fail("IndexOutOfBoundsException not thrown.");
        }
        catch(IndexOutOfBoundsException e)
        {
        }

        try
        {
            PhysicalFile file =
                getDTAUSValidHeaderChecksumIllegalFormatTransaction();

            this.fail("IllegalDataException not thrown.");
        }
        catch (PhysicalFileException e)
        {
            System.err.println(e.toString());
        }

        PhysicalFile file = getDTAUSValidHeaderChecksumAndTransaction(true);
        Transaction t = file.get(0).getTransaction(0);
        Assert.assertTrue(t.getPrimaryBank().intValue() == 11111111);
        Assert.assertTrue(t.getTargetBank().intValue() == 22222222);
        Assert.assertTrue(t.getTargetAccount().longValue() == 3333333333L);
        Assert.assertTrue(t.getReference().longValue() == 44444444444L);
        /*
        Assert.assertTrue(t.getType().equals(AbstractLogicalFileTest.
            getConfiguration().getTransactionType(5,0)));
         */

        Assert.assertTrue(t.getExecutiveBank().intValue() == 55555555);
        Assert.assertTrue(t.getExecutiveAccount().longValue() == 6666666666L);
        Assert.assertTrue(t.getAmount().longValueExact() == 77777777777L);
        Assert.assertTrue(t.getTargetName().format().
            equals("ABCDEFGHIJKLMNOPQRSTUÄÖÜß$."));

        Assert.assertTrue(t.getExecutiveName().format().
            equals("ABCDEFGHIJKLMNOPQRSTUÄÖÜß$."));

        Assert.assertTrue(t.getCurrency().equals(Currency.getInstance("EUR")));

        Transaction.Description d = t.getDescription();
        Assert.assertTrue(d.getDescription(0).format().
            equals("ABCDEFGHIJKLMNOPQRSTUÄÖÜß$."));

        Assert.assertTrue(d.getDescription(1).format().
            equals("2BCDEFGHIJKLMNOPQRSTUÄÖÜß$."));

        Assert.assertTrue(d.getDescription(2).format().
            equals("3BCDEFGHIJKLMNOPQRSTUÄÖÜß$."));

        Assert.assertTrue(d.getDescription(3).format().
            equals("4BCDEFGHIJKLMNOPQRSTUÄÖÜß$."));

        Assert.assertTrue(d.getDescription(4).format().
            equals("5BCDEFGHIJKLMNOPQRSTUÄÖÜß$."));

        Assert.assertTrue(d.getDescription(5).format().
            equals("6BCDEFGHIJKLMNOPQRSTUÄÖÜß$."));

        Assert.assertTrue(d.getDescription(6).format().
            equals("7BCDEFGHIJKLMNOPQRSTUÄÖÜß$."));

        Assert.assertTrue(d.getDescription(7).format().
            equals("8BCDEFGHIJKLMNOPQRSTUÄÖÜß$."));

        Assert.assertTrue(d.getDescription(8).format().
            equals("9BCDEFGHIJKLMNOPQRSTUÄÖÜß$."));

        Assert.assertTrue(d.getDescription(9).format().
            equals("10CDEFGHIJKLMNOPQRSTUÄÖÜß$."));

        Assert.assertTrue(d.getDescription(10).format().
            equals("11CDEFGHIJKLMNOPQRSTUÄÖÜß$."));

        Assert.assertTrue(d.getDescription(11).format().
            equals("12CDEFGHIJKLMNOPQRSTUÄÖÜß$."));

        Assert.assertTrue(d.getDescription(12).format().
            equals("13CDEFGHIJKLMNOPQRSTUÄÖÜß$."));

        Assert.assertTrue(d.getDescription(13).format().
            equals("14CDEFGHIJKLMNOPQRSTUÄÖÜß$."));

        Assert.assertTrue(t.getTargetExt().format().
            equals("1BCDEFGHIJKLMNOPQRSTUÄÖÜß$."));

        Assert.assertTrue(t.getExecutiveExt().format().
            equals("15CDEFGHIJKLMNOPQRSTUÄÖÜß$."));

    }

    /**
     * Testet die
     * {@code org.jdtaus.common.dtaus.LogicalFile#setTransaction(int, Transaction)}
     * Methode.
     */
    public void testSetTransaction() throws Exception
    {
        final Transaction legal = TransactionTest.getLegalTransaction();

        try
        {
            PhysicalFile file = getDTAUSValidHeaderAndChecksum();
            LogicalFile dt = file.get(0);
            dt.setTransaction(-1, legal);
            dt.setTransaction(10000000, legal);
            this.fail("IndexOutOfBoundsException not thrown.");
        }
        catch(IndexOutOfBoundsException e)
        {
        }

        PhysicalFile file = getDTAUSValidHeaderChecksumAndTransaction(true);
        LogicalFile dt = file.get(0);
        dt.setTransaction(0, legal);
        Assert.assertTrue(legal.equals(dt.getTransaction(0)));
    }

    /**
     * Testet die {@link org.jdtaus.common.dtaus.LogicalFile#removeTransaction(int)}
     * Methode.
     */
    public void testRemoveTransaction() throws Exception
    {

        try
        {
            PhysicalFile file = getDTAUSValidHeaderAndChecksum();
            LogicalFile dt = file.get(0);
            dt.removeTransaction(-1);
            dt.removeTransaction(10000000);
            super.fail("IndexOutOfBoundsException not thrown.");
        }
        catch(IndexOutOfBoundsException e)
        {
        }

        PhysicalFile file = getDTAUSValidHeaderChecksumAndTransaction(true);
        LogicalFile dt = file.get(0);
        Transaction t = dt.getTransaction(0);
        final int oldCount = dt.getChecksum().getTransactionCount();
        Assert.assertTrue(dt.removeTransaction(0).equals(t));
        Assert.assertTrue(dt.getChecksum().getTransactionCount() == oldCount - 1);
    }

    public void testEmptyFile() throws Exception
    {
        try
        {
            getEmpty();
            this.fail();
        }
        catch(PhysicalFileException e)
        {
            System.err.println(e.getMessage());
        }
    }

    public void testEmptyFile2() throws Exception
    {
        try
        {
            getEmpty2();
            this.fail();
        }
        catch(PhysicalFileException e)
        {
            System.err.println(e.getMessage());
        }
    }

    public void testEmptyFile3() throws Exception
    {
        try
        {
            getEmpty3();
            this.fail();
        }
        catch(PhysicalFileException e)
        {
            System.err.println(e.getMessage());
        }
    }

    public void testIllegalFileLength() throws Exception
    {
        try
        {
            getIllegalFileLength();
            this.fail();
        }
        catch(PhysicalFileException e)
        {
            System.err.println(e.getMessage());
        }
    }

    //-------------------------------------------------------------------Tests--

}

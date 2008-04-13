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

import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jdtaus.banking.AlphaNumericText27;
import org.jdtaus.banking.dtaus.Checksum;
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.IllegalHeaderException;
import org.jdtaus.banking.dtaus.IllegalTransactionException;
import org.jdtaus.banking.dtaus.LogicalFile;
import org.jdtaus.banking.dtaus.LogicalFileType;
import org.jdtaus.banking.dtaus.PhysicalFile;
import org.jdtaus.banking.dtaus.PhysicalFileException;
import org.jdtaus.banking.dtaus.PhysicalFileFactory;
import org.jdtaus.banking.dtaus.Transaction;
import org.jdtaus.banking.dtaus.ri.zka.DefaultPhysicalFileFactory;
import org.jdtaus.banking.dtaus.test.HeaderTest;
import org.jdtaus.banking.dtaus.test.TransactionTest;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.io.util.MemoryFileOperations;

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
        return ( PhysicalFileFactory ) ContainerFactory.getContainer().
            getImplementation( PhysicalFileFactory.class,
                               "jDTAUS Banking SPI" );

    }

    protected static PhysicalFile getDTAUSSingleHeader() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "dtaus0_valid_header_and_checksum" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getDTAUSUnsupportedDataHeader() throws
        Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "dtaus0_unsupported_data_header" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getDTAUSIllegalFormatHeader()
        throws Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "dtaus0_illegal_format_header" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getDTAUSValidHeaderAndChecksum() throws
        Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "dtaus0_valid_header_and_checksum" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getDTAUSValidHeaderIllegalFormatChecksum()
        throws Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "dtaus0_valid_header_illegal_format_checksum" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getDTAUSValidHeaderChecksumAndTransaction(
        boolean extensions ) throws Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   ( extensions
                   ? "dtaus0_valid_header_checksum_and_transaction_ext"
                   : "dtaus0_valid_header_checksum_and_transaction" ) ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getDTAUSValidHeaderChecksumAndTransactionMissingCurrency()
        throws Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "dtaus0_valid_header_checksum_and_transaction_missing_currency" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getDTAUSValidHeaderChecksumAndTransactionMissingCurrencyValid()
        throws Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "dtaus0_valid_header_checksum_and_transaction_missing_currency_valid" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getDTAUSValidHeaderChecksumIllegalFormatTransaction()
        throws Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "dtaus0_valid_header_checksum_illegal_format_transaction" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getEmpty() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "empty_256" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getEmpty2() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "empty_256_2" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getEmpty3() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "empty_256_3" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getIllegalFileLength() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "illegal_file_length" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getIllegalFileLength2() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "illegal_file_length_2" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getLegalHeaderStandalone() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "dtaus0_legal_header_standalone" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getLegalHeaderAndTransactionButMissingChecksum()
        throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "dtaus0_legal_header_and_transaction_but_missing_checksum" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getMissingHeaderLegalTransactionAndLegalChecksum()
        throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "dtaus0_missing_header_legal_transaction_and_checksum" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getLegalChecksumStandalone() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "dtaus0_legal_checksum_standalone" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getLegalTransactionStandalone() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "dtaus0_legal_transaction_standalone" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getLegalHeaderAndChecksumOnlyWithSpacesAllowedForA10()
        throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "dtaus0_valid_header_when_spaces_allowed_for_a10_and_checksum" ) );


        final java.util.Properties properties = new java.util.Properties();
        properties.setProperty( DefaultPhysicalFileFactory.ATTRIBUTE_SPACE_CHARACTERS_ALLOWED +
                                "A10", Boolean.toString( true ) );

        return factory.getPhysicalFile( ops, properties );
    }

    protected static PhysicalFile getIllegalHeaderAndChecksumAlsoWithSpacesAllowedForA10()
        throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
                   "dtaus0_invalid_header_also_when_spaces_allowed_for_a10_and_checksum" ) );

        final java.util.Properties properties = new java.util.Properties();
        properties.setProperty( DefaultPhysicalFileFactory.ATTRIBUTE_SPACE_CHARACTERS_ALLOWED +
                                "A10", Boolean.toString( true ) );

        return factory.getPhysicalFile( ops, properties );
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

        createCal.set( 2003, 0, 1, 0, 0, 0 );
        executionCal.set( 2003, 0, 15, 0, 0, 0 );
        createCal.set( Calendar.MILLISECOND, 0 );
        executionCal.set( Calendar.MILLISECOND, 0 );

        try
        {
            getDTAUSUnsupportedDataHeader();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            System.out.println( e.toString() );
        }

        try
        {
            getDTAUSIllegalFormatHeader();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            System.out.println( e.toString() );
        }

        final PhysicalFile file = getDTAUSValidHeaderAndChecksum();
        final Header h = file.get( 0 ).getHeader();
        Assert.assertTrue( h.getCustomer().format().equals(
                           "ABCDEFGHUJKI.UHZGTÄÖÜß+ ,$&" ) );

        Assert.assertTrue( h.getAccount().longValue() == 1111111111L );
        Assert.assertTrue( h.getBank().intValue() == 11111111 );
        Assert.assertTrue( h.getBankData() == null );
        Assert.assertTrue( h.getCurrency().equals( Currency.getInstance( "EUR" ) ) );

        Assert.assertTrue( h.getType().equals( LogicalFileType.LK ) );

        Assert.assertTrue( h.getReference().longValue() == 2222222222L );
        Assert.assertEquals( h.getCreateDate(), createCal.getTime() );
        Assert.assertEquals( h.getExecutionDate(), executionCal.getTime() );

        Assert.assertEquals( h.getCreateDate(), createCal.getTime() );
        Assert.assertEquals( h.getExecutionDate(), executionCal.getTime() );
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
            file.get( 0 ).setHeader( HeaderTest.getIllegalHeader() );
        }
        catch ( IllegalArgumentException e )
        {
        }
        catch ( NullPointerException e )
        {
        }

        final PhysicalFile file = getDTAUSValidHeaderAndChecksum();
        final Header legal = HeaderTest.getLegalHeader();
        file.get( 0 ).setHeader( legal );
        Assert.assertTrue( file.get( 0 ).getHeader().equals( legal ) );
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
            fail();
        }
        catch ( PhysicalFileException e )
        {
            System.out.println( e.toString() );
        }

        final PhysicalFile file = getDTAUSValidHeaderAndChecksum();
        LogicalFile dt = file.get( 0 );
        Checksum c = dt.getChecksum();
        Assert.assertTrue( c.getSumTargetAccount() == 0L );
        Assert.assertTrue( c.getSumTargetBank() == 0L );
        Assert.assertTrue( c.getSumAmount() == 0L );
        Assert.assertTrue( c.getTransactionCount() == 0 );
    }

    /**
     * Testet die
     * {@link org.jdtaus.common.dtaus.LogicalFile#addTransaction(Transaction)}
     * Methode.
     */
    public void testAddTransaction() throws Exception
    {
        PhysicalFile pFile = getDTAUSValidHeaderAndChecksum();
        LogicalFile lFile = pFile.get( 0 );

        try
        {
            lFile.addTransaction( TransactionTest.getIllegalTransaction() );
            fail();
        }
        catch ( IllegalArgumentException e )
        {
        }
        catch ( NullPointerException e )
        {
        }

        lFile.addTransaction( TransactionTest.getLegalTransaction() );
        Assert.assertTrue( TransactionTest.getLegalTransaction().equals(
                           lFile.getTransaction( lFile.getChecksum().
                                                 getTransactionCount() - 1 ) ) );

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
            LogicalFile dt = file.get( 0 );
            dt.getTransaction( -1 );
            dt.getTransaction( 10000000 );
            this.fail( "IndexOutOfBoundsException not thrown." );
        }
        catch ( IndexOutOfBoundsException e )
        {
        }

        try
        {
            getDTAUSValidHeaderChecksumIllegalFormatTransaction();
            this.fail( "PhysicalFileException not thrown." );
        }
        catch ( PhysicalFileException e )
        {
            System.out.println( e.toString() );
        }

        PhysicalFile file = getDTAUSValidHeaderChecksumAndTransaction( true );
        Transaction t = file.get( 0 ).getTransaction( 0 );
        Assert.assertTrue( t.getPrimaryBank().intValue() == 11111111 );
        Assert.assertTrue( t.getTargetBank().intValue() == 22222222 );
        Assert.assertTrue( t.getTargetAccount().longValue() == 3333333333L );
        Assert.assertTrue( t.getReference().longValue() == 44444444444L );
        /*
        Assert.assertTrue(t.getType().equals(AbstractLogicalFileTest.
        getConfiguration().getTransactionType(5,0)));
         */

        Assert.assertTrue( t.getExecutiveBank().intValue() == 55555555 );
        Assert.assertTrue( t.getExecutiveAccount().longValue() == 6666666666L );
        Assert.assertTrue( t.getAmount().longValue() == 77777777777L ); // TODO longValueExact()
        Assert.assertTrue( t.getTargetName().format().
                           equals( "ABCDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );

        Assert.assertTrue( t.getExecutiveName().format().
                           equals( "ABCDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );

        Assert.assertTrue( t.getCurrency().equals( Currency.getInstance( "EUR" ) ) );

        AlphaNumericText27[] d = t.getDescriptions();
        Assert.assertTrue( d[0].format().equals( "ABCDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );
        Assert.assertTrue( d[1].format().equals( "2BCDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );
        Assert.assertTrue( d[2].format().equals( "3BCDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );
        Assert.assertTrue( d[3].format().equals( "4BCDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );
        Assert.assertTrue( d[4].format().equals( "5BCDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );
        Assert.assertTrue( d[5].format().equals( "6BCDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );
        Assert.assertTrue( d[6].format().equals( "7BCDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );
        Assert.assertTrue( d[7].format().equals( "8BCDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );
        Assert.assertTrue( d[8].format().equals( "9BCDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );
        Assert.assertTrue( d[9].format().equals( "10CDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );
        Assert.assertTrue( d[10].format().equals( "11CDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );
        Assert.assertTrue( d[11].format().equals( "12CDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );
        Assert.assertTrue( d[12].format().equals( "13CDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );
        Assert.assertTrue( d[13].format().equals( "14CDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );

        Assert.assertTrue( t.getTargetExt().format().
                           equals( "1BCDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );

        Assert.assertTrue( t.getExecutiveExt().format().
                           equals( "15CDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );

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
            LogicalFile dt = file.get( 0 );
            dt.setTransaction( -1, legal );
            dt.setTransaction( 10000000, legal );
            this.fail( "IndexOutOfBoundsException not thrown." );
        }
        catch ( IndexOutOfBoundsException e )
        {
        }

        PhysicalFile file = getDTAUSValidHeaderChecksumAndTransaction( true );
        LogicalFile dt = file.get( 0 );
        dt.setTransaction( 0, legal );
        Assert.assertTrue( legal.equals( dt.getTransaction( 0 ) ) );
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
            LogicalFile dt = file.get( 0 );
            dt.removeTransaction( -1 );
            dt.removeTransaction( 10000000 );
            super.fail( "IndexOutOfBoundsException not thrown." );
        }
        catch ( IndexOutOfBoundsException e )
        {
        }

        PhysicalFile file = getDTAUSValidHeaderChecksumAndTransaction( true );
        LogicalFile dt = file.get( 0 );
        Transaction t = dt.getTransaction( 0 );
        final int oldCount = dt.getChecksum().getTransactionCount();
        Assert.assertTrue( dt.removeTransaction( 0 ).equals( t ) );
        Assert.assertTrue( dt.getChecksum().getTransactionCount() == oldCount -
                           1 );
    }

    /**
     * Testet die korrekte Behandlung von Änderungen des Erstellungsdatums,
     * wenn sich durch die Datums-Änderung die Liste der gültigen Währungen
     * verändert.
     */
    public void testCurrencyConstraints() throws Exception
    {
        final PhysicalFile pFile =
            this.getDTAUSValidHeaderChecksumAndTransaction( true );

        final Header eurHeader = pFile.get( 0 ).getHeader();
        final Header demHeader = pFile.get( 0 ).getHeader();

        final Calendar cal = Calendar.getInstance();
        cal.set( Calendar.DAY_OF_MONTH, 1 );
        cal.set( Calendar.MONTH, 0 );
        cal.set( Calendar.YEAR, 2001 );

        final Date demCreateDate = cal.getTime();

        cal.set( Calendar.YEAR, 2002 );

        final Date eurCreateDate = cal.getTime();

        eurHeader.setCreateDate( eurCreateDate );
        eurHeader.setExecutionDate( null );
        demHeader.setCreateDate( demCreateDate );
        demHeader.setExecutionDate( null );

        try
        {
            pFile.get( 0 ).setHeader( demHeader );
            fail();
        }
        catch ( IllegalHeaderException e )
        {
        }

        final Transaction eurTransaction = pFile.get( 0 ).getTransaction( 0 );
        final Transaction demTransaction = pFile.get( 0 ).getTransaction( 0 );
        demTransaction.setCurrency( Currency.getInstance( "DEM" ) );

        try
        {
            pFile.get( 0 ).setTransaction( 0, demTransaction );
            fail();
        }
        catch ( IllegalTransactionException e )
        {
        }

        pFile.get( 0 ).setHeader( eurHeader );

        for ( int i = 10; i > 0; i-- )
        {
            pFile.get( 0 ).addTransaction( eurTransaction );
        }

        while ( pFile.get( 0 ).getChecksum().getTransactionCount() > 0 )
        {
            final int index = pFile.get( 0 ).getChecksum().
                getTransactionCount() - 1;

            pFile.get( 0 ).setTransaction( index, eurTransaction );
            pFile.get( 0 ).removeTransaction( index );
        }

        pFile.get( 0 ).setHeader( eurHeader );
    }

    public void testEmptyFile() throws Exception
    {
        try
        {
            getEmpty();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            System.out.println( e.toString() );
        }
    }

    public void testEmptyFile2() throws Exception
    {
        try
        {
            getEmpty2();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            System.out.println( e.toString() );
        }
    }

    public void testEmptyFile3() throws Exception
    {
        try
        {
            getEmpty3();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            System.out.println( e.toString() );
        }
    }

    public void testIllegalFileLength() throws Exception
    {
        try
        {
            getIllegalFileLength();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            System.out.println( e.toString() );
        }
    }

    public void testIllegalFileLength2() throws Exception
    {
        try
        {
            getIllegalFileLength2();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            System.out.println( e.toString() );
        }
    }

    public void testLegalHeaderStandalone() throws Exception
    {
        try
        {
            getLegalHeaderStandalone();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            System.out.println( e.toString() );
        }
    }

    public void testLegalHeaderAndTransactionButMissingChecksum() throws Exception
    {
        try
        {
            getLegalHeaderAndTransactionButMissingChecksum();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            System.out.println( e.toString() );
        }
    }

    public void testMissingHeaderLegalTransactionAndLegalChecksum() throws Exception
    {
        try
        {
            getMissingHeaderLegalTransactionAndLegalChecksum();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            System.out.println( e.toString() );
        }
    }

    public void testLegalChecksumStandalone() throws Exception
    {
        try
        {
            getLegalChecksumStandalone();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            System.out.println( e.toString() );
        }
    }

    public void testLegalTransactionStandalone() throws Exception
    {
        try
        {
            getLegalTransactionStandalone();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            System.out.println( e.toString() );
        }
    }

    public void testValidHeaderChecksumAndTransactionMissingCurrency()
        throws Exception
    {
        try
        {
            getDTAUSValidHeaderChecksumAndTransactionMissingCurrency();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            System.out.println( e.toString() );
        }
    }

    public void testValidHeaderChecksumAndTransactionMissingCurrencyValid()
        throws Exception
    {
        getDTAUSValidHeaderChecksumAndTransactionMissingCurrencyValid();
    }

    public void testLegalHeaderAndChecksumOnlyWithSpacesAllowedForA10()
        throws Exception
    {
        getLegalHeaderAndChecksumOnlyWithSpacesAllowedForA10();
    }

    public void testIllegalHeaderAndChecksumAlsoWithSpacesAllowedForA10()
        throws Exception
    {
        try
        {
            getIllegalHeaderAndChecksumAlsoWithSpacesAllowedForA10();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            System.out.println( e.toString() );
        }
    }

    //-------------------------------------------------------------------Tests--
}

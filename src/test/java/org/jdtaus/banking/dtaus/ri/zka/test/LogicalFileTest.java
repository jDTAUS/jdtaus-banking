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

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jdtaus.banking.AlphaNumericText27;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.Kontonummer;
import org.jdtaus.banking.Referenznummer10;
import org.jdtaus.banking.Referenznummer11;
import org.jdtaus.banking.Textschluessel;
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

    private static final String LEGAL_TEXT = "ABCDEFGHIJKLMNOPQRSTUÄÖÜß$.";

    public static Header getLegalHeader()
    {
        final Header legal = new Header();
        final Calendar cal = Calendar.getInstance();
        cal.set( 2005, 0, 1, 0, 0, 0 );

        legal.setAccount( Kontonummer.valueOf( new Long( 1111111111L ) ) );
        legal.setBank( Bankleitzahl.valueOf( new Integer( 22222222 ) ) );
        legal.setBankData( Bankleitzahl.valueOf( new Integer( 33333333 ) ) );
        legal.setCurrency( Currency.getInstance( "EUR" ) );
        legal.setCustomer( AlphaNumericText27.valueOf( LEGAL_TEXT ) );
        legal.setReference( Referenznummer10.valueOf( new Long( 4444444444L ) ) );
        legal.setCreateDate( cal.getTime() );
        legal.setExecutionDate( cal.getTime() );
        legal.setType( LogicalFileType.LB );

        return legal;
    }

    public static Transaction getLegalTransaction()
    {
        final Textschluessel type = new Textschluessel();
        type.setDebit( true );
        type.setExtension( 0 );
        type.setRemittance( true );
        type.setKey( 4 );
        type.setShortDescription( Locale.getDefault(), LEGAL_TEXT );
        type.setVariable( false );

        final Transaction t = new Transaction();

        t.setAmount( BigInteger.ONE );
        t.setCurrency( Currency.getInstance( "EUR" ) );
        t.setExecutiveAccount( Kontonummer.valueOf( new Long( 1111111111L ) ) );
        t.setExecutiveBank( Bankleitzahl.valueOf( new Integer( 22222222 ) ) );
        t.setExecutiveExt( AlphaNumericText27.valueOf( LEGAL_TEXT ) );
        t.setExecutiveName( AlphaNumericText27.valueOf( LEGAL_TEXT ) );
        t.setPrimaryBank( Bankleitzahl.valueOf( new Integer( 33333333 ) ) );
        t.setReference( Referenznummer11.valueOf( new Long( 44444444444L ) ) );
        t.setTargetAccount( Kontonummer.valueOf( new Long( 5555555555L ) ) );
        t.setTargetBank( Bankleitzahl.valueOf( new Integer( 66666666 ) ) );
        t.setTargetExt( AlphaNumericText27.valueOf( LEGAL_TEXT ) );
        t.setTargetName( AlphaNumericText27.valueOf( LEGAL_TEXT ) );
        t.setType( type );

        final AlphaNumericText27[] d = new AlphaNumericText27[ 14 ];
        for ( int i = d.length - 1; i >= 0; i-- )
        {
            d[i] = AlphaNumericText27.valueOf( i + LEGAL_TEXT.substring( i > 9 ? 2 : 1 ) );
        }

        t.setDescriptions( d );
        return t;
    }

    protected static MemoryFileOperations getMemoryFileOperations()
    {
        return new MemoryFileOperations();
    }

    protected static PhysicalFileFactory getPhysicalFileFactory()
    {
        return (PhysicalFileFactory) ContainerFactory.getContainer().getObject(
            PhysicalFileFactory.class, "jDTAUS Banking SPI" );

    }

    protected static PhysicalFile getDTAUSSingleHeader() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream( "dtaus0_valid_header_and_checksum" ) );
        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getDTAUSUnsupportedDataHeader() throws Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream( "dtaus0_unsupported_data_header" ) );
        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getDTAUSIllegalFormatHeader() throws Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream( "dtaus0_illegal_format_header" ) );
        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getDTAUSValidHeaderAndChecksum() throws Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream( "dtaus0_valid_header_and_checksum" ) );
        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getDTAUSValidHeaderIllegalFormatChecksum() throws Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream( "dtaus0_valid_header_illegal_format_checksum" ) );
        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getDTAUSValidHeaderChecksumAndTransaction( boolean extensions ) throws Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
            ( extensions ? "dtaus0_valid_header_checksum_and_transaction_ext"
              : "dtaus0_valid_header_checksum_and_transaction" ) ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getDTAUSValidHeaderChecksumAndTransactionMissingCurrency() throws Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
            "dtaus0_valid_header_checksum_and_transaction_missing_currency" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getDTAUSValidHeaderChecksumAndTransactionMissingCurrencyValid() throws Exception
    {

        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
            "dtaus0_valid_header_checksum_and_transaction_missing_currency_valid" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getDTAUSValidHeaderChecksumIllegalFormatTransaction() throws Exception
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

        ops.write( LogicalFileTest.class.getResourceAsStream( "empty_256" ) );
        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getEmpty2() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream( "empty_256_2" ) );
        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getEmpty3() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream( "empty_256_3" ) );
        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getIllegalFileLength() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream( "illegal_file_length" ) );
        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getIllegalFileLength2() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream( "illegal_file_length_2" ) );
        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getLegalHeaderStandalone() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream( "dtaus0_legal_header_standalone" ) );
        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getLegalHeaderAndTransactionButMissingChecksum() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
            "dtaus0_legal_header_and_transaction_but_missing_checksum" ) );

        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getMissingHeaderLegalTransactionAndLegalChecksum() throws Exception
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

        ops.write( LogicalFileTest.class.getResourceAsStream( "dtaus0_legal_checksum_standalone" ) );
        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getLegalTransactionStandalone() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream( "dtaus0_legal_transaction_standalone" ) );
        return factory.getPhysicalFile( ops );
    }

    protected static PhysicalFile getLegalHeaderAndChecksumOnlyWithSpacesAllowedForA10() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
            "dtaus0_valid_header_when_spaces_allowed_for_a10_and_checksum" ) );


        final java.util.Properties properties = new java.util.Properties();
        properties.setProperty( DefaultPhysicalFileFactory.ATTRIBUTE_SPACE_CHARACTERS_ALLOWED + "A10",
                                Boolean.toString( true ) );

        return factory.getPhysicalFile( ops, properties );
    }

    protected static PhysicalFile getIllegalHeaderAndChecksumAlsoWithSpacesAllowedForA10() throws Exception
    {
        final MemoryFileOperations ops = getMemoryFileOperations();
        final PhysicalFileFactory factory = getPhysicalFileFactory();

        ops.write( LogicalFileTest.class.getResourceAsStream(
            "dtaus0_invalid_header_also_when_spaces_allowed_for_a10_and_checksum" ) );

        final java.util.Properties properties = new java.util.Properties();
        properties.setProperty( DefaultPhysicalFileFactory.ATTRIBUTE_SPACE_CHARACTERS_ALLOWED + "A10",
                                Boolean.toString( true ) );

        return factory.getPhysicalFile( ops, properties );
    }

    /** Testet die {@link org.jdtaus.banking.dtaus.LogicalFile#getHeader()} Methode.*/
    public void testGetHeader() throws Exception
    {
        try
        {
            getDTAUSUnsupportedDataHeader();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            getDTAUSIllegalFormatHeader();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        final PhysicalFile file = getDTAUSValidHeaderAndChecksum();
        this.assertValidHeader( file.getLogicalFile( 0 ).getHeader() );
    }

    /** Testet die {@link org.jdtaus.banking.dtaus.LogicalFile#setHeader(Header)} Methode. */
    public void testSetHeader() throws Exception
    {
        try
        {
            final PhysicalFile file = getDTAUSValidHeaderAndChecksum();
            file.getLogicalFile( 0 ).setHeader( new Header() );
            throw new AssertionError();
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

        final PhysicalFile file = getDTAUSValidHeaderAndChecksum();
        final Header legal = getLegalHeader();
        file.getLogicalFile( 0 ).setHeader( legal );
        Assert.assertTrue( file.getLogicalFile( 0 ).getHeader().equals( legal ) );
    }

    /** Testet die {@link org.jdtaus.banking.dtaus.LogicalFile#getChecksum()} Methode. */
    public void testGetChecksum() throws Exception
    {
        try
        {
            getDTAUSValidHeaderIllegalFormatChecksum();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        final PhysicalFile file = getDTAUSValidHeaderAndChecksum();
        LogicalFile dt = file.getLogicalFile( 0 );
        this.assertEmptyChecksum( dt.getChecksum() );
    }

    /** Testet die {@link org.jdtaus.banking.dtaus.LogicalFile#addTransaction(Transaction)} Methode.*/
    public void testAddTransaction() throws Exception
    {
        PhysicalFile pFile = getDTAUSValidHeaderAndChecksum();
        LogicalFile lFile = pFile.getLogicalFile( 0 );

        try
        {
            lFile.addTransaction( new Transaction() );
            fail();
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

        lFile.addTransaction( getLegalTransaction() );
        Assert.assertTrue( getLegalTransaction().equals( lFile.getTransaction(
            lFile.getChecksum().getTransactionCount() - 1 ) ) );

    }

    /** Testet die {@link org.jdtaus.banking.dtaus.LogicalFile#getTransaction(int)} Methode.*/
    public void testGetTransaction() throws Exception
    {

        try
        {
            PhysicalFile file = getDTAUSValidHeaderAndChecksum();
            LogicalFile dt = file.getLogicalFile( 0 );
            dt.getTransaction( -1 );
            dt.getTransaction( 10000000 );
            fail( "IndexOutOfBoundsException not thrown." );
        }
        catch ( IndexOutOfBoundsException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            getDTAUSValidHeaderChecksumIllegalFormatTransaction();
            fail( "PhysicalFileException not thrown." );
        }
        catch ( PhysicalFileException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        PhysicalFile file = getDTAUSValidHeaderChecksumAndTransaction( true );
        this.assertValidTransaction( file.getLogicalFile( 0 ).getTransaction( 0 ) );
    }

    /** Testet die {@code org.jdtaus.banking.dtaus.LogicalFile#setTransaction(int, Transaction)} Methode. */
    public void testSetTransaction() throws Exception
    {
        final Transaction legal = getLegalTransaction();

        try
        {
            PhysicalFile file = getDTAUSValidHeaderAndChecksum();
            LogicalFile dt = file.getLogicalFile( 0 );
            dt.setTransaction( -1, legal );
            dt.setTransaction( 10000000, legal );
            fail( "IndexOutOfBoundsException not thrown." );
        }
        catch ( IndexOutOfBoundsException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        PhysicalFile file = getDTAUSValidHeaderChecksumAndTransaction( true );
        LogicalFile dt = file.getLogicalFile( 0 );
        dt.setTransaction( 0, legal );
        Assert.assertTrue( legal.equals( dt.getTransaction( 0 ) ) );
    }

    /** Testet die {@link org.jdtaus.banking.dtaus.LogicalFile#removeTransaction(int)} Methode. */
    public void testRemoveTransaction() throws Exception
    {
        try
        {
            PhysicalFile file = getDTAUSValidHeaderAndChecksum();
            LogicalFile dt = file.getLogicalFile( 0 );
            dt.removeTransaction( -1 );
            dt.removeTransaction( 10000000 );
            fail( "IndexOutOfBoundsException not thrown." );
        }
        catch ( IndexOutOfBoundsException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        PhysicalFile file = getDTAUSValidHeaderChecksumAndTransaction( true );
        LogicalFile dt = file.getLogicalFile( 0 );
        Transaction t = dt.getTransaction( 0 );
        final int oldCount = dt.getChecksum().getTransactionCount();
        Assert.assertTrue( dt.removeTransaction( 0 ).equals( t ) );
        Assert.assertTrue( dt.getChecksum().getTransactionCount() == oldCount - 1 );
    }

    /**
     * Testet die korrekte Behandlung von Änderungen des Erstellungsdatums, wenn sich durch die Datums-Änderung die
     * Liste der gültigen Währungen verändert.
     */
    public void testCurrencyConstraints() throws Exception
    {
        final PhysicalFile pFile = getDTAUSValidHeaderChecksumAndTransaction( true );
        final Header eurHeader = pFile.getLogicalFile( 0 ).getHeader();
        final Header demHeader = pFile.getLogicalFile( 0 ).getHeader();

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
            pFile.getLogicalFile( 0 ).setHeader( demHeader );
            fail();
        }
        catch ( IllegalHeaderException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        final Transaction eurTransaction = pFile.getLogicalFile( 0 ).getTransaction( 0 );
        final Transaction demTransaction = pFile.getLogicalFile( 0 ).getTransaction( 0 );
        demTransaction.setCurrency( Currency.getInstance( "DEM" ) );

        try
        {
            pFile.getLogicalFile( 0 ).setTransaction( 0, demTransaction );
            fail();
        }
        catch ( IllegalTransactionException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        pFile.getLogicalFile( 0 ).setHeader( eurHeader );

        for ( int i = 10; i > 0; i-- )
        {
            pFile.getLogicalFile( 0 ).addTransaction( eurTransaction );
        }

        while ( pFile.getLogicalFile( 0 ).getChecksum().getTransactionCount() > 0 )
        {
            final int index = pFile.getLogicalFile( 0 ).getChecksum().getTransactionCount() - 1;
            pFile.getLogicalFile( 0 ).setTransaction( index, eurTransaction );
            pFile.getLogicalFile( 0 ).removeTransaction( index );
        }

        pFile.getLogicalFile( 0 ).setHeader( eurHeader );
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
            Assert.assertNotNull( e.getMessage() );
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
            Assert.assertNotNull( e.getMessage() );
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
            Assert.assertNotNull( e.getMessage() );
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
            Assert.assertNotNull( e.getMessage() );
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
            Assert.assertNotNull( e.getMessage() );
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
            Assert.assertNotNull( e.getMessage() );
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
            Assert.assertNotNull( e.getMessage() );
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
            Assert.assertNotNull( e.getMessage() );
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
            Assert.assertNotNull( e.getMessage() );
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
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    public void testValidHeaderChecksumAndTransactionMissingCurrency() throws Exception
    {
        try
        {
            getDTAUSValidHeaderChecksumAndTransactionMissingCurrency();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    public void testValidHeaderChecksumAndTransactionMissingCurrencyValid() throws Exception
    {
        getDTAUSValidHeaderChecksumAndTransactionMissingCurrencyValid();
    }

    public void testLegalHeaderAndChecksumOnlyWithSpacesAllowedForA10() throws Exception
    {
        getLegalHeaderAndChecksumOnlyWithSpacesAllowedForA10();
    }

    public void testIllegalHeaderAndChecksumAlsoWithSpacesAllowedForA10() throws Exception
    {
        try
        {
            getIllegalHeaderAndChecksumAlsoWithSpacesAllowedForA10();
            fail();
        }
        catch ( PhysicalFileException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    private void assertValidHeader( final Header header )
    {
        final Calendar createCal = Calendar.getInstance();
        final Calendar executionCal = Calendar.getInstance();

        createCal.set( 2003, 0, 1, 0, 0, 0 );
        executionCal.set( 2003, 0, 15, 0, 0, 0 );
        createCal.set( Calendar.MILLISECOND, 0 );
        executionCal.set( Calendar.MILLISECOND, 0 );

        Assert.assertTrue( header.getCustomer().format().equals( "ABCDEFGHUJKI.UHZGTÄÖÜß+ ,$&" ) );
        Assert.assertTrue( header.getAccount().longValue() == 1111111111L );
        Assert.assertTrue( header.getBank().intValue() == 11111111 );
        Assert.assertTrue( header.getBankData() == null );
        Assert.assertTrue( header.getCurrency().equals( Currency.getInstance( "EUR" ) ) );
        Assert.assertTrue( header.getType().equals( LogicalFileType.LK ) );
        Assert.assertTrue( header.getReference().longValue() == 2222222222L );
        Assert.assertEquals( header.getCreateDate(), createCal.getTime() );
        Assert.assertEquals( header.getExecutionDate(), executionCal.getTime() );
    }

    private void assertEmptyChecksum( final Checksum checksum )
    {
        Assert.assertTrue( checksum.getSumTargetAccount() == 0L );
        Assert.assertTrue( checksum.getSumTargetBank() == 0L );
        Assert.assertTrue( checksum.getSumAmount() == 0L );
        Assert.assertTrue( checksum.getTransactionCount() == 0 );
    }

    private void assertValidTransaction( final Transaction transaction )
    {
        Assert.assertTrue( transaction.getPrimaryBank().intValue() == 11111111 );
        Assert.assertTrue( transaction.getTargetBank().intValue() == 22222222 );
        Assert.assertTrue( transaction.getTargetAccount().longValue() == 3333333333L );
        Assert.assertTrue( transaction.getReference().longValue() == 44444444444L );
        /*
        Assert.assertTrue(t.getType().equals(AbstractLogicalFileTest.
        getConfiguration().getTransactionType(5,0)));
         */

        Assert.assertTrue( transaction.getExecutiveBank().intValue() == 55555555 );
        Assert.assertTrue( transaction.getExecutiveAccount().longValue() == 6666666666L );
        Assert.assertTrue( transaction.getAmount().longValue() == 77777777777L ); // TODO longValueExact()
        Assert.assertTrue( transaction.getTargetName().format().equals( "ABCDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );
        Assert.assertTrue( transaction.getExecutiveName().format().equals( "ABCDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );
        Assert.assertTrue( transaction.getCurrency().equals( Currency.getInstance( "EUR" ) ) );
        final AlphaNumericText27[] d = transaction.getDescriptions();
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
        Assert.assertTrue( transaction.getTargetExt().format().equals( "1BCDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );
        Assert.assertTrue( transaction.getExecutiveExt().format().equals( "15CDEFGHIJKLMNOPQRSTUÄÖÜß$." ) );
    }

}

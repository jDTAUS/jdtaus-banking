/*
 *  jDTAUS Banking Test Suite
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
package org.jdtaus.banking.it;

import junit.framework.Assert;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.BankleitzahlExpirationException;
import org.jdtaus.banking.BankleitzahlenVerzeichnis;

/**
 * Testcase for {@code BankleitzahlenVerzeichnis} implementations.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class BankleitzahlenVerzeichnisTest
{

    /** Implementation to test. */
    private BankleitzahlenVerzeichnis directory;

    /**
     * Gets the {@code BankleitzahlenVerzeichnis} implementation tests are performed with.
     *
     * @return the {@code BankleitzahlenVerzeichnis} implementation tests are performed with.
     */
    public BankleitzahlenVerzeichnis getBankleitzahlenVerzeichnis()
    {
        return this.directory;
    }

    /**
     * Sets the {@code BankleitzahlenVerzeichnis} implementation tests are performed with.
     *
     * @param value the {@code BankleitzahlenVerzeichnis} implementation to perform tests with.
     */
    public final void setBankleitzahlenVerzeichnis( final BankleitzahlenVerzeichnis value )
    {
        this.directory = value;
    }

    /**
     * Tests the {@link BankleitzahlenVerzeichnis#getHeadOffice(Bankleitzahl)} method to handle {@code null} references
     * correctly by throwing a corresponding {@code NullPointerException}.
     */
    public void testGetHeadOfficeNull() throws Exception
    {
        assert this.getBankleitzahlenVerzeichnis() != null;

        try
        {
            this.getBankleitzahlenVerzeichnis().getHeadOffice( null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

    }

    /**
     * Tests the {@link BankleitzahlenVerzeichnis#getBranchOffices(Bankleitzahl)} method to handle {@code null}
     * references correctly by throwing a corresponding {@code NullPointerException}.
     */
    public void testGetBranchOfficesNull() throws Exception
    {
        assert this.getBankleitzahlenVerzeichnis() != null;

        try
        {
            this.getBankleitzahlenVerzeichnis().getBranchOffices( null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

    }

    /**
     * Tests the {@link BankleitzahlenVerzeichnis#getDateOfExpiration()} method to not return a {@code null} value.
     */
    public void testGetDateOfExpirationNull() throws Exception
    {
        assert this.getBankleitzahlenVerzeichnis() != null;

        Assert.assertNotNull( this.getBankleitzahlenVerzeichnis().getDateOfExpiration() );
        System.out.println( this.getBankleitzahlenVerzeichnis().getDateOfExpiration() );
    }

    /**
     * Tests the {@link BankleitzahlenVerzeichnis#getHeadOffice(Bankleitzahl)} and
     * {@link BankleitzahlenVerzeichnis#getBranchOffices(Bankleitzahl)} methods to throw a
     * {@code BankleitzahlExpirationException} for the expired Bankleitzahl 26264884 and 83064538.
     */
    public void testBankleitzahlExpirationException() throws Exception
    {
        assert this.getBankleitzahlenVerzeichnis() != null;

        try
        {
            this.getBankleitzahlenVerzeichnis().getHeadOffice( Bankleitzahl.valueOf( "26264884" ) );
            throw new AssertionError();
        }
        catch ( BankleitzahlExpirationException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getBankleitzahlenVerzeichnis().getHeadOffice( Bankleitzahl.valueOf( "83064538" ) );
            throw new AssertionError();
        }
        catch ( BankleitzahlExpirationException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getBankleitzahlenVerzeichnis().getBranchOffices( Bankleitzahl.valueOf( "26264884" ) );
            throw new AssertionError();
        }
        catch ( BankleitzahlExpirationException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getBankleitzahlenVerzeichnis().getBranchOffices( Bankleitzahl.valueOf( "83064538" ) );
            throw new AssertionError();
        }
        catch ( BankleitzahlExpirationException e )
        {
            Assert.assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

    }

    /**
     * Tests the {@link BankleitzahlenVerzeichnis#search(String,String,String,boolean)} method to return sane values.
     */
    public void testSearch() throws Exception
    {
        assert this.getBankleitzahlenVerzeichnis() != null;
        Assert.assertTrue( this.getBankleitzahlenVerzeichnis().search( null, null, null, true ).length >= 0 );
    }

    /**
     * Tests the {@link BankleitzahlenVerzeichnis#searchBankleitzahlInfos(String,String,String,Boolean,Boolean)} method
     * to return sane values.
     */
    public void testSearchBankleitzahlInfos() throws Exception
    {
        assert this.getBankleitzahlenVerzeichnis() != null;
        Assert.assertTrue( this.getBankleitzahlenVerzeichnis().searchBankleitzahlInfos(
            null, null, null, null, null ).length >= 0 );

    }

}

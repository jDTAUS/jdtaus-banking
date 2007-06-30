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
package org.jdtaus.banking.ri.blzdirectory.test;

import java.util.Date;
import java.util.Locale;
import org.jdtaus.banking.BankleitzahlenVerzeichnis;
import org.jdtaus.banking.it.BankleitzahlenVerzeichnisTest;
import org.jdtaus.banking.ri.blzdirectory.BundesbankBankleitzahlenVerzeichnis;
import org.jdtaus.banking.ri.blzdirectory.OutdatedDirectoryMessage;

/**
 * Tests the {@link BundesbankBankleitzahlenVerzeichnis} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class BundesbankBankleitzahlenVerzeichnisTest
    extends BankleitzahlenVerzeichnisTest
{
    //--BankleitzahlenVerzeichnisTest-------------------------------------------

    /** The implementation to test. */
    private BankleitzahlenVerzeichnis directory;

    public BankleitzahlenVerzeichnis getBankleitzahlenVerzeichnis()
    {
        if(this.directory == null)
        {
            this.directory = new BundesbankBankleitzahlenVerzeichnis();
            this.setBankleitzahlenVerzeichnis(this.directory);
        }

        return super.getBankleitzahlenVerzeichnis();
    }

    //-------------------------------------------BankleitzahlenVerzeichnisTest--
    //--Tests-------------------------------------------------------------------

    /**
     * Tests class {@link OutdatedDirectoryMessage} to throw a corresponding
     * {@code NullPointerException} for null arguments.
     */
    public void testOutdatedDirectoryMessageNull() throws Exception
    {
        try
        {
            new OutdatedDirectoryMessage(null);
            throw new AssertionError();
        }
        catch(NullPointerException e)
        {}
    }

    /**
     * Tests class {@link OutdatedDirectoryMessage} to not throw any exception
     * for non-null arguments.
     */
    public void testOutdatedDirectoryMessage() throws Exception
    {
        System.out.println(new OutdatedDirectoryMessage(
            new Date()).getText(Locale.getDefault()));

    }

    //-------------------------------------------------------------------Tests--
}

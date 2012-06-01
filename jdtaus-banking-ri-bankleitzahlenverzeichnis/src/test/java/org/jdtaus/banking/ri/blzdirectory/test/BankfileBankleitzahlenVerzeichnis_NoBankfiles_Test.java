/*
 *  jDTAUS Banking RI Bankleitzahlenverzeichnis
 *  Copyright (c) 2011 Christian Schulte
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

import java.net.URL;
import org.jdtaus.banking.ri.blzdirectory.BankfileBankleitzahlenVerzeichnis;

/**
 * Tests the {@link BankfileBankleitzahlenVerzeichnis} implementation to operate without finding resources.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $JDTAUS$
 */
public class BankfileBankleitzahlenVerzeichnis_NoBankfiles_Test extends AbstractBankfileBankleitzahlenVerzeichnisTest
{

    protected ClassLoader getClassLoader()
    {
        final ResourceLoader cl = new ResourceLoader( this.getClass().getClassLoader() );
        cl.addResources( "META-INF/jdtaus/bankfiles.properties", new URL[]
            {
                this.getClass().getResource( "no-bankfiles.properties" )
            } );

        return cl;
    }

    public void testBankleitzahlExpirationException() throws Exception
    {
        System.out.println( "Skipped due to empty directory." );
    }

}

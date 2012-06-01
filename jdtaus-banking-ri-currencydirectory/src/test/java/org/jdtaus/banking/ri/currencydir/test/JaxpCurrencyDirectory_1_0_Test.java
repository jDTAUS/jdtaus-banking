/*
 *  jDTAUS Banking RI CurrencyDirectory
 *  Copyright (C) 2005 Christian Schulte
 *  <schulte2005@users.sourceforge.net>
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
package org.jdtaus.banking.ri.currencydir.test;

import java.net.URL;
import org.jdtaus.banking.ri.currencydir.JaxpCurrencyDirectory;

/**
 * Tests the {@link JaxpCurrencyDirectory} implementation to support resources of the
 * {@code http://jdtaus.org/banking/xml/currencies} namespace.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $JDTAUS$
 */
public class JaxpCurrencyDirectory_1_0_Test extends AbstractJaxpCurrencyDirectoryTest
{

    protected ClassLoader getClassLoader()
    {
        final ResourceLoader cl = new ResourceLoader( this.getClass().getClassLoader() );
        cl.addResources( "META-INF/jdtaus/currencies.xml", new URL[]
            {
                this.getClass().getResource( "currencies-1.0.xml" )
            } );

        return cl;
    }

}

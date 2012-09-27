/*
 *  jDTAUS Banking RI CurrencyDirectory
 *  Copyright (C) 2005 Christian Schulte
 *  <cs@schulte.it>
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

import org.jdtaus.banking.ri.currencydir.JaxpCurrencyDirectory;
import org.jdtaus.banking.spi.CurrencyMapper;
import org.jdtaus.banking.spi.it.CurrencyMapperTest;

/**
 * Base tests for the {@link JaxpCurrencyDirectory} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public abstract class AbstractJaxpCurrencyDirectoryTest extends CurrencyMapperTest
{

    /** The implementation to test. */
    private CurrencyMapper mapper;

    public CurrencyMapper getCurrencyMapper()
    {
        if ( this.mapper == null )
        {
            Thread.currentThread().setContextClassLoader( this.getClassLoader() );
            this.mapper = new JaxpCurrencyDirectory();
            this.setCurrencyMapper( this.mapper );
        }

        return super.getCurrencyMapper();
    }

    protected abstract ClassLoader getClassLoader();

}

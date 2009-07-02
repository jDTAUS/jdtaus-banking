/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdtaus.banking.ri.currencydir.test;

import org.jdtaus.banking.ri.currencydir.XMLCurrencyDirectory;
import org.jdtaus.banking.spi.CurrencyMapper;
import org.jdtaus.banking.spi.it.CurrencyMapperTest;

/**
 * Base tests for the {@link XMLCurrencyDirectory} implementation.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public abstract class AbstractXMLCurrencyDirectoryTest
    extends CurrencyMapperTest
{
    //--CurrencyMapperTest------------------------------------------------------

    /** The implementation to test. */
    private CurrencyMapper mapper;

    public CurrencyMapper getCurrencyMapper()
    {
        if ( this.mapper == null )
        {
            Thread.currentThread().setContextClassLoader( this.getClassLoader() );
            this.mapper = new XMLCurrencyDirectory();
            this.setCurrencyMapper( this.mapper );
        }

        return super.getCurrencyMapper();
    }

    //------------------------------------------------------CurrencyMapperTest--
    //--AbstractXMLCurrencyDirectoryTest----------------------------------------

    protected abstract ClassLoader getClassLoader();

    //----------------------------------------AbstractXMLCurrencyDirectoryTest--
}

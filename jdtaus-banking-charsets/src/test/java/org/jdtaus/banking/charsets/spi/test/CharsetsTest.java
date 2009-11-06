/*
 *  jDTAUS Banking Charset Providers
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
package org.jdtaus.banking.charsets.spi.test;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.jdtaus.core.nio.util.Charsets;

/**
 * Tests the {@code Charsets} implementation.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class CharsetsTest extends TestCase
{
    //--Tests-------------------------------------------------------------------

    private static final String ASCII = "@[\\]{|}~";

    private static final String ISO646DE = "§ÄÖÜäöüß";

    public void testISO646DE() throws Exception
    {
        byte[] encoded = Charsets.encode( ISO646DE, "ISO646-DE" );
        String decoded = Charsets.decode( encoded, "ISO646-DE" );
        Assert.assertEquals( ISO646DE, decoded );

        encoded = Charsets.encode( ASCII, "ISO646-DE" );
        decoded = Charsets.decode( encoded, "ISO646-DE" );
        Assert.assertEquals( "????????", decoded );

        encoded = ISO646DE.getBytes( "ISO646-DE" );
        decoded = new String( encoded, "ISO646-DE" );
        Assert.assertEquals( ISO646DE, decoded );

        encoded = ASCII.getBytes( "ISO646-DE" );
        decoded = new String( encoded, "ISO646-DE" );
        Assert.assertEquals( "????????", decoded );
    }

    public void testIBM273() throws Exception
    {
        byte[] encoded = Charsets.encode( ISO646DE, "IBM273" );
        String decoded = Charsets.decode( encoded, "IBM273" );
        Assert.assertEquals( ISO646DE, decoded );

        encoded = Charsets.encode( ASCII, "IBM273" );
        decoded = Charsets.decode( encoded, "IBM273" );
        Assert.assertEquals( ASCII, decoded );

        encoded = ISO646DE.getBytes( "IBM273" );
        decoded = new String( encoded, "IBM273" );
        Assert.assertEquals( ISO646DE, decoded );

        encoded = ASCII.getBytes( "IBM273" );
        decoded = new String( encoded, "IBM273" );
        Assert.assertEquals( ASCII, decoded );
    }

    //-------------------------------------------------------------------Tests--
}

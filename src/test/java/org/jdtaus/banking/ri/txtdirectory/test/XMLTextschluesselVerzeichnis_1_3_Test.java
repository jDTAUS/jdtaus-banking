/*
 *  jDTAUS Banking RI Textschluesselverzeichnis
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
package org.jdtaus.banking.ri.txtdirectory.test;

import java.net.URL;
import org.jdtaus.banking.ri.txtdirectory.XMLTextschluesselVerzeichnis;

/**
 * Tests the {@link XMLTextschluesselVerzeichnis} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class XMLTextschluesselVerzeichnis_1_3_Test
    extends AbstractXMLTextschluesselVerzeichnisTest
{
    //--AbstractXMLTextschluesselVerzeichnisTest--------------------------------

    protected ClassLoader getClassLoader()
    {
        final ResourceLoader cl =
            new ResourceLoader( this.getClass().getClassLoader() );

        cl.addResources( "META-INF/jdtaus/textschluessel.xml",
            new URL[]
            {
                this.getClass().getResource( "textschluessel-1.3.xml" )
            } );

        return cl;
    }

    //--------------------------------AbstractXMLTextschluesselVerzeichnisTest--
}

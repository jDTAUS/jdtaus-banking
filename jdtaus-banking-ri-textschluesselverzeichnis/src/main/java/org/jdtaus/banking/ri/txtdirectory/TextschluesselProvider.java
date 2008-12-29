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
package org.jdtaus.banking.ri.txtdirectory;

import java.io.IOException;
import java.net.URL;

/**
 * Textschlüssel resource provider interface.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 *
 * @see XMLTextschluesselVerzeichnis
 */
public interface TextschluesselProvider
{
    //--TextschluesselProvider--------------------------------------------------

    /**
     * Gets URLs to XML documents holding Textschlüssel instances.
     *
     * @return URLs to XML documents holding Textschlüssel instances.
     *
     * @throws IOException if providing the resources fails.
     */
    URL[] getResources() throws IOException;

    //--------------------------------------------------TextschluesselProvider--
}

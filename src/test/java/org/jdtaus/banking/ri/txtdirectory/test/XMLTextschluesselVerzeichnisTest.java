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
package org.jdtaus.banking.ri.txtdirectory.test;

import org.jdtaus.banking.TextschluesselVerzeichnis;
import org.jdtaus.banking.it.TextschluesselVerzeichnisTest;
import org.jdtaus.banking.ri.txtdirectory.XMLTextschluesselVerzeichnis;

/**
 * Tests the {@link TextschluesselVerzeichnis} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class XMLTextschluesselVerzeichnisTest
    extends TextschluesselVerzeichnisTest
{
    //--XMLTextschluesselVerzeichnisTest----------------------------------------

    /** The implementation to test. */
    private TextschluesselVerzeichnis directory;

    public TextschluesselVerzeichnis getTextschluesselVerzeichnis()
    {
        if(this.directory == null)
        {
            this.directory = new XMLTextschluesselVerzeichnis();
            this.setTextschluesselVerzeichnis(this.directory);
        }

        return super.getTextschluesselVerzeichnis();
    }

    //----------------------------------------XMLTextschluesselVerzeichnisTest--
    //--Tests-------------------------------------------------------------------

    //-------------------------------------------------------------------Tests--
}

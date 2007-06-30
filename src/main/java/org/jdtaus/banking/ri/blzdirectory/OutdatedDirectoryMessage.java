/*
 *  jDTAUS - DTAUS fileformat.
 *  Copyright (c) 2005 Christian Schulte <cs@schulte.it>
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
package org.jdtaus.banking.ri.blzdirectory;

import java.util.Date;
import java.util.Locale;
import org.jdtaus.core.text.Message;

/**
 * Message stating that the {@code BundesbankBankleitzahlenVerzeichnis} is
 * outdated.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class OutdatedDirectoryMessage extends Message
{
    //--Constructors------------------------------------------------------------

    /**
     * Date of expiration of the directory.
     * @serial
     */
    private Date dateOfExpiration;

    /**
     * Creates a new {@code OutdatedDirectoryMessage} instance taking the
     * date of expiration.
     *
     * @param dateOfExpiration the date of expiration of the directory.
     *
     * @throws NullPointerException if {@code dateOfExpiration} is {@code null}.
     */
    public OutdatedDirectoryMessage(final Date dateOfExpiration)
    {
        if(dateOfExpiration == null)
        {
            throw new NullPointerException("dateOfExpiration");
        }

        this.dateOfExpiration = dateOfExpiration;
    }

    //------------------------------------------------------------Constructors--
    //--Message-----------------------------------------------------------------

    public Object[] getFormatArguments(final Locale locale)
    {
        return new Object[] { this.dateOfExpiration };
    }

    public String getText(final Locale locale)
    {
        return OutdatedDirectoryMessageBundle.getOutdatedDirectoryMessage(
            locale).format(this.getFormatArguments(locale));

    }

    //-----------------------------------------------------------------Message--
}

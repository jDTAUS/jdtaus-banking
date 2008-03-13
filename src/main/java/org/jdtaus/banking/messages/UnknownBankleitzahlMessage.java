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
package org.jdtaus.banking.messages;

import java.util.Locale;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.core.text.Message;

/**
 * Message stating that Bankleitzahl is unknown.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class UnknownBankleitzahlMessage extends Message
{
    //--Contstants--------------------------------------------------------------

    /** Serial version UID for backwards compatibility with 1.1.x classes. */
    private static final long serialVersionUID = -7923064314185101629L;

    //---------------------------------------------------------------Constants--
    //--Constructors------------------------------------------------------------

    /**
     * Bankleitzahl of the message.
     * @serial
     */
    private final Bankleitzahl bankCode;

    /**
     * Creates a new {@code UnknownBankleitzahlMessage} taking the unknown
     * Bankleitzahl.
     *
     * @param bankCode the unknown Bankleitzahl.
     *
     * @throws NullPointerException if {@code bankCode} is {@code null}.
     */
    public UnknownBankleitzahlMessage( final Bankleitzahl bankCode )
    {
        if ( bankCode == null )
        {
            throw new NullPointerException( "bankCode" );
        }

        this.bankCode = bankCode;
    }

    //------------------------------------------------------------Constructors--
    //--Message-----------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * @return Information regarding the unknown Bankleitzahl.
     * <ul>
     * <li>[0]: the unknown Bankleitzahl.</li>
     * </ul>
     */
    public Object[] getFormatArguments( final Locale locale )
    {
        return new Object[] {
            this.bankCode.format( Bankleitzahl.LETTER_FORMAT )
        };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * <blockquote><pre>
     * Unknown Bankleitzahl {0}.
     * </pre></blockquote>
     */
    public String getText( final Locale locale )
    {
        return UnknownBankleitzahlMessageBundle.getInstance().
            getUnknownBankleitzahlMessage( locale ).
            format( this.getFormatArguments( locale ) );

    }

    //-----------------------------------------------------------------Message--
}

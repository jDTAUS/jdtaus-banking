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
import org.jdtaus.banking.Textschluessel;
import org.jdtaus.banking.dtaus.LogicalFileType;
import org.jdtaus.core.text.Message;

/**
 * Message stating that an invalid {@code Textschluessel} cannot be used.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class TextschluesselConstraintMessage extends Message
{
    //--Constructors------------------------------------------------------------

    /**
     * The type of the logical file incompatible with {@code textschluessel}.
     * @serial
     */
    private LogicalFileType fileType;

    /**
     * The {@code Textschluessel} incompatible with {@code fileType}.
     * @serial
     */
    private Textschluessel textschluessel;

    /**
     * Creates a new {@code TextschluesselConstraintMessage} taking the
     * logical file's type and the incompatible {@code Textschluessel}.l
     *
     * @param fileType the type of the logical file causing this exception.
     * @param textschluessel the {@code Textschluessel} incompatible with
     * {@code fileType}.
     *
     * @throws NullPointerException if either {@code fileType} or
     * {@code textschluessel} is {@code null}.
     */
    public TextschluesselConstraintMessage(final LogicalFileType fileType,
        final Textschluessel textschluessel)
    {
        super();

        if(fileType == null)
        {
            throw new NullPointerException("fileType");
        }
        if(textschluessel == null)
        {
            throw new NullPointerException("textschluessel");
        }

        this.fileType = fileType;
        this.textschluessel = textschluessel;
    }


    //------------------------------------------------------------Constructors--
    //--Message-----------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * @return the DTAUS code of the file's type and the key and extension
     * of the incompatible {@code Textschluessel}.
     * <ul>
     * <li>[0]: the DTAUS code of the file's type.</li>
     * <li>[!]: the key of the incompatible {@code Textschluessel}.</li>
     * <li>[2]: the extension of the incompatible {@code Textschluessel}.</li>
     * </ul>
     */
    public Object[] getFormatArguments(final Locale locale)
    {
        return new Object[] {
            this.fileType.getCode(),
            new Integer(this.textschluessel.getKey()),
            new Integer(this.textschluessel.getExtension())
        };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * (defaults to "A logical file with label {0} cannot hold transactions with Textschlüssel {1,number,00}{2,number,000}.").
     */
    public String getText(final Locale locale)
    {
        return TextschluesselConstraintMessageBundle.
            getTextschluesselConstraintMessage(locale).
            format(this.getFormatArguments(locale));

    }

    //-----------------------------------------------------------------Message--
}

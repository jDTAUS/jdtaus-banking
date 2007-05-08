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
package org.jdtaus.banking.dtaus.ri.zka.messages;

import java.util.Locale;
import org.jdtaus.banking.Textschluessel;
import org.jdtaus.banking.dtaus.LogicalFileType;
import org.jdtaus.core.text.Message;

/**
 * Fehler-Meldung: "Eine logische Datei vom Typ {0} darf keine
 * {1,number,00}{2,number,000} Textschlüssel speichern."
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class TextschluesselConstraintMessage extends Message
{
    //--Message-----------------------------------------------------------------

    public Object[] getFormatArguments(final Locale locale)
    {
        return new Object[] {
            this.fileType.getCode(),
            new Integer(this.textschluessel.getKey()),
            new Integer(this.textschluessel.getExtension())
        };
    }

    public String getText(final Locale locale)
    {
        return TextschluesselConstraintMessageBundle.
            getTextschluesselConstraintMessage(locale).
            format(this.getFormatArguments(locale));

    }

    //-----------------------------------------------------------------Message--
    //--TextschluesselConstraintMessage-----------------------------------------

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

    /**
     * Gets the type of the logical file incompatible with
     * {@code textschluessel}.
     *
     * @return the type of the logical file incompatible with
     * {@code textschluessel}.
     */
    public LogicalFileType getFileType()
    {
        return this.fileType;
    }

    /**
     * Gets the {@code Textschluessel} incompatible with {@code fileType}.
     *
     * @return the {@code Textschluessel} incompatible with {@code fileType}.
     */
    public Textschluessel getTextschluessel()
    {
        return this.textschluessel;
    }

    //-----------------------------------------TextschluesselConstraintMessage--
}

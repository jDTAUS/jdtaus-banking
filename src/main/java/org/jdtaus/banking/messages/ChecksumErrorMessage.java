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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;
import org.jdtaus.banking.dtaus.Checksum;
import org.jdtaus.core.text.Message;

/**
 * Message stating that a checksum is incorrect.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class ChecksumErrorMessage extends Message
{
    //--Constructors------------------------------------------------------------

    /**
     * Absolute position of the file with the incorrect checksum.
     * @serial
     */
    private long position;

    /**
     * Stored checksum.
     * @serial
     */
    private Checksum storedChecksum;

    /**
     * Computed checksum.
     * @serial
     */
    private Checksum computedChecksum;

    /**
     * Creates a new {@code ChecksumErrorMessage} instance.
     *
     * @param storedChecksum the checksum stored in a file.
     * @param computedChecksum the computed checksum from the same file.
     * @param position absolute position of the file with the incorrect
     * checksum.
     *
     * @throws NullPointerException if either {@code storedChecksum} or
     * {@code computedChecksum} is {@code null}.
     * @throws IllegalArgumentException if {@code storedChecksum} is equal to
     * {@code computedChecksum} or if {@code position} is negative.
     */
    public ChecksumErrorMessage(final Checksum storedChecksum,
        final Checksum computedChecksum, final long position)
    {

        if(storedChecksum == null)
        {
            throw new NullPointerException("storedChecksum");
        }
        if(computedChecksum == null)
        {
            throw new NullPointerException("computedChecksum");
        }
        if(storedChecksum.equals(computedChecksum))
        {
            throw new IllegalArgumentException(computedChecksum.toString());
        }
        if(position < 0L)
        {
            throw new IllegalArgumentException(Long.toString(position));
        }

        this.storedChecksum = storedChecksum;
        this.computedChecksum = computedChecksum;
        this.position = position;
    }

    //------------------------------------------------------------Constructors--
    //--Message-----------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * @return Values of the properties of the stored and computed checksum.
     * <ul>
     * <li>[0]: value of property {@code sumAmount} of the stored checksum.</li>
     * <li>[1]: value of property {@code sumTargetAccount} of the stored checksum.</li>
     * <li>[2]: value of property {@code sumTargetBank} of the stored checksum.</li>
     * <li>[3]: value of property {@code transactionCount} of the stored checksum.</li>
     * <li>[4]: value of property {@code sumAmount} of the copmuted checksum.</li>
     * <li>[5]: value of property {@code sumTargetAccount} of the copmuted checksum.</li>
     * <li>[6]: value of property {@code sumTargetBank} of the copmuted checksum.</li>
     * <li>[7]: value of property {@code transactionCount} of the copmuted checksum.</li>
     * <li>[8]: absolute position of the file with the incorrect checksum.</li>
     * </ul>
     */
    public Object[] getFormatArguments(final Locale locale)
    {
        return new Object[] {
            new Long(this.storedChecksum.getSumAmount()),
            new Long(this.storedChecksum.getSumTargetAccount()),
            new Long(this.storedChecksum.getSumTargetBank()),
            new Integer(this.storedChecksum.getTransactionCount()),
            new Long(this.computedChecksum.getSumAmount()),
            new Long(this.computedChecksum.getSumTargetAccount()),
            new Long(this.computedChecksum.getSumTargetBank()),
            new Integer(this.computedChecksum.getTransactionCount()),
            new Long(this.position)
        };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * (defaults to "Analyses file.").
     */
    public String getText(final Locale locale)
    {
        return ChecksumErrorMessageBundle.getChecksumErrorMessage(locale).
            format(this.getFormatArguments(locale));

    }

    //-----------------------------------------------------------------Message--
}

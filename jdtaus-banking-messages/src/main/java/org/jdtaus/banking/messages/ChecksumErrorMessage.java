/*
 *  jDTAUS Banking Messages
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
package org.jdtaus.banking.messages;

import java.util.Locale;
import org.jdtaus.banking.dtaus.Checksum;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.text.Message;

/**
 * Message stating that a checksum is incorrect.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public final class ChecksumErrorMessage extends Message
{

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = 2983079946808628079L;

    /**
     * Absolute position of the file with the incorrect checksum.
     * @serial
     */
    private final long position;

    /**
     * Stored checksum.
     * @serial
     */
    private final Checksum storedChecksum;

    /**
     * Computed checksum.
     * @serial
     */
    private final Checksum computedChecksum;

    /**
     * Creates a new {@code ChecksumErrorMessage} instance.
     *
     * @param storedChecksum The checksum stored in a file.
     * @param computedChecksum The computed checksum from the same file.
     * @param position Absolute position of the file with the incorrect checksum.
     *
     * @throws NullPointerException if either {@code storedChecksum} or {@code computedChecksum} is {@code null}.
     * @throws IllegalArgumentException if {@code storedChecksum} is equal to {@code computedChecksum} or if
     * {@code position} is negative.
     */
    public ChecksumErrorMessage( final Checksum storedChecksum, final Checksum computedChecksum, final long position )
    {
        if ( storedChecksum == null )
        {
            throw new NullPointerException( "storedChecksum" );
        }
        if ( computedChecksum == null )
        {
            throw new NullPointerException( "computedChecksum" );
        }
        if ( storedChecksum.equals( computedChecksum ) )
        {
            throw new IllegalArgumentException( computedChecksum.toString() );
        }
        if ( position < 0L )
        {
            throw new IllegalArgumentException( Long.toString( position ) );
        }

        this.storedChecksum = storedChecksum;
        this.computedChecksum = computedChecksum;
        this.position = position;
    }

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
    public Object[] getFormatArguments( final Locale locale )
    {
        return new Object[]
            {
                new Long( this.storedChecksum.getSumAmount() ),
                new Long( this.storedChecksum.getSumTargetAccount() ),
                new Long( this.storedChecksum.getSumTargetBank() ),
                new Integer( this.storedChecksum.getTransactionCount() ),
                new Long( this.computedChecksum.getSumAmount() ),
                new Long( this.computedChecksum.getSumTargetAccount() ),
                new Long( this.computedChecksum.getSumTargetBank() ),
                new Integer( this.computedChecksum.getTransactionCount() ),
                new Long( this.position )
            };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * <blockquote><pre>
     * The checksum of the file beginning at position {0,number} is invalid.
     * </pre></blockquote>
     */
    public String getText( final Locale locale )
    {
        return this.getChecksumErrorMessage( locale, new Long( this.position ) );
    }

    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>checksumError</code>.
     * <blockquote><pre>Die Prüfsumme der an Position {0,number} beginnenden logischen Datei ist ungültig.</pre></blockquote>
     * <blockquote><pre>The checksum of the logical file beginning at position {0,number} is invalid.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param pos format argument.
     *
     * @return the text of message <code>checksumError</code>.
     */
    private String getChecksumErrorMessage( final Locale locale,
            final java.lang.Number pos )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "checksumError", locale,
                new Object[]
                {
                    pos
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

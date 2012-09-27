/*
 *  jDTAUS Banking API
 *  Copyright (C) 2005 Christian Schulte
 *  <cs@schulte.it>
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
package org.jdtaus.banking.dtaus;

import java.util.Locale;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.text.Message;

/**
 * Gets thrown whenever an illegal transaction is passed to a method expecting a legal transaction.
 * <p>Example: Catching an {@code IllegalTransactionException}<br/><blockquote>
 * <pre>
 * catch(IllegalTransactionException e)
 * {
 *     if(e.getMessages().length > 0)
 *     {
 *         <i>Fetch messages for well-known properties first (optional).</i>
 *         e.getMessages(Transaction.PROP_<i>XYZ</i>);
 *         ...
 *         <i>Fetch all remaining messages.</i>
 *         e.getMessages();
 *         ...
 *     }
 * }</pre></blockquote></p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public abstract class IllegalTransactionException extends IllegalArgumentException
{

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = 2094280705320453233L;

    /** Creates a new {@code IllegalTransactionException} instance. */
    public IllegalTransactionException()
    {
        super();
    }

    /**
     * Gets all messages describing the exception.
     *
     * @return An array of messages describing the exception or an empty array if the instance does not hold any
     * messages.
     */
    public abstract Message[] getMessages();

    /**
     * Gets messages bound to a property removing these messages from the instance.
     *
     * @param propertyName the name of a property to return any messages for.
     *
     * @return All messages bound to a property with name {@code propertyName} or an empty array if the instance does
     * not hold messages for a property with name {@code propertyName}.
     *
     * @throws NullPointerException if {@code propertyName} is {@code null}.
     */
    public abstract Message[] getMessages( final String propertyName );

    /**
     * Gets the names of all properties for which the exception holds messages.
     *
     * @return An array of the names of all properties for which the exception holds messages or an empty array if the
     * exception does not hold any message bound to a property.
     */
    public abstract String[] getPropertyNames();

    /**
     * Returns the message of the exception.
     *
     * @return The message of the exception.
     */
    public String getMessage()
    {
        return this.getIllegalTransactionMessage( this.getLocale() );
    }

    //--Dependencies------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the configured <code>Locale</code> implementation.
     *
     * @return The configured <code>Locale</code> implementation.
     */
    private Locale getLocale()
    {
        return (Locale) ContainerFactory.getContainer().
            getDependency( this, "Locale" );

    }

// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>illegalTransaction</code>.
     * <blockquote><pre>Ungültiger "C" Datensatz.</pre></blockquote>
     * <blockquote><pre>Illegal "C" record.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     *
     * @return the text of message <code>illegalTransaction</code>.
     */
    private String getIllegalTransactionMessage( final Locale locale )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "illegalTransaction", locale, null );

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

/*
 *  jDTAUS Banking SPI
 *  Copyright (C) 2005 Christian Schulte
 *  <schulte2005@users.sourceforge.net>
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
package org.jdtaus.banking.dtaus.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jdtaus.banking.dtaus.Transaction;
import org.jdtaus.core.text.Message;
import org.jdtaus.core.text.Messages;

/**
 * Gets thrown whenever an illegal transaction is passed to a method expecting a legal transaction.
 * <p>Example: Throwing an {@code IllegalTransactionException}<br/><blockquote>
 * <pre>
 * IllegalTransactionException e = new IllegalTransactionException();
 * e.addMessage(message);
 * e.addMessage(Transaction.PROP_<i>XYZ</i>, message);
 * throw e;</pre></blockquote></p>
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $JDTAUS$
 */
public class IllegalTransactionException
    extends org.jdtaus.banking.dtaus.IllegalTransactionException
{

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = -81649021330912822L;

    /** Creates a new {@code IllegalTransactionException} instance. */
    public IllegalTransactionException()
    {
        super();
    }

    /** Key to the list of messages not bound to any particular property. */
    private static final String PROP_UNSPECIFIED = "org.jdtaus.banking.dtaus.Transaction";

    /**
     * The messages describing the exception.
     * @serial
     */
    private Map messages = new HashMap();

    /**
     * Adds a message to the instance.
     *
     * @param message The message to add to the instance.
     *
     * @throws NullPointerException if {@code message} is {@code null}.
     */
    public void addMessage( final Message message )
    {
        this.addMessage( PROP_UNSPECIFIED, message );
    }

    /**
     * Adds messages to the instance.
     *
     * @param messages The messages to add to the instance.
     *
     * @throws NullPointerException if {@code messages} is {@code null}.
     */
    public final void addMessages( final Messages messages )
    {
        if ( messages == null )
        {
            throw new NullPointerException( "messages" );
        }

        for ( int i = messages.size() - 1; i >= 0; i-- )
        {
            this.addMessage( messages.getMessage( i ) );
        }
    }

    /**
     * Adds a message for a property to the instance.
     *
     * @param propertyName The name of a property {@code message} is bound to.
     * @param message The message to add to the instance.
     *
     * @throws NullPointerException if either {@code message} or {@code propertyName} is {@code null}.
     */
    public void addMessage( final String propertyName, final Message message )
    {
        if ( propertyName == null )
        {
            throw new NullPointerException( "propertyName" );
        }
        if ( message == null )
        {
            throw new NullPointerException( "message" );
        }

        List msgs = (List) this.messages.get( propertyName );
        if ( msgs == null )
        {
            msgs = new LinkedList();
            this.messages.put( propertyName, msgs );
        }

        msgs.add( message );
    }

    /**
     * Adds messages bound to a property to the instance.
     *
     * @param propertyName The name of a property {@code messages} are bound to.
     * @param messages The messages to add to the instance.
     *
     * @throws NullPointerException if either {@code messages} or {@code propertyName} is {@code null}.
     */
    public final void addMessages( final String propertyName, final Messages messages )
    {
        if ( propertyName == null )
        {
            throw new NullPointerException( "propertyName" );
        }
        if ( messages == null )
        {
            throw new NullPointerException( "messages" );
        }

        for ( int i = messages.size() - 1; i >= 0; i-- )
        {
            this.addMessage( propertyName, messages.getMessage( i ) );
        }
    }

    /**
     * Gets all messages describing the exception.
     *
     * @return An array of messages describing the exception or an empty array if the instance does not hold any
     * messages.
     */
    public Message[] getMessages()
    {
        final List col = new LinkedList();
        for ( Iterator it = this.messages.keySet().iterator(); it.hasNext(); )
        {
            final String propertyName = (String) it.next();
            col.addAll( (List) this.messages.get( propertyName ) );
        }

        return (Message[]) col.toArray( new Message[ col.size() ] );
    }

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
    public Message[] getMessages( final String propertyName )
    {
        if ( propertyName == null )
        {
            throw new NullPointerException( "propertyName" );
        }

        final List msgs = (List) this.messages.remove( propertyName );
        return msgs == null ? new Message[ 0 ] : (Message[]) msgs.toArray( new Message[ msgs.size() ] );
    }

    /**
     * Gets the names of all properties for which the exception holds messages.
     *
     * @return An array of the names of all properties for which the exception holds messages or an empty array if the
     * exception does not hold any message bound to a property.
     */
    public String[] getPropertyNames()
    {
        final List names = new ArrayList( this.messages.size() );
        for ( Iterator it = this.messages.keySet().iterator(); it.hasNext(); )
        {
            final String name = (String) it.next();
            if ( !PROP_UNSPECIFIED.equals( name ) )
            {
                names.add( name );
            }
        }

        return (String[]) names.toArray( new String[ names.size() ] );
    }

    /**
     * Creates a string representing the messages of the instance.
     *
     * @return A string representing the messages of the instance.
     */
    private String internalString()
    {
        final StringBuffer buf = new StringBuffer( 200 ).append( '{' );
        final String[] propertyNames = this.getPropertyNames();
        final List unspecifiedMsgs = (List) this.messages.get( PROP_UNSPECIFIED );

        for ( int i = 0; i < propertyNames.length; i++ )
        {
            buf.append( propertyNames[i] ).append( "={" );

            int j = 0;
            final List msgs = (List) this.messages.get( propertyNames[i] );
            for ( Iterator it = msgs.iterator(); it.hasNext(); j++ )
            {
                final Message msg = (Message) it.next();
                buf.append( "[" ).append( j ).append( "]=" ).append( msg.getText( Locale.getDefault() ) );
                if ( it.hasNext() )
                {
                    buf.append( ", " );
                }
            }

            buf.append( '}' );

            if ( i + i < propertyNames.length )
            {
                buf.append( ", " );
            }
        }

        if ( unspecifiedMsgs != null && !unspecifiedMsgs.isEmpty() )
        {
            if ( propertyNames.length > 0 )
            {
                buf.append( ", " );
            }

            buf.append( PROP_UNSPECIFIED ).append( "={" );

            int i = 0;
            for ( Iterator it = unspecifiedMsgs.iterator(); it.hasNext(); i++ )
            {
                final Message msg = (Message) it.next();
                buf.append( "[" ).append( i ).append( "]=" ).append( msg.getText( Locale.getDefault() ) );
                if ( it.hasNext() )
                {
                    buf.append( ", " );
                }
            }

            buf.append( '}' );
        }

        buf.append( '}' );
        return buf.toString();
    }

    /**
     * Returns a string representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString()
    {
        return super.toString() + '\n' + this.internalString();
    }

}

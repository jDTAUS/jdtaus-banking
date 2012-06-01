/*
 *  jDTAUS Banking RI DTAUS
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
package org.jdtaus.banking.dtaus.ri.zka;

import org.jdtaus.core.text.Message;
import org.jdtaus.core.text.Messages;

/**
 * Thread-local collections of messages.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $JDTAUS$
 */
public abstract class ThreadLocalMessages
{

    /** Maximum number of messages added to the collection. */
    private static final int MAXIMUM_MESSAGES = 100;

    /** Thread local collections of messages. */
    private static final ThreadLocal current = new ThreadLocal()
    {

        public Object initialValue()
        {
            return new Messages()
            {

                /** Number of messages added to the instance. */
                private int messageCount = 0;

                public void addMessage( final Message message )
                {
                    if ( this.messageCount + 1L <= Integer.MAX_VALUE && this.messageCount + 1 < MAXIMUM_MESSAGES )
                    {
                        this.messageCount++;
                        super.addMessage( message );
                    }
                }

                public void removeMessage( final Message message )
                {
                    if ( this.messageCount - 1 >= 0 )
                    {
                        this.messageCount--;
                        super.removeMessage( message );
                    }
                }

                public void clear()
                {
                    this.messageCount = 0;
                    super.clear();
                }

            };
        }

    };

    /** {@code ThreadLocal} {@code Boolean}. */
    private static final ThreadLocal errorsEnabled = new ThreadLocal()
    {

        protected Object initialValue()
        {
            return Boolean.TRUE;
        }

    };

    /**
     * Flag indicating that {@code CorruptedException}s are enabled.
     *
     * @return {@code true} if a {@code CorruptedException} must be thrown whenever a file error is detected;
     * {@code false} to not throw any exception when detecting a file error.
     */
    public static boolean isErrorsEnabled()
    {
        Boolean fatal = (Boolean) errorsEnabled.get();

        if ( fatal == null )
        {
            throw new IllegalStateException();
        }

        return fatal.booleanValue();
    }

    /**
     * Setter for property {@code errorsEnabled}.
     *
     * @param value {@code true} if a {@code CorruptedException} should be thrown whenever a file error is detected;
     * {@code false} to not throw any exception when detecting a file error.
     */
    public static void setErrorsEnabled( final boolean value )
    {
        errorsEnabled.set( value ? Boolean.TRUE : Boolean.FALSE );
    }

    /**
     * Gets the collection of messages stored with the current thread of execution.
     *
     * @return collection of messages stored with the current thread of execution.
     */
    public static Messages getMessages()
    {
        return (Messages) current.get();
    }

}

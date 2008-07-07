/*
 *  jDTAUS Banking RI DTAUS
 *  Copyright (c) 2005 Christian Schulte
 *
 *  Christian Schulte, Haldener Strasse 72, 58095 Hagen, Germany
 *  <cs@jdtaus.org> (+49 2331 3543887)
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
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public abstract class ThreadLocalMessages
{
    //--Constants---------------------------------------------------------------

    /** Maximum number of messages added to the collection. */
    private static final int MAXIMUM_MESSAGES = 250;

    //---------------------------------------------------------------Constants--
    //--ThreadLocalMessages-----------------------------------------------------

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
                    if ( this.messageCount + 1L <= Integer.MAX_VALUE &&
                        this.messageCount + 1 < MAXIMUM_MESSAGES )
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

    /**
     * Gets the collection of messages stored with the current thread of
     * execution.
     *
     * @return collection of messages stored with the current thread of
     * execution.
     */
    public static Messages getMessages()
    {
        return ( Messages ) current.get();
    }

    //-----------------------------------------------------ThreadLocalMessages--
}

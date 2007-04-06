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
package org.jdtaus.banking.dtaus.spi.runtime;

import org.jdtaus.core.text.Messages;

/**
 * Thread-local collections of messages.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public abstract class ThreadLocalMessages
{

    //--ThreadLocalMessages-----------------------------------------------------

    /** Thread local collections of messages. */
    private static final ThreadLocal current = new ThreadLocal()
    {
        public Object initialValue()
        {
            return new Messages();
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
        return (Messages) current.get();
    }

    //-----------------------------------------------------ThreadLocalMessages--

}

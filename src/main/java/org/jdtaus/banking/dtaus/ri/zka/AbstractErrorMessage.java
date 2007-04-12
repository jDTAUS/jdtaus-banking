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
package org.jdtaus.banking.dtaus.ri.zka;

import org.jdtaus.core.text.Message;

/**
 * Base error message.
 * <p>This class should be used as the base for all error messages produced by
 * an implementation. Whenever an instance of this class is created, it throws a
 * {@code PhysicalFileError} runtime exception if the thread-local property
 * {@code errorsEnabled} is {@code true}. If the property is set to
 * {@code false}, all instances of this class are assembled with the
 * {@code ThreadLocalMessages} utility and no exceptions are thrown.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public abstract class AbstractErrorMessage extends Message
{

    //--Constructors------------------------------------------------------------

    /**
     * Creates a new {@code AbstractErrorMessage} instance.
     * <p>Subclasses must throw a {@code PhysicalFileError} runtime exception
     * if property {@code errorsEnabled} is {@code true} after finishing
     * initialization.</p>
     */
    protected AbstractErrorMessage()
    {
        super();
    }

    //------------------------------------------------------------Constructors--
    //--AbstractErrorMessage----------------------------------------------------

    /** {@code ThreadLocal} {@code Boolean}. */
    private static final ThreadLocal current = new ThreadLocal()
    {
        protected Object initialValue()
        {
            return Boolean.TRUE;
        }
    };

    /**
     * Flag indicating that {@code PhysicalFileError}s are enabled.
     *
     * @return {@code true} if creating an {@code AbstractErrorMessage}
     * instance throws an appropriate {@code PhysicalFileError} runtime
     * exception; {@code false} to assemble instances using
     * {@code ThreadLocalMessages} and not throw any exception.
     */
    public static boolean isErrorsEnabled()
    {
        Boolean fatal = (Boolean) current.get();

        if(fatal == null)
        {
            throw new IllegalStateException();
        }

        return fatal.booleanValue();
    }

    /**
     * Setter for property {@code errorsEnabled}.
     *
     * @param enabled {@code true} if creating an {@code AbstractErrorMessage}
     * should throw an appropriate {@code PhysicalFileError} runtime exception;
     * {@code false} to assemble instances using {@code ThreadLocalMessages} and
     * not throw any exception.
     */
    public static void setErrorsEnabled(final boolean enabled)
    {
        current.set(enabled ? Boolean.TRUE : Boolean.FALSE);
    }

    //----------------------------------------------------AbstractErrorMessage--

}

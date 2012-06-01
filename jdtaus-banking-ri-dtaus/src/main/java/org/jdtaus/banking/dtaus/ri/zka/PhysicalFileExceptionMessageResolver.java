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

import org.jdtaus.banking.dtaus.PhysicalFileException;
import org.jdtaus.core.lang.util.ExceptionMessageResolver;
import org.jdtaus.core.text.Message;
import org.jdtaus.core.text.Messages;

/**
 * {@code ExceptionMessageResolver} producing {@code Message}s for
 * {@code PhysicalFileException} instances.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $JDTAUS$
 *
 * @see ExceptionMessageResolver
 */
public final class PhysicalFileExceptionMessageResolver
    implements ExceptionMessageResolver
{

    public Message[] resolve( final Exception exception )
    {
        Message[] resolved = null;

        if ( exception != null && exception instanceof PhysicalFileException )
        {
            final PhysicalFileException e = (PhysicalFileException) exception;
            final Messages msgs = new Messages();
            msgs.addMessages( e.getMessages() );
            resolved = msgs.getMessages();
        }

        return resolved;
    }

    //--Constructors------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausConstructors
    // This section is managed by jdtaus-container-mojo.

    /** Standard implementation constructor <code>org.jdtaus.banking.dtaus.ri.zka.PhysicalFileExceptionMessageResolver</code>. */
    public PhysicalFileExceptionMessageResolver()
    {
        super();
    }

// </editor-fold>//GEN-END:jdtausConstructors

    //------------------------------------------------------------Constructors--
}

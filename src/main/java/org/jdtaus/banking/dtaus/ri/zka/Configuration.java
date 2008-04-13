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

import java.util.HashMap;
import java.util.Map;

/**
 * Gathers configuration of the file format implementations.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id: AbstractErrorMessage.java 4667 2008-03-13 02:28:10Z schulte2005 $
 */
public class Configuration
{

    /**
     * Map of field constants to a flag indicating if space characters are
     * allowed for a field.
     * @serial
     */
    private Map spacesMap;

    /**
     * Gets a flag indicating if space characters are allowed for a given field.
     *
     * @param field constant for the field to query.
     *
     * @return {@code true} if space characters are allowed for field
     * {@code field}; {@code false} if not.
     */
    public boolean isSpaceCharacterAllowed( final int field )
    {
        if ( this.spacesMap == null )
        {
            this.spacesMap = new HashMap();
        }

        final Boolean b =
            ( Boolean ) this.spacesMap.get( new Integer( field ) );

        return b != null && b.booleanValue();
    }

    /**
     * Sets a flag indicating that space characters are allowed for a given
     * field.
     *
     * @param field constant for the field to set.
     * @param spaceCharacterAllowed {@code true} if space characters are allowed
     * for field {@code field};{@code false} if not.
     */
    public void setSpaceCharacterAllowed( final int field,
                                           boolean spaceCharacterAllowed )
    {
        if ( this.spacesMap == null )
        {
            this.spacesMap = new HashMap();
        }

        this.spacesMap.put( new Integer( field ),
                            Boolean.valueOf( spaceCharacterAllowed ) );

    }

}

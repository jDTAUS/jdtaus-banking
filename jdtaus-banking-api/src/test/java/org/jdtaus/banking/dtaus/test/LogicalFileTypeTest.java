/*
 *  jDTAUS Banking API
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
package org.jdtaus.banking.dtaus.test;

import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;
import junit.framework.Assert;
import org.jdtaus.banking.dtaus.LogicalFileType;

/**
 * Tests class {@code LogicalFileType}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class LogicalFileTypeTest
{

    public void testSearchLogicalFileTypes() throws Exception
    {
        final LogicalFileType[] types =
            LogicalFileType.searchLogicalFileTypes( null, null, null, null );

        Assert.assertNotNull( types );

        final List l = Arrays.asList( types );
        Assert.assertTrue( l.contains( LogicalFileType.GB ) );
        Assert.assertTrue( l.contains( LogicalFileType.GK ) );
        Assert.assertTrue( l.contains( LogicalFileType.LB ) );
        Assert.assertTrue( l.contains( LogicalFileType.LK ) );
    }

    public void testSerializable() throws Exception
    {
        final ObjectInputStream in = new ObjectInputStream(
            this.getClass().getResourceAsStream( "LogicalFileType.ser" ) );

        final LogicalFileType l = (LogicalFileType) in.readObject();
        in.close();

        System.out.println( l );
        Assert.assertEquals( LogicalFileType.GK, l );
    }

}

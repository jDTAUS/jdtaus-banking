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
package org.jdtaus.banking.dtaus.test;

import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;
import junit.framework.Assert;
import org.jdtaus.banking.dtaus.LogicalFileType;

/**
 * Tests class {@code LogicalFileType}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public class LogicalFileTypeTest
{

    public void testSearchLogicalFileTypes() throws Exception
    {
        final LogicalFileType[] types = LogicalFileType.searchLogicalFileTypes( null, null, null, null );
        Assert.assertNotNull( types );

        final List l = Arrays.asList( types );
        Assert.assertTrue( l.contains( LogicalFileType.GB ) );
        Assert.assertTrue( l.contains( LogicalFileType.GK ) );
        Assert.assertTrue( l.contains( LogicalFileType.LB ) );
        Assert.assertTrue( l.contains( LogicalFileType.LK ) );
    }

    public void testSerializable() throws Exception
    {
        final ObjectInputStream in =
            new ObjectInputStream( this.getClass().getResourceAsStream( "LogicalFileType.ser" ) );

        final LogicalFileType l = (LogicalFileType) in.readObject();
        in.close();
        System.out.println( l );
        Assert.assertEquals( LogicalFileType.GK, l );
    }

}

/*
 *  jDTAUS Banking Test Suite
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
package org.jdtaus.banking.it;

import java.util.Date;
import junit.framework.Assert;
import org.jdtaus.banking.TextschluesselVerzeichnis;

/**
 * Testcase for {@code TextschluesselVerzeichnis} implementations.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class TextschluesselVerzeichnisTest
{
    //--TextschluesselVerzeichnisTest-------------------------------------------

    /** Implementation to test. */
    private TextschluesselVerzeichnis directory;

    /**
     * Gets the {@code TextschluesselVerzeichnis} implementation tests are
     * performed with.
     *
     * @return the {@code TextschluesselVerzeichnis} implementation tests are
     * performed with.
     */
    public TextschluesselVerzeichnis getTextschluesselVerzeichnis()
    {
        return this.directory;
    }

    /**
     * Sets the {@code TextschluesselVerzeichnis} implementation tests are
     * performed with.
     *
     * @param value the {@code TextschluesselVerzeichnis} implementation to
     * perform tests with.
     */
    public final void setTextschluesselVerzeichnis(
        final TextschluesselVerzeichnis value )
    {
        this.directory = value;
    }

    //-------------------------------------------TextschluesselVerzeichnisTest--
    //--Tests-------------------------------------------------------------------

    /**
     * Tests the {@link TextschluesselVerzeichnis#getTextschluessel(int,int)}
     * method to handle illegal arguments correctly.
     */
    public void testGetTextschluessel() throws Exception
    {
        assert this.getTextschluesselVerzeichnis() != null;

        try
        {
            this.getTextschluesselVerzeichnis().
                getTextschluessel( Integer.MIN_VALUE, 0 );

            throw new AssertionError();
        }
        catch ( IllegalArgumentException e )
        {
        }

        try
        {
            this.getTextschluesselVerzeichnis().
                getTextschluessel( 0, Integer.MIN_VALUE );

            throw new AssertionError();
        }
        catch ( IllegalArgumentException e )
        {
        }

        try
        {
            this.getTextschluesselVerzeichnis().
                getTextschluessel( Integer.MAX_VALUE, 0 );

            throw new AssertionError();
        }
        catch ( IllegalArgumentException e )
        {
        }


        try
        {
            this.getTextschluesselVerzeichnis().
                getTextschluessel( 0, Integer.MAX_VALUE );

            throw new AssertionError();
        }
        catch ( IllegalArgumentException e )
        {
        }

        try
        {
            this.getTextschluesselVerzeichnis().
                getTextschluessel( Integer.MIN_VALUE, 0, new Date() );

            throw new AssertionError();
        }
        catch ( IllegalArgumentException e )
        {
        }

        try
        {
            this.getTextschluesselVerzeichnis().
                getTextschluessel( 0, Integer.MIN_VALUE, new Date() );

            throw new AssertionError();
        }
        catch ( IllegalArgumentException e )
        {
        }

        try
        {
            this.getTextschluesselVerzeichnis().
                getTextschluessel( Integer.MAX_VALUE, 0, new Date() );

            throw new AssertionError();
        }
        catch ( IllegalArgumentException e )
        {
        }


        try
        {
            this.getTextschluesselVerzeichnis().
                getTextschluessel( 0, Integer.MAX_VALUE, new Date() );

            throw new AssertionError();
        }
        catch ( IllegalArgumentException e )
        {
        }

        try
        {
            this.getTextschluesselVerzeichnis().
                getTextschluessel( 0, 0, null );

            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
        }
    }

    /**
     * Test the {@link TextschluesselVerzeichnis#getTextschluessel()} method
     * to not throw any exceptions.
     */
    public void testGetAllTextschluessel() throws Exception
    {
        assert this.getTextschluesselVerzeichnis() != null;

        Assert.assertNotNull( this.getTextschluesselVerzeichnis().
            getTextschluessel() );

    }

    /**
     * Tests the {@link TextschluesselVerzeichnis#search(boolean,boolean)}
     * method to not return {@code null} references.
     */
    public void testSearch() throws Exception
    {
        assert this.getTextschluesselVerzeichnis() != null;

        Assert.assertTrue( this.getTextschluesselVerzeichnis().
            search( false, false ).length >= 0 );

        try
        {
            this.getTextschluesselVerzeichnis().search( false, false, null );
            throw new AssertionError();
        }
        catch ( NullPointerException e )
        {
        }
    }

    //-------------------------------------------------------------------Tests--
}

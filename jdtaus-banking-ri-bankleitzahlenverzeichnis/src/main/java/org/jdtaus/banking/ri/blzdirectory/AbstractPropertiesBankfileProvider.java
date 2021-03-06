/*
 *  jDTAUS Banking RI Bankleitzahlenverzeichnis
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
package org.jdtaus.banking.ri.blzdirectory;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.jdtaus.banking.util.BankleitzahlenDatei;

/**
 * {@code BankfileProvider} implementation backed by a properties file.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 *
 * @see BankfileBankleitzahlenVerzeichnis
 */
public abstract class AbstractPropertiesBankfileProvider implements BankfileProvider
{

    /** Key of the bankfileCount property. */
    private static final String BANKFILE_COUNT_PROPERTY = "BankleitzahlenVerzeichnis.bankfileCount";

    /** Prefix of bank file properties. */
    private static final String BANKFILE_PREFIX = "BankleitzahlenDatei.";

    /** Properties backing the instance. */
    private Properties properties;

    /** Creates a new {@code AbstractPropertiesBankfileProvider} instance. */
    public AbstractPropertiesBankfileProvider()
    {
        super();
    }

    public long getLastModifiedMillis() throws IOException
    {
        return 0L;
    }

    public final int getBankfileCount() throws IOException
    {
        try
        {
            final String value = this.getProperties().getProperty( BANKFILE_COUNT_PROPERTY, Integer.toString( 0 ) );
            return NumberFormat.getIntegerInstance().parse( value ).intValue();
        }
        catch ( final ParseException e )
        {
            throw (IOException) new IOException( e.getMessage() ).initCause( e );
        }
    }

    /**
     * Gets a bankfile resource location.
     *
     * @param index The index of the bankfile resource location to get.
     *
     * @return The bankfile resource location at {@code index}.
     *
     * @throws IndexOutOfBoundsException if {@code index} is negative or greater or equal to the value returned by
     * method {@code getBankfileCount()}.
     * @throws IOException if getting the bankfile resource location fails.
     */
    public String getBankfileLocation( final int index ) throws IOException
    {
        if ( index < 0 || index >= this.getBankfileCount() )
        {
            throw new IndexOutOfBoundsException( Integer.toString( index ) );
        }

        final String propertyKey = BANKFILE_PREFIX + index + ".location";
        final String location = this.getProperties().getProperty( propertyKey );
        assert location != null : "Property '" + propertyKey + "' not found.";
        return location;
    }

    public final Date getDateOfValidity( final int index ) throws IOException
    {
        if ( index < 0 || index >= this.getBankfileCount() )
        {
            throw new IndexOutOfBoundsException( Integer.toString( index ) );
        }

        try
        {
            final String propertyKey = BANKFILE_PREFIX + index + ".dateOfValidity";
            final String value = this.getProperties().getProperty( propertyKey );
            assert value != null : "Property '" + propertyKey + "' not found.";
            return new SimpleDateFormat( "yyyyMMdd" ).parse( value );
        }
        catch ( final ParseException e )
        {
            throw (IOException) new IOException( e.getMessage() ).initCause( e );
        }
    }

    public final Date getDateOfExpiration( final int index ) throws IOException
    {
        if ( index < 0 || index >= this.getBankfileCount() )
        {
            throw new IndexOutOfBoundsException( Integer.toString( index ) );
        }

        try
        {
            final String propertyKey = BANKFILE_PREFIX + index + ".dateOfExpiration";
            final String value = this.getProperties().getProperty( propertyKey );
            assert value != null : "Property '" + propertyKey + "' not found.";
            return new SimpleDateFormat( "yyyyMMdd" ).parse( value );
        }
        catch ( final ParseException e )
        {
            throw (IOException) new IOException( e.getMessage() ).initCause( e );
        }
    }

    public final int getFormat( final int index ) throws IOException
    {
        if ( index < 0 || index >= this.getBankfileCount() )
        {
            throw new IndexOutOfBoundsException( Integer.toString( index ) );
        }

        final String value =
            this.getProperties().getProperty( BANKFILE_PREFIX + index + ".format" );

        return value == null
               ? BankleitzahlenDatei.JUNE_2006_FORMAT
               : Integer.parseInt( value );

    }

    /**
     * Gets the properties of the instance.
     *
     * @return The properties of the instance.
     *
     *  @throws IOException if getting the properties fails.
     */
    public Properties getProperties() throws IOException
    {
        if ( this.properties == null )
        {
            this.properties = new Properties();
        }

        return this.properties;
    }

    /**
     * Sets the properties of the instance.
     *
     * @param value The new properties of the instance or {@code null}.
     */
    public void setProperties( final Properties value )
    {
        this.properties = value;
    }

}

/*
 *  jDTAUS Banking RI Bankleitzahlenverzeichnis
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
package org.jdtaus.banking.ri.blzdirectory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.PropertyException;

/**
 * {@code BankfileProvider} implementation backed by a property file.
 * <p>This implementation provides bankfile resources specified in a property
 * file. Property {@code configuration} holds the name of the property file
 * resource specifying the bankfiles to load and property {@code dataDirectory}
 * holds the name of the directory in which the bankfiles are kept.</p>
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 *
 * @see BundesbankBankleitzahlenVerzeichnis
 */
public final class PropertyBankfileProvider implements BankfileProvider
{
    //--Constructors------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausConstructors
    // This section is managed by jdtaus-container-mojo.

    /** Standard implementation constructor <code>org.jdtaus.banking.ri.blzdirectory.PropertyBankfileProvider</code>. */
    public PropertyBankfileProvider()
    {
        super();
    }

// </editor-fold>//GEN-END:jdtausConstructors

    //------------------------------------------------------------Constructors--
    //--Properties--------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausProperties
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the value of property <code>defaultPropertiesResource</code>.
     *
     * @return Default name of the properties file resource specifying the bankfiles to load.
     */
    private java.lang.String getDefaultPropertiesResource()
    {
        return (java.lang.String) ContainerFactory.getContainer().
            getProperty( this, "defaultPropertiesResource" );

    }

    /**
     * Gets the value of property <code>defaultDataDirectory</code>.
     *
     * @return Default name of the directory holding bankfiles.
     */
    private java.lang.String getDefaultDataDirectory()
    {
        return (java.lang.String) ContainerFactory.getContainer().
            getProperty( this, "defaultDataDirectory" );

    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
    //--BankfileProvider--------------------------------------------------------

    public URL[] getResources() throws IOException
    {

        this.assertValidProperties();

        final InputStream stream = this.getClassLoader().getResourceAsStream(
            this.getPropertiesResource() );

        final Properties properties = new java.util.Properties();
        properties.load( stream );

        final Map sorted = new TreeMap( PROPERTY_SORTER );
        sorted.putAll( properties );

        final URL[] resources = new URL[ sorted.size() ];
        int i = 0;
        for ( Iterator it = sorted.values().iterator(); it.hasNext(); i++ )
        {
            final String location = this.getDataDirectory() + '/' +
                it.next().toString();

            resources[i] = this.getClassLoader().getResource( location );

            assert resources[i] != null : "Expected resource " + location +
                " missing.";

        }

        return resources;
    }

    //--------------------------------------------------------BankfileProvider--
    //--PropertyBankfileProvider------------------------------------------------

    /** Classloader searched for resources. */
    private ClassLoader classLoader;

    /** Name of the directory holding bankfiles. */
    private String dataDirectory;

    /**
     * Name of the properties file resource specifying the bankfiles to load.
     */
    private String propertiesResource;

    /** Prefix used for property keys in the property file. */
    private static final String PREFIX = "BankleitzahlenDatei.";

    /** {@code Comparator} used to sort property keys in ascending order. */
    private static final Comparator PROPERTY_SORTER = new Comparator()
    {

        public int compare( final Object o1, final Object o2 )
        {
            if ( !( o1 instanceof String ) )
            {
                throw new ClassCastException();
            }
            if ( !( o2 instanceof String ) )
            {
                throw new ClassCastException();
            }

            int ret = 0;
            final NumberFormat fmt = NumberFormat.getIntegerInstance();
            try
            {
                final Number o1Int =
                    fmt.parse( ( (String) o1 ).substring( PREFIX.length() ) );

                final Number o2Int =
                    fmt.parse( ( (String) o2 ).substring( PREFIX.length() ) );

                if ( o1Int.longValue() < o2Int.longValue() )
                {
                    ret = -1;
                }
                else if ( o1Int.longValue() > o2Int.longValue() )
                {
                    ret = 1;
                }

            }
            catch ( ParseException e )
            {
                throw new AssertionError( e );
            }

            return ret;
        }

    };

    /**
     * Creates a new {@code PropertyBankfileProvider} instance taking the name
     * of the properties file resource specifying bankfiles to load and the name
     * of the directory holding these banfiles.
     *
     * @param propertiesResource Name of the properties file resource specifying
     * the bankfiles to load.
     * @param dataDirectory Name of the directory holding bankfiles.
     */
    public PropertyBankfileProvider( final String propertiesResource,
        final String dataDirectory )
    {
        this( propertiesResource, dataDirectory, null );
    }

    /**
     * Creates a new {@code PropertyBankfileProvider} instance taking the
     * classloader to search for resources.
     *
     * @param classLoader Classloader to search for resources.
     */
    public PropertyBankfileProvider( final ClassLoader classLoader )
    {
        this( null, null, classLoader );
    }

    /**
     * Creates a new {@code PropertyBankfileProvider} instance taking the name
     * of the properties file resource specifying bankfiles to load, the name
     * of the directory holding these banfiles and the classloader to search for
     * resources.
     *
     * @param propertiesResource Name of the properties file resource specifying
     * the bankfiles to load.
     * @param dataDirectory Name of the directory holding bankfiles.
     * @param classLoader Classloader to search for resources.
     */
    public PropertyBankfileProvider( final String propertiesResource,
        final String dataDirectory, final ClassLoader classLoader )
    {
        this();
        this.propertiesResource = propertiesResource;
        this.dataDirectory = dataDirectory;
        this.classLoader = classLoader;
    }

    /**
     * Gets the name of the directory holding bankfiles.
     *
     * @return name of the directory holding bankfiles.
     */
    public String getDataDirectory()
    {
        if ( this.dataDirectory == null )
        {
            this.dataDirectory = this.getDefaultDataDirectory();
        }

        return this.dataDirectory;
    }

    /**
     * Gets the name of the properties file resource specifying the bankfiles to
     * load.
     *
     * @return name of the properties file resource specifying the bankfiles to
     * load.
     */
    public String getPropertiesResource()
    {
        if ( this.propertiesResource == null )
        {
            this.propertiesResource = this.getDefaultPropertiesResource();
        }

        return this.propertiesResource;
    }

    /**
     * Gets the classloader searched for resources.
     * <p>This method returns either the current thread's context classloader or
     * this classes classloader, if the current thread has no context classloader
     * set. A custom classloader can be specified by using one of the
     * constructors taking a classloader.</p>
     *
     * @return the classloader to search for resources.
     */
    public ClassLoader getClassLoader()
    {
        if ( this.classLoader == null )
        {
            this.classLoader = Thread.currentThread().getContextClassLoader();

            if ( this.classLoader == null )
            {
                this.classLoader = this.getClass().getClassLoader();

                if ( this.classLoader == null )
                {
                    this.classLoader = ClassLoader.getSystemClassLoader();
                }
            }
        }

        return this.classLoader;
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException if configured properties hold invalid values.
     */
    private void assertValidProperties()
    {
        if ( this.getDataDirectory() == null ||
            this.getDataDirectory().length() == 0 )
        {

            throw new PropertyException( "dataDirectory",
                this.getDataDirectory() );

        }
        if ( this.getPropertiesResource() == null ||
            this.getPropertiesResource().length() == 0 )
        {
            throw new PropertyException( "propertiesResource",
                this.getPropertiesResource() );

        }
    }

    //------------------------------------------------PropertyBankfileProvider--
}

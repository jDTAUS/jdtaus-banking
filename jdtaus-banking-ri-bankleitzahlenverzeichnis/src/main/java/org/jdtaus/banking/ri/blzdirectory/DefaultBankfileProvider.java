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
import java.net.URL;
import java.util.Locale;
import java.util.Properties;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.PropertyException;
import org.jdtaus.core.logging.spi.Logger;

/**
 * Default {@code BankfileProvider} implementation backed by a property file.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 *
 * @see BankfileBankleitzahlenVerzeichnis
 */
public final class DefaultBankfileProvider extends AbstractPropertiesBankfileProvider
{

    /** Class loader searched for resources. */
    private ClassLoader classLoader;

    /** Location searched for resources. */
    private String classpathLocation;

    /** Name of the properties resource. */
    private String propertiesResourceName;

    /** Properties backing the instance. */
    private Properties properties;

    /**
     * Creates a new {@code ClasspathBankfileProvider} instance taking a class loader to search for resources,
     * a location resources are searched at and the name of the properties resource backing the instance.
     *
     * @param classLoader Class loader to search for resources or {@code null}.
     * @param classpathLocation Location to search resources at or {@code null}.
     * @param propertiesResourceName Name of the properties resource backing the instance or {@code null}.
     */
    public DefaultBankfileProvider( final ClassLoader classLoader, final String classpathLocation,
                                    final String propertiesResourceName )
    {
        super();
        this.classLoader = classLoader;
        this.classpathLocation = classpathLocation;
        this.propertiesResourceName = propertiesResourceName;
    }

    public URL getBankfile( final int index ) throws IOException
    {
        if ( index < 0 || index >= this.getBankfileCount() )
        {
            throw new IndexOutOfBoundsException( Integer.toString( index ) );
        }

        final String resourceName = this.getClasspathLocation() + "/" + this.getBankfileLocation( index );
        final URL resource = this.getClassLoader().getResource( resourceName );
        assert resource != null : "Resource '" + resourceName + "' not found.";
        return resource;
    }

    /**
     * Gets the class loader searched for resources.
     * <p>This method returns either the current thread's context class loader or this classes class loader, if the
     * current thread has no context class loader set. A custom class loader can be specified by using one of the
     * constructors taking a class loader.</p>
     *
     * @return The class loader searched for resources.
     */
    public ClassLoader getClassLoader()
    {
        if ( this.classLoader == null )
        {
            if ( Thread.currentThread().getContextClassLoader() != null )
            {
                return Thread.currentThread().getContextClassLoader();
            }

            this.classLoader = this.getClass().getClassLoader();
            if ( this.classLoader == null )
            {
                this.classLoader = ClassLoader.getSystemClassLoader();
            }
        }

        return this.classLoader;
    }

    /**
     * Gets the classpath location searched for resources.
     *
     * @return The classpath location searched for resources.
     */
    public String getClasspathLocation()
    {
        if ( this.classpathLocation == null )
        {
            this.classpathLocation = this.getDefaultClasspathLocation();
        }

        return this.classpathLocation;
    }

    /**
     * Gets the name of the properties resource backing the instance.
     *
     * @return The name of the properties resource backing the instance.
     */
    public String getPropertiesResourceName()
    {
        if ( this.propertiesResourceName == null )
        {
            this.propertiesResourceName = this.getDefaultPropertiesResourceName();
        }

        return this.propertiesResourceName;
    }

    /**
     * Gets the properties backing the instance.
     *
     * @return The properties backing the instance.
     *
     * @throws IOException if loading the properties fails.
     */
    public Properties getProperties() throws IOException
    {
        if ( this.properties == null )
        {
            this.assertValidProperties();
            final String propertiesLocation = this.getClasspathLocation() + "/" + this.getPropertiesResourceName();
            final URL rsrc = this.getClassLoader().getResource( propertiesLocation );
            final Properties p = new Properties();

            if ( rsrc != null )
            {
                p.load( rsrc.openStream() );
            }
            else
            {
                this.getLogger().info( this.getPropertiesNotFoundMessage( this.getLocale(), propertiesLocation ) );
            }

            this.properties = p;
        }

        return this.properties;
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException if properties hold invalid values.
     */
    private void assertValidProperties()
    {
        if ( this.getClasspathLocation() == null )
        {
            throw new PropertyException( "classpathLocation", this.getClasspathLocation() );
        }
        if ( this.getPropertiesResourceName() == null || this.getPropertiesResourceName().length() <= 0 )
        {
            throw new PropertyException( "propertiesResourceName", this.getPropertiesResourceName() );
        }
    }

    //--Constructors------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausConstructors
    // This section is managed by jdtaus-container-mojo.

    /** Standard implementation constructor <code>org.jdtaus.banking.ri.blzdirectory.DefaultBankfileProvider</code>. */
    public DefaultBankfileProvider()
    {
        super();
    }

// </editor-fold>//GEN-END:jdtausConstructors

    //------------------------------------------------------------Constructors--
    //--Dependencies------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the configured <code>Logger</code> implementation.
     *
     * @return The configured <code>Logger</code> implementation.
     */
    private Logger getLogger()
    {
        return (Logger) ContainerFactory.getContainer().
            getDependency( this, "Logger" );

    }

    /**
     * Gets the configured <code>Locale</code> implementation.
     *
     * @return The configured <code>Locale</code> implementation.
     */
    private Locale getLocale()
    {
        return (Locale) ContainerFactory.getContainer().
            getDependency( this, "Locale" );

    }

// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
    //--Properties--------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausProperties
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the value of property <code>defaultPropertiesResourceName</code>.
     *
     * @return Default resource name of the classpath properties resource backing the implementation.
     */
    private java.lang.String getDefaultPropertiesResourceName()
    {
        return (java.lang.String) ContainerFactory.getContainer().
            getProperty( this, "defaultPropertiesResourceName" );

    }

    /**
     * Gets the value of property <code>defaultClasspathLocation</code>.
     *
     * @return Default classpath location of the resources backing the implementation.
     */
    private java.lang.String getDefaultClasspathLocation()
    {
        return (java.lang.String) ContainerFactory.getContainer().
            getProperty( this, "defaultClasspathLocation" );

    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>propertiesNotFound</code>.
     * <blockquote><pre>Properties Ressource ''{0}'' nicht gefunden - keine Bereitstellung von Klassenpfad-Bankleitzahlen-Dateien.</pre></blockquote>
     * <blockquote><pre>Properties resource ''{0}'' not found - not providing classpath bankcode files.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param location format parameter.
     *
     * @return the text of message <code>propertiesNotFound</code>.
     */
    private String getPropertiesNotFoundMessage( final Locale locale,
            final java.lang.String location )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "propertiesNotFound", locale,
                new Object[]
                {
                    location
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

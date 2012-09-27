/*
 *  jDTAUS Banking RI Textschluesselverzeichnis
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
package org.jdtaus.banking.ri.txtdirectory;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import org.jdtaus.core.container.ContainerFactory;

/**
 * Default {@code JaxpTextschluesselProvider} implementation.
 * <p>This implementation provides resources by searching the classpath. Property {@code resourceName} holds the name of
 * the resources to search and defaults to {@code META-INF/jdtaus/textschluessel.xml}.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 *
 * @see JaxpTextschluesselVerzeichnis
 */
public class DefaultJaxpTextschluesselProvider implements JaxpTextschluesselProvider
{

    /** Class loader searched for resources. */
    private ClassLoader classLoader;

    /** Name of the classpath resource to search. */
    private String resourceName;

    /**
     * Creates a new {@code DefaultTextschluesselProvider} instance taking the name of the classpath resource search.
     *
     * @param resourceName Name of the classpath resource to search.
     */
    public DefaultJaxpTextschluesselProvider( final String resourceName )
    {
        this( resourceName, null );
    }

    /**
     * Creates a new {@code DefaultTextschluesselProvider} instance taking the class loader to search for resources.
     *
     * @param classLoader The class loader to search for resources.
     */
    public DefaultJaxpTextschluesselProvider( final ClassLoader classLoader )
    {
        this( null, classLoader );
    }

    /**
     * Creates a new {@code DefaultTextschluesselProvider} instance taking the name of the classpath resource to search
     * and the class loader to search for resources.
     *
     * @param resourceName Name of the classpath resource to search.
     * @param classLoader The class loader to search for resources.
     */
    public DefaultJaxpTextschluesselProvider( final String resourceName, final ClassLoader classLoader )
    {
        this.resourceName = resourceName;
        this.classLoader = classLoader;
    }

    /**
     * Gets the name of the classpath resource to search.
     *
     * @return The name of the classpath resource to search.
     */
    public String getResourceName()
    {
        if ( this.resourceName == null )
        {
            this.resourceName = this.getDefaultResourceName();
        }

        return this.resourceName;
    }

    /**
     * Gets the class loader searched for resources.
     * <p>This method returns either the current thread's context class loader or this classes class loader, if the
     * current thread has no context class loader set. A custom class loader can be specified by using one of the
     * constructors taking a class loader.</p>
     *
     * @return The class loader to search for resources.
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

    public URL[] getResources() throws IOException
    {
        final Collection col = new LinkedList();
        final Enumeration en = this.getClassLoader().getResources( this.getResourceName() );

        while ( en.hasMoreElements() )
        {
            col.add( en.nextElement() );
        }

        return (URL[]) col.toArray( new URL[ col.size() ] );
    }

    //--Constructors------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausConstructors
    // This section is managed by jdtaus-container-mojo.

    /** Standard implementation constructor <code>org.jdtaus.banking.ri.txtdirectory.DefaultJaxpTextschluesselProvider</code>. */
    public DefaultJaxpTextschluesselProvider()
    {
        super();
    }

// </editor-fold>//GEN-END:jdtausConstructors

    //------------------------------------------------------------Constructors--
    //--Properties--------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausProperties
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the value of property <code>defaultResourceName</code>.
     *
     * @return Default name of the classpath resource to search.
     */
    private java.lang.String getDefaultResourceName()
    {
        return (java.lang.String) ContainerFactory.getContainer().
            getProperty( this, "defaultResourceName" );

    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
}

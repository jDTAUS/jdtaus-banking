/*
 *  jDTAUS Banking RI CurrencyDirectory
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
package org.jdtaus.banking.ri.currencydir.test;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Classloader for providing classpath resources.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 *
 * @see #addResource(java.lang.String, java.net.URL)
 * @see #addResources(java.lang.String, java.net.URL[])
 */
public class ResourceLoader extends ClassLoader
{

    /** The classloader delegated to. */
    private ClassLoader delegate;

    /** Mapping of classpath locations to a list of URLs to provide. */
    private Map locations = new HashMap();

    /**
     * Creates a new {@code ResourceLoader} instance taking a classloader
     * classloading is delegated to.
     *
     * @param delegate classloader classloading is delegated to.
     */
    public ResourceLoader( final ClassLoader delegate )
    {
        super();
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    protected Class findClass( final String name )
        throws ClassNotFoundException
    {
        return this.delegate.loadClass( name );
    }

    /**
     * Finds the resource with the given name by searching the instance for any
     * resources first, falling back to the classloader classloading is
     * delegated to.
     *
     * @param  name the name of the resource.
     *
     * @return  a {@code URL} object for reading the resource, or {@code null}
     * if the resource could not be found.
     */
    protected URL findResource( final String name )
    {
        URL thisResource = null;

        if ( this.locations.containsKey( name ) )
        {
            final Set resources = ( Set ) this.locations.get( name );
            thisResource = ( URL ) resources.iterator().next();
        }
        else
        {
            thisResource = this.delegate.getResource( name );
        }

        return thisResource;
    }

    /**
     * Returns an enumeration of {@link java.net.URL {@code URL}} objects
     * representing all the resources with the given name by searching the
     * instance for any resources first, falling back to the classloader
     * classloading is delegated to.
     *
     * @param  name the resource name
     *
     * @return  an enumeration of {@link java.net.URL {@code URL}} objects for
     * the resources.
     *
     * @throws  IOException if searching fails.
     */
    protected Enumeration findResources( final String name )
        throws IOException
    {
        Enumeration thisResources = null;

        if ( this.locations.containsKey( name ) )
        {
            final Set resources = ( Set ) this.locations.get( name );
            thisResources = Collections.enumeration( resources );
        }
        else if ( thisResources == null || !thisResources.hasMoreElements() )
        {
            thisResources = this.delegate.getResources( name );
        }

        return thisResources;
    }

    /**
     * Adds a resource to the instance.
     *
     * @param name the name of the resource to add.
     * @param resource the resource to add to the instance.
     *
     * @throws NullPointerException if {@code name} or {@code resource} is
     * {@code null}.
     */
    public void addResource( final String name, final URL resource )
    {
        if ( name == null )
        {
            throw new NullPointerException( "name " );
        }
        if ( resource == null )
        {
            throw new NullPointerException( "resource" );
        }

        Set resources = ( Set ) this.locations.get( name );
        if ( resources == null )
        {
            resources = new HashSet();
            this.locations.put( name, resources );
        }

        resources.add( resource );
    }

    /**
     * Adds an array of resources to the instance.
     *
     * @param name the name of the resources to add.
     * @param resources the resources to add to the instance.
     *
     * @throws NullPointerException if {@code name} or {@code resources} is
     * {@code null}.
     */
    public void addResources( final String name, final URL[] resources )
    {
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }

        if ( resources == null )
        {
            throw new NullPointerException( "resources" );
        }

        for ( int i = resources.length - 1; i >= 0; i-- )
        {
            this.addResource( name, resources[i] );
        }
    }

}

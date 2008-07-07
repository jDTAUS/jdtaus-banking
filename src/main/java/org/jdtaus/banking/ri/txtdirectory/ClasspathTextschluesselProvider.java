/*
 *  jDTAUS Banking RI Textschluesselverzeichnis
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
package org.jdtaus.banking.ri.txtdirectory;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import org.jdtaus.core.container.ContainerInitializer;
import org.jdtaus.core.container.Dependency;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.container.Properties;
import org.jdtaus.core.container.Property;
import org.jdtaus.core.container.PropertyException;

/**
 * Classpath {@code TextschluesselProvider} implementation.
 * <p>This implementation provides resources from the classpath holding
 * Textschlüssel instances. Property {@code resourceName} holds the name of the
 * resources to provide and property {@code directoryName} holds the name of the
 * directory holding these resources.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 *
 * @see XMLTextschluesselVerzeichnis
 */
public final class ClasspathTextschluesselProvider
    implements ContainerInitializer, TextschluesselProvider
{
    //--Implementation----------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausImplementation
    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(ClasspathTextschluesselProvider.class.getName());
// </editor-fold>//GEN-END:jdtausImplementation

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausConstructors
    // This section is managed by jdtaus-container-mojo.

    /**
     * <code>ClasspathTextschluesselProvider</code> implementation constructor.
     *
     * @param meta Implementation meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    private ClasspathTextschluesselProvider(final Implementation meta)
    {
        super();
        if(meta == null)
        {
            throw new NullPointerException("meta");
        }
        this.initializeProperties(meta.getProperties());
    }
    /**
     * <code>ClasspathTextschluesselProvider</code> dependency constructor.
     *
     * @param meta dependency meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    private ClasspathTextschluesselProvider(final Dependency meta)
    {
        super();
        if(meta == null)
        {
            throw new NullPointerException("meta");
        }
        this.initializeProperties(meta.getProperties());
    }

    /**
     * Initializes the properties of the instance.
     *
     * @param meta the property values to initialize the instance with.
     *
     * @throws NullPointerException if {@code meta} is {@code null}.
     */
    private void initializeProperties(final Properties meta)
    {
        Property p;

        if(meta == null)
        {
            throw new NullPointerException("meta");
        }

        p = meta.getProperty("resourceName");
        this.pResourceName = (java.lang.String) p.getValue();


        p = meta.getProperty("directoryName");
        this.pDirectoryName = (java.lang.String) p.getValue();

    }
// </editor-fold>//GEN-END:jdtausConstructors

    //------------------------------------------------------------Constructors--
    //--ContainerInitializer----------------------------------------------------

    /**
     * Initializes the instance.
     *
     * @see #assertValidProperties()
     */
    public void initialize()
    {
        this.assertValidProperties();
    }

    //----------------------------------------------------ContainerInitializer--
    //--Dependencies------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
    // This section is managed by jdtaus-container-mojo.

// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
    //--Properties--------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausProperties
    // This section is managed by jdtaus-container-mojo.

    /**
     * Property {@code resourceName}.
     * @serial
     */
    private java.lang.String pResourceName;

    /**
     * Gets the value of property <code>resourceName</code>.
     *
     * @return the value of property <code>resourceName</code>.
     */
    private java.lang.String getResourceName()
    {
        return this.pResourceName;
    }

    /**
     * Property {@code directoryName}.
     * @serial
     */
    private java.lang.String pDirectoryName;

    /**
     * Gets the value of property <code>directoryName</code>.
     *
     * @return the value of property <code>directoryName</code>.
     */
    private java.lang.String getDirectoryName()
    {
        return this.pDirectoryName;
    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
    //--TextschluesselProvider--------------------------------------------------

    public URL[] getResources() throws IOException
    {
        final ClassLoader classLoader = this.getClassLoader();
        final Collection col = new LinkedList();
        final Enumeration en = classLoader.getResources(
            this.getDirectoryName() + '/' + this.getResourceName() );

        while ( en.hasMoreElements() )
        {
            col.add( en.nextElement() );
        }

        return ( URL[] ) col.toArray( new URL[ col.size() ] );
    }

    //--------------------------------------------------TextschluesselProvider--
    //--ClasspathTextschluesselProvider-----------------------------------------

    /** Creates a new {@code ClasspathTextschluesselProvider} instance. */
    public ClasspathTextschluesselProvider()
    {
        this( META );
        this.initialize();
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException if properties hold invalid values.
     */
    private void assertValidProperties()
    {
        if ( this.getDirectoryName() == null )
        {
            throw new PropertyException( "directoryName",
                                         this.getDirectoryName() );

        }
        if ( this.getResourceName() == null ||
            this.getResourceName().length() <= 0 )
        {
            throw new PropertyException( "resourceName",
                                         this.getResourceName() );

        }
    }

    /**
     * Gets the classloader used for loading XML resources.
     * <p>The reference implementation will use the current thread's context
     * classloader and will fall back to the system classloader if the
     * current thread has no context classloader set.</p>
     *
     * @return the classloader to be used for loading XML resources.
     */
    private ClassLoader getClassLoader()
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if ( classLoader == null )
        {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        assert classLoader != null :
            "Expected ClassLoader.getSystemClassLoader() to not return null.";

        return classLoader;
    }

    //-----------------------------------------ClasspathTextschluesselProvider--
}

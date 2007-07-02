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
package org.jdtaus.banking.ri.currencydir;

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
 * Classpath {@code CurrenciesProvider} implementation.
 * <p>This implementation provides resources from the classpath holding currency
 * instances. Property {@code resourceName} holds the name of the resources
 * to provide and property {@code directoryName} holds the name of the directory
 * holding these resources.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 *
 * @see XMLCurrencyDirectory
 */
public final class ClasspathCurrenciesProvider
    implements ContainerInitializer, CurrenciesProvider
{
    //--Implementation----------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(ClasspathCurrenciesProvider.class.getName());

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /**
     * Protected <code>ClasspathCurrenciesProvider</code> implementation constructor.
     *
     * @param meta Implementation meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    protected ClasspathCurrenciesProvider(final Implementation meta)
    {
        super();
        if(meta == null)
        {
            throw new NullPointerException("meta");
        }
        this.initializeProperties(meta.getProperties());
    }
    /**
     * Protected <code>ClasspathCurrenciesProvider</code> dependency constructor.
     *
     * @param meta dependency meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    protected ClasspathCurrenciesProvider(final Dependency meta)
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
    protected void initializeProperties(final Properties meta)
    {
        Property p;

        if(meta == null)
        {
            throw new NullPointerException("meta");
        }

        p = meta.getProperty("resourceName");
        this._resourceName = (java.lang.String) p.getValue();


        p = meta.getProperty("directoryName");
        this._directoryName = (java.lang.String) p.getValue();

    }

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

    // This section is managed by jdtaus-container-mojo.


    //------------------------------------------------------------Dependencies--
    //--Properties--------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /**
     * Property {@code resourceName}.
     * @serial
     */
    private java.lang.String _resourceName;

    /**
     * Gets the value of property <code>resourceName</code>.
     *
     * @return the value of property <code>resourceName</code>.
     */
    protected java.lang.String getResourceName()
    {
        return this._resourceName;
    }

    /**
     * Property {@code directoryName}.
     * @serial
     */
    private java.lang.String _directoryName;

    /**
     * Gets the value of property <code>directoryName</code>.
     *
     * @return the value of property <code>directoryName</code>.
     */
    protected java.lang.String getDirectoryName()
    {
        return this._directoryName;
    }


    //--------------------------------------------------------------Properties--
    //--CurrenciesProvider------------------------------------------------------

    public URL[] getResources() throws IOException
    {
        final ClassLoader classLoader = this.getClassLoader();
        final Collection col = new LinkedList();
        final Enumeration en = classLoader.getResources(
            this.getDirectoryName() + '/' + this.getResourceName());

        while(en.hasMoreElements())
        {
            col.add(en.nextElement());
        }

        return (URL[]) col.toArray(new URL[col.size()]);
    }

    //------------------------------------------------------CurrenciesProvider--
    //--ClasspathCurrenciesProvider---------------------------------------------

    /** Creates a new {@code ClasspathCurrenciesProvider} instance. */
    public ClasspathCurrenciesProvider()
    {
        this(META);
        this.initializeProperties(META.getProperties());
        this.initialize();
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException if properties hold invalid values.
     */
    private void assertValidProperties()
    {
        if(this.getDirectoryName() == null)
        {
            throw new PropertyException("directoryName",
                this.getDirectoryName());

        }
        if(this.getResourceName() == null ||
            this.getResourceName().length() <= 0)
        {

            throw new PropertyException("resourceName",
                this.getResourceName());

        }
    }

    /**
     * Gets the classloader used for loading resources.
     * <p>This method uses the current thread's context classloader and will
     * fall back to the system classloader if the current thread has no context
     * classloader set.</p>
     *
     * @return the classloader to be used for loading XML resources.
     */
    private ClassLoader getClassLoader()
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if(classLoader == null)
        {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        assert classLoader != null :
            "Expected ClassLoader.getSystemClassLoader() to not return null.";

        return classLoader;
    }

    //---------------------------------------------ClasspathCurrenciesProvider--
}

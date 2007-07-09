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
package org.jdtaus.banking.ri.blzdirectory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.jdtaus.core.container.ContainerInitializer;
import org.jdtaus.core.container.Dependency;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ImplementationException;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.container.Properties;
import org.jdtaus.core.container.Property;
import org.jdtaus.core.container.PropertyException;

/**
 * {@code BankfileProvider} implementation backed by a property file.
 * <p>This implementation provides bankfile resources specified in a property
 * file. Property {@code configuration} holds the name of the property file
 * resource specifying the bankfiles to load and property {@code dataDirectory}
 * holds the name of the directory in which the bankfiles are kept.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 *
 * @see BundesbankBankleitzahlenVerzeichnis
 */
public final class PropertyBankfileProvider
    implements ContainerInitializer, BankfileProvider
{
    //--Implementation----------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(PropertyBankfileProvider.class.getName());

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /**
     * Protected <code>PropertyBankfileProvider</code> implementation constructor.
     *
     * @param meta Implementation meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    protected PropertyBankfileProvider(final Implementation meta)
    {
        super();
        if(meta == null)
        {
            throw new NullPointerException("meta");
        }
        this.initializeProperties(meta.getProperties());
    }
    /**
     * Protected <code>PropertyBankfileProvider</code> dependency constructor.
     *
     * @param meta dependency meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    protected PropertyBankfileProvider(final Dependency meta)
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

        p = meta.getProperty("configuration");
        this._configuration = (java.lang.String) p.getValue();


        p = meta.getProperty("dataDirectory");
        this._dataDirectory = (java.lang.String) p.getValue();

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
     * Property {@code configuration}.
     * @serial
     */
    private java.lang.String _configuration;

    /**
     * Gets the value of property <code>configuration</code>.
     *
     * @return the value of property <code>configuration</code>.
     */
    protected java.lang.String getConfiguration()
    {
        return this._configuration;
    }

    /**
     * Property {@code dataDirectory}.
     * @serial
     */
    private java.lang.String _dataDirectory;

    /**
     * Gets the value of property <code>dataDirectory</code>.
     *
     * @return the value of property <code>dataDirectory</code>.
     */
    protected java.lang.String getDataDirectory()
    {
        return this._dataDirectory;
    }


    //--------------------------------------------------------------Properties--
    //--BankfileProvider--------------------------------------------------------

    public URL[] getResources() throws IOException
    {
        int i;
        String rsrc;
        InputStream stream = null;
        final URL[] ret;
        final Iterator it;
        final Map sorted = new TreeMap(PROPERTY_SORTER);
        final java.util.Properties props = new java.util.Properties();

        try
        {
            stream = this.getConfigurationResource().openStream();
            props.load(stream);
            sorted.putAll(props);
            ret = new URL[sorted.size()];
            for(it = sorted.values().iterator(), i = 0; it.hasNext(); i++)
            {
                rsrc = this.getDataDirectory() + '/' + it.next().toString();
                ret[i] = this.getClassLoader().getResource(rsrc);
                if(ret[i] == null)
                {
                    throw new ImplementationException(META, rsrc);
                }
            }

            return ret;
        }
        catch(IOException e)
        {
            throw new ImplementationException(META, e);
        }
        finally
        {
            if(stream != null)
            {
                try
                {
                    stream.close();
                }
                catch(IOException e)
                {
                    throw new ImplementationException(META, e);
                }
            }
        }
    }

    //--------------------------------------------------------BankfileProvider--
    //--PropertyBankfileProvider------------------------------------------------

    /** Prefix used for property keys in the property file. */
    private static final String PREFIX = "BankleitzahlenDatei.";

    /** {@code Comparator} used to sort property keys in ascending order. */
    private static final Comparator PROPERTY_SORTER = new Comparator()
    {
        public int compare(final Object o1, final Object o2)
        {
            if(!(o1 instanceof String))
            {
                throw new ClassCastException();
            }
            if(!(o2 instanceof String))
            {
                throw new ClassCastException();
            }

            int ret = 0;
            final NumberFormat fmt = NumberFormat.getIntegerInstance();
            try
            {
                final Number o1Int = fmt.parse(((String) o1).
                    substring(PREFIX.length()));

                final Number o2Int = fmt.parse(((String) o2).
                    substring(PREFIX.length()));

                if(o1Int.longValue() < o2Int.longValue())
                {
                    ret = -1;
                }
                else if(o1Int.longValue() > o2Int.longValue())
                {
                    ret = 1;
                }

            }
            catch(ParseException e)
            {
                throw new ImplementationException(META, e);
            }

            return ret;
        }
    };

    /** Creates a new {@code PropertyBankfileProvider} instance. */
    public PropertyBankfileProvider()
    {
        this(META);
        this.initialize();
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException if configured properties hold invalid values.
     */
    private void assertValidProperties()
    {
        if(this.getDataDirectory() == null ||
            this.getDataDirectory().length() == 0)
        {

            throw new PropertyException("dataDirectory",
                this.getDataDirectory());

        }
        if(this.getConfiguration() == null ||
            this.getConfiguration().length() == 0 ||
            this.getConfigurationResource() == null)
        {
            throw new PropertyException("configuration",
                this.getConfiguration());

        }
    }

    /**
     * Gets an URL to the property file configured via property
     * {@code configuration} holding the files to load.
     *
     * @return an URL to a property file holding the files to load or
     * {@code null} if property {@code configuration} does not point to any
     * resource.
     *
     * @see #getClassLoader()
     */
    private URL getConfigurationResource()
    {
        return this.getClassLoader().getResource(this.getConfiguration());
    }

    /**
     * Gets the classloader used for loading bankfile resources.
     * <p>The reference implementation will use the current thread's context
     * classloader and will fall back to the system classloader if the
     * current thread has no context classloader set.</p>
     *
     * @return the classloader to be used for loading bankfile resources.
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

    //------------------------------------------------PropertyBankfileProvider--
}

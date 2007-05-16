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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.BankleitzahlInfo;
import org.jdtaus.banking.BankleitzahlenVerzeichnis;
import org.jdtaus.banking.util.BankleitzahlenDatei;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.ContainerInitializer;
import org.jdtaus.core.container.ContextFactory;
import org.jdtaus.core.container.ContextInitializer;
import org.jdtaus.core.container.Dependency;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ImplementationException;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.container.Properties;
import org.jdtaus.core.container.Property;
import org.jdtaus.core.container.PropertyException;
import org.jdtaus.core.logging.spi.Logger;

/**
 * Directory of german bank codes.
 * <p>For further information see the
 * "<a href="doc-files/blz.pdf">Bankleitzahlen Richtlinie</a>". An updated
 * version of the document may be found at
 * <a href="http://www.bundesbank.de">Deutsche Bundesbank</a></p>.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 *
 * @see #initialize()
 */
public final class BundesbankBankleitzahlenVerzeichnis
    implements BankleitzahlenVerzeichnis,
    org.jdtaus.banking.spi.BankleitzahlenDatei, ContainerInitializer
{
    //--Implementation----------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(BundesbankBankleitzahlenVerzeichnis.class.getName());

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /**
     * Protected <code>BundesbankBankleitzahlenVerzeichnis</code> implementation constructor.
     *
     * @param meta Implementation meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    protected BundesbankBankleitzahlenVerzeichnis(final Implementation meta)
    {
        super();
        if(meta == null)
        {
            throw new NullPointerException("meta");
        }
        this.initializeProperties(meta.getProperties());
    }
    /**
     * Protected <code>BundesbankBankleitzahlenVerzeichnis</code> dependency constructor.
     *
     * @param meta dependency meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    protected BundesbankBankleitzahlenVerzeichnis(final Dependency meta)
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

    /** {@code BankleitzahlenDatei} delegate. */
    private BankleitzahlenDatei delegate;

    /**
     * Initializes the instance.
     *
     * @throws ImplementationException if initialization fails.
     *
     * @see #assertValidProperties()
     * @see #getFileResources()
     */
    public void initialize()
    {
        this.assertValidProperties();

        try
        {
            final URL[] rsrc = this.getFileResources();

            if(rsrc.length > 0)
            {
                delegate = new BankleitzahlenDatei(rsrc[0]);

                for(int i = 1; i < rsrc.length; i++)
                {
                    delegate.update(new BankleitzahlenDatei(rsrc[i]));
                }
            }
            else
            {
                throw new ImplementationException(META,
                    new IllegalArgumentException(
                    Integer.toString(rsrc.length)));

            }
        }
        catch(IOException e)
        {
            throw new ImplementationException(META, e);
        }
    }

    //----------------------------------------------------ContainerInitializer--
    //--Dependencies------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /** Configured <code>Logger</code> implementation. */
    private transient Logger _dependency0;

    /**
     * Gets the configured <code>Logger</code> implementation.
     *
     * @return the configured <code>Logger</code> implementation.
     */
    private Logger getLogger()
    {
        Logger ret = null;
        if(this._dependency0 != null)
        {
            ret = this._dependency0;
        }
        else
        {
            ret = (Logger) ContainerFactory.getContainer().
                getDependency(BundesbankBankleitzahlenVerzeichnis.class,
                "Logger");

            if(ModelFactory.getModel().getModules().
                getImplementation(BundesbankBankleitzahlenVerzeichnis.class.getName()).
                getDependencies().getDependency("Logger").
                isBound())
            {
                this._dependency0 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext()))
        {
            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }

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
    //--BankleitzahlenVerzeichnis-----------------------------------------------

    public BankleitzahlInfo getHeadOffice(final Bankleitzahl bankCode)
    {
        if(bankCode == null)
        {
            throw new NullPointerException("bankCode");
        }

        BankleitzahlInfo ret = null;
        final BankleitzahlInfo[] matches =
            this.findByBankCode(bankCode.intValue(), false);

        if(matches.length == 1)
        {
            ret = matches[0];
        }

        return ret;
    }

    public BankleitzahlInfo[] getBranchOffices(
        final Bankleitzahl bankCode)
    {

        if(bankCode == null)
        {
            throw new NullPointerException("bankCode");
        }

        return this.findByBankCode(bankCode.intValue(), true);
    }

    public BankleitzahlInfo[] search(final String name, final String postalCode,
        final String city, final boolean branchOffices)
    {
        final Pattern namePat;
        final Pattern postalPat;
        final Pattern cityPat;
        final NumberFormat plzFmt = new DecimalFormat("00000");
        final BankleitzahlInfo[] records = this.delegate.getRecords();
        final Collection col = new ArrayList(records.length);
        String plz;

        try
        {
            namePat = name != null ? Pattern.compile(".*" +
                name.toUpperCase() + ".*") : null;

            postalPat = postalCode != null ? Pattern.compile(
                postalCode.toUpperCase() + ".*") : null;

            cityPat = city != null ? Pattern.compile(".*" +
                city.toUpperCase() + ".*") : null;

            for(int i = records.length - 1; i >= 0; i--)
            {
                plz = plzFmt.format(records[i].getPostalCode());

                if(
                    (namePat == null ?
                        true : namePat.matcher(records[i].getName().
                    toUpperCase()).matches()) &&

                    (postalPat == null ? true : postalPat.matcher(plz).
                    matches()) &&

                    (cityPat == null ?
                        true : cityPat.matcher(records[i].getCity().
                    toUpperCase()).matches()) &&

                    (branchOffices ? true : records[i].isHeadOffice()))
                {
                    col.add(records[i]);
                }
            }

            return (BankleitzahlInfo[]) col.
                toArray(new BankleitzahlInfo[col.size()]);

        }
        catch(PatternSyntaxException e)
        {
            // TODO JDK 1.5: throw new IllegalArgumentException(e.getMessage(), e);
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    //-----------------------------------------------BankleitzahlenVerzeichnis--
    //--BankleitzahlenDatei-----------------------------------------------------

    public BankleitzahlInfo[] getRecords()
    {
        final String method = "getRecords()";
        final String className = org.jdtaus.banking.spi.
            BankleitzahlenDatei.class.getName();

        this.getLogger().warn(BundesbankBankleitzahlenVerzeichnisBundle.
            getDeprecationWarningMessage(Locale.getDefault()).format(
            new Object[] { method, className }));

        return this.delegate.getRecords();
    }

    public BankleitzahlInfo getRecord(final Integer serialNumber)
    {
        final String method = "getRecord(Integer)";
        final String className = org.jdtaus.banking.spi.
            BankleitzahlenDatei.class.getName();

        this.getLogger().warn(BundesbankBankleitzahlenVerzeichnisBundle.
            getDeprecationWarningMessage(Locale.getDefault()).format(
            new Object[] { method, className }));

        return this.delegate.getRecord(serialNumber);
    }

    public void read(final URL resource) throws IOException
    {
        final String method = "read(URL)";
        final String className = org.jdtaus.banking.spi.
            BankleitzahlenDatei.class.getName();

        this.getLogger().warn(BundesbankBankleitzahlenVerzeichnisBundle.
            getDeprecationWarningMessage(Locale.getDefault()).format(
            new Object[] { method, className }));

        this.delegate = new BankleitzahlenDatei(resource);
    }

    public void update(final org.jdtaus.banking.spi.BankleitzahlenDatei file)
    {
        final String method = "update(BankleitzahlenDatei)";
        final String className = org.jdtaus.banking.spi.
            BankleitzahlenDatei.class.getName();

        this.getLogger().warn(BundesbankBankleitzahlenVerzeichnisBundle.
            getDeprecationWarningMessage(Locale.getDefault()).format(
            new Object[] { method, className }));

        if(file == null)
        {
            throw new NullPointerException("file");
        }
    }

    //-----------------------------------------------------BankleitzahlenDatei--
    //--BundesbankBankleitzahlenVerzeichnis-------------------------------------

    /** Prefix used for property keys in the property file. */
    private static final String PREFIX = "BankleitzahlenDatei.";

    /** {@code Comparator} used to sort property keys in ascending order. */
    private static final Comparator PROPERTY_SORTER = new Comparator()
    {
        public int compare(final Object o1, final Object o2)
        {
            if(!(o1 instanceof String))
            {
                throw new IllegalArgumentException(o1.toString());
            }
            if(!(o2 instanceof String))
            {
                throw new IllegalArgumentException(o2.toString());
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

    /** Creates a new {@code BundesbankBankleitzahlenVerzeichnis} instance. */
    public BundesbankBankleitzahlenVerzeichnis()
    {
        this(BundesbankBankleitzahlenVerzeichnis.META);
        this.initialize();
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException if configured properties hold invalid values.
     */
    protected void assertValidProperties()
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
    protected URL getConfigurationResource()
    {
        return this.getClassLoader().getResource(this.getConfiguration());
    }

    /**
     * Gets the configured bankfile resources in the correct order for
     * sequential loading.
     *
     * @return the bankfile resources backing the implementation.
     *
     * @throws ImplementationException if reading configuration resources fails.
     *
     * @see #getConfigurationResource()
     * @see #getClassLoader()
     */
    protected URL[] getFileResources()
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

    /**
     * Gets all records matching {@code bankCode} with
     * {@code isHeadOffice() != branchOffices}.
     *
     * @param bankCode the bank code to return matching records for.
     * @param branchOffices {@code true} to return all known branch offices
     * matching {@code bankCode}; {@code false} to return all known head
     * offices matching {@code bankCode}.
     */
    protected BankleitzahlInfo[] findByBankCode(
        final int bankCode, final boolean branchOffices)
    {
        final BankleitzahlInfo[] records = this.delegate.getRecords();
        final Collection col = new ArrayList(records.length);

        for(int i = records.length - 1; i >= 0; i--)
        {
            if(records[i].getBankCode().intValue() == bankCode &&
                (records[i].isHeadOffice() != branchOffices) &&
                !col.add(records[i]))
            {
                throw new ImplementationException(META,
                    new IllegalStateException());

            }
        }

        return (BankleitzahlInfo[]) col.toArray(
            new BankleitzahlInfo[col.size()]);

    }

    /**
     * Gets the classloader used for loading bankfile resources.
     * <p>The reference implementation will use the current thread's context
     * classloader and will fall back to the system classloader if the
     * current thread has no context classloader set.</p>
     *
     * @return the classloader to be used for loading bankfile resources.
     */
    protected ClassLoader getClassLoader()
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

    //-------------------------------------BundesbankBankleitzahlenVerzeichnis--

}

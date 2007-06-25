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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.BankleitzahlExpirationException;
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
 * {@code BankleitzahlenVerzeichnis} implementation backed by bankfiles.
 * <p>This implementation reads bankfile resources holding
 * {@code BankleitzahlInfo} instances. Property {@code configuration} holds
 * the name of a property file specifying the bankfiles to load and the order
 * the files need to be loaded. Property {@code dataDirectory} holds the
 * name of the directory in which these bankfiles are located.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 *
 * @see #initialize()
 */
public final class BundesbankBankleitzahlenVerzeichnis
    implements BankleitzahlenVerzeichnis, ContainerInitializer
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

        p = meta.getProperty("dateOfExpirationPattern");
        this._dateOfExpirationPattern = (java.lang.String) p.getValue();


        p = meta.getProperty("dateOfExpirationText");
        this._dateOfExpirationText = (java.lang.String) p.getValue();


        p = meta.getProperty("configuration");
        this._configuration = (java.lang.String) p.getValue();


        p = meta.getProperty("dataDirectory");
        this._dataDirectory = (java.lang.String) p.getValue();

    }

    //------------------------------------------------------------Constructors--
    //--ContainerInitializer----------------------------------------------------

    /** {@code BankleitzahlenDatei} delegate. */
    private BankleitzahlenDatei delegate;

    /** Maps bankcodes to a list of outdated records. */
    private Map outdated = new HashMap(5000);

    /** Date of expiration. */
    private Date dateOfExpiration;

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
                this.delegate = new BankleitzahlenDatei(rsrc[0]);
                for(int i = 1; i < rsrc.length; i++)
                {
                    final BankleitzahlenDatei update =
                        new BankleitzahlenDatei(rsrc[i]);

                    // Build mapping of outdated records.
                    final BankleitzahlInfo[] records =
                        this.delegate.getRecords();

                    for(int j = records.length - 1; j >= 0; j--)
                    {
                        if(records[j].getChangeLabel() == 'D')
                        {
                            this.getLogger().debug(
                                BundesbankBankleitzahlenVerzeichnisBundle.
                                getOutdatedInfoMessage(Locale.getDefault()).
                                format(new Object[] { records[j].getBankCode().
                                    format(Bankleitzahl.ELECTRONIC_FORMAT)}));

                            List l = (List) this.outdated.get(
                                records[j].getBankCode());

                            if(l == null)
                            {
                                l = new LinkedList();
                                this.outdated.put(records[j].getBankCode(), l);
                            }

                            l.add(records[j]);
                        }
                    }

                    this.delegate.update(update);
                }
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
     * Property {@code dateOfExpirationPattern}.
     * @serial
     */
    private java.lang.String _dateOfExpirationPattern;

    /**
     * Gets the value of property <code>dateOfExpirationPattern</code>.
     *
     * @return the value of property <code>dateOfExpirationPattern</code>.
     */
    protected java.lang.String getDateOfExpirationPattern()
    {
        return this._dateOfExpirationPattern;
    }

    /**
     * Property {@code dateOfExpirationText}.
     * @serial
     */
    private java.lang.String _dateOfExpirationText;

    /**
     * Gets the value of property <code>dateOfExpirationText</code>.
     *
     * @return the value of property <code>dateOfExpirationText</code>.
     */
    protected java.lang.String getDateOfExpirationText()
    {
        return this._dateOfExpirationText;
    }

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

    public Date getDateOfExpiration()
    {
        return this.dateOfExpiration;
    }

    public BankleitzahlInfo getHeadOffice(final Bankleitzahl bankCode)
    throws BankleitzahlExpirationException
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
        else
        {
            this.checkOutdated(bankCode);
        }

        return ret;
    }

    public BankleitzahlInfo[] getBranchOffices(
        final Bankleitzahl bankCode) throws BankleitzahlExpirationException
    {
        if(bankCode == null)
        {
            throw new NullPointerException("bankCode");
        }

        final BankleitzahlInfo[] matches =
            this.findByBankCode(bankCode.intValue(), true);

        if(matches.length == 0)
        {
            this.checkOutdated(bankCode);
        }

        return matches;
    }

    public BankleitzahlInfo[] search(final String name, final String postalCode,
        final String city, final boolean branchOffices)
    {
        final Pattern namePat;
        final Pattern postalPat;
        final Pattern cityPat;
        final NumberFormat plzFmt = new DecimalFormat("00000");
        final BankleitzahlInfo[] records = this.delegate == null ?
            new BankleitzahlInfo[0] : this.delegate.getRecords();

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
        if(this.getDateOfExpirationText() == null ||
            this.getDateOfExpirationText().length() == 0)
        {
            throw new PropertyException("dateOfExpirationText",
                this.getDateOfExpirationText());

        }
        if(this.getDateOfExpirationPattern() == null ||
            this.getDateOfExpirationPattern().length() == 0)
        {
            throw new PropertyException("dateOfExpirationPattern",
                this.getDateOfExpirationPattern());

        }

        try
        {
            this.dateOfExpiration =
                new SimpleDateFormat(this.getDateOfExpirationPattern()).
                parse(this.getDateOfExpirationText());

        }
        catch(ParseException e)
        {
            throw new PropertyException("dateOfExpirationText",
                this.getDateOfExpirationText(), e);

        }
    }

    /**
     * Throws a {@code BankleitzahlExpirationException} if {@code bankCode}
     * is outdated.
     *
     * @param bankCode the Bankleitzahl to check for expiration.
     *
     * @throws NullPointerException if {@code bankCode} is {@code null}.
     */
    private void checkOutdated(final Bankleitzahl bankCode)
    throws BankleitzahlExpirationException
    {
        if(bankCode == null)
        {
            throw new NullPointerException("bankCode");
        }

        final List l = (List) this.outdated.get(bankCode);

        if(l != null)
        {
            // Finds the most recent record.
            BankleitzahlInfo record = null;
            BankleitzahlInfo replacement = null;

            for(Iterator it = l.iterator(); it.hasNext();)
            {
                record = (BankleitzahlInfo) it.next();
                if(record.getReplacingBankCode() != null)
                {
                    replacement = record;
                }
            }

            // Records specifying a replacement Bankleitzahl take precedence.
            throw new BankleitzahlExpirationException(replacement == null ?
                record : replacement);

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
    private URL[] getFileResources()
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
    private BankleitzahlInfo[] findByBankCode(
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

    //-------------------------------------BundesbankBankleitzahlenVerzeichnis--
}

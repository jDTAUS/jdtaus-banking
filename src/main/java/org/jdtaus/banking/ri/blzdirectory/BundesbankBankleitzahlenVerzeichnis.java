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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.BankleitzahlInfo;
import org.jdtaus.banking.BankleitzahlenVerzeichnis;
import org.jdtaus.banking.spi.BankleitzahlenDatei;
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
import org.jdtaus.core.monitor.Task;
import org.jdtaus.core.monitor.spi.TaskMonitor;
import org.jdtaus.core.text.Message;

/**
 * Directory of german bank codes.
 * <p>For further information see the
 * "<a href="doc-files/blz.pdf">Bankleitzahlen Richtlinie</a>". An updated
 * version of the document may be found at
 * <a href="http://www.bundesbank.de">Deutsche Bundesbank</a></p>.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class BundesbankBankleitzahlenVerzeichnis
    implements BankleitzahlenVerzeichnis, BankleitzahlenDatei,
    ContainerInitializer
{

    //--Implementation----------------------------------------------------------

    // This section is generated by jdtaus-source-plugin.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(BundesbankBankleitzahlenVerzeichnis.class.getName());

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

    // This section is generated by jdtaus-source-plugin.

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


        p = meta.getProperty("encoding");
        this._encoding = (java.lang.String) p.getValue();

    }

    //------------------------------------------------------------Constructors--
    //--Dependencies------------------------------------------------------------

    // This section is generated by jdtaus-source-plugin.

    /** Configured <code>TaskMonitor</code> implementation. */
    private transient TaskMonitor _dependency1;

    /**
     * Gets the configured <code>TaskMonitor</code> implementation.
     *
     * @return the configured <code>TaskMonitor</code> implementation.
     */
    private TaskMonitor getTaskMonitor()
    {
        TaskMonitor ret = null;
        if(this._dependency1 != null)
        {
           ret = this._dependency1;
        }
        else
        {
            ret = (TaskMonitor) ContainerFactory.getContainer().
                getDependency(BundesbankBankleitzahlenVerzeichnis.class,
                "TaskMonitor");

            if(ModelFactory.getModel().getModules().
                getImplementation(BundesbankBankleitzahlenVerzeichnis.class.getName()).
                getDependencies().getDependency("TaskMonitor").
                isBound())
            {
                this._dependency1 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext()))
        {
            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }
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

    // This section is generated by jdtaus-source-plugin.

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

    /**
     * Property {@code encoding}.
     * @serial
     */
    private java.lang.String _encoding;

    /**
     * Gets the value of property <code>encoding</code>.
     *
     * @return the value of property <code>encoding</code>.
     */
    protected java.lang.String getEncoding()
    {
        return this._encoding;
    }


    //--------------------------------------------------------------Properties--
    //--ContainerInitializer----------------------------------------------------

    /** Prefix used for property keys in the property file. */
    private static final String PREFIX = "BankleitzahlenDatei.";

    /** Holds the URL to initialize an instance with. */
    private static int initIndex = Integer.MIN_VALUE;

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

    /**
     * Initializes the instance.
     *
     * @throws IOError if reading the files fails.
     * @throws ImplementationException if initialization fails.
     */
    public void initialize()
    {
        BankleitzahlenDatei file;

        this.assertValidProperties();

        try
        {
            final URL[] rsrc = this.getFileResources();
            for(int i = 0; i < rsrc.length; i++)
            {
                if(i == 0)
                {
                    this.read(rsrc[i]);
                }
                else
                {
                    file = new BundesbankBankleitzahlenVerzeichnis(META);
                    file.read(rsrc[i]);
                    this.update(file);
                }
            }
        }
        catch(IOException e)
        {
            throw new ImplementationException(META, e);
        }
    }

    //----------------------------------------------------ContainerInitializer--
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
        BankleitzahlInfo rec;

        final Iterator it;
        final Pattern namePat;
        final Pattern postalPat;
        final Pattern cityPat;
        final Collection col = new ArrayList(this.records.size());
        final NumberFormat plzFmt = new DecimalFormat("00000");
        String plz;

        try
        {
            namePat = name != null ? Pattern.compile(".*" +
                name.toUpperCase() + ".*") : null;

            postalPat = postalCode != null ? Pattern.compile(
                postalCode.toUpperCase() + ".*") : null;

            cityPat = city != null ? Pattern.compile(".*" +
                city.toUpperCase() + ".*") : null;

            for(it = this.records.values().iterator(); it.hasNext();)
            {
                rec = (BankleitzahlInfo) it.next();
                plz = plzFmt.format(rec.getPostalCode());

                if(
                    (namePat == null ? true : namePat.matcher(rec.getName().
                    toUpperCase()).matches()) &&

                    (postalPat == null ? true : postalPat.matcher(plz).
                    matches()) &&

                    (cityPat == null ? true : cityPat.matcher(rec.getCity().
                    toUpperCase()).matches()) &&

                    (branchOffices ? true : rec.isHeadOffice()))
                {

                    col.add(rec);
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

    /** Records held by the instance. */
    private Map records = new HashMap(5000);
    private BankleitzahlInfo[] cachedRecords;

    public BankleitzahlInfo[] getRecords()
    {
        if(this.cachedRecords == null)
        {
            this.cachedRecords = (BankleitzahlInfo[]) this.records.values().
                toArray(new BankleitzahlInfo[this.records.size()]);

        }

        return this.cachedRecords;
    }

    public BankleitzahlInfo getRecord(final Integer serialNumber)
    {
        if(serialNumber == null)
        {
            throw new NullPointerException("serialNumber");
        }

        return (BankleitzahlInfo) this.records.get(serialNumber);
    }

    public void read(final URL resource) throws IOException
    {
        final BufferedReader reader;

        String line;
        InputStream stream = null;
        BankleitzahlInfo rec;

        if(resource == null)
        {
            throw new NullPointerException("resource");
        }

        this.records.clear();

        if(this.getLogger().isDebugEnabled())
        {
            this.getLogger().debug(BundesbankBankleitzahlenVerzeichnisBundle.
                getFileNameInfoMessage(Locale.getDefault()).
                format(new Object[] { resource.toExternalForm() }));

        }

        try
        {
            stream = resource.openStream();
            reader = new BufferedReader(new InputStreamReader(
                stream, this.getEncoding()));

            while((line = reader.readLine()) != null)
            {
                rec = new BankleitzahlInfo();
                rec.parse(line);

                if(this.records.put(rec.getSerialNumber(), rec) != null)
                {
                    throw new ImplementationException(META,
                        new IllegalArgumentException(rec.toString()));

                }
            }

            this.cachedRecords = null;
        }
        finally
        {
            if(stream != null)
            {
                stream.close();
            }
        }
    }

    public void update(final BankleitzahlenDatei file)
    {
        if(file == null)
        {
            throw new NullPointerException("file");
        }

        int i;
        final Iterator it;
        final boolean log = this.getLogger().isDebugEnabled();
        Task task;
        BankleitzahlInfo oldVersion;
        BankleitzahlInfo newVersion;

        task = new UpdateTask();
        task.setMinimum(0);
        task.setMaximum(file.getRecords().length);
        task.setProgress(0);
        task.setIndeterminate(false);
        this.getTaskMonitor().monitor(task);

        try
        {
            for(i = file.getRecords().length - 1; i >= 0; i--)
            {
                task.setProgress(task.getProgress() + 1);
                newVersion = file.getRecords()[i];
                if('A' == newVersion.getChangeLabel())
                {
                    if(this.records.put(
                        newVersion.getSerialNumber(), newVersion) != null)
                    {

                        throw new ImplementationException(META,
                            new IllegalArgumentException(
                            newVersion.toString()));

                    }

                    if(log)
                    {
                        this.getLogger().debug(
                            BundesbankBankleitzahlenVerzeichnisBundle.
                            getAddRecordInfoMessage(Locale.getDefault()).
                            format(new Object[] {
                            new Character(newVersion.getChangeLabel()),
                            newVersion.getSerialNumber() }));

                    }
                }
                else if('M' == newVersion.getChangeLabel() ||
                    'D' == newVersion.getChangeLabel())
                {

                    oldVersion = (BankleitzahlInfo) this.records.
                        get(newVersion.getSerialNumber());

                    if(oldVersion == null)
                    {
                        throw new ImplementationException(META,
                            new IllegalArgumentException(
                            newVersion.toString()));

                    }

                    this.records.put(newVersion.getSerialNumber(), newVersion);

                    if(log)
                    {
                        this.getLogger().debug(
                            BundesbankBankleitzahlenVerzeichnisBundle.
                            getModifyRecordInfoMessage(Locale.getDefault()).
                            format(new Object[] {
                            new Character(newVersion.getChangeLabel()),
                            newVersion.getSerialNumber() }));

                    }

                }
                else if('U' == newVersion.getChangeLabel())
                {
                    if(!this.records.containsKey(
                        newVersion.getSerialNumber()))
                    {

                        throw new ImplementationException(META,
                            new IllegalArgumentException(
                            newVersion.toString()));

                    }
                }
            }
        }
        finally
        {
            this.getTaskMonitor().finish(task);
        }

        task = new UpdateTask();
        task.setMinimum(0);
        task.setMaximum(this.records.size());
        task.setProgress(0);
        task.setIndeterminate(false);
        this.getTaskMonitor().monitor(task);

        try
        {
            for(it = this.records.values().iterator(); it.hasNext();)
            {
                task.setProgress(task.getProgress() + 1);
                oldVersion = (BankleitzahlInfo) it.next();

                if('D' == oldVersion.getChangeLabel())
                {
                    newVersion = file.getRecord(oldVersion.getSerialNumber());
                    if(newVersion == null)
                    {
                        it.remove();

                        if(log)
                        {
                            this.getLogger().debug(
                                BundesbankBankleitzahlenVerzeichnisBundle.
                                getRemoveRecordInfoMessage(Locale.getDefault()).
                                format(new Object[] {
                                new Character(oldVersion.getChangeLabel()),
                                oldVersion.getSerialNumber() }));

                        }

                    }
                }
            }
        }
        finally
        {
            this.getTaskMonitor().finish(task);
        }

        this.cachedRecords = null;
    }

    //-----------------------------------------------------BankleitzahlenDatei--
    //--BundesbankBankleitzahlenVerzeichnis-------------------------------------

    public static final class UpdateMessage extends Message
    {

        private static final Object[] NO_ARGS = {};

        public Object[] getFormatArguments(final Locale locale)
        {
            return NO_ARGS;
        }

        public String getText(final Locale locale)
        {
            return BundesbankBankleitzahlenVerzeichnisBundle.
                getUpdateTaskText(locale);

        }
    }

    public static final class UpdateTask extends Task
    {

        private final Message description = new UpdateMessage();

        public Message getDescription()
        {
            return this.description;
        }
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
        if(this.getEncoding() == null || this.getEncoding().length() == 0)
        {
            throw new PropertyException("encoding", this.getEncoding());
        }

        try
        {
            "".getBytes(this.getEncoding());
        }
        catch(UnsupportedEncodingException e)
        {
            throw new PropertyException("encoding", this.getEncoding(), e);
        }
    }

    /**
     * Gets an URL to the property file configured via property
     * {@code configuration} holding the files to load.
     *
     * @return an URL to a property file holding the files to load or
     * {@code null} if property {@code configuration} does not point to a
     * resource.
     */
    protected URL getConfigurationResource()
    {
        return this.getClassLoader().getResource(this.getConfiguration());
    }

    /**
     * Gets the configured file resources to load in the order the files need to
     * be loaded.
     *
     * @return the files to be loaded sorted in ascending order.
     *
     * @throws ImplementationException if reading configuration resources fails.
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
        BankleitzahlInfo rec;
        final Iterator it;
        final Collection col = new ArrayList(this.records.size());

        for(it = this.records.values().iterator(); it.hasNext();)
        {
            rec = (BankleitzahlInfo) it.next();
            if(rec.getBankCode().intValue() == bankCode &&
                (rec.isHeadOffice() != branchOffices))
            {

                if(!col.add(rec))
                {
                    throw new ImplementationException(META,
                        new IllegalStateException());

                }
            }
        }

        return (BankleitzahlInfo[]) col.toArray(
            new BankleitzahlInfo[col.size()]);

    }

    private ClassLoader getClassLoader()
    {
        ClassLoader classLoader = Thread.currentThread().
            getContextClassLoader();

        if(classLoader == null)
        {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        if(classLoader == null)
        {
            throw new ImplementationException(META,
                new NullPointerException("classLoader"));

        }

        return classLoader;
    }

    //-------------------------------------BundesbankBankleitzahlenVerzeichnis--

}

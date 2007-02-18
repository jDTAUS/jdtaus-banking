/*
 *  jDTAUS - DTAUS fileformat.
 *  Copyright (C) 2005 - 2007 Christian Schulte <cs@schulte.it>
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
package org.jdtaus.banking.spi.runtime;

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
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.BankleitzahlInfo;
import org.jdtaus.banking.BankleitzahlenVerzeichnis;
import org.jdtaus.banking.spi.BankleitzahlenDatei;
import org.jdtaus.core.container.ContainerError;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.ContainerInitializer;
import org.jdtaus.core.container.ContextFactory;
import org.jdtaus.core.container.ContextInitializer;
import org.jdtaus.core.container.Dependency;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.io.IOError;
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
 */
public class BundesbankBankleitzahlenVerzeichnis
    implements BankleitzahlenVerzeichnis, BankleitzahlenDatei,
    ContainerInitializer {

    //--Implementation----------------------------------------------------------

    // This section is generated by jdtaus-source-plugin.


    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

    // This section is generated by jdtaus-source-plugin.

    /** Protected <code>BundesbankBankleitzahlenVerzeichnis</code> implementation constructor.
    * @param meta Implementation meta-data.
    */
    protected BundesbankBankleitzahlenVerzeichnis(final Implementation meta) {
        super();
        this._configuration = (java.lang.String) meta.getProperties().
            getProperty("configuration").getValue();


        this._dataDirectory = (java.lang.String) meta.getProperties().
            getProperty("dataDirectory").getValue();


        this._encoding = (java.lang.String) meta.getProperties().
            getProperty("encoding").getValue();

        this.assertValidProperties();
    }
    /** Protected <code>BundesbankBankleitzahlenVerzeichnis</code> dependency constructor.
    * @param meta Dependency meta-data.
    */
    protected BundesbankBankleitzahlenVerzeichnis(final Dependency meta) {
        super();
        this._configuration = (java.lang.String) meta.getProperties().
            getProperty("configuration").getValue();


        this._dataDirectory = (java.lang.String) meta.getProperties().
            getProperty("dataDirectory").getValue();


        this._encoding = (java.lang.String) meta.getProperties().
            getProperty("encoding").getValue();

        this.assertValidProperties();
    }

    //------------------------------------------------------------Constructors--
    //--Dependencies------------------------------------------------------------

    // This section is generated by jdtaus-source-plugin.

    /** Configured <code>BankleitzahlenDatei</code> implementation. */
    private transient BankleitzahlenDatei _dependency1;

    /** <code>BankleitzahlenDatei</code> implementation getter. */
    private BankleitzahlenDatei getBankleitzahlenDatei() {
        BankleitzahlenDatei ret = null;
        if(this._dependency1 != null) {
           ret = this._dependency1;
        } else {
            ret = (BankleitzahlenDatei) ContainerFactory.getContainer().
                getDependency(BundesbankBankleitzahlenVerzeichnis.class,
                "BankleitzahlenDatei");

            if(ret == null) {
                throw new ContainerError("BankleitzahlenDatei");
            }

            if(ModelFactory.getModel().getModules().
                getImplementation(BundesbankBankleitzahlenVerzeichnis.class.getName()).
                getDependencies().getDependency("BankleitzahlenDatei").
                isBound()) {

                this._dependency1 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext())) {

            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }
    /** Configured <code>Logger</code> implementation. */
    private transient Logger _dependency0;

    /** <code>Logger</code> implementation getter. */
    private Logger getLogger() {
        Logger ret = null;
        if(this._dependency0 != null) {
           ret = this._dependency0;
        } else {
            ret = (Logger) ContainerFactory.getContainer().
                getDependency(BundesbankBankleitzahlenVerzeichnis.class,
                "Logger");

            if(ret == null) {
                throw new ContainerError("Logger");
            }

            if(ModelFactory.getModel().getModules().
                getImplementation(BundesbankBankleitzahlenVerzeichnis.class.getName()).
                getDependencies().getDependency("Logger").
                isBound()) {

                this._dependency0 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext())) {

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

    /** <code>configuration</code> property getter. */
    protected java.lang.String getConfiguration() {
        return this._configuration;
    }

    /**
     * Property {@code dataDirectory}.
     * @serial
     */
    private java.lang.String _dataDirectory;

    /** <code>dataDirectory</code> property getter. */
    protected java.lang.String getDataDirectory() {
        return this._dataDirectory;
    }

    /**
     * Property {@code encoding}.
     * @serial
     */
    private java.lang.String _encoding;

    /** <code>encoding</code> property getter. */
    protected java.lang.String getEncoding() {
        return this._encoding;
    }


    //--------------------------------------------------------------Properties--
    //--ContainerInitializer----------------------------------------------------

    /** Prefix used for property keys in the property file. */
    private static final String PREFIX = "BankleitzahlenDatei.";

    /** {@code Comparator} used to sort property keys in ascending order. */
    private static final Comparator PROPERTY_SORTER = new Comparator() {
        public int compare(final Object o1, final Object o2) {
            if(!(o1 instanceof String)) {
                throw new IllegalArgumentException(o1.toString());
            }
            if(!(o2 instanceof String)) {
                throw new IllegalArgumentException(o2.toString());
            }

            int ret = 0;
            final NumberFormat fmt = NumberFormat.getIntegerInstance();
            try {
                final Number o1Int = fmt.parse(((String) o1).
                    substring(PREFIX.length()));

                final Number o2Int = fmt.parse(((String) o2).
                    substring(PREFIX.length()));

                if(o1Int.longValue() < o2Int.longValue()) {
                    ret = -1;
                } else if(o1Int.longValue() > o2Int.longValue()) {
                    ret = 1;
                }

            } catch(ParseException e) {
                throw new ContainerError(e);
            }

            return ret;
        }
    };

    /**
     * Initializes the instance.
     *
     * @throws ContainerError if no files could be read.
     */
    public void initialize() throws ContainerError {
        BankleitzahlenDatei file;

        try {
            final URL[] rsrc = this.getFileResources();
            for(int i = 0; i < rsrc.length; i++) {
                if(i == 0) {
                    this.read(rsrc[i]);
                } else {
                    file = this.getBankleitzahlenDatei();
                    file.read(rsrc[i]);
                    this.update(file);
                }
            }
        } catch(IOError e) {
            throw new ContainerError(e);
        }
    }

    //----------------------------------------------------ContainerInitializer--
    //--BankleitzahlenVerzeichnis-----------------------------------------------

    public final BankleitzahlInfo getHeadOffice(final Bankleitzahl bankCode) {
        if(bankCode == null) {
            throw new NullPointerException("bankCode");
        }

        BankleitzahlInfo ret = null;
        final BankleitzahlInfo[] matches =
            this.findByBankCode(bankCode.intValue(), false);

        if(matches.length == 1) {
            ret = matches[1];
        }

        return ret;
    }

    public final BankleitzahlInfo[] getBranchOffices(
        final Bankleitzahl bankCode) {

        if(bankCode == null) {
            throw new NullPointerException("bankCode");
        }

        return this.findByBankCode(bankCode.intValue(), true);
    }

    public final BankleitzahlInfo[]
        search(final String name, final String postalCode, final String city) {

        BankleitzahlInfo rec;

        final Iterator it;
        final Pattern namePat;
        final Pattern postalPat;
        final Pattern cityPat;
        final Collection col = new ArrayList(this.records.size());
        final NumberFormat plzFmt = new DecimalFormat("00000");

        try {
            namePat = Pattern.compile(name == null ? ".*" :
                ".*(" + name.toUpperCase() + "|" +
                name.toLowerCase() + ").*");

            postalPat = Pattern.compile(postalCode == null ? ".*" :
                ".*(" + postalCode.toUpperCase() + "|" +
                postalCode.toLowerCase() + ").*");

            cityPat = Pattern.compile(city == null ? ".*" :
                ".*(" + city.toUpperCase() + "|" +
                city.toLowerCase() + ").*");

            for(it = this.records.values().iterator(); it.hasNext();) {
                rec = (BankleitzahlInfo) it.next();

                if(namePat.matcher(rec.getName()).matches() || postalPat.
                    matcher(plzFmt.format(rec.getPostalCode())).matches() ||
                    cityPat.matcher(rec.getCity()).matches()) {

                    col.add(rec);
                }

            }

            return (BankleitzahlInfo[]) col.
                toArray(new BankleitzahlInfo[col.size()]);

        } catch(PatternSyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    //-----------------------------------------------BankleitzahlenVerzeichnis--
    //--BankleitzahlenDatei-----------------------------------------------------

    /** Records held by the instance. */
    private Map records;
    private BankleitzahlInfo[] cachedRecords;

    public final BankleitzahlInfo[] getRecords() {
        if(this.cachedRecords == null) {
            this.cachedRecords = (BankleitzahlInfo[]) this.records.values().
                toArray(new BankleitzahlInfo[this.records.size()]);

        }

        return this.cachedRecords;
    }

    public final BankleitzahlInfo getRecord(final Integer serialNumber) {
        if(serialNumber == null) {
            throw new NullPointerException("serialNumber");
        }

        return (BankleitzahlInfo) this.records.get(serialNumber);
    }

    public final void read(final URL resource) {
        final BufferedReader reader;

        String line;
        InputStream stream = null;
        BankleitzahlInfo rec;

        if(resource == null) {
            throw new NullPointerException("resource");
        }

        this.records.clear();

        if(this.getLogger().isInfoEnabled()) {
            this.getLogger().info(BundesbankBankleitzahlenVerzeichnisBundle.
                getFileNameInfoMessage(Locale.getDefault()).
                format(new Object[] { resource.toExternalForm() }));

        }

        try {
            stream = resource.openStream();
            reader = new BufferedReader(new InputStreamReader(
                stream, this.getEncoding()));

            while((line = reader.readLine()) != null) {
                rec = new BankleitzahlInfo();
                rec.parse(line);

                if(this.records.put(rec.getSerialNumber(), rec) != null) {
                    throw new IllegalArgumentException(rec.toString());
                }
            }

            this.cachedRecords = null;
        } catch(IOException e) {
            throw new IllegalArgumentException(resource.toString(), e);
        } finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch(IOException e) {
                    throw new IllegalArgumentException(resource.toString(), e);
                }
            }
        }
    }

    public final void update(final BankleitzahlenDatei file) {
        if(file == null) {
            throw new NullPointerException("file");
        }

        int i;
        final Iterator it;
        final boolean logInfo = this.getLogger().isInfoEnabled();
        BankleitzahlInfo oldVersion;
        BankleitzahlInfo newVersion;

        for(i = file.getRecords().length - 1; i >= 0; i--) {
            newVersion = file.getRecords()[i];
            if('A' == newVersion.getChangeLabel()) {
                if(this.records.put(
                    newVersion.getSerialNumber(), newVersion) != null) {

                    throw new IllegalArgumentException(newVersion.toString());
                }

                if(logInfo) {
                    this.getLogger().info(
                        BundesbankBankleitzahlenVerzeichnisBundle.
                        getAddRecordInfoMessage(Locale.getDefault()).
                        format(new Object[] {
                        Character.valueOf(newVersion.getChangeLabel()),
                        newVersion.getSerialNumber() }));

                }
            } else if('M' == newVersion.getChangeLabel() ||
                'D' == newVersion.getChangeLabel()) {

                oldVersion = (BankleitzahlInfo) this.records.
                    get(newVersion.getSerialNumber());

                if(oldVersion == null) {
                    throw new IllegalArgumentException(newVersion.toString());
                }

                this.records.put(newVersion.getSerialNumber(), newVersion);

                if(logInfo) {
                    this.getLogger().info(
                        BundesbankBankleitzahlenVerzeichnisBundle.
                        getModifyRecordInfoMessage(Locale.getDefault()).
                        format(new Object[] {
                        Character.valueOf(newVersion.getChangeLabel()),
                        newVersion.getSerialNumber() }));

                }

            } else if('U' == newVersion.getChangeLabel()) {
                if(!this.records.containsKey(newVersion.getSerialNumber())) {
                    throw new IllegalArgumentException(newVersion.toString());
                }
            }
        }

        for(it = this.records.values().iterator(); it.hasNext();) {
            oldVersion = (BankleitzahlInfo) it.next();
            if('D' == oldVersion.getChangeLabel()) {
                newVersion = file.getRecord(oldVersion.getSerialNumber());
                if(newVersion == null) {
                    this.records.remove(oldVersion.getSerialNumber());

                    if(logInfo) {
                        this.getLogger().info(
                            BundesbankBankleitzahlenVerzeichnisBundle.
                            getRemoveRecordInfoMessage(Locale.getDefault()).
                            format(new Object[] {
                            Character.valueOf(oldVersion.getChangeLabel()),
                            newVersion.getSerialNumber() }));

                    }

                }
            }
        }

        this.cachedRecords = null;
    }

    //-----------------------------------------------------BankleitzahlenDatei--
    //--BundesbankBankleitzahlenVerzeichnis-------------------------------------

    /**
     * Checks configured properties.
     *
     * @throws ContainerError if configured properties hold invalid values.
     */
    protected void assertValidProperties() throws ContainerError {
        if(this.getDataDirectory() == null ||
            this.getDataDirectory().length() == 0) {

            throw new ContainerError("dataDirectory: " +
                this.getDataDirectory());

        }

        if(this.getConfiguration() == null ||
            this.getConfiguration().length() == 0 ||
            this.getConfigurationResource() == null) {

            throw new ContainerError("configuration: " +
                this.getConfiguration());

        }

        if(this.getEncoding() == null || this.getEncoding().length() == 0) {
            throw new ContainerError("encoding: " +
                this.getEncoding());

        }

        try {
            "".getBytes(this.getEncoding());
        } catch(UnsupportedEncodingException e) {
            throw new ContainerError(e);
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
    protected final URL getConfigurationResource() {
        return this.getClassLoader().getResource(this.getConfiguration());
    }

    /**
     * Gets the configured file resources to load in the order the files need to
     * be loaded.
     *
     * @return the files to be loaded sorted in ascending order.
     *
     * @throws IOError if accessing configuration resources fails.
     */
    protected final URL[] getFileResources() throws IOError {
        int i;
        String rsrc;
        InputStream stream = null;
        final URL[] ret;
        final Iterator it;
        final Map sorted = new TreeMap(PROPERTY_SORTER);
        final Properties props = new Properties();

        try {
            stream = this.getConfigurationResource().openStream();
            props.load(stream);
            sorted.putAll(props);
            ret = new URL[sorted.size()];
            for(it = sorted.values().iterator(), i = 0; it.hasNext(); i++) {
                rsrc = this.getDataDirectory() + '/' + it.next().toString();
                ret[i] = this.getClassLoader().getResource(rsrc);
                if(ret[i] == null) {
                    throw new IOError(rsrc);
                }
            }

            return ret;
        } catch(IOException e) {
            throw new IOError(e);
        } finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch(IOException e) {
                    throw new IOError(e);
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
    protected final BankleitzahlInfo[]
        findByBankCode(final int bankCode, final boolean branchOffices) {

        BankleitzahlInfo rec;
        final Iterator it;
        final Collection col = new ArrayList(this.records.size());

        for(it = this.records.values().iterator(); it.hasNext();) {
            rec = (BankleitzahlInfo) it.next();
            if(rec.getBankCode().intValue() == bankCode &&
                (rec.isHeadOffice() != branchOffices)) {

                if(!col.add(rec)) {
                    throw new IllegalStateException();
                }
            }
        }

        return (BankleitzahlInfo[]) col.toArray(
            new BankleitzahlInfo[col.size()]);

    }

    private final ClassLoader getClassLoader() {
        ClassLoader c = Thread.currentThread().getContextClassLoader();
        if(c == null) {
            c = ClassLoader.getSystemClassLoader();
        }
        if(c == null) {
            throw new IllegalStateException();
        }

        return c;
    }

    //-------------------------------------BundesbankBankleitzahlenVerzeichnis--

}

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jdtaus.banking.spi.CurrencyMapper;
import org.jdtaus.banking.spi.UnsupportedCurrencyException;
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
import org.jdtaus.core.container.Specification;
import org.jdtaus.core.logging.spi.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Currency directory implementation backed by XML files.
 * <p>This implementation uses XML resources provided by any available
 * {@link CurrenciesProvider} implementation. Resources with a {@code file} URI
 * scheme are monitored for changes by querying the last modification
 * time. Monitoring is controlled by property {@code reloadIntervalMillis}.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 *
 * @see #initialize()
 */
public final class XMLCurrencyDirectory
    implements CurrencyMapper, ContainerInitializer
{
    //--Constants---------------------------------------------------------------

    /** JAXP configuration key to the Schema implementation attribute. */
    public static final String SCHEMA_LANGUAGE_KEY =
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /** JAXP Schema implementation to use. */
    public static final String SCHEMA_LANGUAGE =
        "http://www.w3.org/2001/XMLSchema";

    /** JAXP configuration key for setting the Schema source. */
    public static final String SCHEMA_SOURCE_KEY =
        "http://java.sun.com/xml/jaxp/properties/schemaSource";

    /** jDTAUS {@code currencies} namespace URI. */
    public static final String MODEL_NS =
        "http://jdtaus.org/banking/xml/currencies";

    /** Location of the jdtaus-currencies-1.0.xsd schema. */
    public static final String MODEL_XSD =
        "org/jdtaus/banking/xml/currencies/jdtaus-currencies-1.0.xsd";

    /** Version supported by this implementation. */
    public static final String SUPPORTED_VERSION = "1.0";

    //---------------------------------------------------------------Constants--
    //--Implementation----------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(XMLCurrencyDirectory.class.getName());

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /**
     * Protected <code>XMLCurrencyDirectory</code> implementation constructor.
     *
     * @param meta Implementation meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    protected XMLCurrencyDirectory(final Implementation meta)
    {
        super();
        if(meta == null)
        {
            throw new NullPointerException("meta");
        }
        this.initializeProperties(meta.getProperties());
    }
    /**
     * Protected <code>XMLCurrencyDirectory</code> dependency constructor.
     *
     * @param meta dependency meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    protected XMLCurrencyDirectory(final Dependency meta)
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

        p = meta.getProperty("reloadIntervalMillis");
        this._reloadIntervalMillis = ((java.lang.Long) p.getValue()).longValue();

    }

    //------------------------------------------------------------Constructors--
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
                getDependency(XMLCurrencyDirectory.class,
                "Logger");

            if(ModelFactory.getModel().getModules().
                getImplementation(XMLCurrencyDirectory.class.getName()).
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
     * Property {@code reloadIntervalMillis}.
     * @serial
     */
    private long _reloadIntervalMillis;

    /**
     * Gets the value of property <code>reloadIntervalMillis</code>.
     *
     * @return the value of property <code>reloadIntervalMillis</code>.
     */
    protected long getReloadIntervalMillis()
    {
        return this._reloadIntervalMillis;
    }


    //--------------------------------------------------------------Properties--
    //--ContainerInitializer----------------------------------------------------

    /** Maps ISO codes to currency instances. */
    private Map isoMap;

    /** Maps DTAUS codes to currency instances. */
    private Map dtausMap;

    /**
     * Initializes the instance to hold the parsed XML currency instances.
     *
     * @throws ImplementationException if no XML resources can be parsed.
     *
     * @see #assertValidProperties()
     * @see #parseResources()
     * @see #transformDocument(Document)
     */
    public void initialize()
    {
        this.assertValidProperties();

        this.monitorMap = new HashMap();
        this.lastCheck = System.currentTimeMillis();

        try
        {
            final Document docs[] = this.parseResources();
            final Collection col = new LinkedList();

            for(int i = docs.length - 1; i >= 0; i--)
            {
                col.addAll(Arrays.asList(this.transformDocument(docs[i])));
            }

            this.isoMap = new HashMap(col.size());
            this.dtausMap = new HashMap(col.size());

            for(Iterator it = col.iterator(); it.hasNext();)
            {
                final XMLCurrency currency = (XMLCurrency) it.next();
                if(this.isoMap.put(currency.getIsoCode(), currency) != null ||
                    (currency.getDtausCode() != null && this.dtausMap.
                    put(currency.getDtausCode(), currency) != null))
                {
                    throw new DuplicateCurrencyException(
                        Currency.getInstance(currency.getIsoCode()));

                }
            }
        }
        catch(IOException e)
        {
            throw new ImplementationException(META, e);
        }
        catch(ParserConfigurationException e)
        {
            throw new ImplementationException(META, e);
        }
        catch(SAXException e)
        {
            throw new ImplementationException(META, e);
        }
        catch(ParseException e)
        {
            throw new ImplementationException(META, e);
        }
    }

    //----------------------------------------------------ContainerInitializer--
    //--CurrencyDirectory-------------------------------------------------------

    public Currency[] getDtausCurrencies(final Date date)
    {
        if(date == null)
        {
            throw new NullPointerException("date");
        }

        this.checkForModifications();

        final Collection col = new LinkedList();
        for(Iterator it = this.isoMap.keySet().iterator(); it.hasNext();)
        {
            final String isoCode = (String) it.next();
            final XMLCurrency currency = (XMLCurrency) this.isoMap.get(isoCode);
            if((date.equals(currency.getStartDate()) ||
                date.after(currency.getStartDate())) &&
                (currency.getEndDate() == null ||
                date.equals(currency.getEndDate()) ||
                date.before(currency.getEndDate())))
            {
                col.add(Currency.getInstance(isoCode));
            }
        }

        return (Currency[]) col.toArray(new Currency[col.size()]);
    }

    public Currency getDtausCurrency(final char code, final Date date)
    {
        if(date == null)
        {
            throw new NullPointerException("date");
        }

        this.checkForModifications();

        final XMLCurrency currency =
            (XMLCurrency) this.dtausMap.get(new Character(code));

        Currency ret = null;

        if(currency != null && (date.equals(currency.getStartDate()) ||
            date.after(currency.getStartDate())) &&
            (currency.getEndDate() == null ||
            date.equals(currency.getEndDate()) ||
            date.before(currency.getEndDate())))
        {
            ret = Currency.getInstance(currency.getIsoCode());
        }

        return ret;
    }

    public char getDtausCode(final Currency currency, final Date date)
    {
        if(currency == null)
        {
            throw new NullPointerException("currency");
        }
        if(date == null)
        {
            throw new NullPointerException("date");
        }

        this.checkForModifications();

        final XMLCurrency xml = (XMLCurrency) this.isoMap.
            get(currency.getCurrencyCode());

        if(xml != null && xml.getDtausCode() != null &&
            (date.equals(xml.getStartDate()) ||
            date.after(xml.getStartDate())) &&
            (xml.getEndDate() == null || date.equals(xml.getEndDate()) ||
            date.before(xml.getEndDate())))
        {
            return xml.getDtausCode().charValue();
        }

        throw new UnsupportedCurrencyException(
            currency.getCurrencyCode(), date);

    }

    //-------------------------------------------------------CurrencyDirectory--
    //--XMLCurrencyDirectory----------------------------------------------------

    /** Maps {@code File} instances to theire last modification timestamp. */
    private Map monitorMap;

    /** Holds the timestamp resources got checked for modifications. */
    private long lastCheck;

    /** Creates a new {@code XMLCurrencyDirectory} instance. */
    public XMLCurrencyDirectory()
    {
        this(XMLCurrencyDirectory.META);
        this.initialize();
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException if properties hold invalid values.
     */
    private void assertValidProperties()
    {
        if(this.getReloadIntervalMillis() < 0L)
        {
            throw new PropertyException("reloadIntervalMillis",
                Long.toString(this.getReloadIntervalMillis()));

        }
    }

    /**
     * Gets XML resources provided by any available {@code CurrenciesProvider}
     * implementation.
     *
     * @return XML resources provided by any available
     * {@code CurrenciesProvider} implementation.
     *
     * @throws IOException if getting the resources fails.
     *
     * @see CurrenciesProvider
     */
    private URL[] getResources() throws IOException
    {
        final Collection resources = new HashSet();
        final Specification providerSpec = ModelFactory.getModel().getModules().
            getSpecification(CurrenciesProvider.class.getName());

        for(int i = providerSpec.getImplementations().size() - 1; i >= 0; i--)
        {
            final CurrenciesProvider provider =
                (CurrenciesProvider) ContainerFactory.getContainer().
                getImplementation(CurrenciesProvider.class,
                providerSpec.getImplementations().getImplementation(i).
                getName());

            resources.addAll(Arrays.asList(provider.getResources()));
        }

        return (URL[]) resources.toArray(new URL[resources.size()]);
    }

    /**
     * Adds a resource to the list of resources to monitor for changes.
     *
     * @param url the URL of the resource to monitor for changes.
     *
     * @throws NullPointerException if {@code url} is {@code null}.
     */
    private void monitorResource(final URL url)
    {
        if(url == null)
        {
            throw new NullPointerException("url");
        }

        try
        {
            final File file = new File(new URI(url.toString()));
            this.monitorMap.put(file, new Long(file.lastModified()));
            this.getLogger().info(XMLCurrencyDirectoryBundle.
                getMonitoringInfoMessage(Locale.getDefault()).format(
                new Object[] { file.getAbsolutePath() }));

        }
        catch(IllegalArgumentException e)
        {
            this.getLogger().warn(XMLCurrencyDirectoryBundle.
                getNotMonitoringWarningMessage(Locale.getDefault()).
                format(new Object[] { url.toExternalForm(), e.getMessage() }));

        }
        catch(URISyntaxException e)
        {
            this.getLogger().warn(XMLCurrencyDirectoryBundle.
                getNotMonitoringWarningMessage(Locale.getDefault()).
                format(new Object[] { url.toExternalForm(), e.getMessage() }));

        }
    }

    /** Reloads the XML files when detecting a change. */
    private void checkForModifications()
    {
        if(System.currentTimeMillis() - this.lastCheck >
            this.getReloadIntervalMillis() && this.monitorMap.size() > 0)
        {
            for(Iterator it = this.monitorMap.entrySet().
                iterator(); it.hasNext();)
            {
                final Map.Entry entry = (Map.Entry) it.next();
                final File file = (File) entry.getKey();
                final Long lastModified = (Long) entry.getValue();

                assert lastModified != null : "Expected modification time.";

                if(file.lastModified() != lastModified.longValue())
                {
                    this.getLogger().info(XMLCurrencyDirectoryBundle.
                        getChangeInfoMessage(Locale.getDefault()).format(
                        new Object[] { file.getAbsolutePath() }));

                    this.initialize();
                    break;
                }
            }
        }
    }

    /**
     * Parses all XML resources.
     *
     * @return the parsed XML documents.
     *
     * @see #getResources()
     * @see #getDocumentBuilder()
     *
     * @throws IOException if reading the schema fails.
     * @throws ParserConfigurationException if no supported XML parser runtime
     * is available.
     * @throws SAXException if parsing fails.
     */
    private Document[] parseResources() throws
        IOException, ParserConfigurationException, SAXException
    {
        InputStream stream = null;

        final URL[] resources = this.getResources();
        final DocumentBuilder parser = this.getDocumentBuilder();
        final Document[] ret = new Document[resources.length];

        for(int i = resources.length - 1; i >= 0; i--)
        {
            try
            {
                this.monitorResource(resources[i]);
                stream = resources[i].openStream();
                ret[i] = parser.parse(stream);
            }
            finally
            {
                if(stream != null)
                {
                    stream.close();
                    stream = null;
                }
            }
        }

        return ret;
    }

    /**
     * Transforms a XML document to the currency instances it contains.
     *
     * @param doc the document to transform.
     *
     * @return an array of the currency instances from the given document.
     *
     * @throws ParseException if parsing values fails.
     */
    private XMLCurrency[] transformDocument(final Document doc)
    throws ParseException
    {
        Element e;
        String str;
        NodeList l;
        XMLCurrency cur;
        final Calendar cal = Calendar.getInstance();
        final Collection col = new ArrayList(500);

        l = doc.getDocumentElement().getElementsByTagNameNS(
            XMLCurrencyDirectory.MODEL_NS, "currency");

        for(int i = l.getLength() - 1; i >= 0; i--)
        {
            e = (Element) l.item(i);
            cur = new XMLCurrency();
            cur.setIsoCode(e.getAttributeNS(XMLCurrencyDirectory.MODEL_NS,
                "isoCode"));

            str = e.getAttributeNS(XMLCurrencyDirectory.MODEL_NS,
                "dtausCode");

            cur.setDtausCode(str != null && str.length() > 0 ?
                new Character(str.charAt(0)) : null);

            str = e.getAttributeNS(XMLCurrencyDirectory.MODEL_NS,
                "startDate");

            cur.setStartDate(new SimpleDateFormat("yyyy-MM-dd").parse(str));

            str = e.getAttributeNS(XMLCurrencyDirectory.MODEL_NS,
                "endDate");

            if(str != null && str.length() > 0)
            {
                cal.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(str));
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                cur.setEndDate(cal.getTime());
            }

            col.add(cur);
        }

        return (XMLCurrency[]) col.toArray(new XMLCurrency[col.size()]);
    }

    /**
     * Creates a new {@code DocumentBuilder} to use for parsing the XML
     * resources.
     * <p>This method tries to set the following JAXP properties on the system's
     * default XML parser:
     * <ul>
     * <li>{@code http://java.sun.com/xml/jaxp/properties/schemaLanguage} set to
     * {@code http://www.w3.org/2001/XMLSchema}</li>
     * <li>{@code http://java.sun.com/xml/jaxp/properties/schemaSource} set to
     * an {@code InputStream} to the XML schema to use for validating
     * resources</li>
     * </ul> When setting one of these properties fails, a non-validating
     * {@code DocumentBuilder} is returned and a warning message is logged.</p>
     *
     * @return a new {@code DocumentBuilder} to be used for parsing the
     * XML resources.
     *
     * @throws IOException if reading the schema fails.
     * @throws ParserConfigurationException if no supported XML parser runtime
     * is available.
     */
    private DocumentBuilder getDocumentBuilder() throws IOException,
        ParserConfigurationException
    {
        final DocumentBuilder xmlBuilder;
        final DocumentBuilderFactory xmlFactory =
            DocumentBuilderFactory.newInstance();

        xmlFactory.setNamespaceAware(true);
        try
        {
            xmlFactory.setValidating(true);
            xmlFactory.setAttribute(
                XMLCurrencyDirectory.SCHEMA_LANGUAGE_KEY,
                XMLCurrencyDirectory.SCHEMA_LANGUAGE);

            final URL schema = this.getClassLoader().getResource(
                XMLCurrencyDirectory.MODEL_XSD);

            xmlFactory.setAttribute(
                XMLCurrencyDirectory.SCHEMA_SOURCE_KEY,
                schema.openStream());

        }
        catch(IllegalArgumentException e)
        {
            this.getLogger().warn(XMLCurrencyDirectoryBundle.
                getNoJAXPValidationWarningMessage(Locale.getDefault()).
                format(new Object[] { e.getMessage() }));

            xmlFactory.setValidating(false);
        }

        xmlBuilder = xmlFactory.newDocumentBuilder();
        xmlBuilder.setErrorHandler(new ErrorHandler()
        {
            public void warning(final SAXParseException e)
            {
                getLogger().warn(e);
            }
            public void fatalError(final SAXParseException e)
            {
                getLogger().fatal(e);
                throw new ImplementationException(META, e);
            }
            public void error(final SAXParseException e)
            {
                getLogger().error(e);
            }
        });

        return xmlBuilder;
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
        if(classLoader == null)
        {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        assert classLoader != null :
            "Expected ClassLoader.getSystemClassLoader() to not return null.";

        return classLoader;
    }

    //----------------------------------------------------XMLCurrencyDirectory--
}

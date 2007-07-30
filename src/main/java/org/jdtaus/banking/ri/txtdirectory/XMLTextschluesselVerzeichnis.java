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
package org.jdtaus.banking.ri.txtdirectory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jdtaus.banking.Textschluessel;
import org.jdtaus.banking.TextschluesselVerzeichnis;
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
 * Textschlüssel directory implementation backed by XML files.
 * <p>This implementation uses XML resources provided by any available
 * {@link TextschluesselProvider} implementation. Resources with a {@code file}
 * URI scheme are monitored for changes by querying the last modification
 * time. Monitoring is controlled by property {@code reloadIntervalMillis}.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 *
 * @see #initialize()
 */
public final class XMLTextschluesselVerzeichnis
    implements TextschluesselVerzeichnis, ContainerInitializer
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

    /** jDTAUS {@code textschluessel} namespace URI. */
    public static final String MODEL_NS =
        "http://jdtaus.org/banking/xml/textschluessel";

    /** Location of the jdtaus-textschluessel-1.0.xsd schema. */
    public static final String MODEL_XSD =
        "org/jdtaus/banking/xml/textschluessel/jdtaus-textschluessel-1.1.xsd";

    /** Version supported by this implementation. */
    public static final String SUPPORTED_VERSION = "1.1";

    //---------------------------------------------------------------Constants--
    //--Implementation----------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausImplementation
    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(XMLTextschluesselVerzeichnis.class.getName());
// </editor-fold>//GEN-END:jdtausImplementation

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausConstructors
    // This section is managed by jdtaus-container-mojo.

    /**
     * <code>XMLTextschluesselVerzeichnis</code> implementation constructor.
     *
     * @param meta Implementation meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    private XMLTextschluesselVerzeichnis(final Implementation meta)
    {
        super();
        if(meta == null)
        {
            throw new NullPointerException("meta");
        }
        this.initializeProperties(meta.getProperties());
    }
    /**
     * <code>XMLTextschluesselVerzeichnis</code> dependency constructor.
     *
     * @param meta dependency meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    private XMLTextschluesselVerzeichnis(final Dependency meta)
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

        p = meta.getProperty("reloadIntervalMillis");
        this._reloadIntervalMillis = ((java.lang.Long) p.getValue()).longValue();

    }
// </editor-fold>//GEN-END:jdtausConstructors

    //------------------------------------------------------------Constructors--
    //--Dependencies------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
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
                getDependency(XMLTextschluesselVerzeichnis.class,
                "Logger");

            if(ModelFactory.getModel().getModules().
                getImplementation(XMLTextschluesselVerzeichnis.class.getName()).
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
// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
    //--Properties--------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausProperties
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
    private long getReloadIntervalMillis()
    {
        return this._reloadIntervalMillis;
    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
    //--ContainerInitializer----------------------------------------------------

    /** Holds the loaded Textschlüssel instances. */
    private Textschluessel[] instances;

    /**
     * Initializes the instance to hold the parsed XML Textschluessel instances.
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

            final Map types = new HashMap(col.size());
            final Collection checked = new ArrayList(col.size());

            for(Iterator it = col.iterator(); it.hasNext();)
            {
                Map keys;
                final Textschluessel i = (Textschluessel) it.next();
                final Integer key = new Integer(i.getKey());
                final Integer ext = new Integer(i.getExtension());

                if((keys = (Map) types.get(key)) == null)
                {
                    keys = new HashMap();
                    types.put(key, keys);
                }

                if(keys.put(ext, i) != null)
                {
                    throw new DuplicateTextschluesselException(i);
                }

                checked.add(i);
            }

            this.instances = (Textschluessel[]) checked.
                toArray(new Textschluessel[checked.size()]);

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
    }

    //----------------------------------------------------ContainerInitializer--
    //--TextschluesselVerzeichnis-----------------------------------------------

    public Textschluessel[] getTextschluessel()
    {
        this.checkForModifications();

        final Textschluessel[] ret = new Textschluessel[this.instances.length];
        for(int i = ret.length - 1; i >= 0; i--)
        {
            ret[i] = (Textschluessel) this.instances[i].clone();
        }

        return ret;
    }

    public Textschluessel getTextschluessel(int key, int extension)
    {
        if(key < 0 || key > 99)
        {
            throw new IllegalArgumentException(Integer.toString(key));
        }
        if(extension < 0 || extension > 999)
        {
            throw new IllegalArgumentException(Integer.toString(extension));
        }

        this.checkForModifications();

        Textschluessel ret = null;

        for(int i = this.instances.length - 1; i >= 0; i--)
        {
            if(this.instances[i].getKey() == key)
            {
                if(this.instances[i].isVariable())
                {
                    ret = (Textschluessel) this.instances[i].clone();
                    break;
                }
                else
                {
                    if(this.instances[i].getExtension() == extension)
                    {
                        ret = (Textschluessel) this.instances[i].clone();
                        break;
                    }
                }
            }
        }

        return ret;
    }

    public Textschluessel[] search(boolean debit, boolean remittance)
    {
        this.checkForModifications();

        final Collection col = new ArrayList(this.instances.length);

        for(int i = this.instances.length - 1; i >= 0; i--)
        {
            if(this.instances[i].isDebit() == debit &&
                this.instances[i].isRemittance() == remittance)
            {
                col.add(this.instances[i].clone());
            }
        }

        return (Textschluessel[]) col.toArray(new Textschluessel[col.size()]);
    }

    //-----------------------------------------------TextschluesselVerzeichnis--
    //--XMLTextschluesselVerzeichnis--------------------------------------------

    /** Maps {@code File} instances to theire last modification timestamp. */
    private Map monitorMap;

    /** Holds the timestamp resources got checked for modifications. */
    private long lastCheck;

    /** Creates a new {@code XMLTextschluesselVerzeichnis} instance. */
    public XMLTextschluesselVerzeichnis()
    {
        this(XMLTextschluesselVerzeichnis.META);
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
     * Gets XML resources provided by any available
     * {@code TextschluesselProvider} implementation.
     *
     * @return XML resources provided by any available
     * {@code TextschluesselProvider} implementation.
     *
     * @throws IOException if getting the resources fails.
     *
     * @see TextschluesselProvider
     */
    private URL[] getResources() throws IOException
    {
        final Collection resources = new HashSet();
        final Specification providerSpec = ModelFactory.getModel().getModules().
            getSpecification(TextschluesselProvider.class.getName());

        for(int i = providerSpec.getImplementations().size() - 1; i >= 0; i--)
        {
            final TextschluesselProvider provider =
                (TextschluesselProvider) ContainerFactory.getContainer().
                getImplementation(TextschluesselProvider.class,
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
            this.getLogger().info(XMLTextschluesselVerzeichnisBundle.
                getMonitoringInfoMessage(Locale.getDefault()).format(
                new Object[] { file.getAbsolutePath() }));

        }
        catch(IllegalArgumentException e)
        {
            this.getLogger().warn(XMLTextschluesselVerzeichnisBundle.
                getNotMonitoringWarningMessage(Locale.getDefault()).
                format(new Object[] { url.toExternalForm(), e.getMessage() }));

        }
        catch(URISyntaxException e)
        {
            this.getLogger().warn(XMLTextschluesselVerzeichnisBundle.
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
                    this.getLogger().info(XMLTextschluesselVerzeichnisBundle.
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
     * Transforms a XML document to the Textschluessel instances it contains.
     *
     * @param doc the document to transform.
     *
     * @return an array of Textschluessel instances from the given document.
     *
     * @see #transformTextschluessel(Textschluessel, Element)
     */
    private Textschluessel[] transformDocument(final Document doc)
    {
        Element e;
        String str;
        NodeList l;
        Textschluessel key;
        final Collection col = new ArrayList(500);

        l = doc.getDocumentElement().getElementsByTagNameNS(
            XMLTextschluesselVerzeichnis.MODEL_NS, "transactionTypes");

        l = ((Element) l.item(0)).getElementsByTagNameNS(
            XMLTextschluesselVerzeichnis.MODEL_NS, "transactionType");

        for(int i = l.getLength() - 1; i >= 0; i--)
        {
            e = (Element) l.item(i);
            key = new Textschluessel();
            str = e.getAttributeNS(
                XMLTextschluesselVerzeichnis.MODEL_NS,
                "type");

            key.setDebit("DEBIT".equals(str));
            key.setRemittance("REMITTANCE".equals(str));

            str = e.getAttributeNS(
                XMLTextschluesselVerzeichnis.MODEL_NS,
                "key");

            key.setKey(Integer.valueOf(str).intValue());

            str = e.getAttributeNS(
                XMLTextschluesselVerzeichnis.MODEL_NS,
                "extension");

            if("VARIABLE".equals(str))
            {
                key.setVariable(true);
                key.setExtension(0);
            }
            else
            {
                key.setExtension(Integer.valueOf(str).intValue());
            }

            this.transformTextschluessel(key, e);
            col.add(key);
        }

        return (Textschluessel[]) col.toArray(new Textschluessel[col.size()]);
    }

    /**
     * Transforms a {@code &lt;transactionType&gt;} element to the corresponding
     * {@code Textschluessel} instance.
     *
     * @param key the instance to be populated with data.
     * @param xmlKey the XML element to read the data for {@code key} from.
     *
     * @throws NullPointerException if either {@code key} or {@code xmlKey} is
     * {@code null}.
     */
    private void transformTextschluessel(final Textschluessel key,
        final Element xmlKey)
    {
        String lang;
        String txt;
        Element e;
        final NodeList l = xmlKey.getElementsByTagNameNS(
            XMLTextschluesselVerzeichnis.MODEL_NS, "description");

        for(int i = l.getLength() - 1; i >= 0; i--)
        {
            e = (Element) l.item(i);
            lang = e.getAttributeNS(XMLTextschluesselVerzeichnis.MODEL_NS,
                "language");

            txt = e.getFirstChild().getNodeValue();
            key.setShortDescription(new Locale(lang), txt);
        }
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
     * @return a new {@code DocumentBuilder} to be used for parsing the XML
     * resources.
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
                XMLTextschluesselVerzeichnis.SCHEMA_LANGUAGE_KEY,
                XMLTextschluesselVerzeichnis.SCHEMA_LANGUAGE);

            final URL schema = this.getClassLoader().getResource(
                XMLTextschluesselVerzeichnis.MODEL_XSD);

            xmlFactory.setAttribute(
                XMLTextschluesselVerzeichnis.SCHEMA_SOURCE_KEY,
                schema.openStream());

        }
        catch(IllegalArgumentException e)
        {
            this.getLogger().warn(XMLTextschluesselVerzeichnisBundle.
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

    //--------------------------------------------XMLTextschluesselVerzeichnis--
}

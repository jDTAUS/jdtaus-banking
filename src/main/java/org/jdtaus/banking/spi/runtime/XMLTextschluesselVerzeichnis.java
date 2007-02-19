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
package org.jdtaus.banking.spi.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jdtaus.banking.Textschluessel;
import org.jdtaus.banking.TextschluesselVerzeichnis;
import org.jdtaus.core.container.ContainerError;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.ContainerInitializer;
import org.jdtaus.core.container.ContextFactory;
import org.jdtaus.core.container.ContextInitializer;
import org.jdtaus.core.container.Dependency;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ImplementationError;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.container.PropertyError;
import org.jdtaus.core.logging.spi.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Textschlüssel directory implementation backed by XML files.
 * <p>This implementation reads XML resources from the classpath holding
 * Textschlüssel instances. Property {@code resource} holds the name of the
 * XML resources to load and property {@code dataDir} holds the directory in
 * which to look for these resources. See
 * <a href="http://www.jdtaus.org/jdtaus-banking/1.0.x/jdtaus-ri-textschluesselverzeichnis/jdtaus-textschluessel.xsd">
 * jdtaus-textschluessel.xsd</a> for further information</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class XMLTextschluesselVerzeichnis implements
    TextschluesselVerzeichnis, ContainerInitializer {

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

    /** jDTAUS {@code Model} namespace URI. */
    public static final String MODEL_NS =
        "http://jdtaus.org/banking/xml";

    /** Location of the jdtaus-textschluessel.xsd schema. */
    public static final String MODEL_XSD =
        "org/jdtaus/banking/xml/jdtaus-textschluessel.xsd";

    /** Version supported by this implementation. */
    public static final String SUPPORTED_VERSION = "1.0";

    /** Implementation meta-data. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(XMLTextschluesselVerzeichnis.class.getName());

    //---------------------------------------------------------------Constants--
    //--Constructors------------------------------------------------------------

    // This section is generated by jdtaus-source-plugin.

    /** Protected <code>XMLTextschluesselVerzeichnis</code> implementation constructor.
    * @param meta Implementation meta-data.
    */
    protected XMLTextschluesselVerzeichnis(final Implementation meta) {
        super();
        this._resource = (java.lang.String) meta.getProperties().
            getProperty("resource").getValue();


        this._dataDirectory = (java.lang.String) meta.getProperties().
            getProperty("dataDirectory").getValue();

        this.assertValidProperties();
    }
    /** Protected <code>XMLTextschluesselVerzeichnis</code> dependency constructor.
    * @param meta Dependency meta-data.
    */
    protected XMLTextschluesselVerzeichnis(final Dependency meta) {
        super();
        this._resource = (java.lang.String) meta.getProperties().
            getProperty("resource").getValue();


        this._dataDirectory = (java.lang.String) meta.getProperties().
            getProperty("dataDirectory").getValue();

        this.assertValidProperties();
    }

    //------------------------------------------------------------Constructors--
    //--Dependencies------------------------------------------------------------

    // This section is generated by jdtaus-source-plugin.

    /** Configured <code>Logger</code> implementation. */
    private transient Logger _dependency0;

    /** <code>Logger</code> implementation getter. */
    private Logger getLogger() {
        Logger ret = null;
        if(this._dependency0 != null) {
           ret = this._dependency0;
        } else {
            ret = (Logger) ContainerFactory.getContainer().
                getDependency(XMLTextschluesselVerzeichnis.class,
                "Logger");

            if(ret == null) {
                throw new ContainerError("Logger");
            }

            if(ModelFactory.getModel().getModules().
                getImplementation(XMLTextschluesselVerzeichnis.class.getName()).
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
     * Property {@code resource}.
     * @serial
     */
    private java.lang.String _resource;

    /** <code>resource</code> property getter. */
    protected java.lang.String getResource() {
        return this._resource;
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


    //--------------------------------------------------------------Properties--
    //--ContainerInitializer----------------------------------------------------

    /** Holds the loaded Textschlüssel instances. */
    private Textschluessel[] instances;

    /**
     * Initializes the instance to hold the parsed XML Textschluessel instances.
     *
     * @throws ImplementationError if no XML resources can be parsed.
     *
     * @see #parseResources()
     */
    public void initialize() {
        try {
            final Document docs[] = this.parseResources();
            final Collection col = new LinkedList();

            for(int i = docs.length - 1; i >= 0; i--) {
                col.add(this.transformDocument(docs[i]));
            }

            this.instances = (Textschluessel[]) col.
                toArray(new Textschluessel[col.size()]);

        } catch(IOException e) {
            throw new ImplementationError(META, e);
        } catch(ParserConfigurationException e) {
            throw new ImplementationError(META, e);
        } catch(SAXException e) {
            throw new ImplementationError(META, e);
        }
    }

    //----------------------------------------------------ContainerInitializer--
    //--TextschluesselVerzeichnis-----------------------------------------------

    public Textschluessel getTextschluessel(int key, int extension) {
        if(key < 0 || key > 99) {
            throw new IllegalArgumentException(Integer.toString(key));
        }
        if(extension < 0 || extension > 999) {
            throw new IllegalArgumentException(Integer.toString(extension));
        }

        Textschluessel ret = null;

        for(int i = this.instances.length - 1; i >= 0; i--) {
            if(this.instances[i].getKey() == key) {
                if(this.instances[i].isVariable()) {
                    ret = (Textschluessel) this.instances[i].clone();
                    break;
                } else {
                    if(this.instances[i].getExtension() == extension) {
                        ret = (Textschluessel) this.instances[i].clone();
                        break;
                    }
                }
            }
        }

        return ret;
    }

    public Textschluessel[] search(boolean debit, boolean remittance) {
        final Collection col = new ArrayList(this.instances.length);

        for(int i = this.instances.length - 1; i >= 0; i--) {
            if(this.instances[i].isDebit() == debit &&
                this.instances[i].isRemittance() == remittance) {

                col.add(this.instances[i].clone());
            }
        }

        return (Textschluessel[]) col.toArray(new Textschluessel[col.size()]);
    }

    //-----------------------------------------------TextschluesselVerzeichnis--
    //--XMLTextschluesselVerzeichnis--------------------------------------------

    /**
     * Checks configure properties.
     *
     * @throws PropertyError if properties hold invalid values.
     */
    protected void assertValidProperties() {
        try {
            if(this.getDataDirectory() == null) {
                throw new PropertyError("dataDirectory",
                    this.getDataDirectory());

            }
            if(this.getResource() == null || this.getResource().length() <= 0 ||
                this.getResources().length <= 0) {

                throw new PropertyError("resource", this.getResource());
            }
        } catch(IOException e) {
            throw new ImplementationError(META, e);
        }
    }

    /**
     * Gets all XML resources as {@code URL} instances.
     *
     * @return an array of {@code URL} instances for all resources named
     * {@code getDataDirectory() + '/' + getResource()}.
     *
     * @throws IOException if getting the resources fails.
     */
    protected URL[] getResources() throws IOException {
        final ClassLoader classLoader = this.getClassLoader();
        final Collection col = new LinkedList();
        final Enumeration en = classLoader.getResources(
            this.getDataDirectory() + '/' + this.getResource());

        for(;en.hasMoreElements();) {
            col.add(en.nextElement());
        }

        return (URL[]) col.toArray(new URL[col.size()]);
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
    protected final Document[] parseResources() throws
        IOException, ParserConfigurationException, SAXException {

        InputStream stream = null;

        final URL[] resources = this.getResources();
        final DocumentBuilder parser = this.getDocumentBuilder();
        final Document[] ret = new Document[resources.length];

        for(int i = resources.length - 1; i >= 0; i--) {
            try {
                stream = resources[i].openStream();
                ret[i] = parser.parse(stream);
            } finally {
                if(stream != null) {
                    stream.close();
                    stream = null;
                }
            }
        }

        return ret;
    }

    /**
     * Transforms a XML resource to the Textschluessel instances it contains.
     *
     * @param doc the document to transform.
     *
     * @return an array of Textschluessel instances from the given document.
     */
    protected Textschluessel[] transformDocument(final Document doc) {
        Element e;
        String str;
        NodeList l;
        Textschluessel key;
        final Collection col = new ArrayList(500);

        l = doc.getDocumentElement().getElementsByTagNameNS(
            XMLTextschluesselVerzeichnis.MODEL_NS, "transactionTypes");

        l = ((Element) l.item(0)).getElementsByTagNameNS(
            XMLTextschluesselVerzeichnis.MODEL_NS, "transactionType");

        for(int i = l.getLength() - 1; i >= 0; i--) {
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

            if("VARIABLE".equals(str)) {
                key.setVariable(true);
                key.setExtension(0);
            } else {
                key.setExtension(Integer.valueOf(str).intValue());
            }

            col.add(key);
        }

        return (Textschluessel[]) col.toArray(new Textschluessel[col.size()]);
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
     * Textschlüssel XML resources.
     *
     * @throws IOException if reading the schema fails.
     * @throws ParserConfigurationException if no supported XML parser runtime
     * is available.
     */
    protected DocumentBuilder getDocumentBuilder() throws IOException,
        ParserConfigurationException {

        final DocumentBuilder xmlBuilder;
        final DocumentBuilderFactory xmlFactory =
            DocumentBuilderFactory.newInstance();

        xmlFactory.setNamespaceAware(true);
        try {
            xmlFactory.setValidating(true);
            xmlFactory.setAttribute(
                XMLTextschluesselVerzeichnis.SCHEMA_LANGUAGE_KEY,
                XMLTextschluesselVerzeichnis.SCHEMA_LANGUAGE);

            final URL schema = this.getClassLoader().getResource(
                XMLTextschluesselVerzeichnis.MODEL_XSD);

            xmlFactory.setAttribute(
                XMLTextschluesselVerzeichnis.SCHEMA_SOURCE_KEY,
                schema.openStream());

        } catch(IllegalArgumentException e) {
            this.getLogger().error(e);
            this.getLogger().warn(XMLTextschluesselVerzeichnisBundle.
                getNoJAXPValidationWarningMessage(Locale.getDefault()).
                format(new Object[] { e.getMessage() }));

            xmlFactory.setValidating(false);
        }

        xmlBuilder = xmlFactory.newDocumentBuilder();
        xmlBuilder.setErrorHandler(new ErrorHandler() {
            public void warning(final SAXParseException e) {
                getLogger().warn(e);
            }
            public void fatalError(final SAXParseException e) {
                getLogger().fatal(e);
                throw new ImplementationError(META, e);
            }
            public void error(final SAXParseException e) {
                getLogger().error(e);
            }
        });

        return xmlBuilder;
    }

    private ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if(classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        if(classLoader == null) {
            throw new ImplementationError(META,
                new NullPointerException("classLoader"));

        }

        return classLoader;
    }

    //--------------------------------------------XMLTextschluesselVerzeichnis--

}

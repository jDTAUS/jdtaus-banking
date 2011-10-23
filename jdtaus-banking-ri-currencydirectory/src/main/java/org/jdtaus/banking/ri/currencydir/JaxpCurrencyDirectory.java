/*
 *  jDTAUS Banking RI CurrencyDirectory
 *  Copyright (c) 2005 Christian Schulte
 *
 *  Christian Schulte, Haldener Strasse 72, 58095 Hagen, Germany
 *  <schulte2005@users.sourceforge.net> (+49 2331 3543887)
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
import java.text.DateFormat;
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
import org.jdtaus.banking.messages.ReadsCurrenciesMessage;
import org.jdtaus.banking.messages.SearchesCurrenciesMessage;
import org.jdtaus.banking.spi.CurrencyMapper;
import org.jdtaus.banking.spi.UnsupportedCurrencyException;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.PropertyException;
import org.jdtaus.core.logging.spi.Logger;
import org.jdtaus.core.monitor.spi.Task;
import org.jdtaus.core.monitor.spi.TaskMonitor;
import org.jdtaus.core.sax.util.EntityResolverChain;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Currency directory implementation backed by XML resources.
 * <p>This implementation uses XML resources provided by any available {@link JaxpCurrenciesProvider} implementation.
 * Resources with a {@code file} URI scheme are monitored for changes by querying the last modification time. Monitoring
 * is controlled by property {@code reloadIntervalMillis}.</p>
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class JaxpCurrencyDirectory implements CurrencyMapper
{

    /** JAXP configuration key to the Schema implementation attribute. */
    private static final String SCHEMA_LANGUAGE_KEY = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /** JAXP Schema implementation to use. */
    private static final String SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

    /** jDTAUS {@code currencies} namespace URI. */
    private static final String CURRENCIES_NS = "http://jdtaus.org/banking/xml/currencies";

    /** jDTAUS {@code banking} namespace URI. */
    private static final String BANKING_NS = "http://jdtaus.org/banking/model";

    /** {@code http://www.w3.org/2001/XMLSchema-instance} namespace URI. */
    private static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

    /** Version supported by this implementation. */
    private static final String[] SUPPORTED_VERSIONS =
    {
        "1.0"
    };

    /** Flag indicating that initialization has been performed. */
    private boolean initialized;

    /** Maps ISO codes to currency instances. */
    private final Map isoMap = new HashMap();

    /** Maps DTAUS codes to currency instances. */
    private final Map dtausMap = new HashMap();

    /** Maps {@code File} instances to theire last modification timestamp. */
    private final Map monitorMap = new HashMap();

    /** Holds the timestamp resources got checked for modifications. */
    private long lastCheck = System.currentTimeMillis();

    /** Number of milliseconds to pass before resources are checked for modifications. */
    private Long reloadIntervalMillis;

    /** Number of currencies for which progress monitoring gets enabled. */
    private Long monitoringThreshold;

    /**
     * Creates a new {@code JaxpCurrencyDirectory} instance taking the number of milliseconds to pass before resources
     * are checked for modifications and the number of currencies for which progress monitoring gets enabled.
     *
     * @param reloadIntervalMillis Number of milliseconds to pass before resources are checked for modifications.
     * @param monitoringThreshold Number of currencies for which progress monitoring gets enabled.
     */
    public JaxpCurrencyDirectory( final long reloadIntervalMillis, final long monitoringThreshold )
    {
        this();
        if ( reloadIntervalMillis > 0 )
        {
            this.reloadIntervalMillis = new Long( reloadIntervalMillis );
        }
        if ( monitoringThreshold > 0 )
        {
            this.monitoringThreshold = new Long( monitoringThreshold );
        }
    }

    /**
     * Gets the number of milliseconds to pass before resources are checked for modifications.
     *
     * @return The number of milliseconds to pass before resources are checked for modifications.
     */
    public long getReloadIntervalMillis()
    {
        if ( this.reloadIntervalMillis == null )
        {
            this.reloadIntervalMillis = this.getDefaultReloadIntervalMillis();
        }

        return this.reloadIntervalMillis.longValue();
    }

    /**
     * Gets the number of currencies for which progress monitoring gets enabled.
     *
     * @return The number of currencies for which progress monitoring gets enabled.
     */
    public long getMonitoringThreshold()
    {
        if ( this.monitoringThreshold == null )
        {
            this.monitoringThreshold = this.getDefaultMonitoringThreshold();
        }

        return this.monitoringThreshold.longValue();
    }

    public Currency[] getDtausCurrencies( final Date date )
    {
        if ( date == null )
        {
            throw new NullPointerException( "date" );
        }

        this.assertValidProperties();
        this.assertInitialized();

        final Collection col = new LinkedList();

        if ( !this.isoMap.isEmpty() )
        {
            final Task task = new Task();
            task.setCancelable( true );
            task.setDescription( new SearchesCurrenciesMessage() );
            task.setIndeterminate( false );
            task.setMaximum( this.isoMap.size() );
            task.setMinimum( 0 );

            int progress = 0;
            task.setProgress( progress );

            try
            {
                if ( task.getMaximum() > this.getMonitoringThreshold() )
                {
                    this.getTaskMonitor().monitor( task );
                }

                for ( final Iterator it = this.isoMap.keySet().iterator(); it.hasNext() && !task.isCancelled(); )
                {
                    task.setProgress( progress++ );
                    final String isoCode = (String) it.next();
                    final JaxpCurrency currency = (JaxpCurrency) this.isoMap.get( isoCode );

                    if ( currency.isValidAt( date ) )
                    {
                        col.add( Currency.getInstance( isoCode ) );
                    }
                }

                if ( task.isCancelled() )
                {
                    col.clear();
                }
            }
            finally
            {
                if ( task.getMaximum() > this.getMonitoringThreshold() )
                {
                    this.getTaskMonitor().finish( task );
                }
            }
        }

        return (Currency[]) col.toArray( new Currency[ col.size() ] );
    }

    public Currency getDtausCurrency( final char code, final Date date )
    {
        if ( date == null )
        {
            throw new NullPointerException( "date" );
        }

        this.assertValidProperties();
        this.assertInitialized();

        final JaxpCurrency currency = (JaxpCurrency) this.dtausMap.get( new Character( code ) );
        Currency ret = null;

        if ( currency != null && currency.isValidAt( date ) )
        {
            ret = Currency.getInstance( currency.getIsoCode() );
        }

        return ret;
    }

    public char getDtausCode( final Currency currency, final Date date )
    {
        if ( currency == null )
        {
            throw new NullPointerException( "currency" );
        }
        if ( date == null )
        {
            throw new NullPointerException( "date" );
        }

        this.assertValidProperties();
        this.assertInitialized();

        final JaxpCurrency xml = (JaxpCurrency) this.isoMap.get( currency.getCurrencyCode() );

        if ( xml != null && xml.getDtausCode() != null && xml.isValidAt( date ) )
        {
            return xml.getDtausCode().charValue();
        }

        throw new UnsupportedCurrencyException( currency.getCurrencyCode(), date );
    }

    /**
     * Initializes the instance to hold the parsed XML currency instances.
     *
     * @see #assertValidProperties()
     * @see #parseResources()
     * @see #transformDocument(Document)
     */
    private synchronized void assertInitialized()
    {
        try
        {
            if ( System.currentTimeMillis() - this.lastCheck > this.getReloadIntervalMillis()
                 && !this.monitorMap.isEmpty() )
            {
                this.lastCheck = System.currentTimeMillis();
                for ( final Iterator it = this.monitorMap.entrySet().iterator(); it.hasNext(); )
                {
                    final Map.Entry entry = (Map.Entry) it.next();
                    final File file = (File) entry.getKey();
                    final Long lastModified = (Long) entry.getValue();
                    assert lastModified != null : "Expected modification time.";

                    if ( file.lastModified() != lastModified.longValue() )
                    {
                        this.getLogger().info( this.getChangeInfoMessage( this.getLocale(), file.getAbsolutePath() ) );
                        this.initialized = false;
                        break;
                    }
                }
            }

            if ( !this.initialized )
            {
                this.monitorMap.clear();
                this.isoMap.clear();
                this.dtausMap.clear();

                final Document[] docs = this.parseResources();
                final Collection col = new LinkedList();

                for ( int i = docs.length - 1; i >= 0; i-- )
                {
                    col.addAll( Arrays.asList( this.transformDocument( docs[i] ) ) );
                }

                for ( final Iterator it = col.iterator(); it.hasNext(); )
                {
                    final JaxpCurrency currency = (JaxpCurrency) it.next();
                    if ( this.isoMap.put( currency.getIsoCode(), currency ) != null
                         || ( currency.getDtausCode() != null
                              && this.dtausMap.put( currency.getDtausCode(), currency ) != null ) )
                    {
                        throw new IllegalStateException( this.getDuplicateCurrencyMessage(
                            this.getLocale(), currency.getIsoCode(),
                            Currency.getInstance( currency.getIsoCode() ).getSymbol( this.getLocale() ) ) );

                    }
                }

                this.getLogger().info( this.getCurrencyInfoMessage(
                    this.getLocale(), new Integer( this.isoMap.size() ), new Integer( docs.length ) ) );

                this.initialized = true;
            }
        }
        catch ( final IOException e )
        {
            this.initialized = false;
            throw new RuntimeException( e );
        }
        catch ( final SAXException e )
        {
            this.initialized = false;
            throw new RuntimeException( e );
        }
        catch ( final ParserConfigurationException e )
        {
            this.initialized = false;
            throw new RuntimeException( e );
        }
        catch ( final ParseException e )
        {
            this.initialized = false;
            throw new RuntimeException( e );
        }
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException if properties hold invalid values.
     */
    private void assertValidProperties()
    {
        if ( this.getReloadIntervalMillis() < 0L )
        {
            throw new PropertyException( "reloadIntervalMillis", Long.toString( this.getReloadIntervalMillis() ) );
        }
        if ( this.getMonitoringThreshold() < 0L )
        {
            throw new PropertyException( "monitoringThreshold", Long.toString( this.getMonitoringThreshold() ) );
        }
    }

    /**
     * Gets XML resources provided by any available {@code CurrenciesProvider} implementation.
     *
     * @return XML resources provided by any available {@code CurrenciesProvider} implementation.
     *
     * @throws IOException if retrieving resources fails.
     *
     * @see JaxpCurrenciesProvider
     */
    private URL[] getResources() throws IOException
    {
        final Collection resources = new HashSet();
        final JaxpCurrenciesProvider[] provider = this.getCurrenciesProvider();

        for ( int i = provider.length - 1; i >= 0; i-- )
        {
            resources.addAll( Arrays.asList( provider[i].getResources() ) );
        }

        return (URL[]) resources.toArray( new URL[ resources.size() ] );
    }

    /**
     * Adds a resource to the list of resources to monitor for changes.
     *
     * @param url The URL of the resource to monitor for changes.
     *
     * @throws NullPointerException if {@code url} is {@code null}.
     */
    private void monitorResource( final URL url )
    {
        if ( url == null )
        {
            throw new NullPointerException( "url" );
        }

        try
        {
            final File file = new File( new URI( url.toString() ) );
            this.monitorMap.put( file, new Long( file.lastModified() ) );
            this.getLogger().info( this.getMonitoringInfoMessage( this.getLocale(), file.getAbsolutePath() ) );
        }
        catch ( final IllegalArgumentException e )
        {
            this.getLogger().info( this.getNotMonitoringWarningMessage(
                this.getLocale(), url.toExternalForm(), e.getMessage() ) );

        }
        catch ( final URISyntaxException e )
        {
            this.getLogger().info( this.getNotMonitoringWarningMessage(
                this.getLocale(), url.toExternalForm(), e.getMessage() ) );

        }
    }

    /**
     * Parses all XML resources.
     *
     * @return The parsed documents.
     *
     * @see #getResources()
     * @see #getDocumentBuilder()
     *
     * @throws IOException if retrieving resources fails.
     * @throws ParserConfigurationException if configuring the XML parser fails.
     * @throws SAXException if parsing fails.
     */
    private Document[] parseResources() throws IOException, ParserConfigurationException, SAXException
    {
        InputStream stream = null;

        final URL[] resources = this.getResources();
        final Document[] ret = new Document[ resources.length ];

        if ( resources.length > 0 )
        {
            final DocumentBuilder validatingParser = this.getDocumentBuilder();
            final DocumentBuilderFactory namespaceAwareFactory = DocumentBuilderFactory.newInstance();
            namespaceAwareFactory.setNamespaceAware( true );
            final DocumentBuilder nonValidatingParser = namespaceAwareFactory.newDocumentBuilder();

            final Task task = new Task();
            task.setCancelable( false );
            task.setDescription( new ReadsCurrenciesMessage() );
            task.setIndeterminate( false );
            task.setMaximum( resources.length - 1 );
            task.setMinimum( 0 );
            task.setProgress( 0 );

            try
            {
                this.getTaskMonitor().monitor( task );

                for ( int i = resources.length - 1; i >= 0; i-- )
                {
                    task.setProgress( task.getMaximum() - i );

                    final URL resource = resources[i];
                    final ErrorHandler errorHandler = new ErrorHandler()
                    {

                        public void warning( final SAXParseException e ) throws SAXException
                        {
                            getLogger().warn( getParseExceptionMessage(
                                getLocale(), resource.toExternalForm(),
                                e.getMessage(), new Integer( e.getLineNumber() ),
                                new Integer( e.getColumnNumber() ) ) );

                        }

                        public void error( final SAXParseException e ) throws SAXException
                        {
                            throw new SAXException( getParseExceptionMessage(
                                getLocale(), resource.toExternalForm(),
                                e.getMessage(), new Integer( e.getLineNumber() ),
                                new Integer( e.getColumnNumber() ) ), e );

                        }

                        public void fatalError( final SAXParseException e ) throws SAXException
                        {
                            throw new SAXException( getParseExceptionMessage(
                                getLocale(), resource.toExternalForm(),
                                e.getMessage(), new Integer( e.getLineNumber() ),
                                new Integer( e.getColumnNumber() ) ), e );

                        }

                    };

                    nonValidatingParser.setErrorHandler( errorHandler );
                    validatingParser.setErrorHandler( errorHandler );

                    try
                    {
                        this.monitorResource( resources[i] );
                        stream = resources[i].openStream();
                        ret[i] = nonValidatingParser.parse( stream );

                        if ( ret[i].getDocumentElement().hasAttributeNS( XSI_NS, "schemaLocation" ) )
                        {
                            stream.close();
                            stream = resources[i].openStream();
                            ret[i] = validatingParser.parse( stream );
                        }
                        else if ( this.getLogger().isInfoEnabled() )
                        {
                            this.getLogger().info( this.getNoSchemaLocationMessage(
                                this.getLocale(), resources[i].toExternalForm() ) );

                        }
                    }
                    finally
                    {
                        if ( stream != null )
                        {
                            stream.close();
                            stream = null;
                        }
                    }
                }
            }
            finally
            {
                this.getTaskMonitor().finish( task );
            }
        }
        else
        {
            this.getLogger().warn( this.getNoCurrenciesFoundMessage( this.getLocale() ) );
        }

        return ret;
    }

    /**
     * Transforms a XML document to the currency instances it contains.
     *
     * @param doc The document to transform.
     *
     * @return An array of the currency instances from the given document.
     *
     * @throws ParseException if parsing values fails.
     */
    private JaxpCurrency[] transformDocument( final Document doc ) throws ParseException
    {
        final Calendar cal = Calendar.getInstance();
        final DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        final Collection col = new ArrayList( 500 );

        String modelVersion = null;
        final String namespace = doc.getDocumentElement().getNamespaceURI();

        if ( namespace == null )
        {
            throw new RuntimeException( this.getUnsupportedNamespaceMessage( this.getLocale(), namespace ) );
        }
        else if ( CURRENCIES_NS.equals( namespace ) )
        {
            modelVersion = doc.getDocumentElement().getAttributeNS( namespace, "version" );
        }
        else if ( BANKING_NS.equals( namespace ) )
        {
            modelVersion = doc.getDocumentElement().getAttributeNS( namespace, "modelVersion" );
        }
        else
        {
            throw new RuntimeException( this.getUnsupportedNamespaceMessage( this.getLocale(), namespace ) );
        }

        boolean supportedModelVersion = false;
        for ( int i = SUPPORTED_VERSIONS.length - 1; i >= 0; i-- )
        {
            if ( SUPPORTED_VERSIONS[i].equals( modelVersion ) )
            {
                supportedModelVersion = true;
                break;
            }
        }

        if ( !supportedModelVersion )
        {
            throw new RuntimeException( this.getUnsupportedModelVersionMessage( this.getLocale(), modelVersion ) );
        }

        final NodeList l = doc.getDocumentElement().getElementsByTagNameNS( namespace, "currency" );

        for ( int i = l.getLength() - 1; i >= 0; i-- )
        {
            final Element e = (Element) l.item( i );

            if ( e.getParentNode().equals( doc.getDocumentElement() ) )
            {
                final JaxpCurrency cur = new JaxpCurrency();
                cur.setIsoCode( e.getAttributeNS( namespace, "isoCode" ) );

                if ( e.hasAttributeNS( namespace, "dtausCode" ) )
                {
                    cur.setDtausCode( new Character( e.getAttributeNS( namespace, "dtausCode" ).charAt( 0 ) ) );
                }

                cal.setTime( dateFormat.parse( e.getAttributeNS( namespace, "startDate" ) ) );
                cal.set( Calendar.HOUR_OF_DAY, 0 );
                cal.set( Calendar.MINUTE, 0 );
                cal.set( Calendar.SECOND, 0 );
                cal.set( Calendar.MILLISECOND, 0 );
                cur.setStartDate( cal.getTime() );

                if ( e.hasAttributeNS( namespace, "endDate" ) )
                {
                    cal.setTime( dateFormat.parse( e.getAttributeNS( namespace, "endDate" ) ) );
                    cal.set( Calendar.HOUR_OF_DAY, 23 );
                    cal.set( Calendar.MINUTE, 59 );
                    cal.set( Calendar.SECOND, 59 );
                    cal.set( Calendar.MILLISECOND, 999 );
                    cur.setEndDate( cal.getTime() );
                }

                col.add( cur );
            }
        }

        return (JaxpCurrency[]) col.toArray( new JaxpCurrency[ col.size() ] );
    }

    /**
     * Creates a new {@code DocumentBuilder} to use for parsing the XML resources.
     * <p>This method tries to set the following JAXP property on the system's default XML parser:
     * <ul>
     * <li>{@code http://java.sun.com/xml/jaxp/properties/schemaLanguage} set to
     * {@code http://www.w3.org/2001/XMLSchema}</li>
     * </ul> When setting this property fails, a non-validating
     * {@code DocumentBuilder} is returned and a warning message is logged.</p>
     *
     * @return A new {@code DocumentBuilder} to be used for parsing resources.
     *
     * @throws ParserConfigurationException if configuring the XML parser fails.
     */
    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException
    {
        final DocumentBuilder xmlBuilder;
        final DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        xmlFactory.setNamespaceAware( true );

        try
        {
            xmlFactory.setValidating( true );
            xmlFactory.setAttribute( SCHEMA_LANGUAGE_KEY, SCHEMA_LANGUAGE );
        }
        catch ( final IllegalArgumentException e )
        {
            this.getLogger().info( this.getNoJAXPValidationWarningMessage( this.getLocale(), e.getMessage() ) );
            xmlFactory.setValidating( false );
        }

        xmlBuilder = xmlFactory.newDocumentBuilder();
        xmlBuilder.setEntityResolver( new EntityResolverChain() );
        return xmlBuilder;
    }

    //--Constructors------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausConstructors
    // This section is managed by jdtaus-container-mojo.

    /** Standard implementation constructor <code>org.jdtaus.banking.ri.currencydir.JaxpCurrencyDirectory</code>. */
    public JaxpCurrencyDirectory()
    {
        super();
    }

// </editor-fold>//GEN-END:jdtausConstructors

    //------------------------------------------------------------Constructors--
    //--Dependencies------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the configured <code>Logger</code> implementation.
     *
     * @return The configured <code>Logger</code> implementation.
     */
    private Logger getLogger()
    {
        return (Logger) ContainerFactory.getContainer().
            getDependency( this, "Logger" );

    }

    /**
     * Gets the configured <code>CurrenciesProvider</code> implementation.
     *
     * @return The configured <code>CurrenciesProvider</code> implementation.
     */
    private JaxpCurrenciesProvider[] getCurrenciesProvider()
    {
        return (JaxpCurrenciesProvider[]) ContainerFactory.getContainer().
            getDependency( this, "CurrenciesProvider" );

    }

    /**
     * Gets the configured <code>TaskMonitor</code> implementation.
     *
     * @return The configured <code>TaskMonitor</code> implementation.
     */
    private TaskMonitor getTaskMonitor()
    {
        return (TaskMonitor) ContainerFactory.getContainer().
            getDependency( this, "TaskMonitor" );

    }

    /**
     * Gets the configured <code>Locale</code> implementation.
     *
     * @return The configured <code>Locale</code> implementation.
     */
    private Locale getLocale()
    {
        return (Locale) ContainerFactory.getContainer().
            getDependency( this, "Locale" );

    }

// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
    //--Properties--------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausProperties
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the value of property <code>defaultReloadIntervalMillis</code>.
     *
     * @return Default number of milliseconds to pass before resources are checked for modifications.
     */
    private java.lang.Long getDefaultReloadIntervalMillis()
    {
        return (java.lang.Long) ContainerFactory.getContainer().
            getProperty( this, "defaultReloadIntervalMillis" );

    }

    /**
     * Gets the value of property <code>defaultMonitoringThreshold</code>.
     *
     * @return Default number of currencies for which progress monitoring gets enabled.
     */
    private java.lang.Long getDefaultMonitoringThreshold()
    {
        return (java.lang.Long) ContainerFactory.getContainer().
            getProperty( this, "defaultMonitoringThreshold" );

    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>noJAXPValidationWarning</code>.
     * <blockquote><pre>Keine JAXP Validierung verfügbar. {0}</pre></blockquote>
     * <blockquote><pre>No JAXP validation available. {0}</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param detailMessage format argument.
     *
     * @return the text of message <code>noJAXPValidationWarning</code>.
     */
    private String getNoJAXPValidationWarningMessage( final Locale locale,
            final java.lang.String detailMessage )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "noJAXPValidationWarning", locale,
                new Object[]
                {
                    detailMessage
                });

    }

    /**
     * Gets the text of message <code>notMonitoringWarning</code>.
     * <blockquote><pre>{0} kann bei Änderung nicht automatisch neu geladen werden. {1}</pre></blockquote>
     * <blockquote><pre>{0} cannot be monitored. {1}</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param resourceName format argument.
     * @param detailMessage format argument.
     *
     * @return the text of message <code>notMonitoringWarning</code>.
     */
    private String getNotMonitoringWarningMessage( final Locale locale,
            final java.lang.String resourceName,
            final java.lang.String detailMessage )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "notMonitoringWarning", locale,
                new Object[]
                {
                    resourceName,
                    detailMessage
                });

    }

    /**
     * Gets the text of message <code>changeInfo</code>.
     * <blockquote><pre>{0} aktualisiert.</pre></blockquote>
     * <blockquote><pre>{0} changed.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param resourceName format argument.
     *
     * @return the text of message <code>changeInfo</code>.
     */
    private String getChangeInfoMessage( final Locale locale,
            final java.lang.String resourceName )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "changeInfo", locale,
                new Object[]
                {
                    resourceName
                });

    }

    /**
     * Gets the text of message <code>monitoringInfo</code>.
     * <blockquote><pre>{0} wird bei Änderung automatisch neu geladen.</pre></blockquote>
     * <blockquote><pre>Monitoring {0} for changes.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param resourceName format argument.
     *
     * @return the text of message <code>monitoringInfo</code>.
     */
    private String getMonitoringInfoMessage( final Locale locale,
            final java.lang.String resourceName )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "monitoringInfo", locale,
                new Object[]
                {
                    resourceName
                });

    }

    /**
     * Gets the text of message <code>parseException</code>.
     * <blockquote><pre>Fehler bei der Verarbeitung der Resource "{0}" in Zeile {2}, Spalte {3}. {1}</pre></blockquote>
     * <blockquote><pre>Error parsing resource "{0}" at line {2}, column {3}. {1}</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param resourceName format argument.
     * @param cause format argument.
     * @param line format argument.
     * @param column format argument.
     *
     * @return the text of message <code>parseException</code>.
     */
    private String getParseExceptionMessage( final Locale locale,
            final java.lang.String resourceName,
            final java.lang.String cause,
            final java.lang.Number line,
            final java.lang.Number column )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "parseException", locale,
                new Object[]
                {
                    resourceName,
                    cause,
                    line,
                    column
                });

    }

    /**
     * Gets the text of message <code>unsupportedNamespace</code>.
     * <blockquote><pre>Ungültiger XML-Namensraum {0}.</pre></blockquote>
     * <blockquote><pre>Unsupported XML namespace {0}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param namespace format argument.
     *
     * @return the text of message <code>unsupportedNamespace</code>.
     */
    private String getUnsupportedNamespaceMessage( final Locale locale,
            final java.lang.String namespace )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "unsupportedNamespace", locale,
                new Object[]
                {
                    namespace
                });

    }

    /**
     * Gets the text of message <code>unsupportedModelVersion</code>.
     * <blockquote><pre>Keine Unterstützung für Modellversion {0}.</pre></blockquote>
     * <blockquote><pre>Unsupported model version {0}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param modelVersion format argument.
     *
     * @return the text of message <code>unsupportedModelVersion</code>.
     */
    private String getUnsupportedModelVersionMessage( final Locale locale,
            final java.lang.String modelVersion )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "unsupportedModelVersion", locale,
                new Object[]
                {
                    modelVersion
                });

    }

    /**
     * Gets the text of message <code>currencyInfo</code>.
     * <blockquote><pre>{1,choice,0#Kein Dokument|1#Ein Dokument|1<{1} Dokumente} gelesen. {0,choice,0#Keine Währung|1#Eine Währung|1<{0} Währungen} verarbeitet.</pre></blockquote>
     * <blockquote><pre>Read {1,choice,0#no document|1#one document|1<{1} documents}. Processed {0,choice,0#no entities|1#one entity|1<{0} entities}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param entityCount format argument.
     * @param documentCount format argument.
     *
     * @return the text of message <code>currencyInfo</code>.
     */
    private String getCurrencyInfoMessage( final Locale locale,
            final java.lang.Number entityCount,
            final java.lang.Number documentCount )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "currencyInfo", locale,
                new Object[]
                {
                    entityCount,
                    documentCount
                });

    }

    /**
     * Gets the text of message <code>noSchemaLocation</code>.
     * <blockquote><pre>Kein schemaLocation Attribut in Ressource "{0}". Keine Schema-Validierung.</pre></blockquote>
     * <blockquote><pre>No schemaLocation attribute in resource "{0}". Schema validation skipped.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param resource format argument.
     *
     * @return the text of message <code>noSchemaLocation</code>.
     */
    private String getNoSchemaLocationMessage( final Locale locale,
            final java.lang.String resource )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "noSchemaLocation", locale,
                new Object[]
                {
                    resource
                });

    }

    /**
     * Gets the text of message <code>duplicateCurrency</code>.
     * <blockquote><pre>Währung {1} ({0}) ist mehrfach vorhanden.</pre></blockquote>
     * <blockquote><pre>Non-unique currency {1} ({0}).</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param currencyCode format argument.
     * @param currencySymbol format argument.
     *
     * @return the text of message <code>duplicateCurrency</code>.
     */
    private String getDuplicateCurrencyMessage( final Locale locale,
            final java.lang.String currencyCode,
            final java.lang.String currencySymbol )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "duplicateCurrency", locale,
                new Object[]
                {
                    currencyCode,
                    currencySymbol
                });

    }

    /**
     * Gets the text of message <code>noCurrenciesFound</code>.
     * <blockquote><pre>Keine Währungen gefunden.</pre></blockquote>
     * <blockquote><pre>No currencies found.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     *
     * @return the text of message <code>noCurrenciesFound</code>.
     */
    private String getNoCurrenciesFoundMessage( final Locale locale )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "noCurrenciesFound", locale, null );

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

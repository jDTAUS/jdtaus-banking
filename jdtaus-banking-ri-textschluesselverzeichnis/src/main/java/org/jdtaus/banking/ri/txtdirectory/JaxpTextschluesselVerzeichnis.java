/*
 *  jDTAUS Banking RI Textschluesselverzeichnis
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
package org.jdtaus.banking.ri.txtdirectory;

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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jdtaus.banking.Textschluessel;
import org.jdtaus.banking.TextschluesselVerzeichnis;
import org.jdtaus.banking.messages.ReadsTextschluesselMessage;
import org.jdtaus.banking.messages.SearchesTextschluesselMessage;
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
 * Textschlüssel directory implementation backed by XML files.
 * <p>This implementation uses XML resources provided by any available {@link JaxpTextschluesselProvider}
 * implementation. Resources with a {@code file} URI scheme are monitored for changes by querying the last modification
 * time. Monitoring is controlled by property {@code reloadIntervalMillis}.</p>
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class JaxpTextschluesselVerzeichnis implements TextschluesselVerzeichnis
{

    /** JAXP configuration key to the Schema implementation attribute. */
    private static final String SCHEMA_LANGUAGE_KEY = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /**
     * JAXP Schema implementation to use.
     * @see javax.xml.XMLConstants#W3C_XML_SCHEMA_NS_URI
     */
    private static final String SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

    /** jDTAUS {@code textschluessel} namespace URI. */
    private static final String TEXTSCHLUESSEL_NS = "http://jdtaus.org/banking/xml/textschluessel";

    /** jDTAUS {@code banking} namespace URI. */
    private static final String BANKING_NS = "http://jdtaus.org/banking/model";

    /** {@code http://www.w3.org/2001/XMLSchema-instance} namespace URI. */
    private static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

    /** Version supported by this implementation. */
    private static final String[] SUPPORTED_VERSIONS =
    {
        "1.0", "1.1"
    };

    /* Flag indicating that initialization has been performed. */
    private boolean initialized;

    /** Holds the loaded Textschlüssel instances. */
    private Textschluessel[] instances;

    /** Maps {@code File} instances to theire last modification timestamp. */
    private final Map monitorMap = new HashMap();

    /** Holds the timestamp resources got checked for modifications. */
    private long lastCheck = System.currentTimeMillis();

    /** Number of milliseconds to pass before resources are checked for modifications. */
    private Long reloadIntervalMillis;

    /** Number of Textschluessel for which progress monitoring gets enabled. */
    private Long monitoringThreshold;

    /**
     * Creates a new {@code XMLTextschluesselVerzeichnis} instance taking the number of milliseconds to pass before
     * resources are checked for modifications and the number of Textschluessel for which progress monitoring gets
     * enabled.
     *
     * @param reloadIntervalMillis Number of milliseconds to pass before resources are checked for modifications.
     * @param monitoringThreshold Number of Textschluessel for which progress monitoring gets enabled.
     */
    public JaxpTextschluesselVerzeichnis( final long reloadIntervalMillis, final long monitoringThreshold )
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
     * Gets the number of Textschluessel for which progress monitoring gets enabled.
     *
     * @return The number of Textschluessel for which progress monitoring gets enabled.
     */
    public long getMonitoringThreshold()
    {
        if ( this.monitoringThreshold == null )
        {
            this.monitoringThreshold = this.getDefaultMonitoringThreshold();
        }

        return this.monitoringThreshold.longValue();
    }

    public Textschluessel[] getTextschluessel()
    {
        this.assertValidProperties();
        this.assertInitialized();
        return this.searchTextschluessel( null, null, null );
    }

    public Textschluessel getTextschluessel( final int key, final int extension )
    {
        if ( key < 0 || key > 99 )
        {
            throw new IllegalArgumentException( Integer.toString( key ) );
        }
        if ( extension < 0 || extension > 999 )
        {
            throw new IllegalArgumentException( Integer.toString( extension ) );
        }

        this.assertValidProperties();
        this.assertInitialized();

        for ( int i = this.instances.length - 1; i >= 0; i-- )
        {
            if ( this.instances[i].getKey() == key &&
                 ( this.instances[i].isVariable() || this.instances[i].getExtension() == extension ) )
            {
                return (Textschluessel) this.instances[i].clone();
            }
        }

        return null;
    }

    public Textschluessel getTextschluessel( final int key, final int extension, final Date date )
    {
        if ( key < 0 || key > 99 )
        {
            throw new IllegalArgumentException( Integer.toString( key ) );
        }
        if ( extension < 0 || extension > 999 )
        {
            throw new IllegalArgumentException( Integer.toString( extension ) );
        }
        if ( date == null )
        {
            throw new NullPointerException( "date" );
        }

        this.assertValidProperties();
        this.assertInitialized();

        final Textschluessel textschluessel = this.getTextschluessel( key, extension );
        return textschluessel != null && textschluessel.isValidAt( date ) ? textschluessel : null;
    }

    public final Textschluessel[] search( final boolean debit, final boolean remittance )
    {
        return this.searchTextschluessel( Boolean.valueOf( debit ), Boolean.valueOf( remittance ), null );
    }

    public final Textschluessel[] search( final boolean debit, final boolean remittance, final Date date )
    {
        if ( date == null )
        {
            throw new NullPointerException( "date" );
        }

        return this.searchTextschluessel( Boolean.valueOf( debit ), Boolean.valueOf( remittance ), date );
    }

    public Textschluessel[] searchTextschluessel( final Boolean debit, final Boolean remittance, final Date date )
    {
        this.assertValidProperties();
        this.assertInitialized();

        final Collection col = new ArrayList( this.instances.length );
        final Task task = new Task();
        task.setCancelable( true );
        task.setDescription( new SearchesTextschluesselMessage() );
        task.setIndeterminate( false );
        task.setMaximum( this.instances.length - 1 );
        task.setMinimum( 0 );
        task.setProgress( 0 );

        try
        {
            if ( task.getMaximum() > this.getMonitoringThreshold() )
            {
                this.getTaskMonitor().monitor( task );
            }

            for ( int i = this.instances.length - 1; i >= 0 && !task.isCancelled(); i-- )
            {
                task.setProgress( task.getMaximum() - i );

                if ( ( debit == null ? true : this.instances[i].isDebit() == debit.booleanValue() ) &&
                     ( remittance == null ? true : this.instances[i].isRemittance() == remittance.booleanValue() ) &&
                     ( date == null ? true : this.instances[i].isValidAt( date ) ) )
                {
                    col.add( this.instances[i].clone() );
                }
            }

            if ( task.isCancelled() )
            {
                col.clear();
            }

            return (Textschluessel[]) col.toArray( new Textschluessel[ col.size() ] );
        }
        finally
        {
            if ( task.getMaximum() > this.getMonitoringThreshold() )
            {
                this.getTaskMonitor().finish( task );
            }
        }
    }

    /**
     * Initializes the instance to hold the parsed XML Textschluessel instances.
     *
     * @see #assertValidProperties()
     * @see #parseResources()
     * @see #transformDocument(Document)
     */
    private synchronized void assertInitialized()
    {
        try
        {
            if ( System.currentTimeMillis() - this.lastCheck > this.getReloadIntervalMillis() &&
                 !this.monitorMap.isEmpty() )
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

                final List/*<Document>*/ documents = this.parseResources();
                final Collection parsedTextschluessel = new LinkedList();

                for ( final Iterator it = documents.iterator(); it.hasNext(); )
                {
                    final Document document = (Document) it.next();
                    parsedTextschluessel.addAll( this.transformDocument( document ) );
                }

                final Map types = new HashMap( parsedTextschluessel.size() );
                final Collection checked = new ArrayList( parsedTextschluessel.size() );

                for ( final Iterator it = parsedTextschluessel.iterator(); it.hasNext(); )
                {
                    Map keys;
                    final Textschluessel i = (Textschluessel) it.next();
                    final Integer key = new Integer( i.getKey() );
                    final Integer ext = new Integer( i.getExtension() );

                    if ( ( keys = (Map) types.get( key ) ) == null )
                    {
                        keys = new HashMap();
                        types.put( key, keys );
                    }

                    if ( keys.put( ext, i ) != null )
                    {
                        throw new IllegalStateException( this.getDuplicateTextschluesselMessage(
                            this.getLocale(), key, ext ) );

                    }

                    checked.add( i );
                }

                this.instances = (Textschluessel[]) checked.toArray( new Textschluessel[ checked.size() ] );
                this.getLogger().info( this.getTextschluesselInfoMessage(
                    this.getLocale(), new Integer( this.instances.length ), new Integer( documents.size() ) ) );

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
     * Gets XML resources provided by any available {@code TextschluesselProvider} implementation.
     *
     * @return XML resources provided by any available {@code TextschluesselProvider} implementation.
     *
     * @throws IOException if retrieving the resources fails.
     *
     * @see JaxpTextschluesselProvider
     */
    private URL[] getResources() throws IOException
    {
        final Collection resources = new HashSet();
        final JaxpTextschluesselProvider[] provider = this.getTextschluesselProvider();

        for ( int i = provider.length - 1; i >= 0; i-- )
        {
            resources.addAll( Arrays.asList( provider[i].getResources() ) );
        }

        return (URL[]) resources.toArray( new URL[ resources.size() ] );
    }

    /**
     * Adds a resource to the list of resources to monitor for changes.
     *
     * @param url the URL of the resource to monitor for changes.
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
     * @return the parsed XML documents.
     *
     * @see #getResources()
     * @see #getDocumentBuilder()
     *
     * @throws ParserConfigurationException if configuring the parser fails.
     * @throws IOException if reading resources fails.
     * @throws SAXException if parsing fails.
     */
    private List/*<Document>*/ parseResources() throws ParserConfigurationException, IOException, SAXException
    {
        InputStream stream = null;

        final URL[] resources = this.getResources();
        final List documents = new LinkedList();
        final DocumentBuilder validatingParser = this.getDocumentBuilder();
        final DocumentBuilderFactory namespaceAwareFactory = DocumentBuilderFactory.newInstance();

        namespaceAwareFactory.setNamespaceAware( true );
        final DocumentBuilder nonValidatingParser = namespaceAwareFactory.newDocumentBuilder();

        final Task task = new Task();
        task.setCancelable( false );
        task.setDescription( new ReadsTextschluesselMessage() );
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

                    public void warning( final SAXParseException e )
                        throws SAXException
                    {
                        getLogger().warn( getParseExceptionMessage(
                            getLocale(), resource.toExternalForm(),
                            e.getMessage(), new Integer( e.getLineNumber() ),
                            new Integer( e.getColumnNumber() ) ) );

                    }

                    public void error( final SAXParseException e )
                        throws SAXException
                    {
                        throw new SAXException( getParseExceptionMessage(
                            getLocale(), resource.toExternalForm(),
                            e.getMessage(), new Integer( e.getLineNumber() ),
                            new Integer( e.getColumnNumber() ) ), e );

                    }

                    public void fatalError( final SAXParseException e )
                        throws SAXException
                    {
                        throw new SAXException( getParseExceptionMessage(
                            getLocale(), resource.toExternalForm(),
                            e.getMessage(), new Integer( e.getLineNumber() ),
                            new Integer( e.getColumnNumber() ) ), e );

                    }

                };

                nonValidatingParser.setErrorHandler( errorHandler );
                validatingParser.setErrorHandler( errorHandler );

                this.monitorResource( resource );
                stream = resource.openStream();
                Document doc = nonValidatingParser.parse( stream );
                if ( doc.getDocumentElement().hasAttributeNS( XSI_NS, "schemaLocation" ) )
                {
                    stream.close();
                    stream = resource.openStream();
                    doc = validatingParser.parse( stream );
                }
                else if ( this.getLogger().isInfoEnabled() )
                {
                    this.getLogger().info(
                        this.getNoSchemaLocationMessage( this.getLocale(), resource.toExternalForm() ) );
                }

                documents.add( doc );
                stream.close();
            }

            return documents;
        }
        finally
        {
            this.getTaskMonitor().finish( task );
        }
    }

    /**
     * Transforms a document to the Textschluessel instances it contains.
     *
     * @param doc the document to transform.
     *
     * @return an array of Textschluessel instances from the given document.
     *
     * @throws IllegalArgumentException if {@code doc} cannot be transformed.
     * @throws ParseException if parsing fails.
     *
     * @see #transformTextschluesselDocument(Document)
     * @see #transformBankingDocument(Document)
     */
    private List/*<Textschluessel>*/ transformDocument( final Document doc ) throws ParseException
    {
        String modelVersion = null;
        final String namespace = doc.getDocumentElement().getNamespaceURI();

        if ( namespace == null )
        {
            throw new RuntimeException( this.getUnsupportedNamespaceMessage( this.getLocale(), namespace ) );
        }
        else if ( TEXTSCHLUESSEL_NS.equals( namespace ) )
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

        final List textschluessel = new LinkedList();

        if ( namespace.equals( TEXTSCHLUESSEL_NS ) )
        {
            textschluessel.addAll( this.transformTextschluesselDocument( doc ) );
        }
        else if ( namespace.equals( BANKING_NS ) )
        {
            textschluessel.addAll( this.transformBankingDocument( doc ) );
        }

        return textschluessel;
    }

    /**
     * Transforms a document from deprecated {@code textschluessel} namespace to the {@code Textschluessel} instances it
     * contains.
     *
     * @param doc the document to transform.
     *
     * @return an list of Textschluessel instances from the given document.
     *
     * @throws IllegalArgumentException if {@code doc} contains invalid content.
     */
    private List/*<Textschluessel>*/ transformTextschluesselDocument( final Document doc )
    {
        final List list = new LinkedList();
        final NodeList typeList = doc.getDocumentElement().getElementsByTagNameNS(
            TEXTSCHLUESSEL_NS, "transactionTypes" );

        for ( int i = typeList.getLength() - 1; i >= 0; i-- )
        {
            final Element parent = (Element) typeList.item( i );
            if ( parent.getParentNode().equals( doc.getDocumentElement() ) )
            {
                final NodeList type = parent.getElementsByTagNameNS( TEXTSCHLUESSEL_NS, "transactionType" );
                for ( int t = type.getLength() - 1; t >= 0; t-- )
                {
                    final Element e = (Element) type.item( t );
                    if ( e.getParentNode().equals( parent ) )
                    {
                        final Textschluessel textschluessel = new Textschluessel();
                        list.add( textschluessel );

                        final String textschluesselType = e.getAttributeNS( TEXTSCHLUESSEL_NS, "type" );
                        textschluessel.setDebit( "DEBIT".equals( textschluesselType ) );
                        textschluessel.setRemittance( "REMITTANCE".equals( textschluesselType ) );
                        textschluessel.setKey( Integer.valueOf(
                            e.getAttributeNS( TEXTSCHLUESSEL_NS, "key" ) ).intValue() );

                        final String extension = e.getAttributeNS( TEXTSCHLUESSEL_NS, "extension" );
                        if ( "VARIABLE".equals( extension ) )
                        {
                            textschluessel.setVariable( true );
                            textschluessel.setExtension( 0 );
                        }
                        else
                        {
                            textschluessel.setExtension( Integer.valueOf( extension ).intValue() );
                        }

                        final NodeList descriptions = e.getElementsByTagNameNS( TEXTSCHLUESSEL_NS, "description" );
                        for ( int d = descriptions.getLength() - 1; d >= 0; d-- )
                        {
                            final Element description = (Element) descriptions.item( d );

                            if ( description.getParentNode().equals( e ) )
                            {
                                final String language = description.getAttributeNS( TEXTSCHLUESSEL_NS, "language" );
                                final String text = description.getFirstChild().getNodeValue();
                                textschluessel.setShortDescription( new Locale( language.toLowerCase() ), text );
                            }
                        }
                    }
                }
            }
        }

        return list;
    }

    /**
     * Transforms a document from the {@code banking} namespace to the {@code Textschluessel} instances it contains.
     *
     * @param doc the document to transform.
     *
     * @return an list of Textschluessel instances from the given document.
     *
     * @throws IllegalArgumentException if {@code doc} contains invalid content.
     * @throws ParseException if parsing fails.
     */
    private List/*<Textschluessel>*/ transformBankingDocument( final Document doc ) throws ParseException
    {
        final List list = new LinkedList();
        final Calendar cal = Calendar.getInstance();
        final DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        final String systemLanguage = Locale.getDefault().getLanguage().toLowerCase();

        final NodeList typeList = doc.getDocumentElement().getElementsByTagNameNS( BANKING_NS, "textschluessel" );
        for ( int i = typeList.getLength() - 1; i >= 0; i-- )
        {
            final Element e = (Element) typeList.item( i );
            if ( e.getParentNode().equals( doc.getDocumentElement() ) )
            {
                final Textschluessel textschluessel = new Textschluessel();
                list.add( textschluessel );

                textschluessel.setKey( Integer.valueOf( e.getAttributeNS( BANKING_NS, "key" ) ).intValue() );
                if ( e.hasAttributeNS( BANKING_NS, "extension" ) )
                {
                    textschluessel.setExtension( Integer.valueOf(
                        e.getAttributeNS( BANKING_NS, "extension" ) ).intValue() );

                }

                textschluessel.setDebit( Boolean.valueOf( e.getAttributeNS( BANKING_NS, "debit" ) ).booleanValue() );
                textschluessel.setRemittance( Boolean.valueOf(
                    e.getAttributeNS( BANKING_NS, "remittance" ) ).booleanValue() );

                textschluessel.setVariable( Boolean.valueOf(
                    e.getAttributeNS( BANKING_NS, "variableExtension" ) ).booleanValue() );

                final NodeList texts = e.getElementsByTagNameNS( BANKING_NS, "texts" );
                if ( e.hasAttributeNS( BANKING_NS, "validFrom" ) )
                {
                    cal.setTime( dateFormat.parse( e.getAttributeNS( BANKING_NS, "validFrom" ) ) );
                    cal.set( Calendar.HOUR_OF_DAY, 0 );
                    cal.set( Calendar.MINUTE, 0 );
                    cal.set( Calendar.SECOND, 0 );
                    cal.set( Calendar.MILLISECOND, 0 );
                    textschluessel.setValidFrom( cal.getTime() );
                }

                if ( e.hasAttributeNS( BANKING_NS, "validTo" ) )
                {
                    cal.setTime( dateFormat.parse( e.getAttributeNS( BANKING_NS, "validTo" ) ) );
                    cal.set( Calendar.HOUR_OF_DAY, 0 );
                    cal.set( Calendar.MINUTE, 0 );
                    cal.set( Calendar.SECOND, 0 );
                    cal.set( Calendar.MILLISECOND, 0 );
                    textschluessel.setValidTo( cal.getTime() );
                }

                for ( int t = texts.getLength() - 1; t >= 0; t-- )
                {
                    final Element textsElement = (Element) texts.item( t );
                    if ( textsElement.getParentNode().equals( e ) )
                    {
                        final String defaultLanguage =
                            textsElement.getAttributeNS( BANKING_NS, "defaultLanguage" ).toLowerCase();

                        boolean hasSystemLanguage = false;
                        String defaultText = null;

                        final NodeList l = textsElement.getElementsByTagNameNS( BANKING_NS, "text" );

                        for ( int d = l.getLength() - 1; d >= 0; d-- )
                        {
                            final Element description = (Element) l.item( d );
                            if ( description.getParentNode().equals( textsElement ) )
                            {
                                final String language = description.getAttributeNS(
                                    BANKING_NS, "language" ).toLowerCase();

                                final String text = description.getFirstChild().getNodeValue();

                                if ( language.equals( defaultLanguage ) )
                                {
                                    defaultText = text;
                                }

                                if ( systemLanguage.equals( language ) )
                                {
                                    hasSystemLanguage = true;
                                }

                                textschluessel.setShortDescription( new Locale( language ), text );
                            }
                        }

                        if ( !hasSystemLanguage )
                        {
                            textschluessel.setShortDescription( new Locale( systemLanguage ), defaultText );
                        }
                    }
                }
            }
        }

        return list;
    }

    /**
     * Creates a new {@code DocumentBuilder} to use for parsing the XML resources.
     * <p>This method tries to set the following JAXP property on the system's default XML parser:
     * <ul>
     * <li>{@code http://java.sun.com/xml/jaxp/properties/schemaLanguage} set to
     * {@code http://www.w3.org/2001/XMLSchema}</li>
     * </ul>When setting this property fails, a non-validating {@code DocumentBuilder} is returned and a warning message
     * is logged.</p>
     *
     * @return a new {@code DocumentBuilder} to be used for parsing resources.
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
        catch ( IllegalArgumentException e )
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

    /** Standard implementation constructor <code>org.jdtaus.banking.ri.txtdirectory.JaxpTextschluesselVerzeichnis</code>. */
    public JaxpTextschluesselVerzeichnis()
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
     * Gets the configured <code>TextschluesselProvider</code> implementation.
     *
     * @return The configured <code>TextschluesselProvider</code> implementation.
     */
    private JaxpTextschluesselProvider[] getTextschluesselProvider()
    {
        return (JaxpTextschluesselProvider[]) ContainerFactory.getContainer().
            getDependency( this, "TextschluesselProvider" );

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
     * @return Default number of Textschlüssel for which progress monitoring gets enabled.
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
     * Gets the text of message <code>textschluesselInfo</code>.
     * <blockquote><pre>{1,choice,0#Kein Dokument|1#Ein Dokument|1<{1} Dokumente} gelesen. {0,choice,0#Keine|1#Einen|1<{0}} Textschlüssel verarbeitet.</pre></blockquote>
     * <blockquote><pre>Read {1,choice,0#no document|1#one document|1<{1} documents}. Processed {0,choice,0#no entities|1#one entity|1<{0} entities}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param entityCount format argument.
     * @param documentCount format argument.
     *
     * @return the text of message <code>textschluesselInfo</code>.
     */
    private String getTextschluesselInfoMessage( final Locale locale,
            final java.lang.Number entityCount,
            final java.lang.Number documentCount )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "textschluesselInfo", locale,
                new Object[]
                {
                    entityCount,
                    documentCount
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
     * Gets the text of message <code>duplicateTextschluessel</code>.
     * <blockquote><pre>Textschlüssel {0,number,00}{1,number,000}  ist mehrfach vorhanden.</pre></blockquote>
     * <blockquote><pre>Non-unique Textschluessel {0,number,00}{1,number,000}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param key format argument.
     * @param extension format argument.
     *
     * @return the text of message <code>duplicateTextschluessel</code>.
     */
    private String getDuplicateTextschluesselMessage( final Locale locale,
            final java.lang.Number key,
            final java.lang.Number extension )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "duplicateTextschluessel", locale,
                new Object[]
                {
                    key,
                    extension
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

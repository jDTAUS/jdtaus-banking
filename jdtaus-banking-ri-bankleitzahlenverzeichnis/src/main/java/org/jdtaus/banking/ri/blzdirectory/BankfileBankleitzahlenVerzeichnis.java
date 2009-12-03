/*
 *  jDTAUS Banking RI Bankleitzahlenverzeichnis
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
package org.jdtaus.banking.ri.blzdirectory;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.BankleitzahlExpirationException;
import org.jdtaus.banking.BankleitzahlInfo;
import org.jdtaus.banking.BankleitzahlenVerzeichnis;
import org.jdtaus.banking.messages.OutdatedBankleitzahlenVerzeichnisMessage;
import org.jdtaus.banking.messages.ReadsBankleitzahlenDateiMessage;
import org.jdtaus.banking.messages.SearchesBankleitzahlInfosMessage;
import org.jdtaus.banking.util.BankleitzahlenDatei;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.PropertyException;
import org.jdtaus.core.logging.spi.Logger;
import org.jdtaus.core.monitor.spi.Task;
import org.jdtaus.core.monitor.spi.TaskMonitor;
import org.jdtaus.core.text.Message;
import org.jdtaus.core.text.MessageEvent;
import org.jdtaus.core.text.spi.ApplicationLogger;

/**
 * {@code BankleitzahlenVerzeichnis} implementation backed by bank files.
 * <p>This implementation uses bank file resources provided by any available {@link BankfileProvider}
 * implementation.</p>
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class BankfileBankleitzahlenVerzeichnis implements BankleitzahlenVerzeichnis
{

    /** Flag indicating that initialization has been performed. */
    private boolean initialized;

    /** {@code BankleitzahlenDatei} delegate. */
    private BankleitzahlenDatei bankFile;

    /** Maps bank codes to a list of outdated records. */
    private final Map outdated = new HashMap( 5000 );

    /** Date of expiration. */
    private Date dateOfExpiration;

    /** Timestamp providers got checked for modifications. */
    private long lastModificationCheck = System.currentTimeMillis();

    /** Number of milliseconds to pass before providers are checked for modifications. */
    private Long reloadIntervalMillis;

    /** Provider checked for modifications. */
    private BankfileProvider provider;

    /** Last modification of the provider checked for modifications. */
    private long lastModifiedMillis;

    /**
     * Creates a new {@code BankfileBankleitzahlenVerzeichnis} instance taking the number of milliseconds to pass before
     * resources are checked for modifications.
     *
     * @param reloadIntervalMillis number of milliseconds to pass before resources are checked for modifications.
     */
    public BankfileBankleitzahlenVerzeichnis( final long reloadIntervalMillis )
    {
        this();

        if ( reloadIntervalMillis > 0 )
        {
            this.reloadIntervalMillis = new Long( reloadIntervalMillis );
        }
    }

    public Date getDateOfExpiration()
    {
        this.assertValidProperties();
        this.assertInitialized();
        return this.dateOfExpiration;
    }

    public BankleitzahlInfo getHeadOffice( final Bankleitzahl bankCode ) throws BankleitzahlExpirationException
    {
        if ( bankCode == null )
        {
            throw new NullPointerException( "bankCode" );
        }

        this.assertValidProperties();
        this.assertInitialized();

        BankleitzahlInfo ret = null;
        final BankleitzahlInfo[] matches = this.findByBankCode( bankCode, false );

        if ( matches.length == 1 )
        {
            ret = matches[0];
        }
        else
        {
            this.checkReplacement( bankCode );
        }

        return ret;
    }

    public BankleitzahlInfo[] getBranchOffices( final Bankleitzahl bankCode ) throws BankleitzahlExpirationException
    {
        if ( bankCode == null )
        {
            throw new NullPointerException( "bankCode" );
        }

        this.assertValidProperties();
        this.assertInitialized();

        final BankleitzahlInfo[] matches = this.findByBankCode( bankCode, true );

        if ( matches.length == 0 )
        {
            this.checkReplacement( bankCode );
        }

        return matches;
    }

    public final BankleitzahlInfo[] search( final String name, final String postalCode, final String city,
                                            final boolean branchOffices )
    {
        return this.searchBankleitzahlInfos( name, postalCode, city, Boolean.valueOf( !branchOffices ),
                                             Boolean.valueOf( branchOffices ) );

    }

    public BankleitzahlInfo[] searchBankleitzahlInfos( final String name, final String postalCode, final String city,
                                                       final Boolean headOffices, final Boolean branchOffices )
    {
        this.assertValidProperties();
        this.assertInitialized();

        final BankleitzahlInfo[] records =
            this.bankFile == null ? new BankleitzahlInfo[ 0 ] : this.bankFile.getRecords();

        final Task task = new Task();
        task.setCancelable( true );
        task.setDescription( new SearchesBankleitzahlInfosMessage() );
        task.setIndeterminate( false );
        task.setMinimum( 0 );
        task.setMaximum( records.length - 1 );
        task.setProgress( 0 );

        try
        {
            this.getTaskMonitor().monitor( task );

            final NumberFormat plzFmt = new DecimalFormat( "00000" );
            final Pattern namePattern =
                name != null ? Pattern.compile( ".*" + name.toUpperCase() + ".*" ) : null;

            final Pattern postalPattern =
                postalCode != null ? Pattern.compile( ".*" + postalCode.toUpperCase() + ".*" ) : null;

            final Pattern cityPattern =
                city != null ? Pattern.compile( ".*" + city.toUpperCase() + ".*" ) : null;

            final Collection col = new ArrayList( records.length );

            for ( int i = records.length - 1; i >= 0 && !task.isCancelled(); i-- )
            {
                final String plz = plzFmt.format( records[i].getPostalCode() );
                task.setProgress( task.getMaximum() - i );

                if ( ( namePattern == null
                       ? true : namePattern.matcher( records[i].getName().toUpperCase() ).matches() ) &&
                     ( postalPattern == null
                       ? true : postalPattern.matcher( plz ).matches() ) &&
                     ( cityPattern == null
                       ? true : cityPattern.matcher( records[i].getCity().toUpperCase() ).matches() ) &&
                     ( headOffices == null
                       ? true : records[i].isHeadOffice() == headOffices.booleanValue() ) &&
                     ( branchOffices == null
                       ? true : records[i].isHeadOffice() != branchOffices.booleanValue() ) )
                {
                    col.add( records[i].clone() );
                }
            }

            if ( task.isCancelled() )
            {
                col.clear();
            }

            return (BankleitzahlInfo[]) col.toArray( new BankleitzahlInfo[ col.size() ] );
        }
        catch ( final PatternSyntaxException e )
        {
            throw (IllegalArgumentException) new IllegalArgumentException( e.getMessage() ).initCause( e );
        }
        finally
        {
            this.getTaskMonitor().finish( task );
        }
    }

    /**
     * Gets the number of milliseconds to pass before providers are checked for modifications.
     *
     * @return The number of milliseconds to pass before providers are checked for modifications.
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
     * Initializes the instance.
     *
     * @throws RuntimeException if initialization fails.
     *
     * @see #assertValidProperties()
     */
    private synchronized void assertInitialized()
    {
        Task task = null;

        try
        {
            if ( this.provider == null ||
                 System.currentTimeMillis() - this.lastModificationCheck > this.getReloadIntervalMillis() )
            {
                this.lastModificationCheck = System.currentTimeMillis();
                if ( this.provider == null || this.provider.getLastModifiedMillis() != this.lastModifiedMillis )
                {
                    this.outdated.clear();
                    this.bankFile = null;
                    this.dateOfExpiration = null;
                    this.initialized = false;

                    if ( this.provider != null )
                    {
                        this.getLogger().info( this.getReloadInfoMessage( this.getLocale(), new Date(
                            this.lastModifiedMillis ), new Date( this.provider.getLastModifiedMillis() ) ) );

                    }
                }
            }

            if ( !this.initialized )
            {
                final DateFormat dateFormat = new SimpleDateFormat( this.getDateOfExpirationPattern() );
                this.dateOfExpiration = dateFormat.parse( this.getDateOfExpirationText() );
                final BankfileProvider bankfileProvider = this.getLatestBankfileProvider();

                if ( bankfileProvider != null && bankfileProvider.getBankfileCount() > 0 )
                {
                    this.provider = bankfileProvider;
                    this.lastModifiedMillis = bankfileProvider.getLastModifiedMillis();
                    this.dateOfExpiration =
                        bankfileProvider.getDateOfExpiration( bankfileProvider.getBankfileCount() - 1 );

                    final URL[] rsrc = new URL[ bankfileProvider.getBankfileCount() ];
                    for ( int i = 0; i < rsrc.length; i++ )
                    {
                        rsrc[i] = bankfileProvider.getBankfile( i );
                    }

                    task = new Task();
                    task.setIndeterminate( false );
                    task.setCancelable( false );
                    task.setDescription( new ReadsBankleitzahlenDateiMessage() );
                    task.setMinimum( 0 );
                    task.setProgress( 0 );
                    task.setMaximum( rsrc.length );
                    this.getTaskMonitor().monitor( task );

                    int progress = 0;
                    long processedRecords = 0L;
                    task.setProgress( progress++ );
                    this.bankFile = new BankleitzahlenDatei( rsrc[0] );
                    processedRecords += this.bankFile.getRecords().length;
                    for ( int i = 1; i < rsrc.length; i++ )
                    {
                        task.setProgress( progress++ );
                        final BankleitzahlenDatei update = new BankleitzahlenDatei( rsrc[i] );

                        // Build mapping of outdated records.
                        final BankleitzahlInfo[] records = this.bankFile.getRecords();

                        for ( int j = records.length - 1; j >= 0; j-- )
                        {
                            if ( records[j].getChangeLabel() == 'D' &&
                                 update.getRecord( records[j].getSerialNumber() ) == null )
                            {
                                List l = (List) this.outdated.get( records[j].getBankCode() );

                                if ( l == null )
                                {
                                    l = new LinkedList();
                                    this.outdated.put( records[j].getBankCode(), l );
                                }

                                l.add( records[j] );
                            }
                        }

                        this.bankFile.update( update );
                        processedRecords += update.getRecords().length;
                    }

                    // Remove all outdated records for which another record with the same Bankleitzahl still exists.
                    for ( final Iterator it = this.outdated.keySet().iterator(); it.hasNext(); )
                    {
                        final Bankleitzahl key = (Bankleitzahl) it.next();
                        if ( this.findByBankCode( key, false ).length > 0 )
                        {
                            it.remove();
                        }
                    }

                    // Log outdated records.
                    if ( this.getLogger().isDebugEnabled() )
                    {
                        for ( final Iterator it = this.outdated.keySet().iterator(); it.hasNext(); )
                        {
                            final Bankleitzahl blz = (Bankleitzahl) it.next();
                            this.getLogger().debug( this.getOutdatedInfoMessage(
                                this.getLocale(), blz.format( Bankleitzahl.LETTER_FORMAT ) ) );

                        }
                    }

                    this.initialized = true;

                    this.getLogger().info( this.getBankfileInfoMessage(
                        this.getLocale(), new Long( processedRecords ), new Integer( rsrc.length ) ) );

                    // Log an application message if the directory is outdated.
                    if ( new Date().after( this.getDateOfExpiration() ) )
                    {
                        this.getApplicationLogger().log( new MessageEvent( this, new Message[]
                            {
                                new OutdatedBankleitzahlenVerzeichnisMessage( this.getDateOfExpiration() )
                            }, MessageEvent.NOTIFICATION ) );

                    }
                }
            }
        }
        catch ( final ParseException e )
        {
            throw new RuntimeException( e );
        }
        catch ( final IOException e )
        {
            throw new RuntimeException( e );
        }
        finally
        {
            if ( task != null )
            {
                this.getTaskMonitor().finish( task );
            }
        }
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException if configured properties hold invalid values.
     */
    private void assertValidProperties()
    {
        if ( this.getReloadIntervalMillis() < 0L )
        {
            throw new PropertyException( "reloadIntervalMillis", Long.toString( this.getReloadIntervalMillis() ) );
        }
        if ( this.getDateOfExpirationText() == null || this.getDateOfExpirationText().length() == 0 )
        {
            throw new PropertyException( "dateOfExpirationText", this.getDateOfExpirationText() );
        }
        if ( this.getDateOfExpirationPattern() == null || this.getDateOfExpirationPattern().length() == 0 )
        {
            throw new PropertyException( "dateOfExpirationPattern", this.getDateOfExpirationPattern() );
        }

        try
        {
            final DateFormat dateFormat = new SimpleDateFormat( this.getDateOfExpirationPattern() );
            dateFormat.parse( this.getDateOfExpirationText() );
        }
        catch ( final ParseException e )
        {
            throw new PropertyException( "dateOfExpirationText", this.getDateOfExpirationText(), e );
        }
    }

    /**
     * Throws a {@code BankleitzahlExpirationException} if {@code bankCode} is outdated and if a valid replacement
     * record exists in the directory.
     *
     * @param bankCode the Bankleitzahl to check for expiration.
     *
     * @throws NullPointerException if {@code bankCode} is {@code null}.
     */
    private void checkReplacement( final Bankleitzahl bankCode ) throws BankleitzahlExpirationException
    {
        if ( bankCode == null )
        {
            throw new NullPointerException( "bankCode" );
        }

        final List l = (List) this.outdated.get( bankCode );

        if ( l != null )
        {
            // Finds the most recent record specifying a replacing Bankleitzahl.
            BankleitzahlInfo current = null;
            BankleitzahlInfo record = null;

            for ( final Iterator it = l.iterator(); it.hasNext(); )
            {
                current = (BankleitzahlInfo) it.next();
                if ( current.getReplacingBankCode() != null )
                {
                    record = current;
                }
            }

            // Only throw an exception for records specifying a replacing Bankleitzahl which is not outdated.
            if ( record != null )
            {
                final BankleitzahlInfo[] replacement = this.findByBankCode( record.getReplacingBankCode(), false );
                assert replacement.length == 0 || replacement.length == 1 :
                    "Multiple head offices for '" + record.getReplacingBankCode() + "'.";

                if ( replacement.length == 1 )
                {
                    throw new BankleitzahlExpirationException( record, replacement[0] );
                }
            }
        }
    }

    /**
     * Gets the {@code BankfileProvider} with the latest date of expiration from the available
     * {@code BankfileProvider}s.
     *
     * @return The {@code BankfileProvider} with the latest date of expiration or {@code null}, if no providers are
     * available.
     *
     * @throws IOException if getting the provider fails.
     *
     * @see BankfileProvider
     */
    private BankfileProvider getLatestBankfileProvider() throws IOException
    {
        final BankfileProvider[] providers = this.getBankfileProvider();
        BankfileProvider latest = null;

        for ( int i = providers.length - 1; i >= 0; i-- )
        {
            if ( providers[i].getBankfileCount() > 0 &&
                 ( latest == null || latest.getDateOfExpiration( latest.getBankfileCount() - 1 ).
                before( providers[i].getDateOfExpiration( providers[i].getBankfileCount() - 1 ) ) ) )
            {
                latest = providers[i];
            }
        }

        return latest;
    }

    /**
     * Gets all records matching {@code bankCode} with {@code isHeadOffice() != branchOffices}.
     *
     * @param bankCode the bank code to return matching records for.
     * @param branchOffices {@code true} to return all known branch offices matching {@code bankCode}; {@code false} to
     * return all known head offices matching {@code bankCode}.
     */
    private BankleitzahlInfo[] findByBankCode( final Bankleitzahl bankCode, final boolean branchOffices )
    {
        final BankleitzahlInfo[] records =
            this.bankFile == null ? new BankleitzahlInfo[ 0 ] : this.bankFile.getRecords();

        final Collection col = new ArrayList( records.length );

        for ( int i = records.length - 1; i >= 0; i-- )
        {
            if ( records[i].getBankCode().equals( bankCode ) &&
                 records[i].isHeadOffice() != branchOffices && !col.add( records[i] ) )
            {
                throw new IllegalStateException( this.getDuplicateRecordMessage(
                    this.getLocale(), records[i].getSerialNumber(), bankCode.format( Bankleitzahl.LETTER_FORMAT ) ) );

            }
        }

        return (BankleitzahlInfo[]) col.toArray( new BankleitzahlInfo[ col.size() ] );
    }

    //--Constructors------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausConstructors
    // This section is managed by jdtaus-container-mojo.

    /** Standard implementation constructor <code>org.jdtaus.banking.ri.blzdirectory.BankfileBankleitzahlenVerzeichnis</code>. */
    public BankfileBankleitzahlenVerzeichnis()
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
     * Gets the configured <code>ApplicationLogger</code> implementation.
     *
     * @return The configured <code>ApplicationLogger</code> implementation.
     */
    private ApplicationLogger getApplicationLogger()
    {
        return (ApplicationLogger) ContainerFactory.getContainer().
            getDependency( this, "ApplicationLogger" );

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
     * Gets the configured <code>BankfileProvider</code> implementation.
     *
     * @return The configured <code>BankfileProvider</code> implementation.
     */
    private BankfileProvider[] getBankfileProvider()
    {
        return (BankfileProvider[]) ContainerFactory.getContainer().
            getDependency( this, "BankfileProvider" );

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
     * @return Default number of milliseconds to pass before providers are checked for modifications.
     */
    private java.lang.Long getDefaultReloadIntervalMillis()
    {
        return (java.lang.Long) ContainerFactory.getContainer().
            getProperty( this, "defaultReloadIntervalMillis" );

    }

    /**
     * Gets the value of property <code>dateOfExpirationText</code>.
     *
     * @return The date of expiration of the directory.
     */
    private java.lang.String getDateOfExpirationText()
    {
        return (java.lang.String) ContainerFactory.getContainer().
            getProperty( this, "dateOfExpirationText" );

    }

    /**
     * Gets the value of property <code>dateOfExpirationPattern</code>.
     *
     * @return Format pattern of the date of expiration property.
     */
    private java.lang.String getDateOfExpirationPattern()
    {
        return (java.lang.String) ContainerFactory.getContainer().
            getProperty( this, "dateOfExpirationPattern" );

    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>outdatedInfo</code>.
     * <blockquote><pre>Bankleitzahl {0} ist veraltet.</pre></blockquote>
     * <blockquote><pre>Bankleitzahl {0} is outdated.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param bankleitzahl format argument.
     *
     * @return the text of message <code>outdatedInfo</code>.
     */
    private String getOutdatedInfoMessage( final Locale locale,
            final java.lang.String bankleitzahl )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "outdatedInfo", locale,
                new Object[]
                {
                    bankleitzahl
                });

    }

    /**
     * Gets the text of message <code>duplicateRecord</code>.
     * <blockquote><pre>Mehrere Bankleitzahlendatei-Datensätze mit Seriennummer {0,number} während der Suche nach Bankleitzahl {1}.</pre></blockquote>
     * <blockquote><pre>Multiple bankfile records with serial number {0,number} detected during searching the directory for bankcode {1}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param serialNumber format argument.
     * @param bankleitzahl format argument.
     *
     * @return the text of message <code>duplicateRecord</code>.
     */
    private String getDuplicateRecordMessage( final Locale locale,
            final java.lang.Number serialNumber,
            final java.lang.String bankleitzahl )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "duplicateRecord", locale,
                new Object[]
                {
                    serialNumber,
                    bankleitzahl
                });

    }

    /**
     * Gets the text of message <code>bankfileInfo</code>.
     * <blockquote><pre>{1,choice,0#Keine Bankleitzahlendatei|1#Eine Bankleitzahlendatei|1<{1} Bankleitzahlendateien} gelesen. {0,choice,0#Keine Datensätze|1#Einen Datensatz|1<{0} Datensätze} verarbeitet.</pre></blockquote>
     * <blockquote><pre>Read {1,choice,0#no bankfile|1#one bankfile|1<{1} bankfiles}. Processed {0,choice,0#no entities|1#one entity|1<{0} entities}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param entityCount format argument.
     * @param bankfileCount format argument.
     *
     * @return the text of message <code>bankfileInfo</code>.
     */
    private String getBankfileInfoMessage( final Locale locale,
            final java.lang.Number entityCount,
            final java.lang.Number bankfileCount )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "bankfileInfo", locale,
                new Object[]
                {
                    entityCount,
                    bankfileCount
                });

    }

    /**
     * Gets the text of message <code>reloadInfo</code>.
     * <blockquote><pre>Änderungszeitstempel des Bankleitzahlen-Providers von ''{0,date} {0,time}'' zu ''{1,date} {1,time}''. Lädt neue Bankleitzahlen.</pre></blockquote>
     * <blockquote><pre>Provider's last modification timestamp changed from ''{0,date} {0,time}'' to ''{1,date} {1,time}''. Loading new bankcodes.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param lastModification format argument.
     * @param lastProviderModification format argument.
     *
     * @return the text of message <code>reloadInfo</code>.
     */
    private String getReloadInfoMessage( final Locale locale,
            final java.util.Date lastModification,
            final java.util.Date lastProviderModification )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "reloadInfo", locale,
                new Object[]
                {
                    lastModification,
                    lastProviderModification
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

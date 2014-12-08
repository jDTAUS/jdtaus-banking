/*
 *  jDTAUS Banking RI Bankleitzahlenverzeichnis
 *  Copyright (C) 2005 Christian Schulte
 *  <cs@schulte.it>
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
import java.util.Locale;
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
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public class BankfileBankleitzahlenVerzeichnis implements BankleitzahlenVerzeichnis
{

    /** Flag indicating that initialization has been performed. */
    private boolean initialized;

    /** {@code BankleitzahlenDatei} delegate. */
    private BankleitzahlenDatei bankFile;

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

    /** Number of bank codes for which progress monitoring gets enabled. */
    private Long monitoringThreshold;

    /**
     * Creates a new {@code BankfileBankleitzahlenVerzeichnis} instance taking the number of milliseconds to pass before
     * resources are checked for modifications and the number of bank codes for which progress monitoring gets enabled.
     *
     * @param reloadIntervalMillis Number of milliseconds to pass before resources are checked for modifications.
     * @param monitoringThreshold Number of bank codes for which progress monitoring gets enabled.
     */
    public BankfileBankleitzahlenVerzeichnis( final long reloadIntervalMillis, final long monitoringThreshold )
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
     * Gets the number of bank codes for which progress monitoring gets enabled.
     *
     * @return The number of bank codes for which progress monitoring gets enabled.
     */
    public long getMonitoringThreshold()
    {
        if ( this.monitoringThreshold == null )
        {
            this.monitoringThreshold = this.getDefaultMonitoringThreshold();
        }

        return this.monitoringThreshold.longValue();
    }

    public Date getDateOfExpiration()
    {
        this.assertValidProperties();
        this.assertInitialized();
        return (Date) this.dateOfExpiration.clone();
    }

    public BankleitzahlInfo getHeadOffice( final Bankleitzahl bankCode ) throws BankleitzahlExpirationException
    {
        if ( bankCode == null )
        {
            throw new NullPointerException( "bankCode" );
        }

        this.assertValidProperties();
        this.assertInitialized();

        BankleitzahlInfo headOffice = this.bankFile.getHeadOfficeRecord( bankCode );

        if ( headOffice == null )
        {
            final BankleitzahlInfo deletedHeadOfficeRecord = this.bankFile.getDeletedHeadOfficeRecord( bankCode );
            final BankleitzahlInfo replacementRecord = this.findReplacementBankeitzahlInfo( deletedHeadOfficeRecord );

            if ( replacementRecord != null
                 && ( replacementRecord.getDeletionDate() == null
                      || !replacementRecord.getDeletionDate().before( this.getDateOfExpiration() ) ) )
            {
                throw new BankleitzahlExpirationException( deletedHeadOfficeRecord, replacementRecord );
            }
        }

        return headOffice;
    }

    public BankleitzahlInfo[] getBranchOffices( final Bankleitzahl bankCode ) throws BankleitzahlExpirationException
    {
        if ( bankCode == null )
        {
            throw new NullPointerException( "bankCode" );
        }

        this.assertValidProperties();
        this.assertInitialized();

        final BankleitzahlInfo[] branchOfficeRecords = this.bankFile.getBranchOfficeRecords( bankCode );

        if ( branchOfficeRecords.length == 0 )
        {
            final BankleitzahlInfo deletedHeadOfficeRecord = this.bankFile.getDeletedHeadOfficeRecord( bankCode );
            final BankleitzahlInfo replacementRecord = this.findReplacementBankeitzahlInfo( deletedHeadOfficeRecord );

            if ( replacementRecord != null
                 && ( replacementRecord.getDeletionDate() == null
                      || !replacementRecord.getDeletionDate().before( this.getDateOfExpiration() ) ) )
            {
                throw new BankleitzahlExpirationException( deletedHeadOfficeRecord, replacementRecord );
            }
        }

        return branchOfficeRecords;
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

        final Collection col = new ArrayList( records.length );

        if ( records.length > 0 )
        {
            final Task task = new Task();
            task.setCancelable( true );
            task.setDescription( new SearchesBankleitzahlInfosMessage() );
            task.setIndeterminate( false );
            task.setMinimum( 0 );
            task.setMaximum( records.length - 1 );
            task.setProgress( 0 );

            try
            {
                if ( task.getMaximum() > this.getMonitoringThreshold() )
                {
                    this.getTaskMonitor().monitor( task );
                }

                final NumberFormat plzFmt = new DecimalFormat( "00000" );
                final Pattern namePattern =
                    name != null ? Pattern.compile( ".*" + name.toUpperCase() + ".*" ) : null;

                final Pattern postalPattern =
                    postalCode != null ? Pattern.compile( ".*" + postalCode.toUpperCase() + ".*" ) : null;

                final Pattern cityPattern =
                    city != null ? Pattern.compile( ".*" + city.toUpperCase() + ".*" ) : null;

                for ( int i = records.length - 1; i >= 0 && !task.isCancelled(); i-- )
                {
                    final String plz = plzFmt.format( records[i].getPostalCode() );
                    task.setProgress( task.getMaximum() - i );

                    if ( ( namePattern == null
                           ? true : namePattern.matcher( records[i].getName().toUpperCase() ).matches() )
                             && ( postalPattern == null
                                  ? true : postalPattern.matcher( plz ).matches() )
                             && ( cityPattern == null
                                  ? true : cityPattern.matcher( records[i].getCity().toUpperCase() ).matches() )
                             && ( headOffices == null
                                  ? true : records[i].isHeadOffice() == headOffices.booleanValue() )
                             && ( branchOffices == null
                                  ? true : records[i].isHeadOffice() != branchOffices.booleanValue() ) )
                    {
                        col.add( records[i].clone() );
                    }
                }

                if ( task.isCancelled() )
                {
                    col.clear();
                }
            }
            catch ( final PatternSyntaxException e )
            {
                throw (IllegalArgumentException) new IllegalArgumentException( e.getMessage() ).initCause( e );
            }
            finally
            {
                if ( task.getMaximum() > this.getMonitoringThreshold() )
                {
                    this.getTaskMonitor().finish( task );
                }
            }
        }

        return (BankleitzahlInfo[]) col.toArray( new BankleitzahlInfo[ col.size() ] );
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
        boolean logExpirationMessage = false;

        try
        {
            if ( this.provider == null
                     || System.currentTimeMillis() - this.lastModificationCheck > this.getReloadIntervalMillis() )
            {
                this.lastModificationCheck = System.currentTimeMillis();
                if ( this.provider == null || this.provider.getLastModifiedMillis() != this.lastModifiedMillis )
                {
                    this.bankFile = null;
                    this.dateOfExpiration = null;
                    this.initialized = false;

                    if ( this.provider != null )
                    {
                        this.getLogger().info( this.getReloadInfoMessage(
                            this.getLocale(), new Date( this.lastModifiedMillis ),
                            new Date( this.provider.getLastModifiedMillis() ) ) );

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
                    this.bankFile = new BankleitzahlenDatei( rsrc[0], bankfileProvider.getFormat( 0 ),
                                                             bankfileProvider.getDateOfValidity( 0 ),
                                                             bankfileProvider.getDateOfExpiration( 0 ) );

                    processedRecords += this.bankFile.getRecords().length;
                    for ( int i = 1; i < rsrc.length; i++ )
                    {
                        task.setProgress( progress++ );
                        final BankleitzahlenDatei update =
                            new BankleitzahlenDatei( rsrc[i], bankfileProvider.getFormat( i ),
                                                     bankfileProvider.getDateOfValidity( i ),
                                                     bankfileProvider.getDateOfExpiration( i ) );

                        this.bankFile.update( update );
                        processedRecords += update.getRecords().length;
                    }

                    // Log outdated records.
                    if ( this.getLogger().isDebugEnabled() )
                    {
                        for ( int i = 0, l0 = this.bankFile.getDeletedRecords().length; i < l0; i++ )
                        {
                            final BankleitzahlInfo record = this.bankFile.getDeletedRecords()[i];

                            if ( record.isHeadOffice() )
                            {
                                this.getLogger().debug( this.getOutdatedInfoMessage(
                                    this.getLocale(), record.getBankCode().format( Bankleitzahl.LETTER_FORMAT ) ) );

                            }
                        }
                    }

                    logExpirationMessage = true;
                    this.initialized = true;

                    this.getLogger().info( this.getBankfileInfoMessage(
                        this.getLocale(), new Long( processedRecords ), new Integer( rsrc.length ) ) );

                }
                else
                {
                    this.getLogger().warn( this.getNoBankfilesFoundMessage( this.getLocale() ) );
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

        // Log an application message if the directory is outdated.
        if ( logExpirationMessage )
        {
            if ( new Date().after( this.getDateOfExpiration() ) )
            {
                this.getApplicationLogger().log( new MessageEvent(
                    this, new Message[]
                    {
                        new OutdatedBankleitzahlenVerzeichnisMessage( this.getDateOfExpiration() )
                    }, MessageEvent.WARNING ) );

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
     * Searches for a record replacing a given record recursively.
     *
     * @param bankInfo The record to search a replacing record for or {@code null}.
     *
     * @return The record to replace {@code bankInfo} with {@code null}.
     */
    private BankleitzahlInfo findReplacementBankeitzahlInfo( final BankleitzahlInfo bankInfo )
    {
        BankleitzahlInfo replacement = null;

        if ( bankInfo != null && bankInfo.getReplacingBankCode() != null )
        {
            replacement = this.bankFile.getHeadOfficeRecord( bankInfo.getReplacingBankCode() );

            if ( replacement == null )
            {
                replacement = this.bankFile.getDeletedHeadOfficeRecord( bankInfo.getReplacingBankCode() );
            }

            final BankleitzahlInfo recurse = this.findReplacementBankeitzahlInfo( replacement );

            if ( recurse != null )
            {
                replacement = recurse;
            }
        }

        return replacement;
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
            if ( providers[i].getBankfileCount() > 0
                     && ( latest == null || latest.getDateOfExpiration( latest.getBankfileCount() - 1 ).
                         before( providers[i].getDateOfExpiration( providers[i].getBankfileCount() - 1 ) ) ) )
            {
                latest = providers[i];
            }
        }

        return latest;
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
     * Gets the value of property <code>defaultMonitoringThreshold</code>.
     *
     * @return Default number of bank codes for which progress monitoring gets enabled.
     */
    private java.lang.Long getDefaultMonitoringThreshold()
    {
        return (java.lang.Long) ContainerFactory.getContainer().
            getProperty( this, "defaultMonitoringThreshold" );

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
     * @param bankleitzahl format parameter.
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
     * Gets the text of message <code>bankfileInfo</code>.
     * <blockquote><pre>{1,choice,0#Keine Bankleitzahlendatei|1#Eine Bankleitzahlendatei|1<{1} Bankleitzahlendateien} gelesen. {0,choice,0#Keine Datensätze|1#Einen Datensatz|1<{0} Datensätze} verarbeitet.</pre></blockquote>
     * <blockquote><pre>Read {1,choice,0#no bankfile|1#one bankfile|1<{1} bankfiles}. Processed {0,choice,0#no entities|1#one entity|1<{0} entities}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param entityCount format parameter.
     * @param bankfileCount format parameter.
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
     * @param lastModification format parameter.
     * @param lastProviderModification format parameter.
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

    /**
     * Gets the text of message <code>noBankfilesFound</code>.
     * <blockquote><pre>Keine Bankleitzahlendateien gefunden.</pre></blockquote>
     * <blockquote><pre>No bankcode files found.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     *
     * @return the text of message <code>noBankfilesFound</code>.
     */
    private String getNoBankfilesFoundMessage( final Locale locale )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "noBankfilesFound", locale, null );

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

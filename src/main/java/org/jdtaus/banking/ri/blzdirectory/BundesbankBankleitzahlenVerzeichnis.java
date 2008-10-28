/*
 *  jDTAUS Banking RI Bankleitzahlenverzeichnis
 *  Copyright (c) 2005 Christian Schulte
 *
 *  Christian Schulte, Haldener Strasse 72, 58095 Hagen, Germany
 *  <cs@jdtaus.org> (+49 2331 3543887)
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.BankleitzahlExpirationException;
import org.jdtaus.banking.BankleitzahlInfo;
import org.jdtaus.banking.BankleitzahlenVerzeichnis;
import org.jdtaus.banking.messages.OutdatedBankleitzahlenVerzeichnisMessage;
import org.jdtaus.banking.messages.ReadsBankleitzahlenDateiMessage;
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
 * {@code BankleitzahlenVerzeichnis} implementation backed by bankfiles.
 * <p>This implementation uses bankfile resources provided by any available
 * {@link BankfileProvider} implementation.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class BundesbankBankleitzahlenVerzeichnis
    implements BankleitzahlenVerzeichnis
{
    //--Constructors------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausConstructors
    // This section is managed by jdtaus-container-mojo.

    /** Standard implementation constructor <code>org.jdtaus.banking.ri.blzdirectory.BundesbankBankleitzahlenVerzeichnis</code>. */
    public BundesbankBankleitzahlenVerzeichnis()
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
     * @return the configured <code>Logger</code> implementation.
     */
    private Logger getLogger()
    {
        return (Logger) ContainerFactory.getContainer().
            getDependency( this, "Logger" );

    }

    /**
     * Gets the configured <code>ApplicationLogger</code> implementation.
     *
     * @return the configured <code>ApplicationLogger</code> implementation.
     */
    private ApplicationLogger getApplicationLogger()
    {
        return (ApplicationLogger) ContainerFactory.getContainer().
            getDependency( this, "ApplicationLogger" );

    }

    /**
     * Gets the configured <code>TaskMonitor</code> implementation.
     *
     * @return the configured <code>TaskMonitor</code> implementation.
     */
    private TaskMonitor getTaskMonitor()
    {
        return (TaskMonitor) ContainerFactory.getContainer().
            getDependency( this, "TaskMonitor" );

    }

    /**
     * Gets the configured <code>BankfileProvider</code> implementation.
     *
     * @return the configured <code>BankfileProvider</code> implementation.
     */
    private BankfileProvider[] getBankfileProvider()
    {
        return (BankfileProvider[]) ContainerFactory.getContainer().
            getDependency( this, "BankfileProvider" );

    }

// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
    //--Properties--------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausProperties
    // This section is managed by jdtaus-container-mojo.

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
    //--BankleitzahlenVerzeichnis-----------------------------------------------

    public Date getDateOfExpiration()
    {
        this.assertValidProperties();
        this.assertInitialized();
        return this.dateOfExpiration;
    }

    public BankleitzahlInfo getHeadOffice( final Bankleitzahl bankCode )
        throws BankleitzahlExpirationException
    {
        if ( bankCode == null )
        {
            throw new NullPointerException( "bankCode" );
        }

        this.assertValidProperties();
        this.assertInitialized();

        BankleitzahlInfo ret = null;
        final BankleitzahlInfo[] matches =
            this.findByBankCode( bankCode, false );

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

    public BankleitzahlInfo[] getBranchOffices(
        final Bankleitzahl bankCode ) throws BankleitzahlExpirationException
    {
        if ( bankCode == null )
        {
            throw new NullPointerException( "bankCode" );
        }

        this.assertValidProperties();
        this.assertInitialized();

        final BankleitzahlInfo[] matches =
            this.findByBankCode( bankCode, true );

        if ( matches.length == 0 )
        {
            this.checkReplacement( bankCode );
        }

        return matches;
    }

    public BankleitzahlInfo[] search( final String name,
        final String postalCode,
        final String city,
        final boolean branchOffices )
    {
        this.assertValidProperties();
        this.assertInitialized();

        final Pattern namePat;
        final Pattern postalPat;
        final Pattern cityPat;
        final NumberFormat plzFmt = new DecimalFormat( "00000" );
        final BankleitzahlInfo[] records = this.bankFile == null
            ? new BankleitzahlInfo[ 0 ]
            : this.bankFile.getRecords();

        final Collection col = new ArrayList( records.length );
        String plz;

        try
        {
            namePat = name != null
                ? Pattern.compile( ".*" +
                name.toUpperCase() + ".*" )
                : null;

            postalPat = postalCode != null
                ? Pattern.compile( ".*" +
                postalCode.toUpperCase() + ".*" )
                : null;

            cityPat = city != null
                ? Pattern.compile( ".*" +
                city.toUpperCase() + ".*" )
                : null;

            for ( int i = records.length - 1; i >= 0; i-- )
            {
                plz = plzFmt.format( records[i].getPostalCode() );

                if ( ( namePat == null
                    ? true
                    : namePat.matcher( records[i].getName().
                    toUpperCase() ).matches() ) &&
                    ( postalPat == null
                    ? true
                    : postalPat.matcher( plz ).matches() ) &&
                    ( cityPat == null
                    ? true
                    : cityPat.matcher( records[i].getCity().
                    toUpperCase() ).matches() ) &&
                    ( branchOffices
                    ? true
                    : records[i].isHeadOffice() ) )
                {
                    col.add( records[i] );
                }
            }

            return (BankleitzahlInfo[]) col.toArray(
                new BankleitzahlInfo[ col.size() ] );

        }
        catch ( PatternSyntaxException e )
        {
            final RuntimeException iae = new IllegalArgumentException();
            iae.initCause( e );
            throw iae;
        }
    }

    //-----------------------------------------------BankleitzahlenVerzeichnis--
    //--BundesbankBankleitzahlenVerzeichnis-------------------------------------

    /** Flag indicating that initialization has been performed. */
    private boolean initialized;

    /** {@code BankleitzahlenDatei} delegate. */
    private BankleitzahlenDatei bankFile;

    /** Maps bankcodes to a list of outdated records. */
    private Map outdated = new HashMap( 5000 );

    /** Date of expiration. */
    private Date dateOfExpiration;

    /**
     * Initializes the instance.
     *
     * @throws RuntimeException if initialization fails.
     *
     * @see #assertValidProperties()
     * @see #getFileResources()
     */
    private void assertInitialized()
    {
        if ( !this.initialized )
        {
            int progress = 0;
            final Task task = new Task();
            long processedRecords = 0L;
            task.setIndeterminate( false );
            task.setCancelable( false );
            task.setDescription( new ReadsBankleitzahlenDateiMessage() );
            task.setMinimum( 0 );

            try
            {
                final URL[] rsrc = this.getFileResources();

                task.setMaximum( rsrc.length == 0
                    ? 0
                    : rsrc.length - 1 );

                task.setProgress( progress );

                this.getTaskMonitor().monitor( task );

                if ( rsrc.length > 0 )
                {
                    task.setProgress( progress++ );
                    this.bankFile = new BankleitzahlenDatei( rsrc[0] );
                    processedRecords += this.bankFile.getRecords().length;
                    for ( int i = 1; i < rsrc.length; i++ )
                    {
                        task.setProgress( progress++ );
                        final BankleitzahlenDatei update =
                            new BankleitzahlenDatei( rsrc[i] );

                        // Build mapping of outdated records.
                        final BankleitzahlInfo[] records =
                            this.bankFile.getRecords();

                        processedRecords += records.length;
                        for ( int j = records.length - 1; j >= 0; j-- )
                        {
                            if ( records[j].getChangeLabel() == 'D' &&
                                update.getRecord(
                                records[j].getSerialNumber() ) == null )
                            {
                                List l = (List) this.outdated.get(
                                    records[j].getBankCode() );

                                if ( l == null )
                                {
                                    l = new LinkedList();
                                    this.outdated.put( records[j].getBankCode(),
                                        l );
                                }

                                l.add( records[j] );
                            }
                        }

                        this.bankFile.update( update );
                    }

                    // Remove all outdated records for which another record
                    // with the same Bankleitzahl still exists.
                    for ( Iterator it = this.outdated.keySet().
                        iterator(); it.hasNext(); )
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
                        for ( Iterator it = this.outdated.keySet().
                            iterator(); it.hasNext(); )
                        {
                            final Bankleitzahl blz = (Bankleitzahl) it.next();
                            this.getLogger().debug(
                                this.getOutdatedInfoMessage(
                                blz.format( Bankleitzahl.LETTER_FORMAT ) ) );

                        }
                    }
                }

                this.initialized = true;

                this.getLogger().info( this.getBankfileInfoMessage(
                    new Long( processedRecords ),
                    new Integer( rsrc.length ) ) );

                // Log an application message if the directory is outdated.
                if ( new Date().after( this.getDateOfExpiration() ) )
                {
                    final MessageEvent evt = new MessageEvent(
                        this, new Message[]
                        {
                            new OutdatedBankleitzahlenVerzeichnisMessage(
                            this.getDateOfExpiration() )
                        }, MessageEvent.NOTIFICATION );

                    this.getApplicationLogger().log( evt );
                }
            }
            catch ( IOException e )
            {
                throw new RuntimeException( e );
            }
            finally
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
        if ( this.getDateOfExpirationText() == null ||
            this.getDateOfExpirationText().length() == 0 )
        {
            throw new PropertyException( "dateOfExpirationText",
                this.getDateOfExpirationText() );

        }
        if ( this.getDateOfExpirationPattern() == null ||
            this.getDateOfExpirationPattern().length() == 0 )
        {
            throw new PropertyException( "dateOfExpirationPattern",
                this.getDateOfExpirationPattern() );

        }

        try
        {
            final DateFormat dateFormat =
                new SimpleDateFormat( this.getDateOfExpirationPattern() );

            this.dateOfExpiration =
                dateFormat.parse( this.getDateOfExpirationText() );

        }
        catch ( ParseException e )
        {
            throw new PropertyException( "dateOfExpirationText",
                this.getDateOfExpirationText(), e );

        }
    }

    /**
     * Throws a {@code BankleitzahlExpirationException} if {@code bankCode}
     * is outdated and if a valid replacement record exists in the directory.
     *
     * @param bankCode the Bankleitzahl to check for expiration.
     *
     * @throws NullPointerException if {@code bankCode} is {@code null}.
     */
    private void checkReplacement( final Bankleitzahl bankCode )
        throws BankleitzahlExpirationException
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

            for ( Iterator it = l.iterator(); it.hasNext(); )
            {
                current = (BankleitzahlInfo) it.next();
                if ( current.getReplacingBankCode() != null )
                {
                    record = current;
                }
            }

            // Only throw an exception for records specifying a replacing
            // Bankleitzahl which is not outdated.
            if ( record != null )
            {
                final BankleitzahlInfo[] replacement =
                    this.findByBankCode( record.getReplacingBankCode(),
                    false );

                assert replacement.length == 0 || replacement.length == 1 :
                    "Multiple head offices for " +
                    record.getReplacingBankCode().
                    format( Bankleitzahl.LETTER_FORMAT ) + ".";

                if ( replacement.length == 1 )
                {
                    throw new BankleitzahlExpirationException(
                        record, replacement[0] );

                }
            }
        }
    }

    /**
     * Gets the bankfile resources provided by any available
     * {@code BankfileProvider} implementation.
     *
     * @return bankfile resources provided by any available
     * {@code BankfileProvider} implementation.
     *
     * @throws IOException if getting the resIllegalStateExceptionources fails.
     *
     * @see BankfileProvider
     */
    private URL[] getFileResources() throws IOException
    {
        final List resources = new LinkedList();
        final BankfileProvider[] provider = this.getBankfileProvider();

        for ( int i = provider.length - 1; i >= 0; i-- )
        {
            resources.addAll( Arrays.asList( provider[i].getResources() ) );
        }

        return (URL[]) resources.toArray( new URL[ resources.size() ] );
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
        final Bankleitzahl bankCode, final boolean branchOffices )
    {
        final BankleitzahlInfo[] records = this.bankFile == null
            ? new BankleitzahlInfo[ 0 ]
            : this.bankFile.getRecords();

        final Collection col = new ArrayList( records.length );

        for ( int i = records.length - 1; i >= 0; i-- )
        {
            if ( records[i].getBankCode().equals( bankCode ) &&
                ( records[i].isHeadOffice() != branchOffices ) &&
                !col.add( records[i] ) )
            {
                throw new IllegalStateException(
                    this.getDuplicateRecordMessage(
                    records[i].getSerialNumber(),
                    bankCode.format( Bankleitzahl.LETTER_FORMAT ) ) );

            }
        }

        return (BankleitzahlInfo[]) col.toArray(
            new BankleitzahlInfo[ col.size() ] );

    }

    //-------------------------------------BundesbankBankleitzahlenVerzeichnis--
    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>outdatedInfo</code>.
     * <blockquote><pre>Bankleitzahl {0} ist veraltet.</pre></blockquote>
     * <blockquote><pre>Bankleitzahl {0} is outdated.</pre></blockquote>
     *
     * @param bankleitzahl format argument.
     *
     * @return the text of message <code>outdatedInfo</code>.
     */
    private String getOutdatedInfoMessage(
            java.lang.String bankleitzahl )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "outdatedInfo",
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
     * @param serialNumber format argument.
     * @param bankleitzahl format argument.
     *
     * @return the text of message <code>duplicateRecord</code>.
     */
    private String getDuplicateRecordMessage(
            java.lang.Number serialNumber,
            java.lang.String bankleitzahl )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "duplicateRecord",
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
     * @param entityCount format argument.
     * @param bankfileCount format argument.
     *
     * @return the text of message <code>bankfileInfo</code>.
     */
    private String getBankfileInfoMessage(
            java.lang.Number entityCount,
            java.lang.Number bankfileCount )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "bankfileInfo",
                new Object[]
                {
                    entityCount,
                    bankfileCount
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

/*
 *  jDTAUS Banking Utilities
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
package org.jdtaus.banking.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.BankleitzahlInfo;
import org.jdtaus.banking.messages.UpdatesBankleitzahlenDateiMessage;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.PropertyException;
import org.jdtaus.core.logging.spi.Logger;
import org.jdtaus.core.monitor.spi.Task;
import org.jdtaus.core.monitor.spi.TaskMonitor;

/**
 * German Bankleitzahlendatei for the format as of 2006-06-01.
 * <p>For further information see the
 * <a href="../../../../doc-files/merkblatt_bankleitzahlendatei.pdf">Merkblatt Bankleitzahlendatei</a>.
 * An updated version of the document may be found at
 * <a href="http://www.bundesbank.de">Deutsche Bundesbank</a>.
 * </p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public final class BankleitzahlenDatei
{
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
     * Gets the value of property <code>defaultEncoding</code>.
     *
     * @return Default encoding to use when reading bankfile resources.
     */
    private java.lang.String getDefaultEncoding()
    {
        return (java.lang.String) ContainerFactory.getContainer().
            getProperty( this, "defaultEncoding" );

    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
    //--BankleitzahlenDatei-----------------------------------------------------

    /**
     * Empty {@code BankleitzahlInfo} array.
     * @since 1.15
     */
    private static final BankleitzahlInfo[] NO_RECORDS =
    {
    };

    /**
     * Constant for the format as of june 2006.
     * @since 1.15
     */
    public static final int JUNE_2006_FORMAT = 20060601;

    /**
     * Constant for the format as of june 2013.
     * @since 1.15
     */
    public static final int JUNE_2013_FORMAT = 20130601;

    /**
     * index = index of the field in the record line; value = offset the field's value starts in the record line.
     */
    private static final int[] FIELD_TO_OFFSET =
    {
        0, 8, 9, 67, 72, 107, 134, 139, 150, 152, 158, 159, 160, 168, 172
    };

    /**
     * index = index of the field in the record line; value = length of the field's value in the record line.
     */
    private static final int[] FIELD_TO_LENGTH =
    {
        8, 1, 58, 5, 35, 27, 5, 11, 2, 6, 1, 1, 8, 4, 2
    };

    /**
     * index = index of the field in the record line; value = end offset in the record line exclusive.
     */
    private static final int[] FIELD_TO_ENDOFFSET =
    {
        FIELD_TO_OFFSET[0] + FIELD_TO_LENGTH[0],
        FIELD_TO_OFFSET[1] + FIELD_TO_LENGTH[1],
        FIELD_TO_OFFSET[2] + FIELD_TO_LENGTH[2],
        FIELD_TO_OFFSET[3] + FIELD_TO_LENGTH[3],
        FIELD_TO_OFFSET[4] + FIELD_TO_LENGTH[4],
        FIELD_TO_OFFSET[5] + FIELD_TO_LENGTH[5],
        FIELD_TO_OFFSET[6] + FIELD_TO_LENGTH[6],
        FIELD_TO_OFFSET[7] + FIELD_TO_LENGTH[7],
        FIELD_TO_OFFSET[8] + FIELD_TO_LENGTH[8],
        FIELD_TO_OFFSET[9] + FIELD_TO_LENGTH[9],
        FIELD_TO_OFFSET[10] + FIELD_TO_LENGTH[10],
        FIELD_TO_OFFSET[11] + FIELD_TO_LENGTH[11],
        FIELD_TO_OFFSET[12] + FIELD_TO_LENGTH[12],
        FIELD_TO_OFFSET[13] + FIELD_TO_LENGTH[13],
        FIELD_TO_OFFSET[14] + FIELD_TO_LENGTH[14]
    };

    /** Records held by the instance. */
    private Map records = new HashMap( 5000 );
    private Map deletedRecords = new HashMap( 5000 );
    private Map headOffices = new HashMap( 5000 );
    private Map branchOffices = new HashMap( 5000 );
    private Map deletedHeadOffices = new HashMap( 5000 );
    private Map deletedBranchOffices = new HashMap( 5000 );
    private BankleitzahlInfo[] cachedRecords;
    private BankleitzahlInfo[] cachedDeletedRecords;

    /** Encoding to use when reading bankfile resources. */
    private String encoding;

    /**
     * Format of the file backing the instance.
     * @since 1.15
     */
    private int format;

    /**
     * The date of validity of the file.
     * @since 1.15
     */
    private Date dateOfValidity;

    /**
     * The date of expiration of the file.
     * @since 1.15
     */
    private Date dateOfExpiration;

    /**
     * Reads a Bankleitzahlendatei form an URL initializing the instance to hold its data.
     * <p>Calling this constructor is the same as calling<blockquote><pre>
     * new BankleitzahlenDatei( url, JUNE_2006_FORMAT );
     * </pre></blockquote></p>
     *
     * @param resource An URL to a Bankleitzahlendatei.
     *
     * @throws NullPointerException if {@code resource} is {@code null}.
     * @throws PropertyException for invalid property values.
     * @throws IllegalArgumentException if {@code resource} does not provide a valid Bankleitzahlendatei.
     * @throws IOException if reading fails.
     *
     * @deprecated As of 1.15, replaced by constructor
     * {@link BankleitzahlenDatei#BankleitzahlenDatei(java.net.URL, int, java.util.Date, java.util.Date)}.
     */
    public BankleitzahlenDatei( final URL resource ) throws IOException
    {
        super();

        if ( resource == null )
        {
            throw new NullPointerException( "resource" );
        }

        this.assertValidProperties();
        this.format = JUNE_2006_FORMAT;
        this.dateOfValidity = null;
        this.dateOfExpiration = null;
        this.readBankfile( resource );
    }

    /**
     * Reads a Bankleitzahlendatei form an URL initializing the instance to hold its data taking a format constant.
     *
     * @param resource An URL to a Bankleitzahlendatei.
     * @param format The format of the file to parse.
     * @param dateOfValidity The date of validity of the file.
     * @param dateOfExpiration The date of expiration of the file.
     *
     * @throws NullPointerException if {@code resource}, {@code dateOfValidity} or {@code dateOfExpiration} is
     * {@code null}.
     * @throws PropertyException for invalid property values.
     * @throws IllegalArgumentException if {@code resource} does not provide a valid Bankleitzahlendatei or if
     * {@code format} does not equal one of the format constants defined in this class.
     * @throws IOException if reading fails.
     *
     * @see #JUNE_2006_FORMAT
     * @see #JUNE_2013_FORMAT
     */
    public BankleitzahlenDatei( final URL resource, final int format, final Date dateOfValidity,
                                final Date dateOfExpiration ) throws IOException
    {
        super();

        if ( resource == null )
        {
            throw new NullPointerException( "resource" );
        }
        if ( dateOfValidity == null )
        {
            throw new NullPointerException( "dateOfValidity" );
        }
        if ( dateOfExpiration == null )
        {
            throw new NullPointerException( "dateOfExpiration" );
        }

        assertValidFormat( format );
        this.assertValidProperties();
        this.format = format;
        this.dateOfValidity = (Date) dateOfValidity.clone();
        this.dateOfExpiration = (Date) dateOfExpiration.clone();
        this.readBankfile( resource );
    }

    /**
     * Reads a Bankleitzahlendatei form an URL initializing the instance to hold its data taking the encoding to use
     * when reading the file.
     * <p>Calling this constructor is the same as calling<blockquote><pre>
     * new BankleitzahlenDatei( url, encoding, JUNE_2006_FORMAT );
     * </pre></blockquote></p>
     *
     * @param resource An URL to a Bankleitzahlendatei.
     * @param encoding The encoding to use when reading {@code resource}.
     *
     * @throws NullPointerException if either {@code resource} or {@code encoding} is {@code null}.
     * @throws PropertyException for invalid property values.
     * @throws IllegalArgumentException if {@code resource} does not provide a valid Bankleitzahlendatei.
     * @throws IOException if reading fails.
     *
     * @deprecated As of 1.15, replaced by constructor
     * {@link BankleitzahlenDatei#BankleitzahlenDatei(java.net.URL, java.lang.String, int, java.util.Date, java.util.Date)}.
     */
    public BankleitzahlenDatei( final URL resource, final String encoding ) throws IOException
    {
        super();

        if ( resource == null )
        {
            throw new NullPointerException( "resource" );
        }
        if ( encoding == null )
        {
            throw new NullPointerException( "encoding" );
        }

        this.assertValidProperties();
        this.format = JUNE_2006_FORMAT;
        this.encoding = encoding;
        this.dateOfValidity = null;
        this.dateOfExpiration = null;
        this.readBankfile( resource );
    }

    /**
     * Reads a Bankleitzahlendatei form an URL initializing the instance to hold its data taking the encoding of the
     * file and a format constant.
     *
     * @param resource An URL to a Bankleitzahlendatei.
     * @param encoding The encoding to use when reading {@code resource}.
     * @param format The format of the file to parse.
     * @param dateOfValidity The date of validity of the file.
     * @param dateOfExpiration The date of expiration of the file.
     *
     * @throws NullPointerException if {@code resource}, {@code encoding}, {@code dateOfValidity} or
     * {@code dateOfExpiration} is {@code null}.
     * @throws PropertyException for invalid property values.
     * @throws IllegalArgumentException if {@code resource} does not provide a valid Bankleitzahlendatei or if
     * {@code format} does not equal one of the format constants defined in this class.
     * @throws IOException if reading fails.
     *
     * @see #JUNE_2006_FORMAT
     * @see #JUNE_2013_FORMAT
     */
    public BankleitzahlenDatei( final URL resource, final String encoding, final int format,
                                final Date dateOfValidity, final Date dateOfExpiration ) throws IOException
    {
        super();

        if ( resource == null )
        {
            throw new NullPointerException( "resource" );
        }
        if ( encoding == null )
        {
            throw new NullPointerException( "encoding" );
        }
        if ( dateOfValidity == null )
        {
            throw new NullPointerException( "dateOfValidity" );
        }
        if ( dateOfExpiration == null )
        {
            throw new NullPointerException( "dateOfExpiration" );
        }

        assertValidFormat( format );
        this.assertValidProperties();
        this.encoding = encoding;
        this.format = format;
        this.dateOfValidity = (Date) dateOfValidity.clone();
        this.dateOfExpiration = (Date) dateOfExpiration.clone();
        this.readBankfile( resource );
    }

    /**
     * Gets the encoding used for reading bankfile resources.
     *
     * @return The encoding used for reading bankfile resources.
     */
    public String getEncoding()
    {
        if ( this.encoding == null )
        {
            this.encoding = this.getDefaultEncoding();
        }

        return this.encoding;
    }

    /**
     * Gets the format of the bankfile backing the instance.
     *
     * @return The format of the bankfile backing the instance.
     *
     * @since 1.15
     */
    public int getFormat()
    {
        return this.format;
    }

    /**
     * Gets the date of validity of the file.
     *
     * @return The date of validity of the file or {@code null}, if the instance got created by using one of the
     * deprecated constructors.
     *
     * @since 1.15
     */
    public Date getDateOfValidity()
    {
        return (Date) ( this.dateOfValidity != null ? this.dateOfValidity.clone() : null );
    }

    /**
     * Gets the date of expiration of the file.
     *
     * @return The date of expiration of the file or {@code null}, if the instance got created by using one of the
     * deprecated constructors.
     *
     * @since 1.15
     */
    public Date getDateOfExpiration()
    {
        return (Date) ( this.dateOfExpiration != null ? this.dateOfExpiration.clone() : null );
    }

    /**
     * Gets all records held by the instance.
     *
     * @return All records held by the instance.
     */
    public BankleitzahlInfo[] getRecords()
    {
        if ( this.cachedRecords == null )
        {
            this.cachedRecords = (BankleitzahlInfo[]) this.records.values().
                toArray( new BankleitzahlInfo[ this.records.size() ] );

        }

        return this.cachedRecords;
    }

    /**
     * Gets all records deleted during updating.
     *
     * @return All records deleted during updating.
     *
     * @see #update(org.jdtaus.banking.util.BankleitzahlenDatei)
     *
     * @see #update(org.jdtaus.banking.util.BankleitzahlenDatei)
     * @since 1.15
     */
    public BankleitzahlInfo[] getDeletedRecords()
    {
        if ( this.cachedDeletedRecords == null )
        {
            this.cachedDeletedRecords = (BankleitzahlInfo[]) this.deletedRecords.values().
                toArray( new BankleitzahlInfo[ this.deletedRecords.size() ] );

        }

        return this.cachedDeletedRecords;
    }

    /**
     * Gets a record identified by a serial number.
     *
     * @param serialNumber The serial number of the record to return.
     *
     * @return The record with serial number {@code serialNumber} or {@code null}, if no record matching
     * {@code serialNumber} exists in the file.
     *
     * @throws NullPointerException if {@code serialNumber} is {@code null}.
     */
    public BankleitzahlInfo getRecord( final Integer serialNumber )
    {
        if ( serialNumber == null )
        {
            throw new NullPointerException( "serialNumber" );
        }

        return (BankleitzahlInfo) this.records.get( serialNumber );
    }

    /**
     * Gets a deleted record identified by a serial number.
     *
     * @param serialNumber The serial number of the deleted record to return.
     *
     * @return The deleted record with serial number {@code serialNumber} or {@code null}, if no such record is found.
     *
     * @throws NullPointerException if {@code serialNumber} is {@code null}.
     *
     * @see #getDeletedRecords()
     * @see #update(org.jdtaus.banking.util.BankleitzahlenDatei)
     * @since 1.15
     */
    public BankleitzahlInfo getDeletedRecord( final Integer serialNumber )
    {
        if ( serialNumber == null )
        {
            throw new NullPointerException( "serialNumber" );
        }

        return (BankleitzahlInfo) this.deletedRecords.get( serialNumber );
    }

    /**
     * Gets a head office record for a given bank code.
     *
     * @param bankCode The bank code of the head office record to return.
     *
     * @return The head office record of the bank identified by {@code bankCode} or {@code null}, if no such record is
     * found.
     *
     * @throws NullPointerException if {@code bankCode} is {@code null}.
     *
     * @see #getRecords()
     * @see BankleitzahlInfo#isHeadOffice()
     * @since 1.15
     */
    public BankleitzahlInfo getHeadOfficeRecord( final Bankleitzahl bankCode )
    {
        if ( bankCode == null )
        {
            throw new NullPointerException( "bankCode" );
        }

        return (BankleitzahlInfo) this.headOffices.get( bankCode );
    }

    /**
     * Gets a deleted head office record for a given bank code.
     *
     * @param bankCode The bank code of the deleted head office record to return.
     *
     * @return The deleted head office record of the bank identified by {@code bankCode} or {@code null}, if no such
     * record is found.
     *
     * @throws NullPointerException if {@code bankCode} is {@code null}.
     *
     * @see #getDeletedRecords()
     * @see BankleitzahlInfo#isHeadOffice()
     * @see #update(org.jdtaus.banking.util.BankleitzahlenDatei)
     * @since 1.15
     */
    public BankleitzahlInfo getDeletedHeadOfficeRecord( final Bankleitzahl bankCode )
    {
        if ( bankCode == null )
        {
            throw new NullPointerException( "bankCode" );
        }

        return (BankleitzahlInfo) this.deletedHeadOffices.get( bankCode );
    }

    /**
     * Gets branch office records for a given bank code.
     *
     * @param bankCode The bank code of the branch office records to return.
     *
     * @return The branch office records of the bank identified by {@code bankCode}.
     *
     * @throws NullPointerException if {@code bankCode} is {@code null}.
     *
     * @see #getRecords()
     * @see BankleitzahlInfo#isHeadOffice()
     * @since 1.15
     */
    public BankleitzahlInfo[] getBranchOfficeRecords( final Bankleitzahl bankCode )
    {
        if ( bankCode == null )
        {
            throw new NullPointerException( "bankCode" );
        }

        final List records = (List) this.branchOffices.get( bankCode );
        return records != null
               ? (BankleitzahlInfo[]) records.toArray( new BankleitzahlInfo[ records.size() ] )
               : NO_RECORDS;

    }

    /**
     * Gets deleted branch office records for a given bank code.
     *
     * @param bankCode The bank code of the deleted branch office records to return.
     *
     * @return The deleted branch office records of the bank identified by {@code bankCode}.
     *
     * @throws NullPointerException if {@code bankCode} is {@code null}.
     *
     * @see #getDeletedRecords()
     * @see BankleitzahlInfo#isHeadOffice()
     * @since 1.15
     */
    public BankleitzahlInfo[] getDeletedBranchOfficeRecords( final Bankleitzahl bankCode )
    {
        if ( bankCode == null )
        {
            throw new NullPointerException( "bankCode" );
        }

        final List records = (List) this.deletedBranchOffices.get( bankCode );
        return records != null
               ? (BankleitzahlInfo[]) records.toArray( new BankleitzahlInfo[ records.size() ] )
               : NO_RECORDS;

    }

    /**
     * Given a newer version of the Bankleitzahlendatei updates the records of the instance to reflect the changes.
     *
     * @param file A newer version of the Bankleitzahlendatei to use for updating the records of this instance.
     *
     * @throws NullPointerException if {@code file} is {@code null}.
     * @throws IllegalArgumentException if {@code file} cannot be used for updating this instance.
     */
    public void update( final BankleitzahlenDatei file )
    {
        if ( file == null )
        {
            throw new NullPointerException( "file" );
        }
        if ( file.getFormat() < this.getFormat() )
        {
            throw new IllegalArgumentException( this.getCannotUpdateIncomptibleFileMessage(
                this.getLocale(), toFormatName( this.getFormat() ), toFormatName( file.getFormat() ) ) );

        }

        final boolean log = this.getLogger().isDebugEnabled();
        final boolean upgrade = this.getFormat() < file.getFormat();

        int progress = 0;
        Task task = new Task();
        task.setIndeterminate( false );
        task.setCancelable( false );
        task.setDescription( new UpdatesBankleitzahlenDateiMessage() );
        task.setMinimum( 0 );
        task.setMaximum( file.getRecords().length );
        task.setProgress( progress );

        try
        {
            this.getTaskMonitor().monitor( task );

            for ( int i = file.getRecords().length - 1; i >= 0; i-- )
            {
                task.setProgress( progress++ );
                final BankleitzahlInfo newVersion = file.getRecords()[i];

                if ( 'A' == newVersion.getChangeLabel() )
                {
                    final BankleitzahlInfo oldVersion =
                        (BankleitzahlInfo) this.records.get( newVersion.getSerialNumber() );

                    if ( oldVersion != null && oldVersion.getChangeLabel() != 'D' )
                    {
                        this.resetRecords();
                        throw new IllegalArgumentException( this.getCannotAddDuplicateRecordMessage(
                            this.getLocale(), newVersion.getSerialNumber() ) );

                    }

                    this.records.put( newVersion.getSerialNumber(), newVersion );

                    if ( log )
                    {
                        this.getLogger().debug( this.getAddRecordInfoMessage(
                            this.getLocale(), String.valueOf( newVersion.getChangeLabel() ),
                            newVersion.getSerialNumber() ) );

                    }
                }
                else if ( 'M' == newVersion.getChangeLabel() || 'D' == newVersion.getChangeLabel() )
                {
                    if ( this.records.put( newVersion.getSerialNumber(), newVersion ) == null )
                    {
                        this.resetRecords();
                        throw new IllegalArgumentException( this.getCannotModifyNonexistentRecordMessage(
                            this.getLocale(), newVersion.getSerialNumber() ) );

                    }

                    if ( log )
                    {
                        this.getLogger().debug( this.getModifyRecordInfoMessage(
                            this.getLocale(), String.valueOf( newVersion.getChangeLabel() ),
                            newVersion.getSerialNumber() ) );

                    }
                }
                else if ( 'U' == newVersion.getChangeLabel() )
                {
                    if ( ( upgrade && this.records.put( newVersion.getSerialNumber(), newVersion ) == null )
                         || !this.records.containsKey( newVersion.getSerialNumber() ) )
                    {
                        this.resetRecords();
                        throw new IllegalArgumentException( this.getCannotModifyNonexistentRecordMessage(
                            this.getLocale(), newVersion.getSerialNumber() ) );

                    }
                }
            }

            if ( upgrade )
            {
                if ( this.getLogger().isInfoEnabled() )
                {
                    this.getLogger().info( this.getBankcodeFileUpgradeInfoMessage(
                        this.getLocale(), toFormatName( this.format ), toFormatName( file.getFormat() ) ) );

                }

                this.format = file.getFormat();
            }

            this.dateOfValidity = file.getDateOfValidity();
            this.dateOfExpiration = file.getDateOfExpiration();
        }
        finally
        {
            this.getTaskMonitor().finish( task );
        }

        progress = 0;
        task = new Task();
        task.setIndeterminate( false );
        task.setCancelable( false );
        task.setDescription( new UpdatesBankleitzahlenDateiMessage() );
        task.setMinimum( 0 );
        task.setMaximum( this.records.size() );
        task.setProgress( progress );

        try
        {
            this.getTaskMonitor().monitor( task );

            for ( final Iterator it = this.records.values().iterator(); it.hasNext(); )
            {
                task.setProgress( progress++ );
                final BankleitzahlInfo oldVersion = (BankleitzahlInfo) it.next();

                if ( 'D' == oldVersion.getChangeLabel() )
                {
                    final BankleitzahlInfo newVersion = file.getRecord( oldVersion.getSerialNumber() );

                    if ( newVersion == null )
                    {
                        if ( this.deletedRecords.put( oldVersion.getSerialNumber(), oldVersion ) != null )
                        {
                            this.resetRecords();
                            throw new IllegalStateException( this.getCannotRemoveDuplicateRecordMessage(
                                this.getLocale(), oldVersion.getSerialNumber() ) );

                        }

                        it.remove();

                        if ( log )
                        {
                            this.getLogger().debug( this.getRemoveRecordInfoMessage(
                                this.getLocale(), String.valueOf( oldVersion.getChangeLabel() ),
                                oldVersion.getSerialNumber() ) );

                        }
                    }
                }
            }
        }
        finally
        {
            this.getTaskMonitor().finish( task );
        }

        this.updateRecords();
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException for invalid property values.
     */
    private void assertValidProperties()
    {
        if ( this.getEncoding() == null || this.getEncoding().length() == 0 )
        {
            throw new PropertyException( "encoding", this.getEncoding() );
        }

        try
        {
            "".getBytes( this.getEncoding() );
        }
        catch ( final UnsupportedEncodingException e )
        {
            throw new PropertyException( "encoding", this.getEncoding(), e );
        }
    }

    /**
     * Checks a given integer to equal one of the format constants defined in this class.
     *
     * @param value The value to check.
     *
     * @throws IllegalArgumentException if {@code value} does not equal one of the format constants defined in this
     * class.
     */
    private static void assertValidFormat( final int value )
    {
        if ( value != JUNE_2006_FORMAT && value != JUNE_2013_FORMAT )
        {
            throw new IllegalArgumentException( Integer.toString( value ) );
        }
    }

    /**
     * Reads a Bankleitzahlendatei from an URL initializing the instance to hold its data.
     *
     * @param resource An URL to a Bankleitzahlendatei.
     *
     * @throws NullPointerException if {@code resource} is {@code null}.
     * @throws IllegalArgumentException if {@code resource} does not provide a valid Bankleitzahlendatei.
     * @throws IOException if reading fails.
     */
    private void readBankfile( final URL resource ) throws IOException
    {
        if ( resource == null )
        {
            throw new NullPointerException( "resource" );
        }

        this.records.clear();

        if ( this.getLogger().isDebugEnabled() )
        {
            this.getLogger().debug( this.getFileNameInfoMessage( this.getLocale(), resource.toExternalForm() ) );
        }

        LineNumberReader reader = null;
        final NumberFormat plzFmt = new DecimalFormat( "00000" );
        final NumberFormat serFmt = new DecimalFormat( "000000" );
        final NumberFormat blzFmt = new DecimalFormat( "00000000" );

        try
        {
            reader = new LineNumberReader( new InputStreamReader( resource.openStream(), this.getEncoding() ) );
            boolean emptyLine = false;

            for ( String line = reader.readLine(); line != null; line = reader.readLine() )
            {
                if ( line.trim().length() == 0 )
                {
                    emptyLine = true;
                    continue;
                }

                if ( emptyLine )
                {
                    throw new IllegalArgumentException( this.getUnexpectedDataMessage(
                        this.getLocale(), new Integer( reader.getLineNumber() ), resource.toExternalForm() ) );

                }

                final BankleitzahlInfo r = new BankleitzahlInfo();

                // Field 1
                r.setBankCode( Bankleitzahl.parse( field( line, FIELD_TO_OFFSET[0], FIELD_TO_ENDOFFSET[0] ) ) );
                // Field 2
                r.setHeadOffice( "1".equals( field( line, FIELD_TO_OFFSET[1], FIELD_TO_ENDOFFSET[1] ) ) );
                // Field 3
                r.setName( field( line, FIELD_TO_OFFSET[2], FIELD_TO_ENDOFFSET[2] ) );
                // Field 4
                r.setPostalCode( plzFmt.parse( field( line, FIELD_TO_OFFSET[3], FIELD_TO_ENDOFFSET[3] ) ).intValue() );
                // Field 5
                r.setCity( field( line, FIELD_TO_OFFSET[4], FIELD_TO_ENDOFFSET[4] ) );
                // Field 6
                r.setDescription( field( line, FIELD_TO_OFFSET[5], FIELD_TO_ENDOFFSET[5] ) );
                // Field 7
                String field = field( line, FIELD_TO_OFFSET[6], FIELD_TO_ENDOFFSET[6] );
                r.setPanInstituteNumber( field.length() > 0 ? plzFmt.parse( field ).intValue() : 0 );
                // Field 8
                r.setBic( field( line, FIELD_TO_OFFSET[7], FIELD_TO_ENDOFFSET[7] ) );
                // Field 9
                r.setValidationLabel( field( line, FIELD_TO_OFFSET[8], FIELD_TO_ENDOFFSET[8] ) );
                // Field 10
                field = field( line, FIELD_TO_OFFSET[9], FIELD_TO_ENDOFFSET[9] );
                r.setSerialNumber( new Integer( serFmt.parse( field ).intValue() ) );
                // Field 11
                r.setChangeLabel( field( line, FIELD_TO_OFFSET[10], FIELD_TO_ENDOFFSET[10] ).toCharArray()[0] );
                // Field 12
                r.setMarkedForDeletion( "1".equals( field( line, FIELD_TO_OFFSET[11], FIELD_TO_ENDOFFSET[11] ) ) );
                // Field 13
                Number blz = blzFmt.parse( field( line, FIELD_TO_OFFSET[12], FIELD_TO_ENDOFFSET[12] ) );
                if ( blz.intValue() != 0 )
                {
                    r.setReplacingBankCode( Bankleitzahl.valueOf( blz ) );
                }
                else
                {
                    r.setReplacingBankCode( null );
                }

                if ( this.getFormat() >= JUNE_2013_FORMAT )
                {
                    // Field 14
                    r.setIbanRuleLabel( Integer.valueOf( field( line, FIELD_TO_OFFSET[13],
                                                                FIELD_TO_ENDOFFSET[13] ) ) );

                    r.setIbanRuleVersion( Integer.valueOf( field( line, FIELD_TO_OFFSET[14],
                                                                  FIELD_TO_ENDOFFSET[14] ) ) );

                }

                switch ( r.getChangeLabel() )
                {
                    case 'A':
                        r.setCreationDate( this.getDateOfValidity() );
                        break;
                    case 'M':
                        r.setModificationDate( this.getDateOfValidity() );
                        break;
                    case 'D':
                        r.setDeletionDate( this.getDateOfExpiration() );
                        break;
                    case 'U':
                        // ignored
                        break;
                    default:
                        throw new AssertionError( r.getChangeLabel() );
                }

                if ( this.records.put( r.getSerialNumber(), r ) != null )
                {
                    this.resetRecords();
                    throw new IllegalArgumentException( this.getCannotAddDuplicateRecordMessage(
                        this.getLocale(), r.getSerialNumber() ) );

                }
            }
        }
        catch ( final ParseException e )
        {
            this.resetRecords();
            throw (IllegalArgumentException) new IllegalArgumentException( resource.toExternalForm() ).initCause( e );
        }
        catch ( final IndexOutOfBoundsException e )
        {
            this.resetRecords();
            throw (IllegalArgumentException) new IllegalArgumentException( resource.toExternalForm() ).initCause( e );
        }
        catch ( final IOException e )
        {
            this.resetRecords();
            throw e;
        }
        finally
        {
            this.cachedRecords = null;
            this.cachedDeletedRecords = null;

            if ( reader != null )
            {
                reader.close();
            }
        }
    }

    private void resetRecords()
    {
        this.records.clear();
        this.deletedRecords.clear();
        this.updateRecords();
    }

    private void updateRecords()
    {
        this.headOffices.clear();
        this.deletedHeadOffices.clear();
        this.branchOffices.clear();
        this.deletedBranchOffices.clear();
        this.cachedRecords = null;
        this.cachedDeletedRecords = null;

        for ( int i = 0, l0 = this.getRecords().length; i < l0; i++ )
        {
            final BankleitzahlInfo record = this.getRecords()[i];

            if ( record.isHeadOffice() )
            {
                if ( this.headOffices.put( record.getBankCode(), record ) != null )
                {
                    this.resetRecords();
                    throw new IllegalStateException( this.getCannotAddDuplicateHeadOfficeRecordMessage(
                        this.getLocale(), record.getBankCode() ) );

                }
            }
            else
            {
                List list = (List) this.branchOffices.get( record.getBankCode() );

                if ( list == null )
                {
                    list = new ArrayList();
                    this.branchOffices.put( record.getBankCode(), list );
                }

                list.add( record );
            }
        }

        for ( int i = 0, l0 = this.getDeletedRecords().length; i < l0; i++ )
        {
            final BankleitzahlInfo record = this.getDeletedRecords()[i];

            if ( record.isHeadOffice() )
            {
                if ( this.deletedHeadOffices.put( record.getBankCode(), record ) != null )
                {
                    this.resetRecords();
                    throw new IllegalStateException( this.getCannotAddDuplicateHeadOfficeRecordMessage(
                        this.getLocale(), record.getBankCode() ) );

                }
            }
            else
            {
                List list = (List) this.deletedBranchOffices.get( record.getBankCode() );

                if ( list == null )
                {
                    list = new ArrayList();
                    this.deletedBranchOffices.put( record.getBankCode(), list );
                }

                list.add( record );
            }
        }
    }

    private static String field( final String line, final int startOffset, final int endOffset )
    {
        return line.substring( startOffset, endOffset ).trim();
    }

    private static String toFormatName( final long format )
    {
        String name = "";

        if ( format == JUNE_2006_FORMAT )
        {
            name = "JUNE2006";
        }
        else if ( format == JUNE_2013_FORMAT )
        {
            name = "JUNE2013";
        }

        return name;
    }

    //-----------------------------------------------------BankleitzahlenDatei--
    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>fileNameInfo</code>.
     * <blockquote><pre>Lädt Bankleitzahlendatei "{0}".</pre></blockquote>
     * <blockquote><pre>Loading Bankleitzahlendatei "{0}".</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param fileName format parameter.
     *
     * @return the text of message <code>fileNameInfo</code>.
     */
    private String getFileNameInfoMessage( final Locale locale,
            final java.lang.String fileName )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "fileNameInfo", locale,
                new Object[]
                {
                    fileName
                });

    }

    /**
     * Gets the text of message <code>addRecordInfo</code>.
     * <blockquote><pre>{0}: Datensatz {1, number} hinzugefügt.</pre></blockquote>
     * <blockquote><pre>{0}: Added record {1, number}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param label format parameter.
     * @param serialNumber format parameter.
     *
     * @return the text of message <code>addRecordInfo</code>.
     */
    private String getAddRecordInfoMessage( final Locale locale,
            final java.lang.String label,
            final java.lang.Number serialNumber )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "addRecordInfo", locale,
                new Object[]
                {
                    label,
                    serialNumber
                });

    }

    /**
     * Gets the text of message <code>modifyRecordInfo</code>.
     * <blockquote><pre>{0}: Datensatz {1, number} aktualisiert.</pre></blockquote>
     * <blockquote><pre>{0}: Updated record {1, number}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param label format parameter.
     * @param serialNumber format parameter.
     *
     * @return the text of message <code>modifyRecordInfo</code>.
     */
    private String getModifyRecordInfoMessage( final Locale locale,
            final java.lang.String label,
            final java.lang.Number serialNumber )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "modifyRecordInfo", locale,
                new Object[]
                {
                    label,
                    serialNumber
                });

    }

    /**
     * Gets the text of message <code>removeRecordInfo</code>.
     * <blockquote><pre>{0}: Datensatz {1, number} entfernt.</pre></blockquote>
     * <blockquote><pre>{0}: Removed record {1, number}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param label format parameter.
     * @param serialNumber format parameter.
     *
     * @return the text of message <code>removeRecordInfo</code>.
     */
    private String getRemoveRecordInfoMessage( final Locale locale,
            final java.lang.String label,
            final java.lang.Number serialNumber )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "removeRecordInfo", locale,
                new Object[]
                {
                    label,
                    serialNumber
                });

    }

    /**
     * Gets the text of message <code>cannotAddDuplicateRecord</code>.
     * <blockquote><pre>Datensatz mit Seriennummer {0,number} existiert bereits und kann nicht hinzugefügt werden.</pre></blockquote>
     * <blockquote><pre>Record with serial number {0,number} already exists and cannot be added.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param serialNumber format parameter.
     *
     * @return the text of message <code>cannotAddDuplicateRecord</code>.
     */
    private String getCannotAddDuplicateRecordMessage( final Locale locale,
            final java.lang.Number serialNumber )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "cannotAddDuplicateRecord", locale,
                new Object[]
                {
                    serialNumber
                });

    }

    /**
     * Gets the text of message <code>cannotAddDuplicateHeadOfficeRecord</code>.
     * <blockquote><pre>Datensatz der Hauptstelle {0,number} existiert bereits und kann nicht hinzugefügt werden.</pre></blockquote>
     * <blockquote><pre>Head office record of bank code {0,number} already exists and cannot be added.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param bankCode format parameter.
     *
     * @return the text of message <code>cannotAddDuplicateHeadOfficeRecord</code>.
     */
    private String getCannotAddDuplicateHeadOfficeRecordMessage( final Locale locale,
            final java.lang.Number bankCode )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "cannotAddDuplicateHeadOfficeRecord", locale,
                new Object[]
                {
                    bankCode
                });

    }

    /**
     * Gets the text of message <code>cannotModifyNonexistentRecord</code>.
     * <blockquote><pre>Ein Datensatz mit Seriennummer {0,number} existiert nicht und kann nicht aktualisiert werden.</pre></blockquote>
     * <blockquote><pre>Record with serial number {0,number} does not exist and cannot be updated.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param serialNumber format parameter.
     *
     * @return the text of message <code>cannotModifyNonexistentRecord</code>.
     */
    private String getCannotModifyNonexistentRecordMessage( final Locale locale,
            final java.lang.Number serialNumber )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "cannotModifyNonexistentRecord", locale,
                new Object[]
                {
                    serialNumber
                });

    }

    /**
     * Gets the text of message <code>cannotUpdateIncomptibleFile</code>.
     * <blockquote><pre>''{0}'' Bankleitzahlendatei kann nicht mit ''{1}'' Bankleitzahlendatei aktualisiert werden.</pre></blockquote>
     * <blockquote><pre>''{0}'' bank code file cannot be updated with a ''{1}'' bank code file.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param targetBankCodeFileFormat format parameter.
     * @param sourceBankCodeFileFormat format parameter.
     *
     * @return the text of message <code>cannotUpdateIncomptibleFile</code>.
     */
    private String getCannotUpdateIncomptibleFileMessage( final Locale locale,
            final java.lang.String targetBankCodeFileFormat,
            final java.lang.String sourceBankCodeFileFormat )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "cannotUpdateIncomptibleFile", locale,
                new Object[]
                {
                    targetBankCodeFileFormat,
                    sourceBankCodeFileFormat
                });

    }

    /**
     * Gets the text of message <code>unexpectedData</code>.
     * <blockquote><pre>Unerwartete Daten in Zeile {0,number} bei der Verarbeitung von {1}.</pre></blockquote>
     * <blockquote><pre>Unexpected data at line {0,number} processing {1}.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param lineNumber format parameter.
     * @param resourceName format parameter.
     *
     * @return the text of message <code>unexpectedData</code>.
     */
    private String getUnexpectedDataMessage( final Locale locale,
            final java.lang.Number lineNumber,
            final java.lang.String resourceName )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "unexpectedData", locale,
                new Object[]
                {
                    lineNumber,
                    resourceName
                });

    }

    /**
     * Gets the text of message <code>bankcodeFileUpgradeInfo</code>.
     * <blockquote><pre>''{0}'' Bankleitzahlendatei zu ''{1}'' Bankleitzahlendatei aktualisiert.</pre></blockquote>
     * <blockquote><pre>''{0}'' bank code file upgraded to ''{1}'' bank code file.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param targetBankCodeFileFormat format parameter.
     * @param sourceBankCodeFileFormat format parameter.
     *
     * @return the text of message <code>bankcodeFileUpgradeInfo</code>.
     */
    private String getBankcodeFileUpgradeInfoMessage( final Locale locale,
            final java.lang.String targetBankCodeFileFormat,
            final java.lang.String sourceBankCodeFileFormat )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "bankcodeFileUpgradeInfo", locale,
                new Object[]
                {
                    targetBankCodeFileFormat,
                    sourceBankCodeFileFormat
                });

    }

    /**
     * Gets the text of message <code>cannotRemoveDuplicateRecord</code>.
     * <blockquote><pre>Datensatz mit Seriennummer {0,number} bereits gelöscht.</pre></blockquote>
     * <blockquote><pre>Record with serial number {0,number} already deleted.</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param serialNumber format parameter.
     *
     * @return the text of message <code>cannotRemoveDuplicateRecord</code>.
     */
    private String getCannotRemoveDuplicateRecordMessage( final Locale locale,
            final java.lang.Number serialNumber )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "cannotRemoveDuplicateRecord", locale,
                new Object[]
                {
                    serialNumber
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

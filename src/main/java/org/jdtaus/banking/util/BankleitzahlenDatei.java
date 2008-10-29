/*
 *  jDTAUS Banking Utilities
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
package org.jdtaus.banking.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
 * <a href="../../../../doc-files/Bankleitzahlen%20Richtlinie%20-%20Stand%208.%20Januar%202007.pdf">
 * Bankleitzahlen Richtlinie</a>. An updated version of the document may be
 * found at <a href="http://www.bundesbank.de/index.en.php">Deutsche Bundesbank</a>.
 * </p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class BankleitzahlenDatei
{
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
     * Gets the configured <code>TaskMonitor</code> implementation.
     *
     * @return the configured <code>TaskMonitor</code> implementation.
     */
    private TaskMonitor getTaskMonitor()
    {
        return (TaskMonitor) ContainerFactory.getContainer().
            getDependency( this, "TaskMonitor" );

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

    /** Records held by the instance. */
    private Map records = new HashMap( 5000 );
    private BankleitzahlInfo[] cachedRecords;

    /** Encoding to use when reading bankfile resources. */
    private String encoding;

    /**
     * Reads a Bankleitzahlendatei form an URL initializing the instance to
     * hold its data.
     *
     * @param resource an URL to a Bankleitzahlendatei.
     *
     * @throws NullPointerException if {@code resource} is {@code null}.
     * @throws PropertyException for invalid property values.
     * @throws IllegalArgumentException if {@code resource} does not provide
     * a valid Bankleitzahlendatei.
     * @throws IOException if reading fails.
     */
    public BankleitzahlenDatei( final URL resource ) throws IOException
    {
        super();
        this.assertValidProperties();
        this.readBankfile( resource );
    }

    /**
     * Reads a Bankleitzahlendatei form an URL initializing the instance to
     * hold its data taking the encoding to use when reading the file.
     *
     * @param resource an URL to a Bankleitzahlendatei.
     * @param encoding the encoding to use when reading {@code resource}.
     *
     * @throws NullPointerException if either {@code resource} or
     * {@code encoding} is {@code null}.
     * @throws PropertyException for invalid property values.
     * @throws IllegalArgumentException if {@code resource} does not provide
     * a valid Bankleitzahlendatei.
     * @throws IOException if reading fails.
     */
    public BankleitzahlenDatei( final URL resource, final String encoding )
        throws IOException
    {
        super();
        this.encoding = encoding;
        this.assertValidProperties();
        this.readBankfile( resource );
    }

    /**
     * Gets the encoding used for reading bankfile resources.
     *
     * @return the encoding used for reading bankfile resources.
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
     * Gets all records held by the instance.
     *
     * @return all records held by the instance.
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
     * Gets a record identified by a serial number.
     *
     * @param serialNumber the serial number of the record to return.
     *
     * @return the record with serial number {@code serialNumber} or
     * {@code null} if no record matching {@code serialNumber} exists in the
     * file.
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
     * Given a newer version of the Bankleitzahlendatei updates the records of
     * the instance to reflect the changes.
     *
     * @param file a newer version of the Bankleitzahlendatei to use for
     * updating the records of this instance.
     *
     * @throws NullPointerException if {@code file} is {@code null}.
     * @throws IllegalArgumentException if {@code file} cannot be used for
     * updating this instance.
     */
    public void update( final BankleitzahlenDatei file )
    {
        if ( file == null )
        {
            throw new NullPointerException( "file" );
        }

        int i;
        final boolean log = this.getLogger().isDebugEnabled();
        BankleitzahlInfo oldVersion;
        BankleitzahlInfo newVersion;

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

            for ( i = file.getRecords().length - 1; i >= 0; i-- )
            {
                task.setProgress( progress++ );
                newVersion = file.getRecords()[i];
                if ( 'A' == newVersion.getChangeLabel() )
                {
                    oldVersion = (BankleitzahlInfo) this.records.get(
                        newVersion.getSerialNumber() );

                    if ( oldVersion != null &&
                        oldVersion.getChangeLabel() != 'D' )
                    {
                        throw new IllegalArgumentException(
                            this.getCannotAddDuplicateRecordMessage(
                            newVersion.getSerialNumber() ) );

                    }

                    this.records.put( newVersion.getSerialNumber(), newVersion );

                    if ( log )
                    {
                        this.getLogger().debug(
                            this.getAddRecordInfoMessage(
                            String.valueOf( newVersion.getChangeLabel() ),
                            newVersion.getSerialNumber() ) );

                    }
                }
                else if ( 'M' == newVersion.getChangeLabel() ||
                    'D' == newVersion.getChangeLabel() )
                {
                    if ( this.records.put( newVersion.getSerialNumber(),
                                           newVersion ) == null )
                    {
                        throw new IllegalArgumentException(
                            this.getCannotModifyNonexistentRecordMessage(
                            newVersion.getSerialNumber() ) );

                    }

                    if ( log )
                    {
                        this.getLogger().debug(
                            this.getModifyRecordInfoMessage(
                            String.valueOf( newVersion.getChangeLabel() ),
                            newVersion.getSerialNumber() ) );

                    }

                }
                else if ( 'U' == newVersion.getChangeLabel() &&
                    !this.records.containsKey( newVersion.getSerialNumber() ) )
                {
                    throw new IllegalArgumentException(
                        this.getCannotModifyNonexistentRecordMessage(
                        newVersion.getSerialNumber() ) );

                }
            }
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
            for ( Iterator it = this.records.values().iterator(); it.hasNext();)
            {
                task.setProgress( progress++ );
                oldVersion = (BankleitzahlInfo) it.next();

                if ( 'D' == oldVersion.getChangeLabel() )
                {
                    newVersion = file.getRecord( oldVersion.getSerialNumber() );
                    if ( newVersion == null )
                    {
                        it.remove();

                        if ( log )
                        {
                            this.getLogger().debug(
                                this.getRemoveRecordInfoMessage(
                                String.valueOf( oldVersion.getChangeLabel() ),
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

        this.cachedRecords = null;
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
        catch ( UnsupportedEncodingException e )
        {
            throw new PropertyException( "encoding", this.getEncoding(), e );
        }
    }

    /**
     * Reads a Bankleitzahlendatei form an URL initializing the instance to
     * hold its data.
     *
     * @param resource an URL to a Bankleitzahlendatei.
     *
     * @throws NullPointerException if {@code resource} is {@code null}.
     * @throws IllegalArgumentException if {@code resource} does not provide
     * a valid Bankleitzahlendatei.
     * @throws IOException if reading fails.
     */
    private void readBankfile( final URL resource ) throws IOException
    {
        String line = null;
        InputStream stream = null;
        BankleitzahlInfo rec = null;
        final BufferedReader reader;

        if ( resource == null )
        {
            throw new NullPointerException( "resource" );
        }

        this.records.clear();

        if ( this.getLogger().isDebugEnabled() )
        {
            this.getLogger().debug(
                this.getFileNameInfoMessage( resource.toExternalForm() ) );

        }

        try
        {
            stream = resource.openStream();
            reader = new BufferedReader( new InputStreamReader(
                                         stream, this.getEncoding() ) );

            while ( ( line = reader.readLine() ) != null )
            {
                rec = new BankleitzahlInfo();
                rec.parse( line );

                if ( this.records.put( rec.getSerialNumber(), rec ) != null )
                {
                    throw new IllegalArgumentException(
                        this.getCannotAddDuplicateRecordMessage(
                        rec.getSerialNumber() ) );

                }
            }

            this.cachedRecords = null;
        }
        finally
        {
            if ( stream != null )
            {
                stream.close();
            }
        }
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
     * @param fileName format argument.
     *
     * @return the text of message <code>fileNameInfo</code>.
     */
    private String getFileNameInfoMessage(
            java.lang.String fileName )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "fileNameInfo",
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
     * @param label format argument.
     * @param serialNumber format argument.
     *
     * @return the text of message <code>addRecordInfo</code>.
     */
    private String getAddRecordInfoMessage(
            java.lang.String label,
            java.lang.Number serialNumber )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "addRecordInfo",
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
     * @param label format argument.
     * @param serialNumber format argument.
     *
     * @return the text of message <code>modifyRecordInfo</code>.
     */
    private String getModifyRecordInfoMessage(
            java.lang.String label,
            java.lang.Number serialNumber )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "modifyRecordInfo",
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
     * @param label format argument.
     * @param serialNumber format argument.
     *
     * @return the text of message <code>removeRecordInfo</code>.
     */
    private String getRemoveRecordInfoMessage(
            java.lang.String label,
            java.lang.Number serialNumber )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "removeRecordInfo",
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
     * @param serialNumber format argument.
     *
     * @return the text of message <code>cannotAddDuplicateRecord</code>.
     */
    private String getCannotAddDuplicateRecordMessage(
            java.lang.Number serialNumber )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "cannotAddDuplicateRecord",
                new Object[]
                {
                    serialNumber
                });

    }

    /**
     * Gets the text of message <code>cannotModifyNonexistentRecord</code>.
     * <blockquote><pre>Ein Datensatz mit Seriennummer {0,number} existiert nicht und kann nicht aktualisiert werden.</pre></blockquote>
     * <blockquote><pre>Record with serial number {0,number} does not exist and cannot be updated.</pre></blockquote>
     *
     * @param serialNumber format argument.
     *
     * @return the text of message <code>cannotModifyNonexistentRecord</code>.
     */
    private String getCannotModifyNonexistentRecordMessage(
            java.lang.Number serialNumber )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "cannotModifyNonexistentRecord",
                new Object[]
                {
                    serialNumber
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}
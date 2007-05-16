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
package org.jdtaus.banking.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.jdtaus.banking.BankleitzahlInfo;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.ContextFactory;
import org.jdtaus.core.container.ContextInitializer;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ImplementationException;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.container.Properties;
import org.jdtaus.core.container.Property;
import org.jdtaus.core.container.PropertyException;
import org.jdtaus.core.logging.spi.Logger;
import org.jdtaus.core.monitor.spi.Task;
import org.jdtaus.core.monitor.spi.TaskMonitor;
import org.jdtaus.core.text.Message;

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
    //--Implementation----------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(BankleitzahlenDatei.class.getName());

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

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

        p = meta.getProperty("encoding");
        this._encoding = (java.lang.String) p.getValue();

    }

    //------------------------------------------------------------Constructors--
    //--Dependencies------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /** Configured <code>TaskMonitor</code> implementation. */
    private transient TaskMonitor _dependency1;

    /**
     * Gets the configured <code>TaskMonitor</code> implementation.
     *
     * @return the configured <code>TaskMonitor</code> implementation.
     */
    private TaskMonitor getTaskMonitor()
    {
        TaskMonitor ret = null;
        if(this._dependency1 != null)
        {
            ret = this._dependency1;
        }
        else
        {
            ret = (TaskMonitor) ContainerFactory.getContainer().
                getDependency(BankleitzahlenDatei.class,
                "TaskMonitor");

            if(ModelFactory.getModel().getModules().
                getImplementation(BankleitzahlenDatei.class.getName()).
                getDependencies().getDependency("TaskMonitor").
                isBound())
            {
                this._dependency1 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext()))
        {
            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }
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
                getDependency(BankleitzahlenDatei.class,
                "Logger");

            if(ModelFactory.getModel().getModules().
                getImplementation(BankleitzahlenDatei.class.getName()).
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
     * Property {@code encoding}.
     * @serial
     */
    private java.lang.String _encoding;

    /**
     * Gets the value of property <code>encoding</code>.
     *
     * @return the value of property <code>encoding</code>.
     */
    protected java.lang.String getEncoding()
    {
        return this._encoding;
    }


    //--------------------------------------------------------------Properties--
    //--BankleitzahlenDatei-----------------------------------------------------

    /** {@code Task} description for updating with another instance. */
    private static final class Description extends Message
    {
        public Object[] getFormatArguments(final Locale locale)
        {
            return new Object[0];
        }

        public String getText(final Locale locale)
        {
            return BankleitzahlenDateiBundle.getUpdateTaskText(locale);
        }
    }

    /** Records held by the instance. */
    private Map records = new HashMap(5000);
    private BankleitzahlInfo[] cachedRecords;

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
    public BankleitzahlenDatei(final URL resource) throws IOException
    {
        super();

        this.initializeProperties(BankleitzahlenDatei.META.getProperties());
        this.assertValidProperties();

        final BufferedReader reader;

        String line;
        InputStream stream = null;
        BankleitzahlInfo rec;

        if(resource == null)
        {
            throw new NullPointerException("resource");
        }

        this.records.clear();

        if(this.getLogger().isDebugEnabled())
        {
            this.getLogger().debug(BankleitzahlenDateiBundle.
                getFileNameInfoMessage(Locale.getDefault()).
                format(new Object[] { resource.toExternalForm() }));

        }

        try
        {
            stream = resource.openStream();
            reader = new BufferedReader(new InputStreamReader(
                stream, "ISO-8859-1"));

            while((line = reader.readLine()) != null)
            {
                rec = new BankleitzahlInfo();
                rec.parse(line);

                if(this.records.put(rec.getSerialNumber(), rec) != null)
                {
                    throw new ImplementationException(META,
                        new IllegalArgumentException(rec.toString()));

                }
            }

            this.cachedRecords = null;
        }
        finally
        {
            if(stream != null)
            {
                stream.close();
            }
        }
    }

    /**
     * Gets all records held by the instance.
     *
     * @return all records held by the instance.
     */
    public BankleitzahlInfo[] getRecords()
    {
        if(this.cachedRecords == null)
        {
            this.cachedRecords = (BankleitzahlInfo[]) this.records.values().
                toArray(new BankleitzahlInfo[this.records.size()]);

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
    public BankleitzahlInfo getRecord(final Integer serialNumber)
    {
        if(serialNumber == null)
        {
            throw new NullPointerException("serialNumber");
        }

        return (BankleitzahlInfo) this.records.get(serialNumber);
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
    public void update(final BankleitzahlenDatei file)
    {
        if(file == null)
        {
            throw new NullPointerException("file");
        }

        int i;
        final Iterator it;
        final boolean log = this.getLogger().isDebugEnabled();
        BankleitzahlInfo oldVersion;
        BankleitzahlInfo newVersion;

        int progress = 0;
        Task task = new Task();
        task.setIndeterminate(false);
        task.setCancelable(false);
        task.setDescription(new Description());
        task.setMinimum(0);
        task.setMaximum(file.getRecords().length);
        task.setProgress(progress);

        try
        {
            this.getTaskMonitor().monitor(task);

            for(i = file.getRecords().length - 1; i >= 0; i--)
            {
                task.setProgress(progress++);
                newVersion = file.getRecords()[i];
                if('A' == newVersion.getChangeLabel())
                {
                    if(this.records.put(
                        newVersion.getSerialNumber(), newVersion) != null)
                    {

                        throw new IllegalArgumentException(
                            newVersion.toString());

                    }

                    if(log)
                    {
                        this.getLogger().debug(BankleitzahlenDateiBundle.
                            getAddRecordInfoMessage(Locale.getDefault()).
                            format(new Object[] {
                            new Character(newVersion.getChangeLabel()),
                            newVersion.getSerialNumber() }));

                    }
                }
                else if('M' == newVersion.getChangeLabel() ||
                    'D' == newVersion.getChangeLabel())
                {

                    oldVersion = (BankleitzahlInfo) this.records.
                        get(newVersion.getSerialNumber());

                    if(oldVersion == null)
                    {
                        throw new IllegalArgumentException(
                            newVersion.toString());

                    }

                    this.records.put(newVersion.getSerialNumber(), newVersion);

                    if(log)
                    {
                        this.getLogger().debug(BankleitzahlenDateiBundle.
                            getModifyRecordInfoMessage(Locale.getDefault()).
                            format(new Object[] {
                            new Character(newVersion.getChangeLabel()),
                            newVersion.getSerialNumber() }));

                    }

                }
                else if('U' == newVersion.getChangeLabel() &&
                    !this.records.containsKey(newVersion.getSerialNumber()))
                {
                    throw new IllegalArgumentException(newVersion.toString());
                }
            }
        }
        finally
        {
            this.getTaskMonitor().finish(task);
        }

        progress = 0;
        task = new Task();
        task.setIndeterminate(false);
        task.setCancelable(false);
        task.setDescription(new Description());
        task.setMinimum(0);
        task.setMaximum(this.records.size());
        task.setProgress(progress);

        try
        {
            this.getTaskMonitor().monitor(task);

            for(it = this.records.values().iterator(); it.hasNext();)
            {
                task.setProgress(progress++);
                oldVersion = (BankleitzahlInfo) it.next();

                if('D' == oldVersion.getChangeLabel())
                {
                    newVersion = file.getRecord(oldVersion.getSerialNumber());
                    if(newVersion == null)
                    {
                        it.remove();

                        if(log)
                        {
                            this.getLogger().debug(BankleitzahlenDateiBundle.
                                getRemoveRecordInfoMessage(Locale.getDefault()).
                                format(new Object[] {
                                new Character(oldVersion.getChangeLabel()),
                                oldVersion.getSerialNumber() }));

                        }
                    }
                }
            }
        }
        finally
        {
            this.getTaskMonitor().finish(task);
        }

        this.cachedRecords = null;
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException for invalid property values.
     */
    protected void assertValidProperties()
    {
        if(this.getEncoding() == null || this.getEncoding().length() == 0)
        {
            throw new PropertyException("encoding", this.getEncoding());
        }

        try
        {
            "".getBytes(this.getEncoding());
        }
        catch(UnsupportedEncodingException e)
        {
            throw new PropertyException("encoding", this.getEncoding(), e);
        }
    }

    //-----------------------------------------------------BankleitzahlenDatei--
}

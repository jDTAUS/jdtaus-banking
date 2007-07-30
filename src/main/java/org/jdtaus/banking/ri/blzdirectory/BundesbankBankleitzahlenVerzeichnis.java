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
package org.jdtaus.banking.ri.blzdirectory;

import java.io.IOException;
import java.net.URL;
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
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.BankleitzahlExpirationException;
import org.jdtaus.banking.BankleitzahlInfo;
import org.jdtaus.banking.BankleitzahlenVerzeichnis;
import org.jdtaus.banking.messages.OutdatedBankleitzahlenVerzeichnisMessage;
import org.jdtaus.banking.util.BankleitzahlenDatei;
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
 *
 * @see #initialize()
 */
public final class BundesbankBankleitzahlenVerzeichnis
    implements BankleitzahlenVerzeichnis, ContainerInitializer
{
    //--Implementation----------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausImplementation
    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(BundesbankBankleitzahlenVerzeichnis.class.getName());
// </editor-fold>//GEN-END:jdtausImplementation

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausConstructors
    // This section is managed by jdtaus-container-mojo.

    /**
     * <code>BundesbankBankleitzahlenVerzeichnis</code> implementation constructor.
     *
     * @param meta Implementation meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    private BundesbankBankleitzahlenVerzeichnis(final Implementation meta)
    {
        super();
        if(meta == null)
        {
            throw new NullPointerException("meta");
        }
        this.initializeProperties(meta.getProperties());
    }
    /**
     * <code>BundesbankBankleitzahlenVerzeichnis</code> dependency constructor.
     *
     * @param meta dependency meta-data.
     *
     * @throws NullPointerException if <code>meta</code> is <code>null</code>.
     */
    private BundesbankBankleitzahlenVerzeichnis(final Dependency meta)
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

        p = meta.getProperty("dateOfExpirationPattern");
        this._dateOfExpirationPattern = (java.lang.String) p.getValue();


        p = meta.getProperty("dateOfExpirationText");
        this._dateOfExpirationText = (java.lang.String) p.getValue();

    }
// </editor-fold>//GEN-END:jdtausConstructors

    //------------------------------------------------------------Constructors--
    //--ContainerInitializer----------------------------------------------------

    /** {@code BankleitzahlenDatei} delegate. */
    private BankleitzahlenDatei bankFile;

    /** Maps bankcodes to a list of outdated records. */
    private Map outdated = new HashMap(5000);

    /** Date of expiration. */
    private Date dateOfExpiration;

    /**
     * Initializes the instance.
     *
     * @throws ImplementationException if initialization fails.
     *
     * @see #assertValidProperties()
     * @see #getFileResources()
     */
    public void initialize()
    {
        this.assertValidProperties();

        try
        {
            final URL[] rsrc = this.getFileResources();

            if(rsrc.length > 0)
            {
                this.bankFile = new BankleitzahlenDatei(rsrc[0]);
                for(int i = 1; i < rsrc.length; i++)
                {
                    final BankleitzahlenDatei update =
                        new BankleitzahlenDatei(rsrc[i]);

                    // Build mapping of outdated records.
                    final BankleitzahlInfo[] records =
                        this.bankFile.getRecords();

                    for(int j = records.length - 1; j >= 0; j--)
                    {
                        if(records[j].getChangeLabel() == 'D' &&
                            update.getRecord(
                            records[j].getSerialNumber()) == null)
                        {
                            List l = (List) this.outdated.get(
                                records[j].getBankCode());

                            if(l == null)
                            {
                                l = new LinkedList();
                                this.outdated.put(records[j].getBankCode(), l);
                            }

                            l.add(records[j]);
                        }
                    }

                    this.bankFile.update(update);
                }

                // Remove all outdated records for which another record
                // with the same Bankleitzahl still exists.
                for(Iterator it = this.outdated.keySet().
                    iterator(); it.hasNext();)
                {
                    final Bankleitzahl key = (Bankleitzahl) it.next();
                    if(this.findByBankCode(key.intValue(), false).length > 0)
                    {
                        it.remove();
                    }
                }

                // Log outdated records.
                if(this.getLogger().isDebugEnabled())
                {
                    for(Iterator it = this.outdated.keySet().
                        iterator(); it.hasNext();)
                    {
                        final Bankleitzahl blz = (Bankleitzahl) it.next();
                        this.getLogger().debug(
                            BundesbankBankleitzahlenVerzeichnisBundle.
                            getOutdatedInfoMessage(Locale.getDefault()).
                            format(new Object[] { blz.format(
                                Bankleitzahl.ELECTRONIC_FORMAT)}));

                    }
                }
            }

            // Log an application message if the directory is outdated.
            if(new Date().after(this.getDateOfExpiration()))
            {
                final MessageEvent evt = new MessageEvent(this, new Message[] {
                    new OutdatedBankleitzahlenVerzeichnisMessage(
                        this.getDateOfExpiration())
                }, MessageEvent.NOTIFICATION);

                this.getApplicationLogger().log(evt);
            }
        }
        catch(IOException e)
        {
            throw new ImplementationException(META, e);
        }
    }

    //----------------------------------------------------ContainerInitializer--
    //--Dependencies------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
    // This section is managed by jdtaus-container-mojo.

    /** Configured <code>ApplicationLogger</code> implementation. */
    private transient ApplicationLogger _dependency1;

    /**
     * Gets the configured <code>ApplicationLogger</code> implementation.
     *
     * @return the configured <code>ApplicationLogger</code> implementation.
     */
    private ApplicationLogger getApplicationLogger()
    {
        ApplicationLogger ret = null;
        if(this._dependency1 != null)
        {
            ret = this._dependency1;
        }
        else
        {
            ret = (ApplicationLogger) ContainerFactory.getContainer().
                getDependency(BundesbankBankleitzahlenVerzeichnis.class,
                "ApplicationLogger");

            if(ModelFactory.getModel().getModules().
                getImplementation(BundesbankBankleitzahlenVerzeichnis.class.getName()).
                getDependencies().getDependency("ApplicationLogger").
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
                getDependency(BundesbankBankleitzahlenVerzeichnis.class,
                "Logger");

            if(ModelFactory.getModel().getModules().
                getImplementation(BundesbankBankleitzahlenVerzeichnis.class.getName()).
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
     * Property {@code dateOfExpirationPattern}.
     * @serial
     */
    private java.lang.String _dateOfExpirationPattern;

    /**
     * Gets the value of property <code>dateOfExpirationPattern</code>.
     *
     * @return the value of property <code>dateOfExpirationPattern</code>.
     */
    private java.lang.String getDateOfExpirationPattern()
    {
        return this._dateOfExpirationPattern;
    }

    /**
     * Property {@code dateOfExpirationText}.
     * @serial
     */
    private java.lang.String _dateOfExpirationText;

    /**
     * Gets the value of property <code>dateOfExpirationText</code>.
     *
     * @return the value of property <code>dateOfExpirationText</code>.
     */
    private java.lang.String getDateOfExpirationText()
    {
        return this._dateOfExpirationText;
    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
    //--BankleitzahlenVerzeichnis-----------------------------------------------

    public Date getDateOfExpiration()
    {
        return this.dateOfExpiration;
    }

    public BankleitzahlInfo getHeadOffice(final Bankleitzahl bankCode)
    throws BankleitzahlExpirationException
    {
        if(bankCode == null)
        {
            throw new NullPointerException("bankCode");
        }

        BankleitzahlInfo ret = null;
        final BankleitzahlInfo[] matches =
            this.findByBankCode(bankCode.intValue(), false);

        if(matches.length == 1)
        {
            ret = matches[0];
        }
        else
        {
            this.checkReplacement(bankCode);
        }

        return ret;
    }

    public BankleitzahlInfo[] getBranchOffices(
        final Bankleitzahl bankCode) throws BankleitzahlExpirationException
    {
        if(bankCode == null)
        {
            throw new NullPointerException("bankCode");
        }

        final BankleitzahlInfo[] matches =
            this.findByBankCode(bankCode.intValue(), true);

        if(matches.length == 0)
        {
            this.checkReplacement(bankCode);
        }

        return matches;
    }

    public BankleitzahlInfo[] search(final String name, final String postalCode,
        final String city, final boolean branchOffices)
    {
        final Pattern namePat;
        final Pattern postalPat;
        final Pattern cityPat;
        final NumberFormat plzFmt = new DecimalFormat("00000");
        final BankleitzahlInfo[] records = this.bankFile == null ?
            new BankleitzahlInfo[0] : this.bankFile.getRecords();

        final Collection col = new ArrayList(records.length);
        String plz;

        try
        {
            namePat = name != null ? Pattern.compile(".*" +
                name.toUpperCase() + ".*") : null;

            postalPat = postalCode != null ? Pattern.compile(
                postalCode.toUpperCase() + ".*") : null;

            cityPat = city != null ? Pattern.compile(".*" +
                city.toUpperCase() + ".*") : null;

            for(int i = records.length - 1; i >= 0; i--)
            {
                plz = plzFmt.format(records[i].getPostalCode());

                if(
                    (namePat == null ?
                        true : namePat.matcher(records[i].getName().
                    toUpperCase()).matches()) &&

                    (postalPat == null ? true : postalPat.matcher(plz).
                    matches()) &&

                    (cityPat == null ?
                        true : cityPat.matcher(records[i].getCity().
                    toUpperCase()).matches()) &&

                    (branchOffices ? true : records[i].isHeadOffice()))
                {
                    col.add(records[i]);
                }
            }

            return (BankleitzahlInfo[]) col.
                toArray(new BankleitzahlInfo[col.size()]);

        }
        catch(PatternSyntaxException e)
        {
            // TODO JDK 1.5: throw new IllegalArgumentException(e.getMessage(), e);
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    //-----------------------------------------------BankleitzahlenVerzeichnis--
    //--BundesbankBankleitzahlenVerzeichnis-------------------------------------

    /** Creates a new {@code BundesbankBankleitzahlenVerzeichnis} instance. */
    public BundesbankBankleitzahlenVerzeichnis()
    {
        this(BundesbankBankleitzahlenVerzeichnis.META);
        this.initialize();
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException if configured properties hold invalid values.
     */
    private void assertValidProperties()
    {
        if(this.getDateOfExpirationText() == null ||
            this.getDateOfExpirationText().length() == 0)
        {
            throw new PropertyException("dateOfExpirationText",
                this.getDateOfExpirationText());

        }
        if(this.getDateOfExpirationPattern() == null ||
            this.getDateOfExpirationPattern().length() == 0)
        {
            throw new PropertyException("dateOfExpirationPattern",
                this.getDateOfExpirationPattern());

        }

        try
        {
            this.dateOfExpiration =
                new SimpleDateFormat(this.getDateOfExpirationPattern()).
                parse(this.getDateOfExpirationText());

        }
        catch(ParseException e)
        {
            throw new PropertyException("dateOfExpirationText",
                this.getDateOfExpirationText(), e);

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
    private void checkReplacement(final Bankleitzahl bankCode)
    throws BankleitzahlExpirationException
    {
        if(bankCode == null)
        {
            throw new NullPointerException("bankCode");
        }

        final List l = (List) this.outdated.get(bankCode);

        if(l != null)
        {
            // Finds the most recent record specifying a replacing Bankleitzahl.
            BankleitzahlInfo current = null;
            BankleitzahlInfo record = null;

            for(Iterator it = l.iterator(); it.hasNext();)
            {
                current = (BankleitzahlInfo) it.next();
                if(current.getReplacingBankCode() != null)
                {
                    record = current;
                }
            }

            // Only throw an exception for records specifying a replacing
            // Bankleitzahl which is not outdated.
            if(record != null)
            {
                final int replacingCode =
                    record.getReplacingBankCode().intValue();

                final BankleitzahlInfo[] replacement =
                    this.findByBankCode(replacingCode, false);

                assert replacement.length == 0 || replacement.length == 1 :
                    "Multiple head offices for " + replacingCode + ".";

                if(replacement.length == 1)
                {
                    throw new BankleitzahlExpirationException(
                        record, replacement[0]);

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
     * @throws IOException if getting the resources fails.
     *
     * @see BankfileProvider
     */
    private URL[] getFileResources() throws IOException
    {
        final List resources = new LinkedList();
        final Specification providerSpec = ModelFactory.getModel().getModules().
            getSpecification(BankfileProvider.class.getName());

        for(int i = providerSpec.getImplementations().size() - 1; i >= 0; i--)
        {
            final BankfileProvider provider =
                (BankfileProvider) ContainerFactory.getContainer().
                getImplementation(BankfileProvider.class,
                providerSpec.getImplementations().getImplementation(i).
                getName());

            resources.addAll(Arrays.asList(provider.getResources()));
        }

        return (URL[]) resources.toArray(new URL[resources.size()]);
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
        final int bankCode, final boolean branchOffices)
    {
        final BankleitzahlInfo[] records = this.bankFile == null ?
            new BankleitzahlInfo[0] : this.bankFile.getRecords();

        final Collection col = new ArrayList(records.length);

        for(int i = records.length - 1; i >= 0; i--)
        {
            if(records[i].getBankCode().intValue() == bankCode &&
                (records[i].isHeadOffice() != branchOffices) &&
                !col.add(records[i]))
            {
                throw new ImplementationException(META,
                    new IllegalStateException());

            }
        }

        return (BankleitzahlInfo[]) col.toArray(
            new BankleitzahlInfo[col.size()]);

    }

    //-------------------------------------BundesbankBankleitzahlenVerzeichnis--
}

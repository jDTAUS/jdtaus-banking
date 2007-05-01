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
package org.jdtaus.banking.ri.txtdirectory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.jdtaus.banking.Textschluessel;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.container.Properties;
import org.jdtaus.core.container.Property;
import org.jdtaus.core.container.PropertyException;

/**
 * Reference {@code Textschluessel} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class RITextschluessel extends Textschluessel
{
    //--Implementation----------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(RITextschluessel.class.getName());

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

        p = meta.getProperty("defaultLanguage");
        this._defaultLanguage = (java.lang.String) p.getValue();

    }

    //------------------------------------------------------------Constructors--
    //--Dependencies------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.


    //------------------------------------------------------------Dependencies--
    //--Properties--------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /**
     * Property {@code defaultLanguage}.
     * @serial
     */
    private java.lang.String _defaultLanguage;

    /**
     * Gets the value of property <code>defaultLanguage</code>.
     *
     * @return the value of property <code>defaultLanguage</code>.
     */
    protected java.lang.String getDefaultLanguage()
    {
        return this._defaultLanguage;
    }


    //--------------------------------------------------------------Properties--
    //--Textschluessel----------------------------------------------------------

    public String getShortDescription(Locale locale)
    {
        if(locale == null)
        {
            locale = Locale.getDefault();
        }

        String description = (String) this.shortDescriptions.
            get(locale.getLanguage());

        if(description == null)
        {
            description = (String) this.shortDescriptions.get(
                this.getDefaultLanguage());

            if(description == null)
            {
                // Fall back to a default message just stating key and extension
                // if no language is available.
                description = RITextschluesselBundle.
                    getDefaultDescriptionText(locale);

            }
        }

        final MessageFormat fmt = new MessageFormat(description);

        return fmt.format(new Object[] {
            new Integer(this.getKey()),
            new Integer(this.getExtension())
        });
    }

    //----------------------------------------------------------Textschluessel--
    //--RITextschluessel--------------------------------------------------------

    /** Creates a new {@code RITextschluessel} instance. */
    public RITextschluessel()
    {
        this.initializeProperties(RITextschluessel.META.getProperties());
        this.assertValidProperties();
    }

    /**
     * Maps language codes to short descriptions.
     * @serial
     */
    private Map shortDescriptions = new HashMap(10);

    /**
     * Updates property {@code shortDescription} for a given locale.
     *
     * @param locale the locale to update the short description for or
     * {@code null} for {@code Locale.getDefault()}.
     * @param shortDescription the new value for property
     * {@code shortDescription} for {@code locale}.
     *
     * @return the value previously held by the instance for {@code locale} or
     * {@code null} if the instance previously held either no value or a
     * {@code null} value for {@code locale}.
     *
     * @throws NullPointerException if {@code shortDescription} is {@code null}.
     */
    public String updateShortDescription(Locale locale,
        final String shortDescription)
    {
        if(shortDescription == null)
        {
            throw new NullPointerException("shortDescription");
        }

        if(locale == null)
        {
            locale = Locale.getDefault();
        }

        return (String) this.shortDescriptions.
            put(locale.getLanguage(), shortDescription);

    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException for invalid property values.
     */
    protected void assertValidProperties()
    {
        if(this.getDefaultLanguage() == null ||
            this.getDefaultLanguage().length() <= 0)
        {
            throw new PropertyException("defaultLanguage",
                this.getDefaultLanguage());
        }
    }

    //--------------------------------------------------------RITextschluessel--

}

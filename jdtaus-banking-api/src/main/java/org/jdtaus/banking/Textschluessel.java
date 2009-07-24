/*
 *  jDTAUS Banking API
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
package org.jdtaus.banking;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.PropertyException;

/**
 * Type of a transaction in germany.
 * <p>A Textschlüssel is made up of a two-digit positive integer (the key) and
 * a three-digit positive integer (the extension). The key, together with a
 * constant extension, uniquely identifies a transaction's type. The extension
 * may also be used to hold non-identifying data. In such cases only the key is
 * used to identify a transaction's type and the extension holds variable
 * data.</p>
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 *
 * @see TextschluesselVerzeichnis
 */
public class Textschluessel implements Cloneable, Comparable, Serializable
{
    //--Constants---------------------------------------------------------------

    /** Constant for the name of property {@code key}. */
    public static final String PROP_KEY =
        Textschluessel.class.getName() + ".PROP_KEY";

    /** Constant for the name of property {@code extension}. */
    public static final String PROP_EXTENSION =
        Textschluessel.class.getName() + ".PROP_EXTENSION";

    /** Constant for the name of property {@code validTo}. */
    public static final String PROP_VALID_TO =
        Textschluessel.class.getName() + ".PROP_VALID_TO";

    /** Constant for the name of property {@code validFrom}. */
    public static final String PROP_VALID_FROM =
        Textschluessel.class.getName() + ".PROP_VALID_FROM";

    /** Constant for the name of property {@code debit}. */
    public static final String PROP_DEBIT =
        Textschluessel.class.getName() + ".PROP_DEBIT";

    /** Constant for the name of property {@code remittance}. */
    public static final String PROP_REMITTANCE =
        Textschluessel.class.getName() + ".PROP_REMITTANCE";

    /** Constant for the name of property {@code variable}. */
    public static final String PROP_VARIABLE =
        Textschluessel.class.getName() + ".PROP_VARIABLE";

    /** Constant for the name of property {@code shortDescription}. */
    public static final String PROP_SHORTDESCRIPTION =
        Textschluessel.class.getName() + ".PROP_SHORTDESCRIPTION";

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = -8556424800883022756L;

    //---------------------------------------------------------------Constants--
    //--Textschluessel----------------------------------------------------------

    /**
     * Key of the Textschlüssel.
     * @serial
     */
    private int key;

    /**
     * Extension of the Textschlüsse.
     * @serial
     */
    private int extension;

    /**
     * Start date of validity.
     * @serial
     */
    private Date validFrom;
    private long validFromMillis;

    /**
     * End date of validity.
     * @serial
     */
    private Date validTo;
    private long validToMillis;

    /**
     * Flag indicating if a transaction of the type is a debit.
     * @serial
     */
    private boolean debit;

    /**
     * Flag indicating if a transaction of the type is a remittance.
     * @serial
     */
    private boolean remittance;

    /**
     * Flag indicating if the extension holds non-identifying, variable data.
     * @serial
     */
    private boolean variable;

    /**
     * Maps language codes to short descriptions.
     * @serial
     */
    private Map shortDescriptions = new HashMap( 10 );

    /** Cached hash code. */
    private transient int hashCode = NO_HASHCODE;
    private static final int NO_HASHCODE = Integer.MIN_VALUE;

    /** Creates a new {@code Textschluessel} instance. */
    public Textschluessel()
    {
        super();
        this.assertValidProperties();
    }

    /**
     * Getter for property {@code key}.
     *
     * @return key of the Textschlüssel.
     */
    public int getKey()
    {
        return this.key;
    }

    /**
     * Setter for property {@code key}.
     *
     * @param key new key of the Textschlüssel.
     */
    public void setKey( final int key )
    {
        this.key = key;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code extension}.
     *
     * @return extension of the Textschlüssel.
     */
    public int getExtension()
    {
        return this.extension;
    }

    /**
     * Setter for property {@code extension}.
     *
     * @param extension new extension of the Textschlüssel.
     */
    public void setExtension( final int extension )
    {
        this.extension = extension;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Gets the start date of validity of the Textschlüssel.
     *
     * @return the date the Textschlüssel is valid from (inclusive) or
     * {@code null} if nothing is known about the start of validity of the
     * Textschlüssel.
     */
    public Date getValidFrom()
    {
        return this.validFrom != null ? (Date) this.validFrom.clone() : null;
    }

    /**
     * Sets the start date of validity of the Textschlüssel.
     *
     * @param value The date the Textschlüssel is valid from or {@code null} if
     * nothing is known about the start of validity of the Textschlüssel.
     */
    public void setValidFrom( final Date value )
    {
        if ( value == null )
        {
            this.validFrom = null;
            this.validFromMillis = 0L;
        }
        else
        {
            this.validFrom = (Date) value.clone();
            this.validFromMillis = value.getTime();
        }
    }

    /**
     * Gets the end date of validity of the Textschlüssel.
     *
     * @return the date the Textschlüssel is valid to (inclusive) or
     * {@code null} if nothing is known about the end of validity of the
     * Textschlüssel.
     */
    public Date getValidTo()
    {
        return this.validTo != null ? (Date) this.validTo.clone() : null;
    }

    /**
     * Sets the end date of validity of the Textschlüssel.
     *
     * @param value The date the Textschlüssel is valid to or {@code null} if
     * nothing is known about the end of validity of the Textschlüssel.
     */
    public void setValidTo( final Date value )
    {
        if ( value == null )
        {
            this.validTo = null;
            this.validToMillis = 0L;
        }
        else
        {
            this.validTo = (Date) value.clone();
            this.validToMillis = 0L;
        }
    }

    /**
     * Gets a flag indicating that the Textschlüssel is valid at a given date.
     *
     * @param date The date with which to check.
     *
     * @return {@code true}, if the Textschlüssel is valid at {@code date};
     * {@code false} if not.
     *
     * @throws NullPointerException if {@code date} is {@code null}.
     */
    public boolean isValidAt( final Date date )
    {
        if ( date == null )
        {
            throw new NullPointerException( "date" );
        }


        return !( ( this.validFrom != null &&
                    this.validFromMillis > date.getTime() ) ||
                  ( this.validTo != null &&
                    this.validToMillis < date.getTime() ) );

    }

    /**
     * Flag indicating if a transaction of this type is a debit.
     *
     * @return {@code true} if a transaction of this type is a debit;
     * {@code false} if not.
     */
    public boolean isDebit()
    {
        return this.debit;
    }

    /**
     * Setter for property {@code debit}.
     *
     * @param debit {@code true} if a transaction of this type is a debit;
     * {@code false} if not.
     */
    public void setDebit( final boolean debit )
    {
        this.debit = debit;
    }

    /**
     * Flag indicating if a transaction of this type is a remittance.
     *
     * @return {@code true} if a transaction of this type is a remittance;
     * {@code false} if not.
     */
    public boolean isRemittance()
    {
        return this.remittance;
    }

    /**
     * Setter for property {@code remittance}.
     *
     * @param remittance {@code true} if a transaction of this type is a
     * remittance; {@code false} if not.
     */
    public void setRemittance( final boolean remittance )
    {
        this.remittance = remittance;
    }

    /**
     * Flag indicating if the extension holds non-identifying, variable data.
     *
     * @return {@code true} if the extension holds non-identifying, variable
     * data; {@code false} if the extension is part of the identifying key.
     */
    public boolean isVariable()
    {
        return this.variable;
    }

    /**
     * Setter for property {@code variable}.
     *
     * @param variable {@code true} if the extension holds non-identifying,
     * variable data; {@code false} if the extension is part of the identifying
     * key.
     */
    public void setVariable( final boolean variable )
    {
        this.variable = variable;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Gets the short description of the Textschlüssel for a given locale.
     *
     * @param locale the locale of the short description to return or
     * {@code null} for {@code Locale.getDefault()}.
     *
     * @return the short description of the instance for {@code locale}.
     */
    public String getShortDescription( Locale locale )
    {
        if ( locale == null )
        {
            locale = Locale.getDefault();
        }

        this.assertValidProperties();

        // Try the requested language.
        String description = (String) this.shortDescriptions.get(
            locale.getLanguage().toLowerCase() );

        if ( description == null )
        { // Try the configured default language.
            description = (String) this.shortDescriptions.get(
                this.getDefaultLanguage().toLowerCase() );

        }

        if ( description == null )
        { // Try the system's default language.
            description = (String) this.shortDescriptions.get(
                Locale.getDefault().getLanguage().toLowerCase() );

        }

        if ( description == null )
        { // Fall back to a default message just stating key and extension.
            description =
                this.getTextschluesselDescriptionMessage(
                this.getLocale(),
                new Integer( this.getKey() ),
                new Integer( this.getExtension() ) );

        }

        final MessageFormat fmt = new MessageFormat( description, locale );
        return fmt.format( new Object[]
            {
                new Integer( this.getKey() ),
                new Integer( this.getExtension() )
            } );

    }

    /**
     * Setter for property {@code shortDescription}.
     *
     * @param locale the locale to set the short description for or {@code null}
     * for {@code Locale.getDefault()}.
     * @param shortDescription the new value for property
     * {@code shortDescription} for {@code locale}.
     *
     * @return the value previously held by the instance for {@code locale} or
     * {@code null} if the instance previously held no value for {@code locale}.
     *
     * @throws NullPointerException if {@code shortDescription} is {@code null}.
     */
    public String setShortDescription(
        Locale locale, final String shortDescription )
    {
        if ( shortDescription == null )
        {
            throw new NullPointerException( "shortDescription" );
        }

        if ( locale == null )
        {
            locale = Locale.getDefault();
        }

        return (String) this.shortDescriptions.put(
            locale.getLanguage().toLowerCase(), shortDescription );

    }

    /**
     * Gets all locales for which the instance holds short descriptions.
     *
     * @return all locales for which the instance holds short descriptions.
     */
    public Locale[] getLocales()
    {
        final Set locales = new HashSet( this.shortDescriptions.size() );
        for ( Iterator it = this.shortDescriptions.keySet().
            iterator(); it.hasNext(); )
        {
            locales.add( new Locale( (String) it.next() ) );
        }

        return (Locale[]) locales.toArray( new Locale[ locales.size() ] );
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException for invalid property values.
     */
    protected void assertValidProperties()
    {
        if ( this.getDefaultLanguage() == null ||
             this.getDefaultLanguage().length() <= 0 )
        {
            throw new PropertyException( "defaultLanguage",
                                         this.getDefaultLanguage() );

        }
    }

    /**
     * Creates a string representing the properties of the instance.
     *
     * @return a string representing the properties of the instance.
     */
    private String internalString()
    {
        return new StringBuffer( 200 ).append( '{' ).
            append( "key=" ).append( this.key ).
            append( ", extension=" ).append( this.extension ).
            append( ", validFrom=" ).append( this.validFrom ).
            append( ", validTo=" ).append( this.validTo ).
            append( ", debit=" ).append( this.debit ).
            append( ", remittance=" ).append( this.remittance ).
            append( ", variable=" ).append( this.variable ).
            append( ", shortDescription=" ).
            append( this.getShortDescription( null ) ).
            append( '}' ).toString();

    }

    //----------------------------------------------------------Textschluessel--
    //--Comparable--------------------------------------------------------------

    /**
     * Compares this object with the specified object for order.
     * <p>Compares the values of properties {@code key} and {@code extension}
     * and returns a negative integer, zero, or a positive integer as this
     * object is less than, equal to, or greater than the specified object.</p>
     *
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     *
     * @throws NullPointerException if {@code o} is {@code null}.
     * @throws ClassCastException if the specified object's type prevents it
     * from being compared to this Object.
     */
    public int compareTo( final Object o )
    {
        if ( o == null )
        {
            throw new NullPointerException( "o" );
        }
        if ( !( o instanceof Textschluessel ) )
        {
            throw new ClassCastException( o.getClass().getName() );
        }

        int result = 0;
        final Textschluessel that = (Textschluessel) o;

        if ( !this.equals( that ) )
        {
            result = this.key == that.key ? 0 : this.key > that.key ? 1 : -1;
            if ( result == 0 && this.extension != that.extension )
            {
                result = this.extension > that.extension ? 1 : -1;
            }
        }

        return result;
    }

    //--------------------------------------------------------------Comparable--
    //--Serializable------------------------------------------------------------

    /**
     * Takes care of initializing the {@code validFromMillis} and
     * {@code validToMillis} fields when constructed from an &lt;1.11
     * object stream.
     *
     * @throws ObjectStreamException if resolution fails.
     */
    private Object readResolve() throws ObjectStreamException
    {
        if ( this.validFrom != null )
        {
            this.validFromMillis = this.validFrom.getTime();
        }
        if ( this.validTo != null )
        {
            this.validToMillis = this.validTo.getTime();
        }

        return this;
    }

    //------------------------------------------------------------Serializable--
    //--Object------------------------------------------------------------------

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this instance.
     */
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            throw new AssertionError( e );
        }
    }

    /**
     * Indicates whether some other object is equal to this one by comparing
     * the values of all properties.
     * <p>The extension will only be compared if it is part of the identifying
     * key based on the value of property {@code variable}.</p>
     *
     * @param o the reference object with which to compare.
     *
     * @return {@code true} if this object is the same as {@code o};
     * {@code false} otherwise.
     */
    public boolean equals( final Object o )
    {
        boolean ret = o == this;

        if ( !ret && o instanceof Textschluessel )
        {
            final Textschluessel that = (Textschluessel) o;

            if ( this.isVariable() )
            {
                ret = that.isVariable() && this.key == that.getKey();
            }
            else
            {
                ret = !that.isVariable() && this.key == that.getKey() &&
                      this.extension == that.getExtension();

            }
        }

        return ret;
    }

    /**
     * Returns a hash code value for this object.
     *
     * @return a hash code value for this object.
     */
    public int hashCode()
    {
        if ( this.hashCode == NO_HASHCODE )
        {
            int hc = 23;

            hc = 37 * hc + ( this.variable ? 0 : 1 );
            hc = 37 * hc + this.key;
            hc = 37 * hc + this.extension;

            this.hashCode = hc;
        }

        return this.hashCode;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    public String toString()
    {
        return super.toString() + this.internalString();
    }

    //------------------------------------------------------------------Object--
    //--Dependencies------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
    // This section is managed by jdtaus-container-mojo.

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
    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>textschluesselDescription</code>.
     * <blockquote><pre>{0,number,00}{1,number,000}</pre></blockquote>
     * <blockquote><pre>{0,number,00}{1,number,000}</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param key format argument.
     * @param extension format argument.
     *
     * @return the text of message <code>textschluesselDescription</code>.
     */
    private String getTextschluesselDescriptionMessage( final Locale locale,
            final java.lang.Number key,
            final java.lang.Number extension )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "textschluesselDescription", locale,
                new Object[]
                {
                    key,
                    extension
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
    //--Properties--------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausProperties
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the value of property <code>defaultLanguage</code>.
     *
     * @return Default language of descriptions when there is no description available for a requested language.
     */
    private java.lang.String getDefaultLanguage()
    {
        return (java.lang.String) ContainerFactory.getContainer().
            getProperty( this, "defaultLanguage" );

    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
}

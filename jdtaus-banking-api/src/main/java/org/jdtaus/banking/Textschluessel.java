/*
 *  jDTAUS Banking API
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
 * <p>A Textschlüssel is made up of a two-digit positive integer (the key) and a three-digit positive integer
 * (the extension). The key, together with a constant extension, uniquely identifies a transaction's type. The extension
 * may also be used to hold non-identifying data. In such cases only the key is used to identify a transaction's type
 * and the extension holds variable data.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 *
 * @see TextschluesselVerzeichnis
 */
public class Textschluessel implements Cloneable, Comparable, Serializable
{

    /** Constant for the name of property {@code key}. */
    public static final String PROP_KEY = "org.jdtaus.banking.Textschluessel.PROP_KEY";

    /** Constant for the name of property {@code extension}. */
    public static final String PROP_EXTENSION = "org.jdtaus.banking.Textschluessel.PROP_EXTENSION";

    /** Constant for the name of property {@code validTo}. */
    public static final String PROP_VALID_TO = "org.jdtaus.banking.Textschluessel.PROP_VALID_TO";

    /** Constant for the name of property {@code validFrom}. */
    public static final String PROP_VALID_FROM = "org.jdtaus.banking.Textschluessel.PROP_VALID_FROM";

    /** Constant for the name of property {@code debit}. */
    public static final String PROP_DEBIT = "org.jdtaus.banking.Textschluessel.PROP_DEBIT";

    /** Constant for the name of property {@code remittance}. */
    public static final String PROP_REMITTANCE = "org.jdtaus.banking.Textschluessel.PROP_REMITTANCE";

    /** Constant for the name of property {@code variable}. */
    public static final String PROP_VARIABLE = "org.jdtaus.banking.Textschluessel.PROP_VARIABLE";

    /** Constant for the name of property {@code shortDescription}. */
    public static final String PROP_SHORTDESCRIPTION = "org.jdtaus.banking.Textschluessel.PROP_SHORTDESCRIPTION";

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = -8556424800883022756L;

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
    private transient long validFromMillis;

    /**
     * End date of validity.
     * @serial
     */
    private Date validTo;
    private transient long validToMillis;

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
     * @return Key of the Textschlüssel.
     */
    public int getKey()
    {
        return this.key;
    }

    /**
     * Setter for property {@code key}.
     *
     * @param value New key of the Textschlüssel.
     */
    public void setKey( final int value )
    {
        this.key = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Getter for property {@code extension}.
     *
     * @return Extension of the Textschlüssel.
     */
    public int getExtension()
    {
        return this.extension;
    }

    /**
     * Setter for property {@code extension}.
     *
     * @param value New extension of the Textschlüssel.
     */
    public void setExtension( final int value )
    {
        this.extension = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Gets the date of validity of the Textschlüssel.
     *
     * @return The date the Textschlüssel is valid from (inclusive) or {@code null} if nothing is known about the start
     * of validity of the Textschlüssel.
     */
    public Date getValidFrom()
    {
        return this.validFrom != null ? (Date) this.validFrom.clone() : null;
    }

    /**
     * Sets the date of validity of the Textschlüssel.
     *
     * @param value The new date the Textschlüssel is valid from or {@code null} if nothing is known about the start of
     * validity of the Textschlüssel.
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
     * Gets the date of expiration of the Textschlüssel.
     *
     * @return The date the Textschlüssel is valid to (inclusive) or {@code null} if nothing is known about the date of
     * expiration of the Textschlüssel.
     */
    public Date getValidTo()
    {
        return this.validTo != null ? (Date) this.validTo.clone() : null;
    }

    /**
     * Sets the date of expiration of the Textschlüssel.
     *
     * @param value The new date the Textschlüssel is valid to or {@code null} if nothing is known about the date of
     * expiration of the Textschlüssel.
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
     * @return {@code true}, if the Textschlüssel is valid at {@code date}; {@code false} if not.
     *
     * @throws NullPointerException if {@code date} is {@code null}.
     */
    public boolean isValidAt( final Date date )
    {
        if ( date == null )
        {
            throw new NullPointerException( "date" );
        }


        return !( ( this.validFrom != null && this.validFromMillis > date.getTime() ) ||
                  ( this.validTo != null && this.validToMillis < date.getTime() ) );

    }

    /**
     * Flag indicating if a transaction of this type is a debit.
     *
     * @return {@code true} if a transaction of this type is a debit; {@code false} if not.
     */
    public boolean isDebit()
    {
        return this.debit;
    }

    /**
     * Setter for property {@code debit}.
     *
     * @param value {@code true} if a transaction of this type is a debit; {@code false} if not.
     */
    public void setDebit( final boolean value )
    {
        this.debit = value;
    }

    /**
     * Flag indicating if a transaction of this type is a remittance.
     *
     * @return {@code true} if a transaction of this type is a remittance; {@code false} if not.
     */
    public boolean isRemittance()
    {
        return this.remittance;
    }

    /**
     * Setter for property {@code remittance}.
     *
     * @param value {@code true} if a transaction of this type is a remittance; {@code false} if not.
     */
    public void setRemittance( final boolean value )
    {
        this.remittance = value;
    }

    /**
     * Flag indicating if the extension holds non-identifying, variable data.
     *
     * @return {@code true} if the extension holds non-identifying, variable data; {@code false} if the extension is
     * part of the identifying key.
     */
    public boolean isVariable()
    {
        return this.variable;
    }

    /**
     * Setter for property {@code variable}.
     *
     * @param value {@code true} if the extension holds non-identifying, variable data; {@code false} if the extension
     * is part of the identifying key.
     */
    public void setVariable( final boolean value )
    {
        this.variable = value;
        this.hashCode = NO_HASHCODE;
    }

    /**
     * Gets the short description of the Textschlüssel for a given locale.
     *
     * @param locale The locale of the short description to return or {@code null} for {@code Locale.getDefault()}.
     *
     * @return The short description of the instance for {@code locale}.
     */
    public String getShortDescription( final Locale locale )
    {
        final Locale l = locale == null ? Locale.getDefault() : locale;

        this.assertValidProperties();

        // Try the requested language.
        String description = (String) this.shortDescriptions.get( l.getLanguage().toLowerCase() );

        if ( description == null )
        {
            // Try the configured default language.
            description = (String) this.shortDescriptions.get( this.getDefaultLanguage().toLowerCase() );
        }

        if ( description == null )
        {
            // Try the system's default language.
            description = (String) this.shortDescriptions.get( Locale.getDefault().getLanguage().toLowerCase() );
        }

        if ( description == null )
        {
            // Fall back to a default message just stating key and extension.
            description = this.getTextschluesselDescriptionMessage(
                this.getLocale(), new Integer( this.getKey() ), new Integer( this.getExtension() ) );

        }

        return new MessageFormat( description, l ).format( new Object[]
            {
                new Integer( this.getKey() ), new Integer( this.getExtension() )
            } );

    }

    /**
     * Setter for property {@code shortDescription}.
     *
     * @param locale The locale to set the short description for or {@code null} for {@code Locale.getDefault()}.
     * @param shortDescription The new value for property {@code shortDescription} for {@code locale}.
     *
     * @return The value previously held by the instance for {@code locale} or {@code null} if the instance previously
     * held no value for {@code locale}.
     *
     * @throws NullPointerException if {@code shortDescription} is {@code null}.
     */
    public String setShortDescription( final Locale locale, final String shortDescription )
    {
        if ( shortDescription == null )
        {
            throw new NullPointerException( "shortDescription" );
        }

        final Locale l = locale == null ? Locale.getDefault() : locale;
        return (String) this.shortDescriptions.put( l.getLanguage().toLowerCase(), shortDescription );
    }

    /**
     * Gets all locales for which the instance holds short descriptions.
     *
     * @return All locales for which the instance holds short descriptions.
     */
    public Locale[] getLocales()
    {
        final Set locales = new HashSet( this.shortDescriptions.size() );
        for ( final Iterator it = this.shortDescriptions.keySet().iterator(); it.hasNext(); )
        {
            locales.add( new Locale( (String) it.next() ) );
        }

        return (Locale[]) locales.toArray( new Locale[ locales.size() ] );
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException for invalid property values.
     *
     * @deprecated Removed without replacement.
     */
    protected void assertValidProperties()
    {
        if ( this.getDefaultLanguage() == null || this.getDefaultLanguage().length() <= 0 )
        {
            throw new PropertyException( "defaultLanguage", this.getDefaultLanguage() );
        }
    }

    /**
     * Compares this object with the specified object for order.
     * <p>Compares the values of properties {@code key} and {@code extension} and returns a negative integer, zero, or a
     * positive integer as this object is less than, equal to, or greater than the specified object.</p>
     *
     * @param o The Object to be compared.
     * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     * the specified object.
     *
     * @throws NullPointerException if {@code o} is {@code null}.
     * @throws ClassCastException if the specified object's type prevents it from being compared to this Object.
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

    /**
     * Takes care of initializing the {@code validFromMillis} and {@code validToMillis} fields when constructed from an
     * &lt;1.11 object stream.
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

    /**
     * Creates and returns a copy of this object.
     *
     * @return A clone of this instance.
     */
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch ( final CloneNotSupportedException e )
        {
            throw new AssertionError( e );
        }
    }

    /**
     * Indicates whether some other object is equal to this one by comparing the values of all properties.
     * <p>The extension will only be compared if it is part of the identifying key based on the value of property
     * {@code variable}.</p>
     *
     * @param o The reference object with which to compare.
     *
     * @return {@code true} if this object is the same as {@code o}; {@code false} otherwise.
     */
    public boolean equals( final Object o )
    {
        boolean equal = o == this;

        if ( !equal && o instanceof Textschluessel )
        {
            final Textschluessel that = (Textschluessel) o;

            if ( this.isVariable() )
            {
                equal = that.isVariable() && this.key == that.getKey();
            }
            else
            {
                equal = !that.isVariable() && this.key == that.getKey() && this.extension == that.getExtension();
            }
        }

        return equal;
    }

    /**
     * Returns a hash code value for this object.
     *
     * @return A hash code value for this object.
     */
    public int hashCode()
    {
        if ( this.hashCode == NO_HASHCODE )
        {
            int hc = 23;

            hc = 37 * hc + ( this.variable ? 0 : 1 );
            hc = 37 * hc + this.key;

            if ( !this.variable )
            {
                hc = 37 * hc + this.extension;
            }

            this.hashCode = hc;
        }

        return this.hashCode;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString()
    {
        return super.toString() + this.internalString();
    }

    /**
     * Creates a string representing the properties of the instance.
     *
     * @return A string representing the properties of the instance.
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
     * @param k format parameter.
     * @param e format parameter.
     *
     * @return the text of message <code>textschluesselDescription</code>.
     */
    private String getTextschluesselDescriptionMessage( final Locale locale,
            final java.lang.Number k,
            final java.lang.Number e )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "textschluesselDescription", locale,
                new Object[]
                {
                    k,
                    e
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

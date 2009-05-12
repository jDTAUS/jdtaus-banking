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
package org.jdtaus.banking.util.swing;

import java.text.ParseException;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.JTextComponent;
import org.jdtaus.banking.Kontonummer;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.PropertyException;

/**
 * {@code JFormattedTextField} supporting the {@code Kontonummer} type.
 * <p>This textfield uses the {@link Kontonummer} type for parsing and
 * formatting. An empty string value is treated as {@code null}. Property
 * {@code format} controls formatting and takes one of the format constants
 * defined in class {@code Kontonummer}. By default the
 * {@code ELECTRONIC_FORMAT} is used. The {@code validating} flag controls
 * validation of values entered into the textfield. If {@code true} (default),
 * an {@code InputVerifier} is registered with the textfield disallowing invalid
 * values, that is, values which are not {@code null} and not empty strings and
 * for which the {@link Kontonummer#parse(String)} method throws a
 * {@code ParseException}.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class KontonummerTextField extends JFormattedTextField
{
    //--Constants---------------------------------------------------------------

    /** Serial version UID for backwards compatibility with 1.1.x classes. */
    private static final long serialVersionUID = -959284086262750493L;

    //---------------------------------------------------------------Constants--
    //--Properties--------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausProperties
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the value of property <code>defaultValidating</code>.
     *
     * @return Default value of the flag indicating if validation should be performed.
     */
    private java.lang.Boolean isDefaultValidating()
    {
        return (java.lang.Boolean) ContainerFactory.getContainer().
            getProperty( this, "defaultValidating" );

    }

    /**
     * Gets the value of property <code>defaultFormat</code>.
     *
     * @return Default value of the format to use when formatting Kontonummer instances (4001 = electronic format, 4002 letter format).
     */
    private java.lang.Integer getDefaultFormat()
    {
        return (java.lang.Integer) ContainerFactory.getContainer().
            getProperty( this, "defaultFormat" );

    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
    //--KontonummerTextField----------------------------------------------------

    /**
     * Format used to format Kontonummer instances.
     * @serial
     */
    private Integer format;

    /**
     * Flag indicating if validation is performed.
     * @serial
     */
    private Boolean validating;

    /** Creates a new default {@code KontonummerTextField} instance. */
    public KontonummerTextField()
    {
        super();
        this.assertValidProperties();
        this.setColumns( Kontonummer.MAX_CHARACTERS );
        this.setFormatterFactory(
            new AbstractFormatterFactory()
            {

                public AbstractFormatter getFormatter(
                    final JFormattedTextField ftf )
                {
                    return new AbstractFormatter()
                    {

                        public Object stringToValue( final String text )
                            throws ParseException
                        {
                            Object value = null;

                            if ( text != null && text.trim().
                                length() > 0 )
                            {
                                value =
                                    Kontonummer.parse( text );
                            }

                            return value;
                        }

                        public String valueToString( final Object value )
                            throws ParseException
                        {
                            String ret = null;

                            if ( value instanceof Kontonummer )
                            {
                                final Kontonummer kto = (Kontonummer) value;
                                ret = kto.format( getFormat() );
                            }

                            return ret;
                        }
                    };
                }
            } );

        this.setInputVerifier(
            new InputVerifier()
            {

                public boolean verify( final JComponent input )
                {
                    boolean valid = true;

                    if ( isValidating() &&
                        input instanceof JTextComponent )
                    {
                        final String text =
                            ( (JTextComponent) input ).getText();

                        if ( text != null &&
                            text.trim().length() > 0 )
                        {
                            try
                            {
                                Kontonummer.parse( text );
                            }
                            catch ( ParseException e )
                            {
                                valid = false;
                            }
                        }
                    }

                    return valid;
                }
            } );
    }

    /**
     * Gets the last valid {@code Kontonummer}.
     *
     * @return the last valid {@code Kontonummer} or {@code null}.
     */
    public Kontonummer getKontonummer()
    {
        return (Kontonummer) this.getValue();
    }

    /**
     * Gets the constant of the format used when formatting Kontonummer
     * instances.
     *
     * @return the constant of the format used when formatting Kontonummer
     * instances.
     *
     * @see Kontonummer#ELECTRONIC_FORMAT
     * @see Kontonummer#LETTER_FORMAT
     */
    public int getFormat()
    {
        if ( this.format == null )
        {
            this.format = this.getDefaultFormat();
        }

        return this.format.intValue();
    }

    /**
     * Sets the constant of the format to use when formatting Kontonummer
     * instances.
     *
     * @param value the constant of the format to use when formatting
     * Kontonummer instances.
     *
     * @throws IllegalArgumentException if {@code format} is neither
     * {@code ELECTRONIC_FORMAT} nor {@code LETTER_FORMAT}.
     *
     * @see Kontonummer#ELECTRONIC_FORMAT
     * @see Kontonummer#LETTER_FORMAT
     */
    public void setFormat( final int value )
    {
        if ( value != Kontonummer.ELECTRONIC_FORMAT &&
            value != Kontonummer.LETTER_FORMAT )
        {
            throw new IllegalArgumentException( Integer.toString( value ) );
        }

        this.format = new Integer( value );
    }

    /**
     * Gets the flag indicating if validation is performed.
     *
     * @return {@code true} if the fields' value is validated; {@code false} if
     * no validation of the fields' value is performed.
     */
    public boolean isValidating()
    {
        if ( this.validating == null )
        {
            this.validating = this.isDefaultValidating();
        }

        return this.validating.booleanValue();
    }

    /**
     * Sets the flag indicating if validation should be performed.
     *
     * @param value {@code true} to validate the fields' values; {@code false}
     * to not validate the fields' values.
     */
    public void setValidating( boolean value )
    {
        this.validating = Boolean.valueOf( value );
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException for invalid property values.
     */
    private void assertValidProperties()
    {
        if ( this.getFormat() != Kontonummer.ELECTRONIC_FORMAT &&
            this.getFormat() != Kontonummer.LETTER_FORMAT )
        {
            throw new PropertyException( "format",
                                         Integer.toString( this.getFormat() ) );

        }
    }

    //----------------------------------------------------KontonummerTextField--
}
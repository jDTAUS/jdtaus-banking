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
package org.jdtaus.banking.util.swing;

import java.text.ParseException;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.JTextComponent;
import org.jdtaus.banking.Referenznummer10;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.container.Properties;
import org.jdtaus.core.container.Property;
import org.jdtaus.core.container.PropertyException;

/**
 * {@code JFormattedTextField} supporting the {@code Referenznummer10} type.
 * <p>This textfield uses the {@link Referenznummer10} type for parsing and
 * formatting. An empty string value is treated as {@code null}. Property
 * {@code format} controls formatting and takes one of the format constants
 * defined in class {@code Referenznummer10}. By default the
 * {@code ELECTRONIC_FORMAT} is used. The {@code validating} flag controls
 * validation of values entered into the textfield. If {@code true} (default),
 * an {@code InputVerifier} is registered with the textfield disallowing invalid
 * values, that is, values which are not {@code null} and not empty strings and
 * for which the {@link Referenznummer10#parse(String)} method throws a
 * {@code ParseException}.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class Referenznummer10TextField extends JFormattedTextField
{
    //--Constants---------------------------------------------------------------

    /** Serial version UID for backwards compatibility with 1.1.x classes. */
    private static final long serialVersionUID = -7966250999405224485L;

    //---------------------------------------------------------------Constants--
    //--Implementation----------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausImplementation
    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(Referenznummer10TextField.class.getName());
// </editor-fold>//GEN-END:jdtausImplementation

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausConstructors
    // This section is managed by jdtaus-container-mojo.

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

        p = meta.getProperty("format");
        this.pFormat = ((java.lang.Integer) p.getValue()).intValue();


        p = meta.getProperty("validating");
        this.pValidating = ((java.lang.Boolean) p.getValue()).booleanValue();

    }
// </editor-fold>//GEN-END:jdtausConstructors

    //------------------------------------------------------------Constructors--
    //--Dependencies------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
    // This section is managed by jdtaus-container-mojo.

// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
    //--Properties--------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausProperties
    // This section is managed by jdtaus-container-mojo.

    /**
     * Property {@code format}.
     * @serial
     */
    private int pFormat;

    /**
     * Gets the value of property <code>format</code>.
     *
     * @return the value of property <code>format</code>.
     */
    public int getFormat()
    {
        return this.pFormat;
    }

    /**
     * Property {@code validating}.
     * @serial
     */
    private boolean pValidating;

    /**
     * Gets the value of property <code>validating</code>.
     *
     * @return the value of property <code>validating</code>.
     */
    public boolean isValidating()
    {
        return this.pValidating;
    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
    //--Referenznummer10TextField-----------------------------------------------

    /** Creates a new default {@code Referenznummer10TextField} instance. */
    public Referenznummer10TextField()
    {
        super();

        this.initializeProperties( META.getProperties() );
        this.assertValidProperties();
        this.setColumns( Referenznummer10.MAX_CHARACTERS );
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
                                    Referenznummer10.parse( text );
                            }

                            return value;
                        }

                        public String valueToString( final Object value )
                            throws ParseException
                        {
                            String ret = null;

                            if ( value instanceof Referenznummer10 )
                            {
                                final Referenznummer10 ref =
                                    ( Referenznummer10 ) value;

                                ret = ref.format( getFormat() );
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
                            ( ( JTextComponent ) input ).getText();

                        if ( text != null &&
                            text.trim().length() > 0 )
                        {
                            try
                            {
                                Referenznummer10.parse( text );
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
     * Gets the last valid {@code Referenznummer10}.
     *
     * @return the last valid {@code Referenznummer10} or {@code null}.
     */
    public Referenznummer10 getReferenznummer10()
    {
        return ( Referenznummer10 ) this.getValue();
    }

    /**
     * Sets the constant of the format to use when formatting Referenznummer10
     * instances.
     *
     * @param format the constant of the format to use when formatting
     * Referenznummer10 instances.
     *
     * @throws IllegalArgumentException if {@code format} is neither
     * {@code ELECTRONIC_FORMAT} nor {@code LETTER_FORMAT}.
     *
     * @see Referenznummer10#ELECTRONIC_FORMAT
     * @see Referenznummer10#LETTER_FORMAT
     */
    public void setFormat( final int format )
    {
        if ( format != Referenznummer10.ELECTRONIC_FORMAT &&
            format != Referenznummer10.LETTER_FORMAT )
        {
            throw new IllegalArgumentException( Integer.toString( format ) );
        }

        this.pFormat = format;
    }

    /**
     * Sets the flag indicating if validation should be performed.
     *
     * @param validating {@code true} to validate the fields' values;
     * {@code false} to not validate the fields' values.
     */
    public void setValidating( boolean validating )
    {
        this.pValidating = validating;
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException for invalid property values.
     */
    private void assertValidProperties()
    {
        if ( this.getFormat() != Referenznummer10.ELECTRONIC_FORMAT &&
            this.getFormat() != Referenznummer10.LETTER_FORMAT )
        {
            throw new PropertyException( "format",
                                         Integer.toString( this.getFormat() ) );

        }
    }

    //-----------------------------------------------Referenznummer10TextField--
}

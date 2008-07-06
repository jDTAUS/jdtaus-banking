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
import org.jdtaus.banking.AlphaNumericText27;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.container.Properties;
import org.jdtaus.core.container.Property;

/**
 * {@code JFormattedTextField} supporting the {@code AlphaNumericText27} type.
 * <p>This textfield uses the {@link AlphaNumericText27} type for parsing and
 * formatting. An empty string value is treated as {@code null}. The
 * {@code normalizing} flag controls parsing. If {@code true} (default) the
 * field's value is normalized using the
 * {@link AlphaNumericText27#normalize(String)} method before parsing. The
 * {@code validating} flag controls validation of values entered into the
 * textfield. If {@code true} (default), an {@code InputVerifier} is registered
 * with the textfield disallowing invalid values, that is, values which are not
 * {@code null} and not empty strings and for which the
 * {@link AlphaNumericText27#parse(String)} method throws a
 * {@code ParseException}.</p>
 * <p><b>Note:</b><br/>
 * When using a normalizing parser, the field's value is never invalid and
 * the {@code validating} flag has no effect.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class AlphaNumericText27TextField extends JFormattedTextField
{
    //--Constants---------------------------------------------------------------

    /** Serial version UID for backwards compatibility with 1.1.x classes. */
    private static final long serialVersionUID = -8152767220100367519L;

    //---------------------------------------------------------------Constants--
    //--Implementation----------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausImplementation
    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(AlphaNumericText27TextField.class.getName());
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

        p = meta.getProperty("normalizing");
        this.pNormalizing = ((java.lang.Boolean) p.getValue()).booleanValue();


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
     * Property {@code normalizing}.
     * @serial
     */
    private boolean pNormalizing;

    /**
     * Gets the value of property <code>normalizing</code>.
     *
     * @return the value of property <code>normalizing</code>.
     */
    public boolean isNormalizing()
    {
        return this.pNormalizing;
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
    //--AlphaNumericText27TextField---------------------------------------------

    /** Creates a new default {@code AlphaNumericText27TextField} instance. */
    public AlphaNumericText27TextField()
    {
        super();
        this.initializeProperties( META.getProperties() );
        this.setColumns( AlphaNumericText27.MAX_LENGTH );
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
                                value = AlphaNumericText27.parse(
                                    isNormalizing()
                                    ? AlphaNumericText27.normalize( text )
                                    : text );

                            }

                            return value;
                        }

                        public String valueToString( final Object value )
                            throws ParseException
                        {
                            String ret = null;

                            if ( value instanceof AlphaNumericText27 )
                            {
                                final AlphaNumericText27 txt =
                                    ( AlphaNumericText27 ) value;

                                ret = txt.isEmpty()
                                    ? null
                                    : txt.format().trim();

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
                                AlphaNumericText27.parse(
                                    isNormalizing()
                                    ? AlphaNumericText27.normalize( text )
                                    : text );

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
     * Gets the last valid {@code AlphaNumericText27}.
     *
     * @return the last valid {@code AlphaNumericText27} or {@code null}.
     */
    public AlphaNumericText27 getAlphaNumericText27()
    {
        return ( AlphaNumericText27 ) this.getValue();
    }

    /**
     * Sets the flag indicating if a normalizing parser should be used.
     *
     * @param value {@code true} to use a normalizing parser; {@code false} to
     * use a strict parser (defaults to {@code true}).
     */
    public void setNormalizing( final boolean value )
    {
        this.pNormalizing = value;
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

    //---------------------------------------------AlphaNumericText27TextField--
}

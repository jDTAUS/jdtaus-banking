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
import org.jdtaus.banking.Kontonummer;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.container.Properties;
import org.jdtaus.core.container.Property;
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
    //--Implementation----------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausImplementation
    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(KontonummerTextField.class.getName());
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
        this._format = ((java.lang.Integer) p.getValue()).intValue();


        p = meta.getProperty("validating");
        this._validating = ((java.lang.Boolean) p.getValue()).booleanValue();

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
    private int _format;

    /**
     * Gets the value of property <code>format</code>.
     *
     * @return the value of property <code>format</code>.
     */
    public int getFormat()
    {
        return this._format;
    }

    /**
     * Property {@code validating}.
     * @serial
     */
    private boolean _validating;

    /**
     * Gets the value of property <code>validating</code>.
     *
     * @return the value of property <code>validating</code>.
     */
    public boolean isValidating()
    {
        return this._validating;
    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
    //--KontonummerTextField----------------------------------------------------

    /** Creates a new default {@code KontonummerTextField} instance. */
    public KontonummerTextField()
    {
        super();

        this.initializeProperties(META.getProperties());
        this.assertValidProperties();
        this.setColumns(Kontonummer.MAX_CHARACTERS);
        this.setFormatterFactory(new AbstractFormatterFactory()
        {
            public AbstractFormatter getFormatter(final JFormattedTextField ftf)
            {
                return new AbstractFormatter()
                {
                    public Object stringToValue(final String text)
                    throws ParseException
                    {
                        Object value = null;

                        if(text != null && text.trim().length() > 0)
                        {
                            value = Kontonummer.parse(text);
                        }

                        return value;
                    }

                    public String valueToString(final Object value)
                    throws ParseException
                    {
                        String ret = null;

                        if(value instanceof Kontonummer)
                        {
                            ret = ((Kontonummer) value).format(getFormat());
                        }

                        return ret;
                    }
                };
            }
        });

        this.setInputVerifier(new InputVerifier()
        {
            public boolean verify(final JComponent input)
            {
                boolean valid = true;

                if(isValidating() && input instanceof JTextComponent)
                {
                    final String text = ((JTextComponent) input).getText();

                    if(text != null && text.trim().length() > 0)
                    {
                        try
                        {
                            Kontonummer.parse(text);
                        }
                        catch(ParseException e)
                        {
                            valid = false;
                        }
                    }
                }

                return valid;
            }
        });
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
     * Sets the constant of the format to use when formatting Kontonummer
     * instances.
     *
     * @param format the constant of the format to use when formatting
     * Kontonummer instances.
     *
     * @throws IllegalArgumentException if {@code format} is neither
     * {@code ELECTRONIC_FORMAT} nor {@code LETTER_FORMAT}.
     *
     * @see Kontonummer#ELECTRONIC_FORMAT
     * @see Kontonummer#LETTER_FORMAT
     */
    public void setFormat(final int format)
    {
        if(format != Kontonummer.ELECTRONIC_FORMAT &&
            format != Kontonummer.LETTER_FORMAT)
        {
            throw new IllegalArgumentException(Integer.toString(format));
        }

        this._format = format;
    }

    /**
     * Sets the flag indicating if validation should be performed.
     *
     * @param validating {@code true} to validate the fields' values;
     * {@code false} to not validate the fields' values.
     */
    public void setValidating(boolean validating)
    {
        this._validating = validating;
    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException for invalid property values.
     */
    private void assertValidProperties()
    {
        if(this.getFormat() != Kontonummer.ELECTRONIC_FORMAT &&
            this.getFormat() != Kontonummer.LETTER_FORMAT)
        {
            throw new PropertyException("format",
                Integer.toString(this.getFormat()));

        }
    }

    //----------------------------------------------------KontonummerTextField--
}

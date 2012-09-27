/*
 *  jDTAUS Banking Utilities
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
package org.jdtaus.banking.util.swing;

import java.text.ParseException;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import org.jdtaus.banking.AlphaNumericText27;
import org.jdtaus.core.container.ContainerFactory;

/**
 * {@code JFormattedTextField} supporting the {@code AlphaNumericText27} type.
 * <p>This textfield uses the {@link AlphaNumericText27} type for parsing and formatting. An empty string value is
 * treated as {@code null}. The {@code normalizing} flag controls parsing. If {@code true} (default) the field's value
 * is normalized using the {@link AlphaNumericText27#normalize(String)} method prior to parsing. The {@code validating}
 * flag controls validation of values entered into the textfield. If {@code true} (default), a {@code DocumentFilter} is
 * registered with the textfield disallowing invalid values, that is, values which are not {@code null} and not empty
 * strings and for which the {@link AlphaNumericText27#parse(String)} method throws a {@code ParseException}.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public final class AlphaNumericText27TextField extends JFormattedTextField
{

    /** Serial version UID for backwards compatibility with 1.1.x classes. */
    private static final long serialVersionUID = -8152767220100367519L;

    /**
     * Flag indicating if a normalizing parser is used.
     * @serial
     */
    private Boolean normalizing;

    /**
     * Flag indicating if validation is performed.
     * @serial
     */
    private Boolean validating;

    /** Creates a new default {@code AlphaNumericText27TextField} instance. */
    public AlphaNumericText27TextField()
    {
        super();
        this.setColumns( AlphaNumericText27.MAX_LENGTH );
        this.setFormatterFactory( new AbstractFormatterFactory()
        {

            public AbstractFormatter getFormatter( final JFormattedTextField ftf )
            {
                return new AbstractFormatter()
                {

                    public Object stringToValue( final String text ) throws ParseException
                    {
                        Object value = null;

                        if ( text != null && text.trim().length() > 0 )
                        {
                            value = AlphaNumericText27.parse(
                                isNormalizing() ? AlphaNumericText27.normalize( text ) : text );

                        }

                        return value;
                    }

                    public String valueToString( final Object value ) throws ParseException
                    {
                        String ret = null;

                        if ( value instanceof AlphaNumericText27 )
                        {
                            final AlphaNumericText27 txt = (AlphaNumericText27) value;
                            ret = txt.isEmpty() ? null : txt.format().trim();
                        }

                        return ret;
                    }

                    protected DocumentFilter getDocumentFilter()
                    {
                        return new DocumentFilter()
                        {

                            public void insertString( final FilterBypass fb, final int o, String s,
                                                      final AttributeSet a ) throws BadLocationException
                            {
                                if ( isValidating() )
                                {
                                    if ( isNormalizing() )
                                    {
                                        final char[] chars = s.toCharArray();
                                        for ( int i = chars.length - 1; i >= 0; i-- )
                                        {
                                            chars[i] = Character.toUpperCase( chars[i] );
                                        }
                                        s = String.valueOf( chars );
                                    }

                                    final StringBuffer b =
                                        new StringBuffer( fb.getDocument().getLength() + s.length() );

                                    b.append( fb.getDocument().getText( 0, fb.getDocument().getLength() ) );
                                    b.insert( o, s );

                                    try
                                    {
                                        AlphaNumericText27.parse( b.toString() );
                                    }
                                    catch ( ParseException e )
                                    {
                                        invalidEdit();
                                        return;
                                    }
                                }

                                super.insertString( fb, o, s, a );
                            }

                            public void replace( final FilterBypass fb, final int o, final int l, String s,
                                                 final AttributeSet a ) throws BadLocationException
                            {
                                if ( isValidating() )
                                {
                                    if ( isNormalizing() )
                                    {
                                        final char[] chars = s.toCharArray();
                                        for ( int i = chars.length - 1; i >= 0; i-- )
                                        {
                                            chars[i] = Character.toUpperCase( chars[i] );
                                        }
                                        s = String.valueOf( chars );
                                    }

                                    final StringBuffer b =
                                        new StringBuffer( fb.getDocument().getLength() + s.length() );

                                    b.append( fb.getDocument().getText( 0, fb.getDocument().getLength() ) );
                                    b.replace( o, o + s.length(), s );

                                    try
                                    {
                                        AlphaNumericText27.parse( b.toString() );
                                    }
                                    catch ( ParseException e )
                                    {
                                        invalidEdit();
                                        return;
                                    }
                                }

                                super.replace( fb, o, l, s, a );
                            }

                        };
                    }

                };
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
        return (AlphaNumericText27) this.getValue();
    }

    /**
     * Gets the flag indicating if a normalizing parser is used.
     *
     * @return {@code true} if a normalizing parser is used; {@code false} if a strict parser is used
     * (defaults to {@code true}).
     */
    public boolean isNormalizing()
    {
        if ( this.normalizing == null )
        {
            this.normalizing = this.isDefaultNormalizing();
        }

        return this.normalizing.booleanValue();
    }

    /**
     * Sets the flag indicating if a normalizing parser should be used.
     *
     * @param value {@code true} to use a normalizing parser; {@code false} to use a strict parser
     * (defaults to {@code true}).
     */
    public void setNormalizing( final boolean value )
    {
        this.normalizing = Boolean.valueOf( value );
    }

    /**
     * Gets the flag indicating if validation is performed.
     *
     * @return {@code true} if the field's value is validated; {@code false} if no validation of the field's value is
     * performed.
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
     * @param value {@code true} to validate the field's value; {@code false} to not validate the field's value.
     */
    public void setValidating( boolean value )
    {
        this.validating = Boolean.valueOf( value );
    }

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
     * Gets the value of property <code>defaultNormalizing</code>.
     *
     * @return Default value of the flag indicating if a normalizing parser should be used.
     */
    private java.lang.Boolean isDefaultNormalizing()
    {
        return (java.lang.Boolean) ContainerFactory.getContainer().
            getProperty( this, "defaultNormalizing" );

    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
}

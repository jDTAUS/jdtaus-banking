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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.BankleitzahlExpirationException;
import org.jdtaus.banking.BankleitzahlInfo;
import org.jdtaus.banking.BankleitzahlenVerzeichnis;
import org.jdtaus.banking.messages.BankleitzahlExpirationMessage;
import org.jdtaus.banking.messages.BankleitzahlReplacementMessage;
import org.jdtaus.banking.messages.UnknownBankleitzahlMessage;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.PropertyException;

/**
 * {@code JFormattedTextField} supporting the {@code Bankleitzahl} type.
 * <p>This textfield uses the {@link Bankleitzahl} type for parsing and formatting. An empty string value is treated as
 * {@code null}. Property {@code format} controls formatting and takes one of the format constants defined in class
 * {@code Bankleitzahl}. By default the {@code ELECTRONIC_FORMAT} is used. The {@code validating} flag controls
 * validation of values entered into the textfield. If {@code true} (default), a {@code DocumentFilter} is registered
 * with the textfield disallowing invalid values, that is, values which are not {@code null} and not empty strings and
 * for which the {@link Bankleitzahl#parse(String)} method throws a {@code ParseException}. The field's tooltip
 * text is updated with information about the field's value.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public final class BankleitzahlTextField extends JFormattedTextField
{

    /** Serial version UID for backwards compatibility with 1.1.x classes. */
    private static final long serialVersionUID = -5461742987164339047L;

    /**
     * The constant of the format to use when formatting Bankleitzahl instances.
     * @serial
     */
    private Integer format;

    /**
     * Flag indicating if validation is performed.
     * @serial
     */
    private Boolean validating;

    /** Creates a new default {@code BankleitzahlTextField} instance. */
    public BankleitzahlTextField()
    {
        super();
        this.assertValidProperties();
        this.setColumns( Bankleitzahl.MAX_CHARACTERS );
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
                            value = Bankleitzahl.parse( text );
                        }

                        return value;
                    }

                    public String valueToString( final Object value ) throws ParseException
                    {
                        String ret = null;

                        if ( value instanceof Bankleitzahl )
                        {
                            final Bankleitzahl blz = (Bankleitzahl) value;
                            ret = blz.format( getFormat() );
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
                                    final StringBuffer b = new StringBuffer( fb.getDocument().getLength() + s.length() );
                                    b.append( fb.getDocument().getText( 0, fb.getDocument().getLength() ) );
                                    b.insert( o, s );

                                    try
                                    {
                                        Bankleitzahl.parse( b.toString() );
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
                                    final StringBuffer b = new StringBuffer( fb.getDocument().getLength() + s.length() );
                                    b.append( fb.getDocument().getText( 0, fb.getDocument().getLength() ) );
                                    b.replace( o, o + s.length(), s );

                                    try
                                    {
                                        Bankleitzahl.parse( b.toString() );
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

        this.addPropertyChangeListener( "value", new PropertyChangeListener()
        {

            public void propertyChange( final PropertyChangeEvent evt )
            {
                new Thread()
                {

                    public void run()
                    {
                        updateTooltip();
                    }

                }.start();
            }

        } );
    }

    /**
     * Gets the last valid {@code Bankleitzahl}.
     *
     * @return the last valid {@code Bankleitzahl} or {@code null}.
     */
    public Bankleitzahl getBankleitzahl()
    {
        return (Bankleitzahl) this.getValue();
    }

    /**
     * Gets the constant of the format used when formatting Bankleitzahl instances.
     *
     * @return the constant of the format used when formatting Bankleitzahl instances.
     *
     * @see Bankleitzahl#ELECTRONIC_FORMAT
     * @see Bankleitzahl#LETTER_FORMAT
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
     * Sets the constant of the format to use when formatting Bankleitzahl instances.
     *
     * @param value the constant of the format to use when formatting Bankleitzahl instances.
     *
     * @throws IllegalArgumentException if {@code format} is neither {@code ELECTRONIC_FORMAT} nor
     * {@code LETTER_FORMAT}.
     *
     * @see Bankleitzahl#ELECTRONIC_FORMAT
     * @see Bankleitzahl#LETTER_FORMAT
     */
    public void setFormat( final int value )
    {
        if ( value != Bankleitzahl.ELECTRONIC_FORMAT && value != Bankleitzahl.LETTER_FORMAT )
        {
            throw new IllegalArgumentException( Integer.toString( value ) );
        }

        this.format = new Integer( value );
    }

    /**
     * Gets the flag indicating if validation is performed.
     *
     * @return {@code true} if the fields' value is validated; {@code false} if no validation of the fields' value is
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
     * @param value {@code true} to validate the fields' values; {@code false} to not validate the fields' values.
     */
    public void setValidating( boolean value )
    {
        this.validating = Boolean.valueOf( value );
    }

    /**
     * Updates the component's tooltip to show information available for the value returned by
     * {@link #getBankleitzahl()}. This method is called whenever a {@code PropertyChangeEvent} for the property with
     * name {@code value} occurs.
     */
    private void updateTooltip()
    {
        final Bankleitzahl blz = this.getBankleitzahl();
        final StringBuffer tooltip = new StringBuffer( 200 );

        if ( blz != null )
        {
            tooltip.append( "<html>" );

            try
            {
                final BankleitzahlInfo headOffice = this.getBankleitzahlenVerzeichnis().getHeadOffice( blz );
                if ( headOffice != null )
                {
                    tooltip.append( "<b>" ).append( this.getHeadOfficeInfoMessage( this.getLocale() ) ).
                        append( "</b><br>" );

                    this.appendBankleitzahlInfo( headOffice, tooltip );
                }
                else
                {
                    tooltip.append( new UnknownBankleitzahlMessage( blz ).getText( this.getLocale() ) );
                }
            }
            catch ( BankleitzahlExpirationException e )
            {
                tooltip.append( new BankleitzahlExpirationMessage(
                    e.getExpiredBankleitzahlInfo() ).getText( this.getLocale() ) );

                tooltip.append( "<br>" ).append( new BankleitzahlReplacementMessage(
                    e.getReplacingBankleitzahlInfo() ).getText( this.getLocale() ) );

                tooltip.append( "<br>" );
                this.appendBankleitzahlInfo( e.getReplacingBankleitzahlInfo(), tooltip );
            }

            tooltip.append( "</html>" );
        }

        SwingUtilities.invokeLater( new Runnable()
        {

            public void run()
            {
                setToolTipText( tooltip.length() > 0 ? tooltip.toString() : null );
            }

        } );

    }

    /**
     * Checks configured properties.
     *
     * @throws PropertyException for invalid property values.
     */
    private void assertValidProperties()
    {
        if ( this.getFormat() != Bankleitzahl.ELECTRONIC_FORMAT && this.getFormat() != Bankleitzahl.LETTER_FORMAT )
        {
            throw new PropertyException( "format", Integer.toString( this.getFormat() ) );
        }
    }

    /**
     * Appends the tooltip text for a given {@code BankleitzahlInfo} to a given {@code StringBuffer}.
     *
     * @param bankleitzahlInfo The {@code BankleitzahlInfo} instance to append to {@code buf}.
     * @param buf The {@code StringBuffer} instance to append {@code bankleitzahlInfo} to.
     *
     * @return {@code buf} with information about {@code bankleitzahlInfo} appended.
     *
     * @throws NullPointerException if {@code bankleitzahlInfo} is {@code null}.
     */
    private StringBuffer appendBankleitzahlInfo( final BankleitzahlInfo bankleitzahlInfo, StringBuffer buf )
    {
        if ( bankleitzahlInfo == null )
        {
            throw new NullPointerException( "bankleitzahlInfo" );
        }
        if ( buf == null )
        {
            buf = new StringBuffer();
        }

        final NumberFormat zipFormat = new DecimalFormat( "#####" );
        buf.append( "<br>" ).append( bankleitzahlInfo.getName() );

        if ( bankleitzahlInfo.getDescription() != null && bankleitzahlInfo.getDescription().trim().length() > 0 &&
             !bankleitzahlInfo.getName().equals( bankleitzahlInfo.getDescription() ) )
        {
            buf.append( " (" ).append( bankleitzahlInfo.getDescription() ).append( ")" );
        }

        buf.append( "<br>" ).append( zipFormat.format( bankleitzahlInfo.getPostalCode() ) ).append( " " ).
            append( bankleitzahlInfo.getCity() );

        buf.append( "<br>" ).append( this.getBlzInfoMessage(
            this.getLocale(), bankleitzahlInfo.getBankCode().format( this.getFormat() ) ) );

        if ( bankleitzahlInfo.getBic() != null && bankleitzahlInfo.getBic().trim().length() > 0 )
        {
            buf.append( "<br>" ).append( this.getBicInfoMessage( this.getLocale(), bankleitzahlInfo.getBic() ) );
        }

        return buf;
    }

    //--Dependencies------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausDependencies
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the configured <code>BankleitzahlenVerzeichnis</code> implementation.
     *
     * @return The configured <code>BankleitzahlenVerzeichnis</code> implementation.
     */
    private BankleitzahlenVerzeichnis getBankleitzahlenVerzeichnis()
    {
        return (BankleitzahlenVerzeichnis) ContainerFactory.getContainer().
            getDependency( this, "BankleitzahlenVerzeichnis" );

    }

// </editor-fold>//GEN-END:jdtausDependencies

    //------------------------------------------------------------Dependencies--
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
     * @return Default value of the format to use when formatting Bankleitzahl instances (3001 = electronic format, 3002 letter format).
     */
    private java.lang.Integer getDefaultFormat()
    {
        return (java.lang.Integer) ContainerFactory.getContainer().
            getProperty( this, "defaultFormat" );

    }

// </editor-fold>//GEN-END:jdtausProperties

    //--------------------------------------------------------------Properties--
    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>blzInfo</code>.
     * <blockquote><pre>BLZ {0}</pre></blockquote>
     * <blockquote><pre>BLZ {0}</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param bankCode format parameter.
     *
     * @return the text of message <code>blzInfo</code>.
     */
    private String getBlzInfoMessage( final Locale locale,
            final java.lang.String bankCode )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "blzInfo", locale,
                new Object[]
                {
                    bankCode
                });

    }

    /**
     * Gets the text of message <code>bicInfo</code>.
     * <blockquote><pre>BIC {0}</pre></blockquote>
     * <blockquote><pre>BIC {0}</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param bic format parameter.
     *
     * @return the text of message <code>bicInfo</code>.
     */
    private String getBicInfoMessage( final Locale locale,
            final java.lang.String bic )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "bicInfo", locale,
                new Object[]
                {
                    bic
                });

    }

    /**
     * Gets the text of message <code>headOfficeInfo</code>.
     * <blockquote><pre>Hauptstelle</pre></blockquote>
     * <blockquote><pre>Headoffice</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     *
     * @return the text of message <code>headOfficeInfo</code>.
     */
    private String getHeadOfficeInfoMessage( final Locale locale )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "headOfficeInfo", locale, null );

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

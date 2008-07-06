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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import org.jdtaus.banking.Bankleitzahl;
import org.jdtaus.banking.BankleitzahlExpirationException;
import org.jdtaus.banking.BankleitzahlInfo;
import org.jdtaus.banking.BankleitzahlenVerzeichnis;
import org.jdtaus.banking.messages.BankleitzahlExpirationMessage;
import org.jdtaus.banking.messages.BankleitzahlReplacementMessage;
import org.jdtaus.banking.messages.UnknownBankleitzahlMessage;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.ContextFactory;
import org.jdtaus.core.container.ContextInitializer;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.container.Properties;
import org.jdtaus.core.container.Property;
import org.jdtaus.core.container.PropertyException;

/**
 * {@code JFormattedTextField} supporting the {@code Bankleitzahl} type.
 * <p>This textfield uses the {@link Bankleitzahl} type for parsing and
 * formatting. An empty string value is treated as {@code null}. Property
 * {@code format} controls formatting and takes one of the format constants
 * defined in class {@code Bankleitzahl}. By default the
 * {@code ELECTRONIC_FORMAT} is used. The {@code validating} flag controls
 * validation of values entered into the textfield. If {@code true} (default),
 * an {@code InputVerifier} is registered with the textfield disallowing invalid
 * values, that is, values which are not {@code null} and not empty strings and
 * for which the {@link Bankleitzahl#parse(String)} method throws a
 * {@code ParseException}. The current look and feel's
 * {@code provideErrorFeedback(Component)} method is called for unknown
 * Bankleitzahl values, and the field's tooltip text is updated with information
 * about the field's value.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class BankleitzahlTextField extends JFormattedTextField
{
    //--Constants---------------------------------------------------------------

    /** Serial version UID for backwards compatibility with 1.1.x classes. */
    private static final long serialVersionUID = -5461742987164339047L;

    //---------------------------------------------------------------Constants--
    //--Implementation----------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausImplementation
    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(BankleitzahlTextField.class.getName());
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

    /** Configured <code>BankleitzahlenVerzeichnis</code> implementation. */
    private transient BankleitzahlenVerzeichnis dBankleitzahlenVerzeichnis;

    /**
     * Gets the configured <code>BankleitzahlenVerzeichnis</code> implementation.
     *
     * @return the configured <code>BankleitzahlenVerzeichnis</code> implementation.
     */
    private BankleitzahlenVerzeichnis getBankleitzahlenVerzeichnis()
    {
        BankleitzahlenVerzeichnis ret = null;
        if(this.dBankleitzahlenVerzeichnis != null)
        {
            ret = this.dBankleitzahlenVerzeichnis;
        }
        else
        {
            ret = (BankleitzahlenVerzeichnis) ContainerFactory.getContainer().
                getDependency(BankleitzahlTextField.class,
                "BankleitzahlenVerzeichnis");

            if(ModelFactory.getModel().getModules().
                getImplementation(BankleitzahlTextField.class.getName()).
                getDependencies().getDependency("BankleitzahlenVerzeichnis").
                isBound())
            {
                this.dBankleitzahlenVerzeichnis = ret;
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
    //--BankleitzahlTextField---------------------------------------------------

    /** Creates a new default {@code BankleitzahlTextField} instance. */
    public BankleitzahlTextField()
    {
        super();

        this.initializeProperties( META.getProperties() );
        this.assertValidProperties();
        this.setColumns( Bankleitzahl.MAX_CHARACTERS );
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
                                value = Bankleitzahl.parse( text );
                            }

                            return value;
                        }

                        public String valueToString( final Object value )
                            throws ParseException
                        {
                            String ret = null;

                            if ( value instanceof Bankleitzahl )
                            {
                                final Bankleitzahl blz = ( Bankleitzahl ) value;
                                ret = blz.format( getFormat() );
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
                                Bankleitzahl.parse( text );
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

        this.addPropertyChangeListener(
            "value", new PropertyChangeListener()
        {

            public void propertyChange( final PropertyChangeEvent evt )
            {
                if ( isValidating() )
                {
                    new Thread()
                    {

                        public void run()
                        {
                            updateTooltip();
                        }

                        }.start();
                }
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
        return ( Bankleitzahl ) this.getValue();
    }

    /**
     * Sets the constant of the format to use when formatting Bankleitzahl
     * instances.
     *
     * @param format the constant of the format to use when formatting
     * Bankleitzahl instances.
     *
     * @throws IllegalArgumentException if {@code format} is neither
     * {@code ELECTRONIC_FORMAT} nor {@code LETTER_FORMAT}.
     *
     * @see Bankleitzahl#ELECTRONIC_FORMAT
     * @see Bankleitzahl#LETTER_FORMAT
     */
    public void setFormat( final int format )
    {
        if ( format != Bankleitzahl.ELECTRONIC_FORMAT &&
            format != Bankleitzahl.LETTER_FORMAT )
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
     * Updates the component's tooltip to show information available for the
     * value returned by {@link #getBankleitzahl()}. This method is called
     * whenever a {@code PropertyChangeEvent} for the property with name
     * {@code value} occurs.
     */
    private void updateTooltip()
    {
        final Bankleitzahl blz = this.getBankleitzahl();
        String tooltipText = null;

        try
        {
            if ( blz != null )
            {
                final BankleitzahlInfo headOffice =
                    this.getBankleitzahlenVerzeichnis().getHeadOffice( blz );

                final StringBuffer tooltip =
                    new StringBuffer( 200 ).append( "<html>" );

                if ( headOffice != null )
                {
                    final NumberFormat plzFmt =
                        new DecimalFormat( "00000" );

                    tooltip.append( "<b>" ).append( headOffice.getName() ).
                        append( "</b><br>" ).append( headOffice.getDescription() ).
                        append( "<br>" ).append(
                        plzFmt.format( headOffice.getPostalCode() ) ).
                        append( ' ' ).append( headOffice.getCity() );

                }
                else
                {
                    UIManager.getLookAndFeel().provideErrorFeedback( this );
                    tooltip.append(
                        new UnknownBankleitzahlMessage( blz ).getText(
                        Locale.getDefault() ) );

                }

                tooltipText = tooltip.append( "</html>" ).toString();
            }
        }
        catch ( BankleitzahlExpirationException e )
        {
            UIManager.getLookAndFeel().provideErrorFeedback( this );

            final StringBuffer tooltip =
                new StringBuffer( 200 ).append( "<html>" );
            tooltip.append( new BankleitzahlExpirationMessage(
                            e.getExpiredBankleitzahlInfo() ).getText(
                            Locale.getDefault() ) );

            tooltip.append( "<br>" );

            tooltip.append( new BankleitzahlReplacementMessage(
                            e.getReplacingBankleitzahlInfo() ).getText(
                            Locale.getDefault() ) );

            tooltip.append( "</html>" );
            tooltipText = tooltip.toString();
        }

        final String finalText = tooltipText;
        SwingUtilities.invokeLater(
            new Runnable()
            {

                public void run()
                {
                    setToolTipText( finalText );
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
        if ( this.getFormat() != Bankleitzahl.ELECTRONIC_FORMAT &&
            this.getFormat() != Bankleitzahl.LETTER_FORMAT )
        {
            throw new PropertyException( "format",
                                         Integer.toString( this.getFormat() ) );

        }
    }

    //---------------------------------------------------BankleitzahlTextField--
}

/*
 *  jDTAUS Banking Messages
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
package org.jdtaus.banking.messages;

import java.util.Locale;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.text.Message;

/**
 * Message stating that a field holds invalid data.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JDTAUS$
 */
public final class IllegalDataMessage extends Message
{

    /** Constant for DTAUS alphabet fields. */
    public static final int TYPE_ALPHA = 1;

    /** Constant for numeric DTAUS alphabet fields. */
    public static final int TYPE_NUMERIC = 2;

    /** Constant for alpha-numeric DTAUS alphabet fields. */
    public static final int TYPE_ALPHA_NUMERIC = 3;

    /** Constant for EBCDIC packed number fields. */
    public static final int TYPE_PACKET_POSITIVE = 4;

    /** Constant for well-known constant value fields. */
    public static final int TYPE_CONSTANT = 5;

    /** Constant for reserved fields. */
    public static final int TYPE_RESERVED = 6;

    /** Constant for two-digit year information date fields. */
    public static final int TYPE_SHORTDATE = 7;

    /** Constant for four-digit year information date fields. */
    public static final int TYPE_LONGDATE = 8;

    /** Constant for field 3 of an A record. */
    public static final int TYPE_FILETYPE = 9;

    /** Constant for {@code Bankleitzahl} fields. */
    public static final int TYPE_BANKLEITZAHL = 10;

    /**  Constant for {@code Kontonummer} fields. */
    public static final int TYPE_KONTONUMMER = 11;

    /**
     * Constant for either a {@code Referenznummer10} or
     * {@code Referenznummer11} fields.
     */
    public static final int TYPE_REFERENZNUMMER = 12;

    /** Constant for {@code Textschluessel} fields. */
    public static final int TYPE_TEXTSCHLUESSEL = 13;

    /** Constant for currency fields. */
    public static final int TYPE_CURRENCY = 14;

    /** All type constants. */
    private static final int[] TYPES =
    {
        TYPE_ALPHA, TYPE_NUMERIC, TYPE_ALPHA_NUMERIC, TYPE_PACKET_POSITIVE, TYPE_CONSTANT, TYPE_RESERVED,
        TYPE_SHORTDATE, TYPE_LONGDATE, TYPE_FILETYPE, TYPE_BANKLEITZAHL, TYPE_KONTONUMMER, TYPE_REFERENZNUMMER,
        TYPE_TEXTSCHLUESSEL, TYPE_CURRENCY
    };

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = 5634623930947949635L;

    /**
     * Constant of the field holding illegal data.
     * @serial
     */
    private final int field;

    /**
     * Absolute position of the field holding illegal data.
     * @serial
     */
    private final long position;

    /**
     * Constant for the type of the field holding illegal data.
     * @serial
     */
    private final int type;

    /**
     * The illegal data.
     * @serial
     */
    private final String invalidData;

    /**
     * Creates a new {@code IllegalDataMessage} instance taking information about the illegal data.
     *
     * @param field Constant for the field holding illegal data.
     * @param type Constant for the type of the field.
     * @param position Absolute position of the field holding illegal data.
     * @param invalidData The illegal data held in {@code field} at {@code position}.
     *
     * @throws IllegalArgumentException if {@code type} does not match one of {@link #TYPE_ALPHA TYPE_<i>XYZ</i>}
     * constants.
     *
     * @see org.jdtaus.banking.dtaus.spi.Fields
     */
    public IllegalDataMessage( final int field, final int type, final long position, final String invalidData )
    {
        super();
        this.field = field;
        this.type = type;
        this.position = position;
        this.invalidData = invalidData;
        this.assertValidType();
    }

    /**
     * Checks a given integer to match one of the {@code TYPE_<i>XYZ</i>} constants.
     *
     * @throws IllegalArgumentException if {@code type} does not match one of {@link #TYPE_ALPHA TYPE_<i>XYZ</i>}
     * constants.
     */
    private void assertValidType()
    {
        boolean valid = false;
        for ( int i = TYPES.length - 1; i >= 0 && !valid; i-- )
        {
            if ( TYPES[i] == this.type )
            {
                valid = true;
                break;
            }
        }

        if ( !valid )
        {
            throw new IllegalArgumentException( Integer.toString( this.type ) );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return A string describing the field holding illegal data, the absolute position of that field, and the illegal
     * data held by the field.
     * <ul>
     * <li>[0]: a string describing the field holding illegal data.</li>
     * <li>[1]: the absolute position of that field.</li>
     * <li>[2]: the invalid data.</li>
     * </ul>
     */
    public Object[] getFormatArguments( final Locale locale )
    {
        return new Object[]
            {
                Integer.toHexString( this.field ).toUpperCase( locale ),
                new Long( this.position ),
                this.invalidData
            };
    }

    /**
     * {@inheritDoc}
     *
     * @return The corresponding text from the message's {@code ResourceBundle}
     * <blockquote><pre>
     * "{2}" is no valid value for field {0} (at {1, number}).
     * </pre></blockquote>
     */
    public String getText( final Locale locale )
    {
        return this.getIllegalDataMessage(
            locale, Integer.toHexString( this.field ).toUpperCase(), new Long( this.position ), this.invalidData );

    }

    //--Messages----------------------------------------------------------------

// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:jdtausMessages
    // This section is managed by jdtaus-container-mojo.

    /**
     * Gets the text of message <code>illegalData</code>.
     * <blockquote><pre>"{2}" ist kein gültiger Wert für Feld {0} (Position {1, number}).</pre></blockquote>
     * <blockquote><pre>"{2}" is no valid value for field {0} (at {1, number}).</pre></blockquote>
     *
     * @param locale The locale of the message instance to return.
     * @param fld format argument.
     * @param pos format argument.
     * @param data format argument.
     *
     * @return the text of message <code>illegalData</code>.
     */
    private String getIllegalDataMessage( final Locale locale,
            final java.lang.String fld,
            final java.lang.Number pos,
            final java.lang.String data )
    {
        return ContainerFactory.getContainer().
            getMessage( this, "illegalData", locale,
                new Object[]
                {
                    fld,
                    pos,
                    data
                });

    }

// </editor-fold>//GEN-END:jdtausMessages

    //----------------------------------------------------------------Messages--
}

/*
 *  jDTAUS Banking API
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
package org.jdtaus.banking;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unique identifier to a particular office (branch) of a german bank.
 * <p>A Bankleitzahl (BLZ) is a positive integer with a maximum of eight
 * digits. For further information see the
 * <a href="../../../doc-files/Bankleitzahlen%20Richtlinie%20-%20Stand%208.%20September%202008.pdf">
 * Bankleitzahlen Richtlinie</a>. An updated version of the document may be
 * found at <a href="http://www.bundesbank.de/index.en.php">
 * Deutsche Bundesbank</a>.</p>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 *
 * @see BankleitzahlenVerzeichnis
 */
public final class Bankleitzahl extends Number implements Comparable
{
    //--Constants---------------------------------------------------------------

    /**
     * Constant for the electronic format of a Bankleitzahl.
     * <p>The electronic format of a Bankleitzahl is an eigth digit number with
     * zeros omitted (e.g. 5678).</p>
     */
    public static final int ELECTRONIC_FORMAT = 3001;

    /**
     * Constant for the letter format of a Bankleitzahl.
     * <p>The letter format of a Bankleitzahl is an eigth digit number with
     * zeros omitted separated by spaces between the first three digits and
     * the second three digits, and between the second three digits and the
     * last two digits (e.g. 123 456 78).</p>
     */
    public static final int LETTER_FORMAT = 3002;

    /** Maximum number of digits of a Bankleitzahl. */
    public static final int MAX_DIGITS = 8;

    /** Maximum number of characters of a Bankleitzahl. */
    public static final int MAX_CHARACTERS = 10;

    /** {@code 10^0..10^7} */
    private static final double[] EXP10 =
    {
        1, 10, 100, 1000, 10000, 100000, 1000000, 10000000
    };

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = -3329406998979147668L;

    //---------------------------------------------------------------Constants--
    //--Constructors------------------------------------------------------------

    /** Used to cache instances. */
    private static Reference cacheReference = new SoftReference( null );

    /**
     * Creates a new {@code Bankleitzahl} instance.
     *
     * @param bankCode the integer to create an instance from.
     *
     * @throws IllegalArgumentException if {@code bankCode} is negative, zero,
     * greater than 99999999 or its first digit is either zero or nine.
     *
     * @see #checkBankleitzahl(Number)
     */
    private Bankleitzahl( final Number bankCode )
    {
        if ( !Bankleitzahl.checkBankleitzahl( bankCode ) )
        {
            throw new IllegalArgumentException( bankCode.toString() );
        }

        final int[] digits = Bankleitzahl.toDigits( bankCode.longValue() );
        final long lCode = bankCode.longValue();

        this.clearingArea = digits[7];
        this.localityCode = (int) Math.floor( lCode / Bankleitzahl.EXP10[5] );
        this.networkCode = digits[4];
        this.instituteCode =
            (int) Math.floor( ( lCode - digits[7] * Bankleitzahl.EXP10[7] -
            digits[6] * Bankleitzahl.EXP10[6] - digits[5] *
            Bankleitzahl.EXP10[5] - digits[4] *
            Bankleitzahl.EXP10[4] ) );

        this.blz = bankCode.intValue();
    }

    /**
     * Parses text from a string to produce a {@code Bankleitzahl}.
     * <p>The method attempts to parse text starting at the index given by
     * {@code pos}. If parsing succeeds, then the index of {@code pos} is
     * updated to the index after the last character used
     * (parsing does not necessarily use all characters up to the end of the
     * string), and the parsed value is returned. The updated {@code pos}
     * can be used to indicate the starting point for the next call to this
     * method.</p>
     *
     * @param bankCode a Bankleitzahl in either electronic or letter format.
     * @param pos a {@code ParsePosition} object with index and error index
     * information as described above.
     *
     * @return the parsed value, or {@code null} if the parse fails.
     *
     * @throws NullPointerException if either {@code bankCode} or {@code pos} is
     * {@code null}.
     */
    public static Bankleitzahl parse(
        final String bankCode, final ParsePosition pos )
    {
        if ( bankCode == null )
        {
            throw new NullPointerException( "bankCode" );
        }
        if ( pos == null )
        {
            throw new NullPointerException( "pos" );
        }

        char c;
        Bankleitzahl ret = null;
        boolean sawSpace = false;
        boolean failed = false;

        final Number num;
        final ParsePosition fmtPos = new ParsePosition( 0 );
        final int len = bankCode.length();
        final int posIndex = pos.getIndex();
        final int maxIndex = posIndex + MAX_CHARACTERS;
        final StringBuffer digits = new StringBuffer( MAX_DIGITS );

        for ( int i = posIndex; i < len && i < maxIndex; i++ )
        {
            pos.setIndex( i );
            c = bankCode.charAt( i );

            if ( Character.isDigit( c ) )
            {
                digits.append( c );
                sawSpace = false;

                if ( digits.length() == MAX_DIGITS )
                {
                    break;
                }

            }
            else if ( c == ' ' )
            {
                if ( sawSpace )
                {
                    failed = true;
                }
                else
                {
                    sawSpace = true;
                }
            }
            else
            {
                failed = true;
            }

            if ( failed )
            {
                pos.setIndex( posIndex );
                pos.setErrorIndex( i );
                break;
            }
        }

        if ( !failed )
        {
            pos.setIndex( pos.getIndex() + 1 );
            num = new DecimalFormat( "########" ).parse( digits.toString(),
                fmtPos );

            if ( num != null && fmtPos.getErrorIndex() == -1 )
            {
                final String key = num.toString();
                ret = (Bankleitzahl) getCache().get( key );

                if ( ret == null )
                {
                    if ( !Bankleitzahl.checkBankleitzahl( num ) )
                    {
                        // Reset pos and indicate parsing error.
                        pos.setIndex( posIndex );
                        pos.setErrorIndex( posIndex );
                        ret = null;
                    }
                    else
                    {
                        ret = new Bankleitzahl( num );
                        getCache().put( key, ret );
                    }
                }
            }
            else
            {
                pos.setIndex( posIndex );
                pos.setErrorIndex( posIndex );
            }
        }

        return ret;
    }

    /**
     * Parses text from the beginning of the given string to produce a
     * {@code Bankleitzahl}.
     * <p>Unlike the {@link #parse(String, ParsePosition)} method this method
     * throws a {@code ParseException} if {@code bankCode} cannot be parsed or
     * is of invalid length.</p>
     *
     * @param bankCode a Bankleitzahl in either electronic or letter format.
     *
     * @return the parsed value.
     *
     * @throws NullPointerException if {@code bankCode} is {@code null}.
     * @throws ParseException if the parse fails or {@code bankCode} is of
     * invalid length.
     */
    public static Bankleitzahl parse( final String bankCode )
        throws ParseException
    {
        if ( bankCode == null )
        {
            throw new NullPointerException( "bankCode" );
        }

        Bankleitzahl blz = (Bankleitzahl) getCache().get( bankCode );

        if ( blz == null )
        {
            final ParsePosition pos = new ParsePosition( 0 );
            blz = Bankleitzahl.parse( bankCode, pos );

            if ( blz == null || pos.getErrorIndex() != -1 ||
                pos.getIndex() < bankCode.length() )
            {
                throw new ParseException( bankCode, pos.getErrorIndex() != -1
                    ? pos.getErrorIndex()
                    : pos.getIndex() );

            }
            else
            {
                getCache().put( bankCode, blz );
            }
        }

        return blz;
    }

    /**
     * Returns an instance for the Bankleitzahl identified by the given number.
     *
     * @param bankCode a number identifying a Bankleitzahl.
     *
     * @return an instance for {@code bankCode}.
     *
     * @throws NullPointerException if {@code bankCode} is {@code null}.
     * @throws IllegalArgumentException if {@code bankCode} is negative, zero,
     * greater than 99999999 or its first digit is either zero or nine.
     *
     * @see #checkBankleitzahl(Number)
     */
    public static Bankleitzahl valueOf( final Number bankCode )
    {
        if ( bankCode == null )
        {
            throw new NullPointerException( "bankCode" );
        }

        final String key = bankCode.toString();
        Bankleitzahl ret = (Bankleitzahl) getCache().get( key );

        if ( ret == null )
        {
            ret = new Bankleitzahl( bankCode );
            getCache().put( key, ret );
        }

        return ret;
    }

    /**
     * Parses text from the beginning of the given string to produce a
     * {@code Bankleitzahl}.
     * <p>Unlike the {@link #parse(String)} method this method
     * throws an {@code IllegalArgumentException} if {@code bankCode} cannot
     * be parsed or is of invalid length.</p>
     *
     * @param bankCode a Bankleitzahl in either electronic or letter format.
     *
     * @return the parsed value.
     *
     * @throws NullPointerException if {@code bankCode} is {@code null}.
     * @throws IllegalArgumentException if the parse fails or {@code bankCode}
     * is of invalid length.
     */
    public static Bankleitzahl valueOf( final String bankCode )
    {
        try
        {
            return Bankleitzahl.parse( bankCode );
        }
        catch ( ParseException e )
        {
            throw new IllegalArgumentException( bankCode );
        }
    }

    /**
     * Checks a given number to conform to a Bankleitzahl.
     *
     * @param bankCode the number to check.
     *
     * @return {@code true} if {@code bankCode} is a valid Bankleitzahl;
     * {@code false} if not.
     */
    public static boolean checkBankleitzahl( final Number bankCode )
    {
        boolean valid = bankCode != null;

        if ( valid )
        {
            final long num = bankCode.longValue();
            final int[] digits = Bankleitzahl.toDigits( num );

            valid = num > 0L && num < 100000000L && digits[7] != 0 &&
                digits[7] != 9;

        }

        return valid;
    }

    //------------------------------------------------------------Constructors--
    //--Number------------------------------------------------------------------

    /**
     * Returns this Bankleitzahl as an int value.
     *
     * @return this Bankleitzahl as an int value.
     */
    public int intValue()
    {
        return this.blz;
    }

    /**
     * Returns this Bankleitzahl as a long value.
     *
     * @return this Bankleitzahl as a long value.
     */
    public long longValue()
    {
        return this.blz;
    }

    /**
     * Returns this Bankleitzahl as a float value.
     *
     * @return this Bankleitzahl as a float value.
     */
    public float floatValue()
    {
        return this.blz;
    }

    /**
     * Returns this Bankleitzahl as a double value.
     *
     * @return this Bankleitzahl as a double value.
     */
    public double doubleValue()
    {
        return this.blz;
    }

    //------------------------------------------------------------------Number--
    //--Bankleitzahl------------------------------------------------------------

    /**
     * German bank code.
     * @serial
     */
    private int blz;

    /**
     * Clearing area code of this Bankleitzahl.
     * @serial
     */
    private int clearingArea;

    /**
     * Locality code of this Bankleitzahl.
     * @serial
     */
    private int localityCode;

    /**
     * Network code of this Bankleitzahl.
     * @serial
     */
    private int networkCode;

    /**
     * Institute code of this Bankleitzahl.
     * @serial
     */
    private int instituteCode;

    /**
     * Gets the clearing area code of this Bankleitzahl.
     * <p><ol>
     * <li>Berlin, Brandenburg, Mecklenburg-Vorpommern</li>
     * <li>Bremen, Hamburg, Niedersachsen, Schleswig-Holstein</li>
     * <li>Rheinland (Regierungsbezirke Düsseldorf, Köln)</li>
     * <li>Westfalen</li>
     * <li>Hessen, Rheinland-Pfalz, Saarland</li>
     * <li>Baden-Württemberg</li>
     * <li>Bayern</li>
     * <li>Sachsen, Sachsen-Anhalt, Thüringen</li>
     * </ol></p>
     *
     * @return code identifying the clearing area of this Bankleitzahl.
     */
    public int getClearingArea()
    {
        return this.clearingArea;
    }

    /**
     * Gets the locality code of this Bankleitzahl.
     *
     * @return locality code of this Bankleitzahl.
     */
    public int getLocalityCode()
    {
        return this.localityCode;
    }

    /**
     * Gets the network code of this Bankleitzahl.
     * <p><table border="0">
     * <tr>
     *   <td>0</td>
     *   <td>Deutsche Bundesbank</td>
     * </tr>
     * <tr>
     *   <td>1 - 3</td>
     *   <td>
     * Kreditinstitute, soweit nicht in einer der anderen Gruppen erfasst
     *   </td>
     * </tr>
     * <tr>
     *   <td>4</td>
     *   <td>Commerzbank</td>
     * </tr>
     * <tr>
     *   <td>5</td>
     *   <td>Girozentralen und Sparkassen</td>
     * </tr>
     * <tr>
     *   <td>6 + 9</td>
     *   <td>
     * Genossenschaftliche Zentralbanken, Kreditgenossenschaften sowie ehemalige
     * Genossenschaften
     *   </td>
     * </tr>
     * <tr>
     *   <td>7</td>
     *   <td>Deutsche Bank</td>
     * </tr>
     * <tr>
     *   <td>8</td>
     *   <td>Dresdner Bank</td>
     * </tr>
     * </table></p>
     *
     * @return network code of this Bankleitzahl.
     */
    public int getNetworkCode()
    {
        return this.networkCode;
    }

    /**
     * Gets the institute code of this Bankleitzahl.
     *
     * @return institute code of this Bankleitzahl.
     */
    public int getInstituteCode()
    {
        return this.instituteCode;
    }

    /**
     * Formats a Bankleitzahl and appends the resulting text to the given string
     * buffer.
     *
     * @param style the style to use ({@code ELECTRONIC_FORMAT} or
     * {@code LETTER_FORMAT}).
     * @param toAppendTo the buffer to which the formatted text is to be
     * appended.
     *
     * @return the value passed in as {@code toAppendTo}.
     *
     * @throws NullPointerException if {@code toAppendTo} is {@code null}.
     * @throws IllegalArgumentException if {@code style} is neither
     * {@code ELECTRONIC_FORMAT} nor {@code LETTER_FORMAT}.
     *
     * @see #ELECTRONIC_FORMAT
     * @see #LETTER_FORMAT
     */
    public StringBuffer format( final int style,
        final StringBuffer toAppendTo )
    {
        if ( toAppendTo == null )
        {
            throw new NullPointerException( "toAppendTo" );
        }
        if ( style != Bankleitzahl.ELECTRONIC_FORMAT &&
            style != Bankleitzahl.LETTER_FORMAT )
        {

            throw new IllegalArgumentException( Integer.toString( style ) );
        }

        final int[] digits = Bankleitzahl.toDigits( this.blz );
        for ( int i = digits.length - 1, lastDigit = 0; i >= 0; i-- )
        {
            if ( digits[i] != 0 || lastDigit > 0 )
            {
                toAppendTo.append( digits[i] );
                lastDigit++;
            }

            if ( style == Bankleitzahl.LETTER_FORMAT &&
                ( lastDigit == 3 || lastDigit == 6 ) )
            {
                toAppendTo.append( ' ' );
            }
        }

        return toAppendTo;
    }

    /**
     * Formats a Bankleitzahl to produce a string. Same as
     * <blockquote>
     * {@link #format(int, StringBuffer) format<code>(style,
     *     new StringBuffer()).toString()</code>}
     * </blockquote>
     *
     * @param style the style to use ({@code ELECTRONIC_FORMAT} or
     * {@code LETTER_FORMAT}).
     *
     * @return the formatted string.
     *
     * @throws IllegalArgumentException if {@code style} is neither
     * {@code ELECTRONIC_FORMAT} nor {@code LETTER_FORMAT}.
     *
     * @see #ELECTRONIC_FORMAT
     * @see #LETTER_FORMAT
     */
    public String format( final int style )
    {
        return this.format( style, new StringBuffer() ).toString();
    }

    /**
     * Formats a Bankleitzahl to produce a string. Same as
     * <blockquote>
     * {@link #format(int) bankleitzahl.format(ELECTRONIC_FORMAT)}
     * </blockquote>
     *
     * @param bankleitzahl the {@code Bankleitzahl} instance to format.
     *
     * @return the formatted string.
     *
     * @throws NullPointerException if {@code bankleitzahl} is {@code null}.
     */
    public static String toString( final Bankleitzahl bankleitzahl )
    {
        if ( bankleitzahl == null )
        {
            throw new NullPointerException( "bankleitzahl" );
        }

        return bankleitzahl.format( ELECTRONIC_FORMAT );
    }

    /**
     * Creates an array holding the digits of {@code number}.
     *
     * @param number the number to return the digits for.
     *
     * @return an array holding the digits of {@code number}.
     */
    private static int[] toDigits( final long number )
    {
        int i;
        int j;
        int subst;
        final int[] ret = new int[ MAX_DIGITS ];

        for ( i = MAX_DIGITS - 1; i >= 0; i-- )
        {
            for ( j = i + 1  , subst = 0; j < MAX_DIGITS; j++ )
            {
                subst += ret[j] * EXP10[j];
            }
            ret[i] = (int) Math.floor( ( number - subst ) / EXP10[i] );
        }

        return ret;
    }

    /**
     * Creates a string representing the properties of the instance.
     *
     * @return a string representing the properties of the instance.
     */
    private String internalString()
    {
        return new StringBuffer( 500 ).append( '{' ).
            append( "blz=" ).append( this.blz ).
            append( ", clearingArea=" ).append( this.clearingArea ).
            append( ", instituteCode=" ).append( this.instituteCode ).
            append( ", localityCode=" ).append( this.localityCode ).
            append( ", networkCode=" ).append( this.networkCode ).
            append( '}' ).toString();

    }

    /**
     * Gets the current cache instance.
     *
     * @return current cache instance.
     */
    private static Map getCache()
    {
        Map cache = (Map) cacheReference.get();
        if ( cache == null )
        {
            cache = Collections.synchronizedMap( new HashMap( 1024 ) );
            cacheReference = new SoftReference( cache );
        }

        return cache;
    }

    //------------------------------------------------------------Bankleitzahl--
    //--Comparable--------------------------------------------------------------

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.<p>
     *
     * @param   o the Object to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
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
        if ( !( o instanceof Bankleitzahl ) )
        {
            throw new ClassCastException( o.getClass().getName() );
        }

        int result = 0;
        final Bankleitzahl that = (Bankleitzahl) o;

        if ( !this.equals( that ) )
        {
            result = this.blz > that.blz
                ? 1
                : -1;
        }

        return result;
    }

    //--------------------------------------------------------------Comparable--
    //--Object------------------------------------------------------------------

    /**
     * Indicates whether some other object is equal to this one.
     *
     * @param o the reference object with which to compare.
     *
     * @return {@code true} if this object is the same as {@code o};
     * {@code false} otherwise.
     */
    public boolean equals( final Object o )
    {
        boolean equal = o == this;

        if ( !equal && o instanceof Bankleitzahl )
        {
            equal = this.blz == ( (Bankleitzahl) o ).blz;
        }

        return equal;
    }

    /**
     * Returns a hash code value for this object.
     *
     * @return a hash code value for this object.
     */
    public int hashCode()
    {
        return this.blz;
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
}

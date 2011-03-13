/*
 *  jDTAUS Banking API
 *  Copyright (c) 2005 Christian Schulte
 *
 *  Christian Schulte, Haldener Strasse 72, 58095 Hagen, Germany
 *  <schulte2005@users.sourceforge.net> (+49 2331 3543887)
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
 * <p>A Bankleitzahl (BLZ) is a positive integer with a maximum of eight digits. For further information see the
 * <a href="../../../doc-files/zv_merkblatt_blz.pdf">Merkblatt Bankleitzahlendatei</a>. An updated version of the document
 * may be found at <a href="http://www.bundesbank.de/index.en.php">Deutsche Bundesbank</a>.</p>
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 *
 * @see BankleitzahlenVerzeichnis
 */
public final class Bankleitzahl extends Number implements Comparable
{

    /**
     * Constant for the electronic format of a Bankleitzahl.
     * <p>The electronic format of a Bankleitzahl is an eigth digit number with leading zeros omitted (e.g. 5678).</p>
     */
    public static final int ELECTRONIC_FORMAT = 3001;

    /**
     * Constant for the letter format of a Bankleitzahl.
     * <p>The letter format of a Bankleitzahl is an eigth digit number with leading zeros omitted separated by spaces
     * between the first three digits and the second three digits, and between the second three digits and the last two
     * digits (e.g. 123 456 78).</p>
     */
    public static final int LETTER_FORMAT = 3002;

    /** Maximum number of digits of a Bankleitzahl. */
    public static final int MAX_DIGITS = 8;

    /** Maximum number of characters of a Bankleitzahl. */
    public static final int MAX_CHARACTERS = 10;

    /** {@code 10^0..10^7}. */
    private static final double[] EXP10 =
    {
        1, 10, 100, 1000, 10000, 100000, 1000000, 10000000
    };

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = -3329406998979147668L;

    /** Used to cache instances. */
    private static volatile Reference cacheReference = new SoftReference( null );

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
     * Creates a new {@code Bankleitzahl} instance.
     *
     * @param bankCode The integer to create an instance from.
     *
     * @throws IllegalArgumentException if {@code bankCode} is negative, zero, greater than 99999999 or its first digit
     * is either zero or nine.
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
            (int) Math.floor( lCode - digits[7] * Bankleitzahl.EXP10[7] -
                              digits[6] * Bankleitzahl.EXP10[6] -
                              digits[5] * Bankleitzahl.EXP10[5] -
                              digits[4] * Bankleitzahl.EXP10[4] );

        this.blz = bankCode.intValue();
    }

    /**
     * Parses text from a string to produce a {@code Bankleitzahl}.
     * <p>The method attempts to parse text starting at the index given by {@code pos}. If parsing succeeds, then the
     * index of {@code pos} is updated to the index after the last character used (parsing does not necessarily use all
     * characters up to the end of the string), and the parsed value is returned. The updated {@code pos} can be used to
     * indicate the starting point for the next call to this method.</p>
     *
     * @param bankCode A Bankleitzahl in either electronic or letter format.
     * @param pos A {@code ParsePosition} object with index and error index information as described above.
     *
     * @return The parsed value, or {@code null} if the parse fails.
     *
     * @throws NullPointerException if either {@code bankCode} or {@code pos} is {@code null}.
     */
    public static Bankleitzahl parse( final String bankCode, final ParsePosition pos )
    {
        if ( bankCode == null )
        {
            throw new NullPointerException( "bankCode" );
        }
        if ( pos == null )
        {
            throw new NullPointerException( "pos" );
        }

        Bankleitzahl ret = null;
        boolean sawSpace = false;
        boolean failed = false;
        final ParsePosition fmtPos = new ParsePosition( 0 );
        final int len = bankCode.length();
        final int startIndex = pos.getIndex();
        final int maxIndex = startIndex + MAX_CHARACTERS;
        final StringBuffer digits = new StringBuffer( MAX_DIGITS );
        int mode = ELECTRONIC_FORMAT;
        int part = 0;
        int partStart = 0;
        int partEnd = 2;
        int digit = 0;
        int i = startIndex;

        for ( ; i < len && i < maxIndex && digits.length() < MAX_DIGITS; i++ )
        {
            final char c = bankCode.charAt( i );

            if ( Character.isDigit( c ) )
            {
                sawSpace = false;

                if ( mode == LETTER_FORMAT )
                {
                    if ( digit < partStart || digit > partEnd )
                    {
                        failed = true;
                    }
                    else
                    {
                        digits.append( c );
                    }
                }
                else
                {
                    digits.append( c );
                }

                digit++;
            }
            else if ( c == ' ' )
            {
                if ( sawSpace || i == startIndex || ( mode == ELECTRONIC_FORMAT && digit != 3 ) )
                {
                    failed = true;
                }
                else
                {
                    mode = LETTER_FORMAT;
                    switch ( part )
                    {
                        case 0:
                            partStart = 3;
                            partEnd = 5;
                            break;
                        case 1:
                            partStart = 6;
                            partEnd = 7;
                            break;
                        default:
                            failed = true;
                            break;
                    }
                    part++;

                    if ( digit < partStart || digit > partEnd )
                    {
                        failed = true;
                    }
                }

                sawSpace = true;
            }
            else
            {
                failed = true;
            }

            if ( failed )
            {
                pos.setErrorIndex( i );
                break;
            }
        }

        if ( !failed )
        {
            final Number num = new DecimalFormat( "########" ).parse( digits.toString(), fmtPos );

            if ( num != null && fmtPos.getErrorIndex() == -1 )
            {
                final String key = num.toString();
                ret = (Bankleitzahl) getCache().get( key );

                if ( ret == null )
                {
                    if ( !Bankleitzahl.checkBankleitzahl( num ) )
                    {
                        pos.setErrorIndex( startIndex );
                        ret = null;
                    }
                    else
                    {
                        pos.setIndex( i );
                        ret = new Bankleitzahl( num );
                        getCache().put( key, ret );
                    }
                }
                else
                {
                    pos.setIndex( i );
                }
            }
            else
            {
                pos.setErrorIndex( startIndex );
            }
        }

        return ret;
    }

    /**
     * Parses text from the beginning of the given string to produce a {@code Bankleitzahl}.
     * <p>Unlike the {@link #parse(String, ParsePosition)} method this method throws a {@code ParseException} if
     * {@code bankCode} cannot be parsed or is of invalid length.</p>
     *
     * @param bankCode A Bankleitzahl in either electronic or letter format.
     *
     * @return The parsed value.
     *
     * @throws NullPointerException if {@code bankCode} is {@code null}.
     * @throws ParseException if the parse fails or {@code bankCode} is of invalid length.
     */
    public static Bankleitzahl parse( final String bankCode ) throws ParseException
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

            if ( blz == null || pos.getErrorIndex() != -1 || pos.getIndex() < bankCode.length() )
            {
                throw new ParseException( bankCode, pos.getErrorIndex() != -1 ? pos.getErrorIndex() : pos.getIndex() );
            }
            else
            {
                getCache().put( bankCode, blz );
            }
        }

        return blz;
    }

    /**
     * Gets a {@code Bankleitzahl} for a given number.
     *
     * @param bankCode A number to get a {@code Bankleitzahl} for.
     *
     * @return An instance for {@code bankCode}.
     *
     * @throws NullPointerException if {@code bankCode} is {@code null}.
     * @throws IllegalArgumentException if {@code bankCode} is negative, zero, greater than 99999999 or its first digit
     * is either zero or nine.
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
     * Parses text from the beginning of the given string to produce a {@code Bankleitzahl}.
     * <p>Unlike the {@link #parse(String)} method this method throws an {@code IllegalArgumentException} if
     * {@code bankCode} cannot be parsed or is of invalid length.</p>
     *
     * @param bankCode A Bankleitzahl in either electronic or letter format.
     *
     * @return The parsed value.
     *
     * @throws NullPointerException if {@code bankCode} is {@code null}.
     * @throws IllegalArgumentException if the parse fails or {@code bankCode} is of invalid length.
     */
    public static Bankleitzahl valueOf( final String bankCode )
    {
        try
        {
            return Bankleitzahl.parse( bankCode );
        }
        catch ( final ParseException e )
        {
            throw (IllegalArgumentException) new IllegalArgumentException( bankCode ).initCause( e );
        }
    }

    /**
     * Checks a given number to conform to a Bankleitzahl.
     *
     * @param bankCode The number to check.
     *
     * @return {@code true} if {@code bankCode} is a valid Bankleitzahl; {@code false} if not.
     */
    public static boolean checkBankleitzahl( final Number bankCode )
    {
        boolean valid = bankCode != null;

        if ( valid )
        {
            final long num = bankCode.longValue();
            valid = num > 0L && num < 100000000L;
            if ( valid && num > 9999999 )
            {
                final int[] digits = Bankleitzahl.toDigits( num );
                valid = digits[7] != 0 && digits[7] != 9;
            }
        }

        return valid;
    }

    /**
     * Returns this Bankleitzahl as an int value.
     *
     * @return This Bankleitzahl as an int value.
     */
    public int intValue()
    {
        return this.blz;
    }

    /**
     * Returns this Bankleitzahl as a long value.
     *
     * @return This Bankleitzahl as a long value.
     */
    public long longValue()
    {
        return this.blz;
    }

    /**
     * Returns this Bankleitzahl as a float value.
     *
     * @return This Bankleitzahl as a float value.
     */
    public float floatValue()
    {
        return this.blz;
    }

    /**
     * Returns this Bankleitzahl as a double value.
     *
     * @return This Bankleitzahl as a double value.
     */
    public double doubleValue()
    {
        return this.blz;
    }

    /**
     * Gets a flag indicating that this Bankleitzahl provides a clearing area code.
     *
     * @return {@code true} if property {@code clearingAreaCode} is supported by this instance; {@code false} if
     * property {@code clearingAreaCode} is not supported by this instance.
     *
     * @see #getClearingAreaCode()
     */
    public boolean isClearingAreaCodeSupported()
    {
        return this.blz > 9999999;
    }

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
     * @return Code identifying the clearing area of this Bankleitzahl.
     *
     * @throws UnsupportedOperationException if this Bankleitzahl does not provide clearing area information.
     *
     * @see #isClearingAreaCodeSupported()
     *
     * @deprecated Renamed to {@link #getClearingAreaCode() }.
     */
    public int getClearingArea()
    {
        return this.getClearingAreaCode();
    }

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
     * @return Code identifying the clearing area of this Bankleitzahl.
     *
     * @throws UnsupportedOperationException if this Bankleitzahl does not provide clearing area information.
     *
     * @see #isClearingAreaCodeSupported()
     */
    public int getClearingAreaCode()
    {
        if ( !this.isClearingAreaCodeSupported() )
        {
            throw new UnsupportedOperationException();
        }

        return this.clearingArea;
    }

    /**
     * Gets a flag indicating that this Bankleitzahl provides a locality code.
     *
     * @return {@code true} if property {@code localityCode} is supported by this instance; {@code false} if property
     * {@code localityCode} is not supported by this instance.
     *
     * @see #getLocalityCode()
     */
    public boolean isLocalityCodeSupported()
    {
        return this.blz > 99999;
    }

    /**
     * Gets the locality code of this Bankleitzahl.
     *
     * @return Locality code of this Bankleitzahl.
     *
     * @throws UnsupportedOperationException if this Bankleitzahl does not provide a locality code.
     *
     * @see #isLocalityCodeSupported()
     */
    public int getLocalityCode()
    {
        if ( !this.isLocalityCodeSupported() )
        {
            throw new UnsupportedOperationException();
        }

        return this.localityCode;
    }

    /**
     * Gets a flag indicating that this Bankleitzahl provides a network code.
     *
     * @return {@code true} if property {@code networkCode} is supported by this instance; {@code false} if property
     * {@code networkCode} is not supported by this instance.
     *
     * @see #getNetworkCode()
     */
    public boolean isNetworkCodeSupported()
    {
        return this.blz > 9999;
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
     * @return Network code of this Bankleitzahl.
     *
     * @throws UnsupportedOperationException if this Bankleitzahl does not provide a network code.
     *
     * @see #isNetworkCodeSupported()
     */
    public int getNetworkCode()
    {
        if ( !this.isNetworkCodeSupported() )
        {
            throw new UnsupportedOperationException();
        }

        return this.networkCode;
    }

    /**
     * Gets the institute code of this Bankleitzahl.
     *
     * @return Institute code of this Bankleitzahl.
     */
    public int getInstituteCode()
    {
        return this.instituteCode;
    }

    /**
     * Formats a Bankleitzahl and appends the resulting text to the given string buffer.
     *
     * @param style The style to use ({@code ELECTRONIC_FORMAT} or {@code LETTER_FORMAT}).
     * @param toAppendTo The buffer to which the formatted text is to be appended.
     *
     * @return The value passed in as {@code toAppendTo}.
     *
     * @throws NullPointerException if {@code toAppendTo} is {@code null}.
     * @throws IllegalArgumentException if {@code style} is neither {@code ELECTRONIC_FORMAT} nor {@code LETTER_FORMAT}.
     *
     * @see #ELECTRONIC_FORMAT
     * @see #LETTER_FORMAT
     */
    public StringBuffer format( final int style, final StringBuffer toAppendTo )
    {
        if ( toAppendTo == null )
        {
            throw new NullPointerException( "toAppendTo" );
        }
        if ( style != Bankleitzahl.ELECTRONIC_FORMAT && style != Bankleitzahl.LETTER_FORMAT )
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

            if ( style == Bankleitzahl.LETTER_FORMAT && ( lastDigit == 3 || lastDigit == 6 ) )
            {
                toAppendTo.append( ' ' );
            }
        }

        return toAppendTo;
    }

    /**
     * Formats a Bankleitzahl to produce a string. Same as
     * <blockquote>
     * {@link #format(int, StringBuffer) format<code>(style, new StringBuffer()).toString()</code>}
     * </blockquote>
     *
     * @param style The style to use ({@code ELECTRONIC_FORMAT} or {@code LETTER_FORMAT}).
     *
     * @return The formatted string.
     *
     * @throws IllegalArgumentException if {@code style} is neither {@code ELECTRONIC_FORMAT} nor {@code LETTER_FORMAT}.
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
     * @param bankleitzahl The {@code Bankleitzahl} instance to format.
     *
     * @return The formatted string.
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
     * @param number The number to return the digits for.
     *
     * @return An array holding the digits of {@code number}.
     */
    private static int[] toDigits( final long number )
    {
        int i;
        int j;
        int subst;
        final int[] ret = new int[ MAX_DIGITS ];

        for ( i = MAX_DIGITS - 1; i >= 0; i-- )
        {
            for ( j = i + 1, subst = 0; j < MAX_DIGITS; j++ )
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
     * @return A string representing the properties of the instance.
     */
    private String internalString()
    {
        return new StringBuffer( 500 ).append( '{' ).
            append( "blz=" ).append( this.blz ).
            append( ", clearingAreaCodeSupported=" ).
            append( this.isClearingAreaCodeSupported() ).
            append( ", clearingArea=" ).append( this.clearingArea ).
            append( ", instituteCode=" ).append( this.instituteCode ).
            append( ", localityCodeSupported=" ).
            append( this.isLocalityCodeSupported() ).
            append( ", localityCode=" ).append( this.localityCode ).
            append( ", networkCodeSupported=" ).
            append( this.isNetworkCodeSupported() ).
            append( ", networkCode=" ).append( this.networkCode ).
            append( '}' ).toString();

    }

    /**
     * Gets the current cache instance.
     *
     * @return Current cache instance.
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

    /**
     * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
     * as this object is less than, equal to, or greater than the specified object.<p>
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

    /**
     * Indicates whether some other object is equal to this one.
     *
     * @param o The reference object with which to compare.
     *
     * @return {@code true} if this object is the same as {@code o}; {@code false} otherwise.
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
     * @return A hash code value for this object.
     */
    public int hashCode()
    {
        return this.blz;
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

}

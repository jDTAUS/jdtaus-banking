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
 * Unique entity identifier.
 * <p>A Referenznummer11 is a positive integer with a maximum of eleven
 * digits.</p>
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public final class Referenznummer11 extends Number implements Comparable
{
    //--Constants---------------------------------------------------------------

    /**
     * Constant for the electronic format of a Referenznummer11.
     * <p>The electronic format of a Referenznummer11 is an eleven digit number
     * with zeros omitted (e.g. 6789).</p>
     */
    public static final int ELECTRONIC_FORMAT = 6001;

    /**
     * Constant for the letter format of a Referenznummer11.
     * <p>The letter format of a Referenznummer11 is an eleven digit number with
     * zeros omitted separated by spaces between the first three digits and
     * the second three digits, the second three digits and the
     * third three digits, and between the third three digits and the last
     * two digits (e.g. 123 456 789 01).</p>
     */
    public static final int LETTER_FORMAT = 6002;

    /** Maximum number of digits of a Referenznummer11. */
    public static final int MAX_DIGITS = 11;

    /** Maximum number of characters of a Referenznummer11. */
    public static final int MAX_CHARACTERS = 14;

    /** {@code 10^0..10^10} */
    private static final double[] EXP10 =
    {
        1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000,
        1000000000, 10000000000L
    };

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = -6729406850413828467L;

    //---------------------------------------------------------------Constants--
    //--Constructors------------------------------------------------------------

    /** Used to cache instances. */
    private static Reference cacheReference = new SoftReference( null );

    /**
     * Creates a new {@code Referenznummer11} instance.
     *
     * @param referenceCode the long to create an instance from.
     *
     * @throws IllegalArgumentException if {@code referenceCode} is negative,
     * zero or greater than 99999999999.
     *
     * @see #checkReferenznummer11(Number)
     */
    private Referenznummer11( final Number referenceCode )
    {
        if ( !Referenznummer11.checkReferenznummer11( referenceCode ) )
        {
            throw new IllegalArgumentException( referenceCode.toString() );
        }

        this.ref = referenceCode.longValue();
    }

    /**
     * Parses text from a string to produce a {@code Referenznummer11}.
     * <p>The method attempts to parse text starting at the index given by
     * {@code pos}. If parsing succeeds, then the index of {@code pos} is
     * updated to the index after the last character used
     * (parsing does not necessarily use all characters up to the end of the
     * string), and the parsed value is returned. The updated {@code pos}
     * can be used to indicate the starting point for the next call to this
     * method.</p>
     *
     * @param referenceCode a Referenznummer11 in either electronic or letter
     * format.
     * @param pos a {@code ParsePosition} object with index and error index
     * information as described above.
     *
     * @return the parsed value, or {@code null} if the parse fails.
     *
     * @throws NullPointerException if either {@code referenceCode} or
     * {@code pos} is {@code null}.
     */
    public static Referenznummer11 parse(
        final String referenceCode, final ParsePosition pos )
    {
        if ( referenceCode == null )
        {
            throw new NullPointerException( "referenceCode" );
        }
        if ( pos == null )
        {
            throw new NullPointerException( "pos" );
        }
        char c;
        Referenznummer11 ret = null;
        boolean sawSpace = false;
        boolean failed = false;

        final Number num;
        final ParsePosition fmtPos = new ParsePosition( 0 );
        final int len = referenceCode.length();
        final int posIndex = pos.getIndex();
        final int maxIndex = posIndex + MAX_CHARACTERS;
        final StringBuffer digits = new StringBuffer( MAX_DIGITS );

        for ( int i = posIndex; i < len && i < maxIndex; i++ )
        {
            pos.setIndex( i );
            c = referenceCode.charAt( i );

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
            num = new DecimalFormat( "###########" ).parse( digits.toString(),
                fmtPos );

            if ( num != null && fmtPos.getErrorIndex() == -1 )
            {
                final String key = num.toString();
                ret = (Referenznummer11) getCache().get( key );

                if ( ret == null )
                {
                    if ( !Referenznummer11.checkReferenznummer11( num ) )
                    {
                        // Reset pos and indicate parsing error.
                        pos.setIndex( posIndex );
                        pos.setErrorIndex( posIndex );
                        ret = null;
                    }
                    else
                    {
                        ret = new Referenznummer11( num );
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
     * {@code Referenznummer11}.
     * <p>Unlike the {@link #parse(String, ParsePosition)} method this method
     * throws a {@code ParseException} if {@code referenceCode} cannot be
     * parsed or is of invalid length.</p>
     *
     * @param referenceCode a Referenznummer11 in either electronic or letter
     * format.
     *
     * @return the parsed value.
     *
     * @throws NullPointerException if {@code referenceCode} is {@code null}.
     * @throws ParseException if the parse fails or {@code referenceCode} is of
     * invalid length.
     */
    public static Referenznummer11 parse( final String referenceCode )
        throws ParseException
    {
        if ( referenceCode == null )
        {
            throw new NullPointerException( "referenceCode" );
        }

        Referenznummer11 ref =
            (Referenznummer11) getCache().get( referenceCode );

        if ( ref == null )
        {
            final ParsePosition pos = new ParsePosition( 0 );
            ref = Referenznummer11.parse( referenceCode, pos );
            if ( ref == null || pos.getErrorIndex() != -1 ||
                pos.getIndex() < referenceCode.length() )
            {
                throw new ParseException( referenceCode,
                    pos.getErrorIndex() != -1
                    ? pos.getErrorIndex()
                    : pos.getIndex() );

            }
            else
            {
                getCache().put( referenceCode, ref );
            }
        }

        return ref;
    }

    /**
     * Returns an instance for the Referenznummer11 identified by the given
     * number.
     *
     * @param referenceCode a number identifying a Referenznummer11.
     *
     * @return an instance for {@code referenceCode}.
     *
     * @throws IllegalArgumentException if {@code referenceCode} is negative,
     * zero or greater than 99999999999.
     *
     * @see #checkReferenznummer11(Number)
     */
    public static Referenznummer11 valueOf( final Number referenceCode )
    {
        final String key = referenceCode.toString();
        Referenznummer11 ret = (Referenznummer11) getCache().get( key );

        if ( ret == null )
        {
            ret = new Referenznummer11( referenceCode );
            getCache().put( key, ret );
        }

        return ret;
    }

    /**
     * Parses text from the beginning of the given string to produce a
     * {@code Referenznummer11}.
     * <p>Unlike the {@link #parse(String)} method this method throws an
     * {@code IllegalArgumentException} if {@code referenceCode} cannot be
     * parsed or is of invalid length.</p>
     *
     * @param referenceCode a Referenznummer11 in either electronic or letter
     * format.
     *
     * @return the parsed value.
     *
     * @throws NullPointerException if {@code referenceCode} is {@code null}.
     * @throws IllegalArgumentException if the parse fails or
     * {@code referenceCode} is of invalid length.
     */
    public static Referenznummer11 valueOf( final String referenceCode )
    {
        try
        {
            return Referenznummer11.parse( referenceCode );
        }
        catch ( ParseException e )
        {
            throw new IllegalArgumentException( referenceCode );
        }
    }

    /**
     * Checks a given number to conform to a Referenznummer11.
     *
     * @param referenceCode the number to check.
     *
     * @return {@code true} if {@code referenceCode} is a valid
     * Referenznummer11; {@code false} if not.
     */
    public static boolean checkReferenznummer11( final Number referenceCode )
    {
        boolean valid = referenceCode != null;

        if ( valid )
        {
            final long num = referenceCode.longValue();
            valid = num >= 0L && num < 100000000000L;
        }

        return valid;
    }

    //------------------------------------------------------------Constructors--
    //--Number------------------------------------------------------------------

    /**
     * Returns this Referenznummer11 as an int value.
     *
     * @return this Referenznummer11 as an int value.
     */
    public int intValue()
    {
        return (int) this.ref;
    }

    /**
     * Returns this Referenznummer11 as a long value.
     *
     * @return this Referenznummer11 as a long value.
     */
    public long longValue()
    {
        return this.ref;
    }

    /**
     * Returns this Referenznummer11 as a float value.
     *
     * @return this Referenznummer11 as a float value.
     */
    public float floatValue()
    {
        return this.ref;
    }

    /**
     * Returns this Referenznummer11 as a double value.
     *
     * @return this Referenznummer11 as a double value.
     */
    public double doubleValue()
    {
        return this.ref;
    }

    //------------------------------------------------------------------Number--
    //--Referenznummer11--------------------------------------------------------

    /**
     * Reference code.
     * @serial
     */
    private long ref;

    /**
     * Formats a Referenznummer11 and appends the resulting text to the given
     * string buffer.
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
    public StringBuffer format(
        final int style, final StringBuffer toAppendTo )
    {
        if ( toAppendTo == null )
        {
            throw new NullPointerException( "toAppendTo" );
        }
        if ( style != Referenznummer11.ELECTRONIC_FORMAT &&
            style != Referenznummer11.LETTER_FORMAT )
        {

            throw new IllegalArgumentException( Integer.toString( style ) );
        }

        if ( this.ref == 0L )
        {
            toAppendTo.append( '0' );
        }
        else
        {
            final int[] digits = Referenznummer11.toDigits( this.ref );
            for ( int i = digits.length - 1, lastDigit = 0; i >= 0; i-- )
            {
                if ( digits[i] != 0 || lastDigit > 0 )
                {
                    toAppendTo.append( digits[i] );
                    lastDigit++;
                }

                if ( style == Referenznummer11.LETTER_FORMAT &&
                    ( lastDigit == 3 || lastDigit == 6 || lastDigit == 9 ) )
                {
                    toAppendTo.append( ' ' );
                }
            }
        }

        return toAppendTo;
    }

    /**
     * Formats a Referenznummer11 to produce a string. Same as
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
     * Formats a Referenznummer11 to produce a string. Same as
     * <blockquote>
     * {@link #format(int) referenznummer11.format(ELECTRONIC_FORMAT)}
     * </blockquote>
     *
     * @param referenznummer11 the {@code Referenznummer11} instance to format.
     *
     * @return the formatted string.
     *
     * @throws NullPointerException if {@code referenznummer11} is {@code null}.
     */
    public static String toString( final Referenznummer11 referenznummer11 )
    {
        if ( referenznummer11 == null )
        {
            throw new NullPointerException( "referenznummer11" );
        }

        return referenznummer11.format( ELECTRONIC_FORMAT );
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
        long subst;
        final int[] ret = new int[ MAX_DIGITS ];

        for ( i = MAX_DIGITS - 1; i >= 0; i-- )
        {
            for ( j = i + 1, subst = 0L; j < MAX_DIGITS; j++ )
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
        return new StringBuffer( 500 ).append( "{referenceNumber=" ).
            append( this.ref ).append( '}' ).toString();

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

    //--------------------------------------------------------Referenznummer11--
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
        if ( !( o instanceof Referenznummer11 ) )
        {
            throw new ClassCastException( o.getClass().getName() );
        }

        int result = 0;
        final Referenznummer11 that = (Referenznummer11) o;

        if ( !this.equals( that ) )
        {
            result = this.ref > that.ref
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

        if ( !equal && o instanceof Referenznummer11 )
        {
            equal = this.ref == ( (Referenznummer11) o ).ref;
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
        return (int) ( this.ref ^ ( this.ref >>> 32 ) );
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

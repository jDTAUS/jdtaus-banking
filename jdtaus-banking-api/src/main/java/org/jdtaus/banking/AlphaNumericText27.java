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

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Alpha numeric text with a maximum length of twenty seven characters.
 * <p>Data type for the alpha-numeric DTAUS alphabet. For further information
 * see the <a href="../../../doc-files/Anlage%203%20-%20Spezifikation%20der%20Datenformate%20-%20Version%202.3%20Endfassung%20vom%2005.11.2008.pdf">
 * Spezifikation der Datenformate</a>. An updated version of the document may
 * be found at <a href="http://www.ebics-zka.de/english">
 * Zentraler Kreditausschuß</a>.</p>
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public final class AlphaNumericText27
    implements CharSequence, Comparable, Serializable
{
    //--Constants---------------------------------------------------------------

    /** Serial version UID for backwards compatibility with 1.0.x classes. */
    private static final long serialVersionUID = -5231830564347967536L;

    //---------------------------------------------------------------Constants--
    //--Constructors------------------------------------------------------------

    /** Used to cache instances. */
    private static Reference cacheReference = new SoftReference( null );

    /**
     * Creates a new {@code AlphaNumericText27} instance holding {@code text}.
     *
     * @param text The text for the instance.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws IllegalArgumentException if the length of {@code text} is greater
     * than {@code MAX_LENGTH}.
     *
     * @see #parse(String, ParsePosition)
     */
    private AlphaNumericText27( final String text )
    {
        if ( text == null )
        {
            throw new NullPointerException( "text" );
        }
        if ( text.length() > MAX_LENGTH )
        {
            throw new IllegalArgumentException( text );
        }

        this.text = text;
        this.empty = text.trim().length() == 0;
    }

    /**
     * Parses text from a string to produce an {@code AlphaNumericText27}
     * instance.
     * <p>The method attempts to parse text starting at the index given by
     * {@code pos}. If parsing succeeds, then the index of {@code pos} is
     * updated to the index after the last character used
     * (parsing does not necessarily use all characters up to the end of the
     * string), and the parsed value is returned. The updated {@code pos}
     * can be used to indicate the starting point for the next call to this
     * method.</p>
     *
     * @param text A string to parse alpha numeric characters from.
     * @param pos A {@code ParsePosition} object with index and error index
     * information as described above.
     *
     * @return The parsed value, or {@code null} if the parse fails.
     *
     * @throws NullPointerException if either {@code text} or {@code pos} is
     * {@code null}.
     */
    public static AlphaNumericText27 parse(
        final String text, final ParsePosition pos )
    {
        if ( text == null )
        {
            throw new NullPointerException( "text" );
        }
        if ( pos == null )
        {
            throw new NullPointerException( "pos" );
        }

        int i;
        boolean valid = true;
        AlphaNumericText27 ret = null;
        final int beginIndex = pos.getIndex();
        final int len = text.length();

        for ( i = beginIndex; i < beginIndex + MAX_LENGTH && i < len; i++ )
        {
            if ( !AlphaNumericText27.checkAlphaNumeric( text.charAt( i ) ) )
            {
                pos.setErrorIndex( i );
                valid = false;
                break;
            }
        }

        if ( valid )
        {
            pos.setIndex( i );
            ret = new AlphaNumericText27( text.substring( beginIndex, i ) );
        }

        return ret;
    }

    /**
     * Parses text from the beginning of the given string to produce an
     * {@code AlphaNumericText27} instance.
     * <p>Unlike the {@link #parse(String, ParsePosition)} method this method
     * throws a {@code ParseException} if {@code text} cannot be parsed or is of
     * invalid length.</p>
     *
     * @param text A string to parse alpha numeric characters from.
     *
     * @return The parsed value.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws ParseException if the parse fails or the length of {@code text}
     * is greater than {@code 27}.
     */
    public static AlphaNumericText27 parse( final String text )
        throws ParseException
    {
        if ( text == null )
        {
            throw new NullPointerException( "text" );
        }

        AlphaNumericText27 txt = (AlphaNumericText27) getCache().get( text );

        if ( txt == null )
        {
            final ParsePosition pos = new ParsePosition( 0 );
            txt = AlphaNumericText27.parse( text, pos );

            if ( txt == null || pos.getErrorIndex() != -1 ||
                 pos.getIndex() < text.length() )
            {
                throw new ParseException( text, pos.getErrorIndex() != -1
                                                ? pos.getErrorIndex()
                                                : pos.getIndex() );

            }
            else
            {
                getCache().put( text, txt );
            }
        }

        return txt;
    }

    /**
     * Parses text from the beginning of the given string to produce an
     * {@code AlphaNumericText27} instance.
     * <p>Unlike the {@link #parse(String)} method this method throws an
     * {@code IllegalArgumentException} if {@code text} cannot be parsed or is
     * of invalid length.</p>
     *
     * @param text A formatted string representation of an
     * {@code AlphaNumericText27} instance.
     *
     * @return The parsed value.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws IllegalArgumentException if the parse fails or the length of
     * {@code text} is greater than {@code 27}.
     */
    public static AlphaNumericText27 valueOf( final String text )
    {
        try
        {
            return AlphaNumericText27.parse( text );
        }
        catch ( ParseException e )
        {
            throw new IllegalArgumentException( text );
        }
    }

    //------------------------------------------------------------Constructors--
    //--AlphaNumericText27------------------------------------------------------

    /** Constant for the maximum length allowed for an instance. */
    public static final int MAX_LENGTH = 27;

    /**
     * The alpha-numeric text.
     * @serial
     */
    private String text;

    /**
     * Flag indicating that {@code text} contains no text.
     * @serial
     */
    private boolean empty;

    /**
     * Formats alpha-numeric characters and appends the resulting text to the
     * given string buffer.
     *
     * @param toAppendTo The buffer to which the formatted text is to be
     * appended.
     *
     * @return The value passed in as {@code toAppendTo}.
     *
     * @throws NullPointerException if {@code toAppendTo} is {@code null}.
     */
    public StringBuffer format( final StringBuffer toAppendTo )
    {
        if ( toAppendTo == null )
        {
            throw new NullPointerException( "toAppendTo" );
        }

        return toAppendTo.append( this.text );
    }

    /**
     * Formats alpha-numeric characters to produce a string. Same as
     * <blockquote>
     * {@link #format(StringBuffer) format<code>(new StringBuffer()).
     *     toString()</code>}
     * </blockquote>
     *
     * @return The formatted string.
     */
    public String format()
    {
        return this.text;
    }

    /**
     * Formats alpha-numeric characters to produce a string. Same as
     * <blockquote>
     * {@link #format() alphaNumericText27.format()}
     * </blockquote>
     *
     * @param alphaNumericText27 The {@code AlphaNumericText27} instance to
     * format.
     *
     * @return The formatted string.
     *
     * @throws NullPointerException if {@code alphaNumericText27} is
     * {@code null}.
     */
    public static String toString( final AlphaNumericText27 alphaNumericText27 )
    {
        if ( alphaNumericText27 == null )
        {
            throw new NullPointerException( "alphaNumericText27" );
        }

        return alphaNumericText27.format();
    }

    /**
     * Checks a given character to belong to the alpha-numeric alphabet.
     *
     * @param c The character to check.
     *
     * @return {@code true} if {@code c} is a character of the alpha-numeric
     * DTAUS alphabet; {@code false} if not.
     */
    public static boolean checkAlphaNumeric( final char c )
    {
        return ( c >= 'A' && c <= 'Z' ) || ( c >= '0' && c <= '9' ) ||
               ( c == '.' || c == '+' || c == '*' || c == '$' || c == ' ' ||
                 c == ',' || c == '&' || c == '-' || c == '/' || c == '%' ||
                 c == 'Ä' || c == 'Ö' || c == 'Ü' || c == 'ß' );

    }

    /**
     * Normalizes text to conform to the alpha-numeric alphabet.
     * <p>This method converts lower case letters to upper case letters and
     * replaces all illegal characters with spaces. It will return the unchanged
     * text if for every given character {@code C} the method
     * {@code checkAlphaNumeric(C)} returns {@code true}.</p>
     * <p>Note that code like
     * <blockquote><code>
     * AlphaNumericText27.parse(AlphaNumericText27.normalize(getSomething()));
     * </code></blockquote>
     * may be dangerous if {@code getSomething()} may provide unchecked or
     * otherwise invalid data which will get converted to valid data by this
     * method.</p>
     * <p>Also note that
     * <blockquote><code>
     * AlphaNumericText27.normalize(<i>something</i>).length() ==
     *<i>something</i>.length();
     * </code></blockquote>
     * is always {@code true} although
     * <blockquote><code>
     * AlphaNumericText27.normalize(<i>something</i>).equals(<i>something</i>);
     * </code></blockquote> may be {@code false}.</p>
     * <p>It is recommended to always check for changes
     * before proceeding with the data this method returns. For example:
     * <blockquote><pre>
     * something = getSomething();
     * normalized = AlphaNumericText27.normalize(something);
     * if(!something.equals(normalized)) {
     *
     *     <i>e.g. check the normalized value, log a warning, display the
     *     normalized value to the user for confirmation</i>
     *
     * }</pre></blockquote></p>
     *
     * @param text The text to normalize.
     *
     * @return {@code text} normalized to conform to the alpha-numeric alphabet.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    public static String normalize( final String text )
    {
        if ( text == null )
        {
            throw new NullPointerException( "text" );
        }

        final char[] ret = text.toCharArray();

        for ( int i = ret.length - 1; i >= 0; i-- )
        {
            if ( Character.isLowerCase( ret[i] ) )
            {
                ret[i] = Character.toUpperCase( ret[i] );
            }

            if ( !AlphaNumericText27.checkAlphaNumeric( ret[i] ) )
            {
                ret[i] = ' ';
            }
        }

        return new String( ret );
    }

    /**
     * Flag indicating that the instance contains no text.
     *
     * @return {@code true} if the instance contains no text but just whitespace
     * characters; {@code false} if the instance contains text.
     */
    public boolean isEmpty()
    {
        return this.empty;
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

    //------------------------------------------------------AlphaNumericText27--
    //--CharSequence------------------------------------------------------------

    /**
     * Returns the length of this character sequence.  The length is the number
     * of 16-bit {@code char}s in the sequence.
     *
     * @return The number of {@code char}s in this sequence.
     */
    public int length()
    {
        return this.text.length();
    }

    /**
     * Returns the {@code char} value at the specified index.
     * <p>An index ranges from zero to {@code length() - 1}. The first
     * {@code char} value of the sequence is at index zero, the next at index
     * one, and so on, as for array indexing.</p>
     *
     * @param index The index of the {@code char} value to be returned.
     *
     * @return The specified {@code char} value.
     *
     * @throws IndexOutOfBoundsException if {@code index} is negative or not
     * less than {@code length()}.
     */
    public char charAt( final int index )
    {
        return this.text.charAt( index );
    }

    /**
     * Returns a new {@code CharSequence} that is a subsequence of this
     * sequence.
     * <p>The subsequence starts with the {@code char} value at the specified
     * index and ends with the {@code char} value at index {@code end - 1}. The
     * length (in {@code char}s) of the returned sequence is
     * {@code end - start}, so if {@code start == end} then an empty sequence is
     * returned.</p>
     *
     * @param start The start index, inclusive.
     * @param end The end index, exclusive.
     *
     * @return The specified subsequence.
     *
     * @throws  IndexOutOfBoundsException if {@code start} or {@code end} are
     * negative, if {@code end} is greater than {@code length()}, or if
     * {@code start} is greater than {@code end}.
     */
    public CharSequence subSequence( final int start, final int end )
    {
        return this.text.subSequence( start, end );
    }

    /**
     * Returns a string containing the characters in this sequence in the same
     * order as this sequence.  The length of the string will be the length of
     * this sequence.
     *
     * @return A string consisting of exactly this sequence of characters.
     */
    public String toString()
    {
        return this.text;
    }

    //------------------------------------------------------------CharSequence--
    //--Comparable--------------------------------------------------------------

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.<p>
     *
     * @param o The Object to be compared.
     * @return A negative integer, zero, or a positive integer as this object
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
        if ( !( o instanceof AlphaNumericText27 ) )
        {
            throw new ClassCastException( o.getClass().getName() );
        }

        final AlphaNumericText27 that = (AlphaNumericText27) o;

        int result = 0;

        if ( !this.equals( o ) )
        {
            if ( this.text == null )
            {
                result = that.text == null
                         ? 0
                         : -1;
            }
            else
            {
                result = that.text == null
                         ? 1
                         : this.text.compareTo( that.text );
            }
        }

        return result;
    }

    //--------------------------------------------------------------Comparable--
    //--Object------------------------------------------------------------------

    /**
     * Indicates whether some other object is equal to this one.
     *
     * @param o The reference object with which to compare.
     *
     * @return {@code true} if this object is the same as {@code o};
     * {@code false} otherwise.
     */
    public boolean equals( final Object o )
    {
        boolean ret = o == this;

        if ( !ret && o instanceof AlphaNumericText27 )
        {
            final AlphaNumericText27 that = (AlphaNumericText27) o;
            ret = this.text == null
                  ? that.text == null
                  : this.text.equals( that.text );

        }

        return ret;
    }

    /**
     * Returns a hash code value for this object.
     *
     * @return A hash code value for this object.
     */
    public int hashCode()
    {
        return this.text == null
               ? 0
               : this.text.hashCode();
    }

    //------------------------------------------------------------------Object--
}

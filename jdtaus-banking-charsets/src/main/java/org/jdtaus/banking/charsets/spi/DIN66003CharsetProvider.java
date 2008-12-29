/*
 *  jDTAUS Banking Charset Providers
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
package org.jdtaus.banking.charsets.spi;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.spi.CharsetProvider;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * {@code CharsetProvider} for DIN-66003 Charset.
 * <p>
 * Name: DIN_66003<br>
 * MIBenum: 24<br>
 * Source: ECMA registry<br>
 * Alias: iso-ir-21<br>
 * Alias: de<br>
 * Alias: ISO646-DE<br>
 * Alias: csISO21German<br>
 * See: RFC1345, KXS2
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public class DIN66003CharsetProvider extends CharsetProvider
{
    //--Constants---------------------------------------------------------------

    /** Common name. */
    static final String COMMON_NAME = "DIN_66003";

    /** Alias names. */
    static final String[] ALIAS_NAMES =
    {
        "iso-ir-21", "de", "iso646-de", "csiso21german"
    };

    /** Supported character set names. */
    static final String[] SUPPORTED_NAMES =
    {
        DIN66003CharsetProvider.COMMON_NAME.toLowerCase(),
        "iso-ir-21", "de", "iso646-de", "csiso21german"
    };

    static final char[] DIN66003_TO_CHAR =
    {
        '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?',
        '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?',
        '?', '?', '?', '?', '?', ' ', '!', '"', '#', '$', '%', '&', '\'',
        '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4',
        '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '§', 'A',
        'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
        'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Ä',
        'Ö', 'Ü', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
        'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
        'v', 'w', 'x', 'y', 'z', 'ä', 'ö', 'ü', 'ß', '\u007F'
    };

    static final byte[] CHAR_TO_DIN66003 = new byte[ 0xFF ];

    //---------------------------------------------------------------Constants--
    //--Constructors------------------------------------------------------------

    static
    {
        byte i;

        Arrays.fill( CHAR_TO_DIN66003, (byte) 0xFF );
        for ( i = 32; i < 64; i++ )
        {
            CHAR_TO_DIN66003[i] = i;
        }

        for ( i = 65; i <= 90; i++ )
        {
            CHAR_TO_DIN66003[i] = i;
        }

        for ( i = 94; i <= 122; i++ )
        {
            CHAR_TO_DIN66003[i] = i;
        }

        CHAR_TO_DIN66003['§'] = (byte) 0x40;
        CHAR_TO_DIN66003['Ä'] = (byte) 0x5B;
        CHAR_TO_DIN66003['Ö'] = (byte) 0x5C;
        CHAR_TO_DIN66003['Ü'] = (byte) 0x5D;
        CHAR_TO_DIN66003['ä'] = (byte) 0x7B;
        CHAR_TO_DIN66003['ö'] = (byte) 0x7C;
        CHAR_TO_DIN66003['ü'] = (byte) 0x7D;
        CHAR_TO_DIN66003['ß'] = (byte) 0x7E;
        CHAR_TO_DIN66003['\u007F'] = (byte) 0x7F;
    }

    /** Creates a new {@code DIN66003CharsetProvider} instance. */
    public DIN66003CharsetProvider()
    {
        super();
    }

    //------------------------------------------------------------Constructors--
    //--CharsetProvider---------------------------------------------------------

    public Charset charsetForName( final String charsetName )
    {
        Charset ret = null;

        if ( charsetName != null )
        {
            final String lower = charsetName.toLowerCase();
            for ( int i = 0; i < SUPPORTED_NAMES.length; i++ )
            {
                if ( SUPPORTED_NAMES[i].equals( lower ) )
                {
                    ret = new DIN66003Charset();
                    break;
                }
            }
        }

        return ret;
    }

    public Iterator charsets()
    {
        return new Iterator()
        {

            private boolean hasNext = true;

            public boolean hasNext()
            {
                return this.hasNext;
            }

            public Object next()
            {
                if ( this.hasNext )
                {
                    this.hasNext = false;
                    return new DIN66003Charset();
                }
                else
                {
                    throw new NoSuchElementException();
                }
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }

        };
    }

    //---------------------------------------------------------CharsetProvider--
}

//--DIN66003Charset-------------------------------------------------------------

/** DIN-66003 {@code Charset} implementation. */
class DIN66003Charset extends Charset
{

    public DIN66003Charset()
    {
        super( DIN66003CharsetProvider.COMMON_NAME,
            DIN66003CharsetProvider.ALIAS_NAMES );

    }

    public CharsetEncoder newEncoder()
    {
        return new DIN66003CharsetEncoder( this );
    }

    public CharsetDecoder newDecoder()
    {
        return new DIN66003CharsetDecoder( this );
    }

    public boolean contains( final Charset charset )
    {
        return false;
    }

    static boolean isCharacterSupported( final char c )
    {
        return ( c >= 32 && c < 64 ) || ( c >= 65 && c <= 90 ) ||
            ( c >= 94 && c <= 122 ) || c == '§' || c == 'Ä' || c == 'Ö' ||
            c == 'Ü' || c == 'ä' || c == 'ö' || c == 'ü' || c == 'ß';

    }

}

//-------------------------------------------------------------DIN66003Charset--
//--DIN66003CharsetEncoder------------------------------------------------------

class DIN66003CharsetEncoder extends CharsetEncoder
{

    private final char[] charBuf = new char[ 65536 ];

    DIN66003CharsetEncoder( final Charset charset )
    {
        super( charset, 1f, 1f );
        this.onUnmappableCharacter( CodingErrorAction.REPLACE );
    }

    protected CoderResult encodeLoop( final CharBuffer in,
        final ByteBuffer buf )
    {

        if ( in.hasArray() && buf.hasArray() )
        {
            return this.encodeLoopArray( in, buf );
        }

        int inRemaining;
        final int inPosition = in.position();
        int i;
        int inLen;

        while ( in.hasRemaining() )
        {
            inRemaining = in.remaining();
            if ( inRemaining < this.charBuf.length )
            {
                in.get( this.charBuf, 0, inRemaining );
                inLen = inRemaining;
            }
            else
            {
                in.get( this.charBuf, 0, this.charBuf.length );
                inLen = this.charBuf.length;
            }
            for ( i = 0; i < inLen; i++ )
            {
                if ( !buf.hasRemaining() )
                {
                    return CoderResult.OVERFLOW;
                }

                if ( !DIN66003Charset.isCharacterSupported( this.charBuf[i] ) )
                {
                    in.position( inPosition + i );
                    return CoderResult.unmappableForLength( 1 );
                }
                buf.put(
                    DIN66003CharsetProvider.CHAR_TO_DIN66003[this.charBuf[i]] );

            }
        }

        return CoderResult.UNDERFLOW;
    }

    private CoderResult encodeLoopArray( final CharBuffer in,
        final ByteBuffer buf )
    {
        final char[] inArray = in.array();
        final int inOffset = in.arrayOffset();
        final byte[] outArray = buf.array();
        final int outOffset = buf.arrayOffset();
        final int inPosition = in.position();
        final int outPosition = buf.position();
        int inRemaining;
        while ( ( inRemaining = in.remaining() ) > 0 )
        {
            if ( buf.remaining() < inRemaining )
            {
                return CoderResult.OVERFLOW;
            }

            for ( int i = 0; i < inRemaining; i++ )
            {
                final int inIndex = inPosition + inOffset + i;
                final int outIndex = outPosition + outOffset + i;
                if ( !DIN66003Charset.isCharacterSupported( inArray[inIndex] ) )
                {
                    in.position( inPosition + i );
                    buf.position( outPosition + i );
                    return CoderResult.unmappableForLength( 1 );
                }
                else
                {
                    outArray[outIndex] =
                    DIN66003CharsetProvider.CHAR_TO_DIN66003[inArray[inIndex]];

                }
            }
            in.position( inPosition + inRemaining );
            buf.position( outPosition + inRemaining );
        }

        return CoderResult.UNDERFLOW;
    }

}

//------------------------------------------------------DIN66003CharsetEncoder--
//--DIN66003CharsetDecoder------------------------------------------------------

class DIN66003CharsetDecoder extends CharsetDecoder
{

    private final byte[] byteBuf = new byte[ 65536 ];

    DIN66003CharsetDecoder( final Charset charset )
    {
        super( charset, 1f, 1f );
        this.onUnmappableCharacter( CodingErrorAction.REPLACE );
    }

    protected CoderResult decodeLoop( final ByteBuffer in,
        final CharBuffer buf )
    {

        if ( in.hasArray() && buf.hasArray() )
        {
            return this.decodeLoopArray( in, buf );
        }

        int inRemaining;
        final int inPosition = in.position();
        int i;
        int inLen;

        while ( in.hasRemaining() )
        {
            inRemaining = in.remaining();
            if ( inRemaining < this.byteBuf.length )
            {
                in.get( this.byteBuf, 0, inRemaining );
                inLen = inRemaining;
            }
            else
            {
                in.get( this.byteBuf, 0, this.byteBuf.length );
                inLen = this.byteBuf.length;
            }
            for ( i = 0; i < inLen; i++ )
            {
                if ( !buf.hasRemaining() )
                {
                    return CoderResult.OVERFLOW;
                }

                if ( ( this.byteBuf[i] & 0xFF ) < 0x20 ||
                    ( this.byteBuf[i] & 0xFF ) > 0x7F )
                {
                    in.position( inPosition + i );
                    return CoderResult.unmappableForLength( 1 );
                }

                buf.put( DIN66003CharsetProvider.DIN66003_TO_CHAR[this.byteBuf[i] &
                    0xFF] );

            }
        }

        return CoderResult.UNDERFLOW;
    }

    private CoderResult decodeLoopArray( final ByteBuffer in,
        final CharBuffer buf )
    {
        final byte[] inArray = in.array();
        final int inOffset = in.arrayOffset();
        final char[] outArray = buf.array();
        final int outOffset = buf.arrayOffset();
        final int inPosition = in.position();
        final int outPosition = buf.position();
        int inRemaining;
        while ( ( inRemaining = in.remaining() ) > 0 )
        {
            for ( int i = 0; i < inRemaining; i++ )
            {
                if ( buf.remaining() < inRemaining )
                {
                    return CoderResult.OVERFLOW;
                }

                final int inIndex = inPosition + inOffset + i;
                final int outIndex = outPosition + outOffset + i;
                if ( ( inArray[inIndex] & 0xFF ) < 0x20 ||
                    ( inArray[inIndex] & 0xFF ) > 0x7F )
                {
                    in.position( inPosition + i );
                    buf.position( outPosition + i );
                    return CoderResult.unmappableForLength( 1 );
                }
                else
                {
                    outArray[outIndex] =
                        DIN66003CharsetProvider.DIN66003_TO_CHAR[inArray[inIndex] &
                        0xFF];

                }
            }
            in.position( inPosition + inRemaining );
            buf.position( outPosition + inRemaining );
        }

        return CoderResult.UNDERFLOW;
    }

}

//------------------------------------------------------DIN66003CharsetDecoder--

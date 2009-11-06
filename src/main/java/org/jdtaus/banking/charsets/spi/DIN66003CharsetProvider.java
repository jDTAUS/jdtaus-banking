/*
 *  jDTAUS Banking Charset Providers
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
package org.jdtaus.banking.charsets.spi;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.spi.CharsetProvider;
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
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
public class DIN66003CharsetProvider extends CharsetProvider
{

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
        COMMON_NAME.toLowerCase(), "iso-ir-21", "de", "iso646-de", "csiso21german"
    };

    static final char[] BYTE_TO_CHAR = new char[ 0xFF ];

    static final byte[] CHAR_TO_BYTE = new byte[ 0xFF ];

    static
    {
        for ( int i = 0x7F; i >= 0; i-- )
        {
            CHAR_TO_BYTE[i] = (byte) i;
            BYTE_TO_CHAR[i] = (char) i;
        }

        CHAR_TO_BYTE['\u00A7'] = (byte) 0x40;
        CHAR_TO_BYTE['\u00C4'] = (byte) 0x5B;
        CHAR_TO_BYTE['\u00D6'] = (byte) 0x5C;
        CHAR_TO_BYTE['\u00DC'] = (byte) 0x5D;
        CHAR_TO_BYTE['\u00E4'] = (byte) 0x7B;
        CHAR_TO_BYTE['\u00F6'] = (byte) 0x7C;
        CHAR_TO_BYTE['\u00FC'] = (byte) 0x7D;
        CHAR_TO_BYTE['\u00DF'] = (byte) 0x7E;

        BYTE_TO_CHAR[0x40] = '\u00A7';
        BYTE_TO_CHAR[0x5B] = '\u00C4';
        BYTE_TO_CHAR[0x5C] = '\u00D6';
        BYTE_TO_CHAR[0x5D] = '\u00DC';
        BYTE_TO_CHAR[0x7B] = '\u00E4';
        BYTE_TO_CHAR[0x7C] = '\u00F6';
        BYTE_TO_CHAR[0x7D] = '\u00FC';
        BYTE_TO_CHAR[0x7E] = '\u00DF';
    }

    /** Creates a new {@code DIN66003CharsetProvider} instance. */
    public DIN66003CharsetProvider()
    {
        super();
    }

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

}

/** DIN-66003 {@code Charset} implementation. */
class DIN66003Charset extends Charset
{

    public DIN66003Charset()
    {
        super( DIN66003CharsetProvider.COMMON_NAME, DIN66003CharsetProvider.ALIAS_NAMES );
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
        return ( c >= 0x00 && c <= 0x3F ) || ( c >= 0x41 && c <= 0x5A ) || ( c >= 0x5F && c <= 0x7A ) ||
               c == '\u00A7' || c == '\u00C4' || c == '\u00D6' || c == '\u00DC' || c == '\u00E4' || c == '\u00F6' ||
               c == '\u00FC' || c == '\u00DF';

    }

}

class DIN66003CharsetEncoder extends CharsetEncoder
{

    private final char[] charBuf = new char[ 65536 ];

    DIN66003CharsetEncoder( final Charset charset )
    {
        super( charset, 1f, 1f );
        this.onUnmappableCharacter( CodingErrorAction.REPLACE );
    }

    protected CoderResult encodeLoop( final CharBuffer in, final ByteBuffer buf )
    {
        if ( in.hasArray() && buf.hasArray() )
        {
            return encodeLoopArray( in, buf );
        }

        while ( in.hasRemaining() )
        {
            in.mark();

            final int len;
            if ( in.remaining() < this.charBuf.length )
            {
                len = in.remaining();
                in.get( this.charBuf, 0, in.remaining() );
            }
            else
            {
                in.get( this.charBuf, 0, this.charBuf.length );
                len = this.charBuf.length;
            }

            for ( int i = 0; i < len; i++ )
            {
                if ( !buf.hasRemaining() )
                {
                    in.reset();
                    in.position( in.position() + i );
                    return CoderResult.OVERFLOW;
                }

                if ( !DIN66003Charset.isCharacterSupported( this.charBuf[i] ) )
                {
                    in.reset();
                    in.position( in.position() + i );
                    return CoderResult.unmappableForLength( 1 );
                }

                buf.put( DIN66003CharsetProvider.CHAR_TO_BYTE[this.charBuf[i]] );
            }
        }

        return CoderResult.UNDERFLOW;
    }

    private static CoderResult encodeLoopArray( final CharBuffer in, final ByteBuffer buf )
    {
        final int len = in.remaining();
        for ( int i = 0; i < len; i++, in.position( in.position() + 1 ), buf.position( buf.position() + 1 ) )
        {
            if ( !buf.hasRemaining() )
            {
                return CoderResult.OVERFLOW;
            }

            if ( !DIN66003Charset.isCharacterSupported( in.array()[in.position() + in.arrayOffset()] ) )
            {
                return CoderResult.unmappableForLength( 1 );
            }

            buf.array()[buf.position() + buf.arrayOffset()] =
                DIN66003CharsetProvider.CHAR_TO_BYTE[in.array()[in.position() + in.arrayOffset()]];

        }

        return CoderResult.UNDERFLOW;
    }

}

class DIN66003CharsetDecoder extends CharsetDecoder
{

    private final byte[] byteBuf = new byte[ 65536 ];

    DIN66003CharsetDecoder( final Charset charset )
    {
        super( charset, 1f, 1f );
        this.onUnmappableCharacter( CodingErrorAction.REPLACE );
    }

    protected CoderResult decodeLoop( final ByteBuffer in, final CharBuffer buf )
    {
        if ( in.hasArray() && buf.hasArray() )
        {
            return decodeLoopArray( in, buf );
        }

        while ( in.hasRemaining() )
        {
            in.mark();

            final int len;
            if ( in.remaining() < this.byteBuf.length )
            {
                len = in.remaining();
                in.get( this.byteBuf, 0, in.remaining() );
            }
            else
            {
                in.get( this.byteBuf, 0, this.byteBuf.length );
                len = this.byteBuf.length;
            }

            for ( int i = 0; i < len; i++ )
            {
                if ( !buf.hasRemaining() )
                {
                    in.reset();
                    in.position( in.position() + i );
                    return CoderResult.OVERFLOW;
                }

                if ( ( this.byteBuf[i] & 0xFF ) < 0x00 || ( this.byteBuf[i] & 0xFF ) > 0x7F )
                {
                    in.reset();
                    in.position( in.position() + i );
                    return CoderResult.unmappableForLength( 1 );
                }

                buf.put( DIN66003CharsetProvider.BYTE_TO_CHAR[this.byteBuf[i] & 0xFF] );
            }
        }

        return CoderResult.UNDERFLOW;
    }

    private static CoderResult decodeLoopArray( final ByteBuffer in, final CharBuffer buf )
    {
        final int len = in.remaining();
        for ( int i = 0; i < len; i++, in.position( in.position() + 1 ), buf.position( buf.position() + 1 ) )
        {
            if ( !buf.hasRemaining() )
            {
                return CoderResult.OVERFLOW;
            }

            if ( ( in.array()[in.position() + in.arrayOffset()] & 0xFF ) < 0x00 ||
                 ( in.array()[in.position() + in.arrayOffset()] & 0xFF ) > 0x7F )
            {
                return CoderResult.unmappableForLength( 1 );
            }

            buf.array()[buf.position() + buf.arrayOffset()] =
                DIN66003CharsetProvider.BYTE_TO_CHAR[in.array()[in.position() + in.arrayOffset()] & 0xFF];

        }

        return CoderResult.UNDERFLOW;
    }

}

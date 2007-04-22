/*
 *  jDTAUS - DTAUS fileformat.
 *  Copyright (c) 2005 Christian Schulte <cs@schulte.it>
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
package org.jdtaus.core.nio.util.spi;

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
 * {@code CharsetProvider} for IBM273 Charset.
 * <p>
 * Name: IBM273<br>
 * MIBenum: 2030<br>
 * Source: IBM NLS RM Vol2 SE09-8002-01, March 1990<br>
 * Alias: CP273<br>
 * Alias: csIBM273<br>
 * See: RFC1345,KXS2<br>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 *
 * @see org.jdtaus.core.nio.util.Charsets
 */
public class IBM273CharsetProvider extends CharsetProvider
{

    //--Constants---------------------------------------------------------------

    /** Common name. */
    private static final String COMMON_NAME = "IBM273";

    /** Alias names. */
    private static final String[] ALIAS_NAMES = {
        "cp273", "csIBM273"
    };

    /** Supported character set names. */
    private static final String[] SUPPORTED_NAMES = {
        IBM273CharsetProvider.COMMON_NAME.toLowerCase(),
        "cp273", "csIBM273"
    };

    private static final char[] IBM273_TO_CHAR = {
        '\u0000', '\u0001', '\u0002', '\u0003', '\u009C', '\u0009', '\u0086',
        '\u007F', '\u0097', '\u008D', '\u008E', '\u000B', '\u000C',
        0xD, '\u000E', '\u000F', '\u0010', '\u0011', '\u0012', '\u0013',
        '\u009D', '\u0085', '\u0008', '\u0087', '\u0018', '\u0019',
        '\u0092', '\u008F', '\u001C', '\u001D', '\u001E', '\u001F',
        '\u0080', '\u0081', '\u0082', '\u0083', '\u0084', 0xA,
        '\u0017', '\u001B', '\u0088', '\u0089', '\u008A', '\u008B',
        '\u008C', '\u0005', '\u0006', '\u0007', '\u0090', '\u0091',
        '\u0016', '\u0093', '\u0094', '\u0095', '\u0096', '\u0004',
        '\u0098', '\u0099', '\u009A', '\u009B', '\u0014', '\u0015',
        '\u009E', '\u001A', '\u0020', '\u00A0', '\u00E2', '\u007B',
        '\u00E0', '\u00E1', '\u00E3', '\u00E5', '\u00E7', '\u00F1',
        '\u00C4', '\u002E', '\u003C', '\u0028', '\u002B', '\u0021',
        '\u0026', '\u00E9', '\u00EA', '\u00EB', '\u00E8', '\u00ED',
        '\u00EE', '\u00EF', '\u00EC', '\u007E', '\u00DC', '\u0024',
        '\u002A', '\u0029', '\u003B', '\u005E', '\u002D', '\u002F',
        '\u00C2', '\u005B', '\u00C0', '\u00C1', '\u00C3', '\u00C5',
        '\u00C7', '\u00D1', '\u00F6', '\u002C', '\u0025', '\u005F',
        '\u003E', '\u003F', '\u00F8', '\u00C9', '\u00CA', '\u00CB',
        '\u00C8', '\u00CD', '\u00CE', '\u00CF', '\u00CC', '\u0060',
        '\u003A', '\u0023', '\u00A7', 0x27, '\u003D', '\u0022', '\u00D8',
        '\u0061', '\u0062', '\u0063', '\u0064', '\u0065', '\u0066',
        '\u0067', '\u0068', '\u0069', '\u00AB', '\u00BB', '\u00F0',
        '\u00FD', '\u00FE', '\u00B1', '\u00B0', '\u006A', '\u006B',
        '\u006C', '\u006D', '\u006E', '\u006F', '\u0070', '\u0071',
        '\u0072', '\u00AA', '\u00BA', '\u00E6', '\u00B8', '\u00C6',
        '\u00A4', '\u00B5', '\u00DF', '\u0073', '\u0074', '\u0075',
        '\u0076', '\u0077', '\u0078', '\u0079', '\u007A', '\u00A1',
        '\u00BF', '\u00D0', '\u00DD', '\u00DE', '\u00AE', '\u00A2',
        '\u00A3', '\u00A5', '\u00B7', '\u00A9', '\u0040', '\u00B6',
        '\u00BC', '\u00BD', '\u00BE', '\u00AC', '\u007C', '\u203E',
        '\u00A8', '\u00B4', '\u00D7', '\u00E4', '\u0041', '\u0042',
        '\u0043', '\u0044', '\u0045', '\u0046', '\u0047', '\u0048',
        '\u0049', '\u00AD', '\u00F4', '\u00A6', '\u00F2', '\u00F3',
        '\u00F5', '\u00FC', '\u004A', '\u004B', '\u004C', '\u004D',
        '\u004E', '\u004F', '\u0050', '\u0051', '\u0052', '\u00B9',
        '\u00FB', '\u007D', '\u00F9', '\u00FA', '\u00FF', '\u00D6',
        '\u00F7', '\u0053', '\u0054', '\u0055', '\u0056', '\u0057',
        '\u0058', '\u0059', '\u005A', '\u00B2', '\u00D4', 0x5C,
        '\u00D2', '\u00D3', '\u00D5', '\u0030', '\u0031', '\u0032',
        '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038',
        '\u0039', '\u00B3', '\u00DB', '\u005D', '\u00D9', '\u00DA',
        '\u009F'
    };

    private static final byte[] CHAR_TO_IBM273 = new byte[0x203F];

    //---------------------------------------------------------------Constants--
    //--Constructors------------------------------------------------------------

    static
    {
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0000'] = (byte) 0x00;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0001'] = (byte) 0x01;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0002'] = (byte) 0x02;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0003'] = (byte) 0x03;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u009C'] = (byte) 0x04;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0009'] = (byte) 0x05;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0086'] = (byte) 0x06;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u007F'] = (byte) 0x07;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0097'] = (byte) 0x08;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u008D'] = (byte) 0x09;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u008E'] = (byte) 0x0A;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u000B'] = (byte) 0x0B;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u000C'] = (byte) 0x0C;
        IBM273CharsetProvider.CHAR_TO_IBM273[0xD] = (byte) 0x0D;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u000E'] = (byte) 0x0E;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u000F'] = (byte) 0x0F;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0010'] = (byte) 0x10;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0011'] = (byte) 0x11;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0012'] = (byte) 0x12;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0013'] = (byte) 0x13;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u009D'] = (byte) 0x14;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0085'] = (byte) 0x15;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0008'] = (byte) 0x16;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0087'] = (byte) 0x17;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0018'] = (byte) 0x18;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0019'] = (byte) 0x19;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0092'] = (byte) 0x1A;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u008F'] = (byte) 0x1B;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u001C'] = (byte) 0x1C;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u001D'] = (byte) 0x1D;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u001E'] = (byte) 0x1E;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u001F'] = (byte) 0x1F;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0080'] = (byte) 0x20;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0081'] = (byte) 0x21;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0082'] = (byte) 0x22;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0083'] = (byte) 0x23;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0084'] = (byte) 0x24;
        IBM273CharsetProvider.CHAR_TO_IBM273[0xA] = (byte) 0x25;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0017'] = (byte) 0x26;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u001B'] = (byte) 0x27;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0088'] = (byte) 0x28;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0089'] = (byte) 0x29;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u008A'] = (byte) 0x2A;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u008B'] = (byte) 0x2B;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u008C'] = (byte) 0x2C;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0005'] = (byte) 0x2D;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0006'] = (byte) 0x2E;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0007'] = (byte) 0x2F;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0090'] = (byte) 0x30;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0091'] = (byte) 0x31;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0016'] = (byte) 0x32;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0093'] = (byte) 0x33;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0094'] = (byte) 0x34;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0095'] = (byte) 0x35;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0096'] = (byte) 0x36;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0004'] = (byte) 0x37;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0098'] = (byte) 0x38;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0099'] = (byte) 0x39;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u009A'] = (byte) 0x3A;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u009B'] = (byte) 0x3B;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0014'] = (byte) 0x3C;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0015'] = (byte) 0x3D;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u009E'] = (byte) 0x3E;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u001A'] = (byte) 0x3F;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0020'] = (byte) 0x40;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00A0'] = (byte) 0x41;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00E2'] = (byte) 0x42;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u007B'] = (byte) 0x43;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00E0'] = (byte) 0x44;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00E1'] = (byte) 0x45;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00E3'] = (byte) 0x46;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00E5'] = (byte) 0x47;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00E7'] = (byte) 0x48;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00F1'] = (byte) 0x49;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00C4'] = (byte) 0x4A;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u002E'] = (byte) 0x4B;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u003C'] = (byte) 0x4C;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0028'] = (byte) 0x4D;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u002B'] = (byte) 0x4E;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0021'] = (byte) 0x4F;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0026'] = (byte) 0x50;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00E9'] = (byte) 0x51;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00EA'] = (byte) 0x52;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00EB'] = (byte) 0x53;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00E8'] = (byte) 0x54;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00ED'] = (byte) 0x55;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00EE'] = (byte) 0x56;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00EF'] = (byte) 0x57;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00EC'] = (byte) 0x58;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u007E'] = (byte) 0x59;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00DC'] = (byte) 0x5A;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0024'] = (byte) 0x5B;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u002A'] = (byte) 0x5C;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0029'] = (byte) 0x5D;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u003B'] = (byte) 0x5E;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u005E'] = (byte) 0x5F;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u002D'] = (byte) 0x60;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u002F'] = (byte) 0x61;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00C2'] = (byte) 0x62;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u005B'] = (byte) 0x63;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00C0'] = (byte) 0x64;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00C1'] = (byte) 0x65;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00C3'] = (byte) 0x66;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00C5'] = (byte) 0x67;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00C7'] = (byte) 0x68;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00D1'] = (byte) 0x69;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00F6'] = (byte) 0x6A;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u002C'] = (byte) 0x6B;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0025'] = (byte) 0x6C;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u005F'] = (byte) 0x6D;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u003E'] = (byte) 0x6E;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u003F'] = (byte) 0x6F;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00F8'] = (byte) 0x70;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00C9'] = (byte) 0x71;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00CA'] = (byte) 0x72;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00CB'] = (byte) 0x73;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00C8'] = (byte) 0x74;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00CD'] = (byte) 0x75;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00CE'] = (byte) 0x76;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00CF'] = (byte) 0x77;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00CC'] = (byte) 0x78;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0060'] = (byte) 0x79;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u003A'] = (byte) 0x7A;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0023'] = (byte) 0x7B;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00A7'] = (byte) 0x7C;
        IBM273CharsetProvider.CHAR_TO_IBM273[0x27] = (byte) 0x7D;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u003D'] = (byte) 0x7E;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0022'] = (byte) 0x7F;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00D8'] = (byte) 0x80;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0061'] = (byte) 0x81;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0062'] = (byte) 0x82;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0063'] = (byte) 0x83;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0064'] = (byte) 0x84;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0065'] = (byte) 0x85;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0066'] = (byte) 0x86;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0067'] = (byte) 0x87;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0068'] = (byte) 0x88;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0069'] = (byte) 0x89;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00AB'] = (byte) 0x8A;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00BB'] = (byte) 0x8B;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00F0'] = (byte) 0x8C;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00FD'] = (byte) 0x8D;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00FE'] = (byte) 0x8E;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00B1'] = (byte) 0x8F;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00B0'] = (byte) 0x90;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u006A'] = (byte) 0x91;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u006B'] = (byte) 0x92;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u006C'] = (byte) 0x93;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u006D'] = (byte) 0x94;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u006E'] = (byte) 0x95;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u006F'] = (byte) 0x96;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0070'] = (byte) 0x97;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0071'] = (byte) 0x98;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0072'] = (byte) 0x99;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00AA'] = (byte) 0x9A;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00BA'] = (byte) 0x9B;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00E6'] = (byte) 0x9C;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00B8'] = (byte) 0x9D;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00C6'] = (byte) 0x9E;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00A4'] = (byte) 0x9F;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00B5'] = (byte) 0xA0;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00DF'] = (byte) 0xA1;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0073'] = (byte) 0xA2;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0074'] = (byte) 0xA3;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0075'] = (byte) 0xA4;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0076'] = (byte) 0xA5;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0077'] = (byte) 0xA6;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0078'] = (byte) 0xA7;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0079'] = (byte) 0xA8;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u007A'] = (byte) 0xA9;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00A1'] = (byte) 0xAA;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00BF'] = (byte) 0xAB;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00D0'] = (byte) 0xAC;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00DD'] = (byte) 0xAD;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00DE'] = (byte) 0xAE;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00AE'] = (byte) 0xAF;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00A2'] = (byte) 0xB0;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00A3'] = (byte) 0xB1;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00A5'] = (byte) 0xB2;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00B7'] = (byte) 0xB3;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00A9'] = (byte) 0xB4;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0040'] = (byte) 0xB5;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00B6'] = (byte) 0xB6;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00BC'] = (byte) 0xB7;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00BD'] = (byte) 0xB8;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00BE'] = (byte) 0xB9;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00AC'] = (byte) 0xBA;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u007C'] = (byte) 0xBB;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u203E'] = (byte) 0xBC;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00A8'] = (byte) 0xBD;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00B4'] = (byte) 0xBE;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00D7'] = (byte) 0xBF;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00E4'] = (byte) 0xC0;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0041'] = (byte) 0xC1;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0042'] = (byte) 0xC2;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0043'] = (byte) 0xC3;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0044'] = (byte) 0xC4;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0045'] = (byte) 0xC5;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0046'] = (byte) 0xC6;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0047'] = (byte) 0xC7;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0048'] = (byte) 0xC8;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0049'] = (byte) 0xC9;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00AD'] = (byte) 0xCA;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00F4'] = (byte) 0xCB;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00A6'] = (byte) 0xCC;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00F2'] = (byte) 0xCD;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00F3'] = (byte) 0xCE;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00F5'] = (byte) 0xCF;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00FC'] = (byte) 0xD0;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u004A'] = (byte) 0xD1;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u004B'] = (byte) 0xD2;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u004C'] = (byte) 0xD3;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u004D'] = (byte) 0xD4;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u004E'] = (byte) 0xD5;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u004F'] = (byte) 0xD6;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0050'] = (byte) 0xD7;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0051'] = (byte) 0xD8;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0052'] = (byte) 0xD9;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00B9'] = (byte) 0xDA;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00FB'] = (byte) 0xDB;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u007D'] = (byte) 0xDC;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00F9'] = (byte) 0xDD;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00FA'] = (byte) 0xDE;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00FF'] = (byte) 0xDF;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00D6'] = (byte) 0xE0;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00F7'] = (byte) 0xE1;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0053'] = (byte) 0xE2;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0054'] = (byte) 0xE3;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0055'] = (byte) 0xE4;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0056'] = (byte) 0xE5;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0057'] = (byte) 0xE6;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0058'] = (byte) 0xE7;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0059'] = (byte) 0xE8;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u005A'] = (byte) 0xE9;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00B2'] = (byte) 0xEA;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00D4'] = (byte) 0xEB;
        IBM273CharsetProvider.CHAR_TO_IBM273[0x5C] = (byte) 0xEC;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00D2'] = (byte) 0xED;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00D3'] = (byte) 0xEE;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00D5'] = (byte) 0xEF;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0030'] = (byte) 0xF0;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0031'] = (byte) 0xF1;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0032'] = (byte) 0xF2;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0033'] = (byte) 0xF3;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0034'] = (byte) 0xF4;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0035'] = (byte) 0xF5;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0036'] = (byte) 0xF6;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0037'] = (byte) 0xF7;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0038'] = (byte) 0xF8;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u0039'] = (byte) 0xF9;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00B3'] = (byte) 0xFA;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00DB'] = (byte) 0xFB;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u005D'] = (byte) 0xFC;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00D9'] = (byte) 0xFD;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u00DA'] = (byte) 0xFE;
        IBM273CharsetProvider.CHAR_TO_IBM273['\u009F'] = (byte) 0xFF;
    }

    /** Creates a new {@code IBM273CharsetProvider} instance. */
    public IBM273CharsetProvider()
    {
        super();
    }

    //------------------------------------------------------------Constructors--
    //--CharsetProvider---------------------------------------------------------

    public Charset charsetForName(final String charsetName)
    {
        Charset ret = null;

        if(charsetName != null)
        {
            final String lower = charsetName.toLowerCase();
            for(int i = 0; i < SUPPORTED_NAMES.length; i++)
            {
                if(IBM273CharsetProvider.SUPPORTED_NAMES[i].equals(lower))
                {
                    ret = new IBM273Charset();
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
                if(this.hasNext)
                {
                    this.hasNext = false;
                    return new IBM273Charset();
                }
                else
                    throw new NoSuchElementException();

            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }

        };
    }

    //---------------------------------------------------------CharsetProvider--

    public static class IBM273Charset extends Charset
    {

        public IBM273Charset()
        {
            super(IBM273CharsetProvider.COMMON_NAME,
                IBM273CharsetProvider.ALIAS_NAMES);

        }


        public CharsetEncoder newEncoder()
        {
            return new IBM273CharsetEncoder();
        }

        public CharsetDecoder newDecoder()
        {
            return new IBM273CharsetDecoder();
        }

        public boolean contains(final Charset charset)
        {
            return false;
        }

        private boolean isCharacterSupported(final char c)
        {
            return (c >= '\u0000' && c <= '\u00AE') ||
                (c >= '\u00B0' && c <= '\u00FF') || c == '\u203E';

        }

        //--IBM273CharsetEncoder------------------------------------------------

        class IBM273CharsetEncoder extends CharsetEncoder
        {

            private final char[] charBuf = new char[65536];

            IBM273CharsetEncoder()
            {
                super(IBM273Charset.this, 1f, 1f);
                this.onUnmappableCharacter(CodingErrorAction.REPLACE);
            }

            protected CoderResult encodeLoop(final CharBuffer in,
                final ByteBuffer buf)
            {

                if(in.hasArray() && buf.hasArray())
                {
                    return this.encodeLoopArray(in, buf);
                }

                int inRemaining;
                final int inPosition = in.position();
                int i;
                int inLen;

                while(in.hasRemaining())
                {
                    inRemaining = in.remaining();
                    if(inRemaining < this.charBuf.length)
                    {
                        in.get(this.charBuf, 0, inRemaining);
                        inLen = inRemaining;
                    }
                    else
                    {
                        in.get(this.charBuf, 0, this.charBuf.length);
                        inLen = this.charBuf.length;
                    }
                    for(i = 0; i < inLen; i++)
                    {
                        if(!buf.hasRemaining())
                        {
                            return CoderResult.OVERFLOW;
                        }

                        if(!isCharacterSupported(this.charBuf[i]))
                        {
                            in.position(inPosition + i);
                            return CoderResult.unmappableForLength(1);
                        }
                        buf.put(IBM273CharsetProvider.CHAR_TO_IBM273[
                            this.charBuf[i]]);

                    }
                }

                return CoderResult.UNDERFLOW;
            }

            protected CoderResult encodeLoopArray(final CharBuffer in,
                final ByteBuffer buf)
            {

                final char[] inArray = in.array();
                final int inOffset = in.arrayOffset();
                final byte[] outArray = buf.array();
                final int outOffset = buf.arrayOffset();
                final int inPosition = in.position();
                final int outPosition = buf.position();
                int inRemaining;
                while((inRemaining = in.remaining()) > 0)
                {
                    if(buf.remaining() < inRemaining)
                    {
                        return CoderResult.OVERFLOW;
                    }

                    for(int i = 0; i < inRemaining; i++)
                    {
                        final int inIndex = inPosition + inOffset + i;
                        final int outIndex = outPosition + outOffset + i;
                        if(!isCharacterSupported(inArray[inIndex]))
                        {
                            in.position(inPosition + i);
                            buf.position(outPosition + i);
                            return CoderResult.unmappableForLength(1);
                        }
                        else
                        {
                            outArray[outIndex] =
                                IBM273CharsetProvider.CHAR_TO_IBM273[
                                inArray[inIndex]];

                        }
                    }
                    in.position(inPosition + inRemaining);
                    buf.position(outPosition + inRemaining);
                }

                return CoderResult.UNDERFLOW;
            }
        }


        //------------------------------------------------IBM273CharsetEncoder--
        //--IBM273CharsetDecoder------------------------------------------------

        class IBM273CharsetDecoder extends CharsetDecoder
        {
            private final byte[] byteBuf = new byte[65536];

            IBM273CharsetDecoder()
            {
                super(IBM273Charset.this, 1f, 1f);
                this.onUnmappableCharacter(CodingErrorAction.REPLACE);
            }

            protected CoderResult decodeLoop(final ByteBuffer in,
                final CharBuffer buf)
            {

                if(in.hasArray() && buf.hasArray())
                {
                    return this.decodeLoopArray(in, buf);
                }

                int inRemaining;
                int i;
                int inLen;

                while(in.hasRemaining())
                {
                    inRemaining = in.remaining();
                    if(inRemaining < this.byteBuf.length)
                    {
                        in.get(this.byteBuf, 0, inRemaining);
                        inLen = inRemaining;
                    }
                    else
                    {
                        in.get(this.byteBuf, 0, this.byteBuf.length);
                        inLen = this.byteBuf.length;
                    }
                    for(i = 0; i < inLen; i++)
                    {
                        if(!buf.hasRemaining())
                        {
                            return CoderResult.OVERFLOW;
                        }

                        buf.put(IBM273CharsetProvider.IBM273_TO_CHAR[
                            this.byteBuf[i] & 0xFF]);

                    }
                }

                return CoderResult.UNDERFLOW;
            }

            protected CoderResult decodeLoopArray(final ByteBuffer in,
                final CharBuffer buf)
            {

                final byte[] inArray = in.array();
                final int inOffset = in.arrayOffset();
                final char[] outArray = buf.array();
                final int outOffset = buf.arrayOffset();
                final int inPosition = in.position();
                final int outPosition = buf.position();
                int inRemaining;
                while((inRemaining = in.remaining()) > 0)
                {
                    for(int i = 0; i < inRemaining; i++)
                    {
                        if(buf.remaining() < inRemaining)
                        {
                            return CoderResult.OVERFLOW;
                        }

                        final int inIndex = inPosition + inOffset + i;
                        final int outIndex = outPosition + outOffset + i;
                        outArray[outIndex] =
                            IBM273CharsetProvider.IBM273_TO_CHAR[
                            inArray[inIndex] & 0xFF];

                    }
                    in.position(inPosition + inRemaining);
                    buf.position(outPosition + inRemaining);
                }

                return CoderResult.UNDERFLOW;
            }
        }

        //------------------------------------------------IBM273CharsetDecoder--

    }

}

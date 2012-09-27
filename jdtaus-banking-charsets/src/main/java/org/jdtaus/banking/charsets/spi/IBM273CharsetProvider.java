/*
 *  jDTAUS Banking Charset Providers
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
 * @version $JDTAUS$
 */
public class IBM273CharsetProvider extends CharsetProvider
{

    /** Common name. */
    static final String COMMON_NAME = "IBM273";

    /** Alias names. */
    static final String[] ALIAS_NAMES =
    {
        "cp273", "csIBM273"
    };

    /** Supported character set names. */
    static final String[] SUPPORTED_NAMES =
    {
        COMMON_NAME.toLowerCase(), "cp273", "csIBM273"
    };

    static final char[] BYTE_TO_CHAR =
    {
        '\u0000', '\u0001', '\u0002', '\u0003', '\u009C', '\u0009', '\u0086', '\u007F', '\u0097', '\u008D', '\u008E',
        '\u000B', '\u000C', 0xD, '\u000E', '\u000F', '\u0010', '\u0011', '\u0012', '\u0013', '\u009D', '\u0085',
        '\u0008', '\u0087', '\u0018', '\u0019', '\u0092', '\u008F', '\u001C', '\u001D', '\u001E', '\u001F', '\u0080',
        '\u0081', '\u0082', '\u0083', '\u0084', 0xA, '\u0017', '\u001B', '\u0088', '\u0089', '\u008A', '\u008B',
        '\u008C', '\u0005', '\u0006', '\u0007', '\u0090', '\u0091', '\u0016', '\u0093', '\u0094', '\u0095', '\u0096',
        '\u0004', '\u0098', '\u0099', '\u009A', '\u009B', '\u0014', '\u0015', '\u009E', '\u001A', '\u0020', '\u00A0',
        '\u00E2', '\u007B', '\u00E0', '\u00E1', '\u00E3', '\u00E5', '\u00E7', '\u00F1', '\u00C4', '\u002E', '\u003C',
        '\u0028', '\u002B', '\u0021', '\u0026', '\u00E9', '\u00EA', '\u00EB', '\u00E8', '\u00ED', '\u00EE', '\u00EF',
        '\u00EC', '\u007E', '\u00DC', '\u0024', '\u002A', '\u0029', '\u003B', '\u005E', '\u002D', '\u002F', '\u00C2',
        '\u005B', '\u00C0', '\u00C1', '\u00C3', '\u00C5', '\u00C7', '\u00D1', '\u00F6', '\u002C', '\u0025', '\u005F',
        '\u003E', '\u003F', '\u00F8', '\u00C9', '\u00CA', '\u00CB', '\u00C8', '\u00CD', '\u00CE', '\u00CF', '\u00CC',
        '\u0060', '\u003A', '\u0023', '\u00A7', 0x27, '\u003D', '\u0022', '\u00D8', '\u0061', '\u0062', '\u0063',
        '\u0064', '\u0065', '\u0066', '\u0067', '\u0068', '\u0069', '\u00AB', '\u00BB', '\u00F0', '\u00FD', '\u00FE',
        '\u00B1', '\u00B0', '\u006A', '\u006B', '\u006C', '\u006D', '\u006E', '\u006F', '\u0070', '\u0071', '\u0072',
        '\u00AA', '\u00BA', '\u00E6', '\u00B8', '\u00C6', '\u00A4', '\u00B5', '\u00DF', '\u0073', '\u0074', '\u0075',
        '\u0076', '\u0077', '\u0078', '\u0079', '\u007A', '\u00A1', '\u00BF', '\u00D0', '\u00DD', '\u00DE', '\u00AE',
        '\u00A2', '\u00A3', '\u00A5', '\u00B7', '\u00A9', '\u0040', '\u00B6', '\u00BC', '\u00BD', '\u00BE', '\u00AC',
        '\u007C', '\u203E', '\u00A8', '\u00B4', '\u00D7', '\u00E4', '\u0041', '\u0042', '\u0043', '\u0044', '\u0045',
        '\u0046', '\u0047', '\u0048', '\u0049', '\u00AD', '\u00F4', '\u00A6', '\u00F2', '\u00F3', '\u00F5', '\u00FC',
        '\u004A', '\u004B', '\u004C', '\u004D', '\u004E', '\u004F', '\u0050', '\u0051', '\u0052', '\u00B9', '\u00FB',
        '\u007D', '\u00F9', '\u00FA', '\u00FF', '\u00D6', '\u00F7', '\u0053', '\u0054', '\u0055', '\u0056', '\u0057',
        '\u0058', '\u0059', '\u005A', '\u00B2', '\u00D4', 0x5C, '\u00D2', '\u00D3', '\u00D5', '\u0030', '\u0031',
        '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039', '\u00B3', '\u00DB', '\u005D',
        '\u00D9', '\u00DA', '\u009F'
    };

    static final byte[] CHAR_TO_BYTE = new byte[ 0x203F ];

    static
    {
        CHAR_TO_BYTE['\u0000'] = (byte) 0x00;
        CHAR_TO_BYTE['\u0001'] = (byte) 0x01;
        CHAR_TO_BYTE['\u0002'] = (byte) 0x02;
        CHAR_TO_BYTE['\u0003'] = (byte) 0x03;
        CHAR_TO_BYTE['\u009C'] = (byte) 0x04;
        CHAR_TO_BYTE['\u0009'] = (byte) 0x05;
        CHAR_TO_BYTE['\u0086'] = (byte) 0x06;
        CHAR_TO_BYTE['\u007F'] = (byte) 0x07;
        CHAR_TO_BYTE['\u0097'] = (byte) 0x08;
        CHAR_TO_BYTE['\u008D'] = (byte) 0x09;
        CHAR_TO_BYTE['\u008E'] = (byte) 0x0A;
        CHAR_TO_BYTE['\u000B'] = (byte) 0x0B;
        CHAR_TO_BYTE['\u000C'] = (byte) 0x0C;
        CHAR_TO_BYTE[0xD] = (byte) 0x0D;
        CHAR_TO_BYTE['\u000E'] = (byte) 0x0E;
        CHAR_TO_BYTE['\u000F'] = (byte) 0x0F;
        CHAR_TO_BYTE['\u0010'] = (byte) 0x10;
        CHAR_TO_BYTE['\u0011'] = (byte) 0x11;
        CHAR_TO_BYTE['\u0012'] = (byte) 0x12;
        CHAR_TO_BYTE['\u0013'] = (byte) 0x13;
        CHAR_TO_BYTE['\u009D'] = (byte) 0x14;
        CHAR_TO_BYTE['\u0085'] = (byte) 0x15;
        CHAR_TO_BYTE['\u0008'] = (byte) 0x16;
        CHAR_TO_BYTE['\u0087'] = (byte) 0x17;
        CHAR_TO_BYTE['\u0018'] = (byte) 0x18;
        CHAR_TO_BYTE['\u0019'] = (byte) 0x19;
        CHAR_TO_BYTE['\u0092'] = (byte) 0x1A;
        CHAR_TO_BYTE['\u008F'] = (byte) 0x1B;
        CHAR_TO_BYTE['\u001C'] = (byte) 0x1C;
        CHAR_TO_BYTE['\u001D'] = (byte) 0x1D;
        CHAR_TO_BYTE['\u001E'] = (byte) 0x1E;
        CHAR_TO_BYTE['\u001F'] = (byte) 0x1F;
        CHAR_TO_BYTE['\u0080'] = (byte) 0x20;
        CHAR_TO_BYTE['\u0081'] = (byte) 0x21;
        CHAR_TO_BYTE['\u0082'] = (byte) 0x22;
        CHAR_TO_BYTE['\u0083'] = (byte) 0x23;
        CHAR_TO_BYTE['\u0084'] = (byte) 0x24;
        CHAR_TO_BYTE[0xA] = (byte) 0x25;
        CHAR_TO_BYTE['\u0017'] = (byte) 0x26;
        CHAR_TO_BYTE['\u001B'] = (byte) 0x27;
        CHAR_TO_BYTE['\u0088'] = (byte) 0x28;
        CHAR_TO_BYTE['\u0089'] = (byte) 0x29;
        CHAR_TO_BYTE['\u008A'] = (byte) 0x2A;
        CHAR_TO_BYTE['\u008B'] = (byte) 0x2B;
        CHAR_TO_BYTE['\u008C'] = (byte) 0x2C;
        CHAR_TO_BYTE['\u0005'] = (byte) 0x2D;
        CHAR_TO_BYTE['\u0006'] = (byte) 0x2E;
        CHAR_TO_BYTE['\u0007'] = (byte) 0x2F;
        CHAR_TO_BYTE['\u0090'] = (byte) 0x30;
        CHAR_TO_BYTE['\u0091'] = (byte) 0x31;
        CHAR_TO_BYTE['\u0016'] = (byte) 0x32;
        CHAR_TO_BYTE['\u0093'] = (byte) 0x33;
        CHAR_TO_BYTE['\u0094'] = (byte) 0x34;
        CHAR_TO_BYTE['\u0095'] = (byte) 0x35;
        CHAR_TO_BYTE['\u0096'] = (byte) 0x36;
        CHAR_TO_BYTE['\u0004'] = (byte) 0x37;
        CHAR_TO_BYTE['\u0098'] = (byte) 0x38;
        CHAR_TO_BYTE['\u0099'] = (byte) 0x39;
        CHAR_TO_BYTE['\u009A'] = (byte) 0x3A;
        CHAR_TO_BYTE['\u009B'] = (byte) 0x3B;
        CHAR_TO_BYTE['\u0014'] = (byte) 0x3C;
        CHAR_TO_BYTE['\u0015'] = (byte) 0x3D;
        CHAR_TO_BYTE['\u009E'] = (byte) 0x3E;
        CHAR_TO_BYTE['\u001A'] = (byte) 0x3F;
        CHAR_TO_BYTE['\u0020'] = (byte) 0x40;
        CHAR_TO_BYTE['\u00A0'] = (byte) 0x41;
        CHAR_TO_BYTE['\u00E2'] = (byte) 0x42;
        CHAR_TO_BYTE['\u007B'] = (byte) 0x43;
        CHAR_TO_BYTE['\u00E0'] = (byte) 0x44;
        CHAR_TO_BYTE['\u00E1'] = (byte) 0x45;
        CHAR_TO_BYTE['\u00E3'] = (byte) 0x46;
        CHAR_TO_BYTE['\u00E5'] = (byte) 0x47;
        CHAR_TO_BYTE['\u00E7'] = (byte) 0x48;
        CHAR_TO_BYTE['\u00F1'] = (byte) 0x49;
        CHAR_TO_BYTE['\u00C4'] = (byte) 0x4A;
        CHAR_TO_BYTE['\u002E'] = (byte) 0x4B;
        CHAR_TO_BYTE['\u003C'] = (byte) 0x4C;
        CHAR_TO_BYTE['\u0028'] = (byte) 0x4D;
        CHAR_TO_BYTE['\u002B'] = (byte) 0x4E;
        CHAR_TO_BYTE['\u0021'] = (byte) 0x4F;
        CHAR_TO_BYTE['\u0026'] = (byte) 0x50;
        CHAR_TO_BYTE['\u00E9'] = (byte) 0x51;
        CHAR_TO_BYTE['\u00EA'] = (byte) 0x52;
        CHAR_TO_BYTE['\u00EB'] = (byte) 0x53;
        CHAR_TO_BYTE['\u00E8'] = (byte) 0x54;
        CHAR_TO_BYTE['\u00ED'] = (byte) 0x55;
        CHAR_TO_BYTE['\u00EE'] = (byte) 0x56;
        CHAR_TO_BYTE['\u00EF'] = (byte) 0x57;
        CHAR_TO_BYTE['\u00EC'] = (byte) 0x58;
        CHAR_TO_BYTE['\u007E'] = (byte) 0x59;
        CHAR_TO_BYTE['\u00DC'] = (byte) 0x5A;
        CHAR_TO_BYTE['\u0024'] = (byte) 0x5B;
        CHAR_TO_BYTE['\u002A'] = (byte) 0x5C;
        CHAR_TO_BYTE['\u0029'] = (byte) 0x5D;
        CHAR_TO_BYTE['\u003B'] = (byte) 0x5E;
        CHAR_TO_BYTE['\u005E'] = (byte) 0x5F;
        CHAR_TO_BYTE['\u002D'] = (byte) 0x60;
        CHAR_TO_BYTE['\u002F'] = (byte) 0x61;
        CHAR_TO_BYTE['\u00C2'] = (byte) 0x62;
        CHAR_TO_BYTE['\u005B'] = (byte) 0x63;
        CHAR_TO_BYTE['\u00C0'] = (byte) 0x64;
        CHAR_TO_BYTE['\u00C1'] = (byte) 0x65;
        CHAR_TO_BYTE['\u00C3'] = (byte) 0x66;
        CHAR_TO_BYTE['\u00C5'] = (byte) 0x67;
        CHAR_TO_BYTE['\u00C7'] = (byte) 0x68;
        CHAR_TO_BYTE['\u00D1'] = (byte) 0x69;
        CHAR_TO_BYTE['\u00F6'] = (byte) 0x6A;
        CHAR_TO_BYTE['\u002C'] = (byte) 0x6B;
        CHAR_TO_BYTE['\u0025'] = (byte) 0x6C;
        CHAR_TO_BYTE['\u005F'] = (byte) 0x6D;
        CHAR_TO_BYTE['\u003E'] = (byte) 0x6E;
        CHAR_TO_BYTE['\u003F'] = (byte) 0x6F;
        CHAR_TO_BYTE['\u00F8'] = (byte) 0x70;
        CHAR_TO_BYTE['\u00C9'] = (byte) 0x71;
        CHAR_TO_BYTE['\u00CA'] = (byte) 0x72;
        CHAR_TO_BYTE['\u00CB'] = (byte) 0x73;
        CHAR_TO_BYTE['\u00C8'] = (byte) 0x74;
        CHAR_TO_BYTE['\u00CD'] = (byte) 0x75;
        CHAR_TO_BYTE['\u00CE'] = (byte) 0x76;
        CHAR_TO_BYTE['\u00CF'] = (byte) 0x77;
        CHAR_TO_BYTE['\u00CC'] = (byte) 0x78;
        CHAR_TO_BYTE['\u0060'] = (byte) 0x79;
        CHAR_TO_BYTE['\u003A'] = (byte) 0x7A;
        CHAR_TO_BYTE['\u0023'] = (byte) 0x7B;
        CHAR_TO_BYTE['\u00A7'] = (byte) 0x7C;
        CHAR_TO_BYTE[0x27] = (byte) 0x7D;
        CHAR_TO_BYTE['\u003D'] = (byte) 0x7E;
        CHAR_TO_BYTE['\u0022'] = (byte) 0x7F;
        CHAR_TO_BYTE['\u00D8'] = (byte) 0x80;
        CHAR_TO_BYTE['\u0061'] = (byte) 0x81;
        CHAR_TO_BYTE['\u0062'] = (byte) 0x82;
        CHAR_TO_BYTE['\u0063'] = (byte) 0x83;
        CHAR_TO_BYTE['\u0064'] = (byte) 0x84;
        CHAR_TO_BYTE['\u0065'] = (byte) 0x85;
        CHAR_TO_BYTE['\u0066'] = (byte) 0x86;
        CHAR_TO_BYTE['\u0067'] = (byte) 0x87;
        CHAR_TO_BYTE['\u0068'] = (byte) 0x88;
        CHAR_TO_BYTE['\u0069'] = (byte) 0x89;
        CHAR_TO_BYTE['\u00AB'] = (byte) 0x8A;
        CHAR_TO_BYTE['\u00BB'] = (byte) 0x8B;
        CHAR_TO_BYTE['\u00F0'] = (byte) 0x8C;
        CHAR_TO_BYTE['\u00FD'] = (byte) 0x8D;
        CHAR_TO_BYTE['\u00FE'] = (byte) 0x8E;
        CHAR_TO_BYTE['\u00B1'] = (byte) 0x8F;
        CHAR_TO_BYTE['\u00B0'] = (byte) 0x90;
        CHAR_TO_BYTE['\u006A'] = (byte) 0x91;
        CHAR_TO_BYTE['\u006B'] = (byte) 0x92;
        CHAR_TO_BYTE['\u006C'] = (byte) 0x93;
        CHAR_TO_BYTE['\u006D'] = (byte) 0x94;
        CHAR_TO_BYTE['\u006E'] = (byte) 0x95;
        CHAR_TO_BYTE['\u006F'] = (byte) 0x96;
        CHAR_TO_BYTE['\u0070'] = (byte) 0x97;
        CHAR_TO_BYTE['\u0071'] = (byte) 0x98;
        CHAR_TO_BYTE['\u0072'] = (byte) 0x99;
        CHAR_TO_BYTE['\u00AA'] = (byte) 0x9A;
        CHAR_TO_BYTE['\u00BA'] = (byte) 0x9B;
        CHAR_TO_BYTE['\u00E6'] = (byte) 0x9C;
        CHAR_TO_BYTE['\u00B8'] = (byte) 0x9D;
        CHAR_TO_BYTE['\u00C6'] = (byte) 0x9E;
        CHAR_TO_BYTE['\u00A4'] = (byte) 0x9F;
        CHAR_TO_BYTE['\u00B5'] = (byte) 0xA0;
        CHAR_TO_BYTE['\u00DF'] = (byte) 0xA1;
        CHAR_TO_BYTE['\u0073'] = (byte) 0xA2;
        CHAR_TO_BYTE['\u0074'] = (byte) 0xA3;
        CHAR_TO_BYTE['\u0075'] = (byte) 0xA4;
        CHAR_TO_BYTE['\u0076'] = (byte) 0xA5;
        CHAR_TO_BYTE['\u0077'] = (byte) 0xA6;
        CHAR_TO_BYTE['\u0078'] = (byte) 0xA7;
        CHAR_TO_BYTE['\u0079'] = (byte) 0xA8;
        CHAR_TO_BYTE['\u007A'] = (byte) 0xA9;
        CHAR_TO_BYTE['\u00A1'] = (byte) 0xAA;
        CHAR_TO_BYTE['\u00BF'] = (byte) 0xAB;
        CHAR_TO_BYTE['\u00D0'] = (byte) 0xAC;
        CHAR_TO_BYTE['\u00DD'] = (byte) 0xAD;
        CHAR_TO_BYTE['\u00DE'] = (byte) 0xAE;
        CHAR_TO_BYTE['\u00AE'] = (byte) 0xAF;
        CHAR_TO_BYTE['\u00A2'] = (byte) 0xB0;
        CHAR_TO_BYTE['\u00A3'] = (byte) 0xB1;
        CHAR_TO_BYTE['\u00A5'] = (byte) 0xB2;
        CHAR_TO_BYTE['\u00B7'] = (byte) 0xB3;
        CHAR_TO_BYTE['\u00A9'] = (byte) 0xB4;
        CHAR_TO_BYTE['\u0040'] = (byte) 0xB5;
        CHAR_TO_BYTE['\u00B6'] = (byte) 0xB6;
        CHAR_TO_BYTE['\u00BC'] = (byte) 0xB7;
        CHAR_TO_BYTE['\u00BD'] = (byte) 0xB8;
        CHAR_TO_BYTE['\u00BE'] = (byte) 0xB9;
        CHAR_TO_BYTE['\u00AC'] = (byte) 0xBA;
        CHAR_TO_BYTE['\u007C'] = (byte) 0xBB;
        CHAR_TO_BYTE['\u203E'] = (byte) 0xBC;
        CHAR_TO_BYTE['\u00A8'] = (byte) 0xBD;
        CHAR_TO_BYTE['\u00B4'] = (byte) 0xBE;
        CHAR_TO_BYTE['\u00D7'] = (byte) 0xBF;
        CHAR_TO_BYTE['\u00E4'] = (byte) 0xC0;
        CHAR_TO_BYTE['\u0041'] = (byte) 0xC1;
        CHAR_TO_BYTE['\u0042'] = (byte) 0xC2;
        CHAR_TO_BYTE['\u0043'] = (byte) 0xC3;
        CHAR_TO_BYTE['\u0044'] = (byte) 0xC4;
        CHAR_TO_BYTE['\u0045'] = (byte) 0xC5;
        CHAR_TO_BYTE['\u0046'] = (byte) 0xC6;
        CHAR_TO_BYTE['\u0047'] = (byte) 0xC7;
        CHAR_TO_BYTE['\u0048'] = (byte) 0xC8;
        CHAR_TO_BYTE['\u0049'] = (byte) 0xC9;
        CHAR_TO_BYTE['\u00AD'] = (byte) 0xCA;
        CHAR_TO_BYTE['\u00F4'] = (byte) 0xCB;
        CHAR_TO_BYTE['\u00A6'] = (byte) 0xCC;
        CHAR_TO_BYTE['\u00F2'] = (byte) 0xCD;
        CHAR_TO_BYTE['\u00F3'] = (byte) 0xCE;
        CHAR_TO_BYTE['\u00F5'] = (byte) 0xCF;
        CHAR_TO_BYTE['\u00FC'] = (byte) 0xD0;
        CHAR_TO_BYTE['\u004A'] = (byte) 0xD1;
        CHAR_TO_BYTE['\u004B'] = (byte) 0xD2;
        CHAR_TO_BYTE['\u004C'] = (byte) 0xD3;
        CHAR_TO_BYTE['\u004D'] = (byte) 0xD4;
        CHAR_TO_BYTE['\u004E'] = (byte) 0xD5;
        CHAR_TO_BYTE['\u004F'] = (byte) 0xD6;
        CHAR_TO_BYTE['\u0050'] = (byte) 0xD7;
        CHAR_TO_BYTE['\u0051'] = (byte) 0xD8;
        CHAR_TO_BYTE['\u0052'] = (byte) 0xD9;
        CHAR_TO_BYTE['\u00B9'] = (byte) 0xDA;
        CHAR_TO_BYTE['\u00FB'] = (byte) 0xDB;
        CHAR_TO_BYTE['\u007D'] = (byte) 0xDC;
        CHAR_TO_BYTE['\u00F9'] = (byte) 0xDD;
        CHAR_TO_BYTE['\u00FA'] = (byte) 0xDE;
        CHAR_TO_BYTE['\u00FF'] = (byte) 0xDF;
        CHAR_TO_BYTE['\u00D6'] = (byte) 0xE0;
        CHAR_TO_BYTE['\u00F7'] = (byte) 0xE1;
        CHAR_TO_BYTE['\u0053'] = (byte) 0xE2;
        CHAR_TO_BYTE['\u0054'] = (byte) 0xE3;
        CHAR_TO_BYTE['\u0055'] = (byte) 0xE4;
        CHAR_TO_BYTE['\u0056'] = (byte) 0xE5;
        CHAR_TO_BYTE['\u0057'] = (byte) 0xE6;
        CHAR_TO_BYTE['\u0058'] = (byte) 0xE7;
        CHAR_TO_BYTE['\u0059'] = (byte) 0xE8;
        CHAR_TO_BYTE['\u005A'] = (byte) 0xE9;
        CHAR_TO_BYTE['\u00B2'] = (byte) 0xEA;
        CHAR_TO_BYTE['\u00D4'] = (byte) 0xEB;
        CHAR_TO_BYTE[0x5C] = (byte) 0xEC;
        CHAR_TO_BYTE['\u00D2'] = (byte) 0xED;
        CHAR_TO_BYTE['\u00D3'] = (byte) 0xEE;
        CHAR_TO_BYTE['\u00D5'] = (byte) 0xEF;
        CHAR_TO_BYTE['\u0030'] = (byte) 0xF0;
        CHAR_TO_BYTE['\u0031'] = (byte) 0xF1;
        CHAR_TO_BYTE['\u0032'] = (byte) 0xF2;
        CHAR_TO_BYTE['\u0033'] = (byte) 0xF3;
        CHAR_TO_BYTE['\u0034'] = (byte) 0xF4;
        CHAR_TO_BYTE['\u0035'] = (byte) 0xF5;
        CHAR_TO_BYTE['\u0036'] = (byte) 0xF6;
        CHAR_TO_BYTE['\u0037'] = (byte) 0xF7;
        CHAR_TO_BYTE['\u0038'] = (byte) 0xF8;
        CHAR_TO_BYTE['\u0039'] = (byte) 0xF9;
        CHAR_TO_BYTE['\u00B3'] = (byte) 0xFA;
        CHAR_TO_BYTE['\u00DB'] = (byte) 0xFB;
        CHAR_TO_BYTE['\u005D'] = (byte) 0xFC;
        CHAR_TO_BYTE['\u00D9'] = (byte) 0xFD;
        CHAR_TO_BYTE['\u00DA'] = (byte) 0xFE;
        CHAR_TO_BYTE['\u009F'] = (byte) 0xFF;
    }

    /** Creates a new {@code IBM273CharsetProvider} instance. */
    public IBM273CharsetProvider()
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
                if ( this.hasNext )
                {
                    this.hasNext = false;
                    return new IBM273Charset();
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

/** IBM273 {@code Charset} implementation. */
class IBM273Charset extends Charset
{

    public IBM273Charset()
    {
        super( IBM273CharsetProvider.COMMON_NAME, IBM273CharsetProvider.ALIAS_NAMES );
    }

    public CharsetEncoder newEncoder()
    {
        return new IBM273CharsetEncoder( this );
    }

    public CharsetDecoder newDecoder()
    {
        return new IBM273CharsetDecoder( this );
    }

    public boolean contains( final Charset charset )
    {
        return false;
    }

    static boolean isCharacterSupported( final char c )
    {
        return ( c >= '\u0000' && c <= '\u00AE' ) || ( c >= '\u00B0' && c <= '\u00FF' ) || c == '\u203E';
    }

}

class IBM273CharsetEncoder extends CharsetEncoder
{

    private final char[] charBuf = new char[ 65536 ];

    IBM273CharsetEncoder( final Charset charset )
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

                if ( !IBM273Charset.isCharacterSupported( this.charBuf[i] ) )
                {
                    in.reset();
                    in.position( in.position() + i );
                    return CoderResult.unmappableForLength( 1 );
                }

                buf.put( IBM273CharsetProvider.CHAR_TO_BYTE[this.charBuf[i]] );
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

            if ( !IBM273Charset.isCharacterSupported( in.array()[in.position() + in.arrayOffset()] ) )
            {
                return CoderResult.unmappableForLength( 1 );
            }

            buf.array()[buf.position() + buf.arrayOffset()] =
                IBM273CharsetProvider.CHAR_TO_BYTE[in.array()[in.position() + in.arrayOffset()]];

        }

        return CoderResult.UNDERFLOW;
    }

}

class IBM273CharsetDecoder extends CharsetDecoder
{

    private final byte[] byteBuf = new byte[ 65536 ];

    IBM273CharsetDecoder( final Charset charset )
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

                buf.put( IBM273CharsetProvider.BYTE_TO_CHAR[this.byteBuf[i] & 0xFF] );
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

            buf.array()[buf.position() + buf.arrayOffset()] =
                IBM273CharsetProvider.BYTE_TO_CHAR[in.array()[in.position() + in.arrayOffset()] & 0xFF];

        }

        return CoderResult.UNDERFLOW;
    }

}

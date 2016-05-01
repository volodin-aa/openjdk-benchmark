/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package su.tweak.openjdk;

import static su.tweak.openjdk.FastString.checkBoundsOffCount;

final class FastStringUTF16 {

	public static char getChar(byte[] val, int index) {
        index <<= 1;
        return (char)(((val[index++] & 0xff) << HI_BYTE_SHIFT) |
                      ((val[index]   & 0xff) << LO_BYTE_SHIFT));
    }

    public static boolean regionMatchesCI(byte[] value, int toffset,
                                          byte[] other, int ooffset, int len) {
        int last = toffset + len;
        while (toffset < last) {
            char c1 = getChar(value, toffset++);
            char c2 = getChar(other, ooffset++);
            if (c1 == c2) {
                continue;
            }
			// Try convert c1 to upper case and compare with c2
			char u1 = Character.toUpperCase(c1);
			if (u1 == c2) {
				continue;
			}
			
			// Try convert c2 to upper case and compare with u1
			char u2 = Character.toUpperCase(c2);
			if (u1 == u2) {
				continue;
			}
            // Unfortunately, conversion to uppercase does not work properly
            // for the Georgian alphabet, which has strange rules about case
            // conversion.  So we need to make one last check before
            // exiting.
            if (Character.toLowerCase(u1) == Character.toLowerCase(u2)) {
                continue;
            }
            return false;
        }
        return true;
    }

    public static boolean regionMatchesCI_Latin1(byte[] value, int toffset,
                                                 byte[] other, int ooffset,
                                                 int len) {
        int last = toffset + len;
        while (toffset < last) {
            char c1 = getChar(value, toffset++);
            char c2 = (char)(other[ooffset++] & 0xff);
            if (c1 == c2) {
                continue;
            }
			// Try convert c1 to upper case and compare with c2
			char u1 = Character.toUpperCase(c1);
			if (u1 == c2) {
				continue;
			}
			
			// Try convert c2 to upper case and compare with u1
			char u2 = Character.toUpperCase(c2);
			if (u1 == u2) {
				continue;
			}
            if (Character.toLowerCase(u1) == Character.toLowerCase(u2)) {
                continue;
            }
            return false;
        }
        return true;
    }

    public static boolean fullMatchesCI(byte[] value,
                                          byte[] other, int len) {
        for (int i = 0; i < len; i++) {
            char c1 = getChar(value, i);
            char c2 = getChar(other, i);
            if (c1 == c2) {
                continue;
            }
			// Try convert c1 to upper case and compare with c2
			char u1 = Character.toUpperCase(c1);
			if (u1 == c2) {
				continue;
			}
			
			// Try convert c2 to upper case and compare with u1
			char u2 = Character.toUpperCase(c2);
			if (u1 == u2) {
				continue;
			}
            // Unfortunately, conversion to uppercase does not work properly
            // for the Georgian alphabet, which has strange rules about case
            // conversion.  So we need to make one last check before
            // exiting.
            if (Character.toLowerCase(u1) == Character.toLowerCase(u2)) {
                continue;
            }
            return false;
        }
        return true;
    }

    public static boolean fullMatchesCI_Latin1(byte[] value,
            byte[] other, int len) {
    	for (int i = 0; i < len; i++) {
            char c1 = getChar(value, i);
            char c2 = (char)(other[i] & 0xff);
            if (c1 == c2) {
                continue;
            }
			// Try convert c1 to upper case and compare with c2
			char u1 = Character.toUpperCase(c1);
			if (u1 == c2) {
				continue;
			}
			
			// Try convert c2 to upper case and compare with u1
			char u2 = Character.toUpperCase(c2);
			if (u1 == u2) {
				continue;
			}
            if (Character.toLowerCase(u1) == Character.toLowerCase(u2)) {
                continue;
            }
            return false;
        }
        return true;
    }
    private static boolean isBigEndian() {
    	return false;	
    }

    static final int HI_BYTE_SHIFT;
    static final int LO_BYTE_SHIFT;
    static {
        if (isBigEndian()) {
            HI_BYTE_SHIFT = 8;
            LO_BYTE_SHIFT = 0;
        } else {
            HI_BYTE_SHIFT = 0;
            LO_BYTE_SHIFT = 8;
        }
    }
    
    public static byte[] newBytesFor(int len) {
        if (len < 0) {
            throw new NegativeArraySizeException();
        }
        if (len > MAX_LENGTH) {
            throw new OutOfMemoryError("UTF16 String size is " + len +
                                       ", should be less than " + MAX_LENGTH);
        }
        return new byte[len << 1];
    }

    public static void putChar(byte[] val, int index, int c) {
        index <<= 1;
        val[index++] = (byte)(c >> HI_BYTE_SHIFT);
        val[index]   = (byte)(c >> LO_BYTE_SHIFT);
    }    
    public static byte[] toBytes(char[] value, int off, int len) {
        byte[] val = newBytesFor(len);
        for (int i = 0; i < len; i++) {
            putChar(val, i, value[off]);
            off++;
        }
        return val;
    }
    static final int MAX_LENGTH = Integer.MAX_VALUE >> 1;


    public static char[] toChars(byte[] value) {
        char[] dst = new char[value.length >> 1];
        getChars(value, 0, dst.length, dst, 0);
        return dst;
    }
    public static void getChars(byte[] value, int srcBegin, int srcEnd, char dst[], int dstBegin) {
        // We need a range check here because 'getChar' has no checks
        checkBoundsOffCount(srcBegin, srcEnd - srcBegin, value.length);
        for (int i = srcBegin; i < srcEnd; i++) {
            dst[dstBegin++] = getChar(value, i);
        }
    }    
}

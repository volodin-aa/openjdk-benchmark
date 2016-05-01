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

import static su.tweak.openjdk.Jdk9String.checkBoundsOffCount;

final class Jdk9StringLatin1 {

    public static char getChar(byte[] val, int index) {
        return (char)(val[index] & 0xff);
    }
    
	public static boolean regionMatchesCI(byte[] value, int toffset,
                                          byte[] other, int ooffset, int len) {
        int last = toffset + len;
        while (toffset < last) {
            char c1 = (char)(value[toffset++] & 0xff);
            char c2 = (char)(other[ooffset++] & 0xff);
            if (c1 == c2) {
                continue;
            }
            char u1 = Character.toUpperCase(c1);
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

    public static boolean regionMatchesCI_UTF16(byte[] value, int toffset,
                                                byte[] other, int ooffset, int len) {
        int last = toffset + len;
        while (toffset < last) {
            char c1 = (char)(value[toffset++] & 0xff);
            char c2 = Jdk9StringUTF16.getChar(other, ooffset++);
            if (c1 == c2) {
                continue;
            }
            char u1 = Character.toUpperCase(c1);
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
    private static void inflate(byte[] src, int srcOff, char[] dst, int dstOff, int len) {
        for (int i = 0; i < len; i++) {
            dst[dstOff++] = (char)(src[srcOff++] & 0xff);
        }
    }
    
    public static void inflate(byte[] src, int srcOff, byte[] dst, int dstOff, int len) {
        // We need a range check here because 'putChar' has no checks
        checkBoundsOffCount(dstOff, len, dst.length);
        for (int i = 0; i < len; i++) {
            Jdk9StringUTF16.putChar(dst, dstOff++, src[srcOff++] & 0xff);
        }
    }   
    
    public static byte[] inflate(byte[] value, int off, int len) {
        byte[] ret = Jdk9StringUTF16.newBytesFor(len);
        inflate(value, off, ret, 0, len);
        return ret;
    }
    
    public static char[] toChars(byte[] value) {
        char[] dst = new char[value.length];
        inflate(value, 0, dst, 0, value.length);
        return dst;
    }}

/*
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
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
/*
 *   Copyright (C) 2016, Volodin Andrey
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License along
 *   with this program; if not, write to the Free Software Foundation, Inc.,
 *   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package su.tweak.openjdk;

import java.io.ObjectStreamField;

/**
 * Fast equalsIgnoreCase(FastString anotherString) test
 * 
 * Based on http://hg.openjdk.java.net/jdk8/jdk8/jdk/file/687fd7c7986d/src/share/classes/java/lang/String.java
 */

public final class FastString implements java.io.Serializable, Comparable<FastString>, CharSequence {
	/** The value is used for character storage. */
    private final byte[] value;

    /**
     * The identifier of the encoding used to encode the bytes in
     * {@code value}. The supported values in this implementation are
     *
     * LATIN1
     * UTF16
     *
     * @implNote This field is trusted by the VM, and is a subject to
     * constant folding if String instance is constant. Overwriting this
     * field after construction will cause problems.
     */
    private final byte coder;

	/** Cache the hash code for the string */
	private int hash; // Default to 0

	/** use serialVersionUID from JDK 1.0.2 for interoperability */
	private static final long serialVersionUID = -6849794470754667710L;

	/**
	 * Class String is special cased within the Serialization Stream Protocol.
	 *
	 * A String instance is written into an ObjectOutputStream according to
	 * <a href="{@docRoot}/../platform/serialization/spec/output.html"> Object
	 * Serialization Specification, Section 6.2, "Stream Elements"</a>
	 */
	private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[0];

    /**
     * If String compaction is disabled, the bytes in {@code value} are
     * always encoded in UTF16.
     *
     * For methods with several possible implementation paths, when String
     * compaction is disabled, only one code path is taken.
     *
     * The instance field value is generally opaque to optimizing JIT
     * compilers. Therefore, in performance-sensitive place, an explicit
     * check of the static boolean {@code COMPACT_STRINGS} is done first
     * before checking the {@code coder} field since the static boolean
     * {@code COMPACT_STRINGS} would be constant folded away by an
     * optimizing JIT compiler. The idioms for these cases are as follows.
     *
     * For code such as:
     *
     *    if (coder == LATIN1) { ... }
     *
     * can be written more optimally as
     *
     *    if (coder() == LATIN1) { ... }
     *
     * or:
     *
     *    if (COMPACT_STRINGS && coder == LATIN1) { ... }
     *
     * An optimizing JIT compiler can fold the above conditional as:
     *
     *    COMPACT_STRINGS == true  => if (coder == LATIN1) { ... }
     *    COMPACT_STRINGS == false => if (false)           { ... }
     *
     * @implNote
     * The actual value for this field is injected by JVM. The static
     * initialization block is used to set the value here to communicate
     * that this static final field is not statically foldable, and to
     * avoid any possible circular dependency during vm initialization.
     */
    static final boolean COMPACT_STRINGS;

    static {
        COMPACT_STRINGS = true;
    }

    byte coder() {
        return COMPACT_STRINGS ? coder : UTF16;
    }

    private boolean isLatin1() {
        return COMPACT_STRINGS && coder == LATIN1;
    }

    static final byte LATIN1 = 0;
    static final byte UTF16  = 1;

	/**
	 * Initializes a newly created {@code String} object so that it represents
	 * the same sequence of characters as the argument; in other words, the
	 * newly created string is a copy of the argument string. Unless an explicit
	 * copy of {@code original} is needed, use of this constructor is
	 * unnecessary since Strings are immutable.
	 *
	 * @param original
	 *            A {@code String}
	 */
	public FastString(FastString original) {
        this.value = original.value;
        this.coder = original.coder;
        this.hash = original.hash;
	}

    /*
     * Package private constructor. Trailing Void argument is there for
     * disambiguating it against other (public) constructors.
     *
     * Stores the char[] value into a byte[] that each byte represents
     * the8 low-order bits of the corresponding character, if the char[]
     * contains only latin1 character. Or a byte[] that stores all
     * characters in their byte sequences defined by the {@code FastStringUTF16}.
     */
	FastString(char[] value, int off, int len) {
        this.coder = UTF16;
        this.value = FastStringUTF16.toBytes(value, off, len);
    }

	/**
	 * Allocates a new {@code String} so that it represents the sequence of
	 * characters currently contained in the character array argument. The
	 * contents of the character array are copied; subsequent modification of
	 * the character array does not affect the newly created string.
	 *
	 * @param value
	 *            The initial value of the string
	 */
    public FastString(char value[]) {
        this(value, 0, value.length);
    }

	/**
	 * Compares this {@code String} to another {@code String}, ignoring case
	 * considerations. Two strings are considered equal ignoring case if they
	 * are of the same length and corresponding characters in the two strings
	 * are equal ignoring case.
	 *
	 * <p>
	 * Two characters {@code c1} and {@code c2} are considered the same ignoring
	 * case if at least one of the following is true:
	 * <ul>
	 * <li>The two characters are the same (as compared by the {@code ==}
	 * operator)
	 * <li>Applying the method {@link java.lang.Character#toUpperCase(char)} to
	 * each character produces the same result
	 * <li>Applying the method {@link java.lang.Character#toLowerCase(char)} to
	 * each character produces the same result
	 * </ul>
	 *
	 * @param anotherString
	 *            The {@code String} to compare this {@code String} against
	 *
	 * @return {@code true} if the argument is not {@code null} and it
	 *         represents an equivalent {@code String} ignoring case; {@code
	 *          false} otherwise
	 *
	 * @see #equals(Object)
	 */
	public boolean equalsIgnoreCase(FastString anotherString) {
		return (this == anotherString) ? true
				: (anotherString != null) 
				&& (anotherString.value.length == value.length) 
				&& fullMatches(anotherString, length());
	}

	/**
	 * Tests if two string regions are equal.
	 * <p>
	 * A substring of this {@code String} object is compared to a substring of
	 * the argument {@code other}. The result is {@code true} if these
	 * substrings represent character sequences that are the same, ignoring case
	 * if and only if {@code ignoreCase} is true. The substring of this
	 * {@code String} object to be compared begins at index {@code toffset} and
	 * has length {@code len}. The substring of {@code other} to be compared
	 * begins at index {@code ooffset} and has length {@code len}. The result is
	 * {@code false} if and only if at least one of the following is true:
	 * <ul>
	 * <li>{@code toffset} is negative.
	 * <li>{@code ooffset} is negative.
	 * <li>{@code toffset+len} is greater than the length of this {@code String}
	 * object.
	 * <li>{@code ooffset+len} is greater than the length of the other argument.
	 * <li>{@code ignoreCase} is {@code false} and there is some nonnegative
	 * integer <i>k</i> less than {@code len} such that: <blockquote>
	 * 
	 * <pre>
	 * this.charAt(toffset + k) != other.charAt(ooffset + k)
	 * </pre>
	 * 
	 * </blockquote>
	 * <li>{@code ignoreCase} is {@code true} and there is some nonnegative
	 * integer <i>k</i> less than {@code len} such that: <blockquote>
	 * 
	 * <pre>
	 * Character.toLowerCase(this.charAt(toffset + k)) != Character.toLowerCase(other.charAt(ooffset + k))
	 * </pre>
	 * 
	 * </blockquote> and: <blockquote>
	 * 
	 * <pre>
	 * Character.toUpperCase(this.charAt(toffset + k)) != Character.toUpperCase(other.charAt(ooffset + k))
	 * </pre>
	 * 
	 * </blockquote>
	 * </ul>
	 *
	 * @param ignoreCase
	 *            if {@code true}, ignore case when comparing characters.
	 * @param toffset
	 *            the starting offset of the subregion in this string.
	 * @param other
	 *            the string argument.
	 * @param ooffset
	 *            the starting offset of the subregion in the string argument.
	 * @param len
	 *            the number of characters to compare.
	 * @return {@code true} if the specified subregion of this string matches
	 *         the specified subregion of the string argument; {@code false}
	 *         otherwise. Whether the matching is exact or case insensitive
	 *         depends on the {@code ignoreCase} argument.
	 */
	/*public boolean ignoreCaseMatches(FastString other) {
		char ta[] = value;
		char pa[] = other.value;
		for (int i = 0; i < value.length; i++) {
			char c1 = ta[i];
			char c2 = pa[i];
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
			// conversion. So we need to make one last check before
			// exiting.
			if (Character.toLowerCase(u1) == Character.toLowerCase(u2)) {
				continue;
			}
			return false;
		}
		return true;
	}
*/
	@Override
	public char charAt(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

    /**
     * Tests if two string regions are equal.
     * <p>
     * A substring of this {@code String} object is compared to a substring
     * of the argument other. The result is true if these substrings
     * represent identical character sequences. The substring of this
     * {@code String} object to be compared begins at index {@code toffset}
     * and has length {@code len}. The substring of other to be compared
     * begins at index {@code ooffset} and has length {@code len}. The
     * result is {@code false} if and only if at least one of the following
     * is true:
     * <ul><li>{@code toffset} is negative.
     * <li>{@code ooffset} is negative.
     * <li>{@code toffset+len} is greater than the length of this
     * {@code String} object.
     * <li>{@code ooffset+len} is greater than the length of the other
     * argument.
     * <li>There is some nonnegative integer <i>k</i> less than {@code len}
     * such that:
     * {@code this.charAt(toffset + }<i>k</i>{@code ) != other.charAt(ooffset + }
     * <i>k</i>{@code )}
     * </ul>
     *
     * <p>Note that this method does <em>not</em> take locale into account.  The
     * {@link java.text.Collator} class provides locale-sensitive comparison.
     *
     * @param   toffset   the starting offset of the subregion in this string.
     * @param   other     the string argument.
     * @param   ooffset   the starting offset of the subregion in the string
     *                    argument.
     * @param   len       the number of characters to compare.
     * @return  {@code true} if the specified subregion of this string
     *          exactly matches the specified subregion of the string argument;
     *          {@code false} otherwise.
     */
    public boolean regionMatches(int toffset, FastString other, int ooffset, int len) {
        byte tv[] = value;
        byte ov[] = other.value;
        // Note: toffset, ooffset, or len might be near -1>>>1.
        if ((ooffset < 0) || (toffset < 0) ||
             (toffset > (long)length() - len) ||
             (ooffset > (long)other.length() - len)) {
            return false;
        }
        if (coder() == other.coder()) {
            if (!isLatin1() && (len > 0)) {
                toffset = toffset << 1;
                ooffset = ooffset << 1;
                len = len << 1;
            }
            while (len-- > 0) {
                if (tv[toffset++] != ov[ooffset++]) {
                    return false;
                }
            }
        } else {
            if (coder() == LATIN1) {
                while (len-- > 0) {
                    if (FastStringLatin1.getChar(tv, toffset++) !=
                        FastStringUTF16.getChar(ov, ooffset++)) {
                        return false;
                    }
                }
            } else {
                while (len-- > 0) {
                    if (FastStringUTF16.getChar(tv, toffset++) !=
                        FastStringLatin1.getChar(ov, ooffset++)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Tests if two string regions are equal.
     * <p>
     * A substring of this {@code String} object is compared to a substring
     * of the argument {@code other}. The result is {@code true} if these
     * substrings represent character sequences that are the same, ignoring
     * case if and only if {@code ignoreCase} is true. The substring of
     * this {@code String} object to be compared begins at index
     * {@code toffset} and has length {@code len}. The substring of
     * {@code other} to be compared begins at index {@code ooffset} and
     * has length {@code len}. The result is {@code false} if and only if
     * at least one of the following is true:
     * <ul><li>{@code toffset} is negative.
     * <li>{@code ooffset} is negative.
     * <li>{@code toffset+len} is greater than the length of this
     * {@code String} object.
     * <li>{@code ooffset+len} is greater than the length of the other
     * argument.
     * <li>{@code ignoreCase} is {@code false} and there is some nonnegative
     * integer <i>k</i> less than {@code len} such that:
     * <blockquote><pre>
     * this.charAt(toffset+k) != other.charAt(ooffset+k)
     * </pre></blockquote>
     * <li>{@code ignoreCase} is {@code true} and there is some nonnegative
     * integer <i>k</i> less than {@code len} such that:
     * <blockquote><pre>
     * Character.toLowerCase(Character.toUpperCase(this.charAt(toffset+k))) !=
     Character.toLowerCase(Character.toUpperCase(other.charAt(ooffset+k)))
     * </pre></blockquote>
     * </ul>
     *
     * <p>Note that this method does <em>not</em> take locale into account,
     * and will result in unsatisfactory results for certain locales when
     * {@code ignoreCase} is {@code true}.  The {@link java.text.Collator} class
     * provides locale-sensitive comparison.
     *
     * @param   ignoreCase   if {@code true}, ignore case when comparing
     *                       characters.
     * @param   toffset      the starting offset of the subregion in this
     *                       string.
     * @param   other        the string argument.
     * @param   ooffset      the starting offset of the subregion in the string
     *                       argument.
     * @param   len          the number of characters to compare.
     * @return  {@code true} if the specified subregion of this string
     *          matches the specified subregion of the string argument;
     *          {@code false} otherwise. Whether the matching is exact
     *          or case insensitive depends on the {@code ignoreCase}
     *          argument.
     */
    public boolean regionMatches(boolean ignoreCase, int toffset,
    		FastString other, int ooffset, int len) {
        if (!ignoreCase) {
            return regionMatches(toffset, other, ooffset, len);
        }
        // Note: toffset, ooffset, or len might be near -1>>>1.
        if ((ooffset < 0) || (toffset < 0)
                || (toffset > (long)length() - len)
                || (ooffset > (long)other.length() - len)) {
            return false;
        }
        byte tv[] = value;
        byte ov[] = other.value;
        if (coder() == other.coder()) {
            return isLatin1()
              ? FastStringLatin1.regionMatchesCI(tv, toffset, ov, ooffset, len)
              : FastStringUTF16.regionMatchesCI(tv, toffset, ov, ooffset, len);
        }
        return isLatin1()
              ? FastStringLatin1.regionMatchesCI_UTF16(tv, toffset, ov, ooffset, len)
              : FastStringUTF16.regionMatchesCI_Latin1(tv, toffset, ov, ooffset, len);
    }

    public boolean fullMatches(FastString other, int len) {
        byte tv[] = value;
        byte ov[] = other.value;
        if (coder() == other.coder()) {
            return isLatin1()
              ? FastStringLatin1.fullMatchesCI(tv, ov, len)
              : FastStringUTF16.fullMatchesCI(tv, ov, len);
        }
        return isLatin1()
              ? FastStringLatin1.fullMatchesCI_UTF16(tv, ov, len)
              : FastStringUTF16.fullMatchesCI_Latin1(tv, ov, len);
    }

	@Override
	public int length() {
        return value.length >> coder();
	}

	@Override
	public CharSequence subSequence(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(FastString arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

    public char[] toCharArray() {
        return isLatin1() ? FastStringLatin1.toChars(value)
                          : FastStringUTF16.toChars(value);
    }

    /*
     * Check {@code offset}, {@code count} against {@code 0} and {@code length}
     * bounds.
     *
     * @throws  StringIndexOutOfBoundsException
     *          If {@code offset} is negative, {@code count} is negative,
     *          or {@code offset} is greater than {@code length - count}
     */
    static void checkBoundsOffCount(int offset, int count, int length) {
        if (offset < 0 || count < 0 || offset > length - count) {
            throw new StringIndexOutOfBoundsException(
                "offset " + offset + ", count " + count + ", length " + length);
        }
    }
}

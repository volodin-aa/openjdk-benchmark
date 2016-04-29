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
import java.util.Arrays;

/**
 * Fast equalsIgnoreCase(FastString anotherString) test
 * 
 * Based on http://hg.openjdk.java.net/jdk8/jdk8/jdk/file/687fd7c7986d/src/share/classes/java/lang/String.java
 */

public final class FastString implements java.io.Serializable, Comparable<FastString>, CharSequence {
	/** The value is used for character storage. */
	private final char value[];

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
	 * Initializes a newly created {@code String} object so that it represents
	 * an empty character sequence. Note that use of this constructor is
	 * unnecessary since Strings are immutable.
	 */
	public FastString() {
		this.value = new char[0];
	}

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
		this.hash = original.hash;
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
		this.value = Arrays.copyOf(value, value.length);
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
				: (anotherString != null) && (anotherString.value.length == value.length) && ignoreCaseMatches(anotherString);
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
	public boolean ignoreCaseMatches(FastString other) {
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

	@Override
	public char charAt(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int length() {
		// TODO Auto-generated method stub
		return 0;
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

    /**
     * Converts this string to a new character array.
     *
     * @return  a newly allocated character array whose length is the length
     *          of this string and whose contents are initialized to contain
     *          the character sequence represented by this string.
     */
    public char[] toCharArray() {
        // Cannot use Arrays.copyOf because of class initialization order issues
        char result[] = new char[value.length];
        System.arraycopy(value, 0, result, 0, value.length);
        return result;
    }
}

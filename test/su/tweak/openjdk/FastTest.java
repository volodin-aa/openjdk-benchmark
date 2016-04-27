/*
 *   Copyright (C) 2016  Volodin Andrey
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

import static org.junit.Assert.*;

import org.junit.Test;

public class FastTest {

	private static final String _STRING = "String";
	private static final String STRING = "string";

	private static final FastString _FAST_STRING = new FastString(_STRING.toCharArray());
	private static final FastString FAST_STRING = new FastString(STRING.toCharArray());
	private static final FastString FAST_STRING_2 = new FastString((STRING + "2").toCharArray());
	private static final FastString FAST_STRING_3 = new FastString((STRING + "3").toCharArray());
	
	@Test
	public void equalTest() {
		assertTrue(_FAST_STRING.equalsIgnoreCase(FAST_STRING));
		assertTrue(_FAST_STRING.equalsIgnoreCase(_FAST_STRING));
	}
	
	@Test
	public void notEqualTest() {
		assertFalse(_FAST_STRING.equalsIgnoreCase(FAST_STRING_2));
		assertFalse(FAST_STRING_2.equalsIgnoreCase(FAST_STRING_3));
	}

}

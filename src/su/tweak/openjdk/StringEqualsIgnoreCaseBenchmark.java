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

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class StringEqualsIgnoreCaseBenchmark {

	private static final String SELF_TEST_MESSAGE = "%s: \"%s\".equalsIgnoreCase(\"%s\") == %s";
	// private static final String _STRING = "STRING";
	private static final String _STRING = "String";
	private static final String STRING = "string";

	private static String _string = new String(_STRING.toCharArray());
	private static String string = new String(STRING.toCharArray());

	private static final FastString _FAST_STRING = new FastString(_STRING.toCharArray());
	private static final FastString FAST_STRING = new FastString(STRING.toCharArray());

	private static FastString _fastString = new FastString(_STRING.toCharArray());
	private static FastString fastString = new FastString(STRING.toCharArray());

	private static Jdk9String _jdk9String = new Jdk9String(_STRING.toCharArray());
	private static Jdk9String jdk9String = new Jdk9String(STRING.toCharArray());

	private static final Jdk9String _JDK9_STRING = new Jdk9String(_STRING.toCharArray());
	private static final Jdk9String JDK9_STRING = new Jdk9String(STRING.toCharArray());

	/*
	 * const.equalsIgnoreCase(Const)
	 * 
	 */
	@Benchmark
	public boolean constConstString() {
		return STRING.equalsIgnoreCase(_STRING);
	}

	/*
	 * var.equalsIgnoreCase(Var)
	 * 
	 */
	@Benchmark
	public boolean varVarString() {
		return string.equalsIgnoreCase(_string);
	}

	/*
	 * new var.equalsIgnoreCase(new Var)
	 * 
	 */
	@Benchmark
	public boolean newNewString() {
		return new String(string.toCharArray()).equalsIgnoreCase(new String(_string.toCharArray()));
	}

	/*
	 * const.equalsIgnoreCase(Const)
	 * 
	 */
	@Benchmark
	public boolean constConstFast() {
		return FAST_STRING.equalsIgnoreCase(_FAST_STRING);
	}

	/*
	 * var.equalsIgnoreCase(Var)
	 * 
	 */
	@Benchmark
	public boolean varVarFast() {
		return fastString.equalsIgnoreCase(_fastString);
	}

	/*
	 * new var.equalsIgnoreCase(new Var)
	 * 
	 */
	@Benchmark
	public boolean newNewFast() {
		return new FastString(fastString.toCharArray()).equalsIgnoreCase(new FastString(_fastString.toCharArray()));
	}

	/*
	 * const.equalsIgnoreCase(Const)
	 * 
	 */
	@Benchmark
	public boolean constConstJdk9() {
		return JDK9_STRING.equalsIgnoreCase(_JDK9_STRING);
	}

	/*
	 * var.equalsIgnoreCase(Var)
	 * 
	 */
	@Benchmark
	public boolean varVarJdk9() {
		return jdk9String.equalsIgnoreCase(_jdk9String);
	}

	/*
	 * new var.equalsIgnoreCase(new Var)
	 * 
	 */
	@Benchmark
	public boolean newNewJdk9() {
		return new Jdk9String(jdk9String.toCharArray()).equalsIgnoreCase(new Jdk9String(_jdk9String.toCharArray()));
	}

	/*
	 * new var.equalsIgnoreCase(new Var)
	 * 
	 */
	@Benchmark
	public int initNewString() {
		return (new String(string.toCharArray())).length() + (new String(_string.toCharArray())).length();
	}

	/*
	 * new var.equalsIgnoreCase(new Var)
	 * 
	 */
	@Benchmark
	public int initNewFast() {
		return (new FastString(fastString.toCharArray())).length() + (new FastString(_fastString.toCharArray())).length();
	}

	/*
	 * new var.equalsIgnoreCase(new Var)
	 * 
	 */
	@Benchmark
	public int initNewJdk9() {
		return (new Jdk9String(jdk9String.toCharArray())).length() + (new Jdk9String(_jdk9String.toCharArray())).length();
	}

	/*
	 * mvn clean install exec:exec
	 */
	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder().include(StringEqualsIgnoreCaseBenchmark.class.getSimpleName()).forks(1)
				.build();

		new Runner(opt).run();

		System.out.println("------------------------------------------------------------------------");
		System.out.println("Self test:");
		System.out.println(String.format(SELF_TEST_MESSAGE, "String", STRING, _STRING,
				new Boolean(STRING.equalsIgnoreCase(_STRING))));
		System.out.println(String.format(SELF_TEST_MESSAGE, "FastString", STRING, _STRING, new Boolean(
				new FastString(fastString.toCharArray()).equalsIgnoreCase(new FastString(_fastString.toCharArray())))));
		System.out.println(String.format(SELF_TEST_MESSAGE, "Jdk9String", STRING, _STRING, new Boolean(
				new Jdk9String(jdk9String.toCharArray()).equalsIgnoreCase(new Jdk9String(_jdk9String.toCharArray())))));
	}

}
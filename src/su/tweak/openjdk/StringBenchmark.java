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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class StringBenchmark {

    private static final String _STRING = "STRING";
    private static final String STRING = "string";

    private static String _string = new String(_STRING.toCharArray());
    private static String string = new String(STRING.toCharArray());

    private static final FastString _FAST_STRING = new FastString(_string.toCharArray());
    private static final FastString FAST_STRING = new FastString(string.toCharArray());

    private static FastString _fastString = new FastString(_string.toCharArray());
    private static FastString fastString = new FastString(string.toCharArray());

    /*
     * const.equalsIgnoreCase(Const)
     * 
     */
    @Benchmark
    public boolean constConst() {
        return STRING.equalsIgnoreCase(_STRING);
    }

    /*
     * var.equalsIgnoreCase(Var)
     * 
     */
    @Benchmark
    public boolean varVar() {
        return string.equalsIgnoreCase(_string);
    }

    /*
     * new var.equalsIgnoreCase(new Var)
     * 
     */
    @Benchmark
    public boolean newNew() {
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
     * mvn clean install exec:exec
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder().include(StringBenchmark.class.getSimpleName()).forks(1).build();

        new Runner(opt).run();
    }

}
/**
 * Copyright (c) 2011-2013 Armin Töpfer
 *
 * This file is part of InDelFixer.
 *
 * InDelFixer is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 *
 * InDelFixer is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * InDelFixer. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.ethz.bsse.indelfixer.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class StatusUpdate {

    private static final long start = System.currentTimeMillis();
    private static final DateFormat df;
    public static int readCount = 0;
    public static int unmappedCount = 0;
    public static int tooSmallCount = 0;
    public static int alignCount1 = 0;
    public static int alignCount2 = 0;
    public static int alignCount3 = 0;
    private static String oldOut = "";

    static {
        df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static void print(String text, double percentage) {
        System.out.print("\r" + time() + "  " + text + Math.round(percentage * 100) / 100 + "%");
    }

    public static void print(String text) {
        System.out.print("\r" + time() + "  " + text);
    }

    public synchronized static void processReads() {
        readCount++;
        if (!oldOut.equals(time())) {
            oldOut = time();
            System.out.print("\r                                                                                                                                                               ");
            System.out.print("\r" + time() + " Mapped: " + readCount + "\t\tUnmapped: " + unmappedCount + "\t\tToo small: " + tooSmallCount + "\t\tPre align: " + alignCount1 + "\t\tIn align: " + alignCount3 + "\t\tPost align: " + alignCount2);
        }
    }

    public synchronized static void processUnmapped() {
        ++unmappedCount;
        if (!oldOut.equals(time())) {
            oldOut = time();
            System.out.print("\r                                                                                                                                                               ");
            System.out.print("\r" + time() + " Mapped: " + (readCount) + "\t\tUnmapped: " + unmappedCount + "\t\tToo small: " + tooSmallCount + "\t\tPre align: " + alignCount1 + "\t\tIn align: " + alignCount3 + "\t\tPost align: " + alignCount2);
        }
    }

    public synchronized static void processLength() {
        ++tooSmallCount;
        if (!oldOut.equals(time())) {
            oldOut = time();
            System.out.print("\r                                                                                                                                                               ");
            System.out.print("\r" + time() + " Mapped: " + (readCount) + "\t\tUnmapped: " + unmappedCount + "\t\tToo small: " + tooSmallCount + "\t\tPre align: " + alignCount1 + "\t\tIn align: " + alignCount3 + "\t\tPost align: " + alignCount2);
        }
    }

    public synchronized static void processAlign1() {
        ++alignCount1;
        if (!oldOut.equals(time())) {
            oldOut = time();
            System.out.print("\r                                                                                                                                                               ");
        }
        System.out.print("\r" + time() + " Mapped: " + (readCount) + "\t\tUnmapped: " + unmappedCount + "\t\tToo small: " + tooSmallCount + "\t\tPre align: " + alignCount1 + "\t\tIn align: " + alignCount3 + "\t\tPost align: " + alignCount2);
    }

    public synchronized static void processAlign2() {
        ++alignCount2;
        if (!oldOut.equals(time())) {
            oldOut = time();
            System.out.print("\r                                                                                                                                                               ");
            System.out.print("\r" + time() + " Mapped: " + (readCount) + "\t\tUnmapped: " + unmappedCount + "\t\tToo small: " + tooSmallCount + "\t\tPre align: " + alignCount1 + "\t\tIn align: " + alignCount3 + "\t\tPost align: " + ++alignCount2);
        }
    }

    public synchronized static void processAlign3() {
        ++alignCount3;
        if (!oldOut.equals(time())) {
            oldOut = time();
            System.out.print("\r                                                                                                                                                               ");
            System.out.print("\r" + time() + " Mapped: " + (readCount) + "\t\tUnmapped: " + unmappedCount + "\t\tToo small: " + tooSmallCount + "\t\tPre align: " + alignCount1 + "\t\tIn align: " + alignCount3 + "\t\tPost align: " + alignCount2);
        }
    }

    public static void println(String text) {
        System.out.println("\r" + time() + "  " + text);
    }

    private static String time() {
        return df.format(new Date(System.currentTimeMillis() - start));
    }
}

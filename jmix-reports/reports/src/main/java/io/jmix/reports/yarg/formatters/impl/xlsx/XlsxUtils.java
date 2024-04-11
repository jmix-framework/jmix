/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jmix.reports.yarg.formatters.impl.xlsx;

public final class XlsxUtils {

    private static final long PX_PER_INCH = 96;
    private static final long EMU_PER_INCH = 914400;

    private XlsxUtils() {
    }

    public static int getNumberFromColumnReference(String columnReference) {
        int sum = 0;

        for (int i = 0; i < columnReference.length(); i++) {
            char c = columnReference.charAt(i);
            int number = ((int) c) - 64 - 1;

            int pow = columnReference.length() - i - 1;
            sum += Math.pow(26, pow) * (number + 1);
        }
        return sum;
    }

    public static String getColumnReferenceFromNumber(int number) {
        int remain = 0;
        StringBuilder ref = new StringBuilder();
        do {

            remain = (number - 1) % 26;
            number = (number - 1) / 26;

            ref.append((char) (remain + 64 + 1));
        } while (number > 0);

        return ref.reverse().toString();
    }

    public static long convertPxToEmu(long px) {
        return px * EMU_PER_INCH / PX_PER_INCH;
    }
}
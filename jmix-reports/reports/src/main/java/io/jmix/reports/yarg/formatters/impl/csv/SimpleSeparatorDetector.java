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

package io.jmix.reports.yarg.formatters.impl.csv;

import io.jmix.reports.yarg.exception.ReportFormattingException;
import com.opencsv.CSVWriter;

import java.util.regex.Matcher;

import static io.jmix.reports.yarg.formatters.impl.AbstractFormatter.UNIVERSAL_ALIAS_PATTERN;

public class SimpleSeparatorDetector {
    public static char detectSeparator(String line) {
        StringBuffer buffer = new StringBuffer();
        Matcher matcher = UNIVERSAL_ALIAS_PATTERN.matcher(line);
        while (matcher.find()) {
            matcher.appendReplacement(buffer, "");
        }
        matcher.appendTail(buffer);

        String separateDetection = buffer.toString().replaceAll("[^,;|\\t]*", "");
        if (separateDetection == null)
            throw new ReportFormattingException("Error while detecting a separator");

        if (!separateDetection.isEmpty())
            return separateDetection.charAt(0);
        else
            return CSVWriter.DEFAULT_SEPARATOR;
    }
}
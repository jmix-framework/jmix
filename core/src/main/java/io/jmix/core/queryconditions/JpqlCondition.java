/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.core.queryconditions;

import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Condition that represents parts of a JPQL query: "where" and optional "join".
 */
public class JpqlCondition extends PropertyCondition {

    public static final Pattern PARAMETER_PATTERN = Pattern.compile(":([\\w.$]+)");

    public static JpqlCondition where(String join, String where) {
        return new JpqlCondition(join, where);
    }

    public static JpqlCondition where(String where) {
        return new JpqlCondition(where);
    }

    public JpqlCondition(List<Entry> entries) {
        super(entries);
    }

    public JpqlCondition(@Nullable String join, String where) {
        super(makeEntries(join, where));
    }

    public JpqlCondition(String where) {
        super(makeEntries(null, where));
    }

    private static List<Entry> makeEntries(@Nullable String join, String where) {
        List<Entry> list = new ArrayList<>();
        if (!Strings.isNullOrEmpty(join))
            list.add(new Entry("join", join));
        if (!Strings.isNullOrEmpty(where))
            list.add(new Entry("where", where));
        return list;
    }

    @Override
    protected void parseParameters() {
        for (Entry entry : entries) {
            Matcher matcher = PARAMETER_PATTERN.matcher(entry.value);
            while (matcher.find()) {
                String parameter = matcher.group(1);
                if (!parameters.contains(parameter))
                    parameters.add(parameter);
            }
        }
    }

    @Override
    public Condition copy() {
        List<Entry> entriesCopy = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            entriesCopy.add(new Entry(entry.name, entry.value));
        }
        return new JpqlCondition(entriesCopy);
    }
}

/*
 * Copyright 2025 Haulmont.
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

package io.jmix.search.index.mapping.processor.impl.dynattr;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The {@code PatternsMatcher} class provides functionality for resolving patterns and identifying
 * matching elements within a collection based on wildcard patterns. It supports processing of string
 * patterns that may include wildcards (e.g., "*") for flexible and dynamic matching.
 */
@Component("search_PatternsMatcher")
public class PatternsMatcher {

    public <T> Collection<T> getMatchingElements(Map<String, T> namedElements, List<String> patternsForCheck) {
        ArrayList<T> result = new ArrayList<>();
        namedElements.forEach((key, value) -> {
            if (getMatchingElementsForSinglePattern(patternsForCheck, key)) {
                result.add(value);
            }
        });
        return result;
    }

    protected boolean getMatchingElementsForSinglePattern(List<String> excludedNames, String patternForCheck) {
        for (String excludingName : excludedNames) {
            if (!hasWildCard(excludingName)) {
                if (patternForCheck.equals(excludingName)) {
                    return true;
                }
            } else {
                Pattern pattern = Pattern.compile(excludingName.replace("*", ".*"));
                if (pattern.matcher(patternForCheck).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static boolean hasWildCard(String patternForCheck) {
        return patternForCheck.contains("*");
    }
}

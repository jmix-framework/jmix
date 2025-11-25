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
 * Provides functionality for resolving patterns and identifying
 * matching elements within a collection based on wildcard patterns. It supports processing of string
 * patterns that may include wildcards "*" for flexible and dynamic matching.
 */
@Component("search_WildcardPatternsMatcher")
public class WildcardPatternsMatcher {

    /**
     * Returns a collection of elements which names match any pattern of the given patterns list.
     * Each pattern can contain one or more wildcard symbols "*".
     * @param namedElements the map with the elements and its names
     * @param patterns the list of patterns.
     * @return the list of filtered elements
     * @param <T> the type of named elements.
     */
    public <T> Collection<T> getMatchingElements(Map<String, T> namedElements, List<String> patterns) {
        List<T> result = new ArrayList<>();
        namedElements.forEach((key, value) -> {
            if (getMatchingElementsForSingleNamedElement(key, patterns)) {
                result.add(value);
            }
        });
        return result;
    }

    protected boolean getMatchingElementsForSingleNamedElement(String elementName, List<String> patterns) {
        for (String pattern : patterns) {
            if (!hasWildCard(pattern)) {
                if (elementName.equals(pattern)) {
                    return true;
                }
            } else {
                Pattern compiledPattern = Pattern.compile(pattern.replace("*", ".*"));
                if (compiledPattern.matcher(elementName).matches()) {
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

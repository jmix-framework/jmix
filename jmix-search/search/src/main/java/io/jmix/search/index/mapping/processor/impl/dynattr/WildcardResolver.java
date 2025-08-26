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

@Component("search_WildcardResolver")
public class WildcardResolver {
    public <T> Collection<T> getMatchingElements(Map<String, T> elementsWithNames, List<String> excludedNames) {
        ArrayList<T> result = new ArrayList<>();
        elementsWithNames.forEach((key, value) -> {
            if (checkHit(excludedNames, key)) {
                result.add(value);
            }
        });
        return result;
    }

    protected boolean checkHit(List<String> excludedNames, String nameToCheck) {
        for (String excludingName : excludedNames) {
            if (!isWithWildCard(excludingName)) {
                if (nameToCheck.equals(excludingName)) {
                    return true;
                }
            } else {
                Pattern pattern = Pattern.compile(excludingName.replace("*", ".*"));
                if (pattern.matcher(nameToCheck).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isWithWildCard(String excludingName) {
        return excludingName.contains("*");
    }
}

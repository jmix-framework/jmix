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

import io.jmix.search.exception.IndexConfigurationException;
import io.jmix.search.index.mapping.DynamicAttributesGroupConfiguration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

import static io.jmix.search.index.mapping.processor.impl.dynattr.DynamicAttributesGroupConfigurationValidator.ArgumentType.*;

/**
 * A component responsible for validating the configuration of dynamic attribute groups.
 * This class performs checks on category names and attribute names to ensure they comply
 * with the required rules and constraints.
 */
@Component("search_DynamicAttributesGroupConfigurationChecker")
public class DynamicAttributesGroupConfigurationValidator {

    protected static List<String> forbiddenSymbols = List.of("+", ".");

    private static boolean allCharsEqual(String str, char c) {
        if (str == null || str.isEmpty()) {
            return true; // Или false, в зависимости от требований
        }
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != c) {
                return false;
            }
        }
        return true;
    }

    public void check(DynamicAttributesGroupConfiguration group) {
        Stream.of(group.getExcludedCategories()).forEach(this::checkCategory);
        Stream.of(group.getExcludedProperties()).forEach(this::checkAttribute);
    }

    protected void checkCategory(String categoryName) {
        checkIsNotBlank(categoryName, CATEGORY);
        checkForbiddenSymbols(categoryName, CATEGORY);
        checkNotWildcardOnly(categoryName, CATEGORY);
    }

    protected void checkAttribute(String attributeName) {
        checkIsNotBlank(attributeName, ATTRIBUTE);
        checkForbiddenSymbols(attributeName, ATTRIBUTE);
        checkNotWildcardOnly(attributeName, ATTRIBUTE);
    }

    protected void checkForbiddenSymbols(String name, ArgumentType argumentType) {
        forbiddenSymbols.forEach(symbol -> {
            if (name.contains(symbol)) {
                throw new IndexConfigurationException(String.format("The '%s' symbol is denied in the %s name. %s name value is '%s'",
                        symbol,
                        argumentType.name,
                        argumentType.nameWithCapitalLetter,
                        name));
            }
        });
    }

    protected void checkIsNotBlank(String name, ArgumentType argumentType) {
        if (name.isBlank()) {
            throw new IndexConfigurationException(String.format("%s name can't be empty", argumentType.nameWithCapitalLetter));
        }
    }

    protected void checkNotWildcardOnly(String name, ArgumentType argumentType) {
        if (allCharsEqual(name, '*')) {
            throw new IndexConfigurationException(String.format("%s name can't be a wildcard without any text. " +
                    "But wildcards like '*abc', 'abc*', 'a*b*c' are supported.", argumentType.nameWithCapitalLetter));
        }
    }

    protected enum ArgumentType {
        CATEGORY("category", "Category"),
        ATTRIBUTE("attribute", "Attribute");

        private final String name;
        private final String nameWithCapitalLetter;

        ArgumentType(String name, String nameWithCapitalLetter) {
            this.name = name;
            this.nameWithCapitalLetter = nameWithCapitalLetter;
        }
    }
}

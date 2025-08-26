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

import io.jmix.search.index.mapping.DynamicAttributesConfigurationGroup;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

import static io.jmix.search.index.mapping.processor.impl.dynattr.DynamicAttributesConfigurationGroupChecker.ArgumentType.*;

@Component("search_DynamicAttributesConfigurationGroupChecker")
public class DynamicAttributesConfigurationGroupChecker {
    protected static List<String> deniedSymbols = List.of("+", ".");

    public void check(DynamicAttributesConfigurationGroup group) {
        Stream.of(group.getExcludedCategories()).forEach(this::checkCategory);
        Stream.of(group.getExcludedProperties()).forEach(this::checkAttribute);
    }

    protected void checkCategory(String categoryName) {
        checkIsNotEmpty(categoryName, CATEGORY);
        checkDeniedSymbols(categoryName, CATEGORY);
        checkThatNotIsTheWildcard(categoryName, CATEGORY);
    }

    protected void checkAttribute(String attributeName) {
        checkIsNotEmpty(attributeName, ATTRIBUTE);
        checkDeniedSymbols(attributeName, ATTRIBUTE);
        checkThatNotIsTheWildcard(attributeName, ATTRIBUTE);
    }

    protected void checkDeniedSymbols(String name, ArgumentType argumentType) {
        deniedSymbols.forEach(symbol -> {
            if (name.contains(symbol)) {
                throw new IllegalStateException(String.format("The '%s' symbol is denied in the %s name. %s name value is '%s'",
                        symbol,
                        argumentType.name,
                        argumentType.nameWithCapitalLetter,
                        name));
            }
        });
    }

    private void checkIsNotEmpty(String name, ArgumentType argumentType) {
        if("".equals(name)){
            throw new IllegalStateException(String.format("%s name can't be empty", argumentType.nameWithCapitalLetter));
        }
    }

    protected void checkThatNotIsTheWildcard(String name, ArgumentType argumentType) {
        if ("*".equals(name)){
            throw new IllegalStateException(String.format("%s name can't be a wildcard without any text. But wildcards like '*abc', 'abc*', 'a*b*c' are supported.", argumentType.nameWithCapitalLetter));
        }
    }

    protected enum ArgumentType{
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

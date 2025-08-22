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

@Component
public class DynamicAttributesConfigurationGroupChecker {
    protected static List<String> deniedSymbols = List.of("+", "*", ".");

    public void check(DynamicAttributesConfigurationGroup group) {
        Stream.of(group.getExcludedCategories()).forEach(this::checkCategory);
        Stream.of(group.getExcludedProperties()).forEach(this::checkAttribute);
    }

    protected void checkCategory(String categoryName) {
        deniedSymbols.forEach(symbol-> {
            if (categoryName.contains(symbol)) {
                throw new IllegalStateException(String.format("The '%s' symbol is denied in the category name", symbol));
            }
        });
    }

    protected void checkAttribute(String attributeName) {

    }

}

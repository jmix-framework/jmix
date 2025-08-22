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

package io.jmix.search.index.mapping.processor.impl.dynattr

import spock.lang.Specification

class DynamicAttributesConfigurationGroupCheckerTest extends Specification {
    def "Check. Not supported symbols in categories"() {

        when:
        def checker = new DynamicAttributesConfigurationGroupChecker()
        checker.checkCategory(categoryName)

        then:
        def throwable = thrown(IllegalStateException)
        throwable.getMessage() == message

        where:
        categoryName || message
        "*"          || "The '*' symbol is denied in the category name"
        "*suffix"    || "The '*' symbol is denied in the category name"
        "prefix*"    || "The '*' symbol is denied in the category name"
        "in*fix"     || "The '*' symbol is denied in the category name"
        "+"          || "The '+' symbol is denied in the category name"
        "+suffix"    || "The '+' symbol is denied in the category name"
        "prefix+"    || "The '+' symbol is denied in the category name"
        "in+fix"     || "The '+' symbol is denied in the category name"
        "."          || "The '.' symbol is denied in the category name"
        ".suffix"    || "The '.' symbol is denied in the category name"
        "prefix."    || "The '.' symbol is denied in the category name"
        "in.fix"     || "The '.' symbol is denied in the category name"
    }
}

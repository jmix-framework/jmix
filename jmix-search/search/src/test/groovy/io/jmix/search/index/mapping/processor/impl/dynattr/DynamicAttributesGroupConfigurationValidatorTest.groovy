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

import io.jmix.search.exception.IndexConfigurationException
import spock.lang.Specification

class DynamicAttributesGroupConfigurationValidatorTest extends Specification {

    def "Check. Not supported symbols in categories"() {
        when:
        def checker = new DynamicAttributesGroupConfigurationValidator()
        checker.checkCategory(categoryName)

        then:
        def throwable = thrown(IndexConfigurationException)
        throwable.getMessage() == message

        where:
        categoryName || message
        ""           || "Category name can't be empty"
        " "          || "Category name can't be empty"
        "*"          || "Category name can't be a wildcard without any text. But wildcards like '*abc', 'abc*', 'a*b*c' are supported."
        "**"         || "Category name can't be a wildcard without any text. But wildcards like '*abc', 'abc*', 'a*b*c' are supported."
        "+"          || "The '+' symbol is denied in the category name. Category name value is '+'"
        "+suffix"    || "The '+' symbol is denied in the category name. Category name value is '+suffix'"
        "prefix+"    || "The '+' symbol is denied in the category name. Category name value is 'prefix+'"
        "in+fix"     || "The '+' symbol is denied in the category name. Category name value is 'in+fix'"
        "."          || "The '.' symbol is denied in the category name. Category name value is '.'"
        ".suffix"    || "The '.' symbol is denied in the category name. Category name value is '.suffix'"
        "prefix."    || "The '.' symbol is denied in the category name. Category name value is 'prefix.'"
        "in.fix"     || "The '.' symbol is denied in the category name. Category name value is 'in.fix'"
    }

    def "Check. Not supported symbols in attributes"() {

        when:
        def checker = new DynamicAttributesGroupConfigurationValidator()
        checker.checkAttribute(categoryName)

        then:
        def throwable = thrown(IndexConfigurationException)
        throwable.getMessage() == message

        where:
        categoryName || message
        ""           || "Attribute name can't be empty"
        " "          || "Attribute name can't be empty"
        "*"          || "Attribute name can't be a wildcard without any text. But wildcards like '*abc', 'abc*', 'a*b*c' are supported."
        "**"         || "Attribute name can't be a wildcard without any text. But wildcards like '*abc', 'abc*', 'a*b*c' are supported."
        "+"          || "The '+' symbol is denied in the attribute name. Attribute name value is '+'"
        "+suffix"    || "The '+' symbol is denied in the attribute name. Attribute name value is '+suffix'"
        "prefix+"    || "The '+' symbol is denied in the attribute name. Attribute name value is 'prefix+'"
        "in+fix"     || "The '+' symbol is denied in the attribute name. Attribute name value is 'in+fix'"
        "."          || "The '.' symbol is denied in the attribute name. Attribute name value is '.'"
        ".suffix"    || "The '.' symbol is denied in the attribute name. Attribute name value is '.suffix'"
        "prefix."    || "The '.' symbol is denied in the attribute name. Attribute name value is 'prefix.'"
        "in.fix"     || "The '.' symbol is denied in the attribute name. Attribute name value is 'in.fix'"
    }

    def "Check. Supported symbols in categories"() {
        when:
        def checker = new DynamicAttributesGroupConfigurationValidator()
        checker.checkCategory(categoryName)

        then:
        notThrown(IndexConfigurationException)

        where:
        categoryName << ["*suffix", "prefix*", "in*fix", "tw*is*e"]
    }

    def "Check. Supported symbols in attributes"() {
        when:
        def checker = new DynamicAttributesGroupConfigurationValidator()
        checker.checkAttribute(categoryName)

        then:
        notThrown(IndexConfigurationException)

        where:
        categoryName << ["*suffix", "prefix*", "in*fix", "tw*is*e"]
    }

}

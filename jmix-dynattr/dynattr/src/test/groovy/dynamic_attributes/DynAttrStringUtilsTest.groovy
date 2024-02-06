/*
 * Copyright 2024 Haulmont.
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

package dynamic_attributes

import io.jmix.dynattr.utils.DynAttrStringUtils
import spock.lang.Specification

class DynAttrStringUtilsTest extends Specification {
    static char[] delimiters = [' ', '.', '_', '-', '\t'] as char[]

    def "toCamelCase in PascalCase"() {
        given:
        String pascalCaseString = "PascalCaseString"

        when:
        String convertedCamelCase = DynAttrStringUtils.toCamelCase(pascalCaseString, delimiters)

        then:
        convertedCamelCase == "pascalCaseString"
    }

    def "toCamelCase in camelCase"() {
        given:
        String pascalCaseString = "camelCaseString"

        when:
        String convertedCamelCase = DynAttrStringUtils.toCamelCase(pascalCaseString, delimiters)

        then:
        convertedCamelCase == "camelCaseString"
    }

    def "toCamelCase in snake_case"() {
        given:
        String pascalCaseString = "snake_case_string"

        when:
        String convertedCamelCase = DynAttrStringUtils.toCamelCase(pascalCaseString, delimiters)

        then:
        convertedCamelCase == "snakeCaseString"
    }

    def "toCamelCase in kebab-case"() {
        given:
        String pascalCaseString = "kebab-case-string"

        when:
        String convertedCamelCase = DynAttrStringUtils.toCamelCase(pascalCaseString, delimiters)

        then:
        convertedCamelCase == "kebabCaseString"
    }

    def "toCamelCase in SCREAMING_SNAKE_CASE"() {
        given:
        String pascalCaseString = "SCREAMING_SNAKE_CASE_STRING"

        when:
        String convertedCamelCase = DynAttrStringUtils.toCamelCase(pascalCaseString, delimiters)

        then:
        convertedCamelCase == "screamingSnakeCaseString"
    }

    def "toCamelCase in Capitalized_Snake_Case"() {
        given:
        String pascalCaseString = "Capitalized_Snake_Case_STRING"

        when:
        String convertedCamelCase = DynAttrStringUtils.toCamelCase(pascalCaseString, delimiters)

        then:
        convertedCamelCase == "capitalizedSnakeCaseString"
    }

    def "isCamelCase in PascalCase"() {
        given:
        String pascalCaseString = "PascalCaseString"

        when:
        boolean isNotCamelCase = !DynAttrStringUtils.isCamelCase(pascalCaseString)

        then:
        isNotCamelCase
    }

    def "isCamelCase in camelCase"() {
        given:
        String pascalCaseString = "camelCaseString"

        when:
        boolean isCamelCase = DynAttrStringUtils.isCamelCase(pascalCaseString)

        then:
        isCamelCase
    }

    def "isCamelCase in snake_case"() {
        given:
        String pascalCaseString = "snake_case_string"

        when:
        boolean isNotCamelCase = !DynAttrStringUtils.isCamelCase(pascalCaseString)

        then:
        isNotCamelCase
    }

    def "isCamelCase in kebab-case"() {
        given:
        String pascalCaseString = "kebab-case-string"

        when:
        boolean isNotCamelCase = !DynAttrStringUtils.isCamelCase(pascalCaseString)

        then:
        isNotCamelCase
    }

    def "isCamelCase in SCREAMING_SNAKE_CASE"() {
        given:
        String pascalCaseString = "SCREAMING_SNAKE_CASE_STRING"

        when:
        boolean isNotCamelCase = !DynAttrStringUtils.isCamelCase(pascalCaseString)

        then:
        isNotCamelCase
    }

    def "isCamelCase in Capitalized_Snake_Case"() {
        given:
        String pascalCaseString = "Capitalized_Snake_Case_STRING"

        when:
        boolean isNotCamelCase = !DynAttrStringUtils.isCamelCase(pascalCaseString)

        then:
        isNotCamelCase
    }

    def "isPascalCase in PascalCase"() {
        given:
        String pascalCaseString = "PascalCaseString"

        when:
        boolean isPascalCase = DynAttrStringUtils.isPascalCase(pascalCaseString)

        then:
        isPascalCase
    }

    def "isPascalCase in camelCase"() {
        given:
        String pascalCaseString = "camelCaseString"

        when:
        boolean isNotPascalCase = !DynAttrStringUtils.isPascalCase(pascalCaseString)

        then:
        isNotPascalCase
    }

    def "isPascalCase in snake_case"() {
        given:
        String pascalCaseString = "snake_case_string"

        when:
        boolean isNotPascalCase = !DynAttrStringUtils.isPascalCase(pascalCaseString)

        then:
        isNotPascalCase
    }

    def "isPascalCase not overflow on big string via recursive string (for SonarLint rule S5998)"() {
        given:
        String pascalCaseString = "PascalCaseStringPascalCaseStringPascalCaseStringPascalCaseStringPascalCaseStringPascalCaseString" +
                "PascalCaseStringPascalCaseStringPascalCaseStringPascalCaseStringPascalCaseStringPascalCaseString" +
                "PascalCaseStringPascalCaseStringPascalCaseStringPascalCaseStringPascalCaseStringPascalCaseString" +
                "PascalCaseStringPascalCaseStringPascalCaseStringPascalCaseStringPascalCaseStringPascalCaseString" +
                "PascalCaseStringPascalCaseStringPascalCaseStringPascalCaseStringPascalCaseStringPascalCaseString" +
                "PascalCaseStringPascalCaseStringPascalCaseStringPascalCaseStringPascalCaseStringPascalCaseString" +
                "PascalCaseStringPascalCaseStringPascalCaseStringPascalCaseStringPascalCaseStringPascalCaseString"

        when:
        DynAttrStringUtils.isPascalCase(pascalCaseString)

        then:
        noExceptionThrown()
    }
}

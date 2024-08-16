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

package io.jmix.dynattr.model;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaFieldNameSupportedSymbolsValidatorTest {

    @Test
    void isValid_true_one_lowercase_letter() {
        JavaFieldNameSupportedSymbolsValidator validator = new JavaFieldNameSupportedSymbolsValidator();
        assertTrue(validator.isValid("a", null));
    }
    @Test
    void isValid_true_letters_only() {
        JavaFieldNameSupportedSymbolsValidator validator = new JavaFieldNameSupportedSymbolsValidator();
        assertTrue(validator.isValid("abc", null));
    }

    @Test
    void isValid_true_number_at_the_ending() {
        JavaFieldNameSupportedSymbolsValidator validator = new JavaFieldNameSupportedSymbolsValidator();
        assertTrue(validator.isValid("abc123", null));
    }

    @Test
    void isValid_true_camel_case_starts_with_lowercase_letter() {
        JavaFieldNameSupportedSymbolsValidator validator = new JavaFieldNameSupportedSymbolsValidator();
        assertTrue(validator.isValid("aBcDeF", null));
    }

    @Test
    void isValid_true_with_numbers_and_underline_symbol() {
        JavaFieldNameSupportedSymbolsValidator validator = new JavaFieldNameSupportedSymbolsValidator();
        assertTrue(validator.isValid("a_b_c_1_2_3_", null));
    }

    @Test
    void isValid_true_number_at_the_middle() {
        JavaFieldNameSupportedSymbolsValidator validator = new JavaFieldNameSupportedSymbolsValidator();
        assertTrue(validator.isValid("abc123def", null));
    }

    @Test
    void isValid_false_camel_case_starts_with_uppercase_letter() {
        JavaFieldNameSupportedSymbolsValidator validator = new JavaFieldNameSupportedSymbolsValidator();
        assertFalse(validator.isValid("Abcdef", null));
    }
    @Test
    void isValid_false_underline_symbol_at_the_beginning() {
        JavaFieldNameSupportedSymbolsValidator validator = new JavaFieldNameSupportedSymbolsValidator();
        assertFalse(validator.isValid("_abc", null));
    }

    @Test
    void isValid_false_special_symbol() {
        JavaFieldNameSupportedSymbolsValidator validator = new JavaFieldNameSupportedSymbolsValidator();
        assertFalse(validator.isValid("abc-", null));
        assertFalse(validator.isValid("abc/", null));
        assertFalse(validator.isValid("abc&", null));
        assertFalse(validator.isValid("abc*", null));
        assertFalse(validator.isValid("abc%", null));
    }

    @Test
    void isValid_false_cyrillic_letters() {
        JavaFieldNameSupportedSymbolsValidator validator = new JavaFieldNameSupportedSymbolsValidator();
        assertFalse(validator.isValid("abcабв", null));
    }

    @Test
    void isValid_false_number_at_the_beginning() {
        JavaFieldNameSupportedSymbolsValidator validator = new JavaFieldNameSupportedSymbolsValidator();
        assertFalse(validator.isValid("1abc", null));
    }

    @Test
    void isValid_false_with_spaces() {
        JavaFieldNameSupportedSymbolsValidator validator = new JavaFieldNameSupportedSymbolsValidator();
        assertFalse(validator.isValid("ab cd", null));
        assertFalse(validator.isValid("   abcd", null));
        assertFalse(validator.isValid(" abcd", null));
        assertFalse(validator.isValid("abcd ", null));
        assertFalse(validator.isValid(" abcd ", null));
    }

    @Test
    void isValid_false_empty_string() {
        JavaFieldNameSupportedSymbolsValidator validator = new JavaFieldNameSupportedSymbolsValidator();
        assertFalse(validator.isValid("", null));
    }

    @Test
    void isValid_false_null() {
        JavaFieldNameSupportedSymbolsValidator validator = new JavaFieldNameSupportedSymbolsValidator();
        assertFalse(validator.isValid(null, null));
    }

}
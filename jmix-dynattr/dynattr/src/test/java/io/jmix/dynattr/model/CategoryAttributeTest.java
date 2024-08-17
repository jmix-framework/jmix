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

import static io.jmix.dynattr.model.CategoryAttribute.CODE_FIELD_REGEXP;
import static org.junit.jupiter.api.Assertions.*;

class CategoryAttributeTest {

    @Test
    void isValid_true_one_lowercase_letter() {
        assertTrue(isValid("a"));
    }
    @Test
    void isValid_true_letters_only() {
        assertTrue(isValid("abc"));
    }

    @Test
    void isValid_true_number_at_the_ending() {
        assertTrue(isValid("abc123"));
    }

    @Test
    void isValid_true_camel_case_starts_with_lowercase_letter() {
        assertTrue(isValid("aBcDeF"));
    }

    @Test
    void isValid_true_with_numbers_and_underline_symbol() {
        assertTrue(isValid("a_b_c_1_2_3_"));
    }

    @Test
    void isValid_true_number_at_the_middle() {
        assertTrue(isValid("abc123def"));
    }

    @Test
    void isValid_false_camel_case_starts_with_uppercase_letter() {
        assertFalse(isValid("Abcdef"));
    }
    @Test
    void isValid_false_underline_symbol_at_the_beginning() {
        assertFalse(isValid("_abc"));
    }

    @Test
    void isValid_false_special_symbol() {
        assertFalse(isValid("abc-"));
        assertFalse(isValid("abc/"));
        assertFalse(isValid("abc&"));
        assertFalse(isValid("abc*"));
        assertFalse(isValid("abc%"));
    }

    @Test
    void isValid_false_cyrillic_letters() {
        assertFalse(isValid("abcабв"));
    }

    @Test
    void isValid_false_number_at_the_beginning() {
        assertFalse(isValid("1abc"));
    }

    @Test
    void isValid_false_with_spaces() {
        assertFalse(isValid("ab cd"));
        assertFalse(isValid("   abcd"));
        assertFalse(isValid(" abcd"));
        assertFalse(isValid("abcd "));
        assertFalse(isValid(" abcd "));
    }

    @Test
    void isValid_false_empty_string() {
        assertFalse(isValid(""));
    }

    private static boolean isValid(String text){
        return text.matches(CODE_FIELD_REGEXP);
    }
}
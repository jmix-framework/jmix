/*
 * Copyright 2026 Haulmont.
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

package io.jmix.texttodata.dataload.validation.validator;

import java.util.regex.Pattern;

public final class JpqlValidatorUtils {

    private JpqlValidatorUtils() {
    }

    public static boolean containsWord(String text, String word) {
        return Pattern.compile("\\b" + Pattern.quote(word) + "\\b").matcher(text).find();
    }

    public static boolean containsFunctionCall(String text, String functionName) {
        return Pattern.compile("\\b" + Pattern.quote(functionName) + "\\s*\\(").matcher(text).find();
    }
}

/*
 * Copyright 2022 Haulmont.
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

package io.jmix.searchflowui.utils;

import org.apache.commons.lang3.RandomStringUtils;

public class FullTextFilterUtils {

    /**
     * Generates a random alphanumeric parameter name with a length of 10 characters.
     * Parameter name attribute sets the name of the associated query parameter name, used by condition.
     *
     * @return A randomly generated parameter name.
     */
    public static String generateParameterName() {
        return RandomStringUtils.randomAlphabetic(10);
    }
}

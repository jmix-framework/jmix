/*
 * Copyright 2021 Haulmont.
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

package io.jmix.search.utils;

import javax.annotation.Nullable;

public class BooleanParser {

    /**
     * Parses object value as boolean:
     * <ul>
     *     <li>null value -&gt; false</li>
     *     <li>Boolean value -&gt; as is</li>
     *     <li>String value -&gt; according to {@link Boolean#parseBoolean(String)}</li>
     * </ul>
     * @param value input value
     * @return parsed boolean value
     */
    public static boolean parse(@Nullable Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
    }
}

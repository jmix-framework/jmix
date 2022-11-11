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

package io.jmix.flowui.sys.substitutor;

import java.util.Map;

/**
 * Substitutes variables within a string by values.
 */
public interface StringSubstitutor {

    /**
     * Substitutes all the occurrences of variables in the given source object with their matching values from the map.
     *
     * @param source    the source text containing the variables to substitute
     * @param valuesMap the map with the values
     * @return the result of the replace operation
     */
    String substitute(String source, Map<String, Object> valuesMap);
}

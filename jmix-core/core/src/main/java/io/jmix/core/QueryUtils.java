/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core;

import java.util.Collection;

public class QueryUtils {

    public static final String ESCAPE_CHARACTER = "\\";

    public static final String CASE_INSENSITIVE_MARKER = "(?i)";

    public static final String QUERY_PARAMETER_REGEXP = "(?:^|[^\\w]):(\\(\\?i\\))?([^\\d][\\w.$]*)";

    public static final String LIKE_REGEXP = "\\slike\\s*" + QUERY_PARAMETER_REGEXP + "\\s*(escape '(\\S+)')?";

    /**
     * Escapes a parameter value for a 'like' operation in JPQL query
     *
     * @param value parameter value
     * @return escaped parameter value
     */
    public static String escapeForLike(String value) {
        return escapeForLike(value, ESCAPE_CHARACTER);
    }

    /**
     * Escapes a parameter value for a 'like' operation in JPQL query
     * @param value parameter value
     * @param escapeCharacter escape character
     * @return escaped parameter value
     */
    public static String escapeForLike(String value, String escapeCharacter) {
        return value.replace(escapeCharacter, escapeCharacter + escapeCharacter)
                .replace("%", escapeCharacter + "%")
                .replace("_", escapeCharacter + "_");
    }

    /**
     * Returns the query string after applying the given processors on it.
     *
     * @param processors query processors
     * @param query query string
     * @param entityClass queried entity
     * @return result of the processing
     */
    public static String applyQueryStringProcessors(Collection<QueryStringProcessor> processors,
                                                    String query,
                                                    Class<?> entityClass) {
        String result = query;
        for (QueryStringProcessor processor : processors) {
            result = processor.process(query, entityClass);
        }
        return result;
    }
}

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

package io.jmix.search.listener;

import java.util.Map;

//TODO class-record?
class DependentEntitiesQuery {
    private final String query;
    private final Map<String, Object> parameters;

    protected DependentEntitiesQuery(String query, Map<String, Object> parameters) {
        this.query = query;
        this.parameters = parameters;
    }

    public String getQuery() {
        return query;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "DependentEntitiesQuery{" +
                "query='" + query + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}

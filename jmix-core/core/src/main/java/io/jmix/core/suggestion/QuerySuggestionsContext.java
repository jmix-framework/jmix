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

package io.jmix.core.suggestion;

import java.util.Map;
import java.util.function.Supplier;

public class QuerySuggestionsContext {
    private int position;
    private String query;
    private String language;
    private Supplier<Map<String, String>> parametersSupplier;

    public int getPosition() {
        return position;
    }

    public String getQuery() {
        return query;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setParametersSupplies(Supplier<Map<String, String>> parametersSupplier) {
        this.parametersSupplier = parametersSupplier;
    }

    public Supplier<Map<String, String>> getParametersSupplier() {
        return parametersSupplier;
    }
}

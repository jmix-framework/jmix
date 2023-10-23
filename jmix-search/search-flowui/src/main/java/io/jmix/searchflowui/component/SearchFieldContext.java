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

package io.jmix.searchflowui.component;

import io.jmix.flowui.view.OpenMode;
import io.jmix.search.searching.SearchStrategy;

import java.util.List;

public class SearchFieldContext {
    protected SearchStrategy searchStrategy;
    protected List<String> entities;
    protected String value;
    protected OpenMode openMode;
    protected int searchSize;

    public SearchFieldContext() {
    }

    public SearchFieldContext(SearchField searchField) {
        this.searchStrategy = searchField.searchStrategy;
        this.entities = searchField.entities;
        this.value = searchField.getValue();
        this.openMode = searchField.getOpenMode();
        this.searchSize = searchField.getSearchSize();
    }

    public OpenMode getOpenMode() {
        return openMode;
    }

    public void setOpenMode(OpenMode openMode) {
        this.openMode = openMode;
    }

    public SearchStrategy getSearchStrategy() {
        return searchStrategy;
    }

    public void setSearchStrategy(SearchStrategy searchStrategy) {
        this.searchStrategy = searchStrategy;
    }

    public List<String> getEntities() {
        return entities;
    }

    public void setEntities(List<String> entities) {
        this.entities = entities;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getSearchSize() {
        return searchSize;
    }

    public void setSearchSize(int searchSize) {
        this.searchSize = searchSize;
    }
}

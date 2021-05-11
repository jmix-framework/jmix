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

package io.jmix.search.searching;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SearchContext {

    protected int size;
    protected int offset;
    protected String searchText;
    protected List<String> entities = Collections.emptyList();

    public SearchContext(String searchText) {
        this.searchText = searchText;
    }

    public int getSize() {
        return size;
    }

    public SearchContext setSize(int size) {
        this.size = size;
        return this;
    }

    public int getOffset() {
        return offset;
    }

    public SearchContext setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public String getSearchText() {
        return searchText;
    }

    public SearchContext setEntities(List<String> entities) {
        this.entities = entities;
        return this;
    }

    public SearchContext setEntities(String... entities) {
        return setEntities(Arrays.asList(entities));
    }

    public List<String> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    @Override
    public String toString() {
        return "SearchContext{" +
                "size=" + size +
                ", offset=" + offset +
                ", searchText='" + searchText + '\'' +
                ", entities=" + entities +
                '}';
    }
}

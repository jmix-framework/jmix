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

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SearchContext {

    protected int size = 10;
    protected int offset;
    protected final String searchText;
    protected List<String> entities = Collections.emptyList();

    public SearchContext(String searchText) {
        this.searchText = searchText;
    }

    /**
     * Gets max amount of documents in result set.
     *
     * @return Size
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets max amount of documents in result set.
     *
     * @param size Size. Must be positive
     * @return Current {@link SearchContext}
     */
    public SearchContext setSize(int size) {
        Preconditions.checkArgument(size > 0, "Size must be positive");
        this.size = size;
        return this;
    }

    /**
     * Gets amount of documents to skip within search.
     *
     * @return Offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Sets amount of documents to skip within search.
     *
     * @param offset Offset. Must be positive
     * @return Current {@link SearchContext}
     */
    public SearchContext setOffset(int offset) {
        Preconditions.checkArgument(offset > 0, "Offset must be positive");
        this.offset = offset;
        return this;
    }

    /**
     * Gets text that should be found.
     *
     * @return Search text
     */
    public String getSearchText() {
        return searchText;
    }

    /**
     * Sets names of entities to search within. Empty list means all indexed entities.
     *
     * @param entities List of entity names
     * @return Current {@link SearchContext}
     */
    public SearchContext setEntities(List<String> entities) {
        Preconditions.checkNotNull(entities);
        this.entities = entities;
        return this;
    }

    /**
     * Sets names of entities to search within. Empty list means all indexed entities.
     *
     * @param entities List of entity names
     * @return Current {@link SearchContext}
     */
    public SearchContext setEntities(String... entities) {
        return setEntities(Arrays.asList(entities));
    }

    /**
     * Gets names of entities to search within. Empty list means all indexed entities.
     *
     * @return List of entity names
     */
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

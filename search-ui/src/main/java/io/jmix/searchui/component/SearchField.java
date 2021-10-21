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

package io.jmix.searchui.component;

import io.jmix.search.searching.SearchResult;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.ui.component.Field;
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.screen.Install;

import java.util.List;
import java.util.function.Consumer;

/**
 * UI component that performs full text search
 */
@StudioComponent(
        caption = "SearchField",
        category = "Search",
        xmlElement = "searchField",
        xmlns = "http://jmix.io/schema/search/ui",
        xmlnsAlias = "search",
        icon = "io/jmix/searchui/icon/searchField.svg",
        canvasBehaviour = CanvasBehaviour.SEARCH_FIELD,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/search/search-in-ui.html",
        unsupportedProperties = {"colspan", "rowspan"}
)
public interface SearchField extends Field<String> {

    String NAME = "searchField";

    /**
     * @return {@link SearchStrategy} related to this SearchField.
     */
    SearchStrategy getSearchStrategy();

    /**
     * Sets {@link SearchStrategy} to this SearchField.
     *
     * @param strategy {@link SearchStrategy}
     */
    @StudioProperty(name = "strategy", type = PropertyType.STRING, defaultValue = "anyTermAnyField",
            options = {"anyTermAnyField", "allTermsAnyField", "allTermsSingleField", "phrase"})
    void setSearchStrategy(SearchStrategy strategy);

    /**
     * @return Names of entities configured to search within
     */
    List<String> getEntities();

    /**
     * Sets names of entities to search within.
     *
     * @param entities List of entity names
     */
    @StudioProperty(name = "entities", type = PropertyType.ENTITY_NAMES)
    void setEntities(List<String> entities);

    /**
     * Performs search.
     */
    void performSearch();

    /**
     * Sets the handler to be invoked when the search is successfully completed.
     * <p>
     * The preferred way to set the handler is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "mySearchField", subject = "searchCompletedHandler")
     * public void mySearchFieldSearchCompletedHandler(SearchField.SearchCompletedEvent event) {
     *     SearchResult searchResult = event.getSearchResult();
     *     //...
     * }
     * </pre>
     *
     * @param handler handler
     */
    void setSearchCompletedHandler(Consumer<SearchCompletedEvent> handler);

    /**
     * Returns the handler to be invoked when the search is successfully completed.
     *
     * @return handler
     */
    Consumer<SearchCompletedEvent> getSearchCompletedHandler();

    class SearchCompletedEvent {
        protected SearchField source;
        protected SearchResult searchResult;

        public SearchCompletedEvent(SearchField source, SearchResult searchResult) {
            this.source = source;
            this.searchResult = searchResult;
        }

        public SearchResult getSearchResult() {
            return searchResult;
        }

        public SearchField getSource() {
            return source;
        }
    }
}

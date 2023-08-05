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

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.QueryParameters;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.OpenMode;
import io.jmix.search.SearchProperties;
import io.jmix.search.searching.EntitySearcher;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchResult;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.searchflowui.view.result.SearchResultsView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static io.jmix.searchflowui.view.result.SearchResultsView.*;

public class SearchField extends TypedTextField<String> {
    public static final String NAME = "searchField";
    public static final String SEARCH_FIELD_STYLENAME = "jmix-search-field";

    @Autowired
    protected EntitySearcher entitySearcher;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Messages messages;
    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected DialogWindows dialogWindows;

    protected Icon searchIcon;
    protected Consumer<SearchCompletedEvent> searchCompletedHandler;

    protected SearchStrategy searchStrategy;
    protected List<String> entities;
    protected OpenMode openMode;

    public SearchField() {
        super();
        addClassName(SEARCH_FIELD_STYLENAME);
    }

    @Override
    protected void initComponent() {
        super.initComponent();

        getValue();
        initSearchField();

        addValueChangeListener(valueChangeEvent -> {
            String value = valueChangeEvent.getValue();
            setValue(value);
        });


        searchCompletedHandler = (event -> {
            SearchResult searchResult = event.getSearchResult();
            if (searchResult.isEmpty()) {
                notifications.create(messages.getMessage(getClass(), "noResults"))
                        .show();
            } else {
                openSearchResultsWindow(event.getSearchResult());
            }
        });
    }

    protected void initSearchField() {
        createSearchIcon();
        this.setSuffixComponent(searchIcon);
        this.addKeyPressListener(Key.ENTER, keyPressEvent -> performSearch());
    }

    protected void createSearchIcon() {
        searchIcon = new Icon(VaadinIcon.SEARCH);
        searchIcon.addClickListener(event -> performSearch());
    }

    protected void openSearchResultsWindow(SearchResult searchResult) {
        if (openMode.equals(OpenMode.NAVIGATION)) {
            viewNavigators.view(SearchResultsView.class)
                    .withBackwardNavigation(true)
                    .withAfterNavigationHandler(event -> {
                        event.getView().initView(new SearchFieldContext(this), searchResult);
                    })
                    .withQueryParameters(new QueryParameters(
                            Map.of(QUERY_PARAM_VALUE, List.of(this.getValue()),
                                    QUERY_PARAM_ENTITIES, this.getEntities(),
                                    QUERY_PARAM_STRATEGY, List.of(this.getSearchStrategy().getName()),
                                    QUERY_PARAM_NEED_RELOAD, List.of(String.valueOf(Boolean.TRUE)))))
                    .navigate();
        } else {
            DialogWindow<SearchResultsView> searchResultsDialog = dialogWindows.view(UiComponentUtils.getView(this),
                            SearchResultsView.class)
                    .build();
            SearchResultsView view = searchResultsDialog.getView();
            view.initView(new SearchFieldContext(this), searchResult);
            searchResultsDialog.open();
        }
    }

    public void performSearch() {
        String searchText = getValue();
        if (StringUtils.isBlank(searchText)) {
            notifications.create(messages.getMessage(getClass(), "noSearchText"))
                    .show();
        } else {
            String preparedSearchText = searchText.trim();
            SearchContext searchContext = new SearchContext(preparedSearchText)
                    .setSize(searchProperties.getSearchResultPageSize())
                    .setEntities(entities);
            SearchResult searchResult = entitySearcher.search(searchContext, searchStrategy);
            if (searchCompletedHandler != null) {
                searchCompletedHandler.accept(new SearchCompletedEvent(this, searchResult));
            }
        }
    }


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

    public List<String> getEntities() {
        return entities;
    }

    public void setEntities(List<String> entities) {
        this.entities = entities;
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

    public void setSearchCompletedHandler(Consumer<SearchCompletedEvent> handler) {
        this.searchCompletedHandler = handler;
    }

    @Nullable
    public Consumer<SearchCompletedEvent> getSearchCompletedHandler() {
        return searchCompletedHandler;
    }
}

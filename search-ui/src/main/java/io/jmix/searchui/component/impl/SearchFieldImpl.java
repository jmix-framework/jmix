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

package io.jmix.searchui.component.impl;

import io.jmix.core.Messages;
import io.jmix.search.SearchProperties;
import io.jmix.search.searching.EntitySearcher;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchResult;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.searchui.component.SearchField;
import io.jmix.searchui.screen.result.SearchResultsScreen;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.impl.ValuePickerImpl;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenContext;
import io.jmix.ui.widget.JmixPickerField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static io.jmix.ui.Notifications.NotificationType.HUMANIZED;
import static io.jmix.ui.screen.UiControllerUtils.getScreenContext;

public class SearchFieldImpl extends ValuePickerImpl<String> implements SearchField {

    public static final String SEARCH_FIELD_STYLENAME = "jmix-search-field";

    @Autowired
    protected Messages messages;
    @Autowired
    protected EntitySearcher entitySearcher;
    @Autowired
    protected ScreenBuilders screenBuilders;
    @Autowired
    protected SearchProperties searchProperties;

    protected SearchStrategy searchStrategy;
    protected List<String> entities;
    protected Consumer<SearchCompletedEvent> searchCompletedHandler;

    public SearchFieldImpl() {
        super();
        addStyleName(SEARCH_FIELD_STYLENAME);
    }

    @Override
    protected void initComponent(JmixPickerField<String> component) {
        super.initComponent(component);

        setFieldEditable(true);

        initActions();

        addFieldValueChangeListener(fieldValueChangeEvent -> {
            String value = fieldValueChangeEvent.getText();
            setValue(value);
        });

        searchCompletedHandler = (event -> {
            SearchResult searchResult = event.getSearchResult();
            if (searchResult.isEmpty()) {
                Screen frameOwner = ComponentsHelper.getWindowNN(this).getFrameOwner();
                ScreenContext screenContext = getScreenContext(frameOwner);
                Notifications notifications = screenContext.getNotifications();
                notifications.create(HUMANIZED)
                        .withCaption(messages.getMessage("io.jmix.searchui.noResults"))
                        .show();
            } else {
                openSearchResultsWindow(event.getSearchResult());
            }
        });
    }

    protected void initActions() {
        removeAllActions();
        BaseAction searchAction = new BaseAction("search")
                .withHandler(actionPerformedEvent -> performSearch())
                .withIcon(JmixIcon.SEARCH.source())
                .withShortcut("ENTER");
        addAction(searchAction);
    }

    protected void openSearchResultsWindow(SearchResult searchResult) {
        Screen frameOwner = ComponentsHelper.getWindowNN(this).getFrameOwner();
        screenBuilders.screen(frameOwner)
                .withScreenClass(SearchResultsScreen.class)
                .withOpenMode(OpenMode.NEW_TAB)
                .build()
                .setSearchResult(searchResult)
                .show();
    }

    public void performSearch() {
        Screen frameOwner = ComponentsHelper.getWindowNN(this).getFrameOwner();
        String searchText = getValue();
        ScreenContext screenContext = getScreenContext(frameOwner);
        if (StringUtils.isBlank(searchText)) {
            Notifications notifications = screenContext.getNotifications();
            notifications.create(HUMANIZED)
                    .withCaption(messages.getMessage("io.jmix.searchui.noSearchText"))
                    .show();
        } else {
            String preparedSearchText = searchText.trim();
            SearchContext searchContext = new SearchContext(preparedSearchText)
                    .setSize(searchProperties.getSearchResultPageSize())
                    .setEntities(getEntities());
            SearchResult searchResult = entitySearcher.search(searchContext, searchStrategy);
            if (searchCompletedHandler != null) {
                searchCompletedHandler.accept(new SearchCompletedEvent(this, searchResult));
            }
        }
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName().replace(SEARCH_FIELD_STYLENAME, ""));
    }

    @Override
    public void setStyleName(@Nullable String name) {
        super.setStyleName(name);
        addStyleName(SEARCH_FIELD_STYLENAME);
    }

    @Override
    public SearchStrategy getSearchStrategy() {
        return searchStrategy;
    }

    @Override
    public void setSearchStrategy(SearchStrategy searchStrategy) {
        this.searchStrategy = searchStrategy;
    }

    @Override
    public List<String> getEntities() {
        return entities;
    }

    @Override
    public void setEntities(List<String> entities) {
        this.entities = entities;
    }

    @Override
    public void setSearchCompletedHandler(Consumer<SearchCompletedEvent> handler) {
        this.searchCompletedHandler = handler;
    }

    @Override
    @Nullable
    public Consumer<SearchCompletedEvent> getSearchCompletedHandler() {
        return searchCompletedHandler;
    }
}

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
import io.jmix.search.searching.SearchStrategy;
import io.jmix.searchflowui.view.result.SearchResultsView;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

import static io.jmix.searchflowui.view.result.SearchResultsView.*;

//todo add studio component annotation
public class SearchField extends TypedTextField<String> {
    public static final String SEARCH_FIELD_STYLENAME = "jmix-search-field";

    protected Notifications notifications;
    protected Messages messages;
    protected SearchProperties searchProperties;
    protected ViewNavigators viewNavigators;
    protected DialogWindows dialogWindows;

    protected Icon searchIcon;
    protected SearchStrategy searchStrategy;
    protected List<String> entities;
    protected OpenMode openMode;
    protected int searchSize;

    public SearchField() {
        super();
        addClassName(SEARCH_FIELD_STYLENAME);
    }

    @Override
    public void afterPropertiesSet() {
        autowireDependencies();

        initComponent();
    }

    protected void autowireDependencies() {
        notifications = applicationContext.getBean(Notifications.class);
        messages = applicationContext.getBean(Messages.class);
        searchProperties = applicationContext.getBean(SearchProperties.class);
        viewNavigators = applicationContext.getBean(ViewNavigators.class);
        dialogWindows = applicationContext.getBean(DialogWindows.class);
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

    protected void openSearchResultsWindow(String searchText) {
      if (openMode == OpenMode.DIALOG) {
            DialogWindow<SearchResultsView> searchResultsDialog = dialogWindows.view(UiComponentUtils.getView(this),
                            SearchResultsView.class)
                    .build();

            SearchResultsView view = searchResultsDialog.getView();
            view.initView(new SearchFieldContext(this));
            searchResultsDialog.open();
        } else {
            viewNavigators.view(SearchResultsView.class)
                    .withBackwardNavigation(true)
                    .withAfterNavigationHandler(event -> {
                        event.getView().initView(new SearchFieldContext(this));
                    })
                    .withQueryParameters(new QueryParameters(
                            Map.of(QUERY_PARAM_VALUE, List.of(this.getValue()),
                                    QUERY_PARAM_ENTITIES, this.getEntities(),
                                    QUERY_PARAM_SEARCH_SIZE, List.of(String.valueOf(this.getSearchSize())),
                                    QUERY_PARAM_STRATEGY, List.of(this.getSearchStrategy().getName()))))
                    .navigate();
        }
    }

    public void performSearch() {
        String searchText = getValue();
        if (StringUtils.isBlank(searchText)) {
            notifications.create(messages.getMessage(getClass(), "noSearchText"))
                    .show();
        } else {
            String preparedSearchText = searchText.trim();

            openSearchResultsWindow(preparedSearchText);
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

    public int getSearchSize() {
        return searchSize;
    }

    public void setSearchSize(int searchSize) {
        this.searchSize = searchSize;
    }
}

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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.router.QueryParameters;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.HasAutofocus;
import io.jmix.flowui.kit.component.HasPlaceholder;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.OpenMode;
import io.jmix.search.SearchProperties;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.searchflowui.view.result.SearchResultsView;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.searchflowui.view.result.SearchResultsView.*;

public class SearchField extends CustomField<String>
        implements ApplicationContextAware, InitializingBean,
        HasHelper, HasLabel, HasSuffix, HasSize,
        HasStyle, HasTooltip, HasThemeVariant<TextFieldVariant>, HasTitle,
        InputNotifier, KeyNotifier, HasAriaLabel, HasAutofocus, HasPlaceholder {
    public static final String SEARCH_FIELD_STYLENAME = "jmix-search-field";

    protected ApplicationContext applicationContext;
    protected UiComponents uiComponents;
    protected Notifications notifications;
    protected Messages messages;
    protected SearchProperties searchProperties;
    protected ViewNavigators viewNavigators;
    protected DialogWindows dialogWindows;
    protected TypedTextField<String> root;
    protected Icon searchIcon;
    protected SearchStrategy searchStrategy;
    protected List<String> entities;
    protected OpenMode openMode;
    protected int searchSize;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        autowireDependencies();

        setClassName(SEARCH_FIELD_STYLENAME);
        initComponent();
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
        notifications = applicationContext.getBean(Notifications.class);
        messages = applicationContext.getBean(Messages.class);
        searchProperties = applicationContext.getBean(SearchProperties.class);
        viewNavigators = applicationContext.getBean(ViewNavigators.class);
        dialogWindows = applicationContext.getBean(DialogWindows.class);
    }

    protected void initComponent() {
        createSearchIcon();

        root = createRootComponent();
        initRootComponent(root);
        add(root);
    }

    @Override
    protected String generateModelValue() {
        checkValueComponentState();
        //noinspection DataFlowIssue
        return UiComponentUtils.getValue(root);
    }

    @Override
    public void setPlaceholder(@Nullable String placeholder) {
        HasPlaceholder.super.setPlaceholder(placeholder);
        root.setPlaceholder(placeholder);
    }

    @Override
    protected void setPresentationValue(String newPresentationValue) {
        if (root != null) {
            root.setValue(newPresentationValue);
        }
    }

    protected void checkValueComponentState() {
        checkState(root != null, "Value component isn't set");
    }


    protected TypedTextField<String> createRootComponent() {
        return uiComponents.create(TypedTextField.class);
    }

    protected void initRootComponent(TypedTextField<String> root) {
        root.setWidthFull();
        root.setSuffixComponent(searchIcon);

        root.addValueChangeListener(valueChangeEvent -> {
            if (valueChangeEvent.isFromClient()) {
                performSearch();
            }
        });
    }

    protected void createSearchIcon() {
        searchIcon = new Icon(VaadinIcon.SEARCH);
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
                            Map.of(QUERY_PARAM_VALUE, List.of(searchText),
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

    @Override
    public String getValue() {
        return root.getValue();
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

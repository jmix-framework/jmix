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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.router.QueryParameters;
import io.jmix.core.Messages;
import io.jmix.flowui.*;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.HasAutofocus;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.View;
import io.jmix.search.SearchProperties;
import io.jmix.search.searching.EntitySearcher;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchResult;
import io.jmix.searchflowui.view.result.SearchResultsView;
import io.jmix.searchflowui.view.settings.SearchFieldSettingsView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.searchflowui.view.result.SearchResultsView.*;

public class SearchField extends CustomField<String>
        implements ApplicationContextAware, InitializingBean,
        HasHelper, HasLabel, HasSuffix, HasSize,
        HasStyle, HasTooltip, HasThemeVariant<TextFieldVariant>, HasTitle,
        InputNotifier, KeyNotifier, HasAriaLabel, HasAutofocus, HasPlaceholder {

    public static final String SEARCH_FIELD_STYLENAME = "jmix-search-field";
    private static final Logger log = LoggerFactory.getLogger(SearchField.class);

    protected ApplicationContext applicationContext;
    protected UiComponents uiComponents;
    protected Notifications notifications;
    protected Messages messages;
    protected SearchProperties searchProperties;
    protected ViewNavigators viewNavigators;
    protected DialogWindows dialogWindows;
    protected Dialogs dialogs;
    protected TypedTextField<String> root;
    protected String searchStrategy;
    protected List<String> entities;
    protected OpenMode openMode;
    protected int searchSize;

    protected Button searchButton;
    protected Button settingsButton;

    /**
     * allows to bind custom results handler to replace standard dialog/view opening behaviour
     */
    protected Consumer<SearchCompletedEvent> searchResultHandler;
    protected EntitySearcher entitySearcher;

    protected Component suffixComponent;

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
        dialogs = applicationContext.getBean(Dialogs.class);
        entitySearcher = applicationContext.getBean(EntitySearcher.class);
    }

    protected void initComponent() {
        createSuffixComponent();

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
        root.setSuffixComponent(suffixComponent);

        root.addKeyPressListener(Key.ENTER, keyPressEvent -> performSearch());
    }

    protected void createSuffixComponent() {
        this.searchButton = createSearchButton();
        this.settingsButton = createSettingsButton();

        HorizontalLayout hbox = uiComponents.create(HorizontalLayout.class);
        hbox.setSpacing(false);
        hbox.add(searchButton);
        hbox.add(settingsButton);

        this.suffixComponent = hbox;
    }

    protected Button createSearchButton() {
        Button button = uiComponents.create(Button.class);
        button.addThemeVariants(
                ButtonVariant.LUMO_TERTIARY_INLINE,
                ButtonVariant.LUMO_CONTRAST,
                ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_SMALL
        );
        button.setIcon(new Icon(VaadinIcon.SEARCH));

        button.addClickListener(clickEvent -> performSearch());
        return button;
    }

    protected Button createSettingsButton() {
        Button settingsButton = uiComponents.create(Button.class);
        settingsButton.addThemeVariants(
                ButtonVariant.LUMO_TERTIARY_INLINE,
                ButtonVariant.LUMO_CONTRAST,
                ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_SMALL
        );
        settingsButton.setIcon(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));

        settingsButton.addClickListener(clickEvent -> {
            View<?> origin = UiComponentUtils.getView(this);
            DialogWindow<SearchFieldSettingsView> settingsDialog = dialogWindows.view(origin, SearchFieldSettingsView.class)
                    .withAfterOpenListener(afterOpenEvent -> {

                    })
                    .withAfterCloseListener(afterCloseEvent -> {
                        if (afterCloseEvent.closedWith(StandardOutcome.SAVE)) {
                            SearchFieldSettingsView view = afterCloseEvent.getView();
                            this.searchStrategy = view.getSelectedSearchStrategy();
                            this.searchSize = view.getSelectedSearchSize();
                            this.entities = view.getSelectedSearchEntities();
                        }
                    })
                    .build();

            SearchFieldSettingsView settingsDialogView = settingsDialog.getView();
            settingsDialogView.initSettingsValues(getSearchStrategy(), getSearchSize(), getEntities());

            settingsDialog.open();
        });

        return settingsButton;
    }

    protected void initViewIfSearchEnabled(SearchResultsView targetView,
                                           @Nullable DialogWindow<SearchResultsView> searchResultsDialog) {
        if (searchProperties.isEnabled()) {
            if (searchResultsDialog != null) {
                // opens a new dialog window
                searchResultsDialog.open();
            }
            targetView.initView(new SearchFieldContext(this));
        } else {
            targetView.createNotificationWithMessage("searchDisabled");
        }
    }

    protected void openSearchResultsWindow(String searchText) {
        if (openMode == OpenMode.DIALOG) {
            View<?> originView = UiComponentUtils.getView(this);
            if (UiComponentUtils.isComponentAttachedToDialog(this)
                    && originView instanceof SearchResultsView targetView) {
                initViewIfSearchEnabled(targetView, null);
            } else {
                DialogWindow<SearchResultsView> searchResultsDialog = dialogWindows.view(
                                originView,
                                SearchResultsView.class)
                        .build();

                SearchResultsView targetView = searchResultsDialog.getView();
                initViewIfSearchEnabled(targetView, searchResultsDialog);
            }
        } else {
            viewNavigators.view(UiComponentUtils.getView(this), SearchResultsView.class)
                    .withBackwardNavigation(true)
                    .withQueryParameters(new QueryParameters(
                            Map.of(QUERY_PARAM_VALUE, List.of(searchText),
                                    QUERY_PARAM_ENTITIES, this.getEntities(),
                                    QUERY_PARAM_SEARCH_SIZE, List.of(String.valueOf(this.getSearchSize())),
                                    QUERY_PARAM_STRATEGY, List.of(this.getSearchStrategy()),
                                    QUERY_PARAM_SEARCH_BUTTON_VISIBLE, List.of(Boolean.toString(this.isSearchButtonVisible())),
                                    QUERY_PARAM_SETTINGS_BUTTON_VISIBLE, List.of(Boolean.toString(this.isSettingsButtonVisible()))
                            )
                    ))
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
            if (searchResultHandler != null) {
                SearchContext searchContext = new SearchContext(preparedSearchText)
                        .setSize(searchProperties.getSearchResultPageSize())
                        .setEntities(getEntities());
                SearchResult searchResult = entitySearcher.search(searchContext, searchStrategy);
                searchResultHandler.accept(new SearchCompletedEvent(this, searchResult));
            } else {
                openSearchResultsWindow(preparedSearchText);
            }
        }
    }

    public void setSearchCompletedHandler(Consumer<SearchCompletedEvent> handler) {
        this.searchResultHandler = handler;
    }

    public Consumer<SearchCompletedEvent> getSearchCompletedHandler() {
        return searchResultHandler;
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

    public String getSearchStrategy() {
        return searchStrategy;
    }

    public void setSearchStrategy(String searchStrategy) {
        this.searchStrategy = searchStrategy;
    }

    public int getSearchSize() {
        return searchSize;
    }

    public void setSearchSize(int searchSize) {
        this.searchSize = searchSize;
    }

    public boolean isSettingsButtonVisible() {
        return settingsButton.isVisible();
    }

    public void setSettingsButtonVisible(boolean settingsButtonVisible) {
        this.settingsButton.setVisible(settingsButtonVisible);
    }

    public boolean isSearchButtonVisible() {
        return searchButton.isVisible();
    }

    public void setSearchButtonVisible(boolean searchButtonVisible) {
        this.searchButton.setVisible(searchButtonVisible);
    }

    public static class SearchCompletedEvent {
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

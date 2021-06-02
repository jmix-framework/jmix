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
import io.jmix.core.common.event.Subscription;
import io.jmix.search.SearchProperties;
import io.jmix.search.searching.EntitySearcher;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchResult;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.searchui.component.SearchField;
import io.jmix.searchui.screen.result.SearchResultsScreen;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.validation.Validator;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static io.jmix.ui.Notifications.NotificationType.HUMANIZED;
import static io.jmix.ui.screen.UiControllerUtils.getScreenContext;

@CompositeDescriptor("/io/jmix/searchui/component/impl/search-field.xml")
public class SearchFieldImpl extends CompositeComponent<CssLayout> implements SearchField,
        CompositeWithCaption, CompositeWithHtmlCaption, CompositeWithHtmlDescription,
        CompositeWithIcon, CompositeWithContextHelp {

    public static final String NAME = "searchField";

    @Autowired
    protected Messages messages;
    @Autowired
    protected EntitySearcher entitySearcher;
    @Autowired
    protected ScreenBuilders screenBuilders;
    @Autowired
    protected SearchProperties searchProperties;

    protected TextField<String> inputField;
    protected Button searchButton;
    protected SearchStrategy searchStrategy;
    protected List<String> entities;

    public SearchFieldImpl() {
        addCreateListener(this::onCreate);
    }

    protected void onCreate(CreateEvent createEvent) {
        inputField = getInnerComponent("inputField");
        searchButton = getInnerComponent("searchButton");

        inputField.addEnterPressListener(enterPressEvent -> performSearch());
        searchButton.addClickListener(clickEvent -> performSearch());
    }

    public void performSearch() {
        Screen frameOwner = ComponentsHelper.getWindowNN(this).getFrameOwner();
        String searchText = inputField.getValue();
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

            if (searchResult.isEmpty()) {
                Notifications notifications = screenContext.getNotifications();

                notifications.create(HUMANIZED)
                        .withCaption(messages.getMessage("io.jmix.searchui.noResults"))
                        .show();
            } else {
                screenBuilders.screen(frameOwner)
                        .withScreenClass(SearchResultsScreen.class)
                        .withOpenMode(OpenMode.NEW_TAB)
                        .build()
                        .setSearchResult(searchResult)
                        .show();
            }
        }
    }

    @Override
    public boolean isEditable() {
        return inputField.isEditable();
    }

    @Override
    public void setEditable(boolean editable) {
        inputField.setEditable(editable);
        searchButton.setEnabled(editable);
    }

    @Override
    public void addValidator(Validator<? super String> validator) {
        inputField.addValidator(validator);
    }

    @Override
    public void removeValidator(Validator<String> validator) {
        inputField.removeValidator(validator);
    }

    @Override
    public Collection<Validator<String>> getValidators() {
        return inputField.getValidators();
    }

    @Nullable
    @Override
    public String getValue() {
        return inputField.getValue();
    }

    @Override
    public void setValue(@Nullable String value) {
        inputField.setValue(value);
    }

    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<String>> listener) {
        return inputField.addValueChangeListener(listener);
    }

    @Override
    public boolean isRequired() {
        return inputField.isRequired();
    }

    @Override
    public void setRequired(boolean required) {
        inputField.setRequired(required);
        getComposition().setRequiredIndicatorVisible(required);
    }

    @Nullable
    @Override
    public String getRequiredMessage() {
        return inputField.getRequiredMessage();
    }

    @Override
    public void setRequiredMessage(@Nullable String msg) {
        inputField.setRequiredMessage(msg);
    }

    @Override
    public boolean isValid() {
        return inputField.isValid();
    }

    @Override
    public void validate() throws ValidationException {
        inputField.validate();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<String> valueSource) {
        inputField.setValueSource(valueSource);
        getComposition().setRequiredIndicatorVisible(inputField.isRequired());
    }

    @Nullable
    @Override
    public ValueSource<String> getValueSource() {
        return inputField.getValueSource();
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
}

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

import io.jmix.core.common.event.Subscription;
import io.jmix.searchui.SearchLauncher;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.validation.Validator;
import io.jmix.ui.screen.Screen;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * UI component that performs full text search
 */
@CompositeDescriptor("search-field.xml")
public class SearchField extends CompositeComponent<CssLayout> implements Field<String>,
        CompositeWithCaption, CompositeWithHtmlCaption, CompositeWithHtmlDescription,
        CompositeWithIcon, CompositeWithContextHelp {

    public static final String NAME = "searchField";

    protected TextField<String> inputField;
    protected Button searchButton;

    public SearchField() {
        addCreateListener(this::onCreate);
    }

    protected void onCreate(CreateEvent createEvent) {
        inputField = getInnerComponent("inputField");
        searchButton = getInnerComponent("searchButton");

        inputField.addEnterPressListener(enterPressEvent -> performSearch());
        searchButton.addClickListener(clickEvent -> performSearch());
    }

    protected void performSearch() {
        SearchLauncher searchLauncher = applicationContext.getBean(SearchLauncher.NAME, SearchLauncher.class);
        Screen frameOwner = ComponentsHelper.getWindowNN(this).getFrameOwner();
        String searchTerm = inputField.getValue();

        searchLauncher.search(frameOwner, searchTerm);
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
}

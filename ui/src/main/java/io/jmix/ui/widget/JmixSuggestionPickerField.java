/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.widget;

import com.vaadin.data.HasValue;
import com.vaadin.shared.Registration;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class JmixSuggestionPickerField<T> extends JmixPickerField<T> {

    protected static final String SUGGESTION_PICKERFIELD_STYLENAME = "c-suggestion-pickerfield";
    protected static final String SUGGESTION_FIELD_STYLENAME = "c-pickerfield-suggestion";

    @Override
    protected void init() {
        super.init();

        addStyleName(SUGGESTION_PICKERFIELD_STYLENAME);
        fieldReadOnly = false;
    }

    @Override
    protected void initField() {
        JmixSuggestionField<T> field = new JmixSuggestionField<>();
        field.addStyleName(SUGGESTION_FIELD_STYLENAME);

        this.field = field;

        (getFieldInternal()).addValueChangeListener(this::onFieldValueChange);
    }

    protected JmixSuggestionField<T> getFieldInternal() {
        //noinspection unchecked
        return (JmixSuggestionField<T>) field;
    }

    @Override
    protected void doSetValue(T value) {
        getFieldInternal().setValue(value);

        updateIcon(value);
    }

    @Override
    public T getValue() {
        return getFieldInternal().getValue();
    }

    @Override
    protected void updateFieldReadOnlyFocusable() {
        // do nothing
    }

    @Override
    public Registration addValueChangeListener(HasValue.ValueChangeListener<T> listener) {
        return getFieldInternal().addValueChangeListener(listener);
    }

    public void setTextViewConverter(Function<T, String> converter) {
        getFieldInternal().setTextViewConverter(converter);
    }

    public int getAsyncSearchDelayMs() {
        return getFieldInternal().getAsyncSearchDelayMs();
    }

    public void setAsyncSearchDelayMs(int asyncSearchDelayMs) {
        getFieldInternal().setAsyncSearchDelayMs(asyncSearchDelayMs);
    }

    public void setEnterActionHandler(Consumer<String> enterActionHandler) {
        getFieldInternal().setEnterActionHandler(enterActionHandler);
    }

    public void setArrowDownActionHandler(Consumer<String> arrowDownActionHandler) {
        getFieldInternal().setArrowDownActionHandler(arrowDownActionHandler);
    }

    public int getMinSearchStringLength() {
        return getFieldInternal().getMinSearchStringLength();
    }

    public void setMinSearchStringLength(int minSearchStringLength) {
        getFieldInternal().setMinSearchStringLength(minSearchStringLength);
    }

    public void setSearchExecutor(Consumer<String> searchExecutor) {
        getFieldInternal().setSearchExecutor(searchExecutor);
    }

    public void showSuggestions(List<T> suggestions, boolean userOriginated) {
        getFieldInternal().showSuggestions(suggestions, userOriginated);
    }

    public void setCancelSearchHandler(Runnable cancelSearchHandler) {
        getFieldInternal().setCancelSearchHandler(cancelSearchHandler);
    }

    public void setSuggestionsLimit(int suggestionsLimit) {
        getFieldInternal().setSuggestionsLimit(suggestionsLimit);
    }

    public int getSuggestionsLimit() {
        return getFieldInternal().getSuggestionsLimit();
    }

    @Nullable
    public String getInputPrompt() {
        return getFieldInternal().getInputPrompt();
    }

    public void setInputPrompt(@Nullable String inputPrompt) {
        getFieldInternal().setInputPrompt(inputPrompt);
    }

    // copied from com.vaadin.ui.AbstractComponent#setStyleName
    public void setPopupStyleName(String styleName) {
        getFieldInternal().setPopupStyleName(styleName);
    }

    // copied from com.vaadin.ui.AbstractComponent#addStyleName
    public void addPopupStyleName(String styleName) {
        getFieldInternal().addPopupStyleName(styleName);
    }

    // copied from com.vaadin.ui.AbstractComponent#removeStyleName
    public void removePopupStyleName(String styleName) {
        getFieldInternal().removePopupStyleName(styleName);
    }

    public void setPopupWidth(String popupWidth) {
        getFieldInternal().setPopupWidth(popupWidth);
    }

    public String getPopupWidth() {
        return getFieldInternal().getPopupWidth();
    }

    public void setOptionsStyleProvider(Function<Object, String> optionsStyleProvider) {
        getFieldInternal().setOptionsStyleProvider(optionsStyleProvider);
    }
}
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
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.IconGenerator;
import com.vaadin.ui.ItemCaptionGenerator;
import com.vaadin.ui.StyleGenerator;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Stream;

public class JmixComboBoxPickerField<T> extends JmixPickerField<T> {

    protected static final String COMBOBOX_PICKERFIELD_STYLENAME = "jmix-combobox-pickerfield";
    protected static final String COMBOBOX_FIELD_STYLENAME = "jmix-pickerfield-combobox";

    @Override
    protected void init() {
        super.init();

        addStyleName(COMBOBOX_PICKERFIELD_STYLENAME);
        fieldReadOnly = false;
    }

    @Override
    protected void initField() {
        JmixComboBox<T> field = new JmixComboBox<>();
        field.addStyleName(COMBOBOX_FIELD_STYLENAME);

        this.field = field;

        (getFieldInternal()).addValueChangeListener(this::onFieldValueChange);
    }

    public JmixComboBox<T> getFieldInternal() {
        //noinspection unchecked
        return (JmixComboBox<T>) field;
    }

    @Override
    public void setStyleName(String style) {
        super.setStyleName(COMBOBOX_PICKERFIELD_STYLENAME + " " + style);
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName().replace(COMBOBOX_PICKERFIELD_STYLENAME, ""));
    }

    @Override
    protected void doSetValue(T value) {
        getFieldInternal().setValue(value);
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

    public void setItems(T... items) {
        getFieldInternal().setItems(items);
    }

    public void setItems(Stream<T> streamOfItems) {
        getFieldInternal().setItems(streamOfItems);
    }

    public void setItems(Collection<T> items) {
        getFieldInternal().setItems(items);
    }

    public void setItems(ComboBox.CaptionFilter captionFilter, Collection<T> items) {
        getFieldInternal().setItems(captionFilter, items);
    }

    public void setItems(ComboBox.CaptionFilter captionFilter, T... items) {
        getFieldInternal().setItems(captionFilter, items);
    }

    public String getEmptySelectionCaption() {
        return getFieldInternal().getEmptySelectionCaption();
    }

    public void setEmptySelectionCaption(String caption) {
        getFieldInternal().setEmptySelectionCaption(caption);
    }

    public boolean isTextInputAllowed() {
        return getFieldInternal().isTextInputAllowed();
    }

    public void setTextInputAllowed(boolean textInputAllowed) {
        getFieldInternal().setTextInputAllowed(textInputAllowed);
    }

    public int getPageLength() {
        return getFieldInternal().getPageLength();
    }

    public void setPageLength(int pageLength) {
        getFieldInternal().setPageLength(pageLength);
    }

    public boolean isEmptySelectionAllowed() {
        return getFieldInternal().isEmptySelectionAllowed();
    }

    public void setEmptySelectionAllowed(boolean emptySelectionAllowed) {
        getFieldInternal().setEmptySelectionAllowed(emptySelectionAllowed);
    }

    public IconGenerator<T> getItemIconGenerator() {
        return getFieldInternal().getItemIconGenerator();
    }

    public void setItemIconGenerator(IconGenerator<T> itemIconGenerator) {
        getFieldInternal().setItemIconGenerator(itemIconGenerator);
    }

    @Nullable
    @Override
    public String getPlaceholder() {
        return getFieldInternal().getPlaceholder();
    }

    @Override
    public void setPlaceholder(@Nullable String placeholder) {
        getFieldInternal().setPlaceholder(placeholder);
    }

    public ItemCaptionGenerator<T> getItemCaptionGenerator() {
        return getFieldInternal().getItemCaptionGenerator();
    }

    public void setItemCaptionGenerator(ItemCaptionGenerator<T> itemCaptionGenerator) {
        getFieldInternal().setItemCaptionGenerator(itemCaptionGenerator);
    }

    @Nullable
    public String getPopupWidth() {
        return getFieldInternal().getPopupWidth();
    }

    public void setPopupWidth(@Nullable String width) {
        getFieldInternal().setPopupWidth(width);
    }

    @Nullable
    public ComboBox.NewItemHandler getNewItemHandler() {
        return getFieldInternal().getNewItemHandler();
    }

    public void setNewItemHandler(@Nullable ComboBox.NewItemHandler newItemHandler) {
        getFieldInternal().setNewItemHandler(newItemHandler);
    }

    public void setStyleGenerator(StyleGenerator<T> generateItemStylename) {
        getFieldInternal().setStyleGenerator(generateItemStylename);
    }
}
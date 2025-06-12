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

package io.jmix.flowui.kit.component.pagination;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A component for managing the number of items displayed per page in a paginated interface.
 * It provides a label and a dropdown selection for users to specify or adjust the number of items per page.
 */
public class JmixItemsPerPage extends Composite<Div> {

    protected static final String BASE_CLASS_NAME = "jmix-items-per-page";
    protected static final String ITEMS_PER_PAGE_SELECT_CLASS_NAME = BASE_CLASS_NAME + "-select";
    protected static final String LABEL_CLASS_NAME = BASE_CLASS_NAME + "-label";

    protected Span labelSpan;
    protected Select<Integer> itemsPerPageSelect;

    protected Integer itemsPerPageDefaultValue;
    protected List<Integer> itemsPerPageItems;

    @Override
    protected Div initContent() {
        Div content = super.initContent();
        content.addClassName(BASE_CLASS_NAME);

        labelSpan = createLabelSpan();
        itemsPerPageSelect = createItemsPerPageSelect();

        content.add(labelSpan, itemsPerPageSelect);

        return content;
    }

    /**
     * Returns the default value for the number of items displayed per page.
     *
     * @return the default value for the number of items per page, or null if no default value is set
     */
    @Nullable
    public Integer getItemsPerPageDefaultValue() {
        return itemsPerPageDefaultValue;
    }

    /**
     * Sets the default value for the number of items displayed per page.
     *
     * @param itemsPerPageDefaultValue the default number of items to be displayed per page,
     *                                or {@code null} if no default value is set
     */
    public void setItemsPerPageDefaultValue(@Nullable Integer itemsPerPageDefaultValue) {
        this.itemsPerPageDefaultValue = itemsPerPageDefaultValue;
    }

    /**
     * Returns the collection of items that represent the available options
     * for the number of items displayed per page.
     *
     * @return a collection of integers representing the available options
     *         for the items-per-page selection
     */
    public Collection<Integer> getItemsPerPageItems() {
        return itemsPerPageItems;
    }

    /**
     * Sets the collection of items that represent the available options for the number of items displayed per page.
     * Items less than or equal to 0 are ignored, and options greater than the entity's maximum fetch size
     * will be replaced by the maximum fetch size.
     *
     * @param itemsPerPageItems a list of integers representing the available options for items-per-page selection
     */
    public void setItemsPerPageItems(List<Integer> itemsPerPageItems) {
        this.itemsPerPageItems = new ArrayList<>(itemsPerPageItems);
    }

    /**
     * Determines whether the "unlimited" item is visible in the items-per-page selection.
     *
     * @return true if the "unlimited" item is visible, false otherwise
     */
    public boolean isItemsPerPageUnlimitedItemVisible() {
        return itemsPerPageSelect.isEmptySelectionAllowed();
    }

    /**
     * Sets whether the "unlimited" option for items-per-page selection is visible.
     * If {@code true}, the "unlimited" option is displayed.
     * If {@code false}, the "unlimited" option is not displayed.
     *
     * @param unlimitedItemVisible a boolean indicating whether the "unlimited" option
     *                              should be visible in the items-per-page selection
     */
    public void setItemsPerPageUnlimitedItemVisible(boolean unlimitedItemVisible) {
        itemsPerPageSelect.setEmptySelectionAllowed(unlimitedItemVisible);
    }

    /**
     * Returns the text of the associated label.
     *
     * @return the text content of the label as a String
     */
    public String getLabelText() {
        return labelSpan.getText();
    }

    /**
     * Sets the text of the label.
     *
     * @param text the text to set for the label
     */
    public void setLabelText(String text) {
        labelSpan.setText(text);
    }

    protected Span createLabelSpan() {
        Span labelSpan = new Span();
        labelSpan.addClassName(LABEL_CLASS_NAME);
        return labelSpan;
    }

    protected Select<Integer> createItemsPerPageSelect() {
        Select<Integer> itemsPerPageSelect = new Select<>();
        itemsPerPageSelect.addClassName(ITEMS_PER_PAGE_SELECT_CLASS_NAME);
        itemsPerPageSelect.setEmptySelectionAllowed(true);
        return itemsPerPageSelect;
    }
}

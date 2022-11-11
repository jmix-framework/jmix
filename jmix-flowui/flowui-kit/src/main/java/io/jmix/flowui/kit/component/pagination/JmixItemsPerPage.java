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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
     *
     * @return
     */
    @Nullable
    public Integer getItemsPerPageDefaultValue() {
        return itemsPerPageDefaultValue;
    }

    /**
     *
     * @param itemsPerPageDefaultValue
     */
    public void setItemsPerPageDefaultValue(@Nullable Integer itemsPerPageDefaultValue) {
        this.itemsPerPageDefaultValue = itemsPerPageDefaultValue;
    }

    /**
     *
     * @return
     */
    public Collection<Integer> getItemsPerPageItems() {
        return itemsPerPageItems;
    }

    /**
     *
     * @param itemsPerPageItems
     */
    public void setItemsPerPageItems(List<Integer> itemsPerPageItems) {
        this.itemsPerPageItems = new ArrayList<>(itemsPerPageItems);
    }

    /**
     *
     * @return
     */
    public boolean isItemsPerPageUnlimitedItemVisible() {
        return itemsPerPageSelect.isEmptySelectionAllowed();
    }

    /**
     *
     * @param unlimitedItemVisible
     */
    public void setItemsPerPageUnlimitedItemVisible(boolean unlimitedItemVisible) {
        itemsPerPageSelect.setEmptySelectionAllowed(unlimitedItemVisible);
    }

    /**
     *
     * @return
     */
    public String getLabelText() {
        return labelSpan.getText();
    }

    /**
     *
     * @param text
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

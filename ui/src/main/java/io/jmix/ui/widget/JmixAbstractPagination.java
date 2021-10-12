/*
 * Copyright 2020 Haulmont.
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

import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import org.apache.commons.lang3.StringUtils;

public class JmixAbstractPagination extends CssLayout {

    public static final String NAVIGATION_BTN_STYLENAME = "navigation-btn";
    public static final String FIRST_PAGE_STYLENAME = "first";
    public static final String PREV_PAGE_STYLENAME = "prev";
    public static final String NEXT_PAGE_STYLENAME = "next";
    public static final String LAST_PAGE_STYLENAME = "last";

    protected String primaryStyleName;

    protected Button firstButton;
    protected Button prevButton;
    protected Button nextButton;
    protected Button lastButton;

    protected JmixItemsPerPageLayout itemsPerPageLayout;

    public JmixAbstractPagination(String primaryStyleName) {
        this.primaryStyleName = primaryStyleName;

        itemsPerPageLayout = createItemsPerPage();
    }

    @Override
    public void setStyleName(String style) {
        super.setStyleName(primaryStyleName + " " + style);
    }

    @Override
    public String getStyleName() {
        String style = super.getStyleName();
        return removeComponentStyle(style, primaryStyleName);
    }

    @Override
    public void removeStyleName(String style) {
        style = removeComponentStyle(style, primaryStyleName);
        super.removeStyleName(style);
    }

    public boolean isItemsPerPageVisible() {
        return itemsPerPageLayout.isVisible();
    }

    public void setItemsPerPageVisible(boolean itemsPerPageVisible) {
        itemsPerPageLayout.setVisible(itemsPerPageVisible);
    }

    public boolean isItemsPerPageUnlimitedOptionVisible() {
        return getItemsPerPageComboBox().isEmptySelectionAllowed();
    }

    public void setItemsPerPageUnlimitedOptionVisible(boolean unlimitedOptionVisible) {
        getItemsPerPageComboBox().setEmptySelectionAllowed(unlimitedOptionVisible);
    }

    public ComboBox<Integer> getItemsPerPageComboBox() {
        return itemsPerPageLayout.getItemsPerPageComboBox();
    }

    public Label getItemsPerPageLabel() {
        return itemsPerPageLayout.getItemsPerPageLabel();
    }

    protected JmixItemsPerPageLayout createItemsPerPage() {
        return new JmixItemsPerPageLayout();
    }

    protected void createNavigationButtons() {
        firstButton = createNavigationBtn(FIRST_PAGE_STYLENAME);
        prevButton = createNavigationBtn(PREV_PAGE_STYLENAME);
        nextButton = createNavigationBtn(NEXT_PAGE_STYLENAME);
        lastButton = createNavigationBtn(LAST_PAGE_STYLENAME);
    }

    protected Button createNavigationBtn(String additionalStyle) {
        Button button = new Button();
        button.setStyleName(primaryStyleName + "-" + NAVIGATION_BTN_STYLENAME);
        button.addStyleName(additionalStyle);
        return button;
    }

    public Button getFirstButton() {
        return firstButton;
    }

    public Button getPrevButton() {
        return prevButton;
    }

    public Button getNextButton() {
        return nextButton;
    }

    public Button getLastButton() {
        return lastButton;
    }

    public JmixItemsPerPageLayout getItemsPerPageLayout() {
        return itemsPerPageLayout;
    }

    protected String removeComponentStyle(String styleNames, String componentStyle) {
        if (styleNames.equals(componentStyle)) {
            return "";
        }
        if (styleNames.startsWith(componentStyle + " ")) {
            styleNames = styleNames.substring(componentStyle.length());
        }
        if (styleNames.contains(" " + componentStyle + " ")) {
            styleNames = styleNames.replaceAll(" " + componentStyle + " ", " ");
        }
        if (styleNames.endsWith(" " + componentStyle)) {
            styleNames = styleNames.substring(0, styleNames.length() - componentStyle.length());
        }
        return StringUtils.normalizeSpace(styleNames);
    }

    protected class JmixItemsPerPageLayout extends CssLayout {

        public static final String ITEMS_PER_PAGE_LAYOUT_STYLENAME = "itemsperpage-layout";
        public static final String ITEMS_PER_PAGE_LABEL_STYLENAME = "itemsperpage-label";
        public static final String ITEMS_PER_PAGE_COMBOBOX_STYLENAME = "itemsperpage-options";

        protected Label itemsPerPageLabel;
        protected ComboBox<Integer> itemsPerPageComboBox;

        public JmixItemsPerPageLayout() {
            this.setStyleName(primaryStyleName + "-" + ITEMS_PER_PAGE_LAYOUT_STYLENAME);

            itemsPerPageLabel = new Label();
            itemsPerPageLabel.setStyleName(primaryStyleName + "-" + ITEMS_PER_PAGE_LABEL_STYLENAME);
            addComponent(itemsPerPageLabel);

            itemsPerPageComboBox = new ComboBox<>();
            itemsPerPageComboBox.setStyleName(primaryStyleName + "-" + ITEMS_PER_PAGE_COMBOBOX_STYLENAME);
            itemsPerPageComboBox.setEmptySelectionAllowed(true);
            itemsPerPageComboBox.setTextInputAllowed(false);
            addComponent(itemsPerPageComboBox);
        }

        public Label getItemsPerPageLabel() {
            return itemsPerPageLabel;
        }

        public ComboBox<Integer> getItemsPerPageComboBox() {
            return itemsPerPageComboBox;
        }
    }
}

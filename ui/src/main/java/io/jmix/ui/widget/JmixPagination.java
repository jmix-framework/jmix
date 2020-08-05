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
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;
import io.jmix.ui.component.Pagination;

public class JmixPagination extends JmixCssActionsLayout {

    public static final String CONTENT_RIGHT_ALIGN_STYLE = "content-right-align";

    protected Pagination.ContentAlignment contentAlignment = Pagination.ContentAlignment.LEFT;

    protected ComponentContainer contentLayout;
    protected Button prevButton;
    protected Button nextButton;
    protected Button firstButton;
    protected Button lastButton;
    protected Label label;
    protected Button countButton;

    protected ComponentContainer maxResultLayout;
    protected Label maxResultLabel;
    protected ComboBox<Integer> maxResultComboBox;

    public JmixPagination() {
        contentLayout = createContentLayout();
        contentLayout.setWidth(100, Unit.PERCENTAGE);

        addComponent(contentLayout);
    }

    protected ComponentContainer createContentLayout() {
        JmixCssActionsLayout contentLayout = new JmixCssActionsLayout();
        contentLayout.setStyleName("c-pagination-wrapper");
        contentLayout.setSpacing(true);

        maxResultLayout = createMaxResultLayout();
        maxResultLayout.setVisible(false);
        contentLayout.addComponent(maxResultLayout);

        firstButton = new JmixButton();
        firstButton.setStyleName("c-pagination-change-page");
        firstButton.addStyleName("c-pagination-first");
        contentLayout.addComponent(firstButton);

        prevButton = new JmixButton();
        prevButton.setStyleName("c-pagination-change-page");
        prevButton.addStyleName("c-pagination-prev");
        contentLayout.addComponent(prevButton);

        label = new Label();
        label.setWidthUndefined();
        label.setStyleName("c-pagination-status");
        contentLayout.addComponent(label);

        countButton = new JmixButton("[?]");
        countButton.setWidthUndefined();
        countButton.setStyleName(ValoTheme.BUTTON_LINK);
        countButton.addStyleName("c-pagination-count");
        countButton.setTabIndex(-1);
        contentLayout.addComponent(countButton);

        nextButton = new JmixButton();
        nextButton.setStyleName("c-pagination-change-page");
        nextButton.addStyleName("c-pagination-next");
        contentLayout.addComponent(nextButton);

        lastButton = new JmixButton();
        lastButton.setStyleName("c-pagination-change-page");
        lastButton.addStyleName("c-pagination-last");
        contentLayout.addComponent(lastButton);

        return contentLayout;
    }

    public Pagination.ContentAlignment getContentAlignment() {
        return contentAlignment;
    }

    public void setContentAlignment(Pagination.ContentAlignment contentAlignment) {
        if (this.contentAlignment != contentAlignment) {
            this.contentAlignment = contentAlignment;

            if (contentAlignment == Pagination.ContentAlignment.LEFT) {
                contentLayout.removeStyleName(CONTENT_RIGHT_ALIGN_STYLE);
            } else {
                contentLayout.addStyleName(CONTENT_RIGHT_ALIGN_STYLE);
            }

            markAsDirty();
        }
    }

    public Label getLabel() {
        return label;
    }

    public Button getCountButton() {
        return countButton;
    }

    public Button getPrevButton() {
        return prevButton;
    }

    public Button getNextButton() {
        return nextButton;
    }

    public Button getFirstButton() {
        return firstButton;
    }

    public Button getLastButton() {
        return lastButton;
    }

    public Label getMaxResultLabel() {
        return maxResultLabel;
    }

    public ComboBox<Integer> getMaxResultComboBox() {
        return maxResultComboBox;
    }

    public ComponentContainer getMaxResultLayout() {
        return maxResultLayout;
    }

    protected ComponentContainer createMaxResultLayout() {
        JmixCssActionsLayout maxResultsLayout = new JmixCssActionsLayout();
        maxResultsLayout.setStyleName("c-pagination-maxresult-layout");

        maxResultLabel = new JmixLabel();
        maxResultLabel.setStyleName("c-pagination-maxresult-label");
        maxResultsLayout.addComponent(maxResultLabel);

        maxResultComboBox = new JmixComboBox<>();
        maxResultComboBox.setStyleName("c-pagination-maxresult-options");
        maxResultComboBox.setEmptySelectionAllowed(false);
        maxResultComboBox.setTextInputAllowed(false);
        maxResultsLayout.addComponent(maxResultComboBox);

        return maxResultsLayout;
    }
}

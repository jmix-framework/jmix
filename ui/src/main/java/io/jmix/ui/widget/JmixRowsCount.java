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

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Is used for supporting RowsCount component in compatibility module.
 */
public class JmixRowsCount extends JmixCssActionsLayout {

    protected Button prevButton;
    protected Button nextButton;
    protected Button firstButton;
    protected Button lastButton;
    protected Label label;
    protected Button countButton;

    public JmixRowsCount() {
        setStyleName("jmix-paging");
        setMargin(new MarginInfo(false, false, false, true));

        ComponentContainer contentLayout = createContentLayout();
        addComponent(contentLayout);

        setWidth(100, Unit.PERCENTAGE);
    }

    protected ComponentContainer createContentLayout() {
        JmixCssActionsLayout contentLayout = new JmixCssActionsLayout();
        contentLayout.setStyleName("jmix-paging-wrap");
        contentLayout.setSpacing(true);

        firstButton = new JmixButton();
        firstButton.setStyleName("jmix-paging-change-page");
        firstButton.addStyleName("jmix-paging-first");
        contentLayout.addComponent(firstButton);

        prevButton = new JmixButton();
        prevButton.setStyleName("jmix-paging-change-page");
        prevButton.addStyleName("jmix-paging-prev");
        contentLayout.addComponent(prevButton);

        label = new Label();
        label.setWidthUndefined();
        label.setStyleName("jmix-paging-status");
        contentLayout.addComponent(label);

        countButton = new JmixButton("[?]");
        countButton.setWidthUndefined();
        countButton.setStyleName(ValoTheme.BUTTON_LINK);
        countButton.addStyleName("jmix-paging-count");
        countButton.setTabIndex(-1);
        contentLayout.addComponent(countButton);

        nextButton = new JmixButton();
        nextButton.setStyleName("jmix-paging-change-page");
        nextButton.addStyleName("jmix-paging-next");
        contentLayout.addComponent(nextButton);

        lastButton = new JmixButton();
        lastButton.setStyleName("jmix-paging-change-page");
        lastButton.addStyleName("jmix-paging-last");
        contentLayout.addComponent(lastButton);

        return contentLayout;
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
}
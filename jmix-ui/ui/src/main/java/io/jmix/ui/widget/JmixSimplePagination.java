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
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;

public class JmixSimplePagination extends JmixAbstractPagination {

    public static final String PRIMARY_STYLENAME = "jmix-simplepagination";

    public static final String NAVIGATION_LAYOUT_STYLENAME = PRIMARY_STYLENAME + "-navigation-layout";
    public static final String PAGINATION_STATUS_STYLENAME = PRIMARY_STYLENAME + "-status";
    public static final String PAGINATION_COUNT_STYLENAME = PRIMARY_STYLENAME + "-count";
    public static final String PAGINATION_COUNT_NUMBER_STYLENAME = PRIMARY_STYLENAME + "-count-number";

    protected CssLayout navigationLayout;

    protected Label label;
    protected Button countButton;

    public JmixSimplePagination() {
        super(PRIMARY_STYLENAME);

        createNavigationButtons();
        createContentLayout();
    }

    protected void createContentLayout() {
        navigationLayout = createNavigationLayout();
        navigationLayout.setStyleName(NAVIGATION_LAYOUT_STYLENAME);

        navigationLayout.addComponents(getFirstButton(), getPrevButton());

        label = new Label();
        label.setWidthUndefined();
        label.setStyleName(PAGINATION_STATUS_STYLENAME);
        navigationLayout.addComponent(label);

        countButton = new Button();
        countButton.setWidthUndefined();
        countButton.setStyleName(ValoTheme.BUTTON_LINK);
        countButton.addStyleName(PAGINATION_COUNT_STYLENAME);
        countButton.setTabIndex(-1);
        navigationLayout.addComponent(countButton);

        navigationLayout.addComponents(getNextButton(), getLastButton());

        addComponent(navigationLayout);

        getItemsPerPageLayout().setVisible(false);
        addComponent(getItemsPerPageLayout());
    }

    protected CssLayout createNavigationLayout() {
        return new CssLayout();
    }

    public Label getLabel() {
        return label;
    }

    public Button getCountButton() {
        return countButton;
    }
}

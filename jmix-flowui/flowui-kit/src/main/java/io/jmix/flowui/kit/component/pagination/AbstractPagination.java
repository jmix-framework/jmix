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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import javax.annotation.Nullable;

public abstract class AbstractPagination extends Composite<Div> implements HasStyle {

    public static final String BASE_BUTTON_CLASS_NAME = "-navigation-button";

    public static final String FIRST_BUTTON_CLASS_NAME = "first";
    public static final String PREV_BUTTON_CLASS_NAME = "prev";
    public static final String NEXT_BUTTON_CLASS_NAME = "next";
    public static final String LAST_BUTTON_CLASS_NAME = "last";

    protected JmixItemsPerPage jmixRowsPerPage;

    protected Button firstButton;
    protected Button previousButton;
    protected Button nextButton;
    protected Button lastButton;

    protected final String componentBaseClassName;

    public AbstractPagination(String componentBaseClassName) {
        this.componentBaseClassName = componentBaseClassName;
    }

    @Override
    protected Div initContent() {
        Div content = super.initContent();

        initNavigationButtons();

        Component innerComponent = createInnerBar();

        content.add(firstButton, previousButton, innerComponent, nextButton, lastButton);

        return content;
    }

    protected abstract Component createInnerBar();

    @Nullable
    protected JmixItemsPerPage getJmixRowsPerPage() {
        return jmixRowsPerPage;
    }

    protected void setItemsPerPage(@Nullable JmixItemsPerPage jmixRowsPerPage) {
        if (this.jmixRowsPerPage != null) {
            getContent().remove(this.jmixRowsPerPage);
        }

        this.jmixRowsPerPage = jmixRowsPerPage;

        if (this.jmixRowsPerPage != null) {
            getContent().add(this.jmixRowsPerPage);
        }
    }

    protected void initNavigationButtons() {
        firstButton = createNavigationButton(FIRST_BUTTON_CLASS_NAME, VaadinIcon.ANGLE_DOUBLE_LEFT);
        previousButton = createNavigationButton(PREV_BUTTON_CLASS_NAME, VaadinIcon.ANGLE_LEFT);
        nextButton = createNavigationButton(NEXT_BUTTON_CLASS_NAME, VaadinIcon.ANGLE_RIGHT);
        lastButton = createNavigationButton(LAST_BUTTON_CLASS_NAME, VaadinIcon.ANGLE_DOUBLE_RIGHT);
    }

    protected Button createNavigationButton(String additionalClassName, VaadinIcon icon) {
        Button button = new Button();
        button.setIcon(new Icon(icon));
        button.addClassNames(componentBaseClassName + BASE_BUTTON_CLASS_NAME, additionalClassName);
        return button;
    }
}

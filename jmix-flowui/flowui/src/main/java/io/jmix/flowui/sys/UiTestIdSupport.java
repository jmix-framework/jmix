/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.sys;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.horizontalmenu.HorizontalMenu;
import io.jmix.flowui.component.main.JmixListMenu;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.kit.component.menubar.JmixMenuBarItem;
import io.jmix.flowui.kit.component.menubar.JmixMenuBarRootItem;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * Support class for calculating and setting a special static ID attribute for testing.
 */
@org.springframework.stereotype.Component("flowui_UiTestIdSupport")
public class UiTestIdSupport {

    private static final Logger log = LoggerFactory.getLogger(UiTestIdSupport.class);

    public static final String UI_TEST_ID = "j-test-id";

    protected UiTestIdManager uiTestIdManager;

    public UiTestIdSupport(UiTestIdManager uiTestIdManager) {
        this.uiTestIdManager = uiTestIdManager;
    }

    /**
     * Adds a listener to add a special static ID attribute for testing as a {@link #UI_TEST_ID}
     * attribute of the component.
     * <p>
     * Typically it equals to {@link Component#getId()}, but in certain cases it can be calculated from
     * the component state.
     *
     * @param component component for adding {@link #UI_TEST_ID} attribute
     */
    public void addTestIdListener(Component component) {
        component.addAttachListener(this::testIdListener);
    }

    /**
     * Attach event listener for components that adds a special static ID attribute for testing.
     *
     * @param attachEvent attach event
     */
    protected void testIdListener(AttachEvent attachEvent) {
        attachEvent.unregisterListener();

        Component component = attachEvent.getSource();

        if (component instanceof JmixListMenu || component instanceof HorizontalMenu) {
            setMenuTestId(component);
        } else {
            setTestId(component);
        }
    }

    /**
     * Sets a special static ID attribute for testing for the {@link JmixListMenu} and {@link HorizontalMenu} components
     * and for their nested components (like a {@link Details}, {@link ListItem} etc.).
     *
     * @param component menu component
     */
    protected void setMenuTestId(Component component) {
        if (component instanceof Composite<?> composite) {
            setTestId(component);

            composite.getContent().getChildren()
                    .forEach(this::setMenuTestId);

        } else if (component instanceof UnorderedList) {
            component.getChildren()
                    .forEach(this::setMenuTestId);

        } else if (component instanceof Details details) {
            setTestId(details, getMenuDetailsTestId(details));

            details.getContent()
                    .forEach(this::setMenuTestId);

        } else if (component instanceof ListItem) {
            setTestId(component, UiTestIdUtil.getNormalizedTestId(component));

            component.getChildren()
                    .forEach(this::setMenuTestId);
        } else if (component instanceof JmixMenuBarRootItem menuBarRootItem) {
            setTestId(menuBarRootItem, UiTestIdUtil.getNormalizedTestId(menuBarRootItem));

            menuBarRootItem.getSubMenu().getItems()
                    .forEach(this::setMenuTestId);
        } else if (component instanceof JmixMenuBarItem menuBarItem) {
            setTestId(menuBarItem, UiTestIdUtil.getNormalizedTestId(menuBarItem));

            menuBarItem.getSubMenu().getItems()
                    .forEach(this::setMenuTestId);
        }
    }

    /**
     * Gets a special static ID attribute for testing for a {@link Details} component in the menu.
     * ID is calculated based on {@link Details#getSummaryText()} or the summary component.
     *
     * @param details {@link Details} component to calculate UI test ID
     * @return special static ID attribute for a {@link Details} component in the menu
     */
    protected String getMenuDetailsTestId(Details details) {
        String summaryText = StringUtils.defaultIfBlank(
                details.getSummaryText(),
                details.getSummary().getChildren()
                        .filter(component -> component instanceof Span)
                        .map(component -> ((Span) component).getText())
                        .findAny()
                        .orElse(StringUtils.EMPTY)
        );

        return UiTestIdUtil.getNormalizedTestId(summaryText, details);
    }

    /**
     * Sets a special static ID attribute for testing for passed component.
     * <p>
     * Typically it equals to {@link Component#getId()}, but in certain cases it can be calculated from
     * the component state.
     * <p>
     * If UI test ID already exist, nothing will happen.
     *
     * @param component component for calculating and setting UI test ID
     */
    protected void setTestId(Component component) {
        String testId = component.getElement().getAttribute(UI_TEST_ID);
        if (testId != null) {
            return;
        }

        FragmentUtils.getComponentId(component)
                .map(id -> addFragmentIdPrefix(component, id))
                .or(component::getId)
                .ifPresentOrElse(
                        id -> setTestId(component, id),
                        () -> setTestId(component, uiTestIdManager.generateUiTestId(component))
                );
    }

    /**
     * Adds the prefix of its parent {@link Fragment} to the component's ID.
     *
     * @param component component to find parent {@link Fragment}
     * @param id        component ID
     * @return component ID with the prefix of its parent {@link Fragment} if the fragment has an ID,
     * the component ID otherwise
     */
    protected String addFragmentIdPrefix(Component component, String id) {
        return UiComponentUtils.getFragment(component)
                .getId()
                .map(fragmentId -> fragmentId + StringUtils.capitalize(id))
                .orElse(id);
    }

    /**
     * Sets passed UI test ID for the passed UI component as a {@link #UI_TEST_ID} web-element attribute.
     *
     * @param component component for setting UI test ID
     * @param testId    UI test ID for setting
     */
    protected void setTestId(Component component, @Nullable String testId) {
        if (testId != null) {
            component.getElement().setAttribute(UI_TEST_ID, testId);
        } else {
            log.info("Calculated test id for {} component is null and will be skipped", component.getClass().getName());
        }
    }
}

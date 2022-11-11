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

package io.jmix.flowui.action.binder.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.action.binder.ActionBinder;
import io.jmix.flowui.action.binder.ActionsHolderBinding;
import io.jmix.flowui.action.binder.ActionsHolderBindingImpl;
import io.jmix.flowui.kit.action.Action;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@SuppressWarnings("rawtypes")
@org.springframework.stereotype.Component("flowui_GridMenuItemActionBinder")
@Order(JmixOrder.LOWEST_PRECEDENCE)
public class GridMenuItemActionBinder implements ComponentActionsHolderBinder<GridMenuItem> {

    @Override
    public boolean supports(Component component) {
        return GridMenuItem.class.isAssignableFrom(component.getClass());
    }

    @Override
    public <H extends Component, A extends Action> ActionsHolderBinding<H, A, GridMenuItem> bind(ActionBinder<H> binder,
                                                                                                 A action,
                                                                                                 GridMenuItem component,
                                                                                                 BiFunction<GridMenuItem, ComponentEventListener, Registration> actionHandler) {
        List<Registration> registrations = new ArrayList<>();
        GridMenuItem menuItem = initComponent(action, component);
        registrations.add(addPropertyChangeListener(action, component));

        return new ActionsHolderBindingImpl<>(binder, binder.getHolder(), action, menuItem, actionHandler, registrations);
    }

    protected GridMenuItem initComponent(Action action, GridMenuItem component) {
        component.setText(action.getText());
        component.setEnabled(action.isEnabled());
        component.setVisible(action.isVisible());

        updateIcon(action, component);

        return component;
    }

    protected Registration addPropertyChangeListener(Action action, GridMenuItem component) {
        return action.addPropertyChangeListener(event -> {
            String propertyName = event.getPropertyName();
            switch (propertyName) {
                case Action.PROP_ENABLED:
                    component.setEnabled(action.isEnabled());
                    break;
                case Action.PROP_VISIBLE:
                    component.setVisible(action.isVisible());
                    break;
                case Action.PROP_TEXT:
                    component.setText(action.getText());
                    break;
                case Action.PROP_ICON:
                    updateIcon(action, component);
                    break;
            }
        });
    }

    protected void updateIcon(Action action, GridMenuItem component) {
        // todo gd refactor icon mechanism
/*        component.getChildren().sequential()
                .filter(child -> child instanceof Icon)
                .map(child -> (Icon) child)
                .findFirst()
                .ifPresent(component::remove);

        if (action.getIcon() != null) {
            component.addComponentAsFirst(new Icon(action.getIcon()));
        }*/
    }
}

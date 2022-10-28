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

package io.jmix.flowui.kit.component.grid;

import com.google.common.base.Strings;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;

import javax.annotation.Nullable;
import java.util.Objects;

public class GridMenuItemActionSupport {

    protected final GridMenuItemActionWrapper<?> menuItem;

    protected Action action;

    protected Registration registration;
    protected Registration actionPropertyChangeRegistration;

    public GridMenuItemActionSupport(GridMenuItemActionWrapper<?> menuItem) {
        this.menuItem = menuItem;
    }

    @Nullable
    public Action getAction() {
        return action;
    }

    public void setAction(@Nullable Action action) {
        if (Objects.equals(this.action, action)) {
            return;
        }

        removeRegistrations();

        this.action = action;

        if (action != null) {
            menuItem.setText(action.getText());
            menuItem.setEnabled(action.isEnabled());
            menuItem.setVisible(action.isVisible());

            registration = menuItem.addMenuItemClickListener(event ->
                    action.actionPerform(event.getSource()));
            actionPropertyChangeRegistration = addPropertyChangeListener();
        }

        updateVisible();
    }

    protected void updateVisible() {
        menuItem.setVisible(
                !Strings.isNullOrEmpty(menuItem.getText())
                        && action != null && action.isVisible()
        );
    }

    protected void removeRegistrations() {
        if (action != null) {
            if (registration != null) {
                registration.remove();
                registration = null;
            }

            if (actionPropertyChangeRegistration != null) {
                actionPropertyChangeRegistration.remove();
                actionPropertyChangeRegistration = null;
            }
        }
    }

    protected Registration addPropertyChangeListener() {
        return action.addPropertyChangeListener(event -> {
            String propertyName = event.getPropertyName();
            switch (propertyName) {
                case Action.PROP_TEXT:
                    menuItem.setText(action.getText());
                    updateVisible();
                    break;
                case Action.PROP_ENABLED:
                    menuItem.setEnabled(action.isEnabled());
                    break;
                case Action.PROP_VISIBLE:
                    updateVisible();
                    break;
                default:
            }
        });
    }
}

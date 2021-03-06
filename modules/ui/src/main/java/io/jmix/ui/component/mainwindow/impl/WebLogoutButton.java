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

package io.jmix.ui.component.mainwindow.impl;

import io.jmix.ui.AppUI;
import io.jmix.ui.component.impl.WebAbstractComponent;
import io.jmix.ui.component.mainwindow.LogoutButton;
import io.jmix.ui.widget.JmixButton;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

public class WebLogoutButton extends WebAbstractComponent<JmixButton> implements LogoutButton {

    public static final String LOGOUT_BUTTON_STYLENAME = "c-logout-button";

    public WebLogoutButton() {
        component = new JmixButton();
        component.addStyleName(LOGOUT_BUTTON_STYLENAME);
        component.addClickListener(event -> logout());
    }

    protected void logout() {
        AppUI ui = ((AppUI) component.getUI());
        if (ui == null) {
            throw new IllegalStateException("Logout button is not attached to UI");
        }
        ui.getApp().logout();
    }

    @Override
    public void setStyleName(@Nullable String name) {
        super.setStyleName(name);

        component.addStyleName(LOGOUT_BUTTON_STYLENAME);
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName().replace(LOGOUT_BUTTON_STYLENAME, ""));
    }
}

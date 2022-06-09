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

package io.jmix.flowui.kit.component.main;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.component.HasTitle;

public class LogoutButton extends Button implements HasTitle {

    protected static final String MAIN_STYLE_NAME = "jmix-logout-button";

    protected boolean hasListeners = false;

    public LogoutButton() {
        addClassName(MAIN_STYLE_NAME);
        attachClickListener();
    }

    protected void attachClickListener() {
        addClickListener(new LogoutButtonClickListener());
    }

    protected void performLogout() {
        // use in inheritors
    }

    @Override
    public Registration addClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        if (!(listener instanceof LogoutButtonClickListener)) {
            hasListeners = true;
        }
        return super.addClickListener(listener);
    }

    protected class LogoutButtonClickListener implements ComponentEventListener<ClickEvent<Button>> {

        @Override
        public void onComponentEvent(ClickEvent<Button> event) {
            if (!LogoutButton.this.hasListeners) {
                LogoutButton.this.performLogout();
            }
        }
    }
}

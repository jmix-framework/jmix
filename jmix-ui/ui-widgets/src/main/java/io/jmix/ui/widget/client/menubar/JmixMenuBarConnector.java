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

package io.jmix.ui.widget.client.menubar;

import io.jmix.ui.widget.JmixMenuBar;
import com.vaadin.client.UIDL;
import com.vaadin.client.ui.VMenuBar;
import com.vaadin.client.ui.menubar.MenuBarConnector;
import com.vaadin.shared.ui.Connect;

@Connect(JmixMenuBar.class)
public class JmixMenuBarConnector extends MenuBarConnector {

    @Override
    public JmixMenuBarState getState() {
        return (JmixMenuBarState) super.getState();
    }

    @Override
    public JmixMenuBarWidget getWidget() {
        return (JmixMenuBarWidget) super.getWidget();
    }

    @Override
    public void layout() {
        super.layout();

        if (getState().vertical) {
            getWidget().addStyleName("jmix-menubar-vertical");
        } else {
            getWidget().removeStyleName("jmix-menubar-vertical");
        }
    }

    @Override
    protected boolean isUseMoreMenuItem() {
        return !getState().vertical;
    }

    @Override
    protected void assignAdditionalAttributes(VMenuBar.CustomMenuItem currentItem, UIDL item) {
        if (item.hasAttribute("cid")) {
            currentItem.getElement().setAttribute("j-test-id", item.getStringAttribute("cid"));
        }
    }

    @Override
    protected void assignAdditionalMenuStyles(VMenuBar currentMenu, UIDL item) {
        String icon = item.getStringAttribute("icon");
        if (icon != null) {
            currentMenu.addStyleDependentName("has-icons");
        }
    }
}

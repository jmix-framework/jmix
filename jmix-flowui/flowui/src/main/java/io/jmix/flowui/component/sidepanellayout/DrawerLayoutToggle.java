/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.component.sidepanellayout;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import io.jmix.flowui.kit.component.sidepanellayout.JmixDrawerLayoutToggle;
import org.springframework.lang.Nullable;

/**
 * Toggle button for opening and closing the drawer panel in a {@link DrawerLayout}.
 */
public class DrawerLayoutToggle extends JmixDrawerLayoutToggle {

    protected DrawerLayout drawerLayout;

    @Nullable
    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    public void setDrawerLayout(@Nullable DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;
    }

    @Override
    protected void onClick(ClickEvent<Button> event) {
        super.onClick(event);

        if (drawerLayout == null) {
            throw new IllegalStateException(DrawerLayout.class.getSimpleName() + " is not set");
        }

        drawerLayout.toggleDrawer();
    }
}

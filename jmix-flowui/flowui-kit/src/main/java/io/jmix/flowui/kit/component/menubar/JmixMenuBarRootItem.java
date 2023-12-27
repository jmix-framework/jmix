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

package io.jmix.flowui.kit.component.menubar;

import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.function.SerializableRunnable;

// CAUTION: copied from com.vaadin.flow.component.menubar.MenuBarRootItem [last update Vaadin 24.3.1]
public class JmixMenuBarRootItem extends JmixMenuBarItem {

    protected JmixMenuBar menuBar;

    JmixMenuBarRootItem(MenuBar menuBar, SerializableRunnable contentReset) {
        super(null, contentReset);
        this.menuBar = (JmixMenuBar) menuBar;
    }

    @Override
    public void setCheckable(boolean checkable) {
        if (checkable) {
            throw new UnsupportedOperationException(String.format(
                    "A root level item in a '%s' can not be checkable", menuBar.getClass().getSimpleName()));
        }
    }

    @Override
    public void addThemeNames(String... themeNames) {
        super.addThemeNames(themeNames);
        menuBar.updateButtons();
    }

    @Override
    public void removeThemeNames(String... themeNames) {
        super.removeThemeNames(themeNames);
        menuBar.updateButtons();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addClassName(String className) {
        super.addClassName(className);
        updateClassName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addClassNames(String... classNames) {
        super.addClassNames(classNames);
        updateClassName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClassName(String className) {
        super.setClassName(className);
        updateClassName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClassName(String className, boolean set) {
        super.setClassName(className, set);
        updateClassName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeClassName(String className) {
        var result = super.removeClassName(className);
        updateClassName();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeClassNames(String... classNames) {
        super.removeClassNames(classNames);
        updateClassName();
    }

    protected void updateClassName() {
        getElement().executeJs(
                "window.Vaadin.Flow.menubarConnector.setClassName(this)");
        menuBar.updateButtons();
    }
}

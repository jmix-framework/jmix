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

package io.jmix.flowui.component.grid;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.grid.Grid;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.component.grid.GridActionsSupport;
import io.jmix.flowui.kit.component.grid.GridMenuItemActionWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component("flowui_DataGridActionsSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DataGridActionsSupport<C extends Grid<T> & ListDataComponent<T>, T> extends GridActionsSupport<C, T>
        implements ApplicationContextAware {

    protected ApplicationContext applicationContext;

    public DataGridActionsSupport(C grid) {
        super(grid);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void initContextMenu() {
        if (component instanceof EnhancedDataGrid enhancedDataGrid) {
            this.contextMenu = enhancedDataGrid.getContextMenu();
            updateContextMenu();
        } else {
            super.initContextMenu();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void attachAction(Action action) {
        super.attachAction(action);

        if (action instanceof ListDataComponentAction) {
            ((ListDataComponentAction<?, T>) action).setTarget(component);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void detachAction(Action action) {
        super.detachAction(action);

        if (action instanceof ListDataComponentAction) {
            ((ListDataComponentAction<?, T>) action).setTarget(null);
        }
    }

    @Override
    protected void addShortcutListenerIfNeeded(Action action) {
        if (!needSkipShortcut(action.getShortcutCombination())) {
            super.addShortcutListenerIfNeeded(action);
        }
    }

    protected boolean needSkipShortcut(@Nullable KeyCombination keyCombination) {
        // Ignore Enter shortcut, because it handled differently
        return keyCombination != null
                && (keyCombination.getKeyModifiers() == null || keyCombination.getKeyModifiers().length == 0)
                && keyCombination.getKey() == Key.ENTER;
    }

    @Override
    protected GridMenuItemActionWrapper<T> createContextMenuItemComponent() {
        GridContextMenuItemComponent<T> itemComponent = new GridContextMenuItemComponent<>();
        itemComponent.setApplicationContext(applicationContext);
        return itemComponent;
    }
}

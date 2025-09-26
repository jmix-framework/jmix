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

package io.jmix.flowui.xml.layout.inittask;

import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.usermenu.ActionUserMenuItem;
import io.jmix.flowui.kit.component.usermenu.HasActionMenuItems;
import io.jmix.flowui.kit.component.usermenu.UserMenuItem;
import io.jmix.flowui.xml.layout.ComponentLoader;
import org.springframework.lang.Nullable;

import java.util.function.Consumer;

public class AssignUserMenuItemActionInitTask<C extends HasActionMenuItems>
        extends AbstractAssignActionInitTask<C> {

    protected final String id;
    protected final int index;

    protected Consumer<UserMenuItem> itemConfigurer;

    public AssignUserMenuItemActionInitTask(C component,
                                            String actionId,
                                            String actionItemId,
                                            int index) {
        super(component, actionId);

        this.id = actionItemId;
        this.index = index;
    }

    public void setItemConfigurer(@Nullable Consumer<UserMenuItem> itemConfigurer) {
        this.itemConfigurer = itemConfigurer;
    }

    @Override
    protected boolean hasOwnAction(String id) {
        return false;
    }

    @Override
    protected void addAction(ComponentLoader.Context context, Action action) {
        ActionUserMenuItem item = component.addActionItem(id, action, index);

        if (itemConfigurer != null) {
            itemConfigurer.accept(item);
        }
    }
}

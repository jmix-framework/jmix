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

package io.jmix.flowui.xml.layout.inittask;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasAction;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.ComponentLoader;

public class AssignActionInitTask<C extends Component & HasAction> extends AbstractAssignActionInitTask<C> {

    @Deprecated(since = "2.3", forRemoval = true)
    public AssignActionInitTask(C component, String actionId, View view) {
        super(component, actionId, view);
    }

    public AssignActionInitTask(C component, String actionId) {
        super(component, actionId);
    }

    @Override
    protected boolean hasOwnAction(String id) {
        return false;
    }

    @Override
    protected void addAction(ComponentLoader.Context context, Action action) {
        component.setAction(action, true);
    }
}

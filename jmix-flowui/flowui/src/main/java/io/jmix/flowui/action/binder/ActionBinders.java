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

package io.jmix.flowui.action.binder;

import com.vaadin.flow.component.Component;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("flowui_ActionBinders")
public class ActionBinders {

    protected ObjectProvider<ActionBinder> actionBinderObjectProvider;

    @Autowired
    public ActionBinders(ObjectProvider<ActionBinder> actionBinderObjectProvider) {
        this.actionBinderObjectProvider = actionBinderObjectProvider;
    }

    @SuppressWarnings("unchecked")
    public <C extends Component> ActionBinder<C> binder(C component) {
        return (ActionBinder<C>) actionBinderObjectProvider.getObject(component);
    }
}

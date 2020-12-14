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

package io.jmix.ui.action.filter;

import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Filter;

import javax.annotation.Nullable;

public abstract class FilterAction extends BaseAction implements Action.ExecutableAction {

    protected Filter filter;

    public FilterAction(String id) {
        super(id);
    }

    @Nullable
    public Filter getFilter() {
        return filter;
    }

    public void setFilter(@Nullable Filter filter) {
        this.filter = filter;
    }

    @Override
    protected boolean isApplicable() {
        return getFilter() != null;
    }

    @Override
    public void actionPerform(Component component) {
        if (!hasSubscriptions(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }
}

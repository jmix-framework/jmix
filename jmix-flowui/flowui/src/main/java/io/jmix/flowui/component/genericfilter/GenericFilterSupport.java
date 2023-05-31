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

package io.jmix.flowui.component.genericfilter;

import com.google.common.collect.ImmutableSet;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.Actions;
import io.jmix.flowui.action.genericfilter.GenericFilterAction;
import io.jmix.flowui.action.genericfilter.GenericFilterClearValuesAction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Internal
@Component("flowui_GenericFilterSupport")
public class GenericFilterSupport {

    protected final Actions actions;

    public GenericFilterSupport(Actions actions) {
        this.actions = actions;
    }

    public List<GenericFilterAction<?>> getDefaultFilterActions(GenericFilter filter) {
        List<GenericFilterAction<?>> filterActions = new ArrayList<>();
        for (String actionId : getDefaultFilterActionIds()) {
            filterActions.add(createFilterAction(actionId, filter));
        }
        return filterActions;
    }

    protected Set<String> getDefaultFilterActionIds() {
        return ImmutableSet.of(
                GenericFilterClearValuesAction.ID
        );
    }

    protected GenericFilterAction<?> createFilterAction(String filterActionId,
                                                        GenericFilter filter) {
        GenericFilterAction<?> filterAction = actions.create(filterActionId);
        filterAction.setTarget(filter);
        return filterAction;
    }
}

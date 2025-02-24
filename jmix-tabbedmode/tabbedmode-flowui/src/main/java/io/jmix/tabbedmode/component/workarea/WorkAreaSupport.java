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

package io.jmix.tabbedmode.component.workarea;

import io.jmix.flowui.Actions;
import io.jmix.flowui.kit.action.Action;
import io.jmix.tabbedmode.action.tabsheet.CloseAllTabsAction;
import io.jmix.tabbedmode.action.tabsheet.CloseOthersTabsAction;
import io.jmix.tabbedmode.action.tabsheet.CloseThisTabAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component("tabmod_WorkAreaSupport")
public class WorkAreaSupport {

    protected final Actions actions;

    public WorkAreaSupport(Actions actions) {
        this.actions = actions;
    }

    public List<Action> getDefaultActions() {
        return getDefaultActionIds()
                .map(this::createAction)
                .toList();
    }

    protected Action createAction(String actionTypeId) {
        return actions.create(actionTypeId);
    }

    protected Stream<String> getDefaultActionIds() {
        return Stream.of(
                CloseThisTabAction.ID,
                CloseOthersTabsAction.ID,
                CloseAllTabsAction.ID
        );
    }
}

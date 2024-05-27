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

import io.jmix.flowui.action.genericfilter.GenericFilterAction;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.delegate.AbstractActionsHolderSupport;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;

public class GenericFilterActionsSupport extends AbstractActionsHolderSupport<GenericFilter> {

    protected final DropdownButton settingsButton;

    public GenericFilterActionsSupport(GenericFilter component, DropdownButton settingsButton) {
        super(component);

        this.settingsButton = settingsButton;
    }

    @Override
    protected void addActionInternal(Action action, int index) {
        super.addActionInternal(action, index);

        addSettingsButtonItem(action, index);
    }

    protected void addSettingsButtonItem(Action action, int index) {
        settingsButton.addItem(action.getId(), action, index);
    }

    @Override
    protected boolean removeActionInternal(Action action) {
        if (super.removeActionInternal(action)) {
            removeSettingsButtonItem(action);

            return true;
        }

        return false;
    }

    protected void removeSettingsButtonItem(Action action) {
        settingsButton.remove(action.getId());
    }

    @Override
    protected void attachAction(Action action) {
        super.attachAction(action);

        if (action instanceof GenericFilterAction<?> filterAction) {
            filterAction.setTarget(component);
        }
    }

    @Override
    protected void detachAction(Action action) {
        super.detachAction(action);

        if (action instanceof GenericFilterAction<?> filterAction) {
            filterAction.setTarget(null);
        }
    }
}

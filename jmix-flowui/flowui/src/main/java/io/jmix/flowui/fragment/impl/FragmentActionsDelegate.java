/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.fragment.impl;

import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentAction;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.delegate.AbstractActionsHolderSupport;

public class FragmentActionsDelegate extends AbstractActionsHolderSupport<Fragment<?>> {

    public FragmentActionsDelegate(Fragment<?> component) {
        super(component);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void attachAction(Action action) {
        super.attachAction(action);

        if (action instanceof FragmentAction fragmentAction) {
            fragmentAction.setTarget(component);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void detachAction(Action action) {
        super.detachAction(action);

        if (action instanceof FragmentAction fragmentAction) {
            fragmentAction.setTarget(null);
        }
    }
}

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

import io.jmix.flowui.fragment.FragmentActions;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.kit.action.Action;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component("flowui_FragmentActions")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FragmentActionsImpl implements FragmentActions {

    protected FragmentActionsDelegate delegate;

    public FragmentActionsImpl(Fragment<?> fragment) {
        delegate = createDelegate(fragment);
    }

    protected FragmentActionsDelegate createDelegate(Fragment<?> fragment) {
        return new FragmentActionsDelegate(fragment);
    }

    @Override
    public void addAction(Action action, int index) {
        delegate.addAction(action, index);
    }

    @Override
    public void removeAction(Action action) {
        delegate.removeAction(action);
    }

    @Override
    public Collection<Action> getActions() {
        return delegate.getActions();
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        return delegate.getAction(id).orElse(null);
    }
}

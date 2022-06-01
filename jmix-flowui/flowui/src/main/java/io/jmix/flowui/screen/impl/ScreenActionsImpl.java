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

package io.jmix.flowui.screen.impl;

import com.vaadin.flow.component.Shortcuts;
import io.jmix.flowui.action.binder.ActionBinder;
import io.jmix.flowui.action.screen.ScreenAction;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.ScreenActions;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;

@Component("flowui_ScreenActions")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ScreenActionsImpl implements ScreenActions {

    protected ActionBinder<Screen> actionBinder;

    public ScreenActionsImpl(ActionBinder<Screen> actionBinder) {
        this.actionBinder = actionBinder;
    }

    @Override
    public void addAction(Action action, int index) {
        if (action.getShortcutCombination() != null) {
            actionBinder.createShortcutActionsHolderBinding(action, getScreen().getContent(),
                    ((screenLayout, shortcutEventListener, keyCombination) ->
                            Shortcuts.addShortcutListener(screenLayout, shortcutEventListener, keyCombination.getKey(),
                                    keyCombination.getKeyModifiers())),
                    index);
        } else {
            actionBinder.addAction(action, index);
        }
        attachAction(action);
    }

    @Override
    public void removeAction(Action action) {
        actionBinder.removeAction(action);
    }

    @Override
    public Collection<Action> getActions() {
        return actionBinder.getActions();
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        return actionBinder.getAction(id).orElse(null);
    }

    protected Screen getScreen() {
        return actionBinder.getHolder();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void attachAction(Action action) {
        if (action instanceof ScreenAction) {
            ((ScreenAction) action).setTarget(getScreen());
        }
    }
}

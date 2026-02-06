/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.di.Instantiator;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.UiObservationSupport;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.KeyCombination;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;


/**
 * Base implementation of {@link Action} that introduces execution observation support.
 *
 * @see UiObservationSupport
 */
public class ObservableBaseAction extends BaseAction {

    @Autowired
    protected UiObservationSupport uiObservationSupport;

    public ObservableBaseAction(String id) {
        super(id);
    }

    @Override
    public void actionPerform(Component component) {
        if (eventBus != null) {
            ActionPerformedEvent event = new ActionPerformedEvent(this, component);
            UiObservationSupport.createActionExeutionObservation(this, getUiObservationSupport())
                    .observe(() -> getEventBus().fireEvent(event));
        }
    }

    @Override
    public ObservableBaseAction withText(@Nullable String text) {
        return (ObservableBaseAction) super.withText(text);
    }

    @Override
    public ObservableBaseAction withEnabled(boolean enabled) {
        return (ObservableBaseAction) super.withEnabled(enabled);
    }

    @Override
    public ObservableBaseAction withVisible(boolean visible) {
        return (ObservableBaseAction) super.withVisible(visible);
    }

    @Override
    public ObservableBaseAction withIcon(@Nullable Component icon) {
        return (ObservableBaseAction) super.withIcon(icon);
    }

    @Deprecated(since = "3.0", forRemoval = true)
    public ObservableBaseAction withIcon(@Nullable Icon icon) {
        return (ObservableBaseAction) super.withIcon(icon);
    }

    @Override
    public ObservableBaseAction withDescription(@Nullable String description) {
        return (ObservableBaseAction) super.withDescription(description);
    }

    @Override
    public ObservableBaseAction withVariant(ActionVariant variant) {
        return (ObservableBaseAction) super.withVariant(variant);
    }

    @Override
    public ObservableBaseAction withShortcutCombination(@Nullable KeyCombination shortcutCombination) {
        return (ObservableBaseAction) super.withShortcutCombination(shortcutCombination);
    }

    @Override
    public ObservableBaseAction withHandler(@Nullable Consumer<ActionPerformedEvent> handler) {
        return (ObservableBaseAction) super.withHandler(handler);
    }

    @Internal
    @Nullable
    protected UiObservationSupport getUiObservationSupport() {
        if (uiObservationSupport != null) {
            return uiObservationSupport;
        }

        // try to instantiate bean in case of action created by a constructor
        UI ui = UI.getCurrent();
        if (ui != null) {
            try {
                uiObservationSupport = Instantiator.get(ui)
                        .getOrCreate(UiObservationSupport.class);
            } catch (Exception e) {
                // expected case, ignore silently
            }
        }

        return uiObservationSupport;
    }
}

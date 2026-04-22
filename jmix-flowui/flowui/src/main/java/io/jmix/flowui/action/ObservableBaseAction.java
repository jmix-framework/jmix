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
import com.vaadin.flow.di.Instantiator;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.observation.UiObservationSupport;
import io.micrometer.observation.Observation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;


/**
 * Base implementation of {@link Action} that introduces execution observation support.
 *
 * @see UiObservationSupport
 */
public class ObservableBaseAction<A extends ObservableBaseAction<A>> extends BaseAction<A> {

    @Autowired
    protected UiObservationSupport uiObservationSupport;

    public ObservableBaseAction(String id) {
        super(id);
    }

    @Override
    public void actionPerform(Component component) {
        if (eventBus != null) {
            ActionPerformedEvent event = new ActionPerformedEvent(this, component);
            getUiObservationSupport()
                    .map(support -> support.createActionExecutionObservation(this))
                    .orElse(Observation.NOOP)
                    .observe(() -> getEventBus().fireEvent(event));
        }
    }

    @Internal
    protected Optional<UiObservationSupport> getUiObservationSupport() {
        if (uiObservationSupport != null) {
            return Optional.of(uiObservationSupport);
        }

        // try to instantiate bean in case of action created by a constructor
        UI ui = UI.getCurrent();
        if (ui != null) {
            try {
                uiObservationSupport = Instantiator.get(ui)
                        .getOrCreate(UiObservationSupport.class);
            } catch (Exception ignored) {
                // expected case, ignore silently
                return Optional.empty();
            }
        }

        return Optional.ofNullable(uiObservationSupport);
    }
}

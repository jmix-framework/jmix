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

package io.jmix.flowui;

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.action.TargetAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.view.View;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Support class for observing UI events such as view lifecycle events and action executions.
 * <p>
 * This class uses an {@link ObservationRegistry} if observation functionality
 * is enabled in the application configuration.
 * <p>
 * Observation functionality can be turned on or off using the {@code jmix.ui.ui-observation-enabled}
 * property in the application's configuration.
 *
 * @see UiProperties#isUiObservationEnabled()
 */
@Internal
@Component("flowui_UiObservationSupport")
public class UiObservationSupport {

    @Autowired(required = false)
    protected ObservationRegistry observationRegistry;

    @Value("${jmix.ui.ui-observation-enabled:false}")
    protected Boolean observationEnabled;

    public static final String VIEW_OBSERVATION_NAME = "jmix.ui.views";
    public static final String ACTION_OBSERVATION_NAME = "jmix.ui.actions";

    public Observation createViewEventObservation(View<?> view, String eventName) {
        if (!observationEnabled) {
            return Observation.NOOP;
        }

        return Observation.createNotStarted(VIEW_OBSERVATION_NAME, observationRegistry)
                .contextualName("fire view event")
                .lowCardinalityKeyValue("event.name", eventName)
                .lowCardinalityKeyValue("view.id", view.getId().orElse(""));
    }

    // the method must be static to avoid NPE in the case where the bean object can't be instantiated
    public static Observation createActionExeutionObservation(Action action,
                                                              @Nullable UiObservationSupport uiObservationSupport) {
        if (uiObservationSupport == null || !uiObservationSupport.observationEnabled) {
            return Observation.NOOP;
        }

        Observation observation = Observation.createNotStarted(ACTION_OBSERVATION_NAME, uiObservationSupport.observationRegistry)
                .contextualName("execute action")
                .lowCardinalityKeyValue("action.id", action.getId());

        if (action instanceof TargetAction<?> targetAction
                && targetAction.getTarget() instanceof com.vaadin.flow.component.Component component) {
            UiComponentUtils.getComponentId(component)
                    .ifPresent(componentId -> observation.lowCardinalityKeyValue("target.id", componentId));
        }

        return observation;
    }
}

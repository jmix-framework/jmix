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
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.view.View;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.lang.Nullable;

@Internal
public final class UiObservationUtils {

    public static final String VIEW_OBSERVATION_NAME = "jmix.ui.views";
    public static final String ACTION_OBSERVATION_NAME = "jmix.ui.actions";

    private UiObservationUtils() {
    }

    public static Observation createViewEventObservation(View<?> view, String eventName,
                                                         @Nullable ObservationRegistry observationRegistry) {
        return Observation.createNotStarted(VIEW_OBSERVATION_NAME, observationRegistry)
                .contextualName("fire view event")
                .lowCardinalityKeyValue("event.name", eventName)
                .lowCardinalityKeyValue("view.id", view.getId().orElse(""));
    }

    public static Observation createActionExeutionObservation(Action action,
                                                              @Nullable ObservationRegistry observationRegistry) {
        return Observation.createNotStarted(ACTION_OBSERVATION_NAME, observationRegistry)
                .contextualName("execute action")
                .lowCardinalityKeyValue("action.id", action.getId());
    }
}

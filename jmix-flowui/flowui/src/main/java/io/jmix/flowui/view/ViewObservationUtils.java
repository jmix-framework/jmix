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

package io.jmix.flowui.view;

import io.jmix.core.annotation.Internal;
import io.micrometer.observation.Observation;

@Internal
public final class ViewObservationUtils {

    private ViewObservationUtils() {
    }

    public static Observation createViewEventObservation(View<?> view, String eventName) {
        return Observation.createNotStarted("jmix.ui.view", view.getObservationRegistry())
                .contextualName("fire view event")
                .lowCardinalityKeyValue("event.name", eventName)
                .lowCardinalityKeyValue("view.id", view.getId().orElse(""));
    }
}

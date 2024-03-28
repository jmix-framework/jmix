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

package io.jmix.flowui.app.propertyfilter.dateinterval.model.predefined;

import io.jmix.core.annotation.Internal;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Provides access to the {@link PredefinedDateInterval}.
 */
@Internal
@Component("flowui_PredefinedDateIntervalRegistry")
public class PredefinedDateIntervalRegistry {

    protected Map<String, PredefinedDateInterval> predefinedDateIntervalMap = new LinkedHashMap<>(6);

    protected List<PredefinedDateInterval> predefinedDateIntervals;

    public PredefinedDateIntervalRegistry(List<PredefinedDateInterval> predefinedDateIntervals) {
        this.predefinedDateIntervals = predefinedDateIntervals;

        for (PredefinedDateInterval interval : predefinedDateIntervals) {
            predefinedDateIntervalMap.put(interval.getName(), interval);
        }
    }

    /**
     * @param name name of predefined date interval
     * @return optional with predefined date interval or empty if there is no predefined interval with given name
     */
    public Optional<PredefinedDateInterval> getIntervalByName(String name) {
        return Optional.ofNullable(predefinedDateIntervalMap.get(name));
    }

    /**
     * @return list of predefined date intervals ordered by {@link Order} annotation
     */
    public List<PredefinedDateInterval> getAllPredefinedIntervals() {
        return Collections.unmodifiableList(predefinedDateIntervals);
    }
}

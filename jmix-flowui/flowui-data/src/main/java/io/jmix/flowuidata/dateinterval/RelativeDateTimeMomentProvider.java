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

package io.jmix.flowuidata.dateinterval;

import io.jmix.core.annotation.Internal;
import io.jmix.data.impl.queryconstant.RelativeDateTimeMoment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Provides enum values for relative date and time moments.
 */
@Internal
@Component("flowui_UiDataRelativeDateTimeMomentProvider")
public class RelativeDateTimeMomentProvider {

    /**
     * @return all enum values
     */
    public List<Enum<?>> getRelativeDateTimeMoments() {
        return Arrays.asList(RelativeDateTimeMoment.values());
    }

    /**
     * @return enum values that correspond to time types
     */
    public List<Enum<?>> getRelativeTimeMoments() {
        return List.of(
                RelativeDateTimeMoment.START_OF_CURRENT_HOUR,
                RelativeDateTimeMoment.END_OF_CURRENT_HOUR,
                RelativeDateTimeMoment.START_OF_CURRENT_MINUTE,
                RelativeDateTimeMoment.END_OF_CURRENT_MINUTE
        );
    }

    /**
     * @param name name of enum constant
     * @return enum value
     */
    public Enum<?> getByName(String name) {
        return RelativeDateTimeMoment.valueOf(name);
    }
}

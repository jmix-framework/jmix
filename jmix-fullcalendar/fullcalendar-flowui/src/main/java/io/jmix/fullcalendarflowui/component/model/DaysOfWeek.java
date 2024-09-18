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

package io.jmix.fullcalendarflowui.component.model;

import io.jmix.core.common.util.Preconditions;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.*;

/**
 * The class represents container for {@link DayOfWeek}. It can be used in entities as a property type.
 */
public class DaysOfWeek implements Serializable {

    public final Set<DayOfWeek> daysOfWeek;

    public DaysOfWeek(Set<DayOfWeek> daysOfWeek) {
        Preconditions.checkNotNullArgument(daysOfWeek);
        this.daysOfWeek = daysOfWeek;
    }

    public Set<DayOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof DaysOfWeek dObj)
                && CollectionUtils.isEqualCollection(dObj.daysOfWeek, daysOfWeek);
    }

    @Override
    public int hashCode() {
        return Objects.hash((daysOfWeek.toArray()));
    }
}

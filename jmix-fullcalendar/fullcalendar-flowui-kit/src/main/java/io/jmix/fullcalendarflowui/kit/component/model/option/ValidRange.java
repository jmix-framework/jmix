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

package io.jmix.fullcalendarflowui.kit.component.model.option;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

import static io.jmix.fullcalendarflowui.kit.component.model.option.OptionUtils.VALID_RANGE;

/**
 * INTERNAL.
 */
public class ValidRange extends AbstractRange {

    public ValidRange() {
        super(VALID_RANGE);
    }

    public void setStart(@Nullable LocalDate start) {
        this.start = start;

        markAsDirty();
    }

    public void setEnd(@Nullable LocalDate end) {
        this.end = end;

        markAsDirty();
    }

    public void setRange(@Nullable LocalDate start, @Nullable LocalDate end) {
        this.start = start;
        this.end = end;

        markAsDirty();
    }
}

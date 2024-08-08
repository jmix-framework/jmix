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

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public abstract class AbstractRange implements Serializable {

    protected LocalDate start;
    protected LocalDate end;

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AbstractRange aObj) {
            return Objects.equals(start, aObj.start)
                    && Objects.equals(end, aObj.end);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}

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

package io.jmix.flowui.facet.urlqueryparameters;

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.component.filter.SingleFilterComponentBase;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Captures and restores the runtime-editable state of a single filter component — its operation
 * (for a {@link PropertyFilter}) and value. Used by the URL query parameter binders so that the
 * definition of "a filter condition's resettable state" lives in one place instead of being
 * duplicated per binder.
 */
@Internal
@Component("flowui_SingleFilterComponentStateSupport")
public class SingleFilterComponentStateSupport {

    /**
     * Captures the runtime-editable state of the given single filter component.
     *
     * @param filterComponent the filter component whose state to capture
     * @return the captured state
     */
    public State capture(SingleFilterComponentBase<?> filterComponent) {
        PropertyFilter.Operation operation = filterComponent instanceof PropertyFilter<?> propertyFilter
                ? propertyFilter.getOperation()
                : null;
        return new State(operation, filterComponent.getValue());
    }

    /**
     * Restores the previously captured state onto the given single filter component. The operation
     * is applied before the value, because changing the operation may recreate the value component
     * and clear the value.
     *
     * @param filterComponent the filter component to restore
     * @param state           the state captured by {@link #capture(SingleFilterComponentBase)}
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void restore(SingleFilterComponentBase filterComponent, State state) {
        if (filterComponent instanceof PropertyFilter propertyFilter && state.operation() != null) {
            propertyFilter.setOperation(state.operation());
        }
        filterComponent.setValue(state.value());
    }

    /**
     * The runtime-editable state of a single filter component.
     *
     * @param operation the {@link PropertyFilter} operation, or {@code null} for components without
     *                  a runtime-editable operation
     * @param value     the component value
     */
    public record State(PropertyFilter.@Nullable Operation operation, @Nullable Object value) {
    }
}

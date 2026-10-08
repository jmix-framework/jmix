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

package test_support;

import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.filter.SingleFilterComponentBase;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration;
import io.jmix.flowui.component.logicalfilter.GroupFilter;
import org.jspecify.annotations.Nullable;

/**
 * Registers a {@link RunTimeConfiguration}, a configuration whose conditions the user can add and remove,
 * for tests of that behavior: the root group is set up as the filter sets up its own, the current values
 * of the conditions, including the ones nested in a group, are recorded as their default values, and the
 * conditions are marked as modified, so they have remove buttons as the conditions the user adds do.
 */
public final class TestRunTimeConfigurations {

    private TestRunTimeConfigurations() {
    }

    public static RunTimeConfiguration register(GenericFilter filter, String id, @Nullable String name,
                                                FilterComponent... filterComponents) {
        GroupFilter rootComponent = filter.filterComponentBuilder()
                .groupFilter()
                .addAll(filterComponents)
                .build();
        rootComponent.setOperationTextVisible(false);

        RunTimeConfiguration configuration = new RunTimeConfiguration(id, rootComponent, filter);
        configuration.setName(name);

        for (FilterComponent filterComponent : rootComponent.getFilterComponents()) {
            if (filterComponent instanceof SingleFilterComponentBase<?> singleFilterComponent
                    && singleFilterComponent.getValue() != null) {
                configuration.setFilterComponentDefaultValue(singleFilterComponent.getParameterName(),
                        singleFilterComponent.getValue());
            }
        }

        configuration.setModified(true);
        filter.addConfiguration(configuration);

        return configuration;
    }
}

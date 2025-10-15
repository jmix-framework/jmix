/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.component.groupgrid.data;

import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.component.groupgrid.GroupProperty;

import java.util.List;

/**
 * Exception thrown when a custom property is used for grouping but no {@link GroupPropertyDescriptor} is found for it
 * in {@link GroupDataGridItems}.
 */
@Experimental
public class MissingPropertyDescriptorException extends RuntimeException {

    protected final List<GroupProperty> groupProperties;

    public MissingPropertyDescriptorException(String message, List<GroupProperty> groupProperties) {
        super(message);

        this.groupProperties = groupProperties;
    }

    /**
     * @return the list of group properties that do not have {@link GroupPropertyDescriptor}.
     */
    public List<GroupProperty> getGroupProperties() {
        return groupProperties;
    }
}

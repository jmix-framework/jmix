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

package io.jmix.aitools.dataload.introspection;

import io.jmix.aitools.dataload.introspection.model.EntityDescriptor;

import java.util.List;

/**
 * Filters introspected entity descriptors down to the ones that may be exposed to the current user.
 */
public interface AvailableEntityFilter {

    /**
     * Filters the given descriptors, keeping only the available ones.
     *
     * @param entityDescriptors descriptors to filter
     * @return descriptors that remain available
     */
    List<EntityDescriptor> filter(List<EntityDescriptor> entityDescriptors);
}

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

package io.jmix.tabbedmode.app.main;

import io.jmix.tabbedmode.component.workarea.WorkArea;

import java.util.Optional;

/**
 * Interface for components that have a {@link WorkArea} component.
 */
public interface HasWorkArea {

    /**
     * @return a {@link WorkArea} component
     * @throws IllegalStateException if the component does not have a work area
     */
    default WorkArea getWorkArea() {
        return getWorkAreaOptional().orElseThrow(() ->
                new IllegalStateException("%s not found".formatted(WorkArea.class.getSimpleName())));
    }

    /**
     * Returns {@link WorkArea} component, if present.
     *
     * @return an {@link Optional} containing the {@link WorkArea} if found;
     * otherwise, an empty {@link Optional}
     */
    Optional<WorkArea> getWorkAreaOptional();
}

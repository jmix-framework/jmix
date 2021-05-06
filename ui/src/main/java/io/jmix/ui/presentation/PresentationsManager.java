/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.presentation;

import io.jmix.core.annotation.Internal;
import io.jmix.ui.component.Component;

import java.util.Collection;

/**
 * Interface to provide ability to manage lifecycle of component presentations in the screen.
 */
@Internal
public interface PresentationsManager {

    /**
     * Sets settings with an initial state to a component. These settings contains initial state of a component
     * before applying last saved settings or presentation settings.
     *
     * @param components components to set default settings
     */
    void setupDefaultSettings(Collection<Component> components);

    /**
     * Applies default presentation to a component if it exists.
     *
     * @param components components to apply default presentation
     */
    void applyDefaultPresentation(Collection<Component> components);

    /**
     * Commits presentation changes.
     *
     * @param components components that should commit presentation changes
     */
    void commitPresentations(Collection<Component> components);
}

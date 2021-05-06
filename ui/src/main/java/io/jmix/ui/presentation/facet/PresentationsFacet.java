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

package io.jmix.ui.presentation.facet;

import io.jmix.ui.component.Component;
import io.jmix.ui.component.Facet;
import io.jmix.ui.component.Window;

import java.util.Collection;
import java.util.Set;

/**
 * Provides the ability to apply and save presentations for components.
 */
public interface PresentationsFacet extends Facet {

    /**
     * @return true if facet should apply and save presentations for all supported component in the screen.
     * False by default.
     */
    boolean isAuto();

    /**
     * Set to true if facet should apply and save presentations for all supported component in the screen.
     * False by default.
     *
     * @param auto whether facet should include all components for applying/saving presentations
     */
    void setAuto(boolean auto);

    /**
     * Adds component ids that should be handled when {@link #isAuto()} returns false.
     * <p>
     * Note, component must be attached to the Window, otherwise it will be ignored.
     *
     * @param ids component ids
     */
    void addComponentIds(String... ids);

    /**
     * @return list of component ids that should be handled when {@link #isAuto()} returns false.
     */
    Set<String> getComponentIds();

    /**
     * Collection depends on {@link #isAuto()} property. If {@link #isAuto()} returns true collection will be
     * filled by {@link Window}'s components, otherwise collection will be filled by components were added by
     * {@link #addComponentIds(String...)}.
     *
     * @return components collection that is used for applying and saving presentations.
     */
    Collection<Component> getComponents();
}

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

package io.jmix.flowui.component.filter;

import com.vaadin.flow.component.Component;
import io.jmix.core.annotation.Internal;

/**
 * A composite filter that owns the composition of the data loader condition
 * ({@code base AND own output}) and can refresh it when the application has
 * replaced the loader condition since the filter's last contribution.
 * <p>
 * A condition component whose modifications are delegated loads the data loader
 * directly, bypassing the owner's composition. Before such a load it locates the
 * nearest owner up the component tree and lets it recompose, so the load never
 * runs by a stale base alone.
 */
@Internal
public interface SupportsLoaderConditionRecompose {

    /**
     * Recomposes the data loader condition if the application has replaced it
     * since this filter composed it last; an untouched loader condition is left
     * as is.
     */
    void recomposeLoaderConditionIfOutdated();

    /**
     * Finds the nearest ancestor of the given component that owns the loader
     * condition composition — skipping delegated filter components, which do not
     * compose themselves — and lets it recompose an outdated loader condition.
     * Does nothing when the component has no such ancestor.
     */
    static void recomposeNearestOwner(Component component) {
        Component parent = component.getParent().orElse(null);
        while (parent != null) {
            if (parent instanceof SupportsLoaderConditionRecompose owner
                    && !(parent instanceof FilterComponent filterComponent
                    && filterComponent.isConditionModificationDelegated())) {
                owner.recomposeLoaderConditionIfOutdated();
                return;
            }
            parent = parent.getParent().orElse(null);
        }
    }
}

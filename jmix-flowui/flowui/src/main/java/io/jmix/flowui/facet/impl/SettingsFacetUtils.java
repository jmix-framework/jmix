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

package io.jmix.flowui.facet.impl;

import com.vaadin.flow.component.Component;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.facet.SettingsFacet;

import java.util.Collection;

/**
 * Utility class for applying settings to components using the {@link SettingsFacet}.
 */
@Internal
public final class SettingsFacetUtils {

    /**
     * Applies settings to the specified components using the provided {@link SettingsFacet} instance.
     *
     * @param settingsFacet the settings facet used to apply settings to the components
     * @param components    the collection of components to which the settings are to be applied
     * @throws UnsupportedOperationException if the provided settings facet is not of a supported type
     */
    public static void applySettings(SettingsFacet settingsFacet, Collection<Component> components) {
        if (settingsFacet instanceof SettingsFacetImpl settingsFacetImpl) {
            settingsFacetImpl.applyViewSettings(settingsFacetImpl.getManagedComponentsFromCollection(components));
        } else {
            throw new UnsupportedOperationException(String.format(
                    "Settings facet with type %s isn't supported yet", settingsFacet.getClass()));
        }
    }
}

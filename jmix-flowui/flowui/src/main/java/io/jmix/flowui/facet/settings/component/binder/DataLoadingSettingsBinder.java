/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.facet.settings.component.binder;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.facet.settings.Settings;

/**
 * Settings binder for components which support data loading.
 */
public interface DataLoadingSettingsBinder<V extends Component, S extends Settings>
        extends ComponentSettingsBinder<V, S> {

    /**
     * Applies data loading settings.
     *
     * @param component component to apply
     * @param settings  settings for the component
     */
    void applyDataLoadingSettings(V component, S settings);
}

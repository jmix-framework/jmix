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

package io.jmix.flowui.facet.settings;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.facet.settings.component.binder.ComponentSettingsBinder;

/**
 * Provides information for which component registered settings class.
 */
public interface ViewSettingsComponentRegistry {

    /**
     * @param componentClass component class (e.g. {@link JmixDetails})
     * @return component settings class
     * @throws IllegalStateException if there is no component settings class registered for the component class
     */
    Class<? extends Settings> getSettingsClass(Class<? extends Component> componentClass);

    /**
     * @param componentClass component class (e.g. {@link JmixDetails})
     * @return settings binder
     * @throws IllegalStateException if there is no component settings binder registered for the component class
     */
    ComponentSettingsBinder<?, ?> getSettingsBinder(Class<? extends Component> componentClass);

    /**
     * @param componentClass component class (e.g. {@link JmixDetails})
     * @return {@code true} if settings is registered for the component class
     */
    boolean isSettingsRegisteredFor(Class<? extends Component> componentClass);
}

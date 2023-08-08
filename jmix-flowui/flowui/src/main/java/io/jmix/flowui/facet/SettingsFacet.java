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

package io.jmix.flowui.facet;

import com.vaadin.flow.component.Component;

import java.util.Collection;
import java.util.Set;

public interface SettingsFacet extends Facet {

    boolean isAuto();

    void setAuto(boolean auto);

    void applySettings();

    void applyDataLoadingSettings();

    void saveSettings();

    void addComponentIds(String... ids);

    Set<String> getComponentIds();

    void addExcludedComponentIds(String... ids);

    Set<String> getExcludedComponentIds();

    Collection<Component> getManagedComponents();
}

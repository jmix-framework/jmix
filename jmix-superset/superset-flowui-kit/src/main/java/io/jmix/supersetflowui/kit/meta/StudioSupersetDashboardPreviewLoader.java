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

package io.jmix.supersetflowui.kit.meta;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.supersetflowui.kit.component.JmixSupersetDashboard;
import jakarta.annotation.Nullable;
import org.dom4j.Element;

public class StudioSupersetDashboardPreviewLoader implements StudioPreviewComponentLoader {
    @Override
    public boolean isSupported(Element element) {
        return "http://jmix.io/schema/superset/ui".equals(element.getNamespaceURI())
                && "dashboard".equals(element.getName());
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        JmixSupersetDashboard resultComponent = new JmixSupersetDashboard();
        loadSizeAttributes(resultComponent, componentElement);
        loadClassNames(resultComponent, componentElement);

        loadString(componentElement, "embeddedId", resultComponent::setEmbeddedId);
        loadBoolean(componentElement, "titleVisibility", resultComponent::setTitleVisible);
        loadBoolean(componentElement, "chartControlsVisibility", resultComponent::setChartControlsVisible);
        loadBoolean(componentElement, "filtersExpanded", resultComponent::setFiltersExpanded);


        return resultComponent;
    }
}


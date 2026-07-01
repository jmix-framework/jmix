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

package io.jmix.flowui.view.template.impl;

import io.jmix.flowui.component.LookupComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.StandardListView;

import java.util.Optional;

/**
 * Generic base controller for template-generated list views.
 */
public class TemplateListView extends StandardListView<Object> {

    protected static final String DEFAULT_LOOKUP_COMPONENT_ID = "dataGrid";

    protected String getLookupComponentId() {
        return DEFAULT_LOOKUP_COMPONENT_ID;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<LookupComponent<Object>> findLookupComponent() {
        return UiComponentUtils.findComponent(this, getLookupComponentId())
                .map(component -> (LookupComponent<Object>) component);
    }
}

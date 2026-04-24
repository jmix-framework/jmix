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

import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;

/**
 * Generic base controller for template-generated detail views.
 */
@EditedEntityContainer("entityDc")
public class TemplateDetailView extends StandardDetailView<Object> {

    /**
     * Resolves the entity type from the edited entity container metadata.
     *
     * @return entity Java class bound to the view
     */
    @Override
    protected Class<Object> resolveEntityClass() {
        return getEditedEntityContainer()
                .getEntityMetaClass()
                .getJavaClass();
    }
}

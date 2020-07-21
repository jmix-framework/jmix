/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory for {@link EntityImportViewBuilder}.
 */
@Component(EntityImportViews.NAME)
public class EntityImportViews {

    public static final String NAME = "core_EntityImportViews";

    @Autowired
    protected ObjectProvider<EntityImportViewBuilder> entityImportViewBuilderProvider;

    /**
     * Returns {@link EntityImportViewBuilder} builder for the given entity class.
     */
    public EntityImportViewBuilder builder(Class<? extends JmixEntity> entityClass) {
        return entityImportViewBuilderProvider.getObject(entityClass);
    }
}

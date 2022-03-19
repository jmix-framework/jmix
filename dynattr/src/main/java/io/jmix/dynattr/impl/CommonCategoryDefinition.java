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

package io.jmix.dynattr.impl;

import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.CategoryDefinition;
import io.jmix.dynattr.model.Category;

import java.util.Collection;
import java.util.List;

public class CommonCategoryDefinition implements CategoryDefinition {
    private static final long serialVersionUID = 5280100014769447066L;

    protected final Category category;
    protected final List<AttributeDefinition> attributes;

    public CommonCategoryDefinition(Category category, List<AttributeDefinition> attributes) {
        this.category = category;
        this.attributes = attributes;
    }

    @Override
    public String getId() {
        return category.getId().toString();
    }

    @Override
    public String getName() {
        return category.getName();
    }

    @Override
    public boolean isDefault() {
        return Boolean.TRUE.equals(category.getIsDefault());
    }

    @Override
    public String getEntityType() {
        return category.getEntityType();
    }

    @Override
    public Collection<AttributeDefinition> getAttributeDefinitions() {
        return attributes;
    }

    @Override
    public Object getSource() {
        return category;
    }
}

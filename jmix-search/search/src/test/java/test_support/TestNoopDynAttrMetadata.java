/*
 * Copyright 2025 Haulmont.
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

package test_support;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.CategoryDefinition;
import io.jmix.dynattr.DynAttrMetadata;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class TestNoopDynAttrMetadata implements DynAttrMetadata {
    @Override
    public Collection<AttributeDefinition> getAttributes(MetaClass metaClass) {
        return List.of();
    }

    @Override
    public Collection<CategoryDefinition> getCategories(MetaClass metaClass) {
        return List.of();
    }

    @Override
    public Optional<AttributeDefinition> getAttributeByCode(MetaClass metaClass, String code) {
        return Optional.empty();
    }

    @Override
    public void reload() {

    }
}

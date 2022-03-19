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

package io.jmix.dynattr;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;

import java.util.Collection;
import java.util.Optional;

public interface DynAttrMetadata {

    String DYN_ATTR_CACHE_NAME = "jmix-dyn-attr-cache";

    Collection<AttributeDefinition> getAttributes(MetaClass metaClass);

    Collection<CategoryDefinition> getCategories(MetaClass metaClass);

    Optional<AttributeDefinition> getAttributeByCode(MetaClass metaClass, String code);

    void reload();
}

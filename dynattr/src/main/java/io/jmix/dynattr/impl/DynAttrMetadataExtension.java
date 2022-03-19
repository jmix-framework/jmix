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

import io.jmix.core.MetadataExtension;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.utils.ObjectPathUtils;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.DynAttrUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DynAttrMetadataExtension implements MetadataExtension {

    private final DynAttrMetadata dynAttrMetadata;

    public DynAttrMetadataExtension(DynAttrMetadata dynAttrMetadata) {
        this.dynAttrMetadata = dynAttrMetadata;
    }

    @Override
    public Set<MetaProperty> getAdditionalProperties(MetaClass metaClass) {
        Collection<AttributeDefinition> attributeDefinitions = dynAttrMetadata.getAttributes(metaClass);
        return attributeDefinitions.stream()
                .map(AttributeDefinition::getMetaProperty)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAdditionalProperty(MetaClass metaClass, String propertyName) {
        String code = DynAttrUtils.getAttributeCodeFromProperty(propertyName);
        return dynAttrMetadata.getAttributeByCode(metaClass, code).isPresent();
    }
}

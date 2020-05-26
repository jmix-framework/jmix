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

import io.jmix.core.MetaPropertyPathResolver;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.DynAttrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(DynAttrMetaPropertyPathResolver.NAME)
public class DynAttrMetaPropertyPathResolver implements MetaPropertyPathResolver {
    public static final String NAME = "dynattr_DynAttrMetaPropertyPathResolver";

    @Autowired
    protected DynAttrMetadata dynAttrMetadata;

    @Override
    public MetaPropertyPath resolveMetaPropertyPath(MetaClass metaClass, String propertyPath) {
        if (DynAttrUtils.isDynamicAttributeProperty(propertyPath)) {
            return dynAttrMetadata.getAttributeByCode(metaClass, DynAttrUtils.getAttributeCodeFromProperty(propertyPath))
                    .map(attr -> new MetaPropertyPath(metaClass, attr.getMetaProperty()))
                    .orElse(null);
        }
        return null;
    }
}

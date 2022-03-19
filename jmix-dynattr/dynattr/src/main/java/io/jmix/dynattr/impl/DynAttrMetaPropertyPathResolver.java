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
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.DynAttrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("dynat_DynAttrMetaPropertyPathResolver")
public class DynAttrMetaPropertyPathResolver implements MetaPropertyPathResolver {

    @Autowired
    protected DynAttrMetadata dynAttrMetadata;

    @Override
    public MetaPropertyPath resolveMetaPropertyPath(MetaClass metaClass, String propertyPath) {
        String[] properties = propertyPath.split("\\.");
        MetaProperty[] metaProperties = new MetaProperty[properties.length];

        MetaProperty currentProperty;
        MetaClass currentClass = metaClass;

        for (int i = 0; i < properties.length; i++) {
            if (currentClass == null) {
                return null;
            }
            currentProperty = currentClass.findProperty(properties[i]);
            if (currentProperty == null) {
                if (!DynAttrUtils.isDynamicAttributeProperty(properties[i])) {
                    return null;
                }

                AttributeDefinition attribute = dynAttrMetadata.getAttributeByCode(currentClass,
                        DynAttrUtils.getAttributeCodeFromProperty(properties[i])).orElse(null);
                if (attribute == null) {
                    return null;
                }

                currentProperty = attribute.getMetaProperty();
            }

            Range range = currentProperty.getRange();
            currentClass = range.isClass() ? range.asClass() : null;

            metaProperties[i] = currentProperty;
        }

        return new MetaPropertyPath(metaClass, metaProperties);
    }
}

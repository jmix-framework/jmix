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

package io.jmix.dynattrflowui.impl;

import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("dynat_AttributeDependencies")
public class AttributeDependencies {


    protected final DynAttrMetadata dynAttrMetadata;

    public AttributeDependencies(DynAttrMetadata dynAttrMetadata) {
        this.dynAttrMetadata = dynAttrMetadata;
    }

    public Set<AttributeDefinition> getDependentAttributes(AttributeDefinition attribute) {
        Set<AttributeDefinition> dependentAttributes = new HashSet<>();
        Collection<AttributeDefinition> attributes = dynAttrMetadata.getAttributes(attribute.getMetaProperty().getDomain());
        for (AttributeDefinition currentAttribute : attributes) {
            if (currentAttribute.getConfiguration().getDependsOnAttributeCodes() != null && !currentAttribute.getConfiguration().getDependsOnAttributeCodes().isEmpty()) {
                List<AttributeDefinition> attributeDefinitions = currentAttribute.getConfiguration().getDependsOnAttributeCodes()
                        .stream()
                        .map(code -> dynAttrMetadata.getAttributeByCode(currentAttribute.getMetaProperty().getDomain(), code))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();
//          todo is it fine ????????
                if (attributeDefinitions.contains(attribute)) {
                    dependentAttributes.add(currentAttribute);
                }
            }
        }
        return dependentAttributes;
    }
}

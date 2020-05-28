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

package io.jmix.dynattrui.impl;

import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component(AttributeDependencies.NAME)
public class AttributeDependencies {
    public static final String NAME = "dynattrui_AttributeDependencies";

    @Autowired
    protected DynAttrMetadata dynAttrMetadata;

    public Set<AttributeDefinition> getDependentAttributes(AttributeDefinition attribute) {
        Set<AttributeDefinition> dependentAttributes = new HashSet<>();
        Collection<AttributeDefinition> attributes = dynAttrMetadata.getAttributes(attribute.getMetaProperty().getDomain());
        for (AttributeDefinition currentAttribute : attributes) {
            if (currentAttribute.getConfiguration().getDependsOnAttributes() != null)
            if (currentAttribute.getConfiguration().getDependsOnAttributes().contains(attribute)) {
                dependentAttributes.add(currentAttribute);
            }
        }
        return dependentAttributes;
    }
}

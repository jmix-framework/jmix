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

package io.jmix.dynattr.impl;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.data.persistence.NonJpaPropertyConditionSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("dynat_DynAttrNonJpaPropertyConditionSupport")
public class DynAttrNonJpaPropertyConditionSupport implements NonJpaPropertyConditionSupport {

    @Autowired
    protected MetadataTools metadataTools;

    @Override
    public boolean supports(MetaClass metaClass, PropertyCondition propertyCondition) {
        MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPathOrNull(metaClass, propertyCondition.getProperty());
        if (metaPropertyPath == null) {
            return false;
        }

        for (MetaProperty metaProperty : metaPropertyPath.getMetaProperties()) {
            if (metaProperty instanceof DynAttrMetaProperty) {
                return true;
            }
        }
        return false;
    }
}

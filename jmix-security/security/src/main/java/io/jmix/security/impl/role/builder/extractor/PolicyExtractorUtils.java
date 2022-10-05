/*
 * Copyright 2022 Haulmont.
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

package io.jmix.security.impl.role.builder.extractor;

import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.stereotype.Component;

@Component("sec_PolicyExtractorUtils")
public class PolicyExtractorUtils {

    protected Metadata metadata;

    protected ExtendedEntities extendedEntities;

    public PolicyExtractorUtils(Metadata metadata, ExtendedEntities extendedEntities) {
        this.metadata = metadata;
        this.extendedEntities = extendedEntities;
    }

    /**
     * Returns an actual entity name of the given entityClass. For example, if entity Foo is being replaced by entity
     * ExtFoo using the ReplaceEntity annotation then the "Foo" entity name will be returned for the Foo.class, not
     * "ExtFoo". This method is required because metadata.getClass(Foo.class) will return you a metaclass for ExtFoo.
     */
    public String getEntityNameByEntityClass(Class<?> entityClass) {
        String entityName;
        MetaClass metaClass = metadata.getClass(entityClass);
        MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
        if (originalMetaClass != null &&
                metaClass != originalMetaClass &&
                entityClass.equals(originalMetaClass.getJavaClass())) {
            //in case entityClass points to an entity that is being replaced by some other entity using
            //the @ReplaceEntity annotation
            entityName = originalMetaClass.getName();
        } else {
            entityName = metaClass.getName();
        }
        return entityName;
    }

}

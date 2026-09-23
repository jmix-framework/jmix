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

package io.jmix.datatools;

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.Stores;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Decides which entities and properties the entity inspector can work with, based on their data
 * store rather than on JPA annotations, so that entities of non-JPA stores are supported.
 */
@NullMarked
@Component("datatl_EntityInspectorSupport")
public class EntityInspectorSupport {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;

    /**
     * Returns the entities the inspector can browse: JPA entities, and entities of any other real
     * data store. Classes without a store, JPA embeddables, and system-level classes of non-JPA
     * stores are left out.
     *
     * @return inspectable meta-classes, empty if none
     */
    public Collection<MetaClass> getInspectableEntityMetaClasses() {
        return metadata.getSession().getClasses().stream()
                .filter(this::isInspectable)
                .toList();
    }

    /**
     * Returns whether the entity's data store accepts a JPQL query string. A store that does not
     * must be loaded with conditions only.
     *
     * @param metaClass entity meta-class
     * @return {@code true} if a JPQL query string can be used
     */
    public boolean supportsJpqlQuery(MetaClass metaClass) {
        return metaClass.getStore().getDescriptor().isJpa();
    }

    /**
     * Returns whether the property holds a value that its data store persists: a JPA attribute, or
     * any property of a real store. A transient property has no store of its own and is left out.
     *
     * @param metaProperty property to check
     * @return {@code true} if the property is stored
     */
    public boolean isStoredProperty(MetaProperty metaProperty) {
        return metadataTools.isJpa(metaProperty)
                || isRealStore(metaProperty.getStore().getName());
    }

    protected boolean isInspectable(MetaClass metaClass) {
        if (!isRealStore(metaClass.getStore().getName())) {
            return false;
        }
        if (metadataTools.isJpaEmbeddable(metaClass)) {
            return false;
        }
        // A JPA store holds mapped entities only; any other real store manages whatever it registers.
        if (metaClass.getStore().getDescriptor().isJpa()) {
            return metadataTools.isJpaEntity(metaClass);
        }
        // A non-JPA store may register infrastructure classes holding no data, marked system-level.
        return !metadataTools.isSystemLevel(metaClass);
    }

    protected boolean isRealStore(String storeName) {
        return !Stores.NOOP.equals(storeName) && !Stores.UNDEFINED.equals(storeName);
    }
}

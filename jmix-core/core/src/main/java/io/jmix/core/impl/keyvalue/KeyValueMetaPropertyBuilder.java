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

package io.jmix.core.impl.keyvalue;

import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.metamodel.datatype.impl.EnumerationImpl;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.metamodel.model.Session;
import io.jmix.core.metamodel.model.impl.ClassRange;
import io.jmix.core.metamodel.model.impl.DatatypeRange;
import io.jmix.core.metamodel.model.impl.EnumerationRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * DEPRECATED. Use {@link KeyValueMetaClassFactory} instead.
 */
@Deprecated
@Component("core_KeyValueMetaPropertyBuilder")
public class KeyValueMetaPropertyBuilder {

    @Autowired
    private Metadata metadata;

    @Autowired
    private DatatypeRegistry datatypeRegistry;

    public KeyValueMetaProperty build(MetaClass metaClass, String name, Class javaClass) {
        MetaProperty.Type type;
        Range range;
        Session metadataSession = metadata.getSession();
        if (Entity.class.isAssignableFrom(javaClass)) {
            range = new ClassRange(metadataSession.findClass(javaClass));
            type = MetaProperty.Type.ASSOCIATION;
        } else if (EnumClass.class.isAssignableFrom(javaClass)) {
            @SuppressWarnings("unchecked")
            EnumerationImpl enumeration = new EnumerationImpl(javaClass);
            range = new EnumerationRange(enumeration);
            type = MetaProperty.Type.ENUM;
        } else {
            @SuppressWarnings("unchecked")
            Datatype datatype = datatypeRegistry.get(javaClass);

            range = new DatatypeRange(datatype);
            type = MetaProperty.Type.DATATYPE;
        }
        return new KeyValueMetaProperty(metaClass, name, javaClass, range, type);
    }

    public KeyValueMetaProperty build(MetaClass metaClass, String name, Datatype datatype) {
        return new KeyValueMetaProperty(metaClass, name, datatype.getJavaClass(), new DatatypeRange(datatype), MetaProperty.Type.DATATYPE);
    }

    public KeyValueMetaProperty build(MetaClass metaClass, String name, MetaClass propertyMetaClass) {
        return new KeyValueMetaProperty(
                metaClass,
                name,
                propertyMetaClass.getJavaClass(),
                new ClassRange(propertyMetaClass),
                MetaProperty.Type.ASSOCIATION
        );
    }
}

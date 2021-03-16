/*
 * Copyright 2021 Haulmont.
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

package io.jmix.hibernate.impl;

import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.hibernate.impl.load.InitialLoadedState;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;

import static io.jmix.hibernate.impl.HibernateUtils.initializeAndUnproxy;

@Component("hibernate_LoadedValueProvider")
public class LoadedValueProvider {

    @Autowired
    protected Metadata metadata;

    public Set<String> getLoadedProperties(Object entity) {
        if (entity instanceof Entity) {
            InitialLoadedState state = EntitySystemAccess.getExtraState(entity, InitialLoadedState.class);
            if (state != null) {
                return state.getLoadedMap().keySet();
            }
        }
        return Collections.emptySet();
    }

    @Nullable
    public Object getLoadedValue(Object entity, String attribute) {
        if (entity instanceof Entity) {
            InitialLoadedState state = EntitySystemAccess.getExtraState(entity, InitialLoadedState.class);
            if (state != null) {
                return state.getLoadedValue(attribute);
            }
        }

        return null;
    }

    @Nullable
    public Object convertLoadedValue(Object entity, String attribute, Object value) {
        if (value instanceof HibernateProxy) {
            return HibernateUtils.initializeAndUnproxy(value);
        } else if (value instanceof Collection) {
            Collection<Object> coll = (Collection<Object>) value;
            Collection<Object> unproxyColl = value instanceof List ? new ArrayList<>() : new LinkedHashSet<>();
            for (Object item : coll) {
                unproxyColl.add(initializeAndUnproxy(item));
            }
            return unproxyColl;
        } else {
            if (entity != null) {
                MetaClass metaClass = metadata.getClass(entity);
                return convertSimpleValueIfNeeded(metaClass.getProperty(attribute), value);
            }
        }
        return value;
    }


    private Object convertSimpleValueIfNeeded(MetaProperty property, Object value) {
        if (property.getRange().isEnum() && !(value instanceof EnumClass)) {
            for (Object enumValue : property.getRange().asEnumeration().getValues()) {
                if (enumValue instanceof EnumClass && ((EnumClass<?>) enumValue).getId().equals(value)) {
                    return enumValue;
                }
            }
        }
        return value;
    }
}

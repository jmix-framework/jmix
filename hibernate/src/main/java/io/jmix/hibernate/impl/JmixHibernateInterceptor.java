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

package io.jmix.hibernate.impl;

import io.jmix.core.Entity;
import io.jmix.core.EntityStates;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.hibernate.impl.load.InitialLoadedState;
import org.hibernate.EmptyInterceptor;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component("hibernate_JmixHibernateInterceptor")
public class JmixHibernateInterceptor extends EmptyInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JmixHibernateInterceptor.class);

    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected MetadataTools metadataTools;

    protected MetadataImplementor hibernateMetadata;

    public JmixHibernateInterceptor(MetadataImplementor hibernateMetadata) {
        this.hibernateMetadata = hibernateMetadata;
    }

    @Override
    public Boolean isTransient(Object entity) {
        if (entity instanceof Entity) {
            return !metadataTools.isJpaEntity(entity.getClass()) || entityStates.isNew(entity);
        }
        return super.isTransient(entity);
    }

    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        saveState(entity, state, propertyNames);
        return false;
    }


    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        saveState(entity, state, propertyNames);
        return false;
    }

    private void saveState(Object entity, Object[] state, String[] propertyNames) {
        if (entity instanceof Entity) {
            InitialLoadedState.Builder builder = InitialLoadedState.builder();
            for (int i = 0; i < propertyNames.length; i++) {
                builder.value(propertyNames[i], state[i]);
            }
            EntitySystemAccess.addExtraState(entity, builder.build(EntitySystemAccess.getEntityEntry(entity)));
        }
    }
}

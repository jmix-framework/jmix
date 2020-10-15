/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.core.app;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.global.ViewRepository;
import io.jmix.core.Entity;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Service(RelatedEntitiesService.NAME)
public class RelatedEntitiesServiceBean implements RelatedEntitiesService {

    @Inject
    protected Persistence persistence;

    @Inject
    protected Metadata metadata;

    @Inject
    protected ViewRepository viewRepository;

    @Inject
    protected ExtendedEntities extendedEntities;

    @Override
    public List<Object> getRelatedIds(List<Object> parentIds, String parentMetaClass, String relationProperty) {
        checkNotNullArgument(parentIds, "parents argument is null");
        checkNotNullArgument(parentMetaClass, "parentMetaClass argument is null");
        checkNotNullArgument(relationProperty, "relationProperty argument is null");

        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(metadata.getClassNN(parentMetaClass));
        Class<? extends Entity> parentClass = metaClass.getJavaClass();

        MetaProperty metaProperty = metaClass.getProperty(relationProperty);

        // return empty list only after all argument checks
        if (parentIds.isEmpty()) {
            return Collections.emptyList();
        }

        MetaClass propertyMetaClass = extendedEntities.getEffectiveMetaClass(metaProperty.getRange().asClass());
        Class<? extends Entity> propertyClass = propertyMetaClass.getJavaClass();

        List<Object> relatedIds = new ArrayList<>();

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            String parentPrimaryKey = metadata.getTools().getPrimaryKeyName(metaClass);
            String queryString = "select x from " + parentMetaClass + " x where x." +
                    parentPrimaryKey + " in :ids";
            Query query = em.createQuery(queryString);

            String relatedPrimaryKey = metadata.getTools().getPrimaryKeyName(propertyMetaClass);
            View view = new View(parentClass);
            view.addProperty(relationProperty, new View(propertyClass).addProperty(relatedPrimaryKey));

            query.setView(view);
            query.setParameter("ids", parentIds);

            List resultList = query.getResultList();
            for (Object obj : resultList) {
                Entity e = (Entity) obj;
                Object value = EntityValues.getValue(e, relationProperty);
                if (value instanceof Entity) {
                    relatedIds.add(EntityValues.getId(value));
                } else if (value instanceof Collection) {
                    for (Object collectionItem : (Collection) value) {
                        relatedIds.add(EntityValues.getId(collectionItem));
                    }
                }
            }

            tx.commit();
        } finally {
            tx.end();
        }

        return relatedIds;
    }
}
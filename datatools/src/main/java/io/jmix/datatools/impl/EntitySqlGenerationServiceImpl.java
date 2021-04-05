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

package io.jmix.datatools.impl;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.data.PersistenceHints;
import io.jmix.datatools.EntitySqlGenerationService;
import io.jmix.datatools.EntitySqlGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Primary
@Component("datatl_EntitySqlGenerationService")
public class EntitySqlGenerationServiceImpl implements EntitySqlGenerationService {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public String generateInsertScript(Object entity) {
        checkNotNullArgument(entity);

        EntitySqlGenerator generator = applicationContext.getBean(EntitySqlGenerator.class, entity.getClass());
        entity = reload(entity);

        return generator.generateInsertScript(entity);
    }

    @Override
    public String generateUpdateScript(Object entity) {
        checkNotNullArgument(entity);

        EntitySqlGenerator generator = applicationContext.getBean(EntitySqlGenerator.class, entity.getClass());
        entity = reload(entity);

        return generator.generateUpdateScript(entity);
    }

    @Override
    public String generateSelectScript(Object entity) {
        checkNotNullArgument(entity);

        EntitySqlGenerator generator = applicationContext.getBean(EntitySqlGenerator.class, entity.getClass());
        return generator.generateSelectScript(entity);
    }

    protected Object reload(Object entity) {
        Object id = EntityValues.getId(entity);
        if (id == null) {
            return entity;
        }

        Class<?> entityClass = entity.getClass();
        FetchPlan fetchPlan = createFullFetchPlan(entityClass);
        return dataManager.load(entityClass)
                .id(id)
                .fetchPlan(fetchPlan)
                .hint(PersistenceHints.SOFT_DELETION, false)
                .one();
    }

    protected FetchPlan createFullFetchPlan(Class<?> entityClass) {
        MetaClass metaClass = metadata.getClass(entityClass);
        return createFullFetchPlanBuilder(entityClass, metaClass).build();
    }

    protected FetchPlanBuilder createFullFetchPlanBuilder(MetaClass metaClass) {
        return createFullFetchPlanBuilder(metaClass.getJavaClass(), metaClass);
    }

    protected FetchPlanBuilder createFullFetchPlanBuilder(Class<?> entityClass, MetaClass metaClass) {
        FetchPlanBuilder builder = fetchPlans.builder(entityClass)
                .addFetchPlan(FetchPlan.LOCAL)
                .addSystem();

        for (MetaProperty metaProperty : metaClass.getProperties()) {
            if (isReferenceField(metaProperty)) {
                builder.add(metaProperty.getName(), FetchPlan.INSTANCE_NAME);
            } else if (metadataTools.isEmbedded(metaProperty)) {
                FetchPlanBuilder embeddedBuilder = createFullFetchPlanBuilder(metaProperty.getRange().asClass());
                builder.add(metaProperty.getName(), embeddedBuilder);
            }
        }

        return builder;
    }

    protected boolean isReferenceField(MetaProperty metaProperty) {
        Range range = metaProperty.getRange();
        return range.isClass() && !range.getCardinality().isMany();
    }
}

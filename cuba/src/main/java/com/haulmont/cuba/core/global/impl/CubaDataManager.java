/*
 * Copyright 2019 Haulmont.
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

package com.haulmont.cuba.core.global.impl;

import com.haulmont.cuba.CubaProperties;
import com.haulmont.cuba.core.entity.contracts.Id;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.validation.EntityValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component(DataManager.NAME)
public class CubaDataManager implements DataManager {

    private static final Logger log = LoggerFactory.getLogger(CubaDataManager.class);

    @Autowired
    protected io.jmix.core.DataManager delegate;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected CubaProperties properties;

    @Autowired
    protected BeanValidation beanValidation;

    @Autowired
    protected BeanLocator beanLocator;

    @Nullable
    @Override
    public <E extends JmixEntity> E load(LoadContext<E> context) {
        return delegate.load(context);
    }

    @Override
    public <E extends JmixEntity> List<E> loadList(LoadContext<E> context) {
        return delegate.loadList(context);
    }

    @Override
    public long getCount(LoadContext<? extends JmixEntity> context) {
        return delegate.getCount(context);
    }

    @Override
    public <E extends JmixEntity> E reload(E entity, String fetchPlanName) {
        Preconditions.checkNotNullArgument(fetchPlanName, "fetchPlanName is null");
        return reload(entity, fetchPlanRepository.getFetchPlan(entity.getClass(), fetchPlanName));
    }

    @Override
    public <E extends JmixEntity> E reload(E entity, FetchPlan fetchPlan) {
        return reload(entity, fetchPlan, null);
    }

    @Override
    public <E extends JmixEntity> E reload(E entity, FetchPlan fetchPlan, @Nullable MetaClass metaClass) {
        return reload(entity, fetchPlan, metaClass, entityHasDynamicAttributes(entity));
    }

    @Override
    public <E extends JmixEntity> E reload(E entity, FetchPlan fetchPlan, @Nullable MetaClass metaClass, boolean loadDynamicAttributes) {
        if (metaClass == null) {
            metaClass = metadata.getSession().getClass(entity.getClass());
        }
        LoadContext<E> context = new LoadContext<>(metaClass);
        context.setId(EntityValues.getId(entity));
        context.setFetchPlan(fetchPlan);
        context.setLoadDynamicAttributes(loadDynamicAttributes);

        E reloaded = load(context);
        if (reloaded == null)
            throw new EntityAccessException(metaClass, EntityValues.getId(entity));

        return reloaded;
    }

    protected boolean entityHasDynamicAttributes(JmixEntity entity) {
        return false;

        // todo dynamic attributes
//        return entity instanceof BaseGenericIdEntity
//                && ((BaseGenericIdEntity) entity).getDynamicAttributes() != null;
    }

    @Override
    public EntitySet commit(CommitContext context) {
        validate(context);
        return delegate.save(context);
    }

    @Override
    public EntitySet commit(JmixEntity... entities) {
        return commit(new CommitContext(entities));
    }

    @Override
    public <E extends JmixEntity> E commit(E entity, @Nullable FetchPlan fetchPlan) {
        return commit(new CommitContext().addInstanceToCommit(entity, fetchPlan)).get(entity);
    }

    @Override
    public <E extends JmixEntity> E commit(E entity, @Nullable String fetchPlanName) {
        if (fetchPlanName != null) {
            FetchPlan view = fetchPlanRepository.getFetchPlan(metadata.getClass(entity.getClass()), fetchPlanName);
            return commit(entity, view);
        } else {
            return commit(entity, (FetchPlan) null);
        }
    }

    @Override
    public <E extends JmixEntity> E commit(E entity) {
        return commit(entity, (FetchPlan) null);
    }

    @Override
    public void remove(JmixEntity entity) {
        CommitContext context = new CommitContext(
                Collections.<JmixEntity>emptyList(),
                Collections.singleton(entity));
        commit(context);
    }

    @Override
    public <T extends JmixEntity, K> void remove(Id<T, K> entityId) {
        remove(getReference(entityId));
    }

    @Override
    public List<KeyValueEntity> loadValues(ValueLoadContext context) {
        return delegate.loadValues(context);
    }

    @Override
    public DataManager secure() {
        return new Secure(this, metadata);
    }

    @Override
    public io.jmix.core.DataManager getDelegate() {
        return delegate;
    }

    @Override
    public <E extends JmixEntity> FluentLoader<E> load(Class<E> entityClass) {
        FluentLoader<E> loader = beanLocator.getPrototype(FluentLoader.class, entityClass);
        loader.setDataManager(getDelegate());
        loader.joinTransaction(false);
        return loader;
    }

    @Override
    public <E extends JmixEntity, K> FluentLoader.ById<E> load(Id<E, K> entityId) {
        FluentLoader<E> loader = beanLocator.getPrototype(FluentLoader.class, entityId.getEntityClass());
        loader.setDataManager(getDelegate());
        loader.joinTransaction(false);
        return loader.id(entityId.getValue());
    }

    @Override
    public FluentValuesLoader loadValues(String queryString) {
        return new FluentValuesLoader(queryString, delegate);
    }

    @Override
    public <T> FluentValueLoader<T> loadValue(String queryString, Class<T> valueClass) {
        return new FluentValueLoader<>(queryString, valueClass, delegate);
    }

    @Override
    public <T extends JmixEntity> T create(Class<T> entityClass) {
        return delegate.create(entityClass);
    }

    @Override
    public <T extends JmixEntity, K> T getReference(Class<T> entityClass, K id) {
        return delegate.getReference(entityClass, id);
    }

    protected void validate(CommitContext context) {
        if (CommitContext.ValidationMode.DEFAULT == context.getValidationMode() && properties.isDataManagerBeanValidation()
                || CommitContext.ValidationMode.ALWAYS_VALIDATE == context.getValidationMode()) {
            for (JmixEntity entity : context.getCommitInstances()) {
                validateEntity(entity, context.getValidationGroups());
            }
        }
    }

    @Override
    public <T extends JmixEntity, K> T getReference(Id<T, K> entityId) {
        Preconditions.checkNotNullArgument(entityId, "entityId is null");
        return getReference(entityId.getEntityClass(), entityId.getValue());
    }

    protected void validateEntity(JmixEntity entity, List<Class> validationGroups) {
        Validator validator = beanValidation.getValidator();
        Set<ConstraintViolation<JmixEntity>> violations;
        if (validationGroups == null || validationGroups.isEmpty()) {
            violations = validator.validate(entity);
        } else {
            violations = validator.validate(entity, validationGroups.toArray(new Class[0]));
        }
        if (!violations.isEmpty())
            throw new EntityValidationException(String.format("Entity %s validation failed.", entity.toString()), violations);
    }

    private static class Secure extends CubaDataManager {

        private DataManager dataManager;

        public Secure(DataManager dataManager, Metadata metadata) {
            this.dataManager = dataManager;
            this.metadata = metadata;
        }

        @Override
        public io.jmix.core.DataManager getDelegate() {
            return dataManager.getDelegate();
        }

        @Nullable
        @Override
        public <E extends JmixEntity> E load(LoadContext<E> context) {
            context.setAuthorizationRequired(true);
            return dataManager.load(context);
        }

        @Override
        public <E extends JmixEntity> List<E> loadList(LoadContext<E> context) {
            context.setAuthorizationRequired(true);
            return dataManager.loadList(context);
        }

        @Override
        public List<KeyValueEntity> loadValues(ValueLoadContext context) {
            //TODO: fix API usage with access constraints
            //context.setAuthorizationRequired(true);
            return dataManager.loadValues(context);
        }

        @Override
        public long getCount(LoadContext<? extends JmixEntity> context) {
            context.setAuthorizationRequired(true);
            return dataManager.getCount(context);
        }

        @Override
        public EntitySet commit(CommitContext context) {
            //TODO: fix API usage with access constraints
            //context.setAuthorizationRequired(true);
            return dataManager.commit(context);
        }
    }
}

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
import io.jmix.core.Entity;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import com.haulmont.cuba.core.global.LoadContext;
import io.jmix.core.*;
import io.jmix.core.commons.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.validation.EntityValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;
import java.util.stream.Collectors;

@Component(DataManager.NAME)
public class CubaDataManager implements DataManager {

    private static final Logger log = LoggerFactory.getLogger(CubaDataManager.class);

    @Inject
    protected io.jmix.core.DataManager delegate;

    @Inject
    protected Metadata metadata;

    @Inject
    protected MetadataTools metadataTools;

    @Inject
    protected FetchPlanRepository fetchPlanRepository;

    @Inject
    protected EntityStates entityStates;

    @Inject
    protected CubaProperties properties;

    @Inject
    protected BeanValidation beanValidation;

    @Nullable
    @Override
    public <E extends Entity> E load(LoadContext<E> context) {
        return delegate.load(context);
    }

    @Override
    public <E extends Entity> List<E> loadList(LoadContext<E> context) {
        return delegate.loadList(context);
    }

    @Override
    public long getCount(LoadContext<? extends Entity> context) {
        return delegate.getCount(context);
    }

    @Override
    public <E extends Entity> E reload(E entity, String fetchPlanName) {
        Preconditions.checkNotNullArgument(fetchPlanName, "fetchPlanName is null");
        return reload(entity, fetchPlanRepository.getFetchPlan(entity.getClass(), fetchPlanName));
    }

    @Override
    public <E extends Entity> E reload(E entity, FetchPlan fetchPlan) {
        return reload(entity, fetchPlan, null);
    }

    @Override
    public <E extends Entity> E reload(E entity, FetchPlan fetchPlan, @Nullable MetaClass metaClass) {
        return reload(entity, fetchPlan, metaClass, entityHasDynamicAttributes(entity));
    }

    @Override
    public <E extends Entity> E reload(E entity, FetchPlan fetchPlan, @Nullable MetaClass metaClass, boolean loadDynamicAttributes) {
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

    protected boolean entityHasDynamicAttributes(Entity entity) {
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
    public EntitySet commit(Entity... entities) {
        return commit(new CommitContext(entities));
    }

    @Override
    public <E extends Entity> E commit(E entity, @Nullable FetchPlan fetchPlan) {
        return commit(new CommitContext().addInstanceToCommit(entity, fetchPlan)).get(entity);
    }

    @Override
    public <E extends Entity> E commit(E entity, @Nullable String fetchPlanName) {
        if (fetchPlanName != null) {
            FetchPlan view = fetchPlanRepository.getFetchPlan(metadata.getClass(entity.getClass()), fetchPlanName);
            return commit(entity, view);
        } else {
            return commit(entity, (FetchPlan) null);
        }
    }

    @Override
    public <E extends Entity> E commit(E entity) {
        return commit(entity, (FetchPlan) null);
    }

    @Override
    public void remove(Entity entity) {
        CommitContext context = new CommitContext(
                Collections.<Entity>emptyList(),
                Collections.singleton(entity));
        commit(context);
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
    public FluentValuesLoader loadValues(String queryString) {
        return new FluentValuesLoader(queryString, delegate);
    }

    @Override
    public <T> FluentValueLoader<T> loadValue(String queryString, Class<T> valueClass) {
        return new FluentValueLoader<>(queryString, valueClass, delegate);
    }

    @Override
    public <T extends Entity> T create(Class<T> entityClass) {
        return delegate.create(entityClass);
    }

    @Override
    public <T extends Entity, K> T getReference(Class<T> entityClass, K id) {
        return delegate.getReference(entityClass, id);
    }

    protected void validate(CommitContext context) {
        if (CommitContext.ValidationMode.DEFAULT == context.getValidationMode() && properties.isDataManagerBeanValidation()
                || CommitContext.ValidationMode.ALWAYS_VALIDATE == context.getValidationMode()) {
            for (io.jmix.core.Entity entity : context.getCommitInstances()) {
                validateEntity(entity, context.getValidationGroups());
            }
        }
    }

    protected void validateEntity(io.jmix.core.Entity entity, List<Class> validationGroups) {
        Validator validator = beanValidation.getValidator();
        Set<ConstraintViolation<io.jmix.core.Entity>> violations;
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
        public <E extends Entity> E load(LoadContext<E> context) {
            context.setAuthorizationRequired(true);
            return dataManager.load(context);
        }

        @Override
        public <E extends Entity> List<E> loadList(LoadContext<E> context) {
            context.setAuthorizationRequired(true);
            return dataManager.loadList(context);
        }

        @Override
        public List<KeyValueEntity> loadValues(ValueLoadContext context) {
            context.setAuthorizationRequired(true);
            return dataManager.loadValues(context);
        }

        @Override
        public long getCount(LoadContext<? extends Entity> context) {
            context.setAuthorizationRequired(true);
            return dataManager.getCount(context);
        }

        @Override
        public EntitySet commit(CommitContext context) {
            context.setAuthorizationRequired(true);
            return dataManager.commit(context);
        }
    }
}

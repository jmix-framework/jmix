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
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.EntitySet;
import com.haulmont.cuba.core.global.FluentLoader;
import com.haulmont.cuba.core.global.FluentValueLoader;
import com.haulmont.cuba.core.global.FluentValuesLoader;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.ValueLoadContext;
import com.haulmont.cuba.core.global.*;
import io.jmix.core.Metadata;
import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.constraint.AccessConstraint;
import io.jmix.core.constraint.RowLevelConstraint;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.validation.EntityValidationException;
import io.jmix.dynattr.DynamicAttributesState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;

import static io.jmix.core.entity.EntitySystemAccess.getExtraState;

@Component(DataManager.NAME)
public class CubaDataManager implements DataManager {
    protected Metadata metadata;
    protected FetchPlanRepository fetchPlanRepository;
    protected CubaProperties properties;
    protected Validator validator;
    protected ApplicationContext applicationContext;
    protected AccessConstraintsRegistry accessConstraintsRegistry;

    protected io.jmix.core.UnconstrainedDataManager delegate;

    @Autowired
    public CubaDataManager(Metadata metadata,
                           FetchPlanRepository fetchPlanRepository,
                           CubaProperties properties,
                           Validator validator,
                           AccessConstraintsRegistry accessConstraintsRegistry,
                           ApplicationContext applicationContext) {
        this.metadata = metadata;
        this.fetchPlanRepository = fetchPlanRepository;
        this.properties = properties;
        this.validator = validator;
        this.accessConstraintsRegistry = accessConstraintsRegistry;
        this.applicationContext = applicationContext;

        initDelegate();
    }

    protected void initDelegate() {
        this.delegate = new RowLevelConstraintsDataManager(applicationContext.getBean(UnconstrainedDataManager.class),
                accessConstraintsRegistry);
    }

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
        DynamicAttributesState state = getExtraState(entity, DynamicAttributesState.class);
        if (state != null) {
            return state.getDynamicAttributes() != null;
        }
        return false;
    }

    @Override
    public EntitySet commit(CommitContext context) {
        validate(context);
        io.jmix.core.EntitySet entitySet = delegate.save(context);
        return new EntitySet(entitySet);
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
            FetchPlan view = fetchPlanRepository.getFetchPlan(metadata.getClass(entity), fetchPlanName);
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
    public <T extends Entity, K> void remove(Id<T, K> entityId) {
        remove(getReference(entityId));
    }

    @Override
    public List<KeyValueEntity> loadValues(ValueLoadContext context) {
        return delegate.loadValues(context);
    }

    @Override
    public DataManager secure() {
        return new Secure(metadata, fetchPlanRepository, properties, validator, accessConstraintsRegistry,
                applicationContext);
    }

    @Override
    public io.jmix.core.UnconstrainedDataManager getDelegate() {
        return delegate;
    }

    @Override
    public <E extends Entity> FluentLoader<E> load(Class<E> entityClass) {
        //noinspection unchecked
        FluentLoader<E> loader = applicationContext.getBean(FluentLoader.class, entityClass);
        loader.setDataManager(getDelegate());
        loader.joinTransaction(false);
        return loader;
    }

    @Override
    public <E extends Entity, K> FluentLoader.ById<E> load(Id<E, K> entityId) {
        //noinspection unchecked
        FluentLoader<E> loader = applicationContext.getBean(FluentLoader.class, entityId.getEntityClass());
        loader.setDataManager(getDelegate());
        loader.joinTransaction(false);
        return loader.id(entityId.getValue());
    }

    @Override
    public FluentValuesLoader loadValues(String queryString) {
        FluentValuesLoader loader = applicationContext.getBean(FluentValuesLoader.class, queryString);
        loader.setDataManager(delegate);
        return loader;
    }

    @Override
    public <T> FluentValueLoader<T> loadValue(String queryString, Class<T> valueClass) {
        //noinspection unchecked
        FluentValueLoader<T> loader = applicationContext.getBean(FluentValueLoader.class, queryString, valueClass);
        loader.setDataManager(delegate);
        return loader;
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
            for (Entity entity : context.getCommitInstances()) {
                validateEntity(entity, context.getValidationGroups());
            }
        }
    }

    @Override
    public <T extends Entity, K> T getReference(Id<T, K> entityId) {
        Preconditions.checkNotNullArgument(entityId, "entityId is null");
        return getReference(entityId.getEntityClass(), entityId.getValue());
    }

    protected void validateEntity(Entity entity, List<Class> validationGroups) {
        Set<ConstraintViolation<Entity>> violations;
        if (validationGroups == null || validationGroups.isEmpty()) {
            violations = validator.validate(entity);
        } else {
            violations = validator.validate(entity, validationGroups.toArray(new Class[0]));
        }
        if (!violations.isEmpty())
            throw new EntityValidationException(String.format("Entity %s validation failed.", entity.toString()), violations);
    }


    private static class Secure extends CubaDataManager {
        public Secure(Metadata metadata,
                      FetchPlanRepository fetchPlanRepository,
                      CubaProperties properties,
                      Validator validator,
                      AccessConstraintsRegistry accessConstraintsRegistry,
                      ApplicationContext applicationContext) {
            super(metadata, fetchPlanRepository, properties, validator, accessConstraintsRegistry, applicationContext);
        }

        @Override
        protected void initDelegate() {
            this.delegate = applicationContext.getBean(io.jmix.core.DataManager.class);
        }
    }

    private static class RowLevelConstraintsDataManager implements io.jmix.core.UnconstrainedDataManager {
        private final io.jmix.core.UnconstrainedDataManager delegate;
        private final AccessConstraintsRegistry accessConstraintsRegistry;

        private RowLevelConstraintsDataManager(io.jmix.core.UnconstrainedDataManager delegate,
                                               AccessConstraintsRegistry accessConstraintsRegistry) {
            this.delegate = delegate;
            this.accessConstraintsRegistry = accessConstraintsRegistry;
        }

        @Nullable
        @Override
        public <E> E load(io.jmix.core.LoadContext<E> context) {
            context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));
            return delegate.load(context);
        }

        @Override
        public <E> List<E> loadList(io.jmix.core.LoadContext<E> context) {
            context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));
            return delegate.loadList(context);
        }

        @Override
        public long getCount(io.jmix.core.LoadContext<?> context) {
            context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));
            return delegate.getCount(context);
        }

        @Override
        public io.jmix.core.EntitySet save(SaveContext context) {
            context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));
            return delegate.save(context);
        }

        @Override
        public io.jmix.core.EntitySet save(Object... entities) {
            return save(new SaveContext().saving(entities));
        }

        @Override
        public <E> E save(E entity) {
            return save(new SaveContext().saving(entity))
                    .optional(entity)
                    .orElseThrow(() -> new IllegalStateException("Data store didn't return a saved entity"));
        }

        @Override
        public void remove(Object... entities) {
            save(new SaveContext().removing(entities));
        }

        @Override
        public <E> void remove(io.jmix.core.Id<E> entityId) {
            remove(getReference(entityId));
        }

        @Override
        public List<KeyValueEntity> loadValues(io.jmix.core.ValueLoadContext context) {
            context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));
            return delegate.loadValues(context);
        }

        @Override
        public long getCount(io.jmix.core.ValueLoadContext context) {
            context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));
            return delegate.getCount(context);
        }

        @Override
        public <E> io.jmix.core.FluentLoader<E> load(Class<E> entityClass) {
            return delegate.load(entityClass);
        }

        @Override
        public <E> io.jmix.core.FluentLoader.ById<E> load(io.jmix.core.Id<E> entityId) {
            return delegate.load(entityId);
        }

        @Override
        public io.jmix.core.FluentValuesLoader loadValues(String queryString) {
            return delegate.loadValues(queryString);
        }

        @Override
        public <T> io.jmix.core.FluentValueLoader<T> loadValue(String queryString, Class<T> valueClass) {
            return delegate.loadValue(queryString, valueClass);
        }

        @Override
        public <T> T create(Class<T> entityClass) {
            return delegate.create(entityClass);
        }

        @Override
        public <T> T getReference(Class<T> entityClass, Object id) {
            return delegate.getReference(entityClass, id);
        }

        @Override
        public <T> T getReference(io.jmix.core.Id<T> entityId) {
            return delegate.getReference(entityId);
        }

        protected List<AccessConstraint<?>> mergeConstraints(List<AccessConstraint<?>> accessConstraints) {
            if (accessConstraints.isEmpty()) {
                return accessConstraintsRegistry.getConstraintsOfType(RowLevelConstraint.class);
            } else {
                Set<AccessConstraint<?>> newAccessConstraints =
                        new LinkedHashSet<>(accessConstraintsRegistry.getConstraintsOfType(RowLevelConstraint.class));
                newAccessConstraints.addAll(accessConstraints);
                return new ArrayList<>(newAccessConstraints);
            }
        }
    }
}

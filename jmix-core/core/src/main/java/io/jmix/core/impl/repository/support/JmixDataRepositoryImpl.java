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

package io.jmix.core.impl.repository.support;

import io.jmix.core.*;
import io.jmix.core.impl.repository.query.utils.LoaderHelper;
import io.jmix.core.impl.repository.support.method_metadata.CrudMethodMetadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.repository.JmixDataRepository;
import io.jmix.core.repository.JmixDataRepositoryContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.*;

import static io.jmix.core.impl.repository.query.utils.LoaderHelper.springToJmixSort;

/**
 * Implementation of base repository methods used by application repository beans.
 *
 * @param <T>
 * @param <ID>
 */
@NoRepositoryBean
public class JmixDataRepositoryImpl<T, ID> implements JmixDataRepository<T, ID>, CrudRepository<T, ID> {


    protected Metadata metadata;

    protected UnconstrainedDataManager unconstrainedDataManager;
    protected DataManager dataManager;

    protected CrudMethodMetadata.Accessor methodMetadataAccessor;


    private Class<T> domainClass;

    public JmixDataRepositoryImpl(Class<T> domainClass,
                                  DataManager dataManager,
                                  Metadata metadata,
                                  CrudMethodMetadata.Accessor methodMetadataAccessor) {
        this.domainClass = domainClass;
        this.unconstrainedDataManager = dataManager.unconstrained();
        this.dataManager = dataManager;
        this.metadata = metadata;
        this.methodMetadataAccessor = methodMetadataAccessor;
    }

    @Override
    public T create() {
        return getDataManager().create(domainClass);
    }


    @Override
    public Optional<T> findById(ID id, FetchPlan fetchPlan) {
        return idLoader(id).fetchPlan(fetchPlan).optional();
    }

    @Override
    public T getById(ID id, FetchPlan fetchPlan) {
        return idLoader(id).fetchPlan(fetchPlan).one();
    }

    @Override
    public T getById(ID id) {
        return idLoader(id).one();
    }

    @Override
    public Iterable<T> findAll(FetchPlan fetchPlan) {
        return allLoader().fetchPlan(fetchPlan).list();
    }

    @Override
    public Iterable<T> findAll(JmixDataRepositoryContext context) {
        FluentLoader.ByCondition<T> loader = conditionOrAllLoader(context.condition())
                .fetchPlan(context.fetchPlan())
                .hints(getHints())
                .hints(context.hints());
        return loader.list();
    }

    @Override
    public Iterable<T> findAll(Iterable<ID> ids, @Nullable FetchPlan fetchPlan) {
        if (!ids.iterator().hasNext()) {
            return Collections.emptyList();
        }

        return getDataManager().load(domainClass).ids(toCollection(ids)).hints(getHints()).fetchPlan(fetchPlan).list();
    }

    @Override
    public <S extends T> S save(S entity) {
        return getDataManager().save(entity);
    }


    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        List<S> savedEntities = new ArrayList<>();
        for (S entity : entities) {
            savedEntities.add(save(entity));
        }
        return savedEntities;
    }

    @Override
    public Optional<T> findById(ID id) {
        return idLoader(id).optional();
    }

    @Override
    public boolean existsById(ID id) {
        return idLoader(id).optional().isPresent();
    }

    @Override
    public Iterable<T> findAll() {
        return allLoader().list();
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        return findAll(ids, null);
    }


    @Override
    public long count() {
        return getDataManager().getCount(new LoadContext<>(metadata.getClass(domainClass)).setHints(getHints()));
    }

    @Override
    public void deleteById(ID id) {
        deleteInternal(getDataManager().getReference(Id.of(id, domainClass)));
    }

    @Override
    public void delete(T entity) {
        deleteInternal(entity);
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        deleteInternal(entities);
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        if (!ids.iterator().hasNext())
            return;

        List<ID> idList = new LinkedList<>();
        ids.forEach(idList::add);

        Iterable<T> entities = findAllById(idList);
        deleteInternal(entities);
    }

    @Override
    public void deleteAll() {
        Iterable<T> entities = allLoader().fetchPlan(FetchPlan.INSTANCE_NAME).list();
        deleteInternal(entities);
    }

    protected void deleteInternal(Object... entities) {
        getDataManager().save(new SaveContext().removing(entities).setHints(getHints()));
    }

    public Class<T> getDomainClass() {
        return domainClass;
    }

    public void setDomainClass(Class<T> domainClass) {
        this.domainClass = domainClass;
    }

    @Override
    public Iterable<T> findAll(Sort sort) {
        return findAll(sort, null);
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        return findAll(pageable, (FetchPlan) null);
    }

    @Override
    public Iterable<T> findAll(Sort sort, @Nullable FetchPlan fetchPlan) {
        return findAll(Pageable.unpaged(sort), fetchPlan);
    }

    @Override
    public Page<T> findAll(Pageable pageable, @Nullable FetchPlan fetchPlan) {
        return findAll(pageable, JmixDataRepositoryContext.plan(fetchPlan).build());
    }

    @Override
    public Page<T> findAll(Pageable pageable, JmixDataRepositoryContext jmixContext) {
        FluentLoader.ByCondition<T> loader = conditionOrAllLoader(jmixContext.condition())
                .fetchPlan(jmixContext.fetchPlan())
                .hints(getHints())
                .hints(jmixContext.hints());

        LoaderHelper.applyPageableForConditionLoader(loader, pageable);
        loader.sort(springToJmixSort(pageable.getSort()));

        List<T> results = loader.list();

        long total = count(jmixContext);
        return new PageImpl<>(results, pageable, total);
    }

    public long count(JmixDataRepositoryContext jmixContext) {
        MetaClass metaClass = metadata.getClass(domainClass);
        LoadContext<T> context = new LoadContext<>(metaClass);
        context.setQuery(new LoadContext.Query(String.format("select e from %s e", metaClass.getName())));
        if (jmixContext.condition() != null) {
            //noinspection DataFlowIssue
            context.getQuery().setCondition(jmixContext.condition());
        }

        Map<String, Serializable> hints = new HashMap<>(getHints());
        hints.putAll(jmixContext.hints());
        context.setHints(hints);
        return getDataManager().getCount(context);
    }

    @Override
    public <S extends T> S save(S entity, FetchPlan fetchPlan) {
        if (!fetchPlan.getEntityClass().isAssignableFrom(entity.getClass())) {
            throw new IllegalArgumentException(
                    String.format("FetchPlan '%s' cannot be used for entity with class '%s'",
                            fetchPlan,
                            entity.getClass()));
        }
        return getDataManager().save(new SaveContext().saving(entity, fetchPlan)).get(entity);
    }

    protected UnconstrainedDataManager getDataManager() {
        return methodMetadataAccessor.getCrudMethodMetadata().isApplyConstraints() ? dataManager : unconstrainedDataManager;
    }

    protected Map<String, Serializable> getHints() {
        return methodMetadataAccessor.getCrudMethodMetadata().getQueryHints();
    }

    protected FluentLoader.ByCondition<T> conditionOrAllLoader(@Nullable Condition condition) {
        return condition == null ? allLoader() : getDataManager().load(domainClass).condition(condition).hints(getHints());
    }

    protected FluentLoader.ByCondition<T> allLoader() {
        return getDataManager().load(domainClass).all().hints(getHints());
    }

    protected FluentLoader.ById<T> idLoader(ID id) {
        return getDataManager().load(domainClass).id(id).hints(getHints());
    }

    protected Collection<ID> toCollection(Iterable<ID> ids) {
        Collection<ID> collection;
        if (ids instanceof Collection) {
            collection = (Collection<ID>) ids;
        } else {
            collection = new ArrayList<>();
            ids.forEach(collection::add);
        }
        return collection;
    }

}

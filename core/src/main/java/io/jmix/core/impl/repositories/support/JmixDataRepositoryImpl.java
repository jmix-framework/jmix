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

package io.jmix.core.impl.repositories.support;

import io.jmix.core.*;
import io.jmix.core.impl.repositories.query.utils.LoaderHelper;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.repositories.JmixDataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

import static io.jmix.core.impl.repositories.query.utils.LoaderHelper.springToJmixSort;

@NoRepositoryBean
public class JmixDataRepositoryImpl<T, ID extends Serializable> implements JmixDataRepository<T, ID> {


    protected Metadata metadata;

    protected DataManager dataManager;


    private Class<T> domainClass;

    public JmixDataRepositoryImpl(Class<T> domainClass, DataManager dataManager, Metadata metadata) {
        this.domainClass = domainClass;
        this.dataManager = dataManager;
        this.metadata = metadata;
    }

    @Override
    public Optional<T> findOne(ID id, String fetchPlan) {
        return dataManager.load(domainClass).id(id).fetchPlan(fetchPlan).optional();
    }

    @Override
    public Iterable<T> findAll(String fetchPlan) {
        return dataManager.load(domainClass).all().fetchPlan(fetchPlan).list();
    }

    @Override
    public Iterable<T> findAll(Iterable<ID> ids, @Nullable String fetchPlan) {
        if (!ids.iterator().hasNext()) {
            return Collections.emptyList();
        }

        Collection<ID> collection;
        if (ids instanceof Collection) {
            collection = (Collection<ID>) ids;
        } else {
            collection = new ArrayList<>();
            ids.forEach(collection::add);
        }

        return dataManager.load(domainClass).ids(collection).fetchPlan(fetchPlan).list();
    }

    @Override
    public <S extends T> S save(S entity) {
        return dataManager.save(entity);
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
        return Optional.of(dataManager.load(domainClass).id(id).one());
    }

    @Override
    public boolean existsById(ID id) {
        return dataManager.load(domainClass).id(id).optional().isPresent();
    }

    @Override
    public Iterable<T> findAll() {
        return dataManager.load(domainClass).all().list();
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        return findAll(ids, null);
    }


    @Override
    public long count() {
        return dataManager.getCount(new LoadContext<>(metadata.getClass(domainClass)));
    }

    @Override
    public void deleteById(ID id) {
        dataManager.remove(Id.of(id, domainClass));
    }

    @Override
    public void delete(T entity) {
        dataManager.remove(entity);
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        entities.forEach(dataManager::remove);
    }


    @Override
    public void deleteAll() {
        Iterable<T> entities = dataManager.load(domainClass).all().fetchPlan(FetchPlan.INSTANCE_NAME).list();
        entities.forEach(dataManager::remove);
    }

    public Class<T> getDomainClass() {
        return domainClass;
    }

    public void setDomainClass(Class<T> domainClass) {
        this.domainClass = domainClass;
    }

    @Override
    public Iterable<T> findAll(Sort sort) {
        return dataManager.load(domainClass).all().sort(springToJmixSort(sort)).list();
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        FluentLoader.ByCondition<T> loader = dataManager.load(domainClass)
                .all();

        LoaderHelper.applyPageableForConditionLoader(loader, pageable);
        loader.sort(springToJmixSort(pageable.getSort()));

        List<T> results = loader.list();

        MetaClass metaClass = metadata.getClass(domainClass);
        LoadContext context = new LoadContext(metaClass)
                .setQuery(new LoadContext.Query(String.format("select e from %s e", metaClass.getName())));

        long total = dataManager.getCount(context);
        return new PageImpl<>(results, pageable, total);

    }
}

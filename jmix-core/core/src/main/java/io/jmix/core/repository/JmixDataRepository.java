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

package io.jmix.core.repository;

import io.jmix.core.FetchPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.Nullable;

import java.util.Optional;

/**
 * Central Jmix data repository interface.
 * <p>
 * Extends functionality of {@link PagingAndSortingRepository} by adding FetchPlan parameters to common methods and providing
 * other Jmix-specific methods
 *
 * @param <T>  the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 * @see PagingAndSortingRepository
 */
@NoRepositoryBean
@ApplyConstraints
public interface JmixDataRepository<T, ID> extends PagingAndSortingRepository<T, ID>, CrudRepository<T, ID> {

    /**
     * Instantiate an entity.
     * Invokes {@link io.jmix.core.Metadata#create(Class)}
     *
     * @return new instance of {@code <T>}.
     */
    T create();

    /**
     * Loads an entity by its {@code id} according to {@code fetchPlan}
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal Optional#empty()} if none found.
     */
    Optional<T> findById(ID id, FetchPlan fetchPlan);

    /**
     * Loads an entity by its {@code id} according to {@code fetchPlan}
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id
     * @throws io.jmix.core.NoResultException if nothing was loaded
     */
    T getById(ID id, FetchPlan fetchPlan);

    /**
     * Loads an entity by its {@code id}
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id
     * @throws io.jmix.core.NoResultException if nothing was loaded
     */
    T getById(ID id);

    /**
     * Returns all instances of the type {@code T} loaded according to {@code fetchPlan}
     *
     * @return all entities
     */
    Iterable<T> findAll(FetchPlan fetchPlan);


    /**
     * Returns all instances of the type {@code T} with the given IDs loaded according to {@code fetchPlan}
     * <p>
     * If some or all ids are not found, no entities are returned for these IDs.
     *
     * @param fetchPlan defines entity graph to load. {@link FetchPlan#BASE} will be used instead in case of null.
     * @param ids       must not be {@literal null} nor contain any {@literal null} values.
     * @return guaranteed to be not {@literal null}. The size can be equal or less than the number of given
     * {@literal ids}.
     * @throws IllegalArgumentException in case the given {@link Iterable ids} or one of its items is {@literal null}.
     */
    Iterable<T> findAll(Iterable<ID> ids, @Nullable FetchPlan fetchPlan);


    /**
     * Returns all entities sorted by the given options and loaded by specified FetchPlan.
     *
     * @param fetchPlan to load entity. {@link FetchPlan#BASE} will be used if {@code fetchPlan == null}
     * @return all entities sorted by the given options
     */
    Iterable<T> findAll(Sort sort, @Nullable FetchPlan fetchPlan);

    /**
     * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code Pageable} object.
     * Entities will be loaded according to passed {@code fetchPlan}
     *
     * @param fetchPlan to load entities. {@link FetchPlan#BASE} will be used if {@code fetchPlan == null}
     * @return a page of entities
     */
    Page<T> findAll(Pageable pageable, @Nullable FetchPlan fetchPlan);

    /**
     * Saves the {@code entity} and returns saved instance loaded with specified {@code fetchPlan}.
     * @param entity entity to save. Must not be null
     * @param fetchPlan {@link FetchPlan} to reload saved entity with. Must be applicable to {@code entity}
     * @throws IllegalArgumentException if {@code fetchPlan} is not applicable to entity
     */
    <S extends T> S save(S entity, FetchPlan fetchPlan);
}

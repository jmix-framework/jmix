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
package com.haulmont.cuba.core;

import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import com.haulmont.cuba.core.global.QueryHints;

import javax.annotation.Nullable;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;

/**
 * Interface used to control query execution.
 *
 * <br>Consider use of {@link TypedQuery} instead of this interface.
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link javax.persistence.Query}.
 */
@Deprecated
public interface Query {

    String NAME = "cuba_Query";

    /**
     * Get the query string.
     *
     * @return query string
     */
    String getQueryString();

    /**
     * Set the query string.
     *
     * @param queryString query string
     */
    Query setQueryString(String queryString);

    /**
     * Execute a SELECT query and return the query results as a List.
     *
     * @return a list of the results
     * @throws IllegalStateException if called for a Java Persistence query language UPDATE or DELETE statement
     */
    List getResultList();

    /**
     * Execute a SELECT query that returns a single result.
     *
     * @return the result
     * @throws javax.persistence.NoResultException        if there is no result
     * @throws javax.persistence.NonUniqueResultException if more than one result
     * @throws IllegalStateException                      if called for a Java Persistence query language UPDATE or DELETE statement
     */
    Object getSingleResult();

    /**
     * Execute a SELECT query.<br>
     * Returns null if there is no result.<br>
     * Returns first result if more than one result.
     *
     * @return the result
     * @throws IllegalStateException if called for a Java Persistence query language UPDATE or DELETE statement
     */
    @Nullable
    Object getFirstResult();

    /**
     * Execute an update or delete statement.
     *
     * @return the number of entities updated or deleted
     * @throws IllegalStateException                          if called for a Java Persistence query language SELECT statement
     * @throws javax.persistence.TransactionRequiredException if there is no transaction
     */
    int executeUpdate();

    /**
     * Set the maximum number of results to retrieve.
     *
     * @return the same query instance
     * @throws IllegalArgumentException if argument is negative
     */
    Query setMaxResults(int maxResult);

    /**
     * Set the position of the first result to retrieve.
     *
     * @param startPosition position of the first result, numbered from 0
     * @return the same query instance
     * @throws IllegalArgumentException if argument is negative
     */
    Query setFirstResult(int startPosition);

    /**
     * Bind an argument to a named parameter.<br>
     * <br>
     * In the query text, named parameters are marked with colon (e.g. {@code :foo}) in JPQL queries or with
     * number sign in native SQL queries (e.g. {@code #foo}).
     *
     * @param name  parameter name
     * @param value parameter value. Entity instance replaced with its ID.
     * @return the same query instance
     * @throws IllegalArgumentException if parameter name does not correspond to parameter in query string
     *                                  or argument is of incorrect type
     */
    Query setParameter(String name, Object value);

    /**
     * Bind an argument to a named parameter.<br>
     * <br>
     * In the query text, named parameters are marked with colon (e.g. {@code :foo}) in JPQL queries or with
     * number sign in native SQL queries (e.g. {@code #foo}).
     *
     * @deprecated implicit conversions are deprecated, do not use this feature
     * @param name                parameter name
     * @param value               parameter value
     * @param implicitConversions whether to make parameter value conversions, e.g. convert an entity to its ID
     * @return the same query instance
     * @throws IllegalArgumentException if parameter name does not correspond to parameter in query string
     *                                  or argument is of incorrect type
     */
    @Deprecated
    Query setParameter(String name, Object value, boolean implicitConversions);

    /**
     * Bind an instance of java.util.Date to a named parameter.<br>
     * <p>
     * In the query text, named parameters are marked with colon (e.g. {@code :foo}) in JPQL queries or with
     * number sign in native SQL queries (e.g. {@code #foo}).
     *
     * @param name         parameter name
     * @param value        parameter value
     * @param temporalType type of Date value
     * @return the same query instance
     * @throws IllegalArgumentException if parameter name does not correspond to parameter in query string
     */
    Query setParameter(String name, Date value, TemporalType temporalType);

    /**
     * Bind an argument to a positional parameter.
     * <p>
     * In the query text, positional parameters are marked with ?N (e.g. {@code ?1}).
     *
     * @param position parameter position, starting with 1
     * @param value    parameter value. Entity instance replaced with its ID.
     * @return the same query instance
     * @throws IllegalArgumentException if position does not correspond to positional parameter of query
     *                                  or argument is of incorrect type
     */
    Query setParameter(int position, Object value);

    /**
     * Bind an argument to a positional parameter.
     * <p>
     * In the query text, positional parameters are marked with ?N (e.g. {@code ?1}).
     *
     * @deprecated implicit conversions are deprecated, do not use this feature
     * @param position            parameter position, starting with 1
     * @param value               parameter value
     * @param implicitConversions whether to make parameter value conversions, e.g. convert an entity to its ID
     * @return the same query instance
     * @throws IllegalArgumentException if position does not correspond to positional parameter of query
     *                                  or argument is of incorrect type
     */
    @Deprecated
    Query setParameter(int position, Object value, boolean implicitConversions);

    /**
     * Bind an instance of java.util.Date to a positional parameter.
     * <br>
     * In the query text, positional parameters are marked with ?N (e.g. {@code ?1}).
     *
     * @param position     parameter position, starting with 1
     * @param value        parameter value
     * @param temporalType type of Date value
     * @return the same query instance
     * @throws IllegalArgumentException if position does not correspond to positional parameter of query
     */
    Query setParameter(int position, Date value, TemporalType temporalType);

    /**
     * Set the lock mode type to be used for the query execution.
     *
     * @param lockMode lock mode
     * @return the same query instance
     */
    Query setLockMode(LockModeType lockMode);

    /**
     * Set View for this Query instance.
     * <br> All non-lazy view properties contained in a combination of all added views are eagerly fetched.
     *
     * @param view view instance. If null, eager fetching is performed according to JPA mappings.
     * @return the same query instance
     */
    Query setView(@Nullable FetchPlan view);

    /**
     * Set View for this Query instance.
     * <br> All non-lazy view properties contained in a combination of all added views are eagerly fetched.
     *
     * @param entityClass entity class to get a view instance by the name provided
     * @param viewName    view name
     * @return the same query instance
     */
    Query setView(Class<? extends Entity> entityClass, String viewName);

    /**
     * Adds View for this Query instance.
     * <br> All non-lazy view properties contained in a combination of all added views are eagerly fetched.
     *
     * @param view view instance - must not be null
     * @return the same query instance
     */
    Query addView(FetchPlan view);

    /**
     * Adds View for this Query instance.
     * <br> All non-lazy view properties contained in a combination of all added views are eagerly fetched.
     *
     * @param entityClass entity class to get a view instance by the name provided
     * @param viewName    view name - must not be null
     * @return the same query instance
     */
    Query addView(Class<? extends Entity> entityClass, String viewName);

    /**
     * Indicates that the query results should be cached.
     *
     * @return the same query instance
     */
    Query setCacheable(boolean cacheable);

    /**
     * Set the flush mode type to be used for the query execution.
     *
     * @param flushMode flush mode
     * @return the same query instance
     */
    Query setFlushMode(FlushModeType flushMode);

    /**
     * Set a query property or hint.
     * @see QueryHints
     *
     * @param hintName  name of property or hint
     * @param value  value of the property or hint
     * @return the same query instance
     */
    Query setHint(String hintName, Object value);

    /**
     * @return underlying implementation provided by ORM
     */
    javax.persistence.Query getDelegate();
}

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
package com.haulmont.cuba.core.sys;

import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.TypedQuery;
import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanRepository;
import io.jmix.data.JmixQuery;
import io.jmix.data.PersistenceHints;
import io.jmix.eclipselink.impl.JmixEclipseLinkQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;

/**
 * Implementation of {@link TypedQuery} interface based on EclipseLink.
 */
@SuppressWarnings("unchecked")
@Component(Query.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class QueryImpl<T> implements TypedQuery<T> {

    private final Logger log = LoggerFactory.getLogger(QueryImpl.class);

    @Autowired
    private FetchPlanRepository fetchPlanRepository;

    private Class resultClass;

    private JmixQuery delegate;

    public QueryImpl(JmixQuery delegate, Class resultClass) {
        this.resultClass = resultClass;
        this.delegate = delegate;
    }

    public QueryImpl(JmixQuery delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<T> getResultList() {
        return delegate.getResultList();
    }

    @Override
    public T getSingleResult() {
        return (T) delegate.getSingleResult();
    }

    @Override
    @Nullable
    public T getFirstResult() {
        return (T) ((JmixEclipseLinkQuery) delegate).getSingleResultOrNull();
    }

    @Override
    public int executeUpdate() {
        return delegate.executeUpdate();
    }

    @Override
    public TypedQuery<T> setMaxResults(int maxResults) {
        delegate.setMaxResults(maxResults);
        return this;
    }

    @Override
    public TypedQuery<T> setFirstResult(int firstResult) {
        delegate.setFirstResult(firstResult);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(String name, Object value) {
        delegate.setParameter(name, value);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(String name, Object value, boolean implicitConversions) {
        return setParameter(name, value);
    }

    @Override
    public TypedQuery<T> setParameter(String name, Date value, TemporalType temporalType) {
        delegate.setParameter(name, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(int position, Object value) {
        delegate.setParameter(position, value);
        return this;
    }

    @Override
    public TypedQuery<T> setParameter(int position, Object value, boolean implicitConversions) {
        return setParameter(position, value);
    }

    @Override
    public TypedQuery<T> setParameter(int position, Date value, TemporalType temporalType) {
        delegate.setParameter(position, value, temporalType);
        return this;
    }

    @Override
    public TypedQuery<T> setLockMode(LockModeType lockMode) {
        delegate.setLockMode(lockMode);
        return this;
    }

    @Override
    public TypedQuery<T> setView(FetchPlan fetchPlan) {
        delegate.setHint(PersistenceHints.FETCH_PLAN, null);
        delegate.setHint(PersistenceHints.FETCH_PLAN, fetchPlan);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypedQuery<T> setViewName(String fetchPlanName) {
        if (resultClass == null)
            throw new IllegalStateException("resultClass is null");

        setView(fetchPlanRepository.getFetchPlan(resultClass, fetchPlanName));
        return this;
    }

    @Override
    public TypedQuery<T> setView(Class<? extends Entity> entityClass, String viewName) {
        setView(fetchPlanRepository.getFetchPlan(entityClass, viewName));
        return this;
    }

    @Override
    public TypedQuery<T> addView(FetchPlan fetchPlan) {
        delegate.setHint(PersistenceHints.FETCH_PLAN, fetchPlan);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypedQuery<T> addViewName(String fetchPlanName) {
        if (resultClass == null)
            throw new IllegalStateException("resultClass is null");

        addView(fetchPlanRepository.getFetchPlan(resultClass, fetchPlanName));
        return this;
    }

    @Override
    public TypedQuery<T> addView(Class<? extends Entity> entityClass, String fetchPlanName) {
        addView(fetchPlanRepository.getFetchPlan(entityClass, fetchPlanName));
        return this;
    }

    @Override
    public javax.persistence.Query getDelegate() {
        return delegate;
    }

    @Override
    public String getQueryString() {
        return delegate.getQueryString();
    }

    @Override
    public TypedQuery<T> setQueryString(String queryString) {
        delegate.setQueryString(queryString);
        return this;
    }

    @Override
    public TypedQuery<T> setCacheable(boolean cacheable) {
        delegate.setHint(PersistenceHints.CACHEABLE, cacheable);
        return this;
    }

    @Override
    public TypedQuery<T> setFlushMode(FlushModeType flushMode) {
        delegate.setFlushMode(flushMode);
        return this;
    }

    @Override
    public TypedQuery<T> setHint(String hintName, Object value) {
        delegate.setHint(hintName, value);
        return this;
    }

    public void setSingleResultExpected(boolean singleResultExpected) {
        ((JmixEclipseLinkQuery) delegate).setSingleResultExpected(singleResultExpected);
    }
}

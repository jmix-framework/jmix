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

package io.jmix.hibernate.impl;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.query.ParameterMetadata;
import org.hibernate.query.QueryParameter;
import org.hibernate.query.internal.QueryParameterBindingsImpl;
import org.hibernate.query.spi.QueryParameterBinding;
import org.hibernate.query.spi.QueryParameterBindings;
import org.hibernate.query.spi.QueryParameterListBinding;
import org.hibernate.type.Type;

import java.util.Map;

public abstract class DelegateQueryParameterBindingsImpl implements QueryParameterBindings {

    protected QueryParameterBindingsImpl delegate;

    public DelegateQueryParameterBindingsImpl(ParameterMetadata parameterMetadata,
                                              SessionFactoryImplementor sessionFactory,
                                              boolean queryParametersValidationEnabled) {
        this.delegate = QueryParameterBindingsImpl.from(parameterMetadata, sessionFactory, queryParametersValidationEnabled);
    }

    @Override
    public boolean isBound(QueryParameter parameter) {
        return delegate.isBound(parameter);
    }

    @Override
    public <T> QueryParameterBinding<T> getBinding(QueryParameter<T> parameter) {
        return delegate.getBinding(parameter);
    }

    @Override
    public <T> QueryParameterBinding<T> getBinding(String name) {
        return delegate.getBinding(name);
    }

    @Override
    public <T> QueryParameterBinding<T> getBinding(int position) {
        return delegate.getBinding(position);
    }

    @Override
    public void verifyParametersBound(boolean callable) {
        delegate.verifyParametersBound(callable);
    }

    @Override
    public String expandListValuedParameters(String queryString, SharedSessionContractImplementor producer) {
        return delegate.expandListValuedParameters(queryString, producer);
    }

    @Override
    public <T> QueryParameterListBinding<T> getQueryParameterListBinding(QueryParameter<T> parameter) {
        return delegate.getQueryParameterListBinding(parameter);
    }

    @Override
    public <T> QueryParameterListBinding<T> getQueryParameterListBinding(String name) {
        return delegate.getQueryParameterListBinding(name);
    }

    @Override
    public <T> QueryParameterListBinding<T> getQueryParameterListBinding(int position) {
        return delegate.getQueryParameterListBinding(position);
    }

    @Override
    public Type[] collectPositionalBindTypes() {
        return delegate.collectPositionalBindTypes();
    }

    @Override
    public Object[] collectPositionalBindValues() {
        return delegate.collectPositionalBindValues();
    }

    @Override
    public Map<String, TypedValue> collectNamedParameterBindings() {
        return delegate.collectNamedParameterBindings();
    }
}

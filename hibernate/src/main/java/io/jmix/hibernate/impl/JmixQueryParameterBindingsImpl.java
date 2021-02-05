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

import org.hibernate.QueryParameterException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.ParameterMetadata;
import org.hibernate.query.QueryParameter;
import org.hibernate.query.spi.QueryParameterBinding;
import org.hibernate.query.spi.QueryParameterListBinding;
import org.springframework.beans.factory.BeanFactory;

public class JmixQueryParameterBindingsImpl extends DelegateQueryParameterBindingsImpl {

    protected BeanFactory beanFactory;
    protected boolean isNative;

    private JmixQueryParameterBindingsImpl(BeanFactory beanFactory, ParameterMetadata parameterMetadata, SessionFactoryImplementor sessionFactory, boolean queryParametersValidationEnabled, boolean isNative) {
        super(parameterMetadata, sessionFactory, queryParametersValidationEnabled);
        this.beanFactory = beanFactory;
        this.isNative = isNative;
    }

    public static JmixQueryParameterBindingsImpl from(
            BeanFactory beanFactory,
            ParameterMetadata parameterMetadata,
            SessionFactoryImplementor sessionFactory,
            boolean queryParametersValidationEnabled,
            boolean isNative) {
        if (parameterMetadata == null) {
            throw new QueryParameterException("Query parameter metadata cannot be null");
        }

        return new JmixQueryParameterBindingsImpl(beanFactory, parameterMetadata, sessionFactory, queryParametersValidationEnabled, isNative);
    }

    public Object getBindValue(String paramName) {
        QueryParameterBinding<?> binding = getBinding(paramName);

        if (binding != null && binding.getBindValue() != null) {
            return binding.getBindValue();
        }
        QueryParameterListBinding<?> listBinding = getQueryParameterListBinding(paramName);
        if (listBinding != null && listBinding.getBindValues() != null) {
            return listBinding.getBindValues();
        }

        return null;
    }

    @Override
    public <T> QueryParameterBinding<T> getBinding(QueryParameter<T> parameter) {
        return new JmixQueryParameterBindingDelegate(super.getBinding(parameter), isNative, beanFactory);
    }

    @Override
    public <T> QueryParameterBinding<T> getBinding(String name) {
        return new JmixQueryParameterBindingDelegate(super.getBinding(name), isNative, beanFactory);
    }

    @Override
    public <T> QueryParameterBinding<T> getBinding(int position) {
        return new JmixQueryParameterBindingDelegate(super.getBinding(position), isNative, beanFactory);
    }
}

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

package com.haulmont.cuba.core.global;

import io.jmix.core.ValueLoadContext;
import io.jmix.data.PersistenceHints;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Primary
@Component("cuba_FluentValueLoader")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FluentValueLoader<T> extends io.jmix.core.FluentValueLoader<T> {

    public FluentValueLoader(String queryString, Class valueClass) {
        super(queryString, valueClass);
    }

    @Override
    protected ValueLoadContext instantiateValueLoadContext() {
        return new com.haulmont.cuba.core.global.ValueLoadContext();
    }

    @Override
    public FluentValueLoader<T> store(String store) {
        super.store(store);
        return this;
    }

    @Override
    public FluentValueLoader<T> hint(String hintName, Serializable value) {
        super.hint(hintName, value);
        return this;
    }

    @Override
    public FluentValueLoader<T> hints(Map<String, Serializable> hints) {
        super.hints(hints);
        return this;
    }

    public FluentValueLoader<T> softDeletion(boolean softDeletion) {
        super.hint(PersistenceHints.SOFT_DELETION, softDeletion);
        return this;
    }

    @Override
    public FluentValueLoader<T> parameter(String name, Object value) {
        super.parameter(name, value);
        return this;
    }

    @Override
    public FluentValueLoader<T> parameter(String name, Date value, TemporalType temporalType) {
        super.parameter(name, value, temporalType);
        return this;
    }

    @Override
    public FluentValueLoader<T> parameter(String name, Object value, boolean implicitConversion) {
        super.parameter(name, value, implicitConversion);
        return this;
    }

    @Override
    public FluentValueLoader<T> setParameters(Map<String, Object> parameters) {
        super.setParameters(parameters);
        return this;
    }

    @Override
    public FluentValueLoader<T> firstResult(int firstResult) {
        super.firstResult(firstResult);
        return this;
    }

    @Override
    public FluentValueLoader<T> maxResults(int maxResults) {
        super.maxResults(maxResults);
        return this;
    }
}


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


import io.jmix.data.PersistenceHints;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Primary
@Component("cuba_FluentValuesLoader")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FluentValuesLoader extends io.jmix.core.FluentValuesLoader {

    public FluentValuesLoader(String queryString) {
        super(queryString);
    }

    @Override
    protected io.jmix.core.ValueLoadContext instantiateValueLoadContext() {
        return new com.haulmont.cuba.core.global.ValueLoadContext();
    }

    @Override
    public FluentValuesLoader property(String name) {
        super.property(name);
        return this;
    }

    @Override
    public FluentValuesLoader properties(List<String> properties) {
        super.properties(properties);
        return this;
    }

    @Override
    public FluentValuesLoader properties(String... properties) {
        super.properties(properties);
        return this;
    }

    @Override
    public FluentValuesLoader store(String store) {
        super.store(store);
        return this;
    }

    @Override
    public FluentValuesLoader hint(String hintName, Serializable value) {
        super.hint(hintName, value);
        return this;
    }

    @Override
    public FluentValuesLoader hints(Map<String, Serializable> hints) {
        super.hints(hints);
        return this;
    }

    public FluentValuesLoader softDeletion(boolean softDeletion) {
        super.hint(PersistenceHints.SOFT_DELETION, softDeletion);
        return this;
    }

    @Override
    public FluentValuesLoader parameter(String name, Object value) {
        super.parameter(name, value);
        return this;
    }

    @Override
    public FluentValuesLoader parameter(String name, Date value, TemporalType temporalType) {
        super.parameter(name, value, temporalType);
        return this;
    }

    @Override
    public FluentValuesLoader parameter(String name, Object value, boolean implicitConversion) {
        super.parameter(name, value, implicitConversion);
        return this;
    }

    @Override
    public FluentValuesLoader setParameters(Map<String, Object> parameters) {
        super.setParameters(parameters);
        return this;
    }

    @Override
    public FluentValuesLoader firstResult(int firstResult) {
        super.firstResult(firstResult);
        return this;
    }

    @Override
    public FluentValuesLoader maxResults(int maxResults) {
        super.maxResults(maxResults);
        return this;
    }
}

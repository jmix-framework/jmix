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

import com.haulmont.cuba.core.global.ViewNotFoundException;
import com.haulmont.cuba.core.global.ViewRepository;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanNotFoundException;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.Entity;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collection;

@Component(ViewRepository.NAME)
public class ViewRepositoryImpl implements ViewRepository {
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    @Override
    public FetchPlan getView(Class<? extends Entity> entityClass, String name) {
        try {
            return fetchPlanRepository.getFetchPlan(entityClass, name);
        } catch (FetchPlanNotFoundException e) {
            throw new ViewNotFoundException(e.getMessage());
        }
    }

    @Override
    public FetchPlan getView(MetaClass metaClass, String name) {
        try {
            return fetchPlanRepository.getFetchPlan(metaClass, name);
        } catch (FetchPlanNotFoundException e) {
            throw new ViewNotFoundException(e.getMessage());
        }
    }

    @Nullable
    @Override
    public FetchPlan findView(MetaClass metaClass, String name) {
        return fetchPlanRepository.findFetchPlan(metaClass, name);
    }

    @Override
    public Collection<String> getViewNames(MetaClass metaClass) {
        return fetchPlanRepository.getFetchPlanNames(metaClass);
    }

    @Override
    public Collection<String> getViewNames(Class<? extends Entity> entityClass) {
        return fetchPlanRepository.getFetchPlanNames(entityClass);
    }
}

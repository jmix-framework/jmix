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

package com.haulmont.cuba.core.global.impl;

import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.global.ViewBuilder;
import io.jmix.core.DevelopmentException;
import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import io.jmix.core.impl.FetchPlanLoader;
import io.jmix.core.impl.FetchPlanRepositoryImpl;
import io.jmix.core.metamodel.model.MetaClass;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * A {@link io.jmix.core.FetchPlanRepository} implementation that supports legacy {@code _minimal} view.
 */
public class CubaFetchPlanRepository extends FetchPlanRepositoryImpl {

    @Override
    protected FetchPlan deployDefaultFetchPlan(MetaClass metaClass, String name, Set<FetchPlanLoader.FetchPlanInfo> visited) {
        if (View.MINIMAL.equals(name)) {
            Class<? extends Entity> javaClass = metaClass.getJavaClass();

            FetchPlanLoader.FetchPlanInfo info = new FetchPlanLoader.FetchPlanInfo(metaClass, name);
            if (visited.contains(info)) {
                throw new DevelopmentException(String.format("Fetch plans cannot have cyclic references. FetchPlan %s for class %s",
                        name, metaClass.getName()));
            }

            ViewBuilder viewBuilder = (ViewBuilder) fetchPlans.builder(javaClass).name(name);
            addAttributesToInstanceNameFetchPlan(metaClass, viewBuilder, info, visited);

            storeFetchPlan(metaClass, viewBuilder.build());

            return viewBuilder.build();

        }
        return super.deployDefaultFetchPlan(metaClass, name, visited);
    }

    @Override
    protected boolean isDefaultFetchPlan(String fetchPlanName) {
        return super.isDefaultFetchPlan(fetchPlanName) || View.MINIMAL.equals(fetchPlanName);
    }

    @Nullable
    @Override
    public FetchPlan findFetchPlan(MetaClass metaClass, @Nullable String name) {
        FetchPlan fetchPlan = super.findFetchPlan(metaClass, name);

        return fetchPlan != null ? fetchPlans.builder(fetchPlan).name(fetchPlan.getName()).build() : null;
    }

}

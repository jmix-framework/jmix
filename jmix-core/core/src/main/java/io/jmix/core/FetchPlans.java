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

package io.jmix.core;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory for {@link FetchPlanBuilder}.
 */
@Component("core_FetchPlans")
public class FetchPlans {

    @Autowired
    protected ObjectProvider<FetchPlanBuilder> fetchPlanBuilderProvider;

    /**
     * Returns {@link FetchPlan} builder for the given entity class.
     */
    public FetchPlanBuilder builder(Class<?> entityClass) {
        return fetchPlanBuilderProvider.getObject(entityClass);
    }

    /**
     * Returns {@link FetchPlan} builder that contains all properties from {@code fetchPlan}
     */
    public FetchPlanBuilder builder(FetchPlan fetchPlan) {
        return builder(fetchPlan.getEntityClass()).addFetchPlan(fetchPlan);
    }
}

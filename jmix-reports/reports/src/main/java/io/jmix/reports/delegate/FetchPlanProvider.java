/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reports.delegate;

import io.jmix.core.FetchPlan;

/**
 * Interface implemented by client to dynamically construct fetch plan for an entity Data Set.
 *
 * @see io.jmix.reports.entity.DataSetType#SINGLE
 * @see io.jmix.reports.entity.DataSetType#MULTI
 */
@FunctionalInterface
public interface FetchPlanProvider {

    /**
     * Construct or obtain a fetch plan
     * that will be used for reloading entity parameter in entity-typed data sets.
     */
    FetchPlan getFetchPlan();
}

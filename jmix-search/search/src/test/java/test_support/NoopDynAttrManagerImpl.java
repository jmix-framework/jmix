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

package test_support;

import io.jmix.core.FetchPlan;
import io.jmix.core.constraint.AccessConstraint;
import io.jmix.dynattr.DynAttrManager;

import java.util.Collection;

public class NoopDynAttrManagerImpl implements DynAttrManager {
    @Override
    public void loadValues(Collection<Object> entities, FetchPlan fetchPlan, Collection<AccessConstraint<?>> accessConstraints) {

    }

    @Override
    public void storeValues(Collection<Object> entities, Collection<AccessConstraint<?>> accessConstraints) {

    }

    @Override
    public void addDynamicAttributesState(Collection<Object> entities, FetchPlan fetchPlan) {

    }
}

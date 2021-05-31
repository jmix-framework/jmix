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

package io.jmix.core.impl;

import io.jmix.core.AccessConstraintsRegistry;
import io.jmix.core.DataManager;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.constraint.AccessConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("core_DataManager")
public class DataManagerImpl extends UnconstrainedDataManagerImpl implements DataManager {
    @Autowired
    protected AccessConstraintsRegistry accessConstraintsRegistry;
    @Autowired
    protected UnconstrainedDataManager unconstrainedDataManager;

    @Override
    public UnconstrainedDataManager unconstrained() {
        return unconstrainedDataManager;
    }

    @Override
    protected List<AccessConstraint<?>> getAppliedConstraints() {
        return accessConstraintsRegistry.getConstraints();
    }
}

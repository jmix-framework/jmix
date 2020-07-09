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

package io.jmix.data.impl;

import io.jmix.core.FetchPlan;
import io.jmix.core.JmixEntity;
import io.jmix.core.security.OnCoreSecurityImplementation;
import io.jmix.data.PersistenceAttributeSecurity;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component(PersistenceAttributeSecurity.NAME)
@Conditional(OnCoreSecurityImplementation.class)
public class CorePersistenceAttributeSecurity implements PersistenceAttributeSecurity {

    @Override
    public FetchPlan createRestrictedFetchPlan(FetchPlan fetchPlan) {
        return fetchPlan;
    }

    @Override
    public void afterLoad(JmixEntity entity) {

    }

    @Override
    public void afterLoad(Collection<? extends JmixEntity> entities) {

    }

    @Override
    public void beforePersist(JmixEntity entity) {

    }

    @Override
    public void beforeMerge(JmixEntity entity) {

    }

    @Override
    public void afterCommit(JmixEntity entity) {

    }
}

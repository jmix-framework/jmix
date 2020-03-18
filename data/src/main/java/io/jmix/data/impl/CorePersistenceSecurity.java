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
import io.jmix.core.Entity;
import io.jmix.core.security.OnCoreSecurityImplementation;
import io.jmix.data.PersistenceSecurity;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component(PersistenceSecurity.NAME)
@Conditional(OnCoreSecurityImplementation.class)
public class CorePersistenceSecurity implements PersistenceSecurity {

    @Override
    public boolean applyConstraints(JmixQuery query) {
        return false;
    }

    @Override
    public void setQueryParam(JmixQuery query, String paramName) {

    }

    @Override
    public void applyConstraints(Entity entity) {

    }

    @Override
    public void applyConstraints(Collection<Entity> entities) {

    }

    @Override
    public boolean filterByConstraints(Collection<Entity> entities) {
        return false;
    }

    @Override
    public boolean filterByConstraints(Entity entity) {
        return false;
    }

    @Override
    public void restoreSecurityState(Entity entity) {

    }

    @Override
    public void restoreFilteredData(Entity entity) {

    }

    @Override
    public void assertToken(Entity entity) {

    }

    @Override
    public void assertTokenForREST(Entity entity, FetchPlan view) {

    }

    @Override
    public void calculateFilteredData(Entity entity) {

    }

    @Override
    public void calculateFilteredData(Collection<Entity> entities) {

    }
}

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

package io.jmix.data.impl.context;

import io.jmix.core.JmixEntity;
import io.jmix.core.context.AccessContext;
import io.jmix.core.metamodel.model.MetaClass;

import java.util.function.Predicate;

public class InMemoryCrudEntityContext implements AccessContext {
    protected final MetaClass entityClass;

    protected Predicate<JmixEntity> createPredicate;
    protected Predicate<JmixEntity> readPredicate;
    protected Predicate<JmixEntity> updatePredicate;
    protected Predicate<JmixEntity> deletePredicate;

    public InMemoryCrudEntityContext(MetaClass entityClass) {
        this.entityClass = entityClass;
    }

    public MetaClass getEntityClass() {
        return entityClass;
    }

    public boolean isCreatePermitted(JmixEntity entity) {
        return createPredicate == null || createPredicate.test(entity);
    }

    public Predicate<JmixEntity> createPredicate() {
        return createPredicate;
    }

    public void addCreatePredicate(Predicate<JmixEntity> predicate) {
        if (this.createPredicate == null) {
            this.createPredicate = predicate;
        } else {
            this.createPredicate = this.createPredicate.and(predicate);
        }
    }

    public boolean isReadPermitted(JmixEntity entity) {
        return readPredicate == null || readPredicate.test(entity);
    }

    public Predicate<JmixEntity> readPredicate() {
        return readPredicate;
    }

    public void addReadPredicate(Predicate<JmixEntity> predicate) {
        if (this.readPredicate == null) {
            this.readPredicate = predicate;
        } else {
            this.readPredicate = this.readPredicate.and(predicate);
        }
    }

    public boolean isUpdatePermitted(JmixEntity entity) {
        return updatePredicate == null || updatePredicate.test(entity);
    }

    public Predicate<JmixEntity> updatePredicate() {
        return updatePredicate;
    }

    public void addUpdatePredicate(Predicate<JmixEntity> predicate) {

    }

    public boolean isDeletePermitted(JmixEntity entity) {
        return deletePredicate == null || deletePredicate.test(entity);
    }

    public Predicate<JmixEntity> deletePredicate() {
        return deletePredicate;
    }

    public void addDeletePredicate(Predicate<JmixEntity> predicate) {
    }
}

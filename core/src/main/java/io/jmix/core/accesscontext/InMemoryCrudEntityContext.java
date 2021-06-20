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

package io.jmix.core.accesscontext;

import io.jmix.core.metamodel.model.MetaClass;

import java.util.function.Predicate;

/**
 * An access context to check permissions on CRUD operations by testing predicates.
 */
public class InMemoryCrudEntityContext implements AccessContext {
    protected final MetaClass entityClass;

    protected Predicate createPredicate;
    protected Predicate readPredicate;
    protected Predicate updatePredicate;
    protected Predicate deletePredicate;

    public InMemoryCrudEntityContext(MetaClass entityClass) {
        this.entityClass = entityClass;
    }

    public MetaClass getEntityClass() {
        return entityClass;
    }

    public boolean isCreatePermitted(Object entity) {
        return createPredicate == null || createPredicate.test(entity);
    }

    public Predicate createPredicate() {
        return createPredicate;
    }

    public void addCreatePredicate(Predicate predicate) {
        if (this.createPredicate == null) {
            this.createPredicate = predicate;
        } else {
            this.createPredicate = this.createPredicate.and(predicate);
        }
    }

    public boolean isReadPermitted(Object entity) {
        return readPredicate == null || readPredicate.test(entity);
    }

    public Predicate readPredicate() {
        return readPredicate;
    }

    public void addReadPredicate(Predicate predicate) {
        if (this.readPredicate == null) {
            this.readPredicate = predicate;
        } else {
            this.readPredicate = this.readPredicate.and(predicate);
        }
    }

    public boolean isUpdatePermitted(Object entity) {
        return updatePredicate == null || updatePredicate.test(entity);
    }

    public Predicate updatePredicate() {
        return updatePredicate;
    }

    public void addUpdatePredicate(Predicate predicate) {
        if (this.updatePredicate == null) {
            this.updatePredicate = predicate;
        } else {
            this.updatePredicate = this.updatePredicate.and(predicate);
        }
    }

    public boolean isDeletePermitted(Object entity) {
        return deletePredicate == null || deletePredicate.test(entity);
    }

    public Predicate deletePredicate() {
        return deletePredicate;
    }

    public void addDeletePredicate(Predicate predicate) {
        if (this.deletePredicate == null) {
            this.deletePredicate = predicate;
        } else {
            this.deletePredicate = this.deletePredicate.and(predicate);
        }
    }
}

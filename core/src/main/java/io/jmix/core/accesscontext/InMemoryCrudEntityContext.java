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
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * An access context to check permissions on CRUD operations by testing predicates.
 */
public class InMemoryCrudEntityContext implements AccessContext {
    protected final MetaClass entityClass;

    protected BiPredicate createPredicate;
    protected BiPredicate readPredicate;
    protected BiPredicate updatePredicate;
    protected BiPredicate deletePredicate;
    
    protected ApplicationContext applicationContext;

    public InMemoryCrudEntityContext(MetaClass entityClass, ApplicationContext applicationContext) {
        this.entityClass = entityClass;
        this.applicationContext = applicationContext;

    }

    public MetaClass getEntityClass() {
        return entityClass;
    }

    public boolean isCreatePermitted(Object entity) {
        return createPredicate == null || createPredicate.test(entity, applicationContext);
    }

    public BiPredicate createPredicate() {
        return createPredicate;
    }

    public void addCreatePredicate(BiPredicate predicate) {
        if (this.createPredicate == null) {
            this.createPredicate = predicate;
        } else {
            this.createPredicate = this.createPredicate.and(predicate);
        }
    }

    public boolean isReadPermitted(Object entity) {
        return readPredicate == null || readPredicate.test(entity, applicationContext);
    }

    public BiPredicate readPredicate() {
        return readPredicate;
    }

    public void addReadPredicate(BiPredicate predicate) {
        if (this.readPredicate == null) {
            this.readPredicate = predicate;
        } else {
            this.readPredicate = this.readPredicate.and(predicate);
        }
    }

    public boolean isUpdatePermitted(Object entity) {
        return updatePredicate == null || updatePredicate.test(entity, applicationContext);
    }

    public BiPredicate updatePredicate() {
        return updatePredicate;
    }

    public void addUpdatePredicate(BiPredicate predicate) {
        if (this.updatePredicate == null) {
            this.updatePredicate = predicate;
        } else {
            this.updatePredicate = this.updatePredicate.and(predicate);
        }
    }

    public boolean isDeletePermitted(Object entity) {
        return deletePredicate == null || deletePredicate.test(entity, applicationContext);
    }

    public BiPredicate deletePredicate() {
        return deletePredicate;
    }

    public void addDeletePredicate(BiPredicate predicate) {
        if (this.deletePredicate == null) {
            this.deletePredicate = predicate;
        } else {
            this.deletePredicate = this.deletePredicate.and(predicate);
        }
    }

    @Nullable
    @Override
    public String explainConstraints() {
        List<String> predicates = new ArrayList<>();
        if (createPredicate != null)
            predicates.add("create");
        if (readPredicate != null)
            predicates.add("read");
        if (updatePredicate != null)
            predicates.add("update");
        if (deletePredicate != null)
            predicates.add("delete");
        if (!predicates.isEmpty()) {
            return entityClass.getName() + " predicates: " + String.join(", ", predicates);
        }
        return null;
    }
}

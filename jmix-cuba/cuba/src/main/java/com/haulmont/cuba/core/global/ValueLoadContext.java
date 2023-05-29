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

package com.haulmont.cuba.core.global;

import io.jmix.core.constraint.AccessConstraint;
import io.jmix.data.PersistenceHints;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.core.ValueLoadContext}.
 */
@Deprecated
public class ValueLoadContext extends io.jmix.core.ValueLoadContext {
    private static final long serialVersionUID = 2502274383837915002L;

    @Override
    public ValueLoadContext setStoreName(String storeName) {
        super.setStoreName(storeName);
        return this;
    }

    @Override
    public ValueLoadContext setQuery(Query query) {
        super.setQuery(query);
        return this;
    }

    @Override
    public ValueLoadContext setHint(String hintName, Serializable value) {
        super.setHint(hintName, value);
        return this;
    }

    @Override
    public ValueLoadContext setHints(Map<String, Serializable> hints) {
        super.setHints(hints);
        return this;
    }

    /**
     * @param softDeletion whether to use soft deletion when loading entities
     */
    public ValueLoadContext setSoftDeletion(boolean softDeletion) {
        super.setHint(PersistenceHints.SOFT_DELETION, softDeletion);
        return this;
    }

    /**
     * @return whether to use soft deletion when loading entities
     */
    public boolean isSoftDeletion() {
        Object value = super.getHints().get(PersistenceHints.SOFT_DELETION);
        return value == null || Boolean.TRUE.equals(value);
    }

    @Override
    public ValueLoadContext addProperty(String name) {
        super.addProperty(name);
        return this;
    }

    @Override
    public ValueLoadContext setProperties(List<String> properties) {
        super.setProperties(properties);
        return this;
    }

    @Override
    public ValueLoadContext setAccessConstraints(List<AccessConstraint<?>> accessConstraints) {
        super.setAccessConstraints(accessConstraints);
        return this;
    }

    @Override
    public ValueLoadContext setJoinTransaction(boolean joinTransaction) {
        super.setJoinTransaction(joinTransaction);
        return this;
    }
}

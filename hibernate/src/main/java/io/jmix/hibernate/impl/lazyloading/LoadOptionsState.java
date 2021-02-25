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

package io.jmix.hibernate.impl.lazyloading;

import io.jmix.core.EntityEntry;
import io.jmix.core.EntityEntryExtraState;
import io.jmix.core.constraint.AccessConstraint;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LoadOptionsState implements EntityEntryExtraState {

    protected EntityEntry entityEntry;
    protected boolean softDeletion;
    protected Map<String, Serializable> hints;
    protected transient List<AccessConstraint<?>> accessConstraints;

    public LoadOptionsState(EntityEntry entityEntry) {
        this.entityEntry = entityEntry;
    }

    public boolean isSoftDeletion() {
        return softDeletion;
    }

    public void setSoftDeletion(boolean softDeletion) {
        this.softDeletion = softDeletion;
    }

    public Map<String, Serializable> getHints() {
        return hints;
    }

    public void setHints(Map<String, Serializable> hints) {
        this.hints = hints;
    }

    public List<AccessConstraint<?>> getAccessConstraints() {
        return accessConstraints == null ? Collections.emptyList() : accessConstraints;
    }

    public void setAccessConstraints(List<AccessConstraint<?>> accessConstraints) {
        this.accessConstraints = accessConstraints;
    }

    @Override
    public EntityEntry getEntityEntry() {
        return entityEntry;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        protected boolean softDeletion;
        protected Map<String, Serializable> hints;
        protected transient List<AccessConstraint<?>> accessConstraints;

        protected Builder() {
        }

        public Builder softDeletion(boolean softDeletion) {
            this.softDeletion = softDeletion;
            return this;
        }

        public Builder hints(Map<String, Serializable> hints) {
            this.hints = hints;
            return this;
        }

        public Builder accessConstraints(List<AccessConstraint<?>> accessConstraints) {
            this.accessConstraints = accessConstraints;
            return this;
        }

        public LoadOptionsState build(EntityEntry entityEntry) {
            LoadOptionsState state = new LoadOptionsState(entityEntry);
            state.setSoftDeletion(softDeletion);
            state.setHints(hints);
            state.setAccessConstraints(accessConstraints);
            return state;
        }
    }
}

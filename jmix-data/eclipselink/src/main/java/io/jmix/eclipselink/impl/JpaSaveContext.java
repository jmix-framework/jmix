/*
 * Copyright 2026 Haulmont.
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

package io.jmix.eclipselink.impl;

import io.jmix.core.SaveContext;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Stores cascaded entities in addition to standard {@link SaveContext} data
 */
public class JpaSaveContext extends SaveContext {
    private static final long serialVersionUID = -915625536778688868L;

    protected Collection<Object> cascadeAffectedEntities = new LinkedHashSet<>();


    public JpaSaveContext(SaveContext context) {
        this.entitiesToSave = context.getEntitiesToSave();
        this.entitiesToRemove = context.getEntitiesToRemove();

        this.fetchPlans = context.getFetchPlans();
        this.discardSaved = context.isDiscardSaved();
        this.joinTransaction = context.isJoinTransaction();
        this.accessConstraints = context.getAccessConstraints();
        this.hints = context.getHints();
    }

    /**
     * @return entities from {@code entitiesToSave} and {@code entitiesToRemove} collections that has been added
     * because of cascade operations.
     */
    public Collection<Object> getCascadeAffectedEntities() {
        return cascadeAffectedEntities;
    }
}

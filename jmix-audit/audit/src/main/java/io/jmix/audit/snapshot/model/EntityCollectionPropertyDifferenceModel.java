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

package io.jmix.audit.snapshot.model;

import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.util.ArrayList;
import java.util.List;

@JmixEntity(name = "audit_EntityCollectionPropertyDiff")
@SystemLevel
public class EntityCollectionPropertyDifferenceModel extends EntityPropertyDifferenceModel {

    private List<EntityPropertyDifferenceModel> addedEntities = new ArrayList<>();

    private List<EntityPropertyDifferenceModel> removedEntities = new ArrayList<>();

    private List<EntityPropertyDifferenceModel> modifiedEntities = new ArrayList<>();

    public List<EntityPropertyDifferenceModel> getAddedEntities() {
        return addedEntities;
    }

    public void setAddedEntities(List<EntityPropertyDifferenceModel> addedEntities) {
        this.addedEntities = addedEntities;
    }

    public List<EntityPropertyDifferenceModel> getRemovedEntities() {
        return removedEntities;
    }

    public void setRemovedEntities(List<EntityPropertyDifferenceModel> removedEntities) {
        this.removedEntities = removedEntities;
    }

    public List<EntityPropertyDifferenceModel> getModifiedEntities() {
        return modifiedEntities;
    }

    public void setModifiedEntities(List<EntityPropertyDifferenceModel> modifiedEntities) {
        this.modifiedEntities = modifiedEntities;
    }
}
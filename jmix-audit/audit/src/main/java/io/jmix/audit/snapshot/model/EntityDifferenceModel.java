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

import io.jmix.core.FetchPlan;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import jakarta.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Diff object for Entity Snapshots
 */

@JmixEntity(annotatedPropertiesOnly = true)
public class EntityDifferenceModel {

    @Id
    @JmixGeneratedValue
    private UUID id;

    private FetchPlan diffFetchPlan;

    private EntitySnapshotModel beforeSnapshot;

    private EntitySnapshotModel afterSnapshot;

    private Object beforeEntity;

    private Object afterEntity;

    private List<EntityPropertyDifferenceModel> propertyDiffs = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public FetchPlan getDiffFetchPlan() {
        return diffFetchPlan;
    }

    public void setDiffFetchPlan(FetchPlan diffFetchPlan) {
        this.diffFetchPlan = diffFetchPlan;
    }

    public EntitySnapshotModel getBeforeSnapshot() {
        return beforeSnapshot;
    }

    public void setBeforeSnapshot(EntitySnapshotModel beforeSnapshot) {
        this.beforeSnapshot = beforeSnapshot;
    }

    public EntitySnapshotModel getAfterSnapshot() {
        return afterSnapshot;
    }

    public void setAfterSnapshot(EntitySnapshotModel afterSnapshot) {
        this.afterSnapshot = afterSnapshot;
    }

    public Object getBeforeEntity() {
        return beforeEntity;
    }

    public void setBeforeEntity(Object beforeEntity) {
        this.beforeEntity = beforeEntity;
    }

    public Object getAfterEntity() {
        return afterEntity;
    }

    public void setAfterEntity(Object afterEntity) {
        this.afterEntity = afterEntity;
    }

    public List<EntityPropertyDifferenceModel> getPropertyDiffs() {
        return propertyDiffs;
    }

    public void setPropertyDiffs(List<EntityPropertyDifferenceModel> propertyDiffs) {
        this.propertyDiffs = propertyDiffs;
    }

    @JmixProperty
    public String getLabel(){
        String label = "";
        if (beforeSnapshot != null)
            label += beforeSnapshot.getLabel() + " : ";
        else
            label += "";

        if (afterSnapshot != null)
            label += afterSnapshot.getLabel();

        return label;
    }
}
/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.core.model.common;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Diff object for Entity Snapshots
 */
@JmixEntity(name = "test$EntityDiff", annotatedPropertiesOnly = true)
@SystemLevel
public class EntityDiff extends BaseUuidEntity {

    private static final long serialVersionUID = -3884249873393845439L;

    private FetchPlan diffView;

    private EntitySnapshot beforeSnapshot;

    private EntitySnapshot afterSnapshot;

    private Entity beforeEntity;

    private Entity afterEntity;

    private List<EntityPropertyDiff> propertyDiffs = new ArrayList<>();

    public EntityDiff(FetchPlan diffView) {
        this.diffView = diffView;
    }

    public FetchPlan getDiffView() {
        return diffView;
    }

    public void setDiffView(FetchPlan diffView) {
        this.diffView = diffView;
    }

    public EntitySnapshot getBeforeSnapshot() {
        return beforeSnapshot;
    }

    public void setBeforeSnapshot(EntitySnapshot beforeSnapshot) {
        this.beforeSnapshot = beforeSnapshot;
    }

    public EntitySnapshot getAfterSnapshot() {
        return afterSnapshot;
    }

    public void setAfterSnapshot(EntitySnapshot afterSnapshot) {
        this.afterSnapshot = afterSnapshot;
    }

    public Entity getBeforeEntity() {
        return beforeEntity;
    }

    public void setBeforeEntity(Entity beforeEntity) {
        this.beforeEntity = beforeEntity;
    }

    public Entity getAfterEntity() {
        return afterEntity;
    }

    public void setAfterEntity(Entity afterEntity) {
        this.afterEntity = afterEntity;
    }

    public List<EntityPropertyDiff> getPropertyDiffs() {
        return propertyDiffs;
    }

    public void setPropertyDiffs(List<EntityPropertyDiff> propertyDiffs) {
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

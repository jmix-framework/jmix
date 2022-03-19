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

package io.jmix.core;

import java.io.Serializable;

public class EntityImportPlanProperty implements Serializable {

    protected String name;

    protected EntityImportPlan plan;

    protected ReferenceImportBehaviour referenceImportBehaviour;

    protected CollectionImportPolicy collectionImportPolicy;

    public EntityImportPlanProperty(String name) {
        this.name = name;
    }

    public EntityImportPlanProperty(String name, EntityImportPlan plan) {
        this.name = name;
        this.plan = plan;
    }

    public EntityImportPlanProperty(String name, EntityImportPlan plan, CollectionImportPolicy collectionImportPolicy) {
        this.name = name;
        this.plan = plan;
        this.collectionImportPolicy = collectionImportPolicy;
    }

    public EntityImportPlanProperty(String name, ReferenceImportBehaviour referenceImportBehaviour) {
        this.name = name;
        this.referenceImportBehaviour = referenceImportBehaviour;
    }

    public EntityImportPlanProperty(String name, ReferenceImportBehaviour referenceImportBehaviour, CollectionImportPolicy collectionImportPolicy) {
        this.name = name;
        this.referenceImportBehaviour = referenceImportBehaviour;
        this.collectionImportPolicy = collectionImportPolicy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityImportPlan getPlan() {
        return plan;
    }

    public void setPlan(EntityImportPlan plan) {
        this.plan = plan;
    }

    public ReferenceImportBehaviour getReferenceImportBehaviour() {
        return referenceImportBehaviour;
    }

    public void setReferenceImportBehaviour(ReferenceImportBehaviour referenceImportBehaviour) {
        this.referenceImportBehaviour = referenceImportBehaviour;
    }

    public CollectionImportPolicy getCollectionImportPolicy() {
        return collectionImportPolicy;
    }

    public void setCollectionImportPolicy(CollectionImportPolicy collectionImportPolicy) {
        this.collectionImportPolicy = collectionImportPolicy;
    }
}

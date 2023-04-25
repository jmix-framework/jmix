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

package io.jmix.reports.entity.wizard;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import java.util.UUID;

/**
 * Immutable property class
 */
@JmixEntity(name = "report_WizardReportRegionProperty")
@SystemLevel
public class RegionProperty implements OrderableEntity {

    private static final long serialVersionUID = 8528946767216568803L;

    @Id
    @JmixGeneratedValue
    protected UUID id;
    @JmixProperty
    @Transient
    protected EntityTreeNode entityTreeNode;
    @JmixProperty
    @Transient
    protected Long orderNum;

    public EntityTreeNode getEntityTreeNode() {
        return entityTreeNode;
    }

    public void setEntityTreeNode(EntityTreeNode entityTreeNode) {
        this.entityTreeNode = entityTreeNode;
    }

    @Override
    public Long getOrderNum() {
        return orderNum;
    }

    @Override
    public void setOrderNum(Long orderNum) {
        this.orderNum = orderNum;
    }

    @JmixProperty
    @Transient
    public String getName() {
        return entityTreeNode.getName();
    }

    @JmixProperty
    @Transient
    public String getLocalizedName() {
        return entityTreeNode.getLocalizedName();
    }

    @JmixProperty
    @Transient
    public String getHierarchicalName() {
        return entityTreeNode.getHierarchicalName();
    }

    @JmixProperty
    @Transient
    public String getHierarchicalNameExceptRoot() {
        return entityTreeNode.getHierarchicalNameExceptRoot();
    }

    @JmixProperty
    @Transient
    public String getHierarchicalLocalizedName() {
        return entityTreeNode.getHierarchicalLocalizedName();
    }

    @JmixProperty
    @Transient
    public String getHierarchicalLocalizedNameExceptRoot() {
        return entityTreeNode.getHierarchicalLocalizedNameExceptRoot();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}



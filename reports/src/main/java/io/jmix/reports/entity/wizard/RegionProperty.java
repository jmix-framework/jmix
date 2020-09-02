/*
 * Copyright (c) 2008-2019 Haulmont.
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

import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import io.jmix.core.entity.annotation.SystemLevel;

import javax.persistence.Transient;

/**
 * Immutable property class
 *
 */
@ModelObject(name = "report$WizardReportRegionProperty")
@SystemLevel
public class RegionProperty extends BaseUuidEntity implements OrderableEntity {

    private static final long serialVersionUID = 8528946767216568803L;

    @ModelProperty
    @Transient
    protected EntityTreeNode entityTreeNode;
    @ModelProperty
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

    @ModelProperty
    @Transient
    public String getName() {
        return entityTreeNode.getName();
    }

    @ModelProperty
    @Transient
    public String getLocalizedName() {
        return entityTreeNode.getLocalizedName();
    }

    @ModelProperty
    @Transient
    public String getHierarchicalName() {
        return entityTreeNode.getHierarchicalName();
    }

    @ModelProperty
    @Transient
    public String getHierarchicalNameExceptRoot() {
        return entityTreeNode.getHierarchicalNameExceptRoot();
    }

    @ModelProperty
    @Transient
    public String getHierarchicalLocalizedName() {
        return entityTreeNode.getHierarchicalLocalizedName();
    }

    @ModelProperty
    @Transient
    public String getHierarchicalLocalizedNameExceptRoot() {
        return entityTreeNode.getHierarchicalLocalizedNameExceptRoot();
    }
}



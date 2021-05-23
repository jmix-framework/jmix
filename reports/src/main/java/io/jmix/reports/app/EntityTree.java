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

package io.jmix.reports.app;

import io.jmix.reports.entity.wizard.EntityTreeNode;

import java.io.Serializable;

public class EntityTree implements Serializable {

    private static final long serialVersionUID = -7639009888440026734L;
    protected EntityTreeNode entityTreeRootNode;
    protected EntityTreeStructureInfo entityTreeStructureInfo;

    public EntityTree() {
    }

    public EntityTree(EntityTreeNode entityTreeRootNode, EntityTreeStructureInfo entityTreeStructureInfo) {
        this.entityTreeRootNode = entityTreeRootNode;
        this.entityTreeStructureInfo = entityTreeStructureInfo;
    }

    public EntityTreeNode getEntityTreeRootNode() {
        return entityTreeRootNode;
    }

    public void setEntityTreeRootNode(EntityTreeNode entityTreeRootNode) {
        this.entityTreeRootNode = entityTreeRootNode;
    }

    public EntityTreeStructureInfo getEntityTreeStructureInfo() {
        return entityTreeStructureInfo;
    }

    public void setEntityTreeStructureInfo(EntityTreeStructureInfo entityTreeStructureInfo) {
        this.entityTreeStructureInfo = entityTreeStructureInfo;
    }
}

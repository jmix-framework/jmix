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

import io.jmix.core.Entity;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;

import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JmixEntity(name = "report_WizardReportEntityTreeNode", annotatedPropertiesOnly = true)
@SystemLevel
public class EntityTreeNode implements Entity {

    private static final long serialVersionUID = 465985155557062476L;

    @Id
    @JmixGeneratedValue
    protected UUID id;

    @JmixProperty
    @Transient
    protected String name;
    @JmixProperty
    @Transient
    protected String localizedName;
    @JmixProperty
    @Transient
    protected EntityTreeNode parent;
    @JmixProperty
    @Composition
    @Transient
    protected List<EntityTreeNode> children = new ArrayList<>();
    @Transient
    protected MetaClass wrappedMetaClass;//'wrappedMetaClass' name cause 'metaClass' field already exists in superclass
    @Transient
    protected MetaProperty wrappedMetaProperty;

    @JmixProperty
    @Transient
    public Integer getNodeDepth() {
        if (getParent() == null) {
            return 1;
        } else {
            return getParent().getNodeDepth() + 1;
        }
    }

    /**
     * Calculates depth of child nodes. Can to be used in sorting
     *
     * @return
     */
    @JmixProperty
    @Transient
    public Integer getNodeChildrenDepth() {
        if (getChildren().isEmpty()) {
            return getNodeDepth();
        } else {
            int maxDepth = 0;
            for (EntityTreeNode entityTreeNode : getChildren()) {
                int depthOfChildren = entityTreeNode.getNodeChildrenDepth();
                if (maxDepth < depthOfChildren) {
                    maxDepth = depthOfChildren;
                }
            }
            return maxDepth;
        }
    }

    @JmixProperty
    @Transient
    public String getHierarchicalName() {
        if (getParent() == null) {
            return name;
        } else {
            return getParent().getHierarchicalName() + "." + name;
        }
    }

    @JmixProperty
    @Transient
    public String getHierarchicalLocalizedName() {
        if (getParent() == null) {
            return localizedName;
        } else {
            return getParent().getHierarchicalLocalizedName() + "." + localizedName;
        }
    }

    @JmixProperty
    @Transient
    public String getHierarchicalNameExceptRoot() {
        if (getParent() == null) {
            return "";
        } else {
            if (!"".equals(getParent().getHierarchicalNameExceptRoot())) {
                return getParent().getHierarchicalNameExceptRoot() + "." + name;
            } else {
                return name;
            }
        }
    }

    @JmixProperty
    @Transient
    public String getHierarchicalLocalizedNameExceptRoot() {
        if (getParent() == null) {
            return "";
        } else {
            if (!"".equals(getParent().getHierarchicalLocalizedNameExceptRoot())) {
                return getParent().getHierarchicalLocalizedNameExceptRoot() + "." + localizedName;
            } else {
                return localizedName;
            }
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public void setLocalizedName(String localizedName) {
        this.localizedName = localizedName;
    }

    public String getName() {
        return name;
        /*if (isAttribute) {
            return wrappedMetaProperty.getFullName();
        } else {
            return wrappedMetaClass.getFullName();
        }*/
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityTreeNode getParent() {
        return parent;
    }

    public void setParent(EntityTreeNode parent) {
        this.parent = parent;
    }

    public List<EntityTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<EntityTreeNode> children) {
        this.children = children;
    }

    public MetaClass getWrappedMetaClass() {
        return wrappedMetaClass;
    }

    public void setWrappedMetaClass(MetaClass wrappedMetaClass) {
        this.wrappedMetaClass = wrappedMetaClass;
    }

    public MetaProperty getWrappedMetaProperty() {
        return wrappedMetaProperty;
    }

    public void setWrappedMetaProperty(MetaProperty wrappedMetaProperty) {
        this.wrappedMetaProperty = wrappedMetaProperty;
    }
}

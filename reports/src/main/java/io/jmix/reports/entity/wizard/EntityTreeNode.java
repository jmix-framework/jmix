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

import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

@ModelObject(name = "report$WizardReportEntityTreeNode")
@SystemLevel
public class EntityTreeNode extends BaseUuidEntity {

    private static final long serialVersionUID = 465985155557062476L;

    @ModelProperty
    @Transient
    protected String name;
    @ModelProperty
    @Transient
    protected String localizedName;
    @ModelProperty
    @Transient
    protected EntityTreeNode parent;
    @ModelProperty
    @Composition
    @Transient
    protected List<EntityTreeNode> children = new ArrayList<>();
    @Transient
    protected MetaClass wrappedMetaClass;//'wrappedMetaClass' name cause 'metaClass' field already exists in superclass
    @Transient
    protected MetaProperty wrappedMetaProperty;

    @ModelProperty
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
    @ModelProperty
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

    @ModelProperty
    @Transient
    public String getHierarchicalName() {
        if (getParent() == null) {
            return name;
        } else {
            return getParent().getHierarchicalName() + "." + name;
        }
    }

    @ModelProperty
    @Transient
    public String getHierarchicalLocalizedName() {
        if (getParent() == null) {
            return localizedName;
        } else {
            return getParent().getHierarchicalLocalizedName() + "." + localizedName;
        }
    }

    @ModelProperty
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

    @ModelProperty
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

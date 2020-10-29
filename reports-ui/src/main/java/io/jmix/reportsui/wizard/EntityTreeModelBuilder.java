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

package io.jmix.reportsui.wizard;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.Metadata;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.reports.ReportingConfig;
import io.jmix.reports.app.EntityTree;
import io.jmix.reports.app.EntityTreeStructureInfo;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component("report_EntityTreeModelBuilder")
@Scope("prototype")
public class EntityTreeModelBuilder implements EntityTreeModelBuilderApi {

    @Autowired
    protected MessageTools messageTools;

    @Autowired
    protected ReportingWizardApi reportingWizardApi;

    @Autowired
    protected ReportingConfig reportingConfig;

    @Autowired
    protected Metadata metadata;

    protected int entityTreeModelMaxDeep = reportingConfig.getEntityTreeModelMaxDeep();

    public int getEntityTreeModelMaxDeep() {
        return entityTreeModelMaxDeep;
    }

    public void setEntityTreeModelMaxDeep(int entityTreeModelMaxDeep) {
        this.entityTreeModelMaxDeep = entityTreeModelMaxDeep;
    }

    @Override
    public EntityTree buildEntityTree(MetaClass metaClass) {
        EntityTree entityTree = new EntityTree();
        EntityTreeStructureInfo entityTreeStructureInfo = new EntityTreeStructureInfo();
        EntityTreeNode root = metadata.create(EntityTreeNode.class);
        root.setName(metaClass.getName());
        root.setLocalizedName(StringUtils.isEmpty(messageTools.getEntityCaption(metaClass)) ? metaClass.getName() : messageTools.getEntityCaption(metaClass));
        root.setWrappedMetaClass(metaClass);
        fillChildNodes(root, 1, new HashSet<String>(), entityTreeStructureInfo);
        entityTree.setEntityTreeRootNode(root);
        entityTree.setEntityTreeStructureInfo(entityTreeStructureInfo);
        return entityTree;
    }

    protected EntityTreeNode fillChildNodes(final EntityTreeNode parentEntityTreeNode, int depth, final Set<String> alreadyAddedMetaProps, final EntityTreeStructureInfo entityTreeStructureInfo) {

        if (depth > getEntityTreeModelMaxDeep()) {
            return parentEntityTreeNode;
        }
        MetaClass fileDescriptorMetaClass = metadata.getClassNN(FileDescriptor.class);
        for (MetaProperty metaProperty : parentEntityTreeNode.getWrappedMetaClass().getProperties()) {
            if (!reportingWizardApi.isPropertyAllowedForReportWizard(parentEntityTreeNode.getWrappedMetaClass(), metaProperty)) {
                continue;
            }
            if (metaProperty.getRange().isClass()) {
                MetaClass metaClass = metaProperty.getRange().asClass();
                MetaClass effectiveMetaClass = metadata.getExtendedEntities().getEffectiveMetaClass(metaClass);
                //does we need to do security checks here? no

                MetadataTools metadataTools = metadata.getTools();

                if (fileDescriptorMetaClass.equals(effectiveMetaClass) || !metadataTools.isSystemLevel(effectiveMetaClass) && !metadataTools.isSystemLevel(metaProperty)) {
                    int newDepth = depth + 1;
                    EntityTreeNode newParentModelNode = metadata.create(EntityTreeNode.class);
                    newParentModelNode.setName(metaProperty.getName());
                    //newParentModelNode.setLocalizedName(messageTools.getEntityCaption(effectiveMetaClass));
                    newParentModelNode.setLocalizedName(
                            StringUtils.isEmpty(messageTools.getPropertyCaption(parentEntityTreeNode.getWrappedMetaClass(), metaProperty.getName())) ?
                                    metaProperty.getName() : messageTools.getPropertyCaption(parentEntityTreeNode.getWrappedMetaClass(), metaProperty.getName())
                    );
                    newParentModelNode.setWrappedMetaClass(effectiveMetaClass);
                    newParentModelNode.setWrappedMetaProperty(metaProperty);
                    newParentModelNode.setParent(parentEntityTreeNode);


                    if (alreadyAddedMetaProps.contains(getTreeNodeInfo(parentEntityTreeNode) + "|" + getTreeNodeInfo(newParentModelNode))) {
                        continue; //avoid parent-child-parent-... infinite loops
                    }
                    //alreadyAddedMetaProps.add(getTreeNodeInfo(parentEntityTreeNode) + "|" + getTreeNodeInfo(newParentModelNode));
                    alreadyAddedMetaProps.add(getTreeNodeInfo(newParentModelNode) + "|" + getTreeNodeInfo(parentEntityTreeNode));

                    //System.err.println(StringUtils.leftPad("", newDepth * 2, " ") + getTreeNodeInfo(parentEntityTreeNode) + "     |     " + getTreeNodeInfo(newParentModelNode));
                    //System.err.println(StringUtils.leftPad("", newDepth * 2, " ") + getTreeNodeInfo(newParentModelNode) + "     |     " + getTreeNodeInfo(parentEntityTreeNode));
                    //System.err.println("");

                    if (!entityTreeStructureInfo.isEntityTreeRootHasCollections() && metaProperty.getRange().getCardinality().isMany() && depth == 1) {
                        entityTreeStructureInfo.setEntityTreeRootHasCollections(true);//TODO set to true if only simple attributes of that collection as children exists
                    }
                    fillChildNodes(newParentModelNode, newDepth, alreadyAddedMetaProps, entityTreeStructureInfo);

                    parentEntityTreeNode.getChildren().add(newParentModelNode);
                }
            } else {
                if (!entityTreeStructureInfo.isEntityTreeHasSimpleAttrs()) {
                    entityTreeStructureInfo.setEntityTreeHasSimpleAttrs(true);
                }
                EntityTreeNode child = metadata.create(EntityTreeNode.class);
                child.setName(metaProperty.getName());
                child.setLocalizedName(StringUtils.isEmpty(messageTools.
                        getPropertyCaption(parentEntityTreeNode.getWrappedMetaClass(), metaProperty.getName())) ?
                        metaProperty.getName() : messageTools.getPropertyCaption(parentEntityTreeNode.getWrappedMetaClass(), metaProperty.getName()));
                child.setWrappedMetaProperty(metaProperty);
                child.setParent(parentEntityTreeNode);
                parentEntityTreeNode.getChildren().add(child);

            }

        }
        return parentEntityTreeNode;
    }

    private String getTreeNodeInfo(EntityTreeNode parentEntityTreeNode) {
        if (parentEntityTreeNode.getWrappedMetaProperty() != null) {
            return (parentEntityTreeNode.getWrappedMetaProperty().getDomain().getName().equals(parentEntityTreeNode.getWrappedMetaClass().getName()) ?
                    "" : parentEntityTreeNode.getWrappedMetaProperty().getDomain() + ".") +
                    parentEntityTreeNode.getWrappedMetaClass().getName() + " isMany:" + parentEntityTreeNode.getWrappedMetaProperty().getRange().getCardinality().isMany();
        } else {
            return parentEntityTreeNode.getWrappedMetaClass().getName() + " isMany:false";
        }
    }
}


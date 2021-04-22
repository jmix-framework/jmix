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

package io.jmix.reportsui.screen.report.wizard;

import io.jmix.core.ExtendedEntities;
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.reports.ReportsProperties;
import io.jmix.reports.app.EntityTree;
import io.jmix.reports.app.EntityTreeStructureInfo;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

@Component("report_EntityTreeModelBuilder")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EntityTreeModelBuilder {

    @Autowired
    protected MessageTools messageTools;

    @Autowired
    protected ReportsWizard reportsWizard;

    @Autowired
    protected ReportsProperties reportsProperties;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected ExtendedEntities extendedEntities;

    @Autowired
    protected MetadataTools metadataTools;

    protected int entityTreeModelMaxDeep;

    @PostConstruct
    protected void init() {
        entityTreeModelMaxDeep = reportsProperties.getEntityTreeModelMaxDeep();
    }

    public int getEntityTreeModelMaxDeep() {
        return entityTreeModelMaxDeep;
    }

    public void setEntityTreeModelMaxDeep(int entityTreeModelMaxDeep) {
        this.entityTreeModelMaxDeep = entityTreeModelMaxDeep;
    }

    public EntityTree buildEntityTree(MetaClass metaClass) {
        EntityTree entityTree = new EntityTree();
        EntityTreeStructureInfo entityTreeStructureInfo = new EntityTreeStructureInfo();
        EntityTreeNode root = metadata.create(EntityTreeNode.class);
        root.setName(metaClass.getName());
        root.setLocalizedName(StringUtils.isEmpty(messageTools.getEntityCaption(metaClass)) ? metaClass.getName() : messageTools.getEntityCaption(metaClass));
        root.setWrappedMetaClass(metaClass.getName());
        fillChildNodes(root, 1, new HashSet<String>(), entityTreeStructureInfo);
        entityTree.setEntityTreeRootNode(root);
        entityTree.setEntityTreeStructureInfo(entityTreeStructureInfo);
        return entityTree;
    }

    protected EntityTreeNode fillChildNodes(final EntityTreeNode parentEntityTreeNode, int depth, final Set<String> alreadyAddedMetaProps, final EntityTreeStructureInfo entityTreeStructureInfo) {

        if (depth > getEntityTreeModelMaxDeep()) {
            return parentEntityTreeNode;
        }
        //todo
//        MetaClass fileDescriptorMetaClass = metadata.getClass(FileDescriptor.class);
        MetaClass wrappedMetaClass = metadata.getClass(parentEntityTreeNode.getWrappedMetaClass());
        for (MetaProperty metaProperty : wrappedMetaClass.getProperties()) {
            if (!reportsWizard.isPropertyAllowedForReportWizard(wrappedMetaClass, metaProperty)) {
                continue;
            }
            if (metaProperty.getRange().isClass()) {
                MetaClass metaClass = metaProperty.getRange().asClass();
                MetaClass effectiveMetaClass = extendedEntities.getEffectiveMetaClass(metaClass);
                //does we need to do security checks here? no

                if (/*fileDescriptorMetaClass.equals(effectiveMetaClass) ||*/  !metadataTools.isSystemLevel(effectiveMetaClass) && !metadataTools.isSystemLevel(metaProperty)) {
                    int newDepth = depth + 1;
                    EntityTreeNode newParentModelNode = metadata.create(EntityTreeNode.class);
                    newParentModelNode.setName(metaProperty.getName());
                    newParentModelNode.setLocalizedName(messageTools.getEntityCaption(effectiveMetaClass));
                    newParentModelNode.setLocalizedName(
                            StringUtils.isEmpty(messageTools.getPropertyCaption(wrappedMetaClass, metaProperty.getName())) ?
                                    metaProperty.getName() : messageTools.getPropertyCaption(wrappedMetaClass, metaProperty.getName())
                    );
                    newParentModelNode.setWrappedMetaClass(effectiveMetaClass.getName());
                    //newParentModelNode.setWrappedMetaProperty(metaProperty.getName());
                    newParentModelNode.setParent(parentEntityTreeNode);


                    if (alreadyAddedMetaProps.contains(getTreeNodeInfo(parentEntityTreeNode) + "|" + getTreeNodeInfo(newParentModelNode))) {
                        continue; //avoid parent-child-parent-... infinite loops
                    }

                    alreadyAddedMetaProps.add(getTreeNodeInfo(newParentModelNode) + "|" + getTreeNodeInfo(parentEntityTreeNode));

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
                        getPropertyCaption(wrappedMetaClass, metaProperty.getName())) ?
                        metaProperty.getName() : messageTools.getPropertyCaption(wrappedMetaClass, metaProperty.getName()));
                child.setWrappedMetaClass(wrappedMetaClass.getName());
                child.setWrappedMetaProperty(metaProperty.getName());
                child.setParent(parentEntityTreeNode);
                parentEntityTreeNode.getChildren().add(child);
            }

        }
        return parentEntityTreeNode;
    }

    private String getTreeNodeInfo(EntityTreeNode parentEntityTreeNode) {
        MetaClass parentMetaClass = metadata.getClass(parentEntityTreeNode.getWrappedMetaClass());
        String parentMetaClassName = parentMetaClass.getName();

        if (parentEntityTreeNode.getWrappedMetaProperty() == null) {
            return parentMetaClassName + " isMany:false";
        }

        MetaClass metaClass = metadata.getClass(parentEntityTreeNode.getWrappedMetaClass());
        MetaProperty metaProperty = metaClass.getProperty(parentEntityTreeNode.getWrappedMetaProperty());

        boolean isMany = metaProperty.getRange().getCardinality().isMany();
        MetaClass domain = metaProperty.getDomain();

        if (domain.getName().equals(parentMetaClassName)) {
            return parentMetaClass + " isMany:" + isMany;
        }

        return domain + "." + parentMetaClass + " isMany:" + isMany;
    }
}


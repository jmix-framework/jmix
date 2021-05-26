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

import javax.annotation.Nullable;
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

    protected int entityTreeModelMaxDepth;

    @PostConstruct
    protected void init() {
        entityTreeModelMaxDepth = reportsProperties.getEntityTreeModelMaxDepth();
    }

    public int getEntityTreeModelMaxDepth() {
        return entityTreeModelMaxDepth;
    }

    public void setEntityTreeModelMaxDepth(int entityTreeModelMaxDepth) {
        this.entityTreeModelMaxDepth = entityTreeModelMaxDepth;
    }

    public EntityTree buildEntityTree(MetaClass metaClass) {
        EntityTreeStructureInfo entityTreeStructureInfo = new EntityTreeStructureInfo();
        EntityTreeNode root = createRootNode(metaClass);
        fillChildNodes(root, 1, new HashSet<>(), entityTreeStructureInfo);
        return new EntityTree(root, entityTreeStructureInfo);
    }

    protected void fillChildNodes(final EntityTreeNode parentNode, int depth, final Set<String> alreadyAddedMetaProps, final EntityTreeStructureInfo treeStructureInfo) {

        if (depth > getEntityTreeModelMaxDepth()) {
            return;
        }

        MetaClass parentMetaClass = metadata.getClass(parentNode.getMetaClassName());
        for (MetaProperty metaProperty : parentMetaClass.getProperties()) {
            if (!reportsWizard.isPropertyAllowedForReportWizard(parentMetaClass, metaProperty)) {
                continue;
            }
            if (metaProperty.getRange().isClass()) {
                MetaClass propertyMetaClass = metaProperty.getRange().asClass();
                MetaClass effectiveMetaClass = extendedEntities.getEffectiveMetaClass(propertyMetaClass);
                //does we need to do security checks here? no

                if (!metadataTools.isSystemLevel(effectiveMetaClass) && !metadataTools.isSystemLevel(metaProperty)) {
                    int newDepth = depth + 1;
                    EntityTreeNode childNode = createEntityTreeNode(metaProperty, parentNode, parentMetaClass, effectiveMetaClass);

                    if (alreadyAddedMetaProps.contains(getTreeNodeInfo(parentNode) + "|" + getTreeNodeInfo(childNode))) {
                        continue; //avoid parent-child-parent-... infinite loops
                    }

                    alreadyAddedMetaProps.add(getTreeNodeInfo(childNode) + "|" + getTreeNodeInfo(parentNode));

                    if (!treeStructureInfo.isEntityTreeRootHasCollections() && isMany(metaProperty) && depth == 1) {
                        treeStructureInfo.setEntityTreeRootHasCollections(true);
                    }
                    fillChildNodes(childNode, newDepth, alreadyAddedMetaProps, treeStructureInfo);

                    parentNode.getChildren().add(childNode);
                }
            } else {
                if (!treeStructureInfo.isEntityTreeHasSimpleAttrs()) {
                    treeStructureInfo.setEntityTreeHasSimpleAttrs(true);
                }
                EntityTreeNode child = createEntityTreeNode(metaProperty, parentNode, parentMetaClass, null);
                parentNode.getChildren().add(child);
            }
        }
    }

    private String getTreeNodeInfo(EntityTreeNode node) {
        String metaClassName = node.getMetaClassName();
        if (node.getMetaPropertyName() == null) {
            return String.format("%s isMany:false", metaClassName);
        }

        MetaClass parentMetaClass = metadata.getClass(node.getParentMetaClassName());
        MetaProperty metaProperty = parentMetaClass.getProperty(node.getMetaPropertyName());

        boolean isMany = isMany(metaProperty);
        MetaClass domain = metaProperty.getDomain();

        if (domain.getName().equals(metaClassName)) {
            return String.format("%s isMany:%s", metaClassName, isMany);
        }

        return String.format("%s.%s isMany:%s", domain, metaClassName, isMany);
    }

    protected EntityTreeNode createEntityTreeNode(MetaProperty metaProperty,
                                                  EntityTreeNode parent,
                                                  MetaClass parentMetaClass,
                                                  @Nullable MetaClass propertyMetaClass) {
        EntityTreeNode node = metadata.create(EntityTreeNode.class);
        node.setName(metaProperty.getName());
        node.setLocalizedName(StringUtils.defaultIfEmpty(messageTools.getPropertyCaption(parentMetaClass, metaProperty.getName()), metaProperty.getName()));
        if (propertyMetaClass != null) {
            node.setMetaClassName(propertyMetaClass.getName());
            node.setEntityClassName(propertyMetaClass.getJavaClass().getSimpleName());
        }
        node.setMetaPropertyName(metaProperty.getName());
        node.setParent(parent);

        return node;
    }

    protected EntityTreeNode createRootNode(MetaClass metaClass) {
        EntityTreeNode root = metadata.create(EntityTreeNode.class);
        root.setName(metaClass.getName());
        root.setEntityClassName(metaClass.getJavaClass().getSimpleName());
        root.setLocalizedName(StringUtils.defaultIfEmpty(messageTools.getEntityCaption(metaClass), metaClass.getName()));
        root.setMetaClassName(metaClass.getName());
        return root;
    }

    protected boolean isMany(MetaProperty metaProperty) {
        return metaProperty.getRange().getCardinality().isMany();
    }
}


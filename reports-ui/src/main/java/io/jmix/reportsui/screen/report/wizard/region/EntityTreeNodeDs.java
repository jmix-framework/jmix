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

package io.jmix.reportsui.screen.report.wizard.region;

import io.jmix.core.MetadataTools;
import io.jmix.core.common.datastruct.Node;
import io.jmix.core.common.datastruct.Tree;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.ui.component.TextField;
import io.jmix.ui.model.impl.CollectionContainerImpl;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.Collator;
import java.util.*;

/**
 * Build Tree using rootEntity as root Node
 */
public class EntityTreeNodeDs extends CollectionContainerImpl<EntityTreeNode> {
    protected boolean collectionsOnly;
    protected boolean scalarOnly;
    protected boolean persistentOnly;
    protected boolean showRoot;

    @Autowired
    protected MetadataTools metadataTools;

    protected Comparator<EntityTreeNode> nodeComparator = (o1, o2) -> {
        Collator collator = Collator.getInstance();
        return collator.compare(o1.getHierarchicalLocalizedNameExceptRoot(), o2.getHierarchicalLocalizedNameExceptRoot());
    };

    public EntityTreeNodeDs(MetaClass metaClass) {
        super(metaClass);
    }

    //    @Override
    protected Tree<EntityTreeNode> loadTree(Map<String, Object> params) {
        collectionsOnly = isTreeForCollectionsOnly(params);
        scalarOnly = isTreeForScalarOnly(params);
        persistentOnly = isTreeForPersistentOnly(params);
        showRoot = isTreeMustContainRoot(params);

        Tree<EntityTreeNode> resultTree = new Tree<>();

//        TextField<String> reportPropertyField = (TextField) params.get("component$reportPropertyName");
//        String searchValue = StringUtils.defaultIfBlank(reportPropertyField.getValue(), "").toLowerCase().trim();
//        if (params.get("rootEntity") != null) {
//            EntityTreeNode rootNodeObject = (EntityTreeNode) params.get("rootEntity");
//            List<Node<EntityTreeNode>> rootNodes;
//            if (isTreeMustContainRoot(params)) {
//                Node<EntityTreeNode> rootNode = new Node<>(rootNodeObject);
//                if (rootNodeObject.getLocalizedName().toLowerCase().contains(searchValue))
//                    fill(rootNode);
//                else
//                    fill(rootNode, searchValue);
//                rootNodes = !rootNode.getChildren().isEmpty() ? Collections.singletonList(rootNode) : Collections.emptyList();
//            } else {//don`t show current node in the tree. show only children
//                rootNodes = new ArrayList<>(rootNodeObject.getChildren().size());
//                for (EntityTreeNode child : rootNodeObject.getChildren()) {
//                    if (scalarOnly && child.getWrappedMetaProperty().getRange().getCardinality().isMany()) {
//                        continue;
//                    }
//                    Node<EntityTreeNode> newRootNode = new Node<>(child);
//                    if (child.getLocalizedName().toLowerCase().contains(searchValue))
//                        fill(newRootNode);
//                    else
//                        fill(newRootNode, searchValue);
//                    if (!newRootNode.getChildren().isEmpty())
//                        rootNodes.add(newRootNode);
//                }
//            }
//            resultTree.setRootNodes(rootNodes);
//        }
        return resultTree;
    }

    protected void fill(final Node<EntityTreeNode> parentNode, String searchValue) {
//        parentNode.getData().getChildren().sort(nodeComparator);
//
//        for (EntityTreeNode child : parentNode.getData().getChildren()) {
//            if (collectionsOnly && !child.getWrappedMetaProperty().getRange().getCardinality().isMany()) {
//                continue;
//            }
//
//            if (metadataTools.isSystemLevel(child.getWrappedMetaProperty())) {
//                continue;
//            }
//
//            if (scalarOnly && child.getWrappedMetaProperty().getRange().getCardinality().isMany()) {
//                continue;
//            }
//
//            if (collectionsOnly && (
//                    (showRoot && parentNode.getParent() != null && parentNode.getParent().getParent() == null) ||
//                            (!showRoot && parentNode.getParent() == null)
//            )) {
//                //for collections max selection depth is limited to 2 cause reporting is not supported collection multiplying. And it is good )
//                continue;
//            }
//
//            if (persistentOnly && !metadataTools.isJpa(child.getWrappedMetaProperty())) {
//                continue;
//            }
//
//            if (!child.getChildren().isEmpty()) {
//                Node<EntityTreeNode> newParentNode = new Node<>(child);
//                newParentNode.parent = parentNode;
//                if (StringUtils.isEmpty(searchValue) || child.getLocalizedName().toLowerCase().contains(searchValue)) {
//                    fill(newParentNode);
//                    parentNode.addChild(newParentNode);
//                }
//            } else {
//                if (scalarOnly && child.getWrappedMetaProperty().getRange().isClass()) {
//                    //doesn't fetch if it is a last entity and is a class cause we can`t select it in UI anyway
//                    continue;
//                }
//                Node<EntityTreeNode> childNode = new Node<>(child);
//                if (StringUtils.isEmpty(searchValue) || child.getLocalizedName().toLowerCase().contains(searchValue)) {
//                    parentNode.addChild(childNode);
//                }
//            }
//        }
    }

    protected void fill(final Node<EntityTreeNode> parentNode) {
        fill(parentNode, "");
    }

    protected boolean isTreeForCollectionsOnly(Map<String, Object> params) {
        return BooleanUtils.toBooleanDefaultIfNull((Boolean) params.get("collectionsOnly"), false);
    }

    protected boolean isTreeForScalarOnly(Map<String, Object> params) {
        return BooleanUtils.toBooleanDefaultIfNull((Boolean) params.get("scalarOnly"), false);
    }

    protected boolean isTreeForPersistentOnly(Map<String, Object> params) {
        return BooleanUtils.toBooleanDefaultIfNull((Boolean) params.get("persistentOnly"), false);
    }

    protected boolean isTreeMustContainRoot(Map<String, Object> params) {
        return BooleanUtils.toBooleanDefaultIfNull((Boolean) params.get("showRoot"), true);
    }
}
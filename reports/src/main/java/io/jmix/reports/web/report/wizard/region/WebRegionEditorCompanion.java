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

package io.jmix.reports.web.report.wizard.region;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.Tree;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.reports.entity.wizard.RegionProperty;
import io.jmix.reports.gui.report.wizard.region.RegionEditor;
import com.vaadin.ui.components.grid.TreeGridDragSource;
import com.vaadin.ui.dnd.DropTargetExtension;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.data.TreeItems;
import io.jmix.ui.screen.ScreenContext;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.widget.JmixTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class WebRegionEditorCompanion implements RegionEditor.Companion {

    protected static final String TREE_ITEM_ID_TYPE = "itemid";

    @SuppressWarnings("unchecked")
    @Override
    public void addTreeTableDblClickListener(Tree<EntityTreeNode> entityTree,
                                             CollectionDatasource<RegionProperty, UUID> reportRegionPropertiesTableDs) {
        JmixTree<EntityTreeNode> webTree = entityTree.unwrap(JmixTree.class);
        webTree.addItemClickListener(event -> {
            if (event.getMouseEventDetails().isDoubleClick()) {
                EntityTreeNode entityTreeNode = event.getItem();
                if (entityTreeNode.getWrappedMetaClass() != null) {
                    if (webTree.isExpanded(entityTreeNode))
                        webTree.collapse(entityTreeNode);
                    else
                        webTree.expand(entityTreeNode);
                    return;
                }

                boolean isAlreadyAdded = reportRegionPropertiesTableDs.getItems().stream()
                        .map(regionProperty -> regionProperty.getEntityTreeNode().getId())
                        .collect(Collectors.toSet())
                        .contains(entityTreeNode.getId());
                if (isAlreadyAdded) {
                    return;
                }

                Metadata metadata = AppBeans.get(Metadata.NAME);
                RegionProperty regionProperty = metadata.create(RegionProperty.class);
                regionProperty.setEntityTreeNode(entityTreeNode);
                regionProperty.setOrderNum((long) reportRegionPropertiesTableDs.getItemIds().size() + 1);
                //first element must be not zero cause later we do sorting by multiplying that values
                reportRegionPropertiesTableDs.addItem(regionProperty);

                Table propertiesTable = (Table) entityTree.getFrame().getComponent("propertiesTable");
                if (propertiesTable != null) {
                    propertiesTable.setSelected(regionProperty);

                    (propertiesTable.unwrap(com.vaadin.v7.ui.Table.class)).setCurrentPageFirstItemId(regionProperty.getId());
                }
            }
        });
    }

    @Override
    public void initControlBtnsActions(Button button, final Table table) {
        button.unwrap(com.vaadin.ui.Button.class).addClickListener((com.vaadin.ui.Button.ClickListener) event -> {
            com.vaadin.v7.ui.Table vaadinTable = table.unwrap(com.vaadin.v7.ui.Table.class);
            vaadinTable.setCurrentPageFirstItemId(vaadinTable.lastItemId());
            vaadinTable.refreshRowCache();
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initDragAndDrop(Tree<EntityTreeNode> entityTree, Table<RegionProperty> propertiesTable,
                                CollectionDatasource<RegionProperty, UUID> reportRegionPropertiesTableDs) {
        JmixTree<EntityTreeNode> vTree = entityTree.unwrap(JmixTree.class);
        TreeGridDragSource<EntityTreeNode> sourceExtension = new TreeGridDragSource<>(vTree.getCompositionRoot());
        sourceExtension.setDragDataGenerator(TREE_ITEM_ID_TYPE, entityTreeNode -> entityTreeNode.getId().toString());

        com.vaadin.v7.ui.Table vTable = propertiesTable.unwrap(com.vaadin.v7.ui.Table.class);
        DropTargetExtension<com.vaadin.v7.ui.Table> dropTargetExtension = new DropTargetExtension<>(vTable);

        Metadata metadata = AppBeans.get(Metadata.NAME);
        Messages messages = AppBeans.get(Messages.NAME);
        dropTargetExtension.addDropListener(event -> {
            String items = event.getDataTransferData(TREE_ITEM_ID_TYPE).isPresent() ?
                    event.getDataTransferData(TREE_ITEM_ID_TYPE).get() : null;
            if (items == null) {
                return;
            }

            String[] itemIds = items.split("\n");
            List<EntityTreeNode> treeNodes = new ArrayList<>();

            TreeItems<EntityTreeNode> treeItems = entityTree.getItems();
            for (String id : itemIds) {
                treeNodes.add(treeItems.getItem(UUID.fromString(id)));
            }

            Set<Object> tableItems = reportRegionPropertiesTableDs.getItems().stream()
                    .map(regionProperty -> regionProperty.getEntityTreeNode().getId())
                    .collect(Collectors.toSet());

            boolean alreadyAdded = false;
            List<RegionProperty> addedProperties = new ArrayList<>();
            for (int i = 0; i < treeNodes.size(); i++) {
                EntityTreeNode treeNode = treeNodes.get(i);
                // if it is entity property
                if (treeNode.getWrappedMetaClass() != null) {
                    continue;
                }

                if (!tableItems.contains(treeNode.getId())) {
                    RegionProperty regionProperty = metadata.create(RegionProperty.class);
                    regionProperty.setEntityTreeNode(treeNode);
                    regionProperty.setOrderNum((long) reportRegionPropertiesTableDs.getItemIds().size() + 1);

                    addedProperties.add(regionProperty);

                    //first element must be not zero cause later we do sorting by multiplying that values
                    reportRegionPropertiesTableDs.addItem(regionProperty);
                } else {
                    alreadyAdded = true;
                }
            }

            if (addedProperties.isEmpty()) {
                ScreenContext screenContext = UiControllerUtils.getScreenContext(entityTree.getFrame().getFrameOwner());
                Notifications notifications = screenContext.getNotifications();
                if (alreadyAdded) {
                    notifications.create(Notifications.NotificationType.TRAY)
                            .withCaption(messages.getMessage(RegionEditor.class, "elementsAlreadyAdded"))
                            .show();
                } else if (entityTree.getSelected().size() != 0) {
                    notifications.create(Notifications.NotificationType.HUMANIZED)
                            .withCaption(messages.getMessage(RegionEditor.class, "selectPropertyFromEntity"))
                            .show();
                } else {
                    notifications.create(Notifications.NotificationType.TRAY)
                            .withCaption(messages.getMessage(RegionEditor.class, "elementsWasNotAdded"))
                            .show();
                }
            } else {
                propertiesTable.setSelected(addedProperties);
            }

            (propertiesTable.unwrap(com.vaadin.v7.ui.Table.class)).setCurrentPageFirstItemId(vTable.lastItemId());
        });
    }
}
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

import io.jmix.core.Entity;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.reports.entity.wizard.RegionProperty;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.reportsui.action.list.OrderableItemMoveAction;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.CollectionUtils;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@UiController("report_Region.edit")
@UiDescriptor("region-edit.xml")
public class RegionEditor extends StandardEditor<ReportRegion> {
    //    @Named("entityTreeFrame.reportEntityTreeNodeDs")
//    protected AbstractTreeDatasource reportEntityTreeNodeDs;
    @Autowired
    protected CollectionContainer<RegionProperty> reportRegionPropertiesTableDc;
    @Named("entityTreeFrame.entityTree")
    protected Tree<EntityTreeNode> entityTree;
    @Named("entityTreeFrame.reportPropertyName")
    protected TextField<String> reportPropertyName;
    @Named("entityTreeFrame.reportPropertyNameSearchButton")
    protected Button reportPropertyNameSearchButton;
    @Autowired
    protected InstanceContainer<ReportRegion> reportRegionDc;
    @Autowired
    protected Button addItem;
    @Autowired
    protected Button removeItem;
    @Autowired
    protected Button upItem;
    @Autowired
    protected Button downItem;
    @Autowired
    protected Table<RegionProperty> propertiesTable;
    @Autowired
    protected Label<String> tipLabel;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected Messages messages;
    @Autowired
    protected Notifications notifications;

    protected boolean isTabulated;//if true then user perform add tabulated region action
    protected boolean asViewEditor;
    protected EntityTreeNode rootNode;
    protected boolean updatePermission;

    public void setTabulated(boolean tabulated) {
        isTabulated = tabulated;
    }

    public void setAsViewEditor(boolean asViewEditor) {
        this.asViewEditor = asViewEditor;
    }

    public void setUpdatePermission(boolean updatePermission) {
        this.updatePermission = updatePermission;
    }

    public void setRootNode(EntityTreeNode rootNode) {
        this.rootNode = rootNode;
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        //params.put("component$reportPropertyName", reportPropertyName);
        //todo
        //reportEntityTreeNodeDs.refresh(params);
        //TODO add disallowing of classes selection in tree
        if (!asViewEditor) {
            if (isTabulated) {
                setTabulatedRegionEditorCaption(rootNode.getName());
            } else {
                setSimpleRegionEditorCaption();
            }
        }
        String group = isTabulated
                ? "selectEntityPropertiesForTableArea"
                : "selectEntityProperties";
        tipLabel.setValue(messages.formatMessage(group, rootNode.getLocalizedName()));
        tipLabel.setHtmlEnabled(true);
        initComponents();
    }

    @Install(to = "addItemAction", subject = "enabledRule")
    private boolean addItemActionEnabledRule() {
        return isUpdatePermitted();
    }

    @Subscribe("addItemAction")
    public void onAddItemAction(Action.ActionPerformedEvent event) {
        @SuppressWarnings("unchecked")
        List<EntityTreeNode> nodesList = CollectionUtils.transform(
                reportRegionPropertiesTableDc.getItems(), o -> ((RegionProperty) o).getEntityTreeNode());

        Set<EntityTreeNode> alreadyAddedNodes = new HashSet<>(nodesList);

        Set<EntityTreeNode> selectedItems = entityTree.getSelected();
        List<RegionProperty> addedItems = new ArrayList<>();
        boolean alreadyAdded = false;
        for (EntityTreeNode entityTreeNode : selectedItems) {
            if (entityTreeNode.getWrappedMetaClass() != null) {
                continue;
            }
            if (!alreadyAddedNodes.contains(entityTreeNode)) {
                RegionProperty regionProperty = metadata.create(RegionProperty.class);
                regionProperty.setEntityTreeNode(entityTreeNode);
                regionProperty.setOrderNum((long) reportRegionPropertiesTableDc.getItems().size() + 1); //first element must be not zero cause later we do sorting by multiplying that values
                reportRegionPropertiesTableDc.getMutableItems().add(regionProperty);
                addedItems.add(regionProperty);
            } else {
                alreadyAdded = true;
            }
        }
        if (addedItems.isEmpty()) {
            if (alreadyAdded) {
                notifications.create(Notifications.NotificationType.TRAY)
                        .withCaption(messages.getMessage("elementsAlreadyAdded"))
                        .show();
            } else if (selectedItems.size() != 0) {
                notifications.create(Notifications.NotificationType.HUMANIZED)
                        .withCaption(messages.getMessage("selectPropertyFromEntity"))
                        .show();
            } else {
                notifications.create(Notifications.NotificationType.TRAY)
                        .withCaption(messages.getMessage("elementsWasNotAdded"))
                        .show();
            }
        } else {
            propertiesTable.setSelected(addedItems);
        }
    }

    @Install(to = "removeItemAction", subject = "enabledRule")
    private boolean removeItemActionEnabledRule() {
        return isUpdatePermitted();
    }

    @Subscribe("removeItemAction")
    public void onRemoveItemAction(Action.ActionPerformedEvent event) {
        for (Entity item : propertiesTable.getSelected()) {
            reportRegionPropertiesTableDc.getMutableItems().remove(item);
            normalizeRegionPropertiesOrderNum();
        }
    }

    protected void initComponents() {
        initControlBtnsActions();
        addRegionPropertiesDsListener();

        if (asViewEditor) {
            initAsViewEditor();
        }
        entityTree.setSelectionMode(Tree.SelectionMode.MULTI);
        entityTree.expand(rootNode);
    }

    protected void initAsViewEditor() {
        if (isTabulated) {
            getWindow().setCaption(messages.getMessage("singleEntityDataSetViewEditor"));
        } else {
            getWindow().setCaption(messages.getMessage("multiEntityDataSetViewEditor"));
        }
    }

    protected boolean isUpdatePermitted() {
        return updatePermission;
    }

    protected void setTabulatedRegionEditorCaption(String collectionEntityName) {
        getWindow().setCaption(messages.getMessage("tabulatedRegionEditor"));
    }

    protected void setSimpleRegionEditorCaption() {
        getWindow().setCaption(messages.getMessage("simpleRegionEditor"));
    }

    protected void addRegionPropertiesDsListener() {
        reportRegionPropertiesTableDc.addItemChangeListener(e -> showOrHideSortBtns());
        reportRegionPropertiesTableDc.addCollectionChangeListener(e -> showOrHideSortBtns());
    }

    protected void showOrHideSortBtns() {
        if (propertiesTable.getSelected().size() == reportRegionPropertiesTableDc.getItems().size() ||
                propertiesTable.getSelected().size() == 0) {
            upItem.setEnabled(false);
            downItem.setEnabled(false);
        } else {
            upItem.setEnabled(isUpdatePermitted());
            downItem.setEnabled(isUpdatePermitted());
        }
    }


    protected void initControlBtnsActions() {
        upItem.setAction(new OrderableItemMoveAction<Table<RegionProperty>, RegionProperty>("upItem", OrderableItemMoveAction.Direction.UP, propertiesTable) {
            @Override
            public boolean isEnabled() {
                return super.isEnabled() && isUpdatePermitted();
            }
        });
        downItem.setAction(new OrderableItemMoveAction<Table<RegionProperty>, RegionProperty>("downItem", OrderableItemMoveAction.Direction.DOWN, propertiesTable) {
            @Override
            public boolean isEnabled() {
                return super.isEnabled() && isUpdatePermitted();
            }
        });
    }

    protected void normalizeRegionPropertiesOrderNum() {
        long normalizedIdx = 0;
        List<RegionProperty> allItems = new ArrayList<>(reportRegionPropertiesTableDc.getItems());
        for (RegionProperty item : allItems) {
            item.setOrderNum(++normalizedIdx); //first must to be 1
        }
    }

    @Subscribe
    protected void onBeforeCommit(BeforeCommitChangesEvent event) {
        if (reportRegionPropertiesTableDc.getItems().isEmpty()) {
            notifications.create(Notifications.NotificationType.TRAY)
                    .withCaption(messages.getMessage("selectAtLeastOneProp"))
                    .show();
            event.preventCommit();
        }
    }
}
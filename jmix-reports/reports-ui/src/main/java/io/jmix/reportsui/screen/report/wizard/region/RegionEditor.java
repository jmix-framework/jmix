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

package io.jmix.reportsui.screen.report.wizard.region;

import io.jmix.core.Metadata;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.reports.entity.wizard.RegionProperty;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.ui.Actions;
import io.jmix.ui.Notifications;
import io.jmix.ui.WindowParam;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.action.ItemTrackingAction;
import io.jmix.ui.action.ListAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.Tree;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.CollectionUtils;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@UiController("report_Region.edit")
@UiDescriptor("region-edit.xml")
@EditedEntityContainer("reportRegionDc")
public class RegionEditor extends StandardEditor<ReportRegion> {

    @Autowired
    protected CollectionContainer<RegionProperty> reportRegionPropertiesTableDc;
    @Named("entityTreeFragment.entityTree")
    protected Tree<EntityTreeNode> entityTree;
    @Autowired
    protected Button addItem;
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
    protected MessageBundle messageBundle;
    @Autowired
    protected Notifications notifications;
    @Autowired
    private Actions actions;
    @WindowParam
    protected EntityTreeNode rootEntity;

    protected boolean isTabulated;//if true then user perform add tabulated region action
    protected boolean asFetchPlanEditor;

    protected boolean updatePermission = true;

    public void setAsFetchPlanEditor(boolean asFetchPlanEditor) {
        this.asFetchPlanEditor = asFetchPlanEditor;
    }

    public void setUpdatePermission(boolean updatePermission) {
        this.updatePermission = updatePermission;
    }

    public void setRootEntity(EntityTreeNode rootEntity) {
        this.rootEntity = rootEntity;
    }

    public EntityTreeNode getRootEntity() {
        return rootEntity;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        isTabulated = BooleanUtils.toBoolean(getEditedEntity().getIsTabulatedRegion());

        if (!asFetchPlanEditor) {
            if (isTabulated) {
                setTabulatedRegionEditorCaption(rootEntity.getName());
            } else {
                setSimpleRegionEditorCaption();
            }
        }
        String messageKey = isTabulated
                ? "selectEntityPropertiesForTableArea"
                : "selectEntityProperties";
        tipLabel.setValue(messageBundle.formatMessage(messageKey, rootEntity.getLocalizedName()));
        tipLabel.setHtmlEnabled(true);
        initComponents();
    }

    protected void initComponents() {
        if (asFetchPlanEditor) {
            initAsFetchPlanEditor();
        }

        initEntityTree();
    }

    protected void initEntityTree() {
        entityTree.setSelectionMode(Tree.SelectionMode.MULTI);

        BaseAction doubleClickAction = new BaseAction("doubleClick")
                .withHandler(event -> addProperty());
        doubleClickAction.addEnabledRule(this::isUpdatePermitted);
        entityTree.setItemClickAction(doubleClickAction);

        ListAction addPropertyAction = actions.create(ItemTrackingAction.class, "addItemAction")
                .withHandler(event -> addProperty());
        addPropertyAction.addEnabledRule(this::isUpdatePermitted);
        entityTree.addAction(addPropertyAction);
        addItem.setAction(addPropertyAction);
    }

    protected void addProperty() {
        @SuppressWarnings("unchecked")
        List<EntityTreeNode> nodesList = CollectionUtils.transform(
                reportRegionPropertiesTableDc.getItems(), o -> ((RegionProperty) o).getEntityTreeNode());

        Set<EntityTreeNode> alreadyAddedNodes = new HashSet<>(nodesList);

        Set<EntityTreeNode> selectedItems = entityTree.getSelected();
        List<RegionProperty> addedItems = new ArrayList<>();
        boolean alreadyAdded = false;
        for (EntityTreeNode entityTreeNode : selectedItems) {
            if (entityTreeNode.getMetaClassName() != null) {
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
                        .withCaption(messageBundle.getMessage("elementsAlreadyAdded"))
                        .show();
            } else if (selectedItems.size() != 0) {
                notifications.create(Notifications.NotificationType.HUMANIZED)
                        .withCaption(messageBundle.getMessage("selectPropertyFromEntity"))
                        .show();
            } else {
                notifications.create(Notifications.NotificationType.TRAY)
                        .withCaption(messageBundle.getMessage("elementsWasNotAdded"))
                        .show();
            }
        } else {
            propertiesTable.setSelected(addedItems);
        }
    }

    protected void initAsFetchPlanEditor() {
        if (isTabulated) {
            getWindow().setCaption(messageBundle.getMessage("singleEntityDataSetFetchPlanEditor"));
        } else {
            getWindow().setCaption(messageBundle.getMessage("multiEntityDataSetFetchPlanEditor"));
        }
    }

    protected boolean isUpdatePermitted() {
        return updatePermission;
    }

    protected void setTabulatedRegionEditorCaption(String collectionEntityName) {
        getWindow().setCaption(messageBundle.getMessage("tabulatedRegionEditor"));
    }

    protected void setSimpleRegionEditorCaption() {
        getWindow().setCaption(messageBundle.getMessage("simpleRegionEditor"));
    }

    @Install(to = "propertiesTable.removeItemAction", subject = "enabledRule")
    private boolean removeItemActionEnabledRule() {
        return isUpdatePermitted();
    }

    @Subscribe("propertiesTable.removeItemAction")
    public void onRemoveItemAction(Action.ActionPerformedEvent event) {
        for (RegionProperty item : propertiesTable.getSelected()) {
            reportRegionPropertiesTableDc.getMutableItems().remove(item);
            normalizeRegionPropertiesOrderNum();
        }
    }

    @Subscribe(id = "reportRegionPropertiesTableDc", target = Target.DATA_CONTAINER)
    public void onReportRegionPropertiesTableDcCollectionChange(CollectionContainer.CollectionChangeEvent<RegionProperty> event) {
        showOrHideSortBtns();
    }

    @Subscribe(id = "reportRegionPropertiesTableDc", target = Target.DATA_CONTAINER)
    public void onReportRegionPropertiesTableDcItemChange(InstanceContainer.ItemChangeEvent<RegionProperty> event) {
        showOrHideSortBtns();
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

    @Install(to = "propertiesTable.upItemAction", subject = "enabledRule")
    protected boolean propertiesTableUpItemActionEnabledRule() {
        return isUpdatePermitted();
    }

    @Install(to = "propertiesTable.downItemAction", subject = "enabledRule")
    protected boolean propertiesTableDownItemActionEnabledRule() {
        return isUpdatePermitted();
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
                    .withCaption(messageBundle.getMessage("selectAtLeastOneProp"))
                    .show();
            event.preventCommit();
        }
    }
}
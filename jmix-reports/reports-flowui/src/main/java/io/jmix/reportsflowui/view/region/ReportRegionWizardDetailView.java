package io.jmix.reportsflowui.view.region;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Route;
import io.jmix.core.Metadata;
import io.jmix.flowui.Actions;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.list.ItemTrackingAction;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.reports.entity.wizard.RegionProperty;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.reportsflowui.view.reportwizard.EntityTreeComposite;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Route(value = "reports/region/:id", layout = DefaultMainViewParent.class)
@ViewController("report_WizardReportRegion.detail")
@ViewDescriptor("report-region-wizard-detail-view.xml")
@EditedEntityContainer("reportRegionDc")
public class ReportRegionWizardDetailView extends StandardDetailView<ReportRegion> {

    @ViewComponent
    protected Label tipLabel;
    @ViewComponent
    protected JmixButton upItem;
    @ViewComponent
    protected JmixButton downItem;
    @ViewComponent
    protected JmixButton addItem;
    @ViewComponent
    protected CollectionPropertyContainer<RegionProperty> reportRegionPropertiesDataGridDc;
    @ViewComponent
    protected FormLayout treePanel;
    @ViewComponent
    protected DataGrid<RegionProperty> propertiesDataGrid;

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Actions actions;
    @Autowired
    protected Metadata metadata;

    protected TreeDataGrid<EntityTreeNode> entityTree;
    protected EntityTreeNode rootEntity;
    protected boolean scalarOnly = false;
    protected boolean collectionsOnly = false;
    protected boolean persistentOnly = false;
    protected boolean isTabulated;//if true then user perform add tabulated region action
    protected boolean updatePermission = true;

    public void setParameters(EntityTreeNode rootEntity, boolean scalarOnly, boolean collectionsOnly, boolean persistentOnly) {
        this.rootEntity = rootEntity;
        this.scalarOnly = scalarOnly;
        this.collectionsOnly = collectionsOnly;
        this.persistentOnly = persistentOnly;
        initTipLabel();
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        initComponents();
    }

    @Install(to = "propertiesDataGrid.upItemAction", subject = "enabledRule")
    protected boolean propertiesDataGridUpEnabledRule() {
        RegionProperty selectedItem = propertiesDataGrid.getSingleSelectedItem();
        return selectedItem != null && selectedItem.getOrderNum() > 1 && isUpdatePermitted();
    }

    @Install(to = "propertiesDataGrid.downItemAction", subject = "enabledRule")
    protected boolean propertiesDataGridDownEnabledRule() {
        RegionProperty selectedItem = propertiesDataGrid.getSingleSelectedItem();
        return selectedItem != null && selectedItem.getOrderNum() < reportRegionPropertiesDataGridDc.getItems().size() && isUpdatePermitted();
    }

    @Subscribe("propertiesDataGrid.upItemAction")
    protected void onPropertiesDataGridUp(ActionPerformedEvent event) {
        swapItems(true);
    }

    @Subscribe("propertiesDataGrid.downItemAction")
    protected void onPropertiesDataGridDown(ActionPerformedEvent event) {
        swapItems(false);
    }

    protected void swapItems(boolean up) {
        if (propertiesDataGrid.getSingleSelectedItem() != null) {
            List<RegionProperty> items = reportRegionPropertiesDataGridDc.getMutableItems();
            RegionProperty currentItem = propertiesDataGrid.getSingleSelectedItem();
            if ((up && currentItem.getOrderNum() != 1) ||
                    (!up && currentItem.getOrderNum() != items.size())) {
                RegionProperty itemToSwap = IterableUtils.find(items,
                        e -> e.getOrderNum().equals(currentItem.getOrderNum() - (up ? 1 : -1)));
                long currentPosition = currentItem.getOrderNum();

                currentItem.setOrderNum(itemToSwap.getOrderNum());
                itemToSwap.setOrderNum(currentPosition);

                Collections.swap(items,
                        currentItem.getOrderNum().intValue() - 1, itemToSwap.getOrderNum().intValue() - 1);

            }
        }
    }

    protected void initTipLabel() {
        String messageKey = isTabulated
                ? "selectEntityPropertiesForTableArea"
                : "selectEntityProperties";
        tipLabel.setText(messageBundle.formatMessage(messageKey, rootEntity.getLocalizedName()));
    }

    protected void initComponents() {
        initEntityTree();
    }

    protected void initEntityTree() {
        EntityTreeComposite entityTreeComposite = uiComponents.create(EntityTreeComposite.class);
        entityTreeComposite.setVisible(true);
        entityTreeComposite.setParameters(rootEntity, scalarOnly, collectionsOnly, persistentOnly);

        entityTree = entityTreeComposite.getEntityTree();
        entityTree.expand(rootEntity);

        BaseAction doubleClickAction = new BaseAction("doubleClick")
                .withHandler(event -> addProperty());
        doubleClickAction.setEnabled(isUpdatePermitted());
        entityTree.addAction(doubleClickAction);
        entityTree.addItemClickListener(event -> {
            if (event.getClickCount() > 1) {
                entityTree.select(event.getItem());
                addProperty();
            }
        });

        ListDataComponentAction addPropertyAction = actions.create(ItemTrackingAction.ID, "addItemAction");
        addPropertyAction.addActionPerformedListener(event -> addProperty());
        addPropertyAction.addEnabledRule(this::isUpdatePermitted);
        entityTree.addAction(addPropertyAction);
        addItem.setAction(addPropertyAction);
        addItem.setIcon(VaadinIcon.ARROW_RIGHT.create());
        treePanel.add(entityTreeComposite);
    }

    protected void addProperty() {
        List<EntityTreeNode> nodesList = reportRegionPropertiesDataGridDc.getItems()
                .stream()
                .map(RegionProperty::getEntityTreeNode).toList();

        Set<EntityTreeNode> alreadyAddedNodes = new HashSet<>(nodesList);

        Set<EntityTreeNode> selectedItems = entityTree.getSelectedItems();
        List<RegionProperty> addedItems = new ArrayList<>();
        boolean alreadyAdded = false;
        for (EntityTreeNode entityTreeNode : selectedItems) {
            if (entityTreeNode.getMetaClassName() != null) {
                continue;
            }
            if (!alreadyAddedNodes.contains(entityTreeNode)) {
                RegionProperty regionProperty = metadata.create(RegionProperty.class);
                regionProperty.setEntityTreeNode(entityTreeNode);
                regionProperty.setOrderNum((long) reportRegionPropertiesDataGridDc.getItems().size() + 1);
                //first element must be not zero cause later we do sorting by multiplying that values
                reportRegionPropertiesDataGridDc.getMutableItems().add(regionProperty);
                addedItems.add(regionProperty);
            } else {
                alreadyAdded = true;
            }
        }
        if (addedItems.isEmpty()) {
            if (alreadyAdded) {
                notifications.create(messageBundle.getMessage("elementsAlreadyAdded"))
                        .show();
            } else if (selectedItems.size() != 0) {
                notifications.create(messageBundle.getMessage("selectPropertyFromEntity"))
                        .show();
            } else {
                notifications.create(messageBundle.getMessage("elementsWasNotAdded"))
                        .show();
            }
        } else {
            propertiesDataGrid.select(addedItems);
        }
    }

    @Install(to = "propertiesDataGrid.removeItemAction", subject = "enabledRule")
    protected boolean propertiesDataGridRemoveItemActionEnabledRule() {
        return isUpdatePermitted();
    }

    protected boolean isUpdatePermitted() {
        return updatePermission;
    }

    @Subscribe("propertiesDataGrid.removeItemAction")
    public void onpropertiesDataGridRemoveItemAction(ActionPerformedEvent event) {
        for (RegionProperty item : propertiesDataGrid.getSelectedItems()) {
            reportRegionPropertiesDataGridDc.getMutableItems().remove(item);
            normalizeRegionPropertiesOrderNum();
        }
    }

    protected void normalizeRegionPropertiesOrderNum() {
        long normalizedIdx = 0;
        List<RegionProperty> allItems = reportRegionPropertiesDataGridDc.getMutableItems();
        for (RegionProperty item : allItems) {
            item.setOrderNum(++normalizedIdx); //first must be 1
        }
    }

    @Subscribe(id = "reportRegionPropertiesDataGridDc", target = Target.DATA_CONTAINER)
    public void onReportRegionPropertiesDataGridDcCollectionChange(CollectionContainer.CollectionChangeEvent<ReportRegion> event) {
        showOrHideSortBtns();
    }

    @Subscribe(id = "reportRegionPropertiesDataGridDc", target = Target.DATA_CONTAINER)
    public void onReportRegionPropertiesDataGridDcItemChange(InstanceContainer.ItemChangeEvent<ReportRegion> event) {
        showOrHideSortBtns();
    }

    protected void showOrHideSortBtns() {
        if (propertiesDataGrid.getSelectedItems().size() == reportRegionPropertiesDataGridDc.getItems().size() ||
                propertiesDataGrid.getSelectedItems().size() == 0) {
            upItem.setEnabled(false);
            downItem.setEnabled(false);
        } else {
            upItem.setEnabled(propertiesDataGridUpEnabledRule());
            downItem.setEnabled(propertiesDataGridDownEnabledRule());
        }
    }

    @Subscribe
    public void onBeforeSave(BeforeSaveEvent event) {
        if (reportRegionPropertiesDataGridDc.getItems().isEmpty()) {
            notifications.create(messageBundle.getMessage("selectAtLeastOneProp"))
                    .show();
            event.preventSave();
        }
    }
}
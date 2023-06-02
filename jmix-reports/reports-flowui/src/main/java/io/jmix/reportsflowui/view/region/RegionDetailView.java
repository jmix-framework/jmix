package io.jmix.reportsflowui.view.region;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
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
import io.jmix.reportsflowui.view.EntityTreeFragment;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Route(value = "region/:id", layout = DefaultMainViewParent.class)
@ViewController("report_WizardReportRegion.detail")
@ViewDescriptor("region-detail-view.xml")
@EditedEntityContainer("reportRegionDc")
public class RegionDetailView extends StandardDetailView<ReportRegion> {

    protected boolean isTabulated;//if true then user perform add tabulated region action
    protected boolean asFetchPlanEditor;
    protected boolean updatePermission = true;

    @ViewComponent
    private DataGrid<RegionProperty> propertiesTable;
    @ViewComponent
    private JmixButton upItem;
    @ViewComponent
    private JmixButton downItem;
    @ViewComponent
    private JmixButton addItem;
    @ViewComponent
    private Label tipLabel;
    @ViewComponent
    private FormLayout treePanel;

    @ViewComponent
    private CollectionPropertyContainer<RegionProperty> reportRegionPropertiesTableDc;

    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Messages messages;
    @Autowired
    private Metadata metadata;
    @Autowired
    private Actions actions;

    protected TreeDataGrid<EntityTreeNode> entityTree;
    protected EntityTreeNode rootEntity;
    protected boolean scalarOnly = false;
    protected boolean collectionsOnly = false;
    protected boolean persistentOnly = false;

    public void setParameters(EntityTreeNode rootEntity, boolean scalarOnly, boolean collectionsOnly, boolean persistentOnly) {
        this.rootEntity = rootEntity;
        this.scalarOnly = scalarOnly;
        this.collectionsOnly = collectionsOnly;
        this.persistentOnly = persistentOnly;
        initTipLabel();
    }

    @Subscribe("propertiesTable.upItemAction")
    protected void onPropertiesTableUp(ActionPerformedEvent event) {
        replaceParameters(true);
    }

    @Subscribe("propertiesTable.downItemAction")
    protected void onPropertiesTableDown(ActionPerformedEvent event) {
        replaceParameters(false);
    }

    protected void replaceParameters(boolean up) {
        if (propertiesTable.getSingleSelectedItem() != null) {
            List<RegionProperty> items = reportRegionPropertiesTableDc.getMutableItems();
            int currentPosition = items.indexOf(propertiesTable.getSingleSelectedItem());
            if ((up && currentPosition != 0)
                    || (!up && currentPosition != items.size() - 1)) {
                int itemToSwapPosition = currentPosition - (up ? 1 : -1);

                Collections.swap(items, itemToSwapPosition, currentPosition);
            }
        }
    }

    private void initTipLabel() {
        String messageKey = isTabulated
                ? "selectEntityPropertiesForTableArea"
                : "selectEntityProperties";
        tipLabel.setText(messages.formatMessage(messageKey, rootEntity.getLocalizedName()));
    }

    public void setContainer(JmixButton downItem) {
        this.downItem = downItem;
    }

    public void setAsFetchPlanEditor(boolean asFetchPlanEditor) {
        this.asFetchPlanEditor = asFetchPlanEditor;
    }

    public void setUpdatePermission(boolean updatePermission) {
        this.updatePermission = updatePermission;
    }

    public ReportRegion getReportRegionsItems() {
        return getEditedEntityContainer().getItem();
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
        initComponents();
    }

    protected void initComponents() {
        if (asFetchPlanEditor) {
            initAsFetchPlanEditor();
        }

        initEntityTree();
    }

    protected void initEntityTree() {
        EntityTreeFragment entityTreeFragment = uiComponents.create(EntityTreeFragment.class);
        entityTreeFragment.setVisible(true);
        entityTreeFragment.setParameters(rootEntity, scalarOnly, collectionsOnly, persistentOnly);

        entityTree = entityTreeFragment.getEntityTree();
        entityTree.expand(rootEntity);

        BaseAction doubleClickAction = new BaseAction("doubleClick")
                .withHandler(event -> addProperty());
        doubleClickAction.setEnabled(isUpdatePermitted());
        entityTree.addAction(doubleClickAction);
        //todo normal double click registration
        entityTree.addItemClickListener(event -> {
            if (event.getClickCount() > 1) {
                entityTree.select(event.getItem());
                addProperty();
            }
        });

        ListDataComponentAction addPropertyAction = actions.create(ItemTrackingAction.class, "addItemAction")
                .withHandler(event -> addProperty());
        addPropertyAction.addEnabledRule(this::isUpdatePermitted);
        entityTree.addAction(addPropertyAction);
        addItem.setAction(addPropertyAction);
        addItem.setIcon(VaadinIcon.ARROW_RIGHT.create());
        treePanel.add(entityTreeFragment);
    }

    protected void addProperty() {
        @SuppressWarnings("unchecked")
        List<EntityTreeNode> nodesList = reportRegionPropertiesTableDc.getItems()
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
                regionProperty.setOrderNum((long) reportRegionPropertiesTableDc.getItems().size() + 1); //first element must be not zero cause later we do sorting by multiplying that values
                reportRegionPropertiesTableDc.getMutableItems().add(regionProperty);
                addedItems.add(regionProperty);
            } else {
                alreadyAdded = true;
            }
        }
        if (addedItems.isEmpty()) {
            if (alreadyAdded) {
                notifications.create(messages.getMessage(getClass(), "elementsAlreadyAdded"))
                        .withType(Notifications.Type.DEFAULT)
                        .show();
            } else if (selectedItems.size() != 0) {
                notifications.create(messages.getMessage(getClass(), "selectPropertyFromEntity"))
                        .withType(Notifications.Type.DEFAULT)
                        .show();
            } else {
                notifications.create(messages.getMessage(getClass(), "elementsWasNotAdded"))
                        .withType(Notifications.Type.DEFAULT)
                        .show();
            }
        } else {
            propertiesTable.select(addedItems);
        }
    }

    protected void initAsFetchPlanEditor() {
        //todo
//        if (isTabulated) {
//            getWindow().setCaption(messages.getMessage(getClass(), "singleEntityDataSetFetchPlanEditor"));
//        } else {
//            getWindow().setCaption(messages.getMessage(getClass(), "multiEntityDataSetFetchPlanEditor"));
//        }
    }

    protected boolean isUpdatePermitted() {
        return updatePermission;
    }

    protected void setTabulatedRegionEditorCaption(String collectionEntityName) {
        //todo
//        getWindow().setCaption(messages.getMessage(getClass(), "tabulatedRegionEditor"));
    }

    protected void setSimpleRegionEditorCaption() {
        //todo
//        getWindow().setCaption(messages.getMessage(getClass(), "simpleRegionEditor"));
    }

    @Install(to = "propertiesTable.removeItemAction", subject = "enabledRule")
    private boolean propertiesTableRemoveItemActionEnabledRule() {
        return isUpdatePermitted();
    }

    @Subscribe("propertiesTable.removeItemAction")
    public void onPropertiesTableRemoveItemAction(ActionPerformedEvent event) {
        for (RegionProperty item : propertiesTable.getSelectedItems()) {
            reportRegionPropertiesTableDc.getMutableItems().remove(item);
            normalizeRegionPropertiesOrderNum();
        }
    }

    @Subscribe(id = "reportRegionPropertiesTableDc", target = Target.DATA_CONTAINER)
    public void onReportRegionPropertiesTableDcCollectionChange(CollectionContainer.CollectionChangeEvent<ReportRegion> event) {
        showOrHideSortBtns();
    }

    @Subscribe(id = "reportRegionPropertiesTableDc", target = Target.DATA_CONTAINER)
    public void onReportRegionPropertiesTableDcItemChange(InstanceContainer.ItemChangeEvent<ReportRegion> event) {
        showOrHideSortBtns();
    }

    protected void showOrHideSortBtns() {
        if (propertiesTable.getSelectedItems().size() == reportRegionPropertiesTableDc.getItems().size() ||
                propertiesTable.getSelectedItems().size() == 0) {
            upItem.setEnabled(false);
            downItem.setEnabled(false);
        } else {
            upItem.setEnabled(isUpdatePermitted());
            downItem.setEnabled(isUpdatePermitted());
        }
    }

    @Install(to = "propertiesTable.upItemAction", subject = "enabledRule")
    private boolean propertiesTableUpItemActionEnabledRule() {
        return isUpdatePermitted();
    }

    @Install(to = "propertiesTable.downItemAction", subject = "enabledRule")
    private boolean propertiesTableDownItemActionEnabledRule() {
        return isUpdatePermitted();
    }


    protected void normalizeRegionPropertiesOrderNum() {
        long normalizedIdx = 0;
        List<RegionProperty> allItems = new ArrayList<>(reportRegionPropertiesTableDc.getItems());
        for (RegionProperty item : allItems) {
            item.setOrderNum(++normalizedIdx); //first must be 1
        }
    }

    @Subscribe
    public void onBeforeSave(BeforeSaveEvent event) {
        if (reportRegionPropertiesTableDc.getItems().isEmpty()) {
            notifications.create(messages.getMessage("selectAtLeastOneProp"))
                    .withType(Notifications.Type.DEFAULT)
                    .show();
            event.preventSave();
        }
    }


}
/*
 * Copyright 2020 Haulmont.
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

package io.jmix.auditflowui.view.entitylog;

import com.google.common.io.Files;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;
import io.jmix.audit.EntityLog;
import io.jmix.audit.entity.EntityLogAttr;
import io.jmix.audit.entity.EntityLogItem;
import io.jmix.audit.entity.LoggedAttribute;
import io.jmix.audit.entity.LoggedEntity;
import io.jmix.core.*;
import io.jmix.core.annotation.TenantId;
import io.jmix.core.metamodel.datatype.EnumClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.UserRepository;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.timepicker.TypedTimePicker;
import io.jmix.flowui.component.upload.FileUploadField;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.upload.event.FileUploadSucceededEvent;
import io.jmix.flowui.kit.component.valuepicker.ValuePicker;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.view.*;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import org.springframework.lang.Nullable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.flowui.download.DownloadFormat.JSON;
import static io.jmix.flowui.download.DownloadFormat.ZIP;

@Route(value = "audit/entitylog", layout = DefaultMainViewParent.class)
@ViewController("entityLog.view")
@ViewDescriptor("entity-log-view.xml")
@LookupComponent("entityLogTable")
@DialogMode(width = "80em", height = "60em", resizable = true)
public class EntityLogView extends StandardListView<EntityLogItem> {
    private static final Logger log = LoggerFactory.getLogger(EntityLogView.class);

    @ViewComponent
    protected CollectionContainer<LoggedEntity> loggedEntityDc;
    @ViewComponent
    protected CollectionLoader<LoggedEntity> loggedEntityDl;
    @ViewComponent
    protected CollectionLoader<EntityLogItem> entityLogDl;
    @ViewComponent
    protected CollectionLoader<LoggedAttribute> loggedAttrDl;
    @ViewComponent
    protected JmixSelect<String> changeTypeField;
    @ViewComponent
    protected ComboBox<String> entityNameField;
    @ViewComponent
    protected ComboBox<String> userField;
    @ViewComponent
    protected ComboBox<String> filterEntityNameField;
    @ViewComponent
    protected ValuePicker<Object> instancePicker;
    @ViewComponent
    protected DataGrid<EntityLogItem> entityLogTable;
    @ViewComponent
    protected DataGrid<LoggedEntity> loggedEntityTable;
    @ViewComponent
    protected DataGrid<EntityLogAttr> entityLogAttrTable;
    @ViewComponent
    protected HorizontalLayout actionsPaneLayout;
    @ViewComponent
    protected TypedDatePicker<LocalDate> tillDateField;
    @ViewComponent
    protected TypedTimePicker<LocalTime> tillTimeField;
    @ViewComponent
    protected TypedDatePicker<LocalDate> fromDateField;
    @ViewComponent
    protected TypedTimePicker<LocalTime> fromTimeField;
    @ViewComponent
    protected Button cancelBtn;
    @ViewComponent
    protected FileUploadField importField;
    @ViewComponent
    protected Checkbox selectAllCheckBox;
    @ViewComponent
    protected CollectionContainer<EntityLogAttr> entityLogAttrDc;
    @ViewComponent
    protected VerticalLayout loggedEntityTableBox;
    @ViewComponent
    protected Tabs tabsheet;
    @ViewComponent
    protected VerticalLayout viewWrapper;
    @ViewComponent
    protected FormLayout setupWrapper;
    @ViewComponent
    protected VerticalLayout loggedEntityMiscBox;
    @ViewComponent
    protected CheckboxGroup<String> attributesCheckboxGroup;

    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected DialogWindows dialogBuilders;
    @Autowired
    protected EntityImportExport entityImportExport;
    @Autowired
    protected EntityImportPlans entityImportPlans;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected SecureOperations secureOperations;
    @Autowired
    protected Messages messages;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected TimeSource timeSource;
    @Autowired
    protected ReferenceToEntitySupport referenceToEntitySupport;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected EntityLog entityLog;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected PolicyStore policyStore;


    protected Object selectedEntity;

    // allow or not selectAllCheckBox to change values of other checkboxes
    protected boolean canSelectAllCheckboxGenerateEvents = true;

    protected void onSelectedTabChange(Tabs.SelectedChangeEvent event) {
        String tabId = event.getSelectedTab().getId()
                .orElse("<no_id>");

        switch (tabId) {
            case "view":
                viewWrapper.setVisible(true);
                setupWrapper.setVisible(false);
                break;
            case "setup":
                viewWrapper.setVisible(false);
                setupWrapper.setVisible(true);
                break;
            default:
                viewWrapper.setVisible(false);
                setupWrapper.setVisible(false);
        }
    }

    protected LocalDateTime getTillDateTime() {
        LocalDateTime tillDateTime = null;
        if (tillDateField.getTypedValue() != null || tillTimeField.getTypedValue() != null) {

            LocalDate afterDate = tillDateField.getTypedValue() != null ? tillDateField.getTypedValue() :
                    LocalDate.now();
            LocalTime afterTime = tillTimeField.getTypedValue() != null ? tillTimeField.getTypedValue() :
                    LocalTime.MAX;
            tillDateTime = LocalDateTime.of(afterDate, afterTime);
        }
        return tillDateTime;
    }

    protected LocalDateTime getFromDateTime() {
        LocalDateTime fromDateTime = null;
        if (fromDateField.getTypedValue() != null || fromTimeField.getTypedValue() != null) {
            LocalDate fromDate = fromDateField.getTypedValue() != null ? fromDateField.getTypedValue() :
                    LocalDate.now();
            LocalTime fromTime = fromTimeField.getTypedValue() != null ? fromTimeField.getTypedValue() :
                    LocalTime.MIN;
            fromDateTime = LocalDateTime.of(fromDate, fromTime);
        }
        return fromDateTime;
    }

    @Subscribe
    protected void onInit(View.InitEvent event) {
        tabsheet.addSelectedChangeListener(this::onSelectedTabChange);

        Map<String, String> changeTypeMap = new LinkedHashMap<>();
        changeTypeMap.put("C", messages.getMessage(EntityLogView.class, "createField"));
        changeTypeMap.put("M", messages.getMessage(EntityLogView.class, "modifyField"));
        changeTypeMap.put("D", messages.getMessage(EntityLogView.class, "deleteField"));
        changeTypeMap.put("R", messages.getMessage(EntityLogView.class, "restoreField"));

        Map<String, String> entityMetaClassesMap = getEntityMetaClasses();
        entityNameField.setItems(entityMetaClassesMap.values());
        ComponentUtils.setItemsMap(changeTypeField, changeTypeMap);

        userField.setItems(userRepository.getByUsernameLike("")
                .stream()
                .map(UserDetails::getUsername)
                .collect(Collectors.toList()));
        filterEntityNameField.setItems(entityMetaClassesMap.values());
        instancePicker.setFormatter(value -> value != null ? metadataTools.getInstanceName(value) : null);

        disableControls();
        setDateFieldTime();

        entityLogTable.addSelectionListener(this::onEntityLogTableSelect);
        loggedEntityTable.addSelectionListener(this::onLoggedEntityTableSelectEvent);
        entityLogTable.addColumn(this::generateEntityInstanceNameColumn
                ).setHeader(messages.getMessage(this.getClass(), "entity"))
                .setSortable(true);

        entityLogTable.addColumn(this::generateEntityIdColumn)
                .setHeader(messages.getMessage(this.getClass(), "entityId"))
                .setResizable(true)
                .setSortable(true);

        entityLogAttrTable.addColumn(entityLogAttr ->
                        evaluateEntityLogItemAttrDisplayValue(entityLogAttr, entityLogAttr.getOldValue()))
                .setHeader(messages.getMessage(this.getClass(), "oldValue"))
                .setResizable(true).setKey("oldValue").setSortable(true);
        entityLogAttrTable.addColumn(entityLogAttr ->
                        evaluateEntityLogItemAttrDisplayValue(entityLogAttr, entityLogAttr.getValue()))
                .setHeader(messages.getMessage(this.getClass(), "newValue"))
                .setResizable(true).setKey("newValue").setSortable(true);

        entityLogAttrTable.addColumn(this::generateAttributeColumn)
                .setHeader(messages.getMessage(this.getClass(), "attribute"))
                .setResizable(true).setKey("attribute").setSortable(true);

        List<Grid.Column<EntityLogAttr>> columnsOrder = new ArrayList<>(Arrays.asList(
                entityLogAttrTable.getColumnByKey("attribute"),
                entityLogAttrTable.getColumnByKey("newValue"),
                entityLogAttrTable.getColumnByKey("valueId"),
                entityLogAttrTable.getColumnByKey("oldValue"),
                entityLogAttrTable.getColumnByKey("oldValueId")
        ));
        entityLogAttrTable.setColumnOrder(columnsOrder);

        setupWrapper.setColspan(loggedEntityTableBox, 2);
    }

    protected Object generateAttributeColumn(EntityLogAttr entityLogAttr) {
        String entityName = entityLogAttr.getLogItem().getEntity();
        MetaClass metaClass = getClassFromEntityName(entityName);
        if (metaClass != null) {
            return messageTools.getPropertyCaption(metaClass, entityLogAttr.getName());
        } else {
            return entityLogAttr.getName();
        }
    }

    protected Object generateEntityInstanceNameColumn(EntityLogItem entityLogItem) {
        String entityName = evaluateEntityLogItemDisplayedEntityName(entityLogItem);
        if (entityName != null) {
            return entityName;
        }
        return null;
    }

    protected Object generateEntityIdColumn(EntityLogItem entityLogItem) {
        if (entityLogItem.getEntityRef().getObjectEntityId() != null) {
            return entityLogItem.getEntityRef().getObjectEntityId().toString();
        }
        return null;
    }

    protected String evaluateEntityLogItemDisplayedEntityName(EntityLogItem entityLogItem) {
        String entityName = entityLogItem.getEntity();
        MetaClass metaClass = getClassFromEntityName(entityName);
        if (metaClass != null) {
            return messageTools.getEntityCaption(metaClass);
        } else {
            return entityName;
        }
    }

    protected String evaluateEntityLogItemAttrDisplayValue(EntityLogAttr entityLogAttr, String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        final String entityName = entityLogAttr.getLogItem().getEntity();
        MetaClass metaClass = getClassFromEntityName(entityName);
        if (metaClass != null) {
            MetaProperty property = metaClass.findProperty(entityLogAttr.getName());
            if (property != null) {
                if (property.getRange().isDatatype()) {
                    return value;
                } else if (property.getRange().isEnum() && EnumClass.class.isAssignableFrom(property.getJavaType())) {
                    Enum en = getEnumById(property.getRange().asEnumeration().getValues(), value);
                    if (en != null) {
                        return messages.getMessage(en);
                    } else {
                        String nameKey = property.getRange().asEnumeration().getJavaClass().getSimpleName() + "." + value;
                        String packageName = property.getRange().asEnumeration().getJavaClass().getPackage().getName();
                        return messages.getMessage(packageName, nameKey);
                    }
                } else {
                    return value;
                }
            } else {
                return value;
            }
        } else {
            return value;
        }
    }

    @Nullable
    protected MetaClass getClassFromEntityName(String entityName) {
        MetaClass metaClass = metadata.findClass(entityName);
        return metaClass == null ? null : extendedEntities.getEffectiveMetaClass(metaClass);
    }

    protected Enum getEnumById(List<Enum> enums, String id) {
        for (Enum e : enums) {
            if (e instanceof EnumClass) {
                Object enumId = ((EnumClass) e).getId();
                if (id.equals(String.valueOf(enumId))) {
                    return e;
                }
            }
        }
        return null;
    }

    protected void onLoggedEntityTableSelectEvent(SelectionEvent<Grid<LoggedEntity>, LoggedEntity> event) {
        LoggedEntity entity = event.getFirstSelectedItem().orElse(null);
        if (entity != null) {
            loggedAttrDl.setParameter("entityId", entity.getId());
            loggedAttrDl.load();
            loggedEntityDc.setItem(entity);
            fillAttributes(entity.getName(), entity, false);
            selectAllCheckBox.setEnabled(true);
        } else {
            setSelectAllCheckBox(false);
            attributesCheckboxGroup.setItems();
            selectAllCheckBox.setEnabled(false);
        }
    }

    @Subscribe("filterEntityNameField")
    protected void onFilterEntityNameFieldComboBoxValueChange(AbstractField.ComponentValueChangeEvent<ComboBox<String>, String> event) {
        if (event.getValue() != null) {
            instancePicker.setEnabled(true);
        } else {
            instancePicker.setEnabled(false);
        }
        instancePicker.clear();
    }

    protected void onEntityLogTableSelect(SelectionEvent<Grid<EntityLogItem>, EntityLogItem> event1) {
        EntityLogItem entity = event1.getFirstSelectedItem().orElse(null);
        if (entity != null) {
            entityLogAttrDc.setItems(entity.getAttributes());
        } else {
            entityLogAttrDc.setItems(null);
        }
    }

    @Subscribe("attributesCheckboxGroup")
    protected void onAttributesCheckboxGroupValueChange(AbstractField.ComponentValueChangeEvent<CheckboxGroup<String>,
            Set<String>> event) {
        if (event.getValue().size() == event.getSource()
                .getListDataView().getItems().toArray().length) {

            selectAllCheckBox.setValue(true);
            selectAllCheckBox.setIndeterminate(false);
        } else if (event.getValue().size() == 0) {
            selectAllCheckBox.setValue(false);
            selectAllCheckBox.setIndeterminate(false);
        } else {
            selectAllCheckBox.setIndeterminate(true);
        }
    }

    @Subscribe("selectAllCheckBox")
    protected void onSelectAllCheckBoxChange(AbstractField.ComponentValueChangeEvent<Checkbox, Boolean> event) {
        if (selectAllCheckBox.getValue()) {
            attributesCheckboxGroup.setValue(new HashSet<>(attributesCheckboxGroup.getListDataView()
                    .getItems().collect(Collectors.toList())));
        } else {
            attributesCheckboxGroup.deselectAll();
        }
    }


    @Subscribe("entityNameField")
    protected void onEntityNameFieldChange(AbstractField.ComponentValueChangeEvent<ComboBox<String>, String> event) {
        if (entityNameField.isEnabled())
            fillAttributes(event.getValue(), null, true);
    }

    protected TreeMap<String, String> getEntityMetaClasses() {
        TreeMap<String, String> options = new TreeMap<>();

        for (MetaClass metaClass : metadataTools.getAllJpaEntityMetaClasses()) {
            if (extendedEntities.getExtendedClass(metaClass) == null) {
                MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
                String originalName = originalMetaClass.getName();
                Class javaClass = originalMetaClass.getJavaClass();
                if (metadataTools.hasCompositePrimaryKey(metaClass) && !metadataTools.hasUuid(originalMetaClass)) {
                    continue;
                }
                String caption = messages.getMessage(javaClass, javaClass.getSimpleName()) + " (" + originalName + ")";
                options.put(caption, originalName);
            }
        }
        return options;
    }

    protected void enableControls() {
        loggedEntityTable.setEnabled(false);
        loggedEntityMiscBox.setEnabled(true);
        attributesCheckboxGroup.setEnabled(true);
        actionsPaneLayout.setVisible(true);
    }

    protected void disableControls() {
        loggedEntityTable.setEnabled(true);
        loggedEntityMiscBox.setEnabled(false);
        attributesCheckboxGroup.setEnabled(false);
        actionsPaneLayout.setVisible(false);
    }

    protected void fillAttributes(String metaClassName, LoggedEntity item, boolean editable) {
        attributesCheckboxGroup.setItems();
        setSelectAllCheckBox(false);

        if (metaClassName != null) {
            MetaClass metaClass = extendedEntities.getEffectiveMetaClass(
                    metadata.getClass(metaClassName));
            List<MetaProperty> metaProperties = new ArrayList<>(metaClass.getProperties());

            List<String> selectedItems = new ArrayList<>();
            List<String> items = new ArrayList<>();

            selectAllCheckBox.setEnabled(editable);
            Set<LoggedAttribute> enabledAttr = null;
            if (item != null)
                enabledAttr = item.getAttributes();
            for (MetaProperty property : metaProperties) {
                if (allowLogProperty(property)) {
                    if (metadataTools.isEmbedded(property)) {
                        MetaClass embeddedMetaClass = property.getRange().asClass();
                        for (MetaProperty embeddedProperty : embeddedMetaClass.getProperties()) {
                            if (allowLogProperty(embeddedProperty)) {
                                String name = String.format("%s.%s", property.getName(), embeddedProperty.getName());
                                items.add(name);
                                if (attributeIsSelected(enabledAttr, name, metaClass)) {
                                    selectedItems.add(name);
                                }
                            }
                        }
                    } else {
                        items.add(property.getName());
                        if (attributeIsSelected(enabledAttr, property.getName(), metaClass)) {
                            selectedItems.add(property.getName());
                        }
                    }
                }
            }

            Collection<MetaProperty> additionalProperties = metadataTools.getAdditionalProperties(metaClass);
            if (additionalProperties != null) {
                for (MetaProperty property : additionalProperties) {
                    if (allowLogProperty(property)) {

                        items.add(property.getName());
                        if (attributeIsSelected(enabledAttr, property.getName(), metaClass)) {
                            selectedItems.add(property.getName());
                        }
                    }
                }
            }
            attributesCheckboxGroup.setItems(items);
            attributesCheckboxGroup.select(selectedItems);
            attributesCheckboxGroup.setEnabled(editable);
        }
    }

    protected boolean attributeIsSelected(Set<LoggedAttribute> enabledAttributes, String name, MetaClass metaclass) {

        return enabledAttributes != null && isEntityHaveAttribute(name, metaclass, enabledAttributes);
    }

    protected void setSelectAllCheckBox(boolean value) {
        canSelectAllCheckboxGenerateEvents = false;
        boolean isEditable = selectAllCheckBox.isEnabled();
        try {
            selectAllCheckBox.setEnabled(true);
            selectAllCheckBox.setValue(value);
        } finally {
            canSelectAllCheckboxGenerateEvents = true;
            selectAllCheckBox.setEnabled(isEditable);
        }
    }

    protected void setDateFieldTime() {
        Date date = timeSource.currentTimestamp();
        LocalDateTime dayAgo = LocalDateTime.ofInstant(DateUtils.addDays(date, -1).toInstant(),
                ZoneId.systemDefault());
        fromDateField.setValue(dayAgo.toLocalDate());
        fromTimeField.setValue(dayAgo.toLocalTime());
        LocalDateTime nextDay = LocalDateTime.ofInstant(DateUtils.addDays(date, 1).toInstant(),
                ZoneId.systemDefault());
        tillDateField.setValue(nextDay.toLocalDate());
        tillTimeField.setValue(nextDay.toLocalTime());
    }

    protected boolean isEntityHaveAttribute(String propertyName, MetaClass metaClass, Set<LoggedAttribute> enabledAttr) {
        if (enabledAttr != null && (metaClass.findProperty(propertyName) == null
                || !metadataTools.isSystem(metaClass.getProperty(propertyName)))) {
            for (LoggedAttribute logAttr : enabledAttr)
                if (logAttr.getName().equals(propertyName))
                    return true;
        }
        return false;
    }

    protected LoggedAttribute getLoggedAttribute(String name, Set<LoggedAttribute> enabledAttr) {
        for (LoggedAttribute atr : enabledAttr)
            if (atr.getName().equals(name))
                return atr;
        return null;
    }

    @Subscribe("instancePicker.valueClearAction")
    protected void onValueClearAction(ActionPerformedEvent event) {
        if (instancePicker.isEnabled()) {
            selectedEntity = null;
            instancePicker.clear();
        }
    }

    @Subscribe("instancePicker.selectAction")
    protected void onSelectAction(ActionPerformedEvent event) {
        if (instancePicker.isEnabled()) {
            final MetaClass metaClass = metadata.getSession().getClass(filterEntityNameField.getValue());
            if (metaClass == null) {
                throw new IllegalStateException("Please specify metaclass or property for PickerField");
            }
            if (!secureOperations.isEntityReadPermitted(metaClass, policyStore)) {
                notifications.create(messages.getMessage(EntityLogView.class, "entityAccessDeniedMessage"))
                        .withType(Notifications.Type.ERROR)
                        .show();
                return;
            }
            try {
                DialogWindow lookup = dialogBuilders.lookup(this, metaClass.getJavaClass())
                        .withSelectHandler(items -> {
                            if (!items.isEmpty()) {
                                Object item = items.iterator().next();
                                selectedEntity = item;
                                instancePicker.setValue(item);
                            }
                        })
                        .withAfterCloseListener(afterCloseEvent -> instancePicker.focus())
                        .build();
                lookup.open();
            } catch (AccessDeniedException ex) {
                notifications.create(messages.getMessage(EntityLogView.class, "entityScreenAccessDeniedMessage"))
                        .withType(Notifications.Type.ERROR)
                        .show();
            }
        }
    }

    @Subscribe("loggedEntityTable.create")
    protected void onLoggedEntityTableCreate(ActionPerformedEvent event) {
        LoggedEntity entity = metadata.create(LoggedEntity.class);
        entity.setAuto(false);
        entity.setManual(false);
        setSelectAllCheckBox(false);
        loggedEntityDc.getMutableItems().add(entity);
        loggedEntityDc.setItem(entity);
        loggedEntityTable.setEnabled(true);
        loggedEntityTable.select(entity);

        enableControls();

        entityNameField.setEnabled(true);
        entityNameField.focus();
    }

    @Subscribe("loggedEntityTable.edit")
    protected void onLoggedEntityTableEdit(ActionPerformedEvent event) {
        enableControls();

        loggedEntityTable.setEnabled(false);
        cancelBtn.focus();
    }

    @Subscribe("searchBtn")
    protected void onSearchBtnClick(ClickEvent<Button> event) {
        Object entity = selectedEntity;

        if (entity != null) {
            Object entityId = referenceToEntitySupport.getReferenceId(entity);
            if (entityId instanceof UUID) {
                entityLogDl.setParameter("entityId", entityId);
            } else if (entityId instanceof String) {
                entityLogDl.setParameter("stringEntityId", entityId);
            } else if (entityId instanceof Integer) {
                entityLogDl.setParameter("intEntityId", entityId);
            } else if (entityId instanceof Long) {
                entityLogDl.setParameter("longEntityId", entityId);
            }
        } else {
            entityLogDl.removeParameter("entityId");
            entityLogDl.removeParameter("stringEntityId");
            entityLogDl.removeParameter("intEntityId");
            entityLogDl.removeParameter("longEntityId");
        }
        if (userField.getValue() != null) {
            entityLogDl.setParameter("user", userField.getValue());
        } else {
            entityLogDl.removeParameter("user");
        }
        if (changeTypeField.getValue() != null) {
            entityLogDl.setParameter("changeType", changeTypeField.getValue());
        } else {
            entityLogDl.removeParameter("changeType");
        }
        if (filterEntityNameField.getValue() != null) {
            entityLogDl.setParameter("entityName", filterEntityNameField.getValue());
        } else {
            entityLogDl.removeParameter("entityName");
        }
        LocalDateTime fromDateTime = getFromDateTime();
        if (getFromDateTime() != null) {
            entityLogDl.setParameter("fromDate",
                    Date.from(fromDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        } else {
            entityLogDl.removeParameter("fromDate");
        }
        LocalDateTime tillDateTime = getTillDateTime();
        if (tillDateField.getValue() != null) {
            entityLogDl.setParameter("tillDate",
                    Date.from(tillDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        } else {
            entityLogDl.removeParameter("tillDate");
        }
        entityLogDl.load();
    }

    @Subscribe("clearEntityLogTableBtn")
    protected void onClearEntityLogTableBtnClick(ClickEvent<Button> event) {
        userField.clear();
        filterEntityNameField.clear();
        changeTypeField.clear();
        instancePicker.clear();
        fromDateField.clear();
        fromTimeField.clear();
        tillDateField.clear();
        tillTimeField.clear();
    }

    protected boolean allowLogProperty(MetaProperty metaProperty) {
        if (metadataTools.isSystem(metaProperty)
                //log system property tenantId
                && !metadataTools.isAnnotationPresent(metaProperty.getDomain().getJavaClass(), metaProperty.getName(), TenantId.class)) {
            return false;
        }
        Range range = metaProperty.getRange();
        if (range.isClass() && metadataTools.hasCompositePrimaryKey(range.asClass()) &&
                !metadataTools.hasUuid(range.asClass())) {
            return false;
        }
        if (range.isClass() && range.getCardinality().isMany()) {
            return false;
        }
        return true;
    }

    private DataContext getDataContext() {
        return getViewData().getDataContext();
    }

    @Subscribe("saveBtn")
    protected void onSaveBtnClick(ClickEvent<Button> event) {
        LoggedEntity selectedEntity = loggedEntityDc.getItem();
        final LoggedEntity selected = selectedEntity;
        if (loggedEntityDc.getItems().stream()
                .anyMatch(e -> !(selected == e) && e.getName().equals(selected.getName()))) {
            notifications.create(messages.getMessage(EntityLogView.class, "settingAlreadyExist"))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return;
        }

        DataContext dataContext = loggedEntityDl.getDataContext();
        if (dataContext == null) {
            throw new RuntimeException("DataContext is null");
        }
        selectedEntity = dataContext.merge(selectedEntity);
        Set<LoggedAttribute> enabledAttributes = selectedEntity.getAttributes() != null ?
                selectedEntity.getAttributes() : new HashSet<>();
        Set<String> selectedItems = attributesCheckboxGroup.getSelectedItems();
        for (Object c : attributesCheckboxGroup.getListDataView().getItems().toArray()) {
            String currentElementCheckbox = (String) c;
            MetaClass metaClass = metadata.getSession().getClass(entityNameField.getValue());
            if (selectedItems.contains(currentElementCheckbox) && !isEntityHaveAttribute(currentElementCheckbox, metaClass, enabledAttributes)) {
                //add attribute if checked and not exist in table
                LoggedAttribute newLoggedAttribute = dataContext.create(LoggedAttribute.class);
                newLoggedAttribute.setName(currentElementCheckbox);
                newLoggedAttribute.setEntity(selectedEntity);
                enabledAttributes.add(newLoggedAttribute);
            }
            if (!selectedItems.contains(currentElementCheckbox) && isEntityHaveAttribute(currentElementCheckbox, metaClass, enabledAttributes)) {
                //remove attribute if unchecked and exist in table
                LoggedAttribute removeAtr = getLoggedAttribute(currentElementCheckbox, enabledAttributes);
                if (removeAtr != null)
                    dataContext.remove(removeAtr);
            }
        }
        selectedEntity.setAttributes(enabledAttributes);
        loggedEntityDc.replaceItem(selectedEntity);
        dataContext.save();
        disableControls();
        loggedEntityTable.setEnabled(true);
        loggedEntityTable.focus();

        entityLog.invalidateCache();
    }

    @Subscribe("loggedEntityTable.remove")
    protected void onLoggedEntityTableRemove(ActionPerformedEvent event) {
        Set<LoggedEntity> selectedItems = loggedEntityTable.getSelectedItems();
        if (!selectedItems.isEmpty()) {
            dialogs.createOptionDialog().withHeader(messages.getMessage("dialogs.Confirmation"))
                    .withText(messages.getMessage("dialogs.Confirmation.Remove"))
                    .withActions(
                            new DialogAction(DialogAction.Type.OK).withHandler(e -> {
                                DataContext dataContext = getDataContext();
                                for (LoggedEntity item : selectedItems) {
                                    if (item.getAttributes() != null) {
                                        for (LoggedAttribute loggedAttribute : new HashSet<>(item.getAttributes())) {
                                            dataContext.remove(loggedAttribute);
                                        }
                                        dataContext.save();
                                    }
                                    dataContext.remove(item);
                                    dataContext.save();
                                }
                                loggedEntityDc.getMutableItems().removeAll(selectedItems);
                                entityLog.invalidateCache();
                            }).withVariant(ActionVariant.PRIMARY),
                            new DialogAction(DialogAction.Type.NO)
                    ).open();
        }
    }

    @Subscribe("cancelBtn")
    protected void onCancelBtnClick(ClickEvent<Button> event) {
        loggedEntityDl.load();
        disableControls();
        loggedEntityTable.setEnabled(true);
        loggedEntityTable.focus();
    }

    @Subscribe("loggedEntityTable.exportJSON")
    public void onRoleModelsTableExportJSON(ActionPerformedEvent event) {
        export(JSON);
    }

    @Subscribe("loggedEntityTable.exportZIP")
    public void onRoleModelsTableExportZIP(ActionPerformedEvent event) {
        export(ZIP);
    }

    protected void export(DownloadFormat downloadFormat) {
        List<Object> dbRowLevelRoles = getExportEntityList();

        if (dbRowLevelRoles.isEmpty()) {
            notifications.create(messages.getMessage(EntityLogView.class, "nothingToExport"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        try {
            byte[] data = downloadFormat == JSON ?
                    entityImportExport.exportEntitiesToJSON(dbRowLevelRoles, buildExportFetchPlan()).getBytes(StandardCharsets.UTF_8) :
                    entityImportExport.exportEntitiesToZIP(dbRowLevelRoles, buildExportFetchPlan());
            downloader.download(data, String.format("LoggedEntity.%s", downloadFormat.getFileExt()), downloadFormat);

        } catch (Exception e) {
            log.warn("Unable to export loggedEntity", e);
            notifications.create(messages.getMessage(EntityLogView.class, "error.exportFailed"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    protected List<Object> getExportEntityList() {
        Collection<LoggedEntity> selected = loggedEntityTable.getSelectedItems();
        if (selected.isEmpty() && loggedEntityTable.getItems() != null) {
            selected = loggedEntityDc.getItems();
        }

        return new ArrayList<>(selected);
    }

    protected FetchPlan buildExportFetchPlan() {
        return fetchPlans.builder(LoggedEntity.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("attributes", FetchPlan.BASE)
                .build();
    }

    @Subscribe("importField")
    public void onImportFieldFileUploadSucceed(FileUploadSucceededEvent<FileUploadField> event) {
        try {
            byte[] bytes = importField.getValue();
            Assert.notNull(bytes, "Uploaded file does not contains data");

            List<Object> importedEntities = getImportedEntityList(event.getFileName(), bytes);

            if (importedEntities.size() > 0) {
                loggedEntityDl.load();
                loggedAttrDl.load();

                notifications.create(messages.getMessage(EntityLogView.class, "importSuccessful"))
                        .withType(Notifications.Type.SUCCESS)
                        .show();
            }
        } catch (Exception e) {
            log.warn("Unable to import logged entity", e);
            notifications.create(messages.getMessage(EntityLogView.class, "error.importFailed"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    protected List<Object> getImportedEntityList(String fileName, byte[] fileContent) {
        Collection<Object> importedEntities;
        if (JSON.getFileExt().equals(Files.getFileExtension(fileName))) {
            importedEntities = entityImportExport.importEntitiesFromJson(new String(fileContent, StandardCharsets.UTF_8), createEntityImportPlan());
        } else {
            importedEntities = entityImportExport.importEntitiesFromZIP(fileContent, createEntityImportPlan());
        }
        return new ArrayList<>(importedEntities);
    }

    protected EntityImportPlan createEntityImportPlan() {
        return entityImportPlans.builder(LoggedEntity.class)
                .addLocalProperties()
                .addProperty(new EntityImportPlanProperty(
                        "attributes",
                        entityImportPlans.builder(LoggedAttribute.class).addLocalProperties().build(),
                        CollectionImportPolicy.KEEP_ABSENT_ITEMS)
                )
                .build();
    }
}

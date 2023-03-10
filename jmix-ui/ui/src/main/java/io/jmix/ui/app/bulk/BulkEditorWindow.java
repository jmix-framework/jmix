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

package io.jmix.ui.app.bulk;

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.core.annotation.TenantId;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.accesscontext.UiEntityAttributeContext;
import io.jmix.ui.accesscontext.UiEntityContext;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.action.DialogAction.Type;
import io.jmix.ui.bulk.BulkEditorDataService;
import io.jmix.ui.bulk.BulkEditorDataService.LoadDescriptor;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.component.validation.Validator;
import io.jmix.ui.component.validator.AbstractBeanValidator;
import io.jmix.ui.deviceinfo.DeviceInfo;
import io.jmix.ui.deviceinfo.DeviceInfoProvider;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.DataComponents;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.InstancePropertyContainer;
import io.jmix.ui.screen.*;
import io.jmix.ui.util.OperationResult;
import io.jmix.ui.util.UnknownOperationResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.jmix.ui.app.bulk.ColumnsMode.TWO_COLUMNS;

@UiController("bulkEditorWindow")
@UiDescriptor("bulk-editor-window.xml")
public class BulkEditorWindow<E> extends Screen implements BulkEditorController<E> {

    protected static final String COLUMN_COUNT_STYLENAME = "jmix-bulk-editor-columns-";

    protected static final ColumnsMode DEFAULT_COLUMNS_MODE = TWO_COLUMNS;

    @Autowired
    protected Button applyButton;
    @Autowired
    protected Action applyChanges;
    @Autowired
    protected Label<String> infoLabel;
    @Autowired
    protected ScrollBoxLayout fieldsScrollBox;

    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected BulkEditorDataService bulkEditorDataService;
    @Autowired
    protected DataComponents dataComponents;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected DeviceInfoProvider deviceInfoProvider;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected ScreenValidation screenValidation;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected UiComponentsGenerator uiComponentsGenerator;

    protected BulkEditorContext<E> context;

    protected Pattern excludeRegex;

    protected Map<String, ManagedField> managedFields = new LinkedHashMap<>();
    protected List<String> managedEmbeddedProperties = new ArrayList<>();
    protected Map<String, Field<?>> dataFields = new LinkedHashMap<>();

    protected InstanceContainer<E> instanceContainer;
    protected List<E> items;

    protected boolean commitPerformed = false;

    @Override
    public void setBulkEditorContext(BulkEditorContext<E> context) {
        this.context = context;

        if (!Strings.isNullOrEmpty(context.getExclude())) {
            excludeRegex = Pattern.compile(context.getExclude());
        }

        MetaClass metaClass = context.getMetaClass();

        for (ManagedField managedField : getManagedFields()) {
            managedFields.put(managedField.getFqn(), managedField);
        }

        FetchPlan fetchPlan = createFetchPlan(metaClass);
        items = loadItems(fetchPlan);

        E instance = metadata.create(metaClass.getJavaClass());
        createEmbeddedFields(metaClass, instance, null);

        instanceContainer = dataComponents.createInstanceContainer(metaClass.getJavaClass());
        getScreenData().registerContainer(metaClass.getName() + "Dc", instanceContainer);
        createNestedDataContainers(metaClass, instanceContainer, null);

        instanceContainer.setItem(instance);
        instanceContainer.setFetchPlan(fetchPlan);

        createDataComponents();
    }

    protected void createDataComponents() {
        if (managedFields.isEmpty()) {
            infoLabel.setValue(messageBundle.getMessage("bulk.noEditableProperties"));
            applyButton.setVisible(false);
            applyChanges.setVisible(false);
            return;
        }

        List<ManagedField> editFields = new ArrayList<>(managedFields.values());

        // sort fields
        Comparator<ManagedField> comparator = createManagedFieldComparator(editFields);
        editFields.sort(comparator);

        ComponentContainer fieldsLayout = createFieldsLayout();

        int fromField;
        int toField = 0;
        int addedColumns = 0;
        ColumnsMode columnsMode = getColumnsMode();

        for (int col = 0; col < columnsMode.getColumnsCount(); col++) {
            fromField = toField;
            toField += getFieldsCountForColumn(
                    editFields.size() - toField,
                    columnsMode.getColumnsCount() - col);

            ComponentContainer column = createColumnLayout();

            for (int fieldIndex = fromField; fieldIndex < toField; fieldIndex++) {
                ComponentContainer row = createRow(editFields, fieldIndex);
                column.add(row);
            }

            fieldsLayout.add(column);
            // if there is no fields remain
            if (editFields.size() - toField == 0) {
                addedColumns = col + 1;
                break;
            }
        }

        fieldsLayout.addStyleName(COLUMN_COUNT_STYLENAME + addedColumns);
        fieldsScrollBox.add(fieldsLayout);

        focusFirstPossibleField(dataFields);
    }

    protected ComponentContainer createRow(List<ManagedField> editFields, int fieldIndex) {
        ManagedField field = editFields.get(fieldIndex);

        ComponentContainer row = createRowLayout();

        Label<String> label = createLabel(field);
        row.add(label);

        InstanceContainer<?> fieldDc = instanceContainer;
        MetaProperty metaProperty = field.getMetaProperty();

        // field owner metaclass is embeddable only if field domain embeddable,
        // so we can check field domain
        if (metadataTools.isJpaEmbeddable(metaProperty.getDomain())
                && field.getParentFqn() != null) {
            fieldDc = getScreenData().getContainer(field.getParentFqn());
        }

        Field<?> editField = createField(metaProperty, fieldDc);

        editField.setFrame(getWindow().getFrame());
        editField.setStyleName("jmix-bulk-editor-field");

        if (isEntityPickerWrapperNeeded(editField)) {
            CssLayout wrapper = uiComponents.create(CssLayout.NAME);
            wrapper.setStyleName("jmix-bulk-editor-picker-field-wrapper");
            wrapper.add(editField);
            row.add(wrapper);
        } else {
            row.add(editField);
        }

        Button clearButton = createClearButton();
        if (editField.isRequired()) {
            // hidden component for correctly showing layout
            clearButton.setStyleName("jmix-bulk-editor-spacer");
        } else {
            clearButton.addClickListener(createClearButtonClickListener(editField));
        }

        row.add(clearButton);

        // disable bean validator
        editField.getValidators().stream()
                .filter(validator -> validator instanceof AbstractBeanValidator)
                .findFirst()
                .ifPresent(((Field) editField)::removeValidator);

        // disable required
        editField.setRequired(false);
        editField.clear();

        Validator validator = context.getFieldValidators().get(field.getFqn());
        if (validator != null) {
            editField.addValidator(validator);
        }

        dataFields.put(field.getFqn(), editField);

        return row;
    }

    protected Consumer<Button.ClickEvent> createClearButtonClickListener(Field<?> editField) {
        return e -> {
            editField.setEnabled(!editField.isEnabled());
            Button button = e.getSource();

            button.setIconFromSet(editField.isEnabled() ? JmixIcon.TRASH : JmixIcon.EDIT);
            button.setDescription(messageBundle.getMessage(editField.isEnabled()
                    ? "bulk.clearAttribute"
                    : "bulk.editAttribute"
            ));

            if (!editField.isEnabled()) {
                editField.clear();
            }
        };
    }

    protected Button createClearButton() {
        Button clearButton = uiComponents.create(Button.class);
        clearButton.setIconFromSet(JmixIcon.TRASH);
        clearButton.setCaption("");
        clearButton.setDescription(messageBundle.getMessage("bulk.clearAttribute"));
        return clearButton;
    }

    protected Field<?> createField(MetaProperty metaProperty, InstanceContainer<?> fieldDc) {
        ComponentGenerationContext generationContext =
                new ComponentGenerationContext(metaProperty.getDomain(), metaProperty.getName());
        generationContext.setValueSource(new ContainerValueSource<>(fieldDc, metaProperty.getName()));
        generationContext.setTargetClass(getClass());
        return (Field<?>) uiComponentsGenerator.generate(generationContext);
    }

    protected Label<String> createLabel(ManagedField field) {
        Label<String> label = uiComponents.create(Label.NAME);
        label.setValue(field.getLocalizedName());
        label.setStyleName("jmix-bulk-editor-label");
        return label;
    }

    protected ComponentContainer createRowLayout() {
        CssLayout row = uiComponents.create(CssLayout.NAME);
        row.setStyleName("jmix-bulk-editor-row");
        row.setWidth("100%");
        return row;
    }

    protected ComponentContainer createColumnLayout() {
        VBoxLayout column = uiComponents.create(VBoxLayout.NAME);
        column.setStyleName("jmix-bulk-editor-column");
        column.setWidth(Component.AUTO_SIZE);
        return column;
    }

    protected ColumnsMode getColumnsMode() {
        return context.getColumnsMode() != null
                ? context.getColumnsMode()
                : DEFAULT_COLUMNS_MODE;
    }

    protected ComponentContainer createFieldsLayout() {
        CssLayout fieldsLayout = uiComponents.create(CssLayout.NAME);
        fieldsLayout.setStyleName("jmix-bulk-editor-fields-layout");
        fieldsLayout.setWidthFull();
        fieldsLayout.setHeightFull();
        return fieldsLayout;
    }

    protected void focusFirstPossibleField(Map<String, Field<?>> dataFields) {
        dataFields.values().stream()
                .filter(field -> field instanceof Component.Focusable)
                .findFirst()
                .ifPresent(field ->
                        ((Component.Focusable) field).focus()
                );
    }

    protected Comparator<ManagedField> createManagedFieldComparator(List<ManagedField> editFields) {
        FieldSorter fieldSorter = context.getFieldSorter();
        Comparator<ManagedField> comparator;
        if (fieldSorter != null) {
            Map<MetaProperty, Integer> sorted = fieldSorter.apply(editFields.stream()
                    .map(ManagedField::getMetaProperty)
                    .collect(Collectors.toList()));
            comparator = Comparator.comparingInt(item ->
                    sorted.get(item.getMetaProperty()));
        } else {
            comparator = Comparator.comparing(ManagedField::getLocalizedName);
        }
        return comparator;
    }

    protected int getFieldsCountForColumn(int remainFields, int remainColumns) {
        int fieldsForColumn = remainFields / remainColumns;
        return remainFields % remainColumns == 0 ? fieldsForColumn : ++fieldsForColumn;
    }

    protected boolean isEntityPickerWrapperNeeded(Field<?> field) {
        DeviceInfo deviceInfo = deviceInfoProvider.getDeviceInfo();
        if (deviceInfo == null) {
            return false;
        }

        boolean isPickerField = field instanceof ValuePicker;
        boolean isAffectedBrowser = deviceInfo.isFirefox()
                || deviceInfo.isEdge()
                || deviceInfo.isIE()
                || deviceInfo.isSafari();

        return isPickerField && isAffectedBrowser;
    }

    protected void createNestedDataContainers(MetaClass metaClass,
                                              InstanceContainer<?> parent, @Nullable String fqnPrefix) {
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            if (MetaProperty.Type.ASSOCIATION == metaProperty.getType()
                    || MetaProperty.Type.COMPOSITION == metaProperty.getType()
                    || MetaProperty.Type.EMBEDDED == metaProperty.getType()) {

                String fqn = generateFqn(metaProperty, fqnPrefix);

                if (managedEmbeddedProperties.contains(fqn)
                        && metadataTools.isEmbedded(metaProperty)) {
                    MetaClass propertyMetaClass = metaProperty.getRange().asClass();

                    InstancePropertyContainer<Object> instanceContainer =
                            dataComponents.createInstanceContainer(metaClass.getJavaClass(),
                                    parent, metaProperty.getName());
                    getScreenData().registerContainer(fqn + "Dc", instanceContainer);
                    createNestedDataContainers(propertyMetaClass, instanceContainer, fqn);
                }
            }
        }
    }

    protected List<E> loadItems(FetchPlan fetchPlan) {
        LoadDescriptor<E> ld = new LoadDescriptor<>(context.getSelected(),
                context.getMetaClass(), fetchPlan);

        return bulkEditorDataService.reload(ld);
    }

    protected FetchPlan createFetchPlan(MetaClass metaClass) {
        FetchPlanBuilder builder = fetchPlans.builder(metaClass.getJavaClass());

        for (MetaProperty metaProperty : metaClass.getProperties()) {
            String fqn = generateFqn(metaProperty, null);

            if (!managedFields.containsKey(fqn)
                    && !managedEmbeddedProperties.contains(fqn)
                    && !isTenantMetaProperty(metaProperty)) {
                continue;
            }

            addFetchPlanProperties(builder, metaProperty, fqn);
        }

        return builder.build();
    }

    protected FetchPlan createEmbeddedFetchPlan(MetaClass metaClass, String fqnPrefix) {
        FetchPlanBuilder builder = fetchPlans.builder(metaClass.getJavaClass());

        for (MetaProperty metaProperty : metaClass.getProperties()) {
            String fqn = generateFqn(metaProperty, fqnPrefix);

            if (!managedFields.containsKey(fqn)) {
                continue;
            }

            addFetchPlanProperties(builder, metaProperty, fqn);
        }

        return builder.build();
    }

    protected void addFetchPlanProperties(FetchPlanBuilder builder, MetaProperty metaProperty, String fqn) {
        switch (metaProperty.getType()) {
            case DATATYPE:
            case ENUM:
                builder.add(metaProperty.getName());
                break;
            case EMBEDDED:
            case ASSOCIATION:
            case COMPOSITION:
                MetaClass propMetaClass = metaProperty.getRange().asClass();

                FetchPlan propFetchPlan = metadataTools.isEmbedded(metaProperty)
                        ? createEmbeddedFetchPlan(propMetaClass, fqn)
                        : fetchPlanRepository.getFetchPlan(propMetaClass, FetchPlan.INSTANCE_NAME);

                // In some cases JPA loads extended entities as instance of base
                // class which leads to ClassCastException loading property lazy
                // prevents this from happening
                builder.add(metaProperty.getName(), propFetchPlanBuilder ->
                        propFetchPlanBuilder.addFetchPlan(propFetchPlan));
                break;
            default:
                throw new IllegalStateException("unknown property type");
        }
    }

    protected void createEmbeddedFields(MetaClass metaClass, Object item, @Nullable String fqnPrefix) {
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            String fqn = generateFqn(metaProperty, fqnPrefix);

            if (managedEmbeddedProperties.contains(fqn)
                    && metadataTools.isEmbedded(metaProperty)) {
                MetaClass embeddedMetaClass = metaProperty.getRange().asClass();
                Object embedded = EntityValues.getValue(item, metaProperty.getName());
                if (embedded == null) {
                    embedded = metadata.create(embeddedMetaClass);
                    EntityValues.setValue(item, metaProperty.getName(), embedded);
                }
                createEmbeddedFields(embeddedMetaClass, embedded, fqn);
            }
        }
    }

    protected String generateFqn(MetaProperty metaProperty, @Nullable String fqnPrefix) {
        String fqn = metaProperty.getName();
        if (!Strings.isNullOrEmpty(fqnPrefix)) {
            fqn = fqnPrefix + "." + fqn;
        }
        return fqn;
    }

    protected List<ManagedField> getManagedFields() {
        MetaClass metaClass = context.getMetaClass();

        return getManagedFields(metaClass, null, null);
    }

    protected List<ManagedField> getManagedFields(MetaProperty embeddedProperty,
                                                  String fqnPrefix, String localePrefix) {
        MetaClass metaClass = embeddedProperty.getRange().asClass();

        return getManagedFields(metaClass, fqnPrefix, localePrefix);
    }

    protected List<ManagedField> getManagedFields(MetaClass metaClass,
                                                  @Nullable String fqnPrefix, @Nullable String localePrefix) {
        List<ManagedField> managedFields = new ArrayList<>();
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            if (isManagedAttribute(metaClass, metaProperty)) {
                String fqn = generateFqn(metaProperty, fqnPrefix);
                String propertyCaption = generatePropertyCaption(metaClass, metaProperty, localePrefix);

                if (!metadataTools.isEmbedded(metaProperty)) {
                    managedFields.add(new ManagedField(fqn, metaProperty, propertyCaption, fqnPrefix));
                } else {
                    List<ManagedField> nestedFields = getManagedFields(metaProperty, fqn, propertyCaption);
                    if (nestedFields.size() > 0) {
                        managedEmbeddedProperties.add(fqn);
                    }
                    managedFields.addAll(nestedFields);
                }
            }
        }

        return managedFields;
    }

    protected String generatePropertyCaption(MetaClass metaClass,
                                             MetaProperty metaProperty, @Nullable String localePrefix) {
        String propertyCaption = messageTools.getPropertyCaption(metaClass, metaProperty.getName());
        if (!Strings.isNullOrEmpty(localePrefix)) {
            propertyCaption = localePrefix + " " + propertyCaption;
        }

        return propertyCaption;
    }

    protected boolean isManagedAttribute(MetaClass metaClass, MetaProperty metaProperty) {
        if (metadataTools.isSystem(metaProperty)
                || (!metadataTools.isJpa(metaProperty) && !isCrossDataStoreReference(metaProperty))
                || metadataTools.isSystemLevel(metaProperty)
                || metaProperty.getRange().getCardinality().isMany()
                || !isEntityAttributeModifyPermitted(metaClass, metaProperty)) {
            return false;
        }

        if (metaProperty.getRange().isDatatype()
                && (isByteArray(metaProperty) || isUuid(metaProperty))) {
            return false;
        }

        if (!isRangeClassPermitted(metaProperty)) {
            return false;
        }

        List<String> includeProperties = context.getIncludeProperties();
        if (!includeProperties.isEmpty()) {
            return includeProperties.contains(metaProperty.getName());
        }

        return !(excludeRegex != null && excludeRegex.matcher(metaProperty.getName()).matches());
    }

    protected boolean isCrossDataStoreReference(MetaProperty metaProperty) {
        return metadataTools.getCrossDataStoreReferenceIdProperty(metaProperty.getStore().getName(), metaProperty) != null;
    }

    protected boolean isEntityAttributeModifyPermitted(MetaClass metaClass, MetaProperty metaProperty) {
        UiEntityAttributeContext attributeContext =
                new UiEntityAttributeContext(metaClass, metaProperty.getName());
        accessManager.applyRegisteredConstraints(attributeContext);

        return attributeContext.canModify();
    }

    protected boolean isRangeClassPermitted(MetaProperty metaProperty) {
        if (metaProperty.getRange().isClass()) {
            MetaClass propertyMetaClass = metaProperty.getRange().asClass();

            UiEntityContext entityContext = new UiEntityContext(propertyMetaClass);
            accessManager.applyRegisteredConstraints(entityContext);

            return !metadataTools.isSystemLevel(propertyMetaClass)
                    && entityContext.isViewPermitted();
        }

        return true;
    }

    protected boolean isByteArray(MetaProperty metaProperty) {
        return metaProperty.getRange().asDatatype().getJavaClass().equals(byte[].class);
    }

    protected boolean isUuid(MetaProperty metaProperty) {
        return metaProperty.getRange().asDatatype().getJavaClass().equals(UUID.class);
    }

    @Subscribe("cancelChanges")
    protected void onCancelChanges(Action.ActionPerformedEvent event) {
        close(WINDOW_CLOSE_ACTION);
    }

    @Subscribe
    protected void onBeforeClose(BeforeCloseEvent event) {
        preventUnsavedChanges(event);
    }

    protected void preventUnsavedChanges(BeforeCloseEvent event) {
        CloseAction action = event.getCloseAction();

        if (action instanceof ChangeTrackerCloseAction
                && ((ChangeTrackerCloseAction) action).isCheckForUnsavedChanges()
                && hasChanges()) {
            UnknownOperationResult result = new UnknownOperationResult();

            screenValidation.showUnsavedChangesDialog(this, action)
                    .onDiscard(() -> result.resume(closeWithDiscard()))
                    .onCancel(result::fail);

            event.preventWindowClose(result);
        }
    }

    protected boolean hasChanges() {
        if (!commitPerformed) {
            for (Map.Entry<String, Field<?>> fieldEntry : dataFields.entrySet()) {
                Field<?> field = fieldEntry.getValue();
                if (isFieldChanged(field)) {
                    return true;
                }
            }
        }

        return false;
    }

    public OperationResult closeWithDiscard() {
        return close(WINDOW_DISCARD_AND_CLOSE_ACTION);
    }

    @Subscribe("applyChanges")
    protected void onApplyChanges(Action.ActionPerformedEvent event) {
        closeWithCommit();
    }

    public void closeWithCommit() {
        ValidationErrors validationErrors = validateScreen();
        if (!validationErrors.isEmpty()) {
            screenValidation.showValidationErrors(this, validationErrors);

            return;
        }

        List<String> fields = new ArrayList<>();
        for (Map.Entry<String, Field<?>> fieldEntry : dataFields.entrySet()) {
            Field<?> field = fieldEntry.getValue();
            if (isFieldChanged(field)) {
                String localizedName = managedFields.get(fieldEntry.getKey()).getLocalizedName();
                fields.add("- " + localizedName);
            }
        }

        if (fields.isEmpty()) {
            notifications.create()
                    .withCaption(messageBundle.getMessage("bulk.noChanges"))
                    .show();
        } else {
            showConfirmDialogOrCommit(fields);
        }
    }

    protected void showConfirmDialogOrCommit(List<String> fields) {
        if (context.isUseConfirmDialog()) {
            dialogs.createOptionDialog()
                    .withCaption(messageBundle.getMessage("bulk.confirmation"))
                    .withMessage(messageBundle.formatMessage("bulk.applyConfirmation",
                            items.size(), StringUtils.join(fields, "\n")))
                    .withActions(new DialogAction(Type.OK)
                                    .withCaption(messages.getMessage("actions.Apply"))
                                    .withHandler(event -> commitBulkChanges()),
                            new DialogAction(Type.CANCEL, Action.Status.PRIMARY))
                    .show();
        } else {
            commitBulkChanges();
        }
    }

    protected void commitBulkChanges() {
        List<String> fields = new ArrayList<>();

        for (Map.Entry<String, Field<?>> fieldEntry : dataFields.entrySet()) {
            Field<?> field = fieldEntry.getValue();
            if (isFieldChanged(field)) {
                fields.add(managedFields.get(fieldEntry.getKey()).getFqn());
            }
        }

        for (Map.Entry<String, Field<?>> fieldEntry : dataFields.entrySet()) {
            Field<?> field = fieldEntry.getValue();

            if (!field.isEnabled() || isFieldChanged(field)) {
                updateItemValues(fieldEntry, field.isEnabled() ? field.getValue() : null);
            }
        }

        EntitySet saved = dataManager.save(new SaveContext().saving(items));

        Logger logger = LoggerFactory.getLogger(BulkEditorWindow.class);
        logger.info("Applied bulk editing for {} entries of {}. Changed properties: {}",
                saved.size(), context.getMetaClass(), StringUtils.join(fields, ", "));

        notifications.create()
                .withCaption(messageBundle.formatMessage("bulk.successMessage", saved.size()))
                .show();

        commitPerformed = true;

        close(WINDOW_COMMIT_AND_CLOSE_ACTION);
    }

    protected void updateItemValues(Map.Entry<String, Field<?>> fieldEntry, @Nullable Object value) {
        for (E item : items) {
            ensureEmbeddedPropertyCreated(item, fieldEntry.getKey());
            EntityValues.setValueEx(item, fieldEntry.getKey(), value);
        }
    }

    protected void ensureEmbeddedPropertyCreated(E item, String propertyPath) {
        if (!StringUtils.contains(propertyPath, ".")) {
            return;
        }

        MetaPropertyPath path = context.getMetaClass().getPropertyPath(propertyPath);

        if (path != null) {
            Object currentItem = item;
            for (MetaProperty property : path.getMetaProperties()) {
                if (metadataTools.isEmbedded(property)) {
                    Object currentItemValue = EntityValues.getValue(currentItem, property.getName());
                    if (currentItemValue == null) {
                        Object newItem = metadata.create(property.getRange().asClass());
                        EntityValues.setValue(currentItem, property.getName(), newItem);
                        currentItem = newItem;
                    } else {
                        currentItem = currentItemValue;
                    }
                } else {
                    break;
                }
            }
        }
    }

    protected boolean isFieldChanged(Field<?> field) {
        return !field.isEnabled() || !field.isEmpty();
    }

    protected ValidationErrors validateScreen() {
        ValidationErrors errors = validateUiComponents();
        applyModelValidators(errors);

        return errors;
    }

    protected ValidationErrors validateUiComponents() {
        return screenValidation.validateUiComponents(getWindow());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void applyModelValidators(ValidationErrors errors) {
        for (Validator validator : context.getModelValidators()) {
            try {
                validator.accept(instanceContainer);
            } catch (ValidationException e) {
                errors.add(e.getDetailsMessage());
            }
        }
    }

    protected boolean isTenantMetaProperty(MetaProperty metaProperty) {
        return metaProperty.getAnnotatedElement().getAnnotation(TenantId.class) != null;
    }

    protected static class ManagedField {

        protected final String fqn;
        protected final String parentFqn;

        protected final String localizedName;

        protected final MetaProperty metaProperty;

        public ManagedField(String fqn, MetaProperty metaProperty,
                            String localizedName, @Nullable String parentFqn) {
            this.fqn = fqn;
            this.metaProperty = metaProperty;
            this.localizedName = localizedName;
            this.parentFqn = parentFqn;
        }

        public String getFqn() {
            return fqn;
        }

        @Nullable
        public String getParentFqn() {
            return parentFqn;
        }

        public String getLocalizedName() {
            return localizedName;
        }

        public MetaProperty getMetaProperty() {
            return metaProperty;
        }
    }
}

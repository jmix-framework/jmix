/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.app.bulk;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.router.Route;
import io.jmix.core.AccessManager;
import io.jmix.core.DataManager;
import io.jmix.core.EntitySet;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanBuilder;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.FetchPlans;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.SaveContext;
import io.jmix.core.annotation.TenantId;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.accesscontext.UiEntityAttributeContext;
import io.jmix.flowui.accesscontext.UiEntityContext;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.action.DialogAction.Type;
import io.jmix.flowui.bulk.BulkEditorDataProvider;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.UiComponentsGenerator;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.util.OperationResult;
import io.jmix.flowui.util.UnknownOperationResult;
import io.jmix.flowui.view.ChangeTrackerCloseAction;
import io.jmix.flowui.view.CloseAction;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.flowui.view.ViewValidation;
import jakarta.validation.metadata.BeanDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.jmix.flowui.app.bulk.ColumnsMode.TWO_COLUMNS;

@ViewController("bulkEditorWindow")
@ViewDescriptor("bulk-edit-view.xml")
@Route("bulk-edit")
@DialogMode(resizable = true, width = "50em", height = "40em")
public class BulkEditView<E> extends StandardView implements BulkEditorController<E> {

    protected static final String COLUMN_COUNT_STYLENAME = "jmix-bulk-editor-columns-";

    protected static final ColumnsMode DEFAULT_COLUMNS_MODE = TWO_COLUMNS;

    @ViewComponent
    protected JmixButton applyButton;
    @ViewComponent
    protected Action applyChanges;
    @ViewComponent
    protected Span infoLabel;
    @ViewComponent
    protected VerticalLayout scrollerContentBox;

    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected BulkEditorDataProvider bulkEditorDataProvider;
    @Autowired
    protected DataComponents dataComponents;
    @Autowired
    protected DataManager dataManager;
    //    @Autowired
//    protected DeviceInfoProvider deviceInfoProvider;
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
    protected ViewValidation viewValidation;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected UiComponentsGenerator uiComponentsGenerator;
    @Autowired
    protected jakarta.validation.Validator validator;
    @Autowired
    protected ObjectProvider<BulkEditBeanPropertyValidator> beanValidatorProvider;

    protected BulkEditorContext<E> context;

    protected Pattern excludeRegex;

    protected Map<String, ManagedField> managedFields = new LinkedHashMap<>();
    protected List<String> managedEmbeddedProperties = new ArrayList<>();
    protected Map<String, AbstractField<?, ?>> dataFields = new LinkedHashMap<>();

    protected InstanceContainer<E> instanceContainer;
    protected List<E> items;

    protected boolean commitPerformed = false;
    protected FetchPlan fetchPlan;

    @Override
    public void setBulkEditorContext(BulkEditorContext<E> context) {
        this.context = context;

        if (!Strings.isNullOrEmpty(context.getExclude())) {
            excludeRegex = Pattern.compile(context.getExclude());
        }

        for (ManagedField managedField : getManagedFields()) {
            managedFields.put(managedField.getFqn(), managedField);
        }

        MetaClass metaClass = context.getMetaClass();

        fetchPlan = createFetchPlan(context.getMetaClass());
        loadItems();

//        E instance = metadata.create(metaClass.getJavaClass());
//        createEmbeddedFields(metaClass, instance, null);

//        instanceContainer = dataComponents.createInstanceContainer(metaClass.getJavaClass());
//        getViewData().registerContainer(metaClass.getName() + "Dc", instanceContainer);
//        createNestedDataContainers(metaClass, instanceContainer, null);

//        instanceContainer.setItem(instance);
//        instanceContainer.setFetchPlan(fetchPlan);

        createDataComponents();
    }

    protected List<ManagedField> getManagedFields() {
        MetaClass metaClass = context.getMetaClass();

        return getManagedFields(metaClass, null, null);
    }

    protected List<ManagedField> getManagedFields(MetaClass metaClass,
                                                  @Nullable String fqnPrefix,
                                                  @Nullable String localePrefix) {
        List<ManagedField> managedFields = new ArrayList<>();
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            if (isManagedAttribute(metaClass, metaProperty)) {
                String fqn = generateFqn(metaProperty, fqnPrefix);
                String propertyCaption = generatePropertyCaption(metaClass, metaProperty, localePrefix);

                if (!metadataTools.isEmbedded(metaProperty)) {
                    managedFields.add(new ManagedField(fqn, metaProperty, propertyCaption, fqnPrefix));
                } else {
                    List<ManagedField> nestedFields = getManagedFields(metaProperty, fqn, propertyCaption);
                    if (!nestedFields.isEmpty()) {
                        managedEmbeddedProperties.add(fqn);
                    }
                    managedFields.addAll(nestedFields);
                }
            }
        }

        return managedFields;
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

        return excludeRegex == null || !excludeRegex.matcher(metaProperty.getName()).matches();
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

    protected boolean isByteArray(MetaProperty metaProperty) {
        return metaProperty.getRange().asDatatype().getJavaClass().equals(byte[].class);
    }

    protected boolean isUuid(MetaProperty metaProperty) {
        return metaProperty.getRange().asDatatype().getJavaClass().equals(UUID.class);
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

    protected String generatePropertyCaption(MetaClass metaClass,
                                             MetaProperty metaProperty,
                                             @Nullable String localePrefix) {
        String propertyCaption = messageTools.getPropertyCaption(metaClass, metaProperty.getName());
        if (!Strings.isNullOrEmpty(localePrefix)) {
            propertyCaption = localePrefix + " " + propertyCaption;
        }

        return propertyCaption;
    }

    protected List<ManagedField> getManagedFields(MetaProperty embeddedProperty,
                                                  String fqnPrefix,
                                                  String localePrefix) {
        MetaClass metaClass = embeddedProperty.getRange().asClass();

        return getManagedFields(metaClass, fqnPrefix, localePrefix);
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

    protected String generateFqn(MetaProperty metaProperty, @Nullable String fqnPrefix) {
        String fqn = metaProperty.getName();
        if (!Strings.isNullOrEmpty(fqnPrefix)) {
            fqn = fqnPrefix + "." + fqn;
        }
        return fqn;
    }

    protected void loadItems() {
        BulkEditorDataProvider.LoadDescriptor<E> ld = new BulkEditorDataProvider.LoadDescriptor<>(context.getSelectedItems(),
                context.getMetaClass(), fetchPlan);

        items = bulkEditorDataProvider.reload(ld);
    }

    protected void createDataComponents() {
        if (managedFields.isEmpty()) {
            infoLabel.setText(messageBundle.getMessage("bulk.noEditableProperties"));
            applyButton.setVisible(false);
            applyChanges.setVisible(false);
            return;
        }

        List<ManagedField> editFields = new ArrayList<>(managedFields.values());

        // sort fields
        Comparator<ManagedField> comparator = createManagedFieldComparator(editFields);
        editFields.sort(comparator);

        HorizontalLayout fieldsLayout = createFieldsLayout();

        int fromField;
        int toField = 0;
        int addedColumns = 0;
        ColumnsMode columnsMode = getColumnsMode();

        for (int col = 0; col < columnsMode.getColumnsCount(); col++) {
            fromField = toField;
            toField += getFieldsCountForColumn(
                    editFields.size() - toField,
                    columnsMode.getColumnsCount() - col);

            VerticalLayout column = createColumnLayout();

            for (int fieldIndex = fromField; fieldIndex < toField; fieldIndex++) {
                HorizontalLayout row = createRow(editFields, fieldIndex);
                column.add(row);
            }

            fieldsLayout.add(column);
            // if there is no fields remain
            if (editFields.size() - toField == 0) {
                addedColumns = col + 1;
                break;
            }
        }

        fieldsLayout.add(COLUMN_COUNT_STYLENAME + addedColumns);
        scrollerContentBox.add(fieldsLayout);

        focusFirstPossibleField(dataFields);
    }


    protected HorizontalLayout createFieldsLayout() {
//        CssLayout fieldsLayout = uiComponents.create(CssLayout.NAME);
        HorizontalLayout fieldsLayout = uiComponents.create(HorizontalLayout.class);
        fieldsLayout.addClassName("jmix-bulk-editor-fields-layout");
//        fieldsLayout.setStyleName("jmix-bulk-editor-fields-layout");
        fieldsLayout.setWidthFull();
        fieldsLayout.setHeightFull();
        return fieldsLayout;
    }

    protected ColumnsMode getColumnsMode() {
        return context.getColumnsMode() != null
                ? context.getColumnsMode()
                : DEFAULT_COLUMNS_MODE;
    }

    protected int getFieldsCountForColumn(int remainFields, int remainColumns) {
        int fieldsForColumn = remainFields / remainColumns;
        return remainFields % remainColumns == 0 ? fieldsForColumn : ++fieldsForColumn;
    }

    protected VerticalLayout createColumnLayout() {
        VerticalLayout column = uiComponents.create(VerticalLayout.class);
        column.addClassName("jmix-bulk-editor-column");
//        column.setWidth(Component.AUTO_SIZE);
        return column;
    }

    protected HorizontalLayout createRow(List<ManagedField> editFields, int fieldIndex) {
        ManagedField field = editFields.get(fieldIndex);

        HorizontalLayout row = createRowLayout();

        Span label = createLabel(field);
        row.add(label);

//        InstanceContainer<?> fieldDc = instanceContainer;
        MetaProperty metaProperty = field.getMetaProperty();

        // field owner metaclass is embeddable only if field domain embeddable,
        // so we can check field domain
//        if (metadataTools.isJpaEmbeddable(metaProperty.getDomain())
//                && field.getParentFqn() != null) {
//            fieldDc = getViewData().getContainer(field.getParentFqn() + "Dc");
//        }


        Validator customValidator = context.getFieldValidators().get(field.getFqn());
        AbstractField<?, ?> editField = createField(metaProperty, customValidator/*, fieldDc*/);
        dataFields.put(field.getFqn(), editField);

//        if (isEntityPickerWrapperNeeded(editField)) {
//            CssLayout wrapper = uiComponents.create(CssLayout.NAME);
//            wrapper.setStyleName("jmix-bulk-editor-picker-field-wrapper");
//            wrapper.add(editField);
//            row.add(wrapper);
//        } else {
        row.add(editField);
//        }

        JmixButton clearButton = createClearButton(editField);

        row.add(clearButton);
        return row;
    }

    protected HorizontalLayout createRowLayout() {
//        CssLayout row = uiComponents.create(CssLayout.NAME);
        HorizontalLayout row = uiComponents.create(HorizontalLayout.class);
        row.addClassName("jmix-bulk-editor-row");
        row.setWidth("100%");
        return row;
    }

    protected Span createLabel(ManagedField field) {
        Span label = uiComponents.create(Span.class);
        label.setText(field.getLocalizedName());
        label.addClassName("jmix-bulk-editor-label");
        return label;
    }

    protected AbstractField<?, ?> createField(MetaProperty metaProperty, @Nullable Validator customFieldValidator/*, InstanceContainer<?> fieldDc*/) {
        ComponentGenerationContext generationContext =
                new ComponentGenerationContext(metaProperty.getDomain(), metaProperty.getName());
//        generationContext.setValueSource(new ContainerValueSource<>(fieldDc, metaProperty.getName()));
        generationContext.setTargetClass(getClass());
        AbstractField<?, ?> field = (AbstractField<?, ?>) uiComponentsGenerator.generate(generationContext);

        field.addClassName("jmix-bulk-editor-field");
        if (field instanceof SupportsValidation<?> supportsValidation) {
            BulkEditBeanPropertyValidator beanValidator = getBeanPropertyValidator(metaProperty);
            supportsValidation.addValidator(beanValidator);
            if (customFieldValidator != null) {
                supportsValidation.addValidator(customFieldValidator);
            }
        }
        return field;
    }

    @Nullable
    protected BulkEditBeanPropertyValidator getBeanPropertyValidator(MetaProperty metaProperty) {
        MetaClass propertyEnclosingMetaClass = metaProperty.getDomain();
        Class<?> enclosingJavaClass = propertyEnclosingMetaClass.getJavaClass();

        BeanDescriptor beanDescriptor = validator.getConstraintsForClass(enclosingJavaClass);
        if (beanDescriptor.isBeanConstrained()) {
            return beanValidatorProvider.getObject(enclosingJavaClass, metaProperty.getName());
        } else {
            return null;
        }
    }

    protected JmixButton createClearButton(AbstractField<?, ?> field) {
        JmixButton clearButton = uiComponents.create(JmixButton.class);
        clearButton.setIcon(VaadinIcon.TRASH.create());
        clearButton.setText("");
        Tooltip.forComponent(clearButton).setText(messageBundle.getMessage("bulk.clearAttribute"));

        if (field instanceof HasRequired hasRequired && hasRequired.isRequired()) {
            // hidden component for correctly showing layout
            clearButton.addClassName("jmix-bulk-editor-spacer");
        } else {
            clearButton.addClickListener(createClearButtonClickListener(field));
        }

        return clearButton;
    }

    protected ComponentEventListener<ClickEvent<Button>> createClearButtonClickListener(AbstractField<?, ?> editField) {
        return e -> {
            editField.setEnabled(!editField.isEnabled());
            Button button = e.getSource();

            button.setIcon(editField.isEnabled() ? VaadinIcon.TRASH.create() : VaadinIcon.EDIT.create());
            Tooltip.forComponent(button).setText(messageBundle.getMessage(editField.isEnabled()
                    ? "bulk.clearAttribute"
                    : "bulk.editAttribute"
            ));

            if (!editField.isEnabled()) {
                editField.clear();
            }
        };
    }

    protected void focusFirstPossibleField(Map<String, AbstractField<?, ?>> dataFields) {
        dataFields.values().stream()
                .filter(field -> field instanceof Focusable<?>)
                .findFirst()
                .ifPresent(field -> ((Focusable<?>) field).focus());
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

/*    protected boolean isEntityPickerWrapperNeeded(Field<?> field) {
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
    }*/

/*    protected void createNestedDataContainers(MetaClass metaClass,
                                              InstanceContainer<?> parent, @Nullable String fqnPrefix) {
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            if (metadataTools.isEmbedded(metaProperty)) {
                String fqn = generateFqn(metaProperty, fqnPrefix);

                if (managedEmbeddedProperties.contains(fqn)) {
                    MetaClass propertyMetaClass = metaProperty.getRange().asClass();

                    InstancePropertyContainer<Object> instanceContainer =
//                            dataComponents.createInstanceContainer(metaClass.getJavaClass(),
//                                    parent, metaProperty.getName());
                            dataComponents.createInstanceContainer(propertyMetaClass.getJavaClass(),
                                    parent, metaProperty.getName());
                    getViewData().registerContainer(fqn + "Dc", instanceContainer);
                    createNestedDataContainers(propertyMetaClass, instanceContainer, fqn);
                }
            }
        }
    }*/

    protected boolean isTenantMetaProperty(MetaProperty metaProperty) {
        return metaProperty.getAnnotatedElement().getAnnotation(TenantId.class) != null;
    }

    protected void addFetchPlanProperties(FetchPlanBuilder builder, MetaProperty metaProperty, String fqn) {
        switch (metaProperty.getType()) {
            case DATATYPE, ENUM -> builder.add(metaProperty.getName());
            case EMBEDDED, ASSOCIATION, COMPOSITION -> {
                MetaClass propMetaClass = metaProperty.getRange().asClass();
                FetchPlan propFetchPlan = metadataTools.isEmbedded(metaProperty)
                        ? createEmbeddedFetchPlan(propMetaClass, fqn)
                        : fetchPlanRepository.getFetchPlan(propMetaClass, FetchPlan.INSTANCE_NAME);

                // In some cases JPA loads extended entities as instance of base
                // class which leads to ClassCastException loading property lazy
                // prevents this from happening
                builder.add(metaProperty.getName(), propFetchPlanBuilder ->
                        propFetchPlanBuilder.addFetchPlan(propFetchPlan));
            }
            default -> throw new IllegalStateException("unknown property type");
        }
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
/*
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
    }*/

    @Subscribe("cancelChanges")
    protected void onCancelChanges(ActionPerformedEvent event) {
        close(StandardOutcome.CLOSE);
    }

    @Subscribe
    protected void onBeforeClose(BeforeCloseEvent event) {
        preventUnsavedChanges(event);
    }

    protected void preventUnsavedChanges(BeforeCloseEvent event) {
        CloseAction action = event.getCloseAction();

        if (action instanceof ChangeTrackerCloseAction changeTrackerCloseAction
                && changeTrackerCloseAction.isCheckForUnsavedChanges()
                && hasChanges()) {
            UnknownOperationResult result = new UnknownOperationResult();

            viewValidation.showUnsavedChangesDialog(this)
                    .onDiscard(() -> result.resume(closeWithDiscard()))
                    .onCancel(result::fail);

            event.preventClose(result);
        }
    }

    protected boolean hasChanges() {
        if (!commitPerformed) {
            for (Map.Entry<String, AbstractField<?, ?>> fieldEntry : dataFields.entrySet()) {
                AbstractField<?, ?> field = fieldEntry.getValue();
                if (isFieldChanged(field)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isFieldChanged(AbstractField<?, ?> field) {
        return !field.isEnabled() || !field.isEmpty();
    }

    public OperationResult closeWithDiscard() {
        return close(StandardOutcome.DISCARD);
    }

    @Subscribe("applyChanges")
    protected void onApplyChanges(ActionPerformedEvent event) {
        closeWithCommit();
    }

    public void closeWithCommit() {
        ValidationErrors validationErrors = validateUiComponents();
        if (validationErrors.isEmpty()) {
            updateItemValues();
            applyModelValidators(validationErrors);
        }

        if (!validationErrors.isEmpty()) {
            viewValidation.showValidationErrors(validationErrors);
            loadItems();
        } else {
            List<String> fields = new ArrayList<>();
            for (Map.Entry<String, AbstractField<?, ?>> fieldEntry : dataFields.entrySet()) {
                AbstractField<?, ?> field = fieldEntry.getValue();
                if (isFieldChanged(field)) {
                    String localizedName = managedFields.get(fieldEntry.getKey()).getLocalizedName();
                    fields.add("- " + localizedName);
                }
            }

            if (fields.isEmpty()) {
                notifications.create(messageBundle.getMessage("bulk.noChanges"))
                        .show();
            } else {
                showConfirmDialogOrCommit(fields);
            }
        }
    }

    protected ValidationErrors validateUiComponents() {
        List<Component> components = dataFields.values().stream().map(f -> (Component) f).toList();
        return viewValidation.validateUiComponents(components);
    }

    protected void applyModelValidators(ValidationErrors errors) {
        for (Validator<E> validator : context.getModelValidators()) {
            for (E item : items) {
                try {
                    validator.accept(item);
                } catch (ValidationException e) {
                    errors.add(e.getDetailsMessage());
                }
            }
        }
    }

    protected void updateItemValues() {
        for (Map.Entry<String, AbstractField<?, ?>> fieldEntry : dataFields.entrySet()) {
            AbstractField<?, ?> field = fieldEntry.getValue();

            if (!field.isEnabled() || isFieldChanged(field)) {
                for (E item : items) {
                    ensureEmbeddedPropertyCreated(item, fieldEntry.getKey());
                    Object value;
                    if (!field.isEnabled()) {
                        value = null;
                    } else {
                        if (field instanceof SupportsTypedValue<?, ?, ?, ?> supportsTypedValue) {
                            value = supportsTypedValue.getTypedValue();
                        } else {
                            value = field.getValue();
                        }
                    }
                    EntityValues.setValueEx(item, fieldEntry.getKey(), value);
                }
            }
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

    protected void showConfirmDialogOrCommit(List<String> fields) {
        if (context.isUseConfirmDialog()) {
            dialogs.createOptionDialog()
                    .withHeader(messageBundle.getMessage("bulk.confirmation"))
                    .withText(messageBundle.formatMessage("bulk.applyConfirmation",
                            items.size(), StringUtils.join(fields, "\n")))
                    .withActions(new DialogAction(Type.OK)
                                    .withText(messages.getMessage("actions.Apply"))
                                    .withHandler(event -> commitBulkChanges()),
                            new DialogAction(Type.CANCEL)
                                    .withVariant(ActionVariant.PRIMARY)
                                    .withHandler(e -> loadItems()))
                    .open();
        } else {
            commitBulkChanges();
        }
    }

    protected void commitBulkChanges() {
        List<String> fields = new ArrayList<>();

        for (Map.Entry<String, AbstractField<?, ?>> fieldEntry : dataFields.entrySet()) {
            AbstractField<?, ?> field = fieldEntry.getValue();
            if (isFieldChanged(field)) {
                fields.add(managedFields.get(fieldEntry.getKey()).getFqn());
            }
        }

        EntitySet saved = dataManager.save(new SaveContext().saving(items));

        Logger logger = LoggerFactory.getLogger(BulkEditView.class);
        logger.info("Applied bulk editing for {} entries of {}. Changed properties: {}",
                saved.size(), context.getMetaClass(), StringUtils.join(fields, ", "));

        notifications.create(messageBundle.formatMessage("bulk.successMessage", saved.size()))
                .show();

        commitPerformed = true;

        close(StandardOutcome.SAVE);
    }

    protected static class ManagedField {

        protected final String fqn;
        protected final String parentFqn;

        protected final String localizedName;

        protected final MetaProperty metaProperty;

        public ManagedField(String fqn, MetaProperty metaProperty, String localizedName, @Nullable String parentFqn) {
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

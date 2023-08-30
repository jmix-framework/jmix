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
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
import io.jmix.flowui.bulk.BulkEditorDataProvider.LoadDescriptor;
import io.jmix.flowui.component.ComponentGenerationContext;
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
import jakarta.validation.constraints.NotNull;
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
@DialogMode(resizable = true, width = "60em", height = "40em", minWidth = "30em")
public class BulkEditView<E> extends StandardView implements BulkEditorController<E> {

    protected static final String COLUMN_COLLAPSE_MIN_WIDTH = "52em";
    protected static final String FIELD_MIN_WIDTH = "10em";

    protected static final ColumnsMode DEFAULT_COLUMNS_MODE = TWO_COLUMNS;

    @ViewComponent
    protected JmixButton applyButton;
    @ViewComponent
    protected Action applyChanges;
    @ViewComponent
    protected Div infoDiv;
    @ViewComponent
    protected FormLayout fieldLayout;

    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected BulkEditorDataProvider bulkEditorDataProvider;
    @Autowired
    protected DataComponents dataComponents;
    @Autowired
    protected DataManager dataManager;
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

    protected List<E> items;

    protected boolean commitPerformed = false;
    protected FetchPlan fetchPlan;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        if (context != null) {
            if (!Strings.isNullOrEmpty(context.getExclude())) {
                excludeRegex = Pattern.compile(context.getExclude());
            }

            for (ManagedField managedField : getManagedFields()) {
                managedFields.put(managedField.getFqn(), managedField);
            }

            fetchPlan = createFetchPlan(context.getMetaClass());
            loadItems();

            createDataComponents();
        }
    }

    @Override
    public void setBulkEditorContext(BulkEditorContext<E> context) {
        this.context = context;
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
            String fqn = generateFqn(metaProperty, fqnPrefix);
            String propertyCaption = generatePropertyCaption(metaClass, metaProperty, localePrefix);

            if (!metadataTools.isEmbedded(metaProperty)) {
                if (isManagedAttribute(metaClass, metaProperty, fqn)) {
                    managedFields.add(new ManagedField(fqn, metaProperty, propertyCaption, fqnPrefix));
                }
            } else {
                List<ManagedField> nestedFields = getManagedFields(metaProperty, fqn, propertyCaption);
                if (!nestedFields.isEmpty()) {
                    managedEmbeddedProperties.add(fqn);
                }
                managedFields.addAll(nestedFields);
            }
        }

        return managedFields;
    }

    protected boolean isManagedAttribute(MetaClass metaClass, MetaProperty metaProperty, String fqn) {
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
            return includeProperties.contains(fqn);
        }

        return excludeRegex == null || !excludeRegex.matcher(fqn).matches();
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

    protected void loadItems() {
        LoadDescriptor<E> ld = new LoadDescriptor<>(context.getSelectedItems(), context.getMetaClass(), fetchPlan);
        items = bulkEditorDataProvider.reload(ld);
    }

    protected void createDataComponents() {
        if (managedFields.isEmpty()) {
            infoDiv.setText(messageBundle.getMessage("bulk.noEditableProperties"));
            applyButton.setVisible(false);
            applyChanges.setVisible(false);
            return;
        }

        List<ManagedField> editFields = new ArrayList<>(managedFields.values());

        Comparator<ManagedField> comparator = createManagedFieldComparator(editFields);
        editFields.sort(comparator);

        setupFormLayout();

        for (ManagedField editField : editFields) {
            Component fieldComponent = createFieldComponent(editField);
            fieldLayout.addFormItem(fieldComponent, editField.getLocalizedName());
        }
        focusFirstPossibleField(dataFields);
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

    protected void setupFormLayout() {
        ColumnsMode contextColumnsMode = context.getColumnsMode();
        ColumnsMode columnsMode = contextColumnsMode != null ? contextColumnsMode : DEFAULT_COLUMNS_MODE;

        List<FormLayout.ResponsiveStep> responsiveSteps = new ArrayList<>();
        FormLayout.ResponsiveStep step1 = new FormLayout.ResponsiveStep("0em", 1,
                FormLayout.ResponsiveStep.LabelsPosition.ASIDE);
        responsiveSteps.add(step1);
        if (TWO_COLUMNS == columnsMode) {
            FormLayout.ResponsiveStep step2 = new FormLayout.ResponsiveStep(COLUMN_COLLAPSE_MIN_WIDTH, 2,
                    FormLayout.ResponsiveStep.LabelsPosition.ASIDE);
            responsiveSteps.add(step2);
        }
        fieldLayout.setResponsiveSteps(responsiveSteps);
        fieldLayout.addClassName("jmix-bulk-edit-view-form-layout");
    }

    protected Component createFieldComponent(ManagedField managedField) {
        HorizontalLayout container = uiComponents.create(HorizontalLayout.class);

        AbstractField<?, ?> editField = createField(managedField);
        dataFields.put(managedField.getFqn(), editField);
        container.add(editField);

        JmixButton clearButton = createClearButton(editField, isFieldRequired(managedField));
        container.add(clearButton);

        return container;
    }

    protected AbstractField<?, ?> createField(ManagedField managedField) {
        MetaProperty metaProperty = managedField.getMetaProperty();
        ComponentGenerationContext generationContext =
                new ComponentGenerationContext(metaProperty.getDomain(), metaProperty.getName());
        generationContext.setTargetClass(getClass());
        AbstractField<?, ?> field = (AbstractField<?, ?>) uiComponentsGenerator.generate(generationContext);

        if (field instanceof SupportsValidation<?> supportsValidation) {
            BulkEditBeanPropertyValidator beanValidator = getBeanPropertyValidator(metaProperty);
            if (beanValidator != null) {
                supportsValidation.addValidator(beanValidator);
            }
            //noinspection rawtypes
            Validator customValidator = context.getFieldValidators().get(managedField.getFqn());
            if (customValidator != null) {
                //noinspection unchecked
                supportsValidation.addValidator(customValidator);
            }
        }
        if (field instanceof HasSize hasSize) {
            hasSize.setMinWidth(FIELD_MIN_WIDTH);
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

    protected boolean isFieldRequired(ManagedField managedField) {
        MetaProperty metaProperty = managedField.getMetaProperty();
        boolean requiredFromMetaProperty = metaProperty.isMandatory();

        Object notNullUiComponent = metaProperty.getAnnotations()
                .get(NotNull.class.getName() + "_notnull_ui_component");
        boolean requiredFromAnnotations = Boolean.TRUE.equals(notNullUiComponent);

        return requiredFromMetaProperty || requiredFromAnnotations;
    }

    protected JmixButton createClearButton(AbstractField<?, ?> field, boolean isFieldRequired) {
        JmixButton button = uiComponents.create(JmixButton.class);
        button.setIcon(VaadinIcon.TRASH.create());

        if (isFieldRequired) {
            button.getElement().getStyle().set("visibility", "hidden");
        } else {
            button.addClickListener(createClearButtonClickListener(field));
            Tooltip.forComponent(button).setText(messageBundle.getMessage("bulk.clearAttribute"));
        }
        return button;
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

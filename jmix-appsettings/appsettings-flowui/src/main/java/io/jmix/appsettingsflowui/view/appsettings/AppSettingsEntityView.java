/*
 * Copyright 2022 Haulmont.
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

package io.jmix.appsettingsflowui.view.appsettings;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.Route;
import io.jmix.appsettings.AppSettings;
import io.jmix.appsettings.AppSettingsTools;
import io.jmix.appsettings.entity.AppSettingsEntity;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.core.metamodel.model.Range;
import io.jmix.data.PersistenceHints;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.accesscontext.FlowuiEntityAttributeContext;
import io.jmix.flowui.accesscontext.FlowuiEntityContext;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.UiComponentsGenerator;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.util.OperationResult;
import io.jmix.flowui.util.UnknownOperationResult;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.RouteSupport;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.Convert;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "app-settings", layout = DefaultMainViewParent.class)
@ViewController("appSettings.view")
@ViewDescriptor("app-settings-entity-view.xml")
@DialogMode(width = "50em", height = "37.5em")
public class AppSettingsEntityView extends StandardView {

    protected static final int MAX_CAPTION_LENGTH = 50;
    protected static final String SELECT_APP_SETTINGS_ENTITY_QUERY = "select e from %s e where e.id = 1";
    protected static final String QUERY_PARAM_SETTINGS_CLASS = "settings";
    protected static final int FORM_COLUMNS = 3;

    @ViewComponent
    protected ComboBox<MetaClass> entitySelector;
    @ViewComponent
    protected Scroller fieldsScroller;
    @ViewComponent
    protected HorizontalLayout buttonsPanel;

    @Autowired
    protected AppSettings appSettings;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected UnconstrainedDataManager dataManager;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected DataComponents dataComponents;
    @Autowired
    protected ViewValidation viewValidation;
    @Autowired
    protected Messages messages;
    @Autowired
    protected AppSettingsTools appSettingsTools;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected UiComponentsGenerator uiComponentsGenerator;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected RouteSupport routeSupport;

    protected DataContext dataContext;
    protected MetaClass currentMetaClass;
    protected MetaClass prevMetaClass;
    protected boolean isNewEntityModified = false;
    protected boolean isEntityChangePrevented = false;
    protected Object entityToEdit;

    @Subscribe
    public void onInit(InitEvent event) {
        entitySelector.setItemLabelGenerator((ItemLabelGenerator<MetaClass>) item ->
                messageTools.getEntityCaption(item) + " (" + item.getName() + ")");
        entitySelector.setItems(getEntitySelectorItems());
    }

    @Subscribe("entitySelector")
    protected void onEntitySelectorValueChange(AbstractField.ComponentValueChangeEvent<ComboBox<MetaClass>, MetaClass> e) {
        fieldsScroller.setVisible(e.getValue() != null);

        if (isEntityChangePrevented) {
            isEntityChangePrevented = false;
            return;
        }

        prevMetaClass = e.getOldValue();
        currentMetaClass = e.getValue();
        getUI().ifPresent(ui -> routeSupport.setQueryParameter(
                ui,
                QUERY_PARAM_SETTINGS_CLASS,
                currentMetaClass.getName()
        ));

        if (dataContext != null && hasUnsavedChanges()) {
            handleEntitySelectorValueChangeWithUnsavedChanges();
            return;
        }

        initEntityPropertiesLayout();
    }

    @Subscribe
    public void beforeShow(BeforeShowEvent event) {
        if (currentMetaClass != null) {
            entitySelector.setValue(currentMetaClass);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> parameters = event.getLocation().getQueryParameters().getParameters();

        if (parameters.containsKey(QUERY_PARAM_SETTINGS_CLASS)) {
            parameters.get(QUERY_PARAM_SETTINGS_CLASS).stream()
                    .findAny()
                    .ifPresent(this::setSettingsClass);
        }
        super.beforeEnter(event);
    }

    @Subscribe
    public void onBeforeClose(BeforeCloseEvent event) {
        preventUnsavedChanges(event);
    }

    protected void preventUnsavedChanges(BeforeCloseEvent event) {
        CloseAction action = event.getCloseAction();

        if (dataContext != null && hasUnsavedChanges()) {
            UnknownOperationResult result = new UnknownOperationResult();

            if (action instanceof NavigateCloseAction) {
                BeforeLeaveEvent beforeLeaveEvent = ((NavigateCloseAction) action).getBeforeLeaveEvent();
                BeforeLeaveEvent.ContinueNavigationAction navigationAction = beforeLeaveEvent.postpone();

                viewValidation.showSaveConfirmationDialog(this)
                        .onSave(() -> result.resume(navigateWithSave(navigationAction)))
                        .onDiscard(() -> result.resume(navigateWithDiscard(navigationAction)))
                        .onCancel(result::fail);
            }

            event.preventClose(result);
        }
    }

    protected OperationResult navigateWithDiscard(BeforeLeaveEvent.ContinueNavigationAction navigationAction) {
        return navigate(navigationAction, StandardOutcome.DISCARD.getCloseAction());
    }

    protected OperationResult navigateWithSave(BeforeLeaveEvent.ContinueNavigationAction navigationAction) {
        return saveChanges()
                .compose(() -> navigate(navigationAction, StandardOutcome.SAVE.getCloseAction()));
    }

    protected OperationResult navigate(BeforeLeaveEvent.ContinueNavigationAction navigationAction,
                                     CloseAction closeAction) {
        navigationAction.proceed();

        AfterCloseEvent afterCloseEvent = new AfterCloseEvent(this, closeAction);
        fireEvent(afterCloseEvent);

        return OperationResult.success();
    }

    public void setSettingsClass(String metaClassName) {
        this.currentMetaClass = metadataTools.getAllJpaEntityMetaClasses().stream()
                .filter(entityClass -> entityClass.getName().equals(metaClassName))
                .findFirst().orElse(null);
    }

    protected void initEntityPropertiesLayout() {
        dataContext = dataComponents.createDataContext();
        getViewData().setDataContext(dataContext);
        showEntityPropertiesLayout();

        dataContext.addChangeListener(changeEvent -> {
            if (entityStates.isNew(changeEvent.getEntity())) {
                this.isNewEntityModified = true;
            }
        });
    }

    protected List<MetaClass> getEntitySelectorItems() {
        return metadataTools.getAllJpaEntityMetaClasses().stream()
                .filter(this::isApplicationSettingsEntity)
                .filter(metaClass -> !metadataTools.isSystemLevel(metaClass))
                .filter(this::readPermitted)
                .collect(Collectors.toList());
    }

    protected boolean readPermitted(MetaClass metaClass) {
        FlowuiEntityContext entityContext = new FlowuiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);
        return entityContext.isViewPermitted();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void showEntityPropertiesLayout() {
        fieldsScroller.setContent(null);
        if (currentMetaClass != null) {
            InstanceContainer container = initInstanceContainerWithDbEntity();
            FormLayout formLayout = createFormLayout(container);
            fieldsScroller.setContent(formLayout);

            container.setItem(entityToEdit);

            buttonsPanel.setVisible(true);
        }
    }

    protected FormLayout createFormLayout(InstanceContainer<?> container) {
        MetaClass metaClass = container.getEntityMetaClass();
        List<MetaProperty> metaProperties = collectMetaProperties(metaClass, entityToEdit).stream()
                .sorted(Comparator.comparing(MetadataObject::getName))
                .collect(Collectors.toList());

        FormLayout formLayout = uiComponents.create(FormLayout.class);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("40em", FORM_COLUMNS));

        for (MetaProperty metaProperty : metaProperties) {
            addPropertiesLayoutItem(container, formLayout, metaProperty);
        }

        return formLayout;
    }

    protected List<MetaProperty> collectMetaProperties(MetaClass metaClass, Object item) {
        List<MetaProperty> result = new ArrayList<>();
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            switch (metaProperty.getType()) {
                case DATATYPE:
                case ENUM:
                    //skip system properties
                    if (metadataTools.isSystem(metaProperty)) {
                        continue;
                    }
                    if (metaProperty.getType() != MetaProperty.Type.ENUM
                            && (metaProperty.getRange().asDatatype().getJavaClass().equals(byte[].class) ||
                            metaProperty.getRange().asDatatype().getJavaClass().equals(UUID.class))) {
                        continue;
                    }
                    if (metadataTools.isAnnotationPresent(item, metaProperty.getName(), Convert.class)) {
                        continue;
                    }
                    result.add(metaProperty);
                    break;
                case COMPOSITION:
                case ASSOCIATION:
                    if (!isMany(metaProperty)) {
                        result.add(metaProperty);
                    }
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    protected boolean isMany(MetaProperty metaProperty) {
        return metaProperty.getRange().getCardinality().isMany();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void addPropertiesLayoutItem(InstanceContainer<?> container,
                                           FormLayout formLayout,
                                           MetaProperty metaProperty) {
        MetaClass metaClass = container.getEntityMetaClass();
        Range range = metaProperty.getRange();

        FlowuiEntityAttributeContext attributeContext = new FlowuiEntityAttributeContext(metaClass,
                metaProperty.getName());
        accessManager.applyRegisteredConstraints(attributeContext);
        if (!attributeContext.canView()) {
            return;
        }

        if (range.isClass()) {
            FlowuiEntityContext entityContext = new FlowuiEntityContext(range.asClass());
            accessManager.applyRegisteredConstraints(entityContext);
            if (!entityContext.isViewPermitted()) {
                return;
            }
        }

        //add label
        Label fieldLabel = uiComponents.create(Label.class);
        fieldLabel.setText(getPropertyCaption(metaClass, metaProperty));
        formLayout.add(fieldLabel);

        //current field
        ValueSource<?> valueSource = new ContainerValueSource<>(container, metaProperty.getName());
        ComponentGenerationContext componentContext = new ComponentGenerationContext(metaClass, metaProperty.getName());
        componentContext.setValueSource(valueSource);
        AbstractField currentField = (AbstractField) uiComponentsGenerator.generate(componentContext);
        ((HasLabel) currentField).setLabel(messages.getMessage(this.getClass(), "currentValueLabel"));
        formLayout.add(currentField);

        //default value
        ComponentGenerationContext componentContextForDefaultField = new ComponentGenerationContext(metaClass,
                metaProperty.getName());
        ValueSource<?> valueSourceForDefaultField =
                new ContainerValueSource<>(dataComponents.createInstanceContainer(metaClass.getJavaClass()),
                        metaProperty.getName());
        componentContextForDefaultField.setValueSource(valueSourceForDefaultField);
        AbstractField defaultValueField = (AbstractField)
                uiComponentsGenerator.generate(componentContextForDefaultField);
        ((HasLabel) defaultValueField).setLabel(messages.getMessage(this.getClass(), "defaultValueLabel"));
        if (defaultValueField instanceof SupportsTypedValue) {
            ((SupportsTypedValue<?, ?, Object, ?>) defaultValueField)
                    .setTypedValue(appSettingsTools.getDefaultPropertyValue(metaClass.getJavaClass(),
                            metaProperty.getName()));
        } else {
            defaultValueField.setValue(appSettingsTools.getDefaultPropertyValue(metaClass.getJavaClass(),
                    metaProperty.getName()));
        }
        defaultValueField.setEnabled(false);
        formLayout.add(defaultValueField);

        Hr hr = uiComponents.create(Hr.class);
        formLayout.add(hr);
        formLayout.setColspan(hr, FORM_COLUMNS);
    }

    protected String getPropertyCaption(MetaClass metaClass, MetaProperty metaProperty) {
        String caption = messageTools.getPropertyCaption(metaClass, metaProperty.getName());
        if (caption.length() < MAX_CAPTION_LENGTH) {
            return caption;
        } else {
            return caption.substring(0, MAX_CAPTION_LENGTH);
        }
    }

    @SuppressWarnings({"rawtypes"})
    protected InstanceContainer initInstanceContainerWithDbEntity() {
        InstanceContainer container = dataComponents.createInstanceContainer(currentMetaClass.getJavaClass());
        entityToEdit = dataManager.load(currentMetaClass.getJavaClass())
                .query(String.format(SELECT_APP_SETTINGS_ENTITY_QUERY, currentMetaClass.getName()))
                .fetchPlan(fetchPlans.builder(currentMetaClass.getJavaClass()).addFetchPlan(FetchPlan.LOCAL).build())
                .hint(PersistenceHints.SOFT_DELETION, false)
                .optional()
                .orElse(null);
        if (entityToEdit == null) {
            entityToEdit = dataContext.create(currentMetaClass.getJavaClass());
        } else {
            entityToEdit = dataContext.merge(entityToEdit);
        }
        return container;
    }

    protected boolean isApplicationSettingsEntity(MetaClass metaClass) {
        return AppSettingsEntity.class.isAssignableFrom(metaClass.getJavaClass());
    }

    @Subscribe("saveAction")
    public void onSave(ActionPerformedEvent event) {
        saveChanges();
    }

    protected boolean hasUnsavedChanges() {
        for (Object modified : dataContext.getModified()) {
            if (!entityStates.isNew(modified)) {
                return true;
            }
        }
        //check whether "new" entity is modified in DataContext
        return isNewEntityModified;
    }

    protected void handleEntitySelectorValueChangeWithUnsavedChanges() {
        UnknownOperationResult result = new UnknownOperationResult();
        viewValidation.showUnsavedChangesDialog(this)
                .onDiscard(() -> result.resume(updateEntitySelectorValue(false)))
                .onCancel(() -> result.resume(updateEntitySelectorValue(true)));
    }

    protected OperationResult saveChanges() {
        ValidationErrors validationErrors = viewValidation.validateUiComponents(this.getContent());
        if (!validationErrors.isEmpty()) {
            viewValidation.showValidationErrors(validationErrors);
            return OperationResult.fail();
        }

        appSettings.save(((AppSettingsEntity) entityToEdit));
        dataContext.clear();
        isNewEntityModified = false;
        showSaveNotification();

        return OperationResult.success();
    }

    protected OperationResult updateEntitySelectorValue(boolean preventEntitySelectorChange) {
        isEntityChangePrevented = preventEntitySelectorChange;
        if (preventEntitySelectorChange) {
            entitySelector.setValue(prevMetaClass);
            return OperationResult.fail();
        } else {
            isNewEntityModified = false;
            initEntityPropertiesLayout();
            return OperationResult.success();
        }
    }

    protected void showSaveNotification() {
        String caption = messages.formatMessage(this.getClass(), "entitySaved",
                messageTools.getEntityCaption(currentMetaClass));
        notifications.create(caption)
                .withType(Notifications.Type.SUCCESS)
                .withPosition(Notification.Position.TOP_END)
                .show();
    }
}

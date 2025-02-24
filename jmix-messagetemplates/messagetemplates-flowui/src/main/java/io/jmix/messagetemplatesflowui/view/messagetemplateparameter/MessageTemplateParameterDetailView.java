/*
 * Copyright 2024 Haulmont.
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

package io.jmix.messagetemplatesflowui.view.messagetemplateparameter;

import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.grid.editor.EditorCloseEvent;
import com.vaadin.flow.component.grid.editor.EditorOpenEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.accesscontext.UiEntityContext;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.UiComponentsGenerator;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.editor.EditComponentGenerationContext;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer.ItemPropertyChangeEvent;
import io.jmix.flowui.view.*;
import io.jmix.messagetemplates.entity.MessageTemplateParameter;
import io.jmix.messagetemplates.entity.ParameterType;
import io.jmix.messagetemplatesflowui.MessageParameterLocalizationSupport;
import io.jmix.messagetemplatesflowui.MessageParameterResolver;
import io.jmix.messagetemplatesflowui.ObjectToStringConverter;
import io.jmix.messagetemplatesflowui.component.factory.MessageTemplateParameterGenerationContext;
import io.jmix.messagetemplatesflowui.view.messagetemplateparameter.model.MessageTemplateParameterLocalization;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@ViewController("msgtmp_MessageTemplateParameter.detail")
@ViewDescriptor("message-template-parameter-detail-view.xml")
@EditedEntityContainer("messageTemplateParameterDc")
@DialogMode(width = "40em", resizable = true)
public class MessageTemplateParameterDetailView extends StandardDetailView<MessageTemplateParameter> {

    @ViewComponent
    protected JmixComboBox<String> metaClassField;
    @ViewComponent
    protected JmixComboBox<String> enumerationField;
    @ViewComponent
    protected JmixCheckbox defaultDateIsCurrentField;
    @ViewComponent
    protected HorizontalLayout defaultValuePlaceholder;
    @ViewComponent
    protected JmixTabSheet mainTabSheet;
    @ViewComponent
    protected MessageBundle messageBundle;

    @ViewComponent
    protected DataGrid<MessageTemplateParameterLocalization> localizationDataGrid;
    @ViewComponent
    protected CollectionContainer<MessageTemplateParameterLocalization> parameterLocalizationDc;

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected UiComponentsGenerator uiComponentsGenerator;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected ViewValidation viewValidation;

    @Autowired
    protected ObjectToStringConverter objectToStringConverter;
    @Autowired
    protected MessageParameterResolver messageParameterResolver;
    @Autowired
    protected MessageParameterLocalizationSupport messageParameterLocalizationSupport;

    protected List<MessageTemplateParameter> parentTemplateParameters;

    @Subscribe
    public void onInit(InitEvent event) {
        initMetaClassField();
        initEnumerationField();
        initLocalizationDataGrid();
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        updateLayoutByParameterType(getEditedEntity().getType());
        setupParameterLocalization();
    }

    protected void setupParameterLocalization() {
        if (Strings.isNullOrEmpty(getEditedEntity().getLocalization())) {
            return;
        }

        List<MessageTemplateParameterLocalization> localization =
                messageParameterLocalizationSupport.convertLocalizationsToLocalizationEntities(getEditedEntity().getLocalization());

        parameterLocalizationDc.setItems(localization);
        localizationDataGrid.getActions().forEach(Action::refreshState);
    }

    @Subscribe
    public void onInitEntity(InitEntityEvent<MessageTemplateParameter> event) {
        event.getEntity().setType(ParameterType.TEXT);
    }

    @Subscribe(id = "messageTemplateParameterDc", target = Target.DATA_CONTAINER)
    public void onParameterDcItemPropertyChange(ItemPropertyChangeEvent<MessageTemplateParameter> event) {
        String property = event.getProperty();

        boolean typeChanged = "type".equalsIgnoreCase(property);
        boolean classChanged = "entityMetaClass".equalsIgnoreCase(property)
                || "enumerationClass".equalsIgnoreCase(property);
        boolean defaultDateIsCurrentChanged = "defaultDateIsCurrent".equalsIgnoreCase(property);
        MessageTemplateParameter editedEntity = getEditedEntity();

        if (typeChanged || classChanged || defaultDateIsCurrentChanged) {
            editedEntity.setDefaultValue(null);

            initDefaultValueField();
        }

        if (typeChanged) {
            editedEntity.setEntityMetaClass(null);
            editedEntity.setEnumerationClass(null);

            updateLayoutByParameterType(((ParameterType) event.getValue()));
        }

        if (defaultDateIsCurrentChanged) {
            initCurrentDateTimeField();
        }
    }

    protected void initMetaClassField() {
        Map<String, String> metaClassesItemssMap = new TreeMap<>();
        Collection<MetaClass> classes = metadata.getSession().getClasses();

        for (MetaClass clazz : classes) {
            if (!metadataTools.isSystemLevel(clazz)) {
                String caption = messageTools.getDetailedEntityCaption(clazz);
                metaClassesItemssMap.put(clazz.getName(), caption);
            }
        }

        ComponentUtils.setItemsMap(metaClassField, metaClassesItemssMap);
    }

    protected void initEnumerationField() {
        Map<String, String> enumsOptionsMap = new TreeMap<>();

        for (Class<?> enumClass : metadataTools.getAllEnums()) {
            String simpleEnumName = enumClass.getSimpleName();
            String enumLocalizedName = messages.getMessage(enumClass, simpleEnumName);

            enumsOptionsMap.put(enumClass.getCanonicalName(),
                    "%s (%s)".formatted(enumLocalizedName, simpleEnumName));
        }

        ComponentUtils.setItemsMap(enumerationField, enumsOptionsMap);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void initDefaultValueField() {
        defaultValuePlaceholder.removeAll();
        MessageTemplateParameter editedEntity = getEditedEntity();

        if (isDefaultValueUnavailable(editedEntity)) {
            return;
        }

        MessageTemplateParameterGenerationContext generationContext =
                new MessageTemplateParameterGenerationContext(editedEntity);
        Component defaultValueComponent = uiComponentsGenerator.generate(generationContext);

        if (defaultValueComponent instanceof SupportsTypedValue<?, ?, ?, ?> typedValueComponent) {
            typedValueComponent.addTypedValueChangeListener(this::onDefaultValueComponentValueChanged);
        } else if (defaultValueComponent instanceof HasValue<?, ?> hasValueComponent) {
            hasValueComponent.addValueChangeListener(this::onDefaultValueComponentValueChanged);
        }

        Class<?> parameterClass = messageParameterResolver.resolveClass(editedEntity);
        if (parameterClass != null && editedEntity.getDefaultValue() != null
                && defaultValueComponent instanceof HasValue hasValueComponent) {
            Object defaultValue = objectToStringConverter.convertFromString(
                    parameterClass, editedEntity.getDefaultValue()
            );

            UiComponentUtils.setValue(hasValueComponent, defaultValue);
        }

        if (defaultValueComponent instanceof HasLabel hasLabelComponent) {
            hasLabelComponent.setLabel(messageBundle.getMessage("defaultValueComponent.label"));
        }

        defaultValuePlaceholder.add(defaultValueComponent);
        MetaClass metaClass = metadata.getClass(MessageTemplateParameter.class);

        UiEntityContext uiEntityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(uiEntityContext);

        defaultValuePlaceholder.setVisible(uiEntityContext.isViewPermitted());
        boolean editPermitted = uiEntityContext.isEditPermitted();

        if (defaultValueComponent instanceof HasValue<?, ?> hasValueComponent) {
            hasValueComponent.setReadOnly(!editPermitted);
        } else if (defaultValueComponent instanceof HasEnabled hasEnabledComponent) {
            hasEnabledComponent.setEnabled(editPermitted);
        } else {
            defaultValueComponent.setVisible(editPermitted);
        }
    }

    protected void onDefaultValueComponentValueChanged(HasValue.ValueChangeEvent<?> event) {
        getEditedEntity().setDefaultValue(objectToStringConverter.convertToString(event.getValue()));
    }

    protected void initCurrentDateTimeField() {
        defaultDateIsCurrentField.setVisible(isParameterDateOrTime());
    }

    protected boolean isDefaultValueUnavailable(MessageTemplateParameter editedEntity) {
        if (isParameterDateOrTime() && Boolean.TRUE.equals(editedEntity.getDefaultDateIsCurrent())) {
            return true;
        }

        ParameterType type = editedEntity.getType();
        return type == null
                || type == ParameterType.ENTITY_LIST
                || (type == ParameterType.ENTITY && StringUtils.isBlank(editedEntity.getEntityMetaClass()))
                || (type == ParameterType.ENUMERATION && StringUtils.isBlank(editedEntity.getEnumerationClass()));
    }

    protected void updateLayoutByParameterType(@Nullable ParameterType type) {
        boolean isEntity = type == ParameterType.ENTITY || type == ParameterType.ENTITY_LIST;
        boolean isEnum = type == ParameterType.ENUMERATION;

        metaClassField.setVisible(isEntity);
        enumerationField.setVisible(isEnum);

        initDefaultValueField();
        initCurrentDateTimeField();
    }

    protected boolean isParameterDateOrTime() {
        return Optional.ofNullable(getEditedEntityOrNull())
                .map(MessageTemplateParameter::getType)
                .map(type ->
                        ParameterType.DATE.equals(type)
                                || ParameterType.DATETIME.equals(type)
                                || ParameterType.TIME.equals(type))
                .orElse(false);
    }

    protected void initLocalizationDataGrid() {
        Shortcuts.addShortcutListener(localizationDataGrid, this::handleGridEnterPress, Key.ENTER)
                .listenOn(localizationDataGrid)
                .allowBrowserDefault();
        Shortcuts.addShortcutListener(localizationDataGrid, this::handleGridEscapePress, Key.ESCAPE)
                .listenOn(localizationDataGrid)
                .allowBrowserDefault();

        localizationDataGrid.getActions().forEach(this::resetActionText);
        localizationDataGrid.getEditor().setColumnEditorComponent("locale", this::generateLocaleComponent);
    }

    protected void handleGridEnterPress() {
        if (localizationDataGrid.getEditor().isOpen()) {
            localizationDataGrid.getEditor().save();
        }
    }

    protected void handleGridEscapePress() {
        if (localizationDataGrid.getEditor().isOpen()) {
            localizationDataGrid.getEditor().cancel();
        }
    }

    protected void resetActionText(Action action) {
        action.setDescription(action.getText());
        action.setText(null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Component generateLocaleComponent(
            EditComponentGenerationContext<MessageTemplateParameterLocalization> context) {
        JmixComboBox localesComboBox = uiComponents.create(JmixComboBox.class);

        List<String> filteredLocales =
                messageParameterLocalizationSupport.getUnselectedLocales(parameterLocalizationDc.getItems());
        localesComboBox.setItems(filteredLocales);

        localesComboBox.setWidthFull();
        localesComboBox.setStatusChangeHandler(context.getStatusHandler());
        localesComboBox.setValueSource(context.getValueSourceProvider().getValueSource("locale"));

        return localesComboBox;
    }

    @Subscribe("localizationDataGrid.create")
    public void onLocalizationDataGridCreate(ActionPerformedEvent event) {
        MessageTemplateParameterLocalization entity = metadata.create(MessageTemplateParameterLocalization.class);
        parameterLocalizationDc.getMutableItems().add(entity);
        localizationDataGrid.select(entity);
        localizationDataGrid.getEditor().editItem(entity);
    }

    @Subscribe("localizationDataGrid.edit")
    protected void onLocalizationDataGridEdit(ActionPerformedEvent event) {
        MessageTemplateParameterLocalization entity = localizationDataGrid.getSingleSelectedItem();

        if (entity != null && !localizationDataGrid.getEditor().isOpen()) {
            localizationDataGrid.getEditor().editItem(entity);
        }
    }

    @Install(to = "localizationDataGrid.create", subject = "enabledRule")
    public boolean localizationDataGridCreateEnabledRule() {
        return parameterLocalizationDc.getItems().size()
                != messageParameterLocalizationSupport.getAvailableLocalesCount()
                && !localizationDataGrid.getEditor().isOpen();
    }

    @Install(to = "localizationDataGrid.edit", subject = "enabledRule")
    public boolean localizationDataGridEditEnabledRule() {
        return !localizationDataGrid.getEditor().isOpen();
    }

    @Install(to = "localizationDataGrid.remove", subject = "enabledRule")
    public boolean localizationDataGridRemoveEnabledRule() {
        return !localizationDataGrid.getEditor().isOpen();
    }

    @Install(to = "localizationDataGrid.@editor", subject = "openListener")
    public void localizationDataGridEditorOpenListener(
            EditorOpenEvent<MessageTemplateParameterLocalization> event) {
        localizationDataGrid.getActions().forEach(Action::refreshState);
    }

    @Install(to = "localizationDataGrid.@editor", subject = "closeListener")
    public void localizationDataGridEditorCloseListener(
            EditorCloseEvent<MessageTemplateParameterLocalization> event) {
        MessageTemplateParameterLocalization item = event.getItem();

        if (Strings.isNullOrEmpty(item.getLocale()) || Strings.isNullOrEmpty(item.getName())
                || isLocaleAlreadyDefined(item.getLocale())) {
            parameterLocalizationDc.getMutableItems().remove(item);
        }

        localizationDataGrid.getActions().forEach(Action::refreshState);
    }

    protected boolean isLocaleAlreadyDefined(String currentLocale) {
        return parameterLocalizationDc.getItems().stream()
                .filter(locale -> locale.getLocale().equals(currentLocale))
                .count() > 1;
    }

    @Install(to = "aliasField", subject = "validator")
    public void aliasFieldValidator(String alias) {
        if (Strings.isNullOrEmpty(alias)) {
            return;
        }

        List<MessageTemplateParameter> parentTemplateParameters = getParentTemplateParameters();
        if (!parentTemplateParameters.isEmpty()
                && parentTemplateParameters.stream()
                .anyMatch(parameter -> alias.equals(parameter.getAlias()))
        ) {
            throw new ValidationException(messageBundle.getMessage("uniqueAliasValidationMessage"));
        }
    }

    protected List<MessageTemplateParameter> getParentTemplateParameters() {
        return CollectionUtils.emptyIfNull(parentTemplateParameters).stream()
                .filter(parameter -> !getEditedEntity().equals(parameter))
                .toList();
    }

    public void setParentTemplateParameters(List<MessageTemplateParameter> parentTemplateParameters) {
        this.parentTemplateParameters = parentTemplateParameters;
    }

    @Subscribe
    public void onValidation(ValidationEvent event) {
        if (localizationDataGrid.getEditor().isOpen()) {
            localizationDataGrid.getEditor().cancel();
        }

        if (mainTabSheet.getSelectedIndex() == 0) {
            return;
        }

        mainTabSheet.setSelectedIndex(0);

        ValidationErrors validationErrors = viewValidation.validateUiComponents(getContent());
        if (!validationErrors.isEmpty()) {
            event.addErrors(validationErrors);
        }
    }

    @Subscribe
    public void onBeforeSave(BeforeSaveEvent event) {
        if (parameterLocalizationDc.getItems().isEmpty()) {
            getEditedEntity().setLocalization(null);
            return;
        }

        String localization = parameterLocalizationDc.getItems().stream()
                .map(messageParameterLocalizationSupport::convertLocalizationEntityToStringMapper)
                .collect(Collectors.joining("\n"));

        getEditedEntity().setLocalization(localization);
    }

    protected void showWarningNotification(String message) {
        notifications.create(message)
                .withType(Notifications.Type.WARNING)
                .withCloseable(false)
                .show();
    }
}

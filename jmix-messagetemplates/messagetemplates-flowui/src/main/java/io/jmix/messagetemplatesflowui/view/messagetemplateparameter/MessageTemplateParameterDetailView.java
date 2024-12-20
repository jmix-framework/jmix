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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.UiComponentsGenerator;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.InstanceContainer.ItemPropertyChangeEvent;
import io.jmix.flowui.view.*;
import io.jmix.messagetemplates.entity.MessageTemplateParameter;
import io.jmix.messagetemplates.entity.ParameterType;
import io.jmix.messagetemplatesflowui.MessageParameterResolver;
import io.jmix.messagetemplatesflowui.ObjectToStringConverter;
import io.jmix.messagetemplatesflowui.component.factory.MessageTemplateParameterGenerationContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

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
    protected MessageBundle messageBundle;

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
    protected ObjectToStringConverter objectToStringConverter;
    @Autowired
    protected MessageParameterResolver messageParameterResolver;

    @Subscribe
    public void onInit(InitEvent event) {
        initMetaClassField();
        initEnumerationField();
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        updateLayoutByParameterType(getEditedEntity().getType());
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
        // TODO: kd, do we need secure check to change default value?
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
}

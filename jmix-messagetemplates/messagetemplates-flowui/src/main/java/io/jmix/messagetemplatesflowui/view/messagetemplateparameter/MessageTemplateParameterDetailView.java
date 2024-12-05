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

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.InstanceContainer.ItemPropertyChangeEvent;
import io.jmix.flowui.view.*;
import io.jmix.messagetemplates.entity.MessageTemplateParameter;
import io.jmix.messagetemplates.entity.ParameterType;
import io.jmix.messagetemplatesflowui.ParameterClassResolver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

@ViewController("msgtmp_MessageTemplateParameter.detail")
@ViewDescriptor("message-template-parameter-detail-view.xml")
@EditedEntityContainer("messageTemplateParameterDc")
@DialogMode(width = "40em", resizable = true)
public class MessageTemplateParameterDetailView extends StandardDetailView<MessageTemplateParameter> {

    @ViewComponent
    protected JmixComboBox<String> metaClassField;
    @ViewComponent
    protected JmixComboBox<String> viewField;
    @ViewComponent
    protected JmixComboBox<String> enumerationField;
    @ViewComponent
    protected HorizontalLayout defaultValuePlaceholder;

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected ViewRegistry viewRegistry;
    @Autowired
    protected ParameterClassResolver parameterClassResolver;

    @Subscribe
    public void onInit(InitEvent event) {
        initMetaClassField();
        initEnumerationField();
    }

    @Override
    public void setEntityToEdit(MessageTemplateParameter entityToEdit) {
        super.setEntityToEdit(entityToEdit);

        if (getEditedEntity().getViewId() != null) {
            initViewField();
            viewField.setValue(getEditedEntity().getViewId());
        }
    }

    @Subscribe(id = "messageTemplateParameterDc", target = Target.DATA_CONTAINER)
    public void onParameterDcItemPropertyChange(ItemPropertyChangeEvent<MessageTemplateParameter> event) {
        String property = event.getProperty();

        boolean typeChanged = "type".equalsIgnoreCase(property);
        boolean classChanged = "entityMetaClass".equalsIgnoreCase(property)
                || "enumerationClass".equalsIgnoreCase(property);
        MessageTemplateParameter editedEntity = getEditedEntity();

        if (typeChanged) {
            editedEntity.setEntityMetaClass(null);
            editedEntity.setEnumerationClass(null);

            updateLayoutByParameterType(((ParameterType) event.getValue()));
        }

        if (typeChanged || classChanged) {
            editedEntity.setDefaultValue(null);
            editedEntity.setViewId(null);

            initViewField();
            initDefaultValueField();
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

    protected void initViewField() {
        MessageTemplateParameter editedEntity = getEditedEntity();

        if (editedEntity.getType() == ParameterType.ENTITY || editedEntity.getType() == ParameterType.ENTITY_LIST) {
            Class<?> parameterClass = parameterClassResolver.resolveClass(editedEntity);
            if (parameterClass != null) {
                // TODO: kd, always only one selection option???????
                String availableListViewId = viewRegistry.getAvailableListViewId(metadata.getClass(parameterClass));
                viewField.setItems(availableListViewId);
            }
        }
    }

    protected void initDefaultValueField() {
        defaultValuePlaceholder.removeAll();
        MessageTemplateParameter editedEntity = getEditedEntity();

        if (isDefaultValueAvailable(editedEntity)) {
            // TODO: kd, initDefaultValueComponent
        }
    }

    protected boolean isDefaultValueAvailable(MessageTemplateParameter editedEntity) {
        ParameterType type = editedEntity.getType();
        return type != null
                && type != ParameterType.ENTITY_LIST
                && (type != ParameterType.ENTITY || StringUtils.isNotBlank(editedEntity.getEntityMetaClass()))
                && (type != ParameterType.ENUMERATION || StringUtils.isNotBlank(editedEntity.getEnumerationClass()));
    }

    protected void updateLayoutByParameterType(@Nullable ParameterType type) {
        boolean isEntity = type == ParameterType.ENTITY || type == ParameterType.ENTITY_LIST;
        boolean isEnum = type == ParameterType.ENUMERATION;

        metaClassField.setVisible(isEntity);
        viewField.setVisible(isEntity);
        enumerationField.setVisible(isEnum);
    }
}

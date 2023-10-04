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

package io.jmix.flowui.app.filter.condition;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.jpqlfilter.JpqlFilterSupport;
import io.jmix.flowui.component.propertyfilter.SingleFilterSupport;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.entity.filter.FilterValueComponent;
import io.jmix.flowui.entity.filter.JpqlFilterCondition;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import jakarta.annotation.Nullable;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RolesAllowed("flowui-filter")
@ViewController("flowui_JpqlFilterCondition.detail")
@ViewDescriptor("jpql-filter-condition-detail-view.xml")
@EditedEntityContainer("filterConditionDc")
@DialogMode(width = "56em", resizable = true)
public class JpqlFilterConditionDetailView extends FilterConditionDetailView<JpqlFilterCondition> {

    @ViewComponent
    protected InstanceContainer<JpqlFilterCondition> filterConditionDc;

    @ViewComponent
    protected JmixTextArea joinField;
    @ViewComponent
    protected JmixTextArea whereField;
    @ViewComponent
    protected JmixSelect<Class<?>> parameterClassField;
    @ViewComponent
    protected JmixSelect<Class<?>> entityClassField;
    @ViewComponent
    protected JmixSelect<Class<?>> enumClassField;
    @ViewComponent
    protected TypedTextField<String> parameterNameField;
    @ViewComponent
    protected HorizontalLayout defaultValueBox;
    @ViewComponent
    protected JmixCheckbox hasInExpressionField;

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected JpqlFilterSupport jpqlFilterSupport;
    @Autowired
    protected ClassManager classManager;
    @Autowired
    protected SingleFilterSupport singleFilterSupport;
    @Autowired
    protected Notifications notifications;

    protected MetaClass filterMetaClass;

    @SuppressWarnings("rawtypes")
    protected HasValueAndElement defaultValueField;

    @Override
    public InstanceContainer<JpqlFilterCondition> getInstanceContainer() {
        return filterConditionDc;
    }

    @Override
    public void setCurrentConfiguration(Configuration currentConfiguration) {
        super.setCurrentConfiguration(currentConfiguration);
        filterMetaClass = currentConfiguration.getOwner().getDataLoader().getContainer().getEntityMetaClass();
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        initParameterClassFieldItems();
        initEntityClassFieldItems();
        initEnumClassFieldItems();
        initJpqlHelperButtons();
    }

    @Subscribe
    protected void onReady(ReadyEvent event) {
        configureParameterClassField();
        initDefaultValueField();
    }

    protected void initParameterClassFieldItems() {
        parameterClassField.setItems(String.class, LocalDate.class, Date.class, LocalTime.class, Time.class,
                LocalDateTime.class, Boolean.class, Double.class, BigDecimal.class, Integer.class, Long.class,
                UUID.class, Entity.class, Enum.class, Void.class);
    }

    protected void initEntityClassFieldItems() {
        List<Class<?>> entityClasses = new ArrayList<>();

        for (MetaClass metaClass : metadataTools.getAllJpaEntityMetaClasses()) {
            if (!metadataTools.isSystemLevel(metaClass)) {
                entityClasses.add(metaClass.getJavaClass());
            }
        }

        entityClassField.setItems(entityClasses);
    }

    protected void initEnumClassFieldItems() {
        enumClassField.setItems(metadataTools.getAllEnums());
    }

    protected void initJpqlHelperButtons() {
        createHelperButton(joinField);
        createHelperButton(whereField);
    }

    @SuppressWarnings({"unchecked"})
    protected void initDefaultValueField() {
        String entityParameterClass = getEditedEntity().getParameterClass();
        Boolean hasInExpression = getEditedEntity().getHasInExpression();

        if (filterMetaClass != null && entityParameterClass != null) {
            Class<?> parameterClass = classManager.loadClass(entityParameterClass);
            defaultValueField = singleFilterSupport.generateValueComponent(filterMetaClass,
                    hasInExpression, parameterClass);

            FilterValueComponent valueComponent = getEditedEntity().getValueComponent();
            if (valueComponent != null && valueComponent.getDefaultValue() != null) {
                String modelDefaultValue = valueComponent.getDefaultValue();
                Object defaultValue = jpqlFilterSupport.parseDefaultValue(parameterClass,
                        hasInExpression, modelDefaultValue);

                UiComponentUtils.setValue(defaultValueField, defaultValue);
            }
        } else {
            defaultValueField = uiComponents.create(TypedTextField.class);
            defaultValueField.setEnabled(false);
        }

        defaultValueBox.removeAll();
        defaultValueBox.add((Component) defaultValueField);

        if (defaultValueField instanceof HasSize) {
            ((HasSize) defaultValueField).setWidthFull();
        }

        if (defaultValueField instanceof HasLabel) {
            String label = messageBundle.getMessage("jpqlFilterConditionDetailView.defaultValue");
            ((HasLabel) defaultValueField).setLabel(label);
        }
    }

    protected void configureParameterClassField() {
        if (getEditedEntity().getParameterClass() != null) {
            Class<?> parameterClass = classManager.loadClass(getEditedEntity().getParameterClass());

            if (Entity.class.isAssignableFrom(parameterClass)) {
                parameterClassField.setValue(Entity.class);
                entityClassField.setValue(parameterClass);

            } else if (Enum.class.isAssignableFrom(parameterClass)) {
                parameterClassField.setValue(Enum.class);
                enumClassField.setValue(parameterClass);

            } else {
                parameterClassField.setValue(parameterClass);
            }
        }
    }

    @Subscribe("parameterClassField")
    protected void onParameterClassFieldComponentValueChange(
            ComponentValueChangeEvent<JmixSelect<Class<?>>, Class<?>> event) {
        Class<?> parameterClass = event.getValue();

        entityClassField.setVisible(parameterClass == Entity.class);
        if (parameterClass != Entity.class) {
            entityClassField.clear();
        }

        enumClassField.setVisible(parameterClass == Enum.class);
        if (parameterClass != Enum.class) {
            enumClassField.clear();
        }

        hasInExpressionField.setVisible(parameterClass != Boolean.class && parameterClass != Void.class);
        if (parameterClass == Boolean.class || parameterClass == Void.class) {
            hasInExpressionField.setValue(false);
        }

        parameterNameField.setVisible(parameterClass != Void.class);

        if (event.isFromClient()) {
            if (parameterClass != null
                    && parameterClass != Entity.class
                    && parameterClass != Enum.class) {
                getEditedEntity().setParameterClass(parameterClass.getName());
                updateParameterName(parameterClass);
            } else {
                getEditedEntity().setParameterClass(null);
            }

            resetDefaultValue();
            initDefaultValueField();
        }
    }

    @Subscribe("entityClassField")
    protected void onEntityClassFieldComponentValueChange(
            ComponentValueChangeEvent<JmixSelect<Class<?>>, Class<?>> event) {
        if (event.isFromClient()) {
            updateDefaultValueByClass(event.getValue());
        }
    }

    @Subscribe("enumClassField")
    protected void onEnumClassFieldComponentValueChange(
            ComponentValueChangeEvent<JmixSelect<Class<?>>, Class<?>> event) {
        if (event.isFromClient()) {
            updateDefaultValueByClass(event.getValue());
        }
    }

    @Subscribe("hasInExpressionField")
    protected void onHasInExpressionComponentValueChange(ComponentValueChangeEvent<JmixCheckbox, Boolean> event) {
        if (event.isFromClient()) {
            resetDefaultValue();
            initDefaultValueField();
        }
    }

    @Install(to = "parameterClassField", subject = "itemLabelGenerator")
    protected String parameterClassFieldItemLabelGenerator(Class<?> parameterClass) {
        return messageBundle.getMessage("jpqlFilterConditionDetailView.parameterClassField."
                + parameterClass.getSimpleName());
    }

    @Install(to = "entityClassField", subject = "itemLabelGenerator")
    protected String entityClassFieldItemLabelGenerator(Class<?> entityClass) {
        MetaClass metaClass = metadata.getClass(entityClass);
        return messageTools.getEntityCaption(metaClass) + " (" + metaClass.getName() + ")";
    }

    @Install(to = "enumClassField", subject = "itemLabelGenerator")
    protected String enumClassFieldItemLabelGenerator(Class<?> enumClass) {
        return enumClass.getSimpleName() + "(" + messages.getMessage(enumClass, enumClass.getSimpleName()) + ")";
    }

    protected void updateDefaultValueByClass(@Nullable Class<?> parameterClass) {
        if (parameterClass != null) {
            getEditedEntity().setParameterClass(parameterClass.getName());
            updateParameterName(parameterClass);
        } else {
            getEditedEntity().setParameterClass(null);
        }

        resetDefaultValue();
        initDefaultValueField();
    }

    protected void updateParameterName(Class<?> parameterClass) {
        String parameterName = jpqlFilterSupport.generateParameterName(getEditedEntity().getComponentId(),
                parameterClass.getSimpleName());
        getEditedEntity().setParameterName(parameterName);
    }

    protected void resetDefaultValue() {
        FilterValueComponent valueComponent = getEditedEntity().getValueComponent();
        if (valueComponent != null) {
            valueComponent.setDefaultValue(null);
        }
    }

    protected void createHelperButton(JmixTextArea textArea) {
        JmixButton helperButton = uiComponents.create(JmixButton.class);
        helperButton.setIcon(VaadinIcon.QUESTION_CIRCLE.create());
        helperButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);

        String message = messageBundle.getMessage(
                "jpqlFilterConditionDetailView." + textArea.getId().orElseThrow() + ".tooltipMessage");
        helperButton.addClickListener(event ->
                notifications.create(new Html(message))
                        .withCloseable(true)
                        .show()
        );

        textArea.setSuffixComponent(helperButton);
    }

    @Subscribe
    protected void onBeforeSave(BeforeSaveEvent event) {
        String entityParameterClass = getEditedEntity().getParameterClass();
        FilterValueComponent valueComponent = getEditedEntity().getValueComponent();

        if (defaultValueField != null && entityParameterClass != null && valueComponent != null) {
            Class<?> parameterClass = classManager.loadClass(entityParameterClass);
            Object value = defaultValueField instanceof SupportsTypedValue ?
                    ((SupportsTypedValue<?, ?, ?, ?>) defaultValueField).getTypedValue() :
                    defaultValueField.getValue();

            String modelDefaultValue = jpqlFilterSupport.formatDefaultValue(parameterClass,
                    getEditedEntity().getHasInExpression(), value);

            valueComponent.setDefaultValue(modelDefaultValue);
            valueComponent.setComponentName(singleFilterSupport.getValueComponentName(defaultValueField));
        }

        String label = getEditedEntity().getLabel();
        if (!Strings.isNullOrEmpty(label)) {
            getEditedEntity().setLocalizedLabel(label);
        }
    }
}

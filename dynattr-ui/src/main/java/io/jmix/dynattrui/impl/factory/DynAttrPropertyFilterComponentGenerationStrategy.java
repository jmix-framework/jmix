/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dynattrui.impl.factory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.Range;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.DynAttrUtils;
import io.jmix.dynattr.MsgBundleTools;
import io.jmix.dynattrui.impl.AttributeDependencies;
import io.jmix.dynattrui.impl.AttributeOptionsLoader;
import io.jmix.dynattrui.impl.AttributeValidators;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.action.entitypicker.EntityLookupAction;
import io.jmix.ui.action.propertyfilter.DateIntervalAction;
import io.jmix.ui.action.valuepicker.ValueClearAction;
import io.jmix.ui.app.propertyfilter.dateinterval.BaseDateInterval;
import io.jmix.ui.app.propertyfilter.dateinterval.DateIntervalUtils;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.DataAwareComponentsTools;
import io.jmix.ui.component.factory.PropertyFilterComponentGenerationContext;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.sys.ScreensHelper;
import org.springframework.context.ApplicationContext;

import static io.jmix.dynattr.AttributeType.ENTITY;
import static io.jmix.ui.component.factory.PropertyFilterComponentGenerationStrategy.UNARY_FIELD_STYLENAME;

@org.springframework.stereotype.Component("dynat_DynAttrPropertyFilterComponentGenerationStrategy")
public class DynAttrPropertyFilterComponentGenerationStrategy extends DynAttrComponentGenerationStrategy {

    protected DataAwareComponentsTools dataAwareComponentsTools;
    protected DateIntervalUtils dateIntervalUtils;

    public DynAttrPropertyFilterComponentGenerationStrategy(Messages messages,
                                                            UiComponents uiComponents,
                                                            DynAttrMetadata dynamicModelMetadata,
                                                            Metadata metadata,
                                                            MsgBundleTools msgBundleTools,
                                                            AttributeOptionsLoader optionsLoader,
                                                            AttributeValidators attributeValidators,
                                                            WindowConfig windowConfig,
                                                            ScreensHelper screensHelper,
                                                            Actions actions,
                                                            AttributeDependencies attributeDependencies,
                                                            FormatStringsRegistry formatStringsRegistry,
                                                            ApplicationContext applicationContext,
                                                            DataAwareComponentsTools dataAwareComponentsTools,
                                                            DateIntervalUtils dateIntervalUtils) {
        super(messages, uiComponents, dynamicModelMetadata, metadata, msgBundleTools, optionsLoader,
                attributeValidators, windowConfig, screensHelper, actions, attributeDependencies, formatStringsRegistry,
                applicationContext);

        this.dataAwareComponentsTools = dataAwareComponentsTools;
        this.dateIntervalUtils = dateIntervalUtils;
    }

    @Override
    public Component createComponent(ComponentGenerationContext context) {
        MetaClass metaClass = context.getMetaClass();
        String propertyName = context.getProperty();

        if (!DynAttrUtils.isDynamicAttributeProperty(propertyName)
                || context.getTargetClass() == null
                || !PropertyFilter.class.isAssignableFrom(context.getTargetClass())
                || !(context instanceof PropertyFilterComponentGenerationContext)) {
            return null;
        }

        return createComponentInternal(context, metaClass, propertyName);
    }

    @Override
    protected Component createComponentInternal(ComponentGenerationContext context, MetaClass metaClass, String propertyName) {
        AttributeDefinition attribute = dynamicModelMetadata.getAttributeByCode(metaClass,
                DynAttrUtils.getAttributeCodeFromProperty(propertyName)).orElse(null);

        if (attribute == null) {
            return null;
        }

        PropertyFilterComponentGenerationContext pfContext = (PropertyFilterComponentGenerationContext) context;
        PropertyFilter.Operation.Type type = pfContext.getOperation().getType();

        Component resultComponent;
        if (type == PropertyFilter.Operation.Type.UNARY) {
            resultComponent = createUnaryField(context);
        } else if (attribute.isCollection() || type == PropertyFilter.Operation.Type.LIST) {
            resultComponent = createCollectionField(context, attribute);
        } else if (attribute.getDataType() == ENTITY) {
            resultComponent = createClassField(context, attribute);
        } else if (type == PropertyFilter.Operation.Type.INTERVAL) {
            resultComponent = createIntervalField(context);
        } else {
            resultComponent = createDatatypeField(context, attribute);
        }

        return resultComponent;
    }

    @Override
    protected Field createBooleanField(ComponentGenerationContext context, AttributeDefinition attribute) {
        return createUnaryField(context);
    }

    protected Field createUnaryField(ComponentGenerationContext context) {
        ComboBox<Boolean> component = uiComponents.create(ComboBox.of(Boolean.class));
        component.setTextInputAllowed(false);
        component.addStyleName(UNARY_FIELD_STYLENAME);

        component.setOptionsMap(ImmutableMap.of(
                messages.getMessage("boolean.yes"), Boolean.TRUE,
                messages.getMessage("boolean.no"), Boolean.FALSE
        ));

        return component;
    }

    @Override
    protected EntityPicker createEntityField(ComponentGenerationContext context, AttributeDefinition attribute) {
        EntityPicker entityPicker = super.createEntityField(context, attribute);

        Class<?> javaType = attribute.getJavaType();
        MetaClass metaClass = metadata.getClass(javaType);

        entityPicker.setMetaClass(metaClass);

        return entityPicker;
    }

    protected Field createIntervalField(ComponentGenerationContext context) {
        ValuePicker<BaseDateInterval> valuePicker = uiComponents.create(ValuePicker.NAME);
        valuePicker.addAction(actions.create(DateIntervalAction.ID));
        valuePicker.addAction(actions.create(ValueClearAction.ID));
        valuePicker.setFormatter(interval -> dateIntervalUtils.formatDateIntervalToLocalizedValue(interval));
        return valuePicker;
    }

    @Override
    protected Component createDateField(ComponentGenerationContext context, AttributeDefinition attribute) {
        DateField dateField = (DateField) super.createDateField(context, attribute);
        dataAwareComponentsTools.setupDateFormat(dateField, attribute.getMetaProperty());
        return dateField;
    }

    @Override
    protected Component createDatatypeField(ComponentGenerationContext context, AttributeDefinition attribute) {
        Component field = super.createDatatypeField(context, attribute);

        Range range = attribute.getMetaProperty().getRange();
        if (field instanceof HasDatatype && range.isDatatype()) {
            ((HasDatatype<?>) field).setDatatype(range.asDatatype());
        }

        return field;
    }

    @Override
    protected void setLookupActionScreen(EntityLookupAction lookupAction, AttributeDefinition attribute) {
        String screen = attribute.getConfiguration().getLookupScreen();
        if (!Strings.isNullOrEmpty(screen)) {
            lookupAction.setScreenId(screen);
        }

        lookupAction.setOpenMode(OpenMode.DIALOG);
    }

    @Override
    protected void setValidators(Field field, AttributeDefinition attribute) {
        // do nothing
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 10;
    }
}

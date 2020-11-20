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

package io.jmix.ui.component.factory;

import com.google.common.collect.ImmutableMap;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.Enumeration;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.entitypicker.EntityClearAction;
import io.jmix.ui.action.entitypicker.LookupAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.DataAwareComponentsTools;
import io.jmix.ui.component.impl.EntityFieldCreationSupport;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.screen.OpenMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import javax.annotation.Nullable;

/**
 * A {@link ComponentGenerationStrategy} used by {@link PropertyFilter} UI component
 */
@org.springframework.stereotype.Component
public class PropertyFilterComponentGenerationStrategy extends AbstractComponentGenerationStrategy implements Ordered {

    protected static final String UNARY_FIELD_STYLENAME = "unary-field";

    protected DataAwareComponentsTools dataAwareComponentsTools;

    @Autowired
    public PropertyFilterComponentGenerationStrategy(Messages messages,
                                                     UiComponents uiComponents,
                                                     EntityFieldCreationSupport entityFieldCreationSupport,
                                                     Metadata metadata,
                                                     MetadataTools metadataTools,
                                                     Icons icons,
                                                     Actions actions,
                                                     DataAwareComponentsTools dataAwareComponentsTools) {
        super(messages, uiComponents, entityFieldCreationSupport, metadata, metadataTools, icons, actions);
        this.dataAwareComponentsTools = dataAwareComponentsTools;
    }

    @Override
    public Component createComponent(ComponentGenerationContext context) {
        if (context.getTargetClass() == null
                || !PropertyFilter.class.isAssignableFrom(context.getTargetClass())
                || !(context instanceof PropertyFilterComponentGenerationContext)) {
            return null;
        }

        return createComponentInternal(context);
    }

    @Nullable
    @Override
    protected Component createComponentInternal(ComponentGenerationContext context) {
        PropertyFilterComponentGenerationContext pfContext = (PropertyFilterComponentGenerationContext) context;
        if (pfContext.getOperation().getType() == PropertyFilter.Operation.Type.UNARY) {
            return createUnaryField(context);
        }

        return super.createComponentInternal(context);
    }

    @Nullable
    @Override
    protected Component createDatatypeField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        Component field = super.createDatatypeField(context, mpp);
        Datatype datatype = mpp.getRange().asDatatype();

        if (field instanceof HasDatatype) {
            ((HasDatatype<?>) field).setDatatype(datatype);
        }

        return field;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Component createDateField(ComponentGenerationContext context) {
        DateField dateField = (DateField) super.createDateField(context);

        MetaClass metaClass = context.getMetaClass();
        MetaPropertyPath mpp = resolveMetaPropertyPath(metaClass, context.getProperty());
        if (mpp != null) {
            dataAwareComponentsTools.setupDateFormat(dateField, mpp.getMetaProperty());
        }

        return dateField;
    }

    @Override
    protected Component createEntityField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        EntityPicker<?> field = uiComponents.create(EntityPicker.class);

        MetaClass metaClass = mpp.getMetaProperty().getRange().asClass();
        field.setMetaClass(metaClass);

        LookupAction<?> lookupAction = (LookupAction<?>) actions.create(LookupAction.ID);
        lookupAction.setOpenMode(OpenMode.DIALOG);
        field.addAction(lookupAction);
        field.addAction(actions.create(EntityClearAction.ID));

        return field;
    }

    @Override
    protected Field createBooleanField(ComponentGenerationContext context) {
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
    protected Field createEnumField(ComponentGenerationContext context) {
        MetaClass metaClass = context.getMetaClass();
        MetaPropertyPath mpp = resolveMetaPropertyPath(metaClass, context.getProperty());
        if (mpp == null) {
            throw new RuntimeException(String.format("Meta properties path not found: %s.%s",
                    metaClass.getName(), context.getProperty()));
        }

        Enumeration<?> enumeration = mpp.getRange().asEnumeration();
        ComboBox<?> component = uiComponents.create(ComboBox.class);
        component.setOptionsEnum(enumeration.getJavaClass());

        return component;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 100;
    }

}

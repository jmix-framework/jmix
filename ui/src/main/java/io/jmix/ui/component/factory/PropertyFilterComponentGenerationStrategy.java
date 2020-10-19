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

import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.Enumeration;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.component.impl.EntityFieldCreationSupport;
import io.jmix.ui.icon.Icons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@link ComponentGenerationStrategy} used by {@link PropertyFilter} UI component
 */
@org.springframework.stereotype.Component
public class PropertyFilterComponentGenerationStrategy extends AbstractComponentGenerationStrategy implements Ordered {

    @Autowired
    public PropertyFilterComponentGenerationStrategy(Messages messages,
                                                     UiComponents uiComponents,
                                                     EntityFieldCreationSupport entityFieldCreationSupport,
                                                     Metadata metadata,
                                                     MetadataTools metadataTools,
                                                     Icons icons) {
        super(messages, uiComponents, entityFieldCreationSupport, metadata, metadataTools, icons);
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Override
    public Component createComponent(ComponentGenerationContext context) {
        if (!PropertyFilter.class.equals(context.getTargetClass())) return null;
        return createComponentInternal(context);
    }

    @Override
    protected Field createBooleanField(ComponentGenerationContext context) {
        ComboBox<Boolean> component = uiComponents.create(ComboBox.class);
        setValueSource(component, context);
        Map<String, Boolean> optionsMap = new LinkedHashMap<>();
        optionsMap.put(messages.getMessage("io.jmix.ui.component/propertyfilter.boolean.true"), Boolean.TRUE);
        optionsMap.put(messages.getMessage("io.jmix.ui.component/propertyfilter.boolean.false"), Boolean.FALSE);
        component.setOptionsMap(optionsMap);
        return component;
    }

    @Override
    protected Component createEntityField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        if (context instanceof PropertyFilterComponentGenerationContext) {
            PropertyCondition propertyCondition = ((PropertyFilterComponentGenerationContext) context).getPropertyCondition();
            if (PropertyCondition.Operation.IS_NULL.equals(propertyCondition.getOperation()) ||
                    PropertyCondition.Operation.IS_NOT_NULL.equals(propertyCondition.getOperation())) {
                ComboBox<Boolean> component = uiComponents.create(ComboBox.class);
                setValueSource(component, context);
                Map<String, Boolean> optionsMap = new LinkedHashMap<>();
                optionsMap.put(messages.getMessage("io.jmix.ui.component/propertyfilter.boolean.true"), Boolean.TRUE);
                component.setOptionsMap(optionsMap);
                return component;
            }
        }
        return super.createEntityField(context, mpp);
    }

    @Override
    protected Field createEnumField(ComponentGenerationContext context) {
        MetaClass metaClass = context.getMetaClass();
        MetaPropertyPath mpp = metaClass.getPropertyPath(context.getProperty());
        if (mpp == null) {
            throw new RuntimeException(String.format("Meta properties path not found: %s.%s", metaClass.getName(), context.getProperty()));
        }
        Enumeration enumeration = mpp.getRange().asEnumeration();
        ComboBox component = uiComponents.create(ComboBox.class);
        component.setOptionsEnum(enumeration.getJavaClass());
        return component;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 100;
    }

}

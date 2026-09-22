/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.component.factory;

import com.vaadin.flow.component.Component;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.impl.LocalizedStringDatatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.valuepicker.LocalizedStringEditAction;
import io.jmix.flowui.action.valuepicker.ValueClearAction;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.valuepicker.JmixValuePicker;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;

/**
 * Generates a {@link JmixValuePicker} with the {@code value_localizedStringEdit} action for localized string
 * properties, so that generated forms do not edit the stored string through a plain text field.
 */
@Internal
@org.springframework.stereotype.Component("flowui_LocalizedStringComponentGenerationStrategy")
public class LocalizedStringComponentGenerationStrategy extends AbstractComponentGenerationStrategy
        implements Ordered {

    public LocalizedStringComponentGenerationStrategy(UiComponents uiComponents,
                                                      Metadata metadata,
                                                      MetadataTools metadataTools,
                                                      Actions actions,
                                                      DatatypeRegistry datatypeRegistry,
                                                      Messages messages,
                                                      EntityFieldCreationSupport entityFieldCreationSupport) {
        super(uiComponents, metadata, metadataTools, actions, datatypeRegistry, messages, entityFieldCreationSupport);
    }

    @Nullable
    @Override
    public Component createComponent(ComponentGenerationContext context) {
        MetaClass metaClass = context.getMetaClass();
        String property = context.getProperty();
        if (metaClass == null || property == null) {
            return null;
        }

        MetaPropertyPath mpp = resolveMetaPropertyPath(metaClass, property);
        if (mpp == null) {
            return null;
        }

        Range range = mpp.getRange();
        if (!range.isDatatype() || range.getCardinality().isMany()) {
            return null;
        }

        Datatype<?> datatype = range.asDatatype();
        if (!(datatype instanceof LocalizedStringDatatype)) {
            return null;
        }

        return createLocalizedStringField(context);
    }

    protected Component createLocalizedStringField(ComponentGenerationContext context) {
        JmixValuePicker<?> picker = uiComponents.create(JmixValuePicker.class);
        setValueSource(picker, context);

        picker.addAction(actions.create(LocalizedStringEditAction.ID));
        picker.addAction(actions.create(ValueClearAction.ID));
        picker.setWidthFull();

        return picker;
    }

    @Override
    public int getOrder() {
        return JmixOrder.LOWEST_PRECEDENCE - 10;
    }
}

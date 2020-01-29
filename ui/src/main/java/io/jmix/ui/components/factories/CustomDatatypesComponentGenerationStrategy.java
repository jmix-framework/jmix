/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.components.factories;

import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.UiComponents;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.ComponentGenerationContext;
import io.jmix.ui.components.actions.GuiActionSupport;
import io.jmix.ui.dynamicattributes.DynamicAttributesTools;
import org.springframework.core.Ordered;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * This generation strategy is intended for generating a default field corresponding to a datatype for which other
 * generation strategies didn't create a field. For instance, when a datatype is custom and doesn't match any type
 * for which {@link DefaultComponentGenerationStrategy} creates fields.
 */
@org.springframework.stereotype.Component(CustomDatatypesComponentGenerationStrategy.NAME)
public class CustomDatatypesComponentGenerationStrategy extends AbstractComponentGenerationStrategy implements Ordered {

    public static final String NAME = "cuba_CustomDatatypesComponentGenerationStrategy";

    @Inject
    public CustomDatatypesComponentGenerationStrategy(Messages messages, DynamicAttributesTools dynamicAttributesTools,
                                                      GuiActionSupport guiActionSupport) {
        super(messages, dynamicAttributesTools, guiActionSupport);
    }

    @Inject
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Nullable
    @Override
    public Component createComponent(ComponentGenerationContext context) {
        MetaClass metaClass = context.getMetaClass();
        MetaPropertyPath mpp = resolveMetaPropertyPath(metaClass, context.getProperty());

        if (mpp != null
                && mpp.getRange().isDatatype()) {
            return createCustomDatatypeField(context, mpp);
        }

        return null;
    }

    protected Component createCustomDatatypeField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        return createStringField(context, mpp);
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}

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
import io.jmix.ui.UiComponents;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.ComponentGenerationContext;
import io.jmix.ui.components.actions.GuiActionSupport;
import io.jmix.ui.dynamicattributes.DynamicAttributesTools;
import org.springframework.core.Ordered;

import javax.annotation.Nullable;
import javax.inject.Inject;

@org.springframework.stereotype.Component(DefaultComponentGenerationStrategy.NAME)
public class DefaultComponentGenerationStrategy extends AbstractComponentGenerationStrategy implements Ordered {
    public static final String NAME = "cuba_DefaultMetaComponentStrategy";

    @Inject
    public DefaultComponentGenerationStrategy(Messages messages, DynamicAttributesTools dynamicAttributesTools,
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
        return createComponentInternal(context);
    }

    @Override
    public int getOrder() {
        return LOWEST_PLATFORM_PRECEDENCE;
    }
}

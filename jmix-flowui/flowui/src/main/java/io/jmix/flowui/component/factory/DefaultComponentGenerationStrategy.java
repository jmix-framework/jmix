/*
 * Copyright 2022 Haulmont.
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
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.ComponentGenerationContext;
import org.springframework.core.Ordered;

import javax.annotation.Nullable;

@org.springframework.stereotype.Component("flowui_DefaultComponentGenerationStrategy")
public class DefaultComponentGenerationStrategy extends AbstractComponentGenerationStrategy implements Ordered {


    public DefaultComponentGenerationStrategy(UiComponents uiComponents,
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
        return createComponentInternal(context);
    }

    @Override
    public int getOrder() {
        return JmixOrder.LOWEST_PRECEDENCE;
    }
}

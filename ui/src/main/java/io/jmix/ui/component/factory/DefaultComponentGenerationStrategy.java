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

package io.jmix.ui.component.factory;

import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentGenerationContext;
import io.jmix.ui.component.impl.EntityFieldCreationSupport;
import io.jmix.ui.icon.Icons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import javax.annotation.Nullable;

@org.springframework.stereotype.Component("ui_DefaultComponentGenerationStrategy")
public class DefaultComponentGenerationStrategy extends AbstractComponentGenerationStrategy implements Ordered {

    @Autowired
    public DefaultComponentGenerationStrategy(Messages messages,
                                              UiComponents uiComponents,
                                              EntityFieldCreationSupport entityFieldCreationSupport,
                                              Metadata metadata,
                                              MetadataTools metadataTools,
                                              Icons icons,
                                              Actions actions) {
        super(messages, uiComponents, entityFieldCreationSupport, metadata, metadataTools, icons, actions);
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

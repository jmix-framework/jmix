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

package io.jmix.flowui.xml.layout.loader.impl;

import io.jmix.core.JmixOrder;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.ComponentPropertyParser;
import io.jmix.flowui.xml.layout.loader.ComponentPropertyParsingContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(JmixOrder.LOWEST_PRECEDENCE - 10)
@Component("flowui_DataContainerComponentPropertyParser")
public class DataContainerComponentPropertyParser implements ComponentPropertyParser {

    public static final String TYPE = "CONTAINER_REF";

    @Override
    public boolean supports(ComponentPropertyParsingContext context) {
        return TYPE.equals(context.type())
                && context.context() instanceof ComponentLoader.ComponentContext;
    }

    @Override
    public Object parse(ComponentPropertyParsingContext context) {
        if (context.context() instanceof ComponentLoader.ComponentContext componentContext) {
            return componentContext.getViewData().getContainer(context.value());
        }

        throw new IllegalArgumentException("Cannot find data container, component loader 'context' must implement " +
                ComponentLoader.ComponentContext.class.getName());
    }
}

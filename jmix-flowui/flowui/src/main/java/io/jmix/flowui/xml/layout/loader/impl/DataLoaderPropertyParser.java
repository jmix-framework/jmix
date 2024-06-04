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
import io.jmix.flowui.xml.layout.loader.PropertyParser;
import io.jmix.flowui.xml.layout.loader.PropertyParsingContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(JmixOrder.LOWEST_PRECEDENCE - 10)
@Component("flowui_DataLoaderPropertyParser")
public class DataLoaderPropertyParser implements PropertyParser {

    public static final String TYPE = "LOADER_REF";

    @Override
    public boolean supports(PropertyParsingContext context) {
        return TYPE.equals(context.type())
                && context.context() instanceof ComponentLoader.ComponentContext;
    }

    @Override
    public Object parse(PropertyParsingContext context) {
        if (context.context() instanceof ComponentLoader.ComponentContext componentContext) {
            return componentContext.getDataHolder().getLoader(context.value());
        }

        throw new IllegalArgumentException("Cannot find data container, component loader 'context' must implement " +
                ComponentLoader.ComponentContext.class.getName());
    }
}

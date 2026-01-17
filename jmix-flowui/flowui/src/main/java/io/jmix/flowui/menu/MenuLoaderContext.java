/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.menu;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.component.HasDataComponents;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.AbstractLoaderContext;

/**
 * Semi-dummy {@link ComponentLoader.Context} for {@link MenuConfig} to support
 * custom icons for menu items.
 */
public class MenuLoaderContext extends AbstractLoaderContext {

    protected final Component origin;

    public MenuLoaderContext(Component origin) {
        this.origin = origin;
    }

    @Override
    public Component getOrigin() {
        return origin;
    }

    @Override
    public HasActions getActionsHolder() {
        throw new UnsupportedOperationException("%s does not support actions"
                .formatted(getClass().getSimpleName()));
    }

    @Override
    public HasDataComponents getDataHolder() {
        throw new UnsupportedOperationException("%s does not support data components"
                .formatted(getClass().getSimpleName()));
    }
}

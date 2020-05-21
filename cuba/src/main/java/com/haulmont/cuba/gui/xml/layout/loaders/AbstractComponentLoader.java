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

package com.haulmont.cuba.gui.xml.layout.loaders;

import io.jmix.ui.component.Component;

import static com.google.common.base.Preconditions.checkState;

public abstract class AbstractComponentLoader<T extends Component>
        extends io.jmix.ui.xml.layout.loader.AbstractComponentLoader<T> {

    @Override
    protected ComponentLoaderContext getComponentContext() {
        checkState(context instanceof ComponentLoaderContext,
            "'context' must implement io.jmix.ui.xml.layout.ComponentLoader.ComponentContext");

        return (ComponentLoaderContext) context;
    }
}

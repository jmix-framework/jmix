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

package io.jmix.flowui.xml.layout.loader;

import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.xml.layout.ComponentRootLoader;
import io.jmix.flowui.xml.layout.loader.container.AbstractContainerLoader;
import io.jmix.flowui.xml.layout.support.ScreenLoaderSupport;

public abstract class AbstractScreenLoader<T extends Screen<?>> extends AbstractContainerLoader<T>
        implements ComponentRootLoader<T> {

    protected ScreenLoaderSupport screenLoader;

    public void setResultComponent(T screen) {
        this.resultComponent = screen;
    }

    public ScreenLoaderSupport getScreenLoader() {
        if (screenLoader == null) {
            screenLoader = applicationContext.getBean(ScreenLoaderSupport.class, resultComponent, context);
        }
        return screenLoader;
    }

    @Override
    protected T createComponent() {
        throw new UnsupportedOperationException("Screen cannot be created from XML element");
    }

    @Override
    public void initComponent() {
        throw new UnsupportedOperationException("Screen cannot be initialized from XML element");
    }
}

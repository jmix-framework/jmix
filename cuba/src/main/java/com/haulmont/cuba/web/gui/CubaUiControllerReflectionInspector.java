/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.web.gui;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.global.Configuration;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.sys.UiControllerDependencyInjector;

import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;

public class CubaUiControllerReflectionInspector extends UiControllerDependencyInjector {

    public CubaUiControllerReflectionInspector(FrameOwner frameOwner, ScreenOptions options) {
        super(frameOwner, options);
    }

    @Nullable
    @Override
    protected Object getInjectedInstance(Class<?> type, String name, Class annotationClass, AnnotatedElement element) {
        if (Config.class.isAssignableFrom(type)) {
            Configuration configuration = beanLocator.get(Configuration.NAME);
            //noinspection unchecked
            return configuration.getConfig((Class<? extends Config>) type);
        }
        return super.getInjectedInstance(type, name, annotationClass, element);
    }
}

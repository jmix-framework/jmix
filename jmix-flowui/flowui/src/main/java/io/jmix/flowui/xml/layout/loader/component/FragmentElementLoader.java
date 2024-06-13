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

package io.jmix.flowui.xml.layout.loader.component;

import io.jmix.core.ClassManager;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.xml.layout.inittask.AbstractInitTask;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.loader.PropertiesLoaderSupport;

public class FragmentElementLoader extends AbstractComponentLoader<Fragment<?>> {

    @Override
    protected Fragment<?> createComponent() {
        String fragmentClass = loadString(element, "class")
                .orElseThrow(() ->
                        new GuiDevelopmentException("Missing required 'fragmentClass' attribute", context));

        Class<?> aClass = applicationContext.getBean(ClassManager.class).loadClass(fragmentClass);
        if (!Fragment.class.isAssignableFrom(aClass)) {
            throw new GuiDevelopmentException("Class '" + fragmentClass + "' is not a fragment", context);
        }

        return factory.create(aClass.asSubclass(Fragment.class));
    }

    @Override
    public void loadComponent() {
        applicationContext.getBean(Fragments.class).init(context, resultComponent);

        if (element.element("properties") != null) {
            // setting properties from XML should be processed after UI components
            // are injected which so properties are handled the same for declarative
            // creation of fragment and programmatic
            getContext().addInitTask(new AbstractInitTask() {
                @Override
                public void execute(Context context) {
                    PropertiesLoaderSupport propertiesLoader =
                            applicationContext.getBean(PropertiesLoaderSupport.class, context);
                    propertiesLoader.loadProperties(resultComponent, element);
                }
            });
        }
    }
}

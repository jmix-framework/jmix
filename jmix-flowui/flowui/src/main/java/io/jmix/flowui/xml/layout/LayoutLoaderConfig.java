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
package io.jmix.flowui.xml.layout;

import io.jmix.core.JmixOrder;
import io.jmix.flowui.xml.layout.loader.ScreenLoader;
import org.dom4j.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
@Order(JmixOrder.LOWEST_PRECEDENCE - 10)
@Component("flowui_LayoutLoaderConfig")
public class LayoutLoaderConfig extends BaseLoaderConfig implements LoaderConfig {

    protected Class<? extends ScreenLoader> screenLoader = ScreenLoader.class;
//    protected Class<? extends FragmentLoader> fragmentLoader = FragmentLoader.class;

    @Override
    public boolean supports(Element element) {
        return loaders.containsKey(element.getName());
    }

    @Override
    public Class<? extends ComponentLoader> getLoader(Element element) {
        return getLoader(element.getName());
    }

    public Class<? extends ComponentLoader> getScreenLoader() {
        return screenLoader;
    }

    @Nullable
    @Override
    public Class<? extends ComponentLoader> getScreenLoader(Element root) {
        return screenLoader;
    }

//    @Override
//    public Class<? extends ComponentLoader> getFragmentLoader(Element root) {
//        return fragmentLoader;
//    }

    @Nullable
    public Class<? extends ComponentLoader> getLoader(String name) {
        return loaders.get(name);
    }

    @Nullable
    protected Element getRootElement(String rootName, Element child) {
        Element parent = child.getParent();
        if (parent == null) {
            // element is root
            if (child.getName().equals(rootName)) {
                return child;
            }
        } else {
            while (parent != null
                    && !rootName.equals(parent.getName())) {
                parent = parent.getParent();
            }
        }

        return parent;
    }
}

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
package io.jmix.flowui.xml.layout;

import io.jmix.core.JmixOrder;
import io.jmix.flowui.xml.layout.loader.MainViewLoader;
import io.jmix.flowui.xml.layout.loader.ViewLoader;
import org.dom4j.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

import static io.jmix.flowui.xml.layout.loader.MainViewLoader.MAIN_VIEW_ROOT;
import static io.jmix.flowui.xml.layout.loader.ViewLoader.VIEW_ROOT;

@Order(JmixOrder.LOWEST_PRECEDENCE - 10)
@Component("flowui_LayoutLoaderConfig")
public class LayoutLoaderConfig extends BaseLoaderConfig implements LoaderConfig {

    protected Class<? extends ViewLoader> viewLoader = ViewLoader.class;
    protected Class<? extends MainViewLoader> mainViewLoader = MainViewLoader.class;

    @Override
    public boolean supports(Element element) {
        return loaders.containsKey(element.getName());
    }

    @Override
    public Class<? extends ComponentLoader> getLoader(Element element) {
        return getLoader(element.getName());
    }

    @Nullable
    @Override
    public Class<? extends ComponentLoader> getViewLoader(Element root) {
        String name = root.getName();
        switch (name) {
            case VIEW_ROOT:
                return viewLoader;
            case MAIN_VIEW_ROOT:
                return mainViewLoader;
            default:
                return null;
        }
    }

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

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

package io.jmix.ui.sys.registration;

import io.jmix.core.JmixOrder;
import io.jmix.ui.xml.layout.ComponentLoader;
import io.jmix.ui.xml.layout.LoaderConfig;
import org.dom4j.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains UI component loaders from add-ons and project configurations.
 *
 * @see CustomComponentsRegistry
 */
@Order(JmixOrder.HIGHEST_PRECEDENCE + 10)
@Component("ui_CustomComponentsLoaderConfig")
public class CustomComponentsLoaderConfig implements LoaderConfig {

    protected Map<String, Class<? extends ComponentLoader>> loaders = new ConcurrentHashMap<>();

    @Override
    public boolean supports(Element element) {
        return loaders.containsKey(element.getName());
    }

    @Override
    public Class<? extends ComponentLoader> getLoader(Element element) {
        return loaders.get(element.getName());
    }

    @Nullable
    @Override
    public Class<? extends ComponentLoader> getFragmentLoader(Element root) {
        // return null as we do not override fragment loader
        return null;
    }

    @Nullable
    @Override
    public Class<? extends ComponentLoader> getWindowLoader(Element root) {
        // return null as we do not override window loader
        return null;
    }

    protected void registerLoader(String tagName, Class<? extends ComponentLoader> loaderClass) {
        loaders.put(tagName, loaderClass);
    }
}

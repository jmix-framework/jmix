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
package io.jmix.ui.xml.layout;

import io.jmix.ui.xml.layout.loaders.FragmentLoader;
import io.jmix.ui.xml.layout.loaders.WindowLoader;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("rawtypes")
@Component(LayoutLoaderConfig.NAME)
public class LayoutLoaderConfig extends BaseLoaderConfig implements LoaderConfig {

    public static final String NAME = "jmix_LayoutLoaderConfig";

    private static final Logger log = LoggerFactory.getLogger(LayoutLoaderConfig.class);

    protected Class<? extends WindowLoader> windowLoader = WindowLoader.class;
    protected Class<? extends FragmentLoader> fragmentLoader = FragmentLoader.class;

    @Override
    public boolean supports(Element element) {
        return isNotLegacyScreen(element)
                && loaders.containsKey(element.getName());
    }

    @Override
    public Class<? extends ComponentLoader> getLoader(Element element) {
        return getLoader(element.getName());
    }

    protected boolean isNotLegacyScreen(Element element) {
        Element parent = element.getParent();

        while (parent != null
                && !"window".equals(parent.getName())) {
            parent = parent.getParent();
        }

        return parent != null
                && parent.attribute("class") == null;
    }

    /**
     * @deprecated use custom implementation of {@link LoaderConfig} that will be resolved by {@link LoaderResolver}.
     */
    @Deprecated
    public void registerLoader(String tagName, Class<? extends ComponentLoader> aClass) {
        log.debug("LayoutLoaderConfig#registerLoader is deprecated. Use your own implementation of LoaderConfig");

        loaders.put(tagName, aClass);
    }

    public Class<? extends ComponentLoader> getWindowLoader() {
        return windowLoader;
    }

    public Class<? extends ComponentLoader> getFragmentLoader() {
        // return fragmentLoader;
        throw new UnsupportedOperationException(); // todo fragments
    }

    public Class<? extends ComponentLoader> getLoader(String name) {
        return loaders.get(name);
    }

    public void registerWindowLoader(Class<? extends WindowLoader> loader) {
        windowLoader = loader;
    }

    public void registerFragmentLoader(Class<? extends FragmentLoader> loader) {
        fragmentLoader = loader;
    }

    protected void register(String tagName, Class<? extends ComponentLoader> loaderClass) {
        loaders.put(tagName, loaderClass);
    }
}

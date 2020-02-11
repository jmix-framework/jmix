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

package com.haulmont.cuba.gui.xml.layout;

import com.haulmont.cuba.gui.components.BulkEditor;
import com.haulmont.cuba.gui.components.FieldGroup;
import com.haulmont.cuba.gui.components.Filter;
import com.haulmont.cuba.gui.xml.layout.loaders.BulkEditorLoader;
import com.haulmont.cuba.gui.xml.layout.loaders.FieldGroupLoader;
import com.haulmont.cuba.gui.xml.layout.loaders.CubaTextFieldLoader;
import com.haulmont.cuba.gui.xml.layout.loaders.FilterLoader;
import io.jmix.ui.components.TextField;
import io.jmix.ui.xml.layout.BaseLoaderConfig;
import io.jmix.ui.xml.layout.ComponentLoader;
import io.jmix.ui.xml.layout.LoaderConfig;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("rawtypes")
@Component(CubaLoaderConfig.NAME)
public class CubaLoaderConfig extends BaseLoaderConfig implements LoaderConfig {

    public static final String NAME = "cuba_LegacyLoaderConfig";

    @Override
    public boolean supports(Element element) {
        return isLegacyScreen(element)
                && loaders.containsKey(element.getName());
    }

    @Override
    public Class<? extends ComponentLoader> getLoader(Element element) {
        return loaders.get(element.getName());
    }

    @Override
    protected void initStandardLoaders() {
        super.initStandardLoaders();

        loaders.put(TextField.NAME, CubaTextFieldLoader.class);
        loaders.put(FieldGroup.NAME, FieldGroupLoader.class);
        loaders.put(BulkEditor.NAME, BulkEditorLoader.class);
        loaders.put(Filter.NAME, FilterLoader.class);
    }

    protected boolean isLegacyScreen(Element element) {
        Element parent = element.getParent();

        while (parent != null
                && !"window".equals(parent.getName())) {
            parent = parent.getParent();
        }

        return parent != null
                && parent.attribute("class") != null;
    }
}

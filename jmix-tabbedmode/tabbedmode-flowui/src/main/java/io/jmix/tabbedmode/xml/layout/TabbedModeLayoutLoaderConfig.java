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

package io.jmix.tabbedmode.xml.layout;

import io.jmix.core.JmixOrder;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.LoaderConfig;
import io.jmix.tabbedmode.xml.layout.loader.TabbedModeMainViewLoader;
import org.dom4j.Element;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import static io.jmix.tabbedmode.xml.layout.loader.TabbedModeMainViewLoader.MAIN_VIEW_ROOT;

@Order(JmixOrder.LOWEST_PRECEDENCE - 100)
@Component("tabmod_TabbedModeLayoutLoaderConfig")
public class TabbedModeLayoutLoaderConfig implements LoaderConfig {

    @Override
    public boolean supports(Element element) {
        return false;
    }

    @Override
    public Class<? extends ComponentLoader> getLoader(Element element) {
        throw new UnsupportedOperationException("%s does not support loading of %s"
                .formatted(getClass().getSimpleName(), element));
    }

    @Nullable
    @Override
    public Class<? extends ComponentLoader> getViewLoader(Element root) {
        return MAIN_VIEW_ROOT.equals(root.getName())
                ? TabbedModeMainViewLoader.class
                : null;
    }
}

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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.components.HasSettings;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.HasTablePresentations;
import io.jmix.ui.component.Window;
import com.haulmont.cuba.settings.CubaLegacySettings;
import com.haulmont.cuba.settings.Settings;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.UUID;

import static io.jmix.ui.component.ComponentsHelper.walkComponents;

@Deprecated
public class WebTabSheet extends io.jmix.ui.component.impl.WebTabSheet {

    @Override
    protected LazyTabChangeListener createLazyTabChangeListener(ComponentContainer tabContent, Element descriptor, ComponentLoader loader) {
        return new CubaLazyTabChangeListener(tabContent, descriptor, loader);
    }

    protected class CubaLazyTabChangeListener extends LazyTabChangeListener {

        public CubaLazyTabChangeListener(ComponentContainer tabContent, Element descriptor, ComponentLoader loader) {
            super(tabContent, descriptor, loader);
        }

        @Override
        protected void applySettings(Window window) {
            if (window == null) {
                return;
            }

            if (window.getFrameOwner() instanceof CubaLegacySettings) {
                Settings settings = ((CubaLegacySettings) window.getFrameOwner()).getSettings();
                if (settings != null) {
                    walkComponents(tabContent, (settingsComponent, name) -> {
                        if (settingsComponent.getId() != null
                                && settingsComponent instanceof HasSettings) {
                            Element e = settings.get(name);
                            ((HasSettings) settingsComponent).applySettings(e);

                            if (component instanceof HasTablePresentations
                                    && e.attributeValue("presentation") != null) {
                                final String def = e.attributeValue("presentation");
                                if (!StringUtils.isEmpty(def)) {
                                    UUID defaultId = UUID.fromString(def);
                                    ((HasTablePresentations) component).applyPresentationAsDefault(defaultId);
                                }
                            }
                        }
                    });
                }
            } else {
                super.applySettings(window);
            }
        }
    }
}

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

import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.HasSettings;
import com.haulmont.cuba.gui.components.TabSheet;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.DsContextImplementation;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import com.haulmont.cuba.settings.CubaLegacySettings;
import com.haulmont.cuba.settings.Settings;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.CssLayout;
import io.jmix.ui.component.HasTablePresentations;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.impl.TabSheetImpl;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.function.Consumer;

import static io.jmix.ui.component.ComponentsHelper.walkComponents;

@Deprecated
public class WebTabSheet extends TabSheetImpl implements TabSheet {

    @Override
    protected CssLayout createLazyTabLayout() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        return uiComponents.create(CssLayout.NAME);
    }

    @Override
    public void removeSelectedTabChangeListener(Consumer<SelectedTabChangeEvent> listener) {
        unsubscribe(SelectedTabChangeEvent.class, listener);
    }

    @Override
    protected void checkFrameInitialization() {
        Window window = ComponentsHelper.getWindow(WebTabSheet.this);
        if (window != null) {
            if (window.getFrameOwner() instanceof LegacyFrame) {
                DsContext dsContext = ((LegacyFrame) window.getFrameOwner()).getDsContext();
                if (dsContext != null) {
                    ((DsContextImplementation) dsContext).resumeSuspended();
                }
            }
        } else {
            LoggerFactory.getLogger(WebTabSheet.class).warn("Please specify Frame for TabSheet");
        }
    }

    @Override
    protected LazyTabChangeListener createLazyTabChangeListener(ComponentContainer tabContent,
                                                                Element descriptor,
                                                                ComponentLoader loader) {
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

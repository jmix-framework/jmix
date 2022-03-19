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
import com.haulmont.cuba.gui.components.Accordion;
import com.haulmont.cuba.gui.components.HasSettings;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.DsContextImplementation;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import com.haulmont.cuba.settings.CubaLegacySettings;
import com.haulmont.cuba.settings.Settings;
import com.vaadin.ui.TabSheet;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.CssLayout;
import io.jmix.ui.component.HasTablePresentations;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.impl.AccordionImpl;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.function.Consumer;

import static io.jmix.ui.component.ComponentsHelper.walkComponents;

@Deprecated
public class WebAccordion extends AccordionImpl implements Accordion {

    @Override
    protected CssLayout createLazyTabLayout() {
        return applicationContext.getBean(UiComponents.class).create(com.haulmont.cuba.gui.components.CssLayout.NAME);
    }

    @Override
    public void removeSelectedTabChangeListener(Consumer<SelectedTabChangeEvent> listener) {
        internalRemoveSelectedTabChangeListener(listener);
    }

    @Override
    protected LazyTabChangeListener createLazyTabChangeListener(ComponentContainer tabContent, Element descriptor, ComponentLoader loader) {
        return new CubaLazyTabChangeListener(tabContent, descriptor, loader);
    }

    @Override
    protected void onSelectedTabChangeListener(TabSheet.SelectedTabChangeEvent event) {
        super.onSelectedTabChangeListener(event);

        Window window = ComponentsHelper.getWindow(WebAccordion.this);
        if (window != null) {
            if (window.getFrameOwner() instanceof LegacyFrame) {
                DsContext dsContext = ((LegacyFrame) window.getFrameOwner()).getDsContext();
                if (dsContext != null) {
                    ((DsContextImplementation) dsContext).resumeSuspended();
                }
            }
        } else {
            LoggerFactory.getLogger(AccordionImpl.class)
                    .warn("Please specify Frame for Accordion");
        }
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

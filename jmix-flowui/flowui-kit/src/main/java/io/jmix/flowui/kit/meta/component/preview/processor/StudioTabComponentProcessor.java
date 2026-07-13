/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.kit.meta.component.preview.processor;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewTabProcessor;

/**
 * Studio preview processor for {@link TabSheet} tabs.
 */
public class StudioTabComponentProcessor implements StudioPreviewTabProcessor {

    @Override
    public boolean addTab(Component parent, Component tab, Component content, int index) {
        if (!(parent instanceof TabSheet tabSheet) || !(tab instanceof Tab realTab)) {
            return false;
        }
        if (index < 0) {
            tabSheet.add(realTab, content);
        } else {
            tabSheet.add(realTab, content, index);
        }
        return true;
    }

    @Override
    public boolean removeTab(Component parent, Component tab) {
        if (!(parent instanceof TabSheet tabSheet) || !(tab instanceof Tab realTab)) {
            return false;
        }
        tabSheet.remove(realTab);
        return true;
    }
}

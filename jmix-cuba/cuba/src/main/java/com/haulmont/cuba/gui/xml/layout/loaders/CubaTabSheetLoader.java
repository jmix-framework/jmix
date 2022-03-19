/*
 * Copyright 2021 Haulmont.
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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.UiComponents;
import io.jmix.ui.component.TabSheet;
import io.jmix.ui.xml.layout.loader.TabSheetLoader;

public class CubaTabSheetLoader extends TabSheetLoader {

    @Override
    protected TabSheet createComponentInternal() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        return uiComponents.create(com.haulmont.cuba.gui.components.TabSheet.NAME);
    }
}

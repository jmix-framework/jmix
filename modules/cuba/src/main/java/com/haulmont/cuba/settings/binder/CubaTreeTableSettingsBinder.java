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

package com.haulmont.cuba.settings.binder;

import com.haulmont.cuba.web.gui.components.WebTreeTable;
import io.jmix.ui.component.Component;
import io.jmix.ui.settings.component.binder.TreeTableSettingsBinder;

@org.springframework.stereotype.Component(CubaTreeTableSettingsBinder.NAME)
public class CubaTreeTableSettingsBinder extends TreeTableSettingsBinder {

    public static final String NAME = "cuba_CubaTreeTableSettingsBinder";

    @Override
    public Class<? extends Component> getComponentClass() {
        return WebTreeTable.class;
    }
}

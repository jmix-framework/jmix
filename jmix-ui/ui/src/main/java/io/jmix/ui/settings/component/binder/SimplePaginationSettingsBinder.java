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

package io.jmix.ui.settings.component.binder;

import io.jmix.ui.component.Component;
import io.jmix.ui.component.impl.SimplePaginationImpl;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.SimplePaginationSettings;

@org.springframework.stereotype.Component("ui_SimplePaginationSettingsBinder")
public class SimplePaginationSettingsBinder extends AbstractPaginationSettingsBinder<SimplePaginationImpl, SimplePaginationSettings> {

    @Override
    public Class<? extends Component> getComponentClass() {
        return SimplePaginationImpl.class;
    }

    @Override
    public Class<? extends ComponentSettings> getSettingsClass() {
        return SimplePaginationSettings.class;
    }

    protected SimplePaginationSettings createSettings() {
        return new SimplePaginationSettings();
    }
}

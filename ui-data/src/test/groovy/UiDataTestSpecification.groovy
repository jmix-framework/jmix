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


import io.jmix.core.BeanLocator
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.settings.ScreenSettings
import io.jmix.ui.testassist.spec.ScreenSpecification
import io.jmix.uidata.UiDataConfiguration
import io.jmix.uidata.settings.ScreenSettingsJson
import io.jmix.uidata.settings.ScreenSettingsManager
import org.springframework.test.context.ContextConfiguration
import test_support.TestUiSettingsCache
import test_support.UiDataTestConfiguration

import javax.inject.Inject

@ContextConfiguration(classes = [CoreConfiguration, DataConfiguration, UiConfiguration, UiDataConfiguration,
        UiDataTestConfiguration])
class UiDataTestSpecification extends ScreenSpecification {

    @Inject
    ScreenSettingsManager settingsManager

    @Inject
    TestUiSettingsCache settingsCache

    @Inject
    BeanLocator beanLocator

    ScreenSettings settings

    void setup() {
        reloadScreenSettings()
    }

    void cleanup() {
        settingsCache.clear()
    }

    protected reloadScreenSettings() {
        settings = beanLocator.getPrototype(ScreenSettingsJson.class, "screenId")
    }
}

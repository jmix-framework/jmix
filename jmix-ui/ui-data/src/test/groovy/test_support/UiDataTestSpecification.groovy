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

package test_support
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

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.screen.Screen
import io.jmix.ui.settings.ScreenSettingsManager
import io.jmix.ui.testassist.spec.ScreenSpecification
import io.jmix.uidata.UiDataConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(classes = [CoreConfiguration, DataConfiguration, EclipselinkConfiguration,
        UiConfiguration, UiDataConfiguration, UiDataTestConfiguration])
class UiDataTestSpecification extends ScreenSpecification {

    @Autowired
    ScreenSettingsManager settingsManager

    @Autowired
    TestUiSettingsCache settingsCache

    @Autowired
    JdbcTemplate jdbcTemplate

    void cleanup() {
        settingsCache.clear()

        jdbcTemplate.update('delete from UI_SETTING')
        jdbcTemplate.update('delete from UI_TABLE_PRESENTATION')
    }

    def <T extends Screen> T createAndShow(Class<T> screenClass) {
        def screen = screens.create(screenClass)
        return (T) screen.show()
    }
}

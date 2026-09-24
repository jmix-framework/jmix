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

package localized_string

import io.jmix.core.CoreConfiguration
import io.jmix.core.LocalizedStringSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification
import test_support.base.TestBaseConfiguration
import test_support.localized_string.TestLocalizedStringConfiguration

// 'tlh' is a language the JDK does not know, so only the configuration makes it a locale.
@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration, TestLocalizedStringConfiguration])
@TestPropertySource(properties = ["jmix.core.available-locales=en,tlh"])
class LocalizedStringAvailableLocaleKeyTest extends Specification {

    @Autowired
    LocalizedStringSupport support

    def "the language of an available locale is known even when the JDK does not know it"() {
        expect: "so the entry the editor writes for that locale is read back"
        support.isEntryKey('tlh')
        support.parse('Default\ntlh=Klingon').getValue('tlh') == 'Klingon'
    }
}

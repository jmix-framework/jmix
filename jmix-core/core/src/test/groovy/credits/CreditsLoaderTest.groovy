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

package credits

import io.jmix.core.CoreConfiguration
import io.jmix.core.credits.CreditsLoader
import io.jmix.core.credits.CreditsHtmlRenderer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.TestContextInititalizer
import test_support.addon1.TestAddon1Configuration
import test_support.app.TestAppConfiguration
import test_support.base.TestBaseConfiguration

@ContextConfiguration(
        classes = [TestAppConfiguration, TestAddon1Configuration, TestBaseConfiguration, CoreConfiguration],
        initializers = [TestContextInititalizer]
)
class CreditsLoaderTest extends Specification {

    @Autowired
    CreditsLoader creditsLoader

    def "load credits"() {
        when:
        def credits = creditsLoader.getCredits()

        then:
        !credits.isEmpty()

        def spring = credits.find { it.name == 'Spring' }
        spring.url == 'https://spring.io/'
        spring.licenseName == 'Apache License, Version 2.0'
        spring.licenseUrl == 'https://www.apache.org/licenses/LICENSE-2.0'

        def apacheLib1 = credits.find { it.name == 'Some Apache Lib 1' }
        apacheLib1.url == 'https://some-apache-lib-1.org/'
        apacheLib1.licenseName == 'Apache License, Version 2.0'
        apacheLib1.licenseUrl == 'https://www.apache.org/licenses/LICENSE-2.0'

        def cddlLib2 = credits.find { it.name == 'Some CDDL Lib 2' }
        cddlLib2.licenseName == 'Common Development and Distribution License 1.0'
        cddlLib2.licenseUrl == 'https://opensource.org/licenses/CDDL-1.0'

        def licOverridelLib1 = credits.find { it.name == 'License Override Test Lib 1' }
        licOverridelLib1.licenseName == 'Not overridden'
        licOverridelLib1.licenseUrl == 'http://not-overridden'

        def itemOverridelLib1 = credits.find { it.name == 'Item Override Test Lib 1' }
        itemOverridelLib1.url == 'https://overridden.org/'
        itemOverridelLib1.licenseName == 'Overridden'
        itemOverridelLib1.licenseUrl == 'http://lic-overridded.org'
    }

    def "render credits"() {
        when:
        def credits = creditsLoader.getCredits()

        def renderer = new CreditsHtmlRenderer(true)
        def result = renderer.render(credits)

        then:
        result.length > 0
    }
}

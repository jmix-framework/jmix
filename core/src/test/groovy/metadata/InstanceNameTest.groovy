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

package metadata

import io.jmix.core.InstanceNameProvider
import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.Metadata
import io.jmix.core.security.CurrentUserSession
import io.jmix.core.security.UserSession
import io.jmix.core.security.impl.AuthenticatorImpl
import org.apache.commons.lang3.LocaleUtils
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import spock.lang.Specification
import test_support.AppContextTestExecutionListener
import test_support.addon1.TestAddon1Configuration
import test_support.app.TestAppConfiguration
import test_support.app.entity.Address
import test_support.app.entity.Owner

import javax.inject.Inject
import java.util.stream.Collectors

@ContextConfiguration(classes = [JmixCoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
@TestExecutionListeners(value = AppContextTestExecutionListener,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class InstanceNameTest extends Specification {

    @Inject
    Metadata metadata

    @Inject
    InstanceNameProvider instanceNameProvider

    @Inject
    AuthenticatorImpl authenticator

    def "instance name method with injected Locale"() {

        def address = metadata.create(Address)
        address.city = "Samara"
        address.zip = "443011"

        expect:

        instanceNameProvider.getInstanceName(address) == "City: Samara, zip: 443011"
    }

    def "instance name method with ru Locale"() {

        def address = metadata.create(Address)
        address.city = "Samara"
        address.zip = "443011"

        authenticator.begin()
        UserSession session = CurrentUserSession.get()
        session.setLocale(LocaleUtils.toLocale("ru"))

        expect:

        instanceNameProvider.getInstanceName(address) == "Город: Samara, индекс: 443011"
        instanceNameProvider.getInstanceNameRelatedProperties(metadata.getClass(Address),true).stream()
                .map{p->p.getName()}
                .collect(Collectors.toSet()) == ["city", "zip"] as Set

        cleanup:
        authenticator.end()
    }

    def "instance name property"() {

        def owner = metadata.create(Owner)
        owner.name = "John"

        expect:

        instanceNameProvider.getInstanceName(owner) == "John"
        instanceNameProvider.getInstanceNameRelatedProperties(metadata.getClass(Owner),true).stream()
                .map{p->p.getName()}
                .collect(Collectors.toSet()) == ["name"] as Set
    }
}

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

package role_localization

import io.jmix.core.CoreConfiguration
import io.jmix.core.MessageTools
import io.jmix.core.Messages
import io.jmix.core.annotation.MessageSourceBasenames
import io.jmix.security.SecurityConfiguration
import io.jmix.security.impl.role.provider.AnnotatedResourceRoleProvider
import io.jmix.security.role.ResourceRoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.SecurityTestConfiguration
import test_support.TestContextInititalizer
import test_support.role_localization.TestLateBundleRole

/**
 * The application adds a bundle with {@link MessageSourceBasenames}; the bundle is registered only when
 * {@code MessageSourceConfiguration} is initialized. A bean created before it, here the configuration that declares
 * the annotation, needs the role providers, so they build their roles first. A message read before the bundle is
 * registered would cache the bundles of the default locale without it.
 */
@ContextConfiguration(
        classes = [EarlyProviderClientConfiguration, CoreConfiguration, SecurityConfiguration, SecurityTestConfiguration],
        initializers = [TestContextInititalizer]
)
class RoleTextsFromLateBundleTest extends Specification {

    @Autowired
    ResourceRoleRepository resourceRoleRepository
    @Autowired
    Messages messages
    @Autowired
    MessageTools messageTools

    def "a role takes its text from a bundle the application adds, though its provider is created before it"() {
        expect:
        resourceRoleRepository.getRoleByCode(TestLateBundleRole.CODE).name == 'Role from a late bundle'
        messages.getMessage('test_support.role_localization/lateBundle.other', messageTools.defaultLocale) ==
                'Another message of the late bundle'
    }

    @Configuration
    @MessageSourceBasenames('test_support/role_localization/late_messages')
    static class EarlyProviderClientConfiguration {

        @Autowired
        AnnotatedResourceRoleProvider annotatedResourceRoleProvider
    }
}

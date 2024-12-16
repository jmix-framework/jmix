/*
 * Copyright 2024 Haulmont.
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


import io.jmix.core.Metadata
import io.jmix.core.UnconstrainedDataManager
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SystemAuthenticator
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.multitenancy.MultitenancyConfiguration
import io.jmix.multitenancy.core.TenantProvider
import io.jmix.securitydata.SecurityDataConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.MultitenancyFlowuiTestConfiguration
import test_support.entity.SimpleUser

@ContextConfiguration(classes = [SecurityDataConfiguration,
        EclipselinkConfiguration,
        MultitenancyFlowuiTestConfiguration,
        MultitenancyConfiguration])
class TenantProviderTest extends Specification {

    @Autowired
    private UnconstrainedDataManager unconstrainedDataManager
    @Autowired
    private SystemAuthenticator authenticator;
    @Autowired
    private TenantProvider tenantProvider;
    @Autowired
    private Metadata metadata
    @Autowired
    private InMemoryUserRepository userRepository;

    SimpleUser simpleUser

    void setup() {
        simpleUser = metadata.create(SimpleUser)
        simpleUser.setUsername("simpleUser")
        simpleUser.setPassword("{noop}simpleUser")
        simpleUser.setTenantId("simpleUser")
        userRepository.addUser(simpleUser)

        unconstrainedDataManager.save(simpleUser)

        authenticator.begin("simpleUser")
    }

    void cleanup() {
        authenticator.end()

        unconstrainedDataManager.remove(simpleUser)
    }

    def "TenantProvider should return tenantId from super class"() {
        when: "Pass to TenantProvider class where tenant field is in ancestor"

        String tenantField = tenantProvider.getCurrentUserTenantId()

        then: "TenantProvider must return tenant field from ancestor"

        tenantField == simpleUser.getTenantId()
    }
}

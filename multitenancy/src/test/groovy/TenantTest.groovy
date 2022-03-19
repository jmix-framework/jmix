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


import io.jmix.core.AccessConstraintsRegistry
import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.core.UnconstrainedDataManager
import io.jmix.core.metamodel.model.MetaProperty
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.multitenancy.MultitenancyConfiguration
import io.jmix.multitenancy.core.TenantEntityOperation
import io.jmix.multitenancy.core.TenantProvider
import io.jmix.multitenancy.entity.Tenant
import io.jmix.securitydata.SecurityDataConfiguration
import io.jmix.securityui.SecurityUiConfiguration
import io.jmix.ui.UiConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.MultitenancyTestConfiguration
import test_support.entity.TestTenantEntity
import test_support.entity.User

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

@ContextConfiguration(classes = [SecurityUiConfiguration.class,
        UiConfiguration.class, SecurityDataConfiguration, EclipselinkConfiguration, MultitenancyTestConfiguration, MultitenancyConfiguration])
class TenantTest extends Specification {

    @Autowired
    private UnconstrainedDataManager unconstrainedDataManager

    @Autowired
    private DataManager dataManager

    @Autowired
    private AccessConstraintsRegistry accessConstraintsRegistry

    @Autowired
    private InMemoryUserRepository userRepository;

    @Autowired
    private Metadata metadata

    @Autowired
    private AuthenticationManager authenticationManager

    @Autowired
    private TenantEntityOperation tenantEntityOperation

    User admin
    User tenantAdminA
    User tenantAdminB
    Tenant tenantA
    Tenant tenantB
    User tenantUserA
    User tenantUserB
    TestTenantEntity testTenantEntityA
    TestTenantEntity testTenantEntityB
    TestTenantEntity testTenantEntity

    void setup() {

        admin = metadata.create(User.class)
        admin.setUsername("admin")
        admin.setPassword("{noop}admin")
        userRepository.addUser(admin)

        tenantAdminA = metadata.create(User.class)
        tenantAdminA.setUsername("tenantAdminA")
        tenantAdminA.setPassword("{noop}tenantAdminA")
        userRepository.addUser(tenantAdminA)

        tenantAdminB = metadata.create(User.class)
        tenantAdminB.setUsername("tenantAdminB")
        tenantAdminB.setPassword("{noop}tenantAdminB")
        userRepository.addUser(tenantAdminB)

        tenantA = metadata.create(Tenant.class)
        tenantA.setTenantId("tenantA")
        tenantA.setName("tenantA")
        unconstrainedDataManager.save(tenantA)

        tenantB = metadata.create(Tenant.class)
        tenantB.setTenantId("tenantB")
        tenantB.setName("tenantB")
        unconstrainedDataManager.save(tenantB)

        tenantUserA = metadata.create(User.class)
        tenantUserA.setUsername("tenantA")
        tenantUserA.setPassword("{noop}tenantA")
        tenantUserA.setTenantId("tenantA")

        userRepository.addUser(tenantUserA)

        tenantUserB = metadata.create(User.class)
        tenantUserB.setUsername("tenantB")
        tenantUserB.setPassword("{noop}tenantB")
        tenantUserB.setTenantId("tenantB")
        userRepository.addUser(tenantUserB)

        testTenantEntityA = metadata.create(TestTenantEntity.class)
        testTenantEntityA.setIdForTenant("tenantA")
        testTenantEntityA.setName("A")
        unconstrainedDataManager.save(testTenantEntityA)

        testTenantEntityB = metadata.create(TestTenantEntity.class)
        testTenantEntityB.setIdForTenant("tenantB")
        testTenantEntityB.setName("B")
        unconstrainedDataManager.save(testTenantEntityB)

        testTenantEntity = metadata.create(TestTenantEntity.class)
        testTenantEntity.setName("without tenant")
        unconstrainedDataManager.save(testTenantEntity)
    }

    void cleanup() {
        unconstrainedDataManager.remove(admin,
                tenantUserA,
                tenantUserB,
                tenantAdminA,
                tenantAdminB,
                tenantA,
                tenantB,
                testTenantEntity,
                testTenantEntityA,
                testTenantEntityB
        )
    }

    def "test user login without tenant"() {
        when:

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken("admin", "admin");
        Authentication authentication = authenticationManager.authenticate(authenticationToken)

        then:
        authentication != null
        User user = authentication.getPrincipal() as User
        user != null
        user.getTenantId() == null
    }

    def "test user login with tenant"() {
        when:

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken("tenantA", "tenantA");
        Authentication authentication = authenticationManager.authenticate(authenticationToken)

        then:
        authentication != null
        User userFromAuthentication = authentication.getPrincipal() as User
        userFromAuthentication != null
        userFromAuthentication.getTenantId() != null
        tenantA.getTenantId() == userFromAuthentication.getTenantId()
    }

    def "get tenant property for user entity"() {
        when:
        MetaProperty metaProperty = tenantEntityOperation.findTenantProperty(User.class);
        then:
        "tenantId" == metaProperty.getName()
    }

    def "get tenant property for tenant entity"() {
        when:
        MetaProperty metaProperty = tenantEntityOperation.findTenantProperty(Tenant.class);
        then:
        "tenantId" == metaProperty.getName()
    }

    def "set tenant property for user entity"() {
        when:
        tenantEntityOperation.setTenant(tenantUserB, TenantProvider.NO_TENANT);
        then:
        TenantProvider.NO_TENANT == tenantUserB.getTenantId()
    }


}

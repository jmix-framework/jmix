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


import io.jmix.core.Metadata
import io.jmix.core.UnconstrainedDataManager
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.multitenancy.MultitenancyConfiguration
import io.jmix.security.role.ResourceRoleRepository
import io.jmix.securitydata.SecurityDataConfiguration
import io.jmix.securityui.SecurityUiConfiguration
import io.jmix.securityui.authentication.LoginScreenAuthenticationSupport
import io.jmix.ui.UiConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.MultitenancyTestConfiguration
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
//@OverrideAutoConfiguration(enabled = false)
class TenantTest extends Specification {

    @Autowired
    private UnconstrainedDataManager unconstrainedDataManager

    @Autowired
    private InMemoryUserRepository userRepository;

    @Autowired
    private Metadata metadata

    @Autowired
    private LoginScreenAuthenticationSupport authenticatorSupport

    @Autowired
    private ResourceRoleRepository resourceRoleRepository

    private User userA
    private User userB
    private User admin


    void setup() {
        admin = metadata.create(User.class)
        admin.setFirstName("Admin")
        admin.setUsername("admin")
        admin.setPassword("{noop}admin123")

        userRepository.addUser(admin);

        userA = metadata.create(User.class)
        userA.setFirstName("User A")
        userA.setUsername("userA")
        userA.setPassword("{noop}123123")

        userRepository.addUser(userA)

        userB = metadata.create(User.class)
        userB.setFirstName("User B")
        userB.setUsername("userB")
        userB.setPassword("{noop}123123")

        userRepository.addUser(userB)
    }

    def "authentication with tenant id in url param"() {
//        tenantConfig.setLoginByTenantParamEnabled(true);
//
//        AuthenticationManager lw = AppBeans.get(AuthenticationManager.NAME);
//        Credentials credentials = new LoginPasswordCredentials("userA", PASSWORD, Locale.ENGLISH);
//        try {
//            lw.login(credentials);
//            fail();
//        } catch (LoginException e) {
//            assertThat(e.getMessage(), containsString("Unknown login name or bad password 'userA'"));
//        }
//
//        tenantConfig.setLoginByTenantParamEnabled(false);
    }

    def "test authentication without tenant id in url param"() {
//        tenantConfig.setLoginByTenantParamEnabled(false);
//
//        AuthenticationManager lw = AppBeans.get(AuthenticationManager.NAME);
//        Credentials credentials = new LoginPasswordCredentials("userA", PASSWORD, Locale.ENGLISH);
//        UserSession userSession = lw.login(credentials).getSession();
//
//        assertNotNull(userSession);
//
//        tenantConfig.setLoginByTenantParamEnabled(true);
    }

    def "test user login without tenant"() {
//        when:
//        Authentication authentication = authenticatorSupport.authenticate( AuthDetails.of("admin", "admin123").withRememberMe(false), null)
//
//        then:
//        authentication != null
//        int i = 0;
    }

//    @Test
//    public void testUserLoginWithTenant() throws LoginException {
//        AuthenticationManager lw = AppBeans.get(AuthenticationManager.NAME);
//        Credentials credentials = new LoginPasswordCredentials("userA", PASSWORD, Locale.getDefault(),
//                ParamsMap.of(tenantConfig.getTenantIdUrlParamName(), "tenant-a"));
//        UserSession userSession = lw.login(credentials).getSession();
//
//        assertNotNull(userSession);
//
//        UserSessionSource uss = AppBeans.get(UserSessionSource.class);
//        UserSession savedUserSession = uss.getUserSession();
//        ((MultiTenancyUserSessionSource) uss).setUserSession(userSession);
//        try {
//            DataManager dm = AppBeans.get(DataManager.NAME);
//            LoadContext<Group> loadContext = LoadContext.create(Group.class)
//                    .setQuery(new LoadContext.Query("select g from sec$Group g"));
//            List<Group> list = dm.loadList(loadContext);
//            assertEquals(1, list.size());
//
//            Group group = list.get(0);
//            assertEquals(group, groupA);
//        } finally {
//            ((MultiTenancyUserSessionSource) uss).setUserSession(savedUserSession);
//        }
//    }

}

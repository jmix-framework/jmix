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

package test_support;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.DataManager;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.rest.RestConfiguration;
import io.jmix.samples.rest.SampleRestApplication;
import io.jmix.samples.rest.security.FullAccessRole;
import io.jmix.security.SecurityConfiguration;
import io.jmix.security.authentication.RoleGrantedAuthority;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;

import static test_support.RestTestUtils.getAuthToken;

@ContextConfiguration(classes = {
        CoreConfiguration.class,
        DataConfiguration.class,
        EclipselinkConfiguration.class,
        SecurityConfiguration.class,
        RestConfiguration.class,
        JmixRestTestConfiguration.class})
@SpringBootTest(classes = SampleRestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractRestControllerFT {

    protected static final String DB_URL = "jdbc:hsqldb:mem:testdb";

    @LocalServerPort
    protected int port;

    @Autowired
    protected JdbcTemplate jdbcTemplate = new JdbcTemplate();

    @Autowired
    protected InMemoryUserRepository userRepository;

    @Autowired
    protected ResourceRoleRepository resourceRoleRepository;

    @Autowired
    protected RowLevelRoleRepository rowLevelRoleRepository;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected DynAttrMetadata dynAttrMetadata;

    protected UserDetails admin;

    protected Connection conn;
    protected DataSet dirtyData = new DataSet();
    protected String oauthToken;
    protected String baseUrl;
    protected String oauthUrl;

    @BeforeEach
    public void setUp() throws Exception {
        admin = User.builder()
                .username("admin")
                .password("{noop}admin123")
                .authorities(RoleGrantedAuthority.ofResourceRole(resourceRoleRepository.getRoleByCode(FullAccessRole.NAME)))
                .build();

        userRepository.addUser(admin);

        oauthUrl = "http://localhost:" + port + "/";
        baseUrl = "http://localhost:" + port + "/rest";

        oauthToken = getAuthToken(oauthUrl, "admin", "admin123");
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        conn = DriverManager.getConnection(DB_URL, "sa", "");
        prepareDb();
    }

    @AfterEach
    public void tearDown() throws Exception {
        dirtyData.cleanup(conn);
        if (conn != null) {
            conn.close();
        }
        userRepository.removeUser(admin);
    }

    public void prepareDb() throws Exception {
    }
}

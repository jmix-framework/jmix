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

package role_detail;

import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.flowui.component.valuepicker.JmixValuePicker;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelDetailView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.SecurityFlowuiTestConfiguration;
import test_support.TestNoRoleDescriptionsUiAuthenticator;


import static org.assertj.core.api.Assertions.assertThat;

/**
 * The user may not view the localized values of a role, so security hides the fields bound to them, and the view must
 * not show them again for a database role.
 */
@UiTest(viewBasePackages = {"io.jmix.securityflowui.view", "test_support.view"},
        authenticator = TestNoRoleDescriptionsUiAuthenticator.class)
@SpringBootTest(classes = {SecurityFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class ResourceRoleModelDetailViewSecurityTest {

    static final String TRANSLATED_ROLE_CODE = "database-translated";

    @Autowired
    ViewNavigationSupport viewNavigationSupport;
    @Autowired
    UrlParamSerializer urlParamSerializer;
    @Autowired
    UnconstrainedDataManager dataManager;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    ResourceRoleRepository resourceRoleRepository;

    @BeforeEach
    void setUp() {
        ResourceRoleEntity translatedRole = dataManager.create(ResourceRoleEntity.class);
        translatedRole.setCode(TRANSLATED_ROLE_CODE);
        translatedRole.setName("Buchhaltung");
        translatedRole.setLocalizedNames("en=Accounting");
        translatedRole.setLocalizedDescriptions("en=Keeps the books");
        dataManager.save(translatedRole);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from SEC_RESOURCE_ROLE");
        // The repository caches roles by code and does not see a delete through JDBC.
        resourceRoleRepository.invalidateCache();
    }

    @Test
    void localizedValueFields_userWhoMayNotViewThem_areHidden() {
        viewNavigationSupport.navigate(ResourceRoleModelDetailView.class,
                new RouteParameters(ResourceRoleModelDetailView.ROUTE_PARAM_NAME,
                        urlParamSerializer.serialize(TRANSLATED_ROLE_CODE)));
        ResourceRoleModelDetailView view = UiTestUtils.getCurrentView();
        JmixValuePicker<String> localizedNamesField = UiTestUtils.getComponent(view, "localizedNamesField");
        JmixValuePicker<String> localizedDescriptionsField =
                UiTestUtils.getComponent(view, "localizedDescriptionsField");

        assertThat(localizedNamesField.isVisible()).isFalse();
        assertThat(localizedDescriptionsField.isVisible()).isFalse();
    }
}

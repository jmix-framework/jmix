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

package role_filter;

import io.jmix.flowui.UiComponents;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.security.model.BaseRole;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RoleSourceType;
import io.jmix.security.role.RoleLocalizationSupport;
import io.jmix.securityflowui.component.rolefilter.RoleFilter;
import io.jmix.securityflowui.component.rolefilter.RoleFilterChangeEvent;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.SecurityFlowuiTestConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The filter resolves the name of a role only when the name field is filled and the other fields match.
 */
@UiTest
@SpringBootTest(classes = {SecurityFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class RoleFilterChangeEventTest {

    @Autowired
    UiComponents uiComponents;

    RoleFilter roleFilter;
    TestRoleLocalizationSupport roleLocalizationSupport;
    ResourceRole role;

    @BeforeEach
    void setUp() {
        roleFilter = uiComponents.create(RoleFilter.class);
        roleLocalizationSupport = new TestRoleLocalizationSupport();
        roleFilter.setRoleLocalizationSupport(roleLocalizationSupport);

        role = new ResourceRole();
        role.setCode("accounting");
        role.setName("Buchhaltung");
        role.setSource(RoleSourceType.ANNOTATED_CLASS.getId());
    }

    @Test
    void matches_emptyName_doesNotResolveName() {
        assertThat(new RoleFilterChangeEvent(roleFilter, "", "account", null).matches(role)).isTrue();

        assertThat(roleLocalizationSupport.nameCalls).isZero();
    }

    @Test
    void matches_codeDoesNotMatch_doesNotResolveName() {
        assertThat(new RoleFilterChangeEvent(roleFilter, "count", "sales", null).matches(role)).isFalse();

        assertThat(roleLocalizationSupport.nameCalls).isZero();
    }

    @Test
    void matches_name_comparesLocalizedName() {
        assertThat(new RoleFilterChangeEvent(roleFilter, "count", null, null).matches(role)).isTrue();

        assertThat(roleLocalizationSupport.nameCalls).isEqualTo(1);
    }

    /**
     * Translates every name to "Accounting" and counts the calls.
     */
    @NullMarked
    static class TestRoleLocalizationSupport extends RoleLocalizationSupport {

        int nameCalls;

        @Override
        public String getLocalizedName(BaseRole role) {
            nameCalls++;
            return "Accounting";
        }
    }
}

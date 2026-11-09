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
package test_support;

import io.jmix.securityflowui.role.UiFilterRole;
import test_support.role.TestUserFilterRole;

import java.util.List;

/**
 * Authenticates UI tests as an English user who may read users and edit their filters: {@link UiFilterRole} and
 * the minimal UI policies, no access to other entities.
 */
public class TestUserFilterUiAuthenticator extends TestUiAuthenticator {

    @Override
    protected List<String> getRoleCodes() {
        return List.of(TestUserFilterRole.CODE, UiFilterRole.CODE);
    }
}

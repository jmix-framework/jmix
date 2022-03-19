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

package io.jmix.securityui.role;

import io.jmix.core.entity.KeyValueEntity;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(name = "UI: minimal access", code = UiMinimalRole.CODE, scope = SecurityScope.UI)
public interface UiMinimalRole {

    String CODE = "ui-minimal";

    @ScreenPolicy(screenIds = "main")
    void main();

    @ScreenPolicy(screenIds = "login")
    @SpecificPolicy(resources = "ui.loginToUi")
    void login();

    @ScreenPolicy(screenIds = "backgroundWorkProgressScreen")
    void backgroundWork();

    @ScreenPolicy(screenIds = "ui_LayoutAnalyzerScreen")
    void layoutAnalyzer();

    @ScreenPolicy(screenIds = "inputDialog")
    void inputDialog();

    @ScreenPolicy(screenIds = "notFoundScreen")
    void notFoundScreen();

    @ScreenPolicy(screenIds = "selectValueDialog")
    void selectValueDialog();

    @EntityPolicy(entityClass = KeyValueEntity.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = KeyValueEntity.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void keyValueEntity();
}

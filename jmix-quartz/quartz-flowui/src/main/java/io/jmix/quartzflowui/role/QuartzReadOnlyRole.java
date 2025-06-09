/*
 * Copyright 2025 Haulmont.
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

package io.jmix.quartzflowui.role;

import io.jmix.quartz.model.JobDataParameterModel;
import io.jmix.quartz.model.JobModel;
import io.jmix.quartz.model.TriggerModel;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Quartz: Read-only access", code = QuartzReadOnlyRole.CODE, scope = "UI")
public interface QuartzReadOnlyRole {

    String CODE = "quartz-read-only";

    @MenuPolicy(menuIds = "quartz_JobModel.list")
    @ViewPolicy(viewIds = {"quartz_JobModel.list", "quartz_JobModel.detail", "quartz_TriggerModel.detail"})
    void views();

    @EntityAttributePolicy(entityClass = JobModel.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = JobModel.class, actions = EntityPolicyAction.READ)
    void jobModel();

    @EntityAttributePolicy(entityClass = JobDataParameterModel.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = JobDataParameterModel.class, actions = EntityPolicyAction.READ)
    void jobDataParameterModel();

    @EntityAttributePolicy(entityClass = TriggerModel.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = TriggerModel.class, actions = EntityPolicyAction.READ)
    void triggerModel();
}

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

package io.jmix.samples.rest.security;

import io.jmix.samples.rest.entity.driver.Car;
import io.jmix.samples.rest.entity.driver.Model;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;

import static io.jmix.security.model.EntityAttributePolicyAction.VIEW;
import static io.jmix.security.model.EntityPolicyAction.READ;

@ResourceRole(name = CarReadAllAttributesRole.NAME, code = CarReadAllAttributesRole.NAME, scope = SecurityScope.API)
public interface CarReadAllAttributesRole {

    String NAME = "car-read-all-attr-access";

    // Car entity
    @EntityPolicy(entityClass = Car.class, actions = READ)
    @EntityAttributePolicy(entityName = "ref_Car", attributes = "*", action = VIEW)
    // Model entity
    @EntityPolicy(entityClass = Model.class, actions = READ)
    // rest
    @SpecificPolicy(resources = "rest.enabled")
    void access();

}

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

package test_support.role;

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import test_support.entity.sales.Customer;

/**
 * Reads customers: every attribute except {@code systemNote}.
 */
@ResourceRole(name = SecCustomerReadRole.CODE, code = SecCustomerReadRole.CODE)
public interface SecCustomerReadRole {

    String CODE = "test-customer-read";

    @EntityPolicy(entityClass = Customer.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = Customer.class, attributes = {"id", "name", "orders", "tags"},
            action = EntityAttributePolicyAction.VIEW)
    void customer();
}

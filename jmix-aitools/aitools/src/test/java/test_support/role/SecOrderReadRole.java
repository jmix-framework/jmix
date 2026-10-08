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
import test_support.entity.sales.Order;
import test_support.entity.sales.OrderApproval;
import test_support.entity.sales.OrderLine;
import test_support.entity.sales.OrderShipment;

/**
 * Reads orders, their lines and shipments fully, and order approvals without {@code approvedBy}.
 */
@ResourceRole(name = SecOrderReadRole.CODE, code = SecOrderReadRole.CODE)
public interface SecOrderReadRole {

    String CODE = "test-order-read";

    @EntityPolicy(entityClass = Order.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = Order.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void order();

    @EntityPolicy(entityClass = OrderLine.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = OrderLine.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void orderLine();

    @EntityPolicy(entityClass = OrderShipment.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = OrderShipment.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void orderShipment();

    @EntityPolicy(entityClass = OrderApproval.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = OrderApproval.class, attributes = "id", action = EntityAttributePolicyAction.VIEW)
    void approval();
}

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

package test_support.role;

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.*;
import test_support.entity.Issue;
import test_support.entity.OrderInfo;
import test_support.entity.TestOrder;

@ResourceRole(name = TestDataManagerReadQueryRole.NAME, code = TestDataManagerReadQueryRole.NAME)
@RowLevelRole(name = TestDataManagerReadQueryRole.NAME, code = TestDataManagerReadQueryRole.NAME)
public interface TestDataManagerReadQueryRole {
    String NAME = "TestDataManagerReadQueryRole";

    @EntityPolicy(entityClass = TestOrder.class,
            actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = TestOrder.class, attributes = "*",
           action = EntityAttributePolicyAction.MODIFY)
    @JpqlRowLevelPolicy(entityClass = TestOrder.class, where = "{E}.number like 'allowed_%'")
    void order();

    @EntityPolicy(entityClass = OrderInfo.class,
            actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = OrderInfo.class, attributes = "*",
            action = EntityAttributePolicyAction.MODIFY)
    @JpqlRowLevelPolicy(entityClass = OrderInfo.class, join = "{E}.order o", where = "o.number like 'allowed_%'")
    void orderInfo();
}

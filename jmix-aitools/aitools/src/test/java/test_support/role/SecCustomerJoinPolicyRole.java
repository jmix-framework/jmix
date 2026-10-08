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

import io.jmix.security.role.annotation.JpqlRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;
import test_support.entity.sales.Customer;

/**
 * Shows only customers that have an order, through a policy with a join clause.
 */
@RowLevelRole(name = SecCustomerJoinPolicyRole.CODE, code = SecCustomerJoinPolicyRole.CODE)
public interface SecCustomerJoinPolicyRole {

    String CODE = "test-customer-join-row-level";

    @JpqlRowLevelPolicy(entityClass = Customer.class, join = "join {E}.orders co", where = "co.number is not null")
    void customer();
}

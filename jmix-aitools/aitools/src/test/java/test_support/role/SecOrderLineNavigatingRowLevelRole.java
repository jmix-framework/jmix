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
import test_support.entity.sales.OrderLine;

/**
 * Hides order lines through a condition that navigates from the line to its order, which cannot be rendered in a
 * join's {@code on} clause as it is.
 */
@RowLevelRole(name = SecOrderLineNavigatingRowLevelRole.CODE, code = SecOrderLineNavigatingRowLevelRole.CODE)
public interface SecOrderLineNavigatingRowLevelRole {

    String CODE = "test-order-line-navigating-row-level";

    @JpqlRowLevelPolicy(entityClass = OrderLine.class, where = "{E}.order.number <> 'ORD-HiddenCo'")
    void orderLine();
}

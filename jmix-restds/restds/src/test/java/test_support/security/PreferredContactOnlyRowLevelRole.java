/*
 * Copyright 2024 Haulmont.
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

package test_support.security;

import io.jmix.security.model.RowLevelBiPredicate;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.role.annotation.PredicateRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;
import org.springframework.context.ApplicationContext;
import test_support.entity.CustomerContact;

@RowLevelRole(name = "PreferredContactOnlyRowLevelRole", code = PreferredContactOnlyRowLevelRole.CODE)
public interface PreferredContactOnlyRowLevelRole {
    String CODE = "contact-default-only-row-level-role";

    @PredicateRowLevelPolicy(entityClass = CustomerContact.class, actions = RowLevelPolicyAction.READ)
    default RowLevelBiPredicate<CustomerContact, ApplicationContext> customerContactPredicate() {
        return (customerContact, applicationContext) -> {
            return Boolean.TRUE.equals(customerContact.getPreferred());
        };
    }
}
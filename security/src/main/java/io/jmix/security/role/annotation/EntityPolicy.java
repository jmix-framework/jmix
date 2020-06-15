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

package io.jmix.security.role.annotation;

import io.jmix.core.Entity;
import io.jmix.security.model.EntityPolicyAction;

import java.lang.annotation.*;

/**
 * Defines entity resource policy in annotated role. Multiple {@code EntityPolicy} annotations may be placed on a single
 * method. {@code EntityPolicy} annotation may present on multiple methods of the same class. Annotated method may have
 * any name and return type.
 * <p>
 * If you need to define access for all CRUD operations then instead of listing all of them (CREATE, READ, UPDATE,
 * DELETE) you may use the {@link EntityPolicyAction#ALL} constant.
 * <p>
 * Example:
 * <pre>
 * &#064;Role(name = "My Role", code = "myRole")
 * public interface MyRole {
 *
 *     &#064;EntityPolicy(entityClass = Order.class,
 *         actions = {EntityPolicyAction.CREATE, EntityPolicyAction.READ})
 *     &#064;EntityPolicy(entityClass = Customer.class,
 *         actions = EntityPolicyAction.ALL)
 *     void entities();
 * }
 * </pre>
 *
 * @see io.jmix.security.role.annotation.Role
 * @see io.jmix.security.model.ResourcePolicy
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(EntityPolicyContainer.class)
public @interface EntityPolicy {

    Class<? extends Entity> entityClass();

    EntityPolicyAction[] actions();

}

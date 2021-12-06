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

import io.jmix.security.impl.role.provider.AnnotatedResourceRoleProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that annotated interface is a "Row Level Role". Row level role is a container that holds row-level policies.
 * <p>
 * Row-level policies restrict which data should be return to the user (e.g. a user should only see contracts created by
 * user's department) or which data the user can create, update or delete.
 * <p>
 * Annotated interfaces are parsed by the {@link AnnotatedResourceRoleProvider} and {@link
 * io.jmix.security.model.RowLevelRole} objects are created using the information from the annotated interface.
 * <p>
 * Role definition example:
 * <pre>
 * &#064;RowLevelRole(code = "orderView", name = "Order view")
 * public interface OrderViewRole {
 *
 *     &#064;JpqlRowLevelPolicy(entityClass = TestOrder.class,
 *             join = "join e.customer c",
 *             where = "c.status = 'active'")
 *     void order();
 *
 *     &#064;PredicateRowLevelPolicy(entityClass = Order.class,
 *         actions = {RowLevelPolicyAction.READ})
 *     static RowLevelPredicate&lt;Order&gt; readZeroOrdersOnly() {
 *         return order -&gt; order.getNumber().startsWith("0");
 *     }
 * }
 * </pre>
 * <p>
 * Role interface may have any number of methods. Methods can have any names, methods are used only to group policies
 * logically. Policies may be grouped by entity they relate (as in the example above) or by type (one method will have
 * annotations for screen policies, another one for entity policies, etc.).
 * <p>
 * Method return type matters only for methods with {@link PredicateRowLevelPolicy} annotations.
 *
 * @see JpqlRowLevelPolicy
 * @see PredicateRowLevelPolicy
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RowLevelRole {

    /**
     * Role name.
     */
    String name();

    /**
     * Role code is an unique role identifier. It is used for linking the role with the user.
     */
    String code();

    /**
     * Role description
     */
    String description() default "";

}

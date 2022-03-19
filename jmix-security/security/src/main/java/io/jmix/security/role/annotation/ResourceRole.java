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
import io.jmix.security.model.SecurityScope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that annotated interface is a "Resource Role". Resource role is a container that holds resource policies.
 * <p>
 * Resource policies define permissions for any system resource: screen, entity read or create operation, entity
 * attribute, etc.
 * <p>
 * Annotated interfaces are parsed by the {@link AnnotatedResourceRoleProvider} and {@link
 * io.jmix.security.model.ResourceRole} objects are created using the information from the annotated interface.
 * <p>
 * Role definition example:
 * <pre>
 * &#064;ResourceRole(code = "orderView", name = "Order view")
 * public interface OrderViewRole {
 *
 *     &#064;MenuPolicy(menuIds = {"application", "application-orders"})
 *     &#064;ScreenPolicy(screenIds = {"sample_Order.browse", "sample_Order.edit"})
 *     &#064;EntityPolicy(entityClass = Order.class,
 *         actions = {EntityPolicyAction.CREATE, EntityPolicyAction.READ})
 *     &#064;EntityAttributePolicy(entityClass = Order.class,
 *         attributes = {"number", "date"},
 *         actions = {EntityAttributePolicyAction.UPDATE})
 *     void order();
 *
 *     &#064;ScreenPolicy(screenClasses = {CustomerBrowse.class, CustomerEdit.class})
 *     &#064;EntityPolicy(entityClass = Customer.class,
 *         actions = {EntityPolicyAction.ALL})
 *     &#064;EntityAttributePolicy(entityClass = Customer.class,
 *         attributes = {"*"},
 *         actions = {EntityAttributePolicyAction.UPDATE})
 *     void customer();
 * }
 * </pre>
 * <p>
 * Role interface may have any number of methods. Methods can have any names, methods are used only to group policies
 * logically. Policies may be grouped by entity they relate (as in the example above) or by type (one method will have
 * annotations for screen policies, another one for entity policies, etc.).
 * <p>
 * Method return type matters only for methods with {@link ExplicitResourcePolicies}
 * annotations.
 *
 * @see EntityPolicy
 * @see EntityAttributePolicy
 * @see SpecificPolicy
 * @see JpqlRowLevelPolicy
 * @see PredicateRowLevelPolicy
 * @see ExplicitResourcePolicies
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceRole {

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

    /**
     * Role security scope.
     */
    String[] scope() default {SecurityScope.UI, SecurityScope.API};
}

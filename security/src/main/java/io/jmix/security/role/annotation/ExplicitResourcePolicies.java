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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation must be put on a method of an interface that defines a resource role (see {@link ResourceRole}).
 * <p>
 * Method annotated with {code @ExplicitResourcePolicies} must be a static method should return a collection of
 * {@link io.jmix.security.model.ResourcePolicy}.
 * <p>
 * Example:
 * <p>
 * <pre>
 * &#064;ResourceRole(name = "Test role", code = "testRole")
 * public interface TestExplicitResourcePoliciesRole {
 *
 *     &#064;ExplicitResourcePolicies
 *     static Collection&lt;ResourcePolicy&gt; explicitOrderPolicies() {
 *         List&lt;ResourcePolicy&gt; resourcePolicies = new ArrayList&lt;&gt;();
 *         ResourcePolicy policy1 = new ResourcePolicy(ResourcePolicyType.SCREEN, "sample_Order.browse");
 *         resourcePolicies.add(policy1);
 *         ResourcePolicy policy2 = new ResourcePolicy(ResourcePolicyType.ENTITY, "sample_Order", EntityPolicyAction.READ.getId());
 *         resourcePolicies.add(policy2);
 *         return resourcePolicies;
 *     }
 * }
 * </pre>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExplicitResourcePolicies {
}

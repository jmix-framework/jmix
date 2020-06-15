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
import io.jmix.security.model.RowLevelPolicyAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation must be put on a method of an interface that defines a role (see {@link Role}).
 * <p>
 * Method annotated with {code @PredicateRowLevelPolicy} must be a static method should return a {@link
 * java.util.function.Predicate}. The input parameter of the predicate must be an instance of the class defined in the
 * {@link #entityClass()}.
 * <p>
 * Example:
 * <pre>
 * &#064;Role(name = "TestPredicateRoleLevelPolicyRole", code = "TestPredicateRoleLevelPolicyRole")
 * public interface TestPredicateRoleLevelPolicyRole {
 *
 *     &#064;PredicateRowLevelPolicy(entityClass = TestOrder.class,
 *             actions = {RowLevelPolicyAction.CREATE, RowLevelPolicyAction.UPDATE})
 *     static Predicate<TestOrder> numberStartsWithA() {
 *         return testOrder -> testOrder.getNumber().startsWith("a");
 *     }
 * }
 * </pre>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PredicateRowLevelPolicy {

    /**
     * Entity class on which the predicate must be tested
     */
    Class<? extends Entity> entityClass();

    /**
     * Entity CRUD operations on which the predicate must be tested
     */
    RowLevelPolicyAction[] actions();
}

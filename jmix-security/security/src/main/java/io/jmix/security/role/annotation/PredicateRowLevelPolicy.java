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

import io.jmix.security.model.RowLevelPolicyAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation must be put on a method of an interface that defines a row level role (see {@link RowLevelRole}).
 * <p>
 * Method annotated with {@code PredicateRowLevelPolicy} must meet the following requirements:
 * <ul>
 *     <li>it must be a static or default method</li>
 *     <li>must return a {@link io.jmix.security.model.RowLevelPredicate} or {@link io.jmix.security.model.RowLevelBiPredicate}</li>
 *     <li>must have no arguments</li>
 * </ul>
 * <p>
 * The input parameter of the {@code RowLevelPredicate} must be an instance of the class defined in the {@link #entityClass()}
 * attribute of the annotation.
 * <p>
 * {@code RowLevelBiPredicate} must be used when you need to use Spring beans in the function. The second parameter of the
 * bi-predicate is {@link org.springframework.context.ApplicationContext}.
 * <p>
 * Example:
 * <pre>
 * &#064;RowLevelRole(name = "TestPredicateRoleLevelPolicyRole", code = "TestPredicateRoleLevelPolicyRole")
 * public interface TestPredicateRoleLevelPolicyRole {
 *
 *     &#064;PredicateRowLevelPolicy(entityClass = TestOrder.class,
 *             actions = {RowLevelPolicyAction.CREATE, RowLevelPolicyAction.UPDATE})
 *     static RowLevelPredicate&lt;TestOrder&gt; numberStartsWithA() {
 *         return testOrder -&gt; testOrder.getNumber().startsWith("a");
 *     }
 *
 *     &#064;PredicateRowLevelPolicy(entityClass = TestOrder.class,
 *             actions = {RowLevelPolicyAction.READ})
 *     default RowLevelBiPredicate&lt;TestOrder, ApplicationContext&gt; numberStartsWithB() {
 *         return (testOrder, applicationContext) -&gt; {
 *            CurrentAuthentication currentAuthentication = applicationContext.getBean(CurrentAuthentication.class);
 *            User currentUser = (User) currentAuthentication.getUser();
 *            return currentUser.equals(testOrder.getManager());
 *         }
 *     }
 * }
 * </pre>
 *
 * @see RowLevelRole
 * @see io.jmix.security.model.RowLevelPolicy
 * @see io.jmix.security.model.RowLevelPredicate
 * @see io.jmix.security.model.RowLevelBiPredicate
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PredicateRowLevelPolicy {

    /**
     * Entity class on which the predicate must be tested
     */
    Class<?> entityClass();

    /**
     * Entity CRUD operations on which the predicate must be tested
     */
    RowLevelPolicyAction[] actions();
}

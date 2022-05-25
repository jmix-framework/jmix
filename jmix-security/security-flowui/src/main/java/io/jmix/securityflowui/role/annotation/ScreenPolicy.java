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

package io.jmix.securityflowui.role.annotation;

import io.jmix.security.role.annotation.ResourceRole;

import java.lang.annotation.*;

/**
 * Defines screen resource policy in annotated resource role. Multiple {@code ScreenPolicy} annotations may be placed on a single
 * method. {@code ScreenPolicy} annotation may present on multiple methods of the same class. Annotated method may have
 * any name and return type.
 * <p>
 * Example:
 * <pre>
 * &#064;ResourceRole(name = "My Role", code = "myRole")
 * public interface MyRole {
 *
 *     &#064;ScreenPolicy(screenIds = {"screen1", "screen2"})
 *     &#064;ScreenPolicy(screenClasses = {TestScreen1.class, TestScreen2.class})
 *     void screens();
 * }
 * </pre>
 *
 * @see ResourceRole
 * @see io.jmix.security.model.ResourcePolicy
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ScreenPolicyContainer.class)
public @interface ScreenPolicy {

    String[] screenIds() default {};

    Class[] screenClasses() default {};

}

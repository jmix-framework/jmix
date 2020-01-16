/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the {@code @Configuration} class is a main configuration of a Jmix module.
 *
 * <p>Specifies what this module depends on using the {@link #dependsOn()} attribute.
 *
 * <p>Contains a list of {@link org.springframework.core.env.Environment} properties provided by this module in
 * the {@link #properties()} attribute. Property values are overridden or appended according to the module
 * dependencies tree. This property source has the lowest precedence so the {@link JmixProperty} values can be
 * overridden in the application properties file.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JmixModule {

    /**
     * Optional module id. If not set, the annotated class package name is used.
     */
    String id() default "";

    /**
     * The list of configuration classes this module depends on. The classes must be annotated with {@link JmixModule}
     * themselves.
     */
    Class[] dependsOn();

    /**
     * The list of properties provided by this module. Actual values in the application are defined as a result of
     * overriding or appending according to the module dependencies tree.
     */
    JmixProperty[] properties() default {};
}

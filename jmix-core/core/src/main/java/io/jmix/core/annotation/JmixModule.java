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
    Class[] dependsOn() default AllModules.class;

    class AllModules {
    }
}

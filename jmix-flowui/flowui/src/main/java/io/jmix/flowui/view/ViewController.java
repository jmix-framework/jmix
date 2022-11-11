/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.view;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Target;
import java.lang.annotation.*;

/**
 * Indicates that the annotated class is a view controller.
 * The annotated class must be a direct or indirect subclass of {@link View}.
 *
 * @see View
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ViewController {
    String ID_ATTRIBUTE = "id";
    String VALUE_ATTRIBUTE = "value";

    /**
     * View identifier.
     */
    @AliasFor(ID_ATTRIBUTE)
    String value() default "";

    /**
     * View identifier.
     */
    @AliasFor(VALUE_ATTRIBUTE)
    String id() default "";
}
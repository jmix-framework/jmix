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

import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for declarative supply methods in UI controllers.
 * The value returned by a supplier is passed as an input parameter
 * to the method defined in {@link #subject()}.
 * <p>
 * In the example below, the returned {@link TextRenderer} instance is used as an input
 * parameter for {@link Column#setRenderer(Renderer)}:
 * <pre>
 * &#64;Provide(to = "fooDataGrid.name", subject = "renderer")
 * private Renderer&lt;Foo&gt; nameRenderer() {
 *     return new TextRenderer&lt;&gt;(Foo::getName);
 * }
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.METHOD)
public @interface Supply {

    /**
     * @return type of target
     */
    Target target() default Target.COMPONENT;

    /**
     * @return type of object to supply, can be used instead of {@link #subject()}
     */
    Class<?> type() default Object.class;

    /**
     * @return property name that will be set using annotated method
     */
    String subject() default "";

    /**
     * @return id or path to target object
     */
    String to() default "";

    /**
     * Declares whether the annotated dependency is required.
     * <p>
     * Defaults to {@code true}.
     */
    boolean required() default true;
}

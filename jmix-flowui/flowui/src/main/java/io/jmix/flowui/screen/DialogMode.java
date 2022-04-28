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

package io.jmix.flowui.screen;


import java.lang.annotation.Target;
import java.lang.annotation.*;

/**
 * Specifies parameters of {@link DialogWindow}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface DialogMode {

    String width() default "";
    String maxWidth() default "";
    String minWidth() default "";

    String height() default "";
    String maxHeight() default "";
    String minHeight() default "";

    boolean modal() default true;

    boolean draggable() default true;

    boolean resizable() default false;

    boolean closeOnOutsideClick() default false;

    boolean closeOnEsc() default false;

//    boolean forceDialog() default false;
}
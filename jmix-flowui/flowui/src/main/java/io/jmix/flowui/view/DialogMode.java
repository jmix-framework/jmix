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


import java.lang.annotation.*;
import java.lang.annotation.Target;

/**
 * Specifies parameters of {@link DialogWindow}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface DialogMode {

    /**
     * Specifies the width of the dialog window. The value can be defined as a valid CSS width
     * (e.g., "400px", "50%", "auto"). If not specified, it defaults to an empty string which
     * implies no specific width is set.
     *
     * @return the specified width of the dialog window
     */
    String width() default "";

    /**
     * Specifies the maximum width of the dialog window. The value can be defined as a valid CSS width
     * (e.g., "600px", "70%", "none"). If not specified, it defaults to an empty string, which implies
     * no specific maximum width is set.
     *
     * @return the maximum width of the dialog window
     */
    String maxWidth() default "";

    /**
     * Specifies the minimum width of the dialog window. The value can be defined as a valid CSS width
     * (e.g., "300px", "40%", "auto"). If not specified, it defaults to an empty string, which implies
     * no specific minimum width is set.
     *
     * @return the minimum width of the dialog window
     */
    String minWidth() default "";

    /**
     * Specifies the height of the dialog window. The value can be defined as a valid CSS height
     * (e.g., "200px", "50%", "auto"). If not specified, it defaults to an empty string, which implies
     * no specific height is set.
     *
     * @return the specified height of the dialog window
     */
    String height() default "";

    /**
     * Specifies the maximum height of the dialog window. The value can be defined as a valid CSS height
     * (e.g., "400px", "60%", "none"). If not specified, it defaults to an empty string, which implies
     * no specific maximum height is set.
     *
     * @return the maximum height of the dialog window
     */
    String maxHeight() default "";

    /**
     * Specifies the minimum height of the dialog window. The value can be defined as a valid CSS height
     * (e.g., "100px", "30%", "auto"). If not specified, it defaults to an empty string, which implies
     * no specific minimum height is set.
     *
     * @return the minimum height of the dialog window
     */
    String minHeight() default "";

    /**
     * Returns the left position of the overlay.
     *
     * @return the left position of the overlay
     */
    String left() default "";

    /**
     * Returns the top position of the overlay.
     *
     * @return the top position of the overlay
     */
    String top() default "";

    /**
     * Specifies whether the dialog window should be modal. When set to {@code true}, the dialog
     * will disable interaction with elements outside of the dialog while it is open.
     *
     * @return {@code true} if the dialog is modal, otherwise {@code false}
     */
    boolean modal() default true;

    /**
     * Specifies whether the dialog window can be dragged by the user.
     *
     * @return {@code true} if the dialog is draggable, otherwise {@code false}
     */
    boolean draggable() default true;

    /**
     * Specifies whether the dialog window can be resized by the user.
     *
     * @return {@code true} if the dialog is resizable, otherwise {@code false}
     */
    boolean resizable() default false;

    /**
     * Specifies whether the dialog should close when a click is detected outside of it.
     *
     * @return {@code true} if the dialog should close on outside click, otherwise {@code false}
     */
    boolean closeOnOutsideClick() default false;

    /**
     * Specifies whether the dialog should close when the "Escape" key is pressed.
     *
     * @return {@code true} if the dialog should close on pressing the "Escape" key, otherwise {@code false}
     */
    boolean closeOnEsc() default false;
}
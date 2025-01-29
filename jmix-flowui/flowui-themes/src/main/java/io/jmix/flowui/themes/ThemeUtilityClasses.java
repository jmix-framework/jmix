/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.themes;

import java.lang.annotation.*;

/**
 * Indicates that the annotated class contains a definition for the CSS utility
 * classes so that Studio can suggest them.
 * <p>
 * NOTE: only {@code public static final} fields are considered as CSS class names.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ThemeUtilityClasses {

    /**
     * @return user-friendly name of the annotated class
     */
    String name();
}

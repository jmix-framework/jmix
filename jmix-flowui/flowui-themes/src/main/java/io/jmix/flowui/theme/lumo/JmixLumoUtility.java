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

package io.jmix.flowui.theme.lumo;

import io.jmix.flowui.theme.StyleUtility;
import io.jmix.flowui.theme.ThemeUtilityClasses;

/**
 * Contains the definition for all CSS utility classes provided by Jmix Lumo.
 *
 * @deprecated Use {@link StyleUtility} instead.
 */
@Deprecated
@ThemeUtilityClasses(name = "Jmix Lumo")
public final class JmixLumoUtility {

    private JmixLumoUtility() {
    }

    /**
     * Containers related classes.
     */
    public static final class Container {

        /**
         * @deprecated Use {@link StyleUtility.Container#BUTTONS_PANEL} instead.
         */
        @Deprecated
        public static final String BUTTONS_PANEL = "buttons-panel";

        /**
         * @deprecated Use {@link StyleUtility.Container#BORDERED_PANEL} instead.
         */
        @Deprecated
        public static final String BORDERED_PANEL = "bordered-panel";

        private Container() {
        }
    }
}

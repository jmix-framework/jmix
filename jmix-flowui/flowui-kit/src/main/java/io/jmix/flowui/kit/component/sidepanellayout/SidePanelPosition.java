/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.kit.component.sidepanellayout;

/**
 * Represents the position of the side panel relative to the main content.
 */
public enum SidePanelPosition {

    /**
     * The side panel is placed at the top of the main content.
     */
    TOP,

    /**
     * The side panel is placed to the right of the main content. Does not consider RTL mode.
     */
    RIGHT,

    /**
     * The side panel is placed at the bottom of the main content.
     */
    BOTTOM,

    /**
     * The side panel is placed to the left of the main content. Does not consider RTL mode.
     */
    LEFT,

    /**
     * The side panel is placed at the start position depending on the directionality (RTL or LTR) of the layout.
     */
    INLINE_START,

    /**
     * The side panel is placed at the end position depending on the directionality (RTL or LTR) of the layout.
     */
    INLINE_END
}

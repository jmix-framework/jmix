/*
 * Copyright 2024 Haulmont.
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

package io.jmix.fullcalendarflowui.component.model;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.jspecify.annotations.Nullable;

/**
 * Represents possible preset rendering styles that the event can use.
 */
public enum Display implements EnumClass<String> {

    /**
     * Component will render an event:
     * <ul>
     *     <li>
     *         Day Grid view: as a solid rectangle if it is all-day or multi-day. If a timed event, will render it with a dot.
     *     </li>
     *     <li>
     *         Other views: will render normally.
     *     </li>
     * </ul>
     */
    AUTO("auto"),

    /**
     * Component will render an event:
     * <ul>
     *     <li>
     *         Day Grid: as a solid rectangle.
     *     </li>
     *     <li>
     *         Other views: will render normally.
     *     </li>
     * </ul>
     */
    BLOCK("block"),

    /**
     * Component will render an event:
     * <ul>
     *     <li>
     *         Day Grid: with a dot.
     *     </li>
     *     <li>
     *          Other views: will render normally.
     *     </li>
     * </ul>
     */
    LIST_ITEM("list-item"),

    /**
     * Component will render an event as a background highlights.
     */
    BACKGROUND("background"),

    /**
     * Component will render an event as a background highlights, but the specified time of event will not be
     * occupied by an event.
     */
    INVERSE_BACKGROUND("inverse-background"),

    /**
     * Component won’t render the event at all.
     */
    NONE("none");

    private final String id;

    Display(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * @param id the ID of enum instance
     * @return enum instance or {@code null} if there is no enum with the provided ID
     */
    @Nullable
    public static Display fromId(String id) {
        for (Display at : Display.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
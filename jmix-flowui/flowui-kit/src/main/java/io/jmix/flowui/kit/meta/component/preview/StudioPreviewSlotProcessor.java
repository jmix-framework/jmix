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

package io.jmix.flowui.kit.meta.component.preview;

import com.vaadin.flow.component.Component;

/**
 * Attaches/detaches a child to/from a named slot (e.g. {@code "prefix"}, {@code "navbar"}) of a
 * parent, for containers a parent+child type pair alone cannot disambiguate.
 */
public interface StudioPreviewSlotProcessor extends StudioPreviewComponentProcessor {

    /**
     * Attaches {@code child} to {@code parent}'s {@code slotHint} slot.
     *
     * @param index target index, or a negative value to append
     * @return {@code true} if handled
     */
    boolean addToSlot(Component parent, Component child, int index, String slotHint);

    /**
     * Detaches {@code child} from {@code parent}'s {@code slotHint} slot.
     *
     * @return {@code true} if handled
     */
    boolean removeFromSlot(Component parent, Component child, String slotHint);
}

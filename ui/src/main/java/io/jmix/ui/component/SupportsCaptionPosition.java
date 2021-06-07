/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component;

import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Interface to be implemented by a component that supports different caption positions.
 */
public interface SupportsCaptionPosition {

    /**
     * @return position of component caption
     */
    CaptionPosition getCaptionPosition();

    /**
     * Sets position of component caption.
     *
     * @param position component caption position
     */
    @StudioProperty(name = "captionPosition", defaultValue = "LEFT", options = {"LEFT", "TOP"})
    void setCaptionPosition(CaptionPosition position);

    /**
     * Caption position of the component.
     */
    enum CaptionPosition implements EnumClass<String> {
        /**
         * Component caption will be placed on the left side of component.
         */
        LEFT,

        /**
         * Component caption will be placed above the component.
         */
        TOP;

        @Override
        public String getId() {
            return name();
        }

        @Nullable
        public static CaptionPosition fromId(String id) {
            for (CaptionPosition captionPosition : CaptionPosition.values()) {
                if (Objects.equals(id, captionPosition.getId())) {
                    return captionPosition;
                }
            }
            return null;
        }
    }
}

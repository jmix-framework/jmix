/*
 * Copyright 2023 Haulmont.
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

package io.jmix.chartsflowui.kit.component.model;

import jakarta.annotation.Nullable;

/**
 * A component that has line style.
 *
 * @param <T> origin class type
 */
public interface HasLineStyle<T> {

    /**
     * @return Style for drawing line endpoints
     */
    Cap getCap();

    /**
     * Sets style for drawing line endpoints or replaces an existing one.
     * More detailed information is provided in the documentation.
     *
     * @param cap cap style to set
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D/lineCap">CanvasRenderingContext2D.lineCap [MDN]</a>
     */
    void setCap(Cap cap);

    /**
     * @param cap style to set
     * @return this
     * @see HasLineStyle#setCap(Cap)
     */
    @SuppressWarnings("unchecked")
    default T withCap(Cap cap) {
        setCap(cap);
        return (T) this;
    }

    /**
     * @return style that used to join two line segments where they intersect
     */
    Join getJoin();

    /**
     * Sets a style that used to join two line segments where they intersect or replaces an existing one.
     * More detailed information is provided in the documentation.
     *
     * @param join style to set
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D/lineJoin">CanvasRenderingContext2D.lineJoin [MDN]</a>
     */
    void setJoin(Join join);

    /**
     * @param join join style to set
     * @return this
     * @see HasLineStyle#setJoin(Join)
     */
    @SuppressWarnings("unchecked")
    default T withJoin(Join join) {
        setJoin(join);
        return (T) this;
    }

    /**
     * @return miter limit ratio
     */
    Integer getMiterLimit();

    /**
     * Sets miter limit ratio or replaces an existing one. Only works when {@link Join} is set as {@link Join#MITER}.
     * More detailed information is provided in the documentation.
     *
     * @param miterLimit miter limit to set
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D/miterLimit">CanvasRenderingContext2D.miterLimit [MDN]</a>
     */
    void setMiterLimit(Integer miterLimit);

    /**
     * @param miterLimit miter limit to set
     * @return this
     * @see HasLineStyle#setMiterLimit(Integer)
     */
    @SuppressWarnings("unchecked")
    default T withMiterLimit(Integer miterLimit) {
        setMiterLimit(miterLimit);
        return (T) this;
    }

    /**
     * Style for drawing line endpoints.
     */
    enum Cap implements HasEnumId {
        BUTT("butt"),
        ROUND("round"),
        SQUARE("square");

        private final String id;

        Cap(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static Cap fromId(String id) {
            for (Cap at : Cap.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    /**
     * Style for joining two line segments where they intersect.
     */
    enum Join implements HasEnumId {
        BEVEL("bevel"),
        ROUND("round"),
        MITER("miter");

        private final String id;

        Join(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static Join fromId(String id) {
            for (Join at : Join.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }
}

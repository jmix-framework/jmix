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

public interface HasLineStyle<T> {

    Cap getCap();

    void setCap(Cap cap);

    @SuppressWarnings("unchecked")
    default T withCap(Cap cap) {
        setCap(cap);
        return (T) this;
    }

    Join getJoin();

    void setJoin(Join join);

    @SuppressWarnings("unchecked")
    default T withJoin(Join join) {
        setJoin(join);
        return (T) this;
    }

    Integer getMiterLimit();

    void setMiterLimit(Integer miterLimit);

    @SuppressWarnings("unchecked")
    default T withMiterLimit(Integer miterLimit) {
        setMiterLimit(miterLimit);
        return (T) this;
    }

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

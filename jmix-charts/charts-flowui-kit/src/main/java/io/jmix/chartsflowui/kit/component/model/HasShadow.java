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

import io.jmix.chartsflowui.kit.component.model.shared.Color;

public interface HasShadow<T> {

    Integer getShadowBlur();

    void setShadowBlur(Integer shadowBlur);

    @SuppressWarnings("unchecked")
    default T withShadowBlur(Integer shadowBlur) {
        setShadowBlur(shadowBlur);
        return (T) this;
    }

    Color getShadowColor();

    void setShadowColor(Color shadowColor);

    @SuppressWarnings("unchecked")
    default T withShadowColor(Color shadowColor) {
        setShadowColor(shadowColor);
        return (T) this;
    }

    Integer getShadowOffsetX();

    void setShadowOffsetX(Integer shadowOffsetX);

    @SuppressWarnings("unchecked")
    default T withShadowOffsetX(Integer shadowOffsetX) {
        setShadowOffsetX(shadowOffsetX);
        return (T) this;
    }

    Integer getShadowOffsetY();

    void setShadowOffsetY(Integer shadowOffsetY);

    @SuppressWarnings("unchecked")
    default T withShadowOffsetY(Integer shadowOffsetY) {
        setShadowOffsetY(shadowOffsetY);
        return (T) this;
    }
}

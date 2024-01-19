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

public interface HasText<T> {

    Color getTextBorderColor();

    void setTextBorderColor(Color textBorderColor);

    @SuppressWarnings("unchecked")
    default T withTextBorderColor(Color textBorderColor) {
        setTextBorderColor(textBorderColor);
        return (T) this;
    }

    Double getTextBorderWidth();

    void setTextBorderWidth(Double textBorderWidth);

    @SuppressWarnings("unchecked")
    default T withTextBorderWidth(Double textBorderWidth) {
        setTextBorderWidth(textBorderWidth);
        return (T) this;
    }

    String getTextBorderType();

    void setTextBorderType(String textBorderType);

    @SuppressWarnings("unchecked")
    default T withTextBorderType(String textBorderType) {
        setTextBorderType(textBorderType);
        return (T) this;
    }

    Integer getTextBorderDashOffset();

    void setTextBorderDashOffset(Integer textBorderDashOffset);

    @SuppressWarnings("unchecked")
    default T withTextBorderDashOffset(Integer textBorderDashOffset) {
        setTextBorderDashOffset(textBorderDashOffset);
        return (T) this;
    }

    Integer getTextShadowBlur();

    void setTextShadowBlur(Integer shadowBlur);

    @SuppressWarnings("unchecked")
    default T withTextShadowBlur(Integer shadowBlur) {
        setTextShadowBlur(shadowBlur);
        return (T) this;
    }

    Color getTextShadowColor();

    void setTextShadowColor(Color shadowColor);

    @SuppressWarnings("unchecked")
    default T withTextShadowColor(Color shadowColor) {
        setTextShadowColor(shadowColor);
        return (T) this;
    }

    Integer getTextShadowOffsetX();

    void setTextShadowOffsetX(Integer shadowOffsetX);

    @SuppressWarnings("unchecked")
    default T withTextShadowOffsetX(Integer shadowOffsetX) {
        setTextShadowOffsetX(shadowOffsetX);
        return (T) this;
    }

    Integer getTextShadowOffsetY();

    void setTextShadowOffsetY(Integer shadowOffsetY);

    @SuppressWarnings("unchecked")
    default T withTextShadowOffsetY(Integer shadowOffsetY) {
        setTextShadowOffsetY(shadowOffsetY);
        return (T) this;
    }
}

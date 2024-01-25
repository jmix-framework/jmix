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

import io.jmix.chartsflowui.kit.component.JmixChart;
import io.jmix.chartsflowui.kit.component.model.shared.Color;

/**
 * A component that has text.
 *
 * @param <T> origin class type
 */
public interface HasText<T> {

    /**
     * @return stroke color of the text
     */
    Color getTextBorderColor();

    /**
     * Sets a stroke color of the text or replaces an existing one.
     *
     * @param textBorderColor color to set
     */
    void setTextBorderColor(Color textBorderColor);

    /**
     * @param textBorderColor color to set
     * @return this
     * @see HasText#setTextBorderColor(Color)
     */
    @SuppressWarnings("unchecked")
    default T withTextBorderColor(Color textBorderColor) {
        setTextBorderColor(textBorderColor);
        return (T) this;
    }

    /**
     * @return stroke line width of the text in pixels
     */
    Double getTextBorderWidth();

    /**
     * Sets a stroke line width of the text or replaces an existing one.
     *
     * @param textBorderWidth line width to set in pixels
     */
    void setTextBorderWidth(Double textBorderWidth);

    /**
     * @param textBorderWidth line width to set in pixels
     * @return this
     * @see HasText#setTextBorderWidth(Double)
     */
    @SuppressWarnings("unchecked")
    default T withTextBorderWidth(Double textBorderWidth) {
        setTextBorderWidth(textBorderWidth);
        return (T) this;
    }

    /**
     * @return stroke line type of the text
     */
    String getTextBorderType();

    /**
     * Sets a stroke line type of the text or replaces an existing one.<br/>
     * Possible values are:
     * <ul>
     *     <li> {@code solid} </li>
     *     <li> {@code dashed} </li>
     *     <li> {@code dotted} </li>
     * </ul>
     * Since {@code v5.0.0}, it can also be a number. For more flexible customization
     * use {@link JmixChart#setNativeJson(String)}.
     *
     * @param textBorderType stroke line type to set
     */
    void setTextBorderType(String textBorderType);

    /**
     * @param textBorderType stroke line type to set
     * @return this
     * @see HasText#setTextBorderType(String)
     */
    @SuppressWarnings("unchecked")
    default T withTextBorderType(String textBorderType) {
        setTextBorderType(textBorderType);
        return (T) this;
    }

    /**
     * @return offset of the line dash
     */
    Integer getTextBorderDashOffset();

    /**
     * Sets offset of the line dash or replaces an existing one.
     *
     * @param textBorderDashOffset offset of the line dash to set
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D/lineDashOffset">CanvasRenderingContext2D.lineDashOffset [MDN]</a>
     */
    void setTextBorderDashOffset(Integer textBorderDashOffset);

    /**
     * @param textBorderDashOffset offset of the line dash to set
     * @return this
     * @see HasText#setTextBorderDashOffset(Integer)
     */
    @SuppressWarnings("unchecked")
    default T withTextBorderDashOffset(Integer textBorderDashOffset) {
        setTextBorderDashOffset(textBorderDashOffset);
        return (T) this;
    }

    /**
     * @return shadow blur of the text itself
     */
    Integer getTextShadowBlur();

    /**
     * Sets a depth of text shadow blur or replaces an existing one.
     *
     * @param shadowBlur depth of text shadow blur
     */
    void setTextShadowBlur(Integer shadowBlur);

    /**
     * @param shadowBlur depth of text shadow blur
     * @return this
     * @see HasText#setTextShadowBlur(Integer)
     */
    @SuppressWarnings("unchecked")
    default T withTextShadowBlur(Integer shadowBlur) {
        setTextShadowBlur(shadowBlur);
        return (T) this;
    }

    /**
     * @return text shadow color
     */
    Color getTextShadowColor();

    /**
     * Sets a color of the text shadow or replaces an existing one.
     *
     * @param shadowColor text shadow color to set
     */
    void setTextShadowColor(Color shadowColor);

    /**
     * @param shadowColor text shadow color to set
     * @return this
     * @see HasText#setTextShadowColor(Color)
     */
    @SuppressWarnings("unchecked")
    default T withTextShadowColor(Color shadowColor) {
        setTextShadowColor(shadowColor);
        return (T) this;
    }

    /**
     * @return horizontal offset for text shadow in pixels
     */
    Integer getTextShadowOffsetX();

    /**
     * Sets horizontal offset for text shadow or replaces an existing one.
     *
     * @param shadowOffsetX offset to set in pixels
     */
    void setTextShadowOffsetX(Integer shadowOffsetX);

    /**
     * @param shadowOffsetX offset to set in pixels
     * @return this
     * @see HasText#setTextShadowOffsetX(Integer)
     */
    @SuppressWarnings("unchecked")
    default T withTextShadowOffsetX(Integer shadowOffsetX) {
        setTextShadowOffsetX(shadowOffsetX);
        return (T) this;
    }

    /**
     * @return vertical offset for text shadow in pixels
     */
    Integer getTextShadowOffsetY();

    /**
     * Sets vertical offset for text shadow or replaces an existing one.
     *
     * @param shadowOffsetY offset to set in pixels
     */
    void setTextShadowOffsetY(Integer shadowOffsetY);

    /**
     * @param shadowOffsetY offset to set in pixels
     * @return this
     * @see HasText#setTextShadowOffsetY(Integer)
     */
    @SuppressWarnings("unchecked")
    default T withTextShadowOffsetY(Integer shadowOffsetY) {
        setTextShadowOffsetY(shadowOffsetY);
        return (T) this;
    }
}

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

package io.jmix.chartsflowui.kit.component.model.legend;

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;
import io.jmix.chartsflowui.kit.component.model.shared.Color;
import io.jmix.chartsflowui.kit.component.model.shared.JsFunction;

/**
 * Scrollable legend. It helps when too many legend items needed to be shown.
 * More detailed information is provided in the documentation.
 *
 * @see Legend
 * @see <a href="https://echarts.apache.org/en/option.html#legend">Legend documentation</a>
 */
public class ScrollableLegend extends AbstractLegend<ScrollableLegend> {

    protected Integer scrollDataIndex;

    protected Integer pageButtonItemGap;

    protected Integer pageButtonGap;

    protected Position pageButtonPosition;

    protected String pageFormatter;

    protected JsFunction pageFormatterFunction;

    protected PageIcons pageIcons;

    protected Color pageIconColor;

    protected Color pageIconInactiveColor;

    protected Integer pageIconSize;

    protected io.jmix.chartsflowui.kit.component.model.shared.TextStyle pageTextStyle;

    protected Boolean animation;

    protected Integer animationDurationUpdate;

    public ScrollableLegend() {
        super(LegendType.SCROLL);
    }

    /**
     * The icons of page button.
     */
    public static class PageIcons extends ChartObservableObject {

        protected String[] horizontal;

        protected String[] vertical;

        public String[] getHorizontal() {
            return horizontal;
        }

        public void setHorizontal(String prevPageIcon, String nextPageIcon) {
            this.horizontal = new String[]{prevPageIcon, nextPageIcon};
            markAsDirty();
        }

        public String[] getVertical() {
            return vertical;
        }

        public void setVertical(String prevPageIcon, String nextPageIcon) {
            this.vertical = new String[]{prevPageIcon, nextPageIcon};
            markAsDirty();
        }

        public PageIcons withHorizontal(String prevPageIcon, String nextPageIcon) {
            setHorizontal(prevPageIcon, nextPageIcon);
            return this;
        }

        public PageIcons withVertical(String prevPageIcon, String nextPageIcon) {
            setVertical(prevPageIcon, nextPageIcon);
            return this;
        }
    }

    public Integer getScrollDataIndex() {
        return scrollDataIndex;
    }

    public void setScrollDataIndex(Integer scrollDataIndex) {
        this.scrollDataIndex = scrollDataIndex;
        markAsDirty();
    }

    public Integer getPageButtonItemGap() {
        return pageButtonItemGap;
    }

    public void setPageButtonItemGap(Integer pageButtonItemGap) {
        this.pageButtonItemGap = pageButtonItemGap;
        markAsDirty();
    }

    public Integer getPageButtonGap() {
        return pageButtonGap;
    }

    public void setPageButtonGap(Integer pageButtonGap) {
        this.pageButtonGap = pageButtonGap;
        markAsDirty();
    }

    public Position getPageButtonPosition() {
        return pageButtonPosition;
    }

    public void setPageButtonPosition(Position pageButtonPosition) {
        this.pageButtonPosition = pageButtonPosition;
        markAsDirty();
    }

    public String getPageFormatter() {
        return pageFormatter;
    }

    public void setPageFormatter(String pageFormatter) {
        this.pageFormatter = pageFormatter;
        markAsDirty();
    }

    public JsFunction getPageFormatterFunction() {
        return pageFormatterFunction;
    }

    public void setPageFormatterFunction(JsFunction pageFormatterFunction) {
        this.pageFormatterFunction = pageFormatterFunction;
        markAsDirty();
    }

    public void setPageFormatterFunction(String pageFormatterFunction) {
        this.pageFormatterFunction = new JsFunction(pageFormatterFunction);
        markAsDirty();
    }

    public PageIcons getPageIcons() {
        return pageIcons;
    }

    public void setPageIcons(PageIcons pageIcons) {
        if (this.pageIcons != null) {
            removeChild(this.pageIcons);
        }

        this.pageIcons = pageIcons;
        addChild(pageIcons);
    }

    public Color getPageIconColor() {
        return pageIconColor;
    }

    public void setPageIconColor(Color pageIconColor) {
        this.pageIconColor = pageIconColor;
        markAsDirty();
    }

    public Color getPageIconInactiveColor() {
        return pageIconInactiveColor;
    }

    public void setPageIconInactiveColor(Color pageIconInactiveColor) {
        this.pageIconInactiveColor = pageIconInactiveColor;
        markAsDirty();
    }

    public Integer getPageIconSize() {
        return pageIconSize;
    }

    public void setPageIconSize(Integer pageIconSize) {
        this.pageIconSize = pageIconSize;
        markAsDirty();
    }

    public io.jmix.chartsflowui.kit.component.model.shared.TextStyle getPageTextStyle() {
        return pageTextStyle;
    }

    public void setPageTextStyle(io.jmix.chartsflowui.kit.component.model.shared.TextStyle pageTextStyle) {
        if (this.pageTextStyle != null) {
            removeChild(this.pageTextStyle);
        }

        this.pageTextStyle = pageTextStyle;
        addChild(pageTextStyle);
    }

    public Boolean getAnimation() {
        return animation;
    }

    public void setAnimation(Boolean animation) {
        this.animation = animation;
        markAsDirty();
    }

    public Integer getAnimationDurationUpdate() {
        return animationDurationUpdate;
    }

    public void setAnimationDurationUpdate(Integer animationDurationUpdate) {
        this.animationDurationUpdate = animationDurationUpdate;
        markAsDirty();
    }

    public ScrollableLegend withScrollDataIndex(Integer scrollDataIndex) {
        setScrollDataIndex(scrollDataIndex);
        return this;
    }

    public ScrollableLegend withPageButtonItemGap(Integer pageButtonItemGap) {
        setPageButtonItemGap(pageButtonItemGap);
        return this;
    }

    public ScrollableLegend withPageButtonGap(Integer pageButtonGap) {
        setPageButtonGap(pageButtonGap);
        return this;
    }

    public ScrollableLegend withPageButtonPosition(Position pageButtonPosition) {
        setPageButtonPosition(pageButtonPosition);
        return this;
    }

    public ScrollableLegend withPageFormatter(String pageFormatter) {
        setPageFormatter(pageFormatter);
        return this;
    }

    public ScrollableLegend withPageFormatterFunction(JsFunction formatterFunction) {
        setPageFormatterFunction(formatterFunction);
        return this;
    }

    public ScrollableLegend withPageFormatterFunction(String formatterFunctionCode) {
        setPageFormatterFunction(formatterFunctionCode);
        return this;
    }

    public ScrollableLegend withPageIcons(PageIcons pageIcons) {
        setPageIcons(pageIcons);
        return this;
    }

    public ScrollableLegend withPageIconColor(Color pageIconColor) {
        setPageIconColor(pageIconColor);
        return this;
    }

    public ScrollableLegend withPageIconInactiveColor(Color pageIconInactiveColor) {
        setPageIconInactiveColor(pageIconInactiveColor);
        return this;
    }

    public ScrollableLegend withPageIconSize(Integer pageIconSize) {
        setPageIconSize(pageIconSize);
        return this;
    }

    public ScrollableLegend withPageTextStyle(io.jmix.chartsflowui.kit.component.model.shared.TextStyle pageTextStyle) {
        setPageTextStyle(pageTextStyle);
        return this;
    }

    public ScrollableLegend withAnimation(Boolean animation) {
        setAnimation(animation);
        return this;
    }

    public ScrollableLegend withAnimationDurationUpdate(Integer animationDurationUpdate) {
        setAnimationDurationUpdate(animationDurationUpdate);
        return this;
    }
}

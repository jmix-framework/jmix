/*
 * Copyright 2021 Haulmont.
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

package io.jmix.pivottable.model;

import io.jmix.ui.meta.StudioElement;

@StudioElement(
        caption = "RendererOptions",
        xmlElement = "rendererOptions",
        icon = "io/jmix/pivottable/icon/component.svg",
        xmlns = "http://jmix.io/schema/ui/pivot-table",
        xmlnsAlias = "pivot"
)
public class RendererOptions extends AbstractPivotObject {

    private static final long serialVersionUID = -1633377582757582532L;

    private HeatmapRendererOptions heatmap;

    private C3RendererOptions c3;

    /**
     * @return options which will be applied to heatmap renderers
     */
    public HeatmapRendererOptions getHeatmap() {
        return heatmap;
    }

    /**
     * Sets options which will be applied to heatmap renderers:
     * <ul>
     * <li>{@link Renderer#HEATMAP}</li>
     * <li>{@link Renderer#COL_HEATMAP}</li>
     * <li>{@link Renderer#ROW_HEATMAP}</li>
     * </ul>
     *
     * @param heatmap options which will be applied to heatmap renderers
     * @return a reference to this object
     */
    @StudioElement
    public RendererOptions setHeatmap(HeatmapRendererOptions heatmap) {
        this.heatmap = heatmap;
        return this;
    }

    /**
     * @return options which will be applied to chart renderers
     */
    public C3RendererOptions getC3() {
        return c3;
    }

    /**
     * Sets options which will be applied to chart renderers:
     * <ul>
     * <li>{@link Renderer#AREA_CHART}</li>
     * <li>{@link Renderer#BAR_CHART}</li>
     * <li>{@link Renderer#LINE_CHART}</li>
     * <li>{@link Renderer#STACKED_BAR_CHART}</li>
     * <li>{@link Renderer#SCATTER_CHART}</li>
     * </ul>
     *
     * @param c3 options which will be applied to chart renderers
     * @return a reference to this object
     */
    @StudioElement
    public RendererOptions setC3(C3RendererOptions c3) {
        this.c3 = c3;
        return this;
    }
}

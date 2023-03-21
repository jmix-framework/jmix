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

import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

@StudioElement(
        caption = "Heatmap",
        xmlElement = "heatmap",
        icon = "io/jmix/pivottable/icon/component.svg",
        xmlns = "http://jmix.io/schema/ui/pivot-table",
        xmlnsAlias = "pivot"
)
public class HeatmapRendererOptions extends AbstractPivotObject {

    private static final long serialVersionUID = 4082501420650191687L;

    private JsFunction colorScaleGeneratorFunction;

    /**
     * @return a function that is used in color scale generator of heatmap renderer
     */
    public JsFunction getColorScaleGeneratorFunction() {
        return colorScaleGeneratorFunction;
    }

    /**
     * Sets a function that is used in color scale generator of heatmap renderer.
     *
     * @param colorScaleGeneratorFunction a function that is used in color scale generator of heatmap renderer
     * @return a refrence to this object
     */
    @StudioProperty(type = PropertyType.JS_FUNCTION)
    public HeatmapRendererOptions setColorScaleGeneratorFunction(JsFunction colorScaleGeneratorFunction) {
        this.colorScaleGeneratorFunction = colorScaleGeneratorFunction;
        return this;
    }
}

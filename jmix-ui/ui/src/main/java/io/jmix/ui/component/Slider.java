/*
 * Copyright 2019 Haulmont.
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

import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

@StudioComponent(
        caption = "Slider",
        category = "Components",
        xmlElement = "slider",
        canvasBehaviour = CanvasBehaviour.SLIDER,
        icon = "io/jmix/ui/icon/component/slider.svg",
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/slider.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF),
                @StudioProperty(name = "property", type = PropertyType.PROPERTY_PATH_REF,
                        options = {"decimal", "double", "int", "long", "float"}, typeParameter = "V"),
                @StudioProperty(name = "orientation", type = PropertyType.ENUMERATION,
                        defaultValue = "horizontal", options = {"vertical", "horizontal"}),
                @StudioProperty(name = "datatype", type = PropertyType.DATATYPE_ID,
                        options = {"decimal", "double", "int", "long"}, typeParameter = "V")
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "property"})
        }
)
public interface Slider<V extends Number> extends Field<V>, HasDatatype<V>, HasOrientation {

    String NAME = "slider";

    /**
     * Sets the minimum value of the slider.
     *
     * @param min the minimum value of the slider
     */
    @StudioProperty(type = PropertyType.DOUBLE, defaultValue = "0")
    void setMin(V min);

    /**
     * @return the minimum value of the slider
     */
    V getMin();

    /**
     * Sets the maximum value of the slider.
     *
     * @param max the maximum value of the slider
     */
    @StudioProperty(type = PropertyType.DOUBLE, defaultValue = "100")
    void setMax(V max);

    /**
     * @return the maximum value of the slider
     */
    V getMax();

    /**
     * Sets the number of digits after the decimal point.
     *
     * @param resolution the number of digits after the decimal point
     */
    @StudioProperty(defaultValue = "0")
    void setResolution(int resolution);

    /**
     * @return resolution the number of digits after the decimal point
     */
    int getResolution();

    /**
     * Sets the slider to update its value when the user clicks on it.
     * <p>
     * By default this behavior is disabled.
     *
     * @param updateValueOnClick {@code true} to update the value of the slider on click
     */
    @StudioProperty(defaultValue = "false")
    void setUpdateValueOnClick(boolean updateValueOnClick);

    /**
     * @return {@code true} if the slider updates its value on click
     */
    boolean isUpdateValueOnCLick();
}

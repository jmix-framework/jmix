/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.kit.component.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Constructor for an object which will aggregate results per cell
 * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Aggregators">documentation</a>).
 */
public class Aggregation extends PivotTableOptionsObservable {

    protected AggregationMode mode;
    protected String caption;
    protected Boolean custom;
    protected JsFunction function;
    protected List<String> properties;

    /**
     * @return one of predefined aggregation modes
     */
    public AggregationMode getMode() {
        return mode;
    }

    /**
     * Sets one of predefined aggregation modes.
     * <p>
     * Applies only when {@code custom=false}.
     *
     * @param mode one of predefined aggregation modes
     * @see #setCustom(Boolean)
     */
    public void setMode(AggregationMode mode) {
        this.mode = mode;
        markAsChanged();
    }

    /**
     * @return a caption of aggregation
     */
    public String getCaption() {
        return caption;
    }

    /**
     * When {@link Aggregation} is set as {@link PivotTableOptions#aggregation},
     * then {@code caption} will be converted to {@code aggregatorName} - the name of
     * the aggregator, used for display purposes in some renderers.
     * <p>
     * When {@link Aggregation} is added as one of {@link Aggregations#aggregations},
     * then {@code caption} will be converted to a key in dictionary of generators
     * for aggregation functions in dropdown menu.
     *
     * @param caption a caption of aggregation
     */
    public void setCaption(String caption) {
        this.caption = caption;
        markAsChanged();
    }

    /**
     * @return {@code true} if a function defined in {@link #function} field
     * must be used as the aggregation, {@code false} otherwise
     */
    public Boolean getCustom() {
        return custom;
    }

    /**
     * Sets whatever a function defined in {@link #function} field must be used as the aggregation.
     *
     * @param custom {@code true} if a function defined in {@link #function} field
     *               must be used as the aggregation, {@code false} otherwise
     * @see #setFunction(JsFunction)
     */
    public void setCustom(Boolean custom) {
        this.custom = custom;
        markAsChanged();
    }

    /**
     * @return a function which will aggregate results per cell
     */
    public JsFunction getFunction() {
        return function;
    }

    /**
     * Sets a function which will aggregate results per cell
     * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Aggregators">documentation</a>).
     * <p>
     * Applies only when {@code custom=true}.
     *
     * @param function a function which will aggregate results per cell
     * @see #setCustom(Boolean)
     */
    public void setFunction(JsFunction function) {
        this.function = function;
        markAsChanged();
    }

    /**
     * @return a collection of property names to pass as parameters to selected aggregation
     */
    public List<String> getProperties() {
        return properties;
    }

    /**
     * Sets a collection of property names to pass as parameters to selected aggregation.
     * <p>
     * Applies only when {@code custom=false}.
     *
     * @param properties a collection of property names to pass as parameters to selected aggregation
     * @see #setCustom(Boolean)
     */
    public void setProperties(List<String> properties) {
        this.properties = properties;
        markAsChanged();
    }

    public void addProperties(String... properties) {
        if (properties != null) {
            if (this.properties == null) {
                this.properties = new ArrayList<>();
            }
            this.properties.addAll(Arrays.asList(properties));
            markAsChanged();
        }
    }
}


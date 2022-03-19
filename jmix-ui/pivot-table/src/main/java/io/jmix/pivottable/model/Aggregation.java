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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Constructor for an object which will aggregate results per cell
 * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Aggregators">documentation</a>).
 */
public class Aggregation extends AbstractPivotObject {
    private static final long serialVersionUID = 8131812058171838527L;

    private String id;

    private AggregationMode mode;

    private String caption;

    private Boolean custom;

    private JsFunction function;

    private List<String> properties;

    public Aggregation() {
        id = UUID.randomUUID().toString();
    }

    /**
     * @return Id for the unique identification of this Aggregation
     */
    public String getId() {
        return id;
    }

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
     * @return a reference to this object
     * @see #setCustom(Boolean)
     */
    public Aggregation setMode(AggregationMode mode) {
        this.mode = mode;
        return this;
    }

    /**
     * @return a caption of aggregation
     */
    public String getCaption() {
        return caption;
    }

    /**
     * When {@link Aggregation} is set as {@link PivotTableModel#aggregation},
     * then {@code caption} will be converted to {@code aggregatorName} - the name of
     * the aggregator, used for display purposes in some renderers.
     * <p>
     * When {@link Aggregation} is added as one of {@link Aggregations#aggregations},
     * then {@code caption} will be converted to a key in dictionary of generators
     * for aggregation functions in dropdown menu.
     *
     * @param caption a caption of aggregation
     * @return a reference to this object
     */
    public Aggregation setCaption(String caption) {
        this.caption = caption;
        return this;
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
     * @return a reference to this object
     * @see #setFunction(JsFunction)
     */
    public Aggregation setCustom(Boolean custom) {
        this.custom = custom;
        return this;
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
     * @return a reference to this object
     * @see #setCustom(Boolean)
     */
    public Aggregation setFunction(JsFunction function) {
        this.function = function;
        return this;
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
     * @return a reference to this object
     * @see #setCustom(Boolean)
     */
    public Aggregation setProperties(List<String> properties) {
        this.properties = properties;
        return this;
    }

    public Aggregation addProperties(String... properties) {
        if (properties != null) {
            if (this.properties == null) {
                this.properties = new ArrayList<>();
            }
            this.properties.addAll(Arrays.asList(properties));
        }
        return this;
    }
}

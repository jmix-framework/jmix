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
import io.jmix.ui.meta.StudioProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration of aggregations. Use when {@link PivotTableModel#editable} is set to {@code true}.
 */
@StudioElement(
        caption = "Aggregations",
        xmlElement = "aggregations",
        icon = "io/jmix/pivottable/icon/component.svg",
        xmlns = "http://jmix.io/schema/ui/pivot-table",
        xmlnsAlias = "pivot"
)
public class Aggregations extends AbstractPivotObject {
    private static final long serialVersionUID = 5569146922427717821L;

    private List<Aggregation> aggregations;

    private AggregationMode selectedAggregation;

    /**
     * @return a list which will be converted to a dictionary
     * of generators for aggregation functions in dropdown menu
     */
    public List<Aggregation> getAggregations() {
        return aggregations;
    }

    /**
     * Sets a list which will be converted to a dictionary of
     * generators for aggregation functions in dropdown menu.
     *
     * @param aggregations a list which will be converted to a dictionary
     *                     of generators for aggregation functions in dropdown menu
     * @return a reference to this object
     */
    public Aggregations setAggregations(List<Aggregation> aggregations) {
        this.aggregations = aggregations;
        return this;
    }

    /**
     * Adds an array which will be converted to a dictionary of
     * generators for aggregation functions in dropdown menu.
     *
     * @param aggregations an array which will be converted to a dictionary
     *                     of generators for aggregation functions in dropdown menu
     * @return a reference to this object
     */
    public Aggregations addAggregations(Aggregation... aggregations) {
        if (aggregations != null) {
            if (this.aggregations == null) {
                this.aggregations = new ArrayList<>();
            }
            this.aggregations.addAll(Arrays.asList(aggregations));
        }
        return this;
    }

    /**
     * @return a selected aggregation
     */
    public AggregationMode getSelectedAggregation() {
        return selectedAggregation;
    }

    /**
     * Sets one of predefined aggregations, which name will be
     * converted to {@code aggregatorName} - an aggregator to
     * prepopulate in dropdown i.e. key to {@code aggregators} object.
     *
     * @param selectedAggregation an aggregation to set as a selected
     * @return a reference to this object
     */
    @StudioProperty(name = "selected")
    public Aggregations setSelectedAggregation(AggregationMode selectedAggregation) {
        this.selectedAggregation = selectedAggregation;
        return this;
    }

    /**
     * Meta method for support in Screen Designer only.
     */
    @StudioProperty(name = "default")
    private void setDefault(AggregationMode defaultAggregation) {

    }

    /**
     * Meta method for support in Screen Designer only.
     */
    @StudioElement
    private void setAggregation(Aggregation aggregation) {

    }
}

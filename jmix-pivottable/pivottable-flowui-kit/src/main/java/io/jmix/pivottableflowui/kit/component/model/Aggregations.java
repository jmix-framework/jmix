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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Aggregations extends PivotTableOptionsObservable {

    protected List<Aggregation> aggregations;

    @JsonProperty("selected")
    protected AggregationMode selectedAggregation;

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
     */
    public void setAggregations(List<Aggregation> aggregations) {
        this.aggregations = aggregations;
        markAsChanged();
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
            markAsChanged();
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
     */
    public void setSelectedAggregation(AggregationMode selectedAggregation) {
        this.selectedAggregation = selectedAggregation;
        markAsChanged();
    }
}


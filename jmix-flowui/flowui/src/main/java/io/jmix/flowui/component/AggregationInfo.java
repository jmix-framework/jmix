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

package io.jmix.flowui.component;

import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.aggregation.AggregationStrategy;
import io.jmix.flowui.kit.component.SupportsFormatter;
import io.jmix.flowui.kit.component.formatter.Formatter;
import io.jmix.flowui.kit.meta.StudioIgnore;
import org.springframework.lang.Nullable;

/**
 * POJO to store all information needed to create aggregation in {@link DataGrid}.
 */
public class AggregationInfo implements SupportsFormatter<Object> {

    /**
     * Represents the type of aggregation to be performed.
     */
    public enum Type {
        /**
         * Represents a "SUM" type of aggregation, which calculates the total
         * sum of the values within a collection.
         */
        SUM,

        /**
         * Represents an "AVG" type of aggregation, which calculates the average
         * of the values within a collection.
         */
        AVG,

        /**
         * Represents a "COUNT" type of aggregation, which calculates the total
         * number of items within a collection.
         */
        COUNT,

        /**
         * Represents a "MIN" type of aggregation, which calculates the minimum
         * value within a collection.
         */
        MIN,

        /**
         * Represents a "MAX" type of aggregation, which calculates the maximum
         * value within a collection.
         */
        MAX,

        /**
         * Enables us to use custom {@link AggregationStrategy} implementation.
         */
        CUSTOM
    }

    protected MetaPropertyPath propertyPath;
    protected Type type;

    protected Formatter<Object> formatter;
    protected AggregationStrategy<?, ?> strategy;

    protected String cellTitle;

    /**
     * Returns the {@link MetaPropertyPath} used for aggregation.
     *
     * @return the {@code MetaPropertyPath}, or {@code null} if not set
     */
    @Nullable
    public MetaPropertyPath getPropertyPath() {
        return propertyPath;
    }

    /**
     * Sets the {@link MetaPropertyPath} used for aggregation.
     *
     * @param propertyPath the {@code MetaPropertyPath} representing the path to the property,
     *                     or {@code null} if no property path is set
     */
    public void setPropertyPath(@Nullable MetaPropertyPath propertyPath) {
        this.propertyPath = propertyPath;
    }


    /**
     * Returns the type of aggregation.
     *
     * @return the type of aggregation, or {@code null} if it is not set.
     */
    @Nullable
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of aggregation.
     *
     * @param type the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

    @Nullable
    @Override
    public Formatter<Object> getFormatter() {
        return formatter;
    }

    @StudioIgnore
    @Override
    public void setFormatter(@Nullable Formatter<? super Object> formatter) {
        this.formatter = formatter;
    }

    /**
     * Returns the current aggregation strategy in use.
     *
     * @return the aggregation strategy if present, or {@code null} if no strategy is set
     */
    @Nullable
    public AggregationStrategy<?, ?> getStrategy() {
        return strategy;
    }

    /**
     * Sets the aggregation strategy to be used. If a non-null strategy is provided,
     * the type is set to CUSTOM. This method allows customization of aggregation logic.
     *
     * @param strategy the aggregation strategy to set; can be {@code null} to indicate no strategy
     */
    public void setStrategy(@Nullable AggregationStrategy<?, ?> strategy) {
        if (strategy != null) {
            setType(Type.CUSTOM);
        }
        this.strategy = strategy;
    }

    /**
     * Returns the title of the cell, if available.
     *
     * @return the cell title, or {@code null} if no title is set
     */
    @Nullable
    public String getCellTitle() {
        return cellTitle;
    }

    /**
     * Sets the title of the cell.
     *
     * @param cellTitle the title to set for the cell
     */
    public void setCellTitle(String cellTitle) {
        this.cellTitle = cellTitle;
    }
}

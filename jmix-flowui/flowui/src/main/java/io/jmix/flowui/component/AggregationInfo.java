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
import io.jmix.flowui.data.aggregation.AggregationStrategy;
import io.jmix.flowui.kit.component.SupportsFormatter;
import io.jmix.flowui.kit.component.formatter.Formatter;
import io.jmix.flowui.kit.meta.StudioIgnore;
import org.springframework.lang.Nullable;

public class AggregationInfo implements SupportsFormatter<Object> {

    public enum Type {
        SUM,
        AVG,
        COUNT,
        MIN,
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

    @Nullable
    public MetaPropertyPath getPropertyPath() {
        return propertyPath;
    }

    public void setPropertyPath(@Nullable MetaPropertyPath propertyPath) {
        this.propertyPath = propertyPath;
    }

    @Nullable
    public Type getType() {
        return type;
    }

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

    @Nullable
    public AggregationStrategy<?, ?> getStrategy() {
        return strategy;
    }

    public void setStrategy(@Nullable AggregationStrategy<?, ?> strategy) {
        if (strategy != null) {
            setType(Type.CUSTOM);
        }
        this.strategy = strategy;
    }

    @Nullable
    public String getCellTitle() {
        return cellTitle;
    }

    public void setCellTitle(String cellTitle) {
        this.cellTitle = cellTitle;
    }
}

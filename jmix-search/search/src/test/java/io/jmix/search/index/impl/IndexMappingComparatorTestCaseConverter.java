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

package io.jmix.search.index.impl;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

public class IndexMappingComparatorTestCaseConverter implements ArgumentsAggregator {
    /*@Override
    public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
        String[] parts = ((String) source).split(";");
        return IndexMappingComparatorTestCase.testCase(parts[0], parts[1], MappingComparingResult.valueOf(parts[2]));
    }*/

    @Override
    public @Nullable Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context) throws ArgumentsAggregationException {
        return IndexMappingComparatorTestCase.testCase(
                accessor.getString(0),
                accessor.getString(1),
                MappingComparingResult.valueOf(accessor.getString(2))
        );
    }
}

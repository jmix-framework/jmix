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

package io.jmix.graphql.schema;

import com.google.common.collect.ImmutableList;
import graphql.schema.GraphQLScalarType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

import static graphql.Scalars.*;
import static io.jmix.graphql.schema.Types.FilterOperation.*;
import static io.jmix.graphql.schema.scalar.CustomScalars.GraphQLBigDecimal;
import static io.jmix.graphql.schema.scalar.CustomScalars.GraphQLLong;
import static io.jmix.graphql.schema.scalar.CustomScalars.*;

@Component("gql_FilterManager")
public class FilterManagerImpl implements FilterManager {

    private static final List<GraphQLScalarType> numberTypes = ImmutableList.of(
            GraphQLBigDecimal,
            GraphQLLong,
            GraphQLByte,
            GraphQLShort,
            GraphQLFloat,
            GraphQLBigInteger,
            GraphQLInt
    );

    private static final List<GraphQLScalarType> dateTimeTypes = ImmutableList.of(
            GraphQLLocalDateTime,
            GraphQLDate
    );

    private static final List<GraphQLScalarType> stringTypes = ImmutableList.of(
            GraphQLString,
            GraphQLChar
    );

    @Override
    public EnumSet<Types.FilterOperation> availableOperations(GraphQLScalarType scalarType) {

        if (scalarType.equals(GraphQLUUID)) {
            return EnumSet.of(EQ, NEQ, IN_LIST, NOT_IN_LIST, IS_NULL);
        }
        if (numberTypes.contains(scalarType)) {
            return EnumSet.of(EQ, NEQ, GT, GTE, LT, LTE, IN_LIST, NOT_IN_LIST, IS_NULL);
        }
        if (stringTypes.contains(scalarType)) {
            return EnumSet.of(EQ, NEQ, IN_LIST, NOT_IN_LIST, STARTS_WITH, ENDS_WITH, CONTAINS, IS_NULL);
        }
        if (dateTimeTypes.contains(scalarType)) {
            return EnumSet.of(EQ, NEQ, IN_LIST, NOT_IN_LIST, GT, GTE, LT, LTE, IS_NULL);
        }
        if (scalarType.equals(GraphQLBoolean)) {
            return EnumSet.of(EQ, NEQ, IS_NULL);
        }
        if (scalarType.equals(GraphQLVoid)) {
            return EnumSet.noneOf(Types.FilterOperation.class);
        }

        throw new UnsupportedOperationException("Can't define the operation type for " + scalarType);
    }
}

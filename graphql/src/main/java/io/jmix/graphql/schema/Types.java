package io.jmix.graphql.schema;

import com.google.common.collect.ImmutableList;
import graphql.schema.GraphQLScalarType;
import io.jmix.core.querycondition.PropertyCondition;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static graphql.Scalars.*;
import static io.jmix.graphql.schema.scalar.CustomScalars.GraphQLBigDecimal;
import static io.jmix.graphql.schema.scalar.CustomScalars.GraphQLLong;
import static io.jmix.graphql.schema.scalar.CustomScalars.*;


// todo rename to FilterTypes ?
public class Types {

    public enum SortOrder {
        ASC,
        DESC
    }

    public enum FilterOperation {
        EQ("_eq", PropertyCondition.Operation.EQUAL, "equals"),
        NEQ("_neq", PropertyCondition.Operation.NOT_EQUAL, "not equals"),
        GT("_gt", PropertyCondition.Operation.GREATER, "greater than"),
        GTE("_gte", PropertyCondition.Operation.GREATER_OR_EQUAL, "greater than or equals"),
        LT("_lt", PropertyCondition.Operation.LESS, "less that"),
        LTE("_lte", PropertyCondition.Operation.LESS_OR_EQUAL, "less than or equals"),
        CONTAINS("_contains", PropertyCondition.Operation.CONTAINS, "contains substring"),
        NOT_CONTAINS("_notContains", PropertyCondition.Operation.NOT_CONTAINS, "not contains substring"),
        STARTS_WITH("_startsWith", PropertyCondition.Operation.STARTS_WITH, "starts with substring"),
        ENDS_WITH("_endsWith", PropertyCondition.Operation.ENDS_WITH, "ends with substring"),
        IN_LIST("_in", PropertyCondition.Operation.IN_LIST, "in list"),
        NOT_IN_LIST("_notIn", PropertyCondition.Operation.NOT_IN_LIST, "not in list"),
        IS_NULL("_isNull", PropertyCondition.Operation.IS_SET, "is null");

        private final String id;
        private final String jmixOperation;
        private final String description;

        FilterOperation(String id, String jmixOperation, @Nullable String description) {
            this.id = id;
            this.jmixOperation = jmixOperation;
            this.description = description;
        }

        public static FilterOperation fromId(String id) {
            if (id != null) {
                for (FilterOperation operation :
                        FilterOperation.values()) {
                    if (operation.getId().equals(id)) {
                        return operation;
                    }
                }
            }

            return null;
        }

        public String getId() {
            return id;
        }

        public String getJmixOperation() {
            return jmixOperation;
        }

        public String getDescription() {
            return description;
        }
    }

    public static final List<GraphQLScalarType> numberTypes = ImmutableList.of(
            GraphQLBigDecimal,
            GraphQLLong,
            GraphQLByte,
            GraphQLShort,
            GraphQLFloat,
            GraphQLBigInteger,
            GraphQLInt
    );

    public static final List<GraphQLScalarType> dateTimeTypes = ImmutableList.of(
            GraphQLLocalDateTime,
            GraphQLLocalDate,
            GraphQLOffsetDateTime,
            GraphQLDate,
            GraphQLDateTime
    );

    public enum ConditionUnionType {
        // todo need to investigate how to implement using jmix conditions
//        NOT,
        AND,
        OR;

        @Nullable
        public static ConditionUnionType find(String type) {
            return Arrays.stream(values())
                    .filter(conditionUnionType -> conditionUnionType.name().equals(type))
                    .findAny().orElse(null);
        }
    }
}

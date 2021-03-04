package io.jmix.graphql.schema;

import graphql.Scalars;
import graphql.language.*;
import graphql.schema.GraphQLScalarType;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.graphql.schema.scalar.CustomScalars;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;


// todo rename to FilterTypes ?
public class Types {

    public enum SortOrder {
        ASC,
        DESC
    }

    public enum FilterOperation {
        EQ("EQ", PropertyCondition.Operation.EQUAL),
        NEQ("NEQ", PropertyCondition.Operation.NOT_EQUAL),
        GT("GT", PropertyCondition.Operation.GREATER),
        GTE("GTE", PropertyCondition.Operation.GREATER_OR_EQUAL),
        LT("LT", PropertyCondition.Operation.LESS),
        LTE("LTE", PropertyCondition.Operation.LESS_OR_EQUAL),
        CONTAINS("_contains", PropertyCondition.Operation.CONTAINS),
        NOT_CONTAINS("_notContains", PropertyCondition.Operation.NOT_CONTAINS),
        STARTS_WITH("_startsWith", PropertyCondition.Operation.STARTS_WITH),
        ENDS_WITH("_endsWith", PropertyCondition.Operation.ENDS_WITH),
        IN_LIST("_in", PropertyCondition.Operation.IN_LIST),
        NOT_IN_LIST("_notIn", PropertyCondition.Operation.NOT_IN_LIST);

        private final String id;
        private final String jmixOperation;

        FilterOperation(String id, String jmixOperation) {
            this.id = id;
            this.jmixOperation = jmixOperation;
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
    }

    public static GraphQLScalarType[] scalars = {
            Scalars.GraphQLInt,
            Scalars.GraphQLBigInteger,
            Scalars.GraphQLBoolean,
            Scalars.GraphQLByte,
            Scalars.GraphQLChar,
            Scalars.GraphQLFloat,
            Scalars.GraphQLShort,
            Scalars.GraphQLString,
            CustomScalars.GraphQLVoid,
            CustomScalars.GraphQLLocalDateTime,
            CustomScalars.GraphQLDate,
            CustomScalars.GraphQLBigDecimal,
            CustomScalars.GraphQLLong,
            CustomScalars.GraphQLUUID,
    };

    public static EnumTypeDefinition enumSortOrder = BaseTypesBuilder.buildEnumTypeDef(SortOrder.class);

    /**
     * Shortcut for input value definition
     *
     * @param fieldName field name
     * @return field
     */
    public static InputValueDefinition valueDef(String fieldName, String type, @Nullable String description) {
        return InputValueDefinition.newInputValueDefinition()
                .name(fieldName).type(new TypeName(type))
                .description(StringUtils.isBlank(description) ? null : new Description(description, null, false))
                .build();
    }

    /**
     * Shortcut for input value definition that has list type
     *
     * @param fieldName field name
     * @return field
     */
    public static InputValueDefinition listValueDef(String fieldName, String type, @Nullable String description) {
        return InputValueDefinition.newInputValueDefinition()
                .name(fieldName).type(new ListType(new TypeName(type)))
                .description(StringUtils.isBlank(description) ? null : new Description(description, null, false))
                .build();
    }

}

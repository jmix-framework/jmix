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
     * @param type input value type
     * @param description input value description
     * @return field
     */
    public static InputValueDefinition valueDef(String fieldName, String type, @Nullable String description) {
        return InputValueDefinition.newInputValueDefinition()
                .name(fieldName).type(new TypeName(type))
                .description(StringUtils.isBlank(description) ? null : new Description(description, null, false))
                .build();
    }

    /**
     * Shortcut for input value definition
     *
     * @param operation field name and describtion
     * @return field
     */
    public static InputValueDefinition valueDef(FilterOperation operation, String type) {
        return valueDef(operation.getId(), type, operation.description);
    }

    /**
     * Shortcut for input value definition that has list type
     *
     * @param fieldName field name
     * @param type input value type
     * @param description input value description
     * @return field
     */
    public static InputValueDefinition listValueDef(String fieldName, String type, @Nullable String description) {
        return InputValueDefinition.newInputValueDefinition()
                .name(fieldName).type(new ListType(new TypeName(type)))
                .description(StringUtils.isBlank(description) ? null : new Description(description, null, false))
                .build();
    }

    /**
     * Shortcut for input value definition that has list type
     *
     * @param operation field name and describtion
     * @return field
     */
    public static InputValueDefinition listValueDef(FilterOperation operation, String type) {
        return listValueDef(operation.getId(), type, operation.getDescription());
    }

}

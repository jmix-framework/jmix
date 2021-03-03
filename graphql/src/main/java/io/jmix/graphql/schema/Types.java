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
        EQ(PropertyCondition.Operation.EQUAL),
        NEQ(PropertyCondition.Operation.NOT_EQUAL),
        GT(PropertyCondition.Operation.GREATER),
        GTE(PropertyCondition.Operation.GREATER_OR_EQUAL),
        LT(PropertyCondition.Operation.LESS),
        LTE(PropertyCondition.Operation.LESS_OR_EQUAL),
        _contains(PropertyCondition.Operation.CONTAINS),
        _notContains(PropertyCondition.Operation.NOT_CONTAINS),
        _startsWith(PropertyCondition.Operation.STARTS_WITH),
        _endsWith(PropertyCondition.Operation.ENDS_WITH);

        FilterOperation(String jmixOperation) {
            this.jmixOperation = jmixOperation;
        }

        private final String jmixOperation;

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

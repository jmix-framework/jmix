package io.jmix.graphql.schema;

import graphql.language.*;

// todo rename to FilterTypes ?
public class Types {

    public static final String CONDITION_AND = "AND";
    public static final String CONDITION_OR = "OR";

    // use GroupConditionType.getName() if need outside
    protected static final String GROUP_CONDITION_TYPE_NAME = "inp_GroupCondition";

    public static InputObjectTypeDefinition Condition = InputObjectTypeDefinition.newInputObjectDefinition()
            .name("inp_Condition")
            .inputValueDefinition(valueDef("property", "String"))
            .inputValueDefinition(valueDef("operator", "String"))
            .inputValueDefinition(valueDef("value", "String"))
            .build();

    public static EnumTypeDefinition GroupConditionType = EnumTypeDefinition.newEnumTypeDefinition()
            .name("GroupConditionType")
            .enumValueDefinition(EnumValueDefinition.newEnumValueDefinition().name(CONDITION_AND).build())
            .enumValueDefinition(EnumValueDefinition.newEnumValueDefinition().name(CONDITION_OR).build())
            .build();

    public static InputObjectTypeDefinition GroupCondition = InputObjectTypeDefinition.newInputObjectDefinition()
            .name(GROUP_CONDITION_TYPE_NAME)
            .inputValueDefinition(listValueDef("conditions", Condition.getName()))
            .inputValueDefinition(listValueDef("groupConditions", GROUP_CONDITION_TYPE_NAME))
            .inputValueDefinition(valueDef("group", GroupConditionType.getName()))
            .build();

    /**
     * Shortcut for input value definition
     *
     * @param fieldName field name
     * @return field
     */
    public static InputValueDefinition valueDef(String fieldName, String type) {
        return InputValueDefinition.newInputValueDefinition()
                .name(fieldName).type(new TypeName(type))
                .build();
    }

    /**
     * Shortcut for input value definition that has list type
     *
     * @param fieldName field name
     * @return field
     */
    public static InputValueDefinition listValueDef(String fieldName, String type) {
        return InputValueDefinition.newInputValueDefinition()
                .name(fieldName).type(new ListType(new TypeName(type)))
                .build();
    }

}

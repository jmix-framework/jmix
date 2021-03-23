import graphql.Scalars
import graphql.language.*
import io.jmix.core.Metadata
import io.jmix.graphql.schema.FilterTypesBuilder
import io.jmix.graphql.schema.scalar.CustomScalars
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.CarDto

import javax.annotation.Nullable

class FiltrationTest extends AbstractGraphQLTest {

    @Autowired
    private FilterTypesBuilder filterTypesBuilder
    @Autowired
    private Metadata metadata

    @SuppressWarnings('unused')
    void setup() {

    }

    def "buildFilterConditionType"() {
        given:
        def conditionType

        when:
        conditionType = filterTypesBuilder.buildFilterConditionType(metadata.findClass(CarDto))
        def standard = buildStandardFilterTypeCarDto()

        then:
        conditionType != null
        conditionType.isEqualTo(standard)
    }

    private static InputObjectTypeDefinition buildStandardFilterTypeCarDto() {
        String className = 'inp_scr_CarDtoFilterCondition'

        InputObjectTypeDefinition.Builder builder = InputObjectTypeDefinition.newInputObjectDefinition()
                .name(className)

        List<InputValueDefinition> valueDefs = new ArrayList<>()

        def suffix = 'FilterCondition'
        valueDefs.add(listValueDef("id", CustomScalars.GraphQLUUID.name + suffix, null))
        valueDefs.add(listValueDef("manufacturer", Scalars.GraphQLString.name + suffix, null))
        valueDefs.add(listValueDef("price", Scalars.GraphQLBigDecimal.name + suffix, null))
        valueDefs.add(listValueDef("model", Scalars.GraphQLString.name + suffix, null))
        valueDefs.add(listValueDef(FilterTypesBuilder.ConditionUnionType.AND.name(), className, null))
        valueDefs.add(listValueDef(FilterTypesBuilder.ConditionUnionType.OR.name(), className, null))

        builder.inputValueDefinitions(valueDefs)

        return builder.build()
    }

    private static InputValueDefinition listValueDef(String fieldName, String type, @Nullable String description) {
        return InputValueDefinition.newInputValueDefinition()
                .name(fieldName).type(new ListType(new TypeName(type)))
                .description(StringUtils.isBlank(description) ? null : new Description(description, null, false))
                .build()
    }
}

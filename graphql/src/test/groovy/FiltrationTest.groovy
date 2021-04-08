import graphql.language.*
import graphql.schema.*
import io.jmix.core.Metadata
import io.jmix.graphql.schema.FilterManager
import io.jmix.graphql.schema.FilterTypesBuilder
import io.jmix.graphql.schema.Types
import io.jmix.graphql.schema.scalar.CustomScalars
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.CarDto

import javax.annotation.Nullable

import static graphql.Scalars.*
import static io.jmix.graphql.schema.Types.FilterOperation.*
import static io.jmix.graphql.schema.scalar.CustomScalars.GraphQLLong
import static io.jmix.graphql.schema.scalar.CustomScalars.GraphQLUUID

class FiltrationTest extends AbstractGraphQLTest {

    @Autowired
    private FilterTypesBuilder filterTypesBuilder
    @Autowired
    private Metadata metadata
    @Autowired
    private FilterManager filterManager

    private Set<Types.FilterOperation> stringFilterOPs
    private Set<Types.FilterOperation> uuidFilterOPs
    private Set<Types.FilterOperation> numbersFilterOPs
    private Set<Types.FilterOperation> dateTimeFilterOPs
    private Set<Types.FilterOperation> booleanFilterOPs
    private Set<Types.FilterOperation> emptyFilterOPs

    @SuppressWarnings('unused')
    void setup() {
        stringFilterOPs = new HashSet<Types.FilterOperation>(Arrays.asList(EQ, NEQ, IN_LIST, NOT_IN_LIST, STARTS_WITH, ENDS_WITH, CONTAINS, IS_NULL))
        uuidFilterOPs = new HashSet<Types.FilterOperation>(Arrays.asList(EQ, NEQ, IN_LIST, NOT_IN_LIST, IS_NULL))
        numbersFilterOPs = new HashSet<Types.FilterOperation>(Arrays.asList(EQ, NEQ, GT, GTE, LT, LTE, IN_LIST, NOT_IN_LIST, IS_NULL))
        dateTimeFilterOPs = new HashSet<Types.FilterOperation>(Arrays.asList(EQ, NEQ, IN_LIST, NOT_IN_LIST, GT, GTE, LT, LTE, IS_NULL))
        booleanFilterOPs = new HashSet<Types.FilterOperation>(Arrays.asList(EQ, NEQ, IS_NULL))
        emptyFilterOPs = new HashSet<Types.FilterOperation>(Arrays.asList(IS_NULL))
    }

    @SuppressWarnings('UnnecessaryQualifiedReference')
    def "buildScalarFilterConditionType for GraphQLString"() {
        given:
        def emptyER = createCondition("inp_voidFilterCondition", emptyFilterOPs)
        def uuidER = createCondition("inp_uUIDFilterCondition", uuidFilterOPs)
        def booleanER = createCondition("inp_booleanFilterCondition", booleanFilterOPs)

        //numberTypes
        def bigDecimalER = createCondition("inp_bigDecimalFilterCondition", numbersFilterOPs)
        def longER = createCondition("inp_longFilterCondition", numbersFilterOPs)
        def byteER = createCondition("inp_byteFilterCondition", numbersFilterOPs)
        def shortER = createCondition("inp_shortFilterCondition", numbersFilterOPs)
        def floatER = createCondition("inp_floatFilterCondition", numbersFilterOPs)
        def bigIntER = createCondition("inp_bigIntegerFilterCondition", numbersFilterOPs)
        def intER = createCondition("inp_intFilterCondition", numbersFilterOPs)

        //dateTimeTypes
        def localDateTimeER = createCondition("inp_localDateTimeFilterCondition", dateTimeFilterOPs)
        def dateER = createCondition("inp_dateFilterCondition", dateTimeFilterOPs)

        //stringTypes
        def stringER = createCondition("inp_stringFilterCondition", stringFilterOPs)
        def charER = createCondition("inp_charFilterCondition", stringFilterOPs)

        when:
        def emptyAR = filterTypesBuilder.buildScalarFilterConditionType(CustomScalars.GraphQLVoid)
        def uuidAR = filterTypesBuilder.buildScalarFilterConditionType(CustomScalars.GraphQLUUID)
        def booleanAR = filterTypesBuilder.buildScalarFilterConditionType(GraphQLBoolean)

        //numberTypes
        def bigDecimalAR = filterTypesBuilder.buildScalarFilterConditionType(CustomScalars.GraphQLBigDecimal)
        def longAR = filterTypesBuilder.buildScalarFilterConditionType(GraphQLLong)
        def byteAR = filterTypesBuilder.buildScalarFilterConditionType(GraphQLByte)
        def shortAR = filterTypesBuilder.buildScalarFilterConditionType(GraphQLShort)
        def floatAR = filterTypesBuilder.buildScalarFilterConditionType(GraphQLFloat)
        def bigIntAR = filterTypesBuilder.buildScalarFilterConditionType(GraphQLBigInteger)
        def intAR = filterTypesBuilder.buildScalarFilterConditionType(GraphQLInt)

        //dateTimeTypes
        def localDateTimeAR = filterTypesBuilder.buildScalarFilterConditionType(CustomScalars.GraphQLLocalDateTime)
        def dateAR = filterTypesBuilder.buildScalarFilterConditionType(CustomScalars.GraphQLDate)

        //stringTypes
        def stringAR = filterTypesBuilder.buildScalarFilterConditionType(GraphQLString)
        def charAR = filterTypesBuilder.buildScalarFilterConditionType(GraphQLChar)

        then:
        emptyAR.isEqualTo(emptyER)
        uuidAR.isEqualTo(uuidER)
        booleanAR.isEqualTo(booleanER)

        //numberTypes
        bigDecimalAR.isEqualTo(bigDecimalER)
        longAR.isEqualTo(longER)
        byteAR.isEqualTo(byteER)
        shortAR.isEqualTo(shortER)
        floatAR.isEqualTo(floatER)
        bigIntAR.isEqualTo(bigIntER)
        intAR.isEqualTo(intER)

        //dateTimeTypes
        localDateTimeAR.isEqualTo(localDateTimeER)
        dateAR.isEqualTo(dateER)

        //stringTypes
        charAR.isEqualTo(charER)
        stringAR.isEqualTo(stringER)
    }

    @SuppressWarnings('UnnecessaryQualifiedReference')
    def "available operations for scalars"() {
        given:

        when:
        def voidActual = filterManager.availableOperations(CustomScalars.GraphQLVoid)
        def uuidActual = filterManager.availableOperations(CustomScalars.GraphQLUUID)
        def booleanActual = filterManager.availableOperations(GraphQLBoolean)

        //numberTypes
        def bigDecimalActual = filterManager.availableOperations(CustomScalars.GraphQLBigDecimal)
        def longActual = filterManager.availableOperations(CustomScalars.GraphQLLong)
        def byteActual = filterManager.availableOperations(GraphQLByte)
        def shortActual = filterManager.availableOperations(GraphQLShort)
        def floatActual = filterManager.availableOperations(GraphQLFloat)
        def bigIntActual = filterManager.availableOperations(GraphQLBigInteger)
        def intActual = filterManager.availableOperations(GraphQLInt)

        //dateTimeTypes
        def localDateTimeActual = filterManager.availableOperations(CustomScalars.GraphQLLocalDateTime)
        def dateActual = filterManager.availableOperations(CustomScalars.GraphQLDate)

        //stringTypes
        def stringActual = filterManager.availableOperations(GraphQLString)
        def charActual = filterManager.availableOperations(GraphQLChar)

        then:
        voidActual == emptyFilterOPs
        uuidActual == uuidFilterOPs
        booleanActual == booleanFilterOPs
        bigDecimalActual == numbersFilterOPs
        longActual == numbersFilterOPs
        byteActual == numbersFilterOPs
        shortActual == numbersFilterOPs
        floatActual == numbersFilterOPs
        bigIntActual == numbersFilterOPs
        intActual == numbersFilterOPs
        localDateTimeActual == dateTimeFilterOPs
        dateActual == dateTimeFilterOPs
        stringActual == stringFilterOPs
        charActual == stringFilterOPs
    }

    def "doesn't support operation"() {
        given:
        def unsupportedScalar = new GraphQLScalarType(
                "unknownName",
                "unknown description",
                new Coercing() {
                    @Override
                    Object serialize(Object dataFetcherResult) throws CoercingSerializeException {
                        return null
                    }

                    @Override
                    Object parseValue(Object input) throws CoercingParseValueException {
                        return null
                    }

                    @Override
                    Object parseLiteral(Object input) throws CoercingParseLiteralException {
                        return null
                    }
                })

        when:
        filterManager.availableOperations(unsupportedScalar)

        then:
        thrown UnsupportedOperationException
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
        valueDefs.add(listValueDef("id", GraphQLUUID.name + suffix, null))
        valueDefs.add(listValueDef("manufacturer", GraphQLString.name + suffix, null))
        valueDefs.add(listValueDef("price", GraphQLBigDecimal.name + suffix, null))
        valueDefs.add(listValueDef("model", GraphQLString.name + suffix, null))
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

    private static InputValueDefinition valueDef(Types.FilterOperation operation, String type) {
        return valueDef(operation.getId(), type, operation.description)
    }

    private static InputValueDefinition valueDef(String fieldName, String type, @Nullable String description) {
        return InputValueDefinition.newInputValueDefinition()
                .name(fieldName).type(new TypeName(type))
                .description(StringUtils.isBlank(description) ? null : new Description(description, null, false))
                .build()
    }

    private static InputValueDefinition listValueDef(Types.FilterOperation operation, String type) {
        return listValueDef(operation.getId(), type, operation.getDescription())
    }

    static InputObjectTypeDefinition createCondition(String name, Set<Types.FilterOperation> operations) {
       def builder = InputObjectTypeDefinition.newInputObjectDefinition()
                .name(name)
        for (Types.FilterOperation operation : operations) {
            if (IN_LIST == operation || NOT_IN_LIST == operation) {
                builder.inputValueDefinition(listValueDef(operation, GraphQLString.name))
            } else {
                builder.inputValueDefinition(valueDef(operation, GraphQLString.name))
            }
        }
        return builder.build()
    }
}

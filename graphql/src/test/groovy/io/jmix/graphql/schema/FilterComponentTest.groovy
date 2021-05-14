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

package io.jmix.graphql.schema

import graphql.language.*
import io.jmix.core.Metadata
import io.jmix.graphql.AbstractGraphQLTest
import io.jmix.graphql.schema.scalar.CustomScalars
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.CarDto

import javax.annotation.Nullable

import static graphql.Scalars.*
import static io.jmix.graphql.schema.Types.FilterOperation.*
import static io.jmix.graphql.schema.scalar.CustomScalars.GraphQLLong
import static io.jmix.graphql.schema.scalar.CustomScalars.GraphQLUUID

class FilterComponentTest extends AbstractGraphQLTest {

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
    private Set<Types.FilterOperation> timeFilterOPs
    private Set<Types.FilterOperation> booleanFilterOPs

    @SuppressWarnings('unused')
    void setup() {
        stringFilterOPs = new HashSet<Types.FilterOperation>(Arrays.asList(EQ, NEQ, IN_LIST, NOT_IN_LIST, STARTS_WITH, ENDS_WITH, CONTAINS, NOT_CONTAINS, IS_NULL))
        uuidFilterOPs = new HashSet<Types.FilterOperation>(Arrays.asList(EQ, NEQ, IN_LIST, NOT_IN_LIST, IS_NULL))
        numbersFilterOPs = new HashSet<Types.FilterOperation>(Arrays.asList(EQ, NEQ, GT, GTE, LT, LTE, IN_LIST, NOT_IN_LIST, IS_NULL))
        dateTimeFilterOPs = new HashSet<Types.FilterOperation>(Arrays.asList(EQ, NEQ, IN_LIST, NOT_IN_LIST, GT, GTE, LT, LTE, IS_NULL))
        timeFilterOPs = new HashSet<Types.FilterOperation>(Arrays.asList(EQ, NEQ, GT, GTE, LT, LTE, IS_NULL))
        booleanFilterOPs = new HashSet<Types.FilterOperation>(Arrays.asList(EQ, NEQ, IS_NULL))
    }

    @SuppressWarnings('UnnecessaryQualifiedReference')
    def "buildScalarFilterConditionType for GraphQLString"() {
        given:
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
        def localDateER = createCondition("inp_localDateFilterCondition", dateTimeFilterOPs)
        def offsetDateTimeER = createCondition("inp_offsetDateTimeFilterCondition", dateTimeFilterOPs)
        def dateER = createCondition("inp_dateFilterCondition", dateTimeFilterOPs)
        def dateTimeER = createCondition("inp_dateTimeFilterCondition", dateTimeFilterOPs)

        //timeTypes
        def localTimeER = createCondition("inp_localTimeFilterCondition", timeFilterOPs)
        def offsetTimeER = createCondition("inp_offsetTimeFilterCondition", timeFilterOPs)
        def timeER = createCondition("inp_timeFilterCondition", timeFilterOPs)

        //stringTypes
        def stringER = createCondition("inp_stringFilterCondition", stringFilterOPs)
        def charER = createCondition("inp_charFilterCondition", stringFilterOPs)

        when:
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
        def offsetDateTimeAR = filterTypesBuilder.buildScalarFilterConditionType(CustomScalars.GraphQLOffsetDateTime)
        def localDateAR = filterTypesBuilder.buildScalarFilterConditionType(CustomScalars.GraphQLLocalDate)
        def dateAR = filterTypesBuilder.buildScalarFilterConditionType(CustomScalars.GraphQLDate)
        def dateTimeAR = filterTypesBuilder.buildScalarFilterConditionType(CustomScalars.GraphQLDateTime)

        //timeTypes
        def offsetTimeAR = filterTypesBuilder.buildScalarFilterConditionType(CustomScalars.GraphQLOffsetTime)
        def localTimeAR = filterTypesBuilder.buildScalarFilterConditionType(CustomScalars.GraphQLLocalTime)
        def timeAR = filterTypesBuilder.buildScalarFilterConditionType(CustomScalars.GraphQLTime)

        //stringTypes
        def stringAR = filterTypesBuilder.buildScalarFilterConditionType(GraphQLString)
        def charAR = filterTypesBuilder.buildScalarFilterConditionType(GraphQLChar)

        then:
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
        offsetDateTimeAR.isEqualTo(offsetDateTimeER)
        localDateAR.isEqualTo(localDateER)
        dateAR.isEqualTo(dateER)
        dateTimeAR.isEqualTo(dateTimeER)

        //timeTypes
        offsetTimeAR.isEqualTo(offsetTimeER)
        localTimeAR.isEqualTo(localTimeER)
        timeAR.isEqualTo(timeER)

        //stringTypes
        charAR.isEqualTo(charER)
        stringAR.isEqualTo(stringER)
    }

    @SuppressWarnings('UnnecessaryQualifiedReference')
    def "available operations for scalars"() {
        given:

        when:
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
        def offsetDateTimeActual = filterManager.availableOperations(CustomScalars.GraphQLOffsetDateTime)
        def localDateActual = filterManager.availableOperations(CustomScalars.GraphQLLocalDate)
        def dateActual = filterManager.availableOperations(CustomScalars.GraphQLDate)
        def dateTimeActual = filterManager.availableOperations(CustomScalars.GraphQLDateTime)

        //timeTypes
        def offsetTimeActual = filterManager.availableOperations(CustomScalars.GraphQLOffsetTime)
        def localTimeActual = filterManager.availableOperations(CustomScalars.GraphQLLocalTime)
        def timeActual = filterManager.availableOperations(CustomScalars.GraphQLTime)

        //stringTypes
        def stringActual = filterManager.availableOperations(GraphQLString)
        def charActual = filterManager.availableOperations(GraphQLChar)

        then:
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
        localDateActual == dateTimeFilterOPs
        localTimeActual == timeFilterOPs
        offsetTimeActual == timeFilterOPs
        timeActual == timeFilterOPs
        offsetDateTimeActual == dateTimeFilterOPs
        dateActual == dateTimeFilterOPs
        dateTimeActual == dateTimeFilterOPs
        stringActual == stringFilterOPs
        charActual == stringFilterOPs
    }

    def "doesn't support operation"() {
        given:

        when:
        filterManager.availableOperations(CustomScalars.GraphQLVoid)

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

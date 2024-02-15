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

package repository.conditions

import io.jmix.core.impl.repository.query.utils.ConditionTransformer
import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import org.springframework.data.repository.query.parser.PartTree
import test_support.DataSpec
import test_support.entity.repository.Customer
import test_support.entity.repository.SalesOrder

class ConditionBuildingTest extends DataSpec {


    void "simple condition built"() {
        when:
        Condition condition = ConditionTransformer.fromPartTree(new PartTree("findByName", Customer),
                new ArrayList<String>())

        then:
        condition != null
        condition instanceof PropertyCondition
        ((PropertyCondition) condition).property == "name"
        ((PropertyCondition) condition).operation == "equal"
        ((PropertyCondition) condition).parameterName == "name"


        when:
        condition = ConditionTransformer.fromPartTree(new PartTree("countByNumberInOrDateIsNull", SalesOrder),
                new ArrayList<String>())

        then:
        condition != null
        condition instanceof LogicalCondition

        PropertyCondition first = ((LogicalCondition) condition).conditions[0]
        PropertyCondition second = ((LogicalCondition) condition).conditions[1]

        first.property == "number"
        first.operation == "in_list"
        first.parameterName == "number"

        second.property == "date"
        second.operation == "is_set"
        second.parameterName.startsWith("date")
    }

    void "complex condition built"() {
        when:
        Condition condition = ConditionTransformer.fromPartTree(
                new PartTree("existsByDateLessThanEqualAndCustomerAddressStreetEndsWithOrNumberLikeAndCustomerAddressCityIn", SalesOrder),
                new ArrayList<String>()
        )
        then:
        condition != null
        condition instanceof LogicalCondition//in form of "A and B or C and D"

        LogicalCondition and1 = ((LogicalCondition) condition).conditions[0]
        PropertyCondition A = and1.conditions[0]
        A.property == "date"
        A.operation == "less_or_equal"
        A.parameterName == "date"

        PropertyCondition B = and1.conditions[1]
        B.property == "customer.address.street"
        B.operation == "ends_with"
        B.parameterName == "street"


        LogicalCondition and2 = ((LogicalCondition) condition).conditions[1]
        PropertyCondition C = and2.conditions[0]
        C.property == "number"
        C.operation == "contains"
        C.parameterName == "number"

        PropertyCondition D = and2.conditions[1]
        D.property == "customer.address.city"
        D.operation == "in_list"
        D.parameterName == "city"
    }
}

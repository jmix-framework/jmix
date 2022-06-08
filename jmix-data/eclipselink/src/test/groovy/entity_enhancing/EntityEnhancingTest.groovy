/*
 * Copyright 2019 Haulmont.
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

package entity_enhancing

import io.jmix.core.entity.EntityPropertyChangeListener
import io.jmix.core.entity.EntityValues
import test_support.DataSpec
import test_support.entity.TestNotStoredEntity
import test_support.entity.is_get_conflict.GetterConflictEntity
import test_support.entity.petclinic.Pet

class EntityEnhancingTest extends DataSpec {

    def "JPA entity is enhanced"() {

        def pet = new Pet()
        EntityPropertyChangeListener listener = Mock()
        pet.__getEntityEntry().addPropertyChangeListener(listener)

        when:
        pet.setName('Misty')

        then:
        1 * listener.propertyChanged(_)
    }

    def "non-JPA entity is enhanced"() {

        def entity = new TestNotStoredEntity()
        EntityPropertyChangeListener listener = Mock()
        entity.__getEntityEntry().addPropertyChangeListener(listener)

        when:
        entity.setName('Misty')

        then:
        1 * listener.propertyChanged(_)
    }

    def "is-/get- getter should be selected correctly for boolean property"() {
        def entity = new GetterConflictEntity()

        when:
        EntityValues.getValue(entity, "custom")
        EntityValues.getValue(entity, "debit")
        EntityValues.getValue(entity, "overpayment")
        EntityValues.getValue(entity, "counter")
        EntityValues.getValue(entity, "positive")

        then:
        entity.getCustomInvoked
        entity.getDebitInvoked
        entity.isOverpaymentInvoked
        entity.isCounterInvoked
        entity.getPositiveInvoked

        !entity.isCustomInvoked
        !entity.isDebitInvoked
        !entity.getOverpaymentInvoked
        !entity.getCounterInvoked
        !entity.isPositiveInvoked
    }


}

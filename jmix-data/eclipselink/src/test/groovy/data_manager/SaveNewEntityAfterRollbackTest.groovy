/*
 * Copyright 2026 Haulmont.
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

package data_manager

import io.jmix.core.DataManager
import io.jmix.core.EntityStates
import io.jmix.data.exception.UniqueConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.TestManualIdEntity

class SaveNewEntityAfterRollbackTest extends DataSpec {

    @Autowired
    DataManager dataManager

    @Autowired
    EntityStates entityStates

    void cleanup() {
        jdbc.update('delete from TEST_MANUAL_ID_ENTITY')
    }

    def "entity remains new after failed save"() {
        def first = dataManager.create(TestManualIdEntity)
        first.id = 111L
        first.name = 'first'
        dataManager.save(first)

        def second = dataManager.create(TestManualIdEntity)
        second.id = 111L
        second.name = 'second'

        when:
        dataManager.save(second)

        then:
        thrown(UniqueConstraintViolationException)
        entityStates.isNew(second)
        !entityStates.isDetached(second)
    }

    def "new entity can be saved with different id after failed save"() {
        def first = dataManager.create(TestManualIdEntity)
        first.id = 111L
        first.name = 'first'
        dataManager.save(first)

        def second = dataManager.create(TestManualIdEntity)
        second.id = 111L
        second.name = 'second'

        when:
        dataManager.save(second)

        then:
        thrown(UniqueConstraintViolationException)

        when:
        second.id = 222L
        dataManager.save(second)

        then:
        noExceptionThrown()
        dataManager.load(TestManualIdEntity).id(111L).one().name == 'first'
        dataManager.load(TestManualIdEntity).id(222L).one().name == 'second'
    }

    def "repeated failed save does not overwrite existing record"() {
        def first = dataManager.create(TestManualIdEntity)
        first.id = 111L
        first.name = 'first'
        dataManager.save(first)

        def second = dataManager.create(TestManualIdEntity)
        second.id = 111L
        second.name = 'second'

        when:
        dataManager.save(second)

        then:
        thrown(UniqueConstraintViolationException)

        when:
        dataManager.save(second)

        then:
        thrown(UniqueConstraintViolationException)
        dataManager.load(TestManualIdEntity).id(111L).one().name == 'first'
    }
}

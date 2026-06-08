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
package events

import io.jmix.core.DataManager
import io.jmix.core.Id
import io.jmix.core.SaveContext
import io.jmix.data.PersistenceHints
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.events.Bar
import test_support.listeners.TestSaveWithoutEventsListener

class SaveWithoutEventsTest extends DataSpec {

    @Autowired
    DataManager dataManager

    @Autowired
    TestSaveWithoutEventsListener listener

    void setup() {
        listener.reset()
    }

    void cleanup() {
        jdbc.update('delete from TEST_EVENTS_BAR')
    }

    def "both events fire by default"() {
        def bar = dataManager.create(Bar)
        bar.name = 'b1'
        bar.amount = 1

        when:
        dataManager.save(bar)

        then:
        listener.savingCount == 1
        listener.changedCount == 1
    }

    def "EntitySavingEvent is suppressed by hint"() {
        def bar = dataManager.create(Bar)
        bar.name = 'b2'
        bar.amount = 2

        when:
        dataManager.save(new SaveContext()
                .saving(bar)
                .setHint(PersistenceHints.SKIP_ENTITY_SAVING_EVENT, true))

        then:
        listener.savingCount == 0
        listener.changedCount == 1
        dataManager.load(Id.of(bar)).one().amount == 2
    }

    def "EntityChangedEvent is suppressed by hint"() {
        def bar = dataManager.create(Bar)
        bar.name = 'b3'
        bar.amount = 3

        when:
        dataManager.save(new SaveContext()
                .saving(bar)
                .setHint(PersistenceHints.SKIP_ENTITY_CHANGED_EVENT, true))

        then:
        listener.savingCount == 1
        listener.changedCount == 0
        dataManager.load(Id.of(bar)).one().amount == 3
    }

    def "both events suppressed and entity still persisted"() {
        def bar = dataManager.create(Bar)
        bar.name = 'b4'
        bar.amount = 4

        when:
        dataManager.save(new SaveContext()
                .saving(bar)
                .setHint(PersistenceHints.SKIP_ENTITY_SAVING_EVENT, true)
                .setHint(PersistenceHints.SKIP_ENTITY_CHANGED_EVENT, true))

        then:
        listener.savingCount == 0
        listener.changedCount == 0
        dataManager.load(Id.of(bar)).one().amount == 4
    }
}

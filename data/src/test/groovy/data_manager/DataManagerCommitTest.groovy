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

package data_manager

import test_support.entity.TestAppEntity
import test_support.entity.TestAppEntityItem
import test_support.entity.TestSecondAppEntity
import io.jmix.core.DataManager
import io.jmix.core.EntityStates
import io.jmix.core.ViewBuilder
import test_support.DataSpec

import javax.inject.Inject

class DataManagerCommitTest extends DataSpec {

    @Inject
    DataManager dataManager

    @Inject
    EntityStates entityStates

    TestAppEntity appEntity
    TestAppEntityItem appEntityItem


    void setup() {
        appEntity = new TestAppEntity(name: 'appEntity')
        appEntityItem = new TestAppEntityItem(name: 'appEntityItem', appEntity: appEntity)

        dataManager.commit(appEntity, appEntityItem)
    }


    def "test view after commit"() {
        when:

        def view = ViewBuilder.of(TestAppEntity)
                .add("createTs")
                .add("items.createTs")
                .build()
                .setLoadPartialEntities(true)

        def loadedAppEntity = dataManager.reload(appEntity, view)

        then:

        loadedAppEntity.items[0] != null

        !entityStates.isLoaded(loadedAppEntity.items[0], 'name')

        when:

        def entity = new TestSecondAppEntity(name: 'secondAppEntity', appEntity: loadedAppEntity)

        def commitView = ViewBuilder.of(TestSecondAppEntity)
                .add("name")
                .add("appEntity.createTs")
                .add("appEntity.items.name")
                .build()

        def entity1 = dataManager.commit(entity, commitView)

        then:

        entityStates.isLoaded(entity1.appEntity, 'createTs')
        entityStates.isLoaded(entity1.appEntity.items[0], 'name')
    }
}

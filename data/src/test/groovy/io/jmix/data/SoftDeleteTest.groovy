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

package io.jmix.data


import com.sample.app.entity.TestAppEntity
import io.jmix.core.DataManager
import io.jmix.core.EntityStates
import io.jmix.data.test.DataSpec

import javax.inject.Inject

class SoftDeleteTest extends DataSpec {

    @Inject
    DataManager dataManager

    @Inject
    EntityStates entityStates

    def "load deleted entity with filter by soft delete"() {
        def entity
        setup:

        entity = dataManager.create(TestAppEntity)
        entity.name = 'e1'
        entity = dataManager.commit(entity)
        dataManager.remove(entity)

        when:

        def entity2 = dataManager.load(TestAppEntity).id(entity.id).optional().orElse(null)

        then:

        entity2 == null
    }

    def "load deleted entity with disabled filter by soft delete"() {
        def entity
        setup:

        entity = dataManager.create(TestAppEntity)
        entity.name = 'e1'
        entity = dataManager.commit(entity)
        dataManager.remove(entity)

        when:

        def entity2 = dataManager.load(TestAppEntity).id(entity.id).softDeletion(false)
                .optional().orElse(null)

        then:

        entity2 != null
    }
}

/*
 * Copyright 2022 Haulmont.
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

package data_components

import io.jmix.core.entity.EntityValues
import io.jmix.flowui.model.DataComponents
import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.TestIdentityIdEntity
import test_support.spec.DataContextSpec

class CollectionContainerIdentityIdTest extends DataContextSpec {

    @Autowired DataComponents factory

    def "replace item"() {
        def container = factory.createCollectionContainer(TestIdentityIdEntity)

        def entity1 = new TestIdentityIdEntity(name: '111')

        container.getMutableItems().add(entity1)

        TestIdentityIdEntity serverSideEntity = reserialize(entity1)
        serverSideEntity.setId(10)
        TestIdentityIdEntity returnedEntity = reserialize(serverSideEntity)

        when:
        container.replaceItem(returnedEntity)

        then:
        container.getItems().size() == 1
        container.containsItem(returnedEntity)
    }

    def "item index"() {
        def container = factory.createCollectionContainer(TestIdentityIdEntity)

        def entity1 = new TestIdentityIdEntity(id: 1, name: '111')
        def entity11 = reserialize(entity1)

        container.getMutableItems().add(entity1)

        expect:
        container.getItemIndex(EntityValues.getId(entity1)) == 0
        container.getItemIndex(entity1) == 0
        container.getItemIndex(entity11) == 0
    }
}

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

package delete_policy

import io.jmix.core.DataManager
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.delete_policy.CascadeDeletionChild
import test_support.entity.delete_policy.CascadeDeletionParent

class DeletePolicyTest extends DataSpec {

    @Autowired
    protected DataManager dataManager

    def "Cascade deletion test"() {
        def parent1 = dataManager.create(CascadeDeletionParent)
        parent1.name = "parent 1"
        parent1 = dataManager.save(parent1)

        def parent2 = dataManager.create(CascadeDeletionParent)
        parent2.name = "parent 2"
        parent2 = dataManager.save(parent2)

        def child = dataManager.create(CascadeDeletionChild)
        child.name = "child 1-1"
        child.parent = parent1
        dataManager.save(child)

        child = dataManager.create(CascadeDeletionChild)
        child.name = "child 1-2"
        child.parent = parent1
        dataManager.save(child)

        child = dataManager.create(CascadeDeletionChild)
        child.name = "child 2-1"
        child.parent = parent2
        dataManager.save(child)

        child = dataManager.create(CascadeDeletionChild)
        child.name = "child 2-2"
        child.parent = parent2
        child = dataManager.save(child)

        when:
        def parentsBefore = dataManager.load(CascadeDeletionParent).all().list()
        def childrenBefore = dataManager.load(CascadeDeletionChild).all().list()
        dataManager.remove(parent1)
        dataManager.remove(child)
        def parentsAfter = dataManager.load(CascadeDeletionParent).all().list()
        def childrenAfter = dataManager.load(CascadeDeletionChild).all().list()

        then:
        parentsBefore.size() == 2
        childrenBefore.size() == 4
        parentsAfter.size() == 1
        childrenAfter.size() == 1
    }

}

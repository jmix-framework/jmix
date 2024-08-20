/*
 * Copyright 2024 Haulmont.
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

package lazy_loading

import io.jmix.core.DataManager
import io.jmix.core.FetchPlan
import io.jmix.core.Id
import io.jmix.core.Metadata
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.embedded_pk.Branch
import test_support.entity.embedded_pk.Root

class EmbeddedIdReferenceTest extends DataSpec {

    @Autowired
    DataManager dataManager
    @Autowired
    Metadata metadata


    void "test o2m loading"() {
        setup:

        var root = metadata.create(Root)
        root.id.code1 = "r"
        root.id.code2 = 1
        root.name = "root1"
        root.branches = []

        var branch1 = metadata.create(Branch)
        branch1.id.code12 = "b"
        branch1.id.code22 = 1
        branch1.name = "branch1"

        root.branches.add(branch1)
        branch1.root = root

        var branch2 = metadata.create(Branch)
        branch2.id.code12 = "b"
        branch2.id.code22 = 2
        branch2.name = "branch2"

        root.branches.add(branch2)
        branch2.root = root

        dataManager.save(branch1, branch2, root)


        when:
        var fullyLoaded = dataManager.load(Id.of(root))
                .fetchPlan(b -> {
                    b.add("branches", FetchPlan.BASE)
                })
                .one()

        then:
        fullyLoaded.branches[0].root.name == "root1"
        fullyLoaded.branches[1].root.name == "root1"

        when:
        var partiallyLoaded = dataManager.load(Id.of(root)).one()

        then:
        partiallyLoaded.branches[0].root.name == "root1"
        partiallyLoaded.branches[1].root.name == "root1"


        when:
        var partiallyLoadedBranch = dataManager.load(Id.of(branch1)).one()

        then:
        partiallyLoadedBranch.root.branches.contains(branch1)
        partiallyLoadedBranch.root.branches.contains(branch2)

        cleanup:
        jdbc.update('delete from TST_BRANCH')
        jdbc.update('delete from TST_ROOT')
    }


    void "test o2o loading"() {
        setup:

        var root = metadata.create(Root)
        root.id.code1 = "r"
        root.id.code2 = 1
        root.name = "root1"
        root.branches = []

        var o2oBranch = metadata.create(Branch)
        o2oBranch.id.code12 = "ob"
        o2oBranch.id.code22 = 1
        o2oBranch.name = "o2oBranch1"

        var o2oRoot = metadata.create(Root)
        o2oRoot.id.code1 = "o2oR"
        o2oRoot.id.code2 = 1
        o2oRoot.name = "o2oRoot1"
        o2oRoot.branches = []

        o2oRoot.o2oBranch = o2oBranch
        o2oBranch.o2oRoot = o2oRoot
        root.branches.add(o2oBranch)
        o2oBranch.root = root

        dataManager.save(o2oBranch, root, o2oRoot)


        when:
        var fullyLoaded = dataManager.load(Id.of(o2oRoot))
                .fetchPlan(b -> {
                    b.add("o2oBranch", FetchPlan.BASE)
                })
                .one()

        then:
        fullyLoaded.o2oBranch.name == "o2oBranch1"


        when:
        var partiallyLoadedO2oRoot = dataManager.load(Id.of(o2oRoot)).one()

        then:
        partiallyLoadedO2oRoot.o2oBranch.o2oRoot.name == "o2oRoot1"
        partiallyLoadedO2oRoot.o2oBranch.getRoot().name == "root1"


        when:
        var partiallyLoadedO2oBranch = dataManager.load(Id.of(o2oBranch)).one()

        then:
        partiallyLoadedO2oBranch.o2oRoot.o2oBranch.name == "o2oBranch1"
        partiallyLoadedO2oBranch.root.branches[0].name == "o2oBranch1"


        cleanup:
        jdbc.update('delete from TST_BRANCH')
        jdbc.update('delete from TST_ROOT')
    }

}
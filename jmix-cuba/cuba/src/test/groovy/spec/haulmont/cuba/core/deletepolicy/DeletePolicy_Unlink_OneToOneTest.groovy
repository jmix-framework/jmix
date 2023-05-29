/*
 * Copyright (c) 2008-2018 Haulmont.
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

package spec.haulmont.cuba.core.deletepolicy

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.LoadContext
import com.haulmont.cuba.core.global.View
import com.haulmont.cuba.core.model.deletepolicy.DeletePolicy_OneToOne_First
import com.haulmont.cuba.core.model.deletepolicy.DeletePolicy_OneToOne_Second
import io.jmix.core.FetchPlan
import io.jmix.core.Metadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import spec.haulmont.cuba.core.CoreTestSpecification

class DeletePolicy_Unlink_OneToOneTest extends CoreTestSpecification {
    @Autowired
    private Persistence persistence
    @Autowired
    private Metadata metadata
    @Autowired
    private DataManager dataManager

    private DeletePolicy_OneToOne_First first
    private DeletePolicy_OneToOne_Second second

    void setup() {
        persistence.runInTransaction({ em ->
            first = metadata.create(DeletePolicy_OneToOne_First)
            first.firstFld = 'first fld'
            em.persist(first)

            second = metadata.create(DeletePolicy_OneToOne_Second)
            second.secondFld = 'second fld'
            second.setFirst(first)
            em.persist(second)
        })
    }

    void cleanup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.dataSource)
        jdbcTemplate.update('delete from TEST_DELETE_POLICY_ONE_TO_ONE_SECOND')
        jdbcTemplate.update('delete from TEST_DELETE_POLICY_ONE_TO_ONE_FIRST')
    }

    def "unlink @OneToOny property if it isn't owning side"() {
        setup:
        FetchPlan secondView_1 = new View(DeletePolicy_OneToOne_Second.class)
                .addProperty("secondFld")
        FetchPlan firstView_1 = new View(DeletePolicy_OneToOne_First.class)
                .addProperty("firstFld")
                .addProperty("second", secondView_1)

        FetchPlan firstView_2 = new View(DeletePolicy_OneToOne_First.class)
                .addProperty("firstFld")
        FetchPlan secondView_2 = new View(DeletePolicy_OneToOne_Second.class)
                .addProperty("secondFld")
                .addProperty("first", firstView_2)

        when:

        DeletePolicy_OneToOne_First entityFirst = dataManager.load(
                new LoadContext<DeletePolicy_OneToOne_First>(DeletePolicy_OneToOne_First.class)
                        .setView(firstView_1)
                        .setId(first.id))

        then:

        entityFirst.second != null

        when:

        dataManager.remove(entityFirst)
        DeletePolicy_OneToOne_Second entitySecond = dataManager.load(
                new LoadContext<DeletePolicy_OneToOne_Second>(DeletePolicy_OneToOne_Second.class)
                        .setView(secondView_2)
                        .setId(second.id))

        then:

        entitySecond.first == null
    }

    def "unlink @OneToOny property if it is owning side and is loaded"() {
        setup:
        FetchPlan firstView = new View(DeletePolicy_OneToOne_First.class)
                .addProperty("firstFld")
        FetchPlan secondView_2 = new View(DeletePolicy_OneToOne_Second.class)
                .addProperty("secondFld")
                .addProperty("first", firstView)

        when:

        DeletePolicy_OneToOne_Second entitySecond = dataManager.load(
                new LoadContext<DeletePolicy_OneToOne_Second>(DeletePolicy_OneToOne_Second.class)
                        .setView(secondView_2)
                        .setId(second.id))

        then:

        entitySecond.first != null

        when:

        dataManager.remove(entitySecond)
        entitySecond = dataManager.load(
                new LoadContext<DeletePolicy_OneToOne_Second>(DeletePolicy_OneToOne_Second.class)
                        .setView(secondView_2)
                        .setId(second.id).setSoftDeletion(false))

        then:

        entitySecond.first == null
    }

    def "unlink @OneToOny property if it is owning side and isn't loaded"() {
        setup:
        FetchPlan firstView = new View(DeletePolicy_OneToOne_First.class)
                .addProperty("firstFld")
        FetchPlan secondView_2 = new View(DeletePolicy_OneToOne_Second.class)
                .addProperty("secondFld")
                .addProperty("first", firstView)

        when:

        DeletePolicy_OneToOne_Second entitySecond = dataManager.load(
                new LoadContext<DeletePolicy_OneToOne_Second>(DeletePolicy_OneToOne_Second.class)
                        .setView(secondView_2)
                        .setId(second.id))

        then:

        entitySecond.first != null

        when:

        dataManager.remove(dataManager.load(new LoadContext<DeletePolicy_OneToOne_Second>(DeletePolicy_OneToOne_Second.class)
                .setView(FetchPlan.LOCAL)
                .setId(second.id)))
        entitySecond = dataManager.load(
                new LoadContext<DeletePolicy_OneToOne_Second>(DeletePolicy_OneToOne_Second.class)
                        .setView(secondView_2)
                        .setId(second.id).setSoftDeletion(false))

        then:

        entitySecond.first == null
    }
}

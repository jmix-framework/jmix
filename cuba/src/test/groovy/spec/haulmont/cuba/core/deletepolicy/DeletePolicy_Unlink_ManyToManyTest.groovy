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
import com.haulmont.cuba.core.model.deletepolicy.DeletePolicy_ManyToMany_First
import com.haulmont.cuba.core.model.deletepolicy.DeletePolicy_Root
import io.jmix.core.FetchPlan
import io.jmix.core.Metadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import spec.haulmont.cuba.core.CoreTestSpecification

class DeletePolicy_Unlink_ManyToManyTest extends CoreTestSpecification {
    @Autowired
    private Persistence persistence
    @Autowired
    private Metadata metadata
    @Autowired
    private DataManager dataManager

    private DeletePolicy_Root root
    private DeletePolicy_ManyToMany_First first1, first2

    void setup() {
        persistence.runInTransaction({ em ->
            root = metadata.create(DeletePolicy_Root)
            root.rootFld = 'root fld'
            em.persist(root)

            first1 = metadata.create(DeletePolicy_ManyToMany_First)
            first1.firstFld = 'first fld #1'
            em.persist(first1)

            first2 = metadata.create(DeletePolicy_ManyToMany_First)
            first2.firstFld = 'first fld #2'
            em.persist(first2)

            def list = new ArrayList()
            list.add(first1)
            list.add(first2)

            root.setManytomany(list)
        })
    }

    void cleanup() {
        def jdbcTemplate = new JdbcTemplate(persistence.dataSource)
        jdbcTemplate.update('delete from TEST_DELETE_POLICY_ROOT_DELETE_POLICY_MANY_TO_MANY_FIRST_LINK')
        jdbcTemplate.update('delete from TEST_DELETE_POLICY_MANY_TO_MANY_FIRST')
        jdbcTemplate.update('delete from TEST_DELETE_POLICY_ROOT')
    }

    def "unlink @ManyToMany property if it is owning side and is loaded"() {
        setup:
        FetchPlan firstView = new View(DeletePolicy_ManyToMany_First.class)
                .addProperty("firstFld")
        FetchPlan rootView = new View(DeletePolicy_Root.class)
                .addProperty("rootFld")
                .addProperty("manytomany", firstView)

        when:

        DeletePolicy_Root entityRoot = dataManager.load(
                new LoadContext<DeletePolicy_Root>(DeletePolicy_Root.class)
                        .setView(rootView)
                        .setId(root.id))

        then:

        entityRoot.manytomany != null
        entityRoot.manytomany.size() == 2

        when:

        dataManager.remove(entityRoot)
        entityRoot = dataManager.load(
                new LoadContext<DeletePolicy_Root>(DeletePolicy_Root.class)
                        .setView(rootView)
                        .setId(root.id).setSoftDeletion(false))

        DeletePolicy_ManyToMany_First entityFirst1 = dataManager.load(
                new LoadContext<DeletePolicy_ManyToMany_First>(DeletePolicy_ManyToMany_First.class)
                        .setView(FetchPlan.LOCAL)
                        .setId(first1.id))

        DeletePolicy_ManyToMany_First entityFirst2 = dataManager.load(
                new LoadContext<DeletePolicy_ManyToMany_First>(DeletePolicy_ManyToMany_First.class)
                        .setView(FetchPlan.LOCAL)
                        .setId(first2.id))

        then:

        entityRoot.manytomany != null
        entityRoot.manytomany.size() == 0
        entityFirst1 != null
        entityFirst2 != null
    }

    def "unlink @ManyToMany property if it is owning side and isn't loaded"() {
        setup:
        FetchPlan firstView = new View(DeletePolicy_ManyToMany_First.class)
                .addProperty("firstFld")
        FetchPlan rootView = new View(DeletePolicy_Root.class)
                .addProperty("rootFld")
                .addProperty("manytomany", firstView)

        when:

        DeletePolicy_Root entityRoot = dataManager.load(
                new LoadContext<DeletePolicy_Root>(DeletePolicy_Root.class)
                        .setView(rootView)
                        .setId(root.id))

        then:

        entityRoot.manytomany != null
        entityRoot.manytomany.size() == 2

        when:

        dataManager.remove(dataManager.load(new LoadContext<DeletePolicy_Root>(DeletePolicy_Root.class)
                .setView(FetchPlan.LOCAL)
                .setId(root.id)))
        entityRoot = dataManager.load(
                new LoadContext<DeletePolicy_Root>(DeletePolicy_Root.class)
                        .setView(rootView)
                        .setId(root.id).setSoftDeletion(false))

        DeletePolicy_ManyToMany_First entityFirst1 = dataManager.load(
                new LoadContext<DeletePolicy_ManyToMany_First>(DeletePolicy_ManyToMany_First.class)
                        .setView(FetchPlan.LOCAL)
                        .setId(first1.id))

        DeletePolicy_ManyToMany_First entityFirst2 = dataManager.load(
                new LoadContext<DeletePolicy_ManyToMany_First>(DeletePolicy_ManyToMany_First.class)
                        .setView(FetchPlan.LOCAL)
                        .setId(first2.id))

        then:

        entityRoot.manytomany != null
        entityRoot.manytomany.size() == 0
        entityFirst1 != null
        entityFirst2 != null
    }
}

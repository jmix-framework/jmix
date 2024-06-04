/*
 * Copyright 2023 Haulmont.
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import test_support.DataSpec
import test_support.entity.lazyloading.self_ref_nested_loading.O2MSelfRefEntity
import test_support.entity.lazyloading.self_ref_nested_loading.O2OSelfRefEntity

import javax.sql.DataSource

/**
 * Added to ensure that self reference replaced correctly for OneToOne relationship on mapped by side.
 * OneToOne owning side and OneToMany relationships have no such vulnerability and covered by test just in case.
 * ManyToMany case is too different and skipped.
 */
class NestedLazyLoadingSelfReferenceTest extends DataSpec {
    @Autowired
    DataManager dataManager
    @Autowired
    DataSource dataSource

    private JdbcTemplate jdbcTemplate

    O2OSelfRefEntity o2OEntity1, o2OEntity2, o2OEntity3
    O2MSelfRefEntity o2MEntity1, o2MEntity2, o2MEntity3


    @Override
    void setup() {
        jdbcTemplate = new JdbcTemplate(dataSource)

        o2OEntity1 = saveO2OSelfRefEntity(1, null)
        o2OEntity2 = saveO2OSelfRefEntity(2, o2OEntity1)
        o2OEntity3 = saveO2OSelfRefEntity(3, o2OEntity2)

        o2MEntity1 = saveO2MSelfRefEntity(1, null)
        o2MEntity2 = saveO2MSelfRefEntity(2, o2MEntity1)
        o2MEntity3 = saveO2MSelfRefEntity(3, o2MEntity2)
    }

    @Override
    void cleanup() {
        jdbcTemplate.update("delete from TEST_LL_O2O_SELF_REF_ENTITY")
        jdbcTemplate.update("delete from TEST_LL_O2M_SELF_REF_ENTITY")
    }

    def "Self reference replaced correctly: O2O mappedBy side"() {
        when:
        O2OSelfRefEntity loadedO2MEntity1 =
                dataManager.load(O2OSelfRefEntity.class)
                        .query("select e from test_ll_O2OSelfRefEntity e where e.manager is null")
                        .list().stream().findFirst().orElseThrow()

        then: "reference replaced correctly after lazy loading"
        loadedO2MEntity1.getReport() == o2OEntity2
        // Second report was incorrect. It was set to "o2MEntity1" instead
        loadedO2MEntity1.getReport().getReport() == o2OEntity3


        cleanup:
        jdbcTemplate.execute("delete from TEST_LL_O2O_SELF_REF_ENTITY")
    }

    /**
     * Just in case: Check the same as for "Self reference replaced correctly O2O mappedBy side"
     */
    def "Self reference replaced correctly: O2O owning side"() {
        when:
        O2OSelfRefEntity loadedO2MEntity3 =
                dataManager.load(O2OSelfRefEntity.class)
                        .query("select e from test_ll_O2OSelfRefEntity e where e.report is null")
                        .list().stream().findFirst().orElseThrow()

        then:
        loadedO2MEntity3.getManager() == o2OEntity2
        loadedO2MEntity3.getManager().getManager() == o2OEntity1

        cleanup:
        jdbcTemplate.update("delete from TEST_LL_O2O_SELF_REF_ENTITY")
    }

    /**
     * Just in case: Check the same as for "Self reference replaced correctly O2O mappedBy side"
     */
    def "Self reference replaced correctly: O2M mappedBy side"() {
        when:
        O2MSelfRefEntity loadedO2MEntity1 =
                dataManager.load(O2MSelfRefEntity.class)
                        .query("select e from test_ll_O2MSelfRefEntity e where e.manager is null")
                        .list().stream().findFirst().orElseThrow()

        then:
        loadedO2MEntity1.getReport().iterator().next() == o2MEntity2
        loadedO2MEntity1.getReport().iterator().next()
                .getReport().iterator().next() == o2MEntity3


        cleanup:
        jdbcTemplate.execute("delete from TEST_LL_O2M_SELF_REF_ENTITY")
    }

    /**
     * Just in case: Check the same as for "Self reference replaced correctly O2O mappedBy side"
     */
    def "Self reference replaced correctly: for O2M owning side"() {
        when:
        List<Map<String, Object>> rows =
                jdbcTemplate.queryForList("SELECT id, manager_id FROM TEST_LL_O2M_SELF_REF_ENTITY ORDER BY id")

        then:
        rows.get(0).get("id") == uuid(1).toString()
        rows.get(0).get("manager_id") == null

        rows.get(1).get("id") == uuid(2).toString()
        rows.get(1).get("manager_id") == uuid(1).toString()

        rows.get(2).get("id") == uuid(3).toString()
        rows.get(2).get("manager_id") == uuid(2).toString()

        when:
        O2MSelfRefEntity loadedO2MEntity3 =
                dataManager.load(O2MSelfRefEntity.class)
                        .query("select e from test_ll_O2MSelfRefEntity e where e.report is empty")
                        .list().stream().findFirst().orElseThrow()

        then:
        loadedO2MEntity3.getManager() == o2MEntity2
        loadedO2MEntity3.getManager().getManager() == o2MEntity1
    }

    private O2OSelfRefEntity saveO2OSelfRefEntity(int number, O2OSelfRefEntity manager) {
        O2OSelfRefEntity employee = dataManager.create(O2OSelfRefEntity.class)
        employee.setId(uuid(number))
        employee.setName("" + number)
        employee.setManager(manager)
        dataManager.save(employee)
        return employee
    }

    private O2MSelfRefEntity saveO2MSelfRefEntity(int number, O2MSelfRefEntity manager) {
        O2MSelfRefEntity o2MSelfRefEntity = dataManager.create(O2MSelfRefEntity.class)
        o2MSelfRefEntity.setId(uuid(number))
        o2MSelfRefEntity.setName("" + number)
        o2MSelfRefEntity.setManager(manager)
        dataManager.save(o2MSelfRefEntity)
        return o2MSelfRefEntity
    }

    private static UUID uuid(int number) {
        return UUID.fromString("00000000-0000-0000-0000-00000000000" + number)
    }
}

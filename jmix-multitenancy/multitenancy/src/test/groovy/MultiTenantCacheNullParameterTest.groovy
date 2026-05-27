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

import io.jmix.core.CoreConfiguration
import io.jmix.core.UnconstrainedDataManager
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.multitenancy.MultitenancyConfiguration
import io.jmix.security.SecurityConfiguration
import io.jmix.securitydata.SecurityDataConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.MultitenancyTestConfiguration
import test_support.entity.MyEntity

@ContextConfiguration(classes = [CoreConfiguration, DataConfiguration, EclipselinkConfiguration,
        SecurityConfiguration, SecurityDataConfiguration, MultitenancyConfiguration, MultitenancyTestConfiguration])
class MultiTenantCacheNullParameterTest extends Specification {

    @Autowired
    UnconstrainedDataManager unconstrainedDataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    void cleanup() {
        jdbcTemplate.execute("delete from MY_ENTITY")
    }

    def "loads cached tenant entity with null query parameter"() {
        given:
        MyEntity entity = unconstrainedDataManager.create(MyEntity)
        entity.name = "test-name"
        unconstrainedDataManager.save(entity)

        when:
        List<MyEntity> loadedList = unconstrainedDataManager.load(MyEntity)
                .query("select e from MyEntity e where (:name is null or e.name = :name)")
                .parameter("name", null)
                .list()

        then:
        loadedList.size() == 1
    }
}

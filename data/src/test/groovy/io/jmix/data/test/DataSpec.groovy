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

package io.jmix.data.test

import com.sample.app.TestAppConfiguration
import io.jmix.core.JmixCoreConfiguration
import io.jmix.data.JmixDataConfiguration
import io.jmix.data.Persistence
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject

@ContextConfiguration(classes = [JmixCoreConfiguration, JmixDataConfiguration, JmixDataTestConfiguration, TestAppConfiguration])
class DataSpec extends Specification {

    @Inject
    Persistence persistence

    void setup() {
        persistence.createTransaction().commit()
    }

    void cleanup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource())

        jdbcTemplate.update('delete from SEC_USER_ROLE')
        jdbcTemplate.update('delete from SEC_ROLE')
        jdbcTemplate.update('delete from SEC_USER')
        jdbcTemplate.update('delete from SEC_GROUP')

        jdbcTemplate.update('delete from TEST_DATE_TIME_ENTITY')
        jdbcTemplate.update('delete from TEST_APP_ENTITY_ITEM')
        jdbcTemplate.update('delete from TEST_SECOND_APP_ENTITY')
        jdbcTemplate.update('delete from TEST_APP_ENTITY')
        jdbcTemplate.update('delete from TEST_IDENTITY_ID_ENTITY')
        jdbcTemplate.update('delete from TEST_IDENTITY_UUID_ENTITY')
        jdbcTemplate.update('delete from TEST_COMPOSITE_KEY_ENTITY')

        jdbcTemplate.update('delete from SALES_PRODUCT')
    }
}

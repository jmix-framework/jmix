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

package test_support

import io.jmix.core.JmixCoreConfiguration
import io.jmix.data.JmixDataConfiguration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired

@ContextConfiguration(classes = [JmixCoreConfiguration, JmixDataConfiguration, JmixDataTestConfiguration])
@TestExecutionListeners(value = AppContextTestExecutionListener,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class DataSpec extends Specification {

    @Autowired
    TransactionTemplate transaction
    
    @Autowired
    JdbcTemplate jdbc

    void setup() {
        transaction.executeWithoutResult {}
    }

    void cleanup() {
        jdbc.update('delete from SEC_USER_ROLE')
        jdbc.update('delete from SEC_ROLE')
        jdbc.update('delete from SEC_USER')
        jdbc.update('delete from SEC_GROUP')

        jdbc.update('delete from TEST_DATE_TIME_ENTITY')
        jdbc.update('delete from TEST_APP_ENTITY_ITEM')
        jdbc.update('delete from TEST_SECOND_APP_ENTITY')
        jdbc.update('delete from TEST_APP_ENTITY')
        jdbc.update('delete from TEST_IDENTITY_ID_ENTITY')
        jdbc.update('delete from TEST_IDENTITY_UUID_ENTITY')
        jdbc.update('delete from TEST_COMPOSITE_KEY_ENTITY')

        jdbc.update('delete from SALES_PRODUCT')
    }
}

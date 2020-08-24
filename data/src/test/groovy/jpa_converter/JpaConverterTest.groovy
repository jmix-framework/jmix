/*
 * Copyright 2020 Haulmont.
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

package jpa_converter

import io.jmix.core.DataManager
import io.jmix.core.Id
import test_support.DataSpec
import test_support.entity.TestConverterEntity
import test_support.entity.TestPhone

import org.springframework.beans.factory.annotation.Autowired
import javax.sql.DataSource

class JpaConverterTest extends DataSpec {

    @Autowired
    DataManager dataManager

    @Autowired
    DataSource dataSource

    def "test"() {
        def uri = new URI("https://jmix.io")
        def phone = new TestPhone('7', '9271112233')
        def entity = dataManager.create(TestConverterEntity)
        entity.uri = uri
        entity.phone = phone

        when:
        dataManager.save(entity)
        def entity1 = dataManager.load(Id.of(entity)).one()

        then:
        entity1.getUri() == uri
        entity1.getPhone() == phone

        and:
        def databaseMetaData = dataSource.getConnection().getMetaData()
        def resultSet = databaseMetaData.getColumns(null, null, 'TEST_CONVERTER_ENTITY', 'URI')
        resultSet.next()
        def dataType = resultSet.getInt('DATA_TYPE')
        dataType == java.sql.Types.VARCHAR
    }
}

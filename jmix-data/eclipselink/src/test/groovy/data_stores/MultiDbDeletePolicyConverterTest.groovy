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

package data_stores

import io.jmix.core.DataManager
import io.jmix.core.FetchPlan
import io.jmix.core.Metadata
import io.jmix.data.StoreAwareLocator
import io.jmix.data.persistence.DbTypeConverter
import io.jmix.data.persistence.DbmsSpecifics
import org.spockframework.spring.SpringSpy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import test_support.DataSpec
import test_support.entity.multidb.Db1Airport
import test_support.entity.multidb.Db1Terminal

import java.sql.ResultSet

/**
 * When delete policy processing has to null a foreign key of an unfetched reference via raw JDBC,
 * the id bind parameter must be converted with the {@link DbTypeConverter} of the entity's own store,
 * not the main store's one (see jmix-framework/jmix DeletePolicyProcessor).
 */
class MultiDbDeletePolicyConverterTest extends DataSpec {

    @Autowired
    Metadata metadata
    @Autowired
    DataManager dataManager
    @Autowired
    StoreAwareLocator storeAwareLocator

    @SpringSpy
    DbmsSpecifics dbmsSpecifics

    def cleanup() {
        try {
            storeAwareLocator.getJdbcTemplate('db1').update('delete from TERMINAL')
            storeAwareLocator.getJdbcTemplate('db1').update('delete from AIRPORT')
        } catch (DataAccessException e) {
            // ignore
        }
    }

    def "FK nulling of an unfetched reference converts the id with the DbTypeConverter of the entity's store"() {
        given: "a terminal referencing an airport, both in the additional db1 store"
        Db1Airport airport = metadata.create(Db1Airport)
        airport.name = 'Sheremetyevo'
        Db1Terminal terminal = metadata.create(Db1Terminal)
        terminal.name = 'B'
        terminal.airport = airport
        dataManager.save(airport, terminal)

        and: "DbTypeConverter lookups are wrapped to record values passed to getSqlObject"
        List converterCalls = []
        dbmsSpecifics.getDbTypeConverter() >> {
            new RecordingConverter(target: callRealMethod() as DbTypeConverter,
                    tag: 'main:no-arg', calls: converterCalls)
        }
        dbmsSpecifics.getDbTypeConverter(_ as String) >> { String storeName ->
            new RecordingConverter(target: callRealMethodWithArgs(storeName) as DbTypeConverter,
                    tag: storeName, calls: converterCalls)
        }

        and: "the terminal is loaded without the airport reference fetched"
        Db1Terminal loadedTerminal = dataManager.load(Db1Terminal)
                .id(terminal.id)
                .fetchPlan(FetchPlan.LOCAL)
                .one()

        when: "the terminal is soft-deleted"
        dataManager.remove(loadedTerminal)

        then: "the raw JDBC update binds the terminal id through the db1 store converter"
        converterCalls.contains(['db1', terminal.id])

        and: "not through the main store converter"
        !converterCalls.contains(['main:no-arg', terminal.id])

        and: "the reference is actually nulled in the db1 database"
        def row = storeAwareLocator.getJdbcTemplate('db1')
                .queryForMap("select AIRPORT_ID, DELETE_TS from TERMINAL where ID = '${terminal.id}'")
        row['AIRPORT_ID'] == null
        row['DELETE_TS'] != null
    }

    static class RecordingConverter implements DbTypeConverter {
        DbTypeConverter target
        String tag
        List calls

        @Override
        Object getJavaObject(ResultSet resultSet, int column) {
            target.getJavaObject(resultSet, column)
        }

        @Override
        Object getSqlObject(Object value) {
            calls << [tag, value]
            target.getSqlObject(value)
        }

        @Override
        int getSqlType(Class<?> javaClass) {
            target.getSqlType(javaClass)
        }

        @Override
        String getTypeAndVersion() {
            target.getTypeAndVersion()
        }
    }
}

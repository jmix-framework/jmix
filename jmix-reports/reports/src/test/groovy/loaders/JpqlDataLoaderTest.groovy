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

package loaders

import io.jmix.data.StoreAwareLocator
import io.jmix.reports.libintegration.JpqlDataLoader
import org.eclipse.persistence.queries.CursoredStream
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.SQLException

/**
 * Unit tests for {@link JpqlDataLoader} streaming internals: DB-product-aware fetch size (MySQL/MariaDB
 * need the driver's magic value) and cursor teardown that must not mask an in-flight exception.
 */
class JpqlDataLoaderTest extends Specification {

    def "JPQL streaming resolves the driver's magic fetch size for MySQL/MariaDB, default otherwise"() {
        given:
        def metaData = Mock(DatabaseMetaData) { getDatabaseProductName() >> productName }
        def connection = Mock(Connection) { getMetaData() >> metaData }
        def dataSource = Mock(DataSource) { getConnection() >> connection }
        def locator = Mock(StoreAwareLocator) { getDataSource("main") >> dataSource }
        def loader = new JpqlDataLoader()
        loader.storeAwareLocator = locator

        expect:
        loader.resolveStreamingFetchSize("main") == expectedFetchSize

        where:
        productName            | expectedFetchSize
        "MySQL"                | Integer.MIN_VALUE
        "MariaDB"              | Integer.MIN_VALUE
        "PostgreSQL"           | 1000
        "HSQL Database Engine" | 1000
    }

    def "a transient metadata-detection failure is not cached, so the next call retries"() {
        given: "the first connection attempt fails transiently, the second succeeds reporting MySQL"
        def metaData = Mock(DatabaseMetaData) { getDatabaseProductName() >> "MySQL" }
        def goodConnection = Mock(Connection) { getMetaData() >> metaData }
        def dataSource = Mock(DataSource)
        dataSource.getConnection() >> { throw new SQLException("pool exhausted") } >> goodConnection
        def locator = Mock(StoreAwareLocator) { getDataSource("main") >> dataSource }
        def loader = new JpqlDataLoader()
        loader.storeAwareLocator = locator

        when: "first call hits the transient failure"
        def first = loader.resolveStreamingFetchSize("main")

        then: "it falls back to the default without caching that fallback"
        first == 1000

        when: "second call retries detection and now sees MySQL"
        def second = loader.resolveStreamingFetchSize("main")

        then: "the fallback was not cached, so detection reruns and returns the MySQL magic value"
        second == Integer.MIN_VALUE
    }

    def "a cursor close failure is swallowed so it cannot mask the in-flight exception"() {
        given:
        def cursor = Mock(CursoredStream)
        def loader = new JpqlDataLoader()

        when: "closing the cursor throws (e.g. a dead connection) during teardown"
        loader.closeCursorQuietly(cursor, "q")

        then: "the close failure is swallowed so a user cancel or formatter error survives instead"
        1 * cursor.close() >> { throw new RuntimeException("dead connection") }
        noExceptionThrown()
    }
}

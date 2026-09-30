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

package dbms

import io.jmix.eclipselink.impl.dbms.JmixOraclePlatform
import org.eclipse.persistence.logging.SessionLog
import org.eclipse.persistence.mappings.converters.TypeConversionConverter
import org.eclipse.persistence.sessions.DatabaseLogin
import org.eclipse.persistence.sessions.JNDIConnector
import org.eclipse.persistence.sessions.Project
import org.eclipse.persistence.sessions.server.ServerSession
import org.springframework.jdbc.datasource.DriverManagerDataSource
import spock.lang.Shared
import spock.lang.Specification
import test_support.TestCountingDataSource

import java.sql.Clob

/**
 * Checks conversion of an empty string to CLOB by {@link JmixOraclePlatform} with the session setup used by Jmix:
 * server session over an external connection pool (Spring {@code DataSource}). The database is HSQL, as the tested
 * code only needs the JDBC {@code Connection.createClob()} method.
 */
class JmixOraclePlatformTest extends Specification {

    @Shared
    TestCountingDataSource dataSource

    @Shared
    ServerSession serverSession

    void setupSpec() {
        dataSource = new TestCountingDataSource(
                new DriverManagerDataSource('jdbc:hsqldb:mem:jmix_oracle_platform_test', 'sa', ''))

        def login = new DatabaseLogin(new JmixOraclePlatform())
        login.connector = new JNDIConnector(dataSource)
        login.useExternalConnectionPooling()

        serverSession = new ServerSession(new Project(login))
        serverSession.logLevel = SessionLog.WARNING
        serverSession.login()
    }

    void cleanupSpec() {
        serverSession?.logout()
    }

    def "test empty string is not converted to CLOB outside of transaction"() {
        // E.g. comparison of attribute values on merge of a detached entity before the database transaction begins
        def clientSession = serverSession.acquireClientSession()
        def uow = clientSession.acquireUnitOfWork()
        def connectionCount = dataSource.connectionCount

        when:
        def value = clobConverter().convertObjectValueToDataValue('', uow)

        then:
        value == ''

        and: "no connection is acquired"
        dataSource.connectionCount == connectionCount

        cleanup:
        uow.release()
        clientSession.release()
    }

    def "test empty string is converted to CLOB inside transaction"() {
        def clientSession = serverSession.acquireClientSession()
        clientSession.beginTransaction()
        // The transaction connection is acquired lazily on first use, e.g. on flush
        def transactionConnection = clientSession.accessor.connection
        def uow = clientSession.acquireUnitOfWork()
        def connectionCount = dataSource.connectionCount

        when:
        def value = clobConverter().convertObjectValueToDataValue('', uow)

        then:
        value instanceof Clob

        and: "the CLOB is created using the transaction connection, which is kept"
        dataSource.connectionCount == connectionCount
        clientSession.accessor.connection.is(transactionConnection)

        cleanup:
        uow.release()
        clientSession.rollbackTransaction()
        clientSession.release()
    }

    private static TypeConversionConverter clobConverter() {
        def converter = new TypeConversionConverter()
        converter.objectClass = String
        converter.dataClass = Clob
        return converter
    }
}

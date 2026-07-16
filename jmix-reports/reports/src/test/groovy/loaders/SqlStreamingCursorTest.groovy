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

import io.jmix.reports.yarg.exception.DataLoadingException
import io.jmix.reports.yarg.exception.ReportingInterruptedException
import io.jmix.reports.yarg.loaders.impl.SqlDataLoader
import io.jmix.reports.yarg.structure.BandData
import io.jmix.reports.yarg.structure.ReportQuery
import org.springframework.jdbc.datasource.DriverManagerDataSource
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

/**
 * SQL streaming cursor: rows are pulled from a forward-only ResultSet inside the loader-owned
 * connection, with the same alias/parameter handling as the list-based {@code loadData}.
 */
class SqlStreamingCursorTest extends Specification {

    def "streams rows from a forward-only cursor with aliases and parameters"() {
        given:
        def dataSource = new DriverManagerDataSource("jdbc:hsqldb:mem:sqlcursor", "SA", "")
        def conn = dataSource.getConnection()
        conn.createStatement().execute("create table t (id int, val varchar(16))")
        (1..50).each { conn.createStatement().execute("insert into t values ($it, 'v$it')") }
        conn.close()

        def loader = new SqlDataLoader(dataSource)
        def query = Mock(ReportQuery) {
            getScript() >> 'select id as "num", val as "text" from t where id > ${minId} order by id'
            getName() >> "q"
            getProcessTemplate() >> false
        }

        when:
        def collected = []
        def result = loader.loadDataStreaming(query, null, [minId: 10]) { rows ->
            rows.each { collected << it }
            return collected.size()
        }

        then:
        result == 40
        collected.size() == 40
        collected.first()["num"] == 11
        collected.first()["text"] == "v11"
        collected.last()["num"] == 50
    }

    def "a Groovy template error in the SQL streaming query surfaces as DataLoadingException"() {
        given:
        def loader = new SqlDataLoader(Mock(DataSource))
        def query = Mock(ReportQuery) {
            getScript() >> 'select * from t where x = ${missing.bad}'
            getName() >> "q"
            getProcessTemplate() >> true
        }

        when: "the Groovy template references an undefined binding, failing during preprocessing"
        loader.loadDataStreaming(query, null, [:]) { rows -> rows.hasNext() }

        then: "the raw Groovy error is wrapped, matching the batch path and JPQL streaming"
        thrown(DataLoadingException)
    }

    def "reporting exceptions thrown by the render callback propagate unwrapped"() {
        given:
        def dataSource = new DriverManagerDataSource("jdbc:hsqldb:mem:sqlinterrupt", "SA", "")
        def conn = dataSource.getConnection()
        conn.createStatement().execute("create table ti (id int)")
        conn.createStatement().execute("insert into ti values (1)")
        conn.close()
        def loader = new SqlDataLoader(dataSource)
        def query = Mock(ReportQuery) {
            getScript() >> 'select id as "id" from ti'
            getName() >> "q"
            getProcessTemplate() >> false
        }

        when:
        loader.loadDataStreaming(query, null, [:]) { rows ->
            throw new ReportingInterruptedException("cancelled by user")
        }

        then:
        def e = thrown(ReportingInterruptedException)
        e.message == "cancelled by user"
    }

    def "column label with regex metacharacters does not break the loader"() {
        given:
        def dataSource = new DriverManagerDataSource("jdbc:hsqldb:mem:sqlquote", "SA", "")
        def conn = dataSource.getConnection()
        conn.createStatement().execute("create table tq (id int)")
        conn.createStatement().execute("insert into tq values (1)")
        conn.createStatement().execute("insert into tq values (2)")
        conn.close()
        def loader = new SqlDataLoader(dataSource)
        def query = Mock(ReportQuery) {
            getScript() >> 'select count(*) as "COUNT(*)" from tq'
            getName() >> "q"
            getProcessTemplate() >> false
        }

        expect: "streaming path"
        loader.loadDataStreaming(query, null, [:]) { rows -> rows.next()["COUNT(*)"] } == 2

        and: "legacy path"
        loader.loadData(query, null, [:]).first()["COUNT(*)"] == 2
    }

    def "streaming resolves the data source per query (multi-store routing hook)"() {
        given: "the loader's default data source has no such table; the query's store does"
        def mainDs = new DriverManagerDataSource("jdbc:hsqldb:mem:sqlmain", "SA", "")
        def storeDs = new DriverManagerDataSource("jdbc:hsqldb:mem:sqlstore", "SA", "")
        def conn = storeDs.getConnection()
        conn.createStatement().execute("create table st (id int)")
        conn.createStatement().execute("insert into st values (7)")
        conn.close()

        def loader = new SqlDataLoader(mainDs) {
            protected DataSource resolveDataSource(ReportQuery reportQuery) {
                return storeDs
            }
        }
        def query = Mock(ReportQuery) {
            getScript() >> 'select id as "id" from st'
            getName() >> "q"
            getProcessTemplate() >> false
        }

        expect:
        loader.loadDataStreaming(query, null, [:]) { rows -> rows.next()["id"] } == 7
    }

    def "null parameters are bound with setNull, not setObject"() {
        given:
        def statement = Mock(PreparedStatement)
        def resultSetMetaData = Mock(java.sql.ResultSetMetaData) { getColumnCount() >> 0 }
        def resultSet = Mock(ResultSet) {
            next() >> false
            getMetaData() >> resultSetMetaData
        }
        def metaData = Mock(DatabaseMetaData) { getDatabaseProductName() >> "HSQL Database Engine" }
        def connection = Mock(Connection) {
            getAutoCommit() >> true
            prepareStatement(_, _, _) >> statement
            getMetaData() >> metaData
        }
        def dataSource = Mock(DataSource) { getConnection() >> connection }
        statement.executeQuery() >> resultSet

        def loader = new SqlDataLoader(dataSource)
        def parentBand = new BandData("Parent")
        parentBand.setData([pv: null])
        def query = Mock(ReportQuery) {
            getScript() >> 'select id as "id" from t where val = ${Parent.pv}'
            getName() >> "q"
            getProcessTemplate() >> false
        }

        when:
        loader.loadDataStreaming(query, parentBand, [:]) { rows -> rows.hasNext() }

        then:
        1 * statement.setNull(1, Types.VARCHAR)
        0 * statement.setObject(_, _)
    }

    def "MySQL connections stream with the driver's magic fetch size"() {
        given:
        def statement = Mock(PreparedStatement)
        def resultSetMetaData = Mock(java.sql.ResultSetMetaData) { getColumnCount() >> 0 }
        def resultSet = Mock(ResultSet) {
            next() >> false
            getMetaData() >> resultSetMetaData
        }
        def metaData = Mock(DatabaseMetaData) { getDatabaseProductName() >> productName }
        def connection = Mock(Connection) {
            getAutoCommit() >> true
            prepareStatement(_, _, _) >> statement
            getMetaData() >> metaData
        }
        def dataSource = Mock(DataSource) { getConnection() >> connection }
        statement.executeQuery() >> resultSet

        def loader = new SqlDataLoader(dataSource)
        def query = Mock(ReportQuery) {
            getScript() >> 'select id as "id" from t'
            getName() >> "q"
            getProcessTemplate() >> false
        }

        when:
        loader.loadDataStreaming(query, null, [:]) { rows -> rows.hasNext() }

        then:
        1 * statement.setFetchSize(expectedFetchSize)

        where:
        productName             | expectedFetchSize
        "MySQL"                 | Integer.MIN_VALUE
        "MariaDB"               | Integer.MIN_VALUE
        "PostgreSQL"            | 1000
        "HSQL Database Engine"  | 1000
    }

    def "cancels the running statement before closing the cursor when the render is interrupted"() {
        given:
        def statement = Mock(PreparedStatement)
        def resultSetMetaData = Mock(java.sql.ResultSetMetaData) { getColumnCount() >> 0 }
        def resultSet = Mock(ResultSet) { getMetaData() >> resultSetMetaData }
        def metaData = Mock(DatabaseMetaData) { getDatabaseProductName() >> "MySQL" }
        def connection = Mock(Connection) {
            getAutoCommit() >> true
            prepareStatement(_, _, _) >> statement
            getMetaData() >> metaData
        }
        def dataSource = Mock(DataSource) { getConnection() >> connection }
        statement.executeQuery() >> resultSet

        def loader = new SqlDataLoader(dataSource)
        def query = Mock(ReportQuery) {
            getScript() >> 'select id as "id" from t'
            getName() >> "q"
            getProcessTemplate() >> false
        }

        when: "the render callback is interrupted (throws) before consuming the whole cursor"
        loader.loadDataStreaming(query, null, [:]) { rows -> throw new ReportingInterruptedException("cancel") }

        then: "cancel() is issued BEFORE the ResultSet is closed (otherwise close drains the remaining rows)"
        1 * statement.cancel()

        then: "only after cancel is the cursor closed"
        1 * resultSet.close()

        then: "and the cancellation propagates with its type"
        thrown(ReportingInterruptedException)
    }

    def "empty result yields an empty iterator (empty-row semantics live in the feed, not the loader)"() {
        given:
        def dataSource = new DriverManagerDataSource("jdbc:hsqldb:mem:sqlcursor2", "SA", "")
        def conn = dataSource.getConnection()
        conn.createStatement().execute("create table t (id int)")
        conn.close()

        def loader = new SqlDataLoader(dataSource)
        def query = Mock(ReportQuery) {
            getScript() >> 'select id as "id" from t'
            getName() >> "q"
            getProcessTemplate() >> false
        }

        expect:
        loader.loadDataStreaming(query, null, [:]) { rows -> rows.hasNext() } == false
    }

    def "a teardown failure in finally does not mask the render exception"() {
        given:
        def statement = Mock(PreparedStatement)
        def resultSetMetaData = Mock(java.sql.ResultSetMetaData) { getColumnCount() >> 0 }
        def resultSet = Mock(ResultSet) {
            next() >> false
            getMetaData() >> resultSetMetaData
        }
        def metaData = Mock(DatabaseMetaData) { getDatabaseProductName() >> "HSQL Database Engine" }
        def connection = Mock(Connection) {
            getAutoCommit() >> true
            prepareStatement(_, _, _) >> statement
            getMetaData() >> metaData
            rollback() >> { throw new java.sql.SQLException("connection aborted") }
        }
        def dataSource = Mock(DataSource) { getConnection() >> connection }
        statement.executeQuery() >> resultSet

        def loader = new SqlDataLoader(dataSource)
        def query = Mock(ReportQuery) {
            getScript() >> 'select id as "id" from t'
            getName() >> "q"
            getProcessTemplate() >> false
        }

        when: "the render callback is cancelled while the connection is broken"
        loader.loadDataStreaming(query, null, [:]) { rows ->
            throw new ReportingInterruptedException("cancelled by user")
        }

        then: "the user-cancel keeps its type; the rollback SQLException must not replace it"
        def e = thrown(ReportingInterruptedException)
        e.message == "cancelled by user"
    }

    def "a rollback failure still restores autoCommit on the pooled connection"() {
        given:
        def statement = Mock(PreparedStatement)
        def resultSetMetaData = Mock(java.sql.ResultSetMetaData) { getColumnCount() >> 0 }
        def resultSet = Mock(ResultSet) {
            next() >> false
            getMetaData() >> resultSetMetaData
        }
        def metaData = Mock(DatabaseMetaData) { getDatabaseProductName() >> "HSQL Database Engine" }
        def connection = Mock(Connection) {
            getAutoCommit() >> true
            prepareStatement(_, _, _) >> statement
            getMetaData() >> metaData
            rollback() >> { throw new java.sql.SQLException("rollback failed") }
        }
        def dataSource = Mock(DataSource) { getConnection() >> connection }
        statement.executeQuery() >> resultSet

        def loader = new SqlDataLoader(dataSource)
        def query = Mock(ReportQuery) {
            getScript() >> 'select id as "id" from t'
            getName() >> "q"
            getProcessTemplate() >> false
        }

        when:
        loader.loadDataStreaming(query, null, [:]) { rows -> rows.hasNext() }

        then: "rollback threw, but autoCommit must still be restored so the pooled connection is not left in a transaction"
        1 * connection.setAutoCommit(true)
    }

    def "streaming setup uses a forward-only read-only cursor with autoCommit disabled and a fetch size"() {
        given:
        def statement = Mock(PreparedStatement)
        def resultSetMetaData = Mock(java.sql.ResultSetMetaData) { getColumnCount() >> 0 }
        def resultSet = Mock(ResultSet) {
            next() >> false
            getMetaData() >> resultSetMetaData
        }
        def metaData = Mock(DatabaseMetaData) { getDatabaseProductName() >> "HSQL Database Engine" }
        def connection = Mock(Connection) {
            getAutoCommit() >> true
            getMetaData() >> metaData
        }
        def dataSource = Mock(DataSource) { getConnection() >> connection }
        statement.executeQuery() >> resultSet

        def loader = new SqlDataLoader(dataSource)
        def query = Mock(ReportQuery) {
            getScript() >> 'select id as "id" from t'
            getName() >> "q"
            getProcessTemplate() >> false
        }

        when:
        loader.loadDataStreaming(query, null, [:]) { rows -> rows.hasNext() }

        then: "the JDBC cursor is configured to stream, not to buffer the whole ResultSet client-side"
        1 * connection.setAutoCommit(false)
        1 * connection.prepareStatement(_, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY) >> statement
        1 * statement.setFetchSize(_)
    }
}

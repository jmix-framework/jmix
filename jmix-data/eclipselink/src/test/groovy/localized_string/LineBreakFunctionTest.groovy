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

package localized_string

import io.jmix.eclipselink.impl.dbms.H2DbmsFeatures
import io.jmix.eclipselink.impl.dbms.HsqlDbmsFeatures
import io.jmix.eclipselink.impl.dbms.MysqlDbmsFeatures
import io.jmix.eclipselink.impl.dbms.OracleDbmsFeatures
import io.jmix.eclipselink.impl.dbms.PostgresqlDbmsFeatures
import io.jmix.eclipselink.impl.dbms.SqlServerDbmsFeatures
import spock.lang.Specification

/**
 * The name of the function that produces the line break separating the locale entries, and whether it returns a
 * binary string. Only HSQLDB is reachable from the test suite, so the per-database answers are pinned here: a wrong
 * name breaks every sort and every filter on a localized property, and on the database it breaks nothing else would
 * show it.
 */
class LineBreakFunctionTest extends Specification {

    def "the line break function is the one the database knows"() {
        expect:
        features.charFunctionName == function

        where:
        features                      || function
        new HsqlDbmsFeatures()        || 'char'
        new H2DbmsFeatures()          || 'char'
        new MysqlDbmsFeatures()       || 'char'
        new SqlServerDbmsFeatures()   || 'char'
        new PostgresqlDbmsFeatures()  || 'chr'
        new OracleDbmsFeatures()      || 'chr'
    }

    def "only the char function of MySQL returns a binary string"() {
        expect: "the expression lower-cases the column for a case-insensitive comparison on such a database"
        features.charFunctionBinary == binary

        where:
        features                      || binary
        new HsqlDbmsFeatures()        || false
        new H2DbmsFeatures()          || false
        new MysqlDbmsFeatures()       || true
        new SqlServerDbmsFeatures()   || false
        new PostgresqlDbmsFeatures()  || false
        new OracleDbmsFeatures()      || false
    }
}

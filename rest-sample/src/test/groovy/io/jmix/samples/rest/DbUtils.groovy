/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest

import groovy.sql.Sql

class DbUtils {
    static Sql getSql() {
        return Sql.newInstance('jdbc:hsqldb:mem:testdb',
                'sa', '', 'org.hsqldb.jdbc.JDBCDriver')
    }
}

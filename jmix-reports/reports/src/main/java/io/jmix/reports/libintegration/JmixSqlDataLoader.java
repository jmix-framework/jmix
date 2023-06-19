/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reports.libintegration;

import io.jmix.reports.yarg.loaders.impl.SqlDataLoader;
import io.jmix.reports.yarg.util.db.QueryRunner;
import io.jmix.reports.yarg.util.db.ResultSetHandler;
import io.jmix.reports.yarg.structure.ReportQuery;
import io.jmix.data.StoreAwareLocator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class JmixSqlDataLoader extends SqlDataLoader {

    @Autowired
    protected StoreAwareLocator storeAwareLocator;

    public JmixSqlDataLoader(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected List runQuery(ReportQuery reportQuery, String queryString, Object[] params, ResultSetHandler<List> handler) throws SQLException {
        QueryRunner runner = new QueryRunner(storeAwareLocator.getDataSource(StoreUtils.getStoreName(reportQuery)));
        return runner.query(queryString, params, handler);
    }
}

/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.reports.yarg.loaders.impl;

import io.jmix.reports.yarg.exception.DataLoadingException;
import io.jmix.reports.yarg.exception.ReportingException;
import io.jmix.reports.yarg.loaders.StreamingReportDataLoader;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportQuery;
import io.jmix.reports.yarg.util.db.QueryRunner;
import io.jmix.reports.yarg.util.db.ResultSetHandler;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads data from database using sql
 * You can use aliases for output values
 *
 * Example:
 * select login as "Login", password as "Password" from user where create_ts &gt; ${startDate}
 *
 * ${startDate} is alias of the input parameter, which will be passed to the query
 */
public class SqlDataLoader extends AbstractDbDataLoader implements StreamingReportDataLoader {

    private static final Logger log = LoggerFactory.getLogger(SqlDataLoader.class);

    private DataSource dataSource;

    /** JDBC fetch size for the streaming cursor; PostgreSQL also requires autoCommit=false to stream. */
    protected int streamingFetchSize = 1000;

    public SqlDataLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setStreamingFetchSize(int streamingFetchSize) {
        this.streamingFetchSize = streamingFetchSize;
    }

    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, @Nullable BandData parentBand, Map<String, Object> params) {
        try {
            String query = reportQuery.getScript();
            if (StringUtils.isBlank(query)) {
                return Collections.emptyList();
            }
            final List<OutputValue> outputValues = new ArrayList<>();
            if (Boolean.TRUE.equals(reportQuery.getProcessTemplate())) {
                query = processQueryTemplate(query, parentBand, params);
            }
            final QueryPack pack = prepareQuery(query, parentBand, params);

            List<Object> resultingParams = new ArrayList<>();
            QueryParameter[] queryParameters = pack.getParams();
            for (QueryParameter queryParameter : queryParameters) {
                if (queryParameter.isSingleValue()) {
                    resultingParams.add(queryParameter.getValue());
                } else {
                    resultingParams.addAll(queryParameter.getMultipleValues());
                }
            }

            List resList = runQuery(reportQuery, pack.getQuery(), resultingParams.toArray(), new ResultSetHandler<List>() {
                @Override
                public List handle(ResultSet rs) throws SQLException {
                    List<Object[]> resList = new ArrayList<>();

                    while (rs.next()) {
                        ResultSetMetaData metaData = rs.getMetaData();
                        if (outputValues.isEmpty()) {
                            outputValues.addAll(buildOutputValues(metaData, pack.getQuery()));
                        }

                        Object[] values = new Object[metaData.getColumnCount()];
                        for (int columnIndex = 0; columnIndex < metaData.getColumnCount(); columnIndex++) {
                            values[columnIndex] = convertOutputValue(rs.getObject(columnIndex + 1));
                        }
                        resList.add(values);
                    }

                    return resList;
                }
            });
            return fillOutputData(resList, outputValues);
        } catch (DataLoadingException e) {
            throw e;
        } catch (Throwable e) {
            throw new DataLoadingException(String.format("An error occurred while loading data for data set [%s]", reportQuery.getName()), e);
        }
    }

    protected List runQuery(ReportQuery reportQuery, String queryString, Object[] params, ResultSetHandler<List> handler) throws SQLException {
        QueryRunner runner = new QueryRunner(getDataSource());
        return runner.query(queryString, params, handler);
    }

    @Override
    public <T> T loadDataStreaming(ReportQuery reportQuery, @Nullable BandData parentBand,
                                   Map<String, Object> params, Function<Iterator<Map<String, Object>>, T> work) {
        String query = reportQuery.getScript();
        if (StringUtils.isBlank(query)) {
            return work.apply(Collections.emptyIterator());
        }

        // Wrap query preprocessing so a Groovy template error surfaces as DataLoadingException, the same as
        // the batch path (loadData) and the JPQL streaming path — otherwise it would escape raw from here.
        QueryPack pack;
        try {
            if (Boolean.TRUE.equals(reportQuery.getProcessTemplate())) {
                query = processQueryTemplate(query, parentBand, params);
            }
            pack = prepareQuery(query, parentBand, params);
        } catch (ReportingException e) {
            throw e;
        } catch (Throwable e) {
            throw new DataLoadingException(String.format(
                    "An error occurred while streaming data for data set [%s]", reportQuery.getName()), e);
        }

        List<Object> resultingParams = new ArrayList<>();
        for (QueryParameter queryParameter : pack.getParams()) {
            if (queryParameter.isSingleValue()) {
                resultingParams.add(queryParameter.getValue());
            } else {
                resultingParams.addAll(queryParameter.getMultipleValues());
            }
        }

        try (Connection connection = resolveDataSource(reportQuery).getConnection()) {
            // Set read-only before autoCommit(false) so it is applied outside a transaction (PostgreSQL
            // requires that). The whole streaming read only selects, so a read-only connection lets the
            // database take a lighter path on this render-long connection. Restored in the finally block.
            boolean previousReadOnly = connection.isReadOnly();
            connection.setReadOnly(true);
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(pack.getQuery(),
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                statement.setFetchSize(resolveStreamingFetchSize(connection));

                for (int i = 0; i < resultingParams.size(); i++) {
                    Object value = resultingParams.get(i);
                    if (value == null) {
                        // VARCHAR works with most drivers regardless of the actual column type;
                        // Oracle rejects setObject(i, null) (mirrors QueryRunner.fillStatement).
                        statement.setNull(i + 1, Types.VARCHAR);
                    } else {
                        statement.setObject(i + 1, value);
                    }
                }
                // The ResultSet is deliberately NOT a try-with-resources: on cancellation cancel() must run
                // BEFORE the close. On MySQL in row-streaming mode (Integer.MIN_VALUE) closing an unexhausted
                // streaming ResultSet makes Connector/J read and discard every remaining row to free the
                // wire, hanging a cancel for minutes while holding the pooled connection; cancel() aborts the
                // query first so the close is cheap. A try-with-resources would close the ResultSet before
                // the catch runs, so cancel() would arrive after the draining close and be useless.
                ResultSet resultSet = statement.executeQuery();
                //noinspection TryFinallyCanBeTryWithResources
                try {
                    return work.apply(new SqlRowIterator(resultSet, pack.getQuery()));
                } catch (Throwable e) {
                    try {
                        statement.cancel();
                    } catch (SQLException cancelError) {
                        log.warn("Failed to cancel the streaming statement after data set [{}]",
                                reportQuery.getName(), cancelError);
                    }
                    throw e;
                } finally {
                    try {
                        resultSet.close();
                    } catch (SQLException closeError) {
                        log.warn("Failed to close the streaming result set after data set [{}]",
                                reportQuery.getName(), closeError);
                    }
                }

            } finally {
                // Never let a teardown failure replace the in-flight exception: a broken connection can
                // make rollback()/setAutoCommit() throw, which would mask a user cancel or formatter error
                // (and strip its ReportingException type that runReport routes on). Roll back and restore
                // autoCommit independently, so a failed rollback still restores autoCommit — otherwise the
                // connection returns to the pool with autoCommit=false and corrupts a later borrower.
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    log.warn("Failed to roll back the streaming connection after data set [{}]",
                            reportQuery.getName(), e);
                }
                try {
                    connection.setAutoCommit(previousAutoCommit);
                } catch (SQLException e) {
                    log.warn("Failed to restore autoCommit on the streaming connection after data set [{}]",
                            reportQuery.getName(), e);
                }
                try {
                    // Restore after autoCommit is back (transaction ended by the rollback above), so the
                    // read-only flag is toggled outside a transaction — otherwise the pooled connection
                    // would return read-only and break a later borrower that needs to write.
                    connection.setReadOnly(previousReadOnly);
                } catch (SQLException e) {
                    log.warn("Failed to restore the read-only flag on the streaming connection after data set [{}]",
                            reportQuery.getName(), e);
                }
            }
        } catch (ReportingException e) {
            // DataLoadingException, ReportingInterruptedException (user cancel) and formatter errors
            // must keep their type: runReport routes them differently from data loading failures.
            throw e;
        } catch (Throwable e) {
            throw new DataLoadingException(
                    String.format("An error occurred while streaming data for data set [%s]", reportQuery.getName()), e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * The data source the streaming cursor connects to. The default is the constructor-injected one;
     * subclasses may route by the dataset's data store (mirrors {@code runQuery} overrides).
     */
    protected DataSource resolveDataSource(ReportQuery reportQuery) {
        return dataSource;
    }

    /**
     * MySQL Connector/J ignores positive fetch sizes and buffers the whole result set client-side;
     * {@code Integer.MIN_VALUE} switches it (and MariaDB Connector/J) to row streaming.
     */
    protected int resolveStreamingFetchSize(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        if (productName != null) {
            String lower = productName.toLowerCase();
            // MariaDB Connector/J 3.x reports "MariaDB" (2.x reported "MySQL"); both ignore a positive
            // fetch size and buffer the whole result set unless switched to row streaming.
            if (lower.contains("mysql") || lower.contains("mariadb")) {
                return Integer.MIN_VALUE;
            }
        }
        return streamingFetchSize;
    }

    /**
     * Builds the output-value descriptors from the result-set metadata, capturing each column's
     * case-sensitive {@code as <synonym>} alias from the query. Shared by the batch loader and the
     * streaming cursor so the synonym semantics stay in one place.
     */
    protected List<OutputValue> buildOutputValues(ResultSetMetaData metaData, String query) throws SQLException {
        List<OutputValue> outputValues = new ArrayList<>();
        for (int columnIndex = 1; columnIndex <= metaData.getColumnCount(); columnIndex++) {
            String columnName = metaData.getColumnLabel(columnIndex);
            OutputValue outputValue = new OutputValue(columnName);
            Matcher matcher = Pattern.compile("(?i)as\\s*(" + Pattern.quote(columnName) + ")").matcher(query);
            if (matcher.find()) {
                outputValue.setSynonym(matcher.group(1));
            }
            outputValues.add(outputValue);
        }
        return outputValues;
    }

    /** Forward-only row iterator; output columns are captured from metadata once, at construction. */
    protected class SqlRowIterator implements Iterator<Map<String, Object>> {

        protected final ResultSet resultSet;
        protected final List<OutputValue> outputValues;
        protected final int columnCount;
        protected Boolean hasNextCached;

        protected SqlRowIterator(ResultSet resultSet, String query) {
            this.resultSet = resultSet;
            try {
                ResultSetMetaData metaData = resultSet.getMetaData();
                this.columnCount = metaData.getColumnCount();
                this.outputValues = buildOutputValues(metaData, query);
            } catch (SQLException e) {
                throw new DataLoadingException("An error occurred while reading streaming cursor metadata", e);
            }
        }

        @Override
        public boolean hasNext() {
            if (hasNextCached == null) {
                try {
                    hasNextCached = resultSet.next();
                } catch (SQLException e) {
                    throw new DataLoadingException("An error occurred while advancing the streaming cursor", e);
                }
            }
            return hasNextCached;
        }

        @Override
        public Map<String, Object> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            hasNextCached = null;
            try {
                Object[] values = new Object[columnCount];
                for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                    values[columnIndex] = convertOutputValue(resultSet.getObject(columnIndex + 1));
                }
                return fillOutputRow(values, outputValues);
            } catch (SQLException e) {
                throw new DataLoadingException("An error occurred while reading the streaming cursor row", e);
            }
        }
    }
}
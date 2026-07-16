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

import io.jmix.reports.yarg.exception.DataLoadingException;
import io.jmix.reports.yarg.exception.ReportingException;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.loaders.StreamingReportDataLoader;
import io.jmix.reports.yarg.loaders.impl.AbstractDbDataLoader;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportQuery;
import io.jmix.core.*;
import io.jmix.data.StoreAwareLocator;
import io.jmix.reports.app.EntityMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.jpa.JpaQuery;
import org.eclipse.persistence.queries.CursoredStream;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NullMarked
public class JpqlDataLoader extends AbstractDbDataLoader implements ReportDataLoader, StreamingReportDataLoader {

    private static final Logger log = LoggerFactory.getLogger(JpqlDataLoader.class);

    /** Cached JDBC streaming fetch size per data store — the database product does not change at runtime. */
    protected final Map<String, Integer> streamingFetchSizeByStore = new ConcurrentHashMap<>();

    @Autowired
    protected BeanFactory beanFactory;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected StoreAwareLocator storeAwareLocator;

    @Autowired
    protected ReportsGroovyFeatureSupport groovyFeatureSupport;

    /**
     * JDBC fetch size for the streaming cursor.
     */
    protected int streamingFetchSize = 1000;
    /**
     * Release EclipseLink's internal row references every N rows.
     */
    protected int cursorClearInterval = 1000;

    private static final String QUERY_END = "%%END%%";
    private static final String ALIAS_PATTERN = "as\\s+\"?([\\w|\\d|_|\\.]+)\"?\\s*";
    private static final Pattern OUTPUT_PARAMS_PATTERN =
            Pattern.compile("(?i)" + ALIAS_PATTERN + "[,|from|" + QUERY_END + "]", Pattern.CASE_INSENSITIVE);


    public void setStreamingFetchSize(int streamingFetchSize) {
        this.streamingFetchSize = streamingFetchSize;
    }

    public void setCursorClearInterval(int cursorClearInterval) {
        this.cursorClearInterval = cursorClearInterval;
    }

    protected List<OutputValue> parseQueryOutputParametersNames(String query) {
        List<OutputValue> result = new ArrayList<>();
        final Matcher matcher = OUTPUT_PARAMS_PATTERN.matcher(trimQuery(query) + QUERY_END);

        while (matcher.find()) {
            String group = matcher.group(matcher.groupCount());
            if (group != null)
                result.add(new OutputValue(group.trim()));
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, @Nullable BandData parentBand, Map<String, Object> params) {
        String storeName = StoreUtils.getStoreName(reportQuery);
        String query = reportQuery.getScript();
        if (StringUtils.isBlank(query)) {
            return Collections.emptyList();
        }
        try {
            PreparedJpqlQuery prepared = prepareJpqlQuery(query, parentBand, params,
                    Boolean.TRUE.equals(reportQuery.getProcessTemplate()));

            List queryResult = executeQuery(parentBand, params, storeName, prepared.query());
            if (CollectionUtils.isNotEmpty(queryResult) && queryResult.get(0) instanceof Entity) {
                List<Map<String, Object>> wrappedResults = new ArrayList<>();
                for (Object theResult : queryResult) {
                    wrappedResults.add(new EntityMap((Entity) theResult, beanFactory));
                }
                return wrappedResults;
            } else {
                return fillOutputData(queryResult, prepared.outputValues());
            }
        } catch (Throwable e) {
            throw new DataLoadingException(String.format("An error occurred while loading data for data set [%s]", reportQuery.getName()), e);
        }
    }

    /**
     * Strips {@code as "alias"} entries from the JPQL text, capturing them as output value names.
     */
    protected PreparedJpqlQuery prepareJpqlQuery(String query, @Nullable BandData parentBand,
                                                 Map<String, Object> params, boolean processTemplate) {
        if (processTemplate) {
            query = processQueryTemplate(query, parentBand, params);
        }
        List<OutputValue> outputValues = parseQueryOutputParametersNames(query);
        query = query.replaceAll("(?i)" + ALIAS_PATTERN + ",", ",");//replaces [as alias_name], entries except last
        query = query.replaceAll("(?i)" + ALIAS_PATTERN, " ");//replaces last [as alias_name] entry
        return new PreparedJpqlQuery(query, outputValues);
    }

    /**
     * Streams rows from an EclipseLink cursor. The whole callback runs inside the store's transaction,
     * keeping the cursor and lazy {@code EntityMap} attribute access valid for the entire render.
     */
    @Override
    public <T> T loadDataStreaming(ReportQuery reportQuery, @Nullable BandData parentBand,
                                   Map<String, Object> params, Function<Iterator<Map<String, Object>>, T> work) {
        String storeName = StoreUtils.getStoreName(reportQuery);
        String rawQuery = reportQuery.getScript();
        if (StringUtils.isBlank(rawQuery)) {
            return work.apply(Collections.emptyIterator());
        }

        try {
            PreparedJpqlQuery prepared = prepareJpqlQuery(rawQuery, parentBand, params,
                    Boolean.TRUE.equals(reportQuery.getProcessTemplate()));
            List<OutputValue> outputParameters = prepared.outputValues();
            String finalQuery = prepared.query();

            // A streaming report holds this cursor open for the whole render and periodically calls
            // entityManager.clear(). Run it in a dedicated (REQUIRES_NEW) transaction so that clear() never
            // detaches entities from a caller's ambient transaction — e.g. when the report is launched from
            // inside a @Transactional service method, whose persistence context must not be wiped.
            TransactionTemplate streamingTransaction =
                    new TransactionTemplate(storeAwareLocator.getTransactionManager(storeName));
            streamingTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            // Resolve the JDBC fetch size (a metadata lookup that borrows a pooled connection) BEFORE the
            // streaming transaction opens, so it does not contend for a second connection while the cursor's
            // REQUIRES_NEW transaction already holds one.
            int jdbcFetchSize = resolveStreamingFetchSize(storeName);

            return streamingTransaction.execute(transactionStatus -> {
                Query select = insertParameters(trimQuery(finalQuery), storeName, parentBand, params);
                // Configure the cursor on the native EclipseLink query: hints set on the JPA wrapper
                // are not propagated, and getSingleResult() enforces exactly-one-result semantics.
                JpaQuery<?> jpaQuery = select.unwrap(JpaQuery.class);

                ReadAllQuery readAllQuery = (ReadAllQuery) jpaQuery.getDatabaseQuery();
                // useCursoredStream() sizes EclipseLink's own forward-only cursor reads (keep it positive);
                // setFetchSize() maps to the JDBC statement fetch size, which must be Integer.MIN_VALUE on
                // MySQL/MariaDB to switch the driver into row streaming instead of buffering the whole
                // result set client-side (same handling as SqlDataLoader).
                readAllQuery.useCursoredStream(streamingFetchSize, streamingFetchSize);
                readAllQuery.setFetchSize(jdbcFetchSize);

                // Obtain the EntityManager before opening the cursor: if this throws, no cursor is leaked.
                EntityManager entityManager = storeAwareLocator.getEntityManager(storeName);
                CursoredStream cursor = (CursoredStream) jpaQuery.getResultCursor();
                try {
                    return work.apply(new JpqlRowIterator(cursor, entityManager, outputParameters));
                } finally {
                    // Never let a cursor-close failure replace the in-flight exception (a user cancel or a
                    // formatter error whose ReportingException type runReport routes on): a broken connection
                    // can make close() throw, which would otherwise mask the original and get repackaged as
                    // DataLoadingException by the catch below (parity with the SqlDataLoader teardown).
                    closeCursorQuietly(cursor, reportQuery.getName());
                }
            });
        } catch (ReportingException e) {
            // DataLoadingException, ReportingInterruptedException (user cancel) and formatter errors
            // must keep their type: runReport routes them differently from data loading failures.
            throw e;
        } catch (Throwable e) {
            throw new DataLoadingException(
                    String.format("An error occurred while streaming data for data set [%s]", reportQuery.getName()), e);
        }
    }

    /**
     * Cached per store (the database product is stable, so the metadata connection runs once per store).
     * A transient detection failure is NOT cached: it falls back to the default for this call but lets the
     * next report retry, so a one-off failure (e.g. a pool spike) does not pin MySQL to a buffering fetch
     * size forever.
     */
    protected int resolveStreamingFetchSize(String storeName) {
        Integer cached = streamingFetchSizeByStore.get(storeName);
        if (cached != null) {
            return cached;
        }
        Integer detected = detectStreamingFetchSize(storeName);
        if (detected == null) {
            return streamingFetchSize;
        }
        streamingFetchSizeByStore.put(storeName, detected);
        return detected;
    }

    /**
     * MySQL Connector/J ignores a positive fetch size and buffers the whole result set client-side;
     * {@code Integer.MIN_VALUE} switches it (and MariaDB Connector/J) to row streaming. The EclipseLink
     * cursor consumes rows forward-only, so the driver's row-streaming mode fits it. Mirrors
     * {@code SqlDataLoader#resolveStreamingFetchSize}. Returns {@code null} on a transient failure so the
     * caller can fall back without caching.
     */
    protected @Nullable Integer detectStreamingFetchSize(String storeName) {
        DataSource dataSource = storeAwareLocator.getDataSource(storeName);
        try (Connection connection = dataSource.getConnection()) {
            String productName = connection.getMetaData().getDatabaseProductName();
            if (productName != null) {
                String lower = productName.toLowerCase(Locale.ROOT);
                if (lower.contains("mysql") || lower.contains("mariadb")) {
                    return Integer.MIN_VALUE;
                }
            }
            return streamingFetchSize;
        } catch (SQLException e) {
            log.warn("Could not resolve the database product for the JPQL streaming fetch size of store [{}]; "
                    + "using the default fetch size", storeName, e);
            return null;
        }
    }

    /**
     * Closes the EclipseLink cursor without letting a close failure escape: on a broken connection close()
     * can throw, and in a {@code finally} that would replace the in-flight exception (masking a user cancel
     * or formatter error and stripping the ReportingException type runReport routes on). Mirrors the
     * SqlDataLoader teardown, which guards rollback/setAutoCommit the same way.
     */
    protected void closeCursorQuietly(CursoredStream cursor, String dataSetName) {
        try {
            cursor.close();
        } catch (RuntimeException e) {
            log.warn("Failed to close the streaming cursor after data set [{}]", dataSetName, e);
        }
    }

    @Nullable
    protected List executeQuery(BandData parentBand, Map<String, Object> params, String storeName, String query) {
        return storeAwareLocator.getTransactionTemplate(storeName).execute(transactionStatus -> {
            Query select = insertParameters(trimQuery(query), storeName, parentBand, params);
            return select.getResultList();
        });
    }

    protected Query insertParameters(String query, String storeName, BandData parentBand, Map<String, Object> params) {
        QueryPack pack = prepareQuery(query, parentBand, params);

        boolean inserted = pack.getParams().length > 0;
        EntityManager entityManager = storeAwareLocator.getEntityManager(storeName);
        Query select = entityManager.createQuery(pack.getQuery());
        if (inserted) {
            //insert parameters to their position
            for (QueryParameter queryParameter : pack.getParams()) {
                Object value = queryParameter.getValue();
                select.setParameter(resolveNamedParameterName(queryParameter), convertParameter(value));
            }
        }
        return select;
    }

    @Override
    protected String insertParameterToQuery(String query, QueryParameter parameter) {
        query = query.replaceFirst(parameter.getParamRegexp(), ":" + resolveNamedParameterName(parameter));
        return query;
    }

    protected String resolveNamedParameterName(QueryParameter parameter) {
        //Just transform positional parameters into named - the simplest solution to the problem of mixing
        // input parameters and JPQL macros without modification of YARG (#805).
        return "param_" + parameter.getPosition();
    }

    @Override
    protected String processQueryTemplate(String query, @Nullable BandData parentBand, Map<String, Object> reportParams) {
        if (!groovyFeatureSupport.isGroovyEnabled()) {
            return groovyFeatureSupport.getDisabledQueryTemplateResult("jpql", query);
        }
        return super.processQueryTemplate(query, parentBand, reportParams);
    }

    protected String trimQuery(String query) {
        if (query.endsWith(";")) {
            return query.substring(0, query.length() - 1);
        } else {
            return query;
        }
    }

    protected record PreparedJpqlQuery(String query, List<OutputValue> outputValues) {
    }

    /**
     * Lazy iterator over an EclipseLink CursoredStream; wraps entities into EntityMap, scalars via OutputValue.
     */
    protected class JpqlRowIterator implements Iterator<Map<String, Object>> {

        protected final CursoredStream cursor;
        protected final EntityManager entityManager;
        protected final List<OutputValue> outputParameters;
        protected long rowsRead = 0;
        /**
         * Entity-vs-scalar shape, decided once from the first row like the batch {@link #loadData}.
         */
        @Nullable
        protected Boolean entityRows;

        protected JpqlRowIterator(CursoredStream cursor, EntityManager entityManager,
                                  List<OutputValue> outputParameters) {
            this.cursor = cursor;
            this.entityManager = entityManager;
            this.outputParameters = outputParameters;
        }

        @Override
        public boolean hasNext() {
            return cursor.hasMoreElements();
        }

        @Override
        public Map<String, Object> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            if (rowsRead > 0 && rowsRead % cursorClearInterval == 0) {
                // Release rows consumed so far BEFORE reading the next one, so the row just returned
                // to the caller is never detached mid-render: cursor.clear() drops the stream's own
                // buffer, entityManager.clear() evicts the managed clones from the persistence context
                // (otherwise the UnitOfWork grows O(rows) and defeats streaming).
                cursor.clear();
                entityManager.clear();
            }

            Object element = cursor.nextElement();
            rowsRead++;
            // Decide the row shape once, from the first row, exactly like the batch loadData (which keys
            // off queryResult.get(0)). Deciding per row would make a nullable entity projection mix
            // EntityMap and scalar rows in the same result, so the template fields resolve inconsistently.
            if (entityRows == null) {
                entityRows = element instanceof Entity;
            }

            if (entityRows) {
                return new EntityMap((Entity) element, beanFactory);
            }

            return fillOutputRow(element, outputParameters);
        }
    }
}

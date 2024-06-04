/*
 * Copyright 2019 Haulmont.
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

package io.jmix.data.impl;

import io.jmix.core.*;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.data.*;
import io.jmix.data.persistence.DbTypeConverter;
import io.jmix.data.persistence.DbmsSpecifics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Supports functionality that allows queries from previously selected results.
 */
@Component("data_QueryResultsManager")
public class QueryResultsManagerImpl implements QueryResultsManager {

    private final Logger log = LoggerFactory.getLogger(QueryResultsManagerImpl.class);

    @Autowired
    protected DbmsSpecifics dbmsSpecifics;

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Autowired
    protected Metadata metadata;

    @Autowired
    private MetadataTools metadataTools;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;

    @Autowired
    protected ObjectProvider<JpqlQueryBuilder> jpqlQueryBuilderProvider;

    protected JdbcTemplate jdbcTemplate;

    protected TransactionTemplate transaction;

    protected static final int BATCH_SIZE = 100;

    protected static final int DELETE_BATCH_SIZE = 100;

    protected static final int INACTIVE_DELETION_MAX = 100000;

    @Autowired
    protected void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Autowired
    protected void setTransactionManager(PlatformTransactionManager transactionManager) {
        transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void savePreviousQueryResults(LoadContext loadContext) {
        @SuppressWarnings("unchecked") List<LoadContext.Query> prevQueries = loadContext.getPreviousQueries();
        if (prevQueries.isEmpty())
            return;

        LoadContext.Query contextQuery = prevQueries.get(prevQueries.size() - 1);
        String entityName = loadContext.getEntityMetaClass().getName();

        QueryParser parser = queryTransformerFactory.parser(contextQuery.getQueryString());
        if (!parser.isEntitySelect(entityName))
            return;

        int queryKey = loadContext.getQueryKey();

        if (resultsAlreadySaved(queryKey, contextQuery))
            return;

        List idList = transaction.execute(status -> {
            entityManager.setProperty(PersistenceHints.SOFT_DELETION,
                    loadContext.getHints().get(PersistenceHints.SOFT_DELETION));

            QueryTransformer transformer = queryTransformerFactory.transformer(contextQuery.getQueryString());
            String primaryKeyName = metadataTools.getPrimaryKeyName(metadata.getClass(entityName));
            if (primaryKeyName == null) {
                throw new IllegalStateException("Cannot find primarykey name for " + entityName);
            }
            transformer.replaceWithSelectId(primaryKeyName);
            transformer.removeOrderBy();
            String queryString = transformer.getResult();

            JpqlQueryBuilder queryBuilder = jpqlQueryBuilderProvider.getObject();
            queryBuilder.setQueryString(queryString)
                    .setEntityName(entityName)
                    .setCondition(contextQuery.getCondition())
                    .setDistinct(contextQuery.isDistinct())
                    .setSort(contextQuery.getSort())
                    .setQueryParameters(contextQuery.getParameters());

            if (prevQueries.size() > 1) {
                //todo MG
//                queryBuilder.setPreviousResults(userSessionSource.getUserSession().getId(), loadContext.getQueryKey());
            }

            Query query = queryBuilder.getQuery(entityManager);

            String logMsg = "Load previous query results: " + JpqlQueryBuilder.printQuery(((JmixQuery) query).getQueryString());
            log.debug(logMsg);
            long start = System.currentTimeMillis();

            List resultList = query.getResultList();
            log.debug("Done in " + (System.currentTimeMillis() - start) + "ms : " + logMsg);
            return resultList;
        });
        assert idList != null;

        delete(queryKey);
        insert(queryKey, idList);
    }

    protected boolean resultsAlreadySaved(Integer queryKey, LoadContext.Query query) {
        //todo MG
//        LinkedHashMap<Integer, QueryHolder> recentQueries =
//                userSessionSource.getUserSession().getAttribute("_recentQueries");
        LinkedHashMap<Integer, QueryHolder> recentQueries = null;
        if (recentQueries == null) {
            recentQueries = new LinkedHashMap<Integer, QueryHolder>() {
                private static final long serialVersionUID = -901296839279897248L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<Integer, QueryHolder> eldest) {
                    return size() > 10;
                }
            };
        }

        QueryHolder queryHolder = new QueryHolder(query);
        QueryHolder oldQueryHolder = recentQueries.put(queryKey, queryHolder);

        // do not set to session attribute recentQueries directly, it contains reference to QueryResultsManager class
        // copy data to new LinkedHashMap
        //todo MG
//        userSessionSource.getUserSession().setAttribute("_recentQueries", new LinkedHashMap<>(recentQueries));

        return queryHolder.equals(oldQueryHolder);
    }

    @Override
    public void insert(int queryKey, List idList) {
        if (idList.isEmpty())
            return;

        //todo MG
//        UUID userSessionId = userSessionSource.getUserSession().getId();
        UUID userSessionId = UUID.randomUUID();
        long start = System.currentTimeMillis();
        String logMsg = "Insert " + idList.size() + " query results for " + userSessionId + " / " + queryKey;
        log.debug(logMsg);

        transaction.executeWithoutResult(transactionStatus -> {
            DbTypeConverter converter = dbmsSpecifics.getDbTypeConverter();
            Object idFromList = idList.get(0);
            String columnName = null;
            if (idFromList instanceof String) {
                columnName = "STRING_ENTITY_ID";
            } else if (idFromList instanceof Long) {
                columnName = "LONG_ENTITY_ID";
            } else if (idFromList instanceof Integer) {
                columnName = "INT_ENTITY_ID";
            } else {
                columnName = "ENTITY_ID";
            }

            String userSessionIdStr = converter.getSqlObject(userSessionId).toString(); // assuming that UUID can be passed to query as string in all databases
            String sql = String.format("insert into SYS_QUERY_RESULT (SESSION_ID, QUERY_KEY, %s) values ('%s', %s, ?)",
                    columnName, userSessionIdStr, queryKey);
            int[] paramTypes = new int[]{converter.getSqlType(idFromList.getClass())};
            for (int i = 0; i < idList.size(); i += BATCH_SIZE) {
                @SuppressWarnings("unchecked")
                List<UUID> sublist = idList.subList(i, Math.min(i + BATCH_SIZE, idList.size()));
                List<Object[]> params = new ArrayList<>(sublist.size());
                for (int j = 0; j < sublist.size(); j++) {
                    Object[] row = new Object[1];
                    row[0] = sublist.get(j);
                    params.add(row);
                }
                jdbcTemplate.batchUpdate(sql, params, paramTypes);
            }
            log.debug("Done in " + (System.currentTimeMillis() - start) + "ms: " + logMsg);
        });
    }

    @Override
    public void delete(int queryKey) {
        DbTypeConverter converter = dbmsSpecifics.getDbTypeConverter();
        //todo MG
//        UUID userSessionId = userSessionSource.getUserSession().getId();
        UUID userSessionId = UUID.randomUUID();
        String userSessionIdStr = converter.getSqlObject(userSessionId).toString();
        long start = System.currentTimeMillis();
        String logMsg = "Delete query results for " + userSessionId + " / " + queryKey;
        log.debug(logMsg);

        String sql = "delete from SYS_QUERY_RESULT where SESSION_ID = '"
                + userSessionIdStr + "' and QUERY_KEY = " + queryKey;

        jdbcTemplate.update(sql);

        log.debug("Done in " + (System.currentTimeMillis() - start) + "ms : " + logMsg);
    }

    @Override
    public void deleteForCurrentSession() {
        DbTypeConverter converter = dbmsSpecifics.getDbTypeConverter();
        //todo MG
//        UUID userSessionId = userSessionSource.getUserSession().getId();
        UUID userSessionId = UUID.randomUUID();
        String userSessionIdStr = converter.getSqlObject(userSessionId).toString();
        jdbcTemplate.update("delete from SYS_QUERY_RESULT where SESSION_ID = '"
                + userSessionIdStr + "'");
    }

    @Override
    public void deleteForInactiveSessions() {
        internalDeleteForInactiveSessions();
    }

    public void internalDeleteForInactiveSessions() {
        log.debug("Delete query results for inactive user sessions");

        List<Object[]> rows = transaction.execute(status -> {
            TypedQuery<Object[]> query = entityManager.createQuery(
                    "select e.id, e.sessionId from sys$QueryResult e", Object[].class);
            query.setMaxResults(INACTIVE_DELETION_MAX);
            return query.getResultList();
        });
        assert rows != null;
        if (rows.size() == INACTIVE_DELETION_MAX) {
            log.debug("Processing " + INACTIVE_DELETION_MAX + " records, run again for the rest");
        }

        //todo MG
//        Set<UUID> sessionIds = userSessions.getUserSessionsStream().map(UserSession::getId).collect(Collectors.toSet());
        Set<UUID> sessionIds = new HashSet<>();

        List<Long> ids = new ArrayList<>();
        int i = 0;
        for (Object[] row : rows) {
            if (!sessionIds.contains((UUID) row[1])) {
                ids.add((Long) row[0]);
            }
            i++;
            if (i % DELETE_BATCH_SIZE == 0) {
                if (!ids.isEmpty())
                    delete(ids);
                ids.clear();
            }
        }
        if (!ids.isEmpty())
            delete(ids);
    }

    protected void delete(List<Long> ids) {
        log.debug("Deleting " + ids.size() + " records");
        String str = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        try {
            jdbcTemplate.update("delete from SYS_QUERY_RESULT where ID in (" + str + ")");
        } catch (DataAccessException e) {
            throw new RuntimeException("Error deleting query result records", e);
        }
    }
}

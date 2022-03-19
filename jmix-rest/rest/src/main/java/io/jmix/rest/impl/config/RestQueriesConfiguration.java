/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.rest.impl.config;

import com.google.common.base.Strings;
import io.jmix.core.FetchPlan;
import io.jmix.core.Resources;
import io.jmix.core.common.util.Dom4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringTokenizer;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Class is used for loading and storing of predefined JPQL queries that are used by the REST API. Queries are loaded
 * from configuration files defined by the {@code jmix.rest.queriesConfig} application property.
 * <p>
 * Queries with the name defined by the {@link #ALL_ENTITIES_QUERY_NAME} field should not be present in the queries
 * config. If the query with this name is requested, the {@link QueryInfo} for the query that returns all entities will
 * be returned.
 */
@Component("rest_RestQueriesConfiguration")
public class RestQueriesConfiguration {

    protected static final String JMIX_REST_QUERIES_CONFIG_PROP_NAME = "jmix.rest.queries-config";

    private final Logger log = LoggerFactory.getLogger(RestQueriesConfiguration.class);

    protected volatile boolean initialized;

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    @Autowired
    protected Resources resources;

    @Autowired
    protected Environment environment;

    protected List<QueryInfo> queries = new ArrayList<>();

    public static final String ALL_ENTITIES_QUERY_NAME = "all";

    /**
     * Returns a query description with the given name for the given entity.
     *
     * @param entityName entity name
     * @param queryName  query name
     * @return query description
     */
    @Nullable
    public QueryInfo getQuery(String entityName, String queryName) {
        lock.readLock().lock();
        try {
            checkInitialized();
            if (ALL_ENTITIES_QUERY_NAME.equalsIgnoreCase(queryName)) {
                return createAllEntitiesQuery(entityName);
            }
            for (QueryInfo query : queries) {
                if (queryName.equals(query.getName()) && entityName.equals(query.getEntityName())) {
                    return query;
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<QueryInfo> getQueries() {
        lock.readLock().lock();
        try {
            checkInitialized();
            return queries;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<QueryInfo> getQueries(String entityName) {
        lock.readLock().lock();
        try {
            checkInitialized();
            return queries.stream()
                    .filter(queryInfo -> entityName.equals(queryInfo.getEntityName()))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    protected void checkInitialized() {
        if (!initialized) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (!initialized) {
                    init();
                    initialized = true;
                }
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        }
    }

    protected void init() {
        String configName = environment.getProperty(JMIX_REST_QUERIES_CONFIG_PROP_NAME);
        StringTokenizer tokenizer = new StringTokenizer(configName);
        for (String location : tokenizer.getTokenArray()) {
            Resource resource = resources.getResource(location);
            if (resource.exists()) {
                InputStream stream = null;
                try {
                    stream = resource.getInputStream();
                    loadConfig(Dom4j.readDocument(stream).getRootElement());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    IOUtils.closeQuietly(stream);
                }
            } else {
                log.warn("Resource {} not found, ignore it", location);
            }
        }
    }

    protected void loadConfig(Element rootElem) {
        for (Element queryElem : rootElem.elements("query")) {
            String queryName = queryElem.attributeValue("name");
            if (ALL_ENTITIES_QUERY_NAME.equalsIgnoreCase(queryName)) {
                log.error("{} is a predefined query name. It can not be used.", queryName);
                continue;
            }
            String entityName = queryElem.attributeValue("entity");
            String fetchPlanName = queryElem.attributeValue("fetchPlan");
            String cacheable = queryElem.attributeValue("cacheable");
            String anonymousAllowed = queryElem.attributeValue("anonymousAllowed");
            String jpql = queryElem.elementText("jpql");
            String limit = queryElem.attributeValue("limit");
            String offset = queryElem.attributeValue("offset");

            if (Strings.isNullOrEmpty(queryName)) {
                log.error("queryName attribute is not defined");
                continue;
            }
            if (Strings.isNullOrEmpty(entityName)) {
                log.error("entityName attribute is not defined");
                continue;
            }
            if (Strings.isNullOrEmpty(fetchPlanName)) {
                log.error("fetchPlanName attribute is not defined");
                continue;
            }
            if (Strings.isNullOrEmpty(jpql)) {
                log.error("Query jpql is not defined");
                continue;
            }

            QueryInfo queryInfo = new QueryInfo();
            queryInfo.setName(queryName);
            queryInfo.setEntityName(entityName);
            queryInfo.setViewName(fetchPlanName);
            queryInfo.setFetchPlanName(fetchPlanName);
            queryInfo.setJpql(jpql);
            queryInfo.setCacheable("true".equals(cacheable));
            queryInfo.setAnonymousAllowed("true".equals(anonymousAllowed));
            if (StringUtils.isNotEmpty(offset)) {
                queryInfo.setOffset(Integer.valueOf(offset));
            }
            if (StringUtils.isNotEmpty(limit)) {
                queryInfo.setLimit(Integer.valueOf(limit));
            }

            Element paramsEl = queryElem.element("params");
            if (paramsEl != null) {
                for (Element paramElem : paramsEl.elements("param")) {
                    String paramName = paramElem.attributeValue("name");
                    String paramType = paramElem.attributeValue("type");
                    QueryParamInfo param = new QueryParamInfo(paramName, paramType);
                    queryInfo.getParams().add(param);
                }
            }

            queries.add(queryInfo);
        }
    }

    protected QueryInfo createAllEntitiesQuery(String entityName) {
        QueryInfo queryInfo = new QueryInfo();
        queryInfo.setName(ALL_ENTITIES_QUERY_NAME);
        queryInfo.setEntityName(entityName);
        queryInfo.setViewName(FetchPlan.INSTANCE_NAME);
        queryInfo.setJpql(String.format("select e from %s e", entityName));
        return queryInfo;
    }

    /**
     * Class stores an information about the predefined JPQL query
     */
    public static class QueryInfo {

        protected String name;
        protected String jpql;
        protected String entityName;
        protected String viewName;
        protected String fetchPlanName;
        protected Integer limit;
        protected Integer offset;
        protected boolean cacheable;
        protected boolean anonymousAllowed;
        protected List<QueryParamInfo> params = new ArrayList<>();

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public String getFetchPlanName() {
            return fetchPlanName;
        }

        public void setFetchPlanName(String fetchPlanName) {
            this.fetchPlanName = fetchPlanName;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getJpql() {
            return jpql;
        }

        public void setJpql(String jpql) {
            this.jpql = jpql;
        }

        public String getEntityName() {
            return entityName;
        }

        public void setEntityName(String entityName) {
            this.entityName = entityName;
        }

        public String getViewName() {
            return viewName;
        }

        public void setViewName(String viewName) {
            this.viewName = viewName;
        }

        public boolean isCacheable() {
            return cacheable;
        }

        public void setCacheable(boolean cacheable) {
            this.cacheable = cacheable;
        }

        public boolean isAnonymousAllowed() {
            return anonymousAllowed;
        }

        public void setAnonymousAllowed(boolean anonymousAllowed) {
            this.anonymousAllowed = anonymousAllowed;
        }

        public List<QueryParamInfo> getParams() {
            return params;
        }

        public void setParams(List<QueryParamInfo> params) {
            this.params = params;
        }
    }

    /**
     * Class stores an information about the predefined JPQL query parameter
     */
    public static class QueryParamInfo {
        protected String name;
        protected String type;

        public QueryParamInfo(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}

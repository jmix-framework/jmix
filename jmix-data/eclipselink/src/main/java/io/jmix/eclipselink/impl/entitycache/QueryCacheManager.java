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

package io.jmix.eclipselink.impl.entitycache;

import com.google.common.collect.Lists;
import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.data.PersistenceHints;
import io.jmix.data.StoreAwareLocator;
import io.jmix.eclipselink.EclipselinkProperties;
import io.jmix.eclipselink.impl.JmixEclipseLinkQuery;
import io.jmix.eclipselink.impl.JmixEntityManager;
import jakarta.persistence.Cache;
import jakarta.persistence.EntityManager;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("eclipselink_QueryCacheManager")
public class QueryCacheManager {
    /**
     * Oracle's "IN" clause parameter max count: 1000<p>
     * It is also usually exceeds cache size
     */
    public static final int MAX_BATCH_SIZE = 1000;

    @Autowired
    protected EclipselinkProperties properties;
    @Autowired
    protected QueryCache queryCache;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected StoreAwareLocator storeAwareLocator;

    protected static final Logger log = LoggerFactory.getLogger(QueryCacheManager.class);

    /**
     * Returns true if query cache enabled
     */
    public boolean isEnabled() {
        return properties.isQueryCacheEnabled();
    }

    /**
     * Get query results from query cache by specified {@code queryKey}
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getResultListFromCache(QueryKey queryKey, List<FetchPlan> fetchPlans) {
        log.debug("Looking for query in cache: {}", queryKey.printDescription());
        List<T> resultList = null;
        QueryResult queryResult = queryCache.get(queryKey);
        if (queryResult != null) {
            MetaClass metaClass = metadata.getClass(queryResult.getType());
            String storeName = metaClass.getStore().getName();
            EntityManager em = storeAwareLocator.getEntityManager(storeName);

            if (!metadataTools.isCacheable(metaClass)) {
                log.warn("Using cacheable query without entity cache for {}", queryResult.getType());
            }

            if (!queryResult.getResult().isEmpty()) {
                Cache entityCache = em.getEntityManagerFactory().getCache();
                List<Object> queryCacheResult = queryResult.getResult();

                boolean allEntitiesCached = true;
                for (Object id : queryCacheResult) {
                    if (!entityCache.contains(metaClass.getJavaClass(), id)) {
                        allEntitiesCached = false;
                        break;
                    }
                }

                if (allEntitiesCached) {
                    log.trace("Results for query with id '{}' are found in Query and Entity Caches.", queryKey.getId());
                    resultList = new ArrayList<>(queryResult.getResult().size());
                    for (Object id : queryCacheResult) {
                        resultList.add(em.find(metaClass.getJavaClass(), id, PersistenceHints.builder().withFetchPlans(fetchPlans).build()));
                    }
                } else {
                    log.trace("Results for query with id '{}' are found in Query Cache but not fully present in Entity Cache. " +
                            "Loading entities by ids.", queryKey.getId());
                    resultList = loadAllByIds(queryCacheResult, metaClass, fetchPlans, (JmixEntityManager) em);
                }
            } else {
                return Collections.emptyList();
            }
        } else {
            log.debug("Query results are not found in cache: {}", queryKey.printDescription());
        }
        return resultList;
    }

    protected <T> List<T> loadAllByIds(List<Object> queryCacheResult,
                                       MetaClass metaClass,
                                       List<FetchPlan> fetchPlans,
                                       JmixEntityManager em) {
        Map<Object, Object> batchLoadedById;

        String pkName = metadataTools.getPrimaryKeyName(metaClass);
        if (pkName == null)
            throw new IllegalStateException("Cannot determine PK name for entity " + metaClass);

        if (queryCacheResult.size() > MAX_BATCH_SIZE) {
            log.warn("Cached query result size exceeds {}. " +
                    "Such amount of entities may make cache ineffective", MAX_BATCH_SIZE);

            batchLoadedById = new HashMap<>();
            List<List<Object>> batches = Lists.partition(queryCacheResult, MAX_BATCH_SIZE);
            for (List<Object> batch : batches) {
                batchLoadedById.putAll(loadBatchByIds(batch, metaClass, pkName, fetchPlans, em));
            }
        } else {
            batchLoadedById = loadBatchByIds(queryCacheResult, metaClass, pkName, fetchPlans, em);
        }

        List<T> resultList = new ArrayList<>(queryCacheResult.size());
        for (Object id : queryCacheResult) {
            //noinspection unchecked
            T entity = (T) batchLoadedById.get(id);
            if (entity != null) //entity may be null in case of concurrent deletion after id obtained from Query Cache but before loading from Entity Cache
                resultList.add(entity);
        }
        return resultList;
    }

    protected Map<Object, Object> loadBatchByIds(List<?> queryCacheResult,
                                                 MetaClass metaClass,
                                                 String pkName,
                                                 List<FetchPlan> fetchPlans,
                                                 JmixEntityManager em) {
        //noinspection unchecked
        JmixEclipseLinkQuery<Entity> query = (JmixEclipseLinkQuery<Entity>) em.createQuery(
                String.format("select e from %s e where e.%s in ?1", metaClass.getName(), pkName));
        query.setParameter(1, queryCacheResult);
        query.setHint(PersistenceHints.FETCH_PLAN, fetchPlans);

        List<?> batchLoaded = query.getResultList();
        Map<Object, Object> batchLoadedById = new HashMap<>();
        for (Object e : batchLoaded) {
            batchLoadedById.put(((Entity) e).__getEntityEntry().getEntityId(), e);
        }
        return batchLoadedById;
    }

    /**
     * Get single query results from query cache by specified {@code queryKey}
     * If query is cached and no results found exception is thrown
     */
    @SuppressWarnings("unchecked")
    public <T> T getSingleResultFromCache(QueryKey queryKey, List<FetchPlan> fetchPlans) {
        log.debug("Looking for query in cache: {}", queryKey.printDescription());
        QueryResult queryResult = queryCache.get(queryKey);
        if (queryResult != null) {
            MetaClass metaClass = metadata.getClass(queryResult.getType());
            if (!metadataTools.isCacheable(metaClass)) {
                log.warn("Using cacheable query without entity cache for {}", queryResult.getType());
            }
            if (queryResult.getException() != null) {
                RuntimeException ex = queryResult.getException();
                ex.fillInStackTrace();
                throw queryResult.getException();
            }
            String storeName = metaClass.getStore().getName();
            EntityManager em = storeAwareLocator.getEntityManager(storeName);
            for (Object id : queryResult.getResult()) {
                return (T) em.find(metaClass.getJavaClass(), id, PersistenceHints.builder().withFetchPlans(fetchPlans).build());
            }
        }
        log.debug("Query results are not found in cache: {}", queryKey.printDescription());
        return null;
    }


    /**
     * Put query results into query cache for specified query {@code queryKey}.
     * Results are extracted as identifiers from {@code resultList}
     *
     * @param type         - result entity type (metaClass name)
     * @param relatedTypes - query dependent types (metaClass names). It's a list of entity types used in query
     */
    @SuppressWarnings("unchecked")
    public void putResultToCache(QueryKey queryKey, List resultList, String type, Set<String> relatedTypes) {
        QueryResult queryResult;
        if (resultList.size() > 0) {
            List idList = (List) resultList.stream()
                    .filter(item -> item instanceof Entity)
                    .map(item -> EntityValues.getId(((Entity) item)))
                    .collect(Collectors.toList());
            queryResult = new QueryResult(idList, type, getDescendants(relatedTypes));
        } else {
            queryResult = new QueryResult(Collections.emptyList(), type, getDescendants(relatedTypes));
        }
        log.debug("Put results into cache for query: {}, relatedTypes: {}", queryKey.printDescription(), relatedTypes);
        queryCache.put(queryKey, queryResult);
    }

    /**
     * Put query results into query cache for specified query {@code queryKey}.
     * Results are extracted as identifiers from entity {@code result}
     *
     * @param type         - result entity type (metaClass name)
     * @param relatedTypes - query dependent types (metaClass names). It's a list of entity types used in query
     * @param exception    - store exception in the query cache if {@link TypedQuery#getSingleResult()} throws exception
     */
    @SuppressWarnings("unchecked")
    public <T> void putResultToCache(QueryKey queryKey, T result, String type, Set<String> relatedTypes, RuntimeException exception) {
        QueryResult queryResult;
        if (exception == null) {
            queryResult = new QueryResult(Collections.singletonList(EntityValues.getId(((Entity) result))), type, relatedTypes);
        } else {
            queryResult = new QueryResult(Collections.emptyList(), type, relatedTypes, exception);
        }
        log.debug("Put results into cache for query: {}, relatedTypes: {}", queryKey.printDescription(), relatedTypes);
        queryCache.put(queryKey, queryResult);
    }

    /**
     * Discards cached query results for java class (associated with metaClass) {@code typeClass}
     */
    public void invalidate(Class typeClass) {
        if (isEnabled()) {
            MetaClass metaClass = metadata.getClass(typeClass);
            invalidate(metaClass.getName());
        }
    }

    /**
     * Discards cached query results for metaClass name {@code typeName}
     */
    public void invalidate(String typeName) {
        if (isEnabled()) {
            queryCache.invalidate(typeName);
        }
    }

    /**
     * Discards cached query results for metaClass names {@code typeNames}
     */
    public void invalidate(Set<String> typeNames) {
        if (isEnabled()) {
            if (typeNames != null && typeNames.size() > 0) {
                queryCache.invalidate(typeNames);
            }
        }
    }

    public void invalidateAll() {
        if (isEnabled()) {
            queryCache.invalidateAll();
        }
    }

    protected Set<String> getDescendants(Set<String> relatedTypes) {
        if (relatedTypes == null) return null;
        Set<String> newRelatedTypes = new HashSet<>();
        relatedTypes.forEach(type -> {
            newRelatedTypes.add(type);
            MetaClass metaClass = metadata.getClass(type);
            if (metaClass.getDescendants() != null) {
                Set<String> descendants = metaClass.getDescendants().stream()
                        .filter(it -> it.getJavaClass() != null && !it.getJavaClass().isAnnotationPresent(MappedSuperclass.class))
                        .map(MetadataObject::getName).collect(Collectors.toSet());
                newRelatedTypes.addAll(descendants);
            }
        });
        return newRelatedTypes;
    }
}

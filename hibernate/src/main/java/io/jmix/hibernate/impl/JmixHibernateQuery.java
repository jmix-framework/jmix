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

package io.jmix.hibernate.impl;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.EntityFetcher;
import io.jmix.data.JmixQuery;
import io.jmix.data.PersistenceHints;
import io.jmix.hibernate.impl.metadata.FetchGraphProvider;
import io.jmix.data.impl.QueryMacroHandler;
import io.jmix.data.impl.entitycache.QueryCacheManager;
import io.jmix.data.persistence.DbmsFeatures;
import io.jmix.data.persistence.DbmsSpecifics;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.graph.spi.RootGraphImplementor;
import org.hibernate.query.ParameterMetadata;
import org.hibernate.query.Query;
import org.hibernate.query.QueryParameter;
import org.hibernate.query.internal.AbstractProducedQuery;
import org.hibernate.query.spi.QueryImplementor;
import org.hibernate.query.spi.QueryParameterBinding;
import org.hibernate.query.spi.QueryParameterListBinding;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;

import javax.annotation.Nullable;
import javax.persistence.FlushModeType;
import javax.persistence.Parameter;
import javax.persistence.TypedQuery;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.core.entity.EntitySystemAccess.getEntityEntry;

public class JmixHibernateQuery<E> extends AbstractProducedQuery<E> implements Query<E>, JmixQuery<E> {

    private static final Logger log = LoggerFactory.getLogger(JmixHibernateQuery.class);

    private Class<E> resultClass;

    private final JmixQueryParameterBindingsImpl queryParameterBindings;

    protected BeanFactory beanFactory;
    protected Environment environment;
    protected Metadata metadata;
    protected MetadataTools metadataTools;
    protected FetchGraphProvider fetchGraphProvider;
    protected ExtendedEntities extendedEntities;
    protected FetchPlanRepository fetchPlanRepository;
    protected EntityFetcher entityFetcher;
    protected QueryCacheManager queryCacheMgr;
    protected QueryTransformerFactory queryTransformerFactory;
    protected DbmsSpecifics dbmsSpecifics;
    protected Collection<QueryMacroHandler> macroHandlers;

    protected HibernatePersistenceSupport support;
    protected HibernateEntityChangedEventManager entityChangedEventManager;

    protected Query query;
    protected boolean isNative;
    protected String queryString;
    protected String transformedQueryString;
    //    protected Set<Param> params = new HashSet<>();
    protected List<FetchPlan> fetchPlans = new ArrayList<>();
    protected boolean singleResultExpected;
    protected boolean cacheable;

    protected Set<String> obsoleteParams = new HashSet<>();

    public JmixHibernateQuery(SessionImplementor entityManager, ParameterMetadata parameterMetadata, BeanFactory beanFactory, boolean isNative, String qlString,
                              @Nullable Class<E> resultClass) {
        super(entityManager, parameterMetadata);
        this.beanFactory = beanFactory;
        this.isNative = isNative;
        this.queryString = qlString;
        this.resultClass = resultClass;

        environment = beanFactory.getBean(Environment.class);
        metadata = beanFactory.getBean(Metadata.class);
        metadataTools = beanFactory.getBean(MetadataTools.class);
        extendedEntities = beanFactory.getBean(ExtendedEntities.class);
        fetchPlanRepository = beanFactory.getBean(FetchPlanRepository.class);
        entityFetcher = beanFactory.getBean(EntityFetcher.class);
        queryCacheMgr = beanFactory.getBean(QueryCacheManager.class);
        queryTransformerFactory = beanFactory.getBean(QueryTransformerFactory.class);
        dbmsSpecifics = beanFactory.getBean(DbmsSpecifics.class);
        macroHandlers = beanFactory.getBeanProvider(QueryMacroHandler.class).stream().collect(Collectors.toList());

        support = beanFactory.getBean(HibernatePersistenceSupport.class);
        entityChangedEventManager = beanFactory.getBean(HibernateEntityChangedEventManager.class);
        fetchGraphProvider = beanFactory.getBean(FetchGraphProvider.class);

        this.queryParameterBindings = JmixQueryParameterBindingsImpl.from(
                beanFactory,
                parameterMetadata,
                entityManager.getFactory(),
                entityManager.isQueryParametersValidationEnabled(),
                isNative
        );
    }

    @Override
    protected List<E> doList() {
        if (log.isDebugEnabled())
            log.debug(queryString.replaceAll("[\\t\\n\\x0B\\f\\r]", " "));

        singleResultExpected = false;

        Query<E> query = getQuery();
        preExecute(query);

        List<E> queryResultList = query.getResultList();
        List<E> resultList = new ArrayList<>(queryResultList.size());
        for (E item : queryResultList) {
            item = HibernateUtils.initializeAndUnproxy(item);
            if (item instanceof Entity) {
                getEntityEntry(item).setNew(false);
                for (FetchPlan fetchPlan : fetchPlans) {
                    entityFetcher.fetch(item, fetchPlan);
                }
            }
            resultList.add(item);
        }
        return resultList;
    }

    @Override
    public QueryImplementor<E> setMaxResults(int maxResult) {
        super.setMaxResults(maxResult);
        if (query != null)
            query.setMaxResults(maxResult);
        return this;
    }

    @Override
    public QueryImplementor<E> setFirstResult(int startPosition) {
        super.setFirstResult(startPosition);
        if (query != null)
            query.setFirstResult(startPosition);
        return this;
    }

    @Override
    public JmixHibernateQuery<E> setHint(String hintName, Object value) {
        if (PersistenceHints.FETCH_PLAN.equals(hintName)) {
            if (isNative)
                throw new UnsupportedOperationException("FetchPlan is not supported for native queries");
            if (value == null) {
                fetchPlans.clear();
            } else if (value instanceof Collection) {
                //noinspection unchecked
                fetchPlans.addAll((Collection) value);
            } else {
                fetchPlans.add((FetchPlan) value);
            }
        } else if (PersistenceHints.CACHEABLE.equals(hintName)) {
            cacheable = (boolean) value;
        }
        super.setHint(hintName, value);
        return this;
    }

    @Override
    protected boolean isNativeQuery() {
        return isNative;
    }

    @Override
    public int doExecuteUpdate() {
        Query<E> query = getQuery();
        preExecute(query);
        return query.executeUpdate();
    }

    @Override
    public SessionImplementor getProducer() {
        return (SessionImplementor) super.getProducer();
    }

    @Override
    public Type[] getReturnTypes() {
        return getProducer().getFactory().getReturnTypes(queryString);
    }

    @Override
    public String[] getReturnAliases() {
        return getProducer().getFactory().getReturnAliases(queryString);
    }

    @Override
    public Query setEntity(int position, Object val) {
        return setParameter(position, val, getProducer().getFactory().getTypeHelper().entity(resolveEntityName(val)));
    }

    @Override
    public Query setEntity(String name, Object val) {
        return setParameter(name, val, getProducer().getFactory().getTypeHelper().entity(resolveEntityName(val)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> cls) {
        if (cls.isAssignableFrom(this.getClass())) {
            // unwraps any proxy to JmixQuery
            return (T) this;
        }
        return getQuery().unwrap(cls);
    }

    @Override
    protected JmixQueryParameterBindingsImpl getQueryParameterBindings() {
        return queryParameterBindings;
    }

    @Nullable
    public E getSingleResultOrNull() {
        if (log.isDebugEnabled())
            log.debug(queryString.replaceAll("[\\t\\n\\x0B\\f\\r]", " "));

        Integer saveMaxResults = getMaxResults();
        setMaxResults(1);
        try {
            List<E> resultList = getResultList();
            if (resultList.isEmpty()) {
                return null;
            } else {
                return resultList.get(0);
            }
        } finally {
            setMaxResults(saveMaxResults);
        }
    }

    /**
     * INTERNAL
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * INTERNAL
     */
    public void setQueryString(String queryString) {
        checkState();
        this.queryString = queryString;
    }

    /**
     * INTERNAL
     */
    public void setSingleResultExpected(boolean singleResultExpected) {
        this.singleResultExpected = singleResultExpected;
    }

    private Query<E> getQuery() {
        if (query == null) {
            FetchPlan fetchPlan = fetchPlans.isEmpty() ? null : fetchPlans.get(0);

            if (isNative) {
                log.trace("Creating SQL query: {}", queryString);
                if (resultClass == null)
                    query = getProducer().createNativeQuery(queryString);
                else {
                    if (!Entity.class.isAssignableFrom(resultClass)) {
                        throw new IllegalArgumentException("Non-entity result class for native query is not supported" +
                                " by EclipseLink: " + resultClass);
                    }
                    Class effectiveClass = extendedEntities.getEffectiveClass(resultClass);
                    query = getProducer().createNativeQuery(queryString, effectiveClass);
                }
            } else {
                log.trace("Creating JPQL query: {}", queryString);
                transformedQueryString = transformQueryString();
                log.trace("Transformed JPQL query: {}", transformedQueryString);

                Class effectiveClass = getEffectiveResultClass();
                query = buildJPAQuery(transformedQueryString, effectiveClass);
            }

            if (getComment() != null)
                query.setComment(getComment());

            if (getHibernateFlushMode() != null)
                query.setHibernateFlushMode(getHibernateFlushMode());


            if (getFlushMode() == null) {
                if (fetchPlan != null && !fetchPlan.loadPartialEntities()) {
                    query.setFlushMode(FlushModeType.AUTO);
                } else {
                    query.setFlushMode(FlushModeType.COMMIT);
                }
            } else {
                query.setFlushMode(getFlushMode());
            }


            for (QueryParameter<?> param : getParameterMetadata().collectAllParameters()) {
                if (param.getName() != null && !obsoleteParams.contains(param.getName())) {
                    Object value = getQueryParameterBindings().getBindValue(param.getName());
                    query.setParameter(param.getName(), value);
                } else if (param.getPosition() != null) {
                    QueryParameterBinding<Object> binding = getQueryParameterBindings().getBinding(param.getPosition());
                    if (binding != null) {
                        Object value = binding.getBindValue();
                        query.setParameter(param.getPosition(), value);
                    }
                }
            }

            addMacroParams(query);

            // disable SQL caching to support "is null" generation
//            if (nullParam)
//                query.setHint(QueryHints.PREPARE, HintValues.FALSE);

            // Set maxResults and firstResult only if the query is not by ID, otherwise EclipseLink does not select
            // nested collections in some cases
            if (getQueryOptions().getMaxRows() != null && !singleResultExpected)
                query.setMaxResults(getMaxResults());
            if (getQueryOptions().getFirstRow() != null && !singleResultExpected)
                query.setFirstResult(getFirstResult());

            if (getLockMode() != null)
                query.setLockMode(getLockMode());

            if (getLockOptions() != null)
                query.setLockOptions(getLockOptions());

            if (getHints() != null) {

                for (Map.Entry<String, Object> hint : getHints().entrySet()) {
                    query.setHint(hint.getKey(), hint.getValue());
                }
            }
            query.setCacheable(cacheable);
            if (getCacheMode() != null)
                query.setCacheMode(getCacheMode());

            if (getCacheRegion() != null)
                query.setCacheRegion(getCacheRegion());


            if (fetchPlan != null) {
                MetaClass metaClass = metadata.getClass(fetchPlan.getEntityClass());
                if (!metadataTools.isCacheable(metaClass) || !singleResultExpected) {
                    query.setCacheable(false);
                }

                RootGraphImplementor<?> entityGraph = getProducer().createEntityGraph(fetchPlan.getEntityClass());
                if (entityGraph != null) {
                    for (FetchPlan plan : fetchPlans) {
                        fetchGraphProvider.fillGraph(entityGraph, plan);
                    }
                    query = query.applyLoadGraph(entityGraph);
                }
            }
        }
        return query;
    }

    @Nullable
    private Class getEffectiveResultClass() {
        if (resultClass == null) {
            return null;
        }
        if (Entity.class.isAssignableFrom(resultClass)) {
            return extendedEntities.getEffectiveClass(resultClass);
        }
        return resultClass;
    }

    private Query<E> buildJPAQuery(String queryString, Class<E> resultClass) {
        Query<E> query;
        if (resultClass != null) {
            query = getProducer().createQuery(queryString, resultClass);
        } else {
            query = getProducer().createQuery(queryString);
        }
        return query;
    }


    private String transformQueryString() {
        String result = expandMacros(queryString);

        boolean rebuildParser = false;
        QueryParser parser = queryTransformerFactory.parser(result);

        String entityName = parser.getEntityName();
        Class effectiveClass = extendedEntities.getEffectiveClass(entityName);
        MetaClass effectiveMetaClass = metadata.getClass(effectiveClass);
        String effectiveEntityName = effectiveMetaClass.getName();
        if (!effectiveEntityName.equals(entityName)) {
            QueryTransformer transformer = queryTransformerFactory.transformer(result);
            transformer.replaceEntityName(effectiveEntityName);
            result = transformer.getResult();
            rebuildParser = true;
        }

        if (getQueryOptions().getFirstRow() != null && getQueryOptions().getFirstRow() > 0) {
            String storeName = metadataTools.getStoreName(effectiveMetaClass);
            DbmsFeatures dbmsFeatures = dbmsSpecifics.getDbmsFeatures(storeName);
            if (dbmsFeatures.useOrderByForPaging()) {
                QueryTransformer transformer = queryTransformerFactory.transformer(result);
                transformer.addOrderByIdIfNotExists(metadataTools.getPrimaryKeyName(effectiveMetaClass));
                result = transformer.getResult();
                rebuildParser = true;
            }
        }

        result = replaceParams(result, parser);

        if (rebuildParser) {
            parser = queryTransformerFactory.parser(result);
        }
        String nestedEntityName = parser.getOriginalEntityName();
        String nestedEntityPath = parser.getOriginalEntityPath();
        if (nestedEntityName != null) {
            if (parser.isCollectionOriginalEntitySelect()) {
                throw new IllegalStateException(String.format("Collection attributes are not supported in select clause: %s", nestedEntityPath));
            }
            QueryTransformer transformer = queryTransformerFactory.transformer(result);
            transformer.replaceWithSelectEntityVariable("tempEntityAlias");
            transformer.addFirstSelectionSource(String.format("%s tempEntityAlias", nestedEntityName));
            transformer.addWhereAsIs(String.format("tempEntityAlias.id = %s.id", nestedEntityPath));
            transformer.addEntityInGroupBy("tempEntityAlias");
            result = transformer.getResult();
        }

        result = replaceIsNullAndIsNotNullStatements(result);

        return result;
    }

    private String expandMacros(String queryStr) {
        String result = queryStr;
        if (macroHandlers != null) {
            for (QueryMacroHandler handler : macroHandlers) {
                result = handler.expandMacro(result);
            }
        }
        return result;
    }

    private String replaceParams(String query, QueryParser parser) {
        String result = query;
        Set<String> paramNames = Sets.newHashSet(parser.getParamNames());

        for (String paramName : getParameterMetadata().getNamedParameterNames()) {
            if (getQueryParameterBindings().collectNamedParameterBindings().containsKey(paramName)) {
                QueryParameterBinding<Object> binding = getQueryParameterBindings().getBinding(paramName);
                result = processStringValue(result, paramName, binding);
                if (binding.getBindValue() == null) {
                    if (parser.isParameterInCondition(paramName)) {
                        result = replaceInCollectionParam(result, paramName);
                        obsoleteParams.add(paramName);
                    }
                }
            } else {
                QueryParameterListBinding<Object> listBinding = getQueryParameterBindings().getQueryParameterListBinding(paramName);
                if (listBinding != null && listBinding.getBindValues() != null) {
                    Collection collectionValue = listBinding.getBindValues();
                    if (collectionValue.isEmpty()) {
                        result = replaceInCollectionParam(result, paramName);
                        obsoleteParams.add(paramName);
                    }
                }
            }
            paramNames.remove(paramName);
        }

        for (QueryParameter<?> positionalParameter : getParameterMetadata().getPositionalParameters()) {
            QueryParameterBinding<?> binding = getQueryParameterBindings().getBinding(positionalParameter);
            result = processStringValue(result, String.valueOf(positionalParameter.getPosition()), binding);
        }

        for (String paramName : paramNames) {
            result = replaceInCollectionParam(result, paramName);
        }
        return result;
    }

    private String processStringValue(String result, String paramName, QueryParameterBinding binding) {
        if (binding.getBindType().getReturnedClass().equals(String.class)) {
            String strValue = (String) binding.getBindValue();
            if (strValue.startsWith("(?i)")) {
                result = replaceCaseInsensitiveParam(result, paramName);
                binding.setBindValue(strValue.substring(4).toLowerCase());
            }
        }
        return result;
    }

    private String replaceCaseInsensitiveParam(String query, String paramName) {
        QueryTransformer transformer = queryTransformerFactory.transformer(query);
        transformer.handleCaseInsensitiveParam(paramName);
        return transformer.getResult();
    }

    private String replaceInCollectionParam(String query, String paramName) {
        QueryTransformer transformer = queryTransformerFactory.transformer(query);
        transformer.replaceInCondition(paramName);
        return transformer.getResult();
    }

    private String replaceIsNullAndIsNotNullStatements(String query) {
        Set<String> replacedParams = new HashSet<>();

        QueryTransformer transformer = queryTransformerFactory.transformer(query);

        getParameterMetadata().getNamedParameterNames().stream()
                .map(param -> Maps.immutableEntry(param, getQueryParameterBindings().getBinding(param)))
                .map(pair -> Maps.immutableEntry(pair.getKey(), transformer.replaceIsNullStatements(
                        pair.getKey(), getQueryParameterBindings().getBinding(pair.getKey()).getBindValue() == null)))
                .filter(Map.Entry::getValue)
                .forEach(entry -> replacedParams.add(entry.getKey()));

        if (replacedParams.isEmpty()) {
            return query;
        }

        String resultQuery = transformer.getResult();

        QueryParser parser = queryTransformerFactory.parser(resultQuery);
        obsoleteParams.addAll(replacedParams.stream()
                .filter(param -> !parser.isParameterUsedInAnyCondition(param))
                .collect(Collectors.toSet()));

        return resultQuery;
    }

    private void addMacroParams(TypedQuery jpaQuery) {
        if (macroHandlers != null) {
            for (QueryMacroHandler handler : macroHandlers) {

                Map<String, Object> namedParams = new HashMap<>();
                for (String paramName : getParameterMetadata().getNamedParameterNames()) {
                    QueryParameterBinding<Object> binding = getQueryParameterBindings().getBinding(paramName);
                    namedParams.put(paramName, binding.getBindValue());
                }

                Map<String, Class> paramsTypes = new HashMap<>();
                for (Parameter<?> parameter : jpaQuery.getParameters()) {
                    if (parameter.getName() != null) {
                        paramsTypes.put(parameter.getName(), parameter.getParameterType());
                    }
                }
                handler.setQueryParams(namedParams);
                handler.setExpandedParamTypes(paramsTypes);

                for (Map.Entry<String, Object> entry : handler.getParams().entrySet()) {
                    jpaQuery.setParameter(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    protected void beforeQuery() {
        super.beforeQuery();
        if (log.isDebugEnabled())
            log.debug(queryString.replaceAll("[\\t\\n\\x0B\\f\\r]", " "));
    }

    private void preExecute(Query<E> jpaQuery) {
        // copying behaviour of org.eclipse.persistence.internal.jpa.QueryImpl.executeReadQuery()
        if (jpaQuery.getFlushMode() == FlushModeType.AUTO && (!jpaQuery.isReadOnly())) {
            // flush is expected
            entityChangedEventManager.beforeFlush(getProducer(), support.getInstances(getProducer()));
            support.processFlush(getProducer(), true);
        }
    }

    private void checkState() {
        if (query != null)
            throw new IllegalStateException("Query delegate has already been created");
    }
}

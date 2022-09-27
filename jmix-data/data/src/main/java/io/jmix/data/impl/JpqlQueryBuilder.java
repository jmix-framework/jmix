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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.jmix.core.*;
import io.jmix.core.common.util.StringHelper;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.querycondition.*;
import io.jmix.data.JmixQuery;
import io.jmix.data.QueryTransformer;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.data.impl.jpql.generator.ConditionGenerationContext;
import io.jmix.data.impl.jpql.generator.ConditionJpqlGenerator;
import io.jmix.data.impl.jpql.generator.ParameterJpqlGenerator;
import io.jmix.data.impl.jpql.generator.SortJpqlGenerator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds {@link Query} instance to use in DataService.
 */
@Component("data_JpqlQueryBuilder")
@Scope("prototype")
public class JpqlQueryBuilder<Q extends JmixQuery> {

    protected Object id;
    protected List<?> ids;

    protected String queryString;
    protected Map<String, Object> queryParameters;
    protected Condition condition;
    protected Sort sort;

    protected String entityName;
    protected List<String> valueProperties;

    protected boolean previousResults;
    protected UUID sessionId;
    protected int queryKey;

    protected boolean countQuery;
    protected LockModeType lockMode;

    protected String resultQuery;
    protected Map<String, Object> resultParameters;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected ConditionJpqlGenerator conditionJpqlGenerator;

    @Autowired
    protected ParameterJpqlGenerator conditionParameterJpqlGenerator;

    @Autowired
    protected SortJpqlGenerator sortJpqlGenerator;

    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected BeanFactory beanFactory;

    @Autowired
    protected QueryParamValuesManager queryParamValuesManager;

    public JpqlQueryBuilder setId(@Nullable Object id) {
        this.id = id;
        return this;
    }

    public JpqlQueryBuilder setIds(List<?> ids) {
        this.ids = ids;
        return this;
    }

    public JpqlQueryBuilder setEntityName(String entityName) {
        this.entityName = entityName;
        return this;
    }

    public JpqlQueryBuilder setValueProperties(List<String> valueProperties) {
        this.valueProperties = valueProperties;
        return this;
    }

    public JpqlQueryBuilder setQueryString(String queryString) {
        this.queryString = queryString;
        return this;
    }

    public JpqlQueryBuilder setQueryParameters(Map<String, Object> queryParams) {
        this.queryParameters = queryParams;
        return this;
    }

    public JpqlQueryBuilder setCondition(Condition condition) {
        this.condition = condition;
        return this;
    }

    public JpqlQueryBuilder setSort(Sort sort) {
        this.sort = sort;
        return this;
    }

    public JpqlQueryBuilder setPreviousResults(UUID sessionId, int queryKey) {
        this.previousResults = true;
        this.sessionId = sessionId;
        this.queryKey = queryKey;
        return this;
    }

    public JpqlQueryBuilder setCountQuery() {
        this.countQuery = true;
        return this;
    }

    public JpqlQueryBuilder setLockMode(@Nullable LockModeType lockMode) {
        this.lockMode = lockMode;
        return this;
    }

    public String getResultQueryString() {
        if (resultQuery == null) {
            buildResultQuery();
        }
        return resultQuery;
    }

    public Map<String, Object> getResultParameters() {
        if (resultQuery == null) {
            buildResultQuery();
        }
        return resultParameters;
    }

    public Q getQuery(EntityManager em) {
        Q query = (Q) em.createQuery(getResultQueryString());

        //we have to replace parameter names in macros because for {@link com.haulmont.cuba.core.sys.querymacro.TimeBetweenQueryMacroHandler}
        //we need to replace a parameter with number of days with its value before macros is expanded to JPQL expression
        replaceParamsInMacros(query);

        Set<String> paramNames = queryTransformerFactory.parser(getResultQueryString()).getParamNames();

        for (Map.Entry<String, Object> entry : getResultParameters().entrySet()) {
            String name = entry.getKey();
            if (paramNames.contains(name)) {
                Object value = entry.getValue();

                if (value instanceof TemporalValue) {
                    TemporalValue temporalValue = (TemporalValue) value;
                    query.setParameter(name, temporalValue.date, temporalValue.type);
                } else {
                    query.setParameter(name, value);
                }
            } else {
                if (entry.getValue() != null)
                    throw new DevelopmentException(String.format("Parameter '%s' is not used in the query", name));
            }
        }

        if (lockMode != null) {
            query.setLockMode(lockMode);
        }

        return query;
    }

    protected void buildResultQuery() {
        resultQuery = queryString;
        resultParameters = queryParameters;
        if (entityName != null) {
            if (Strings.isNullOrEmpty(queryString)) {
                if (id != null) {
                    resultQuery = String.format("select e from %s e where e.%s = :entityId", entityName, getPrimaryKeyProperty().getName());
                    resultParameters = Maps.newHashMap(ImmutableMap.of("entityId", id));
                } else if (ids != null && !ids.isEmpty()) {
                    resultQuery = String.format("select e from %s e where e.%s in :entityIds", entityName, getPrimaryKeyProperty().getName());
                    resultParameters = Maps.newHashMap(ImmutableMap.of("entityIds", ids));
                } else {
                    resultQuery = String.format("select e from %s e", entityName);
                    resultParameters = Collections.emptyMap();
                }
            }
        }
        applyFiltering();
        applySorting();
        applyCount();
        restrictByPreviousResults();
    }

    protected void applySorting() {
        if (sort != null) {
            resultQuery = sortJpqlGenerator.processQuery(entityName, valueProperties, resultQuery, sort);
        }
    }

    protected void applyFiltering() {
        if (condition != null) {
            Set<String> nonNullParamNames = queryParameters.entrySet().stream()
                    .filter(e ->
                            e.getValue() != null &&
                                    !(e.getValue() instanceof TemporalValue && ((TemporalValue) e.getValue()).date == null))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            // keep parameters that can be assigned by a QueryParamValueProvider
            for (String parameter : condition.getParameters()) {
                if (queryParamValuesManager.supports(parameter)) {
                    nonNullParamNames.add(parameter);
                }
            }

            Condition actualized = condition.actualize(nonNullParamNames);

            Set<String> excludedParameters = condition.getExcludedParameters(nonNullParamNames);
            resultParameters.entrySet().removeIf(e -> excludedParameters.contains(e.getKey()));

            if (actualized != null) {
                resultParameters = conditionParameterJpqlGenerator
                        .processParameters(resultParameters, queryParameters, actualized, entityName);
            }

            resultQuery = conditionJpqlGenerator
                    .processQuery(resultQuery, createConditionGenerationContext(actualized));
        }
    }

    protected ConditionGenerationContext createConditionGenerationContext(@Nullable Condition condition) {
        ConditionGenerationContext generationContext = new ConditionGenerationContext(condition);
        generationContext.setEntityName(entityName);
        generationContext.setValueProperties(valueProperties);
        return generationContext;
    }

    protected void applyCount() {
        if (countQuery) {
            QueryTransformer transformer = queryTransformerFactory.transformer(resultQuery);
            transformer.replaceWithCount();
            resultQuery = transformer.getResult();
        }
    }

    protected void restrictByPreviousResults() {
        if (previousResults) {
            Class type = getPrimaryKeyProperty().getJavaType();
            String entityIdField;
            if (UUID.class.equals(type)) {
                entityIdField = "entityId";
            } else if (Long.class.equals(type)) {
                entityIdField = "longEntityId";
            } else if (Integer.class.equals(type)) {
                entityIdField = "intEntityId";
            } else if (String.class.equals(type)) {
                entityIdField = "stringEntityId";
            } else {
                throw new IllegalStateException(
                        String.format("Unsupported primary key type: %s for %s", type.getSimpleName(), entityName));
            }

            QueryTransformer transformer = queryTransformerFactory.transformer(resultQuery);
            transformer.addJoinAndWhere(
                    ", sys$QueryResult _qr",
                    String.format("_qr.%s = {E}.%s and _qr.sessionId = :_qr_sessionId and _qr.queryKey = %s",
                            entityIdField, getPrimaryKeyProperty().getName(), queryKey)
            );

            this.resultQuery = transformer.getResult();
            this.resultParameters.put("_qr_sessionId", sessionId);
        }
    }

    protected void replaceParamsInMacros(Q query) {
        Collection<QueryMacroHandler> handlers = beanFactory.getBeanProvider(QueryMacroHandler.class).stream()
                .collect(Collectors.toList());
        String modifiedQuery = query.getQueryString();
        for (QueryMacroHandler handler : handlers) {
            modifiedQuery = handler.replaceQueryParams(modifiedQuery, queryParameters);
        }
        query.setQueryString(modifiedQuery);
    }

    protected MetaProperty getPrimaryKeyProperty() {
        MetaClass metaClass = metadata.getClass(entityName);
        MetaProperty property = metadataTools.getPrimaryKeyProperty(metaClass);
        if (property == null) {
            throw new IllegalStateException(String.format("Entity %s has no primary key", entityName));
        }
        return property;
    }

    public static @Nullable
    String printQuery(@Nullable String query) {
        return query == null ? null : StringHelper.removeExtraSpaces(query.replace('\n', ' '));
    }
}

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

package io.jmix.dynattrflowui.impl;


import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.StoreAwareLocator;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.OptionsLoaderType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.support.StaticScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("dynat_AttributeOptionsLoader")
public class AttributeOptionsLoaderImpl implements AttributeOptionsLoader {

    protected final Map<String, OptionsLoaderStrategy> loaderStrategies = new HashMap<>();

    @Autowired
    protected StoreAwareLocator storeAwareLocator;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ScriptEvaluator scriptEvaluator;

    protected static final String ENTITY_QUERY_PARAM = "entity";
    protected static final String ENTITY_FIELD_QUERY_PARAM = "entity.";
    protected static final Pattern COMMON_PARAM_PATTERN = Pattern.compile("\\$\\{(.+?)}");

    public interface OptionsLoaderStrategy {
        List loadOptions(Object entity, AttributeDefinition attribute, String script);
    }

    @PostConstruct
    public void init() {
        loaderStrategies.put(OptionsLoaderType.GROOVY.getId(), this::executeGroovyScript);
        loaderStrategies.put(OptionsLoaderType.SQL.getId(), this::executeSql);
        loaderStrategies.put(OptionsLoaderType.JPQL.getId(), this::executeJpql);
    }

    @Override
    public List loadOptions(Object entity, AttributeDefinition attribute) {
        AttributeDefinition.Configuration configuration = attribute.getConfiguration();
        String loaderScript = configuration.getOptionsLoaderScript();
        List result = null;
        if (Objects.nonNull(configuration.getOptionsLoaderType())) {
            OptionsLoaderStrategy loaderStrategy = resolveLoaderStrategy(configuration.getOptionsLoaderType());
            result = loaderStrategy.loadOptions(entity, attribute, loaderScript);
        }
        return result == null ? Collections.emptyList() : result;
    }

    protected OptionsLoaderStrategy resolveLoaderStrategy(OptionsLoaderType loaderType) {
        OptionsLoaderStrategy loaderStrategy = loaderStrategies.get(loaderType.getId());
        if (loaderStrategy == null) {
            throw new IllegalStateException(String.format("Unsupported options loader type: %s", loaderType.getId()));
        }
        return loaderStrategy;
    }

    protected List executeSql(Object entity, AttributeDefinition attribute, String script) {
        if (!Strings.isNullOrEmpty(script)) {
            return storeAwareLocator.getTransactionTemplate(Stores.MAIN)
                    .execute(status -> {
                        EntityManager entityManager = storeAwareLocator.getEntityManager(Stores.MAIN);
                        SqlQuery sqlQuery = buildSqlQuery(script, Collections.singletonMap("entity", entity));

                        Query query = entityManager.createNativeQuery(sqlQuery.query);

                        if (sqlQuery.params != null) {
                            int i = 1;
                            for (Object param : sqlQuery.params) {
                                query.setParameter(i++, param);
                            }
                        }

                        return query.getResultList();
                    });
        }
        return null;
    }

    protected static class SqlQuery {
        protected String query;
        protected List<Object> params;

        public SqlQuery(String query, List<Object> params) {
            this.query = query;
            this.params = params;
        }
    }

    protected SqlQuery buildSqlQuery(String script, Map<String, Object> params) {
        Matcher matcher = COMMON_PARAM_PATTERN.matcher(script);
        boolean result = matcher.find();
        if (result) {
            List<Object> queryParams = new ArrayList<>();
            StringBuffer query = new StringBuffer();
            do {
                String parameterName = matcher.group(1);
                queryParams.add(getQueryParameterValue(parameterName, params));
                matcher.appendReplacement(query, "?");
                result = matcher.find();
            } while (result);
            matcher.appendTail(query);
            return new SqlQuery(query.toString(), queryParams);
        }
        return new SqlQuery(script, null);
    }

    protected Object getQueryParameterValue(String name, Map<String, Object> params) {
        if (ENTITY_QUERY_PARAM.equals(name)) {
            Object entity = params.get("entity");
            if (entity != null) {
                return EntityValues.getId(entity);
            }
        } else if (name != null && name.startsWith(ENTITY_FIELD_QUERY_PARAM)) {
            Object entity = params.get("entity");
            if (entity != null) {
                String attributePath = name.substring(ENTITY_FIELD_QUERY_PARAM.length());
                Object value = EntityValues.getValueEx(entity, attributePath);
                return value instanceof Entity ? EntityValues.getId(value) : value;
            }
        }
        return null;
    }

    protected List executeJpql(Object entity, AttributeDefinition attribute, String script) {
        MetaClass metaClass = metadata.getClass(attribute.getJavaType());

        StringBuilder queryString = new StringBuilder(String.format("select e from %s e", metaClass.getName()));

        if (!Strings.isNullOrEmpty(attribute.getConfiguration().getJoinClause())) {
            queryString.append(" ").append(attribute.getConfiguration().getJoinClause());
        }

        if (!Strings.isNullOrEmpty(attribute.getConfiguration().getWhereClause())) {
            queryString.append(" where ").append(attribute.getConfiguration().getWhereClause().replaceAll("\\{E}", "e"));
        }

        LoadContext.Query query = buildJpqlQuery(queryString.toString(), Collections.singletonMap("entity", entity));

        LoadContext<?> loadContext = new LoadContext<>(metaClass);
        loadContext.setQuery(query);

        //todo: secure data manager
        return dataManager.loadList(loadContext);
    }

    protected LoadContext.Query buildJpqlQuery(String script, Map<String, Object> params) {
        Matcher matcher = COMMON_PARAM_PATTERN.matcher(script);
        boolean result = matcher.find();
        if (result) {
            Map<String, Object> queryParams = new HashMap<>();
            StringBuffer queryString = new StringBuffer();
            int i = 1;
            do {
                String paramKey = String.format("param_%s", i);
                queryParams.put(paramKey, getQueryParameterValue(matcher.group(1), params));
                matcher.appendReplacement(queryString, ":" + paramKey);
                result = matcher.find();
            } while (result);
            matcher.appendTail(queryString);
            LoadContext.Query query = new LoadContext.Query(queryString.toString());
            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
            return query;
        } else {
            return new LoadContext.Query(script);
        }
    }

    protected List executeGroovyScript(Object entity, AttributeDefinition attribute, String script) {
        if (!Strings.isNullOrEmpty(script)) {
            return (List) scriptEvaluator.evaluate(new StaticScriptSource(script), Collections.singletonMap("entity", entity));
        }
        return null;
    }
}

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

package io.jmix.reports.libintegration;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import io.jmix.core.JmixEntity;
import io.jmix.reports.app.EntityMap;
import com.haulmont.yarg.exception.DataLoadingException;
import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.loaders.impl.AbstractDbDataLoader;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JpqlDataDataLoader extends AbstractDbDataLoader implements ReportDataLoader {
    @Autowired
    private Persistence persistence;

    @Autowired
    private BeanFactory beanFactory;

    private static final String QUERY_END = "%%END%%";
    private static final String ALIAS_PATTERN = "as\\s+\"?([\\w|\\d|_|\\.]+)\"?\\s*";
    private static final Pattern OUTPUT_PARAMS_PATTERN =
            Pattern.compile("(?i)" + ALIAS_PATTERN + "[,|from|" + QUERY_END + "]", Pattern.CASE_INSENSITIVE);

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
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, BandData parentBand, Map<String, Object> params) {
        String storeName = StoreUtils.getStoreName(reportQuery);
        String query = reportQuery.getScript();
        if (StringUtils.isBlank(query)) {
            return Collections.emptyList();
        }
        try (Transaction tx = persistence.createTransaction(storeName)) {
            if (Boolean.TRUE.equals(reportQuery.getProcessTemplate())) {
                query = processQueryTemplate(query, parentBand, params);
            }

            List<OutputValue> outputParameters = parseQueryOutputParametersNames(query);

            query = query.replaceAll("(?i)" + ALIAS_PATTERN + ",", ",");//replaces [as alias_name], entries except last
            query = query.replaceAll("(?i)" + ALIAS_PATTERN, " ");//replaces last [as alias_name] entry

            Query select = insertParameters(trimQuery(query), storeName, parentBand, params);
            List queryResult = select.getResultList();
            tx.commit();
            if (queryResult.size() > 0 && queryResult.get(0) instanceof JmixEntity) {
                List<Map<String, Object>> wrappedResults = new ArrayList<>();
                for (Object theResult : queryResult) {
                    wrappedResults.add(new EntityMap((JmixEntity) theResult,beanFactory));
                }
                return wrappedResults;
            } else {
                return fillOutputData(queryResult, outputParameters);
            }
        } catch (Throwable e) {
            throw new DataLoadingException(String.format("An error occurred while loading data for data set [%s]", reportQuery.getName()), e);
        }
    }

    protected Query insertParameters(String query, String storeName, BandData parentBand, Map<String, Object> params) {
        QueryPack pack = prepareQuery(query, parentBand, params);

        boolean inserted = pack.getParams().length > 0;
        EntityManager em = persistence.getEntityManager(storeName);
        Query select = em.createQuery(pack.getQuery());
        if (inserted) {
            //insert parameters to their position
            for (QueryParameter queryParameter : pack.getParams()) {
                Object value = queryParameter.getValue();
                select.setParameter(queryParameter.getPosition(), convertParameter(value));
            }
        }
        return select;
    }

    @Override
    protected String insertParameterToQuery(String query, QueryParameter parameter) {
        query = query.replaceFirst(parameter.getParamRegexp(), "?" + parameter.getPosition());
        return query;
    }

    protected String trimQuery(String query) {
        if (query.endsWith(";")) {
            return query.substring(0, query.length() - 1);
        } else {
            return query;
        }
    }
}
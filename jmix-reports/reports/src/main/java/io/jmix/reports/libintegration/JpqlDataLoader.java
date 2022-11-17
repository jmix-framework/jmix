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

import com.haulmont.yarg.exception.DataLoadingException;
import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.loaders.impl.AbstractDbDataLoader;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;
import io.jmix.core.*;
import io.jmix.data.StoreAwareLocator;
import io.jmix.reports.app.EntityMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JpqlDataLoader extends AbstractDbDataLoader implements ReportDataLoader {

    @Autowired
    protected TransactionTemplate transaction;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected StoreAwareLocator storeAwareLocator;

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
        try {
            if (Boolean.TRUE.equals(reportQuery.getProcessTemplate())) {
                query = processQueryTemplate(query, parentBand, params);
            }

            List<OutputValue> outputParameters = parseQueryOutputParametersNames(query);

            query = query.replaceAll("(?i)" + ALIAS_PATTERN + ",", ",");//replaces [as alias_name], entries except last
            query = query.replaceAll("(?i)" + ALIAS_PATTERN, " ");//replaces last [as alias_name] entry

            List queryResult = executeQuery(parentBand, params, storeName, query);
            if (CollectionUtils.isNotEmpty(queryResult) && queryResult.get(0) instanceof Entity) {
                List<Map<String, Object>> wrappedResults = new ArrayList<>();
                for (Object theResult : queryResult) {
                    wrappedResults.add(new EntityMap((Entity) theResult, beanFactory));
                }
                return wrappedResults;
            } else {
                return fillOutputData(queryResult, outputParameters);
            }
        } catch (Throwable e) {
            throw new DataLoadingException(String.format("An error occurred while loading data for data set [%s]", reportQuery.getName()), e);
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

    protected String trimQuery(String query) {
        if (query.endsWith(";")) {
            return query.substring(0, query.length() - 1);
        } else {
            return query;
        }
    }
}
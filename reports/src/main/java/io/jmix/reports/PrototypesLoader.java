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

package io.jmix.reports;

import io.jmix.core.*;
import io.jmix.core.common.util.StringHelper;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.app.ParameterPrototype;
import io.jmix.reports.exception.ReportingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("report_PrototypesLoader")
public class PrototypesLoader {

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected ReportsProperties reportsProperties;

    @Autowired
    protected Metadata metadata;

    /**
     * Load parameter data
     *
     * @param parameterPrototype Parameter prototype
     * @return Entities list
     */
    public List loadData(ParameterPrototype parameterPrototype) {
        MetaClass metaClass = metadata.getSession().getClass(parameterPrototype.getMetaClassName());

        FetchPlan queryFetchPlan = parameterPrototype.getFetchPlan();
        if (queryFetchPlan == null) {
           queryFetchPlan = fetchPlanRepository.getFetchPlan(metaClass, parameterPrototype.getFetchPlanName());
        }

        LoadContext loadContext = new LoadContext(metaClass);

        LoadContext.Query query = new LoadContext.Query(parameterPrototype.getQueryString());

        query.setParameters(parameterPrototype.getQueryParams());
        query.setCondition(parameterPrototype.getCondition());
        query.setSort(parameterPrototype.getSort());
        query.setFirstResult(parameterPrototype.getFirstResult() == null ? 0 : parameterPrototype.getFirstResult());

        if (parameterPrototype.getMaxResults() != null && !parameterPrototype.getMaxResults().equals(0)) {
            query.setMaxResults(parameterPrototype.getMaxResults());
        } else {
            query.setMaxResults(reportsProperties.getParameterPrototypeQueryLimit());
        }

        loadContext.setFetchPlan(queryFetchPlan);
        loadContext.setQuery(query);
        List queryResult;
        try {
            queryResult = dataManager.loadList(loadContext);
        } catch (Exception e) {
            throw new ReportingException(e);
        }

        return queryResult;
    }

    private String printQuery(String query) {
        if (query == null)
            return null;
        else
            return StringHelper.removeExtraSpaces(query.replace("\n", " "));
    }
}
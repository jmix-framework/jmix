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

package io.jmix.reports;

import com.haulmont.cuba.core.global.*;
import io.jmix.core.FetchPlan;
import io.jmix.core.common.util.StringHelper;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.app.ParameterPrototype;
import io.jmix.reports.exception.ReportingException;

import java.util.List;

public class PrototypesLoader {

    /**
     * Load parameter data
     *
     * @param parameterPrototype Parameter prototype
     * @return Entities list
     */
    public List loadData(ParameterPrototype parameterPrototype) {
        Metadata metadata = AppBeans.get(Metadata.class);

        MetaClass metaClass = metadata.getSession().getClassNN(parameterPrototype.getMetaClassName());
        FetchPlan queryView = metadata.getViewRepository().getView(metaClass, parameterPrototype.getViewName());

        DataManager dataManager = AppBeans.get(DataManager.NAME);
        LoadContext loadContext = LoadContext.create(metaClass.getJavaClass());

        LoadContext.Query query = new LoadContext.Query(parameterPrototype.getQueryString());

        query.setParameters(parameterPrototype.getQueryParams());
        query.setCondition(parameterPrototype.getCondition());
        query.setSort(parameterPrototype.getSort());
        query.setFirstResult(parameterPrototype.getFirstResult() == null ? 0 : parameterPrototype.getFirstResult());

        if (parameterPrototype.getMaxResults() != null && !parameterPrototype.getMaxResults().equals(0)) {
            query.setMaxResults(parameterPrototype.getMaxResults());
        } else {
            Configuration configuration = AppBeans.get(Configuration.NAME);
            ReportingConfig config = configuration.getConfig(ReportingConfig.class);
            query.setMaxResults(config.getParameterPrototypeQueryLimit());
        }

        loadContext.setView(queryView);
        loadContext.setQuery(query);
        List queryResult;
        try {
            dataManager = dataManager.secure();
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
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

import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.structure.ReportQuery;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.Entity;
import io.jmix.reports.Reports;
import io.jmix.reports.entity.DataSet;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractEntityDataLoader implements ReportDataLoader {

    @Autowired
    protected BeanFactory beanFactory;

    @Autowired
    protected Reports reports;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    protected Entity reloadEntityByDataSetView(ReportQuery reportQuery, Object inputObject) {
        Entity entity = null;
        if (inputObject instanceof Entity && reportQuery instanceof DataSet) {
            entity = (Entity) inputObject;
            DataSet dataSet = (DataSet) reportQuery;
            FetchPlan view = getView(entity, dataSet);
            if (view != null) {
                entity = reports.reloadEntity(entity, view);
            }
        }

        return entity;
    }

    protected FetchPlan getView(Entity entity, DataSet dataSet) {
        FetchPlan view;
        if (Boolean.TRUE.equals(dataSet.getUseExistingView())) {
            view = fetchPlanRepository.getFetchPlan(entity.getClass(), dataSet.getViewName());
        } else {
            view = dataSet.getFetchPlan();
        }
        return view;
    }
}

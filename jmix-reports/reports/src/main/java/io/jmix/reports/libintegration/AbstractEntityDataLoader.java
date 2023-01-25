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

import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.structure.ReportQuery;
import io.jmix.core.*;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.Report;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

public abstract class AbstractEntityDataLoader implements ReportDataLoader {

    @Autowired
    protected BeanFactory beanFactory;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected EntityStates entityStates;

    @Nullable
    protected Entity reloadEntityByDataSetFetchPlan(ReportQuery reportQuery, Object inputObject) {
        Entity entity = null;
        if (inputObject instanceof Entity && reportQuery instanceof DataSet) {
            entity = (Entity) inputObject;
            if (!metadataTools.isJpaEntity(entity.getClass())) {
                // Don't reload DTO
                return entity;
            }

            DataSet dataSet = (DataSet) reportQuery;
            FetchPlan fetchPlan = getFetchPlan(entity, dataSet);
            if (fetchPlan != null) {
                if(!entityStates.isLoadedWithFetchPlan(entity, fetchPlan)) {
                    entity = reloadEntity(entity, fetchPlan);
                }
            }
        }

        return entity;
    }
    protected  <T> T reloadEntity(T entity, FetchPlan fetchPlan) {
        if (entity instanceof Report && ((Report) entity).getIsTmp()) {
            return entity;
        }
        return (T) dataManager.load(entity.getClass())
                .id(Id.of(entity))
                .fetchPlan(fetchPlan)
                .one();
    }

    @Nullable
    protected FetchPlan getFetchPlan(Entity entity, DataSet dataSet) {
        FetchPlan fetchPlan;
        if (Boolean.TRUE.equals(dataSet.getUseExistingFetchPLan())) {
            fetchPlan = fetchPlanRepository.getFetchPlan(entity.getClass(), dataSet.getFetchPlanName());
        } else {
            fetchPlan = dataSet.getFetchPlan();
        }
        return fetchPlan;
    }
}

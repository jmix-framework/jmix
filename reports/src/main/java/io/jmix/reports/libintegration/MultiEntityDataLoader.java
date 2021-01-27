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

import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ProxyWrapper;
import com.haulmont.yarg.structure.ReportQuery;
import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import io.jmix.core.entity.EntityValues;
import io.jmix.reports.app.EntityMap;
import io.jmix.reports.entity.DataSet;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MultiEntityDataLoader extends AbstractEntityDataLoader {

    public static final String DEFAULT_LIST_ENTITIES_PARAM_NAME = "entities";
    public static final String NESTED_COLLECTION_SEPARATOR = "#";

    @Override
    public List<Map<String, Object>> loadData(ReportQuery dataSet, BandData parentBand, Map<String, Object> params) {
        Map<String, Object> additionalParams = dataSet.getAdditionalParams();
        String paramName = (String) additionalParams.get(DataSet.LIST_ENTITIES_PARAM_NAME);
        if (StringUtils.isBlank(paramName)) {
            paramName = DEFAULT_LIST_ENTITIES_PARAM_NAME;
        }

        boolean hasNestedCollection = paramName.contains(NESTED_COLLECTION_SEPARATOR);
        String entityParameterName = StringUtils.substringBefore(paramName, NESTED_COLLECTION_SEPARATOR);
        String nestedCollectionName = StringUtils.substringAfter(paramName, NESTED_COLLECTION_SEPARATOR);
        FetchPlan nestedCollectionView = null;

        dataSet = ProxyWrapper.unwrap(dataSet);
        Object entities = null;
        if (params.containsKey(paramName)) {
            entities = params.get(paramName);
        } else if (hasNestedCollection && params.containsKey(entityParameterName)) {
            Entity entity = (Entity) params.get(entityParameterName);
            entity = reloadEntityByDataSetView(dataSet, entity);
            if (entity != null) {
                entities = EntityValues.getValueEx(entity, nestedCollectionName);
                if (dataSet instanceof DataSet) {
                    FetchPlan entityView = getView(entity, (DataSet) dataSet);
                    if (entityView != null && entityView.getProperty(nestedCollectionName) != null) {
                        //noinspection ConstantConditions
                        nestedCollectionView = entityView.getProperty(nestedCollectionName).getFetchPlan();
                    }
                }
            }
        }

        if (!(entities instanceof Collection)) {
            if (hasNestedCollection) {
                throw new IllegalStateException(
                        String.format("Input parameters do not contain '%s' parameter, " +
                                "or the entity does not contain nested collection '%s'", entityParameterName, nestedCollectionName)
                );
            } else {
                throw new IllegalStateException(
                        String.format("Input parameters do not contain '%s' parameter or it has type other than collection", paramName)
                );
            }
        }

        Collection<Entity> entitiesList = (Collection) entities;
        params.put(paramName, entitiesList);

        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Entity entity : entitiesList) {
            if (!hasNestedCollection) {
                entity = reloadEntityByDataSetView(dataSet, entity);
            }
            if (dataSet instanceof DataSet) {
                if (hasNestedCollection) {
                    if (nestedCollectionView != null) {
                        resultList.add(new EntityMap(entity, nestedCollectionView, beanFactory));
                    } else {
                        resultList.add(new EntityMap(entity, beanFactory));
                    }
                } else {
                    resultList.add(new EntityMap(entity, getView(entity, (DataSet) dataSet), beanFactory));
                }
            } else {
                resultList.add(new EntityMap(entity, beanFactory));
            }
        }
        return resultList;
    }
}

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

import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ProxyWrapper;
import com.haulmont.yarg.structure.ReportQuery;
import io.jmix.core.Entity;
import io.jmix.reports.app.EntityMap;
import io.jmix.reports.entity.DataSet;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SingleEntityDataLoader extends AbstractEntityDataLoader {

    public static final String DEFAULT_ENTITY_PARAM_NAME = "entity";

    @Override
    public List<Map<String, Object>> loadData(ReportQuery dataSet, BandData parentBand, Map<String, Object> params) {
        Map<String, Object> additionalParams = dataSet.getAdditionalParams();
        String paramName = (String) additionalParams.get(DataSet.ENTITY_PARAM_NAME);
        if (StringUtils.isBlank(paramName)) {
            paramName = DEFAULT_ENTITY_PARAM_NAME;
        }

        Object entity = params.get(paramName);

        if (entity == null) {
            throw new IllegalStateException(
                    String.format("Input parameters do not contain '%s' parameter", paramName)
            );
        }

        dataSet = ProxyWrapper.unwrap(dataSet);
        entity = reloadEntityByDataSetFetchPlan(dataSet, entity);
        params.put(paramName, entity);

        EntityMap result;
        if (dataSet instanceof DataSet) {
            result = new EntityMap((Entity) entity, getFetchPlan((Entity) entity, (DataSet) dataSet), beanFactory);
        } else {
            result = new EntityMap((Entity) entity, beanFactory);
        }
        return Collections.singletonList(result);
    }


}

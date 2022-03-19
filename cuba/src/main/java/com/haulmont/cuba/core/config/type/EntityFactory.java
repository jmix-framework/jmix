/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.core.config.type;

import io.jmix.core.DataManager;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.LoadContext;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

@Component(TypeFactory.ENTITY_FACTORY_BEAN_NAME)
public class EntityFactory extends TypeFactory {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private FetchPlanRepository fetchPlanRepository;

    @Override
    public Object build(String string) {
        if (StringUtils.isBlank(string))
            return null;
        EntityLoadInfo info = EntityLoadInfo.parse(string);
        if (info == null)
            throw new IllegalArgumentException("Invalid entity info: " + string);

        LoadContext<?> ctx = new LoadContext<>(info.getMetaClass()).setId(info.getId());
        if (info.getViewName() != null)
            ctx.setFetchPlan(fetchPlanRepository.getFetchPlan(info.getMetaClass(), info.getViewName()));
        return dataManager.load(ctx);
    }
}

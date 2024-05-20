/*
 * Copyright 2024 Haulmont.
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

package io.jmix.supersetflowui.component;

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.superset.SupersetAccessTokenManager;
import io.jmix.superset.SupersetProperties;
import io.jmix.supersetflowui.component.dataconstraint.DatasetConstrainsProvider;
import io.jmix.supersetflowui.component.dataconstraint.DatasetConstraint;
import jakarta.annotation.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import io.jmix.supersetflowui.kit.component.JmixSupersetDashboard;

import java.util.List;

public class SupersetDashboard extends JmixSupersetDashboard implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected CurrentUserSubstitution currentUserSubstitution;
    protected SupersetProperties supersetProperties;
    protected SupersetAccessTokenManager accessTokenManager;

    protected DatasetConstrainsProvider datasetConstrainsProvider;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        currentUserSubstitution = applicationContext.getBean(CurrentUserSubstitution.class);
        supersetProperties = applicationContext.getBean(SupersetProperties.class);
        accessTokenManager = applicationContext.getBean(SupersetAccessTokenManager.class);

        setUrlInternal(supersetProperties.getUrl());
        setAccessToken(accessTokenManager.getAccessToken());
        setUserInfo(currentUserSubstitution.getEffectiveUser().getUsername());
    }

    @Nullable
    public DatasetConstrainsProvider getDatasetConstrainsProvider() {
        return datasetConstrainsProvider;
    }

    public void setDatasetConstrainsProvider(@Nullable DatasetConstrainsProvider datasetConstrainsProvider) {
        this.datasetConstrainsProvider = datasetConstrainsProvider;

        if (datasetConstrainsProvider != null) {
            setDatasetConstraints(convertDatasetConstrainsToJson(datasetConstrainsProvider.getConstraints()));
        }
    }

    public void forceEmbed() {
        requestEmbedComponent();
    }

    protected JsonValue convertDatasetConstrainsToJson(List<DatasetConstraint> dataConstraints) {
        JreJsonFactory factory = new JreJsonFactory();
        JsonArray array = factory.createArray();
        for (int i = 0; i < dataConstraints.size(); i++) {
            DatasetConstraint dataConstraint = dataConstraints.get(0);
            JsonObject constraint = factory.createObject();
            constraint.put("dataset", dataConstraint.dataset());
            constraint.put("clause", dataConstraint.clause());
            array.set(i, constraint);
        }
        return array;
    }

    @Override
    protected String fetchAccessToken() {
        return accessTokenManager.getAccessToken();
    }
}

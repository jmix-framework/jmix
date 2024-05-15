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

import com.vaadin.flow.component.DetachEvent;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.superset.SupersetAccessTokenManager;
import io.jmix.superset.SupersetProperties;
import io.jmix.superset.event.SupersetAccessTokenUpdated;
import io.jmix.supersetflowui.component.dataconstraint.SupersetDataConstrainsProvider;
import io.jmix.supersetflowui.component.dataconstraint.SupersetDataConstraint;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import io.jmix.supersetflowui.kit.component.JmixSupersetDashboard;

import java.util.List;

public class SupersetDashboard extends JmixSupersetDashboard implements ApplicationContextAware, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(SupersetDashboard.class);

    protected ApplicationContext applicationContext;
    protected CurrentUserSubstitution currentUserSubstitution;
    protected SupersetProperties supersetProperties;
    protected SupersetAccessTokenManager accessTokenManager;

    protected SupersetDataConstrainsProvider dataConstrainsProvider;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        currentUserSubstitution = applicationContext.getBean(CurrentUserSubstitution.class);
        supersetProperties = applicationContext.getBean(SupersetProperties.class);
        accessTokenManager = applicationContext.getBean(SupersetAccessTokenManager.class);

        initAccessTokenUpdatedListener();
        setSupersetDomainInternal(supersetProperties.getUrl());
        setAccessToken(accessTokenManager.getAccessToken());
        setUserInfo(currentUserSubstitution.getEffectiveUser().getUsername());
    }

    protected void initAccessTokenUpdatedListener() {
        accessTokenManager.addAccessTokenUpdatedListener(this::onSupersetAccessTokenUpdated);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        accessTokenManager.removeAccessTokenUpdatedListener(this::onSupersetAccessTokenUpdated);
    }

    @Nullable
    public SupersetDataConstrainsProvider getDataConstrainsProvider() {
        return dataConstrainsProvider;
    }

    public void setDataConstrainsProvider(@Nullable SupersetDataConstrainsProvider dataConstrainsProvider) {
        this.dataConstrainsProvider = dataConstrainsProvider;

        if (dataConstrainsProvider != null) {
            setDataConstraints(convertDataConstrainsToJson(dataConstrainsProvider.getConstraints()));
        }
    }

    public void forceEmbed() {
        requestEmbedComponent();
    }

    protected JsonValue convertDataConstrainsToJson(List<SupersetDataConstraint> dataConstraints) {
        JreJsonFactory factory = new JreJsonFactory();
        JsonArray array = factory.createArray();
        for (int i = 0; i < dataConstraints.size(); i++) {
            SupersetDataConstraint dataConstraint = dataConstraints.get(0);
            JsonObject constraint = factory.createObject();
            constraint.put("dataset", dataConstraint.dataset());
            constraint.put("clause", dataConstraint.clause());
            array.set(i, constraint);
        }
        return array;
    }

    protected void onSupersetAccessTokenUpdated(SupersetAccessTokenUpdated event) {
        getUI().ifPresent(ui -> {
            ui.access(() -> setAccessToken(event.getAccessToken()));
        });
    }
}

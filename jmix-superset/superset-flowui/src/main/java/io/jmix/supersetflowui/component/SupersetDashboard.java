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

import com.google.common.base.Strings;
import com.vaadin.flow.component.AttachEvent;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.backgroundtask.BackgroundWorker;
import io.jmix.superset.schedule.SupersetTokenManager;
import io.jmix.superset.SupersetProperties;
import io.jmix.superset.service.SupersetService;
import io.jmix.superset.service.model.GuestTokenBody;
import io.jmix.supersetflowui.GuestTokenHandler;
import io.jmix.supersetflowui.component.dataconstraint.DatasetConstrainsProvider;
import io.jmix.supersetflowui.component.dataconstraint.DatasetConstraint;
import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import io.jmix.supersetflowui.kit.component.JmixSupersetDashboard;

import java.util.Collections;
import java.util.List;

import static io.jmix.superset.service.model.GuestTokenBody.Resource.DASHBOARD_TYPE;

public class SupersetDashboard extends JmixSupersetDashboard implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected CurrentUserSubstitution currentUserSubstitution;
    protected SupersetProperties supersetProperties;
    protected GuestTokenHandler guestTokenHandler;

    protected DatasetConstrainsProvider datasetConstrainsProvider;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        currentUserSubstitution = applicationContext.getBean(CurrentUserSubstitution.class);
        supersetProperties = applicationContext.getBean(SupersetProperties.class);
        guestTokenHandler = applicationContext.getBean(GuestTokenHandler.class,
                applicationContext.getBean(SupersetService.class),
                applicationContext.getBean(SupersetTokenManager.class),
                applicationContext.getBean(BackgroundWorker.class));

        setUrlInternal(supersetProperties.getUrl());
    }

    /**
     * @return dataset constraints provider or {@code null} if not set
     */
    @Nullable
    public DatasetConstrainsProvider getDatasetConstrainsProvider() {
        return datasetConstrainsProvider;
    }

    /**
     * Sets dataset constraints provider. These constraints are applied to datasets that are used in an embedded
     * dashboard {@link #getEmbeddedId()}.
     *
     * @param datasetConstrainsProvider dataset constraints providers
     */
    public void setDatasetConstrainsProvider(@Nullable DatasetConstrainsProvider datasetConstrainsProvider) {
        this.datasetConstrainsProvider = datasetConstrainsProvider;
    }

    @Override
    protected void refreshGuestToken() {
        super.refreshGuestToken();

        startGuestTokenRefreshing();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Do not start internal guest token if custom is defined
        if (Strings.isNullOrEmpty(getGuestToken())) {
            startGuestTokenRefreshing();
        }
    }

    protected void startGuestTokenRefreshing() {
        guestTokenHandler.requestGuestToken(buildGuestTokenBody(),
                response -> setGuestTokenInternal(response.getToken()));
    }

    protected GuestTokenBody buildGuestTokenBody() {
        if (Strings.isNullOrEmpty(getEmbeddedId())) {
            throw new IllegalStateException("Embedded id is required");
        }

        List<GuestTokenBody.RowLevelRole> rls = Collections.emptyList();
        if (datasetConstrainsProvider != null) {
            rls = convertToSupersetRls(datasetConstrainsProvider.getConstraints());
        }

        return GuestTokenBody.builder()
                .withResource(new GuestTokenBody.Resource()
                        .withId(getEmbeddedId())
                        .withType(DASHBOARD_TYPE))
                .withRowLevelRoles(rls)
                .withUser(new GuestTokenBody.User()
                        .withUsername(currentUserSubstitution.getEffectiveUser().getUsername()))
                .build();
    }

    protected List<GuestTokenBody.RowLevelRole> convertToSupersetRls(List<DatasetConstraint> datasetConstraints) {
        return CollectionUtils.isNotEmpty(datasetConstraints)
                ? datasetConstraints.stream()
                .map(dc -> new GuestTokenBody.RowLevelRole()
                        .withClause(dc.clause())
                        .withDataset(dc.dataset()))
                .toList()
                : Collections.emptyList();
    }
}

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
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.BackgroundWorker;
import io.jmix.flowui.backgroundtask.TaskLifeCycle;
import io.jmix.superset.SupersetProperties;
import io.jmix.superset.SupersetTokenManager;
import io.jmix.superset.client.SupersetClient;
import io.jmix.superset.client.model.GuestTokenBody;
import io.jmix.superset.client.model.GuestTokenResponse;
import io.jmix.supersetflowui.SupersetUiProperties;
import io.jmix.supersetflowui.component.dataconstraint.DatasetConstraint;
import io.jmix.supersetflowui.component.dataconstraint.DatasetConstraintsProvider;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import io.jmix.supersetflowui.kit.component.JmixSupersetDashboard;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.jmix.superset.client.model.GuestTokenBody.Resource.DASHBOARD_TYPE;

/**
 * The component for showing embedded dashboards from Superset. It uses the embedded-sdk library on the client-side to
 * embed a dashboard into an IFrame.
 * <p>
 * To work with the component correctly, you should provide the embedded ID from a configured dashboard in Superset
 * and set it to the component.
 * <p>
 * By default, the component manages guest token acquisition and refresh requests. It configures the request according
 * to the Superset API and contains the following information: dataset constraints, the embedded ID, and the current
 * username. The client-side of the component handles token expiration time. Before expiration, the component requests
 * fetching a new token in a non-blocking manner.
 * <p>
 * The example of component in a view descriptor:
 * <pre>
 * xmlns:superset="http://jmix.io/schema/superset/ui"
 *
 * &lt;superset:dashboard id="dashboard"
 *                     height="100%"
 *                     width="100%"
 *                     embeddedId="d8bb6f86-dfef-4049-a39a-fd73da1a601d"/&gt;
 * </pre>
 */
public class SupersetDashboard extends JmixSupersetDashboard implements ApplicationContextAware, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(SupersetDashboard.class);

    protected ApplicationContext applicationContext;
    protected SupersetClient supersetClient;
    protected SupersetTokenManager tokenManager;
    protected BackgroundWorker backgroundWorker;
    protected CurrentUserSubstitution currentUserSubstitution;
    protected SupersetUiProperties supersetUiProperties;

    protected DatasetConstraintsProvider datasetConstraintsProvider;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        supersetClient = applicationContext.getBean(SupersetClient.class);
        tokenManager = applicationContext.getBean(SupersetTokenManager.class);
        backgroundWorker = applicationContext.getBean(BackgroundWorker.class);
        currentUserSubstitution = applicationContext.getBean(CurrentUserSubstitution.class);
        supersetUiProperties = applicationContext.getBean(SupersetUiProperties.class);

        setUrlInternal(applicationContext.getBean(SupersetProperties.class).getUrl());
    }

    /**
     * @return dataset constraints provider or {@code null} if not set
     */
    @Nullable
    public DatasetConstraintsProvider getDatasetConstraintsProvider() {
        return datasetConstraintsProvider;
    }

    /**
     * Sets dataset constraints provider. These constraints are applied to datasets that are used in an embedded
     * dashboard {@link #getEmbeddedId()}.
     *
     * @param datasetConstraintsProvider dataset constraints providers
     */
    public void setDatasetConstraintsProvider(@Nullable DatasetConstraintsProvider datasetConstraintsProvider) {
        this.datasetConstraintsProvider = datasetConstraintsProvider;
    }

    @Override
    protected void fetchGuestToken() {
        super.fetchGuestToken();

        if (Strings.isNullOrEmpty(tokenManager.getAccessToken())) {
            log.error("Cannot request guest token, no access token provided");
            return;
        }
        if (Strings.isNullOrEmpty(getEmbeddedId())) {
            log.error("Cannot request guest token, embedded ID is not set");
            return;
        }

        FetchGuestTokenTask guestTokenTask = createFetchGuestTokenTask(
                supersetUiProperties.getBackgroundFetchingGuestTokenTimeout().toMillis(),
                buildGuestTokenBody(getEmbeddedId(), datasetConstraintsProvider),
                tokenManager.getAccessToken(),
                tokenManager.getCsrfToken());

        backgroundWorker.handle(guestTokenTask).execute();
    }

    protected GuestTokenBody buildGuestTokenBody(String embeddedID,
                                                 @Nullable DatasetConstraintsProvider constraintsProvider) {
        List<GuestTokenBody.RowLevelRole> rls = Collections.emptyList();
        if (constraintsProvider != null) {
            rls = convertToSupersetRls(constraintsProvider.getConstraints());
        }

        return GuestTokenBody.builder()
                .withResource(new GuestTokenBody.Resource()
                        .withId(embeddedID)
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

    protected FetchGuestTokenTask createFetchGuestTokenTask(long timeout, GuestTokenBody body, String accessToken,
                                                            @Nullable String csrfToken) {
        return new FetchGuestTokenTask(timeout, body, accessToken, csrfToken);
    }

    protected class FetchGuestTokenTask extends BackgroundTask<Void, GuestTokenResponse> {
        protected GuestTokenBody body;
        protected String accessToken;
        protected String csrfToken;

        public FetchGuestTokenTask(long timeout, GuestTokenBody body, String accessToken, @Nullable String csrfToken) {
            super(timeout, TimeUnit.MILLISECONDS);

            this.body = body;
            this.accessToken = accessToken;
            this.csrfToken = csrfToken;
        }

        @Override
        public GuestTokenResponse run(TaskLifeCycle<Void> taskLifeCycle) throws IOException, InterruptedException {
            return supersetClient.fetchGuestToken(body, accessToken, csrfToken);
        }

        @Override
        public void done(GuestTokenResponse response) {
            if (!Strings.isNullOrEmpty(response.getMessage())
                    || !Strings.isNullOrEmpty(response.getSystemMessage())
                    || CollectionUtils.isNotEmpty(response.getErrors())) {
                if (isAccessTokenExpired(response.getSystemMessage())) {
                    // If access token is expired it means refresh access token request
                    // is failed in SupersetTokenManager. We do nothing.
                    log.error("Guest token request failed. Access token expired.");
                } else if (!Strings.isNullOrEmpty(response.getMessage())) {
                    log.error("Guest token request failed. Message from Superset: {}", response.getMessage());
                } else if (CollectionUtils.isNotEmpty(response.getErrors())) {
                    log.error("Guest token request failed. Errors from Superset: {}",
                            ArrayUtils.toString(response.getErrors()));
                } else {
                    log.error("Guest token request failed. Unexpected exception while getting guest token.");
                }
            } else {
                setGuestTokenInternal(response.getToken());
            }
        }

        protected boolean isAccessTokenExpired(@Nullable String message) {
            return "Token has expired".equals(message);
        }
    }
}

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

package io.jmix.supersetflowui;

import com.google.common.base.Strings;
import io.jmix.core.annotation.Internal;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.BackgroundWorker;
import io.jmix.flowui.backgroundtask.TaskLifeCycle;
import io.jmix.superset.SupersetTokenManager;
import io.jmix.superset.client.model.GuestTokenBody;
import io.jmix.superset.client.model.GuestTokenResponse;
import io.jmix.superset.client.SupersetClient;
import io.jmix.supersetflowui.component.SupersetDashboard;
import io.jmix.supersetflowui.component.dataconstraint.DatasetConstrainsProvider;
import io.jmix.supersetflowui.component.dataconstraint.DatasetConstraint;
import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static io.jmix.superset.client.model.GuestTokenBody.Resource.DASHBOARD_TYPE;

/**
 * The class fetches a guest token for the {@link SupersetDashboard} component in a non-blocking manner.
 *
 * @see SupersetTokenManager
 * @see SupersetClient
 */
@Internal
@Component("sprset_GuestTokenHandler")
public class DefaultGuestTokenProvider {
    private static final Logger log = LoggerFactory.getLogger(DefaultGuestTokenProvider.class);

    protected final SupersetClient supersetClient;
    protected final SupersetTokenManager tokenManager;
    protected final BackgroundWorker backgroundWorker;
    protected final CurrentUserSubstitution currentUserSubstitution;
    protected final SupersetFlowuiProperties supersetFlowuiProperties;

    public DefaultGuestTokenProvider(SupersetClient supersetClient,
                                     SupersetTokenManager tokenManager,
                                     BackgroundWorker backgroundWorker,
                                     CurrentUserSubstitution currentUserSubstitution,
                                     SupersetFlowuiProperties supersetFlowuiProperties) {
        this.supersetClient = supersetClient;
        this.tokenManager = tokenManager;
        this.backgroundWorker = backgroundWorker;
        this.currentUserSubstitution = currentUserSubstitution;
        this.supersetFlowuiProperties = supersetFlowuiProperties;
    }

    /**
     * Fetches a guest token from Superset. It does not block the UI thread.
     *
     * @param source   a component that requests a guest token
     * @param callback guest token callback, this callback should be invoked when a guest token is fetched
     */
    public void fetchGuestToken(SupersetDashboard source, Consumer<String> callback) {
        if (Strings.isNullOrEmpty(tokenManager.getAccessToken())) {
            log.error("Cannot request guest token, no access token provided");
            return;
        }
        if (Strings.isNullOrEmpty(source.getEmbeddedId())) {
            log.error("Cannot request guest token, embedded ID is not set");
            return;
        }

        GuestTokenBody body = buildGuestTokenBody(source.getEmbeddedId(), source.getDatasetConstrainsProvider());
        long timeout = supersetFlowuiProperties.getBackgroundFetchingGuestTokenTimeout().toMillis();

        GuestTokenTask guestTokenTask = createSupersetGuestTokenTask(timeout, body, tokenManager.getAccessToken(),
                tokenManager.getCsrfToken());

        guestTokenTask.addProgressListener(new BackgroundTask.ProgressListenerAdapter<>() {
            @Override
            public void onDone(GuestTokenResponse response) {
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
                    callback.accept(response.getToken());
                }
            }
        });
        backgroundWorker.handle(guestTokenTask).execute();
    }

    protected GuestTokenBody buildGuestTokenBody(String embeddedID,
                                                 @Nullable DatasetConstrainsProvider constrainsProvider) {
        List<GuestTokenBody.RowLevelRole> rls = Collections.emptyList();
        if (constrainsProvider != null) {
            rls = convertToSupersetRls(constrainsProvider.getConstraints());
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

    protected boolean isAccessTokenExpired(@Nullable String message) {
        return "Token has expired".equals(message);
    }

    protected GuestTokenTask createSupersetGuestTokenTask(long timeout, GuestTokenBody body, String accessToken,
                                                          @Nullable String csrfToken) {
        return new GuestTokenTask(timeout, body, accessToken, csrfToken);
    }

    protected class GuestTokenTask extends BackgroundTask<Void, GuestTokenResponse> {
        protected GuestTokenBody body;
        protected String accessToken;
        protected String csrfToken;

        public GuestTokenTask(long timeout, GuestTokenBody body, String accessToken, @Nullable String csrfToken) {
            super(timeout, TimeUnit.MILLISECONDS);

            this.body = body;
            this.accessToken = accessToken;
            this.csrfToken = csrfToken;
        }

        @Override
        public GuestTokenResponse run(TaskLifeCycle<Void> taskLifeCycle) throws IOException, InterruptedException {
            return supersetClient.fetchGuestToken(body, accessToken, csrfToken);
        }
    }
}

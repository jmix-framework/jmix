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
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.BackgroundWorker;
import io.jmix.flowui.backgroundtask.TaskLifeCycle;
import io.jmix.superset.schedule.SupersetTokenManager;
import io.jmix.superset.service.model.GuestTokenBody;
import io.jmix.superset.service.model.GuestTokenResponse;
import io.jmix.superset.service.SupersetService;
import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Consumer;

@Internal
@Component("sprset_GuestTokenHandler")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GuestTokenHandler {
    private static final Logger log = LoggerFactory.getLogger(GuestTokenHandler.class);

    protected final SupersetService supersetService;
    protected final SupersetTokenManager tokenManager;
    protected final BackgroundWorker backgroundWorker;

    public GuestTokenHandler(SupersetService supersetService,
                             SupersetTokenManager tokenManager,
                             BackgroundWorker backgroundWorker) {
        this.supersetService = supersetService;
        this.tokenManager = tokenManager;
        this.backgroundWorker = backgroundWorker;
    }

    public void requestGuestToken(GuestTokenBody body, Consumer<GuestTokenResponse> callback) {
        if (Strings.isNullOrEmpty(tokenManager.getAccessToken())) {
            log.error("Cannot request guest token, no access token provided");
            return;
        }

        GuestTokenTask guestTokenTask = createSupersetGuestTokenTask(body, tokenManager.getAccessToken(),
                tokenManager.getCsrfToken());

        guestTokenTask.addProgressListener(new BackgroundTask.ProgressListenerAdapter<>() {
            @Override
            public void onDone(GuestTokenResponse response) {
                if (!Strings.isNullOrEmpty(response.getMessage())
                        || !Strings.isNullOrEmpty(response.getSystemMessage())
                        || CollectionUtils.isNotEmpty(response.getErrors())) {
                    if (isAccessTokenExpired(response.getSystemMessage())) {
                        // If access token is expired it means refresh access token request
                        // is failed in AccessTokenManager. We do nothing.
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
                    callback.accept(response);
                }
            }
        });
        backgroundWorker.handle(guestTokenTask).execute();
    }

    protected boolean isAccessTokenExpired(@Nullable String message) {
        return "Token has expired".equals(message);
    }

    protected GuestTokenTask createSupersetGuestTokenTask(GuestTokenBody body, String accessToken, @Nullable String csrfToken) {
        return new GuestTokenTask(body, accessToken, csrfToken);
    }

    protected class GuestTokenTask extends BackgroundTask<Void, GuestTokenResponse> {
        protected GuestTokenBody body;
        protected String accessToken;
        protected String csrfToken;

        public GuestTokenTask(GuestTokenBody body, String accessToken, @Nullable String csrfToken) {
            super(Duration.ofMinutes(1).getSeconds());

            this.body = body;
            this.accessToken = accessToken;
            this.csrfToken = csrfToken;
        }

        @Override
        public GuestTokenResponse run(TaskLifeCycle<Void> taskLifeCycle) throws IOException, InterruptedException {
            return supersetService.fetchGuestToken(body, accessToken, csrfToken);
        }
    }
}

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
import io.jmix.core.common.util.Preconditions;
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

import java.time.Duration;
import java.util.function.Consumer;

@Component("sprset_GuestTokenHandler")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GuestTokenHandler {
    private static final Logger log = LoggerFactory.getLogger(GuestTokenHandler.class);

    protected final SupersetService supersetService;
    protected final SupersetTokenManager accessTokenManager;
    protected final BackgroundWorker backgroundWorker;

    public GuestTokenHandler(SupersetService supersetService,
                             SupersetTokenManager accessTokenManager,
                             BackgroundWorker backgroundWorker) {
        this.supersetService = supersetService;
        this.accessTokenManager = accessTokenManager;
        this.backgroundWorker = backgroundWorker;
    }

    public void requestGuestToken(GuestTokenBody body, Consumer<GuestTokenResponse> callback) {
        requestGuestToken(body)
                .onSuccess(result -> callback.accept(result.result()))
                .onFail(result -> {
                    GuestTokenResponse response = result.result();
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
                })
                .start();
    }

    protected OperationCallback<GuestTokenResponse> requestGuestToken(GuestTokenBody body) {
        GuestTokenTask guestTokenTask = createSupersetGuestTokenTask(body);

        OperationCallback<GuestTokenResponse> callback = new OperationCallback<>(guestTokenTask);

        guestTokenTask.addProgressListener(new BackgroundTask.ProgressListenerAdapter<>() {
            @Override
            public void onDone(GuestTokenResponse response) {
                if (!Strings.isNullOrEmpty(response.getMessage())
                        || !Strings.isNullOrEmpty(response.getSystemMessage())
                        || CollectionUtils.isNotEmpty(response.getErrors())) {
                    callback.fail(response);
                } else {
                    callback.success(response);
                }
            }
        });

        return callback;
    }

    protected boolean isAccessTokenExpired(@Nullable String message) {
        return "Token has expired".equals(message);
    }

    protected GuestTokenTask createSupersetGuestTokenTask(GuestTokenBody body) {
        return new GuestTokenTask(body, accessTokenManager.getAccessToken(), accessTokenManager.getCsrfToken());
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
        public GuestTokenResponse run(TaskLifeCycle<Void> taskLifeCycle) {
            // todo rp handle exceptions
            return supersetService.getGuestToken(body, accessToken, csrfToken);
        }
    }

    protected class OperationCallback<T> {

        protected Consumer<OperationResult<T>> failCallback;
        protected Consumer<OperationResult<T>> successCallback;
        protected Consumer<OperationResult<T>> operationFinishCallback;

        protected boolean succeed = true;
        protected T result;

        protected BackgroundTask<?, ?> asyncTask;
        protected Runnable syncTask;

        public OperationCallback(Runnable task) {
            this.syncTask = task;
        }

        public OperationCallback(BackgroundTask<?, ?> task) {
            Preconditions.checkNotNullArgument(task);
            this.asyncTask = task;
        }

        public void fail(T result) {
            this.succeed = false;

            if (failCallback != null) {
                failCallback.accept(new OperationResult<>(result, false));
            }

            onFinish();
        }

        public void success(T result) {
            this.succeed = true;

            if (successCallback != null) {
                successCallback.accept(new OperationResult<>(result, true));
            }

            onFinish();
        }

        public OperationCallback<T> onFail(Consumer<OperationResult<T>> resultCallback) {
            this.failCallback = resultCallback;
            return this;
        }

        public OperationCallback<T> onSuccess(Consumer<OperationResult<T>> resultCallback) {
            this.successCallback = resultCallback;
            return this;
        }

        /**
         * Sets a callback that will be invoked in any case: success or fail, until the exception is not thrown
         * in previous callbacks.
         *
         * @param resultCallback callback to invoke after operation finish
         * @return current instance
         */
        public OperationCallback<T> onOperationFinish(Consumer<OperationResult<T>> resultCallback) {
            this.operationFinishCallback = resultCallback;
            return this;
        }

        public OperationResult<T> getResult() {
            return new OperationResult<>(result, succeed);
        }

        void start() {
            if (asyncTask != null) {
                backgroundWorker.handle(asyncTask).execute();
            } else if (syncTask != null) {
                syncTask.run();
            }
        }

        void onFinish() {
            if (operationFinishCallback != null) {
                operationFinishCallback.accept(new OperationResult<>(result, succeed));
            }
        }
    }

    protected record OperationResult<T>(T result, boolean succeed) {
    }
}

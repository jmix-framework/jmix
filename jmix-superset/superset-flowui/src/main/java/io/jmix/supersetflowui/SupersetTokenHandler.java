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
import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.BackgroundWorker;
import io.jmix.flowui.backgroundtask.TaskLifeCycle;
import io.jmix.superset.service.model.GuestTokenBody;
import io.jmix.superset.service.model.GuestTokenResponse;
import io.jmix.superset.service.model.LoginResponse;
import io.jmix.superset.service.SupersetService;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Component("superset_SupersetTokenHandler")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SupersetTokenHandler {
    private static final Logger log = LoggerFactory.getLogger(SupersetTokenHandler.class);

    protected final SupersetService supersetService;
    protected final BackgroundWorker backgroundWorker;

    public SupersetTokenHandler(SupersetService supersetService,
                                BackgroundWorker backgroundWorker) {
        this.supersetService = supersetService;
        this.backgroundWorker = backgroundWorker;
    }

    public void requestGuestToken(GuestTokenBody body, Consumer<GuestTokenResponse> callback) {
        if (Strings.isNullOrEmpty(getAccessToken())) {
            doLoginAndRequestGuestToken(body, callback);
        } else {
            doRequestGuestToken(body, callback);
        }
    }

    protected OperationCallback<LoginResponse> loginToSuperset() {
        SupersetTask<LoginResponse> loginTask = createSupersetLoginTask();

        OperationCallback<LoginResponse> operationResultCallback = new OperationCallback<>(loginTask);

        loginTask.addProgressListener(new BackgroundTask.ProgressListenerAdapter<>() {
            @Override
            public void onDone(LoginResponse response) {
                if (!Strings.isNullOrEmpty(response.getMessage())) {
                    operationResultCallback.fail(response);
                } else {
                    operationResultCallback.success(response);
                }
            }
        });

        return operationResultCallback;
    }

    protected void refreshAccessToken() {
        // todo rp
    }

    protected void doLoginAndRequestGuestToken(GuestTokenBody body, Consumer<GuestTokenResponse> callback) {
        new OperationsChain()
                .operation((prevOperation) ->
                        loginToSuperset()
                                .onOperationFinish(result -> {
                                    if (result.succeed()) {
                                        // todo rp when update cached tokens or remove?
                                        VaadinSession.getCurrent().setAttribute(LoginResponse.class, result.result());
                                    }
                                }))
                .operation((prevOperation) -> {
                    return prevOperation.getResult().succeed() ?
                            requestGuestToken(body)
                                    .onOperationFinish(result -> {
                                        if (result.succeed()) {
                                            // todo cache guest token for UI for View for Component
                                            callback.accept(result.result());
                                        } else {
                                            /*just fail todo error or log*/
                                        }
                                    })
                            : null; // Break the operations chain
                })
                .start();
    }

    protected void doRequestGuestToken(GuestTokenBody body, Consumer<GuestTokenResponse> callback) {
        new OperationsChain()
                .operation((prevOperation) ->
                        requestGuestToken(body)
                                .onOperationFinish(result -> {
                                    if (result.succeed()) {
                                        // todo cache guest token for UI for View for Component
                                        callback.accept(result.result());
                                    } else if (!Strings.isNullOrEmpty(result.result().getMessage())) {
                                        throw new IllegalStateException("Guest token request failed: "
                                                + result.result().getMessage());
                                    }
                                }))
                .operation(prevOperation -> {
                    if (!prevOperation.getResult().succeed()) {
                        GuestTokenResponse response = (GuestTokenResponse) prevOperation.getResult().result();
                        if (!Strings.isNullOrEmpty(response.getMsg())) {
                            // expired access token
                            doLoginAndRequestGuestToken(body, callback);
                        } else {
                            throw new IllegalStateException();
                        }
                    }
                    // Break the operations chain
                    return null;
                })
                .start();
    }

    protected OperationCallback<GuestTokenResponse> requestGuestToken(GuestTokenBody body) {
        SupersetTask<GuestTokenResponse> guestTokenTask = createSupersetGuestTokenTask(body);

        OperationCallback<GuestTokenResponse> callback = new OperationCallback<>(guestTokenTask);

        guestTokenTask.addProgressListener(new BackgroundTask.ProgressListenerAdapter<>() {
            @Override
            public void onDone(GuestTokenResponse response) {
                if (!Strings.isNullOrEmpty(response.getMessage()) || !Strings.isNullOrEmpty(response.getMsg())) {
                    callback.fail(response);
                } else {
                    callback.success(response);
                }
            }
        });

        return callback;
    }

    protected SupersetTask<LoginResponse> createSupersetLoginTask() {
        return new SupersetTask<>(supersetService, Collections.emptyMap()) {
            @Override
            public LoginResponse run(TaskLifeCycle taskLifeCycle) {
                return supersetService.login();
            }
        };
    }

    protected void updateAccessToken() {

    }

    protected SupersetTask<GuestTokenResponse> createSupersetGuestTokenTask(GuestTokenBody body) {
        // by logic here must be available access token
        String accessToken = getAccessToken();
        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalStateException("Cannot get guest token, since access token is null");
        }
        return new SupersetTask<>(supersetService, ParamsMap.of(
                "body", body,
                "accessToken", accessToken)) {
            @Override
            public GuestTokenResponse run(TaskLifeCycle<Void> taskLifeCycle) {
                // todo rp when update cached tokens or remove?

                return supersetService.getGuestToken(body, (String) params.get("accessToken"));
            }
        };
    }

    @Nullable
    protected String getAccessToken() {
        LoginResponse loginResponse = VaadinSession.getCurrent().getAttribute(LoginResponse.class);
        return loginResponse != null
                ? loginResponse.getAccessToken()
                : null;
    }

    protected static abstract class SupersetTask<R> extends BackgroundTask<Void, R> {
        protected SupersetService supersetService;
        protected Map<String, Object> params;

        public SupersetTask(SupersetService supersetService, Map<String, Object> params) {
            super(Duration.ofMinutes(1).getSeconds());

            this.supersetService = supersetService;
            this.params = params;
        }
    }

    protected static class OperationsChain {

        protected List<Function<OperationCallback<?>, OperationCallback<?>>> operationInvokers =
                new ArrayList<>(2);

        protected OperationCallback<?> previousResult;

        OperationsChain operation(Function<OperationCallback<?>, OperationCallback<?>> operationInvoker) {
            operationInvokers.add(operationInvoker);
            return this;
        }

        void start() {
            Function<OperationCallback<?>, OperationCallback<?>> operationInvoker = operationInvokers.get(0);
            startRecursively(operationInvoker);
        }

        void startRecursively(Function<OperationCallback<?>, OperationCallback<?>> operationInvoker) {
            previousResult = operationInvoker.apply(previousResult);
            if (previousResult == null) {
                log.debug("The operation result is null. Next operations won't be started");
                return;
            }

            previousResult.onOperationFinishInternal(o -> {
                if (hasNextInvoker(operationInvoker)) {
                    Function<OperationCallback<?>, OperationCallback<?>> nextOperationInvoker =
                            operationInvokers.get(operationInvokers.indexOf(operationInvoker) + 1);
                    startRecursively(nextOperationInvoker);
                }
            });
            previousResult.start();
        }

        boolean hasNextInvoker(Function<OperationCallback<?>, OperationCallback<?>> operationInvoker) {
            int i = operationInvokers.indexOf(operationInvoker) + 1;
            return operationInvokers.size() > i;
        }
    }

    protected class OperationCallback<T> {

        protected Consumer<OperationResult<T>> failCallback;
        protected Consumer<OperationResult<T>> successCallback;
        protected Consumer<OperationResult<T>> operationFinishCallback;
        protected Consumer<OperationResult<T>> operationFinishInternalCallback;

        protected boolean succeed = true;
        protected T result;

        protected final SupersetTask<?> task;

        public OperationCallback(SupersetTask<?> task) {
            Preconditions.checkNotNullArgument(task);
            this.task = task;
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
         * Sets a callback that will be invoked in any case: success or fail.
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
            backgroundWorker.handle(task).execute();
        }

        void onFinish() {
            if (operationFinishCallback != null) {
                operationFinishCallback.accept(new OperationResult<>(result, succeed));
            }
            if (operationFinishInternalCallback != null) {
                operationFinishInternalCallback.accept(new OperationResult<>(result, succeed));
            }
        }

        /**
         * Sets a callback that will be invoked in any case: success or fail.
         *
         * @param resultCallback callback to invoke after operation finish
         * @return current instance
         */
        OperationCallback<T> onOperationFinishInternal(Consumer<OperationResult<T>> resultCallback) {
            this.operationFinishInternalCallback = resultCallback;
            return this;
        }
    }

    protected record OperationResult<T>(T result, boolean succeed) {
    }
}

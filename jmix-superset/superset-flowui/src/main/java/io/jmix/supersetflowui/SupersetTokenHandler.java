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
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.BackgroundWorker;
import io.jmix.flowui.backgroundtask.TaskLifeCycle;
import io.jmix.superset.SupersetService;
import io.jmix.superset.model.GuestTokenBody;
import io.jmix.superset.model.GuestTokenResponse;
import io.jmix.superset.model.LoginResponse;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component("superset_SupersetTokenHandler")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SupersetTokenHandler {

    protected final SupersetService supersetService;
    protected final BackgroundWorker backgroundWorker;

    public SupersetTokenHandler(SupersetService supersetService,
                                BackgroundWorker backgroundWorker) {
        this.supersetService = supersetService;
        this.backgroundWorker = backgroundWorker;
    }

    public void requestGuestToken(GuestTokenBody body, Consumer<GuestTokenResponse> callback) {
        new OperationOrder()
                .operation(() ->
                        loginToSuperset()
                                .onFail(result -> {
                                    throw new IllegalStateException("Failed to log in to Superset: \"" + result.getMessage() + "\"");
                                })
                                .onSuccess(result -> {
                                    // todo rp when update cached tokens or remove?
                                    VaadinSession.getCurrent().setAttribute(LoginResponse.class, result);
                                }))
                // after first operation success
                .operation(() ->
                        requestGuestToken(body)
                                .onFail(result -> {
                                    /*just fail*/
                                })
                                .onSuccess(result -> {
                                    // cache guest token for UI for View for Component
                                    callback.accept(result);
                                }))
                .start();
    }

    protected OperationResultCallback<LoginResponse> loginToSuperset() {
        OperationResultCallback<LoginResponse> operationResultCallback = new OperationResultCallback<>();

        SupersetTask<LoginResponse> loginTask = createSupersetLoginTask();
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
        backgroundWorker.handle(loginTask).execute();

        return operationResultCallback;
    }

    protected void refreshAccessToken() {
        // todo rp
    }

    protected OperationResultCallback<GuestTokenResponse> requestGuestToken(GuestTokenBody body) {
        OperationResultCallback<GuestTokenResponse> callback = new OperationResultCallback<>();

        SupersetTask<GuestTokenResponse> guestTokenTask = createSupersetGuestTokenTask(body);
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
        backgroundWorker.handle(guestTokenTask).execute();

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

    protected SupersetTask<GuestTokenResponse> createSupersetGuestTokenTask(GuestTokenBody body) {
        // by logic here should be available access token
        LoginResponse loginResponse = VaadinSession.getCurrent().getAttribute(LoginResponse.class);
        if (loginResponse == null) {
            throw new IllegalStateException("Cannot get guest token, since access token is null");
        }
        return new SupersetTask<>(supersetService, ParamsMap.of(
                "body", body,
                "accessToken", loginResponse.getAccessToken())) {
            @Override
            public GuestTokenResponse run(TaskLifeCycle<Void> taskLifeCycle) {
                // todo rp when update cached tokens or remove?

                return supersetService.getGuestToken(body, (String) params.get("accessToken"));
            }
        };
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

    protected static class OperationOrder {

        protected List<Supplier<OperationResultCallback<?>>> operationInvokers = new ArrayList<>(2);

        OperationOrder operation(Supplier<OperationResultCallback<?>> operationInvoker) {
            operationInvokers.add(operationInvoker);
            return this;
        }

        void start() {
            Supplier<OperationResultCallback<?>> operationInvoker = operationInvokers.get(0);
            startRecursively(operationInvoker);
        }

        void startRecursively(Supplier<OperationResultCallback<?>> operationInvoker) {
            OperationResultCallback<?> callback = operationInvoker.get();
            callback.onAfterSuccess(o -> {
                if (hasNextInvoker(operationInvoker)) {
                    Supplier<OperationResultCallback<?>> nextOperationInvoker =
                            operationInvokers.get(operationInvokers.indexOf(operationInvoker) + 1);
                    startRecursively(nextOperationInvoker);
                }
            });
        }

        boolean hasNextInvoker(Supplier<OperationResultCallback<?>> operationInvoker) {
            int i = operationInvokers.indexOf(operationInvoker) + 1;
            return operationInvokers.size() > i;
        }
    }

    protected static class OperationResultCallback<T> {

        protected Consumer<T> failCallback;
        protected Consumer<T> successCallback;
        protected Consumer<T> afterSuccessCallback;


        public void fail(T result) {
            if (failCallback != null) {
                failCallback.accept(result);
            }
        }

        public void success(T result) {
            if (successCallback != null) {
                successCallback.accept(result);

                if (afterSuccessCallback != null) {
                    afterSuccessCallback.accept(result);
                }
            }
        }

        public OperationResultCallback<T> onSuccess(Consumer<T> resultCallback) {
            this.successCallback = resultCallback;
            return this;
        }

        public void onAfterSuccess(Consumer<T> resultCallback) {
            this.afterSuccessCallback = resultCallback;
        }

        public OperationResultCallback<T> onFail(Consumer<T> resultCallback) {
            this.failCallback = resultCallback;
            return this;
        }
    }
}

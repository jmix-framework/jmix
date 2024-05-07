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
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.BackgroundWorker;
import io.jmix.flowui.backgroundtask.TaskLifeCycle;
import io.jmix.flowui.util.OperationResult;
import io.jmix.superset.SupersetService;
import io.jmix.superset.model.GuestTokenBody;
import io.jmix.superset.model.GuestTokenResponse;
import io.jmix.superset.model.LoginResponse;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Consumer;

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
        // todo do not sent request each time
        SupersetTask<LoginResponse> loginTask = createSupersetLoginTask();
        loginTask.addProgressListener(new SupersetLoginTaskListener());
        backgroundWorker.handle(loginTask);

        SupersetTask<GuestTokenResponse> guestTokenTask = createSupersetGuestTokenTask();
    }

    protected OperationResult startLoginToSuperset() {
    }

    protected SupersetTask<LoginResponse> createSupersetLoginTask() {
        return new SupersetTask<>(supersetService) {
            @Override
            public LoginResponse run(TaskLifeCycle taskLifeCycle) {
                return supersetService.login();
            }
        };
    }

    protected SupersetTask<GuestTokenResponse> createSupersetGuestTokenTask() {
        return new SupersetTask<>(supersetService) {
            @Override
            public GuestTokenResponse run(TaskLifeCycle<Void> taskLifeCycle) {
                // todo rp when update cached tokens or remove?
                LoginResponse loginResponse = VaadinSession.getCurrent().getAttribute(LoginResponse.class);
                return supersetService.getGuestToken(, loginResponse.getAccessToken());
            }
        };
    }

    protected static abstract class SupersetTask<R> extends BackgroundTask<Void, R> {
        protected SupersetService supersetService;
        public SupersetTask(SupersetService supersetService) {
            super(Duration.ofMinutes(1).getSeconds());

            this.supersetService = supersetService;
        }
    }

    protected class SupersetLoginTaskListener
            extends BackgroundTask.ProgressListenerAdapter<Void, LoginResponse> {
        @Override
        public void onDone(LoginResponse response) {
            if (!Strings.isNullOrEmpty(response.getMessage())) {
                throw new IllegalStateException("Failed to log in to Superset: \"" + response.getMessage() + "\"");
            }

            // todo rp when update cached tokens or remove?
            VaadinSession.getCurrent().setAttribute(LoginResponse.class, response);
        }
    }
}

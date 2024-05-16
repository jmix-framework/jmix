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

package io.jmix.superset;

import com.google.common.base.Strings;
import io.jmix.superset.event.SupersetAccessTokenUpdated;
import io.jmix.superset.service.model.LoginResponse;
import io.jmix.superset.service.model.RefreshResponse;
import io.jmix.superset.service.SupersetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Component("superset_SupersetAccessTokenManager")
public class SupersetAccessTokenManager {

    private static final Logger log = LoggerFactory.getLogger(SupersetAccessTokenManager.class);
    private final SupersetService supersetService;

    private final List<Consumer<SupersetAccessTokenUpdated>> listeners =
            Collections.synchronizedList(new ArrayList<>());

    private String accessToken;
    private String refreshToken;

    public SupersetAccessTokenManager(SupersetService supersetService) {
        this.supersetService = supersetService;
    }

    public synchronized void updateAccessToken() {
        boolean success;
        if (accessToken == null) {
            success = performLogin();
        } else {
            success = refreshAccessToken();
        }

        if (success) {
            publishAccessTokenUpdatedEvent(new SupersetAccessTokenUpdated(this, accessToken));
        }
    }

    public void addAccessTokenUpdatedListener(Consumer<SupersetAccessTokenUpdated> listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeAccessTokenUpdatedListener(Consumer<SupersetAccessTokenUpdated> listener) {
        listeners.remove(listener);
    }

    public String getAccessToken() {
        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalStateException("Access token is not initialized");
        }
        return accessToken;
    }

    public String getRefreshAccessToken() {
        if (Strings.isNullOrEmpty(refreshToken)) {
            throw new IllegalStateException("Refresh token is not initialized");
        }
        return refreshToken;
    }

    protected boolean performLogin() {
        LoginResponse response;
        try {
            response = supersetService.login();
        } catch (Exception e) {
            log.error("Cannot log in to superset. Dashboard functionality may work incorrectly", e);
            return false;
        }

        if (response.getMessage() == null) {
            accessToken = response.getAccessToken();
            refreshToken = response.getRefreshToken();
            return true;
        } else {
            log.error("Cannot log in to superset. Dashboard functionality may work incorrectly. Message from Superset:" +
                    " {}", response.getMessage());
            return false;
        }
    }

    protected boolean refreshAccessToken() {
        boolean retry = false;
        RefreshResponse response = null;
        try {
            response = supersetService.refresh(refreshToken);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (!Strings.isNullOrEmpty(cause.getMessage()) && cause.getMessage().contains("GOAWAY received")) {
                log.error("Failed to refresh access token. Retrying request");
                retry = true;
            } else {
                log.error("Failed to refresh access token. Dashboard functionality may work incorrectly", e);
                return false;
            }
        }

        if (retry) {
            try {
                response = supersetService.refresh(refreshToken);
            } catch (Exception e) {
                Throwable cause = e.getCause();
                if (!Strings.isNullOrEmpty(cause.getMessage()) && cause.getMessage().contains("GOAWAY received")) {
                    log.error("Retrying the request failed. Dashboard functionality may work incorrectly.", e);
                    return false;
                } else {
                    log.error("Failed to refresh access token. Dashboard functionality may work incorrectly", e);
                    return false;
                }
            }
        }

        // Response cannot be null here
        if (response.getErrorMessage() == null) {
            accessToken = response.getAccessToken();
            return true;
        } else {
            log.error("Failed to update access token. Dashboard functionality may work incorrectly. Message from Superset:" +
                    " {}", response.getErrorMessage());
            return false;
        }
    }

    private void publishAccessTokenUpdatedEvent(SupersetAccessTokenUpdated event) {
        for (Consumer<SupersetAccessTokenUpdated> listener : listeners) {
            listener.accept(event);
        }
    }
}

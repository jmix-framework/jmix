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
import io.jmix.superset.model.LoginResponse;
import io.jmix.superset.model.RefreshResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component("superset_SupersetAccessTokenManager")
public class SupersetAccessTokenManager {

    private static final Logger log = LoggerFactory.getLogger(SupersetAccessTokenManager.class);
    private final ApplicationEventPublisher eventPublisher;
    private final SupersetService supersetService;

    private String accessToken;
    private String refreshToken;

    public SupersetAccessTokenManager(ApplicationEventPublisher eventPublisher, SupersetService supersetService) {
        this.eventPublisher = eventPublisher;
        this.supersetService = supersetService;
    }

    public synchronized void updateAccessToken() {
        if (accessToken == null) {
            performLogin();
        } else {
            performRefresh();
        }

        eventPublisher.publishEvent(new SupersetAccessTokenUpdated(this, accessToken));
    }

    private void performLogin() {
        LoginResponse response;
        try {
            response = supersetService.login();
        } catch (Exception e) {
            log.error("Cannot log in to superset. Dashboard functionality may work incorrectly", e);
            return;
        }

        if (response.getMessage() == null) {
            accessToken = response.getAccessToken();
            refreshToken = response.getRefreshToken();
        } else {
            log.error("Cannot log in to superset. Dashboard functionality may work incorrectly. Message from Superset:" +
                    " {}", response.getMessage());
        }
    }

    private void performRefresh() {
        RefreshResponse response;
        try {
            response = supersetService.refresh(refreshToken);
        } catch (Exception e) {
            log.error("Failed to update access token. Dashboard functionality may work incorrectly", e);
            return;
        }

        if (response.getErrorMessage() == null) {
            accessToken = response.getAccessToken();
        } else {
            log.error("Failed to update access token. Dashboard functionality may work incorrectly. Message from Superset:" +
                    " {}", response.getErrorMessage());
        }
    }

    public String getAccessToken() {
        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalStateException("Access token is not initialized");
        }
        return accessToken;
    }

    public String refreshAccessToken() {
        if (Strings.isNullOrEmpty(refreshToken)) {
            throw new IllegalStateException("Refresh token is not initialized");
        }
        return refreshToken;
    }
}

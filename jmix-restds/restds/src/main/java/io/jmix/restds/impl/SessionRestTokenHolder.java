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

package io.jmix.restds.impl;

import io.jmix.core.session.SessionData;
import org.springframework.beans.factory.ObjectProvider;

public class SessionRestTokenHolder implements RestTokenHolder {

    private static final String ACCESS_TOKEN_SESSION_ATTR = SessionRestTokenHolder.class.getName() + ".accessToken";
    private static final String REFRESH_TOKEN_SESSION_ATTR = SessionRestTokenHolder.class.getName() + ".refreshToken";

    private final ObjectProvider<SessionData> sessionDataProvider;

    public SessionRestTokenHolder(ObjectProvider<SessionData> sessionDataProvider) {
        this.sessionDataProvider = sessionDataProvider;
    }

    @Override
    public String getAccessToken() {
        return (String) sessionDataProvider.getObject().getAttribute(ACCESS_TOKEN_SESSION_ATTR);
    }

    @Override
    public String getRefreshToken() {
        return (String) sessionDataProvider.getObject().getAttribute(REFRESH_TOKEN_SESSION_ATTR);
    }

    @Override
    public void setTokens(String accessToken, String refreshToken) {
        sessionDataProvider.getObject().setAttribute(ACCESS_TOKEN_SESSION_ATTR, accessToken);
        sessionDataProvider.getObject().setAttribute(REFRESH_TOKEN_SESSION_ATTR, refreshToken);
    }
}

/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.rest.api.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.http.ResponseEntity;

import javax.annotation.Nullable;

/**
 * Event fired when token has been revoked by client before HTTP response is sent.
 */
public class OAuthTokenRevokedResponseEvent extends ApplicationEvent {
    protected String requestedRevocationToken;
    protected String tokenValue;
    protected ResponseEntity responseEntity;

    public OAuthTokenRevokedResponseEvent(String requestedRevocationToken,
                                          @Nullable String tokenValue) {
        super(requestedRevocationToken);

        this.requestedRevocationToken = requestedRevocationToken;
        this.tokenValue = tokenValue;
    }

    @Override
    public String getSource() {
        return (String) super.getSource();
    }

    public String getRequestedRevocationToken() {
        return requestedRevocationToken;
    }

    @Nullable
    public String getTokenValue() {
        return tokenValue;
    }

    @Nullable
    public ResponseEntity getResponseEntity() {
        return responseEntity;
    }

    public void setResponseEntity(ResponseEntity responseEntity) {
        this.responseEntity = responseEntity;
    }
}

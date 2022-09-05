/*
 * Copyright 2021 Haulmont.
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

package io.jmix.oidc.resourceserver;

import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.Authentication;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * A copy of io.jmix.securityoauth2.event.BeforeInvocationEvent. Event fired before API call. Event listeners can
 * prevent a controller invocation using {@link #preventInvocation()} method.
 */
public class BeforeResourceServerApiInvocationEvent extends ApplicationEvent {
    private static final long serialVersionUID = 5865129356260466774L;

    private final ServletRequest request;
    private final ServletResponse response;
    private boolean invocationPrevented = false;
    private int errorCode;
    private String errorMessage;

    public BeforeResourceServerApiInvocationEvent(Authentication authentication,
                                                  ServletRequest request,
                                                  ServletResponse response) {
        super(authentication);
        this.request = request;
        this.response = response;
    }

    @Override
    public Authentication getSource() {
        return (Authentication) super.getSource();
    }

    public Authentication getAuthentication() {
        return (Authentication) super.getSource();
    }

    public ServletRequest getRequest() {
        return request;
    }

    public ServletResponse getResponse() {
        return response;
    }

    public boolean isInvocationPrevented() {
        return invocationPrevented;
    }

    public void preventInvocation() {
        this.invocationPrevented = true;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

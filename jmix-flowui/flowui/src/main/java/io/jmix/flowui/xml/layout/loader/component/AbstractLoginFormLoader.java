/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component;

import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginI18n;
import io.jmix.flowui.kit.component.loginform.JmixLoginI18n;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import org.dom4j.Element;

public abstract class AbstractLoginFormLoader<C extends AbstractLogin> extends AbstractComponentLoader<C> {

    @Override
    public void loadComponent() {
        componentLoader().loadEnabled(resultComponent, element);
        loadBoolean(element, "forgotPasswordButtonVisible", resultComponent::setForgotPasswordButtonVisible);

        loadLocalization(resultComponent, element);
    }

    protected void loadLocalization(C resultComponent, Element element) {
        JmixLoginI18n i18n = JmixLoginI18n.createDefault();

        loadHeader(resultComponent, element, i18n);
        loadForm(resultComponent, element, i18n);
        loadErrorMessage(resultComponent, element, i18n);
        loadAdditionalInformation(resultComponent, element, i18n);

        resultComponent.setI18n(i18n);
    }

    protected void loadHeader(C resultComponent, Element element, LoginI18n i18n) {
        Element headerElement = element.element("header");
        if (headerElement != null) {
            LoginI18n.Header header = new LoginI18n.Header();

            loadResourceString(headerElement, "title",
                    context.getMessageGroup(), header::setTitle);
            loadResourceString(headerElement, "description",
                    context.getMessageGroup(), header::setDescription);

            i18n.setHeader(header);
        }
    }

    protected void loadForm(C resultComponent, Element element, LoginI18n i18n) {
        Element formElement = element.element("form");
        if (formElement != null) {
            JmixLoginI18n.JmixForm form = new JmixLoginI18n.JmixForm();

            loadResourceString(formElement, "title",
                    context.getMessageGroup(), form::setTitle);
            loadResourceString(formElement, "username",
                    context.getMessageGroup(), form::setUsername);
            loadResourceString(formElement, "password",
                    context.getMessageGroup(), form::setPassword);
            loadResourceString(formElement, "submit",
                    context.getMessageGroup(), form::setSubmit);
            loadResourceString(formElement, "forgotPassword",
                    context.getMessageGroup(), form::setForgotPassword);
            loadResourceString(formElement, "rememberMe",
                    context.getMessageGroup(), form::setRememberMe);

            i18n.setForm(form);
        }
    }

    protected void loadErrorMessage(C resultComponent, Element element, LoginI18n i18n) {
        Element errorMessageElement = element.element("errorMessage");
        if (errorMessageElement != null) {
            LoginI18n.ErrorMessage errorMessage = new LoginI18n.ErrorMessage();

            loadResourceString(errorMessageElement, "title",
                    context.getMessageGroup(), errorMessage::setTitle);
            loadResourceString(errorMessageElement, "message",
                    context.getMessageGroup(), errorMessage::setMessage);

            i18n.setErrorMessage(errorMessage);
        }
    }

    protected void loadAdditionalInformation(C resultComponent, Element element, LoginI18n i18n) {
        Element additionalInformationElement = element.element("additionalInformation");
        if (additionalInformationElement != null) {
            loadResourceString(additionalInformationElement, "message",
                    context.getMessageGroup(), i18n::setAdditionalInformation);
        }
    }
}

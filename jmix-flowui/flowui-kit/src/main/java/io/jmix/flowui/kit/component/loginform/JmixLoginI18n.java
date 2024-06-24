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

package io.jmix.flowui.kit.component.loginform;

import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.internal.JsonSerializer;
import elemental.json.JsonFactory;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JmixLoginI18n extends LoginI18n {
    private JmixForm form;

    public static JmixLoginI18n createDefault() {
        JmixLoginI18n jmixLoginI18n = new JmixLoginI18n();

        JmixForm jmixForm = new JmixForm();
        jmixForm.setTitle("Log in");
        jmixForm.setUsername("Username");
        jmixForm.setPassword("Password");
        jmixForm.setSubmit("Log in");
        jmixForm.setForgotPassword("Forgot password");
        jmixForm.setRememberMe("Remember me");

        jmixLoginI18n.setForm(jmixForm);

        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setTitle("Incorrect username or password");
        errorMessage.setMessage("Check that you have entered the correct username and password and try again.");
        errorMessage.setUsername("Username is required");
        errorMessage.setPassword("Password is required");

        jmixLoginI18n.setErrorMessage(errorMessage);

        return jmixLoginI18n;
    }

    public void setForm(Form form) {
        if (form instanceof JmixForm jmixForm) {
            this.form = jmixForm;
        } else {
            throw new IllegalStateException("Setter doesn't support value of %s" + form.getClass());
        }
    }

    @Override
    public JmixForm getForm() {
        return form;
    }

    public static class JmixForm extends Form {

        protected String rememberMe;

        public String getRememberMe() {
            return rememberMe;
        }

        public void setRememberMe(String rememberMe) {
            this.rememberMe = rememberMe;
        }
    }
}

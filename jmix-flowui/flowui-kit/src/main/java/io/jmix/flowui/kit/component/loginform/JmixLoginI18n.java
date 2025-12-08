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

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;

/**
 * Internationalization object for customizing the component UI texts. An
 * instance with the default messages can be obtained using
 * {@link LoginI18n#createDefault()}
 *
 * @see LoginForm#setI18n(LoginI18n)
 */
public class JmixLoginI18n extends LoginI18n {

    protected JmixForm form;

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

    @Override
    public void setForm(Form form) {
        Preconditions.checkArgument(form instanceof JmixForm,
                "Passed value must be an instance of " + JmixForm.class.getSimpleName());
        this.form = ((JmixForm) form);
    }

    @Override
    public JmixForm getForm() {
        return form;
    }

    /**
     * A subclass of {@link Form} that adds additional functionality specific
     * to the customization of a login form used in the application.
     * This class introduces a `rememberMe` property, allowing the
     * configuration of a "Remember Me" option in the login form.
     */
    public static class JmixForm extends Form {

        protected String rememberMe;

        /**
         * Retrieves the value of the "Remember Me" property.
         *
         * @return the current value of the "Remember Me" property
         */
        public String getRememberMe() {
            return rememberMe;
        }

        /**
         * Sets the value of the "Remember Me" property.
         *
         * @param rememberMe the value to set for the "Remember Me" property
         */
        public void setRememberMe(String rememberMe) {
            this.rememberMe = rememberMe;
        }
    }
}

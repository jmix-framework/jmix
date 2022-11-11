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

    protected static final JsonValue JMIX_DEFAULT_I18N;

    /*
     * CAUTION! Copied from com.vaadin.flow.component.login.LoginI18n
     */
    static {
        try {
            final JsonFactory JSON_FACTORY = new JreJsonFactory();
            JMIX_DEFAULT_I18N = JSON_FACTORY.parse(
                    IOUtils.toString(LoginI18n.class.getResource("i18n.json"),
                            StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot find the default i18n configuration. "
                            + "Please make sure the i18n.json does exist.");
        }
    }

    public static JmixLoginI18n createDefault() {
        return JsonSerializer.toObject(JmixLoginI18n.class, JMIX_DEFAULT_I18N);
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

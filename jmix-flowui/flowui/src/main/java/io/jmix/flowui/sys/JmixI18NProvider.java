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

package io.jmix.flowui.sys;

import com.vaadin.flow.i18n.I18NProvider;
import io.jmix.core.CoreProperties;
import io.jmix.core.Messages;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component("flowui_JmixI18NProvider")
public class JmixI18NProvider implements I18NProvider {

    protected CoreProperties coreProperties;
    protected Messages messages;

    public JmixI18NProvider(CoreProperties coreProperties,
                            Messages messages) {
        this.coreProperties = coreProperties;
        this.messages = messages;
    }

    @Override
    public List<Locale> getProvidedLocales() {
        return coreProperties.getAvailableLocales();
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        if (params.length == 0) {
            return messages.getMessage(key, locale);
        } else {
            return messages.formatMessage(getGroup(key), getKey(key), locale, params);
        }
    }

    protected String getKey(String messageKey) {
        String[] keys = messageKey.split("/");
        if (keys.length == 2) {
            return keys[1];
        } else {
            return messageKey;
        }
    }

    protected String getGroup(String messageKey) {
        String[] keys = messageKey.split("/");
        if (keys.length == 2) {
            return keys[0];
        } else {
            return "";
        }
    }
}

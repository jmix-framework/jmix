/*
 * Copyright 2019 Haulmont.
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

package com.haulmont.cuba.core.global.impl;

import com.haulmont.cuba.core.global.Messages;
import io.jmix.core.MessageTools;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Locale;

public class MessagesImpl extends io.jmix.core.impl.MessagesImpl implements Messages {

    @Autowired
    protected MessageTools messageTools;

    @Autowired
    protected CubaMessages cubaMessages;

    @Override
    public MessageTools getTools() {
        return messageTools;
    }

    @Override
    public String getMainMessage(String key) {
        return getMessage(key);
    }

    @Override
    public String getMainMessage(String key, Locale locale) {
        return getMessage(key, locale);
    }

    @Override
    public String formatMainMessage(String key, Object... params) {
        return formatMessage("", key, params);
    }

    @Override
    protected String fallbackMessageOrKey(@Nullable String group, String key, Locale locale) {
        return group == null ?
                key :
                cubaMessages.getMessage(group, key, locale);
    }

    @Override
    public void clearCache() {
        super.clearCache();
        cubaMessages.clearCache();
    }

    @Override
    @Nullable
    protected String fallbackMessageOrNull(@Nullable String group, String key, Locale locale) {
        return group == null ?
                null :
                cubaMessages.findMessage(group, key, locale);
    }
}

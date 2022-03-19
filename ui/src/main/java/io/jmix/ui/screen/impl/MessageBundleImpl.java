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

package io.jmix.ui.screen.impl;

import com.google.common.base.Strings;
import io.jmix.core.Messages;
import io.jmix.ui.screen.MessageBundle;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component("ui_MessageBundle")
public class MessageBundleImpl implements MessageBundle {

    protected Messages messages;
    protected String messageGroup;

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Override
    public String getMessageGroup() {
        return messageGroup;
    }

    @Override
    public void setMessageGroup(String messageGroup) {
        this.messageGroup = messageGroup;
    }

    @Override
    public String getMessage(String key) {
        if (Strings.isNullOrEmpty(messageGroup)) {
            throw new IllegalStateException("messageGroup is not set");
        }

        return messages.getMessage(messageGroup, key);
    }

    @Override
    public String formatMessage(String key, Object... params) {
        if (Strings.isNullOrEmpty(messageGroup)) {
            throw new IllegalStateException("messageGroup is not set");
        }

        return messages.formatMessage(messageGroup, key, params);
    }
}
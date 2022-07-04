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

package io.jmix.flowui.view;

import com.google.common.base.Strings;
import io.jmix.core.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Class that provides messages from a message group bound to view controller.
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component("flowui_MessageBundle")
public class MessageBundle {

    protected Messages messages;
    protected String messageGroup;

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    /**
     * @return associated message group
     */
    public String getMessageGroup() {
        return messageGroup;
    }

    /**
     * Sets source message group.
     *
     * @param messageGroup associated message group
     */
    public void setMessageGroup(String messageGroup) {
        this.messageGroup = messageGroup;
    }

    /**
     * Gets localized message from the message group associated with this view.
     *
     * @param key message key
     * @return localized message
     * @see Messages#getMessage(String, String)
     */
    public String getMessage(String key) {
        if (Strings.isNullOrEmpty(messageGroup)) {
            throw new IllegalStateException("messageGroup is not set");
        }

        return messages.getMessage(messageGroup, key);
    }

    /**
     * Gets localized message from the message group associated with this view, and uses it as a format string
     * for parameters provided.
     *
     * @param key    message key
     * @param params parameter values
     * @return formatted string or the key in case of IllegalFormatException
     * @see Messages#formatMessage(String, String, Object...)
     */
    public String formatMessage(String key, Object... params) {
        if (Strings.isNullOrEmpty(messageGroup)) {
            throw new IllegalStateException("messageGroup is not set");
        }

        return messages.formatMessage(messageGroup, key, params);
    }
}
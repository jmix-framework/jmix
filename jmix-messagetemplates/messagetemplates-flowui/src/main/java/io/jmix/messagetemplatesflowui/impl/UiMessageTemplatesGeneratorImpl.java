/*
 * Copyright 2024 Haulmont.
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

package io.jmix.messagetemplatesflowui.impl;

import io.jmix.messagetemplates.MessageTemplatesGenerator;
import io.jmix.messagetemplates.entity.MessageTemplate;
import io.jmix.messagetemplatesflowui.UiMessageTemplatesGenerator;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("msgtmp_UiMessageTemplatesImpl")
public class UiMessageTemplatesGeneratorImpl implements UiMessageTemplatesGenerator {

    protected MessageTemplatesGenerator messageTemplatesGenerator;

    public UiMessageTemplatesGeneratorImpl(MessageTemplatesGenerator messageTemplatesGenerator) {
        this.messageTemplatesGenerator = messageTemplatesGenerator;
    }

    @Override
    public String generateMessage(String templateCode) {
        // TODO: kd, open dialog, request parameters

        return messageTemplatesGenerator.generateMessage(templateCode, Map.of());
    }

    @Override
    public String generateMessage(MessageTemplate template) {
        return messageTemplatesGenerator.generateMessage(template, Map.of());
    }
}

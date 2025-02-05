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

package io.jmix.messagetemplatesflowui.component.factory;

import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.messagetemplates.entity.MessageTemplateParameter;

/**
 * The generation context that is used in {@link MessageTemplateParameterGenerationStrategy}.
 * Used to generate UI component for a {@link MessageTemplateParameter}.
 *
 * @see MessageTemplateParameterGenerationStrategy
 */
public class MessageTemplateParameterGenerationContext extends ComponentGenerationContext {

    protected final MessageTemplateParameter messageTemplateParameter;

    public MessageTemplateParameterGenerationContext(MessageTemplateParameter messageTemplateParameter) {
        super(null, null);
        this.messageTemplateParameter = messageTemplateParameter;
    }

    /**
     * @return the parameter for which this context is defined
     */
    public MessageTemplateParameter getMessageTemplateParameter() {
        return messageTemplateParameter;
    }
}

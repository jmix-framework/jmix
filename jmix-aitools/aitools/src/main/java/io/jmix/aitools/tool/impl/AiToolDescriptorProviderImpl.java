/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitools.tool.impl;

import io.jmix.aitools.tool.AiToolDescriptor;
import io.jmix.aitools.tool.AiToolDescriptorProvider;
import io.jmix.aitools.tool.JmixAiTool;
import org.springframework.ai.tool.support.ToolUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Default {@link AiToolDescriptorProvider} that derives the tool name and description from the
 * method's Spring AI {@code @Tool} metadata.
 */
public class AiToolDescriptorProviderImpl implements AiToolDescriptorProvider {

    @Override
    public AiToolDescriptor getDescriptor(JmixAiTool tool, Method toolMethod) {
        return new AiToolDescriptor(
                ToolUtils.getToolName(toolMethod),
                ToolUtils.getToolDescription(toolMethod)
        );
    }
}

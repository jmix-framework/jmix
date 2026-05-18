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

package io.jmix.aitools.dataload.tool.impl;

import io.jmix.aitools.tool.AiToolDescriptor;
import io.jmix.aitools.tool.AiToolDescriptorProvider;
import io.jmix.aitools.dataload.tool.DataLoadAiTool;
import io.jmix.aitools.dataload.tool.DataLoadToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.ai.tool.support.ToolUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component("aitols_DataLoadToolCallbackProviderImpl")
public class DataLoadToolCallbackProviderImpl implements DataLoadToolCallbackProvider {

    @Autowired
    protected List<DataLoadAiTool> dataLoadAiTools;

    @Autowired
    protected AiToolDescriptorProvider aiToolDescriptorProvider;

    @Override
    public List<ToolCallback> getToolCallbacks() {
        List<ToolCallback> toolCallbacks = new ArrayList<>();

        for (DataLoadAiTool dataLoadAiTool : dataLoadAiTools) {
            Class<?> targetClass = AopUtils.isAopProxy(dataLoadAiTool)
                    ? AopUtils.getTargetClass(dataLoadAiTool)
                    : dataLoadAiTool.getClass();

            Stream.of(ReflectionUtils.getDeclaredMethods(targetClass))
                    .filter(ReflectionUtils.USER_DECLARED_METHODS::matches)
                    .filter(this::isToolAnnotatedMethod)
                    .map(toolMethod -> createToolCallback(dataLoadAiTool, toolMethod))
                    .forEach(toolCallbacks::add);
        }

        List<String> duplicateToolNames = ToolUtils.getDuplicateToolNames(toolCallbacks);
        if (!duplicateToolNames.isEmpty()) {
            throw new IllegalStateException("Multiple data load tools with the same name found: "
                    + String.join(", ", duplicateToolNames));
        }

        return List.copyOf(toolCallbacks);
    }

    protected ToolCallback createToolCallback(DataLoadAiTool dataLoadAiTool, Method toolMethod) {
        AiToolDescriptor descriptor = aiToolDescriptorProvider.getDescriptor(dataLoadAiTool, toolMethod);
        ToolDefinition toolDefinition = ToolDefinitions.builder(toolMethod)
                .name(descriptor.getName())
                .description(descriptor.getDescription())
                .build();

        return MethodToolCallback.builder()
                .toolDefinition(toolDefinition)
                .toolMetadata(ToolMetadata.from(toolMethod))
                .toolMethod(toolMethod)
                .toolObject(dataLoadAiTool)
                .toolCallResultConverter(ToolUtils.getToolCallResultConverter(toolMethod))
                .build();
    }

    protected boolean isToolAnnotatedMethod(Method method) {
        return AnnotationUtils.findAnnotation(method, Tool.class) != null;
    }
}

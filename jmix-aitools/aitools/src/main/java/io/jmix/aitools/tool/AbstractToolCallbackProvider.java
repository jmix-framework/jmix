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

package io.jmix.aitools.tool;

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
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractToolCallbackProvider<T extends JmixAiTool> {

    @Autowired
    protected AiToolDescriptorProvider aiToolDescriptorProvider;

    protected List<ToolCallback> getToolCallbacks(List<T> aiTools) {
        List<ToolCallback> toolCallbacks = new ArrayList<>();

        for (T aiTool : aiTools) {
            Class<?> targetClass = AopUtils.isAopProxy(aiTool)
                    ? AopUtils.getTargetClass(aiTool)
                    : aiTool.getClass();

            Stream.of(ReflectionUtils.getDeclaredMethods(targetClass))
                    .filter(ReflectionUtils.USER_DECLARED_METHODS::matches)
                    .filter(this::isToolAnnotatedMethod)
                    .map(toolMethod -> createToolCallback(aiTool, toolMethod))
                    .forEach(toolCallbacks::add);
        }

        List<String> duplicateToolNames = ToolUtils.getDuplicateToolNames(toolCallbacks);
        if (!duplicateToolNames.isEmpty()) {
            throw new IllegalStateException("Multiple AI tools with the same name found: "
                    + String.join(", ", duplicateToolNames));
        }

        return List.copyOf(toolCallbacks);
    }

    protected ToolCallback createToolCallback(T aiTool, Method toolMethod) {
        AiToolDescriptor descriptor = aiToolDescriptorProvider.getDescriptor(aiTool, toolMethod);
        ToolDefinition toolDefinition = ToolDefinitions.builder(toolMethod)
                .name(descriptor.getName())
                .description(descriptor.getDescription())
                .build();

        return MethodToolCallback.builder()
                .toolDefinition(toolDefinition)
                .toolMetadata(ToolMetadata.from(toolMethod))
                .toolMethod(toolMethod)
                .toolObject(aiTool)
                .toolCallResultConverter(ToolUtils.getToolCallResultConverter(toolMethod))
                .build();
    }

    protected boolean isToolAnnotatedMethod(Method method) {
        return AnnotationUtils.findAnnotation(method, Tool.class) != null;
    }
}

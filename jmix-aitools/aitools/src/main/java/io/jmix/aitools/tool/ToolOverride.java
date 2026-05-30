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

import io.jmix.core.annotation.Experimental;
import org.springframework.ai.tool.annotation.Tool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link Tool @Tool} method as a replacement for an existing AI tool with the given name.
 * <p>
 * The annotated method itself must also be annotated with {@link Tool @Tool} and declared on a bean
 * that implements {@link JmixAiTool}.
 * <p>
 * During tool registry assembly, when multiple {@code @Tool} methods produce the same tool name,
 * the method annotated with {@code @ToolOverride} wins and the original one is excluded from the registry.
 * <p>
 * If the referenced tool name is not present in the registry, a warning is logged and the override
 * is treated as a regular new tool. This fallback fails with an {@link IllegalStateException} when
 * the override's own {@link Tool#name() @Tool name} is already used by another tool, since the
 * fallback would otherwise silently replace an unrelated tool.
 *
 * <pre>
 * &#064;Component
 * public class MyTools implements DataLoadAiTool {
 *
 *     &#064;Tool(description = "Improved listing of available entities")
 *     &#064;ToolOverride("getAvailableEntities")
 *     public List<EntitySummary> customListing() { ... }
 * }
 * </pre>
 *
 * @see JmixAiTool
 * @see AiToolRegistry
 */
@Experimental
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ToolOverride {

    /**
     * Name of the existing tool that this method overrides. Must match the {@code name} value of
     * the original {@link Tool @Tool} method (or its derived name, if not explicitly set).
     */
    String value();
}

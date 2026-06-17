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

import java.util.List;
import java.util.Optional;

/**
 * Central registry of all AI tools available in the application.
 * <p>
 * The registry is built once at application startup from every Spring bean implementing
 * {@link JmixAiTool}. For each such bean the registry collects {@code @Tool}-annotated methods,
 * applies {@link ToolOverride @ToolOverride} resolution and produces a flat list of
 * {@link ResolvedAiTool} entries.
 */
public interface AiToolRegistry {

    /**
     * Returns all resolved tools in registration order.
     */
    List<ResolvedAiTool> getAll();

    /**
     * Returns a resolved tool by its name (the value passed to {@code @Tool(name=...)} or derived
     * from the method name), or empty when no such tool is registered.
     */
    Optional<ResolvedAiTool> findByName(String name);

    /**
     * Returns all resolved tools produced by beans implementing the given marker sub-interface of
     * {@link JmixAiTool} (e.g. {@code DataLoadAiTool.class}).
     */
    List<ResolvedAiTool> findByMarker(Class<? extends JmixAiTool> marker);

    /**
     * Shortcut returning {@link ToolCallback}s of all resolved tools in the same order as
     * {@link #getAll()}.
     */
    List<ToolCallback> getAllCallbacks();
}

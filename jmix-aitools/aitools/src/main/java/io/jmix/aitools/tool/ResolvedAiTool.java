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

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Resolved AI tool entry produced by {@link AiToolRegistry}. Represents a single {@code @Tool} method
 * after deduplication and {@link ToolOverride @ToolOverride} resolution.
 */
public class ResolvedAiTool {

    protected final String name;
    protected final String description;
    protected final ToolCallback callback;
    protected final JmixAiTool source;
    protected final Method method;
    protected final Set<Class<? extends JmixAiTool>> markers;

    public ResolvedAiTool(String name,
                          String description,
                          ToolCallback callback,
                          JmixAiTool source,
                          Method method,
                          Set<Class<? extends JmixAiTool>> markers) {
        this.name = name;
        this.description = description;
        this.callback = callback;
        this.source = source;
        this.method = method;
        this.markers = Set.copyOf(markers);
    }

    /**
     * Returns the tool name as seen by the model.
     *
     * @return tool name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the tool description as seen by the model.
     *
     * @return tool description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the callback that invokes the tool.
     *
     * @return tool callback
     */
    public ToolCallback getCallback() {
        return callback;
    }

    /**
     * Returns the bean that declares the tool method.
     *
     * @return source tool bean
     */
    public JmixAiTool getSource() {
        return source;
    }

    /**
     * Returns the {@code @Tool}-annotated method backing this tool.
     *
     * @return tool method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Marker sub-interfaces of {@link JmixAiTool} that the source bean implements. Used by registry
     * lookups such as {@link AiToolRegistry#findByMarker(Class)}.
     *
     * @return marker sub-interfaces implemented by the source bean
     */
    public Set<Class<? extends JmixAiTool>> getMarkers() {
        return markers;
    }
}

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

package io.jmix.aitools.dataload.tool;

import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationService;
import io.jmix.aitools.tool.AiToolRegistry;
import io.jmix.aitools.tool.JmixAiTool;

/**
 * Marker interface for Spring AI tools exposed to the entity-scoped data-load flow that
 * generates a JPQL query for a specific target entity.
 * <p>
 * Beans implementing this interface can be collected by {@link AiToolRegistry#findByMarker(Class)}.
 *
 * @see EntityDataLoadGenerationService
 */
public interface EntityDataLoadAiTool extends JmixAiTool {
}
